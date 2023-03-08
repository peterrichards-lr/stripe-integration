package com.liferay.sales.engineering.payment.method.stripe;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.stripe.exception.StripeException;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@Component(
        immediate = true,
        property = {
                "osgi.http.whiteboard.context.path=/",
                "osgi.http.whiteboard.servlet.pattern=/stripe/create-payment-intent"
        },
        service = Servlet.class
)

public class StripePaymentIntentServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    static class CreatePayment {
        @SerializedName("items")
        Object[] items;

        public Object[] getItems() {
            return items;
        }
    }

    static class CreatePaymentResponse {
        private String clientSecret;

        public CreatePaymentResponse(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    static int calculateOrderAmount(Object[] items) {
        // Replace this constant with a calculation of the order's amount
        // Calculate the order total on the server to prevent
        // users from directly manipulating the amount on the client
        return 1400;
    }

    @Override
    public void init() throws ServletException {
        _log.info("StripePaymentIntentServlet init");
        super.init();

        Stripe.apiKey = "pk_test_51MjI4xFpK5doxSlptPyuy3QatebCL91jXmDemLMkMOUmE4cfqbC1tXpxLfIAHbaVt9dsareCfdyCSzLF1ymXkeOV00YDBIhotl";
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            BufferedReader reader = request.getReader();
            CreatePayment postBody = gson.fromJson(reader, CreatePayment.class);
            PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                    .setCurrency("usd")
                    .setAmount((long) calculateOrderAmount(postBody.getItems()))
                    .build();
            // Create a PaymentIntent with the order amount and currency
            PaymentIntent intent = PaymentIntent.create(createParams);

            CreatePaymentResponse paymentResponse = new CreatePaymentResponse(intent.getClientSecret());

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(gson.toJson(paymentResponse));
            out.flush();
        } catch (IOException | StripeException e) {
            _log.error(e);
            throw new RuntimeException(e);
        }
    }

    private static final Log _log = LogFactoryUtil.getLog(
            StripePaymentIntentServlet.class);
}

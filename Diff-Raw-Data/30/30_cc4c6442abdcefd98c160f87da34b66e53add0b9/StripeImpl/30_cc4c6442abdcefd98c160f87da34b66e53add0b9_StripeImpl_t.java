 package com.arcbees.stripe.client;
 
 import com.google.gwt.core.client.Callback;
 import com.google.gwt.core.client.ScriptInjector;
 
 public class StripeImpl implements Stripe {
     private static final String STRIPE_JAVASCRIPT_URL = "https://js.stripe.com/v2/";
     private boolean injected;
 
     @Override
     public void inject(final Callback<Void, Exception> callback) {
         if (!injected) {
             ScriptInjector.fromUrl(STRIPE_JAVASCRIPT_URL).setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
                 @Override
                 public void onFailure(Exception reason) {
                     injected = false;
                     callback.onFailure(reason);
                 }
 
                 @Override
                 public void onSuccess(Void result) {
                     injected = true;
                     callback.onSuccess(result);
                 }
             })
                     .inject();
         }
     }
 
     @Override
     public void getStripeToken(CreditCard creditCard, ResponseHandler responseHandler) {
        getStripeToken(creditCard.getCreditCardNumber(),
                creditCard.getCvc(),
                creditCard.getExpirationMonth(),
                creditCard.getExpirationYear(),
                creditCard.getName(),
                creditCard.getAddressLine1(),
                creditCard.getAddressLine2(),
                creditCard.getAddressCity(),
                creditCard.getAddressState(),
                creditCard.getAddressZip(),
                creditCard.getAddressCountry(),
                 responseHandler);
     }
 
     @Override
     public native boolean validateCardNumber(String cardNumber) /*-{
         return $wnd.Stripe.card.validateCardNumber(cardNumber);
     }-*/;
 
     @Override
     public native boolean validateCardExpiry(String month, String year) /*-{
         return $wnd.Stripe.card.validateExpiry(month, year);
     }-*/;
 
     @Override
     public native boolean validateCardCvc(String cvc) /*-{
         return $wnd.Stripe.card.validateCVC(cvc);
     }-*/;
 
     @Override
     public native String getCardType(String cardNumber) /*-{
         return $wnd.Stripe.card.cardType(cardNumber);
     }-*/;
 
     @Override
     public boolean isInjected() {
         return injected;
     }
 
     @Override
     public native void setPublishableKey(String publishableKey) /*-{
         $wnd.Stripe.setPublishableKey(publishableKey);
     }-*/;
 
    private native void getStripeToken(String creditCardNumber,
                                       String cvc,
                                       int expiryMonth,
                                       int expiryYear,
                                       String name,
                                       String addressLine1,
                                       String addressLine2,
                                       String addressCity,
                                       String addressState,
                                       String addressZip,
                                       String addressCountry,
                                        ResponseHandler responseHandler) /*-{
         var creditCardInfo = {
             number: creditCardNumber,
             cvc: cvc,
             exp_month: expiryMonth,
             exp_year: expiryYear,
             name: name,
             address_line1: addressLine1,
             address_line2: addressLine2,
             address_city: addressCity,
             address_state: addressState,
             address_zip: addressZip,
             address_country: addressCountry
         };
 
         var createTokenCallback = function (status, response) {
             responseHandler.@com.arcbees.stripe.client.ResponseHandler::onTokenReceived(ILcom/arcbees/stripe/client/jso/Response;)(status, response);
         }
 
         $wnd.Stripe.card.createToken(creditCardInfo, createTokenCallback);
     }-*/;
 }

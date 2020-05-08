 /*
  * Copyright 2008-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.broadleafcommerce.vendor.authorizenet.web.controller;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
 import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
 import org.broadleafcommerce.core.order.domain.NullOrderImpl;
 import org.broadleafcommerce.core.order.domain.Order;
 import org.broadleafcommerce.core.payment.domain.PaymentInfo;
 import org.broadleafcommerce.core.payment.domain.PaymentResponseItem;
 import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
 import org.broadleafcommerce.core.pricing.service.exception.PricingException;
 import org.broadleafcommerce.vendor.authorizenet.service.payment.AuthorizeNetCheckoutService;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author elbertbautista
  */
 public class BroadleafAuthorizeNetController {
 
     private static final Log LOG = LogFactory.getLog(BroadleafAuthorizeNetController.class);
 
     @Resource(name="blAuthorizeNetCheckoutService")
     protected AuthorizeNetCheckoutService authorizeNetCheckoutService;
 
     @Value("${authorizenet.error.url}")
     protected String authorizeNetErrorUrl;
 
     @Value("${authorizenet.confirm.url}")
     protected String authorizeNetConfirmUrl;
 
     public @ResponseBody String processAuthorizeNetAuthorizeAndDebit(HttpServletRequest request, HttpServletResponse response, Model model) throws NoSuchAlgorithmException, PricingException, InvalidKeyException, UnsupportedEncodingException, BroadleafAuthorizeNetException {
     	LOG.debug("Authorize URL request - "+request.getRequestURL().toString());
     	LOG.debug("Authorize Request Parameter Map (params: [" + requestParamToString(request) + "])");
         Order order = authorizeNetCheckoutService.findCartForCustomer(request.getParameterMap());
         if (order != null && !(order instanceof NullOrderImpl)) {
             try {
 
                 CheckoutResponse checkoutResponse = authorizeNetCheckoutService.completeAuthorizeAndDebitCheckout(order, request.getParameterMap());
 
                 PaymentInfo authorizeNetPaymentInfo = null;
                 for (PaymentInfo paymentInfo : checkoutResponse.getPaymentResponse().getResponseItems().keySet()){
                     if (PaymentInfoType.CREDIT_CARD.equals(paymentInfo.getType())){
                         authorizeNetPaymentInfo = paymentInfo;
                     }
                 }
 
                 PaymentResponseItem paymentResponseItem = checkoutResponse.getPaymentResponse().getResponseItems().get(authorizeNetPaymentInfo);
                 if (paymentResponseItem.getTransactionSuccess()){
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Transaction success for order " + checkoutResponse.getOrder().getOrderNumber());
                         LOG.debug("Response for Authorize.net to relay to client: ");
                         LOG.debug(authorizeNetCheckoutService.buildRelayResponse(authorizeNetConfirmUrl + "/" + checkoutResponse.getOrder().getOrderNumber()));
                     }
                     return authorizeNetCheckoutService.buildRelayResponse(authorizeNetConfirmUrl + "/" + checkoutResponse.getOrder().getOrderNumber());
                 }
                 
             } catch (CheckoutException e) {
                 if (LOG.isErrorEnabled()) {
                     LOG.error("Checkout Exception occurred processing Authorize.net relay response (params: [" + requestParamToString(request) + "])" + e);
                 }
             }
         } else {
             if (LOG.isFatalEnabled()) {
                 LOG.fatal("The order could not be determined from the Authorize.net relay response (params: [" + requestParamToString(request) + "]). NOTE: The transaction may have completed successfully. Check your application keys and hash.");
                 throw new BroadleafAuthorizeNetException("Fatal Error has occured with in BroadleafAuthorizeNet");
             }
         }
         
         return authorizeNetCheckoutService.buildRelayResponse(authorizeNetErrorUrl);
     }
 
 
     protected String requestParamToString(HttpServletRequest request) {
         StringBuffer requestMap = new StringBuffer();
         for (String key : (Set<String>)request.getParameterMap().keySet()) {
             requestMap.append(key + ": " + request.getParameter(key) + ", ");
         }
         return requestMap.toString();
     }
     
 }

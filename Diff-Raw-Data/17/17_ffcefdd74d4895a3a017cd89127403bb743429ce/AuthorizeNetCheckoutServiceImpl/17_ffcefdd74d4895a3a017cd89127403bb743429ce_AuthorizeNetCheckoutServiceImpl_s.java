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
 
 package org.broadleafcommerce.vendor.authorizenet.service.payment;
 
 import net.authorize.ResponseField;
 import net.authorize.sim.Fingerprint;
 import net.authorize.sim.Result;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.broadleafcommerce.core.checkout.service.CheckoutService;
 import org.broadleafcommerce.core.checkout.service.exception.CheckoutException;
 import org.broadleafcommerce.core.checkout.service.workflow.CheckoutResponse;
 import org.broadleafcommerce.core.order.domain.Order;
 import org.broadleafcommerce.core.order.service.OrderService;
 import org.broadleafcommerce.core.payment.domain.CreditCardPaymentInfo;
 import org.broadleafcommerce.core.payment.domain.PaymentInfo;
 import org.broadleafcommerce.core.payment.domain.Referenced;
 import org.broadleafcommerce.core.payment.service.PaymentInfoService;
 import org.broadleafcommerce.core.payment.service.SecurePaymentInfoService;
 import org.broadleafcommerce.core.payment.service.type.PaymentInfoType;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.Resource;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
 import java.security.InvalidKeyException;
import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author elbertbautista
  */
 @Service("blAuthorizeNetCheckoutService")
 public class AuthorizeNetCheckoutServiceImpl implements AuthorizeNetCheckoutService {
 
     private static final Log LOG = LogFactory.getLog(AuthorizeNetCheckoutServiceImpl.class);
     public static final String BLC_CID = "blc_cid";
     public static final String BLC_OID = "blc_oid";
     public static final String BLC_TPS = "blc_tps";
 
     @Resource(name="blSecurePaymentInfoService")
     protected SecurePaymentInfoService securePaymentInfoService;
 
     @Resource(name="blPaymentInfoService")
     protected PaymentInfoService paymentInfoService;
 
     @Resource(name = "blAuthorizeNetVendorOrientedPaymentService")
     protected AuthorizeNetPaymentService authorizeNetPaymentService;
 
     @Resource(name="blCheckoutService")
     protected CheckoutService checkoutService;
 
     @Resource(name="blOrderService")
     protected OrderService orderService;
 
     @Override
     public Order findCartForCustomer(Map<String, String[]> responseMap) throws InvalidKeyException, NoSuchAlgorithmException {
         Result result = authorizeNetPaymentService.createResult(responseMap);
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Result Reason Text - " + result.getResponseMap().get(ResponseField.RESPONSE_REASON_TEXT.getFieldName()));
         }
 
         if (result.isAuthorizeNet()){
             Long customerId = Long.parseLong(result.getResponseMap().get(BLC_CID));
             Long orderId = Long.parseLong(result.getResponseMap().get(BLC_OID));
             String formTps = result.getResponseMap().get(BLC_TPS);
             String tps = createTamperProofSeal(customerId, orderId);
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Customer ID - " + customerId);
                 LOG.debug("Order ID - " + orderId);
                 LOG.debug("Form tps - " + formTps);
                 LOG.debug("tps - " + tps);
             }
 
             if (tps.equals(formTps)) {
                 Order order = orderService.findOrderById(orderId);
                 if (order != null && order.getCustomer().getId().equals(customerId)){
                     return order;
                 }
             }
         }
 
         return null;
     }
 
     @Override
     public CheckoutResponse completeAuthorizeAndDebitCheckout(Order order, Map<String, String[]> responseMap) throws CheckoutException {
          Map<PaymentInfo, Referenced> payments = new HashMap<PaymentInfo, Referenced>();
 
         //NOTE: assumes only one payment info of type credit card on the order.
         //Start by removing any payment info of type credit card already on the order.
         orderService.removePaymentsFromOrder(order, PaymentInfoType.CREDIT_CARD);
 
         PaymentInfo authorizeNetPaymentInfo = paymentInfoService.create();
         authorizeNetPaymentInfo.setOrder(order);
         authorizeNetPaymentInfo.setType(PaymentInfoType.CREDIT_CARD);
         authorizeNetPaymentInfo.setReferenceNumber(order.getOrderNumber());
         authorizeNetPaymentInfo.setRequestParameterMap(responseMap);
 
         //finally add the authorizenet payment info to the order
         order.getPaymentInfos().add(authorizeNetPaymentInfo);
 
         CreditCardPaymentInfo creditCardPaymentInfo = ((CreditCardPaymentInfo) securePaymentInfoService.create(PaymentInfoType.CREDIT_CARD));
         creditCardPaymentInfo.setReferenceNumber(authorizeNetPaymentInfo.getReferenceNumber());
         payments.put(authorizeNetPaymentInfo, creditCardPaymentInfo);
 
         return checkoutService.performCheckout(order, payments);
     }
 
     @Override
     public Map<String, String> constructAuthorizeAndDebitFields(Order order) throws InvalidKeyException, NoSuchAlgorithmException {
         if (order != null) {
             String apiLoginId = authorizeNetPaymentService.getGatewayRequest().getApiLoginId();
             String transactionKey = authorizeNetPaymentService.getGatewayRequest().getTransactionKey();
             String relayResponseURL = authorizeNetPaymentService.getGatewayRequest().getRelayResponseUrl();
             String merchantTransactionVersion = authorizeNetPaymentService.getGatewayRequest().getMerchantTransactionVersion();
             String xTestRequest = authorizeNetPaymentService.getGatewayRequest().getxTestRequest();
             String serverUrl = authorizeNetPaymentService.getGatewayRequest().getServerUrl();
 
             Fingerprint fingerprint = Fingerprint.createFingerprint(apiLoginId, transactionKey, System.currentTimeMillis(), order.getTotal().toString());
             Map<String, String> formFields = new HashMap<String, String>();
             formFields.put("x_invoice_num", System.currentTimeMillis()+"");
             formFields.put("x_relay_url", relayResponseURL);
             formFields.put("x_login", apiLoginId);
             formFields.put("x_fp_sequence", fingerprint.getSequence()+"");
             formFields.put("x_fp_timestamp", fingerprint.getTimeStamp()+"");
             formFields.put("x_fp_hash", fingerprint.getFingerprintHash());
             formFields.put("x_version", merchantTransactionVersion);
             formFields.put("x_method", "CC");
             formFields.put("x_type", "AUTH_CAPTURE");
             formFields.put("x_amount", order.getTotal().toString());
             formFields.put("x_test_request", xTestRequest);
 
             formFields.put(BLC_CID, order.getCustomer().getId().toString());
             formFields.put(BLC_OID, order.getId().toString());
             formFields.put(BLC_TPS, createTamperProofSeal(order.getCustomer().getId(), order.getId()));
 
             formFields.put("authorizenet_server_url", serverUrl);
 
             return formFields;
         }
 
         return null;
     }
 
     @Override
     public String buildRelayResponse (String receiptUrl) {
         StringBuffer response = new StringBuffer();
         response.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n \"http://www.w3.org/TR/html4/loose.dtd\">");
         response.append("<html>");
         response.append("<head>");
         response.append("</head>");
         response.append("<body>");
         response.append("<script type=\"text/javascript\">");
         response.append("var referrer = document.referrer;");
         response.append("if (referrer.substr(0,7)==\"http://\") referrer = referrer.substr(7);");
         response.append("if (referrer.substr(0,8)==\"https://\") referrer = referrer.substr(8);");
         response.append("if(referrer && referrer.indexOf(document.location.hostname) != 0) {");
         response.append("document.location = \"" + receiptUrl +"\";");
         response.append("}");
         response.append("</script>");
         response.append("<noscript>");
         response.append("<meta http-equiv=\"refresh\" content=\"0;url=" + receiptUrl + "\">");
         response.append("</noscript>");
         response.append("</body>");
         response.append("</html>");
 
         return response.toString();
     }
 
     @Override
     public String createTamperProofSeal(Long customerId, Long orderId) throws NoSuchAlgorithmException, InvalidKeyException {
         String transactionKey = authorizeNetPaymentService.getGatewayRequest().getTransactionKey();
 
         Base64 encoder = new Base64();
         Mac sha1Mac = Mac.getInstance("HmacSHA1");
         SecretKeySpec publicKeySpec = new SecretKeySpec(transactionKey.getBytes(), "HmacSHA1");
         sha1Mac.init(publicKeySpec);
         String customerOrderString = customerId.toString() + orderId.toString();
         byte[] publicBytes = sha1Mac.doFinal(customerOrderString.getBytes());
         String publicDigest = encoder.encodeToString(publicBytes);
         return publicDigest.replaceAll("\\r|\\n", "");
 
     }
 
 }

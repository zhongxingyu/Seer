 package com.abudko.reseller.huuto.mvc.order;
 
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         import java.util.Locale;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import nl.captcha.Captcha;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.abudko.reseller.huuto.notification.email.order.OrderConfirmationEmailSender;
 import com.abudko.reseller.huuto.notification.email.order.OrderEmailSender;
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 
 @Controller
 @SessionAttributes({ SEARCH_RESULTS_ATTRIBUTE, SEARCH_PARAMS_ATTRIBUTE })
 public class OrderController {
 
     private static final String ITEM_ORDER_ATTRIBUTE = "itemOrder";
     private static final String CAPTCHA_ATTRIBUTE = "captcha";
 
     private static final String ORDER_FAILED_PATH = "order/orderForm";
 
     private static final String ORDER_SUCCESS_PATH = "order/orderSuccess";
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private ApplicationContext context;
 
     @Resource
     private OrderEmailSender orderEmailSender;
 
     @Resource
     private OrderConfirmationEmailSender orderConfirmationEmailSender;
 
     @RequestMapping(value = "/order", method = RequestMethod.POST)
     public String order(@ModelAttribute(ITEM_ORDER_ATTRIBUTE) @Valid ItemOrder itemOrder, BindingResult result,
             HttpSession session) {
 
         log.info(String.format("Handling POST order request. Order %s", itemOrder));
 
         validateCaptcha(itemOrder, result, session);
 
         if (result.hasErrors()) {
             return ORDER_FAILED_PATH;
         }
 
         String path = sendEmails(itemOrder, result);
 
         return path;
     }
 
     private String sendEmails(ItemOrder itemOrder, BindingResult result) {
         try {
             orderEmailSender.send(itemOrder);
         } catch (EmailNotificationException e) {
             handleEmailException(e, result);
             return ORDER_FAILED_PATH;
         }
 
         try {
             orderConfirmationEmailSender.send(itemOrder);
         } catch (EmailNotificationException e) {
             log.error("Exception happened while sending an email order confirmation", e);
         }
 
         return ORDER_SUCCESS_PATH;
     }
 
     private void validateCaptcha(ItemOrder itemOrder, BindingResult result, HttpSession session) {
         Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
         if (captcha == null || captcha.isCorrect(itemOrder.getCaptcha()) == false) {
             String invalidCaptchaMessage = context.getMessage("captcha.invalid", null, Locale.getDefault());
             result.addError(new FieldError(ITEM_ORDER_ATTRIBUTE, CAPTCHA_ATTRIBUTE, invalidCaptchaMessage));
         }
     }
 
     private void handleEmailException(EmailNotificationException e, BindingResult result) {
         log.error("Exception happened while sending an email order", e);
         String sendEmailFailedMessage = context.getMessage("send_email.failed", null, Locale.getDefault());
         result.addError(new ObjectError(ITEM_ORDER_ATTRIBUTE, sendEmailFailedMessage));
     }
 }

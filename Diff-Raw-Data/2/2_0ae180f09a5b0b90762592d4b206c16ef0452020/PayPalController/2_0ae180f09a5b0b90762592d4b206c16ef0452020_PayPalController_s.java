 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.Operation;
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.*;
 import com.financial.pyramid.service.beans.PayPalDetails;
 import com.financial.pyramid.settings.Setting;
 import com.financial.pyramid.utils.Session;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import java.math.BigDecimal;
 import java.util.*;
 
 /**
  * User: dbudunov
  * Date: 14.08.13
  * Time: 19:53
  */
 @Controller
 @RequestMapping("/paypal")
 public class PayPalController extends AbstractController {
 
     @Autowired
     UserService userService;
 
     @Autowired
     PayPalService payPalService;
 
     @Autowired
     PaymentsService paymentsService;
 
     @Autowired
     SettingsService settingsService;
 
     @Autowired
     OperationsService operationsService;
 
     @Autowired
     LocalizationService localizationService;
 
     @Autowired
     ApplicationConfigurationService configurationService;
 
     @RequestMapping(value = "/buyOfficeAndApp", method = RequestMethod.GET)
     public String buyOfficeAndApp(ModelMap model) {
         PayPalDetails details = new PayPalDetails();
         payPalService.updatePayPalDetails(details);
         String applicationURL = settingsService.getProperty(Setting.APPLICATION_URL);
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         String applicationPrice = settingsService.getProperty(Setting.APPLICATION_PRICE);
         details.currencySign = settingsService.getProperty(Setting.CASH_SIGN);
         Double totalPrice = Double.valueOf(officePrice) + Double.valueOf(applicationPrice);
         details.receiverEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.cancelUrl = applicationURL + "/paypal/buyOfficeAndApp";
         details.returnUrl = applicationURL + "/paypal/success";
         details.notifyUrl = applicationURL + "/paypal/notify";
         details.amount = totalPrice.toString();
         model.addAttribute("payPalDetails", details);
         return "tabs/user/buy-office";
     }
 
     @RequestMapping(value = "/payOfficeAndApp", method = RequestMethod.POST)
     public String payOfficeAndApp(ModelMap model, @ModelAttribute("payPalDetails") PayPalDetails details) {
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         String applicationPrice = settingsService.getProperty(Setting.APPLICATION_PRICE);
         details.months = details.months > 12 ? 12 : details.months;
         Double totalPrice = Double.valueOf(officePrice) * details.months + Double.valueOf(applicationPrice);
         details.amount = totalPrice.toString();
         details.memo = localizationService.translate("paymentOfficeAndApp");
         String redirectURL = payPalService.processPayment(details);
         return "redirect:" + redirectURL;
     }
 
     @RequestMapping(value = "/buyOffice", method = RequestMethod.GET)
     public String buyOffice(ModelMap model) {
         PayPalDetails details = new PayPalDetails();
         payPalService.updatePayPalDetails(details);
         String applicationURL = settingsService.getProperty(Setting.APPLICATION_URL);
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         details.currencySign = settingsService.getProperty(Setting.CASH_SIGN);
         details.receiverEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.cancelUrl = applicationURL + "/paypal/buyOffice";
         details.returnUrl = applicationURL + "/paypal/success";
         details.notifyUrl = applicationURL + "/paypal/notify";
         details.amount = officePrice;
         model.addAttribute("payPalDetails", details);
         return "tabs/user/pay-office";
     }
 
     @RequestMapping(value = "/payOffice", method = RequestMethod.POST)
     public String payOffice(ModelMap model, @ModelAttribute("payPalDetails") PayPalDetails details) {
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         details.months = details.months > 12 ? 12 : details.months;
         Double totalPrice = Double.valueOf(officePrice) * details.months;
         details.amount = totalPrice.toString();
         details.memo = localizationService.translate("paymentOffice");
         String redirectURL = payPalService.processPayment(details);
         return "redirect:" + redirectURL;
     }
 
     @RequestMapping(value = "/sendMoney", method = RequestMethod.GET)
     public String sendMoney(ModelMap model) {
         PayPalDetails details = new PayPalDetails();
         payPalService.updatePayPalDetails(details);
         String maxAllowedAmount = settingsService.getProperty(Setting.MAX_ALLOWED_TRANSFER_AMOUNT_PER_DAY);
         String applicationURL = settingsService.getProperty(Setting.APPLICATION_URL);
         details.currencySign = settingsService.getProperty(Setting.CASH_SIGN);
         details.senderEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.amount = "0.00";
         details.cancelUrl = applicationURL + "/paypal/sendMoney";
         details.returnUrl = applicationURL + "/pyramid/office";
         com.financial.pyramid.domain.User currentUser = Session.getCurrentUser();
         details.receiverEmail = currentUser.getEmail();
         model.addAttribute("payPalDetails", details);
         model.addAttribute("maxAllowedAmount", maxAllowedAmount);
         return "tabs/user/send-money";
     }
 
     @RequestMapping(value = "/sendFunds", method = RequestMethod.POST)
     public String sendFunds(RedirectAttributes redirectAttributes,
                             ModelMap model,
                             @ModelAttribute("payPalDetails") PayPalDetails details) {
         com.financial.pyramid.domain.User user = Session.getCurrentUser();
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         Date date = calendar.getTime();
         Double transferSum = Double.valueOf(details.getAmount());
         Double balance = userService.getAccountDetails(user).getBalance();
         Double maxAllowedSum = paymentsService.allowedToBeTransferred(date, user.getId());
         boolean isTransferAllowed = maxAllowedSum > 0 && transferSum <= maxAllowedSum && transferSum <= balance;
         if (isTransferAllowed) {
             details.memo = localizationService.translate("moneyTransfer");
             boolean result = payPalService.processTransfer(details);
             if (result) {
                 userService.withdrawFromAccount(user, transferSum);
                 redirectAttributes.addFlashAttribute(AlertType.SUCCESS.getName(), localizationService.translate("operationSuccess"));
             } else {
                 redirectAttributes.addFlashAttribute(AlertType.ERROR.getName(), localizationService.translate("operationFailed"));
             }
         } else {
             if (transferSum > balance) {
                 redirectAttributes.addAttribute("error", "not_enough_money");
             } else if (maxAllowedSum == 0) {
                 redirectAttributes.addAttribute("error", "limit_reached");
             } else {
                 redirectAttributes.addAttribute("error", "not_allowed_to_be_transferred");
                 redirectAttributes.addAttribute("transfer_sum", new BigDecimal(maxAllowedSum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
             }
             return "redirect:" + details.cancelUrl;
         }
         return "redirect:" + details.returnUrl;
     }
 
     @RequestMapping(value = "/success", method = RequestMethod.GET)
     public String success(RedirectAttributes redirectAttributes, ModelMap model) {
         redirectAttributes.addFlashAttribute(AlertType.SUCCESS.getName(), localizationService.translate("thanksForPayment"));
         return "redirect:/pyramid/office";
     }
 
     @RequestMapping(value = "/notify", method = RequestMethod.POST)
     public void notify(HttpServletRequest request) {
         logger.info("Notification messages listener has been invoked...");
         Map<String, String> params = new HashMap<String, String>();
         Enumeration<String> names = request.getParameterNames();
         while (names.hasMoreElements()) {
             String param = names.nextElement();
             String value = request.getParameter(param);
             params.put(param, value);
         }
         String transactionId = params.get("txn_id");
         String payerEmail = params.get("payer_email");
         logger.info("IPN notification for transaction " + transactionId + " has been received from PayPal");
         if (transactionId != null) {
             boolean verified = payPalService.verifyNotification(params);
             logger.info("Transaction " + transactionId + " has been verified (" + verified + ")");
             if (verified) {
                 Operation operation = operationsService.findByTransactionId(transactionId);
                 User user = userService.findByEmail(payerEmail);
                 userService.activateUserAccount(user, operation.getMonthsPayed());
             }
         }
     }
 }

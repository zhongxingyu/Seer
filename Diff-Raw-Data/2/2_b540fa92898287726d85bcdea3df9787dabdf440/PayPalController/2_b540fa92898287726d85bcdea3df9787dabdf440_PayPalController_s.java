 package com.financial.pyramid.web;
 
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
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 
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
 
     @RequestMapping(value="/buyOfficeAndApp", method = RequestMethod.GET)
     public String buyOfficeAndApp(ModelMap model){
         PayPalDetails details = new PayPalDetails();
         payPalService.updatePayPalDetails(details);
         String applicationURL = settingsService.getProperty(Setting.APPLICATION_URL);
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         String applicationPrice = settingsService.getProperty(Setting.APPLICATION_PRICE);
         Double totalPrice = Double.valueOf(officePrice) + Double.valueOf(applicationPrice);
         details.receiverEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.cancelUrl = applicationURL + "/paypal/payment";
         details.returnUrl = applicationURL + "/paypal/success";
         details.amount = totalPrice.toString();
         model.addAttribute("payPalDetails", details);
         return "tabs/user/buy-office";
     }
 
     @RequestMapping(value = "/payOfficeAndApp", method = RequestMethod.POST)
     public String payOfficeAndApp(ModelMap model, @ModelAttribute("payPalDetails") PayPalDetails details) {
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         String applicationPrice = settingsService.getProperty(Setting.APPLICATION_PRICE);
         Double totalPrice = Double.valueOf(officePrice) + Double.valueOf(applicationPrice);
         if (!totalPrice.toString().equals(details.amount)) {
             details.amount = totalPrice.toString();
         }
         details.memo = localizationService.translate("paymentOffice");
         String redirectURL = payPalService.processPayment(details);
         return "redirect:" + redirectURL;
     }
 
     @RequestMapping(value = "/buyOffice", method = RequestMethod.GET)
     public String buyOffice(ModelMap model) {
         PayPalDetails details = new PayPalDetails();
         payPalService.updatePayPalDetails(details);
         String applicationURL = settingsService.getProperty(Setting.APPLICATION_URL);
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         details.receiverEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.cancelUrl = applicationURL + "/paypal/payment";
         details.returnUrl = applicationURL + "/paypal/success";
         details.amount = officePrice;
         model.addAttribute("payPalDetails", details);
         return "tabs/user/pay-office";
     }
 
     @RequestMapping(value = "/payOffice", method = RequestMethod.POST)
     public String payOffice(ModelMap model, @ModelAttribute("payPalDetails") PayPalDetails details) {
         String officePrice = settingsService.getProperty(Setting.OFFICE_PRICE);
         if (!officePrice.equals(details.amount)) {
             details.amount = officePrice;
         }
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
         details.senderEmail = configurationService.getParameter(Setting.PAY_PAL_LOGIN);
         details.amount = maxAllowedAmount;
        details.cancelUrl = applicationURL + "/paypal/transferMoney";
         details.returnUrl = applicationURL + "/paypal/success";
         com.financial.pyramid.domain.User currentUser = Session.getCurrentUser();
         details.receiverEmail = currentUser.getEmail();
         model.addAttribute("payPalDetails", details);
         model.addAttribute("maxAllowedAmount", maxAllowedAmount);
         return "tabs/user/send-money";
     }
 
     @RequestMapping(value = "/sendFunds", method = RequestMethod.POST)
     public String sendFunds(ModelMap model, @ModelAttribute("payPalDetails") PayPalDetails details) {
         com.financial.pyramid.domain.User user = Session.getCurrentUser();
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         calendar.set(Calendar.HOUR, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         Date date = calendar.getTime();
         Double transferSum = Double.valueOf(details.getAmount());
         Double earningsSum = userService.getAccountDetails(user).getEarningsSum();
         Double allowedSum = paymentsService.allowedToBeTransferred(date, user.getId());
         boolean isTransferAllowed = allowedSum > 0 &&  transferSum <= allowedSum && transferSum <= earningsSum;
         if (isTransferAllowed) {
             details.memo = localizationService.translate("moneyTransfer");
             payPalService.processTransfer(details);
         } else {
             if (transferSum > earningsSum){
               model.addAttribute("error", "not_enough_money");
             } else if (allowedSum == 0) {
                 model.addAttribute("error", "limit_reached");
             } else {
                 model.addAttribute("error","not_allowed_to_be_transferred");
                 model.addAttribute("transfer_sum", new BigDecimal(allowedSum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
             }
             return "redirect:" + details.cancelUrl;
         }
         return "redirect:" + details.returnUrl;
     }
 
     @RequestMapping(value="/success", method = RequestMethod.GET)
     public String success(ModelMap model){
         model.addAttribute(AlertType.SUCCESS.getName(), localizationService.translate("successfulPayment"));
         return "redirect:/pyramid/office";
     }
 }

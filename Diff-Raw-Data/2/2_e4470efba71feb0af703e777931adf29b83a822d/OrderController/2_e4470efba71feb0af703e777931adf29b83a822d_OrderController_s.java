 package com.abudko.reseller.huuto.mvc.order;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import nl.captcha.Captcha;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.abudko.reseller.huuto.notification.email.order.OrderEmailSender;
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 
 @Controller
 public class OrderController {
 
 	private Log log = LogFactory.getLog(getClass());
 	
 	@Resource
 	private OrderEmailSender orderEmailSender;
 
 	@RequestMapping(value = "/order", method = RequestMethod.POST)
 	public String order(@ModelAttribute("itemOrder") @Valid ItemOrder itemOrder, Model model, BindingResult result, HttpSession session) {
 		
 	    log.info(String.format("Handling POST order request. Order %s", itemOrder));
 	    
 	    validateCaptcha(itemOrder, result, session);
 	    
 	    if (result.hasErrors()) {
 	        model.addAttribute("itemOrder", itemOrder);
             return "item";
         }
 		
 	    try {
 	        orderEmailSender.sendOrder(itemOrder);
 	    }
 	    catch (EmailNotificationException e) {
 	        log.error("Exception happened while sending an email order");
 	        return "order/orderForm";
 	    }
 		
 		return "order/orderSuccess";
 	}
 	
 	private void validateCaptcha(ItemOrder itemOrder, BindingResult result, HttpSession session) {
 	    Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
         if (captcha == null || captcha.isCorrect(itemOrder.getCaptcha()) == false) {
            result.addError(new ObjectError("captcha", "Invalid image characters"));
         }
 	}
 }

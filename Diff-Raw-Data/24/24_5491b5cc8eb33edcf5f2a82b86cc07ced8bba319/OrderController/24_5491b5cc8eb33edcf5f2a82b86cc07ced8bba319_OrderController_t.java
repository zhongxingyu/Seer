 package com.abudko.reseller.huuto.mvc.order;
 
 import javax.validation.Valid;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.abudko.reseller.huuto.order.ItemOrder;
 
 @Controller
 public class OrderController {
 
 	private Log log = LogFactory.getLog(getClass());
 
 	@RequestMapping(value = "/order", method = RequestMethod.POST)
 	public String order(@ModelAttribute("itemOrder") @Valid ItemOrder itemOrder, BindingResult result) {
 		log.info("");
		return "order/orderSuccess";
 	}
 }

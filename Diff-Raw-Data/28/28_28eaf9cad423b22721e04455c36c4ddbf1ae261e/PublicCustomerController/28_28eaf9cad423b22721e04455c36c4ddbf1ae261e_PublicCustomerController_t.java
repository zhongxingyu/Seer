 package com.nanhua.trading.web.ui.customer;
 
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.nanhua.trading.domain.customer.Customer;
 import com.nanhua.trading.domain.servicesupplier.ServiceSupplier;
 
 @Controller
 @RequestMapping("/public/customer")
 public class PublicCustomerController {
 	
 	@ResponseStatus(value = HttpStatus.OK)
 	@RequestMapping(value = "correlator",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
     public ModelAndView getCustomerCorrelators(@RequestParam String username,@RequestParam String password) {
 		ModelAndView mav = new ModelAndView("getCorrelatorJsonView");
 		mav.addObject("correlators",Customer.findCustomerByUniqueIdentify(username).getCorrelators());
 		return mav;
     }
 	@ResponseStatus(value = HttpStatus.OK)
 	@RequestMapping(value = "serviceSupplier",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getAllSuppliers(@RequestParam String username,@RequestParam String password,HttpServletRequest req,HttpServletResponse res) {
		Enumeration<String> enums = req.getHeaderNames();
		while(enums.hasMoreElements()){
			String name = enums.nextElement();
			System.out.print(name+":");
			System.out.println(req.getHeader(name));
		}
		
 		ModelAndView mav = new ModelAndView("getSuppliersJsonView");
 		mav.addObject("suppliers",ServiceSupplier.findAllServiceSuppliers());
 		return mav;
     }
 	
 	
 }

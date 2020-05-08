 package com.euroit.militaryshop.web.controller;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.euroit.militaryshop.web.form.LoginForm;
 
 /**
  * @author EuroITConsulting
  */
 @Controller
 public class EndUserController {
 	private static final Logger LOG = LoggerFactory.getLogger(EndUserController.class.getName());
 	
     @Autowired
     @Qualifier("endUserAuthenticationManager")
     private AuthenticationManager authenticationManager;
     
     @RequestMapping(value = "/register/for/order", method = RequestMethod.POST)
     public ModelAndView registerForOrder() {
     	ModelAndView mav = new ModelAndView("redirect:/order/payment");
     	        
     	return mav;
     }
     
     @RequestMapping(value = "/login/for/order", method = RequestMethod.POST)
     public ModelAndView loginForOrder(@Valid LoginForm loginForm, BindingResult bindingResult, HttpSession session) {
     	ModelAndView mav = new ModelAndView("redirect:/order/payment");
    	LOG.debug("email: {}, password: {}", loginForm.getEmail(), loginForm.getPassword());
     	
     	Authentication request = new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword());
     	LOG.debug("auth request: {}", request);
         Authentication result = authenticationManager.authenticate(request);
         LOG.debug("auth result: {}", result);
         SecurityContextHolder.getContext().setAuthentication(result);
         //TODO figure out how to do that with the filters
         session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
     	return mav;
     }
 }

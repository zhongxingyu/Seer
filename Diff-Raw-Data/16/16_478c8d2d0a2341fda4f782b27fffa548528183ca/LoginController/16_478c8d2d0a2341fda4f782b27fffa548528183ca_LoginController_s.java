 package com.potatorental.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Created with IntelliJ IDEA.
  * User: milky
  * Date: 5/1/13
  * Time: 5:10 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/login*")
@SessionAttributes("login")
 public class LoginController {
 
     @RequestMapping(value = "login", method = RequestMethod.GET)
     public String defaultPage() {
         return "login";
     }
 
     @RequestMapping(value = "loginfailed", method = RequestMethod.GET)
     public ModelAndView failLogin() {
         return new ModelAndView("login", "message", "errors bro");
     }
 }

 package com.thoughtworks.twu.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 
 
 @Controller
 public class HomeController {
 
     @RequestMapping(value = "", method = RequestMethod.GET)
     public ModelAndView homepage() {
         ModelAndView modelAndView = new ModelAndView("redirect:home.html");
 
         return modelAndView;
     }
 
     @RequestMapping(value = "/home.htm*", method = RequestMethod.GET)
     public ModelAndView getHomePage(HttpServletRequest httpServletRequest) {
         ModelAndView modelAndView = new ModelAndView("home");
         String username = httpServletRequest.getUserPrincipal().getName();
 
        modelAndView.addObject("username", username);
         return modelAndView;
 
     }
 
     @RequestMapping(value = "/logout*")
     public ModelAndView logoutPage(HttpServletRequest httpServletRequest) throws IOException {
         String serverName = httpServletRequest.getServerName();
         String cas;
         if (serverName.contains(".135")) cas = "cas";
         else cas = "castest";
         ModelAndView modelAndView = new ModelAndView("redirect:http://"+cas+".thoughtworks.com/cas/logout");
         httpServletRequest.getSession().invalidate();
         return modelAndView;
 
     }
 
 
 }

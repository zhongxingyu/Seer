 package it.sevenbits.space.web.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class SigninController {
 
     @RequestMapping(value = "/signin.html", method = RequestMethod.GET)
     public String signin() {
         return "signin";
     }
 
     @RequestMapping(value = "/signin-failure.html", method = RequestMethod.GET)
     public String signinFailure() {
         return "signin_failure";
     }
 }

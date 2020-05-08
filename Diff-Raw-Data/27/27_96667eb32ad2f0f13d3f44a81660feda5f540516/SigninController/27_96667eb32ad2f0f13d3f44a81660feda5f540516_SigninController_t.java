 package it.sevenbits.space.web.controller;
 
 import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
import java.security.Principal;

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

    @RequestMapping(value="/user.html", method = RequestMethod.GET)
    public String printWelcome(ModelMap model, Principal principal ) {

        if (principal != null) {
            model.addAttribute("username", principal.getName());
        } else {
            model.addAttribute("username", "nobody");
        }
        model.addAttribute("message", "Current login info");
        return "user";

    }
 }

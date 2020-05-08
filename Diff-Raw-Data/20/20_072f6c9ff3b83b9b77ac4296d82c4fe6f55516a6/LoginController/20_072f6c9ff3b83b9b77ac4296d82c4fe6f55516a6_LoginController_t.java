 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Login Controller implementation
  * 
  * @author Vojtech Schlemmer
  */
 @Controller
 public class LoginController {
     
     @RequestMapping(value="/login", method = RequestMethod.GET)
     public String login(ModelMap model) {
         DefaultController.addHeaderFooterInfo(model);
         model.addAttribute("pageTitle", "lang.login");
         return "login";
     }
     
     @RequestMapping(value="/loginfailed", method = RequestMethod.GET)
     public String loginerror(ModelMap model) {
         model.addAttribute("error", "true");
         return "login";
     }
 
     @RequestMapping(value="/logout", method = RequestMethod.GET)
     public String logout(ModelMap model) {
        SecurityContextHolder.clearContext();
         DefaultController.addHeaderFooterInfo(model);
        return "redirect:/j_spring_security_logout";
     }
 }

 package se.arcticblue.raven.server.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import se.arcticblue.raven.server.form.LoginForm;
 import org.springframework.ui.ModelMap;
 
 @Controller
 public class AutheticationController extends AbstractController {
 
    @RequestMapping(value="/login", method = RequestMethod.GET)
     public String login(ModelMap model, @ModelAttribute LoginForm loginForm) {
        return "/login";
     }
 
     @RequestMapping(value="/loginfailed", method = RequestMethod.GET)
     public String loginerror(ModelMap model) {
         model.addAttribute("error", "true");
        return "/login";
     }
 
 }

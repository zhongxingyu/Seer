 package com.sendgrid.controllers;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.sendgrid.forms.Signup;
 import com.github.sendgrid.SendGrid;
import com.github.scottmotte.Vcapenv;
 
 @Controller
 public class SignupController {
 
   @RequestMapping("/")
   public String index(Model model) {
     model.addAttribute("signup", new Signup());
 
     return "index";
   }
 
   @RequestMapping(value = "/signup", method = RequestMethod.POST)
   public String postSignup(@ModelAttribute Signup signup, Model model) {
 
    Vcapenv vcapenv   = new Vcapenv();
    String username   = vcapenv.SENDGRID_USERNAME();
    String password   = vcapenv.SENDGRID_PASSWORD();

    SendGrid sendgrid = new SendGrid(username, password);
     sendgrid.addTo(signup.getEmail());
     sendgrid.setFrom("scott.motte@sendgrid.com");
     sendgrid.setSubject("[Spring Attack] Welcome");
     sendgrid.setHtml("<h3>Thanks for signing up. Here is a picture of Spring to start you off.</h3><p><img src='http://www.lolomgwtfbbq.com/wp-content/uploads/2012/08/oh-waterfall.jpeg'></p>");
 
     String response = sendgrid.send();
 
     return "signup";
   }
 }

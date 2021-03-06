 package com.in6k.mypal.controller;
 
 import com.in6k.mypal.form.RegistrationForm;
 import com.in6k.mypal.service.RegistrationService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 
 @Controller
 public class SecurityController {
     @RequestMapping(value = "/registration", method = RequestMethod.GET)
     public String showRegistrationForm(Model model) {
         RegistrationForm registrationForm = new RegistrationForm();
 
         model.addAttribute("registrationForm", registrationForm);
         return "security/registration";
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     public String processRegistrationForm(@Valid RegistrationForm registrationForm,BindingResult result, Model model) {
        RegistrationService registrationService = new RegistrationService(registrationForm);

        if (result.hasErrors() || registrationService.hasErrors()) {
             model.addAttribute("registrationForm", registrationForm);
            model.addAttribute("email_error", "*User with this email exists");
             return "security/registration";
         }
 
         registrationService.register();
 
        return "redirect:/";
     }
 }

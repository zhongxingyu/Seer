 package com.tp.restaurants.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 
 import com.tp.restaurants.Site;
 import com.tp.restaurants.User;
 import com.tp.restaurants.validations.UserValidator;
 
 @Controller
@RequestMapping(value = "/user/new", method = RequestMethod.GET)
 public class AddUserForm {
 	
 	private final Site site;
 	
 	@Autowired
     public AddUserForm(Site site) {
         this.site = site;
     }
 	
     @RequestMapping(method = RequestMethod.GET)
     public String setupForm(Model model) {
         User user = new User();
         model.addAttribute("user", user);
         return "user/form";
     }
 	
     @RequestMapping(method = RequestMethod.POST)
     public String processSubmit(@ModelAttribute User user, BindingResult result, SessionStatus status) {
         new UserValidator().validate(user, result);
         if (result.hasErrors()) {
             return "user/form";
         }
         else {
             //this.site.storeUser(user);
             //status.setComplete();
             return "redirect:/user/" + user.getId();
         }
     }
 
 }

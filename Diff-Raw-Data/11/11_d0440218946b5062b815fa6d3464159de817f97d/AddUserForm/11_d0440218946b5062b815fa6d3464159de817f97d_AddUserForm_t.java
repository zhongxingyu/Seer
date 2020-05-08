 package com.tp.restaurants.web;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "/user/new")
 public class AddUserForm {
 	
	private final Logger logger = LoggerFactory.getLogger(getClass());
 	private final Site site;
 	
 	@Autowired
     public AddUserForm(Site site) {
         this.site = site;
     }
 	
     @RequestMapping(method = RequestMethod.GET)
     public String setupForm(Model model) {
         User user = new User();
        this.logger.info("New user created");
         model.addAttribute("user", user);
        this.logger.info("Redirecting to user form");
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

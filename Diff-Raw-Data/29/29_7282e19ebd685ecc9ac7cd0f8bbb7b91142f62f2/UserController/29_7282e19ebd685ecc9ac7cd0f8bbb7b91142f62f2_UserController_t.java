 package com.forum.web.controller;
 
 
 import com.forum.domain.Country;
 import com.forum.domain.User;
 import com.forum.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.util.List;
 import java.util.Map;
import java.util.logging.Logger;
 
 @Controller
 public class UserController {
     public static final String COUNTRIES = "countries";
     public static final String USER = "user";
     public static final String JOIN = "join";
     public static final String SHOW_PROFILE = "showProfile";
     private UserService userService;
     private List<Country> countries;
 
    private static Logger logger = Logger.getLogger(UserController.class.getName());

     @Autowired
     public UserController(UserService userService){
         this.userService = userService;
         countries = userService.getAvailableCountries();
     }
 
     @RequestMapping(value = "/join",method = RequestMethod.GET)
     public String showRegistrationForm(Map model) {
         User user = new User();
         model.put(USER, user);
         model.put(COUNTRIES, countries);
         return JOIN;
     }
 
     @RequestMapping(value = "/join", method = RequestMethod.POST)
     public String processRegistrationForm(@Valid User user,  BindingResult result, Map model) {
        logger.info("user = " + user.toString());
         model.put(COUNTRIES, countries);
 
         if (result.hasErrors()) {
             return JOIN;
         }
 
         model.put(USER, user);
         userService.createUser((User)model.get(UserController.USER));
 
         return SHOW_PROFILE;
     }
 }

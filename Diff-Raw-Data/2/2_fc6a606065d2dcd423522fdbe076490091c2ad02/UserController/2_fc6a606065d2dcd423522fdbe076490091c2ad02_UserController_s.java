 package edu.chl.dat076.foodfeed.controller;
 
 
 import edu.chl.dat076.foodfeed.model.dao.UserDao;
 import edu.chl.dat076.foodfeed.model.entity.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Serves Registration and view of user profiles.
  *
  */
 @Controller
 @RequestMapping("/user")
 public class UserController {
     
     @Autowired
     private UserDao userDao;
 
     private static final Logger logger = LoggerFactory
             .getLogger(UserController.class);
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
     public String doRegister(Model model, @Validated User usr) {
         logger.info("Registering new user");
         userDao.create(usr);
         // TODO: Some more black magic to be done here (make sure the user is 
         // automatically logged in
         return "redirect:/";
     }
 
     @RequestMapping(value = "/register", method = RequestMethod.GET)
     public String getRegisterForm(Model model) {
         model.addAttribute("user", new User());
         return "user/register";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable long id, Model model) {
         logger.info("showing user with id: "+id);
         model.addAttribute("user", userDao.find(id));
         return "user/show";
     }
     
 
     @RequestMapping(value = "/login", method = RequestMethod.GET)
     public String loginForm() {
         logger.info("Showing form to log in.");
         return "user/login";
     }
 }

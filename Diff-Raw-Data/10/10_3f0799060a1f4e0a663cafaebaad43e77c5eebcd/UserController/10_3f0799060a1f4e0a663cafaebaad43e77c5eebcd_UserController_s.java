 package edu.chl.dat076.foodfeed.controller;
 
 
 import edu.chl.dat076.foodfeed.model.dao.IUserDao;
 import edu.chl.dat076.foodfeed.model.entity.User;
 import edu.chl.dat076.foodfeed.model.flash.FlashMessage;
 import edu.chl.dat076.foodfeed.model.flash.FlashType;
 import edu.chl.dat076.foodfeed.util.EncoderUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

 /**
  * Serves Registration and view of user profiles.
  *
  */
 @Controller
 @Transactional
 @RequestMapping("/user")
 public class UserController {
     
     @Autowired
     private IUserDao userDao;
     
 
     private static final Logger logger = LoggerFactory
             .getLogger(UserController.class);
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String doRegister(Model model, @Validated User usr, BindingResult result, RedirectAttributes ra) {
         
         if(result.hasErrors()){
            ra.addFlashAttribute("flash", new FlashMessage((result.getFieldError().getDefaultMessage()), FlashType.ERROR));
            return "redirect:register";
         } else {
             userDao.create(usr);
             return "redirect:login";
         }
         
 
     }
 
     /*
      * Form for registering a new user
      */
     @RequestMapping(value = "/register", method = RequestMethod.GET)
     public String getRegisterForm(Model model) {
         model.addAttribute("user", new User());
         return "user/register";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String showById(@PathVariable String id, Model model) {
         logger.info("showing user with id: "+id);
         model.addAttribute("user", userDao.find(id));
         return "user/show";
     }
     
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String show(Model model){
         logger.info("showing the authenticated user");        
         User usr = userDao.find(SecurityContextHolder.getContext().getAuthentication().getName());
         
         model.addAttribute("user", usr);
         model.addAttribute("recipes", usr.getRecipes());
         
         return "user/show";
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.GET)
     public String loginForm() {
         logger.info("Showing form to log in.");
         return "user/login";
     }
     
     @RequestMapping(value="/{id}/edit", method = RequestMethod.GET)
     public String getEditForm(@PathVariable String id, Model model){
         User user = userDao.find(id);
         
         model.addAttribute("newPass", new String());
         model.addAttribute("oldPass", new String());
         model.addAttribute("user", user);
         
         return "user/edit";
     }
     
     
     @RequestMapping(value="/{id}/edit", method = RequestMethod.POST)
     public String doEdit(Model model, User user){
         
         String oldPass = (String) model.asMap().get("oldPass");
         
         if(EncoderUtil.encode(oldPass).equals(user.getPassword())) {
             String newPass = (String) model.asMap().get("newPass");
             user.setPassword(EncoderUtil.encode(newPass));
         }
         
         userDao.update(user);
         
         return null;
     }
             
 }

 package com.thoughtworks.controllers;
 
 import com.thoughtworks.models.User;
 import com.thoughtworks.repositories.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import java.util.Map;
 
 @Controller
 @SessionAttributes(types = User.class)
 public class LoginController {
 
   @Qualifier("userRepository")
   @Autowired
   private UserRepository repository;
 
 
   @RequestMapping(value = "/", method = RequestMethod.GET)
   public String login_request(Map<String, Object> model) {
     User login = new User();
     model.put("login", login);
     return "user/index";
   }
 
 
   @RequestMapping(value = "/", method = RequestMethod.POST)
   public String login(@RequestParam("employeeId") Long employeeId, @RequestParam("password") String password, HttpServletRequest request) {
     User user = repository.findOne(employeeId);
     if(user != null && user.getPassword().equals(password)){
 
     request.getSession().setAttribute("isAdmin", user.isAdmin());
     request.getSession().setAttribute("employeeId", employeeId);
     request.getSession().setAttribute("fullName", user.getFullName());
     request.getSession().setAttribute("isLogin", true);
 
     return "redirect:book/index";
     }
    request.getSession().setAttribute("isAdmin", user.isAdmin());
     request.getSession().setAttribute("isLogin", false);
 
         return "redirect:/";
   }
 
   @RequestMapping(value = "/logout", method = RequestMethod.GET)
   public String logout(HttpServletRequest request) {
     request.getSession().setAttribute("isAdmin", null);
     request.getSession().setAttribute("isLogin", null);
     return "redirect:/";
   }
 
   @RequestMapping(value = "/register", method = RequestMethod.GET)
   public String register(Map<String, Object> model) {
     User user = new User();
     model.put("user", user);
     return "user/register";
   }
 
   @RequestMapping(value = "/register", method = RequestMethod.POST)
   public String register(@ModelAttribute("user") @Valid User user, BindingResult result, HttpServletRequest request, RedirectAttributes attributes) {
     if (result.hasErrors()) {
         attributes.addFlashAttribute("errorMassage","you not registered...........");
         return "user/register";
     }
     user.setActive(true);
     this.repository.save(user);
     request.getSession().setAttribute("isLogin", Boolean.TRUE);
     request.getSession().setAttribute("isAdmin", Boolean.FALSE);
       attributes.addFlashAttribute("successMessage","you are registered successfully........");
       return "redirect:book/index";
 
   }
 }
 
 

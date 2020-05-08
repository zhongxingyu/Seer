 package com.directi.train.tweetapp.controllers;
 
 import com.directi.train.tweetapp.model.FeedItem;
 import com.directi.train.tweetapp.model.TweetItem;
 import com.directi.train.tweetapp.services.UserStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.List;
 
 @Controller
@RequestMapping("/auth")
 public class UserController {
     public final SimpleJdbcTemplate db;
     private final UserStore userStore;
 
     @Autowired
     public UserController(SimpleJdbcTemplate db,UserStore userStore) {this.db = db;this.userStore = userStore;}
 
     @RequestMapping(value = "login", method = RequestMethod.GET)
     public String loginForm() {
         return "index";
     }
 
     @RequestMapping(value = "register", method = RequestMethod.GET)
     public ModelAndView registerForm() {
         return new ModelAndView("register");
     }
     @RequestMapping(value = "register", method = RequestMethod.POST)
     public ModelAndView register(@RequestParam("username") String userName,
                                  @RequestParam("password") String password,
                                  @RequestParam("email") String email) {
         ModelAndView mv = new ModelAndView("/index");
         mv.addObject("message",userStore.registerUser(email,userName,password));
         return mv;
     }
 
     @RequestMapping(value = "login", method = RequestMethod.POST)
     public ModelAndView login(@RequestParam("username") String userName,
                               @RequestParam("password") String password, HttpSession session) {
         ModelAndView mv = new ModelAndView("/index");
         long userID;
         System.out.println(userName+password);
         try {
             userID = userStore.checkLogin(userName,password).getId();
             session.setAttribute("userName", userName);
             session.setAttribute("userID", userID);
         } catch (Exception e) {
             System.out.println(e);
             mv.addObject("message",e.getMessage());
         }
         mv.setViewName("redirect:/tweet");
         return mv;
     }
 
     @RequestMapping(value = "logout")
     public String logout(HttpSession session) {
         session.invalidate();
         return "redirect:/";
     }
 }

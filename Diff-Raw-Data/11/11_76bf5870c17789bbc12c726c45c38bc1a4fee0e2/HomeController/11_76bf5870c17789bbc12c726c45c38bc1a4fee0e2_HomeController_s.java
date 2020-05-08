 package com.your_new_project.controller;
 
 import com.your_new_project.models.User;
 import com.your_new_project.services.IUserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 @Controller
 public class HomeController {
 
     private IUserService userService;
 
     /**
     * Controller zur Darstellung des Players.
      *
      * @param request
      * @param response
      * @return ModelAndView
      */
     @RequestMapping(value="/user", method = RequestMethod.GET)
     public ModelAndView player(HttpServletRequest request, HttpServletResponse response) {
         HashMap map = new HashMap();
 
         return new ModelAndView("welcome", map);
     }
 
     /**
     * Example of Jackson JSON Marshalling.
      * 
     * @param token
      * @param request
      * @return
      */
     @RequestMapping(value="/users", method = RequestMethod.GET)
    public @ResponseBody User[] interprets(@RequestParam("token") String token, HttpServletRequest request) {
         ArrayList<User> users = (ArrayList<User>) userService.findAllUsers();
         return users.toArray(new User[users.size()]);
     }
 
     public IUserService getUserService() {
         return userService;
     }
 
     @Autowired
     public void setUserService(IUserService userService) {
         this.userService = userService;
     }
 }

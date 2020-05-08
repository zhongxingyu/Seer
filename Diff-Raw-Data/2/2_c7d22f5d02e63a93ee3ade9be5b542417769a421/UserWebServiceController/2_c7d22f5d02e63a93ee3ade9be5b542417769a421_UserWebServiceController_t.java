 package com.mops.registrar.web.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.mops.registrar.elements.user.User;
 import com.mops.registrar.services.user.UserService;
 
 /**
  * A REST web service controller used to access the {@link User}s
  * 
  * @author dylants
  */
 @RequestMapping(value = "/service")
 @Controller
 public class UserWebServiceController {
     @Autowired
     private UserService userService = null;
 
     /**
      * Returns all {@link User}s available
      * 
      * @return all {@link User}s available
      */
     @RequestMapping(value = "/users", method = RequestMethod.GET)
     @ResponseBody
     public List<User> getUsers() {
         return this.userService.getUsers();
     }
 
     /**
      * Returns the {@link User} found by <code>emailAddress</code>
      * 
      * @param emailAddress
      *            The {@link User}'s email address
      * @return The {@link User} found, else {@literal null}
      */
    @RequestMapping(value = "/user/{emailAddress:.+}", method = RequestMethod.GET)
     @ResponseBody
     public User getUser(@PathVariable("emailAddress") String userName) {
         return this.userService.getUser(userName);
     }
 
     /**
      * Returns the (first) {@link User} found by <code>firstName</code> and <code>lastName</code>
      * 
      * @param firstName
      *            The {@link User}s first name
      * @param lastName
      *            The {@link User}s last name
      * @return The (first) {@link User} found, else {@literal null}
      */
     @RequestMapping(value = "/user", method = RequestMethod.GET)
     @ResponseBody
     public User getUser(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName) {
         return this.userService.getUser(firstName, lastName);
     }
 
     /**
      * Adds a {@link User} to our {@link UserService}
      * 
      * @param user
      *            The {@link User} to add
      * @return The constructed {@link User}
      */
     @RequestMapping(value = "/user", method = RequestMethod.PUT)
     @ResponseBody
     public User addUser(@RequestBody User user) {
         this.userService.addUser(user);
         return user;
     }
 
     /**
      * @return the userService
      */
     public UserService getUserService() {
         return userService;
     }
 
     /**
      * @param userService
      *            the userService to set
      */
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 }

 package com.springapp.mvc.controller;
 
 import com.google.gson.Gson;
 import com.springapp.mvc.data.FriendRepository;
 import com.springapp.mvc.data.TweetRepository;
 import com.springapp.mvc.model.Tweet;
 import com.springapp.mvc.model.User;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.util.JSONPObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 import com.springapp.mvc.data.UserRepository;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 @Controller
 public class UserController {
 
     private final UserRepository userRepository;
     private final TweetRepository tweetRepository;
     private final FriendRepository friendRepository;
 
     static Logger log = Logger.getLogger(UserRepository.class);
 
     @Autowired
     public UserController(UserRepository userRepository,TweetRepository tweetRepository,FriendRepository friendRepository) {
         this.userRepository = userRepository;
         this.tweetRepository = tweetRepository;
         this.friendRepository = friendRepository;
     }
 
     @RequestMapping(value = "MiniTwitter/{id}", method = RequestMethod.GET)
     @ResponseBody
     public ModelAndView printWelcome(@PathVariable("id") String userName) {
         ModelAndView modelAndView = new ModelAndView("index");
         List<Tweet> tweets = tweetRepository.fetchTweets(userName);
         modelAndView.addObject("tweets", tweets);
         modelAndView.addObject("info",userRepository.fetchUser(userName));
         modelAndView.addObject("num_followers", friendRepository.fetchFollowers(userName).size());
         modelAndView.addObject("num_following", friendRepository.fetchFollowing(userName).size());
         modelAndView.addObject("num_of_tweets", tweets.size());
         return modelAndView;
     }
 
     public static List<String> getUserNames(List<User> users) {
         List<String> userNames = new ArrayList<>();
         for ( User user : users) userNames.add(user.getName());
         return userNames;
     }
 
 	@RequestMapping("showusers")
     @ResponseBody
     public List<User> printWelcome(ModelMap model) {
         return userRepository.findUsers();
 	}
 
     @RequestMapping(value = "MiniTwitter/users", method = RequestMethod.POST)
     @ResponseBody
     public void add(@RequestBody Map<String, String> user) {
         System.out.println("Creating new user: " + user.get("username") + " " + user.get("password"));
         if ( userRepository.isUserPresent(user.get("username")) ) {
             return;
         }
         userRepository.addUser(user.get("username"), encodePassword(user.get("password")), user.get("name"), user.get("email"));
     }
 
    @RequestMapping(value = "MiniTwitter/account/settings", method = RequestMethod.PUT)
     @ResponseBody
     public void modifyUser(@RequestBody Map<String, String> values,HttpServletRequest httpServletRequest) {
         String userName = httpServletRequest.getAttribute("currentUser").toString();
         System.out.println("for user: " + userName + " Modifying value: " + values.get("name"));
         userRepository.modifyUser(userName, values.get("name"), values.get("email"), values.get("Password"));
     }
 
     public String encodePassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 
 
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wad.spring.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import net.sf.json.JSONObject;
 import javax.inject.Inject;
 import javax.inject.Provider;
 import org.springframework.social.facebook.api.Facebook;
 import javax.servlet.http.HttpServletRequest;
 import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
 import org.codehaus.jackson.annotate.JsonMethod;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.social.facebook.api.*;
 import org.springframework.social.facebook.api.impl.FacebookTemplate;
 import org.springframework.social.twitter.api.CursoredList;
 import org.springframework.social.twitter.api.Twitter;
 import org.springframework.social.twitter.api.UserList;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import wad.spring.repository.UserRepository;
 
 /**
  *
  * @author janne
  */
 @Controller
 @RequestMapping(value = "/social")
 public class SocialController {
 
     private final Facebook facebook;
     private final Twitter twitter;
     private final Provider<ConnectionRepository> connectionRepositoryProvider;
     private final UserRepository userRepository;
 
     @Inject
     public SocialController(Facebook facebook, Twitter twitter, Provider<ConnectionRepository> connectionRepositoryProvider, UserRepository userRepository, Facebook faecbook) {
         this.connectionRepositoryProvider = connectionRepositoryProvider;
         this.userRepository = userRepository;
         this.facebook = facebook;
         this.twitter = twitter;
     }
 
     private ConnectionRepository getConnectionRepository() {
         return connectionRepositoryProvider.get();
     }
 
     @RequestMapping(value = "/facebook", method = RequestMethod.GET)
     public String facebookHome(HttpServletRequest request, Model model) {
 
         Connection<Facebook> connection = getConnectionRepository().findPrimaryConnection(Facebook.class);
         List<Connection<Facebook>> facebookConnections = connectionRepositoryProvider.get().findConnections(Facebook.class);
         if (facebookConnections.isEmpty()) {
             return "redirect:/connect";
         } else {
 
             List<Post> posts = connection.getApi().feedOperations().getPosts();

             model.addAttribute("connectionsToFacebook", facebookConnections);
 
             model.addAttribute("friends", facebook.userOperations().getUserPermissions());
             model.addAttribute("profile", facebook.userOperations().getUserProfile());
             model.addAttribute("feed", posts);
             model.addAttribute("userid", "me");
 
             return "facebook";
         }
     }
 
     @RequestMapping(value = "/facebook", method = RequestMethod.POST)
     public String facebookUserPost(@RequestParam String userId, Model model) throws IOException {
         
         return "redirect:facebook/" + userId;
     }
 
     @RequestMapping(value = "/facebook/{userId}", method = RequestMethod.GET)
     public String facebookUserGet(Model model, @PathVariable Long userId) throws IOException {
 
         String s = userId.toString();
         Connection<Facebook> connection = getConnectionRepository().findPrimaryConnection(Facebook.class);
         List<Connection<Facebook>> facebookConnections = connectionRepositoryProvider.get().findConnections(Facebook.class);
         
         if (facebookConnections.isEmpty()) {
             return "redirect:/connect";
         } else {
             List<Post> posts = connection.getApi().feedOperations().getPosts();
 
             model.addAttribute("connectionsToFacebook", facebookConnections);
 
             model.addAttribute("profile", facebook.userOperations().getUserProfile());
             model.addAttribute("feed", posts);
             model.addAttribute("userid", userId);
 
             return "facebook";
         }
     }
 
     @RequestMapping(value = "/twitter", method = RequestMethod.GET)
     public String twitterHome(HttpServletRequest request, Model model) {
 
         Connection<Twitter> connection = getConnectionRepository().findPrimaryConnection(Twitter.class);
         List<Connection<Twitter>> twitterConnections = connectionRepositoryProvider.get().findConnections(Twitter.class);
         if (twitterConnections.isEmpty()) {
             return "redirect:/connect";
         } else {
             CursoredList<UserList> posts = connection.getApi().listOperations().getLists();
 
             model.addAttribute("connectionsToTwitter", twitterConnections);
 
             model.addAttribute("profile", twitter.userOperations().getUserProfile());
             model.addAttribute("feed", posts);
             model.addAttribute("userid", "me");
 
             return "twitter";
         }
     }
 
     @RequestMapping(value = "/twitter", method = RequestMethod.POST)
     public String twitterUserPost(@RequestParam String userId, Model model) throws IOException {
         
         return "redirect:twitter/" + userId;
     }
 
     @RequestMapping(value = "/twitter/{userId}", method = RequestMethod.GET)
     public String twitterUserGet(Model model, @PathVariable Long userId) throws IOException {
 
         String s = userId.toString();
         Connection<Twitter> connection = getConnectionRepository().findPrimaryConnection(Twitter.class);
         List<Connection<Twitter>> twitterConnections = connectionRepositoryProvider.get().findConnections(Twitter.class);
 
         if (twitterConnections.isEmpty()) {
             return "redirect:/connect";
         } else {
             model.addAttribute("connectionsToTwitter", twitterConnections);
 
             model.addAttribute("profile", twitter.userOperations().getUserProfile());
             model.addAttribute("userid", userId);
 
             return "twitter";
         }
     }
 }

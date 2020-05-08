 package com.um.canario.controllers;
 
 import java.lang.System;
 import java.util.List;
 import com.um.canario.exceptions.ThisIsNotTheUserYouAreLookingForException;
 import com.um.canario.models.Tweet;
 import com.um.canario.models.Tweeter;
 import com.um.canario.models.Following;
 import com.um.canario.validators.TweeterValidator;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 @RequestMapping("/tweeter/**")
 @Controller
 public class TweeterController {
 
     @RequestMapping(params={"view","follow=1"}, produces="text/html", method=RequestMethod.POST)
     public String follow(Model uiModel, HttpServletRequest request) {
 
         Following following = new Following();
         Following fFromDb;
         Tweeter followed;
         Tweeter follower;
         boolean isFollowing;
 
         followed = Tweeter.findTweeter(new Long(request.getParameter("view")));
         try {
             follower = getTweeter();
         } catch (ThisIsNotTheUserYouAreLookingForException e) {
             return "redirect:tweet/index";
         }
         fFromDb = Following.findFollowingByFollowedAndFollower(followed, follower);
         isFollowing = fFromDb != null;
         if (isFollowing) {
             fFromDb.remove();
             isFollowing = false;
         }
         else {
             following.setFollower(follower);
             following.setFollowed(followed);
             following.persist();
             isFollowing = true;
         }
         uiModel.addAttribute("tweeter", followed);
         uiModel.addAttribute("following", isFollowing);
         return "tweeter/view";
     }
 
     @RequestMapping(params="view", produces="text/html")
     public String view(Model uiModel, HttpServletRequest request) {
         Tweeter tweeter = Tweeter.findTweeter(new Long(request.getParameter("view")));
         Tweeter user;
         try {
             user = getTweeter();
         } catch(ThisIsNotTheUserYouAreLookingForException e) {
             return "redirect:tweeter/view";
         }
         uiModel.addAttribute("tweeter", tweeter);
         uiModel.addAttribute("following", Following.isFollowing(tweeter, user));
        uiModel.addAttribute("isMe", tweeter.getId() == user.getId());
         return "tweeter/view";
     }
 
     @RequestMapping
     public String index() {
         return "tweeter/index";
     }
 
     @RequestMapping(value="/new", produces="text/html", method=RequestMethod.POST)
     public String newTweeterPost(@ModelAttribute Tweeter tweeter, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
         new TweeterValidator().validate(tweeter, bindingResult, httpServletRequest.getParameter("rePassword"));
         if(bindingResult.hasErrors()) {
             uiModel.addAttribute("tweeter", tweeter);
             return "tweeter/new";
         }
         ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
         tweeter.setPassword(encoder.encodePassword(tweeter.getPassword(), null));
         tweeter.setAuthority("ROLE_USER");
         tweeter.setAllowGeolocation(true);
         tweeter.setEnabled(true);
         uiModel.asMap().clear();
         tweeter.persist();
         return "redirect:/site/login";
     }
 
     @RequestMapping(value="/new", produces="text/html")
     public String newTweeter(Model uiModel){
     	uiModel.addAttribute("tweeter", new Tweeter());
     	return "tweeter/new";
     }
 
     private Tweeter getTweeter() throws ThisIsNotTheUserYouAreLookingForException {
         String username = SecurityContextHolder.getContext().getAuthentication().getName();
         Tweeter tweeter = Tweeter.findTweeterByUsername(username);
         if(tweeter == null) {
             throw new ThisIsNotTheUserYouAreLookingForException();
         }
         return tweeter;
     }
 }

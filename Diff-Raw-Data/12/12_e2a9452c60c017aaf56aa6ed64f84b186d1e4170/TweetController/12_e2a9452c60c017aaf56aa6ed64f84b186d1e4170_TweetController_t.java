 package com.um.canario.controllers;
 
 import java.lang.System;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Collections;
 import org.apache.commons.beanutils.BeanUtils;
 import com.um.canario.exceptions.ThisIsNotTheUserYouAreLookingForException;
 import com.um.canario.models.Tweet;
 import com.um.canario.models.Tweeter;
 import com.um.canario.models.Following;
 import com.um.canario.models.Hash;
 import com.um.canario.models.HashMention;
 import com.um.canario.validators.TweetValidator;
 import com.um.canario.models.utils.HashUtils;
 import com.um.canario.models.utils.MentionUtils;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 @RequestMapping("/tweet/**")
 @Controller
 public class TweetController {
 
     @RequestMapping(params="retweet=1", method=RequestMethod.POST)
     public String retweet(Model uiModel, HttpServletRequest request) {
         Tweet newTweet = new Tweet();
         Tweet tweet;
         Tweeter tweeter;
 
         try {
             tweeter = getTweeter();
         } catch (ThisIsNotTheUserYouAreLookingForException e) {
             return "redirect: /tweet/index";
         }
         tweet = Tweet.findTweet(new Long(request.getParameter("tweet")));
 
         newTweet.setTweeter(tweeter);
         newTweet.setReTweet(tweet);
         newTweet.setDate(new java.util.Date());
         newTweet.persist();
 
         return "redirect:/tweet/index";
     }
 
     @RequestMapping(params="hash")
     public String hash(Model uiModel, HttpServletRequest request) {
         Set<HashMention> hMentions;
         ArrayList<Tweet> tweets = new ArrayList<Tweet>();
         Hash hash = Hash.findHash(request.getParameter("hash"));
 
         hMentions = hash.getTweets();
         for (HashMention t : hMentions) {
             tweets.add(t.getTweet());
         }
         Collections.reverse(tweets);
         uiModel.addAttribute("tweets", tweets);
         return "tweet/index";
     }
 
     @RequestMapping
     public String index(Model uiModel) {
         Set<Following> following;
         Tweeter user;
         List<Tweeter> tweeters = new ArrayList<Tweeter>();
         List<Tweet> tweets;
         int i = 0;
         try {
             user = getTweeter();
         } catch (ThisIsNotTheUserYouAreLookingForException e) {
             return "redirect: /tweet/index";
         }
         following = user.getFollowing();
         for(Following f : following) {
             tweeters.add(f.getFollowed());
         }
         tweeters.add(user);
 
         tweets = Tweet.findTweetsFromTweeters(tweeters);
        tweeters = Tweeter.findOtherTweeters(user, 1);
         uiModel.addAttribute("tweets", tweets);
        uiModel.addAttribute("tweeters", tweeters);
         return "tweet/index";
     }
 
     @RequestMapping(value="/new", produces="text/html", method=RequestMethod.POST)
     public String newTweeterPost(@ModelAttribute Tweet tweet, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
         String[] strings;
         String content;
         new TweetValidator().validate(tweet, bindingResult);
         if(bindingResult.hasErrors()) {
             uiModel.addAttribute("tweet", tweet);
             return "tweet/new";
         }
         try {
             tweet.setTweeter(getTweeter());
         } catch (ThisIsNotTheUserYouAreLookingForException exception){
         	//TODO cambiar por página de error
             return "tweet/index";
         }
         tweet.setDate(new java.util.Date());
         tweet.persist();
         tweet.flush();
         try {
             MentionUtils.parseMentions(tweet);
         } catch (ThisIsNotTheUserYouAreLookingForException e) {
             bindingResult.rejectValue("content", "user_invalid", "Todos los usuarios mencionados deben existir");
             uiModel.addAttribute("tweet", tweet);
             tweet.remove();
             tweet.flush();
             return "tweet/new";
         }
         HashUtils.parseHashes(tweet);
 
 
         tweet.persist();
         uiModel.asMap().clear();
     	return "redirect:/tweet/index";
     }
 
     @RequestMapping(value="/new", produces="text/html")
     public String newTweet(Model uiModel) {
     	uiModel.addAttribute("tweet", new Tweet());
     	return "tweet/new";
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

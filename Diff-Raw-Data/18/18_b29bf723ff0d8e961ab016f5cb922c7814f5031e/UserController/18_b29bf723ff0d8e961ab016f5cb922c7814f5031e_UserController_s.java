 package com.worldofzaar.controller;
 
 import com.worldofzaar.adapter.*;
 import com.worldofzaar.entity.*;
 import com.worldofzaar.service.*;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kseon
  * Date: 24.10.13
  * Time: 17:13
  * To change this template use File | Settings | File Templates.
  */
 
 @Controller
 @RequestMapping("/profile")
 public class UserController {
 
    @RequestMapping(value = "/userDecks/{userId}", method = RequestMethod.GET)
     public
     @ResponseBody
     List<DeckAdapter> getUserDecks(ModelMap model, HttpServletRequest request, @PathVariable Integer userId) {
         DeckService deckService = new DeckService();
         return deckService.getUserDecksById(userId);
     }
 
    @RequestMapping(value = "/userCards/{userId}", method = RequestMethod.GET)
     public
     @ResponseBody
     List<UserCardAdapter> getUserCards(ModelMap model, HttpServletRequest request, @PathVariable Integer userId) {
         UserCardService userCardService = new UserCardService();
         return userCardService.getAllUserCardsById(userId);
     }
 
    @RequestMapping(value = "/deckCards/{deckId}", method = RequestMethod.GET)
     public
     @ResponseBody
     List<DeckCardAdapter> getDeckCards(ModelMap model, HttpServletRequest request, @PathVariable Integer deckId) {
         DeckCardService deckCardService = new DeckCardService();
         return deckCardService.getDeckCardsById(deckId);
     }
 
     @RequestMapping(value = "/allCards/", method = RequestMethod.GET)
     public
     @ResponseBody
     List<MasterOfDeckAdapter> getAllCards(ModelMap model, HttpServletRequest request) {
         MasterOfDeckCustomService masterOfDeckCustomService = new MasterOfDeckCustomService();
 
         return masterOfDeckCustomService.getCustomMasterOfDeck("Lang");
     }
 
    @RequestMapping(value = "/gameProfile/", method = RequestMethod.GET)
     public
     @ResponseBody
     GameProfile getUserProfile(ModelMap model, HttpServletRequest request, @PathVariable Integer userId) {
 
         UserService userService = new UserService();
         return userService.getUserGameProfileById(userId);
     }
 }

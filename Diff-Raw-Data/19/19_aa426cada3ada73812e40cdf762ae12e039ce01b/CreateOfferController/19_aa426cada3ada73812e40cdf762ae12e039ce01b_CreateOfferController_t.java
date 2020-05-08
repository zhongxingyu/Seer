 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Offer;
 import com.thoughtworks.twu.service.OfferServiceInterface;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @SessionAttributes({"username"})
 @Transactional
 public class CreateOfferController {
 
     @Autowired
     private OfferServiceInterface offerService;
 
     public CreateOfferController() {
     }
 
     public CreateOfferController(OfferServiceInterface offerService) {
         this.offerService = offerService;
     }
 
     @RequestMapping(value = "/createOffer.ftl", method = RequestMethod.GET)
     public String goToHomepageAfterCreatingOffer() {
         return "home/createOffer";
     }
 
    @RequestMapping(value = "/home", method = RequestMethod.POST)
     public ModelAndView createOffer(@RequestParam("title") String title, @RequestParam("category") String category,
                                     @RequestParam("description") String description, @ModelAttribute("username") final String username
                                     ) {
         Offer offer = new Offer(title, description, category, username);
         String offerId = offerService.saveOffer(offer);
         ModelAndView modelAndView = new ModelAndView("home/home");
         modelAndView.addObject("offerId", offerId);
 
         return modelAndView;
     }
 }

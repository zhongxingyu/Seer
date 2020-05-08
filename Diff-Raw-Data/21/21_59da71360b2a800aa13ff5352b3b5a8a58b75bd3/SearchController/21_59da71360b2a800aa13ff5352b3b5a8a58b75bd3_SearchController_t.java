 package com.morgajel.spoe.controller;
 
 
 import org.apache.log4j.Logger;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.SnippetService;
 
 /**
  * Controls all Search interactions.
  */
 @Controller
 public class SearchController extends MultiActionController {
 
     @Autowired
     private AccountService accountService;
     @Autowired
     private SnippetService snippetService;
 
     private static final transient Logger LOGGER = Logger.getLogger(SnippetController.class);
 
     /**
      * Displays the results for a given search.
      * @param searchQuery the text you're searching for
      * @return ModelAndView mav
      */
     @RequestMapping(value = "/")
    public ModelAndView quickSearch(@RequestParam("q") final String searchQuery) {
         LOGGER.info("Searching for " + searchQuery);
         ModelAndView mav = new ModelAndView();
         try {
             //FIXME need to sanitize user input
             //TODO search for stuff
 
             //mav.addObject("results", results);
             mav.addObject("message", "Results for '" + searchQuery + "'");
 
         } catch (Exception ex) {
             String message = "Something failed while trying to display " + searchQuery;
             LOGGER.error(message, ex);
             mav.addObject("message", message);
         }
         mav.setViewName("search/results");
         return mav;
     }
 
     /**
      * This is the default view for snippet, a catch-all for most any one-offs.
      * Will show the generic snippet page.
      * @return ModelAndView mav
      */
     @RequestMapping
     public ModelAndView defaultView() {
         LOGGER.info("showing the default view");
         ModelAndView  mav = new ModelAndView();
         mav.setViewName("snippet/view");
         mav.addObject("message", "show the default view for snippets");
 
         return mav;
     }
 
     public void setAccountService(AccountService pAccountService) {
         this.accountService = pAccountService;
     }
 
     public AccountService getAccountService() {
         return this.accountService;
     }
 
     public void setSnippetService(SnippetService pSnippetService) {
         this.snippetService = pSnippetService;
     }
 
     public SnippetService getSnippetService() {
         return this.snippetService;
     }
 
     /**
      * Returns the account for the current context.
      * @return Account
      */
     public Account getContextAccount() {
         String username = SecurityContextHolder.getContext().getAuthentication().getName();
         return accountService.loadByUsername(username);
     }
 
 }

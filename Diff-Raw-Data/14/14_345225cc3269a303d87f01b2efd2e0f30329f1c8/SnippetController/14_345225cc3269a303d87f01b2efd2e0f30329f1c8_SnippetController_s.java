 package com.morgajel.spoe.controller;
 
 
 import javax.validation.Valid;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.model.Snippet;
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.SnippetService;
 import com.morgajel.spoe.web.EditSnippetForm;
 
 /**
  * Controls all snippet interactions, etc.
  */
 @Controller
 public class SnippetController {
 
     @Autowired
     private AccountService accountService;
     @Autowired
     private SnippetService snippetService;
 
     private MailSender mailSender;
 
     private static final transient Logger LOGGER = Logger.getLogger(SnippetController.class);
 
     public void setMailSender(MailSender pMailSender) {
         //NOTE this may not be used anytime soon.
         this.mailSender = pMailSender;
     }
 
     public MailSender getMailSender() {
         //TODO is this needed?
         return this.mailSender;
     }
 
     /**
      * Saves the results of the incoming snippet form.
      * @param editSnippetForm the form you are submitting with snippet data.
      * @return ModelAndView mav
      */
     @RequestMapping(value = "save", method = RequestMethod.POST)
     //TODO figure out why I can't mark editSnippetForm @Valid
     public ModelAndView saveSnippet(EditSnippetForm editSnippetForm) {
         ModelAndView mav = new ModelAndView();
         try {
             //FIXME refactor this whole try block
             LOGGER.debug("trying to save " + editSnippetForm);
             Account account = getContextAccount();
             if (editSnippetForm.getSnippetId() == null) {
                 LOGGER.info("no snippetId found, saving as a new snippet " + editSnippetForm);
                 Snippet snippet = new Snippet(account);
                 snippet.configure(editSnippetForm);
                 snippetService.saveSnippet(snippet);
                 LOGGER.info("saved new snippet " + snippet);
                 mav.setViewName("snippet/editSnippet");
                 mav.addObject("message", "Snippet saved.");
             } else {
                 LOGGER.info("snippetId found, verifying it's a real snippet " + editSnippetForm);
                 Snippet snippet = snippetService.loadById(editSnippetForm.getSnippetId());
                 if (snippet != null) {
                     if (snippet.getAuthor().getUsername().equals(account.getUsername())) {
                         LOGGER.info("look, '" + snippet.getSnippetId() + "' is a real snippet and owned by " + account.getUsername());
                         snippet.configure(editSnippetForm);
                         snippetService.saveSnippet(snippet);
                         LOGGER.info("saved existing snippet " + snippet.getSnippetId());
 
                         editSnippetForm.importSnippet(snippet);
                         LOGGER.info("refreshing editSnippetForm with " + editSnippetForm);
                         mav.setViewName("snippet/editSnippet");
                         mav.addObject("message", "Snippet saved.");
                     } else {
                         mav.setViewName("snippet/snippetFailure");
                         mav.addObject("message", "I'm sorry, you're not the author of this snippet.");
                     }
                 } else {
                     mav.setViewName("snippet/snippetFailure");
                     mav.addObject("message", "I'm sorry, that snippet wasn't found.");
                 }
             }
 
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("damnit, something failed.", ex);
             mav.setViewName("snippet/snippetFailure");
             mav.addObject("message", "something failed.");
         }
         mav.addObject("editSnippetForm", editSnippetForm);
         return mav;
     }
     /**
      * Displays the form for editing a given snippet.
      * @param snippetId The ID of the snippet you wish to edit.
      * @return ModelAndView mav
      */
     @RequestMapping(value = "edit/{snippetId}", method = RequestMethod.GET)
     public ModelAndView editSnippet(@PathVariable Long snippetId) {
         LOGGER.debug("trying to edit " + snippetId);
         ModelAndView mav = new ModelAndView();
         try {
             Snippet snippet = snippetService.loadById(snippetId);
             Account account = getContextAccount();
             EditSnippetForm editSnippetForm = new EditSnippetForm();
             LOGGER.info(snippet);
             if (snippet != null) {
                 LOGGER.info("Snippet found");
                 if (account != null && snippet.getAuthor().getUsername().equals(account.getUsername())) {
                     LOGGER.info("Username matches " + snippet.getAuthor().getUsername());
                     mav.setViewName("snippet/editSnippet");
                     editSnippetForm.importSnippet(snippet);
                     mav.addObject("editSnippetForm", editSnippetForm);
                 } else {
                     LOGGER.info(SecurityContextHolder.getContext().getAuthentication().getName() + " isn't the author " + snippet.getAuthor().getUsername());
                     String message = "I'm sorry, Only the author can edit a snippet.";
                     mav.setViewName("snippet/viewSnippet");
                     mav.addObject("snippet", snippet);
                     mav.addObject("message", message);
                 }
             } else {
                 LOGGER.info("snippet doesn't exist");
                 String message = "I'm sorry, " + snippetId + " was not found.";
                 mav.setViewName("snippet/snippetFailure");
                 mav.addObject("message", message);
             }
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("Something failed while trying to display " + snippetId + ".", ex);
             mav.addObject("message", "Something failed while trying to display " + snippetId + ".");
             mav.setViewName("snippet/snippetFailure");
         }
         return mav;
     }
 
     /**
      * Displays the basic info for a given Snippet.
      * @param snippetId The snippet you wish to display.
      * @return ModelAndView mav
      */
     @RequestMapping(value = "id/{snippetId}", method = RequestMethod.GET)
     public ModelAndView displaySnippet(@PathVariable Long snippetId) {
         LOGGER.debug("trying to display " + snippetId);
         ModelAndView mav = new ModelAndView();
         try {
             Account account = getContextAccount();
             Snippet snippet = snippetService.loadById(snippetId);
             LOGGER.info(snippet);
             if (snippet != null) {
                  if (account != null && snippet.getAuthor().getUsername().equals(account.getUsername())) {
                      // user logged in and owns it.
                      mav.addObject("editlink", "<div style='float:right;'><a href='/snippet/edit/" + snippet.getSnippetId() + "'>[edit]</a></div>");
                      mav.addObject("snippet", snippet);
                      mav.setViewName("snippet/viewSnippet");
                  } else if (snippet.getPublished()) {
                      // user not logged in or doesn't own it
                      mav.addObject("snippet", snippet);
                      mav.setViewName("snippet/viewSnippet");
                  } else {
                      //  note is not published
                      String message = "I'm sorry, " + snippetId + " is not available.";
                      LOGGER.error(message);
                      mav.setViewName("snippet/snippetFailure");
                      mav.addObject("message", message);
                 }
             } else {
                 String message = "I'm sorry, " + snippetId + " was not found.";
                 LOGGER.error(message);
                 mav.setViewName("snippet/snippetFailure");
                 mav.addObject("message", message);
             }
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("Something failed while trying to display " + snippetId + ".", ex);
             mav.addObject("message", "Something failed while trying to display " + snippetId + ".");
         }
         return mav;
     }
 
     /**
      * This will display the user's snippets.
      * @return ModelAndView mav
      */
     @RequestMapping("my")
     public ModelAndView showMySnippets() {
         LOGGER.info("showing user snippets");
         ModelAndView mav = new ModelAndView();
         try {
             Account account = getContextAccount();
             if (account  != null) {
                 mav.addObject("account", account);
                 mav.setViewName("snippet/mySnippets");
             } else {
                 String message = "Odd, I couldn't find your account.";
                 LOGGER.error(message);
                 mav.addObject("message", message);
                 mav.setViewName("snippet/snippetFailure");
             }
         } catch (Exception ex) {
             String message = "Something failed while trying to display user snippets.";
             LOGGER.error(message, ex);
             mav.addObject("message", message);
             mav.setViewName("snippet/snippetFailure");
         }
         return mav;
     }
 
     /**
      * This will display the snippet creation form.
      * @return ModelAndView mav
      */
     @RequestMapping("create")
     public ModelAndView createSnippetForm() {
         LOGGER.info("showing the createSnippetForm");
         ModelAndView  mav = new ModelAndView();
         mav.setViewName("snippet/editSnippet");
         mav.addObject("editSnippetForm", new EditSnippetForm());
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

 package com.morgajel.spoe.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.morgajel.spoe.model.Account;
 import com.morgajel.spoe.model.Role;
 
 import com.morgajel.spoe.service.AccountService;
 import com.morgajel.spoe.service.RoleService;
 import com.morgajel.spoe.service.SnippetService;
 import com.morgajel.spoe.web.EditAccountForm;
 import com.morgajel.spoe.web.RegistrationForm;
 import com.morgajel.spoe.web.SetPasswordForm;
 
 /**
  * Controls all account interactions from changing passwords, registering and activating accounts, etc.
  */
 @Controller
 public class AccountController  extends MultiActionController {
 
     private VelocityEngine velocityEngine;
     private MailSender mailSender;
     private SimpleMailMessage templateMessage;
     @Autowired
     private AccountService accountService;
     @Autowired
     private RoleService roleService;
 
    private static final String REGISTRATION_TEMPLATE = "registrationEmail.vm";
     private static final String ACTIVATION_URL = "http://127.0.0.62:8080/account/activate/";
     private static final transient Logger LOGGER = Logger.getLogger(AccountController.class);
 
     /**
      * Activate account by comparing a given username and checksum against a given account.
      * Account must be disabled for this to work. Maps /activate/{username}/{checksum}
      * @return ModelAndView mav
      * @param username username name of user to activate
      * @param checksum hash of username, random password hash and enabled status
      * @param passform the password form you'll send to change the password.
      */
     @RequestMapping(value = "/activate/{username}/{checksum}", method = RequestMethod.GET)
     public ModelAndView activateAccount(@PathVariable String username, @PathVariable String checksum, SetPasswordForm passform) {
         //NOTE I understand why I need to pass passform on the way out, but what/how/why am I passing it in? where is it coming from?
         LOGGER.trace("trying to activate " + username + " with checksum " + checksum);
         ModelAndView mav = new ModelAndView();
         try {
             LOGGER.trace("attempting to load account of " + username);
 
             Account account = accountService.loadByUsernameAndChecksum(username, checksum);
             LOGGER.info(account);
             if (account != null) {
                 String calculatedChecksum = account.activationChecksum();
                 LOGGER.trace("compare given checksum " + checksum + " with calculated checksum " + calculatedChecksum);
                 if (!account.getEnabled()) {
                     LOGGER.info("Holy shit it worked:");
                     passform.setChecksum(checksum);
                     passform.setUsername(username);
                     mav.setViewName("account/activationSuccess");
                     mav.addObject("message", "a simple message");
                     mav.addObject("passform", passform);
                 } else {
                     LOGGER.info("account already enabled");
                     String message = "I'm sorry, this account has already been enabled.";
                     mav.setViewName("account/activationFailure");
                     mav.addObject("message", message);
 
                 }
             } else {
                 String message = "I'm sorry, that account doesn't exist, is already enabled, or the url was incomplete.";
                 mav.setViewName("account/activationFailure");
                 mav.addObject("message", message);
             }
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("damnit, something failed." + ex);
             mav.setViewName("account/activationFailure");
             mav.addObject("message", "<!--" + ex + "-->");
         }
         return mav;
     }
     /**
      * Displays the given user's public information.
      * @param username username you wish to display.
      * @return ModelAndView mav
      */
     @RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
     public ModelAndView displayUser(@PathVariable String username) {
         LOGGER.debug("trying to display " + username);
         ModelAndView mav = new ModelAndView();
         try {
             Account account = accountService.loadByUsername(username);
             LOGGER.info(account);
             if (account != null && !username.equals("anonymousUser")) {
                 mav.addObject("message", username);
                 mav.setViewName("account/viewUser");
                 mav.addObject("account", account);
             } else {
                 LOGGER.info("account doesn't exist");
                 String message = "I'm sorry, " + username + " was not found.";
                 mav.setViewName("account/viewUser");
                 mav.addObject("message", message);
             }
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("damnit, something failed." + ex);
             mav.setViewName("account/activationFailure");
             mav.addObject("message", "Something failed while trying to display " + username);
         }
         return mav;
     }
 
     /**
      * Create an account with the given information, then send the user an activation email.
      * @return ModelAndView
      * @param registrationForm contains initial user information
      * @param result I don't know that bind results is needed.
      */
     @RequestMapping(value = "/register.submit", method = RequestMethod.POST)
     public ModelAndView createAccount(@ModelAttribute("registrationForm") RegistrationForm registrationForm, BindingResult result) {
         // TODO unit test
         ModelAndView mav = new ModelAndView();
         try {
             if (registrationForm.getEmail().equals(registrationForm.getConfirmEmail())) {
                 Account account = new Account();
                 account.importRegistration(registrationForm);
                 account.setHashedPassword(Account.generatePassword(Account.MAXLENGTH));
                 LOGGER.trace("password field set to '" + account.getPassword() + "', sending email...");
 
                 String url = ACTIVATION_URL + account.getUsername() + "/" + account.activationChecksum();
                 sendRegEmail(account, url);
                 LOGGER.info("Email sent, adding account " + account.getUsername());
                 accountService.addAccount(account);
 
                 //FIXME this role addition should be done in the account service I think.
                 Role reviewerRole = roleService.loadByName("ROLE_REVIEWER");
                 LOGGER.info("ready to add " + reviewerRole.getName() + " to account " + account);
                 account.addRole(reviewerRole);
 
                 LOGGER.info("created account " + account.getUsername());
                 accountService.saveAccount(account);
 
                 mav.setViewName("account/registrationSuccess");
                 mav.addObject("url", url);
                 mav.addObject("account", account);
             } else {
                 LOGGER.error("Email addresses did not match.");
                 mav.setViewName("account/registrationForm");
                 mav.addObject("message", "Sorry, your Email addresses didn't match.");
 
 
             }
 
         } catch (Exception ex) {
             // TODO catch actual errors and handle them
             // TODO tell the user wtf happened
             LOGGER.error("Message failed to send:" + ex);
             mav.setViewName("account/registrationForm");
             mav.addObject("message", "There was an issue creating your account."
                     + "Please contact the administrator for assistance.");
         }
         return mav;
     }
 
     /**
      * Displays the registration form for users to log in.
      * @param registrationForm the form needed to register
      * @return ModelAndView
      */
     @RequestMapping("/register")
     public ModelAndView getRegistrationForm(RegistrationForm registrationForm) {
         LOGGER.info("getregistrationForm loaded");
         ModelAndView  mav = new ModelAndView();
         mav.addObject("registrationForm", registrationForm);
         mav.setViewName("account/registrationForm");
         return mav;
     }
     /**
      * Displays the form for Editing your account.
      * @param eaForm edit account form
      * @return ModelAndView mav
      */
     @RequestMapping(value = "/edit")
     public ModelAndView editAccountForm(EditAccountForm eaForm) {
         ModelAndView  mav = new ModelAndView();
         Account account = getContextAccount();
         eaForm.loadAccount(account);
         mav.addObject("eaForm", eaForm);
         mav.setViewName("account/editAccountForm");
         return mav;
     }
     /**
      * Saves changes when editing your account.
      * @param eaForm edit account form
      * @return ModelAndView mav
      */
     @RequestMapping(value = "/edit.submit")
     public ModelAndView saveEditAccountForm(EditAccountForm eaForm) {
         ModelAndView  mav = new ModelAndView();
         Account account = getContextAccount();
         eaForm.loadAccount(account);
         //TODO Do stuff here.
         mav.addObject("eaForm", eaForm);
         mav.addObject("message", "your form has been submitted, but this is currently unimplemented...");
         mav.setViewName("account/editAccountForm");
         return mav;
     }
 
     /**
      * Takes the checksum and username and sets it to the password that the user provides.
      * Account is enabled if passwords match and username/checksum is found.
      * Will bounce you back to activationSuccess if you give it mismatched passwords.
      * @param passform password form
      * @return ModelAndView mav
      */
     @RequestMapping(value = "/activate.setpassword", method = RequestMethod.POST)
     public ModelAndView setPassword(SetPasswordForm passform) {
         ModelAndView  mav = new ModelAndView();
         if (passform.getPassword().equals(passform.getConfirmPassword())) {
             Account account = accountService.loadByUsernameAndChecksum(passform.getUsername(), passform.getChecksum());
             account.setEnabled(true);
             account.setHashedPassword(passform.getPassword());
             accountService.addAccount(account);
             LOGGER.info("set password, then display view." + passform);
             mav.setViewName("redirect:/");
         } else {
             LOGGER.info("Your passwords did not match, try again.");
             passform.setPassword("");
             passform.setConfirmPassword("");
             mav.setViewName("account/activationSuccess");
             //FIXME this should be an error
             mav.addObject("message", "Your passwords did not match, try again.");
             mav.addObject("passform", passform);
         }
         return mav;
     }
     /**
      * This is the default view for account, a catch-all for most any one-offs.
      * Will show user's account information in the future.
      * @return ModelAndView mav
      */
     @RequestMapping
     public ModelAndView defaultView() {
         LOGGER.info("showing the default view");
         ModelAndView  mav = new ModelAndView();
 
         String username = SecurityContextHolder.getContext().getAuthentication().getName();
         Account account = accountService.loadByUsername(username);
         mav.setViewName("account/view");
         mav.addObject("message", "show the default view for " + username);
         mav.addObject("account", account);
         return mav;
     }
 
     public MailSender getMailSender() {
         //TODO is this needed?
         return this.mailSender;
     }
 
     public SimpleMailMessage getTemplateMessage() {
         //TODO might be able to remove it and replace with reflection.
         return this.templateMessage;
     }
 
     public VelocityEngine getVelocityEngine() {
         //TODO might be able to remove it and replace with reflection.
         return this.velocityEngine;
     }
 
     /**
      * Sends Registration email to user which includes an activation link.
      * @param account user account to send an email
      * @param url URL to use for activation
      */
     public void sendRegEmail(Account account, String url) {
         LOGGER.info("trying to send email...");
 
         Map<String, String> model = new HashMap<String, String>();
         model.put("firstname", account.getFirstname());
         model.put("url", url);
 
         SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
 
         msg.setTo(account.getEmail());
         LOGGER.info("sending message to " + account.getEmail());
 
         msg.setText(VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, REGISTRATION_TEMPLATE, model));
         LOGGER.info(msg.getText());
 
         this.mailSender.send(msg);
     }
 
     public void setAccountService(AccountService pAccountService) {
         this.accountService = pAccountService;
     }
 
     public void setMailSender(MailSender pMailSender) {
         this.mailSender = pMailSender;
     }
 
     public void setRoleService(RoleService pRoleService) {
         this.roleService = pRoleService;
     }
 
     public void setTemplateMessage(SimpleMailMessage pTemplateMessage) {
         this.templateMessage = pTemplateMessage;
     }
 
     public void setVelocityEngine(VelocityEngine pVelocityEngine) {
         this.velocityEngine = pVelocityEngine;
     }
 
     public String getActivationUrl() {
         return ACTIVATION_URL;
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

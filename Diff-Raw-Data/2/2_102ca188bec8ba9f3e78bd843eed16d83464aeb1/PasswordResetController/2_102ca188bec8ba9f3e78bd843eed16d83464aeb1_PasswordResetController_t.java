 package org.antbear.tododont.web.controller.security;
 
 import org.antbear.tododont.backend.service.security.PasswordResetException;
 import org.antbear.tododont.backend.service.security.PasswordResetService;
 import org.antbear.tododont.web.beans.security.PasswordReset;
 import org.antbear.tododont.web.beans.security.PasswordResetAttempt;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.util.UriComponents;
 import org.springframework.web.util.UriComponentsBuilder;
 
 import javax.validation.Valid;
 
 @RequestMapping("/password-reset")
 @Controller
 public class PasswordResetController {
 
     private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
 
     public static final String CHANGE_PASSWORD_PATH = "password-reset/change/";
 
     @Value("${web.app.base.uri}")
     private String applicationBaseUri;
 
     @Autowired
     private PasswordResetService passwordResetService;
 
     public UriComponents getPasswordResetUriComponents() {
         return UriComponentsBuilder.fromUriString(this.applicationBaseUri + CHANGE_PASSWORD_PATH
                 + "{email}/{changeToken}").build();
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView showPasswordReset() {
         log.debug("GET");
         // The password is unused in a password reset attempt, pre fill it to make bean validation happy
         final PasswordResetAttempt passwordResetAttempt = new PasswordResetAttempt();
         return new ModelAndView("password-reset/start", "passwordResetAttempt", passwordResetAttempt);
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public ModelAndView PasswordReset(@Valid final PasswordResetAttempt passwordResetAttempt,
                                       final BindingResult bindingResult) throws PasswordResetException {
         log.debug("password reset request (POST) {}", passwordResetAttempt);
 
         if (bindingResult.hasErrors()) {
             log.warn("binding result has errors; returning to start page");
             return new ModelAndView("password-reset/start");
         } else {
             log.debug("Validating password reset request for {}", passwordResetAttempt);
             this.passwordResetService.validateInitialRequest(passwordResetAttempt);
             log.debug("OK, handing over password forget attempt to password reset service");
             this.passwordResetService.passwordResetAttempt(passwordResetAttempt, getPasswordResetUriComponents());
             return new ModelAndView("password-reset/done", "email", passwordResetAttempt.getEmail());
         }
     }
 
     @ExceptionHandler(PasswordResetException.class)
     public ModelAndView handlePasswordResetException(final PasswordResetException ex) {
         log.debug("ExceptionHandler handlePasswordResetException", ex);
         final ModelAndView modelAndView = new ModelAndView("password-reset/error");
         modelAndView.addObject("errorMessageKey", ex.getMessageKey());
         return modelAndView;
     }
 
     @RequestMapping(value = "change/{email}/{passwordResetToken}", method = RequestMethod.GET)
     public ModelAndView passwordResetForm(@PathVariable("email") final String email,
                                           @PathVariable("passwordResetToken") final String passwordResetToken)
             throws PasswordResetException {
         log.debug("Password reset change form requested for {} and token {}", email, passwordResetToken);
 
         this.passwordResetService.validateChangeAttempt(email, passwordResetToken);
         final PasswordReset passwordReset = new PasswordReset();
         passwordReset.setEmail(email);
         passwordReset.setPasswordResetToken(passwordResetToken);
         return new ModelAndView("password-reset/change/start", "passwordReset", passwordReset);
     }
 
     @RequestMapping(value = "change", method = RequestMethod.POST)
     public ModelAndView passwordReset(@Valid final PasswordReset passwordReset,
                                       final BindingResult bindingResult) throws PasswordResetException {
         log.debug("Password change requested for {}", passwordReset.getEmail());
 
         if (bindingResult.hasErrors()) {
             log.warn("binding result has errors; returning to change/start page");
             return new ModelAndView("password-reset/change/start");
         } else {
             log.debug("Validating password reset change request for {}", passwordReset.getEmail());
             this.passwordResetService.validateChangeAttempt(passwordReset.getEmail(), passwordReset.getPasswordResetToken());
             log.info("Giving password reset service a chance for {}", passwordReset);
             this.passwordResetService.passwordChange(passwordReset);
             return new ModelAndView("password-reset/chanage/done");
         }
     }
 }

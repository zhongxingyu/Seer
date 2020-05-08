 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.EmailService;
 import com.financial.pyramid.service.RegistrationService;
 import com.financial.pyramid.service.exception.UserConfirmOverdueException;
 import com.financial.pyramid.service.exception.UserNotFoundException;
 import com.financial.pyramid.service.validators.RegistrationFormValidator;
 import com.financial.pyramid.web.form.AuthenticationForm;
 import com.financial.pyramid.web.form.PageForm;
 import com.financial.pyramid.web.form.QueryForm;
 import com.financial.pyramid.web.form.RegistrationForm;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.List;
 
 /**
  * User: Danil
  * Date: 29.05.13
  * Time: 22:45
  */
 @Controller
 @RequestMapping("/user")
 public class UserController extends AbstractController {
 
     private final static Logger logger = Logger.getLogger(UserController.class);
 
     @Autowired
     private com.financial.pyramid.service.UserService userService;
 
     @Autowired
     private RegistrationService registrationService;
 
     @Autowired
     private AuthenticationManager authenticationManager;
 
     @Autowired
     private EmailService emailService;
 
     @Autowired
     private PasswordEncoder passwordEncoder;
 
     private Validator registrationFormValidator = new RegistrationFormValidator();
 
     @RequestMapping(value = "/checkLogin/{login}", method = RequestMethod.GET)
     public
     @ResponseBody
     String checkLogin(Model model, @PathVariable String login) {
         return String.valueOf(userService.checkLogin(login));
     }
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     public String registration(ModelMap model, @ModelAttribute("registration") final RegistrationForm registration) {
         model.addAttribute("page-name", "office");
         model.addAttribute("registration", registration);
         BeanPropertyBindingResult result = new BeanPropertyBindingResult(registration, "registration");
         registrationFormValidator.validate(registration, result);
         if (result.getErrorCount() > 0) {
             return "/tabs/office";
         }
         List<User> users = userService.findByLogin(registration.getLogin());
         if (users.size() > 0) {
             return "/tabs/office";
         }
         boolean tr = registrationService.registration(registration, true);
         if (!tr) return "tabs/user/registration-fail";
         return "tabs/user/registration-success";
     }
 
     @RequestMapping(value = "/authentication", method = RequestMethod.POST)
     public String authentication(ModelMap model, @ModelAttribute("authentication") final AuthenticationForm authentication) {
         model.addAttribute("page-name", "office");
         try {
             Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                     authentication.getName(),
                     authentication.getPassword())
             );
             SecurityContextHolder.getContext().setAuthentication(authenticate);
         } catch (AuthenticationException e) {
             logger.warn("Authentication failed: " + e.getMessage());
             model.addAttribute("registration", new RegistrationForm());
             return "/tabs/login";
         }
         logger.info("Successfully authenticated. Security context contains: " + SecurityContextHolder.getContext().getAuthentication());
         return "/tabs/office";
     }
 
     @RequestMapping(value = "/confirm", method = RequestMethod.GET, params = {"ui"})
     public String confirmed(ModelMap model, @RequestParam(value = "ui") String uuid) {
         model.addAttribute("page-name", "office");
         try {
             userService.confirm(uuid);
         } catch (UserNotFoundException e) {
             return "tabs/user/user-not-found";
         } catch (UserConfirmOverdueException e) {
             return "tabs/user/confirm-overdue";
         }
         model.addAttribute("authentication", new AuthenticationForm());
         return "/tabs/login";
     }
 
     @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
     public
     @ResponseBody
     PageForm list(ModelMap mode, @ModelAttribute("queryForm") final QueryForm queryForm) {
         return new PageForm<User>(userService.findByQuery(queryForm));
     }
 
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public String add(@ModelAttribute("registration") final RegistrationForm registration) {
         if (registration.getId() == null || registration.getId().isEmpty()) {
             registrationService.registration(registration, false);
         } else {
             userService.updateUser(registration);
         }
         return "redirect:/pyramid/admin/user_settings";
     }
 
     @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
     public String delete(@PathVariable("id") final Long id) {
         userService.deleteUser(id);
         return "redirect:/pyramid/admin/user_settings";
     }
 
     @RequestMapping(value = "/forgot", method = RequestMethod.GET)
     public String forgot(ModelMap model) {
         return "/tabs/forgot";
     }
 
     @RequestMapping(value = "/restore", method = RequestMethod.POST)
     public String restore(ModelMap model, @RequestParam(value = "email") String email) {
         List<User> users = userService.findByEmail(email.trim());
         boolean result = false;
         if (!users.isEmpty()) {
             User user = users.get(0);
             String newPassword = userService.createPassword(15);
             user.setPassword(passwordEncoder.encode(newPassword));
             userService.saveUser(user);
             String text = "Your new password is " + newPassword;
             result = emailService.sendToUser(email, text);
         }
         model.addAttribute("result", result);
        model.addAttribute("email", email);
         return "redirect:/user/forgot";
     }
 
     @RequestMapping(value = "/logout", method = RequestMethod.GET)
     public void logout() {
         SecurityContextHolder.clearContext();
     }
 }

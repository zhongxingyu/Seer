 package com.ffbit.bcrypt.web;
 
 import com.ffbit.bcrypt.domain.User;
 import com.ffbit.bcrypt.domain.UserRole;
 import com.ffbit.bcrypt.dto.SignUpForm;
 import com.ffbit.bcrypt.repository.UserRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.persistence.PersistenceException;
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 @RequestMapping("/signup")
 @Secured("isAnonymous()")
 public class SignUpController {
     private final String DEFAULT_REDIRECT = "redirect:/";
     private final String VIEW = "signup-form";
 
     private Logger logger = LoggerFactory.getLogger(getClass());
 
     @Autowired
     private UserRepository userRepository;
 
     @ModelAttribute("signUpForm")
     public SignUpForm createSignUpFormBean(Model model) {
         return new SignUpForm();
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String show() {
         return VIEW;
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String create(Model model, @Valid SignUpForm signUpForm,
                          BindingResult result) {
         boolean success = !result.hasErrors() && signUpUser(signUpForm, result);
 
         if (success) {
             return DEFAULT_REDIRECT;
         }
 
         logSignUpErrors(result);
         fillErrors(model, result);
 
         return VIEW;
     }
 
     private boolean signUpUser(SignUpForm signUpForm, BindingResult result) {
         User existent = userRepository.findByUsername(signUpForm.getUsername());
         ObjectError error = new ObjectError("username", "signup.page.form.error.username.taken");
 
         if (existent != null) {
             result.addError(error);
 
             return false;
         }
 
         User user = new User(signUpForm.getUsername(), signUpForm.getPassword());
         user.grantAuthority(UserRole.ROLE_USER);
 
         try {
            userRepository.sigUp(user);
             userRepository.flush();
         } catch (PersistenceException e) {
             logger.warn(e.getMessage(), e);
             result.addError(error);
 
             return false;
         }
 
         authorizeUser(user);
 
         return true;
     }
 
     private void authorizeUser(User user) {
         SecurityContext context = SecurityContextHolder.getContext();
         Authentication authentication =
                 new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
         context.setAuthentication(authentication);
     }
 
     // Just for fun
     private void fillErrors(Model model, BindingResult result) {
         List<ObjectError> errors = result.getAllErrors();
         List<String> errorMessages = new ArrayList<String>(errors.size());
 
         for (ObjectError e : errors) {
             errorMessages.add(e.getDefaultMessage());
         }
 
         model.addAttribute("errors", errorMessages);
     }
 
     private void logSignUpErrors(BindingResult validationResult) {
         logger.info("Sing Up errors");
 
         for (ObjectError e : validationResult.getAllErrors()) {
             logger.info("sing up user error", e);
         }
     }
 
 }

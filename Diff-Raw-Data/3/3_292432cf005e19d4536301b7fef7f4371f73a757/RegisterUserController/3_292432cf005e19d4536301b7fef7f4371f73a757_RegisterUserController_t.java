 package com.mops.registrar.web.user.page;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.mops.registrar.entities.user.User;
 import com.mops.registrar.services.user.UserService;
 import com.mops.registrar.web.user.validator.UserValidator;
 
 /**
  * Web controller that handles registering new {@link User}s
  * 
  * @author dylants
  * 
  */
 @Controller
 @RequestMapping(value = "/user/register")
 public class RegisterUserController {
     @Autowired
     private UserService userService = null;
     @Autowired
     private UserValidator userValidator = null;
 
     /**
      * Configures the {@link UserValidator} to be used when validating {@link User} type objects.
      * 
      * @param binder
      */
     @InitBinder
     protected void initBinder(WebDataBinder binder) {
         binder.setValidator(this.userValidator);
     }
 
     /**
      * Displays a view used to register a {@link User}
      * 
      * @param model
      *            Contains information used by the view
      * @return The JSP used to register the user
      */
     @RequestMapping(method = RequestMethod.GET)
     public String registerUser(Model model) {
         User user = new User();
         model.addAttribute("user", user);
         model.addAttribute("heading", "Welcome! Please register");
         model.addAttribute("submitButtonText", "Register");
         return "user/userForm";
     }
 
     /**
      * Processes the register user request, registering the user and displaying a view based on the result.
      * 
      * @param user
      *            The {@link User} to register
      * @param bindingResult
      *            The result of the binding of the user input to the {@link User} object
      * @param model
      *            Contains information used by the view
      * @return The JSP used to display the next page
      */
     @RequestMapping(method = RequestMethod.POST)
     public String processRegisterUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
         // first cover binding errors (invalid input)
         if (bindingResult.hasErrors()) {
             // return them back to the registration page
            model.addAttribute("user", user);
            model.addAttribute("heading", "Welcome! Please register");
            model.addAttribute("submitButtonText", "Register");
             return "user/userForm";
         }
 
         // else add the user to our registry
         this.userService.addUser(user);
         model.addAttribute("user", user);
         return "user/registrationSuccess";
     }
 
 }

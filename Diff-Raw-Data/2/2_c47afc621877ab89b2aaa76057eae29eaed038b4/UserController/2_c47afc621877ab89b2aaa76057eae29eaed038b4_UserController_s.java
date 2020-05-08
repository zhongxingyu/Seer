 package edu.chl.dat076.foodfeed.controller;
 
 import edu.chl.dat076.foodfeed.exception.ResourceNotFoundException;
 import edu.chl.dat076.foodfeed.model.entity.User;
 import edu.chl.dat076.foodfeed.model.flash.*;
 import edu.chl.dat076.foodfeed.model.service.UserService;
 import edu.chl.dat076.foodfeed.util.EncoderUtil;
 import javax.servlet.http.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.authentication.*;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.WebAuthenticationDetails;
 import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 /**
  * Serves Registration and view of user profiles.
  *
  */
 @Controller
 @RequestMapping("/user")
 public class UserController {
 
     @Autowired
     @Qualifier("org.springframework.security.authenticationManager")
     protected AuthenticationManager authenticationManager;
     @Autowired
     private UserService userService;
     private static final Logger logger = LoggerFactory
             .getLogger(UserController.class);
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
     public String doRegister(Model model, @Validated User usr, BindingResult result, HttpServletRequest request, HttpServletResponse response, RedirectAttributes ra) {
 
         if (result.hasErrors()) {
             model.addAttribute("flash", new FlashMessage((result.getFieldError().getDefaultMessage()), FlashType.ERROR));
             return "user/register";
 
         } else {
             try {
                 userService.create(usr);
             } catch (DataIntegrityViolationException e) {
                 model.addAttribute("flash", new FlashMessage(
                         "User name already taken, try another.", FlashType.ERROR));
                 return "user/register";
             }
             logger.info("Password after DB create: " + usr.getPassword());
             UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usr.getUsername(), usr.getPassword());
 
             token.setDetails(new WebAuthenticationDetails(request));
             Authentication auth = authenticationManager.authenticate(token);
             SecurityContextHolder.getContext().setAuthentication(auth);
             request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
             
             ra.addFlashAttribute("flash", new FlashMessage("You were automatically logged in", FlashType.INFO));
             
             return "redirect:../";
         }
     }
 
     /*
      * Form for registering a new user
      */
     @RequestMapping(value = "/register", method = RequestMethod.GET)
     public String getRegisterForm(Model model) {
         model.addAttribute("user", new User());
         return "user/register";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String showById(@PathVariable String id, Model model) {
 
 
         logger.info("showing user with id: " + id);        
         User usr = userService.find(id);
         
 
         boolean showOptions = false;
         
         try{
             User loggedInUser = getLoggedInUser();
             showOptions = loggedInUser.getId().equals(id);
         } catch(ResourceNotFoundException exc) {
             // that's OK, user may not be logged in.
         }
         
         
         logger.info("showing user with id: " + id);
         model.addAttribute("showOptions", showOptions);
         model.addAttribute("user", userService.find(id));
         model.addAttribute("recipes", usr.getRecipes());
         
         return "user/show";
     }
     
     @Secured("ROLE_USER")
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String show(Model model) {
         logger.info("showing the authenticated user");
         User usr = getLoggedInUser();
         
         model.addAttribute("showOptions", true);
         model.addAttribute("user", usr);
         model.addAttribute("recipes", usr.getRecipes());
 
         return "user/show";
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.GET)
     public String loginForm() {
         logger.info("Showing form to log in.");
         return "user/login";
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.POST)
     public String doLogin(Model model, @RequestParam("username") String username, @RequestParam("password") String pass, HttpServletRequest request) {
         User cand;
         try {
             cand = userService.find(username);
         } catch (ResourceNotFoundException exc) {
             model.addAttribute("flash", new FlashMessage("User does not exist", FlashType.ERROR));
             return "user/login";
         }
 
         if (!EncoderUtil.matches(pass, cand.getPassword())) {
             // No match!
             model.addAttribute("flash", new FlashMessage("Wrong password", FlashType.ERROR));
             return "user/login";
         } else {
             UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, pass);
 
             token.setDetails(new WebAuthenticationDetails(request));
             Authentication auth = authenticationManager.authenticate(token);
             SecurityContextHolder.getContext().setAuthentication(auth);
             request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
 
             return "redirect:../";
         }
     }
     
     @Secured("ROLE_USER")
     @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String getEditForm(@PathVariable String id, Model model) {
         User user = getLoggedInUser();
         
         model.addAttribute("newPass", new String());
         model.addAttribute("oldPass", new String());
         model.addAttribute("user", user);
 
         return "user/edit";
     }
     
     @Secured("ROLE_USER")
     @RequestMapping(value = "/edit", method = RequestMethod.POST)
     public String doEdit(Model model, @RequestParam("newPass") String newPass, @RequestParam("oldPass") String oldPass, RedirectAttributes ra) {
 
 
         User user = getLoggedInUser();
         logger.info("Trying to set new password for user " + user.getId());
         if (EncoderUtil.matches(oldPass, user.getPassword())) {
             logger.info("Encoded oldPass was the same as stored hash, setting password");
             user.setPassword(EncoderUtil.encode(newPass));
 
             userService.update(user);
 
             ra.addFlashAttribute("flash", new FlashMessage("Password was updated", FlashType.INFO));
             return "redirect:/user";
         } else {
             model.addAttribute("flash", new FlashMessage("Confirmation password was not correct", FlashType.ERROR));
             return "user/edit";
         }
     }
     
     private User getLoggedInUser() throws ResourceNotFoundException {
         return userService.find(SecurityContextHolder.getContext().getAuthentication().getName());
     }
 }

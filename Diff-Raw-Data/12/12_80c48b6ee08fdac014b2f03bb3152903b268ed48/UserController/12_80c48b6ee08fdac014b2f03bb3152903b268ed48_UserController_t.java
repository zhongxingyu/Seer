 package fi.helsinki.cs.controller;
 
 import fi.helsinki.cs.dao.UserDao;
 import fi.helsinki.cs.model.User;
 import fi.helsinki.cs.validator.UserValidator;
 import java.security.Principal;
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class UserController {
         
         /**
          * Binds UserValidator to this class. User is then automatically validated by UserValidator when "/addUser" is called. 
          * 
          */
         @InitBinder
         protected void initBinder(WebDataBinder binder) {
             binder.setValidator(new UserValidator());
         }
         
         @Autowired
         PasswordEncoder passwordEncoder;
         
 	@Autowired
 	private UserDao userDao;
 
         /**
          * Handles "/register" calls. 
          * 
          * @return Returns register.jsp with empty User object.
          */
         @RequestMapping("/register")
 	public ModelAndView reqister() {
             return new ModelAndView("register", "user", new User());
 	}
         
         /**
          * Adds user to database. Method also encrypts received password.
          * 
          * @param user
          * @param result
          * @return If there was validation errors, return to register.jsp with errors, otherwise return to login.jsp.
          */
         @RequestMapping(value = "/addUser", method = { RequestMethod.PUT, RequestMethod.POST })
         public String addUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
             if (result.hasErrors()) {
                 return "register";
             } else {
                 String password = user.getPassword();
                 String passwordSha = passwordEncoder.encodePassword(password, user.getEmail());
                 user.setPassword(passwordSha);
                 //TODO: Check wheter user exists
                 User user2 = userDao.findByEmail(user.getEmail());
                if (user2!=null) { //User exists
                     if (user2.getStatus().equals(User.INACTIVE_USER)) { //User is inactive, then reactivate 
                         user2.setPassword(passwordSha); //Update password
                         user2.setName(user.getName()); //Update name (might have changed)
                         user2.setStatus(User.ACTIVE_USER);
                         userDao.save(user2);
                     } else { //User already exists
                         result.rejectValue("email", "email.same");
                         return "register";
                     }
                 } else { //New user
                     userDao.save(user);
                 }
                 return "login";
             }           
         }
         
         /**
          * Handles "/userdata" calls. 
          * 
          * @return Returns userdata.jsp with User object in its model
          */
         @RequestMapping("/userdata")
 	public ModelAndView userdata(Principal principal) {
             User user = userDao.findByEmail(principal.getName());
             return new ModelAndView("userdata", "user", user);
 	}
         
         /**
          * Handles "/userdata" calls. 
          * 
          * @return Returns userdata.jsp with User object in its model
          */
         @RequestMapping("/deleteUser/{userId}")
 	public String deleteUser(Principal principal, @PathVariable Long userId) {
             User user = userDao.findByEmail(principal.getName());
             if (user.getId().equals(userId) || user.getRole().equals(User.ROLE_ADMIN)) {
                 userDao.delete(userId);
                 if (user.getRole().equals(User.ROLE_ADMIN)) {
                     return "redirect:../admin";
                 } else {
                     return "redirect:../login";
                 }
             }
             return "redirect../start";
 	}
         
         /**
          * Modify user's data in database. Method also encrypts received password.
          * 
          * @param user
          * @param result
          * @return If there was validation errors, return to userdata.jsp with errors, otherwise return to start.jsp.
          */
         @RequestMapping(value = "/modifyUser", method = { RequestMethod.PUT, RequestMethod.POST })
         public String modifyUser(@Valid @ModelAttribute("user") User user, BindingResult result) {
             if (result.hasErrors()) {
                 return "userdata";
             } else {
                 String password = user.getPassword();
                 String passwordSha = passwordEncoder.encodePassword(password, user.getEmail());
                 user.setPassword(passwordSha);
                 //TODO: Check wheter new user email exists
                 User user2 = userDao.findByEmail(user.getEmail());
                 if (user2!=null) { //User exists
                     if (user2.getStatus().equals(User.INACTIVE_USER)) { //User is inactive, then reactivate 
                         user2.setPassword(passwordSha); //Update password
                         user2.setName(user.getName()); //Update name (might have changed)
                         user2.setStatus(User.ACTIVE_USER);
                         userDao.save(user2);
                     } else { //User already exists
                         result.rejectValue("email", "email.same");
                         return "userdata";
                     }
                 } else { //New user
                     userDao.save(user);
                 }
                 return "redirect:j_spring_security_logout";
             }           
         }
 
 
 }

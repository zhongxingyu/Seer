 package de.hliebau.tracktivity.presentation;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import de.hliebau.tracktivity.domain.User;
 import de.hliebau.tracktivity.service.UserService;
 
 @Controller
 public class UserController {
 
 	@Autowired
 	private PasswordEncoder passwordEncoder;
 
 	@Autowired
 	private UserService userService;
 
 	@RequestMapping(value = "/users/{username}", method = RequestMethod.GET)
 	public String displayUserProfile(@PathVariable String username, Model model) {
 		User user = userService.retrieveUser(username, true);
 		if (user != null) {
 			model.addAttribute(user);
 		}
 		return "user";
 	}
 
 	@RequestMapping(value = "/users")
 	public String listUsers(Model model) {
 		model.addAttribute(userService.getAllUsers());
 		return "users";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String registerUser(Model model) {
 		model.addAttribute(new User());
 		return "register";
 	}
 
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public String registerUser(@Valid User user, BindingResult bindingResult) {
 		if (!bindingResult.hasErrors()) {
 			try {
 				String plainPassword = user.getPassword();
 				user.setPassword(passwordEncoder.encodePassword(plainPassword, user.getUsername()));
 				userService.createUser(user);
 			} catch (DataIntegrityViolationException e) {
 				bindingResult.addError(new FieldError("user", "username", "Please choose another username, <em>"
 						+ user.getUsername() + "</em> is already taken."));
 			}
 		}
 		if (bindingResult.hasErrors()) {
 			return "register";
 		}
		return "redirect:users/" + user.getUsername();
 	}
 
 }

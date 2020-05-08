 package edu.rit.taskers.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
 
 import edu.rit.taskers.command.NewUserCommand;
 import edu.rit.taskers.data.NewUser;
 import edu.rit.taskers.persistence.UserDao;
 
 /**
  * Handles requests with a user account.
  */
 @Controller
 @RequestMapping("/account")
 public class AccountController {
 
 	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
 
     @Autowired
     private UserDao userDao;
 
 	/**
	 * Create New Account page requested
 	 */
	@RequestMapping(value="/new", method=RequestMethod.GET)
	public ModelAndView getCreateAccountPage() {
		ModelAndView newAccountPage = new ModelAndView("newaccount");
		return newAccountPage;
 	}
 	
 	/**
 	 * Create a new account given details
 	 * @return if Account was created successfully
 	 */
 	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public @ResponseBody String createNewUser(@RequestParam(value="login") String login,
 			@RequestParam(value="password") String password,
 			@RequestParam(value="name") String name,
 			@RequestParam(value="phone") String phone,
 			@RequestParam(value="email") String email,
 			@RequestParam(value="role") String role,
 			@RequestParam(value="bio") String bio,
 			@RequestParam(value="pictureURL") String pictureURL){
 		
 		if ( isEmpty( login ) ) {
 			return "Please specify a username.";
 		} else if ( isEmpty( password ) ) {
 			return "Please specify a password.";
 		} else if ( isEmpty( email ) ) {
 			return "Please specify an email.";
 		} else if ( isEmpty( phone ) ) {
 			return "Please specify a phone number.";
 		}
 
 		// Create the NewUser object and persist it
 		NewUser newuser = new NewUser();
 		newuser.setLogin(login);
 		newuser.setPassword(password);
 		newuser.setName(name);
 		newuser.setEmail(email);
 		newuser.setPhone(phone);
 		newuser.setRole(role);
 		newuser.setBio(bio);
 		newuser.setPictureURL(pictureURL);
 		
 		NewUserCommand command = new NewUserCommand(newuser);

		//TODO error checking
 		command.execute();		
 		return "User successfully created!"; 
 	}
 	
 	/**
 	 * Retrieve whether or not the requested account email is taken already
 	 */
 	@RequestMapping(value="/{email}", method=RequestMethod.GET)
 	public @ResponseBody boolean isEmailAvailable(@RequestParam String email) {
 		//TODO figure out email format validation location (probably client side)
 		logger.info("Checking if email address is available [email:" + email + "]");
 
 		//TODO
 		return true;
 	}
 
 	/**
 	 * String isEmpty helper method
 	 */
 	private boolean isEmpty(String s) {
 		return ( s == null ) || ( "".equals(s) ) || ( s.length() == 0 );
 	}
 }

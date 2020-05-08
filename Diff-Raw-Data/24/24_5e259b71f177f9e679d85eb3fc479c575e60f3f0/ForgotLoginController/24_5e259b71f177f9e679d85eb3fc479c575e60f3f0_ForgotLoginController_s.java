 package edu.unlv.cs.socialwebspider.web;
 
 import java.util.Random;
 
 import javax.persistence.TypedQuery;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edu.unlv.cs.socialwebspider.domain.User;
 
 /**
  * Controller that controls the retreival of the user's login information
  * 
  * @author Ryan
  */
 @RequestMapping("/forgotlogin/**")
 @Controller
 public class ForgotLoginController {
 
 	@Autowired
 	private ForgotLoginValidator validator;	// Sets up a validator
 	
 	@Autowired 
 	transient MailSender mailSender;		// Sets up a mail sender
 	
 	@Autowired
 	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;	// Sets up a password encryptor
 	
 	/**
 	 * Sets up a new forgotloginform model
 	 * 
 	 * @return The URL to the jspx
 	 */
 	@ModelAttribute("forgotLoginForm")
 	public ForgotLoginForm fromBackingObject() {
 		return new ForgotLoginForm();
 	}
 	
 	/**
 	 * Returns the user a pre filled out forgotloginform
 	 * 
 	 * @param model The MVC model
 	 * @param form A pre filled out form
 	 * @return The URL to the jspx
 	 */
 	@RequestMapping(method = RequestMethod.GET)
 	public String createForm(Model model, ForgotLoginForm form) {
 		model.addAttribute("form", form);
 		return "forgotlogin/index";
 	}
 	
 	/**
 	 * Attempts to send the user their login information
 	 * 
 	 * @param form The form the user has filled out
 	 * @param result The results of the form (errors)
 	 * @param model The MVC model
 	 * @param request The HTTP request
 	 * @return The URL to the jspx
 	 */
 	@RequestMapping(method = RequestMethod.POST)
 	public String create(@Valid ForgotLoginForm form, BindingResult result, Model model, HttpServletRequest request) {
 		validator.validate(form, result);	// Validates provided information
 		
 		// Returns the form with errors found
 		if(result.hasErrors()) {
 			return createForm(model, form);
 		}
 		
 		String emailAddress = form.getEmailAddress();	// Gets the email from the form
 		TypedQuery<User> query = User.findUsersByEmailAddress(emailAddress);	// Stores the query for the user
 		User targetUser = query.getSingleResult();	// Stores the user found with the query
 		Random random = new Random(System.currentTimeMillis());	// Generates a random number
		String newPassword = "" + (random.nextInt() * -1);	// Sets the string for the new password
 		
 		// If reset is specified
 		if(form.getResetPassword())
 		{
 			targetUser.setPassword(messageDigestPasswordEncoder.encodePassword(newPassword, null));	// Encrypts and sets the user password
 			targetUser.merge();		// Merges the local user with the database user
 		}
 		
 		// Sends the user an e-mail
 		SimpleMailMessage mail = new SimpleMailMessage();
 		mail.setTo(targetUser.getEmailAddress());
 		mail.setSubject("Social Web Spider Login Retrevial");
 		
 		// If the user requested a password reset send that message message
 		if(form.getResetPassword())
 		{
 			mail.setText("Hello, here is your requested login and password reset.\n\nUsername: " + targetUser.getUsername() + "\nNew Password: " + newPassword + "\n\nThanks, Social Web Spider");
 			mailSender.send(mail);
 			return "forgotlogin/thanks";
 		}
 		
 		// Send the user just an e-mail
 		mail.setText("Hello, here is your requested login.\n\nUsername: " + targetUser.getUsername() + "\n\nThanks, Social Web Spider");
 		mailSender.send(mail);
 		return "forgotlogin/thanks";
 	}
 }

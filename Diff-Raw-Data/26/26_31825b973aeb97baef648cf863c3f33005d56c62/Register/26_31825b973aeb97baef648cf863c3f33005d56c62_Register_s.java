 package controllers;
 
 import models.User;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import play.data.validation.Valid;
 import play.libs.Mail;
 import play.mvc.Controller;
 import util.Config;
 
 public class Register extends Controller {
 
 	private static final String EMAIL_SUBJECT = "Remind me event registration";
 
 	public static void form() {
 		User user = new User();
 		render(user);
 	}
 
 	public static void perform(@Valid User user) {
 		if (validation.hasErrors()) {
 			params.flash(); // add http parameters to the flash scope
 			validation.keep(); // keep the errors for the next request
 			form();
 		}
 		user.hashPassword();
 		user.generateConfirmationToken();
		user.save();

		sendConfirmationEmail(user);
		flash.success("Check your mail box, an email as been send to confirm your registration");

		Home.home();
 	}
 
	private static void sendConfirmationEmail(User user) {
 		try {
 			SimpleEmail email = new SimpleEmail();
 			email.setFrom(Config.getEmailFromAddress(), Config.getEmailFromName());
 			email.addTo(user.email);
 			email.setSubject(EMAIL_SUBJECT);
 			email.setMsg(createRegistrationMessage(user));
			Mail.send(email);
 		} catch (EmailException e) {
			// TODO ....
			throw new RuntimeException(e);
 		}
 	}
 
 	private static String createRegistrationMessage(User user) {
 		// TODO retrieve public URL, voir pour encoder l'url
 		return "http://localhost:9000/register/confirm/" + user.email + "/" + user.confirmationToken;
 	}
 
 	public static void confirmRegistration(String email, String token) {
 		User user = User.findByEmail(email);
 		if (user != null && user.confirmationToken.equals(token)) {
 			user.state = User.State.CONFIRMED;
 			user.save();
 			flash.success("Your are now a remindmeevent user!");
 			session.put("username", user.email);
 			Home.home();
 		}
 
 	}
 }

 package controllers;
 
 import static play.data.Form.form;
 import static ui.tags.Messages.info;
 import static ui.tags.MessagesHelper.invalidForm;
 import models.User;
 import play.data.Form;
 import play.data.validation.Constraints.Required;
 import play.mvc.Controller;
 import play.mvc.Result;
 import scala.Option;
 import services.UserService;
 import util.security.PasswordUtil;
 import util.security.SessionUtil;
 
 public class Authentication extends Controller {
 
 	public static class Registration {
 		@Required(message = "User name is mandatory.")
 		public String username;
 		@Required(message = "Password is mandatory. Think about your security.")
 		public String password;
 		@Required(message = "Password confirmation is mandatory")
 		public String passwordConfirm;
 		public String email;
 	}
 
 	public static class Login {
 		@Required(message = "Please enter your user name")
 		public String username;
 		@Required(message = "Please enter your password")
 		public String password;
 	}
 
 	private static Form<Registration> FORM_REGISTRATION = form(Registration.class);
 	private static Form<Login> FORM_LOGIN = form(Login.class);
 
 	public static Result register() {
 		return ok(views.html.register.render(FORM_REGISTRATION));
 	}
 
 	public static Result saveRegistration() {
 		Form<Registration> filledForm = FORM_REGISTRATION.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			return invalidForm(views.html.register.render(filledForm));
 		}
 
 		// Register user.
 		Registration registration = filledForm.get();
 		if (!registration.password.equals(registration.passwordConfirm)) {
 			filledForm.reject("password", "Passwords do not match");
 			filledForm.reject("passwordConfirm", "");
 			return invalidForm(views.html.register.render(filledForm));
 		}
 		User user = getUserFromRegistration(registration);
 		UserService.registerUser(user);
 
 		// Automatically log user in.
 		SessionUtil.setUser(user);
 
 		// Redirect to home page.
 		info("Welcome " + user.username + "! Thank you for your registration.");
 		return redirect(routes.Application.index());
 	}
 
 	private static User getUserFromRegistration(Registration registration) {
 		User user = new User();
 		user.registered = true;
 		user.username = registration.username;
 		user.email = registration.email;
 		user.passwordHash = PasswordUtil.hashPassword(registration.username,
 				registration.password);
 		return user;
 	}
 
 	public static Result login() {
 		return ok(views.html.login.render(FORM_LOGIN));
 	}
 
 	public static Result authenticate() {
 		Form<Login> formLogin = FORM_LOGIN.bindFromRequest();
 		if (formLogin.hasErrors()) {
 			return invalidForm(views.html.login.render(formLogin));
 		}
 
 		Login login = formLogin.get();
 		Option<User> optUser = UserService.authenticate(login.username,
 				login.password);
 
 		if (optUser.isEmpty()) {
 			formLogin.reject("username", "User name and password do not match");
 			formLogin.reject("password", "");
 			return invalidForm(views.html.login.render(formLogin));
 		}
 
 		User user = optUser.get();
 		SessionUtil.setUser(user);
 
 		// Redirect to main page.
 		info("Welcome " + user.username + "!");
 		return redirect(routes.Application.index());
 	}
 
 	public static Result logout() {
 		SessionUtil.clear();
		return Application.index();
 	}
 }

 package controllers;
 
 import static play.data.Form.form;
 import static ui.tags.MessagesHelper.invalidForm;
 import static util.user.message.Messages.info;
 import models.User;
 import play.data.Form;
 import play.data.validation.Constraints.Email;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Transactional;
 import play.mvc.Controller;
 import play.mvc.Result;
 import scala.Option;
 import services.UserService;
 import services.exception.user.AlreadyRegisteredUser;
 import ui.util.UIUtil;
 import util.security.PasswordUtil;
 import util.security.CurrentUser;
 import util.user.message.Messages;
 
 import com.google.common.base.Strings;
 
 import controllers.message.ControllerMessage;
 
 public class Authentication extends Controller {
 
 	public static class Registration {
 		@Required(message = "authentication.registration.username.mandatory")
 		public String username;
 		@Required(message = "authentication.registration.password.mandatory")
 		public String password;
 		@Required(message = "authentication.registration.password_confirm.mandatory")
 		public String passwordConfirm;
 		@Email(message = "user.constraint.email.format")
 		@Required(message = "authentication.registration.email.mandatory")
 		public String email;
 	}
 
 	public static class Login {
 		@Required(message = "authentication.login.username.mandatory")
 		public String username;
 		@Required(message = "authentication.login.password.mandatory")
 		public String password;
 	}
 
 	private static Form<Registration> FORM_REGISTRATION = form(Registration.class);
 	private static Form<Login> FORM_LOGIN = form(Login.class);
 
 	@Transactional
 	public static Result register(String returnUrl) {
 		return ok(views.html.user.register.render(FORM_REGISTRATION,
 				UIUtil.urlEncode(returnUrl)));
 	}
 
 	@Transactional
 	public static Result saveRegistration() {
 		String returnUrl = "/";
 		Form<Registration> filledForm = FORM_REGISTRATION.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			return invalidForm(views.html.user.register.render(filledForm,
 					returnUrl));
 		}
 
 		// Register user.
 		Registration registration = filledForm.get();
 		if (!registration.password.equals(registration.passwordConfirm)) {
 			filledForm.reject("password", "Passwords do not match");
 			filledForm.reject("passwordConfirm", "");
 			return invalidForm(views.html.user.register.render(filledForm,
 					returnUrl));
 		}
 		User user = getUserFromRegistration(registration);
 
 		try {
 			UserService.registerUser(user);
 		} catch (AlreadyRegisteredUser e) {
 			Messages.error(e.getMessageKey(), e.getParams());
 			return invalidForm(views.html.user.register.render(filledForm,
 					returnUrl));
 		}
 
 		// Automatically log user in.
 		CurrentUser.setUser(user);
 
 		// Redirect to home page.
 		info(ControllerMessage.REGISTRATION_THANKS, user.getDisplay());
 		if (Strings.isNullOrEmpty(returnUrl)) {
 			return redirect(routes.Application.index());
 		}
 		return redirect(UIUtil.fullUrlDecode(returnUrl));
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
 
 	@Transactional
 	public static Result login(String returnUrl) {
 		return ok(views.html.user.login.render(FORM_LOGIN, returnUrl));
 	}
 
 	@Transactional
 	public static Result authenticate(String url) {
 
 		Form<Login> formLogin = FORM_LOGIN.bindFromRequest();
 		if (formLogin.hasErrors()) {
 			return invalidForm(views.html.user.login.render(formLogin, url));
 		}
 
 		Login login = formLogin.get();
 		Option<User> optUser = UserService.authenticate(login.username,
 				login.password);
 
 		if (optUser.isEmpty()) {
 			formLogin.reject("username", "User name and password do not match");
 			formLogin.reject("password", "");
 			return invalidForm(views.html.user.login.render(formLogin, url));
 		}
 
 		User user = optUser.get();
 		CurrentUser.setUser(user);
 
 		// Redirect to page.
 		info(ControllerMessage.APPLICATION_WELCOME, user.getDisplay());
 		if (Strings.isNullOrEmpty(url)) {
 			return redirect(routes.Application.index());
 		}
 		return redirect(UIUtil.fullUrlDecode(url));
 	}
 
 	@Transactional
 	public static Result logout() {
		CurrentUser.clear();
 		return redirect(routes.Application.index());
 	}
 }

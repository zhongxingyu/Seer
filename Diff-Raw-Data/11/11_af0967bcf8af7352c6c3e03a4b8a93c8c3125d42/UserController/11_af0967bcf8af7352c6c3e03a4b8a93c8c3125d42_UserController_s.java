 package controllers;
 
 import com.avaje.ebean.Ebean;
 import controllers.util.PasswordHasher;
 import controllers.util.PasswordHasher.SaltAndPassword;
 import models.EMessages;
 import models.data.Link;
 import models.dbentities.UserModel;
 import models.mail.EMail;
 import models.mail.ForgotPwdMail;
 import models.user.AuthenticationManager;
 import models.user.Role;
 import models.user.UserType;
 import play.api.Play;
 import play.api.libs.Crypto;
 import play.data.Form;
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.mvc.Http.Context;
 import play.mvc.Result;
 import play.mvc.Results;
 import scala.math.BigInt;
 import views.html.commons.noaccess;
 import views.html.forgotPwd;
 import views.html.login.register;
 import views.html.login.registerLandingPage;
 import views.html.mimic.mimicForm;
 import views.html.login.resetPwd;
 
 import javax.mail.MessagingException;
 
 import java.math.BigInteger;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.InvalidKeySpecException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class receives all GET requests and based on there session identifier (cookie)
  * and current role in the system they will be served a different view.
  *
  * @author Sander Demeester, Ruben Taelman
  */
 
 public class UserController extends EController {
 
 	private static 		SecureRandom secureRandom = new SecureRandom();
 
 
 	private static final String EMAIL_PATTERN =
 			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
 
 	/**
 	 * This methode gets requested when the user clicks on "signup".
 	 *
 	 * @return Result page.
 	 */
 	public static Result signup() {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 		return ok(register.render(EMessages.get("register.title"),
 				breadcrumbs,
 				form(Register.class)
 				));
 	}
 
 	public static Result mimic() {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.mimic"), "/mimic"));
 
 		if (!AuthenticationManager.getInstance().getUser().hasRole(Role.MIMIC))
 			return ok(noaccess.render(breadcrumbs));
 
 		return ok(mimicForm.render(EMessages.get("app.mimic"), breadcrumbs, form(MimicForm.class)));
 	}
 
 	public static Result mimicExecute() {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.mimic"), "/mimic"));
 
 		if (!AuthenticationManager.getInstance().getUser().hasRole(Role.MIMIC))
 			return ok(noaccess.render(breadcrumbs));
 
 		Map<String, String[]> parameters = request().body().asFormUrlEncoded();
 		String id = parameters.get("id")[0];
 		UserModel userModel = Ebean.find(UserModel.class).where().eq("id", id).findUnique();
 		if (userModel == null) {
 			return badRequest(EMessages.get("error.mimic.cant_find_user"));
 		}
 
 		if (AuthenticationManager.getInstance().isUserLoggedIn(userModel.getID())) {
 			// The user that we are trying to mimic is logged into the system.
 			return badRequest(EMessages.get("error.mimic.user_logged_in"));
 		}
 		if (AuthenticationManager.getInstance().login(userModel, Context.current().request().cookies().get(
 				AuthenticationManager.COOKIENAME).value()) == null) {
 			return badRequest(EMessages.get("error.mimic.policy_deny"));
 		}
 		AuthenticationManager.getInstance().getUser().setMimickStatus(true);
 
 		return ok(Context.current().request().cookies().get(
 				AuthenticationManager.COOKIENAME).value());
 
 	}
 
 	/**
 	 * this methode is called when the user submits his/here register information.
 	 *
 	 * @return Result page
 	 */
 	public static Result register() {
 		// Bind play form request.
 		Form<Register> registerForm = form(Register.class).bindFromRequest();
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 
 		// If the form contains error's (specified by "@"-annotation in the class "Register" then this will be true.
 		if (registerForm.hasErrors()) {
 			flash("error", EMessages.get(EMessages.get("error.no_password")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		if (!registerForm.get().password.equals(registerForm.get().controle_passwd)) {
 			flash("error", EMessages.get(EMessages.get("register.password_mismatch")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// check if date is lower then current date
 		try {
 			Date birtyDay = new SimpleDateFormat("dd/MM/yyyy").parse(registerForm.get().bday);
 			Date currentDate = new Date();
 
 			if (birtyDay.after(currentDate)) {
 				flash("error", EMessages.get(EMessages.get("error.wrong_date_time")));
 				return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 			}
 		} catch (Exception e) {
 			flash("error", EMessages.get(EMessages.get("error.date")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 		// Check if the email adres is uniqe.
 		if (!registerForm.get().email.isEmpty()) {
 
 			if (Ebean.find(UserModel.class).where().eq(
 					"email", registerForm.get().email).findUnique() != null) {
 
 				flash("error", EMessages.get(EMessages.get("register.same_email")));
 				return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 			}
 		}
 
 		Pattern pattern = Pattern.compile("[^a-z -]", Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(registerForm.get().name);
 
 		// Check if full name contains invalid symbols.
 		if (matcher.find()) {
 			flash("error", EMessages.get(EMessages.get("error.invalid_symbols")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Compile new pattern to check for invalid email symbols.
 		// These are all the symbols that are allow in email addresses.
 		// Alle symbols are containd in character classes, so no need for escaping.
 		pattern = Pattern.compile("[^A-Za-z._+@0-9!#$%&'*+-/=?^_`{|}~]");
 		matcher = pattern.matcher(registerForm.get().email);
 
 		if (matcher.find()) {
 			flash("error", EMessages.get(EMessages.get("error.invalid_email")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Try to validate email, this check happens on the client side, but date can be send without using the form.
 		// If the check fails the user is presented with a error page.
 		try {
 			new SimpleDateFormat("yyyy/mm/dd").parse(registerForm.get().bday);
 		} catch (Exception e) {
 			flash("error", EMessages.get(EMessages.get("error.invalid_date")));
 			return badRequest(register.render((EMessages.get("register.title")), breadcrumbs, registerForm));
 		}
 
 		// Delegate create user to Authentication Manager.
 		String bebrasID = null;
 		try {
 			bebrasID = AuthenticationManager.getInstance().createUser(registerForm);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Return a register succes page.
 		return ok(registerLandingPage.render(EMessages.get("info.success"), new ArrayList<Link>(), bebrasID));
 	}
 
 	/**
 	 * This methode is called when the users clicks on "login".
 	 *
 	 * @return returns the users cookie.
 	 */
 	public static Result validate_login(String id, String password) throws Exception {
 		String cookie = "";
 		try {
 			//generate random id to auth user.
 			cookie = Integer.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextInt(100)));
 
 			//set the cookie. There really is no need for Crypto.sign because a cookie should be random value that has no meaning
 			cookie = Crypto.sign(cookie);
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 
 		// We do the same check here, if the input forms are empty return a error message.
 		if (id == "" || password == "") {
 			return badRequest(EMessages.get("register.giveinfo"));
 		} else {
 			int return_code = AuthenticationManager.getInstance().validate_credentials(id, password, cookie);
 			switch (return_code) {
 			case AuthenticationManager.VALID_LOGING: {
 				return ok(cookie);
 			}
 			case AuthenticationManager.INVALID_LOGIN: {
 				return badRequest(EMessages.get("error.login"));
 			}
 			case AuthenticationManager.DUPLICATED_LOGIN: {
 				return badRequest(EMessages.get("error.duplicated_login"));
 			}
 			default: {
 				return badRequest(EMessages.get("error.login"));
 
 			}
 			}
 		}
 	}
 
 
 	/**
 	 * Logout current user
 	 *
 	 * @return Result
 	 */
 	public static Result logout() {
 		AuthenticationManager.getInstance().logout();
 		return Results.redirect(routes.Application.index());
 	}
 
 	/**
 	 * @return Returns a scala template based on the type of user that is requesting the page.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Result landingPage() throws Exception {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
 
 		UserType type = AuthenticationManager.getInstance().getUser().getType();
 		if (UserType.ANON.equals(type)) {
 			return Results.redirect(routes.Application.index());
 		} else {
 			return ok(views.html.landing_page.render(
 					AuthenticationManager.getInstance().getUser(),
 					breadcrumbs
 					));
 		}
 	}
 
 	/**
 	 * Inline class that contains public fields for play forms.
 	 */
 	public static class Register {
 		@Required
 		public String name;
 		public String email;
 		@Required
 		@Formats.DateTime(pattern = "dd/MM/yyyy")
 		public String bday;
 		@Required
 		public String password;
 		@Required
 		public String controle_passwd;
 		@Required
 		public String gender;
 		@Required
 		public String prefLanguage;
 	}
 
 	/**
 	 * Inline class that contains public fields for play forms.
 	 */
 	public static class Login {
 		public String id;
 		public String password;
 	}
 
 	/**
 	 * This method is called when a user hits the 'Forgot Password' button.
 	 *
 	 * @return forgot_pwd page
 	 */
 	public static Result forgotPwd() {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link(EMessages.get("forgot_pwd.forgot_pwd"), "/forgotPwd"));
 		return ok(forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
 				breadcrumbs,
 				form(ForgotPwd.class)
 				));
 	}
 
 	public static Result forgotPwdSendMail() throws InvalidKeySpecException, NoSuchAlgorithmException {
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link("Home", "/"));
 		breadcrumbs.add(new Link(EMessages.get("forgot_pwd.forgot_pwd"), "/forgotPwd"));
 
 
 		Form<ForgotPwd> form = form(ForgotPwd.class).bindFromRequest();
 
 		if (form.hasErrors()) {
 			flash("error", EMessages.get(EMessages.get("forms.error")));
 			return badRequest(forgotPwd.render((EMessages.get("forgot_pwd.forgot_pwd")), breadcrumbs, form));
 		}
 		System.out.println(form.get().id);
 		String id = form.get().id;
 		UserModel userModel = Ebean.find(UserModel.class).where().eq("id", id).findUnique();
 
 		if(userModel == null){
			flash("error", EMessages.get("error.invalid_id"));
 			return badRequest(views.html.forgotPwd.render(EMessages.get("forgot_pwd.forgot_pwd"), breadcrumbs, form));
 		}
 
 		// There are two cases, the user has an email or the user does not has a email
 
 		if(!userModel.email.isEmpty()){
 			// Case 1
 			// Put reset token into database
 			userModel.reset_token = new BigInteger(130, secureRandom).toString(32);
 			Ebean.save(userModel);
 
 			String baseUrl = request().host() + "/reset_password?token=" + userModel.reset_token;
 			//TODO: delete
 			System.out.println(baseUrl);
 			// Prepare email
 			EMail mail = new ForgotPwdMail(userModel.email,userModel.id,"url");
 			try{
 				mail.send();
 				flash("success", EMessages.get("forgot_pwd.mail"));
 			}catch(MessagingException e){
 				flash("error", EMessages.get("forgot_pwd.nosent"));
 			}
 		}else{
 			// Case 2
 		}
 		return Results.redirect("/");
 	}
 
 	/**
 	 * The methode is called when the user clicks in the email on the provided link.
 	 * The purpose of this methode is to generate a new time-based token to verify the
 	 * form validity and the provide a new view for the users to enter his new password.
 	 * @param The generated token <url>?token=TOKEN
 	 * @return if the provided token is valid, this method will return a view for the user to set his new password.
 	 */
 	public static Result receivePasswordResetToken(String token){
 		//TODO: remove
 		System.out.println(token);
 
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 
 		UserModel userModel = Ebean.find(UserModel.class).where().eq("reset_token",token).findUnique();
 		if(userModel == null){
 			return ok(noaccess.render(breadcrumbs));
 		}else{
 			Form<ResetPasswordVerify> reset_form = form(ResetPasswordVerify.class);
 
 			// old token that is beining re-used?
 			// generate new token to send back to the client to make sure that we dont get a random request.
 			// it's import that time is included in this token.
 			String secure_token = new BigInteger(130,secureRandom).toString(32);
 
 			Long unixTime = System.currentTimeMillis() / 1000L;
 			secure_token = secure_token + unixTime.toString();
 
 			//TODO: remove
 			System.out.println("secure token :" + secure_token);
 
 			userModel.reset_token = secure_token;
 
 			// Save  new token.
 			userModel.save();
 
 			return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
 					breadcrumbs,
 					reset_form,
 					secure_token
 					));
 		}
 	}
 
 	/**
 	 * This methode is called when the users filled in his new password. 
 	 * The purpose of this methode is to calculate the new hash value of the password and store it into the database
 	 * @return
 	 * @throws Exception 
 	 */
 	public static Result resetPassword() throws Exception{
 		List<Link> breadcrumbs = new ArrayList<Link>();
 		breadcrumbs.add(new Link(EMessages.get("app.home"), "/"));
 		breadcrumbs.add(new Link(EMessages.get("app.signUp"), "/signup"));
 
 		Form<ResetPasswordVerify> form = form(ResetPasswordVerify.class).bindFromRequest();
 		String id = form.get().id;
 		String reset_token = form.get().reset_token;
 		UserModel userModel =         Ebean.find(UserModel.class).where().eq("id", id).findUnique();
 		if(userModel == null){
 			// If provided id is invalid. Abort reset proces;
 			userModel.reset_token = "";
 			userModel.save();
 			return ok(noaccess.render(breadcrumbs));
 		}
 
 		String reset_token_database = userModel.reset_token;
 
 		System.out.println("reset token client: " + reset_token);
 		System.out.println("reset token server: " + reset_token_database);
 		
 		//TODO: check timestamp on token from client.
 		if(reset_token.equals(reset_token_database)){
 
 			SaltAndPassword sp = PasswordHasher.generateSP(form.get().password.toCharArray());
 			String passwordHEX = sp.password;
 			String saltHEX = sp.salt;
 
 			userModel.password = passwordHEX;
 			userModel.hash = saltHEX;
 			
 			userModel.reset_token = "";
 			
 			userModel.save();
 
 			flash("success", EMessages.get("forgot_pwd.reset_succes"));
 			return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
 					breadcrumbs,
 					form,
 					reset_token
 					));
 		}else{
 			flash("error", EMessages.get("forgot_pwd.reset_succes"));
 			return ok(resetPwd.render(EMessages.get("forgot_pwd.forgot_pwd"),
 					breadcrumbs,
 					form,
 					reset_token
 					));
 		}
 	}
 
 	public static class ForgotPwd {
 		@Required
 		public String id;
 		public String email;
 	}
 
 	public static class MimicForm {
 		public String bebrasID;
 	}
 
 	public static class ResetPwd {
 		public String id;
 		public String pwd;
 	}
 	public static class ResetPasswordVerify{
 		public String id;
 		public String password;
 		public String confirmPassword;
 		public String reset_token;
 	}
 }

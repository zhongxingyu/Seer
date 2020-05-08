 package controllers;
 
 import com.google.inject.Inject;
 import com.typesafe.plugin.MailerAPI;
 import io.rampant.orchard.mongo.dao.UserDAO;
 import io.rampant.orchard.security.AuthManagerService;
 import models.LoginForm;
 import models.User;
 import org.joda.time.DateTime;
 import org.joda.time.Seconds;
 import play.Logger;
 import play.Play;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import views.html.auth.login;
 import views.html.auth.tokenSentEmailHtml;
 import views.html.auth.tokenSentEmailText;
 
 import java.util.Map;
 
 import static play.data.Form.form;
 
 /**
  * @author jonathan
  */
 public class Auth extends Controller {
 	private MailerAPI mailer;
 	private UserDAO userDAO;
 	private AuthManagerService tokenManager;
 
 	@Inject
 	public Auth(MailerAPI mailer, AuthManagerService tokenManagerService, UserDAO userDAO) {
 		this.mailer = mailer;
 		this.tokenManager = tokenManagerService;
 		this.userDAO = userDAO;
 	}
 
 	public Result admin() {
 		return ok(views.html.auth.admin.render(null));
 	}
 
 	public Result adminLogin() {
 		Map<String,String[]> data = request().body().asFormUrlEncoded();
 		if( !data.containsKey("password") || data.get("password").length == 0) {
 			return badRequest(views.html.auth.admin.render(null));
 		}
 		String password = data.get("password")[0];
 
 		if( !Play.application().configuration().getString("auth.admin.password").equalsIgnoreCase(password) ) {
 			return unauthorized(views.html.auth.admin.render("Invalid Password"));
 		}
 
 		// FIXME: Inject this.
 		Http.Context.current().session().put(Play.application().configuration().getString("auth.admin.sessionkey"), "true");
 		return redirect(routes.Application.index());
 	}
 
 	public Result login() {
 		return ok(views.html.auth.login.render(form(LoginForm.class)));
 	}
 
 	public Result sendToken() {
 		Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();
 
 		if( loginForm.hasErrors() ) {
 			return badRequest(login.render(loginForm));
 		}
 		else {
 			LoginForm loginUser = loginForm.get();
 			User user = userDAO.findByEmail(loginForm.get().email);
 			if( null == user || !user.canLogin() ) {
 				loginForm.reject("email", "Invalid email address.");
 				return forbidden(login.render(loginForm));
 			}
 			String authToken = tokenManager.generateAuthToken(loginUser.email, loginUser.rememberMe);
 
 			/**
 			 * TODO: Maybe this should be abstracted somewhere?
 			 */
 			mailer.setSubject("Orchard BBS Login Link");
 			mailer.addRecipient(loginForm.get().email);
 			mailer.addFrom(Play.application().configuration().getString("mailer.from"));
 			// TODO: Make this SSL Aware
 			String loginUrl = "http://" + request().host() + "/login/" + authToken;
 			mailer.send(
 				tokenSentEmailText.render(loginUrl).body(),
 				tokenSentEmailHtml.render(loginUrl).body()
 			);
 
 			return ok(views.html.auth.tokenSent.render(loginForm.get().email));
 		}
 	}
 
 	public Result logout() {
 		response().discardCookie(Play.application().configuration()
 			.getString("auth.cookie.name"));
 		return redirect(routes.Application.index());
 	}
 
 	public Result authenticate(String authToken) {
 		if( !tokenManager.authTokenExists(authToken) ) {
 			/**
 			 * Maybe display some error here?
 			 */
 			return redirect(routes.Auth.login());
 		}
 		else {
 			User user = tokenManager.getUser(authToken);
 			boolean persistLogin = tokenManager.isPersistentLogin(authToken);
 
 			// Generate a new app token
 			String appToken = user.makeToken();
 
 			Logger.info("app token made: " + appToken);
 			userDAO.save(user);
 
 			// Determine Cookie Max Age
 			// If the Auth Token record had "remember me", set this for a year
 			Integer maxAge = null;
 			if( persistLogin ) {
 				maxAge = Seconds.secondsBetween(DateTime.now(), DateTime.now().plusYears(1)).getSeconds();
 				Logger.info("Persisting login, cookie maxAge:" + maxAge);
 			}
 
 			// Set it in a cookie
 			response().setCookie(Play.application().configuration()
 				.getString("auth.cookie.name"), appToken, maxAge);
 
 			tokenManager.resolveAuthToken(authToken);
 
 			// Send us back to the index.
 			return redirect(routes.Application.index());
 		}
 	}
 
 }

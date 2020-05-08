 package controllers;
 
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import models.SessionToken;
 import models.User;
 import play.Routes;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.*;
 import play.mvc.Http.Session;
 import play.mvc.Result;
 import providers.MyUsernamePasswordAuthProvider;
 import providers.MyUsernamePasswordAuthProvider.MyLogin;
 import providers.MyUsernamePasswordAuthProvider.MySignup;
 
 import views.html.account.*;
 
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
 import com.feth.play.module.pa.user.AuthUser;
 
 public class Application extends Controller {
 
 	public static final String FLASH_MESSAGE_KEY = "message";
 	public static final String FLASH_ERROR_KEY = "error";
 	public static final String USER_ROLE = "user";
 
     // Home page  will remove if all auth modules adapted
 	public static Result index() {
         //return ok(main.render("", "", Html.apply("TODO: Some content here")));
         return redirect("/");
 	}
 
     /** ------ The next methods handle the followed links from email ------ **/
     public static Result tokenSuccess() {
         return ok(auth_result.render("Account Activation", "alert-sucess", "Account activated"));
     }
 
     public static Result tokenFail() {
         return badRequest(auth_result.render("Account Activation", "alert-error", "Invalid token"));
     }
 
     public static Result passwordResetSuccess() {
         return ok(auth_result.render("Password Reset", "alert-success", "Password succesfully reset"));
     }
 
     public static Result passwordResetFail(String msg) {
         return badRequest(auth_result.render("Password Reset", "alert-error", msg));
     }
     /** -------------------------------------------------------------------- **/
 
 
 	public static User getLocalUser(final Session session) {
 		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
 		final User localUser = User.findByAuthUserIdentity(currentAuthUser);
 		return localUser;
 	}
 
     public static String getValidationErrorsHtml(Collection<List<ValidationError>> errors) {
         StringBuffer sb = new StringBuffer();
         for (List<ValidationError> vel : errors)
             for (ValidationError ve : vel)
                 sb.append("<li>").append(ve.key()).append(": ").append(ve.message()).append("</li>");
         return "<ul>" + sb.toString() + "</ul>";
     }
 
 	public static Result doLogin() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
 		if (filledForm.hasErrors()) {
 			// User did not fill everything properly
 			return badRequest(getValidationErrorsHtml(filledForm.errors().values()));
 		} else {
 			// Everything was filled
             return UsernamePasswordAuthProvider.handleLogin(ctx());
 		}
 	}
 
 	public static Result jsRoutes() {
 		return ok(
             Routes.javascriptRouter("jsRoutes"//,
                 //controllers.routes.javascript.Signup.forgotPassword()
             )
         ).as("text/javascript");
 	}
 
 	public static Result doSignup() {
 		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
 		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM.bindFromRequest();
 		if (filledForm.hasErrors()) {
             // User did not fill everything properly
             return badRequest(getValidationErrorsHtml(filledForm.errors().values()));
 		} else {
 			// Everything was filled, do something with your part of the form before handling the user signup
 			return UsernamePasswordAuthProvider.handleSignup(ctx());
 		}
 	}
 
 	public static String formatTimestamp(final long t) {
 		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
 	}
 
     public static Result testTokenAuth(String token, String resturl) {
         SessionToken st = SessionToken.findByToken(token);
         if (st != null && !st.expired()) {
             session().put(PlayAuthenticate.ORIGINAL_URL, request().uri());
             session().put(PlayAuthenticate.USER_KEY, st.getUserId().toString());
             session().put(PlayAuthenticate.PROVIDER_KEY, st.getProviderId());
             session().put(PlayAuthenticate.EXPIRES_KEY, Long.toString(st.getExpires().getTime()));
             session().put(PlayAuthenticate.SESSION_ID_KEY, st.getToken());
             return redirect("/" + resturl);
         } else {
             return forbidden("Need to login");
         }
     }
 
 }

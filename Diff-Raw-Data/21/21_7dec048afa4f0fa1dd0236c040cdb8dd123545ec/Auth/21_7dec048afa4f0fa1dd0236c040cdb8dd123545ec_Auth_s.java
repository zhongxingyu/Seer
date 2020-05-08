 package controllers;
 
 import models.User;
 import play.Play;
 import play.libs.OpenID;
 import play.libs.OpenID.UserInfo;
 import play.mvc.Controller;
 import play.mvc.Http.Request;
 
 public class Auth extends Controller{
 	private static final String KEY_EMAIL = "email";
 	public static void logout() {
 		session.clear();
 		CloudPaster.index(0);
 	}
 	public static void yahoologin() {
 		if (!OpenID.id("https://me.yahoo.com").required(KEY_EMAIL, "http://axschema.org/contact/email")
 				.returnTo(Play.configuration.getProperty("auth.returnto", "Auth.login"))
 				.forRealm(Play.configuration.getProperty("auth.returnto", "Auth.login")).verify()) {
 			flash.error("Oops. Cannot contact yahoo");
 			login();
 		}
 	}
 	public static void googlelogin() {
 		if (!OpenID.id("https://www.google.com/accounts/o8/id").required(KEY_EMAIL, "http://axschema.org/contact/email")
 				.returnTo(Play.configuration.getProperty("auth.returnto", "Auth.login"))
 				.forRealm(Play.configuration.getProperty("auth.returnto", "Auth.login")).verify()) {
 			flash.error("Oops. Cannot contact google");
			CloudPaster.index(0);
 		}
 	}
 	public static void login() {
 		if(OpenID.isAuthenticationResponse()) {
 	        // Retrieve the verified id
 	        UserInfo userinfo = OpenID.getVerifiedID();
 	        if(userinfo == null) {
 	              flash.error("不妙，登陆错误。");
 	              login();
 	        } else {
               String email = userinfo.extensions.get(KEY_EMAIL);
               User user = User.createOrGet(email);
               session.put(CloudPaster.KEY_USER, user.key);
               CloudPaster.my(0);
 	        }
 		}
 		render();
 	}
 }

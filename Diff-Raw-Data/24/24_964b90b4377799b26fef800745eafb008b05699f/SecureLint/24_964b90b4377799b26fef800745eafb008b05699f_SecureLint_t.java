 package controllers;
 
 
 import helpers.SubdomainChecker;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.TimeoutException;
 
 import lagoon.LintRobot;
 import play.Logger;
 import play.data.validation.Required;
 import play.libs.Crypto;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.utils.Java;
 import exceptions.LintException;
 
 /**
  * Manages login and authentication
  * 
  * @author linda.velte
  * 
  */
 public class SecureLint extends Controller {
 
 	@Before(unless = { "login", "authenticate", "logout", "register", "registerUser" })
 	static void checkAccess() throws Throwable {
 
 		// Check Authentication
 		if (!session.contains("username") || !session.contains("id")) {
 			flash.put("url", "GET".equals(request.method) ? request.url : "/");
 			login();
 		}
 
 		// Check Authorization
 		if (!LintRobot.checkRequest(request, Long.parseLong(session.get("id")),SubdomainChecker.currentSubdomain(request))) {
 			Lintity.invoke("onCheckFailed");
 		}
 	}
 
 	// ~~~ Login
 
 	public static void login() throws Throwable {
 		Http.Cookie remember = request.cookies.get("rememberme");
 		if (remember != null && remember.value.indexOf("-") > 0) {
 			String sign = remember.value.substring(0, remember.value.indexOf("-"));
 			String username = remember.value.substring(remember.value.indexOf("-") + 1);
 			if (Crypto.sign(username).equals(sign)) {
 				session.put("username", username);
 				redirectToOriginalURL();
 			}
 		}
 		flash.keep("url");
 		render();
 	}
 
 	public static void authenticate(@Required String username, String password, boolean remember) throws Throwable {
 		// Check tokens
 
 		Boolean allowed = (Boolean) Lintity.invoke("authenticate", username, password);
 
 		String context = SubdomainChecker.currentSubdomain(request);
 		if (allowed) {
 			try {
 				Object login = LintRobot.login(username, password, context);
 				if (login != null) {
 					session.put("id", login);
 				}
 			} catch (LintException e) {
 				Logger.error("Lint - " + e.getMessage());
 				validation.addError("Lint Login", "Cannot identify user");
 			} catch (TimeoutException e) {
 				Logger.error("Lint - " + e.getMessage());
 				validation.addError("Lint Login", "Timeout");
 			}
 		}
 
 		if (validation.hasErrors() || !allowed) {
 			flash.keep("url");
 			flash.error("lint.error");
 			params.flash();
 			login();
 		}
 
 		// Mark user as connected
 		session.put("username", username);
 		// Remember if needed
 		if (remember) {
 			response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
 		}
 		redirectToOriginalURL();
 	}
 
 	public static void logout() throws Throwable {
 		String context = SubdomainChecker.currentSubdomain(request);
 		Lintity.invoke("onDisconnect");
 		LintRobot.logout(Long.parseLong(session.get("id")), context);
 		session.clear();
 		response.removeCookie("rememberme");
 		Lintity.invoke("onDisconnected");
 		flash.success("lint.logout");
 		login();
 	}
 
 	// ~~~ Utils
 
 	static void redirectToOriginalURL() throws Throwable {
 		Lintity.invoke("onAuthenticated");
 		String url = flash.get("url");
 		if (url == null) {
 			url = "/";
 		}
 		redirect(url);
 	}
 
 	public static class Lintity extends Controller {
 
 		public static void register() {
			renderTemplate("SecureLint/Lintity/register.html");
 		}
 
 		public static void activation(String token) {
			renderTemplate("SecureLint/Lintity/activation.html", token);
 		}
 
 		public static void registerUser(@Required String username, @Required String email, @Required String name) throws Exception {
 			throw new UnsupportedOperationException();
 		}
 
 		public static void activateUser(@Required String passowrd, @Required String passwordConf, String token) throws Exception {
 			throw new UnsupportedOperationException();
 		}
 
 		/**
 		 * This method is called during the authentication process. This is where you check if the user is allowed to log in into the system. This is
 		 * the actual authentication process against a third party system (most of the time a DB).
 		 * 
 		 * @param username
 		 * @param password
 		 * @return true if the authentication process succeeded
 		 * @throws LintException
 		 */
 		static boolean authenticate(String username, String password) {
 			return true;
 		}
 
 		/**
 		 * This method returns the current connected username
 		 * 
 		 * @return
 		 */
 		static String connected() {
 			return session.get("username");
 		}
 
 		/**
 		 * Indicate if a user is currently connected
 		 * 
 		 * @return true if the user is connected
 		 */
 		static boolean isConnected() {
 			return session.contains("username");
 		}
 
 		/**
 		 * This method is called after a successful authentication. You need to override this method if you with to perform specific actions (eg.
 		 * Record the time the user signed in)
 		 */
 		static void onAuthenticated() {
 		}
 
 		/**
 		 * This method is called before a user tries to sign off. You need to override this method if you wish to perform specific actions (eg. Record
 		 * the name of the user who signed off)
 		 */
 		static void onDisconnect() {
 		}
 
 		/**
 		 * This method is called after a successful sign off. You need to override this method if you wish to perform specific actions (eg. Record the
 		 * time the user signed off)
 		 */
 		static void onDisconnected() {
 		}
 
 		/**
 		 * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
 		 */
 		static void onCheckFailed() {
 			forbidden();
 		}
 
 		private static Object invoke(String m, Object... args) throws Throwable {
 
 			try {
 				return Java.invokeChildOrStatic(Lintity.class, m, args);
 			} catch (InvocationTargetException e) {
 				throw e.getTargetException();
 			}
 		}
 
 	}
 }

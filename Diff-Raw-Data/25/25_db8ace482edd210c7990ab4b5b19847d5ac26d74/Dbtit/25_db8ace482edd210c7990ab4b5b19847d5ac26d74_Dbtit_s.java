 package controllers;
 
 import java.util.List;
 import java.util.TimeZone;
 
 import models.Room;
 import models.Room.RoomThread;
 import models.Thread;
 import models.User;
 import play.Logger;
 import play.data.validation.Valid;
 import play.i18n.Messages;
 import play.libs.OpenID;
 import play.libs.OpenID.UserInfo;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Router;
 import utils.Pagination;
 
 /**
  * This controller defines basic actions such as login, logout or edit profile
  * 
  * @author julien
  * 
  */
 public class Dbtit extends Controller {
 
 	@Before(priority = 10)
 	protected static void checkLoggedIn() {
 		LoggedIn loggedIn = getActionAnnotation(LoggedIn.class);
 		if (loggedIn != null) {
 			if (connectedUser() == null) {
 				login(request.method == "GET" ? request.url : Router
 						.getFullUrl("Dbtit.index"));
 			}
 		}
 	}
 
 	@Before(priority = 20)
 	protected static void putUser() {
 		renderArgs.put("user", connectedUser());
 	}
 
 	protected static User connectedUser() {
 		if (!session.contains("user-email")) {
 			return null;
 		}
 		return User.find("byEmail", session.get("user-email")).first();
 	}
 
 	public static void login(String url) {
 		// First time
 		if (!OpenID.isAuthenticationResponse()) {
 			if (url != null)
 				flash.put("url", url);
 			if (!OpenID.id("https://www.google.com/accounts/o8/id")
 					.required("email", "http://axschema.org/contact/email")
 					.verify()) {
 				flash.error(Messages.get("openIdError"));
 				index();
 			}
 		// Après authentification
 		} else {
 			UserInfo verifiedUser = OpenID.getVerifiedID();
 			if (verifiedUser == null) {
 				flash.error(Messages.get("loginFail"));
 				index();
 			}
 			String email = verifiedUser.extensions.get("email");
 			session.put("user-email", email);
 			User user = User.find("byEmail", email).first();
 			String redirectUrl = flash.get("url");
 			if (redirectUrl == null)
 				redirectUrl = Router.reverse("Dbtit.index").url;
 			if (user == null) { // New User
 				String name = email.substring(0, email.indexOf('@'));
 				user = User.create(name, email, TimeZone.getDefault().getID());
 				Logger.info("New user %s (%s) created", email, user.id);
 				flash.success(Messages.get("welcomeFirst"));
 				profile(redirectUrl);
 			} else {
 				/*if (user.rememberMe) {
 					response.setCookie("rememberme", Crypto.sign(email) + "-" + email, "365d");
 				}*/
 				flash.success(Messages.get("welcome", user.name));
 				redirect(redirectUrl);
 			}
 		}
 	}
 
 	public static void logout() {
 		session.remove("user-email");
 		// response.setCookie("rememberme", "", 0);
 		index();
 	}
 
 	@LoggedIn
 	public static void profile(String url) {
 		User.Update userUpdate = new models.User.Update(connectedUser());
 		if (url != null)
 			flash.put("url", url);
 		render(userUpdate);
 	}
 
 	@LoggedIn
 	public static void postProfile(@Valid User.Update userUpdate) {
 		User connectedUser = connectedUser();
 		if (validation.hasErrors()) {
 			render("@profile", userUpdate);
 		}
 		connectedUser.updateProfile(userUpdate);
 		Logger.info("Profile updated by %s (%s)", connectedUser.name,
 				connectedUser.id);
 		flash.success(Messages.get("profileUpdated"));
 		String url = flash.get("url");
 		if (url == null)
 			url = Router.reverse("Dbtit.index").url;
 		redirect(url);
 	}
 
 	/**
 	 * Display the index page
 	 */
 	public static void index() {
 		Room room = Room.getOpenRoom();
		Pagination pagination = new Pagination(params, 4);
		List<Thread> threads = Thread.find("room = ? ORDER BY lastActivity DESC", room).fetch(pagination.getFrom(), pagination.getPageSize());
		List<RoomThread> rooms = Room.getRecentPublicActivity(0, 6);
 
 		render(threads, pagination, rooms);
 	}
 
 	public static void features() {
 		render();
 	}
 }

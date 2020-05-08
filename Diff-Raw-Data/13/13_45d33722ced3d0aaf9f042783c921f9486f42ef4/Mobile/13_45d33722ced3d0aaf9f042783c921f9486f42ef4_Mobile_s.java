 package controllers;
 
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.safety.Whitelist;
 
 import models.Message;
 import models.User;
 import models.forum.Topic;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.libs.Crypto;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Http.Request;
 import play.mvc.Scope.Session;
 import utils.StringUtils;
 import utils.UAgentInfo;
 
 public class Mobile extends Controller {
 	@Before
 	static void addDefaults() {
 		Application.addDefaults();
 	}
 
 	public static void index() {
 		news();
 	}
 	
 	public static void news() {
 		List<Topic> topics = Topic.find("forum.isNewsBoard = ? ORDER BY created desc", true).fetch();
 		render(topics);
 	}
 	
 	public static void shoutbox() {
		List<Message> shouts = Message.find("order by date desc").fetch(10);
 		render(shouts);
 	}
 	
 	public static void login() {
 		render();
 	}
 	public static void dologin(@Required String username, String password, boolean remember) throws Throwable {
 		// Check tokens
 		Boolean allowed = false;
 
 		User check = User.connect(username, password);
 		
 		if (check != null) {
 			allowed = true;
 		}
 		
 		if (Validation.hasErrors() || !allowed) {
 			flash.keep("url");
 			flash.error("secure.error");
 			params.flash();
 			redirect("/mobile/login");
 		}
 		// Mark user as connected
 		session.put("username", username);
 		response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
 		// Remember if needed
 		if (remember) {
 			response.setCookie("rememberme", Crypto.sign(username) + "-" + username, "30d");
 		}
 		
 		redirect("/mobile/news");
 	}
 	public static void dologout() throws Throwable {
 		session.clear();
 		response.removeCookie("rememberme");
 		flash.success("secure.logout");
 		String url = flash.get("url");
 		redirect("/mobile/news");
 	}
 	
 	public static void shoutboxAddMessageMobile(String nickname, String message) {
 		User user = null;
 		if (Session.current().contains("username")) {
 			String username = Session.current().get("username");
 			user = User.getConnectedUser(username);
 		}
 
		Message cm = new Message(message, user).save();
 		redirect("/mobile/shoutbox");
 	}
 }

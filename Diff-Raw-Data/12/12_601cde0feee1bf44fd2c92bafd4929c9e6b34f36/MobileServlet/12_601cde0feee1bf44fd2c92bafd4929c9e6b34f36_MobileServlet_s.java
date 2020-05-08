 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import bzb.gae.Utility;
 import bzb.gae.exceptions.BadArrivalTimeException;
 import bzb.gae.exceptions.TooFewArgumentsException;
 import bzb.gae.exceptions.TooManyArgumentsException;
 import bzb.gae.exceptions.UserAlreadyExistsException;
 import bzb.gae.exceptions.UserNotFoundException;
 import bzb.gae.summer.jdo.Group;
 import bzb.gae.summer.jdo.User;
 import bzb.gae.ws.FacebookCookie;
 
 /**
  * @author bzb
  * 
  */
 @SuppressWarnings("serial")
 public class MobileServlet extends HttpServlet {
 	
 	private static final String CLIENT_ID = "123839414321448";
 	//private static final String CLIENT_SECRET = "9f8b40b75c06e0db09c53452e5a674fe";
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 
 		if (request.getParameterMap().isEmpty()) {
 			response.setContentType("text/html");
 			
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
					userCookie = new FacebookCookie(cookies[i]);
					break;
 				}
 			}
			
 			String html = "<body>";
 			
 			if (userCookie != null) {
 				html += "<p>Hi " + userCookie.getName()[0] + ". ";
 				User user;
 				if ((user = User.getUser(userCookie.getProfileURL())) != null) {
 					html = Utility.headWithRefreshAndTitle("DTC Summer School App") + html;
 					List<User> groupMembers = Group.getGroup(user.getGroupKey()).getUsers(user.getUsername());
 					if (groupMembers.size() > 0) {
 						html += "Your group contains ...</p>" +
 								"<ul>";
 						Iterator<User> it = groupMembers.iterator();
 						while (it.hasNext()) {
 							User thisUser = it.next();
 							html += "<li>" + thisUser.getUsername() + " (";
 							if (Utility.isValidPhone(thisUser.getPhoneNumber())) {
 								html += "Tel: " + thisUser.getPhoneNumber();
 							} else if (Utility.isValidTwitter(thisUser.getPhoneNumber())) {
 								html += "Twitter: <a href=\"http://www.twitter.com/" + thisUser.getPhoneNumber().substring(1) + "\" target=\"_blank\">" + thisUser.getPhoneNumber() + "</a>";
 							} else if (Utility.isValidFacebookURL(thisUser.getPhoneNumber())){
 								html += "Facebook: <a href=\"" + thisUser.getPhoneNumber() + "\" target=\"_blank\">" + thisUser.getPhoneNumber() + "</a>";
 							}
 							html += ")</li>";
 						}
 						html += "</ul>";
 					} else {
 						html += "There is no-one else in your group yet.</p>";
 					}
 					html += "<p>This page will automatically update itself every " + Utility.REFRESH_PERIOD + " seconds to allow you to see when new travellers join your group.</p>";
 				} else {
 					html = Utility.headWithTitle("DTC Summer School App") + html + "Click <a href=\"?action=register\">here</a> to join a travelling group.</p>";
 				}
 			} else {
 				html = Utility.headWithTitle("DTC Summer School App") + html + "<fb:login-button></fb:login-button>";
 			}
 
 			html +=	"<div id=\"fb-root\"></div>" +
 			"<script src=\"http://connect.facebook.net/en_US/all.js\"></script>" +
 			"<script>" +
 			"FB.init({appId: '" + CLIENT_ID + "', status: true, cookie: true, xfbml: true});" +
 			"FB.Event.subscribe('auth.sessionChange', function(response) {" +
 			"if (response.session) {" +
 				// A user has logged in, and a new cookie has been saved
 			"} else {" + 
 				// The user has logged out, and the cookie has been cleared
 			"}" +
 			"});" +
 			"</script>" +
 			"</body>";
 			
 			response.getWriter().println(html);
 		} else if (request.getParameter("action") != null && request.getParameter("action").equals("register")) {
 			response.setContentType("text/html");
 			
 			String html = Utility.headWithTitle("DTC Summer School App");
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
 			for (int i = 0; i < cookies.length; i++) {
 				if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
 					userCookie = new FacebookCookie(cookies[i]);
 					break;
 				}
 			}
 			
 			html += "<body>";
 			
 			if (userCookie != null) {
 				html += "<p>Register</p>" +
 						"<form name=\"input\" action=\"http://carsharing-sms.appspot.com/summer/mobile\" method=\"get\">" +
 						"<input type=\"hidden\" name=\"action\" value=\"register2\" /><br />" +
 						"Username:" +
 						"<input type=\"text\" name=\"user\" size=\"20\" value=\"" + userCookie.getName()[0] + " " + userCookie.getName()[1] + "\" /><br />" +
 						"Scheduled arrival time:" +
 						"<input type=\"text\" name=\"arrival\" size=\"4\" value=\"" + new SimpleDateFormat("km").format(new Date(System.currentTimeMillis() + 1000*60*60)) + "\" /><br />" +
 						"<input type=\"submit\" value=\"Submit\" />" +
 						"</form>";
 			} else {
 				html += "<fb:login-button></fb:login-button>";
 			}
 			
 			html +=	"<div id=\"fb-root\"></div>" +
 			"<script src=\"http://connect.facebook.net/en_US/all.js\"></script>" +
 			"<script>" +
 			"FB.init({appId: '" + CLIENT_ID + "', status: true, cookie: true, xfbml: true});" +
 			"FB.Event.subscribe('auth.sessionChange', function(response) {" +
 			"if (response.session) {" +
 				// A user has logged in, and a new cookie has been saved
 			"} else {" + 
 				// The user has logged out, and the cookie has been cleared
 			"}" +
 			"});" +
 			"</script>" +
 			"</body>";
 			
 			response.getWriter().println(html);
 			
 			// oauth method
 			/*response.sendRedirect("https://graph.facebook.com/oauth/authorize?" +
 			    "client_id=" + CLIENT_ID + "&" +
 			    "redirect_uri=http://carsharing-sms.appspot.com/summer/mobile&" +
 			    "display=touch");*/
 		} else if (request.getParameter("action") != null && request.getParameter("action").equals("register2")) {
 			response.setContentType("text/html");
 			
 			String html = Utility.headWithTitle("DTC Summer School App");
 			FacebookCookie userCookie = null;
 			
 			Cookie[] cookies = request.getCookies();
 			for (int i = 0; i < cookies.length; i++) {
 				if (cookies[i].getName().equals("fbs_" + CLIENT_ID)) {
 					userCookie = new FacebookCookie(cookies[i]);
 					break;
 				}
 			}
 			
 			html += "<body>";
 			
 			if (userCookie != null) {
 				if (request.getParameter("user") != null && request.getParameter("arrival") != null) {
 					try {
 						SummerSMS registration = new SummerSMS(userCookie.getProfileURL(), new String[]{"summer", request.getParameter("user"), request.getParameter("arrival")});
 						html += "<p>Thanks " + userCookie.getName()[0] + ". Now click <a href=\"http://carsharing-sms.appspot.com/summer/mobile\">here</a> to watch for other travellers joining your group.</p>";
 					} catch (UserAlreadyExistsException e) {
 						html += "<p>Thanks " + userCookie.getName()[0] + ". Your details have been updated. Now click <a href=\"http://carsharing-sms.appspot.com/summer/mobile\">here</a> to watch for other travellers joining your group.</p>";
 						e.printStackTrace();
 					} catch (BadArrivalTimeException e) {
 						e.printStackTrace();
 					} catch (TooManyArgumentsException e) {
 						e.printStackTrace();
 					} catch (TooFewArgumentsException e) {
 						e.printStackTrace();
 					} catch (UserNotFoundException e) {
 						e.printStackTrace();
 					}
 				}				
 			} else {
 				html += "<fb:login-button></fb:login-button>";
 			}
 			
 			html +=	"<div id=\"fb-root\"></div>" +
 			"<script src=\"http://connect.facebook.net/en_US/all.js\"></script>" +
 			"<script>" +
 			"FB.init({appId: '" + CLIENT_ID + "', status: true, cookie: true, xfbml: true});" +
 			"FB.Event.subscribe('auth.sessionChange', function(response) {" +
 			"if (response.session) {" +
 				// A user has logged in, and a new cookie has been saved
 			"} else {" + 
 				// The user has logged out, and the cookie has been cleared
 			"}" +
 			"});" +
 			"</script>" +
 			"</body>";
 			
 			response.getWriter().println(html);
 			
 			// oauth method
 			/*response.sendRedirect("https://graph.facebook.com/oauth/authorize?" +
 			    "client_id=" + CLIENT_ID + "&" +
 			    "redirect_uri=http://carsharing-sms.appspot.com/summer/mobile&" +
 			    "display=touch");*/
 		} /*else if (request.getParameter("code") != null) {
 			token = Utility.makeGetRequest("https://graph.facebook.com/oauth/access_token?" +
 			    "client_id=" + CLIENT_ID + "&" +
 			    "redirect_uri=http://carsharing-sms.appspot.com/summer/mobile&" +
 			    "client_secret=" + CLIENT_SECRET + "&" +
 			    "code=" + request.getParameter("code"));
 			try {
 				JSONObject thisUser = new JSONObject(Utility.makeGetRequest("https://graph.facebook.com/me?" + token));
 				response.setContentType("text/plain");
 				response.getWriter().println(thisUser.toString());
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}*/
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		doGet(request, response);
 	}
 }

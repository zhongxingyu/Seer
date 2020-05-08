 package controllers;
 
 import play.libs.F.Promise;
 import play.libs.WS;
 import play.libs.WS.WSRequestHolder;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import com.mongodb.BasicDBObject;
 
 public class OpenfireXMPPUserRegistration extends Controller {
 
 	private final static String XMPP_SERVER_CONSOLE_URL = "http://ltg.evl.uic.edu:9090";
 	private static final String XMPP_SERVER_SECRET = "a6e58698e57a60acb3a8e20cbf3bdc3fdc9d8697";
 
 	public static Result registerXMPPUser(BasicDBObject json) {
 		WSRequestHolder req = WS.url(XMPP_SERVER_CONSOLE_URL+"/plugins/userService/userservice");
 		req.setQueryParameter("type", "add");
 		req.setQueryParameter("secret", XMPP_SERVER_SECRET);
 		req.setQueryParameter("username", json.getString("_id"));
 		req.setQueryParameter("password", json.getString("_id"));
 		Promise<WS.Response> res = req.get();
 		if (res.get().getBody().equals("<error>UserAlreadyExistsException</error>"))
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("message", "A user with this '_id' already exists, pick a different one")).toString()).as("application/json");
 		if (res.get().getBody().equals("<error>RequestNotAuthorised</error>"))
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to satisfy the request").toString()).as("application/json");
 		if (!res.get().getBody().equals("<result>ok</result>\n"))
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Myseterious error while trying to register the XMPP user").toString()).as("application/json");
 		return ok(new BasicDBObject("status", "success").append("data", new BasicDBObject("person", json)).toString()).as("application/json");
 	}
 
 
 	public static Result unregisterXMPPUser(String person) {
 		WSRequestHolder req = WS.url(XMPP_SERVER_CONSOLE_URL+"/plugins/userService/userservice");
 		req.setQueryParameter("type", "delete");
 		req.setQueryParameter("secret", XMPP_SERVER_SECRET);
 		req.setQueryParameter("username", person);
 		Promise<WS.Response> res = req.get();
 		if (res.get().getBody().equals("<error>UserNotFoundException</error>")) {
 			// Do nothing because we are allowing unlimited successful deletion
 		}
 		if (res.get().getBody().equals("<error>RequestNotAuthorised</error>"))
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to satisfy the request").toString()).as("application/json");
 		if (!res.get().getBody().equals("<result>ok</result>\n"))
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Myseterious error while trying to un-register the XMPP user").toString()).as("application/json");
 		return ok(new BasicDBObject("status", "success").append("data", null).toString()).as("application/json");
 	}
 
 }

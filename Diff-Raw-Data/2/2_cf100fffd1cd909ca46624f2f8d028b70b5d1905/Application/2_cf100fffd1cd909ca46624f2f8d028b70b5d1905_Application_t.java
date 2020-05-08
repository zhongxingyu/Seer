 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import views.html.*;
 import com.force.api.*;
 import java.util.Map;
 
 import com.typesafe.plugin.RedisPlugin;
 import redis.clients.jedis.*;
 
 public class Application extends Controller {
 
 	public static Result index() {
 		return ok(index.render("welcome to the adiabats manager" + 
 					queryFDC()));
 	}
 
 	public static Result managerHome() {
 		String token = ((ApiSession)play.cache.Cache.get(session("SFDCUserId"))).getAccessToken();
 		return ok(index.render("welcome to the adiabats manager with auth token:" + token ));
 	}
 
 	public static Result loginToSFDC() {
 		String url = Auth.startOAuthWebServerFlow(new AuthorizationRequest()
 				.apiConfig(new ApiConfig()
 					.setClientId("3MVG9y6x0357Hlec8S2SO0GslEED6ht6ARorUCD0oJvWAWgBNQThaNgwXJ3esF4iaa3QmY3Zw_LVgaOqEU86c")
 					.setRedirectURI("https://adiabats.herokuapp.com/oauth"))
 				.state("mystate"));
 		return redirect(url);
 	}
 
 	public static Result handleOAuth(String token) {
 		Logger.warn("using code: " + token);
 		ApiConfig config = new ApiConfig()
 					.setClientId("3MVG9y6x0357Hlec8S2SO0GslEED6ht6ARorUCD0oJvWAWgBNQThaNgwXJ3esF4iaa3QmY3Zw_LVgaOqEU86c")
 					.setClientSecret("3360843589409396938")
					.setRedirectURI("https://adiabats.herokuapp.com/oauth");
 		ApiSession session = Auth.completeOAuthWebServerFlow(new AuthorizationResponse()
 				.apiConfig(config)
 				.code(token));
 		Logger.warn("about to create ForceApi");	
 		ForceApi api = new ForceApi(config,session);
 		String userId = api.getIdentity().getUserId();
 		play.cache.Cache.set(sessionKey(userId), session);		
 		session("SFDCUserId", userId);
 		return redirect("/manager/home");
 	}
 
 	private static String sessionKey(String userId){
 		return "user:"+userId+":session";
 	}
 
 	private static String queryFDC() {
 		String s = "";
 		QueryResult<Map> result = connectToFDC().query("select id, name from Team__c");
 		for (Map<?,?> m : result.getRecords()){
 			s += "Set: " + m.keySet();
 		}
 		//s += "max batch size: " + connectToFDC().describeGlobal().getMaxBatchSize();
 		//	s += "session: " + connectToFDC().session.getAccessToken();
 		//	s += "ID;: " + connectToFDC().getIdentity().getUserId();
 		return s;
 	}
 
 	public static ForceApi connectToFDC() {
 		ForceApi api = new ForceApi(new ApiConfig()
 				.setUsername("adiabatsadmin@demo92.com")
 				.setPassword("adiabats1232JcvPE55yyuxYa8alQ30udmc")
 				.setClientId("3MVG9y6x0357Hlec8S2SO0GslEED6ht6ARorUCD0oJvWAWgBNQThaNgwXJ3esF4iaa3QmY3Zw_LVgaOqEU86c")
 				.setClientSecret("3360843589409396938")
 				);
 		return api;
 	}
 
 }

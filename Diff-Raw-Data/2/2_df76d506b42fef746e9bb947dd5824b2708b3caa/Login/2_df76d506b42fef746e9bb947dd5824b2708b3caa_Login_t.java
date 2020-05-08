 package controllers;
 
 import java.lang.Exception;
 import java.util.*;
 
 //import play.api.libs.concurrent.Promise;
 import play.libs.F.Promise;
 
 import play.libs.Json;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.JsonParser;
 
 import play.*;
 import play.mvc.*;
 import play.data.*;
 
 import views.html.*;
 
 import models.*;
 
 import play.mvc.Http.Request;
 import play.mvc.Http.Cookie;
 import play.libs.OpenID;
 import play.libs.OpenID.UserInfo;
 
 public class Login extends Controller {
     
 	public static int sessionDuration = 3600*24*30;
 	
 	@SuppressWarnings("serial")
 	public static final Map<String, String> identifiers = new HashMap<String, String>() {
 		{
 			put("google", "https://www.google.com/accounts/o8/id");
 		}
 	};
 	
 	public static Result auth() {
 		String providerId = "google";
 		String providerUrl = identifiers.get(providerId);
 		//String returnToUrl = "http://localhost:9000/login/verify";
 		String returnToUrl = Application.domain + "/login/verify";
 		
 		if (providerUrl == null)
 			return badRequest(index.render(getCustSession(), InfoDisplay.ERROR, "Provider could not be found."));
 			
 		// set the information to get
 		Map<String, String> attributes = new HashMap<String, String>();
 		attributes.put("Email", "http://schema.openid.net/contact/email");
 		attributes.put("FirstName", "http://schema.openid.net/namePerson/first");
 		attributes.put("LastName", "http://schema.openid.net/namePerson/last");
 		
 		// ask to openid the information
 		Promise<String> redirectUrl = OpenID.redirectURL(providerUrl, returnToUrl, attributes);
 		return redirect(redirectUrl.get());
 	}
 
 	// return the session of the logged customer
 	public static CustomerSession getCustSession() {
 		MonDataBase db = MonDataBase.getInstance();
 		Http.Cookie cookie = request().cookies().get("connected");
 		
 		try {
 			if (cookie != null) {
 				String custId = cookie.value();
 				return new CustomerSession(custId, db.getLogin(custId));
 			}
 			return null;
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public boolean isConnected() {
 		return (getCustSession() != null);
 	}
 
 	// get the result of openid
 	public static Result verify() {
 		Promise<UserInfo> userInfoPromise = OpenID.verifiedId();
 		UserInfo userInfo = userInfoPromise.get();
 		JsonNode json = Json.toJson(userInfo);
 		
 		// parse the data from openId
 		String email = json.findPath("Email").getTextValue();
 		String firstName = json.findPath("FirstName").getTextValue();
 		String lastName = json.findPath("LastName").getTextValue();
 		
 		MonDataBase db = MonDataBase.getInstance();
 		
 		try {
 			String customerId = db.connection(email);
 			response().setCookie("connected", customerId, sessionDuration);
 			
 			CustomerSession custSession = new CustomerSession(customerId, MonDataBase.getInstance().getLogin(customerId));
 			
 			return ok(index.render(custSession, InfoDisplay.SUCCESS, "You are now connected."));					
 		}
 		catch (CustomerException e) {
 			try {
				String customerId = db.addCustomer(email, lastName, firstName);
 				response().setCookie("connected", customerId, sessionDuration);
 				CustomerSession custSession = new CustomerSession(customerId, firstName + " " + lastName);
 			
 				return ok(index.render(custSession, InfoDisplay.SUCCESS, "You are now connected"));
 			}
 			catch (Exception f) {
 				return badRequest(index.render(getCustSession(), InfoDisplay.ERROR, "Error when logging in. " + f));
 			}
 		}
 		catch (Exception e) {
 			return badRequest(index.render(getCustSession(), InfoDisplay.ERROR, "Error when logging in. " + e));
 		}
 	}
 	
 	/*public static String connection(String login, String password) throws Exception {
 		MonDataBase db = MonDataBase.getInstance();
 	
 		String customerId = db.connection(login, password);
 		response().setCookie("connected", customerId, sessionDuration);
 			
 		return customerId;
 	}*/
 	
 	public static String getConnected() throws Exception {
 		CustomerSession custSession = getCustSession();
 		
 		if (custSession == null)
 			throw new CustomerException("You must be connected.");
 			
 		return custSession.getId();
 	}
 	
 	public static Result logout() {
 		response().discardCookies("connected");
 		return ok(index.render(null, InfoDisplay.INFO, "You are now logged out."));
 	}
 
 	/*public static Result submit() {
 			Form<Client> filledForm = loginForm.bindFromRequest();
 			
 			//Check in the database if login exist and if it matches with the password
 			if(!filledForm.hasErrors()) {
 				try {
 					String checker = connection(filledForm.field("login").value(), filledForm.field("password").value());
 					CustomerSession custSession = new CustomerSession(checker, filledForm.field("login").value());
 					
 					Client created = filledForm.get();
 					return ok(index.render(custSession, filledForm, InfoDisplay.SUCCESS, "You are now connected."));					
 				}
 				catch (LoginException e) {
 					filledForm.reject("login", "This login does not exist");
 					return badRequest(index.render(getCustSession(), filledForm, InfoDisplay.ERROR, "Error when logging in. This login does not exist."));
 				}
 				catch (PasswordException e) {
 					filledForm.reject("password", "The password does not match");
 					return badRequest(index.render(getCustSession(), filledForm, InfoDisplay.ERROR, "Error when logging in. The password does not match."));
 				}
 				catch(Exception e) {
 					return badRequest(index.render(getCustSession(), filledForm, InfoDisplay.ERROR, "Error when logging in. " + e));
 				}
 			}
 			return badRequest(index.render(getCustSession(), filledForm, InfoDisplay.ERROR, "Error when logging in. Please fill correctly all the fields."));
 	}*/
 }
 
 
 

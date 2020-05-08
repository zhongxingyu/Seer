 package controllers;
 
 import global.GlobalContext;
 
 import org.jcheng.service.account.AccountService;
 import org.jcheng.service.account.AuthorizationService;
 
 import play.data.DynamicForm;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 import actions.AccountAuthAction;
 
 import com.google.common.base.Strings;
 
 /**
  * Provides a JSON web service for managing accounts.
  * 
  * @author jcheng
  *
  */
 public class AccountWebService extends Controller {
 	
 	private static AccountService accountService = (AccountService) GlobalContext.getApplicationContext().getBean("accountService");
 	private static AuthorizationService authorizationService = (AuthorizationService) GlobalContext.getApplicationContext().getBean("authorizationService");
 	
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)	
 	public static Result doCreate() {
	    DynamicForm data = form().bindFromRequest();
 	    String username = data.get("username");
 	    String pwHash = data.get("pwHash");
 		System.err.println( username + " , " + pwHash);
 		boolean isCreated = accountService.isAccountCreated(username);
 		if ( isCreated ) {
 			return badRequest(Json.toJson(new WSResult(false, "account already exists")));
 		} else {
 			boolean created = accountService.createAccount(username, pwHash, "identity");
 			if ( created ) {
 				return ok(Json.toJson(new WSResult(true, "ok")));
 			} else {
 				return badRequest(Json.toJson(new WSResult(false, "failed")));
 			}
 		}
 	}
 	
 	
 	public static Result doLogin(String username, String pwHash) {
 		boolean isValid = accountService.isLoginValid(username, pwHash);
 		if ( isValid ) {
 			String authToken = authorizationService.getToken(username);
 			return ok(Json.toJson(new WSResult(true, authToken)));
 		} else {
 			return forbidden(Json.toJson(new WSResult(false, "login failed")));
 		}
 	}
 	
 	@With(AccountAuthAction.class)
 	public static Result getLuckyColor(String username) {
 		String luckyColor = accountService.getLuckyColor(username);
 		WSResult result;
 		if ( Strings.isNullOrEmpty(luckyColor) ) {
 			result = new WSResult(false, "invalid request");
 		} else {
 			result = new WSResult(true, luckyColor);
 		}
 		return ok(Json.toJson(result));
 	}
 	
 	@With(AccountAuthAction.class)
 	public static Result removeAllAccounts(String username) {
 		accountService.removeAll();
 		return ok(Json.toJson("ok"));
 	}
 	
 
 }

 package controllers.authentication;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import controllers.APIClient;
 import controllers.Application;
 import controllers.BaseController;
 
 import api.entities.ChoiceJSON;
 import api.entities.UserJSON;
 import api.helpers.GsonHelper;
 import api.requests.AuthenticateUserRequest;
 import api.requests.CreatePollRequest;
 import api.requests.DeauthenticateUserRequest;
 import api.requests.SimpleAuthenticateUserRequest;
 import api.responses.AuthenticateUserResponse;
 import api.responses.CreatePollResponse;
 import api.responses.EmptyResponse;
 import play.Logger;
 import play.i18n.Messages;
 import play.libs.Crypto;
 import play.libs.Crypto.HashType;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Util;
 import play.mvc.Http.StatusCode;
 
 public class SimpleAuthenticationFrontend extends BaseAuthenticationFrontend {
 	
 	public static void showform(String email) {
 		BaseAuthenticationFrontend.showform(email);
 	}
 	
 	public static void submit(String email, String password) {
 		// Validate that the question and answers are there.
 		validation.required(email);
 		validation.required(password);
 
 		// If we have an error, go to loginform.
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			BaseAuthenticationFrontend.showform(email);
 		} else {
 			try {
 				boolean success = authenticateSimple(email, password);
 				if (success) {
 					// Go to the page.
 					String redirectTo = session.get("page_prior_to_login");
 					if(redirectTo == null) {
 						Application.index();
 					} else {
 						redirect(redirectTo);
 					}
 				} else {
 					// It failed!
					flash.error(Messages.get("unexpected_behavior"));
 					params.flash();
 					validation.keep();
					showform(email);
 				}
			} catch (Exception e) {
 				// It failed!
				flash.error(e.getMessage().toString());
 				params.flash();
 				validation.keep();
				showform(email);
 			}
 		}
 	}
 
 	@Util
 	public static boolean authenticateSimple(String email, String password) {
 		SimpleAuthenticateUserRequest req = new SimpleAuthenticateUserRequest(email);
 		req.password = Crypto.passwordHash(password, HashType.SHA256);
 		AuthenticateUserResponse authenticateResponse = (AuthenticateUserResponse) APIClient.send(req, false);
 		if(StatusCode.success(authenticateResponse.statusCode) && authenticateResponse.user != null && authenticateResponse.user.id != null && authenticateResponse.user.secret != null) {
 			session.put("user_id", authenticateResponse.user.id);
 			session.put("user_secret", Crypto.encryptAES(authenticateResponse.user.secret));
 			return true;
 		} else {
 			APIClient.deauthenticate();
 			return false;
 		}
 	}
 }

 package controllers;
 
 import java.util.HashMap;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.mail.EmailException;

 import models.BadgeAssertion;
 import models.User;
 import play.Configuration;
 import play.Logger;
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.Http.Request;
 import play.mvc.Result;
 
 public class APIController extends Controller {
 
 	private static Configuration config = Play.application().configuration();
 	public static boolean overSSL = config.getBoolean("ssl.routing");
 
 	public static HashMap<String, String> headers;
 
 	public static boolean checkAuth(String apiKey) {
 
 		for (User u : User.find.all()) {
 			String authString = u.email + ":" + u.password;
 			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
 			String authStringEnc = "Basic " + new String(authEncBytes);
 			if (authStringEnc.equals(apiKey)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static Result createBadgeAssertion() {
 
 		String authHead = null;
 		String recipient = null;
 		String evidence = null;
 		String badgeID = null;
 
 		authHead = request().getHeader("Authorization");
 
 		if (headers != null) {
 			authHead = headers.get("Authorization");
 			if (checkAuth(authHead)) {
 				recipient = headers.get("recipient");
 				evidence = headers.get("evidence");
 				badgeID = headers.get("badgeId");
 			}
 		}
 
 		else if (authHead != null) {
 			if (checkAuth(authHead)) {
 				recipient = request().getHeader("recipient");
 				evidence = request().getHeader("evidence");
 				badgeID = request().getHeader("badgeId");
 			}
 
 		} else {
 			Logger.error("BIG ERROR");
 			headers = null;
 			return badRequest("NO RECEIVED DATA");
 		}
 
 		if (recipient == null) {
 			return badRequest("recipient==null");
 		}
 		if (evidence == null) {
 			return badRequest("evidence==null");
 		}
 		if (badgeID == null) {
 			return badRequest("badgeID==null");
 		}
 
 		BadgeAssertion ba = AssertionController.createBadgeAssertionAPI(
 				recipient, badgeID, evidence);
 
 		String assertionURL = routes.PublicController.getAssertion(ba.uid)
 				.absoluteURL(request(), overSSL);
 
 		if (!EmailController.NotifyNewBadge(recipient, ba, request())) {
 			flash(Application.GLOBAL_FLASH_ERROR,
 					"Cound not notify user, please check your email settings");
 		} else {
 			flash(Application.GLOBAL_FLASH_SUCCESS,
 					"Badge created and email sent");
 			if (headers != null) {
 				return redirect(routes.AssertionController.assertions());
 			}
 		}
 
 		//
 		// ThreadSendEmail emailRunner = new ThreadSendEmail(recipient,
 		// evidence,
 		// ba, request());
 		// new Thread(emailRunner).start();
 		//
 		// if (headers != null) {
 		// headers = null;
 		// return redirect(routes.AssertionController.assertions());
 		// }
 		// headers = null;
		return ok(assertionURL);
 
 		// return badRequest("Could not create assertion");
 	}
 
 	// private static class ThreadSendEmail implements Runnable {
 	// String recipient;
 	// String evidence;
 	// BadgeAssertion ba;
 	// Request request;
 	//
 	// public ThreadSendEmail(String recipient, String evidence,
 	// BadgeAssertion ba, Request request) {
 	// // TODO Auto-generated constructor stub
 	// this.recipient = recipient;
 	// this.evidence = evidence;
 	// this.ba = ba;
 	// this.request = request;
 	// }
 	//
 	// @Override
 	// public void run() {
 	// try {
 	// String url = routes.PublicController.giveBadge(ba.uid)
 	// .absoluteURL(request, overSSL);
 	// EmailController.sendMail(recipient, "You earnt a badge!",
 	// "You have received a badge to show your work on "
 	// + evidence + ". " + url);
 	// ;
 	// } catch (EmailException e) {
 	// e.printStackTrace();
 	// }
 	// }
 	// }
 }

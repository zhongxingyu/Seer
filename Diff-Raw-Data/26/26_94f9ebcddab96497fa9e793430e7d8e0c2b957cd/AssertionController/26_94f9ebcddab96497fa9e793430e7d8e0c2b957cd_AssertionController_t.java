 package controllers;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 import models.BadgeAssertion;
 import models.BadgeClass;
 import models.IdentityHash;
 import models.IdentityObject;
 import models.IdentityType;
 import models.User;
 import models.VerificationObject;
 import models.VerificationType;
 import play.Logger;
 import play.api.libs.ws.WS;
 import play.data.Form;
 import play.libs.F.Promise;
 import play.mvc.*;
 import views.html.*;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import scala.Tuple2;
 import scala.collection.Seq;
 
 @Security.Authenticated(Secured.class)
 public class AssertionController extends Controller {
 
 	public static class EasyAssertion {
 
 		public String recipient;
 		public String evidence;
 		public String badgeId;
 		public Boolean getfrombadge;
 
 	}
 
 	public static Result EasyAssertion() {
 		Form<EasyAssertion> assertionForm = new Form<EasyAssertion>(
 				EasyAssertion.class);
 
 		List<BadgeClass> badges = BadgeClass.find.all();
 
 		return ok(easyassertion.render(assertionForm, badges));
 		// return TODO;
 	}
 
 	public static Result addAssertion() {
 
 		Form<EasyAssertion> assertionForm = new Form<EasyAssertion>(
 				EasyAssertion.class).bindFromRequest();
 		if (assertionForm.hasErrors()) {
 			return badRequest("ERROR");
 		}
 
 		// form ok
 
 		EasyAssertion ea = assertionForm.get();
 
 		// I wish my wife was this dirty!
 
 		String emailAddress = session().get("email");
 		if (emailAddress == null) {
 			return badRequest("ERROR");
 		}
 		String password = User.findByEmail(emailAddress).password;
 
 		String authString = emailAddress + ":" + password;
 		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
 		String authStringEnc = "Basic " + new String(authEncBytes);
 
 		// .setQueryParameter("Authorization",
 		// authStringEnc).setQueryParameter("recipient",
 		// ea.recipient).setQueryParameter("evidence",
 		// ea.evidence).setQueryParameter("badgeId", ea.badgeId)
 
 		String evidence = "N/A";
 
 		if (assertionForm.get().getfrombadge) {
 			Long bId = Long.parseLong(ea.badgeId);
 			BadgeClass badge = BadgeClass.find.byId(bId);
 			evidence = badge.getAlignment().url.toString();
 		} else {
 			evidence = ea.evidence;
 		}
 
 		HashMap<String, String> headers = new HashMap<String, String>();
 
 		headers.put("Authorization", authStringEnc);
 		headers.put("recipient", ea.recipient);
 		headers.put("evidence", evidence);
 		headers.put("badgeId", ea.badgeId);
 
 		APIController.headers = headers;
 		// APIController.createBadgeAssertion();
 		flash(Application.GLOBAL_FLASH_SUCCESS, "Assertion added");
 		return APIController.createBadgeAssertion();
 
 		// return TODO;
 	}
 
 	public static Result assertions() {
 		// list assertions
 
 		List<BadgeAssertion> assertionsList = BadgeAssertion.find.all();
 
 		return ok(assertions.render(assertionsList));
 
 	}
 
 	public static BadgeAssertion createBadgeAssertionAPI(String identity,
 			String badgeId, String evidence) {
 
 		// check badge existance
 		Long badgeIdLong = Long.parseLong(badgeId);
 		BadgeClass bc = BadgeClass.find.byId(badgeIdLong);
 		if (bc == null) {
 			// DEAD END
			return null;
 		}
 
 		// check valid URLs
 		URL badgeURL = null;
 		try {
 			badgeURL = new URL(routes.PublicController
 					.getBadgeJson(badgeIdLong).absoluteURL(request(), APIController.overSSL));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		URL exidenceURL = null;
 		try {
 			exidenceURL = new URL(evidence);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		IdentityHash ih = new IdentityHash(identity);
 
 		boolean hashed = true;
 		IdentityObject io = new IdentityObject(ih, IdentityType.email, hashed,
 				ih.getSalt());
 		io.save();
 
 		VerificationType vt = VerificationType.hosted;
 
 		URL fakeURL = null;
 		try {
 			fakeURL = new URL("http://www.example.org");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		VerificationObject vo = new VerificationObject(vt, fakeURL); // TRICY!!
 		vo.save();
 		BadgeAssertion ba = new BadgeAssertion(io.id, badgeURL, vo.id,
 				exidenceURL);
 		ba.save();
 
 		// get REAL vo url
 		URL thisURL = null;
 		try {
 			thisURL = new URL(routes.PublicController.getAssertion(ba.uid)
 					.absoluteURL(request(), APIController.overSSL));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		vo.url = thisURL; // replace after creation
 		vo.save();
 		ba.save();
 		return ba;
 		// return ok(Json.toJson(ba));
 	}
 
 	// @BodyParser.Of(play.mvc.BodyParser.Json.class)
 	// public static Result getAssertion(Long id) {
 	// BadgeAssertion ba = BadgeAssertion.find.byId(id);
 	// return ok(Json.toJson(ba));
 	// }
 
 	
 
 }

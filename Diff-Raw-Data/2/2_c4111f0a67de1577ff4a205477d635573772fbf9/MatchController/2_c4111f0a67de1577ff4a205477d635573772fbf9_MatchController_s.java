 package controllers;
 
 import models.Notification;
 import models.TripMatch;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.With;
 import workers.GCMWorker;
 import actions.BasicAuthAction;
 import flexjson.JSONSerializer;
 
 public class MatchController extends Controller {
 
 	public static Result getMatch(Integer id) {
 		TripMatch match = TripMatch.find.byId(id);
 
 		if (match == null) {
 			return notFound();
 		}
 
 		response().setContentType("application/json");
 		return ok(getSerializer().serialize(match));
 	}
 
 	private static JSONSerializer getSerializer() {
 		return new JSONSerializer().exclude("tripOffer.matches",
 				"tripRequest.matches").include("*");
 	}
 
 	@With(BasicAuthAction.class)
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result updateMatch(Integer id) {
 		TripMatch match = TripMatch.find.byId(id);
 
 		int newState = request().body().asJson().get("state").asInt();
 
 		if (match.getState() != newState) {
 			match.setState(newState);
 			match.update();
 
 			String deviceId = match.getTripOffer().getUser().getDeviceID();
 
 			if (!deviceId.isEmpty()) {
 
 				Notification n = new Notification();
 				n.setTripMatch(match);
 				n.setUser(match.getTripOffer().getUser());
 				match.update();
 
 				String message = null;
 				String title = null;
 
 				String offerName = match.getTripOffer().getUser()
 						.getFirstName()
 						+ " " + match.getTripOffer().getUser().getLastName();
 				String requestName = match.getTripRequest().getUser()
 						.getFirstName()
 						+ " " + match.getTripRequest().getUser().getLastName();
 
 				switch (match.getEnumState()) {
 				case POTENTIAL:
 					title = "New trip request";
 					message = "You have received an request from "
 							+ requestName;
 					break;
 				case CONFIRMED_POTENTIAL:
 					title = "Trip confirmed";
 					message = offerName + " accepted your request.";
 					break;
 				case DECLINED_POTENTIAL:
 					title = "Trip declined";
 					message = offerName + " declined your request.";
 					break;
 				case CONFIRMED_MATCH:
 					title = "Match confirmed";
 					message = requestName + " confirmed the match";
 					break;
 				case DECLINED_MATCH:
 					title = "Match declined";
 					message = requestName + " declined the match";
 				default:
 					// keep message null
 					break;
 				}
 
 				if (message != null && title != null) {
					GCMWorker.sendMessage(deviceId, message, title,
 							Integer.toString(match.getId()));
 				}
 			}
 		}
 
 		response().setContentType("application/json");
 		return ok(getSerializer().serialize(match));
 	}
 }

 package controllers;
 
 import helpers.MongoControlCenter;
 
 import java.net.UnknownHostException;
 import play.mvc.*;
 
 import views.html.*;
 
 public class Application extends Controller {
 
 	public static Result index() throws UnknownHostException {
 		MongoControlCenter control = new MongoControlCenter(
 				"venture.se.rit.edu", 27017);
 		control.setDatabase("dev");
 
 		String username = "RickyWinterborn"; // TODO : Make this pull current
 												// user name
 
 		Object[] userEntities = control.getEventsForUser("jay-z");
 		Object[] teamEntities = control.getTeamEventsForUser("RickyWinterborn");
 		Object[] orgEntities = control.getOrgEventsForUser("jay-z");
 		// Object[] userSubscriptions = //TODO
 
 		control.closeConnection();
 
 		return ok(index.render(userEntities, teamEntities, orgEntities,
 				username));
 	}
 
 	public static Result search() {
 		return ok(search.render());
 	}
 
 	public static Result subscriptions() {
 		return ok(subscriptions.render());
 	}
 
 	public static Result adminTools() {
 		return ok(adminTools.render("", AdminController.entitForm));
 	}
 
 	public static Result userSettings() {
 		return ok(settings.render());
 	}
 
 	public static Result entityView(String arg, String type)
 			throws UnknownHostException {
 
 		MongoControlCenter control = new MongoControlCenter(
 				"venture.se.rit.edu", 27017);
 		control.setDatabase("dev");
 
 		if (type.equals("INITIATIVE")) {
 
 			Object testInitiative = control.getInitiativeByKey(arg);
 			return ok(initiative.render(testInitiative));
 		}
 
 		else if (type.equals("MILESTONE")) {
 			return null;
 
 		}
 
 		else {
 			return ok(risk.render(null));
 		}
 
 	}
 }

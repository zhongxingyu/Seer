 package controllers;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import models.BabySitter;
 import models.BabySitterAvailable;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.addBabysitter;
 import views.html.listBabySitters;
 import views.html.search;
 import views.html.viewBabysitter;
 public class Application extends Controller
 {
 
	public static Result listAvailableBabySitters(double latitude, double longitude, String start, String end) {
 //		Ebean.getServer(null).getAdminLogging().setDebugGeneratedSql(true);
 		if (start.equals("")) start = "2012/05/04";
 		if (end.equals("")) end = "2012/05/10";
 		Date startDate = null;
 		Date endDate = null;
 		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
 		try {
 			startDate = formatter.parse(start);
 			Logger.debug("Parsed the start date!");
 		} catch (Exception e) {
 			Logger.debug("Failed to parse start date");
 			startDate = new Date();
 		}
 
 		try {
 			endDate = formatter.parse(end);
 			Logger.debug("Parsed the end date!");
 		} catch (Exception e) {
 			Logger.debug("Failed to parse end date");
 			endDate = new Date();
 		}
 
 		Logger.debug("Got the parameter: " + latitude +":" + longitude + ", start: " + startDate + ", end: " + endDate);
		return ok(listBabySitters.render(BabySitter.find("here", startDate, endDate)));
 	}
 	
 	public static Result addBabysitter()
 	{
 		return ok();
 	}
 
 	public static Result index()
 	{
 		return ok(search.render("This is the default controller"));
 	}
 	
 	public static Result addNewBabysitter()
 	{
 		return ok(addBabysitter.render("This is the default controller"));
 	}
     
     public static Result getBabysitter(String id)
     {
 		//return ok();
     	return ok(viewBabysitter.render(BabySitter.getSitter(id)));
     }
     
     //TODO Ids for availabilty are also needed here
     public static Result contactBabysitter(String sitterId)
     {
 		return ok();
 //    	return ok(contactBabysitter.render(BabySitter.getSitter(sitterId)));
     }
 
     //TODO Ids for availabilty should be used here, not sitter ids!
     public static Result requestSitter()
     {
     	BabySitterAvailable.setRequested((String) request().body().asFormUrlEncoded().get("id")[0]);
    	return ok(search.render("This is the default controller"));
     }
     
     //TODO Ids for availabilty should be used here, not sitter ids!
     public static Result agreeSitter()
     {
     	BabySitterAvailable.setAgreed((String) request().body().asFormUrlEncoded().get("id")[0]);
     	return ok();
     }
 
 
 
 }

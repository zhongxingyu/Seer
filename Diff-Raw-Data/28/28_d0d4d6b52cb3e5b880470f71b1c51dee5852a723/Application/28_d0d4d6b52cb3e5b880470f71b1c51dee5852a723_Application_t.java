 package controllers;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import models.BabySitter;
 import models.BabySitterAvailable;
 import models.Parent;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 public class Application extends Controller
 {
 
 
 	public static Result listAvailableBabySitters(String latitude, String longitude, String start, String end) {
 //		Ebean.getServer(null).getAdminLogging().setDebugGeneratedSql(true);
 		if (start.equals("")) start = "2012/05/09";
 		if (end.equals("")) end = "2012/05/10";
 		Date startDate = null;
 		Date endDate = null;
 		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
 		try {
 			startDate = formatter.parse(start);
 			Logger.debug("Parsed the start date! " + start);
 		} catch (Exception e) {
 			Logger.error("Failed to parse start date");
 			startDate = new Date();
 		}
 
 		try {
 			endDate = formatter.parse(end);
 			Logger.debug("Parsed the end date! " + end);
 		} catch (Exception e) {
 			Logger.error("Failed to parse end date");
 			endDate = new Date();
 		}
 		
 		// Update the user's model to include the latest lat & lon
 		Parent.find.all().get(0).delete();
 		Parent p = new Parent();
 		p.id = 1L;
 		p.lastLatitude = latitude;
 		p.lastLongitude = longitude;
 		p.save();
 
 		// I could not get the update working and the google did not help me :(. Maybe some problems with
 		// the cache or something? We should have started with the Play 1.2.x version instead of this 
 		// "brand new, not so polished" 2.x...
 /*		Parent user = Ebean.find(Parent.class, 1);
 		user.lastLatitude = latitude;
 		user.lastLongitude = longitude;
 		user.save();
 */
 		Logger.debug("Got the parameter: " + latitude +":" + longitude + ", start: " + startDate + ", end: " + endDate);
 		return ok(listBabySitters.render(BabySitter.find(Double.parseDouble(latitude), Double.parseDouble(longitude), startDate, endDate)));
 	}
 	
 	public static Result addBabysitter()
 	{
 		return ok();
 	}
 
 	public static Result index()
 	{
 		// Get the first result, which is our test user's account.
 		// My eclipse gives an error about this, because it thinks that the scala template wants a String
 		// instead of a Parent... Strange.
 		return ok(search.render(Parent.find.all().get(0)));
 	}
 	
 	public static Result indexMobile()
 	{
 		//return ok();
 		return ok(searchMobile.render("This is the default controller"));
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
     
     //TODO Ids for availability are also needed here
     public static Result contactBabysitter(String sitterId)
     {
 //		return ok();
     	return ok(contactBabysitter.render(BabySitter.getSitter(sitterId)));
     }
     
     public static Result contactBabysitterMobile(String sitterId)
     {
    	//return ok();
    	return ok(contactBabysitterMobile.render(BabySitter.getSitter(sitterId)));
     }
 
     //TODO Ids for availability should be used here, not sitter ids!
     public static Result requestSitter()
     {
     	BabySitterAvailable.setRequested((String) request().body().asFormUrlEncoded().get("id")[0]);
     	// return ok(search.render("This is the default controller"));
     	return redirect("/listBabySitters");
     }
     
     //TODO Ids for availability should be used here, not sitter ids!
     public static Result agreeSitter()
     {
     	BabySitterAvailable.setAgreed((String) request().body().asFormUrlEncoded().get("id")[0]);
     	return ok();
     }
 
 
 
 
 }

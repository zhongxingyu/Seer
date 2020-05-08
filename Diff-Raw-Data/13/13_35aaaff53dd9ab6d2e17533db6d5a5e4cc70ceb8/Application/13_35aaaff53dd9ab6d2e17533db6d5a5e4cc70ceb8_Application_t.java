 package controllers;
 
 import java.util.List;
 
 import models.User;
 
 import play.Logger;
 import play.mvc.Controller;
 import siena.Json;
 import utils.BigBrotherHelper;
 
 public class Application extends Controller {
 
     public static void index() {
     	session.clear();
     	renderArgs.put("index", true);
         render();
     }
 
     public static void doLogin(String latitude, String longitude) {
     	if (latitude == null || latitude.isEmpty()) {
     		latitude = "37.7712498";
     	}
     	if (longitude == null || longitude.isEmpty()) {
     		longitude = "-122.4048177";
     	}
     	//TODO check we receive latitude and longitude
     	session.put("latitude", latitude);
     	session.put("longitude", longitude);
     	FourSquareController.austh4SQ();
     }
     
     public static void renderVenues() {
     	String coordinates = session.get("latitude") + "," + session.get("longitude");
     	Json venues = BigBrotherHelper.getVenues(coordinates, session.get("token"));
     	render("application/venues-list.html", venues);
     }
     
     public static void callBack4SQAuth(){
 		String code = params.get("code");
 		String token = BigBrotherHelper.retrieveToken(code);
 		session.put("token", token);
 		Application.renderVenues();
 	}
 	
 	public static void showUser(String id) {
 		User user = User.all().filter("id", id).get();
 		render("application/user-view.html", user);
 	}
     
     public static void renderUsers(String venueId) {
     	List<User> peopleHere = BigBrotherHelper.queryVenue(venueId, session.get("token"));
     	render("application/people-list.html", peopleHere);
     }
     
 }

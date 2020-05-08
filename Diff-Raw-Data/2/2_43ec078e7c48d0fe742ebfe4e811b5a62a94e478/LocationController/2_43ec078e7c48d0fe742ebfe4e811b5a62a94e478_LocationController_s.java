 package controllers;
 
 import java.util.List;
 
 import javax.persistence.Query;
 
 import play.db.jpa.GenericModel.JPAQuery;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.With;
 import models.Author;
 import models.Location;
 
 @With(Secure.class)
 public class LocationController extends Controller {
 
 	public static void formEditLocation(String lid) {
 		Location location=Location.findById(Long.parseLong(lid));
 		render(location);
 	}
 
 	public static void formAddLocation() {
 		List<Location> allLocations=Location.findAll();
 		render(allLocations);
 	}
 
 	public static void addLocation(String name, String zip, String parentId) {
 		// TODO:Autor übergeben (@author alex)
 		Author aut=new Author();
 		aut.save();
 		Location newLoc;
 		if(parentId.equals("0"))
 			newLoc = new Location(name, Integer.parseInt(zip), null, aut);
 		else{
			Location parentObj=Location.findById(Integer.parseInt(parentId));
 			newLoc = new Location(name, Integer.parseInt(zip), parentObj, aut);
 		}
 		newLoc.save();
 		flash.success("Ort wurde gespeichert.");
 		Application.index(); //Redirect to index
 	}
 	
 	public static void saveLocation(long locId, String name, int zip,
 			Location parent) {
 		// TODO: Autor übergeben
 		Location loc = Location.findById(locId);
 		loc.name = name;
 		loc.zip = zip;
 		loc.parent = parent;
 	}
 
 	public static void showLocations() {
 		List<Location> allLocations = Location.findAll();
 		render(allLocations);
 	}
 
 }

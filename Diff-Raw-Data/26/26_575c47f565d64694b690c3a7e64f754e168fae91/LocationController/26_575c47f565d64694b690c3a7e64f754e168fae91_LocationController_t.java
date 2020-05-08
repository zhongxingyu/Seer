 package controllers;
 
 import models.Author;
 import models.Location;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class LocationController extends Controller {
 
 	/**
 	 * TODO FIXME zip code cannot be used yet since treeview only supports one
 	 * changeable value per node.
 	 * 
 	 * @param name
 	 * @param zip
 	 * @param parent
 	 */
 	public static void add(String name, int zip, Location parent) {
 		// TODO:Autor übergeben (@author alex)
 
 		Author aut = Author.find("byName", session.get("username")).first();
 		Location location = new Location(name, 0, parent, aut);
 		location.save();
 
 		flash.success("Ort wurde gespeichert.");
 	}
 
 	public static void rename(Location location, String newName) {
 		location.name = newName;
 		location.updateFullName();
 		location.save();
		for (Location c : location.children)
			c.updateFullName();
 	}
 
 	public static void move(Location location, long newParentId) {
 		location.parent = Location.findById(newParentId);
 		location.updateFullName();
 		location.save();
		for (Location c : location.children)
			c.updateFullName();
 	}
 
 	public static void delete(Location location) {
 		location.delete();
 	}
 }

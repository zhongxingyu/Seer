 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Act;
 import models.Actor;
 import models.Author;
 import models.Location;
 import models.Revision;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class ActController extends Controller {
 
 	public static void editAct(long actId, String name, long legislatorId,
 			long locationId) {
 		Act act = Act.findById(actId);
 		act.name = name;
 		Actor newLegislator = Actor.findById(legislatorId);
 		act.legislator = newLegislator;
 		Location newLocation = Location.findById(locationId);
 		act.location = newLocation;
 		act.save();
 		flash.success("Text wurde geändert.");
 		Application.index();
 	}
 
 	public static void addAct(String name, long legislatorId, long locationId) {
 		Act act = new Act(name);
 		act.legislator = Actor.findById(legislatorId);
 		act.location = Location.findById(locationId);
 		List<Author> aut = Author.find("byName", session.get("username"))
 				.fetch();
 		act.author = aut.get(0);
 		// act.setLocation(location);
 		// set authora
 		act.save();
 
 		Author author = new Author();
 		author.save();
 
 		Revision rev = new Revision(act, 0, new Date(), new Date(), author);
 		rev.save();
 
 		flash.success("Text wurde gespeichert.");
 		Application.index();
 	}
 
 	public static void formEditAct(long actId) {
 		Act act = Act.findById(actId);
 		Actor legislator = act.legislator;
		long elegislatorId = 0;
 		if (legislator != null)
			elegislatorId = legislator.id;
 		Location location = act.location;
		long elocationId = 0;
 		if (location != null)
			elocationId = location.id;
		render(act, legislator, elegislatorId, location, elocationId);
 	}
 
 	public static void actList() {
 		List<Act> acts = Act.findAll();
 		render(acts);
 	}
 }

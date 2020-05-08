 package controllers;
 
 import java.util.Date;
import java.util.List;
 
 import models.Act;
import models.Actor;
 import models.Author;
import models.Location;
 import models.Revision;
 import play.mvc.Controller;
 
 public class ActController extends Controller {
 
	public static void editAct(long aId, String name, long legislatorId, long locationId) {
		Act act=Act.findById(aId);
		act.setName(name);
		Actor newLegislator=Actor.findById(legislatorId);
		act.setLegislator(newLegislator);
		Location newLocation=Location.findById(locationId);
		act.setLocation(newLocation);
		act.save();
		// Änderungen werden noch nicht übernommen.
 		flash.success("Text wurde geändert.");
		Application.index();
 	}
 
 	public static void addAct(String name, long legislatorId, long locationId) {
 		Act act = new Act(name);
 		// act.setLegislator(legislator);
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
 
 	public static void formEditAct(long aid) {
 		Act act = Act.findById(aid);
		List<Location> locations=Location.find("order by name asc").fetch();
		List<Actor> actors=Actor.find("order by name asc").fetch();
		render(act, locations, actors);
 	}
 
 	public static void listTerms(long aid, long revisionId) {
 		// Post.find("select p from Post p, Comment c where c.post = p and c.subject like ?",
 		// "%hop%");
 		// List<Term> terms = Term.find(
 		// "SELECT t FROM Term t, Revision r, Act a WHERE t.revision ")
 		// .fetch();
 		Revision revision = Revision.findById(revisionId);
 		Act act = Act.findById(aid);
 		render(act, revision);
 	}
 
 }

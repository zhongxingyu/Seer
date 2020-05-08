 package controllers;
 
 import java.util.Date;
 
 import models.Act;
 import models.Author;
 import models.Revision;
 import play.mvc.Controller;
 
 public class ActController extends Controller {
 
	public static void editAct(Act act) {
 		flash.success("Text wurde geändert.");
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
		render(act);
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

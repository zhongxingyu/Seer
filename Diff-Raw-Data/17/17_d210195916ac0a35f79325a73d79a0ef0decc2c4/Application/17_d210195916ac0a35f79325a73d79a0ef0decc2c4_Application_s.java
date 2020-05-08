 package controllers;
 
 import java.util.Collections;
 import java.util.List;
 
 import models.Act;
 import models.Action;
 import models.Actor;
 import models.Location;
 import models.Revision;
 import models.Term;
 import play.mvc.Controller;
 import tools.JsonAttributeArray;
 import tools.Serialisable;
 
 public class Application extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 	public static void listActs() {
 		List<Act> acts = Act.findAll();
 		Collections.sort(acts);
 		render(acts);
 	}
 
 	/* for playing only */
 	public static void addAct(String name) {
 		Act newAct = new Act(name);
 		newAct.save();
 		index();
 	}
 
 	public static void listTerms(long actId, long revisionId) {
 		// Post.find("select p from Post p, Comment c where c.post = p and c.subject like ?",
 		// "%hop%");
 		// List<Term> terms = Term.find(
 		// "SELECT t FROM Term t, Revision r, Act a WHERE t.revision ")
 		// .fetch();
 		Revision revision = Revision.findById(revisionId);
 		Act act = Act.findById(actId);
 		render(act, revision);
 	}
 
 	public static void termDetails(long actId, long revisionId, long termId) {
 		Term term = Term.findById(termId);
 		List<Action> actions = Action.findAll();
 		List<Actor> actors = Actor.findAll();
 		render(term, actions, actors, actId, revisionId);
 	}
 
 	public static void listActors() {
 		List<Actor> actors = Actor.findAll();
 		render(actors);
 	}
 
 	public static void listLocations() {
 		List<Location> locations = Location.findAll();
 		render(locations);
 	}
 
 	public static void listRevisions(long actId) {
 		Act act = Act.findById(actId);
 		List<Revision> revisions = act.revisions;
 		render(revisions);
 	}
 
 	public static void listCompetences(long termId) {
 		Term term = Term.findById(termId);
 		List<Action> actions = Action.findAll();
 		List<Actor> actors = Actor.findAll();
 		render(term, actions, actors);
 	}
 
 	/*
 	 * Auto-complete methods
 	 */
 	public static void actAutocomplete(String term) {
		List<Serialisable> acts = Act.find("byNameLike", "%" + term + "%")
				.fetch();
 
 		renderJSON(JsonAttributeArray.build(acts).toString());
 	}
 
 	public static void actorAutocomplete(String term) {
		List<Serialisable> actors = Actor.find("byNameLike", "%" + term + "%")
				.fetch();
 
 		renderJSON(JsonAttributeArray.build(actors).toString());
 	}
 
 	public static void locationAutocomplete(String term) {
 		List<Serialisable> locations = Location.find("byNameLike",
				"%" + term + "%").fetch();
 
 		renderJSON(JsonAttributeArray.build(locations).toString());
 	}
 
 	public static void actionAutocomplete(String term) {
		List<Serialisable> actions = Action
				.find("byNameLike", "%" + term + "%").fetch();
 
 		renderJSON(JsonAttributeArray.build(actions).toString());
 	}
 }

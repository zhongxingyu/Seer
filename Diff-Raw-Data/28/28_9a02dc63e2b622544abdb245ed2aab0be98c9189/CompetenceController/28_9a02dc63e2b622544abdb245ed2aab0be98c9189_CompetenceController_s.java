 package controllers;
 
 import java.util.Date;
 
 import models.Action;
 import models.Actor;
 import models.Author;
 import models.Competence;
 import models.LogFile;
 import models.Term;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class CompetenceController extends Controller {
 
 	public static void addCompetence(long actId, long revisionId, long termId,
 			long actionId, long actorId, long passiveActorId) {
 		Term term = Term.findById(termId);
 		Action action = Action.findById(actionId);
 		Actor actor = Actor.findById(actorId);
 		Actor passiveActor = Actor.findById(passiveActorId);
 		Competence competence = new Competence(actor, action, passiveActor,
 				term, null);
 		Author aut = Author.find("byName", session.get("username")).first();
 		competence.author = aut;
 		competence.save();
 
 		flash.success("Kompetenz wurde gespeichert.");
 		Application.termDetails(actId, revisionId, termId);
 	}
 
 	public static void formEditCompetence(long competenceId) {
 		Competence competence = Competence.findById(competenceId);
 		long editActorId = 0;
 		if (competence.actor != null)
 			editActorId = competence.actor.id();
 		long editActionId = 0;
 		if (competence.action != null)
 			editActionId = competence.action.id;
 		long editPassiveActorId = 0;
 		if (competence.passiveActor != null)
 			editPassiveActorId = competence.passiveActor.id;
 		render(competence, editActorId, editActionId, editPassiveActorId);
 	}
 
 	public static void editCompetence(long termId, long competenceId,
 			long actorId, long actionId, long passiveActorId) {
 		Competence competence = Competence.findById(competenceId);
 		// Store old information for log:
 		String original = competence.toString();
		competence.actor = Actor.findById(actorId);
		competence.action = Action.findById(actionId);
		competence.passiveActor = Actor.findById(passiveActorId);
 		competence.save();
 
 		LogFile.logCompetenceChange(competence, original,
 				competence.toString(), session.get("username"), new Date());
 	}
 
 }

 package jobs;
 
 import java.text.ParseException;
 import java.util.List;
 
import controllers.Utility;
 
 import models.*;
 import play.db.jpa.JPABase;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.libs.Crypto;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	public void doJob() throws ParseException {
 		// Set loadTestData to true if you want to load test data. CAUTION: All
 		// existing data will be deleted!
 		Boolean loadTestData = false;
 
 		if (loadTestData) {
 			Fixtures.deleteAll();
 			Fixtures.load("initial-data.yml");
 
 			/** Encrypt all passwords set in 'initial-data.yml'. **/
 			for (JPABase a : Author.findAll()) {
 				Author aut = (Author) a;
 				aut.password = Crypto.passwordHash(aut.password);
 				aut.save();
 			}
 		}
 
 		/** Create user 'admin' if necessary **/
 		List<Author> existAdmin = Author.find("byName", "admin").fetch();
 		Author admin;
 		if (existAdmin.isEmpty()) {
 			admin = new Author("admin");
 			admin.password = Crypto.passwordHash("admin");
 			admin.save();
 		} else
 			admin = existAdmin.get(0);
 
 		/** assign parent-nodes [only if initial data is loaded!]. **/
 		if (loadTestData) {
 			/** Root elements **/
 			Location rootLocation = new Location("Ort", 0, null, admin);
 			rootLocation.save();
 			List<Location> locations = Location.findAll();
 
 			Action rootAction = new Action("Aktion", null, admin);
 			rootAction.save();
 			List<Action> actions = Action.findAll();
 
 			Actor rootActor = new Actor("Akteur", null, admin);
 			rootActor.save();
 			List<Actor> actors = Actor.findAll();
 
 			/**
 			 * Allocate root elements as parent to every object whose parent
 			 * element is null.
 			 **/
 			for (Location l : locations) {
 				if (!l.equals(rootLocation) && l.parent == null) {
 					l.parent = rootLocation;
 					l.save();
 				}
 			}
 			for (Action a : actions) {
 				if (!a.equals(rootAction) && a.parent == null) {
 					a.parent = rootAction;
 					a.save();
 				}
 			}
 			for (Actor a : actors) {
 				if (!a.equals(rootActor) && a.parent == null) {
 					a.parent = rootActor;
 					a.save();
 				}
 			}
 		}
 		
 		/**
 		 * Add Dates to acts and revisions
 		 */
 		Act act = Act.find("byName", "Reglement über den Eintritt in die Hochschule Bern").first();
 		Revision rev = act.getFirstRevision();
 		rev.orderDate = Utility.stringToDate("12.1.1901");
 		rev.commencementDate = Utility.stringToDate("12.1.1901");
 		rev.save();
 		act = Act.find("byName", "Dekret betreffend Beteiligung des Staates an der bernischen Lehrerversicherungskasse.").first();
 		rev = act.getFirstRevision();
 		rev.orderDate = Utility.stringToDate("30.12.1903");
 		rev.commencementDate = Utility.stringToDate("30.12.1903");
 		rev.save();
 		act = Act.find("byName", "Reglement für die Patentprüfung der Primarlehrer und der Primarlehrerinnen des deutschen Kantonsteil.").first();
 		rev = act.getFirstRevision();
 		rev.orderDate = Utility.stringToDate("08.03.1905");
 		rev.commencementDate = Utility.stringToDate("01.03.1905");
 		rev.save();
 		
 	}
 }

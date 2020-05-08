 package jobs;
 
 import javax.persistence.Query;
 
 import models.Author;
 import play.db.jpa.JPA;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	public void doJob() {
		Author a = Author.find("byName", "admin").first();

		if ( a == null ) {
 			Author admin = new Author("admin");
 			admin.password = "admin".hashCode();
 			admin.save();
 		}
 	}
 
 }

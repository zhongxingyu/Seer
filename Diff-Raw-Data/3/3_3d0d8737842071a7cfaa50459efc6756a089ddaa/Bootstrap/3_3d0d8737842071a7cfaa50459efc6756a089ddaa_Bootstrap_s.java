 package jobs;
 
 import models.User;
 
 import org.h2.store.Page;
 
 import play.Logger;
 import play.Play;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
     
     public void doJob() {
     	if(User.count() == 0) {
     		// Database is empty, load initial data
     		Fixtures.loadModels("initial-data.yaml");
     		
     		Logger.info("Loaded %d users", User.count());
     	}
     }
     
 }

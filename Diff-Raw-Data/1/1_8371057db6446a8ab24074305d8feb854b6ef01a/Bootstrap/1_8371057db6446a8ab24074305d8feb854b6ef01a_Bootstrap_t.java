 package bootstrap;
 
 import models.User;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	@Override
 	public void doJob() {
		Fixtures.deleteAllModels();
 		if (User.count() == 0) {
 			Fixtures.loadModels("bootstrap-data.yml");
 		}
 	}
 }

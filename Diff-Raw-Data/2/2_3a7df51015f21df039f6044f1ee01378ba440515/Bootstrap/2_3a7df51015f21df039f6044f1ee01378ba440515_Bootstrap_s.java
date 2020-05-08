 /**
 * @author Boris
  */
 
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 	public void doJob() {
 		Fixtures.load("initial-items.yml");
 		Fixtures.load("initial-workplaces.yml");
 	}
 }

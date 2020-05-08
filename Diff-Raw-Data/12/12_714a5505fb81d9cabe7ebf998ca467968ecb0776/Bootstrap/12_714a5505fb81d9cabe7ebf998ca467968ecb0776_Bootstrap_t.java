 package bootstrap;
 
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
  
     public void doJob() {
     	//TODO: hello mr
 //        // Check if the database is empty
        //Fixtures.loadModels("initial-data.yml");
     }
  
 }

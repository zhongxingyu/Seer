 import play.*;
 import play.jobs.*;
 import play.test.*;
  
 import models.*;
  
 @OnApplicationStart
 public class Bootstrap extends Job {
  
     public void doJob() {
         // Check if the database is empty
        if(Article.count() == 0) {
			Fixtures.delete() ;
             Fixtures.loadModels("initial-data.yml");
        }
     }
  
 }

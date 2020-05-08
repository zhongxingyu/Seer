 
 import play.test.*;
 import play.jobs.*;
 import models.*;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
     @Override
     public void doJob() {
         if (User.count() == 0) {
             Fixtures.loadModels("users.yml");
         }
     }
 }

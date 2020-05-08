 
 import play.test.*;
 import play.jobs.*;
 import models.*;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
     @Override
     public void doJob() {
        // Load default data if the database is empty
        //if (Document.count() == 0) {
          //  Fixtures.loadModels("documents.yml");
        //}

         if (User.count() == 0) {
             Fixtures.loadModels("users.yml");
         }
     }
 }

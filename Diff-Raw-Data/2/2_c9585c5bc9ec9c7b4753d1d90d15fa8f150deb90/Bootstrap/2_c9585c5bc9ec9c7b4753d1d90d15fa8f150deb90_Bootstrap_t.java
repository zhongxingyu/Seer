 import models.Article;
 import models.User;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job{
 
     public void doJob() {
         // Check if the database is empty
         if(Article.count() == 0) {
 //            Fixtures.loadModels("initial-data.yml");
             System.out.println("U Got No Data!");
         }
         if (User.count() == 0){
            new User("Jasdeep","Madan","JD","jasdeepm@gmail.com","98036054","theboss",true,true, "127.0.0.1").save();
             Fixtures.loadModels("initial-data.yml");
         }
         
     }
 
 
 }

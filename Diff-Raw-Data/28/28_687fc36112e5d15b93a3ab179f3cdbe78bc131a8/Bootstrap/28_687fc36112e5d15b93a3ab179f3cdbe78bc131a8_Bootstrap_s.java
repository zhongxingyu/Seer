 package jobs;
 
 import models.Incident;
 import models.IncidentCategory;
 import models.Location;
 import models.User;
 import play.jobs.*;
  
 @OnApplicationStart
 public class Bootstrap extends Job {
     
     public void doJob() {
    	Location l = new Location("Champ de Mars", 123, 1243);
     	l.save();
    	Location l2 = new Location("Cite Soleil", 425, 12438);
     	l2.save();
     	
     	IncidentCategory cat1 = new IncidentCategory("Gunshot", 60l*60l*1000l);
     	cat1.save();
     	IncidentCategory cat2 = new IncidentCategory("Fire", 10l*60l*60l*1000l);
     	cat2.save();
     	
     
         User user1 = new User("john", "doe", "password", "5144244244", true, l);
         user1.save();
         User user2 = new User("anna", "doe", "password", "5142444433", true, l2);
         user2.save();
         
         
         Incident i = new Incident(cat1, "Shoots", "Several shoots were heard.", (System.currentTimeMillis() - 1000000L), 75, l, "North", user1);
         i.save();
         Incident i2 = new Incident(cat2, "Tanks burning", "A class has left one tank on fire.", (System.currentTimeMillis() - 300000L), 200, l2, "South", user1);
         i2.save();
     }
     
 }

 import play.*;
 import play.libs.*;
 
 import java.util.*;
 
 import com.avaje.ebean.*;
 
 import models.*;
 
 public class Global extends GlobalSettings {
 
   public void onStart(Application app) {
<<<<<<< HEAD
         //InitialData.insert(app); // ei t66ta korralikult H2 in-memory baasiga! = Index conflict
=======
        InitialData.insert(app);
>>>>>>> 78196d50f51c9cdb2df7b360c0486b5074059c7e
     }
     
     static class InitialData {
         
         public static void insert(Application app) {
             
                 
                 Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");
 
 				if(Ebean.find(User.class).findRowCount() == 0) {
 					Ebean.save(all.get("users"));
 				}
 				
 				if(Ebean.find(Post.class).findRowCount() == 0) {
 					Ebean.save(all.get("posts"));
 				}
                
         }
         
     }
 }

 package controllers;
 
 import java.util.Date;
 
 import models.User;
import play.modules.gae.GAE;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 
 public class Application extends Controller {
 
 	@Before
     static void checkConnected() {
         if(Auth.getUser() == null) {
        	GAE.login("Application.index");
         } else {
             renderArgs.put("user", Auth.getEmail());
         }
     }
 	
     public static void index() {
     	if(Auth.isLoggedIn()) {
             User user = User.findByEmail(Auth.getEmail());
             if(null == user) {
                 user = new User();
                 user.email = Auth.getEmail();
                 user.created = new Date();
                 user.save();
             }
             render();
         }
     	//login();
     }
        
     
 
 }

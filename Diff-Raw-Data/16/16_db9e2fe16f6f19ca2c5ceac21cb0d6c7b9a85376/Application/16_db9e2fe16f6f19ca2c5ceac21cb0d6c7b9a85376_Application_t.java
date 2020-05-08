 package controllers;
 
 import java.util.Date;
 import java.util.Set;
 
 import com.google.appengine.repackaged.com.google.common.collect.Sets;
 
 import models.User;
 import play.modules.gae.GAE;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 
 public class Application extends Controller {
	static final Set<String> emails = Sets.newHashSet(
			"andrea.salvadore@gmail.com",
			"mdenieffe@gmail.com",
			"michael.k.baxter@gmail.com",
			"ftrilnik@gmail.com");
 	
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
                 user.name = Auth.getUser().getNickname();
                 user.created = new Date();
                 user.save();
             }
             if (emails.contains(Auth.getEmail())){
             	render();
             }else{
             	forbidden("Not authorized");
             }
         }
     	//login();
     }
         
     
 
 }

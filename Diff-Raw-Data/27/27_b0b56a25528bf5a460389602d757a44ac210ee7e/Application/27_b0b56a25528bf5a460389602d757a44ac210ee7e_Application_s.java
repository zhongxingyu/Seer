 package controllers;
 
 import play.data.validation.Required;
 import play.mvc.*;
 import models.*;
 
 public class Application extends Controller {
 
     @Before
     static void bootStrapApp(){
         User newUser = new User("demo","demo","demo");
         newUser.persistNewUser();
     }
     public static void index() {
         render();
     }
     
    public static void deskScreen(User user){
         render(user);
     }
 
 }

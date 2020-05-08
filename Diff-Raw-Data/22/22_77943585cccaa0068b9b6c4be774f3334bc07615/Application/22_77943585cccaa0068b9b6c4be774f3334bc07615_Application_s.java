 package controllers;
 
import controllers.securesocial.SecureSocial;
 import models.User;
 import play.mvc.Controller;
import play.mvc.With;
 
@With(SecureSocial.class)
 public class Application extends Controller {
 
     public static User activeUser;
 
     public static void index() {
         render();
     }
 }

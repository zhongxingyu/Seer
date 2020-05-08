 package controllers;
 
 import models.Activity;
 import play.mvc.Controller;
 import play.mvc.With;
 import controllers.deadbolt.Deadbolt;
 
 @With(Deadbolt.class)
 public class Application extends Controller {
 
     public static void index() {
    	long unassigned = Activity.count("task_id = null");
         render(unassigned);
     }
 }

 package controllers;
 
 import info.seltenheim.play2.usertracking.actions.TrackAs;
 import play.mvc.Controller;
 import play.mvc.Result;
import views.html.index;
 
 @TrackAs(controller="App")
 public class Application extends Controller {
   
     @TrackAs(action="base")
     public static Result index() {
        return ok(index.render("Your new application is ready."));
     }
   
 }

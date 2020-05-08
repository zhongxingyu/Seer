 package controllers;
 
 import info.seltenheim.play2.usertracking.actions.TrackAs;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 @TrackAs(controller="App")
 public class Application extends Controller {
   
     @TrackAs(action="base")
     public static Result index() {
        return ok(views.html.index.render("Your new application is ready."));
     }
   
 }

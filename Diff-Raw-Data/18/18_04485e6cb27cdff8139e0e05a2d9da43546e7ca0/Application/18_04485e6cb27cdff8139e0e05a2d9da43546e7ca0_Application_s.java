 package de.htwg.seapal.maps.controllers;
 
 import play.*;
 import play.mvc.*;
 
 import de.htwg.seapal.boatdemo.views.html.*;
 
 public class Application extends Controller {
   
     public static Result index() {
        return ok(index.render("Your new application is ready."));
     }
   
 }

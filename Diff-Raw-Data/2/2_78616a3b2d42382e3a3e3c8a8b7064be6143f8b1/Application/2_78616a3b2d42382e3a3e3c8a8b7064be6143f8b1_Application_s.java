 package controllers;
 
 import models.Hotel;
 import play.data.Form;
 import play.mvc.*;
 
 public class Application extends Controller {
 
     public static Result index() {
        return ok(views.html.index.render());
     }
   
 }

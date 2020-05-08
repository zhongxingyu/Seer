 package controllers;
 
 import play.*;
 import play.libs.*;
 import play.libs.Json;
 import org.codehaus.jackson.*;
 import play.mvc.*;
 import java.util.*;
 
 import views.html.*;
 import models.*;
 
 public class Cheeses extends Controller {
   
     public static Result index() {
         // If the user is logged in, then display their cheeses
         // TODO: make it so that the two lists of cheeeses can be viewed while logged in
         if(session("email") != null) {
             List<Cheese> cheeses = 
                    Cheese.find.where().eq("user.id", session("email")).findList();
             User user = User.find.byId(session("email"));
            return ok(views.html.cheeses.userCheeses.render("Cheeses", cheeses, user));
         } else {
             List<Cheese> cheeses = 
                     Cheese.find.where().eq("visible", true).findList();
            return ok(views.html.cheeses.publicCheeses.render("Cheeses", cheeses));
         }
     }
 
     public static Result getById(Long cheese) {
         Cheese foundCheese = Cheese.find.byId(cheese);
         if(foundCheese != null) {
         	if(!request().accepts("text/html") && request().accepts("application/json")) {
         		JsonNode result = Json.toJson(foundCheese);
                 return ok(result);
         	} else {
         		return ok("You asked for this Cheese: " + foundCheese.name);
         	}
         } else {
             return badRequest("Sorry sir, we're out of that cheese (Monty Python)");
         }
     }
 
     public static Result create() {
     	return new Results.Todo();
     }
   	
     public static Result update(Long cheese) {
     	return new Results.Todo();
     }
 
     public static Result delete(Long cheese) {
     	return new Results.Todo();
     }
 }

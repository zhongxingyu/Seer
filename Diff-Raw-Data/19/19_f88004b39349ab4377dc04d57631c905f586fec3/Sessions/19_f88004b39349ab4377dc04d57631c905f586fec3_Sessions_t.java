 package controllers;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 import views.html.chatroom;
 import models.PrivateVars;
 /*
 import play.*;
 import play.mvc.*;
 import play.data.*;
 import static play.data.Form.*;
 */
 
 import models.*;
 import views.html.*;
 
 
 public class Sessions extends Controller {
 String ApiKey = PrivateVars.SCRIBBLAR_API_KEY;
 
     public static Result launchSession(String chatRoomId, String userScribblarId) {
    	return ok(chatroom.render("This is the page title for the chatroom.", charRoomId, userScribblarId));
     	//return ok(""+chatRoomId);
         //return ok(index.render("Your new application is ready."));
     }
 
     public static Result xmlTest() {
         return ok(Session.addRoom());
     }
 
     public static Result studentRegister() {
         return TODO;
     }
 
     public static Result tutorRegister() {
         return TODO;
     }
 
 }

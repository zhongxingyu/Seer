 package controllers;
 
 import play.mvc.Controller;
 
 public class Application extends Controller {
     public static void index() {
         if (request.headers.get("user-agent") != null
                 && request.headers.get("user-agent").toString().contains(
                         "AppleWebKit")) {
            render("Application/iphone.html");
         }
         render();
     }
 }

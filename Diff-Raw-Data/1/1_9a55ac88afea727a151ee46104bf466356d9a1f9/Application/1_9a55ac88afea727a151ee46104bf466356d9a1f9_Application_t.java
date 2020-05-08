 package controllers;
 
 import play.mvc.Controller;
 
 import java.io.File;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void upload(File file, long userId){
         System.out.println("Uploaded file name " + file.getName() + " by user " + userId);
		renderText("OK");
     }
 }

 package app.controllers;
 
 import org.javalite.activeweb.AppController;
 import org.javalite.activeweb.annotations.GET;
 
import de.pinuts.helper.StringFun;

 
 public class HelloController extends AppController {
 
     @GET
     public void index(){
 
         String theText = param("text");
 
         view("theText", "Simulacron sagt: " + theText);        
 
 		render("index").noLayout();
     }
 }

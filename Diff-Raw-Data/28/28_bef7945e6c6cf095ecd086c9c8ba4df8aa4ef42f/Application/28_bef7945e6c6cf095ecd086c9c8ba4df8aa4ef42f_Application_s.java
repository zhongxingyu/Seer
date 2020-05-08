 package controllers;
 
 import play.*;
 import play.db.jpa.JPA;
 import play.mvc.*;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.persistence.*;
 
 import org.hibernate.validator.constraints.Length;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
     
     public static void enhancers() {
    	render();
     }
     
     public static void tfsites() {
    	render();
     }
     
     public static void analyses() {
     	render();
     }
     
     public static void about() {
     	render();
     }
     
     public static void pairwise() {
     	render();
     }
     
     
     public static void rateOfLoss(List<TFsite> sites) {
     	//Do stuff
     }
     
     public static void rateOfGain(List<TFsite> sites) {
     	//Do stuff
     }
     
 
     
 
 }

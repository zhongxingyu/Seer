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
    	List<Enhancer> enhancers = Enhancer.all().fetch(25);
    	render(enhancers);
    }
    
    public static void enhancersByName(String name) {
    	Enhancer enhancer = Enhancer.find("byName", name).first();
    	render(enhancer);
     }
     
     public static void tfsites() {
    	List<TFsite> tfsites = TFsite.all().fetch(25);
    	render(tfsites);
    }
    
    public static void tfsitesByTF(String TF) {
    	List<TFsite> tfsites = TFsite.find("byTF", TF).fetch(25);
    	Logger.info(tfsites.toString());
    	render(tfsites);
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

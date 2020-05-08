 package controllers;
 
 import play.*;
 import play.modules.morphia.Model.MorphiaQuery;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	MorphiaQuery q2 = Operational.find(); // create a Query
 	    q2.or(q2.criteria("status").contains("Prospect"),
	    		q2.criteria("status").contains("Berthing"), 
 	    		q2.criteria("status").contains("Booking Approved"), 
 	    		q2.criteria("status").contains("Berthing Rejected"));
 	    List<Operational> berthings = q2.asList();
 	    render(berthings);
     }
     
 }

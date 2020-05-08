 package controllers;
 
 import play.*;
 import play.data.validation.Validation;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 @With(Secure.class)
 public class Admin extends Controller {
 	
 	@Before
 	static void setConnectedUser() {
 		if(Security.isConnected()) {
 			User user = User.find("byEmail", Security.connected()).first();
 			renderArgs.put("user", user.firstname);
 		}
 	}
 	 
     public static void index() {
         render();
     }
 
     
 //	public static void index() {
 //		String user = Security.connected();
 //		List<Shipment> shipments = Shipment.find("user.email", user).fetch();
 //		render(shipments);
 //	}
 //
 //	public static void save(Long id) {
 //		Shipment shipment;
 //		if(id == null) {
 //			// Create Shipment
 //			User user = User.find("byEmail", Security.connected()).first();
 //			shipment = new Shipment(user);
 //		} else {
 //			// Retrieve Shipments
 //			shipment = Shipment.findById(id);
 //			
 //			// Edit
 //			shipment.user = user;
 //		}
 //		
 //		
 //		// Validate
 //		validation.valid(shipment);
 //		if(validation.hasErrors()) {
 //			render("@form", shipment);
 //		}
 //		
 //		// Save
 //		shipment.save();
 //		index();
 //	}
 //	
 //	public static void form(Long id) {
 //		if(id != null) {
 //			Shipment shipment = Shipment.findById(id);
 //			render(shipment);
 //		}
 //		render();
 //	}
 }

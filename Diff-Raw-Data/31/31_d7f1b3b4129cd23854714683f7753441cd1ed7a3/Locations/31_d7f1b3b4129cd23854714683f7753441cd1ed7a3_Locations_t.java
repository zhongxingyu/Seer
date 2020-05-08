 package controllers;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.OneToMany;
 
 import controllers.CRUD.ObjectType;
 
import models.Company;
 import models.Location;
 import models.User;
 import play.*;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.mvc.*;
 
 public class Locations extends CRUD {
 
 //	@OneToMany(cascade=CascadeType.ALL, mappedBy="user")
 //	public List<Location> locations = new ArrayList<Location>();
 	
     public static void list(int page, String search, String searchFields, String orderBy, String order) {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         if (page < 1) {
             page = 1;
         }
 
         User user = User.find("byEmail", Security.connected()).first();

        //1 find company of user connected
        //2 find all users of company connected
        List<User> companyUsers = User.find("byCompany", user.company).fetch();
        
        //3 create a list of locations
        List<Location> allLocations = new ArrayList();
        
        //4 loop users
        for (int i = 0; i < companyUsers.size(); i++) {
        	
        	User thisUser = companyUsers.get(i);
        	
        	List<Location> userLocations = Location.find("byUser", thisUser).fetch();
        	
        	//5 find locations for user being looped
        	//6 add user locations to locations list
        	if (userLocations != null) {
        		allLocations.addAll(userLocations);
        	}
        }
        
        int count = allLocations.size(); //Location.count("byUser", user);
         
        List<Location> objects = allLocations;
         
         Long totalCount = type.count(null, null, (String) request.args.get("where"));
         
         render(type, objects, count, totalCount, page, orderBy, order, user);
     }
     
 	public static void blank() {
         User connectedUser = User.find("byEmail", Security.connected()).first();
         render(connectedUser);
     }
 
 }

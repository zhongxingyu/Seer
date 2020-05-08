 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import controllers.Security;
 
 import play.db.jpa.*;
 import play.data.validation.*;
 
 @Entity
 public class Location extends Model {
 	
 	public String name;
 	@Required
 	public String addresslineone;
 	public String addresslinetwo;
 	@Required
 	public String city;
 	@Required
 	public String state;
 	public String zip;
 	@Required
 	public String country;
 	@Required
 	public String phone;
 	
 	@ManyToOne
 	public User user;
 	
 	public Location(
 			User user, 
 			String name,
 			String addresslineone,
 			String addresslinetwo,
 			String city,
 			String state,
 			String zip,
 			String country,
 			String phone) {
 		this.user = user;
 		this.name = name;
 		this.addresslineone = addresslineone;
 		this.addresslinetwo = addresslinetwo;
 		this.city = city;
 		this.state = state;
 		this.zip = zip;
 		this.country = country;
 		this.phone = phone;
 		
 	}
 	
 	public String toString() {
 	    return name;
 	}
 
 	public static List<Location> findByUser(String email) {
 		User user = User.find("byEmail", email).first();
 		
 		List<Location> locations = Location.find("byUser_id",user.id).fetch();
//		if(user.isAdmin) {
//			locations = Location.findAll();
//		}
 		
 		return locations;
 	}
 	
 	
 }

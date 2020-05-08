 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 @Entity
 public class Trip extends Model {
 	
 	@SuppressWarnings("unused")
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 	
 	@Required
 	public City from;
 	
 	@Required
 	public City to;
 	
 	public double distance;
 	
 	@OneToMany(mappedBy = "trip", cascade = CascadeType.DETACH)
 	public List<City> cities;
 	
 	// Constructor
 	public Trip(){
		this.cities = new ArrayList<City>();
 	}
 }

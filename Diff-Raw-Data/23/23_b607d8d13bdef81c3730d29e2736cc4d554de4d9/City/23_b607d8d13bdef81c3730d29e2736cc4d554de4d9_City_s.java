 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 @Entity
 public class City extends Model {
 	// See: http://stackoverflow.com/questions/3825662/how-to-handle-jpa-many-to-one-relation
 	// OneToMany mapping & lazy load: http://stackoverflow.com/questions/2492675/jpa-one-to-many-relationship-persistence-bug
 	
 	@SuppressWarnings("unused")
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 	
 	@Required
 	public String name;
 	
 	@Column(name="longitude", nullable=true)
 	public double longitude;
 	
 	@Column(nullable=true)
 	public double latitude;
 	
 	@Column(nullable=true)
 	public int insee;
 	
 	@Column(nullable=true)
 	public int postalCode;
 	
 	@ManyToMany
 	public List<City> neighborhood;
 	
 	// Setters
 	public void setName(String value){
 		if(value == null || value.length() == 0){
 			throw new IllegalArgumentException("Bad value");
 		}
 		
 		this.name = value;
 	}
 }

 package models;
 
 import java.util.*;
 import play.db.ebean.*;
 import play.db.ebean.Model.Finder;
 import play.data.format.Formats.*;
 import play.data.validation.Constraints.*;
 import javax.persistence.*;
 import play.i18n.*;
 
 @Entity
 public class Plane extends Model {
 	
 	@Id
 	public Long id;
 
 	@Required
 	public String matriculation;
 
 	@Required
 	public Double price;
 
 	@Required
 	public Double weekReduction;
 
 	@OneToMany
 	public List<Flight> flights;
 
 	public static Finder<Long, Plane> find = new Finder(Long.class, Plane.class);
 
 	public String toString() {
 		return Messages.get("models.plane.toString", id, matriculation);
 	}
 
 	// Generates a list of options for a html select element
 	public static Map<String,String> options() {
 		LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
 		for (Plane plane : Plane.find.findList()) {
 			options.put(plane.id.toString(), plane.toString());
 		}
 		return options;
 	}
 }

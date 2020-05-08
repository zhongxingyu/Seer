 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import play.data.validation.Required;
 import play.db.jpa.*;
 
 @Entity
 public class Country extends Model {
 	@SuppressWarnings("unused")
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 	
 	@Required
 	public String name;
 	
 	public double latMin;
 	public double latMax;
 	public double longMin;
 	public double longMax;
 	
 	public int height;
 	
 	public int width;
 	
	@OneToMany
	public List<City> cites;
 }

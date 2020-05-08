 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
import play.db.ebean.Model;
 
 @Entity
 @Table(name = "availability")
 public class Availability {
 	
 	@Id
 	@Formats.NonEmpty
 	public Long availability_id;
 	
 	@Formats.NonEmpty
 	@Constraints.Required
 	@ManyToOne
 	@JoinColumn(name="person")
 	public Long person_id;  
 	
     @Formats.DateTime(pattern="yyyy-MM-dd")
     public Date from_date;
     
     @Formats.DateTime(pattern="yyyy-MM-dd")
     public Date to_date;
     
    public static Model.Finder<String, Availability> find = new Model.Finder(String.class, Availability.class);
    
	public static Availability findById(Long id) {
		return find.where().eq("availability_id", id).findUnique();
	}
	
	/**
	 * Validate Availability form.
	 * 
	 * @return null if validation is successful, else error message is returned.
	 */
	public String validate() {
	
		return null;
	}
 
 
 }

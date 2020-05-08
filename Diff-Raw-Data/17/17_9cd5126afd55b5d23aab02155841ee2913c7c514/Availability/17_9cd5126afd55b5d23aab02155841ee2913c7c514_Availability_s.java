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
     
 
 
 }

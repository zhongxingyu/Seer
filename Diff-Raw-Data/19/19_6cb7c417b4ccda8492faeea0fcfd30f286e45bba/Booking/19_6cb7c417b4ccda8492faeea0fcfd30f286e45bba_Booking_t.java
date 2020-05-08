 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
  
 import play.data.validation.Check;
 import play.data.validation.CheckWith;
 import play.db.jpa.*;
 
 import util.validation.*;
 
 @Entity
 public class Booking extends Model{
 	
	@ManyToOne(fetch=FetchType.LAZY)
 	  @JoinColumn(name="EVENT_ID")
 	public Event Event;
 	public long UserID;
 	public boolean vegetarian;
 	
 	
 	public Booking(Event event, long user, boolean vegetarian){
 		this.Event = event;
 		this.UserID = user;
 		this.vegetarian = vegetarian;
 	}
 
 }

 package models.concerts;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.jpa.*;
 import play.data.validation.*;
 
 public class Event extends Model {
 	@OneToMany(mappedBy="concertEvent")
 	public List<Band> bands;
 
 	public Date eventDate;
 	public String startTime;
 
 	public Event() {}
 	public Event(Date eventDate, String startTime) {
 		this.eventDate = eventDate;
 		this.startTime = startTime;
 	}
 }

 package models;
 
import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.db.jpa.Model;
 
 @Entity
 public class Calendar extends Model {
 
 	public String name;
 
 	@ManyToOne
 	public User owner;
 
 	@OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL)
 	public List<Event> events;
 
 	public Calendar(String name, User owner) {
 		this.name = name;
 		this.owner = owner;
		this.events = new ArrayList<Event>();
 	}
 
 }

 package models.concerts;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.jpa.*;
import play.data.validation.*;
 
 @Entity
 public class Band extends Model {
 	public String bandName;
 
 	@ManyToOne
 	public Event concertEvent;
 
 	public Band(String bandName) {
 		this.bandName = bandName;
 	}
 }

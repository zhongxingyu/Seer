 package models;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 
 import play.db.jpa.GenericModel;
 import play.db.jpa.Model;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 
 @Entity
 public class Attendee extends Model {
 	
 	@ManyToOne
 	public User user;
 	
 	@ManyToOne
 	public Meeting meeting;
 	
 	@Enumerated(EnumType.STRING)
 	public MeetingResponse rsvp;
 	
 	public enum MeetingResponse {
 		YES, MAYBE, NO
 	}
 	
	@Override
 	public GenericModel delete() {
 		
 		// Remove 
 		user.meetingsRelated.remove(this);
 		user.save();
 		meeting.attendees.remove(this);
 		meeting.save();
 		
 		return super.delete();
 	}
 
 }

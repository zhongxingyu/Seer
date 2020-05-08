 package models;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import models.eventstream.EventTopic;
 import models.eventstream.UserEventBuffer;
 
 import com.google.gson.JsonObject;
 
 import play.Logger;
 import play.data.validation.Unique;
 import play.db.jpa.Model;
 
 import play.db.jpa.*;
 
 /**
  * User model and Entity class, used by JPA to save user data into the connected
  * database
  * 
  * @author Alexandre Bourdin
  * 
  */
 @Entity
 @Table(name = "users")
 public class User extends Model {
 	@Unique
 	public String email;
 	public String password;
 	public String name;
 	public String gender;
 	public String facebookId;
 	public String googleId;
 	public String twitterId;
 	public String avatarUrl;
 	public String mailnotif;
 	public Integer connected;
 	@ElementCollection
 	@controllers.CRUD.Exclude
 	public List<String> eventTopicIds;
 	@Transient
 	public UserEventBuffer eventBuffer;
 	@Transient
 	public Date lastRequest;
 
 	public User(String email, String password, String name, String gender,
 			String mailnotif, ArrayList<String> eventTopicIds) {
 		this.email = email;
 		this.password = password;
 		this.name = name;
 		this.gender = gender;
 		this.facebookId = null;
 		this.googleId = null;
 		this.twitterId = null;
 		this.avatarUrl = null;
 		this.eventTopicIds = eventTopicIds;
 		this.mailnotif = mailnotif;
 		this.connected = 0;
 		this.lastRequest = new Date();
		UserEventBuffer eventBuffer = new UserEventBuffer();
 	}
 
 	public User(String email, String password, String name, String gender,
 			String mailnotif) {
 		this(email, password, name, gender, mailnotif, new ArrayList<String>());
 	}
 
 	/**
 	 * Subscribe to a topic
 	 * 
 	 * @param et
 	 * @return
 	 */
 	public boolean subscribe(EventTopic et) {
 		if (eventTopicIds.contains(et.getId())) {
 			return false;
 		}
 		eventTopicIds.add(et.getId());
 		Collections.sort(eventTopicIds);
 		et.subscribersCount++;
 		update();
 		return true;
 	}
 
 	/**
 	 * Unsubscribe to a topic
 	 * 
 	 * @param topicId
 	 * @return
 	 */
 	public boolean unsubscribe(EventTopic et) {
 		if (!eventTopicIds.contains(et.getId())) {
 			return false;
 		}
 		eventTopicIds.remove(et.getId());
 		// et.subscribersCount--;
 		update();
 		return true;
 	}
 
 	/**
 	 * Get topics the user has subscribed to
 	 * 
 	 * @return
 	 */
 	public ArrayList<EventTopic> getTopics() {
 		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
 		for (String sid : eventTopicIds) {
 			EventTopic es = ModelManager.get().getTopicById(sid);
 			if (es != null) {
 				result.add(es);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Do subscriptions to all topics Called on user connection to add user to
 	 * the subscribing user list of all topics he has subscribed to (avoids
 	 * database calls)
 	 * 
 	 * public void doSubscriptions() { for (String esid : eventTopicIds) {
 	 * EventTopic eb = ModelManager.get().getTopicById(esid); if (eb != null) {
 	 * eb.addUser(this); } } }
 	 */
 
 	public void update() {
 		User u = User.find("byId", this.id).first();
 		if(u != null) {
 			u.email = email;
 			u.password = password;
 			u.name = name;
 			u.gender = gender;
 			u.mailnotif = mailnotif;
 			u.facebookId = facebookId;
 			u.googleId = googleId;
 			u.twitterId = twitterId;
 			u.avatarUrl = avatarUrl;
 			u.connected = connected;
 			
 			u.eventTopicIds = eventTopicIds;
 			u.save();	
 		}
 	}
 
 	/**
 	 * GETTERS AND SETTERS
 	 */
 
 	public UserEventBuffer getEventBuffer() {
 		return eventBuffer;
 	}
 
 	public void setEventBuffer(UserEventBuffer eventBuffer) {
 		this.eventBuffer = eventBuffer;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (!(o instanceof User))
 			return false;
 		User u = (User) o;
 		if (u.id.equals(id) && u.name.equals(name) && u.password.equals(password)
 				&& u.email.equals(email)) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "User id=" + id + " [email=" + email + ", name=" + name + ", gender=" + gender + ", eventTopicIds=" + eventTopicIds + "]";
 	}
 
 }

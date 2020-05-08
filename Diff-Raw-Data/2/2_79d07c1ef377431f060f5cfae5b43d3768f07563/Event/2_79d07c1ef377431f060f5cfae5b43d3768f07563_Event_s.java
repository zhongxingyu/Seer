 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.annotation.CreatedTimestamp;
 
 /**
  * Event model
  * 
  * @author Lars Kristian
  * 
  */
 @Entity
 @Table
 public class Event extends Model {
 
 	public enum MessageType {
 		UPDATE, ALARM, EXCEPTION
 	};
 
 	private static final long serialVersionUID = -599938708834182681L;
 
 	@Id
 	public long id;
 
 	@Transient
 	public Long sheepId;
 
 	@Transient
 	public Long sheepPid;
 
 	@Column(nullable = false)
 	public long rfid;
 
 	@Required
 	@ManyToOne(optional = false)
 	@JoinColumn(name = "sheep_id", referencedColumnName = "id", nullable = false)
 	public Sheep sheep;
 
 	public MessageType messageType;
 
 	@Formats.DateTime(pattern = "yyyy-MM-dd hh:mm:ss")
 	public Date timeSent;
 
 	@CreatedTimestamp
 	@Formats.DateTime(pattern = "yyyy-MM-dd hh:mm:ss")
 	public Date timeReceived;
 
 	public double latitude;
 	public double longitude;
 
 	public int pulse;
 	public double temperature;
 
 	/**
 	 * Event finder
 	 */
 	public static Model.Finder<Long, Event> find = new Model.Finder<Long, Event>(Long.class, Event.class);
 
 	/**
 	 * Finds all events for a given user id, limited by a max length limit
 	 * 
 	 * @param userId The user ID to find events for
 	 * @param limit The list max limit
 	 * @return A list of events
 	 */
 	public static List<Event> findByUserId(Long userId, int limit) {
		return find.where().eq("sheep.user.userId", userId).orderBy().desc("timeSent").setMaxRows(limit).findList();
 	}
 
 	/**
 	 * Finds all events of a given type for a given user ID, limited by max length
 	 * 
 	 * @param userId The user ID to find events for
 	 * @param type The type of events to find
 	 * @param limit The maximum limit
 	 * @return A list of events
 	 */
 	public static List<Event> findTypeByUserId(long userId, MessageType type, int limit) {
 		return find.where().eq("sheep.user.id", userId).eq("messageType", type).orderBy().desc("timeSent").setMaxRows(limit).findList();
 	}
 
 	/**
 	 * Finds all events for a given sheep ID
 	 * 
 	 * @param id The sheep ID to find events for
 	 * @return A list of events
 	 */
 	public static List<Event> findBySheepId(Long id) {
 		return find.where().eq("sheep.id", id).orderBy().desc("timeSent").findList();
 	}
 
 	/**
 	 * Finds all events for a given sheep ID, list length limited by limit
 	 * 
 	 * @param sheepId The Sheep ID to find events for
 	 * @return A list of events
 	 */
 	public static List<Event> findBySheepIdLimit(long sheepId, int limit) {
 		return find.where().eq("sheep.id", sheepId).orderBy().desc("timeSent").setMaxRows(limit).findList();
 	}
 
 	/**
 	 * Checks if a given user ID is the owner of a given event ID
 	 * 
 	 * @param eventId The event ID
 	 * @param userId The user ID
 	 * @return True if the user is the owner, false otherwise
 	 */
 	public static boolean isOwner(Long eventId, String userId) {
 		return find.where().eq("id", eventId).eq("sheep.user.id", userId).findRowCount() > 0;
 	}
 
 }

 package uk.frequency.glance.server.model.user;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 import uk.frequency.glance.server.model.GenericEntity;
 import uk.frequency.glance.server.model.event.Event;
 import uk.frequency.glance.server.model.trace.Trace;
 
 @Entity
 @Table(name="tb_user") //for PostgreSQL compatibility, TODO:do this for all tables as a NamingStrategy 
 public class User extends GenericEntity{
 
 	@Column(unique=true)
 	String username;
 	
 	@Column(unique=true)
 	String facebookId;
 	
 	String fbAccessToken;
 	
 	@Cascade(value=CascadeType.DELETE)
 	@ElementCollection
 	@CollectionTable(joinColumns=@JoinColumn(name="user_id")) //for PostgreSQL compatibility, TODO:do this for all fks as a NamingStrategy
 	List<UserProfile> profileHistory;
 	
 	UserSettings settings;
 	
 	@Cascade(value=CascadeType.DELETE)
 	@OneToMany(mappedBy="user")
 	List<Event> events;
 	
 	@Cascade(value=CascadeType.DELETE)
 	@OneToMany(mappedBy="user")
 	List<Trace> traces;
 	
 	@Cascade(value=CascadeType.DELETE)
 	@OneToOne(mappedBy="user")
 	EventGenerationInfo eventGenerationInfo;
 	
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getFacebookId() {
 		return facebookId;
 	}
 
 	public void setFacebookId(String facebookId) {
 		this.facebookId = facebookId;
 	}
 
 	public UserSettings getSettings() {
 		return settings;
 	}
 
 	public void setSettings(UserSettings settings) {
 		this.settings = settings;
 	}
 	
 
 	public List<UserProfile> getProfileHistory() {
 		return profileHistory;
 	}
 
 	public void setProfileHistory(List<UserProfile> profileHistory) {
 		this.profileHistory = profileHistory;
 	}
 	
 	public void setProfile(UserProfile... profile){
 		this.profileHistory = Arrays.asList(profile);
 	}
 
 	public List<Event> getEvents() {
 		return events;
 	}
 
 	public void setEvents(List<Event> events) {
 		this.events = events;
 	}
 
 	public List<Trace> getTraces() {
 		return traces;
 	}
 
 	public void setTraces(List<Trace> traces) {
 		this.traces = traces;
 	}
 
 	public EventGenerationInfo getEventGenerationInfo() {
 		return eventGenerationInfo;
 	}
 
 	public void setEventGenerationInfo(EventGenerationInfo eventGenerationInfo) {
 		this.eventGenerationInfo = eventGenerationInfo;
 	}
 
 	public String getFbAccessToken() {
 		return fbAccessToken;
 	}
 
 	public void setFbAccessToken(String fbAccessToken) {
 		this.fbAccessToken = fbAccessToken;
 	}
 
 	@Override
 	public String toString() {
 		return super.toString()
 				+ " | " + profileHistory;
 	}
 	
 }

 package model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * This class is an entity class that represents the connection between conferences and invitees participants in 
  * in the database (e.g. whether a user arrived to a conference).
  * This class holds no logic, only getters, setter and constructors.
  * This class also contains instructions to hibernate that define how the entity should be saved to the database.
  */
 @Entity
 @Table( name = "Conferences_Participants" )
 public class ConferencesParticipants implements Serializable {
 
 	private static final long serialVersionUID = -4855020745124479949L;
 	
 	private Conference conference; 
 	private User user; 
 	private Date conferenceDateDay;
 	
 	ConferencesParticipants() {}  //not public on purpose!
 	
 	public ConferencesParticipants(Conference conf, User user, Date date) 
 	{
 		this.conference = conf;
 		this.user = user;
 		this.conferenceDateDay = date;
 	}
 	
 	@Id
 	@ManyToOne(cascade = CascadeType.ALL)
 	public Conference getConference() {
 		return conference;
 	}
 	public void setConference(Conference conference) {
 		this.conference = conference;
 	}
 	
 	@Id
 	@ManyToOne(cascade = CascadeType.ALL)
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	@Id
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date getDate() {
 		return conferenceDateDay;
 	}
 	public void setDate(Date date) {
 		this.conferenceDateDay = date;
 	}
 	
 }

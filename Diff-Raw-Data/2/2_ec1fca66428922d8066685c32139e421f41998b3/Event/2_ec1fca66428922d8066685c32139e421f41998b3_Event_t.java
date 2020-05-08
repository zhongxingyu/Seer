 package com.eventsharing.entity;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
@Entity @Table(name="EVENT")
 public class Event {
 
 	@Id
 	@Column(name="EVENT_ID")
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private Integer eventID;
 	
 	@Column(name="NAME")
 	private String name;
 	
 	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="LOCATION_ID")
 	private EventLocation eventLocation;
 	
 	@Column(name="DATETIME")
 	private String dateTime;
 
 	public Integer getEventID() {
 		return eventID;
 	}
 
 	public void seteventID(Integer id) {
 		this.eventID = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public EventLocation getEventLocation() {
 		return eventLocation;
 	}
 
 	public void setLocation(EventLocation eventLocation) {
 		this.eventLocation = eventLocation;
 	}
 
 	public String getDateTime() {
 		return dateTime;
 	}
 
 	public void setDateTime(String dateTime) {
 		this.dateTime = dateTime;
 	}
 	
 	
 }

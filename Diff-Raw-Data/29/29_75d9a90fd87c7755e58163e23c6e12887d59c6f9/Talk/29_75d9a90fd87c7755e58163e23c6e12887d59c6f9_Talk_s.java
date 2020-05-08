 package com.prodyna.pac.conference.client.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.validation.constraints.AssertTrue;
 import javax.validation.constraints.Future;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import com.prodyna.pac.conference.common.util.DateUtil;
 
 @Entity
 @Table(name = "talk")
 @NamedQueries({
 		@NamedQuery(name = "Talk.findTalkById", query = "SELECT t FROM Talk t WHERE t.id = :id"),
 		@NamedQuery(name = "Talk.findTalkByName", query = "SELECT t FROM Talk t WHERE t.name = :name"),
 		@NamedQuery(name = "Talk.findTalksByConferenceId", query = "SELECT t FROM Talk t WHERE t.conference.id = :id"),
 		@NamedQuery(name = "Talk.findTalksByRoomId", query = "SELECT t FROM Talk t WHERE t.room.id = :id"),
 		@NamedQuery(name = "Talk.findAll", query = "SELECT t FROM Talk t"), })
 public class Talk implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private long id;
 
 	@NotNull
 	@Size(min = 1, max = 100)
 	private String name;
 
 	@NotNull
 	@Size(min = 1, max = 500)
 	private String description;
 
 	@Min(1)
 	private int duration;
 
 	@NotNull
 	@Future
 	private Date start;
 
 	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "room_id")
 	private Room room;
 
 	@ManyToOne(optional = false,fetch=FetchType.EAGER)
	@JoinColumn(name = "conference_id")
 	private Conference conference;
 
 	@AssertTrue(message = "Talk is not in daterange of the Conference")
 	private boolean isInConferenceDateRange() {
 
 		Date conferenceStart = conference.getStart();
 		Date conferenceEnd = conference.getEnd();
 		Date end = DateUtil.addMinutesToDate(start, duration);
 
 		return conferenceStart.before(start) && conferenceEnd.after(end);
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public int getDuration() {
 		return duration;
 	}
 
 	public void setDuration(int duration) {
 		this.duration = duration;
 	}
 
 	public Room getRoom() {
 		return room;
 	}
 
 	public void setRoom(Room room) {
 		this.room = room;
 	}
 
 	public Conference getConference() {
 		return conference;
 	}
 
 	public void setConference(Conference conference) {
 		this.conference = conference;
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public void setStart(Date start) {
 		this.start = DateUtil.normalizeDate(start);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((conference == null) ? 0 : conference.hashCode());
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		result = prime * result + duration;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((room == null) ? 0 : room.hashCode());
 		result = prime * result + ((start == null) ? 0 : start.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Talk other = (Talk) obj;
 		if (conference == null) {
 			if (other.conference != null)
 				return false;
 		} else if (!conference.equals(other.conference))
 			return false;
 		if (description == null) {
 			if (other.description != null)
 				return false;
 		} else if (!description.equals(other.description))
 			return false;
 		if (duration != other.duration)
 			return false;
 		if (id != other.id)
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (room == null) {
 			if (other.room != null)
 				return false;
 		} else if (!room.equals(other.room))
 			return false;
 		if (start == null) {
 			if (other.start != null)
 				return false;
 		} else if (!start.equals(other.start))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Talk [id=" + id + ", name=" + name + ", description="
 				+ description + ", duration=" + duration + ", start=" + start
 				+ ", room=" + room + ", conference=" + conference + "]";
 	}
 
 }

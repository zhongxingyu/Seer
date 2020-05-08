 package org.sukrupa.event;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.sukrupa.platform.DoNotRemove;
 import org.sukrupa.student.Student;
 
 import javax.persistence.*;
 import java.util.Set;
 
 @Entity
 public class Event {
 
 	@Id
 	@GeneratedValue
 	@Column(name = "event_id")
 	private int eventId;
 
 	@Column(name = "event_title")
 	private String title;
 
 	@Column(name = "event_venue")
 	private String venue;
 
 	@Column(name = "event_coordinator")
 	private String coordinator;
 
 	@Column(name = "event_description")
 	private String description;
 
 	@Column(name = "event_notes")
 	private String notes;
 
 
 	@ManyToMany
	@JoinTable(name = "EVENTATTENDEES",
 			joinColumns = {@JoinColumn(name = "event_id")},
 			inverseJoinColumns = {@JoinColumn(name = "id")})
 	private Set<Student> attendees;
 
 	@Type(type = "org.sukrupa.event.PersistentEventDate")
 	private EventDate datetime;
 
 	public Set<Student> getAttendees() {
 		return this.attendees;
 	}
 
 	@DoNotRemove
 	public Event() {
 	}
 
 	public Event(String title, EventDate datetime, String venue, String coordinator, String description, String notes, Set<Student> attendees) {
 		this.title = title;
 		this.datetime = datetime;
 		this.venue = venue;
 		this.coordinator = coordinator;
 		this.description = description;
 		this.notes = notes;
 		this.attendees = attendees;
 	}
 
 	@Transient
 	private String[] excludedFields = new String[] {"eventId"};
 
 	public boolean equals(Object other) {
 		return EqualsBuilder.reflectionEquals(this, other, excludedFields);
 	}
 
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this, excludedFields);
 	}
 
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
 	}
 
 	public static Event createFrom(EventRecord eventRecord, Set<Student> attendees) {
 		return new EventBuilder().title(eventRecord.getTitle())
 			    .venue(eventRecord.getVenue())
 			    .description(eventRecord.getDescription())
 			    .coordinator(eventRecord.getCoordinator())
 			    .notes(eventRecord.getNotes())
 			    .datetime(parseDateTime(eventRecord))
 			    .attendees(attendees)
 			    .build();
 	}
 
 	private static EventDate parseDateTime(EventRecord eventRecord) {
 		return new EventDate(eventRecord.getDate(), eventRecord.getTime());
 	}
 }

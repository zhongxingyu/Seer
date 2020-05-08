 package no.niths.domain;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlSchemaType;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import no.niths.common.AppConstants;
 import no.niths.domain.adapter.JsonCalendarAdapter;
 import no.niths.domain.adapter.XmlCalendarAdapter;
 import no.niths.domain.location.Location;
 
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
 @XmlRootElement(name = AppConstants.FEED)
 @Entity
 @Table(name = AppConstants.FEEDS)
 @XmlAccessorType(XmlAccessType.FIELD)
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class Feed implements Serializable {
 
 	@Transient
 	private static final long serialVersionUID = -7280194759501252804L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 
 	@Column
 	@Size(min = 0, max = 255, message = "Can not be more then 255 chars")
 	private String message;
 
 	@Column(name = "published")
 	@Temporal(TemporalType.TIMESTAMP)
 	@XmlSchemaType(name = "date")
 	@XmlJavaTypeAdapter(XmlCalendarAdapter.class)
 	private Calendar published;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinTable(name = "feeds_location", 
 		joinColumns = @JoinColumn(name = "feeds_id"), 
 		inverseJoinColumns = @JoinColumn(name = "location_id"))
 	@Cascade(CascadeType.ALL)
 	private Location location;
 
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinTable(name = "feeds_student", 
 		joinColumns = @JoinColumn(name = "feeds_id"), 
 		inverseJoinColumns = @JoinColumn(name = "student_id"))
 	@Cascade(CascadeType.ALL)
 	private Student student;
 
 	public Feed() {
 		this(null);
 		setPublished(null);
 		setStudent(null);
 		setLocation(null);
 		setPublished(null);
 		
 	}
 
 	public Feed(String message) {
 		setMessage(message);
 		setPublished(new GregorianCalendar());
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public Student getStudent() {
 		return student;
 	}
 
 	public void setStudent(Student student) {
 		this.student = student;
 	}
 
 	@Override
 	public boolean equals(Object that) {
 		if (this == that)
 			return true;
 
 		if (!(that instanceof Feed))
 			return false;
 
 		Feed feed = (Feed)that;

		return (getId()==(feed.getId()));
 	}
 
 	@Override
 	public String toString() {
 		return String.format("[%s][%s]", id, message);
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	@JsonSerialize(using = JsonCalendarAdapter.class)
 	public Calendar getPublished() {
 		return published;
 	}
 
 	public void setPublished(Calendar published) {
 		this.published = published;
 	}
 }

 package max.utility.tomato.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.hibernate.annotations.Type;
 import org.joda.time.LocalDateTime;
 
 @Entity
 public class Tomato {
 
 	@Column(nullable = false, length = 500)
 	private String focusOn;
 
 	@Id
 	@Column(name = "ID", unique = true, nullable = false, scale = 0)
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 
 	@Column(nullable = false)
 	// @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
 	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
 	private LocalDateTime startTime;
 
 	public Tomato() {
 		super();
 	}
 
 	public Tomato(String focusOn) {
 		this.focusOn = focusOn;
 		startTime = new LocalDateTime();
 	}
 
 	public String getFocusOn() {
 		return focusOn;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public LocalDateTime getStartTime() {
 		return startTime;
 	}
 
 	public void setFocusOn(String focusOn) {
 		this.focusOn = focusOn;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setStartTime(LocalDateTime startTime) {
 		this.startTime = startTime;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Tomato [id=");
 		builder.append(id);
 		builder.append(", focusOn=");
 		builder.append(focusOn);
 		builder.append(", startTime=");
 		builder.append(startTime);
 		builder.append("]");
 		return builder.toString();
 	}
 
 }

 package se.ryttargardskyrkan.rosette.model;
 
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.hibernate.validator.constraints.ScriptAssert;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.index.Indexed;
 import org.springframework.data.mongodb.core.mapping.Document;
 import se.ryttargardskyrkan.rosette.converter.RosetteDateTimeTimezoneJsonDeserializer;
 import se.ryttargardskyrkan.rosette.converter.RosetteDateTimeTimezoneJsonSerializer;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.util.Date;
 
 @Document(collection = "posters")
@ScriptAssert(lang = "javascript", script = "_this.startTime.before(_this.endTime)", message = "poster.startBeforeEndTime")
 public class Poster {
     @Id
     private String id;
 
     @NotEmpty(message = "poster.title.notEmpty")
     private String title;
 
 	// Start using poster after this time
 	@NotNull(message = "poster.startTime.notNull")
 	@Indexed
 	@JsonSerialize(using = RosetteDateTimeTimezoneJsonSerializer.class)
 	@JsonDeserialize(using = RosetteDateTimeTimezoneJsonDeserializer.class)
 	private Date startTime;
 
 	// Don't use poster after this time
 	@NotNull(message = "poster.endTime.notNull")
 	@Indexed
 	@JsonSerialize(using = RosetteDateTimeTimezoneJsonSerializer.class)
 	@JsonDeserialize(using = RosetteDateTimeTimezoneJsonDeserializer.class)
 	private Date endTime;
 
 	// Number of seconds to display poster in "slide show"
 	@Min(value = 1, message = "poster.duration.tooShort")
 	private int duration;
 	
     // Getters and setters
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public Date getEndTime() {
 		return endTime;
 	}
 
 	public void setEndTime(Date endTime) {
 		this.endTime = endTime;
 	}
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 }

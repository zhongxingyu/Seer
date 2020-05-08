 package velo.entity;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Lob;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 @MappedSuperclass
 public class LogEntry {
 	public enum EventLogLevel {
     	DEBUG,INFO,WARN,ERROR,CRITICAL
     }
 	
     private String message;
     private EventLogLevel level;
     private Date creationDate;
     private String server;
     
 
     public LogEntry() {
     	
     }
     
     public LogEntry(EventLogLevel level, String message) {
 		this.message = message;
 		this.level = level;
 		this.creationDate = new Date();
 		//this.server = server;
 	}
     
     @Lob
 	@Column(name="MESSAGE", nullable=false)
 	public String getMessage() {
 		return message;
 	}
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	
	//Level is a unique identifier in Oracle
	//@Column(name="LEVEL")
	@Column(name="LOG_LEVEL")
     @Enumerated(EnumType.STRING)
 	public EventLogLevel getLevel() {
 		return level;
 	}
 	public void setLevel(EventLogLevel level) {
 		this.level = level;
 	}
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name="CREATION_DATE")
 	public Date getCreationDate() {
 		return creationDate;
 	}
 	
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 	
 	@Column(name="SERVER")
 	public String getServer() {
 		return server;
 	}
 	
 	public void setServer(String server) {
 		this.server = server;
 	}
 }

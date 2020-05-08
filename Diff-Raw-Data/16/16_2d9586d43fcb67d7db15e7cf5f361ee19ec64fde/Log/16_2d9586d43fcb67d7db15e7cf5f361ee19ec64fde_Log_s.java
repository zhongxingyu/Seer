 package cs.wintoosa.domain;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 
 /**
  *
  * @author jonimake
  */
@MappedSuperclass
//@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
 public abstract class Log implements Serializable{
 
     private static final long serialVersionUID = 1234l;
     
     @Id
     @GeneratedValue(strategy= GenerationType.AUTO)
     protected Long id;
     
     @ManyToOne(targetEntity=SessionLog.class)
     protected SessionLog sessionLog;
     
     @NotNull(message="PhoneId can't be null")
     protected String phoneId;
     
     @NotNull (message="timestamp must represent valid unixtime in seconds")
     protected Long timestamp;
     
     @Transient
     private String checksum;
        
     public Log() {
     }
     
     public Log(String phoneId) {
         this.phoneId = phoneId;
     }
     
     public String getPhoneId() {
         return phoneId;
     }
 
     public void setPhoneId(String id) {
         this.phoneId = id;
     }
     public Long getId() {
         return id;
     }
 
     public void setId(Long logId) {
         this.id = logId;
     }
 
     public Long getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Long timestamp) {
         this.timestamp = timestamp;
     }
 
     public String getChecksum() {
         return checksum;
     }
 
     public void setChecksum(String checksum) {
         this.checksum = checksum;
     }
 
     public SessionLog getSessionLog() {
         return sessionLog;
     }
 
     public void setSessionLog(SessionLog sessionLog) {
         this.sessionLog = sessionLog;
     }
 
 }

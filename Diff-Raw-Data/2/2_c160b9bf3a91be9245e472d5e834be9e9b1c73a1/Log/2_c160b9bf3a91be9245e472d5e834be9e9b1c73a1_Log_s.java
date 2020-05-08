 package cs.wintoosa.domain;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 /**
  *
  * @author jonimake
  */
 @Entity
publicsda  class Log implements Serializable{
     
     @Id
     private Long phoneId;
     
     private String lines;
 
     public String getLines() {
         return lines;
     }
 
     public void setLines(String lines) {
         this.lines = lines;
     }
 
     public Long getPhoneId() {
         return phoneId;
     }
 
     public void setPhoneId(Long phoneId) {
         this.phoneId = phoneId;
     }
 }

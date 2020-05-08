 package com.aciertoteam.common.dto;
 
 import com.aciertoteam.common.interfaces.Identifiable;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import java.util.Date;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class AbstractDTO implements Identifiable {
 
     @JsonProperty("id")
     private Long id;
 
     @JsonProperty("valid_from")
     private Date validFrom;
 
     @JsonProperty("valid_thru")
     private Date validThru;
 
     @JsonProperty("timestamp")
     private Date timestamp;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Date getValidFrom() {
         return validFrom;
     }
 
     public void setValidFrom(Date validFrom) {
         this.validFrom = validFrom;
     }
 
     public Date getValidThru() {
         return validThru;
     }
 
     public void setValidThru(Date validThru) {
         this.validThru = validThru;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 }

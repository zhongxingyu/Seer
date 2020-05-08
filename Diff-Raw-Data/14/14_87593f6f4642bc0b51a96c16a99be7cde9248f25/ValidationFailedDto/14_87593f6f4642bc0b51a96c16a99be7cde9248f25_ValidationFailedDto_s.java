 package oshop.web.api.dto;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import java.util.Map;
 
 
 public class ValidationFailedDto {
 
     private String error;
 
     @JsonProperty("fields")
     private Map<String, String> fields;
 
     public ValidationFailedDto(Map<String, String> fields) {
         this.fields = fields;
     }
 
     public String getError() {
         return error;
     }
 
     public void setError(String error) {
         this.error = error;
     }
 
    public Map<String, String> getMap() {
         return fields;
     }
 
    public void setMap(Map<String, String> fields) {
         this.fields = fields;
     }
 }

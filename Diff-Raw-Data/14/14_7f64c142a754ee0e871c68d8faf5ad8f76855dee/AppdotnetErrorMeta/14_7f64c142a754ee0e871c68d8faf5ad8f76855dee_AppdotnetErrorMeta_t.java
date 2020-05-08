 package org.springframework.social.appdotnet.api.impl.error;
 
import org.codehaus.jackson.annotate.JsonCreator;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
 
 /**
  * @author Arik Galansky
  */
 @JsonIgnoreProperties(ignoreUnknown = true)
 class AppdotnetErrorMeta {
     private int code;
     private String errorMessage;
     private String errorSlug;
 
    @JsonCreator
    public AppdotnetErrorMeta(@JsonProperty("code") int code, @JsonProperty("error_message") String errorMessage, @JsonProperty("error_slug") String errorSlug) {
         this.code = code;
         this.errorMessage = errorMessage;
         this.errorSlug = errorSlug;
     }
 
     public int getCode() {
         return code;
     }
 
     public String getErrorMessage() {
         return errorMessage;
     }
 
     public String getErrorSlug() {
         return errorSlug;
     }
 }

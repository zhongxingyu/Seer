 package com.cloudbees.cloud_resource.types;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 /**
  * Represents Cloud Resource error object
  *
  * CRs are free to report any additional properties in the error.
  *
  * Cloud-Resource-Type: https://types.cloudbees.com/error
  * Schema := Error
  * Error := object {
  *     “message”: string?,	// human readable description of an error
  *     “url”: string?,		// URL that provides more information for humans
  *     “cause”: Error?	// nested error, if any
  * }
  *
  * @author Vivek Pandey
  */
 @CloudResourceType("https://types.cloudbees.com/error")
public class CloudResourceError implements CloudResource {
 
     private final String message;
 
     private final String url;
 
     private final CloudResourceError cause;
 
     public CloudResourceError(String message,  CloudResourceError cause, String url) {
         this.message = message;
         this.url = url;
         this.cause = cause;
     }
 
     public CloudResourceError(String message) {
         this.message = message;
         this.url = null;
         this.cause = null;
     }
 
     @JsonProperty("message")
     public String getMessage() {
         return message;
     }
 
     @JsonProperty("url")
     public String getUrl() {
         return url;
     }
 
     @JsonProperty("cause")
     public CloudResourceError getCause() {
         return cause;
     }
 }

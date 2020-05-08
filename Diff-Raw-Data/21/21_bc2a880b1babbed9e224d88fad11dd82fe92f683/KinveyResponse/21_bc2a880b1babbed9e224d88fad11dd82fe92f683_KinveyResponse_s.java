 package com.kinvey.business_logic;
 
 
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 import java.util.HashMap;
 
 
 /**
  *  An "internal" Kinvey Response object.
  */
 public class KinveyResponse {
     private boolean complete;
     private HashMap<String, Object> headers;
     private HashMap<String, Object> body;
     private int statusCode;
     private int status;
 
     /**
      * Create a new KinveyResponse (helper for Jackson).
      *
      * @param status  the HTTP status code of the response
      * @param complete  a status flag indicating that processing should terminate
      */
     @JsonCreator()
     public KinveyResponse(@JsonProperty("status") int status, @JsonProperty("complete") boolean complete) {
         this.status = status;
         this.statusCode = status;
         this.complete = complete;
     }
 
 
     public boolean isComplete() {
         return complete;
     }
 
     public void setComplete(boolean complete) {
         this.complete = complete;
     }
 
     public HashMap<String, Object> getHeaders() {
         return headers;
     }
 
     public void setHeaders(HashMap<String, Object> headers) {
         this.headers = headers;
     }
 
     public HashMap<String, Object> getBody() {
         return body;
     }
 
     public void setBody(HashMap<String, Object> body) {
         this.body = body;
     }
 
     public void setError(KinveyError error) {
         this.statusCode = error.getErrorCode();
        this.body = error.getErrorBody();
         this.setComplete(true);
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     /**
      * Update the HTTP status code.
      *
      * @param status  the new HTTP status code to use
      *
      */
     public void setStatus(int status) {
         this.status = status;
         this.statusCode = status;
     }
 
     /**
      * Generate the debugging string for a response.
      *
      * @return  the string representation
      *
      */
     public String toString() {
         return "Complete? " + complete
                 + " SC: (" + statusCode + "/" + status
                 + ") H: " + headers
                 + " B: " + body;
     }
 
 }

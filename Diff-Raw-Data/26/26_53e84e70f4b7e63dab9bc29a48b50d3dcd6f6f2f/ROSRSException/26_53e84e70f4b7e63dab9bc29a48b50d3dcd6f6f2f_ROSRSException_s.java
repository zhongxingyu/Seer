 package org.purl.wf4ever.rosrs.client.exception;
 
 /**
  * Exception that indicates an unexpected response from the ROSR Service.
  * 
  * @author piotrekhol
  * 
  */
 public class ROSRSException extends Exception {
 
     /** id. */
     private static final long serialVersionUID = -2891988826340254599L;
 
     /** response status. */
     private final int status;
 
     /** response reason. */
     private final String reason;
 
 
     /**
      * Constructor.
      * 
      * @param message
      *            Context message
      * @param status
      *            Response status
      * @param reason
      *            Response reason
      */
     public ROSRSException(String message, int status, String reason) {
         super(String.format("%s (%d %s)", message, status, reason));
         this.status = status;
         this.reason = reason;
     }
 
 
     public int getStatus() {
         return status;
     }
 
 
     public String getReason() {
         return reason;
     }
 }

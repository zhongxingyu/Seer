 package org.iplantc.de.shared;
 
import org.iplantc.de.client.I18N;
 import org.iplantc.de.shared.services.ConfluenceService;
 
 /**
  * An exception that is thrown when something goes wrong while using the Confluence API.
  * 
  * @see ConfluenceService
  * @author hariolf
  * 
  */
 public class ConfluenceException extends Exception {
     private static final long serialVersionUID = -7195224545122730806L;
 
     /**
      * Parameterless constructor, mandated by GWT.
      */
     public ConfluenceException() {
     }
 
     /**
      * Creates a new ConfluenceException with an underlying cause.
      * 
      * @param cause an exception that caused this exception
      */
     public ConfluenceException(Throwable cause) {
         // don't include comment id etc to keep the exception message user-friendly
        super(I18N.ERROR.confluenceError(), cause);
     }
 }

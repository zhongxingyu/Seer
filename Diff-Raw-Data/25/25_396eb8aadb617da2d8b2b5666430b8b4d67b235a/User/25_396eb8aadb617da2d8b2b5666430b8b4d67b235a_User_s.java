 package pl.psnc.dl.wf4ever.portal.model;
 
 import java.io.Serializable;
 import java.net.URI;
 
 /**
  * RODL user.
  * 
  * @author piotrekhol
  * 
  */
 public class User implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = 3317434240117309505L;
 
     /** User RODL URI. */
     private URI uri;
 
     /** Nice username. */
     private String username;
 
 
     /**
      * Constructor.
      * 
      * @param uri
      *            User RODL URI
      * @param username
      *            Nice username
      */
     public User(URI uri, String username) {
         this.uri = uri;
         this.username = username;
     }
 
 
     /**
      * Nice username.
      * 
      * @return the username
      */
     public String getUsername() {
         return username;
     }
 
 
     /**
     * Nice username.
      * 
      * @return the userURI
      */
     public URI getURI() {
         return uri;
     }
 
 
     /**
      * Nice username or default.
      * 
      * @param defaultValue
      *            the value to use if username is null
      * @return the username
      */
     public String getUsername(String defaultValue) {
         if (username != null) {
             return username;
         }
         return defaultValue;
     }
 
 }

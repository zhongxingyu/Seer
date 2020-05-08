 
 package models.data;
 
import java.net.URL;
import java.net.MalformedURLException;

 /**
  * This clas represents a Link, as the combination of a name and a URL.
  * @author Felix Van der Jeugt
  */
 public class Link {
 
     private String name;
     private String url;
 
     /**
      * Creates a new link. This method links the given name to a URL created
      * from the given string.
      * @param name The name of the link.
      * @param url The url as a String.
     * @throws MalformedURLException If the given url is malformed.
      */
     public Link(String name, String url) {
         this.name = name;
         this.url  = url;
     }
 
     /**
      * Returns the name of this link.
      * @return The name of the link.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Return the URL this link points to.
      * @return The URL.
      */
     public String getUrl() {
         return url;
     }
 
 }

 package org.purl.wf4ever.rosrs.client;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 import org.purl.wf4ever.rosrs.client.users.UserManagementService;
 
 /**
  * Base of all resources.
  * 
  * @author piotrekhol
  * 
  */
 public class Thing implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = -6086301275622387040L;
 
     /** Date format: Tuesday 14:03. */
     public static final SimpleDateFormat SDF1 = new SimpleDateFormat("EEEE HH:mm");
 
     /** Date format: 24 05 2012 23:04. */
     public static final SimpleDateFormat SDF2 = new SimpleDateFormat("dd MMMM yyyy HH:mm");
 
     /** resource URI. */
     protected final URI uri;
 
     /** last segment of URI or full URI if no path exists. */
     protected final String name;
 
     /** creator URI. */
     protected URI creator;
 
     /** creation date. */
     protected DateTime created;
 
     /** temporary, moved from the portal. */
     private Set<Creator> creators;
 
 
     /**
      * Constructor.
      * 
      * @param uri
      *            resource URI
      * @param created
      *            creation date
      * @param creator
      *            creator URI
      */
     public Thing(URI uri, URI creator, DateTime created) {
         this.uri = uri;
         this.creator = creator;
         this.created = created;
         this.name = calculateName();
     }
 
 
     public URI getUri() {
         return uri;
     }
 
 
     public URI getCreator() {
         return creator;
     }
 
 
     public DateTime getCreated() {
         return created;
     }
 
 
     public String getCreatedFormatted() {
         if (getCreated() != null) {
             if (getCreated().compareTo(new DateTime().minusWeeks(1)) > 0) {
                 return SDF1.format(getCreated().toDate());
             } else {
                 return SDF2.format(getCreated().toDate());
             }
 
         } else {
             return null;
         }
     }
 
 
     /**
      * Returns the last segment of the resource path, or the whole URI if has no path.
      * 
      * @return name or null
      */
     public String calculateName() {
         if (uri == null) {
             return null;
         }
        if (uri.getPath().isEmpty() || uri.getPath().equals("/")) {
             return uri.toString();
         }
         String[] segments = uri.getPath().split("/");
         String name2 = segments[segments.length - 1];
         if (uri.getPath().endsWith("/")) {
             name2 = name2.concat("/");
         }
         return name2;
     }
 
 
     public String getName() {
         return name;
     }
 
 
     public void setCreator(URI creator) {
         this.creator = creator;
     }
 
 
     public void setCreated(DateTime created) {
         this.created = created;
     }
 
 
     public Set<Creator> getCreators() {
         return creators;
     }
 
 
     public void setCreators(Set<Creator> creators) {
         this.creators = creators;
     }
 
 
     public void addCreator(UserManagementService ums, URI creator) {
         if (this.creators == null) {
             this.creators = new HashSet<>();
         }
         creators.add(new Creator(ums, creator));
     }
 
 }

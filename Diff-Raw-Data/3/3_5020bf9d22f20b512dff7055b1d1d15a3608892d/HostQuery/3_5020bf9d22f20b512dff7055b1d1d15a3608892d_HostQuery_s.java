 package sibli;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 
 import java.util.Date;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import sibli.Host;
 
 /*
   JS notation:
   host      : new db.ReferenceProperty({referenceClass: Host, required: true}),
   executed  : new db.DateTimeProperty({autoNowAdd: true}),
   status    : new db.IntegerProperty(),
   time      : new db.FloatProperty()
 */
 
 @PersistenceCapable
 public class HostQuery {
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
     /**
      * NB:
      * https://developers.google.com/appengine/docs/java/datastore/jdo/relationships
      *
      * In some cases, you may find it necessary to model an owned relationship as if it is unowned.
      * This is because all objects involved in an owned relationship are automatically placed in the same entity group,
      * and an entity group can only support one to ten writes per second. So, for example, if a parent object
      * is receiving .75 writes per second and a child object is receiving .75 writes per second,
      * it may make sense to model this relationship as unowned so that both parent and child reside in their own,
      * independent entity groups.
      */
     @Persistent
     private Host host;
 
     @Persistent
     private Date executed;
 
     @Persistent
     private String status;
 
     @Persistent
     private Float time;
 
 
     /**
      * Constructor
      */
     public HostQuery(Host host,  Date executed, String status, Float time) {
         this.host = host;
         this.executed = executed;
         this.status = status;
         this.time = time;
     }
 
 
     /**
      * Getters
      */
     public Key getKey() {
         return key;
     }
 
     public Host getHost() {
         return host;
     }
 
     public Date getExecuted() {
         return executed;
     }
 
     public String getStatus() {
         return status;
     }
 
     public Float getTime() {
         return time;
     }
 
 
     /**
      * Setters
      */
     public HostQuery setUrl(Host host) {
         this.host = host;
         return this;
     }
 
     public HostQuery setAdded(Date executed) {
         this.executed = executed;
         return this;
     }
 
     public HostQuery setStatus(String status) {
         this.status = status;
         return this;
     }
 
     public HostQuery setUpdated(Float time) {
         this.time = time;
         return this;
     }
 
 } // HostQuery class

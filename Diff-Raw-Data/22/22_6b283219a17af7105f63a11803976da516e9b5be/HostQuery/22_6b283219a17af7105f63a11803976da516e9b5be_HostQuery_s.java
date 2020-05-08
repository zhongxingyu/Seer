 package sibli;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.users.User;
 
 import java.util.Date;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
import Host;
 
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
 
     @Persistent
     private Host host;
 
     @Persistent
     private Date executed;
 
     @Persistent
     private Integer status;
 
     @Persistent
     private Float time;
 
 
     /**
      * Constructor
      */
    public Host(Host host,  Date executed, Integer status, Float time) {
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
 
     public Integer getStatus() {
         return status;
     }
 
     public Float getTime() {
         return time;
     }
 
 
     /**
      * Setters
      */
     public void setUrl(Host host) {
         this.host = host;
     }
 
     public void setAdded(Date executed) {
         this.executed = executed;
     }
 
     public void setStatus(Integer status) {
         this.status = status;
     }
 
     public void setUpdated(Float time) {
         this.time = time;
     }
 
 } // HostQuery class

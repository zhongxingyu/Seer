 package edacc.model;
 
 import java.io.Serializable;
 
 /**
  *
  * @author simon
  */
 public class StatusCode extends BaseModel implements Serializable {
     private static final long serialVersionUID = -53256782673L;
 
     public static final StatusCode LAUNCHERCRASH = new StatusCode(-5, "launcher crash");
     public static final StatusCode WATCHERCRASH = new StatusCode(-4, "watcher crash");
     public static final StatusCode SOLVERCRASH = new StatusCode(-3, "solver crash");
     public static final StatusCode VERIFIERCRASH = new StatusCode(-2, "verifier crash");
     public static final StatusCode NOT_STARTED = new StatusCode(-1, "not started");
     public static final StatusCode RUNNING = new StatusCode(0, "running");
     public static final StatusCode SUCCESSFUL = new StatusCode(1, "finished");

     // these values will be saved by the DAO automatically on initialization if they don't exist in the database
     public static final StatusCode[] CONST = {LAUNCHERCRASH, WATCHERCRASH, SOLVERCRASH, VERIFIERCRASH, NOT_STARTED, RUNNING, SUCCESSFUL};
 
     private int statusCode;
     private String description;
     public StatusCode (int statusCode, String description) {
         this.statusCode = statusCode;
         this.description = description;
     }
 
     public int getStatusCode() {
         return statusCode;
     }
 
     public String getDescription() {
         return description;
     }
 
     @Override
     public String toString() {
         return description;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final StatusCode other = (StatusCode) obj;
         if (this.statusCode != other.statusCode) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 89 * hash + this.statusCode;
         return hash;
     }
 }

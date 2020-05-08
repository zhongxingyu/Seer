 
 package net.ontopia.topicmaps.utils.sdshare.client;
 
 import java.util.Iterator;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.sql.Timestamp;
 
 import net.ontopia.utils.SDShareRuntimeException;
 
 import org.xml.sax.SAXException;
 
 /**
  * PUBLIC: Represents information about an SDshare source to
  * synchronize from. Handles error blocking, time to next check, and
  * other housekeeping, and leaves the real work to the frontend.
  */
 public class SyncSource {
   private ClientFrontendIF frontend;
   private String error; // error message; if null there is no error
   private int checkInterval; // in seconds!
   /**
    * The time (on this machine) of the last time we checked this source.
    * In milliseconds since epoch.
    */
   private long lastCheck;
   /**
    * The timestamp on the last change processed from this source, as given
    * by the server.
    */
   private Timestamp lastChange;
   private boolean active;
   /**
    * Number of fragments returned by this source since we booted.
    */
   private int fragcount;
   private SyncEndpoint endpoint;
   private static String DEF_FRONT =
     "net.ontopia.topicmaps.utils.sdshare.client.AtomFrontend";
   
   public SyncSource(String handle, int checkInterval, String frontend) {
     this.frontend = instantiate(handle, frontend == null ? DEF_FRONT : frontend);
     this.checkInterval = checkInterval;
   }
   
   public String getHandle() { // of source collection
     return frontend.getHandle();
   }
 
   public SnapshotFeed getSnapshotFeed() throws IOException, SAXException {
     return frontend.getSnapshotFeed();
   }
 
   public Iterator<FragmentFeed> getFragmentFeeds() throws IOException, SAXException {
     return frontend.getFragmentFeeds(lastChange);
   }
 
   /**
    * Returns the time we last checked for changes, as given by
    * System.currentTimeMillis.
    */
   public long getLastCheck() {
     return lastCheck;
   }
   
   /**
    * Returns the timestamp of the last change processed from this
    * source, as given in the Atom feed.
    */
   public Timestamp getLastChange() {
     return lastChange;
   }
 
   /**
    * Updates the timestamp of the last change *iff* this timestamp is
    * later than the latest one seen so far. The rationale is that we
    * are not absolutely sure what order fragments are returned in.
    */
   public void setLastChange(Timestamp lastChange) {
    if (this.lastChange == null || lastChange.after(this.lastChange))
       this.lastChange = lastChange;
   }
 
   /**
    * Called to let the source know that it is now updated.
    */
   public void updated() {
     this.lastCheck = System.currentTimeMillis();
   }
 
   /**
    * Returns true iff the current time >= time of last check + check
    * interval.
    */
   public boolean isTimeToCheck() {
     int secs_to_wait = checkInterval;
     if (isBlockedByError())
       secs_to_wait = checkInterval * 6; // FIXME: default. make configurable
     
     return System.currentTimeMillis() >= lastCheck + (secs_to_wait * 1000);
   }
 
   public boolean isBlockedByError() {
     return error != null;
   }
 
   /**
    * Returns true iff this source is being sync-ed from right now.
    */
   public boolean isActive() {
     return active;
   }
   
   public void setActive(boolean active) {
     this.active = active;
   }  
 
   public String getError() {
     return error;
   }
 
   public void setError(String error) {
     this.error = error;
   }
 
   public void clearError() {
     this.error = null;
   }
 
   public int getFragmentCount() {
     return fragcount;
   }
 
   public void addFragmentCount(int increment) {
     fragcount += increment;
   }
 
   public SyncEndpoint getEndpoint() {
     return endpoint;
   }
 
   // called by SyncEndpoint.addSource
   void setEndpoint(SyncEndpoint endpoint) {
     this.endpoint = endpoint;
   }
 
   private ClientFrontendIF instantiate(String handle, String klass) {
     try {
       Class theklass = Class.forName(klass);
       Class[] paramdefs = new Class[] { java.lang.String.class };
       Constructor construct = theklass.getConstructor(paramdefs);
       Object[] params = new Object[] { handle };
       return (ClientFrontendIF) construct.newInstance(params);
     } catch (Exception e) {
       throw new SDShareRuntimeException(e);
     }
   }
 }

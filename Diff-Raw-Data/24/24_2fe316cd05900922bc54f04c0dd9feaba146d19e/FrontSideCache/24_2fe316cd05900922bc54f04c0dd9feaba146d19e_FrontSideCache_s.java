 package org.hackystat.dailyprojectdata.frontsidecache;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hackystat.dailyprojectdata.server.Server;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.uricache.UriCache;
 
 /**
  * A cache for successfully DPD instances.  It is a "front side" cache, in the sense
  * that it faces the clients, as opposed to the caches associated with the SensorDataClient
  * instances, which are "back side" in that they cache sensor data instances.  While the
  * "back side" caches avoid calls to the lower-level sensorbase, this front-side cache 
  * avoids the overhead of DPD computation itself.
  * <p>
  * The front side cache is organized as follows.  Each DPD instance is associated with a 
  * project and a project owner. The FrontSideCache is implemented as a collection of
  * UriCaches, one for each project owner.  When a client adds data to the FrontSideCache, it
  * must supply the project owner (which is used to figure out which UriCache to use),
  * the URI of the DPD request (which is the key), and the string representation of the 
  * DPD (which is the value).  
  * <p>
  * The FrontSideCache currently has hard-coded maxLife of 1000 hours and each UriCache has
  * a capacity of 1M instances. We could set these via ServerProperties values if necessary. 
  * <p>
  * There is one important component missing from the FrontSideCache, and that is access
  * control.  The FrontSideCache does not check to see if the client checking the cache has 
  * the right to retrieve the cached data.  To perform access control, you should use 
  * the SensorDataClient.inProject(owner, project) method, which checks to see if the 
  * user associated with the SensorDataClient instance has the right to access information
  * about the project identified by the passed owner/project pair.  To see how this 
  * works, here is some example get code, which checks the cache but only returns the 
  * DPD instance if the calling user is in the project:
  * <pre>
  * String cachedDpd = this.server.getFrontSideCache().get(uriUser, uriString);
  * if (cachedDpd != null && client.inProject(authUser, project)) {
  *   return super.getStringRepresentation(cachedDpd);
  * }
  * </pre>
  *  
  * @author Philip Johnson
  *
  */
 public class FrontSideCache {
   
   /** The .hackystat subdirectory containing these cache instances. */ 
   private String subDir = "dailyprojectdata/frontsidecache";
   
   /** The number of hours that a cached DPD instance stays in the cache before being deleted. */
   private double maxLife = 1000;
 
   /** The total capacity of this cache. */
   private long capacity = 1000000L;
   
   /** Maps user names to their associated UriCache instance. */
   private Map<String, UriCache> user2cache = new HashMap<String, UriCache>();
   
   /** The server that holds this FrontSideCache. */
   private Server server = null;
   
   /** 
    * Creates a new front-side cache, which stores the DPD instances recently created.
    * There should be only one of these created for a given DPD server.  Note that 
    * this assumes that only one DPD service is running on a given file system.  
    * @param server The DPD server associated with this cache. 
    */
   public FrontSideCache(Server server) { 
     this.server = server;
   }
   
   /**
    * Adds a (user, dpd) pair to this front-side cache. 
    * The associated UriCache for this user is created if it does not already exist.
    * Does nothing if frontsidecaching is disabled. 
    * @param user The user who is the owner of the project associated with this DPD.
    * @param uri The URL naming this DPD, as a string. 
    * @param dpdRepresentation A string representing the DPD instance. 
    */
   public void put(String user, String uri, String dpdRepresentation) {
     if (isDisabled()) {
       return;
     }
     try {
       UriCache uriCache = user2cache.get(user);
       if (uriCache == null) {
         uriCache = new UriCache(user, subDir, maxLife, capacity);
         user2cache.put(user, uriCache);
       }
       uriCache.put(uri, dpdRepresentation);
     }
     catch (Exception e) {
       this.server.getLogger().warning("Error during DPD front-side cache add: " +
           StackTrace.toString(e));
     }
   }
   
   /**
    * Returns the string representation of the DPD associated with the DPD owner and the 
    * URI, or null if not in the cache. 
    * @param user The user who is the owner of the Project associated with this DPD.
   * @param uri The URI naming this DPD.  
    * @return
    */
   public String get(String user, String uri) {
     if (isDisabled()) {
       return null;
     }
     UriCache uriCache = user2cache.get(user);
     return ((uriCache == null) ? null : (String)uriCache.get(uri));
   }
   
   /**
    * Clears the cache associated with user. Instantiates one if not available so that 
    * any persistent cache that has not yet been read into memory is cleared.
    * @param user The user whose cache is to be cleared.
    */
   public void clear(String user) {
     if (isDisabled()) {
       return;
     }
     try {
       UriCache cache = user2cache.get(user);
       if (cache == null) {
         cache = new UriCache(user, subDir, maxLife, capacity);
       }
       cache.clear();
     }
     catch (Exception e) {
       this.server.getLogger().warning("Error during DPD front-side cache clear: " +
           StackTrace.toString(e));
     }
   }
   
   /**
    * Returns true if frontsidecaching is disabled.
    * @return True if disabled.
    */
   private boolean isDisabled() {
     return !this.server.getServerProperties().isFrontSideCacheEnabled();
   }
 
 }

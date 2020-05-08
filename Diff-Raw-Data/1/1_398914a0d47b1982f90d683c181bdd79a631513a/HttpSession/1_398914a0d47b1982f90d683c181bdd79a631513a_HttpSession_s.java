 package de.skuzzle.polly.sdk.http;
 
 import java.net.InetAddress;
 import java.util.Map;
 import java.util.TreeMap;
 
 import de.skuzzle.polly.sdk.model.User;
 import de.skuzzle.polly.sdk.AbstractDisposable;
 
 
 /**
  * <p>A Http session is created for each IP address that accesses the polly webinterface. 
  * Once created, it is cached for a certain time and is reused each time the same IP
  * accesses any part of the web interface.</p>
  * 
  * <p>Once a user identified to the web interface by logging in, his HttpSession instance
  * contains his user instance and the user is considered "logged in".</p>
  * 
  * <p>After a certain amount of idling, that is, no further interactions with the web 
  * interface happened, a users session will be killed automatically by polly causing him
  * to be logged off.</p>
  * 
  * <p>Sessions carry further data, such as a unique id, a timestamp of when it was 
  * created, the last called uri and a map which you can use to assign further data with
  * that session.</p>
  * 
  * @author Simon
  * @since 0.9.1
  */
 public class HttpSession extends AbstractDisposable {
     
     private String id;
     private int errorCounter;
     private long started;
     private long lastAction;
     private InetAddress remoteIp;
     private User user;
     private String lastUri;
     private Map<String, Object> data;
     private int trafficUp;
     private int trafficDown;
     
     
     
     /**
      * Creates a new HttpSession with the given id for the given remote ip.
      * 
      * @param id The id of the session.
      * @param remoteIp The remote ip .
      */
     public HttpSession(String id, InetAddress remoteIp) {
         this.id = id;
         this.remoteIp = remoteIp;
         this.started = System.currentTimeMillis();
         this.lastAction = System.currentTimeMillis();
         this.lastUri = "";
         this.data = new TreeMap<String, Object>();
     }
     
     
     
     /**
      * Copies this session for a given user. The result is an exact copy of this session,
      * except that the new sessions data (see {@link #get(String)}, 
      * {@link #putDtata(String, Object)}) will be empty.
      * 
      * @return A copy of this session.
      * @since 0.9.1
      */
     public HttpSession copy() {
         HttpSession copy = new HttpSession(this.id, this.remoteIp);
         copy.errorCounter = this.errorCounter;
         copy.started = this.started;
         copy.lastAction = this.lastAction;
         copy.user = this.user;
         copy.lastUri = this.lastUri;
         copy.trafficUp = this.trafficUp;
         return copy;
     }
     
     
     
     /**
      * Increases the error counter for this session by 1. If a session raises multiple
      * errors, the polly web service may kill it for security purposes.
      * @since 0.9.1
      */
     public void increaseErrorCounter() {
         ++this.errorCounter;
     }
     
     
     
     /**
      * Resets the error counter of this session so it will no longer be blocked.
      * @since 0.9.1
      */
     public void resetErroCounter() {
         this.errorCounter = 0;
     }
     
     
     
     /**
      * Gets the count of errors that this session caused.
      * 
      * @return The number of errors.
      */
     public int getErrorCounter() {
         return this.errorCounter;
     }
     
     
     
     /**
      * A HttpSession will automatically be blocked by polly web service if it caused too 
      * many unexpected errors. It will either be unblocked after a certain time or 
      * manually by calling {@link #resetErroCounter()}.
      * 
      * @param errorThreshold The maximum number of unexpected errors that a session may 
      *          caused before being closed for security reasons. 
      * @return Whether the ip of this session should be blocked.
      * @since 0.9.1
      */
     public boolean shouldBlock(int errorThreshold) {
         return this.errorCounter > errorThreshold;
     }
     
     
     
     /**
      * Tests whether this session is inactive for at least the amount of milliseconds 
      * passed.
      * 
      * @param timeOut Amount of milliseconds after which a session should be considered
      *          "timed out"
      * @return Whether this session was inactive for longer than the given amount of 
      *          milliseconds or exceeded the given error threshold.
      */
     public boolean isTimedOut(int timeOut) {
         return System.currentTimeMillis() - this.lastAction > timeOut;
     }
     
     
     
     /**
      * Gets the user that logged in on this session.
      * 
      * @return The user of this session or <code>null</code>, if the user did not yet 
      *          identify himself to the session by logging in.
      */
     public synchronized User getUser() {
         return this.user;
     }
     
     
     
     /**
      * Assigns a user to this sessions. If this sessions user is != <code>null</code> it
      * is considered a "logged in" session and has now access to all pages which 
      * require permissions that the user owns. 
      *  
      * @param user The user for this sessions.
      */
     public synchronized void setUser(User user) {
         this.user = user;
     }
     
     
     
     /**
      * Determines whether a user has been set for this session.
      * 
      * @return <code>true</code> if a user logged in on this sessions.
      */
     public boolean isLoggedIn() {
         return this.user != null;
     }
     
     
     
     /**
      * Gets a timestamp of the last interaction with the web interface of this session.
      * 
      * @return A timestamp of last interaction.
      */
     public synchronized long getLastAction() {
         return this.lastAction;
     }
     
     
     
     
     /**
      * Sets the time of the last interaction with the web interface. This is done 
      * automatically by polly each time a web requests happens using the ip that this 
      * session was assigned with.
      * 
      * @param lastAction Timestamp of last interaction.
      */
     public synchronized void setLastAction(long lastAction) {
         this.lastAction = lastAction;
     }
     
     
     
     /**
      * Gets the last uri that this sessions accessed.
      * 
      * @return The uri.
      */
     public synchronized String getLastUri() {
         return this.lastUri;
     }
     
     
     
     /**
      * Sets the last uri that this session accessed. This is done 
      * automatically by polly each time a web requests happens using the ip that this 
      * session was assigned with.
      * 
      * @param lastUri The uri.
      */
     public synchronized void setLastUri(String lastUri) {
         this.lastUri = lastUri;
     }
     
     
     
     /**
      * Gets the unique id of this session.
      * 
      * @return The id.
      */
     public String getId() {
         return this.id;
     }
     
     
     
     /**
      * Gets a timestamp of when this session was created.
      * 
      * @return A timestamp.
      */
     public long getStarted() {
         return this.started;
     }
     
     
     
     /**
      * Gets the ip address to which this session is assigend.
      * 
      * @return The ip address.
      */
     public InetAddress getRemoteIp() {
         return this.remoteIp;
     }
     
     
     
     /**
      * <p>This method allows you to store additional data of any type with this 
      * session.</p>
      * 
      * <p>If an entry with the same key exists in this session, it will be overridden with
      * the new value.</p>
      * 
      * <p>Note that all these object will be lost when a session is killed (e.g. a user
      * logs off or the session timed out).</p>
      * 
      * @param key The key of the object to store.
      * @param o The data to store with that key.
      */
     public void putDtata(String key, Object o) {
         this.data.put(key, o);
     }
     
     
     
     /**
      * Gets additional data stored with that session via 
      * {@link #putDtata(String, Object)}. If the key you specify does not exist, this
      * method returns <code>null</code>.
      * 
      * @param key The key of the object to retrieve.
      * @return The object stored using that key, or <code>null</code> if that key does 
      *          not exist. 
      */
     public Object get(String key) {
         return this.data.get(key);
     }
     
     
     
     /**
      * Removes additional data that was stored via {@link #putDtata(String, Object)}. 
      * If the specified key does not exist, nothing happens.
      * 
      * @param key The key to remove.
      */
     public void removeData(String key) {
         this.data.remove(key);
     }
     
     
     
     /**
      * Updates the traffic that has been sent back to the client.
      * 
      * @param len The bytes that have been sent back.
      */
     public void updateUpload(int len) {
         this.trafficUp += len;
     }
     
     
     
     /**
      * Updates the traffic that has been sent by the client.
      * 
      * @param len The bytes sent by the client.
      */
     public void updateDownload(int len) {
         this.trafficDown += len;
     }
     
     
     
     /**
      * Gets the traffic that has been received from the client.
      * 
      * @return The bytes sent by the client.
      */
     public int getTrafficDown() {
         return this.trafficDown;
     }
     
     
     
     
     /**
      * Gets the traffic that has been sent back to the client.
      * 
      * @return The uploaded traffic.
      */
     public int getTrafficUp() {
         return this.trafficUp;
     }
     
     
     
     @Override
     public String toString() {
         return "HttpSession [ip = " + this.remoteIp + ", id = " + this.id + 
             ", user = " + this.user + "]";
     }
 
 
 
     @Override
     protected void actualDispose() {
         this.data.clear();
         this.user = null;
     }
 }

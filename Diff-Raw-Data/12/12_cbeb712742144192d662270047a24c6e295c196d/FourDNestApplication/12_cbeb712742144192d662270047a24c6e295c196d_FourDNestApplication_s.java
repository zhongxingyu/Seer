 package org.fourdnest.androidclient;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.fourdnest.androidclient.comm.ProtocolFactory;
 import org.fourdnest.androidclient.comm.UnknownProtocolException;
 import org.fourdnest.androidclient.services.StreamReaderService;
 import org.fourdnest.androidclient.services.TagSuggestionService;
 import org.fourdnest.androidclient.services.TagSuggestionService.TagCache;
 
 import android.app.Application;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Manages shared resources, including the preferences and singletons like
  * services and databases.
  * The application object is accessible from any part of the program which 
  * has access to the application context, through
  * FourDNestApplication application = (FourDNestApplication) getApplication() 
  */
 public class FourDNestApplication extends Application
     implements OnSharedPreferenceChangeListener {
     private static final String TAG = FourDNestApplication.class.getSimpleName();
     private static final int NEST_ID = 1;
     
     private SharedPreferences prefs;
     private NestManager nestManager;
     
     private static final String draftEggManagerRole = "draft";
     private EggManager draftEggManager;
     private static final String streamEggManagerRole = "stream";
     private EggManager streamEggManager;
     
     private Handler handler;
 	private TagSuggestionService.TagCache tagCache;
     
     private static FourDNestApplication app;
     
     @Override
     public void onCreate() {
         super.onCreate();
         synchronized(this) {
             this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
             this.prefs.registerOnSharedPreferenceChangeListener(this);    
             // For debugging, insert default Nest and settings
             Nest nest = this.getNestManager().getNest(NEST_ID);
             if(nest == null) {
                 Nest defaultNest = this.getDefaultNest();
                 this.getNestManager().saveNest(defaultNest);
                 this.setCurrentNestId(defaultNest.getId());
 
                 this.prefs.edit()
                 .putString("nest_base_uri", defaultNest.getBaseURI().toString())
                 .putString("nest_username", defaultNest.getUserName())
                 .putString("nest_password", defaultNest.getSecretKey())
                 .commit();
             } else {
                 this.setCurrentNestId(nest.getId());
             }
 
             this.tagCache = new TagSuggestionService.TagCache();
             
             // Init handler
             this.handler = new Handler();
       }
       
       app = this;
       Log.i(TAG, "onCreated");
       //warm start TagSuggestionService
       startService(new Intent(this, StreamReaderService.class));
       startService(new Intent(this, TagSuggestionService.class));
     }
     
     public static FourDNestApplication getApplication() {
         return app;
     }
 
     /**
      * Gets the NestManager singleton.
      * The NestManager object synchronizes itself, so the caller is free to store
      * the object.  
      * @return the NestManager
      */
     public NestManager getNestManager() {
         if(this.nestManager == null) {
             this.nestManager = new NestManager(this);
         }
         return this.nestManager;
     }
     
     /**
      * Gets the EggManager singleton for draft eggs.
      * The EggManager object synchronizes itself, so the caller is free to store
      * the object.
      * @return the EggManager
      */
     public EggManager getDraftEggManager() {
         if(this.draftEggManager == null) {
             this.draftEggManager = new EggManager(this, this.draftEggManagerRole);
         }
         return this.draftEggManager;
     }
     
     /**
      * Gets the EggManager singleton for stream eggs.
      * The EggManager object synchronizes itself, so the caller is free to store
      * the object.
      * @return the EggManager
      */
     public EggManager getStreamEggManager() {
         if(this.streamEggManager == null) {
             this.streamEggManager = new EggManager(this, this.streamEggManagerRole);
         }
         return this.streamEggManager;
     }
     
     
     /**
      * Handler for OnSharedPreferenceChangeListener
      * Called when preferences change
      */
     public synchronized void onSharedPreferenceChanged(
             SharedPreferences sharedPreferences, String key) {
         this.getApplicationContext();
         
         Log.d(TAG, "pref " + key + " changed");
 
         // When a nest_ preference changes, call relevant handler
         if(key.startsWith("nest_")) {
             this.handleNestPrefChanges();
         }
         if (key.equals("stream_frequency")) {
         	Intent i = new Intent(this, StreamReaderService.class);
         	i.addCategory(StreamReaderService.STREAM_FREQ);
         	i.putExtra(StreamReaderService.NEW_FREQUENCY, this.prefs.getString("stream_frequency", ""));
         	startService(i);
         }
         if (key.equals("stream_size")) {
         	Intent i = new Intent(this, StreamReaderService.class);
         	i.addCategory(StreamReaderService.STREAM_SIZE);
         	i.putExtra(StreamReaderService.NEW_STREAM_SIZE, this.prefs.getString("stream_size", ""));
         	//startService(i);
         }
     }
     
     /**
      * Returns current active nest from nest manager
      * @return currently active Nest
      */
     public synchronized Nest getCurrentNest() {
         return this.nestManager.getNest(this.getCurrentNestId());
     }
 
     /**
      * Gets the id of the currently active Nest.
      * @return The id of the currently active Nest. 
      */
     public synchronized int getCurrentNestId() {
         return this.prefs.getInt("currentNestId", 0);
     }
     
 
     /**
      * Sets the currently active Nest. The setting is persisted between
      * restarts of the application.
      * @param newNestId Id of the new active Nest. Must be a valid Nest id.
      */
     public synchronized void setCurrentNestId(int newNestId) {
         this.prefs.edit()
             .putInt("currentNestId", newNestId)
             .commit();
     }
     
     /**
      * Called when onSharedPreferenceChanged detects nest_ -prefixed
      * preference change.
      * 
      * Creates new active nest with extracted URL, user name and password
      */
     private synchronized void handleNestPrefChanges() {
         String baseUri = this.prefs.getString("nest_base_uri", "");
         String user = this.prefs.getString("nest_username", "");
         String pass = this.prefs.getString("nest_password", "");
         
         URI newURI = null;
         URI defaultURI = null;
         try {
             newURI = new URI(baseUri);
             new URI("");
         } catch(URISyntaxException e) {
             this.handler.post(new ToastDisplay("Invalid URI setting", Toast.LENGTH_SHORT));
             newURI = defaultURI;
         }
         NestManager m = this.getNestManager();
         m.deleteAllNests();
 
         Nest n = null;
         try {
             n = new Nest(NEST_ID, "Active Nest", "Active Nest", newURI, ProtocolFactory.PROTOCOL_4DNEST, user, pass);
             m.saveNest(n);
             this.setCurrentNestId(n.getId());
         } catch(UnknownProtocolException upe) { }
     }
     
     /**
      * Returns a Nest with default dev server properties. Debug use.
      */
     private Nest getDefaultNest() {
         Nest n = null;
         try {
             n = new Nest(NEST_ID, "testNest", "testNest", new URI("http://test42.4dnest.org/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
         } catch(URISyntaxException urie) {  
         } catch(UnknownProtocolException upe) {
         }
         
         return n;
     }
     
     /**
      * Internal class for showing messages from background threads (e.g. pref changes)
      */
     private class ToastDisplay implements Runnable {
         private String message;
         private int duration;
         
         /**
          * Runnable ToastDisplayer, added to a Handler to be run at a later time or
          * in a different thread
          * @param context Target context to display Toast in
          * @param message Message content
          * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
          */
         public ToastDisplay(String message, int duration) {
             this.message = message;
             this.duration = duration;
         }
         
         /**
          * Called when Runnable is executed. displays Toast as specified when object was created
          */
         public void run() {
             Toast.makeText(app, this.message, this.duration).show();
         }
     
     }
     
     /**
      * Are all certificates valid without checking?
      * @return boolean setting value, default true
      */
     public synchronized boolean getAllowAllCerts() {
         return this.prefs.getBoolean("nest_accept_all_certs", true);
     }
     
     /**
      * Is kiosk mode enabled?
      * @return boolean setting value, default true
      */
     public synchronized boolean getKioskModeEnabled() {     
         return this.prefs.getBoolean("kiosk_mode", false);
     }
     
     /**
      * Set value of allow_all_certs setting. Used in testing.
      * @param val
      */
     public synchronized void setAllowAllCerts(Boolean val) {
         SharedPreferences.Editor prefEditor = this.prefs.edit();
         prefEditor.putBoolean("nest_accept_all_certs", val);
     }
 
	public TagCache getTagCache() {
 		return this.tagCache;
 	}
     
 
 }

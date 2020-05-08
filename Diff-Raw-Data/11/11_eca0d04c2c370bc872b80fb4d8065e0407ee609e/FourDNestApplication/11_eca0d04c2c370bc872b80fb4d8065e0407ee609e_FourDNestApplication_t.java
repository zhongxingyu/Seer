 package org.fourdnest.androidclient;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.fourdnest.androidclient.comm.ProtocolFactory;
 import org.fourdnest.androidclient.comm.UnknownProtocolException;
 import org.fourdnest.androidclient.services.SendQueueService;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
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
 	
 	private SharedPreferences prefs;
 	private NestManager nestManager;
 	private EggManager eggManager;
 	
 	@Override
 	public void onCreate() { //
 	  super.onCreate();
 	  this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	  this.prefs.registerOnSharedPreferenceChangeListener(this);
 
 	  this.setUpTestValues();
 	  Log.i(TAG, "onCreated");
 	}
 	
 	/**
 	 * Checks if Nest with ID 0 exists and creates it if not. Temporary debug-helper method.
 	 */
 	private void setUpTestValues() {
 		try {
 			NestManager m = this.getNestManager();
			m.deleteNest(0);

			Nest n = new Nest(0, "testNest", "testNest", new URI("http://test42.4dnest.org/"), ProtocolFactory.PROTOCOL_4DNEST, "testuser", "secretkey");
			m.saveNest(n);

 			
 			this.setCurrentNestId(n.getId());
 		} catch(URISyntaxException urie) {	
 		} catch(UnknownProtocolException upe) {
 		}
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
 	 * Gets the EggManager singleton.
 	 * The EggManager object synchronizes itself, so the caller is free to store
 	 * the object.
 	 * @return the EggManager
 	 */
 	public EggManager getEggManager() {
 		if(this.eggManager == null) {
 			this.eggManager = new EggManager(this);
 		}
 		return this.eggManager;		
 	}
 
 
 	public synchronized void onSharedPreferenceChanged(
 			SharedPreferences sharedPreferences, String key) {
 		this.getApplicationContext();
 		// TODO Auto-generated method stub
 	}
 	
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
 		//FIXME check that newNestId is valid?
 		SharedPreferences.Editor prefEditor = this.prefs.edit();
 		prefEditor.putInt("currentNestId", newNestId);
 	}
 
 }

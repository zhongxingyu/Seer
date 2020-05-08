 /**
  * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
  * 
  * #TODO License
  */
 package rednus.gncandroid;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Resources;
 import android.util.Log;
 
 /**
  * This is the main application class. Every class in package should have a
  * reference to this. Keeps a copy of resources. Reads preferences. Defines data
  * handler. Implements preference change listener to flag if the data file has
  * to be read again or not.
  * 
  * @author shyam.avvari
  * 
  */
 public class GNCAndroid extends Application implements
 		OnSharedPreferenceChangeListener {
 	// TAG for this activity
 	private static final String	TAG			= "GNCAndroid";
 	public static final String	SPN			= "gnc4aprefs";
 	public final boolean		localLOGV	= true;
 	// Log information boolean
 	public Resources			res;
 	public GNCDataHandler		gncDataHandler;
 	private String				dataFile	= null;
 	private boolean				gzipFile	= false;
	private boolean				reloadFile	= true;
 	/**
 	 * This method checks preferences and confirms if all information is
 	 * available to read data file.
 	 */
 	public boolean canReadData() {
 		boolean can = true;
 		if (null == dataFile ) {
 			can = false;
 		}
 		return can;
 	}
 	/**
 	 * Returns data file path
 	 */
 	public String getDataFilePath() {
 		return dataFile;
 	}
 	/**
 	 * Returns if the reload file flag is set or not
 	 */
 	public boolean isReloadFile() {
 		return reloadFile;
 	}
 	/**
 	 * Method is triggered when application starts. Gets a copy of resources
 	 * object and reads user preferences.
 	 * 
 	 * @see android.app.Application#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		// keep a copy of resources for later use in application
 		res = getResources();
 		// read preferences
 		readPreferences();
 	}
 	/**
 	 * This method listens changes to preferences. And if any of the value is is
 	 * changed, it should let application know that the data file should be read
 	 * again.
 	 * 
 	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
 	 *      onSharedPreferenceChanged(android.content.SharedPreferences,
 	 *      java.lang.String)
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
 		if (localLOGV) {
 			Log.i(TAG, "Pref " + key + "changed... reading new value...");
 		}
 		if (key.equals(res.getString(R.string.pref_data_file_key))) {
 			// get new value
 			String newPath = sp.getString(
 					res.getString(R.string.pref_data_file_key), null);
 			if (localLOGV) {
 				Log.i(TAG, "New value " + String.valueOf(newPath));
 			}
 			// change value
 			setDataFileChanged(newPath);
 		} else if (key.equals(res.getString(R.string.pref_data_file_gzip))) {
 			// get new value
 			gzipFile = sp.getBoolean(
 					res.getString(R.string.pref_data_file_gzip), false);
 			if (localLOGV) {
 				Log.i(TAG, "New value " + String.valueOf(gzipFile));
 			}
 			// Set to reload file
 			reloadFile = true;
 		} 
 	}
 	/**
 	 * Creates new gncDataHandler with values from preferences
 	 */
 	public boolean readData() {
 		if (null == dataFile)
 			return false;
 		if (localLOGV) {
 			Log.i(TAG, "Reading Data...");
 		}
 		gncDataHandler = new GNCDataHandler(this, dataFile, gzipFile);
		reloadFile = false;
 		return true;
 	}
 	/**
 	 * This method reads preferences and stores in private attributes.
 	 */
 	private void readPreferences() {
 		SharedPreferences sp = getSharedPreferences(SPN, MODE_PRIVATE);
 		// set listener to this
 		sp.registerOnSharedPreferenceChangeListener(this);
 		// read shared preferences to get data file
 		dataFile = sp.getString(res.getString(R.string.pref_data_file_key),
 				null);
 		if (localLOGV) {
 			Log.i(TAG, "Data file is " + dataFile);
 		}
 		if (dataFile == null)
 			return;
 		// read if data file is compressed
 		gzipFile = sp.getBoolean(res.getString(R.string.pref_data_file_gzip),
 				false);
 	}
 	/**
 	 * This method check old and new value of data file path and sets flag to
 	 * reload file
 	 * 
 	 * @param newPath
 	 */
 	private void setDataFileChanged(String newPath) {
 		// set file to be reloaded if changed
 		if ((null == dataFile && !newPath.equals(""))
 				|| (null != dataFile && !dataFile.equals(newPath))) {
 			reloadFile = true;
 		}
 		// copy new value
 		dataFile = newPath;
 		// add log
 		if (localLOGV) {
 			Log.i(TAG, "Reload file set to " + String.valueOf(reloadFile));
 		}
 	}
 }

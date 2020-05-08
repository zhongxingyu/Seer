 package com.shoutbreak.service;
 
 import java.util.HashMap;
 
 import com.shoutbreak.ShoutbreakUI;
 import com.shoutbreak.Vars;
 import com.shoutbreak.ui.Inbox;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.widget.ListView;
 
 public class User {
 	// All Database stuff should go through User. Any writes should be
 	// synchronized.
 
 	// STATICS ////////////////////////////////////////////////////////////////
 
 	public static float calculateRadius(int level, float density) {
 		int maxPeople = level * 5;
 		float area = maxPeople / density;
 		float radius = (float) Math.sqrt(area / Math.PI);
 		return radius;
 	}
 
 	public static void setBooleanPreference(Context context, String key, boolean val) {
 		SharedPreferences settings = context.getSharedPreferences(Vars.PREFS_NAMESPACE, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putBoolean(key, val);
 		editor.commit();
 	}
 
 	public static boolean getBooleanPreference(Context context, String key) {
 		boolean defaultReturnVal = true;
 		SharedPreferences settings = context.getSharedPreferences(Vars.PREFS_NAMESPACE, Context.MODE_PRIVATE);
 		boolean val = settings.getBoolean(key, defaultReturnVal);
 		return val;
 	}
 
 	// END STATICS ////////////////////////////////////////////////////////////
 
 	private Context _context = null;
 	private TelephonyManager _tm = null;
 	private Database _db;
 	private CellDensity _cellDensity;
 	private LocationTracker _locationTracker;
 	protected Inbox _inbox;
 	private int _shoutsJustReceived;
	private String _uid;
	private String _auth;
	private boolean _passwordExists; // no reason to put actual pw into memory
	
 	public User(Context c) {
 		_context = c;
 		_tm = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
 		_db = new Database(_context);
 		_locationTracker = new LocationTracker(_context);
 		initializeUser();
 	}
 
 	public void initializeUser() {
 		_passwordExists = false;
 		_shoutsJustReceived = 0;
 		_auth = "default"; // we don't have auth yet... just give us nonce
 		HashMap<String, String> userSettings = _db.getUserSettings();
 		if (userSettings.containsKey(Vars.KEY_USER_PW)) {
 			_passwordExists = true;
 		}
 		if (userSettings.containsKey(Vars.KEY_USER_ID)) {
 			_uid = userSettings.get(Vars.KEY_USER_ID);
 		}
 		_cellDensity = new CellDensity();
 		_cellDensity.isSet = false;
 	}
 	
 	public void setShoutsJustReceived(int i) {
 		_shoutsJustReceived = i;
 	}
 	
 	public int getShoutsJustReceived() {
 		return _shoutsJustReceived;
 	}
 	
 	public double getLatitude() {
 		return _locationTracker.getLatitude();
 	}
 
 	public double getLongitude() {
 		return _locationTracker.getLongitude();
 	}
 
 	public void initializeInbox(ShoutbreakUI ui, ListView inboxListView) {
 		_inbox = new Inbox(_context, ui, _db, inboxListView);
 	}
 
 	public Inbox getInbox() {
 		return _inbox;
 	}
 
 	public synchronized CellDensity getCellDensity() {
 
 		CellDensity oldCellDensity = _cellDensity;
 		CellDensity tempCellDensity = _locationTracker.getCurrentCell();
 		_cellDensity.cellX = tempCellDensity.cellX;
 		_cellDensity.cellY = tempCellDensity.cellY;
 
 		if (_cellDensity.isSet && _cellDensity.cellX == oldCellDensity.cellX
 				&& _cellDensity.cellY == oldCellDensity.cellY) {
 			// in same cell
 			return _cellDensity;
 		} else {
 			// check db for cached result
 			tempCellDensity = _db.getDensityAtCell(_cellDensity);
 			if (tempCellDensity.isSet) {
 				_cellDensity.density = tempCellDensity.density;
 				_cellDensity.isSet = true;
 			}
 		}
 		return _cellDensity;
 	}
 
 	public synchronized void saveDensity(float density) {
 		CellDensity tempCellDensity = _locationTracker.getCurrentCell();
 		_cellDensity.cellX = tempCellDensity.cellX;
 		_cellDensity.cellY = tempCellDensity.cellY;
 		_cellDensity.density = density;
 		_db.saveCellDensity(_cellDensity);
 		_cellDensity.isSet = true;
 	}
 
 	public String getAuth() {
 		return _auth;
 	}
 
 	public synchronized void updateAuth(String nonce) {
 		String pw = "";
 		HashMap<String, String> userSettings = _db.getUserSettings();
 		if (userSettings.containsKey(Vars.KEY_USER_PW)) {
 			pw = userSettings.get(Vars.KEY_USER_PW);
 		}
 		// $auth = sha1($uid . $pw . $nonce);
 		_auth = Hash.sha1(_uid + pw + nonce);
 	}
 
 	public boolean hasAccount() {
 		return _passwordExists;
 	}
 
 	public synchronized void setPassword(String pw) {
 		// TODO: should we encrypt or obfuscate this or something?
 		// plaintext in db safe?
 		_db.saveUserSetting(Vars.KEY_USER_PW, pw);
 		_passwordExists = true;
 	}
 
 	public synchronized void setUID(String uid) {
 		_db.saveUserSetting(Vars.KEY_USER_ID, uid);
 		_uid = uid;
 	}
 
 	public String getUID() {
 		return _uid;
 	}
 
 	public String getDeviceId() {
 		return _tm.getDeviceId();
 	}
 
 	public String getPhoneNumber() {
 		return _tm.getLine1Number();
 	}
 
 	public String getNetworkOperator() {
 		return _tm.getNetworkOperatorName();
 	}
 
 	public String getAndroidId() {
 		return Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
 	}
 
 }

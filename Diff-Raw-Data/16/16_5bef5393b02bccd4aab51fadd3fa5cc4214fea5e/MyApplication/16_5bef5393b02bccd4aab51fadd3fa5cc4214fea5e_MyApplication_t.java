 package jp.tkgktyk.wimaxhelperforaterm.my;
 
 import jp.tkgktyk.wimaxhelperforaterm.AtermHelper;
 import jp.tkgktyk.wimaxhelperforaterm.R;
 import android.app.Application;
 
 /**
  * An application class provides AtermHelper object to activities and services.
  */
 public class MyApplication extends Application {
 	
 	private AtermHelper _aterm;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		MyFunc.setContext(this);
 		_checkVersion();
 	}
 	
 	private void _checkVersion() {
 		// get last running version
 		MyVersion last = new MyVersion(MyFunc.getStringPreference(R.string.pref_key_version_name));
 		// save current version
 		MyVersion current = new MyVersion(this);
 		MyFunc.setStringPreference(R.string.pref_key_version_name, current.toString());
 		
 		// care of changing version
 		if (new MyVersion("1.1.4").isNewerThan(last)) {
 			// introduce preferences of router's SSID and versionName.
 			// SSID is saved only when Bluetooth MAC address is changed.
 			// so remove Bluetooth MAC address on preference to save SSID.
 			MyFunc.removePreference(R.string.pref_key_bt_address);
 			// change the method of checking whether the router is active.
 			// in connection with it, need to change the default value of screen_on_wait.
 			// so reset screen_on_wait preference.
 //			MyFunc.removePreference(R.string.pref_key_screen_on_wait);
 		}
 		if (new MyVersion("1.1.5").isNewerThan(last)) {
 			// debugged GT-N7000 4.1.2.
 			// change default value.
 //			MyFunc.removePreference(R.string.pref_key_screen_on_wait);
 			MyFunc.removePreference(R.string.pref_key_bt_connect_timeout);
 		}
 		if (new MyVersion("1.1.6").isNewerThan(last)) {
 			// change default value.
 			MyFunc.removePreference(R.string.pref_key_bt_connect_timeout); // 5000msec
 		}
 		if (new MyVersion("1.2").isNewerThan(last)) {
 			// change default value.
 			MyFunc.removePreference(R.string.pref_key_bt_connect_timeout); // 10000msec
 		}
 		if (new MyVersion("1.3").isNewerThan(last)) {
 			// remove a preference
 			MyFunc.removePreference(R.string.pref_key_bt_connect_timeout); // never use
 		}
 	}
 	
 	public AtermHelper getAterm() {
 		if (_aterm == null)
 			_aterm = new AtermHelper(this);
 		return _aterm;
 	}
 	
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		_aterm.stopWakeUpService();
 		_aterm = null;
 	}
 }

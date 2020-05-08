 package org.quickwifi;
 
 import android.content.ContextWrapper;
 import android.content.Intent;
 import android.provider.Settings;
 import android.util.Log;
 
 /**
  * Switch for Airplane mode.
  * 
  * <p>
  * uses-permission
  * <ul>
  * <li>android.permission.WRITE_SETTINGS</li>
  * </ul>
  * </p>
  * 
  * @author takayuki hirota
  */
 public class AirplaneModeSwitch {
 	private static final String TAG = "AirplaneModeSwitch";
 
 	private final ContextWrapper contextWrapper;
 
 	public AirplaneModeSwitch(final ContextWrapper contextWrapper) {
 		this.contextWrapper = contextWrapper;
 	}
 
 	/**
 	 * turn on airplane mode.
 	 */
 	public void enable() {
 		setMode(true);
 	}
 
 	/**
 	 * turn off airplane mode.
 	 */
 	public void disable() {
 		setMode(false);
 	}
 
 	private boolean getCurrentMode() {
 		final boolean result = Settings.System.getInt(
 				contextWrapper.getContentResolver(),
 				Settings.System.AIRPLANE_MODE_ON, 0) == 1;
 		Log.i(TAG, "airplaneMode: " + result);
 		return result;
 	}
 
 	private void setMode(final boolean after) {
 
 		final boolean before = getCurrentMode();
 		if (before == after) {
 			Log.i(TAG, "airplaneMode alreay "
					+ (before ? "enabled" : "disabled"));
 			return;
 		}
 
 		Settings.System.putInt(contextWrapper.getContentResolver(),
 				Settings.System.AIRPLANE_MODE_ON, after ? 1 : 0);
 		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 		intent.putExtra("state", after);
 		contextWrapper.sendBroadcast(intent);
 	}
 }

 package ca.idrc.tecla.framework;
 
 import android.content.Context;
 import android.provider.Settings;
 
 public class Persistence {
 
 	private static final String IME_ID = "com.android.inputmethod.latin/.LatinIME";
 
 	public static final float MAX_SCAN_DELAY = 3000;
 
 	private static Persistence sInstance;
 	
 	private boolean is_ime_running;
 	private boolean is_ime_showing;
 
 	private int mScanDelay;
 	
 	public Persistence(Context context) {
 		is_ime_running = false;
 		is_ime_showing = false;
 		mScanDelay = 1000;
 		
 		sInstance = this;
 	}
 
 	public static Persistence getInstance() {
 		return sInstance;
 	}
 	
 	public int getScanDelay() {
 		return mScanDelay;
 	}
 	
 	public void setScanDelay(int delay) {
 		mScanDelay = delay;
 	}
 	
 	public void setIMERunning(boolean is_showing) {
 		is_ime_running = is_showing;
 	}
 
 	public boolean isIMERunning() {
 		return is_ime_running;
 	}
 
 	public void setIMEShowing(boolean is_showing) {
 		is_ime_showing = is_showing;
 	}
 
 	public boolean isIMEShowing() {
 		return is_ime_showing;
 	}
 
 	public static Boolean isDefaultIME(Context context) {
 		String ime_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
 		if (ime_id.equals(IME_ID)) return true;
 		return false;
 	}
 
 }

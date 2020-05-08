 package dude.morrildl.providence;
 
 import java.util.Properties;
 
 import android.content.Context;
 import android.util.Log;
 
 public class Config {
 	private static boolean loaded = false;
 
 	public static String OAUTH_AUDIENCE = "";
 	public static String REGID_URL = "";
 	public static String VBOF_SEND_URL = "";
 	public static String PHOTO_BASE = "";
 
 	private Config() {
 	}
 
 	public static void load(Context context) {
 		if (!loaded) {
 			synchronized (Config.class) {
 				if (!loaded) {
 					Properties props = new Properties();
 					try {
 						props.load(context.getResources().openRawResource(
								R.raw.config));
 					} catch (Exception e) {
 						Log.e("Config.loadConfig", "exception during load", e);
 					}
 
 					OAUTH_AUDIENCE = props.getProperty("OAUTH_AUDIENCE");
 					REGID_URL = props.getProperty("REGID_URL");
 					VBOF_SEND_URL = props.getProperty("VBOF_SEND_URL");
 					PHOTO_BASE = props.getProperty("PHOTO_BASE");
 
 					loaded = true;
 				}
 			}
 		}
 	}
 }

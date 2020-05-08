 /**
  * 
  */
 package de.h3ndrik.openlocation.util;
 
 import java.util.zip.Deflater;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.location.LocationManager;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import de.h3ndrik.openlocation.LocationReceiver;
 import de.h3ndrik.openlocation.UpdateReceiver;
 
 /**
  * @author h3ndrik
  * 
  */
 public class Utils { // TODO: Rename class?
 
 	public static void startReceiver(Context context, Boolean sendNow) {
 		SharedPreferences SP = PreferenceManager
 				.getDefaultSharedPreferences(context);
 
 		Intent locationIntent = new Intent(context, LocationReceiver.class);
 		PendingIntent pendingLocationIntent = PendingIntent.getBroadcast(context, 0, locationIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		Intent updateIntent = new Intent(context, UpdateReceiver.class);
 		updateIntent.setAction("de.h3ndrik.openlocation.update");
 		PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		LocationManager locationManager = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 		AlarmManager alarmManager = (AlarmManager) context
 				.getSystemService(Context.ALARM_SERVICE);
 		PackageManager pm = context.getPackageManager();
 		ComponentName updateReceiver = new ComponentName(context, UpdateReceiver.class);
 		ComponentName locationReceiver = new ComponentName(context, LocationReceiver.class);
 
 		if (SP.getBoolean("activate", false)) {
 			pm.setComponentEnabledSetting(updateReceiver, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
 			pm.setComponentEnabledSetting(locationReceiver, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
			locationManager.removeUpdates(pendingLocationIntent);
 			locationManager.requestLocationUpdates(
 					LocationManager.PASSIVE_PROVIDER, 0, 0, pendingLocationIntent);
			alarmManager.cancel(pendingUpdateIntent);
 			alarmManager.setInexactRepeating(
 					AlarmManager.ELAPSED_REALTIME_WAKEUP,
 					SystemClock.elapsedRealtime() + 90000l,
 					AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingUpdateIntent);
 			// alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
 			// SystemClock.elapsedRealtime() + 15000l, 15000l, pendingIntent);
 			if (sendNow)
 				context.sendBroadcast(updateIntent);
 		} else {
 			locationManager.removeUpdates(pendingLocationIntent);
 			alarmManager.cancel(pendingUpdateIntent);
 			pm.setComponentEnabledSetting(updateReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
 			pm.setComponentEnabledSetting(locationReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
 		}
 	}
 
 	public static void stopReceiver(Context context) {
 
 		Intent locationIntent = new Intent(context, LocationReceiver.class);
 		PendingIntent pendingLocationIntent = PendingIntent.getBroadcast(context, 0, locationIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		LocationManager locationManager = (LocationManager) context
 				.getSystemService(Context.LOCATION_SERVICE);
 		locationManager.removeUpdates(pendingLocationIntent);
 		Intent updateIntent = new Intent(context, UpdateReceiver.class);
 		updateIntent.setAction("de.h3ndrik.openlocation.update");
 		PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, 0, updateIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		PackageManager pm = context.getPackageManager();
 		ComponentName updateReceiver = new ComponentName(context, UpdateReceiver.class);
 		ComponentName locationReceiver = new ComponentName(context, LocationReceiver.class);
 		AlarmManager alarmManager = (AlarmManager) context
 				.getSystemService(Context.ALARM_SERVICE);
 		alarmManager.cancel(pendingUpdateIntent);
 		pm.setComponentEnabledSetting(updateReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
 		pm.setComponentEnabledSetting(locationReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
 	}
 	
 	public static byte[] gzdeflate(final byte[] uncompressed) {
 		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
 		deflater.setInput(uncompressed);
 		deflater.finish();
 		byte[] compressed = new byte[uncompressed.length];
 		int compressedLength = deflater.deflate(compressed);
 		deflater.end();
 		byte[] output = new byte[compressedLength];
 		System.arraycopy(compressed, 0, output, 0, compressedLength);	// trim array
 
 		return output;
 	}
 	
 	public static String getDomain(Context context) {
 		String domain = null, username = null, password = null, fullusername = null;
 		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
 		String pref_username = SP.getString("username", "");
 		String pref_password = SP.getString("password", "");
 
 		if (pref_username.length() > 0 && pref_password.length() > 0) {	// Preferences are set
 			if (pref_username.indexOf('@') > 0) {	// username enthält eine domain
 				username = pref_username.substring(0, pref_username.indexOf('@'));
 				domain = pref_username.substring(pref_username.indexOf('@') + 1);
 			}
 			else {	// username enthält keine domain
 				username = pref_username;
 				domain = "location.h3ndrik.de";
 			}
 			fullusername = username + "@" + domain;
 			password = pref_password;
 		}
 		else {
 			domain = "location.h3ndrik.de";
 		}
 		return domain;
 	}
 	
 	public static String getUsername(Context context) {
 		String domain = null, username = null, password = null, fullusername = null;
 		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
 		String pref_username = SP.getString("username", "");
 		String pref_password = SP.getString("password", "");
 
 		if (pref_username.length() > 0 && pref_password.length() > 0) {	// Preferences are set
 			if (pref_username.indexOf('@') > 0) {	// username enthält eine domain
 				username = pref_username.substring(0, pref_username.indexOf('@'));
 				domain = pref_username.substring(pref_username.indexOf('@') + 1);
 			}
 			else {	// username enthält keine domain
 				username = pref_username;
 				domain = "location.h3ndrik.de";
 			}
 			fullusername = username + "@" + domain;
 			password = pref_password;
 		}
 		else {
 			domain = "location.h3ndrik.de";
 		}
 		return username;
 	}
 	
 	public static String getPassword(Context context) {
 		String domain = null, username = null, password = null, fullusername = null;
 		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
 		String pref_username = SP.getString("username", "");
 		String pref_password = SP.getString("password", "");
 
 		if (pref_username.length() > 0 && pref_password.length() > 0) {	// Preferences are set
 			if (pref_username.indexOf('@') > 0) {	// username enthält eine domain
 				username = pref_username.substring(0, pref_username.indexOf('@'));
 				domain = pref_username.substring(pref_username.indexOf('@') + 1);
 			}
 			else {	// username enthält keine domain
 				username = pref_username;
 				domain = "location.h3ndrik.de";
 			}
 			fullusername = username + "@" + domain;
 			password = pref_password;
 		}
 		else {
 			domain = "location.h3ndrik.de";
 		}
 		return password;
 	}
 	
 	public static String getFullUsername(Context context) {
 		String domain = null, username = null, password = null, fullusername = null;
 		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
 		String pref_username = SP.getString("username", "");
 		String pref_password = SP.getString("password", "");
 
 		if (pref_username.length() > 0 && pref_password.length() > 0) {	// Preferences are set
 			if (pref_username.indexOf('@') > 0) {	// username enthält eine domain
 				username = pref_username.substring(0, pref_username.indexOf('@'));
 				domain = pref_username.substring(pref_username.indexOf('@') + 1);
 			}
 			else {	// username enthält keine domain
 				username = pref_username;
 				domain = "location.h3ndrik.de";
 			}
 			fullusername = username + "@" + domain;
 			password = pref_password;
 		}
 		else {
 			domain = "location.h3ndrik.de";
 		}
 		return fullusername;
 	}
 
 }

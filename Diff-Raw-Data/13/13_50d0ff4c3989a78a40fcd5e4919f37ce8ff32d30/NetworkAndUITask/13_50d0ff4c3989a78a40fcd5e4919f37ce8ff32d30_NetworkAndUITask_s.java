 package com.blklb.mpdhd.tasks;
 
 import java.net.UnknownHostException;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.blklb.mpdhd.tools.MPDHDInfo;
 import com.blklb.mpdhd.tools.TimerHelper;
 import com.blklb.mpdhd.tools.UIUtilities;
 
 public class NetworkAndUITask extends TimerTask {
 
 	Activity _activity;
 	String tag = "Network & UI Task";
 
 	public NetworkAndUITask(Activity a) {
 		_activity = a;
 		loadPrefrences();
 	}
 
 	@Override
 	public void run() {		 
 
 		if (hasConnectionInfo() && hasNetworkAccess() && canHitHost()) {
 			try {
 				
 				
 				// TODO:Find a fix for changing server names once connected to a server
 				//force a prefrence reload load only when there has been a change.
 				//Figure out preference change listener
 				//if(prefrencesHaveChanged)
 				//	 loadPrefrences();
 				//	 reestablishConnection();
 				
 				
 				switch (MPDHDInfo.currentTab) {
 				case NowPlaying:
 					UIUtilities.updateNowPlayingUI(_activity);
 					break;
 				case Queue:
 					UIUtilities.updateQueueUI(_activity);
 					break;
 				case Database:
 					UIUtilities.updateDatabaseUI(_activity);
 					break;
 				case Search:
 					UIUtilities.updateSearchUI(_activity);
 					break;
 
 				case Playlists:
 					UIUtilities.updatePlaylistUI(_activity);
 					break;
				}
 			} catch (NullPointerException e) {
 				//TODO: Fix the not playing bug 
 				Log.w(tag, "Can't hit the server or it is not playing");
 			}
			// Run task again in half a second to update the UI again
			TimerHelper.getInstance().scheduleTask(
					new NetworkAndUITask(_activity), 300);
 		} else {
 			// Run task again in 5 seconds to give time for a connection
 			// to be reestablished
 			TimerHelper.getInstance().scheduleTask(
 					new NetworkAndUITask(_activity), 5000);
 		}
 	}
 
 	// /////////////////////////////////////////////////////////////////////////
 	// ////////////////////////UTILITY METHODS//////////////////////////////////
 	// /////////////////////////////////////////////////////////////////////////
 	
 	
 	private void loadPrefrences() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(_activity);
 		MPDHDInfo.hostname = prefs.getString("hostname", "");
 		MPDHDInfo.password = prefs.getString("password", "");
 		MPDHDInfo.port = prefs.getString("port", "6600");
 	}
 
 	/**
 	 * 
 	 * @return true only if network access is available
 	 */
 	private boolean hasNetworkAccess() {
 
 		final ConnectivityManager connMgr = (ConnectivityManager) _activity
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		final android.net.NetworkInfo wifi = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 		final android.net.NetworkInfo mobile = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (wifi.isAvailable() || mobile.isAvailable())
 			return true;
 
 		Log.e(tag, "Problem on network access");
 		return false;
 	}
 
 	/**
 	 * 
 	 * @return true if there is a valid hostname
 	 */
 	private boolean hasConnectionInfo() {
 		String hostname = MPDHDInfo.hostname;
 		if ((hostname.length() > 0))
 			return true;
 		Log.e(tag, "Problem on connection info");
 		return false;
 	}
 
 	private boolean canHitHost() {
 		try {
 			java.net.InetAddress.getByName(MPDHDInfo.hostname);
 		} catch (UnknownHostException e) {
 			Log.e(tag, "Problem on host hit");
 			return false;
 		}
 		return true;
 	}
 }

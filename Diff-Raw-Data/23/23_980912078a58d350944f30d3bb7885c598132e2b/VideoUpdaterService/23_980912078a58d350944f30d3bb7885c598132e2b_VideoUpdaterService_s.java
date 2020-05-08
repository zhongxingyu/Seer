 package com.anderspersson.xbmcwidget.recentvideo;
 
 import java.util.Date;
 
 import com.anderspersson.xbmcwidget.common.ITimerCallback;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public abstract class VideoUpdaterService implements ITimerCallback {
 	     
 	public enum UpdateResult {
 		Failed,
 		Updated,
 		NoChange
 	}
 
     protected Context _ctx;
 
 	protected abstract Intent createRecentVideoRefreshIntent(UpdateResult updateResult);
     protected abstract UpdateResult tryRefreshData();
     
     public VideoUpdaterService(Context ctx) {
     	this._ctx = ctx;
     }
     
 	public void performUpdate() {
 		if(isConnectedToWifi() == false) {
 			_ctx.startService(createRecentVideoRefreshIntent(UpdateResult.Failed));
 			return;
 		}
 		
 		UpdateResult result = tryRefreshData();
 
 		if(result == UpdateResult.Failed) {
 			_ctx.startService(createRecentVideoRefreshIntent(result));
 			return;
 		}
 		
 		setLastUpdateTime();
 		
 		_ctx.startService(createRecentVideoRefreshIntent(result));
 	}
 
 
 	private void setLastUpdateTime() {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
 		Editor editor = prefs.edit();
		editor.putString("recent_video_last_refresh", new Date().getTime() + "");
 		editor.commit();
 	}
 
 	private boolean isConnectedToWifi() {
 		
 		try {
 			if(android.os.Debug.isDebuggerConnected()) {
 				return true;
 			}
 		
 			ConnectivityManager connManager = (ConnectivityManager) _ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return wifi != null && wifi.isAvailable() && wifi.isConnected();
 		}
 		catch(Exception ex) {
 			Log.w(
 				this.getClass().getSimpleName(), 
 				"Unable to determine WIFI state - widget cannot be refreshed.");
 			return false;
 		}
 	}
 }

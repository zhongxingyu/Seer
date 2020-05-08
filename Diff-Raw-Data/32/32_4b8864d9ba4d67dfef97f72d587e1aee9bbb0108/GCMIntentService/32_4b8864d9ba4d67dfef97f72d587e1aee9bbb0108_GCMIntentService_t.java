 package edu.gatech.oftentimes2000;
 
import java.util.Set;

 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 import edu.gatech.oftentimes2000.data.Announcement;
 import edu.gatech.oftentimes2000.gcm.GCMManager;
 import edu.gatech.oftentimes2000.gcm.NotificationCenter;
 import edu.gatech.oftentimes2000.server.ContentManager;
 
 public class GCMIntentService extends GCMBaseIntentService
 {
 	private final String TAG = "GCMIntentService";
 	
 	/**
 	 * Constructor
 	 */
 	public GCMIntentService()
 	{
 		super(GCMManager.SENDER_ID);
 	}
 	
 	@Override
 	public void onError(Context context, String errorId) 
 	{
 		Log.e(TAG, "GCM Error: " + errorId);
 	}
 
 	@Override
 	public void onMessage(Context context, Intent intent) 
 	{
 		String dataRaw = intent.getStringExtra("data");
 		Log.d(TAG, "Received GCM");
 		
		if (dataRaw == null)
			return;
		
 		try
 		{
 			JSONObject json = new JSONObject(dataRaw);
 			
			ContentManager cm = ContentManager.getInstance();
			Announcement announcement = cm.parseAnnouncement(json);
 			
 			// Sent notification
 			Context appContext = context.getApplicationContext();
			Intent notiIntent = new Intent(appContext, AnnouncementDetail.class);
			notiIntent.putExtra("announcement", announcement);
 			
		    PendingIntent pIntent = PendingIntent.getActivity(appContext, 0, notiIntent, 0);
 		    NotificationCenter.createNotification(appContext, pIntent, "You get new announcements!");
 		}
 		catch (JSONException e) 
 		{
 			Log.e(TAG, e.getMessage());
 		}
 	}
 
 	@Override
 	public void onRegistered(Context context, String registrationId) 
 	{
 		GCMManager.registerDevice(this, registrationId);
 	}
 
 	@Override
 	public void onUnregistered(Context context, String registrationId) 
 	{
 		GCMManager.unregisterDevice(this, registrationId);
 	}
 
 }

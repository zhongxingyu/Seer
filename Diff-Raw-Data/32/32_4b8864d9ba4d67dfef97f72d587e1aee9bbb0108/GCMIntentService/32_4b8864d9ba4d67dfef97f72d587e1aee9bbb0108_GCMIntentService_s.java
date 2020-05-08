 package edu.gatech.oftentimes2000;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
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
 		
 		try
 		{
 			JSONObject json = new JSONObject(dataRaw);
			ContentManager cm = ContentManager.getInstance();
 			
			JSONArray data = json.getJSONArray("data");
			Announcement[] announcements = new Announcement[data.length()];
			for (int i = 0; i < data.length(); i++)
			{
				// Parse data
				JSONObject datum = data.getJSONObject(i);
				Announcement announcement = cm.parseAnnouncement(datum);
				
				// Store it in the announcement array
				announcements[i] = announcement;
			}
 			
 			// Sent notification
 			Context appContext = context.getApplicationContext();
			Intent notiIntent = new Intent(appContext, AnnouncementSelection.class);
			notiIntent.putExtra("announcements", announcements);
 			
		    PendingIntent pIntent = PendingIntent.getActivity(appContext, 0, intent, 0);
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

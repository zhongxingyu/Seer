 package com.connectsy.events.comments;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.preference.PreferenceManager;
 
 import com.connectsy.data.DataManager.DataUpdateListener;
 import com.connectsy.events.EventList;
 import com.connectsy.events.EventManager;
 import com.connectsy.events.EventManager.Event;
 import com.connectsy.notifications.NotificationHandlerBase;
 
 public class CommentNotification extends NotificationHandlerBase implements DataUpdateListener {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "CommentNotification";
 	private static final int GET_EVENT = 0;
 	protected String tickerText = "New Connectsy Comment";
 	
 	@Override
 	public void send(Context context) throws JSONException {
 		if (!PreferenceManager.getDefaultSharedPreferences(context)
 				.getBoolean("preference_notifications_comment", true))
 			return;
 		else
 			super.send(context);
 	}
 
 	@Override
 	protected void prepareNotification() throws JSONException {
 		String title;
 		String body;
 		Intent i;
 		Event event = null;
 		
 		boolean oneEvent = true;
 		String rev = notifications.get(0).getString("event_revision");
 		for (JSONObject notice: notifications)
 			if (!rev.equals(notice.getString("event_revision")))
 				oneEvent = false;
 		if (oneEvent){
 			EventManager eventManager = new EventManager(context, this, null,
 					null);
 			event = eventManager.getEvent(rev);
 			if (event == null) {
 				eventManager.refreshEvent(rev, GET_EVENT);
 				return;
 			}
 			i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.dir/vnd.connectsy.event.comment");
 			i.putExtra("com.connectsy.events.revision", event.revision);
 			
 			if (notifications.size() == 1) {
 				String commenter = notifications.get(0).getString("commenter");
 				title = commenter+" posted a comment";
 				body = notifications.get(0).getString("comment");
 			} else {
 				title = notifications.size()+" comments";
 				body = "";
 			}
 		}else{
 			title = notifications.size()+" comments";
 			body = "";
 			i = new Intent(context, EventList.class);
 		}
 		sendNotification(title, body, i, "comment");
 	}
 	
 	public void onDataUpdate(int code, String response) {
 		if (code == GET_EVENT) {
 			try {
 				prepareNotification();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onRemoteError(int httpStatus, String response, int code) {}
 }

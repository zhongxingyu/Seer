 package com.example.friendzyapp;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 
 import com.example.friendzyapp.HttpRequests.PostMsgRequest;
 import com.example.friendzyapp.HttpRequests.MeetupLocation;
 import com.example.friendzyapp.HttpRequests.newLocation;
 import com.example.friendzyapp.HttpRequests.ServerMeetupLocation;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ChatScreenActivity extends Activity {
     private static final String YOU = "You";
 
     private static final int DEFAULT_ZOOM = 16;
 
     private static final String TAG = "chat";
 
     public Global globals;
 
     public AlarmManager alarmManager;
     public PendingIntent pendingIntent;
     public static final long PING_TIME = 15 * 1000; // in ms
 
     public ArrayList<ArrayList<String>> chatList;
     public ListView chatListView;
     public ChatListAdapter chatListAdapter;
 
     public String userId;
     public String friendId;
     public String userName;
     public String friendName;
     public List<MeetupLocation> suggested;
     private OverlayItem currentLocation;
     
     public MapView mapView;
     public MapController mapController;
     final ArrayList<OverlayItem> mapItems = new ArrayList<OverlayItem>();
     
     Drawable personMarker;
     Drawable selectedFlagMarker;
     Drawable nonSelectedFlagMarker;
 
 	@SuppressWarnings("unchecked")
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat_screen);
 
         globals = (Global) getApplicationContext();
         Log.d(TAG, "setting up chat alarm manager");
 
         Bundle extras = getIntent().getExtras();
 
         if (extras.getBoolean("FROM_ALARM")) {
             Log.d(TAG, "No, this was recalled from Alarm!   Going to force close this.");
             finish(); // does this work?
             return;
         }
         
         alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
         Intent i = new Intent(this, ChatAlarmReceiver.class);
         pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
         
         userId = extras.getString("USER_ID");
         friendId = extras.getString("FRIEND_ID");
         
         userName = YOU;
         friendName = globals.getDisplayName(friendId);
         
         setTitle("Chat with " + friendName);
 
         chatList = new ArrayList<ArrayList<String>>();
 
         chatListView = (ListView) findViewById(R.id.chat_list);
         chatListAdapter = new ChatListAdapter(this, R.id.chat_list, chatList);
         chatListView.setAdapter(chatListAdapter);
 
         if (!globals.chatAlarmOn) {
             Log.d(TAG, "Creating an alarm now");
 
             globals.chatAlarmOn = true;
             alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                     SystemClock.elapsedRealtime(), PING_TIME /* in ms */,
                     pendingIntent);
         }
         globals.chatOpen = true;
 
         // TODO: What does this do?
         doPostMsg("", new ServerMeetupLocation()); // do this once to double check everything is good.
         
         personMarker = getResources().getDrawable(R.drawable.map_marker);
         selectedFlagMarker = getResources().getDrawable(R.drawable.map_flag);
         nonSelectedFlagMarker = getResources().getDrawable(R.drawable.map_flag_nonselected);
 
         GeoPoint userLocation = extras.getParcelable("USER_LOCATION");
         OverlayItem userOverlay = new OverlayItem(YOU, "You are here!", userLocation);
         // So I'm pretty sure this sets the marker hotspot to the default marker's hotspot, which will obviously be incorrect for
         // the new marker we are installing. It's better than nothing, though. 
         userOverlay.setMarkerHotspot(userOverlay.getMarkerHotspot());
         userOverlay.setMarker(personMarker);
         mapItems.add(userOverlay);
         
         GeoPoint friendLocation = extras.getParcelable("FRIEND_LOCATION");
         OverlayItem friendOverlay = new OverlayItem("Friend", "Your friend!", friendLocation);
         friendOverlay.setMarkerHotspot(friendOverlay.getMarkerHotspot());
         friendOverlay.setMarker(personMarker);
         mapItems.add(friendOverlay);
         
         suggested = (List<MeetupLocation>) extras.getSerializable("SUGGESTED_MEETUPS");
         
         for (MeetupLocation m : suggested) {
             Log.d(TAG, "processing location name=" + m.meetingName);
             
             OverlayItem meetingOverlay = new OverlayItem(
                     m.meetingName, "Meet at " + m.meetingName + "?", m.meetingLocation);
             
             meetingOverlay.setMarkerHotspot(meetingOverlay.getMarkerHotspot());
             meetingOverlay.setMarker(nonSelectedFlagMarker);
             mapItems.add(meetingOverlay);
             
         }
         
         // TODO: What does this mean?
 //        currentLoc = 2; //TODO ??? first in list?
         currentLocation = mapItems.get(2);
         
         
         mapView = (MapView) findViewById(R.id.map);
         mapView.setBuiltInZoomControls(true);
         mapView.setMultiTouchControls(true);
         mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
         
         mapController = mapView.getController();
         mapController.setZoom(DEFAULT_ZOOM);
         mapController.setCenter(suggested.get(0).meetingLocation);
 
         ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(mapItems,
                 new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                     @Override
                     public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "####################################");
                     	Log.d(TAG, "Clicked on icon!  index is:" + index +"__"+item.mTitle);
                     	if (index >= 2) { // first two indexes are you and friend
                     		
                     		// we clicked on a meetuplocation; now we ask if the user wants to change their location to here!
                     		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     switch (which) {
                                     case DialogInterface.BUTTON_POSITIVE:
                                     	
                                     	/*doPostMsg("", new MeetupLocation(
                                     			item.mTitle, item.mGeoPoint));*/
                                     	
                                    	newLocation loc = new newLocation("" + item.mGeoPoint.getLatitudeE6(),
                                    			"" + item.mGeoPoint.getLongitudeE6());
                                     	doPostMsg("", new ServerMeetupLocation(item.mTitle,
                                     			loc));
                                     	
                                     	
                                     	currentLocation = item;
                                     	Log.d(TAG, "Setting currentLoc to " + currentLocation.mTitle);
                                     	updateFlagGraphic();
                                     	// TODO perhaps if this is already selected, don't have the dialog??????
                                         
                                         Toast.makeText(ChatScreenActivity.this, "Changed meetup location to: " + item.mTitle, Toast.LENGTH_SHORT).show();
                                         break;
                                     case DialogInterface.BUTTON_NEGATIVE:
                                         break;
                                     }
                                 }
                             };
                             new AlertDialog.Builder(ChatScreenActivity.this).setMessage("Set meetup point to be " + item.mTitle + "?")
                                 .setPositiveButton(R.string.yes, listener)
                                 .setNegativeButton(R.string.no, listener)
                                 .show();
                     		
                     		
                     		
                     	} else {
                     		Toast.makeText(ChatScreenActivity.this, 
                                 item.mDescription, Toast.LENGTH_LONG).show();
                     	}
                         return true;
                     }
                     @Override
                     public boolean onItemLongPress(final int index, final OverlayItem item) {
                         return true;
                     }
                 }, new DefaultResourceProxyImpl(getApplicationContext()));
         
 
     	mapView.getOverlays().add(currentLocationOverlay);
     	updateFlagGraphic();
     	// What is going on here?
 //        Toast.makeText(this, "Meet at " + suggestedNamesArr[currentLoc - 2] + "?", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.chat_screen, menu);
         return true;
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         Log.d(TAG, "onNewIntent was called! chatAlarmOn:" + globals.chatAlarmOn);
 
         // so now we ping server!!! 
         
         doPostMsg("", new ServerMeetupLocation());  
         // if new, then we don't update the loc on the server 
 
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         
         // We may have been summoned from the middle of nowhere. It happens.
         if (globals == null) {
             Log.d(TAG, "Recreating application context");
             globals = (Global) getApplicationContext();
         }
         
         Log.d(TAG, "onDestroy called!  So alarm is now turned off");
         // cancel polling
         if (globals.chatAlarmOn) {
             globals.chatAlarmOn = false;
             
             if (alarmManager != null) { // Who knows if this might happen?
                 if (pendingIntent != null)
                     alarmManager.cancel(pendingIntent);
                 else
                     Log.d(TAG, "pendingIntent was null");
             } else {
                 Log.d(TAG, "alarmManager was null");
             }
         }
         globals.chatOpen = false;
     }
 
     /*
      * Called from the button press
      */
     public void onPostMsg(View view) {
         EditText messageView = (EditText) findViewById(R.id.text_to_post);
         String m = messageView.getText().toString();
         messageView.setText(""); // TODO check if this works
         
         if (TextUtils.isEmpty(m)) {
             messageView.setError(getString(R.string.error_empty_field));
             messageView.requestFocus();
             return;
         }
 
         addMsgToList(userName, m);
 
         Log.d(TAG, "trying to send '" + m + "' to server");
         doPostMsg(m, new ServerMeetupLocation());
     }
 
     private void addMsgToList(String author, String msg) {
         ArrayList<String> clm = new ArrayList<String>(); // 5 steps to add a new
                                                          // msg to list
         clm.add(author);
         clm.add(msg);
         chatList.add(clm);
         chatListAdapter.notifyDataSetChanged();
         Log.d(TAG, "adding new message to list: "+msg);
         
     }
     
     private void updateFlagGraphic() {
         Log.d(TAG, "updating flag graphic");
     	for (int i = 0; i < mapItems.size(); i++) {
     		
     		if (mapItems.get(i).equals(currentLocation)) {
     			
     			mapItems.get(i).setMarker(selectedFlagMarker);
     		} else if (i >= 2) {
     			mapItems.get(i).setMarker(nonSelectedFlagMarker);
     			
     		} else {
     			// it is a person, do nothing!
     		}
     	}
     	
     	// actually make it redraw now.
     	mapView.invalidate();
     	
     }
     
 
     /*
      * If msg = "", then it just requests an update. If not "", then it will
      * send the msg to the server!
      */
     private void doPostMsg(String msg, ServerMeetupLocation loc) {
         Log.d(TAG, "setting up ChatAsyncTask to send chat info for msg: '"
                 + msg + "'");
 
        String params = globals.gson.toJson(new PostMsgRequest(userId,
                 friendId, msg, loc));//
 
         ChatAsyncTask post = new ChatAsyncTask();
         post.execute(params);
     }
 
     private class ChatAsyncTask extends InternetConnectionAsyncTask {
         private static final String resource = "chat";
 
         public ChatAsyncTask() {
             super(resource);
             Log.d(TAG, "init ChatAsyncTask");
         }
 
         protected void onPostExecute(final String respString) {
             if (respString == null) {
                 Log.e(TAG, "reader is null, did download fail?");
                 return;
             }
 
             Log.d(TAG, "ChatAsyncTask: onPostExecute: serverResponse:"
                     + respString);
 
             parseServerResponse(respString);
 
         }
     }
 
     private class ChatServerResponse {
         // public Map<String, String> data;
         public String senderId;
         public Boolean connected;
         public List<ArrayList<String>> msg;
         public MeetupLocation current_meetup_location; 
         // we can just push it into this object yay
     }
 
     private void parseServerResponse(String respString) {
 
         // GenericDataResponse response = globals.gson.fromJson(respString,
         // GenericDataResponse.class);
         try { // TODO: get backend to reliably send [[""]]
             ChatServerResponse response = globals.gson.fromJson(respString,
                     ChatServerResponse.class);
             if (!response.connected) {
                 // then close chat
             }
             Log.d(TAG, "response.msg: " + response.msg);
             Log.d(TAG, " current loc: " + response.current_meetup_location.meetingName);
             
             // TODO: FIX
             // now we try and find which one in the list matches what the server sent
 //            for (int i = 0; i < suggestedList.size(); i++) {
 //            	if (suggestedList.get(i).meetingName.equals(response.current_meetup_location.meetingName)) {
 //            		currentLoc = i + 2;
             	    // WTF?
 //            	}
 //            }
             if (!currentLocation.equals(response.current_meetup_location)) {
 	            for (int i = 0; i < mapItems.size(); i++) {
 	            	if (mapItems.get(i).equals(response.current_meetup_location)) {
 	            		Log.d(TAG, "--- Found current place!");
 	            		
 	            		currentLocation = mapItems.get(i);
 	            		
 	            		Toast.makeText(this, "Location just changed to " + currentLocation.mTitle, Toast.LENGTH_SHORT).show();
 	            	}
 	            }
             }
             
             
             
             // then update the graphics to match.
             updateFlagGraphic();
             
             
  
             if (response.msg == null || response.msg.isEmpty() || response.msg.get(0).isEmpty()) {
             	Log.d(TAG, "No updates, so we do nothing");
             } else {
             	for (ArrayList<String> ls : response.msg) {
                     if (ls.size() > 1) {
                         addMsgToList(friendName, ls.get(0));  //ls.get(1) is the timestamp!
                     }
                 }
             }
         } catch (Exception e) {
             Log.wtf(TAG,
                     "Exception caught from parsing!  Will assume no changes.  e:"
                             + e);
         }
 
     }
 
     private class ChatListAdapter extends ArrayAdapter<ArrayList<String>> {
         private static final String TAG = "ChatListAdapter";
         private ArrayList<ArrayList<String>> messages;
 
         // private static HashMap<String, String> displayNames;
 
         public ChatListAdapter(Context context, int resourceId,
                 ArrayList<ArrayList<String>> messages) {
             super(context, resourceId, messages);
             this.messages = messages;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) { // position
                                                                                 // i
             View view = convertView;
             if (view == null) {
                 LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 view = inflater.inflate(R.layout.chatitem, null);
             }
             // Reverse!!
             ArrayList<String> msg = messages.get(messages.size() - position - 1);
             if (msg.get(0) != "") {
 
                 view.setFocusable(false);
                 view.setClickable(false); // TODO: this doesn't actually disable
                                           // clicking on them, later on, fix
                                           // this!!
 
                 TextView authorView = (TextView) view.findViewById(R.id.author);
                 TextView tView = (TextView) view.findViewById(R.id.text);
 
                 if (authorView != null) {
                     authorView.setText(msg.get(0));
                 }
 
                 if (tView != null) {
                     tView.setText(msg.get(1));
                 }
             }
             return view;
         }
     }
 
 }

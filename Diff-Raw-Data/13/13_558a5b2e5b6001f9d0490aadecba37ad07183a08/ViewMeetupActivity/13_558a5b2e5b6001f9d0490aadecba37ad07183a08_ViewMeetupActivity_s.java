 package com.example.friendzyapp;
 
 import java.util.ArrayList;
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.ItemizedIconOverlay;
 import org.osmdroid.views.overlay.OverlayItem;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewMeetupActivity extends SherlockActivity {
     private static final int DEFAULT_ZOOM = 14;
 
     private static final String TAG = "ViewMeetupActivity";
 
     public Global globals;
 
     public String userId; // yours
     public String userName;
     
     public String attendee1Name;
     public String attendee2Name;
     public String[] statuses;
     public String matchAge;
 
     public MapView mapView;
     public MapController mapController;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_meetup);
 
         globals = (Global) getApplicationContext();
 
         Log.d(TAG, "onCreate started!!!");
 
         Bundle extras = getIntent().getExtras();
 
         
         userId = extras.getString("USER_ID");
         userName = "You";
         
         String[] attendees = extras.getStringArray("ATTENDEES");
         attendee1Name = globals.getDisplayName(attendees[0]);
         attendee2Name = globals.getDisplayName(attendees[1]);
         // TODO: make sure namecallback is good.
         TextView descTextView = (TextView) findViewById(R.id.desc_text);
         descTextView.setText("Meetup between " + attendee1Name + " and " + attendee2Name + ".");
         
         
         statuses = extras.getStringArray("STATUSES");
         //matchAge = extras.getString("MATCH_AGE");
         
         TextView status1View = (TextView) findViewById(R.id.status1);
         TextView status2View = (TextView) findViewById(R.id.status2);
         status1View.setText(statuses[0]);
         status2View.setText(statuses[1]);
         
         TextView matchAgeView = (TextView) findViewById(R.id.match_age);
         matchAgeView.setText(extras.getString("MATCH_AGE"));
         
         /*
          * 
          * intent.putExtra("USER_ID", userId);
             intent.putExtra("ATTENDEES", (String[]) meetup.attendees);
             intent.putExtra("STATUSES", (String[]) meetup.statuses);
             intent.putExtra("MATCH_AGE",  meetup.match_age);
             intent.putExtra("MEETING_LOCATION", new double[] { Double.parseDouble(meetup.latitude), Double.parseDouble(meetup.longitude) } );
             intent.putExtra("MEETING_NAME", meetup.location_name);
          */
 
         final Drawable personMarker = getResources().getDrawable(R.drawable.map_marker);
         final Drawable flagMarker = getResources().getDrawable(R.drawable.map_flag);
 
         final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
 
 //        int[] userLocationArr = extras.getIntArray("USER_LOCATION");
         // Get User's location by ourselves, big boys that we are
         
         GeoPoint userLocation = new GeoPoint(globals.getLocation());
         
         OverlayItem userOverlay = new OverlayItem("You", "You are here!", userLocation);
         
         // So I'm pretty sure this sets the marker hotspot to the default
         // marker's hotspot, which will obviously be incorrect for
         // the new marker we are installing. It's better than nothing, though.
         userOverlay.setMarkerHotspot(userOverlay.getMarkerHotspot());
         userOverlay.setMarker(personMarker);
         items.add(userOverlay);
 
         // Need to add multiple people
         /*
         int[] friendLocationArr = extras.getIntArray("FRIEND_LOCATION");
         GeoPoint friendLocation = new GeoPoint(friendLocationArr[0], friendLocationArr[1]);
         OverlayItem friendOverlay = new OverlayItem("Friend", "Your friend!", friendLocation);
         friendOverlay.setMarkerHotspot(friendOverlay.getMarkerHotspot());
         friendOverlay.setMarker(personMarker);
         items.add(friendOverlay);
         */
 
         double[] meetingLocationArr = extras.getDoubleArray("MEETING_LOCATION");
         Log.wtf(TAG, "***" + meetingLocationArr.toString());
         GeoPoint meetingLocation = new GeoPoint(meetingLocationArr[0], meetingLocationArr[1]);
         Log.wtf(TAG, meetingLocation.toDoubleString());
         // This should be properly string formatted
        OverlayItem meetingOverlay = new OverlayItem(extras.getString("MEETING_NAME"), "Meet at "
                + extras.getString("MEETING_NAME") + "?", meetingLocation);
         meetingOverlay.setMarkerHotspot(meetingOverlay.getMarkerHotspot());
         meetingOverlay.setMarker(flagMarker);
         items.add(meetingOverlay);
 
         mapView = (MapView) findViewById(R.id.map);
         // mapView.setBuiltInZoomControls(true);
         mapView.setMultiTouchControls(true);
         mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
 
         mapController = mapView.getController();
         mapController.setZoom(DEFAULT_ZOOM);
         mapController.setCenter(meetingLocation);
 
         ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                 new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                     @Override
                     public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                         Toast.makeText(ViewMeetupActivity.this, item.mDescription, Toast.LENGTH_LONG).show();
                         return true;
                     }
 
                     @Override
                     public boolean onItemLongPress(final int index, final OverlayItem item) {
                         return true;
                     }
                 }, new DefaultResourceProxyImpl(getApplicationContext()));
         mapView.getOverlays().add(currentLocationOverlay);
 
        Toast.makeText(this, "Meet at " + extras.getString("MEETING_NAME") + "?", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.chat_screen, menu);
         return true;
     }
 
 }

 /**
  * 
  */
 package com.ssasha.parking;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author smagee
  *
  */
 public class RetrieveActivity extends MapActivity {
 
	private static String TAG="com.ssasha.parking.RetrieveActivity";
 	
 	protected int appLat, appLng;
 	protected long time;
 	protected String address;
 	protected GeoPoint myPoint;
 	protected MapView mapView;
 	protected MapController mc;
 	protected TextView textview;
 	protected PrefsEditor prefs;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.retrieve);
         // get info from prefs     	
         readPrefs();
         //initialize mapview
         mapView = (MapView)findViewById(R.id.mapview);
         //show zoom in/out buttons
         mapView.setBuiltInZoomControls(true);
         //Standard view of the map(map/sat)
         mapView.setSatellite(false);
         //get controller of the map for zooming in/out
         mc = mapView.getController();
         // Zoom Level
         mc.setZoom(18); 
         
         //textview 
         textview = (TextView)findViewById(R.id.address_display);
         textview.setText(address);
         
         TextView timeview = (TextView)findViewById(R.id.time_display);
         timeview.setText("Car needs to be moved by " + new SimpleDateFormat().format(time).toString());
 
         //Get the current location in start-up
         myPoint = new GeoPoint(appLat,appLng);
         
         LocOverlay myLocationOverlay = new LocOverlay(myPoint);
         List<Overlay> list = mapView.getOverlays();
         list.add(myLocationOverlay);
         
         //show location
         mc.animateTo(myPoint);
     }
     
     //read prefs and get relevant values
     private void readPrefs() {
 		if (prefs == null) {
 			prefs = new PrefsEditor();		
 		}
 		prefs.read(this);
 		appLat = prefs.lat;
 		appLng = prefs.lng;
 		time = prefs.time;
 		address = prefs.address;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
     //button click handler
     public void onButtonClick(View view) {
     	switch (view.getId()) {
     	case R.id.clear_btn :
     		if (prefs == null)
     			prefs = new PrefsEditor();
     		prefs.clearPrefs(this);
     		//clear alarm
     		Intent intent = new Intent(this, ReminderReceiver.class);
     		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
     		PendingIntent pi = PendingIntent.getBroadcast(this, 1, intent, 0);
     		am.cancel(pi);
         	Toast.makeText(getApplicationContext(), "Car Reminder Cleared", Toast.LENGTH_SHORT).show();
     		break;
     	}
     }
 	
 }

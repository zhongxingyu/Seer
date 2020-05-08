 package edu.dartmouth.cs.tractable;
 
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 public class DisplaySessionActivity extends Activity {
 	
 	private LocationManager mLocationManager;
 	
 	private Context mContext;
 	
 	// Map elements:
 	public GoogleMap mMap;
 	public LatLng location;
 	public double latitude;
 	public double longitude;
 	public Marker mMarker;
 
 	public static final String MINUTES_FORMAT = "%d minutes";
 	public static final String SECONDS_FORMAT = "%d seconds";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.display_session);
 		
 		mContext = this;
 		
 		latitude = getIntent().getExtras().getDouble(Globals.KEY_LATITUDE);
 		longitude = getIntent().getExtras().getDouble(Globals.KEY_LONGITUDE);
 		location = new LatLng(latitude, longitude);
 		
 		// get the google map
 		FragmentManager fm = getFragmentManager();
 		MapFragment myMapFragment = (MapFragment) fm.findFragmentById(R.id.map);
 		mMap = myMapFragment.getMap();
 		mMap.setMyLocationEnabled(false);
         mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
         
         // zoom in to the bathroom location
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,
                 Globals.DEFAULT_MAP_ZOOM_LEVEL));
         
         // put the marker at the location
         mMarker = mMap.addMarker(new MarkerOptions().position(location));
         
         
         mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
 			
 			Intent i = getIntent();
 			
 			@Override
 			public View getInfoContents(Marker arg0) {
 				View v = getLayoutInflater().inflate(R.layout.info_window, null);
 				
 				TextView tv = (TextView) v.findViewById(R.id.textBuilding);
 				tv.setText("Building: " + i.getStringExtra(Globals.KEY_BUILDING));
 				
				tv = (TextView) v.findViewById(R.id.textFloor);
				tv.setText("Floor: " + i.getIntExtra(Globals.KEY_FLOOR, -1));
				
 				tv = (TextView) v.findViewById(R.id.textBathroomQuality);
 				tv.setText("Bathroom Quality: " +
 							i.getDoubleExtra(Globals.KEY_BATHROOMQUALITY, -1) +
 							" out of " + Globals.MAX_BATHROOM_QUALITY);
 				
 				tv = (TextView) v.findViewById(R.id.textExperienceQuality);
 				tv.setText("Experience Quality: " + 
 							i.getIntExtra(Globals.KEY_EXPERIENCEQUALITY, -1) + 
 							" out of " + Globals.MAX_EXPERIENCE_QUALITY);
 				
 				tv = (TextView) v.findViewById(R.id.textComment);
 				tv.setText("Comment: " + 
 						i.getStringExtra(Globals.KEY_COMMENT));
 				
 				tv = (TextView) v.findViewById(R.id.textDuration);
 				tv.setText("Duration: " + 
 						parseDuration(i.getIntExtra(Globals.KEY_DURATION, -1)));
 				
 				return v;
 			}
 
 			@Override
 			public View getInfoWindow(Marker arg0) {
 				// use the generic window
 				return null;
 			}
 		});
         
         mMarker.showInfoWindow();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_session, menu);
 		return true;
 	}
 	
 
 	// Convert duration in seconds to minutes.
 	private String parseDuration(int durationInSeconds) {
 		return durationInSeconds > 60 ? String.format(MINUTES_FORMAT,
 				durationInSeconds / 60) : String.format(SECONDS_FORMAT,
 						durationInSeconds);
 
 	}
 	
 }

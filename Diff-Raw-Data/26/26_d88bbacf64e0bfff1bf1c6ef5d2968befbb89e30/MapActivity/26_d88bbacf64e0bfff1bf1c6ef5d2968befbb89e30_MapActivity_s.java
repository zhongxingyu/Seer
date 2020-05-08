 package com.example.getconnected.activities;
 
 import com.example.getconnected.R;
 import com.example.getconnected.R.id;
 import com.example.getconnected.R.layout;
 import com.example.getconnected.R.menu;
 import com.example.getconnected.R.string;
 import com.example.getconnected.gps.GPSLocator;
 
 
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 
 import android.app.Activity;
 import android.os.Bundle;
public class MapActivity extends Activity {//BaseActivity {
 /*
 	private Button buttonGetLocation;
 	private TextView textLocation;
 	protected GPSLocator locator;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		
 		locator = new GPSLocator(getApplicationContext());
 		
 		buttonGetLocation = (Button) findViewById(R.id.map_button_getLocation);
 		textLocation = (TextView) findViewById(R.id.map_text_location);
 		buttonGetLocation.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				textLocation.setText("Latitude: " + locator.getLatitude() + ", Longitude: " + locator.getLongitude());
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.map, menu);
 		return true;
 	}
 */
 	private MapController mapController;
     private MapView mapView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setTileSource(TileSourceFactory.MAPNIK);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
         mapController.setZoom(15);
        GeoPoint point2 = new GeoPoint(51496994, -134733);
         mapController.setCenter(point2);
     }
     protected boolean isRouteDisplayed() {
         // TODO Auto-generated method stub
         return false;
     }
 }

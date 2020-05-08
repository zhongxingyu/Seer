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
import android.view.Menu;
public class MapActivity extends BaseActivity {
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
    protected GPSLocator locator;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
    	try{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
        initLayout(R.string.title_activity_main, true, true, true, false);
        
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setTileSource(TileSourceFactory.MAPNIK);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
         mapController.setZoom(15);
        locator = new GPSLocator(getApplicationContext());
        GeoPoint point2 = new GeoPoint(locator.getLatitude(), locator.getLongitude());
         mapController.setCenter(point2);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
     }
     protected boolean isRouteDisplayed() {
         // TODO Auto-generated method stub
         return false;
     }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
 }

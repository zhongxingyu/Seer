 package com.chalmers.schmaps;
 
 
 import java.util.List;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Dialog;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class GoogleMapActivity extends MapActivity implements View.OnClickListener {
 	
 	private static String TAG = "GoogleMapActivity";
 	
 	private Button editButton;
     private EditText lectureEdit;
 	
 	
 	private LocationManager location_manager;
 	private LocationListener location_listener;
 	private List<Overlay> mapOverlays;
 	private MapItemizedOverlay overlay;
 	private String roomToFind;
 	private MapView mapView;
 	private SearchSQL search;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map); 
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);

		mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.dot); 
		overlay = new MapItemizedOverlay(drawable, this);
	
 		assignInstances();
 		
		

 		location_manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		location_listener = new LocationListener(){
 
 			public void onLocationChanged(Location location) { //metod som hämtar din position genom att anropa onResume
 
 				/*
 				int longitude = (int) (location.getLongitude() * 1E6);
 				int latitude = (int) (location.getLatitude() * 1E6);
 
 				GeoPoint point = new GeoPoint(latitude, longitude);
 				OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
 				overlay.addOverlay(overlayitem);
 				mapOverlays.add(overlay);
 				 */
 
 			}
 
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 
 			}	
 		};
 		
 		search = new SearchSQL(GoogleMapActivity.this);
 		search.createDatabase();
 
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_map, menu);
 		return true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		location_manager.removeUpdates(location_listener);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		try {
 			// Register the listener with the Location Manager to receive
 			// location updates
 			location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, location_listener);
 			location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 100, location_listener);
 		}
 		catch (Exception e) {
 			//print("Couldn't use the GPS: " + e + ", " + e.getMessage());
 		}
 	}
 	
     private void assignInstances() {
         editButton = (Button) findViewById(R.id.edittextbutton);
         lectureEdit = (EditText) findViewById(R.id.edittextlecture);
         editButton.setOnClickListener(this);
 	}
 
 		public void onClick(View v) {
 		roomToFind = lectureEdit.getText().toString();
 		roomToFind.toLowerCase().trim(); //removes white signs and converts to lower case
 		roomToFind = roomToFind.replaceAll("[^a-zA-Z0-9]+",""); //Removes illegal characters to prevent sql injection
 		search.openRead(); //öppnar databasen för läsafrån den
 		 if(search.exists(roomToFind)){
 			 GeoPoint gp = new GeoPoint(search.getLat(roomToFind),search.getLong(roomToFind)); //create a geopoint
 
 			 MapController mapcon = mapView.getController();
 			 mapcon.animateTo(gp);
 		     mapcon.setZoom(18); //zoom level
 			 OverlayItem over = new OverlayItem(gp, search.getAddress(roomToFind), search.getLevel(roomToFind)); //address and level is shown in the dialog
 			 
 			 search.close();
 			 overlay.removeOverlay();
 			 overlay.addOverlay(over);
 			 mapOverlays.add(overlay);
 			 mapView.postInvalidate();
 			 }
 		}
 }

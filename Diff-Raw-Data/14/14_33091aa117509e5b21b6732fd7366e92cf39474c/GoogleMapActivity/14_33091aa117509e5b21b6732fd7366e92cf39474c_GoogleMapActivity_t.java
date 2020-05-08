 package com.chalmers.schmaps;
 
 import java.util.HashMap;
 import java.util.List;
 
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class GoogleMapActivity extends MapActivity implements View.OnClickListener {
 	
 	private Button editButton;
     private EditText lectureEdit;
 	
 	
 	private LocationManager location_manager;
 	private LocationListener location_listener;
 	private List<Overlay> mapOverlays;
 	private MapItemizedOverlay overlay;
 	private String roomToFind;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_map); //en xml får skapas med det namnet, ändra namnet annars
 		MapView mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setSatellite(true);
 
 		mapOverlays = mapView.getOverlays();
 		Drawable drawable = this.getResources().getDrawable(R.drawable.ic_launcher); 
 		overlay = new MapItemizedOverlay(drawable, this);
 
 		SearchSQL search = new SearchSQL(GoogleMapActivity.this);
 		search.openWrite(); //öppnar databasen för att skriva i den, denna kodsnutt ska inte vara här sen!
 		search.createEntry();
 		search.close(); 
 		
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
 
 	// detta är simons kod, markerar bort den än så länge
  
 
     private void assignInstances() {
         //lectureHashMap = new HashMap();
         editButton = (Button) findViewById(R.id.edittextbutton);
         lectureEdit = (EditText) findViewById(R.id.edittextlecture);
         editButton.setOnClickListener(this);
         //lectureHashMap.put("Matsalen", 42);
         //showLecture = (TextView) findViewById(R.id.showLectureText);
         //showLecture.setText("Hi!");
 	}
 
 		public void onClick(View v) {
 		roomToFind = lectureEdit.getText().toString();
 		roomToFind.toLowerCase();
 		
 		SearchSQL search = new SearchSQL(GoogleMapActivity.this);
 		search.openRead(); //öppnar databasen för läsafrån den
 		GeoPoint gp = new GeoPoint(search.getLat(roomToFind),search.getLong(roomToFind)); //hämtar latitude och longitude i databasen och skapar en geopunkt 
 
 		String s1 = search.getAddress(roomToFind);
 		String s2 = search.getLevel(roomToFind);
 		search.close();
 		OverlayItem over = new OverlayItem(gp, s1, s2); //s1 och s2 visas i dialogrutan
 
 		overlay.addOverlay(over);
 		mapOverlays.add(overlay);
 		
 	}
 
 }

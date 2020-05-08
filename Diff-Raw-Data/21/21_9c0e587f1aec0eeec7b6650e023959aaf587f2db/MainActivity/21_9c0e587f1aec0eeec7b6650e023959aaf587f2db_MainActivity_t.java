 package uk.co.jarofgreen.FindCoords;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	LocationListener locationListener;
 
 	Location currentLocation;
 	Location desiredLocation;
 	
 	TextView tvCurrentLat;
 	TextView tvCurrentLng;
	TextView tvCurrentAccuracy;
 	EditText etDesiredLat;
 	EditText etDesiredLng;
 	TextView tvDistance;
 	TextView tvBearing;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		tvCurrentLat = (TextView) findViewById(R.id.currentLat);
 		tvCurrentLng = (TextView) findViewById(R.id.currentLng);
		tvCurrentAccuracy = (TextView) findViewById(R.id.currentAccuracy);
 		
 		etDesiredLat = (EditText) findViewById(R.id.desiredLat);
 		etDesiredLat.addTextChangedListener(new TextWatcher(){
 			public void afterTextChanged(Editable s) { updateDesiredLocation(); }
 			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
 			public void onTextChanged(CharSequence s, int start, int before, int count){ updateDesiredLocation(); }
 		});
 		
 		etDesiredLng = (EditText) findViewById(R.id.desiredLng);
 		etDesiredLng.addTextChangedListener(new TextWatcher(){
 			public void afterTextChanged(Editable s) { updateDesiredLocation(); }
 			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
 			public void onTextChanged(CharSequence s, int start, int before, int count){ updateDesiredLocation(); }
 		}); 
 		
 		tvDistance = (TextView) findViewById(R.id.desiredDistance);
 		tvBearing = (TextView) findViewById(R.id.desiredBearing);
 		
 		locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				currentLocation = location;
 				update();
 			}
 			
 			public void onStatusChanged(String provider, int status, Bundle extras) {}
 			
 			public void onProviderEnabled(String provider) {}
 			
 			public void onProviderDisabled(String provider) {}
 		};
 		
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 		
 		currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		update();
 	}
 
 	public void updateDesiredLocation() {
 		try { 
 			double lat = Double.parseDouble(etDesiredLat.getText().toString());
 			double lng = Double.parseDouble(etDesiredLng.getText().toString());
 			desiredLocation = new Location(LocationManager.GPS_PROVIDER);
 			desiredLocation.setLatitude(lat);
 			desiredLocation.setLongitude(lng);			
 		} catch (NumberFormatException e) {
 			desiredLocation = null;
 		}		
 		update();
 	}
 	
 	public void update() {
 		
 		if (currentLocation instanceof Location) {
 					
 			double currentLat = currentLocation.getLatitude();
 			tvCurrentLat.setText(Double.toString(currentLat));
 								
 			double currentLng = currentLocation.getLongitude();
 			tvCurrentLng.setText(Double.toString(currentLng));
 
			float currentAccuracy = currentLocation.getAccuracy();
			tvCurrentAccuracy.setText(Float.toString(currentAccuracy));
			
 			if (desiredLocation instanceof Location) {
 				
 				float distance = currentLocation.distanceTo(desiredLocation);
 				tvDistance.setText(Float.toString(distance));
 				
 				float bearing = currentLocation.bearingTo(desiredLocation) ;
 				tvBearing.setText(Float.toString(bearing));
 				
 			} else {
 				tvDistance.setText("destination not parsed");
 				tvBearing.setText("destination not parsed");
 			}
 			
 			
 		} else {
 
 			tvCurrentLat.setText("dunno");
 			tvCurrentLng.setText("dunno");
			tvCurrentAccuracy.setText("dunno");
 			tvDistance.setText("dunno");
 			tvBearing.setText("dunno");
 			
 		}
 		
 	}
 	
 }

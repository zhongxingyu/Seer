 package uw.changecapstone.tweakthetweet;
 import java.io.IOException;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.maps.GeoPoint;
 
 @SuppressLint("NewApi")
 
 public class LocationAndMapActivity extends Activity {
 	private GoogleMap mMap;
 	private LatLng latLng;
 	GeoPoint p;
	public final static String LATITUDE = "uw.changecapstone.tweakthetweet.latitude";
	public final static String LONGITUDE = "uw.changecapstone.tweakthetweet.longitude";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_location_and_map);
 		// Get the message from the intent
 		Intent intent = getIntent();
 		String location = intent.getStringExtra(MainActivity.LOCATION_TEXT);
 		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.loc_map)).getMap();
 		mMap.setMyLocationEnabled(true);
 		if (location == null || location.equals("")){
 			mMap.addMarker(new MarkerOptions()
 	        .position(new LatLng(47.6561359, -122.3042217))
 	        .title("test marker . This is where I take my bus home"));
 			mMap.setMyLocationEnabled(true);
 			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			LatLng latLng = new LatLng(getMidLat(46.6561359,48.6561359), getMidLng(-121.3042217,-123.3042217));
 			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
 		} else	if (location !=null && !location.equals("")){
 			new GeocoderTask().execute(location);
 		} 
 		mMap.setOnMapClickListener(getOnMapClickListener());
 	}
 	/*Gets the lat/long location that was touched.*/
 	private OnMapClickListener getOnMapClickListener() {
 		return new OnMapClickListener() {			
 		public void onMapClick(LatLng point) {
 			double lat = point.latitude;
 			double lng = point.longitude;
			Intent intent = new Intent();
		    intent.putExtra(LATITUDE, lat);
		    intent.putExtra(LONGITUDE, lng);
		    startActivity(intent);
 			Toast.makeText(getBaseContext(), "Location: "+lat + "," + 
 					lng, Toast.LENGTH_SHORT).show();
 		}
 		
 	};
 	}
 	/* reference: http://wptrafficanalyzer.in/blog */
 	private class GeocoderTask extends AsyncTask<String, Void, List <Address> >{
 
 		@Override
 		protected List<Address> doInBackground(String... arg0) {
 			Geocoder geocoder = new Geocoder(getBaseContext());
 			List <Address> addresses = null;
 			
 			try {
 				//Get maximum of 1 Address that matches the input text
 				addresses = geocoder.getFromLocationName(arg0[0],1);
 			} catch (IOException e){
 				e.printStackTrace();
 			}
 			return addresses;
 		}
 		@Override
 		protected void onPostExecute(List <Address> addresses){
 			if(addresses==null || addresses.size()==0){
 				Toast.makeText(getBaseContext(), "No Location Found", Toast.LENGTH_SHORT).show();
 			}
 			else /*if (addresses != null && addresses.size() > 0)*/{
 				Address address = addresses.get(0);
 				double lat = address.getLatitude();
 				double lng = address.getLongitude();
 				latLng = new LatLng(lat, lng);
 				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
 			}
 		}
 	}
 	public float getMidLat(double minLat, double maxLat){
 		return (float) ((minLat+maxLat)/2);
 	}
 	public float getMidLng(double minLng, double maxLng){
 		return (float) ((minLng+maxLng)/2);
 	}
 }

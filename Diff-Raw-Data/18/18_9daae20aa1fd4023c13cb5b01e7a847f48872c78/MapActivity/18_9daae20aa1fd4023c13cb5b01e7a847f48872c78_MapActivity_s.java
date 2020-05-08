 package com.engine9;
 
 import java.io.InputStream;
 import java.util.Vector;
 
 import com.engine9.R;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 
 public class MapActivity extends FragmentActivity {
 
 	private GoogleMap mMap;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map);
 		// Show the Up button in the action bar.
 		setupActionBar();
		setUpMap(mMap, null);
 		
 		Button dButton = (Button) findViewById(R.id.download);
 		dButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				new MapRequest().execute("https://dl.dropboxusercontent.com/u/26635718/markers.xml");
 			}
 			
 		});
 		
 		
 		
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
	private void setUpMap(GoogleMap map, LatLng center) {
        if (map == null) {
        	map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
         	if(center != null){
        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));}
         }
 	}
 	
 	public class MapRequest extends Request
 	{
 		@Override
 		public void onPostExecute(String result)
 		{
 			addMarkers(MarkerParser.main(result));
 		}
 	}
 	
 	private void addMarkers(Vector<LatLng> v){
 		for (LatLng l : v){
 			
 			mMap.addMarker(new MarkerOptions()
 			.position(l));
 		}
 	}
 }

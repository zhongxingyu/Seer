 package com.LocateMeInc.locate;
 
 import android.location.Location;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private static Context context;
 	private static CurLocation curloc;
 	private Location loc1;
 	private Location loc2;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		context = getApplicationContext();
 		curloc = new CurLocation();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void fetchLocation(View v) {
 		// Update fields
 		Location loc = curloc.getLocation();
 		if (v.getId() == R.id.loc1get) {
 			this.loc1 = loc;
 			((EditText)findViewById(R.id.loc1lat)).setText(Double.toString(loc1.getLatitude()));
 			((EditText)findViewById(R.id.loc1long)).setText(Double.toString(loc1.getLongitude()));
 			((EditText)findViewById(R.id.loc1alt)).setText(Double.toString(loc1.getAltitude()));
 		}
 		
 		else if (v.getId() == R.id.loc2get) {
 			this.loc2 = loc;
 			((EditText)findViewById(R.id.loc2lat)).setText(Double.toString(loc2.getLatitude()));
 			((EditText)findViewById(R.id.loc2long)).setText(Double.toString(loc2.getLongitude()));
 			((EditText)findViewById(R.id.loc2alt)).setText(Double.toString(loc2.getAltitude()));
 		}
 		
 		// Notify user of accuracy
 		Toast.makeText(
 			getApplicationContext(), 
 			getResources().getString(R.string.toast_accuracy) + Float.toString(loc.getAccuracy()) + "m", 
 			Toast.LENGTH_SHORT
 		).show();
 		
 		// Update distance measurement
 		if (loc1 != null && loc2 != null) {
 			// Update measurement
 			String distance = Float.toString(loc1.distanceTo(loc2));
 			((EditText)findViewById(R.id.distance)).setText(distance);
 		}
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 	
 	@Override
 	public void onDestroy() {
 		// Tell the Location managers that we don't need location updates anymore.
 		curloc.close();
 	}
 }

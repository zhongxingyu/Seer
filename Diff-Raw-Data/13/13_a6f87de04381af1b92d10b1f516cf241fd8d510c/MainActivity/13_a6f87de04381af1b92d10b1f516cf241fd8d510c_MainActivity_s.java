 package com.LocateMeInc.locate;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.location.Location;
 import android.os.Bundle;
import android.os.Parcel;
 import android.app.Activity;
 import android.content.Context;
import android.util.Log;
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
 
     @Override
     public void onPause() {
     	super.onPause();
 
     	try {
 	     	FileOutputStream filehandler = context.openFileOutput("cachedLocations.dat", Context.MODE_PRIVATE);
 	     	ObjectOutputStream filestream = new ObjectOutputStream(filehandler);
 	     	filestream.writeObject(new SLocation(this.loc1));
 	     	filestream.writeObject(new SLocation(this.loc2));
 	     	filestream.close();
 	     	
 	     	Toast.makeText(getApplicationContext(), "Saved cached locations!", Toast.LENGTH_SHORT).show();
     	}
     	
     	catch(Exception e) {
     		//e.printStackTrace();
     		Toast.makeText(getApplicationContext(), "Unable to save cached locations", Toast.LENGTH_SHORT).show();
     	}
     }
 	
     @Override
     public void onResume() {
     	super.onResume();
      
     	try {
 	     	FileInputStream filehandler = context.openFileInput("cachedLocations.dat");
 	     	ObjectInputStream filestream = new ObjectInputStream(filehandler);
 	     	this.loc1 = ((SLocation)(filestream.readObject())).get();
 	     	this.loc2 = ((SLocation)(filestream.readObject())).get();
 	     	filestream.close();
 	     	
 	     	updateTexts(true, this.loc1);
 	     	updateTexts(false, this.loc2);
 	     	
 	     	Toast.makeText(getApplicationContext(), "Cached locations loaded!", Toast.LENGTH_SHORT).show();
     	}
     	
     	catch(Exception e) {
     		//e.printStackTrace();
     		Toast.makeText(getApplicationContext(), "Unable to load cached locations", Toast.LENGTH_SHORT).show();
     	}
     }
     
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Tell the Location managers that we don't need location updates anymore.
 		curloc.close();
 	}
 	
 	private void updateTexts(Boolean ispos1, Location loc) {
 		((EditText)findViewById(ispos1 ? R.id.loc1lat : R.id.loc2lat)).setText(Double.toString(loc.getLatitude()));
 		((EditText)findViewById(ispos1 ? R.id.loc1long : R.id.loc2long)).setText(Double.toString(loc.getLongitude()));
 		((EditText)findViewById(ispos1 ? R.id.loc1alt : R.id.loc2alt)).setText(Double.toString(loc.getAltitude()));
 		
 		// Update distance measurement
 		if (this.loc1 != null && this.loc2 != null) {
 			// Update measurement
 			String distance = Float.toString(this.loc1.distanceTo(this.loc2));
 			((EditText)findViewById(R.id.distance)).setText(distance);
 		}
 	}
 	
 	public void fetchLocation(View v) {
 		// Update fields
 		Location loc = new Location(curloc.getLocation());
 		if (v.getId() == R.id.loc1get) {
 			this.loc1 = loc;
 		}
 		
 		else if (v.getId() == R.id.loc2get) {
 			this.loc2 = loc;
 		}
 		updateTexts(v.getId() == R.id.loc1get, loc);
 		
 		// Notify user of accuracy
 		Toast.makeText(
 			getApplicationContext(), 
 			getResources().getString(R.string.toast_accuracy) + Float.toString(loc.getAccuracy()) + "m", 
 			Toast.LENGTH_SHORT
 		).show();
 	}
 
 	public static Context getContext() {
 		return context;
 	}
 }

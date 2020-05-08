 package com.vanderbilt.people.finder;
 
 import com.vanderbilt.people.finder.Provider.Constants;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class StartupActivity extends Activity 
 {
 	private static final String TAG = "StartupActivity";
 	
 	private double latitude;
 	private double longitude;
 	
 	private TextView latLabel;
 	private TextView longLabel;
 	
 	private EditText nameEditText;
 	private EditText statusEditText;
 	
 	private Button registerButton;
 	
 	private ProgressDialog progress;
 	
 	 public void onCreate(Bundle icicle)
 	 {
 		 super.onCreate(icicle);
 		 requestWindowFeature(Window.FEATURE_LEFT_ICON);
 	     setContentView(R.layout.startup);
 	     getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);
	     this.setFinishOnTouchOutside(false);
 	     
 	     latLabel = (TextView)findViewById(R.id.lat_label);
 	     longLabel = (TextView)findViewById(R.id.long_label);
 	     nameEditText = (EditText)findViewById(R.id.s_name_edit);
 	     statusEditText = (EditText)findViewById(R.id.s_status_edit);
 	     registerButton = (Button)findViewById(R.id.register_button);
 	     
 	     // Acquire a reference to the system Location Manager
 	     LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 	     // Define a listener that responds to location updates
 	     LocationListener locationListener = new LocationListener() 
 	     {
 	         public void onLocationChanged(Location location) 
 	         {
 	        	 latitude = location.getLatitude();
 	        	 latLabel.setText(Double.toString(latitude));
 	        	 longitude = location.getLongitude();
 	        	 longLabel.setText(Double.toString(longitude));
 	         }
 
 	         public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	         public void onProviderEnabled(String provider) {}
 
 	         public void onProviderDisabled(String provider) {}
 	       };
 
 	     // Register the listener with the Location Manager to receive location updates
 	     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	     Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	     if (l != null)
	     {
	    	 latitude = l.getLatitude();
	    	 latLabel.setText(Double.toString(latitude));
	    	 longitude = l.getLongitude();
	    	 longLabel.setText(Double.toString(longitude));
	     }
 	     
 	     registerButton.setOnClickListener(new View.OnClickListener() 
 	     {
 			public void onClick(View v) 
 			{
 				DataModel d = new DataModel();
 				d.setName(nameEditText.getText().toString());
 				d.setStatus(statusEditText.getText().toString());
 				d.setLatitude(latitude);
 				d.setLongitude(longitude);
 				
 				new UploadNewUserTask().execute(d);
 			}
 		});
 	 }
 	 
 	 private class UploadNewUserTask extends AsyncTask<DataModel, Void, Long>
 	 {
 		 protected void onPreExecute()
 		 {
 			 progress = new ProgressDialog(StartupActivity.this);
 			 progress.setMessage("Registering...");
 			 progress.show();
 		 }
 		
 		protected Long doInBackground(DataModel... params) 
 		{
 			return NetworkUtilities.pushClientStatus(params[0]);
 		}
 		
 		protected void onPostExecute(Long l)
 		{
 			UserId.establishId(StartupActivity.this, l);
 			
 			ContentValues cv = new ContentValues();
 			cv.put(Constants.SERVER_KEY, UserId.getId(StartupActivity.this));
 			cv.put(Constants.NAME, nameEditText.getText().toString());
 			cv.put(Constants.LATITUDE, latitude);
 			cv.put(Constants.LONGITUDE, longitude);
 			cv.put(Constants.MESSAGE, statusEditText.getText().toString());
 			
 			Uri newUser = getContentResolver().insert(Constants.CONTENT_URI, cv);
 			Log.v(TAG, "New user stored at: " + newUser.toString());
 			
 			SharedPreferences settings = getSharedPreferences("UserData", MODE_PRIVATE);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.putBoolean("needsInitialization", false);
 			editor.commit();
 			
 			if (progress.isShowing())
 				progress.dismiss();
 			
 			// Dismiss activity
 			setResult(RESULT_OK);
 			finish();
 		}
 	 }
 }

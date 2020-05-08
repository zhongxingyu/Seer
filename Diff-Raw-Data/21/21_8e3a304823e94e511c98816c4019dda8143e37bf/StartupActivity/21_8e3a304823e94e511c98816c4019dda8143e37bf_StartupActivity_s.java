 package com.vanderbilt.people.finder;
 
 import com.vanderbilt.people.finder.Provider.Constants;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
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
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class StartupActivity extends AccountAuthenticatorActivity 
 {
 	private static final String TAG = "StartupActivity";
 	
 	private long syncFreqSeconds;
 	private double latitude;
 	private double longitude;
 	
 	private TextView latLabel;
 	private TextView longLabel;
 	
 	private EditText nameEditText;
 	private EditText statusEditText;
 	
 	private Button registerButton;
 	private RadioGroup syncGroup;
 	
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
 	     syncGroup = (RadioGroup)findViewById(R.id.sync_freq);
 	     syncGroup.check(R.id.r_auto);
 	     syncFreqSeconds = -1;
 	     
 	     syncGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
 	     {
 			public void onCheckedChanged(RadioGroup group, int checkedId) 
 			{
 				Log.v(TAG, "radio button changed");
 				switch(checkedId)
 				{
 					case R.id.r_auto:
 						Log.v(TAG, "set to -1");
 						syncFreqSeconds = -1;
 						break;
 					case R.id.r_fifteen:
 						syncFreqSeconds = 15 * 60;
 						break;
 					case R.id.r_hour:
 						syncFreqSeconds = 60 * 60;
 						break;
 				}
 			}
 		});
 	     
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
				// Set ip field in UploadNewUserTask for now,
				// may not be the best way, long-term
				DataModel d = new DataModel();
				d.setName(nameEditText.getText().toString());
				d.setStatus(statusEditText.getText().toString());
				d.setLatitude(latitude);
				d.setLongitude(longitude);
				
				new UploadNewUserTask().execute(d);
 			}
 		});
 	 }
 	 
 	 private void registerAccount()
 	 {
 		 final Account account = new Account(nameEditText.getText().toString(), AccountConstants.ACCOUNT_TYPE);
 		 AccountManager accountManager = AccountManager.get(this);
 		 accountManager.addAccountExplicitly(account, null, null);
 		 UserData.establishAccount(getApplicationContext(), account);
 		 
 		 Log.v(TAG, "The sync frequency is (-1 for auto): " + syncFreqSeconds);
 		 ContentResolver.setSyncAutomatically(account, Constants.AUTHORITY, true);
 		 if (syncFreqSeconds != -1)
 		 {
 			 ContentResolver.addPeriodicSync(account, Constants.AUTHORITY, new Bundle(), syncFreqSeconds);
 		 }
 			 
 		
 		 final Intent intent = new Intent();
 	     intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, nameEditText.getText().toString());
 	     intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountConstants.ACCOUNT_TYPE);
 	     setAccountAuthenticatorResult(intent.getExtras());
 	     setResult(RESULT_OK, intent);
 	     
 	     UserData.establishInitialization(getApplicationContext());
 	 }
 	 
 	 private class UploadNewUserTask extends AsyncTask<DataModel, Void, Long>
 	 {
 		 private String externalIp;
 		 
 		 protected void onPreExecute()
 		 {
 			 progress = new ProgressDialog(StartupActivity.this);
 			 progress.setMessage("Registering...");
 			 progress.show();
 		 }
 		
 		protected Long doInBackground(DataModel... params) 
 		{
 			DataModel d = params[0];
 			externalIp = NetworkUtilities.getMyExternalIp();
 			d.setIpAddress(externalIp);
 			return NetworkUtilities.pushClientStatus(d);
 		}
 		
 		protected void onPostExecute(Long l)
 		{
 			UserData.establishId(StartupActivity.this, l);
 			
 			ContentValues cv = new ContentValues();
 			cv.put(Constants.SERVER_KEY, UserData.getId(StartupActivity.this));
 			cv.put(Constants.NAME, nameEditText.getText().toString());
 			cv.put(Constants.LATITUDE, latitude);
 			cv.put(Constants.LONGITUDE, longitude);
 			cv.put(Constants.MESSAGE, statusEditText.getText().toString());
 			cv.put(Constants.IP, externalIp);
 			
 			Uri newUser = getContentResolver().insert(Constants.CONTENT_URI, cv);
 			Log.v(TAG, "New user stored at: " + newUser.toString());
 		
 			if (progress.isShowing())
 				progress.dismiss();
 			
 			registerAccount();
 			finish();
 		}
 	 }
 }

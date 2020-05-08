 package ca.ryerson.scs.rus;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ca.ryerson.scs.rus.adapters.HttpRequestAdapter;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.IntentRes;
 import ca.ryerson.scs.rus.util.URLResource;
 import ca.ryerson.scs.rus.util.ValidityCheck;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity implements LocationListener,
 		OnClickListener {
 	// Flag for debug/log messages
 	public static final boolean DEBUG = true;
 
 	// Tag for debugging
 	public static final String TAG = "RyeSocial";
 
 	private Context context;
 
 	private TextView btnSubmit, btnRegister;
 	private EditText evUsername, evPassword;
 
 	private LocationManager locationManager;
 	private Location locationSend;
 
 	private static final long MIN_TIME = 400;
 	private static final float MIN_DISTANCE = 1000;
 
 	private static final String JSON_STATUS = "user";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.login);
 
 		context = this;
 
 		btnSubmit = (TextView) findViewById(R.id.BtnSubmit);
 		btnRegister = (TextView) findViewById(R.id.BtnRegister);
 		evUsername = (EditText) findViewById(R.id.EVUsername);
 		evPassword = (EditText) findViewById(R.id.EVPassword);
 
 		btnSubmit.setFocusable(true);
 		btnRegister.setFocusable(true);
 		evUsername.setFocusable(true);
 		evPassword.setFocusable(true);
 
 		btnSubmit.setOnClickListener(this);
 		btnRegister.setOnClickListener(this);
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
 
 		// TODO: Implement persistence checking for saved username and password
 	}
 
 	public void onClick(View v) {
 		if (v == btnSubmit) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Login Button");
 				Log.i(TAG, evUsername.getText() + " ");
 				Log.i(TAG, evPassword.getText() + " ");
 			}
 			
 			
 			if (!ValidityCheck.usernameCheck(evUsername.getText().toString())) {
 				
 				final Dialog dialogUser = new Dialog(LoginActivity.this);
 				dialogUser.setContentView(R.layout.dialouge_email);
 				dialogUser.setTitle("Username Error");
 				dialogUser.setCancelable(true);
 
 				// set up text
 				TextView text = (TextView) dialogUser
 						.findViewById(R.id.dia_email);
 				text.setText(R.string.TVErrUsername);
 
 				// set up button
 				Button button = (Button) dialogUser.findViewById(R.id.Button01);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialogUser.dismiss();
 					}
 				});
 
 				dialogUser.show();
 			}
 			
 			else if (ValidityCheck.emptyCheck((evPassword.getText().toString()))) {
 				
 				final Dialog dialog = new Dialog(LoginActivity.this);
 				dialog.setContentView(R.layout.dialouge_email);
 				dialog.setTitle("Password Error");
 				dialog.setCancelable(true);
 
 				// set up text
 				TextView text = (TextView) dialog.findViewById(R.id.dia_email);
 				text.setText(R.string.TVErrPassword2);
 
 				// set up button
 				Button button = (Button) dialog.findViewById(R.id.Button01);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialog.dismiss();
 						evPassword.setText("");
 					}
 				});
 
 				dialog.show();
 			}
 			
 			else {
 		
 			String URLfinal = (ValidityCheck.removeWhiteSpace(URLResource.LOGIN
 					+ evUsername.getText().toString()
 					+ "&password="
 					+ evPassword.getText().toString()
 					+ "&geoX="
 					+ Double.toString(locationSend.getLatitude())
 					+ "&geoY="
 					+ Double.toString(locationSend.getLongitude())));
 					
 			HttpRequestAdapter.httpRequest(this, URLfinal,new LoginHandler());
 			}
 			
 		} else if (v == btnRegister) {
 			startActivity(new Intent(IntentRes.REGISTER_STRING));
 		}
 
 	}
 
 	private class LoginHandler implements HttpRequestAdapter.ResponseHandler {
 		@Override
 		public void postResponse(JSONObject response) {
 
 			// Log.i("RESPONSETESTETSTETESTJEIOTUEWIOTUEIOWTUTEIO",
 			// response.getString(JSON_STATUS));
 			// if (response.getString(JSON_STATUS)==""){
 			Log.i("LAT", ""+Double.toString(locationSend.getLatitude()));
 			Log.i("LONG", ""+Double.toString(locationSend.getLongitude()));
 			Intent intent = new Intent(IntentRes.MENU_STRING);
 			intent.putExtra("user", evUsername.getText().toString());
 			intent.putExtra("location", locationSend);
 			DefaultUser.setUser(evUsername.getText().toString());
 			startActivity(intent);
 
 			// Log.i("RESPONSE INFO",""+response.keys());
 			/*
 			 * Intent intent = new Intent(MENU_STRING);
 			 * intent.putExtra("username", evUsername.getText().toString());
 			 * intent.putExtra("location", locationSend); startActivity(intent);
 			 */
 			
			try {
 				if (response.getString("Status").equals("Success")) {
 					Toast.makeText(context, response.getString("Status"),
 							Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(context, "Service Currently Unavailable",
 							Toast.LENGTH_LONG).show();
 				}
 				
 				finish();
 			}
 
 			catch (JSONException e) {
 				Toast.makeText(context, "Service Currently Unavailable",
 						Toast.LENGTH_LONG).show();
			}
 			
 		}
 
 		@Override
 		public void postTimeout() {
 			Toast.makeText(context, "Connection timed out", Toast.LENGTH_LONG)
 					.show();
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		locationSend = location;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 }

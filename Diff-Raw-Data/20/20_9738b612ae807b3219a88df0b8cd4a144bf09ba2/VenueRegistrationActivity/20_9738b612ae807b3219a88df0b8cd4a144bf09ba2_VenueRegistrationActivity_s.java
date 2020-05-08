 package com.vendsy.bartsy.venue;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources.NotFoundException;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Toast;
 
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.WebServices;
 
 public class VenueRegistrationActivity extends Activity implements
 		OnClickListener {
 
 	private EditText locuId, paypal, wifiName, wifiPassword;
 	private RadioGroup typeOfAuthentication, wifiPresent;
 
 	private Handler handler = new Handler();
 	private LinearLayout wifiNameLinear, wifiTypeLinear, wifiPasswordLinear;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.venue_registration);
 
 		locuId = (EditText) findViewById(R.id.locuId);
 		paypal = (EditText) findViewById(R.id.paypalEdit);
 		wifiName = (EditText) findViewById(R.id.wifiName);
 		wifiPassword = (EditText) findViewById(R.id.wifiPassword);
 		typeOfAuthentication = (RadioGroup) findViewById(R.id.authentication);
 		wifiPresent = (RadioGroup) findViewById(R.id.wifiPresent);
 
 		wifiNameLinear = (LinearLayout) findViewById(R.id.wifiNameLinear);
 		wifiTypeLinear = (LinearLayout) findViewById(R.id.wifiTypeLinear);
 		wifiPasswordLinear = (LinearLayout) findViewById(R.id.wifiPasswordLinear);
 
 		// Setup a listener for the submit button
 		findViewById(R.id.button_venue_registration_submit).setOnClickListener(
 				this);
 
 		wifiPresent.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				// TODO Auto-generated method stub
 
 				RadioButton selectedButton = (RadioButton) findViewById(checkedId);
 				String name = selectedButton.getText().toString();
 				if (name.equalsIgnoreCase("No")) {
 					wifiNameLinear.setVisibility(View.GONE);
 					wifiTypeLinear.setVisibility(View.GONE);
 					wifiPasswordLinear.setVisibility(View.GONE);
 				} else
 
 				{
 					wifiNameLinear.setVisibility(View.VISIBLE);
 					wifiTypeLinear.setVisibility(View.VISIBLE);
 					wifiPasswordLinear.setVisibility(View.VISIBLE);
 				}
 
 			}
 		});
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// Intent intent = new Intent(this, MainActivity.class);
 
 		// Perform registration - for now assume all will go well
 		registrationAction();
 		Log.d("Bartsy", "Clicked on submit button");
 	}
 
 	public void registrationAction() {
 
 		int selectedWifiPresent = wifiPresent.getCheckedRadioButtonId();
 
 		// Gets a reference to our "selected" radio button
 		RadioButton wifi = (RadioButton) findViewById(selectedWifiPresent);
 
 		int selectedTypeOfAuthentication = typeOfAuthentication
 				.getCheckedRadioButtonId();
 
 		// Gets a reference to our "selected" radio button
 		RadioButton typeOfAuthentication = (RadioButton) findViewById(selectedTypeOfAuthentication);
 		SharedPreferences settings = getSharedPreferences(
 				GCMIntentService.REG_ID, 0);
 		String deviceToken = settings.getString("RegId", "");
 
 		System.out.println("sumbit");
 		final JSONObject postData = new JSONObject();
 		try {
 			postData.put("locuId", locuId.getText().toString());
 			postData.put("deviceToken", deviceToken);
 			postData.put("wifiName", wifiName.getText().toString());
 			postData.put("wifiPassword", wifiPassword.getText().toString());
 			postData.put("typeOfAuthentication",
 					typeOfAuthentication == null ? "" : typeOfAuthentication
 							.getText().toString());
 			postData.put("paypalId", paypal.getText().toString());
 			postData.put("deviceType", "0");
 
 			if (wifi == null ? false : wifi.getText().toString()
 					.equalsIgnoreCase("Yes"))
 				postData.put("wifiPresent", "1");
 			else
 				postData.put("wifiPresent", "0");
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		new Thread() {
 			@Override
 			public void run() {
 
 				try {
 					String response = WebServices.postRequest(
 							Constants.URL_SAVE_VENUEDETAILS, postData,
 							VenueRegistrationActivity.this);
 
 					Log.d("Bartsy", "response :: " + response);
 
 					if (response != null) {
 						final JSONObject json = new JSONObject(response);
 
 						handler.post(new Runnable() {
 
 							@Override
 							public void run() {
 								try {
 									int errorCode = Integer.parseInt(json
 											.getString("errorCode"));
 									String errorMessage = json
 											.getString("errorMessage");
 									String venueName = null, venueId = null;
 									Toast.makeText(getApplicationContext(),
 											errorMessage, Toast.LENGTH_LONG)
 											.show();
 									BartsyApplication app;
 									switch (errorCode) {
									case 2:
 										// venue already exists - still save the
 										// profile locally for now
										venueName = "Chaya Venice";
										venueId = "5a0999dda39f9fe07a44";
 									case 0:
 										// Save the venue id in shared
 										// preferences
 										venueId = venueId == null ? json
 												.getString("venueId") : venueId;
 										venueName = venueName == null ? json
 												.getString("venueName")
 												: venueName;
 										SharedPreferences sharedPref = getSharedPreferences(
 												getResources()
 														.getString(
 																R.string.config_shared_preferences_name),
 												Context.MODE_PRIVATE);
 										SharedPreferences.Editor editor = sharedPref
 												.edit();
 										editor.putString("RegisteredVenueId",
 												venueId);
 										editor.putString("RegisteredVenueName",
 												venueName);
 										app = (BartsyApplication) getApplication();
 										app.venueProfileID = venueId;
 										app.venueProfileName = venueName;
 
 										editor.commit();
 
 										Intent intent = new Intent(
 												VenueRegistrationActivity.this,
 												MainActivity.class);
 										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 										startActivity(intent);
 										finish();
 									}
 								} catch (NumberFormatException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								} catch (NotFoundException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								} catch (JSONException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 
 							}
 						});
 
 					}
 
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		}.start();
 	}
 }

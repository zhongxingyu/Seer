 package com.turbo_extreme_sloth.ezzence.activities;
 
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.turbo_extreme_sloth.ezzence.exceptions.UncaughtExceptionHandler;
 import com.turbo_extreme_sloth.ezzence.CurrentUser;
 import com.turbo_extreme_sloth.ezzence.R;
 import com.turbo_extreme_sloth.ezzence.User;
 import com.turbo_extreme_sloth.ezzence.config.Config;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequest;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestEvent;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestListener;
 
 public class SetTemperatureActivity extends BaseActivity implements RESTRequestListener {
 	protected static final String CURRENT_USER_KEY           = "CURRENT_USER";
 	protected static final String UNLOCK_EVENT_ID            = "unlockEvent";
 	protected static final String GET_CURRENT_TEMPERATURE_ID = "getCurrentTemperature";
 	protected static final String SET_TEMPERATURE_ID         = "setTemperature";
 	
 	protected SharedPreferences userCredentials;
 	
 	protected TextView currentTemperatureTextView;
 	
 	protected EditText setTemperatureEditText;
 	
 	protected Button setTemperatureButton;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// Set the default exceptions handler
 		Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler.getUncaughtExceptionHandler(this, getResources().getString(R.string.error_unknown_exception)));
 		
 		if (!CurrentUser.isLoggedIn())
 		{
 			startActivity(new Intent(this, LoginActivity.class));
 			
 			finish();
 			
 			return;
 			
 			//CurrentUser.performLogin(this, savedInstanceState);
 		}
 		Log.e("<message>", CurrentUser.getCurrentUser().getType()+"");
 		setContentView(R.layout.activity_set_temperature);
 	
 		currentTemperatureTextView = (TextView) findViewById(R.id.currentTemperatureTextView);	
 		setTemperatureEditText = (EditText) findViewById(R.id.setTemperatureEditText);	
 		setTemperatureButton = (Button) findViewById(R.id.setTemperatureButton);
 		setTemperatureButton.setOnClickListener(setTemperatureButtonOnClickListener);
 		if(CurrentUser.getCurrentUser().getType() == User.Type.SUPER_USER.getValue()){
			setTemperatureButton.setVisibility(View.VISIBLE);
			findViewById(R.id.setTemperatureEditText).setVisibility(View.VISIBLE);
 		}
 		else if(CurrentUser.getCurrentUser().getType() == User.Type.NORMAL_USER.getValue()){
 			setTemperatureButton.setVisibility(View.GONE);
			findViewById(R.id.setTemperatureEditText).setVisibility(View.GONE);
 		}
		updateCurrentTemperature();
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return false;
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState)
 	{
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 	
 //	@Override
 //	/**
 //	 * Saves instance state
 //	 */
 //	protected void onSaveInstanceState(Bundle outState)
 //	{	
 //		super.onSaveInstanceState(outState);
 //		
 //		outState.putParcelable(CURRENT_USER_KEY, currentUser);
 //	}
 	
 	/**
 	 * 
 	 */
 	protected OnClickListener setTemperatureButtonOnClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View view)
 		{
 			setTemperature(setTemperatureEditText.getText().toString());
 			
 			// Force the keyboard to close
 			InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 
 			inputMethodManager.hideSoftInputFromWindow(setTemperatureEditText.getWindowToken(), 0);
 		}
 	};
 	
 	/**
 	 * Send a RESTful request to the API to change the temperature according to the passed temperature
 	 */
 	public void setTemperature(String temperature)
 	{
 		if (temperature == null ||
 			temperature.length() <= 0)
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 			builder.setMessage(R.string.main_empty_temperature);
 			builder.setPositiveButton(R.string.ok, null);
 			builder.show();
 			
 			return;
 		}
 		
 		// Create new RESTRequest instance and fill it with user data
 		RESTRequest restRequest = new RESTRequest(Config.REST_REQUEST_BASE_URL + Config.REST_REQUEST_SET_TEMPERATURE, SET_TEMPERATURE_ID);
 		
 		restRequest.putString("sessionID"  , CurrentUser.getCurrentUser().getSessionID());
 		restRequest.putString("temperature", temperature);
 		
 		restRequest.addEventListener(this);
 		
 		restRequest.execute();
 	}
 	
 	/**
 	 * Gets the current temperature from the RESTful API and updates the current temperature view
 	 */
 	public void updateCurrentTemperature()
 	{
 		// Create new RESTRequest instance and fill it with user data
 		RESTRequest restRequest = new RESTRequest(Config.REST_REQUEST_BASE_URL + Config.REST_REQUEST_TEMPERATURE, GET_CURRENT_TEMPERATURE_ID);
 		
 		restRequest.putString("sessionID", CurrentUser.getCurrentUser().getSessionID());
 		
 		restRequest.addEventListener(this);
 		
 		restRequest.execute();
 	}
 	
 //	/**
 //	 * @param event
 //	 */
 //	protected void handleRESTRequestUnlockEvent(RESTRequestEvent event)
 //	{
 //		String result = event.getResult();
 //		
 //		try
 //		{
 //			// Parse JSON
 //			JSONObject jsonObject = new JSONObject(result);
 //			
 //			String message   = jsonObject.getString("message");
 //			String sessionID = jsonObject.getString("sessionID");
 //			
 //			int userType = jsonObject.getInt("userType");
 //			
 //			// Message should be equal to success and sessionID should be available to be logged in successfully
 //			if (message == null ||
 //				!message.equals("success") ||
 //				sessionID == null ||
 //				sessionID.length() <= 0)
 //			{
 //				return;
 //			}
 //			
 //			currentUser.setSessionID(sessionID);
 //			currentUser.setType(userType);
 //		}
 //		catch (JSONException e) { }
 //		
 //		Intent intent = new Intent(this, UnlockActivity.class);
 //		
 //		intent.putExtra("user", currentUser);
 //		
 //		startActivity(intent);
 //		
 //		finish();
 //	}
 	
 	/**
 	 * @param event
 	 */
 	protected void handleRESTRequestGetCurrentTemperature(RESTRequestEvent event)
 	{
 		String result = event.getResult();
 		
 		try
 		{
 			// Parse JSON
 			JSONObject jsonObject = new JSONObject(result);
 			
 			String message     = jsonObject.getString("message");
 			String temperature = jsonObject.getString("temperature");
 			
 			// Message should be equal to success and temperature should be set
 			if (message == null ||
 				!message.equals("success") ||
 				temperature == null ||
 				temperature.length() <= 0)
 			{
 				return;
 			}
 			
 			// \u2103 stands for degrees Celcius. This unit is hard coded, it would've been nicer to have this as a setting.
 			currentTemperatureTextView.setText(temperature + "\u2103");
 		}
 		catch (JSONException e) { }
 	}
 	
 	/**
 	 * @param event
 	 */
 	protected void handleRESTRequestSetTemperature(RESTRequestEvent event)
 	{
 		String result = event.getResult();
 		
 		try
 		{
 			// Parse JSON
 			JSONObject jsonObject = new JSONObject(result);
 			
 			String message     = jsonObject.getString("message");
 			
 			// Check if temperature was changed succesfully
 			if (message == null ||
 				!message.equals("success"))
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(SetTemperatureActivity.this);
 
 				builder.setMessage(R.string.main_temperature_not_set);
 				builder.setPositiveButton(R.string.ok, null);
 				builder.show();
 			}
 			
 			// Success message
 			AlertDialog.Builder builder = new AlertDialog.Builder(SetTemperatureActivity.this);
 
 			builder.setMessage(R.string.main_temperature_set);
 			builder.setPositiveButton(R.string.ok, null);
 			builder.show();
 			
 			updateCurrentTemperature();
 		}
 		catch (JSONException e) { }
 	}
 
 	@Override
 	public void RESTRequestOnPreExecute(RESTRequestEvent event) { }
 
 	@Override
 	public void RESTRequestOnPostExecute(RESTRequestEvent event)
 	{
 		/*if (event.getID().equals(UNLOCK_EVENT_ID))
 		{
 			handleRESTRequestUnlockEvent(event);
 		}
 		else */if (event.getID().equals(GET_CURRENT_TEMPERATURE_ID))
 		{
 			handleRESTRequestGetCurrentTemperature(event);
 		}
 		else if (event.getID().equals(SET_TEMPERATURE_ID))
 		{
 			handleRESTRequestSetTemperature(event);
 		}
 	}
 }

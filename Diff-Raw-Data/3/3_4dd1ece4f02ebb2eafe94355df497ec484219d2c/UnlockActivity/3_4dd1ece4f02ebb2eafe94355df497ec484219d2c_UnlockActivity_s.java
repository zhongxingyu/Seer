 package com.turbo_extreme_sloth.ezzence.activities;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.turbo_extreme_sloth.ezzence.CurrentUser;
 import com.turbo_extreme_sloth.ezzence.R;
 import com.turbo_extreme_sloth.ezzence.SharedPreferencesHelper;
 import com.turbo_extreme_sloth.ezzence.User;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestEvent;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestListener;
 
 public class UnlockActivity extends BaseActivity implements RESTRequestListener
 {
 	/** The ID for recognizing a login event. */
 	protected static final String UNLOCK_EVENT_ID = "unlockEvent";
 	
 	/** User. */
 	protected User user;
 	
 	/** Elements. */
 	protected EditText unlockPinEditText;
 	
 	protected Button unlockButton;
 	
 	protected ProgressDialog progressDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		user = SharedPreferencesHelper.getUser(this);
 		
 		// If a user is not set, start the login activity
 		if (user == null ||
 			user.getPin() == null ||
 			user.getPin().length() <= 0)
 		{
 			startActivity(new Intent(this, LoginActivity.class));
 			
 			finish();
 			
 			return;
 		}
 		
 		setContentView(R.layout.activity_unlock);
 		
 		unlockPinEditText = (EditText) findViewById(R.id.unlockPinEditText);
 		
 		unlockButton = (Button) findViewById(R.id.unlockButton);
 		
 		unlockButton.setOnClickListener(unlockButtonOnClickListener);
 	}
 	
 	/**
 	 * 
 	 */
 	protected OnClickListener unlockButtonOnClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View view)
 		{
 			performUnlock(unlockPinEditText.getText().toString());
 		}
 	};
 	
 	/**
 	 * Unlock application with pin
 	 * 
 	 * @param pin
 	 */
 	protected void performUnlock(String pin)
 	{
 		// Test if pin is correct
 		if (pin.equals(user.getPin()))
 		{
 //			RESTRequest restRequest = new RESTRequest(Config.REST_REQUEST_BASE_URL + Config.REST_REQUEST_LOGIN, UNLOCK_EVENT_ID);
 //			
 //			restRequest.putString("username", user.getName());
 //			restRequest.putString("password", user.getPassword());
 //			
 //			restRequest.addEventListener(this);
 //			
 //			restRequest.execute();
 			
 			CurrentUser.setCurrentUser(user);
 			
 			startActivity(new Intent(this, MainActivity.class));
 			
 			finish();
 		}
 		else
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(UnlockActivity.this);
 
 			builder.setTitle(R.string.unlock_failed);
 			builder.setMessage(R.string.unlock_wrong_pin);
 			builder.setPositiveButton(R.string.ok, null);			
 			builder.show();
 			
 			int consecutiveFailedLoginAttempts = SharedPreferencesHelper.getConsecutiveFailedLoginAttempts(this);
 			
 			consecutiveFailedLoginAttempts++;
 			
 			// Three or more failed unlock attempts, log user out
 			if (consecutiveFailedLoginAttempts >= 3)
 			{
 				SharedPreferencesHelper.deleteConsecutiveFailedLoginAttempts(this);
 				
 				// Unset user to be able to login again
 				CurrentUser.unsetCurrentUser(this);
 
 				startActivity(new Intent(this, LoginActivity.class));
 				
 				finish();
 				
 				return;
 			}
 			
 			// Store number of consecutive failed login attempts
 			SharedPreferencesHelper.storeConsecutiveFailedLoginAttempts(this, consecutiveFailedLoginAttempts);
 		}
 	}
 
 	@Override
 	public void RESTRequestOnPreExecute(RESTRequestEvent event)
 	{
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setTitle(getResources().getString(R.string.loading));
 		progressDialog.show();
 	}
 
 	@Override
 	public void RESTRequestOnPostExecute(RESTRequestEvent event)
 	{
 		progressDialog.dismiss();
 		
 		if (UNLOCK_EVENT_ID.equals(event.getID()))
 		{
 			handleRESTRequestUnlockEvent(event);
 		}
 	}
 	
 	/**
 	 * @param event
 	 */
 	private void handleRESTRequestUnlockEvent(RESTRequestEvent event)
 	{
 		String result = event.getResult();
 		
 		try
 		{
 			// Parse JSON
 			JSONObject jsonObject = new JSONObject(result);
 			
 			String message   = jsonObject.getString("message");
 			String sessionID = jsonObject.getString("sessionID");
 			
 			int userType = jsonObject.getInt("userType");
 			
 			// Message should be equal to success and sessionID should be available to be logged in successfully
 			if (message == null ||
 				!message.equals("success") ||
 				sessionID == null ||
 				sessionID.length() <= 0)
 			{
 				return;
 			}
 			
 			user.setSessionID(sessionID);
 			user.setType(userType);
 		}
 		catch (JSONException e) { }
 		
 		// Correct login, start main activity
 		if (user.isLoggedIn())
 		{
 			SharedPreferencesHelper.storeUser(this, user);
 			
 			CurrentUser.setCurrentUser(user);
 			
 			startActivity(new Intent(this, MainActivity.class));
 			
 			finish();
 		}
 		else
 		{
 			String message = event.getMessageFromResult();
 			
 			// The server couldn't be reached, as no message is set
 			if (message == null)
 			{
 				Toast.makeText(getApplicationContext(), getResources().getString(R.string.rest_not_found), Toast.LENGTH_SHORT).show();
 			}
 			// The server did not accept the passed user credentials
 			else
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(UnlockActivity.this);
 
 				builder.setTitle(R.string.unlock_failed);
 				builder.setMessage(R.string.unlock_wrong_pin);
 				builder.setPositiveButton(R.string.ok, null);			
 				builder.show();
 			}
 		}
 	}
 }

 package com.turbo_extreme_sloth.ezzence.activities;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
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
 import com.turbo_extreme_sloth.ezzence.config.Config;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequest;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestEvent;
 import com.turbo_extreme_sloth.ezzence.rest.RESTRequestListener;
 
 public class LoginActivity extends Activity implements RESTRequestListener
 {
 	/** The ID for recognizing a login event. */
 	protected static final String LOGIN_EVENT_ID = "loginEvent";
 	
 	/** Stores the user information during the request. */
 	protected User user;
 	
 	/** Elements. */
 	protected EditText userNameEditText;
 	protected EditText passwordEditText;
 	protected EditText loginPinEditText;
 
 	protected Button loginButtonButton;
 	
 	protected ProgressDialog progressDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		User user = SharedPreferencesHelper.getUser(this);
 		
 		// If a user is present in shared preferences, start the unlock activity
 		if (user.getName() != null &&
 			user.getPassword() != null &&
 			user.getPin() != null &&
 			user.getName().length() > 0 &&
 			user.getPassword().length() > 0 &&
 			user.getPin().length() > 0)
 		{
 			startActivity(new Intent(this, UnlockActivity.class));
 			
 			finish();
 			
 			return;
 		}
 		
 		setContentView(R.layout.activity_login);
 		
 		userNameEditText = (EditText) findViewById(R.id.loginUserNameEditText);
 		passwordEditText = (EditText) findViewById(R.id.loginPasswordEditText);
 		loginPinEditText = (EditText) findViewById(R.id.loginPinEditText);
 		
 		loginButtonButton = (Button) findViewById(R.id.loginButtonButton);
 		
 		loginButtonButton.setOnClickListener(loginButtonButtonOnClickListener);
 	}
 	
 	/**
 	 * The click listener for the login button.
 	 */
 	protected OnClickListener loginButtonButtonOnClickListener = new OnClickListener()
 	{
 		@Override
 		public void onClick(View view)
 		{
 			String userName = userNameEditText.getText().toString();
 			String password = passwordEditText.getText().toString();
 			String pin      = loginPinEditText.getText().toString();
 			
 			// User name must not be empty
 			if (userName.length() <= 0)
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 				
 				builder.setMessage(R.string.login_empty_userName);
 				builder.setPositiveButton(R.string.ok, null);
 				builder.show();
 				
 				return;
 			}
 			
 			// Password must not be empty
 			if (password.length() <= 0)
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 
 				builder.setMessage(R.string.login_empty_password);
 				builder.setPositiveButton(R.string.ok, null);
 				builder.show();
 				
 				return;
 			}
 			
 			// Pin code must be at least 5 digits
 			if (pin.length() < 5)
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 
 				builder.setMessage(R.string.login_pin_too_short);
 				builder.setPositiveButton(R.string.ok, null);
 				builder.show();
 				
 				return;
 			}
 			
 			performLoginRequest(userName, password, pin);
 		}
 	};
 	
 	/**
 	 * Performs a RESTful request to the server
 	 */
 	protected void performLoginRequest(String userName, String password, String pin)
 	{
 		// Store these user credentials in the user class
 		user = new User(userName, password, (String) null, pin, 0);
 		
 		// Create new RESTRequest instance and fill it with user data
 		RESTRequest restRequest = new RESTRequest(Config.REST_REQUEST_BASE_URL + Config.REST_REQUEST_LOGIN, LOGIN_EVENT_ID);
 		
 		restRequest.putString("username", userName);
 		restRequest.putString("password", password);
 		
 		restRequest.addEventListener(this);
 		
 		// Send an asynchronous RESTful request
 		restRequest.execute();
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
 		
 		if (LOGIN_EVENT_ID.equals(event.getID()))
 		{
 			handleRESTRequestLoginEvent(event);
 		}
 	}
 	
 	/**
 	 * @param event
 	 */
 	private void handleRESTRequestLoginEvent(RESTRequestEvent event)
 	{
 		String result = event.getResult();
 		
 		// Check if the returned string isn't an error generated by the REST class
 		if (RESTRequest.isExceptionCode(result))
 		{
 			Toast.makeText(this, getString(R.string.error_unknown_exception), Toast.LENGTH_SHORT).show();
 			
 			return;
 		}
 		
 		try
 		{
 			// Parse JSON
 			JSONObject jsonObject = new JSONObject(result);
 			
 			String message   = jsonObject.getString("message");

 			// Show a message when user is blocked
 			if ("blocked".equals(message))
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 
 				builder.setTitle(R.string.login_failed);
 				builder.setMessage(R.string.login_blocked);
				builder.setPositiveButton(R.string.ok, null);
 				builder.show();
 				
 				return;
 			}
 			
			String sessionID = jsonObject.getString("sessionID");
			int userType     = jsonObject.getInt("userType");
			
 			// Message should be equal to success and sessionID should be available to be logged in successfully
 			if (!"success".equals(message) ||
 				sessionID == null ||
 				sessionID.length() <= 0)
 			{
 				Toast.makeText(getApplicationContext(), getResources().getString(R.string.rest_not_found), Toast.LENGTH_SHORT).show();
 				
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
 				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
 
 				builder.setTitle(R.string.login_failed);
 				builder.setMessage(R.string.login_wrong_credentials);
 				builder.setPositiveButton(R.string.ok, null);			
 				builder.show();
 			}
 		}
 	}
 }

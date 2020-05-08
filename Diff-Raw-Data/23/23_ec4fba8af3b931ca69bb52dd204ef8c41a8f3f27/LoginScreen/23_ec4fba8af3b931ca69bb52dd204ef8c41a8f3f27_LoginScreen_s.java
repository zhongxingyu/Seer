 package com.example.sicbogameexample;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import sicbo.components.UserComponent;
 import sicbo_networks.ConnectionHandler;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.UiLifecycleHelper;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.LoginButton;
 
 public class LoginScreen extends Activity implements OnClickListener {
 	EditText edt_username;
 	EditText edt_password;
 	Button btn_sign_in;
 	ImageButton btnBack;
 	TextView txtCreatAccount;
 	TextView txtForgotPassword;
 	List<Object> dataList;
 	String[] responseName = { "username", "password" };
 	ConnectionAsync connectionAsync;
 	private UiLifecycleHelper uiHelper;
	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login_screen);
 		uiHelper = new UiLifecycleHelper(this, callback);
 		uiHelper.onCreate(savedInstanceState);
 		LoginButton authButton = (LoginButton) findViewById(R.id.facebookLoginButton);
 		authButton.setReadPermissions(Arrays.asList("email"));
 
 		GameEntity.getInstance().connectionHandler = new ConnectionHandler();
 
 		edt_username = (EditText) findViewById(R.id.edt_username);
 		edt_password = (EditText) findViewById(R.id.edt_password);
 		btn_sign_in = (Button) findViewById(R.id.btn_sign_in);
 		btnBack = (ImageButton) findViewById(R.id.btn_back);
 		btn_sign_in.setOnClickListener(this);
 		txtCreatAccount = (TextView) findViewById(R.id.txt_create_account);
 		txtForgotPassword = (TextView) findViewById(R.id.txt_forgot_password);
 		edt_username.setOnClickListener(this);
 		txtForgotPassword.setOnClickListener(this);
 		txtCreatAccount.setOnClickListener(this);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.layout.menu_option, menu);
 		return true;
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 
 		case R.id.menu_help:
 			Intent intent = new Intent(LoginScreen.this, WebviewHelpPage.class);
 			startActivity(intent);
 			return true;
 		case R.id.menu_profile:
 			Intent intent1 = new Intent(LoginScreen.this, ProfileActivity.class);
 			startActivity(intent1);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 
 		switch (arg0.getId())
 
 		{
 		case R.id.txt_create_account:
 			Intent intent = new Intent(this, RegisterScreen.class);
 			this.startActivity(intent);
 			finish();
 			break;
 		case R.id.btn_sign_in:
 			connectionAsync = new ConnectionAsync();
 			String[] paramsName = { "username", "password" };
 			String[] paramsValue = { edt_username.getText().toString().trim(),
 					edt_password.getText().toString().trim() };
 			Object[] params = { GameEntity.getInstance().connectionHandler,
 					this, GameEntity.SIGNIN_TASK, paramsName, paramsValue };
 			connectionAsync.execute(params);
 			break;
 		case R.id.txt_forgot_password:
 
 			Intent itent = new Intent(LoginScreen.this, ResetPassword.class);
 			startActivity(itent);
 			break;
 
 		case R.id.btn_back:
 			break;
 		}
 
 	}
 
 	class ConnectionAsync extends AsyncTask<Object, String, Integer> {
 		ConnectionHandler connectionHandler;
 		Activity activity;
 		String fb_username;
 		String fb_email;
 		String fb_fullname;
 		@Override
 		protected Integer doInBackground(Object... params) {
 			// TODO Auto-generated method stub
 			connectionHandler = (ConnectionHandler) params[0];
 			activity = (Activity) params[1];
 			try {
 				connectionHandler.requestToServer((String) params[2],
 						(String[]) params[3], (Object[]) params[4]);
 				
 				if(params.length >= 6)
 				{
 					fb_username = ((String[]) params[5])[0];
 					fb_fullname = ((String[]) params[5])[1];
 				}
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer value) {
 			try {
 				// dataList = connectionHandler.parseData(responseName);
 				JSONObject result = connectionHandler.getResult();
 
 				// Create user and move to game scene
 				boolean isSuccess = result.getBoolean("is_success");
 				if (isSuccess) {
 					Intent intent = new Intent(activity,
 							SicBoGameActivity.class);
 
 					GameEntity.getInstance().userComponent = new UserComponent(
 							edt_username.getText().toString(),
 
 							(String) result.get("email"),
 							result.getDouble("balance"));
 					activity.startActivity(intent);
 					activity.finish();
 				}else if(!isSuccess && result.getBoolean("is_allow_facebook_register"))
 				{
 					//New facebook account => move to register screen
 					Intent intent = new Intent(activity, RegisterScreen.class);
 					//put extra facebook information
 					intent.putExtra("email", result.getString("email"));
 					intent.putExtra("username", fb_username);
 					intent.putExtra("fullname", fb_fullname);
 					activity.startActivity(intent);
 					activity.finish();
 				}
 				
 				else {
 					Toast.makeText(activity, "Login fail, please try again",
 							Toast.LENGTH_LONG).show();
 				}
 
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Facebook login implementation
 	 * */
 
 	private Session.StatusCallback callback = new Session.StatusCallback() {
 		@Override
 		public void call(Session session, SessionState state,
 				Exception exception) {
 			onSessionStateChange(session, state, exception);
 		}
 	};
 
 	private void onSessionStateChange(Session session, SessionState state,
 			Exception exception) {
		if (state.isOpened()) {
 			Log.i("Facebook login", "Logged in...");
 			Request.executeMeRequestAsync(session,
 					new Request.GraphUserCallback() {
 
 						@Override
 						public void onCompleted(GraphUser user,
 								Response response) {
 							if (user != null) {
 								// Display the parsed user info
 								loginByFacebook(user);
 							}
 						}
 					});
 		} else if (state.isClosed()) {
 			Log.i("Facebook login", "Logged out...");
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// For scenarios where the main activity is launched and user
 		// session is not null, the session state change notification
 		// may not be triggered. Trigger it if it's open/closed.
 		Session session = Session.getActiveSession();
 		if (session != null && (session.isOpened() || session.isClosed())) {
 			onSessionStateChange(session, session.getState(), null);
 		}
 		uiHelper.onResume();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		uiHelper.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		uiHelper.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		uiHelper.onDestroy();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		uiHelper.onSaveInstanceState(outState);
 	}
 
 	private String buildUserInfoDisplay(GraphUser user) {
 		StringBuilder userInfo = new StringBuilder("");
 
 		// Example: typed access (name)
 		// - no special permissions required
 		userInfo.append(String.format("Name: %s\n\n", user.getName()));
 
 		// Example: typed access (birthday)
 		// - requires user_birthday permission
 		userInfo.append(String.format("Birthday: %s\n\n", user.getBirthday()));
 
 		// Example: partially typed access, to location field,
 		// name key (location)
 		// - requires user_location permission
 		userInfo.append(String.format("Location: %s\n\n", user.getLocation()
 				.getProperty("name")));
 
 		// Example: access via property name (locale)
 		// - no special permissions required
 		userInfo.append(String.format("Locale: %s\n\n",
 				user.getProperty("locale")));
 
 		userInfo.append(String.format("Email: %s\n\n",
 				user.getProperty("email")));
 
 		// Example: access via key for array (languages)
 		// - requires user_likes permission
 		/*
 		 * JSONArray languages = (JSONArray) user.getProperty("languages"); if
 		 * (languages.length() > 0) { ArrayList<String> languageNames = new
 		 * ArrayList<String>(); for (int i = 0; i < languages.length(); i++) {
 		 * JSONObject language = languages.optJSONObject(i); // Add the language
 		 * name to a list. Use JSON // methods to get access to the name field.
 		 * languageNames.add(language.optString("name")); }
 		 * userInfo.append(String.format("Languages: %s\n\n",
 		 * languageNames.toString())); }
 		 */
 		return userInfo.toString();
 	}
 
 	private void loginByFacebook(GraphUser user) {
 		String email = (String) user.getProperty("email");
 		String username = user.getUsername();
 		String fullname = user.getName();
 
 		// if this is the first time user login -> move to register screen
 		// Request to server
 		connectionAsync = new ConnectionAsync();
 		String[] additionalParams = {username, fullname};
 		String[] paramsName = { "email" };
 		String[] paramsValue = { email };
 		Object[] params = { GameEntity.getInstance().connectionHandler, this,
 				GameEntity.SIGNIN_FACEBOOK_TASK, paramsName, paramsValue, additionalParams };
 		connectionAsync.execute(params);
 	}
 }

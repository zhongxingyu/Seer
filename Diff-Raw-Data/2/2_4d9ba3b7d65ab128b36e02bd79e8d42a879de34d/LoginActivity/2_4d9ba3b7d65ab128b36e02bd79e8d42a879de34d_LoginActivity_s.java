 package com.isawabird;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.isawabird.parse.ParseConsts;
 import com.parse.FindCallback;
 import com.parse.LogInCallback;
 import com.parse.ParseException;
 import com.parse.ParseQuery;
 import com.parse.ParseTwitterUtils;
 import com.parse.ParseUser;
 import com.parse.SignUpCallback;
 
 public class LoginActivity extends Activity {
 
 	private TextView tv_title;
 	private TextView tv_forgot;
 	private TextView tv_or;
 
 	private Button mLoginButton;
 	private TextView mSignupButton;
 	private TextView mSkipButton;
 
 	private TextView mShowSignupButton;
 	private TextView mShowLoginButton;
 
 	private EditText mUsernameText;
 	private EditText mPassText;
 	private EditText mPassConfirmText;
 
 	Typeface openSansLight;
 	Typeface openSansBold;
 	Typeface openSansBoldItalic;
 	Typeface sonsie;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 
 		// hide action bar before switching to login screen
 		getActionBar().hide();
 
 		openSansLight = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
 		openSansBold = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Bold.ttf");
 		openSansBoldItalic = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-BoldItalic.ttf");
 		sonsie = Typeface.createFromAsset(getAssets(), "fonts/SonsieOne-Regular.ttf");
 
 		tv_title = (TextView)findViewById(R.id.textView_title);
 		tv_forgot = (TextView) findViewById(R.id.btn_forgot_password);
 		tv_or = (TextView) findViewById(R.id.textView_or);
 
 		mLoginButton = (Button) findViewById(R.id.btn_login);
 		mSignupButton = (TextView) findViewById(R.id.btn_signup);
 		mSkipButton = (TextView) findViewById(R.id.btn_skip);
 
 		mShowSignupButton = (TextView) findViewById(R.id.btn_showsignup);
 		mShowLoginButton = (TextView) findViewById(R.id.btn_showlogin);
 
 		mUsernameText = (EditText) findViewById(R.id.text_email);
 		mPassText = (EditText) findViewById(R.id.text_pass);
 		mPassConfirmText = (EditText) findViewById(R.id.text_confirm);
 
 		tv_title.setTypeface(sonsie);
 		tv_forgot.setTypeface(openSansLight);
 		tv_or.setTypeface(openSansBold);
 
 		mLoginButton.setTypeface(openSansBold);
 		mSignupButton.setTypeface(openSansBold);
 		mSkipButton.setTypeface(openSansBold);
 
 		mUsernameText.setTypeface(openSansLight);
 		mPassText.setTypeface(openSansLight);
 		mPassConfirmText.setTypeface(openSansLight);
 
 		mSkipButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showHome();
 			}
 		});
 	}
 
 	public void showLogin(View view) {
 		mShowLoginButton.setVisibility(View.GONE);
 		mSignupButton.setVisibility(View.GONE);
 		mPassConfirmText.setVisibility(View.GONE);
 		mShowSignupButton.setVisibility(View.VISIBLE);
 		mLoginButton.setVisibility(View.VISIBLE);
 	}
 
 	public void showSignup(View view) {
 		mShowSignupButton.setVisibility(View.GONE);
 		mLoginButton.setVisibility(View.GONE);
 		mShowLoginButton.setVisibility(View.VISIBLE);
 		mPassConfirmText.setVisibility(View.VISIBLE);
 		mSignupButton.setVisibility(View.VISIBLE);
 	}
 
 	public void login(View view) {
 
 		try {
 			String user = mUsernameText.getText().toString();
 			String pass = mPassText.getText().toString();
 //			Log.i(Consts.TAG, "Logging in...");
 //			ParseUser.logIn("sriniketana", "test123");
 //			Log.i(Consts.TAG, "Logged in");
 //			showHome();
 			Log.i(Consts.TAG, "User/pwd = " + user + ":" + pass);
 			ParseUser.logInInBackground(user, pass, new LogInCallback() {
 				public void done(ParseUser user, ParseException e) {
 					if(user == null) {
 						Toast.makeText(getApplicationContext(), "Not able to login. Please try again later", Toast.LENGTH_SHORT).show();
 					} else {
 						showHome();
 					}
 				}
 			});
 		} catch (Exception e) {
 			Toast.makeText(getApplicationContext(), "Not able to login. Please try again later", Toast.LENGTH_SHORT).show();
 			e.printStackTrace();					
 		}
 	}
 
 	public void loginTwitter(View view) {
 		// network not available
 		if(!Utils.isNetworkAvailable(getApplicationContext())) {
 			Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		ParseTwitterUtils.initialize(ParseConsts.TWITTER_CONSUMER_KEY, ParseConsts.TWITTER_CONSUMER_SECRET); 
		ParseTwitterUtils.logIn(getApplicationContext(), new LogInCallback() {
 
 			@Override
 			public void done(ParseUser user, ParseException ex) {
 				if (user == null){
 					Toast.makeText(getApplicationContext(), "Unable to login using Twitter " + ex.getMessage(), Toast.LENGTH_SHORT).show(); 
 				}else{
 					Utils.setCurrentUsername(user.getUsername()); 
 					showHome(); 
 				}
 			}
 		});
 	}
 
 	public void loginFacebook(View view) {
 		// network not available
 		if(!Utils.isNetworkAvailable(getApplicationContext())) {
 			Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
 			return;
 		}
 	}
 
 	public void loginGoogle(View view) {
 		// network not available
 		if(!Utils.isNetworkAvailable(getApplicationContext())) {
 			Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
 			return;
 		}
 	}
 
 	public void signup(View view) {
 
 		// network not available
 		if(!Utils.isNetworkAvailable(getApplicationContext())) {
 			Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		final String email = mUsernameText.getText().toString();
 		final String pass = mPassText.getText().toString();
 		final String passConfirm = mPassConfirmText.getText().toString();
 
 		if(!pass.equals(passConfirm)) {
 			Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		ParseQuery<ParseUser> query = ParseUser.getQuery();
 		query.whereEqualTo("username", email);
 		// TODO: add busy indicator
 		query.findInBackground(new FindCallback<ParseUser>() {
 			public void done(List<ParseUser> objects, ParseException e) {
 				if (e == null) {
 					if(objects != null && objects.size() > 0) {
 						Toast.makeText(getApplicationContext(), "User with email '" + email + "' already exists", Toast.LENGTH_SHORT).show();
 						return;
 					} else {
 						ParseUser user = new ParseUser();
 						user.setUsername(email);
 						user.setPassword(pass);
 						user.setEmail(email);
 
 						user.signUpInBackground(new SignUpCallback() {
 							@Override
 							public void done(ParseException e) {
 								// TODO Auto-generated method stub
 								if (e != null) {
 									Toast.makeText(getApplicationContext(), "Not able to signup. Please try again later", Toast.LENGTH_SHORT).show();
 								} else {
 									Toast.makeText(getApplicationContext(), "Successfully signed up", Toast.LENGTH_SHORT).show();
 									ParseUser.logInInBackground(email, pass, new LogInCallback() {
 										@Override
 										public void done(ParseUser user,
 												ParseException e) {
 											if(user == null) {
 												Toast.makeText(getApplicationContext(), "Not able to login. Please try again later", Toast.LENGTH_SHORT).show();
 											} else {
 												showHome();
 											}
 										}
 									});
 								}
 							}
 						});
 					}
 				} else {
 					Toast.makeText(getApplicationContext(), "Not able to signup. Please try again later", Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private void showHome() {
 		Log.i(Consts.TAG, "TO HOME");
 		Utils.setFirstTime(false);
 		Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
 		// FLAG_ACTIVITY_CLEAR_TOP is required if we are coming from settings by clicking on 'Login'
 		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
 		startActivity(homeIntent);
 		finish();
 	}
 }

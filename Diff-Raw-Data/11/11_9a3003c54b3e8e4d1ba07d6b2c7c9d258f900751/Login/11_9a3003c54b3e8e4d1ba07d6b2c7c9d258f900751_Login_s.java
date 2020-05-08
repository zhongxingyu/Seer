 package com.osu.insecurity;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.AssetManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.osu.insecurity.util.SystemUiHider;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  * 
  * @see SystemUiHider
  */
 public class Login extends Activity {
 	/**
 	 * Whether or not the system UI should be auto-hidden after
 	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
 	 */
 	private static final boolean AUTO_HIDE = true;
 
 	/**
 	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
 	 * user interaction before hiding the system UI.
 	 */
 	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
 
 	/**
 	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
 	 * will show the system UI visibility upon interaction.
 	 */
 	private static final boolean TOGGLE_ON_CLICK = true;
 
 	/**
 	 * The flags to pass to {@link SystemUiHider#getInstance}.
 	 */
 	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
 
 	/**
 	 * The instance of the {@link SystemUiHider} for this activity.
 	 */
 	private SystemUiHider mSystemUiHider;
 	
 	/**
 	 * The incorrect email address/password Toast string
 	 */
 	private static final String LOGIN_INCORRECT = "Incorrect email address/password";
 	
 	
 	/**
 	 * Regular Expression Email Pattern
 	 */
 	protected static final String EMAIL_PATTERN = 
 			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 	/**
 	 * Below are the controls of the Login menu
 	 * login_emailAddressEditText = email address edit text box
 	 * login_passwordEditText = password edit text box
 	 * login_registerText = register text view
 	 * login_forgotPasswordText = forgot password text view
 	 * login_loginButton = login button
 	 */
 	private static EditText login_emailAddressEditText;
 	private static  EditText login_passwordEditText;
 	private TextView login_registerText;
 	private TextView login_forgotPasswordText;
 	private Button login_loginButton;
 	private CheckBox login_staySignedInCheckBox;
 	
 	
 	/**
 	 * database is a collection sent over a server to keep track of the 
 	 * users and their personal information
 	 */
 	protected static Set<Profile> database;
 	
 	
 	/**
	 * The email to set the login screen from forgotpassword 
 	 */
 	protected static String forgotEmail;
 	
 	/**
 	 * The email and password that was just registered
 	 */
 	protected static String registeredEmail;
 	protected static String registeredPassword;
 	
 	/**
 	 * The saved password and email for the user
 	 */
 	private String savedEmail;
 	private String savedPassword;
 	
 	/**
 	 * Email used to sign in
 	 */
 	protected static Profile currentUserSignedIn;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) ==  ConnectionResult.SUCCESS)
 		{
 			database = new HashSet<Profile>();
 			forgotEmail = "";
 			//loadData("database.txt");
 			try {
 				loadData();
 			} catch (IOException e) {
 				
 			}
 			login();
 		}
 		else
 		{
 			switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()))
 			{
 				case ConnectionResult.SERVICE_MISSING: 
 					//GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, this, );
 				case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED: 
 				case ConnectionResult.SERVICE_DISABLED: 
 			}
 		}
 		
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 
 		// Trigger the initial hide() shortly after the activity has been
 		// created, to briefly hint to the user that UI controls
 		// are available.
 		delayedHide(100);
 	}
 	
 	/**
 	 * Touch listener to use for in-layout UI controls to delay hiding the
 	 * system UI. This is to prevent the jarring behavior of controls going away
 	 * while interacting with activity UI.
 	 */
 	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
 		@Override
 		public boolean onTouch(View view, MotionEvent motionEvent) {
 			if (AUTO_HIDE) {
 				delayedHide(AUTO_HIDE_DELAY_MILLIS);
 			}
 			return false;
 		}
 	};
 
 	Handler mHideHandler = new Handler();
 	Runnable mHideRunnable = new Runnable() {
 		@Override
 		public void run() {
 			mSystemUiHider.hide();
 		}
 	};
 
 	/**
 	 * Schedules a call to hide() in [delay] milliseconds, canceling any
 	 * previously scheduled calls.
 	 */
 	private void delayedHide(int delayMillis) {
 		mHideHandler.removeCallbacks(mHideRunnable);
 		mHideHandler.postDelayed(mHideRunnable, delayMillis);
 	}
 	
 	/**
 	 * Sets up the login screen
 	 */
 	private void login()
 	{
 		setContentView(R.layout.login_layout);
 
 		final View controlsView = findViewById(R.id.fullscreen_content_controls);
 		final View contentView = findViewById(R.id.fullscreen_content);
 		login_emailAddressEditText = (EditText) findViewById(R.id.login_email_address_edit_text);
 		login_passwordEditText = (EditText) findViewById(R.id.login_password_edit_text);
 		login_registerText = (TextView) findViewById(R.id.login_register_button);
 		login_forgotPasswordText = (TextView) findViewById(R.id.login_forgot_password_button);
 		login_loginButton = (Button) findViewById(R.id.login_login_button);
 		login_staySignedInCheckBox = (CheckBox) findViewById(R.id.login_stay_signed_in_check_box);
 		
 		//TODO for testing purposes only 
 		Profile temp = null;
 		Iterator<Profile> iter = database.iterator();
 		
 		while(iter.hasNext())
 		{
 			Profile next = iter.next();
 			if(next.getEmail().equals("buckeye.1@osu.edu"))
 			{
 				temp = next;
 			}
 		}
 		login_emailAddressEditText.setText(temp.getEmail());
 		login_passwordEditText.setText(temp.getPassword());
 		
		if(forgotEmail.length() > 0)
 		{
 			login_emailAddressEditText.setText(forgotEmail);
 			forgotEmail = "";
 		}
		else if(savedEmail != null && !savedEmail.equals(""))
 		{
 			login_emailAddressEditText.setText(savedEmail);
 			login_passwordEditText.setText(savedPassword);
 		}
		else if(registeredEmail != null && !registeredEmail.equals(""))
 		{
 			login_emailAddressEditText.setText(registeredEmail);
 			login_passwordEditText.setText(registeredPassword);
 			registeredEmail = "";
 			registeredPassword = "";
 		}
 		// Set up an instance of SystemUiHider to control the system UI for
 		// this activity.
 		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
 				HIDER_FLAGS);
 		mSystemUiHider.setup();
 		mSystemUiHider
 				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
 					// Cached values.
 					int mControlsHeight;
 					int mShortAnimTime;
 
 					@Override
 					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 					public void onVisibilityChange(boolean visible) {
 						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 							// If the ViewPropertyAnimator API is available
 							// (Honeycomb MR2 and later), use it to animate the
 							// in-layout UI controls at the bottom of the
 							// screen.
 							if (mControlsHeight == 0) {
 								mControlsHeight = controlsView.getHeight();
 							}
 							if (mShortAnimTime == 0) {
 								mShortAnimTime = getResources().getInteger(
 										android.R.integer.config_shortAnimTime);
 							}
 							controlsView
 									.animate()
 									.translationY(visible ? 0 : mControlsHeight)
 									.setDuration(mShortAnimTime);
 						} else {
 							// If the ViewPropertyAnimator APIs aren't
 							// available, simply show or hide the in-layout UI
 							// controls.
 							controlsView.setVisibility(visible ? View.VISIBLE
 									: View.GONE);
 						}
 
 						if (visible && AUTO_HIDE) {
 							// Schedule a hide().
 							delayedHide(AUTO_HIDE_DELAY_MILLIS);
 						}
 					}
 				});
 
 		// Set up the user interaction to manually show or hide the system UI.
 		contentView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (TOGGLE_ON_CLICK) {
 					mSystemUiHider.toggle();
 				} else {
 					mSystemUiHider.show();
 				}
 			}
 		});
 
 		// Upon interacting with UI controls, delay any scheduled hide()
 		// operations to prevent the jarring behavior of controls going away
 		// while interacting with the UI.
 		
 		//sets up the interaction when the user clicks the login button
 		login_loginButton.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				if(loginSuccessful()) //check to see if the user entered correct email address/password
 				{
 					if(login_staySignedInCheckBox.isChecked())
 					{
 						//save the email and password
 						savedEmail = login_emailAddressEditText.getText().toString();
 						savedPassword = login_passwordEditText.getText().toString();
 					}
 					else
 					{
 						savedEmail = "";
 						savedPassword = "";
 					}
 					//go to the main screen
 					startActivity(new Intent ("com.osu.insecurity.MAIN"));
 				}
 				else
 				{
 					Toast.makeText(getApplicationContext(), LOGIN_INCORRECT, Toast.LENGTH_SHORT).show();
 				}
 			}	
 		});
 		
 		//sets up the interaction when the user clicks the register text
 		login_registerText.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				//go to the register screen
 				startActivity(new Intent ("com.osu.insecurity.REGISTER"));
 			}	
 		});
 		
 		//sets up the interaction when the user clicks the forgot password text
 		login_forgotPasswordText.setOnClickListener(new View.OnClickListener() 
 		{
 			@Override
 			public void onClick(View v) 
 			{
 				//go to the contacts screen
 				startActivity(new Intent ("com.osu.insecurity.FORGOTPASSWORD"));
 			}	
 		});
 	}
 	/**
 	 * 
 	 * @return true if the email address/password is correct, otherwise return false
 	 */
 	private boolean loginSuccessful()
 	{
 		boolean success = false;
 		if(login_emailAddressEditText.getText().toString().length() != 0) //check to see if the user has entered an email address
 		{
 			if(login_passwordEditText.getText().toString().length() != 0 ) //check to see if the user has entered a password
 			{
 				String email = login_emailAddressEditText.getText().toString();
 				String password = login_passwordEditText.getText().toString();
 				
 				Iterator<Profile> iter = database.iterator();
 				
 				while(iter.hasNext())
 				{
 					Profile next = iter.next();
 					if(next.getEmail().equals(email)) //check the email address/password with the data base
 					{
 						if(next.getPassword().equals(password))
 						{
 							success = true;
 							currentUserSignedIn = next;
 						}
 					}
 				}
 			}
 		}
 		return success;
 	}
 	
 	
 	
 	
 	/**
 	 * Loads the data from database.txt in the assets folder
 	 * Loads the email addresses and passwords
 	 */
 	private void loadData() throws IOException
 	{
 		AssetManager assets = getResources().getAssets();
 		DataInputStream in = new DataInputStream(assets.open("database.txt"));// opens file  sent into loadData into the data input stream
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));// creates buffered reader br 
 		String name = ""; 
 		String email = "";
 		String password = "";
 		String distress = "";
 		boolean onName = true;
 		boolean onEmail = true;
 		boolean onPassword = true;
 		boolean onDistress = true;
 		while(br.ready())
 		{
 			char c = (char) br.read();// sets c as the character read from the buffered reader
 			if(c == ':')
 			{
 				if(onName)
 				{
 					onName = false;
 					onEmail = true;
 				}
 				else if(onEmail)
 				{
 					onEmail = false;
 					onPassword = true;
 				}
 				else if(onPassword)
 				{
 					onPassword = false;
 					onDistress = true;
 				}
 			}
 			else if(c == ';') //check to see if end of line
 			{
 				Profile newPI = new Profile(name, email, password, distress);
 				database.add(newPI);
 				name = "";
 				email = "";
 				password = "";
 				distress = "";
 				onDistress = false;
 				onName = true;
 			}
 			else
 			{
 				if(onName)
 				{
 					name = name + c;
 				}
 				else if(onEmail) //check to see if we are still getting characters from the email
 				{
 					email = email + c;
 				}
 				else if(onPassword)
 				{
 					password = password + c;
 				}
 				else if(onDistress)
 				{
 					distress = distress + c;
 				}
 			}
 		}
 		br.close();//close buffered reader
 		in.close();// close input stream
 	}
 	
 	/**
 	 * Saves the data base
 	 */
 	private void saveDataBase()
 	{
 		try 
 		{
 			File output= new File("database.txt");
 			if (output.canWrite()) 
 			{
 				BufferedWriter out = new BufferedWriter(new FileWriter(output, false));
 			    Iterator<Profile> iter =  database.iterator();
 			    while(iter.hasNext()) //iterate over the keys and values and print to the database file
 			    {
 			    	Profile pi = iter.next();
 			    	out.write(pi.getName() + ":" + pi.getEmail() + ":" + pi.getPassword() + ":" + pi.getDistressPassword() + ";");
 			    	out.write("\n");
 			    }
 			    out.close();
 			}
 		} 
 		catch (IOException e) 
 		{
 		    
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 	    	//if the user hits the back button on the keypad, make sure we save the contacts
 	        saveDataBase();
 	        finish();
 	        return true;
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
 	 * Updates the email and password edit text fields
 	 */
 	public static void updateFields()
 	{
 		if(forgotEmail.length() > 0) //see if there were any updates
 		{
 			login_emailAddressEditText.setText(forgotEmail);
 			forgotEmail = ""; //reset
 		}
 		if(registeredEmail.length() > 0) //see if there were any updates
 		{
 			login_emailAddressEditText.setText(registeredEmail);
 			login_emailAddressEditText.setText(registeredPassword);
 			registeredEmail = ""; //reset
 			registeredPassword = ""; //reset
 		}
 	}
 }

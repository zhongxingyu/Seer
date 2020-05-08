 package com.boh.flatmate;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 import com.boh.flatmate.R;
 import com.boh.flatmate.FlatMate.ConnectionExchanger;
 import com.boh.flatmate.connection.Flat;
 import com.boh.flatmate.connection.ServerConnection;
 import com.boh.flatmate.connection.User;
 import com.google.android.gcm.GCMRegistrar;
 import com.google.android.maps.GeoPoint;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.location.Address;
 import android.location.Geocoder;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.StrictMode;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Profile;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class SplashActivity extends Activity {
 
 	private boolean AUTOMATIC_LOGIN = true;
 
 	int screenPosition = 0;
 
 	ServerConnection connection = new ServerConnection();
 	String FILENAME;
 	final Handler myHandler = new Handler();
 
 	private int registrationID;
 	private String registrationEmail;
 	private String registrationPassword;
 	private String registrationFirstName;
 	private String registrationLastName;
 	private String registrationPhone;
 	private String registrationFlatID;
 	private String deviceId;
 
 	private ListView mListView;
 	private SplashRowAdapter mAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Show the splash screen
 		setContentView(R.layout.splash);
 
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
 		StrictMode.setThreadPolicy(policy);
 
 		FILENAME = this.getFilesDir().getPath().toString() + "UserAuthCode.txt";
 
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		deviceId = GCMRegistrar.getRegistrationId(this);
 		if (deviceId.equals("")) {
 			GCMRegistrar.register(this, "1098971778005");
 		} else {
 			Log.v("GCM", "Already registered");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_logout:
 			File logFile = new File(ConnectionExchanger.connection.FileName);
 			if (logFile.exists())
 			{
 				logFile.delete();
 			}
 			finish();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	@Override
 	public void onStart(){
 		super.onStart();
 
 		Thread splashWait = new Thread(){
 			@Override
 			public void run(){
 				try{
 					sleep(100);
 				}catch(InterruptedException e){
 				}finally{
 					myHandler.post(Login);
 				}
 			}
 		};
 
 		splashWait.start();
 
 	}
 
 	public void checkFlatOnLogIn(){
 		View loginBox = findViewById(R.id.loginBox);
 		View registerBox = findViewById(R.id.loginBox);
 		loginBox.setVisibility(View.GONE);
 		registerBox.setVisibility(View.GONE);
 		User myUser = connection.getMe();
 		if(myUser.getFlat_id() == null){
 			registrationEmail = myUser.getEmail();
 			registrationFirstName = myUser.getFirst_name();
 			registrationLastName = myUser.getLast_name();
 			registrationPhone = myUser.getPhone_number();
 			registrationID = myUser.getId();
			if(screenPosition < 2){
				showFindFlat();
			}
 		}else{
 			startApp();
 			finish();
 		}
 	}
 
 	final Runnable Login = new Runnable() {
 		public void run() {
 			loginChecks();
 		}
 	};
 
 	private void loginChecks(){
 		int loggedin = userLogin();
 
 		if(loggedin == 1){
 			if(deviceId != null) connection.maintainGcmRegistration(deviceId);
 			checkFlatOnLogIn();
 		}else{
 			if(screenPosition == 0) newLogin();
 		}
 	}
 
 	private int userLogin(){
 		String auth = ServerConnection.loadAuth(FILENAME);
 		if(auth == null){
 			return 0;
 		} else {
 			// sucessfull login here return 1; test return 0;
 			if(AUTOMATIC_LOGIN) return 1;
 			else return 0;
 			//return 0;
 		}
 	}
 
 	private void newLogin(){
 		View loginBox = findViewById(R.id.loginBox);
 		View flatLogo = findViewById(R.id.splashLogo);
 
 		findViewById(R.id.loginBox).setVisibility(View.VISIBLE);
 
 		TranslateAnimation anim = new TranslateAnimation( 0, 0 , 125, 0 );
 		anim.setDuration(250);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim);
 
 		AlphaAnimation anim2 = new AlphaAnimation( 0, 1 );
 		anim2.setDuration(250);
 		anim2.setStartOffset(150);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				//anim2.setDuration(1);
 				//findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		loginBox.startAnimation(anim2);
 
 
 		AccountManager am = AccountManager.get(this);
 		Account[] accounts = am.getAccountsByType("com.google");
 		String email;
 		if(accounts.length > 0){
 			Account googleAccount = accounts[0];
 			email = googleAccount.name;
 		} else {
 			email = "";
 		}
 		EditText emailBox = (EditText) findViewById(R.id.emailTbox);
 
 		emailBox.setText(email);
 
 		//temp for testing ---- use above line for real
 		//emailBox.setText("adam9@bla.com");
 		//EditText passwordBox1 = (EditText) findViewById(R.id.passwordBox);
 		//passwordBox1.setText("Password");
 
 
 		Button login = (Button) findViewById(R.id.loginButton);
 		login.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				EditText emailBox2 = (EditText) findViewById(R.id.emailTbox);		
 				String emailInput = emailBox2.getText().toString();
 				EditText passwordBox1 = (EditText) findViewById(R.id.passwordBox);			
 				String passwordInput = passwordBox1.getText().toString();
 				Button login2 = (Button) findViewById(R.id.loginButton);
 				login2.setVisibility(View.GONE);
 				Button register = (Button) findViewById(R.id.registerButton);
 				register.setVisibility(View.GONE);
 				ProgressBar spinner = (ProgressBar) findViewById(R.id.loginSpinner);
 				spinner.setVisibility(View.VISIBLE);
 				new serverLogin().execute(emailInput,passwordInput,deviceId);
 			}
 		});
 
 		Button register = (Button) findViewById(R.id.registerButton);
 		register.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				registerButtonPressed();
 			}
 		});
 
 	}
 
 	@TargetApi(14)
 	private void registerButtonPressed(){
 		screenPosition = 1; 
 		View loginButtonBox = findViewById(R.id.loginButtonBox);
 
 		AlphaAnimation anim3 = new AlphaAnimation( 1, 0 );
 		anim3.setDuration(25);
 		anim3.setFillAfter( false );
 		anim3.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				View loginButtonBox = findViewById(R.id.loginButtonBox);
 				loginButtonBox.setVisibility(View.GONE);
 				View registerBox = findViewById(R.id.registerBox);
 				registerBox.setVisibility(View.VISIBLE);
 				TranslateAnimation anim = new TranslateAnimation( 0, 0 , 145, 0 );
 				anim.setDuration(250);
 				anim.setFillAfter( false );
 				anim.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 						anim2.setDuration(1);
 						findViewById(R.id.splashLogo).startAnimation(anim2);
 						System.out.println(findViewById(R.id.splashLogo).getY());
 					}
 					@Override
 					public void onAnimationRepeat(Animation animation) { }
 					@Override
 					public void onAnimationStart(Animation animation) { }
 				});
 				View flatLogo = findViewById(R.id.splashLogo);
 				View loginBox = findViewById(R.id.loginBox);
 				flatLogo.startAnimation(anim);
 				loginBox.startAnimation(anim);
 				AlphaAnimation anim2 = new AlphaAnimation( 0, 1 );
 				anim2.setDuration(125);
 				anim2.setStartOffset(150);
 				anim2.setFillAfter( false );
 				anim2.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						//TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 						//anim2.setDuration(1);
 						//findViewById(R.id.splashLogo).startAnimation(anim2);
 					}
 					@Override
 					public void onAnimationRepeat(Animation animation) { }
 					@Override
 					public void onAnimationStart(Animation animation) { }
 				});
 				registerBox.startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		loginButtonBox.startAnimation(anim3);
 
 		Button back = (Button) findViewById(R.id.backButton);
 		back.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				backButtonPressed();
 			}
 		});
 
 		Button register = (Button) findViewById(R.id.createButton);
 		register.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createButtonPressed();
 			}
 		});
 
 		EditText phoneInput = (EditText)findViewById(R.id.phoneTextBox);
 		EditText firstInput = (EditText)findViewById(R.id.firstBox);
 		EditText lastInput = (EditText)findViewById(R.id.lastBox);
 
 		Uri pesonUri = Profile.CONTENT_URI;
 
 		Cursor people = getContentResolver().query(pesonUri, null, null, null, null);
 
 		int firstName = people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
 
 
 		people.moveToFirst();
 		if(people != null && people.getCount() >0){
 			String[] name = people.getString(firstName).split(" ");
 			if(name.length > 1){
 				String first = name[0];
 				String last = name[1];
 				System.out.println("first = "+first+", last = "+last);
 				firstInput.setText(first);
 				lastInput.setText(last);
 			}
 		}
 
 		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 
 		Cursor phoneNumber = getContentResolver().query(uri, null, null, null, null);
 
 		int userID = phoneNumber.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
 		int userNumber = phoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
 
 		phoneNumber.moveToFirst();
 		if(phoneNumber != null && phoneNumber.getCount() >0){
 			do {
 				String id   = phoneNumber.getString(userID);
 				String number = phoneNumber.getString(userNumber);
 				System.out.println("id = "+id+", number = "+number);
 				if(id.equals("1")){
 					if(number.startsWith("+44")){
 						number = number.replace("+44", "0");
 					}
 					phoneInput.setText(number);
 					break;
 				}
 			} while (phoneNumber.moveToNext());
 		}
 	}
 
 	private void backButtonPressed(){
 		screenPosition = 0;
 		View flatLogo = findViewById(R.id.splashLogo);
 		View loginBox = findViewById(R.id.loginBox);
 		View registerBox = findViewById(R.id.registerBox);
 
 		TranslateAnimation anim = new TranslateAnimation( 0, 0 , 0, 145 );
 		anim.setDuration(250);
 		anim.setStartOffset(0);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 				System.out.println(findViewById(R.id.splashLogo).getY());
 				View registerBox = findViewById(R.id.registerBox);
 				registerBox.setVisibility(View.GONE);
 				View loginButtonBox = findViewById(R.id.loginButtonBox);
 				loginButtonBox.setVisibility(View.VISIBLE);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim);
 		loginBox.startAnimation(anim);
 
 		AlphaAnimation anim2 = new AlphaAnimation( 1, 0 );
 		anim2.setDuration(50);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				AlphaAnimation anim = new AlphaAnimation( 0, 0 );
 				anim.setDuration(250);
 				anim.setFillAfter( false );
 				View registerBox = findViewById(R.id.registerBox);
 				registerBox.startAnimation(anim);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		registerBox.startAnimation(anim2);
 
 	}
 
 	private void showFindFlat(){
 		screenPosition = 2;
 
 		View flatBox = findViewById(R.id.flatBox);
 		View flatLogo = findViewById(R.id.splashLogo);
 
 		flatBox.setVisibility(View.VISIBLE);
 
 		TranslateAnimation anim = new TranslateAnimation( 0, 0 , 125, 0 );
 		anim.setDuration(250);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim);
 
 		AlphaAnimation anim2 = new AlphaAnimation( 0, 1 );
 		anim2.setDuration(250);
 		anim2.setStartOffset(150);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				//anim2.setDuration(1);
 				//findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatBox.startAnimation(anim2);
 
 
 		Button search = (Button) findViewById(R.id.searchButton);
 		search.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				searchButtonPressed();
 			}
 		});
 
 		Button newFlat = (Button) findViewById(R.id.newFlat);
 		newFlat.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				newFlatButtonPressed();
 			}
 		});
 	}
 
 	private void newFlatButtonPressed(){
 		screenPosition = 3;
 		TranslateAnimation anim = new TranslateAnimation( 0, -750 , 0, 0 );
 		anim.setDuration(300);
 		anim.setStartOffset(0);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				View flatBox = findViewById(R.id.flatBox);
 				View newFlatBox = findViewById(R.id.addFlatBox);
 				flatBox.setVisibility(View.GONE);
 				newFlatBox.setVisibility(View.VISIBLE);
 				TranslateAnimation anim = new TranslateAnimation( 750, 0 , 0, 0 );
 				anim.setDuration(300);
 				anim.setStartOffset(0);
 				anim.setFillAfter( false );
 				anim.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {}
 					@Override
 					public void onAnimationRepeat(Animation animation) {}
 					@Override
 					public void onAnimationStart(Animation animation) {}
 				});
 				newFlatBox.startAnimation(anim);
 
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		View flatBox = findViewById(R.id.flatBox);
 		flatBox.startAnimation(anim);
 
 		View flatLogo = findViewById(R.id.splashLogo);
 
 		TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 22 );
 		anim2.setDuration(300);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim2);
 
 		Button flatBack = (Button) findViewById(R.id.newFlatBack);
 		flatBack.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				newFlatBack();
 			}
 		});
 
 		Button createNewFlat = (Button) findViewById(R.id.createFlatButton);
 		createNewFlat.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				createNewFlatPressed();
 			}
 		});
 
 	}
 
 	private void createButtonPressed(){
 		int correct = registerCheck1();
 		//int correct = 1;
 
 		if(correct == 1){
 			EditText emailInput = (EditText)findViewById(R.id.emailTbox);
 			EditText password1Input = (EditText)findViewById(R.id.passwordBox);
 			EditText firstInput = (EditText)findViewById(R.id.firstBox);
 			EditText lastInput = (EditText)findViewById(R.id.lastBox);
 			EditText phoneInput = (EditText)findViewById(R.id.phoneTextBox);
 			registrationEmail = emailInput.getText().toString();
 			registrationPassword = password1Input.getText().toString();
 			registrationFirstName = firstInput.getText().toString();
 			registrationLastName = lastInput.getText().toString();
 			registrationPhone = phoneInput.getText().toString();
 
 			User newUser = new User();
 			newUser.setEmail(registrationEmail);
 			newUser.setFirst_name(registrationFirstName);
 			newUser.setLast_name(registrationLastName);
 			newUser.setPhone_number(registrationPhone);
 			newUser.setPassword(registrationPassword);
 
 			View buttons = findViewById(R.id.backNextButtons);
 			buttons.setVisibility(View.GONE);
 			View spinner = findViewById(R.id.registerSpinner);
 			spinner.setVisibility(View.VISIBLE);
 
 			Log.v("REG","Registering user in background");
 			new registerUser().execute(newUser);
 		}
 	}
 
 	public void updateComplete(){
 		Log.v("REG","Update complete");
 		startApp();
 		finish();
 	}
 
 	public void registrationNewFlatComplete(Flat newFlat){
 		registrationFlatID = newFlat.getId()+"";
 
 		User newUser = new User();
 		newUser.setEmail(registrationEmail);
 		newUser.setFirst_name(registrationFirstName);
 		newUser.setLast_name(registrationLastName);
 		newUser.setPhone_number(registrationPhone);
 		newUser.setFlat_id(registrationFlatID);
 		newUser.setId(registrationID);
 
 		Log.v("REG","Updating user's flat to new flat: " + registrationFlatID);
 		new updateUser().execute(newUser);
 
 	}
 
 	public void newFlatFailed(){
 		View createButtons = findViewById(R.id.createButtons);
 		createButtons.setVisibility(View.VISIBLE);
 		View createSpinner = findViewById(R.id.createSpinnerBox);
 		createSpinner.setVisibility(View.GONE);
 		Toast.makeText(getApplicationContext(), "Error, new flat could not be created", Toast.LENGTH_SHORT).show();
 	}
 
 	public void registrationComplete(String result){
 		if(result == "failed" || result == "invalid" || result == "connection"){
 			View buttons = findViewById(R.id.backNextButtons);
 			buttons.setVisibility(View.VISIBLE);
 			View spinner = findViewById(R.id.registerSpinner);
 			spinner.setVisibility(View.GONE);
 			Toast.makeText(getApplicationContext(), "Registration could not be Completed", Toast.LENGTH_SHORT).show();
 		}else{
 			View loginBox = findViewById(R.id.loginBox);
 			View registerBox = findViewById(R.id.registerBox);
 			loginBox.setVisibility(View.GONE);
 			registerBox.setVisibility(View.GONE);
 			new serverLogin().execute(registrationEmail,registrationPassword,deviceId);
 		}
 	}
 
 	public void newFlatBack(){
 		screenPosition = 2;
 		int offset = findViewById(R.id.addFlatBox).getWidth();
 		TranslateAnimation anim = new TranslateAnimation( 0, offset , 0, 0 );
 		anim.setDuration(300);
 		anim.setStartOffset(0);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				View addFlatBox = findViewById(R.id.addFlatBox);
 				View flatBox = findViewById(R.id.flatBox);
 				flatBox.setVisibility(View.VISIBLE);
 				addFlatBox.setVisibility(View.GONE);
 				int offset = findViewById(R.id.loginBox).getWidth();
 				TranslateAnimation anim = new TranslateAnimation( -offset, 0 , 0, 0 );
 				anim.setDuration(300);
 				anim.setStartOffset(0);
 				anim.setFillAfter( false );
 				anim.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {}
 					@Override
 					public void onAnimationRepeat(Animation animation) {}
 					@Override
 					public void onAnimationStart(Animation animation) {}
 				});
 				flatBox.startAnimation(anim);
 
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		View addFlatBox = findViewById(R.id.addFlatBox);
 		addFlatBox.startAnimation(anim);
 
 		View flatLogo = findViewById(R.id.splashLogo);
 
 		TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, -22 );
 		anim2.setDuration(300);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim2);
 	}
 
 	public void flatSelectionBack(){
 		screenPosition = 1;
 		int offset = findViewById(R.id.flatBox).getWidth();
 		TranslateAnimation anim = new TranslateAnimation( 0, offset , 0, 0 );
 		anim.setDuration(300);
 		anim.setStartOffset(0);
 		anim.setFillAfter( false );
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				View loginBox = findViewById(R.id.loginBox);
 				View registerBox = findViewById(R.id.registerBox);
 				View flatBox = findViewById(R.id.flatBox);
 				loginBox.setVisibility(View.VISIBLE);
 				registerBox.setVisibility(View.VISIBLE);
 				flatBox.setVisibility(View.GONE);
 				int offset = findViewById(R.id.loginBox).getWidth();
 				TranslateAnimation anim = new TranslateAnimation( -offset, 0 , 0, 0 );
 				anim.setDuration(300);
 				anim.setStartOffset(0);
 				anim.setFillAfter( false );
 				anim.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {}
 					@Override
 					public void onAnimationRepeat(Animation animation) {}
 					@Override
 					public void onAnimationStart(Animation animation) {}
 				});
 				loginBox.startAnimation(anim);
 				registerBox.startAnimation(anim);
 
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		View flatBox = findViewById(R.id.flatBox);
 		flatBox.startAnimation(anim);
 
 		View flatLogo = findViewById(R.id.splashLogo);
 
 		TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, -80 );
 		anim2.setDuration(300);
 		anim2.setFillAfter( false );
 		anim2.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				TranslateAnimation anim2 = new TranslateAnimation( 0, 0 , 0, 0 );
 				anim2.setDuration(1);
 				findViewById(R.id.splashLogo).startAnimation(anim2);
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) { }
 			@Override
 			public void onAnimationStart(Animation animation) { }
 		});
 		flatLogo.startAnimation(anim2);
 	}
 
 	public void flatSelected(Flat flat){
 		registrationFlatID = flat.getId()+"";
 
 		User newUser = new User();
 		newUser.setEmail(registrationEmail);
 		newUser.setFirst_name(registrationFirstName);
 		newUser.setLast_name(registrationLastName);
 		newUser.setPhone_number(registrationPhone);
 		newUser.setFlat_id(registrationFlatID);
 		newUser.setId(registrationID);
 
 		ListView flatsList = (ListView) findViewById(android.R.id.list);
 		flatsList.setVisibility(View.GONE);
 
 		View spinner = findViewById(R.id.addFlatSpinner);
 		spinner.setVisibility(View.VISIBLE);
 
 		Log.v("REG","Updating user's flat to existing flat: " + registrationFlatID);
 		new updateUser().execute(newUser);
 
 
 	}
 
 	public void createNewFlatPressed(){
 		EditText nameInput = (EditText)findViewById(R.id.flatNameInput);
 		EditText addressInput = (EditText)findViewById(R.id.address1Text);
 		EditText postCodeInput = (EditText)findViewById(R.id.postCodeTextBox);
 		if(nameInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter A Flat Name", Toast.LENGTH_SHORT).show();
 			return;
 		}else if(addressInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Enter Your Address Line 1", Toast.LENGTH_SHORT).show();
 			return;
 		}else if(postCodeInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Enter a Valid Postcode", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		Geocoder coder = new Geocoder(this, Locale.UK);
 
 		String strAddress = addressInput.getText().toString()+", "+postCodeInput.getText().toString();
 
 		List<Address> address;
 
 		GeoPoint p1 = null;
 
 		try {
 			address = coder.getFromLocationName(strAddress,5);
 			if (address == null) {
 				return;
 			}
 			Address location = address.get(0);
 			location.getLatitude();
 			location.getLongitude();
 
 			p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
 					(int) (location.getLongitude() * 1E6));
 		}catch(Exception e){
 		}
 		int noPostcode = 0;
 		if(p1 != null){
 			if(p1.getLatitudeE6() == 0.0f && p1.getLongitudeE6() == 0.0f){
 				coder = new Geocoder(this, Locale.UK);
 
 				strAddress = postCodeInput.getText().toString();
 
 				p1 = null;
 
 				try {
 					address = coder.getFromLocationName(strAddress,5);
 					if (address == null) {
 						return;
 					}
 					Address location = address.get(0);
 					location.getLatitude();
 					location.getLongitude();
 
 					p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
 							(int) (location.getLongitude() * 1E6));
 				}catch(Exception e){
 				}
 				if(p1 != null){
 					if(p1.getLatitudeE6() == 0.0f && p1.getLongitudeE6() == 0.0f){
 						Toast.makeText(getApplicationContext(), "Could not find flat Location, please chech address", Toast.LENGTH_SHORT).show();
 						noPostcode = 1;
 					}
 				}else{
 					Toast.makeText(getApplicationContext(), "Could not find flat Location, please chech address", Toast.LENGTH_SHORT).show();
 					noPostcode = 1;
 				}
 				
 			}
 		}else{
 			coder = new Geocoder(this, Locale.UK);
 
 			strAddress = postCodeInput.getText().toString();
 
 			p1 = null;
 
 			try {
 				address = coder.getFromLocationName(strAddress,5);
 				if (address == null) {
 					return;
 				}
 				Address location = address.get(0);
 				location.getLatitude();
 				location.getLongitude();
 
 				p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
 						(int) (location.getLongitude() * 1E6));
 			}catch(Exception e){
 			}
 			if(p1 != null){
 				if(p1.getLatitudeE6() == 0.0f && p1.getLongitudeE6() == 0.0f){
 					Toast.makeText(getApplicationContext(), "Could not find flat Location, please chech address", Toast.LENGTH_SHORT).show();
 					noPostcode = 1;
 				}
 			}else{
 				Toast.makeText(getApplicationContext(), "Could not find flat Location, please chech address", Toast.LENGTH_SHORT).show();
 				noPostcode = 1;
 			}
 		}
 		Flat newFlat = new Flat();
 		newFlat.setNickname(nameInput.getText().toString());
 		newFlat.setPostcode(postCodeInput.getText().toString());
 		if(noPostcode == 0){
			System.out.println(p1.getLatitudeE6() / 1e6);
			System.out.println(p1.getLongitudeE6()/ 1e6);
			newFlat.setGeocode_lat((float)(p1.getLatitudeE6()/ 1e6));
			newFlat.setGeocode_long((float)(p1.getLongitudeE6()/ 1e6));
 			View createButtons = findViewById(R.id.createButtons);
 			createButtons.setVisibility(View.GONE);
 			View createSpinner = findViewById(R.id.createSpinnerBox);
 			createSpinner.setVisibility(View.VISIBLE);
 			new registerNewFlat().execute(newFlat);
 		}
 	}
 
 	private void searchButtonPressed(){
 		String searchText = "";
 		EditText phoneInput = (EditText)findViewById(R.id.searchTextBox);
 		searchText = phoneInput.getText().toString();
 		Button search = (Button) findViewById(R.id.searchButton);
 		search.setVisibility(View.GONE);
 		ProgressBar spinner = (ProgressBar) findViewById(R.id.searchSpinner);
 		spinner.setVisibility(View.VISIBLE);
 		new searchFlats().execute(searchText);
 	}
 
 	private void updateFlatList(Flat[] flats){
 		Button search = (Button) findViewById(R.id.searchButton);
 		search.setVisibility(View.VISIBLE);
 		ProgressBar spinner = (ProgressBar) findViewById(R.id.searchSpinner);
 		spinner.setVisibility(View.GONE);
 		if(flats.length != 0){
 			for(Flat f : flats){
 				System.out.println(f.toHTTPString());
 			}
 			mAdapter = new SplashRowAdapter(this, android.R.id.list, flats);
 			mListView = (ListView)findViewById(android.R.id.list);
 			mListView.setAdapter(mAdapter);
 		}else{
 			Toast.makeText(getApplicationContext(), "No Flats Found", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private int registerCheck1(){
 		//Check that correct information has been added.
 		EditText emailInput = (EditText)findViewById(R.id.emailTbox);
 		EditText password1Input = (EditText)findViewById(R.id.passwordBox);
 		EditText password2Input = (EditText)findViewById(R.id.passwordBox2);
 		EditText firstInput = (EditText)findViewById(R.id.firstBox);
 		EditText lastInput = (EditText)findViewById(R.id.lastBox);
 		EditText phoneInput = (EditText)findViewById(R.id.phoneTextBox);
 		if(emailInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter An Email Address", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText()).matches()){
 			Toast.makeText(getApplicationContext(), "Enter a Valid Email Address", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(password1Input.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter A Password", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(password2Input.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Re-Enter The Password", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(password1Input.getText().length() < 6){
 			Toast.makeText(getApplicationContext(), "Password Is Too Short", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(!password1Input.getText().toString().equals(password2Input.getText().toString())){
 			Toast.makeText(getApplicationContext(), "Passwords Do Not Match", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(firstInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter First Name", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(lastInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter Last Name", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(phoneInput.getText().length() == 0){
 			Toast.makeText(getApplicationContext(), "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
 			return 0;
 		}else if(!phoneInput.getText().toString().matches("^[+]?[0-9]{10,13}$")){
 			Toast.makeText(getApplicationContext(), "Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
 			return 0;
 		}
 		/*else if(registrationFlatID == null){
 			Toast.makeText(getApplicationContext(), "Please Find Your Flat", Toast.LENGTH_SHORT).show();
 			return 0;
 		}*/
 
 		return 1;
 	}
 
 	@Override
 	public void onBackPressed() {
 		if(screenPosition == 0){
 			super.onBackPressed();
 		}else if(screenPosition == 1){
 			backButtonPressed();
 		}else if(screenPosition == 2){
 			super.onBackPressed();
 		}else if(screenPosition == 3){
 			newFlatBack();
 		}
 	}
 
 	private void serverLogin(String key){
 		int failed = 0;
 		if(key == "failed" || key == "connection" || key == "invalid"){
 			failed = 1; 
 		}
 
 		if(failed == 0){
 			File logFile = new File(FILENAME);
 			if (logFile.exists())
 			{
 				logFile.delete();
 			}
 			try
 			{
 				logFile.createNewFile();
 			} 
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 			try
 			{
 				//BufferedWriter for performance, true to set append to file flag
 				BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false)); 
 				buf.append(key);
 				buf.newLine();
 				buf.close();
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 			checkFlatOnLogIn();
 		}else{
 			Button login2 = (Button) findViewById(R.id.loginButton);
 			login2.setVisibility(View.VISIBLE);
 			Button register = (Button) findViewById(R.id.registerButton);
 			register.setVisibility(View.VISIBLE);
 			ProgressBar spinner = (ProgressBar) findViewById(R.id.loginSpinner);
 			spinner.setVisibility(View.GONE);
 			String failMessage;
 			if(key == "invalid"){
 				failMessage = "Incorrect login details... Please Try Again!";
 			}else if(key == "connection"){
 				failMessage = "Failed to connect to sever... Please Check Connection!";
 			}else{
 				failMessage = "An error as occurred... Please Try Again!";
 			}
 			Toast toast = Toast.makeText(getApplicationContext(), failMessage, Toast.LENGTH_SHORT);
 			toast.show();
 		}
 	}
 
 	private void startApp() {
 		Intent intent = new Intent(SplashActivity.this, FlatMate.class);
 		startActivity(intent);
 	}
 
 	private class searchFlats extends AsyncTask<String,Void,Flat[]>{
 
 		@Override
 		protected Flat[] doInBackground(String... search) {
 			Flat[] flats;
 			if(search[0].length() != 0){
 				flats = connection.searchFlats(search[0], "");
 			}else{
 				flats = connection.getFlats();
 			}
 			return flats;
 		}
 
 		protected void onPostExecute(Flat[] result) {
 			updateFlatList(result);
 		}
 
 	}
 
 	private class registerNewFlat extends AsyncTask<Flat,Void,Flat>{
 
 		@Override
 		protected Flat doInBackground(Flat... flat) {
 			return connection.addFlat(flat[0]);
 		}
 
 		protected void onPostExecute(Flat newFlat) {
 			if(newFlat.getNickname().equals("Error")){
 				newFlatFailed();
 			}else{
 				updateComplete();
 				//registrationNewFlatComplete(newFlat);
 			}
 			return;
 		}
 
 	}
 
 	private class updateUser extends AsyncTask<User,Void,Void>{
 
 		@Override
 		protected Void doInBackground(User... users) {
 			connection.updateUser(users[0]);
 			return null;
 		}
 
 		protected void onPostExecute(Void result) {
 			updateComplete();
 			return;
 		}
 
 	}
 
 	private class registerUser extends AsyncTask<User,Void,String>{
 
 		@Override
 		protected String doInBackground(User... users) {
 			return connection.registerUser(users[0],registrationPassword);
 		}
 
 		protected void onPostExecute(String result) {
 			registrationComplete(result);
 			return;
 		}
 
 	}
 
 	private class serverLogin extends AsyncTask<String,Void,String>{
 
 		@Override
 		protected String doInBackground(String... details) {
 			String key = connection.login(details[0], details[1], details[2]);
 			return key;
 		}
 
 		protected void onPostExecute(String result) {
 			serverLogin(result);
 		}
 
 	}
 }

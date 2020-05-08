 package com.t3hh4xx0r.haxchat.activities;

 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Patterns;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.parse.FindCallback;
 import com.parse.GetDataCallback;
 import com.parse.LogInCallback;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 import com.parse.SignUpCallback;
 import com.t3hh4xx0r.haxchat.DBAdapter;
 import com.t3hh4xx0r.haxchat.FileCache;
 import com.t3hh4xx0r.haxchat.R;
 import com.t3hh4xx0r.haxchat.parse.ParseHelper;
 import com.t3hh4xx0r.haxchat.preferences.PreferencesProvider;
 
 /**
  * Activity which displays a login screen to the user, offering registration as
  * well.
  */
 public class LoginActivity extends SherlockActivity {
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mUser;
 	private String mPassword;
 	private String mPossibleEmail;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	
 	String PARSE_INVALID_LOGIN = "invalid login credentials";
 	
 
 	ParseUser currentUser;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		ParseHelper.init(this);
 		if (ParseUser.getCurrentUser() != null) {
 			startChat(this);
 		}
 		// Set up the login form.
 		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
 		Account[] accounts = AccountManager.get(this).getAccounts();
 		for (Account account : accounts) {
 		    if (emailPattern.matcher(account.name).matches()) {
 		    	mPossibleEmail = account.name;
 		    }
 		}
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mPossibleEmail);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mPossibleEmail);
 		mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView textView, int id,
 					KeyEvent keyEvent) {
 				if (id == EditorInfo.IME_ACTION_NEXT) {
 					if (!mEmailView.getText().toString().contains("@")) {
 						mEmailView.setText(mEmailView.getText().toString() +"@gmail.com");
 					} 
 				}
 				return false;
 			}
 		});
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView.setOnFocusChangeListener(new OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean b) {
 				if (b) {
 					if (!mEmailView.getText().toString().contains("@")) {
 						mEmailView.setText(mEmailView.getText().toString() +"@gmail.com");
 					} 
 				}
 			}
 		});
 		mPasswordView
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView textView, int id,
 							KeyEvent keyEvent) {
 						if (id == R.id.login || id == EditorInfo.IME_NULL) {
 							attemptLogin();
 							return true;
 						} else if (id == R.id.password) {
 							mEmail = mEmailView.getText().toString();
 							if (!mEmail.contains("@")) {
 								mEmailView.setText(mEmail +"@gmail.com");
 							} 
 						}
 						return false;
 					}
 				});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.sign_in_button).setOnClickListener(
 				new View.OnClickListener() {
 					@Override
 					public void onClick(View view) {
 						attemptLogin();
 					}
 				});
 	}
 
 	private void startChat(Context c) {
 		Intent i = new Intent(c, ChatMainActivity.class);
 		startActivity(i);
 		finish();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.login_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Attempts to sign in or register the account specified by the login form.
 	 * If there are form errors (invalid email, missing fields, etc.), the
 	 * errors are presented and no actual login attempt is made.
 	 */
 	public void attemptLogin() {		
 		// Reset errors.
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mUser = mEmailView.getText().toString().replace("@gmail.com", "");
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		} else if (mPassword.length() < 4) {
 			mPasswordView.setError(getString(R.string.error_invalid_password));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid email address.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
 			focusView = mEmailView;
 			cancel = true;
 		} else if (!mEmail.contains("@")) {
 			mEmail = mEmail +"@gmail.com";
 		} else if (!mEmail.contains("@gmail.com")) {
 			mEmailView.setError(getString(R.string.error_invalid_email));
 			focusView = mEmailView;
 			cancel = true;
		} else if (containsPeriod() == true) {
			mEmailView.setError(getString(R.string.error_contains_period));
			focusView = mEmailView;
			cancel = true;
 		}
 
 		if (cancel) {
 			// There was an error; don't attempt login and focus the first
 			// form field with an error.
 			focusView.requestFocus();
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user login attempt.
 			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
 			showProgress(true);
 			doRegister();
 		}
 	}
	
	private boolean containsPeriod() {
		boolean b = false;
		String[] email = mEmail.split("@");
		if (email[0].contains(".")) {
			b = true;
		} else { 
			return b;
		}
		return b;		
	}
 
 	private void doRegister() {
 		ParseUser user = new ParseUser();
 		user.setUsername(mUser);
 		user.setPassword(mPassword);
 		user.setEmail(mEmail);
 		user.signUpInBackground(new SignUpCallback() {
 			public void done(ParseException e) {
 			    if (e == null) {
 			    	doLoggedIn();
 			    } else if (e.getMessage().equals("username "+mUser+" already taken")) {
 			    	doLogin();
 			    }  else {	
 			    	genericLoginWarning(e);
 			    }
 			}
 		});
 	}
 
 	protected void doLogin() {
 		ParseUser.logInInBackground(mUser, mPassword, new LogInCallback() {
     		@Override
 			public void done(ParseUser user, ParseException e) {
 				if (e == null) {
 					doLoggedIn();
 				} else if (e.getMessage().equals(PARSE_INVALID_LOGIN)) {
 			    	invalidLoginWarning();
 			    } else {	
 			    	genericLoginWarning(e);
 			    }
     		}			
     	});	
 	}
 
 	protected void invalidLoginWarning() {
     	showProgress(false);
     	mPasswordView.setText("");
     	Toast.makeText(getApplicationContext(), "Incorrect user/pass combo.", Toast.LENGTH_LONG).show();		
 	}
 
 	protected void genericLoginWarning(ParseException e) {
 		e.printStackTrace();
     	showProgress(false);
     	mPasswordView.setText("");
     	Toast.makeText(getApplicationContext(), "Unknown error, please try again.", Toast.LENGTH_LONG).show();		
 	}
 
 	protected void doLoggedIn() {
     	currentUser = ParseUser.getCurrentUser();
     	ParseHelper.refresh();
 		if (currentUser != null) {
 		  DBAdapter db = new DBAdapter(getApplicationContext());
 		  db.open(true);
 		  db.putFriendsList(currentUser.getUsername(), ParseHelper.getUsersFriends());
 		  db.close();
 		  ParseHelper.subscribePrivateChat(this);		  
 		  ParseHelper.isDeviceRegistered(this, new FindCallback() {
 			@Override
 			public void done(List<ParseObject> r, ParseException e) {
 				if (e == null) {
 					  if (r.size() == 0) {
 						  ParseObject device = new ParseObject("Device");
 						  device.put("UserId", currentUser.getObjectId());
 						  device.put("DeviceID", PreferencesProvider.id(getApplicationContext()));
 						  device.put("DeviceNick", mUser);
 						  PreferencesProvider.setNick(getApplicationContext(), mUser);
 						  device.put("DeviceModel", Build.MODEL);
 						  device.saveInBackground(new SaveCallback() {
 							@Override
 							public void done(ParseException e) {
 								if (e == null) {
 									showProgress(false);
 									startAvatarFetching();
 									startChat(getApplicationContext());
 								} else {
 									e.printStackTrace();
 								}
 							}			  
 						  });
 					  } else {
 						  showProgress(false);
 						  startAvatarFetching();
 						  startChat(getApplicationContext());
 					  }
 				  }
 				}
 		  });
 		} else {
 	    	mPasswordView.setText("");		
 	    	Toast.makeText(getApplicationContext(), "Unknown error, please try again.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private void startAvatarFetching() {
 		try {
 			ParseHelper.getAllUsers(new FindCallback() {			
 				@Override
 				public void done(List<ParseObject> r, ParseException e) {
 					if (e == null) {
 						final FileCache fCache = new FileCache(getApplicationContext());
 						for (int i=0;i<r.size();i++) {
 							final String uName = ((ParseUser) r.get(i)).getUsername();
 							Bitmap avi = fCache.getBitmap(((ParseUser) r.get(i)).getUsername(), true);
 							if (avi == null) {
 								ParseFile avatarBitmap = (ParseFile) r.get(i).get("Avatar");
 								if (avatarBitmap != null) {
 									avatarBitmap.getDataInBackground(new GetDataCallback() {
 										public void done(byte[] data, ParseException e) {
 											if (e == null) {
 												InputStream is = new ByteArrayInputStream(data);
 												Bitmap bmp = BitmapFactory.decodeStream(is);
 												fCache.putBitmap(bmp, uName, true);
 										    } else {
 										      e.printStackTrace();
 										    }
 										  }
 									});
 								}
 							} else {
 								fCache.putBitmap(avi, uName, true);
 							}
 						}
 					} else {}
 				}
 			});
 		} catch (ParseException e) {}								
 	}				
 
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mLoginFormView.setVisibility(View.VISIBLE);
 			mLoginFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 }

 package bcp.web.bcpgradebook;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import bcp.web.bcpgradebook.lib.DatabaseHandler;
 
 import de.keyboardsurfer.android.widget.crouton.Configuration;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LoginActivity extends Activity {
 	
 	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
 
 	private UserLoginTask mAuthTask = null;
 
 	// Values for email and password at the time of the login attempt.
 	private String mEmail;
 	private String mPassword;
 	private String mEncryptedPassword;
 
 	// UI references.
 	private EditText mEmailView;
 	private EditText mPasswordView;
 	private View mLoginFormView;
 	private View mLoginStatusView;
 	private TextView mLoginStatusMessageView;
 	
 	// Other values.
 	private boolean wantsRemember = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		setTitle("Sign in");
 		
 		// Check if logged in already.
 		SharedPreferences passPref = getSharedPreferences("password", MODE_PRIVATE);
 		String savedPassword = passPref.getString("password", "");
 		SharedPreferences userPref = getSharedPreferences("username", MODE_PRIVATE);
 		String savedUsername = userPref.getString("username", "");
 		if(savedPassword != null && savedPassword.length() > 0) {
 			Intent intent = new Intent(getBaseContext(), GradeViewActivity.class);
 			intent.putExtra("username", savedUsername);
 			intent.putExtra("encryptedPassword", savedPassword);
 			startActivity(intent);
 			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 			finish();
 			return; // exit onCreate
 		}
 		
 		// Not logged in already - delete the stored grades database.
 		new DatabaseHandler(this).deleteAll();
 		
 		// Set up the login form.
 		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
 		mEmailView = (EditText) findViewById(R.id.email);
 		mEmailView.setText(mEmail);
 
 		mPasswordView = (EditText) findViewById(R.id.password);
 		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView textView, int id,
 					KeyEvent keyEvent) {
 				if (id == R.id.login || id == EditorInfo.IME_NULL) {
 					attemptLogin();
 					return true;
 				}
 				return false;
 			}
 		});
 
 		mLoginFormView = findViewById(R.id.login_form);
 		mLoginStatusView = findViewById(R.id.login_status);
 		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
 
 		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				attemptLogin();
 			}
 		});
 		
 		if(!isOnline()) {
 			displayCrouton("NO INTERNET CONNECTION", 3000, Style.ALERT);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
     }
 	
 	public boolean isOnline() {
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
 	}
 
 	public void attemptLogin() {
 		if (mAuthTask != null) {
 			return;
 		}
 		
 		if(!isOnline()) {
 			displayCrouton("NO INTERNET CONNECTION", 3000, Style.ALERT);
 			return;
 		}
 
 		// Reset errors.
 		mEmailView.setError(null);
 		mPasswordView.setError(null);
 
 		// Store values at the time of the login attempt.
 		mEmail = mEmailView.getText().toString();
 		mPassword = mPasswordView.getText().toString();
 
 		boolean cancel = false;
 		View focusView = null;
 
 		// Check for a valid password.
 		if (TextUtils.isEmpty(mPassword)) {
 			mPasswordView.setError(getString(R.string.error_field_required));
 			focusView = mPasswordView;
 			cancel = true;
 		}
 
 		// Check for a valid user name.
 		if (TextUtils.isEmpty(mEmail)) {
 			mEmailView.setError(getString(R.string.error_field_required));
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
 			mAuthTask = new UserLoginTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 
 	/**
 	 * Shows the progress UI and hides the login form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
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
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 	
 	public void onCheckboxClicked(View view) {
 	    // Is the view now checked?
 	    boolean checked = ((CheckBox) view).isChecked();
 	    wantsRemember = checked;
 	}
 
 	/**
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			try {
 				return loadPasswordFromNetwork("http://didjem.com/bell_api/login.php?username=" + mEmail + "&password=" + mPassword);
 			} catch(Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 			// TODO: register the new account here.
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				if(wantsRemember) {
 					// Store encrypted password.
 					Editor editorPass = getSharedPreferences("password", MODE_PRIVATE).edit();
 					editorPass.putString("password", mEncryptedPassword);
 					editorPass.commit();
 				}
 				
				// Store username.
				Editor editorUser = getSharedPreferences("username", MODE_PRIVATE).edit();
				editorUser.putString("username", mEmail);
				editorUser.commit();
				
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
 				imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
 				Intent intent = new Intent(getBaseContext(), GradeViewActivity.class);
 				intent.putExtra("username", mEmail);
 				intent.putExtra("encryptedPassword", mEncryptedPassword);
 				startActivity(intent);
 				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 				finish();
 			} else {
 				mPasswordView.setError(getString(R.string.error_incorrect_password));
 				mPasswordView.requestFocus();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 	
 	private boolean loadPasswordFromNetwork(String urlString) throws IOException, JSONException {
 		InputStream stream = null;
 		String encryptPass = "";
 		String json = "";
 		try {
 			stream = downloadUrl(urlString);
 			json = convertStreamToString(stream);
 		} finally {
 			if(stream != null)
 				stream.close();
 		}
 		JSONObject result = new JSONObject(json);
 		JSONObject dat = result.getJSONObject("data");
 		if(result.getInt("error") != 0) { // error
 			return false;
 		}
 		encryptPass = dat.getString("encryptedPass");
 		mEncryptedPassword = encryptPass;
 		return true;
 	}
 	
 	private InputStream downloadUrl(String urlString) throws IOException {
 		URL url = new URL(urlString);
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 		conn.setReadTimeout(10000 /* milliseconds */);
 	    conn.setConnectTimeout(15000 /* milliseconds */);
 	    conn.setRequestMethod("GET");
 	    conn.setDoInput(true);
 	    // Starts the query
 	    conn.connect();
 	    return conn.getInputStream();
 	}
 	
 	private static String convertStreamToString(InputStream inputStream) throws IOException {
         if (inputStream != null) {
             Writer writer = new StringWriter();
 
             char[] buffer = new char[1024];
             try {
                 Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } finally {
                 inputStream.close();
             }
             return writer.toString();
         } else {
             return "";
         }
     }
 	
 	public void displayCrouton(String text, int timeMilli, Style style) {
 		Style style1;
 		if(style.equals(Style.ALERT)) {
 			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
 					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoRedLight).build();
 		} else if(style.equals(Style.CONFIRM)) {
 			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
 					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoGreenLight).build();
 		} else {
 			style1 = new Style.Builder().setHeight(LayoutParams.WRAP_CONTENT).setGravity(Gravity.CENTER_HORIZONTAL)
 					.setTextSize(15).setPaddingInPixels(15).setBackgroundColorValue(Style.holoBlueLight).build();
 		}
 		Configuration config = new Configuration.Builder().setDuration(timeMilli).build();
 		Crouton.makeText(LoginActivity.this, text, style1).setConfiguration(config).show();
 	}
 }

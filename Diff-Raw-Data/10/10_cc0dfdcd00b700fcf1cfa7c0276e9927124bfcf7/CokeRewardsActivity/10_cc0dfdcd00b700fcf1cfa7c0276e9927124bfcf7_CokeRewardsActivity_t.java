 package com.brianreber.cokerewards;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 /**
  * Main activity for the app
  * 
  * @author breber
  */
 public class CokeRewardsActivity extends Activity {
 	/**
 	 * SharedPreference name
 	 */
 	public static final String COKE_REWARDS = "CokeRewards";
 
 	/**
 	 * Email address preference key
 	 */
 	public static final String EMAIL_ADDRESS = "emailAddress";
 
 	/**
 	 * Password preference key
 	 */
 	public static final String PASSWORD = "password";
 
 	/**
 	 * Logged in state preference key
 	 */
 	public static final String LOGGED_IN = "loggedIn";
 
 	/**
 	 * Point count preference key
 	 */
 	public static final String POINTS = "points";
 
 	/**
 	 * Screen name preference key
 	 */
 	public static final String SCREEN_NAME = "screenName";
 
 	/**
 	 * Result of latest code entry
 	 */
 	public static final String ENTER_CODE_RESULT = "enterCodeResult";
 
 	/**
 	 * Result of latest code entry
 	 */
 	public static final String POINTS_EARNED_RESULT = "pointsEarnedResult";
 
 	/**
 	 * Result of messages
 	 */
 	public static final String MESSAGES = "messagesResult";
 
 	/**
 	 * Request code for starting our RegisterActivity
 	 */
 	private static final int REGISTER_REQUEST_CODE = 1000;
 
 	/**
 	 * Handler to use for the threads
 	 */
 	private static Handler handler = new Handler();
 
 	/**
 	 * Google Analytics tracker
 	 */
 	private GoogleAnalyticsTracker tracker;
 
 	/**
 	 * Dialog to display that we are submitting a code
 	 */
 	private ProgressDialog dlg;
 
 	/**
 	 * A Runnable that will update the UI with values stored
 	 * in SharedPreferences
 	 */
 	private Runnable updateUIRunnable = new Runnable() {
 		@Override
 		public void run() {
 			SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			TextView tv = (TextView) findViewById(R.id.numPoints);
 			tv.setText("Number of Points: " + prefs.getInt(POINTS, 0));
 
 			tv = (TextView) findViewById(R.id.screenName);
 			String name = ("".equals(prefs.getString(SCREEN_NAME, "")) ?
 					prefs.getString(EMAIL_ADDRESS, "") : prefs.getString(SCREEN_NAME, ""));
 
 			tv.setText("Welcome " + name + "!");
 		}
 	};
 
 	/**
 	 * A Runnable that will update the UI with values stored
 	 * in SharedPreferences
 	 */
 	private Runnable codeUpdateRunnable = new Runnable() {
 		@Override
 		public void run() {
 			SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			if (prefs.getBoolean(ENTER_CODE_RESULT, false)) {
 				EditText tv = (EditText) findViewById(R.id.code);
 				tv.setText("");
 
 				int numPointsEarned = prefs.getInt(POINTS_EARNED_RESULT, 0);
 				Toast.makeText(getApplicationContext(), "Code submitted successfully. " + numPointsEarned + " points earned!", Toast.LENGTH_SHORT).show();
 				tracker.trackEvent("SuccessfulCode", "SuccessfulCode", "SuccessfulCodeSubmission", 0);
 			} else {
 				String messages = prefs.getString(MESSAGES, "");
 				Toast.makeText(getApplicationContext(), "Code submission failed..." + messages, Toast.LENGTH_SHORT).show();
 
 				tracker.trackEvent("FailedCode", "FailedCode", "Code submission failed: " + messages, 0);
 			}
 
 			getNumberOfPoints();
 		}
 	};
 
 	/**
 	 * A Runnable that will show an error Toast
 	 */
 	private Runnable errorRunnable = new Runnable() {
 		@Override
 		public void run() {
 			if (dlg != null && dlg.isShowing()) {
 				dlg.dismiss();
 			}
 
 			Toast.makeText(CokeRewardsActivity.this, "An error has occurred. Please try again.", Toast.LENGTH_SHORT).show();
 		}
 	};
 
 
 	/**
 	 * Set up the basic UI elements
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		tracker = GoogleAnalyticsTracker.getInstance();
 
 		// Start the tracker in manual dispatch mode...
 		tracker.startNewSession("UA-3673402-16", 20, this);
 		tracker.setAnonymizeIp(true);
 
 		dlg = new ProgressDialog(this);
 		dlg.setCancelable(false);
 		dlg.setMessage(getResources().getText(R.string.submitting));
 
 		SharedPreferences prefs = getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 		TextView tv = (TextView) findViewById(R.id.numPoints);
 
 		try {
 			tv.setText("Number of Points: " + prefs.getInt(POINTS, 0));
 		} catch (ClassCastException ex) {
 			try {
 				tv.setText("Number of Points: " + prefs.getString(POINTS, ""));
 			} catch (Exception e) {	}
 			getNumberOfPoints();
 		}
 
 		tv = (TextView) findViewById(R.id.screenName);
 		String name = ("".equals(prefs.getString(SCREEN_NAME, "")) ?
 				prefs.getString(EMAIL_ADDRESS, "") : prefs.getString(SCREEN_NAME, ""));
 
 		tv.setText("Welcome " + name + "!");
 
 		Button submitCode = (Button) findViewById(R.id.submitCode);
 		submitCode.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText tv = (EditText) findViewById(R.id.code);
 				String code = tv.getText().toString();
 
 				code = code.replace(" ", "");
 				final String finalCode = code.toUpperCase();
 
 				if (finalCode.length() < 10) {
 					Toast.makeText(CokeRewardsActivity.this, "Code not long enough", Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				dlg.show();
 
 				new Thread(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							tracker.trackEvent("Button", "SubmitCode", "Submit Code button clicked", 0);
 							try {
 								Map<String, Object> result = CokeRewardsRequest.createCodeRequestBody(CokeRewardsActivity.this, finalCode);
 								parseResult(CokeRewardsActivity.this, codeUpdateRunnable, result);
 							} catch (Exception e) {
 								tracker.trackEvent("Exception", "ExceptionSecureSubmitCode", "Submit Code encountered an exception: " + e.getMessage(), 0);
 								e.printStackTrace();
 								Map<String, Object> result = CokeRewardsRequest.createCodeRequestBody(CokeRewardsActivity.this, finalCode);
 								parseResult(CokeRewardsActivity.this, codeUpdateRunnable, result);
 							}
 						} catch (Exception e) {
 							tracker.trackEvent("Exception", "ExceptionSubmitCode", "Submit Code encountered an exception: " + e.getMessage(), 0);
 							handler.post(errorRunnable);
 							e.printStackTrace();
 						}
 					}
 				}).start();
 			}
 		});
 
 		Button visitWebsite = (Button) findViewById(R.id.showWebsite);
 		visitWebsite.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse("http://m.mycokerewards.com/"));
 				startActivityForResult(i, REGISTER_REQUEST_CODE);
 			}
 		});
 
 		if (isLoggedIn()) {
 			getNumberOfPoints();
 		} else {
 			Intent register = new Intent(this, RegisterActivity.class);
 			startActivityForResult(register, REGISTER_REQUEST_CODE);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		// Stop the tracker when it is no longer needed.
 		tracker.stopSession();
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		if (isLoggedIn()) {
 			getNumberOfPoints();
 		}
 	}
 
 	/**
 	 * Get the number of points from the MyCokeRewards server
 	 */
 	private void getNumberOfPoints() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					tracker.trackEvent("CountFetch", "CountFetch", "Fetched number of points", 0);
 					try {
 						Map<String, Object> result = CokeRewardsRequest.createLoginRequestBody(CokeRewardsActivity.this);
 						parseResult(CokeRewardsActivity.this, updateUIRunnable, result);
 					} catch (Exception e) {
 						tracker.trackEvent("Exception", "ExceptionSecureGetNumPoints", "Get number of points encountered an exception: " + e.getMessage(), 0);
 						e.printStackTrace();
 						Map<String, Object> result = CokeRewardsRequest.createLoginRequestBody(CokeRewardsActivity.this);
 						parseResult(CokeRewardsActivity.this, updateUIRunnable, result);
 					}
 				} catch (Exception e) {
 					tracker.trackEvent("Exception", "ExceptionGetNumPoints", "Get number of points encountered an exception: " + e.getMessage(), 0);
 					handler.post(errorRunnable);
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Do we have a logged in user?
 	 * 
 	 * @return whether we have a valid email address, password, and have successfully
 	 * logged in with the most recent request
 	 */
 	private boolean isLoggedIn() {
 		return isLoggedIn(this);
 	}
 
 	/**
 	 * Do we have a logged in user?
 	 * 
 	 * @return whether we have a valid email address, password, and have successfully
 	 * logged in with the most recent request
 	 */
 	public static boolean isLoggedIn(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_READABLE);
 		return prefs.contains(EMAIL_ADDRESS) && prefs.contains(PASSWORD) && prefs.getBoolean(LOGGED_IN, false);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		int menuId = item.getItemId();
 
 		if (menuId == R.id.logout) {
 			SharedPreferences prefs = getSharedPreferences(CokeRewardsActivity.COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 			Editor edit = prefs.edit();
 
 			edit.clear();
 			edit.commit();
 
 			Intent register = new Intent(this, RegisterActivity.class);
 			startActivityForResult(register, REGISTER_REQUEST_CODE);
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menus, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (isLoggedIn()) {
 			getNumberOfPoints();
 		} else {
 			finish();
 		}
 	}
 
 	/**
 	 * Make a server request and update preferences accordingly.
 	 * 
 	 * @param postValue The value to POST to the server
 	 * @param runnable The Runnable to start after we are done processing
 	 * @throws IOException
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws ParserConfigurationException
 	 */
 	public static void parseResult(Context ctx, Runnable runnable, Map<String, Object> data) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
 		SharedPreferences prefs = ctx.getSharedPreferences(COKE_REWARDS, Context.MODE_WORLD_WRITEABLE);
 		Editor edit = prefs.edit();
 
 		if (data.containsKey("POINTS")) {
 			edit.putInt(POINTS, (Integer) data.get("POINTS"));
 		}
 
 		if (data.containsKey("LOGIN_RESULT")) {
 			edit.putBoolean(LOGGED_IN, Boolean.parseBoolean((String) data.get("LOGIN_RESULT")));
 		}
 
 		if (data.containsKey("SCREEN_NAME")) {
			String screenName = (String) data.get("SCREEN_NAME");

			if (screenName != null && !"".equals(screenName)) {
				edit.putString(SCREEN_NAME, screenName);
			}
 		}
 
 		if (data.containsKey("ENTER_CODE_RESULT")) {
 			edit.putBoolean(ENTER_CODE_RESULT, Boolean.parseBoolean((String) data.get("ENTER_CODE_RESULT")));
 		}
 
 		if (data.containsKey("POINTS_EARNED")) {
 			edit.putInt(POINTS_EARNED_RESULT, (Integer) data.get("POINTS_EARNED"));
 		}
 
 		if (data.containsKey("MESSAGES")) {
 			String messages = (String) ((Object[]) data.get("MESSAGES"))[0];
 			edit.putString(MESSAGES, messages);
 		}
 
 		edit.commit();
 
 		handler.post(runnable);
 	}
 }

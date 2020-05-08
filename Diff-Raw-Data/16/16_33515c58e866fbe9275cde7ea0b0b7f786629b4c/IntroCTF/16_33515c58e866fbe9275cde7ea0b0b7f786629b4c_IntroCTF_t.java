 package stetson.CTF;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import stetson.CTF.utils.CurrentUser;
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.Util;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.Facebook.DialogListener;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class IntroCTF extends Activity {
 
 	// Data members
 
 	private static AsyncFacebookRunner mAsyncRunner;
 	public static final String PREFS_NAME = "CTFuid";
 	private static final String TAG = "FACEBOOK CONNECT";
 	private static final String APP_ID = "215859728429846";
 	private static final String[] PERMS = new String[] {"publish_stream" };
 	private static Facebook facebook;
 	private SharedPreferences settings;
 //	private MediaPlayer mp;
 
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.intro);
 		
 		// set or get the Users id
 		setUID();
 		// Set music
 		// not used in intro
 //		mp = MediaPlayer.create(getBaseContext(), R.raw.town4);
 		
 		// facebook calls
 		facebook= new Facebook(APP_ID);
 		mAsyncRunner = new AsyncFacebookRunner(facebook);
 		
 		// start button and image listeners
 		buildListeners();
 		
 	}
 	
 	public void onResume()
 	{
 		super.onResume();
 		// start music
 //		mp.setLooping(true);
 //		mp.start();
 		CurrentUser.userLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE), JoinCTF.GPS_UPDATE_FREQUENCY_INTRO);	
 		
 	}
 	public void onDestroy()
 	{
 		super.onDestroy();
 		SharedPreferences.Editor editor = settings.edit();
 		// save UID temporarily
 		editor.putString("UID", CurrentUser.getUID());
 		// Commit the edits!
 		editor.commit();
 		// stop music and call GC
 		//		mp.stop();
 		//		mp.release();
 		
 	}
 
 	/**
 	 * Build button and image listeners
 	 * 
 	 */
 	public void buildListeners()
 	{		
 		
 		final Button guestButton = (Button) findViewById(R.id.intro_guest_button);
 		guestButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click
 				AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
 				alertDialog.setTitle("Enter your name: ");
 				final EditText input = new EditText(v.getContext()); 
 				input.setText(R.string.guest_button);
 				input.setOnClickListener(new OnClickListener(){
 					public void onClick(View arg0) {
 						input.setText("");
 					}
 				});
 				alertDialog.setView(input);
 				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						String text = input.getText().toString();
 						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 						mgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
 						CurrentUser.setName(text);
 						gpsLock();
 						return;
 					} }); 
 				alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						return;
 					}}); 
 				alertDialog.show();
 			}
 		});
 		final Button facebookButton = (Button) findViewById(R.id.intro_facebook_button);
 		facebookButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Initialize facebook on click
 				intFacebook();	
 			}
 		});
 	}
 	/**
 	 * authorize facebook, login...
 	 * 
 	 * 
 	 */
 	public void intFacebook()
 	{
 
 		facebook.authorize(this,PERMS, new LoginDialogListener() {
 			public void onComplete(Bundle values) {             }
 			public void onFacebookError(FacebookError error) {}
 			public void onError(DialogError e) {}
 			public void onCancel() {}
 		});
 	}
 	/**
 	 * Method checks to see if UserID is stored. If it isn't call generate user id.
 	 * If it is set UserID to it.
 	 * 
 	 */
 	public void setUID()
 	{
 		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
 		String uid = settings.getString("UID", "0");
 		if(uid.equals("0") || uid.equals(""))
 		{
 			CurrentUser.genUID();
 		}
 		else
 		{
 			CurrentUser.setUID(uid);
 		}
 	}
 	/**
 	 * Method forces user to login and wait for GPS signal
 	 * 
 	 * 
 	 * 
 	 */
 	public void gpsLock()
 	{
 			new loadingDialog().execute();
 	}
 	
 	/**
 	 * Allows messages to be posted to the Current User's wall
 	 * @param String msg - message to be posted
 	 */
 	public static boolean postToWall(String msg)
 	{
 		if(facebook.isSessionValid())
 		{
 			Bundle parameters = new Bundle();
 			parameters.putString("message",msg);
 			try {
 				facebook.request("me/feed", parameters,"POST");
 					
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			//      Other way to implement wall posts
 			//		mAsyncRunner.request("me/feed", params, "POST", new TestRequestListener());
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	 * facebook helper method 
 	 * 
 	 */
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		facebook.authorizeCallback(requestCode, resultCode, data);
 		mAsyncRunner.request("me", new IDRequestListener());
 		gpsLock();
 	}
 	/**
 	 * Facebook
 	 * 
 	 * Listener for name request, sets CurrentUser name
 	 * 
 	 */
 	private class IDRequestListener implements RequestListener {
 		public void onComplete(String response, Object state) {
 			try {
 				// process the response here: executed in background thread
 				Log.d(TAG, "Response: " + response.toString());
 				JSONObject json = Util.parseJson(response);
 				final String name = json.getString("name");	
 				
 				// then post the processed result back to the UI thread
 				// if we do not do this, an runtime exception will be generated
 				// e.g. "CalledFromWrongThreadException: Only the original
 				// thread that created a view hierarchy can touch its views."
 				runOnUiThread(new Runnable() {
 					public void run() {
 					CurrentUser.setName(name);
 				}
 			});	
 						
 			} catch (JSONException e) {
 				Log.w(TAG, "JSON Error in response");
 			} catch (FacebookError e) {
 				Log.w(TAG, "Facebook Error: " + e.getMessage());
 			}
 		}
 		public void onIOException(IOException e, Object state) {}
 		public void onFileNotFoundException(FileNotFoundException e,
 				Object state) {}
 		public void onMalformedURLException(MalformedURLException e,
 				Object state) {}
 		public void onFacebookError(FacebookError e, Object state) {}
 	}
 	/**
 	 * Facebook login Dialog listener
 	 * 
 	 */
 	private class LoginDialogListener implements DialogListener {
 		/**
 		 * Called when the dialog has completed successfully
 		 */
 		public void onComplete(Bundle values) {
 			// Process onComplete
 			// Dispatch on its own thread
 //			runOnUiThread(new Runnable() {
 //				public void run() {
 //				}
 //			});
 		}
 		public void onFacebookError(FacebookError error) {}
 		public void onError(DialogError error) {}
 		public void onCancel() {}
 	}
 
 	/**
 	 * Loading dialog for GPS signal
 	 *
 	 */
 	 private class loadingDialog extends AsyncTask<Void,String, Void> {
 		 ProgressDialog progressDialog;
 		 Context mContext = IntroCTF.this;
 
 		 protected void onPreExecute()
 		 {
 			 progressDialog = new ProgressDialog(mContext);
 			 progressDialog.setTitle("Hello " + CurrentUser.getName());
 			 progressDialog.setMessage(IntroCTF.this.getString(R.string.please_wait));
 			 progressDialog.setIndeterminate(true);
 			 progressDialog.show();
 	     }
 		 protected void onPostExecute(Void result)
 	     {
 	         progressDialog.dismiss();
 	         setResult(RESULT_OK);
 	         finish();
 	     }
 		 /**
 			 * Runs every time publicProgress() is called.
 			 * Clears the gamesGroup view and adds a message with the progress text.
 			 */
 		 protected void onProgressUpdate(String... progress) {
 			 progressDialog.setMessage(IntroCTF.this.getString(R.string.please_wait) + progress[0]);
 			 if(progress[1]!= null)
 			 {
 				 progressDialog.setTitle(progress[1]);
 				 progress[1] = null;
 			 }
 		 }
 		 protected Void doInBackground(Void... params)
 		 {
 
 			 this.publishProgress(IntroCTF.this.getString(R.string.wait_for_name),null);
 			 while(CurrentUser.getName().equals("")) {
 				 try {
 					 Thread.sleep(800);
 				 } catch (InterruptedException e) {
 					 Log.e(TAG, "Can't sleep :(", e);
 				 }
 			 }
 			 
 			 this.publishProgress(IntroCTF.this.getString(R.string.wait_for_signal),"Hello " + CurrentUser.getName());
 			 while(!CurrentUser.hasLocation()) {
 				 try {
 					 Thread.sleep(800);
 				 } catch (InterruptedException e) {
 					 Log.e(TAG, "Can't sleep :(", e);
 				 }
 			 }
 			 return null;
 		 }
 	 }
 }

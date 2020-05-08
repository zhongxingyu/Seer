 package stetson.CTF;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
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
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.text.InputType;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Title extends Activity {
 
 	// Data members
 
 	private static AsyncFacebookRunner mAsyncRunner;
 	private static final String TAG = "FACEBOOK CONNECT";
 	private static final String APP_ID = "215859728429846";
 	private static final String[] PERMS = new String[] {"publish_stream" };
 	private static Facebook facebook;
 	private ImageView image;
 
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		// load image
 		image = (ImageView) findViewById(R.id.loading_image);
 		// set loading message
 		setLoadingMessage("Please enter your name\nor Login through Facebook", true);
 
 		
 		facebook= new Facebook(APP_ID);
 		mAsyncRunner = new AsyncFacebookRunner(facebook);
 		// start button and image listeners
 
 		buildListeners();
 		
 	}
 	// button and image listeners
 	public void buildListeners()
 	{		
 		image.setOnTouchListener(new OnTouchListener() {
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				return false;
 			}
 		});
 		final Button guestButton = (Button) findViewById(R.id.guest);
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
//			    	  input.setInputType(InputType.TYPE_NULL);
						InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						 mgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
						// only will trigger it if no physical keyboard is open
//						mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
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
 		final Button facebookButton = (Button) findViewById(R.id.facebook);
 		facebookButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on click	
 				intFacebook();	
 			}
 		});
 	}
 	// authorize facebook, login...
 	public void intFacebook()
 	{
 
 		facebook.authorize(this,PERMS, new LoginDialogListener() {
 			public void onComplete(Bundle values) {             }
 			public void onFacebookError(FacebookError error) {}
 			public void onError(DialogError e) {}
 			public void onCancel() {}
 		});
 
 	}
 	// Allows messages to be posted to the Current User's wall
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
 
 	// facebook helper method 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		facebook.authorizeCallback(requestCode, resultCode, data);
 		mAsyncRunner.request("me", new IDRequestListener());
 //		new loadingDialog().execute();
 //		gpsLock();
 	}
 	// Listener for name request, sets CurrentUser name
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
 					gpsLock();
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
 	// Dialog for login,authorize
 	private class LoginDialogListener implements DialogListener {
 		/**
 		 * Called when the dialog has completed successfully
 		 */
 		public void onComplete(Bundle values) {
 			// Process onComplete
 			// Dispatch on its own thread
 			runOnUiThread(new Runnable() {
 				public void run() {
 
 
 				}
 			});
 		}
 		public void onFacebookError(FacebookError error) {}
 		public void onError(DialogError error) {}
 		public void onCancel() {}
 	}
 	public void onResume()
 	{
 		super.onResume();
 		// start GPS
 		CurrentUser.userLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_INTRO);	
 	}
 	public void onBackPressed()
 	{	
 	}
 	public void gpsLock()
 	{
 		if(!CurrentUser.getName().equals(""))
 		{
 			new loadingDialog().execute();
 		}
 		else
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Please enter a name.");
 			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                
 		           }
 		       });
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		
 	}
 	public void setLoadingMessage(String msg, boolean visible)
 	{
 //		TextView loading = (TextView) findViewById(R.id.loading);
 //		if(visible)
 //		{
 //			loading.setVisibility(TextView.VISIBLE);
 //		}
 //		loading.setText(msg);
 	}
 	 private class loadingDialog extends AsyncTask<Void,Void, Void> {
 		 ProgressDialog progressDialog;
 		 Context mContext = Title.this;
 
 		 protected void onPreExecute()
 		 {
 			 progressDialog = new ProgressDialog(mContext);
 			 progressDialog.setTitle("Hello " + CurrentUser.getName());
 			 progressDialog.setMessage("Acquiring GPS signal please wait...");
 			 progressDialog.setIndeterminate(true);
 			 progressDialog.show();
 
 	     }
 		 protected void onPostExecute(Void result)
 	     {
 	         progressDialog.hide();
 	         finish();
 	     }
 		
 		 protected Void doInBackground(Void... params)
 		 {
 
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

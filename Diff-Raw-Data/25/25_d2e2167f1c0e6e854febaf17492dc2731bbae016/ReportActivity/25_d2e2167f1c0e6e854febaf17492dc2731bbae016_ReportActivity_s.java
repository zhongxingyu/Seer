 package com.frontcast;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.ScaleAnimation;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.facebook.android.FacebookError;
 import com.facebook.android.R;
 import com.frontcast.SessionEvents.AuthListener;
 import com.frontcast.SessionEvents.LogoutListener;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.json.JsonHttpContent;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 
 public class ReportActivity extends Activity implements LocationListener {
 	
 	
 	// UI components
 	private TextView loginPromptText;
 	private TextView promptText;
 	private Button sunnyButton;
 	private Button cloudyButton;
 	private Button rainyButton;
 	
 	private SeekBar levelSeekbar;
 	private Button reportButton;
 	private Button cancelButton;
 	
 	private ImageView weatherSelectedImage;
 	private TextView descriptionText;
 	
 	// weather types
 	private int weatherSelected = NONE;
 	
 	public static final int NONE = -1;
 	public static final int SUNNY = 1;
 	public static final int CLOUDY = 2;
 	public static final int RAINY = 3;
 	
 	// location services
 	private LocationManager lMgr;
 	private String best;
 	
 	// app engine services
 	public static final String SERVER_URL = "http://frontcast-server.appspot.com/rpc";
 	private static final HttpTransport transport = new ApacheHttpTransport();
 	
 	// facebook services
 	private static final String APP_ID = "260058654093775";
 	private LoginButton mLoginButton;
     private TextView mText;
     private ImageView mUserPic;
     private Handler mHandler;
     ProgressDialog dialog;
     final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
     String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins"}; 
     
     private static final int MENU_LOGOUT = Menu.FIRST;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.report);
 		mHandler = new Handler();
 		findViews();
 		setupFacebook();
 		setListeners();
 	}
 	
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_LOGOUT, 0, "Logout");
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case MENU_LOGOUT:
 			Toast.makeText(ReportActivity.this, "logout!", Toast.LENGTH_SHORT).show();
 			mLoginButton.logout();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 
 
 	private void findViews() {
 		mLoginButton = (LoginButton) findViewById(R.id.login);
 		loginPromptText = (TextView) findViewById(R.id.loginPrompt_text);
 		promptText = (TextView) findViewById(R.id.reportPrompt_text);
 		sunnyButton = (Button) findViewById(R.id.sunny_button);
 		cloudyButton = (Button) findViewById(R.id.cloudy_button);
 		rainyButton = (Button) findViewById(R.id.rainy_button);
 		
 		weatherSelectedImage = (ImageView) findViewById(R.id.weather_image);
 		descriptionText = (TextView) findViewById(R.id.descrpition_text);
 		levelSeekbar = (SeekBar) findViewById(R.id.level_seekbar);
 		reportButton = (Button) findViewById(R.id.report_button);
 		cancelButton = (Button) findViewById(R.id.cancel_button);
 	}
 	
 	private void setupFacebook() {
 		Utility.mFacebook = new Facebook(APP_ID);
         Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
         
 		SessionStore.restore(Utility.mFacebook, this);
         SessionEvents.addAuthListener(new FbAPIsAuthListener());
         SessionEvents.addLogoutListener(new FbAPIsLogoutListener());
         
 		mLoginButton.init(this, AUTHORIZE_ACTIVITY_RESULT_CODE, Utility.mFacebook, permissions);
 		
 		if (Utility.mFacebook.isSessionValid()) {
 			showLoginViews(View.GONE);
             requestUserData();
             showViews();
         }
 	}
 	
 	private void setListeners() {
 		OnClickListener weatherOnClickListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (v instanceof Button) {
 					Button sender = (Button) v;
 					Log.d("report weather", sender.getText().toString());
 					if (sender == sunnyButton) {
 						weatherSelected = SUNNY;
 						weatherSelectedImage.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
 					} else if (sender == cloudyButton) {
 						weatherSelected = CLOUDY;
 						weatherSelectedImage.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
 					} else {
 						weatherSelected = RAINY;
 						weatherSelectedImage.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
 					}
 					levelSeekbar.setProgress(40);
 					showViews();
 				}
 			}
 		};
 		
 		sunnyButton.setOnClickListener(weatherOnClickListener);
 		cloudyButton.setOnClickListener(weatherOnClickListener);
 		rainyButton.setOnClickListener(weatherOnClickListener);
 		
 		reportButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// update to Frontcast server
 				new ReportFrontcastTask().execute();
 				
 				// post to Facebook
 				Bundle params = new Bundle();
	            params.putString("message", "現在外頭的天氣約莫是" + getDescription());
 	            //Utility.mAsyncRunner.request("me/feed", params, "POST", new WallPostListener(), null);
 				
 	            // Move on to QueryActivity
 				Intent intent = new Intent();
 				intent.setClass(ReportActivity.this, QueryActivity.class);
 				Bundle bundle = new Bundle();
 				bundle.putInt("type", weatherSelected);
 				bundle.putInt("level", levelSeekbar.getProgress());
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 		});
 		
 		cancelButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				AnimationSet animation = new AnimationSet(true);
 				animation.setInterpolator(new AccelerateDecelerateInterpolator());
 				animation.setFillAfter(true);
 				animation.setFillEnabled(true);
 				TranslateAnimation translateAnimation = null;
 				float scale = 1.0f/1.5f;
 				ScaleAnimation scaleAnimation = new ScaleAnimation(1, scale, 1, scale);
 				
 				weatherSelected = NONE;
 				showViews();
 			}
 		});
 		levelSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				descriptionText.setText(getDescription());
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 			
 		});
 	}
 	
     public class WallPostListener extends BaseRequestListener {
         @Override
         public void onComplete(final String response, final Object state) {
             //oast.makeText(ReportActivity.this, "API Response: " + response, Toast.LENGTH_SHORT).show();
         }
 
         public void onFacebookError(FacebookError error) {
             //Toast.makeText(ReportActivity.this, "Check-in Error: " + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
         }
     }
 	
 	private class ReportFrontcastTask extends AsyncTask<Void, Void, Void> {
     	private ProgressDialog Dialog = new ProgressDialog(ReportActivity.this);
     	String fb_id;
     	double lat;
     	double lng;
     	String type;
     	int level;
     	
     	@Override
     	protected void onPreExecute() {
     		super.onPreExecute();
     		fb_id = Utility.userUID;
     		
     		switch(weatherSelected) {
     		case SUNNY:
     			type = "sunny"; break;
     		case CLOUDY:
     			type = "cloudy"; break;
     		case RAINY:
     			type = "rainy"; break;
     		}
     		
     		Location location = lMgr.getLastKnownLocation(best);
 			if (location != null) {
 				lat = location.getLatitude();
 				lng = location.getLongitude();
 			} else {
 				// handle no location found error
 			}
 			
     		level = levelSeekbar.getProgress();
     		Dialog.setMessage("Reporting your Frontcast...");
     		Dialog.show();
     	}
     	
     	
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			String[] data = {"ReportFrontcast", fb_id, String.valueOf(lat), String.valueOf(lng), type, String.valueOf(level)};
 			JsonHttpContent json = new JsonHttpContent(new JacksonFactory(), data);
 			
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request;
 			try {
 				request = httpRequestFactory.buildPostRequest(new GenericUrl(SERVER_URL), json);
 				String result = request.execute().parseAsString();
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
     	
 		@Override
 		protected void onPostExecute(Void unused) {
 			Dialog.dismiss();
 			Toast.makeText(ReportActivity.this, "done!", Toast.LENGTH_LONG).show();
 		}
     }
 	
 	public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
 		   
 		return transport.createRequestFactory(new HttpRequestInitializer() {
 			public void initialize(HttpRequest request) {
 			    JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
 				request.addParser(parser);
 			}
 		});
 	}
 	
 	private void showViews() {
 		Animation fadeIn = new AlphaAnimation(0, 1);
 		fadeIn.setInterpolator(new DecelerateInterpolator());
 		fadeIn.setDuration(700);
 		
 		Animation fadeOut = new AlphaAnimation(1, 0);
 		fadeOut.setInterpolator(new AccelerateInterpolator());
 		fadeOut.setDuration(150);
 		
 		if (weatherSelected == NONE) {
 			weatherSelectedImage.startAnimation(fadeOut); weatherSelectedImage.setVisibility(View.GONE);
 			descriptionText.startAnimation(fadeOut); descriptionText.setVisibility(View.GONE);
 			levelSeekbar.startAnimation(fadeOut); levelSeekbar.setVisibility(View.GONE);
 			cancelButton.startAnimation(fadeOut); cancelButton.setVisibility(View.GONE);
 			reportButton.startAnimation(fadeOut); reportButton.setVisibility(View.GONE);
 			promptText.setVisibility(View.VISIBLE); promptText.startAnimation(fadeIn);
 			sunnyButton.setVisibility(View.VISIBLE); sunnyButton.startAnimation(fadeIn);
 			cloudyButton.setVisibility(View.VISIBLE); cloudyButton.startAnimation(fadeIn);
 			rainyButton.setVisibility(View.VISIBLE); rainyButton.startAnimation(fadeIn);
 		} else {
 			weatherSelectedImage.setVisibility(View.VISIBLE); weatherSelectedImage.startAnimation(fadeIn);
 			descriptionText.setVisibility(View.VISIBLE); descriptionText.startAnimation(fadeIn);
 			levelSeekbar.setVisibility(View.VISIBLE); levelSeekbar.startAnimation(fadeIn);
 			cancelButton.setVisibility(View.VISIBLE); cancelButton.startAnimation(fadeIn);
 			reportButton.setVisibility(View.VISIBLE); reportButton.startAnimation(fadeIn);
 			promptText.startAnimation(fadeOut); promptText.setVisibility(View.GONE);
 			sunnyButton.startAnimation(fadeOut); sunnyButton.setVisibility(View.GONE);
 			cloudyButton.startAnimation(fadeOut); cloudyButton.setVisibility(View.GONE);
 			rainyButton.startAnimation(fadeOut); rainyButton.setVisibility(View.GONE);
 		}
 	}
 	
 	/*
      * The Callback for notifying the application when authorization succeeds or
      * fails.
      */
     public class FbAPIsAuthListener implements AuthListener {
 
         
         public void onAuthSucceed() {
         	Log.d("facebook_auth", "auth succeed");
         	showLoginViews(View.GONE);
         	showViews();
         }
 
         public void onAuthFail(String error) {
         }
     }
 
     /*
      * The Callback for notifying the application when log out starts and
      * finishes.
      */
     public class FbAPIsLogoutListener implements LogoutListener {
         
         public void onLogoutBegin() {
         	Log.d("facebook_logout", "logout begin");
         }
 
         
         public void onLogoutFinish() {
         	Log.d("facebook_logout", "logout finish");
         	showLoginViews(View.VISIBLE);
         	weatherSelected = NONE;
 			weatherSelectedImage.setVisibility(View.GONE);
 			descriptionText.setVisibility(View.GONE);
 			levelSeekbar.setVisibility(View.GONE);
 			cancelButton.setVisibility(View.GONE);
 			reportButton.setVisibility(View.GONE);
 			promptText.setVisibility(View.GONE);
 			sunnyButton.setVisibility(View.GONE);
 			cloudyButton.setVisibility(View.GONE);
 			rainyButton.setVisibility(View.GONE);
         }
     }
     
     public void requestUserData() {
         Bundle params = new Bundle();
         params.putString("fields", "name, picture");
         //get user id, name, picture...
         Utility.mAsyncRunner.request("me", params, new UserRequestListener());
     }
     
     /*
      * Callback for fetching current user's name, picture, uid.
      */
     //when user login successfully...
     public class UserRequestListener extends BaseRequestListener {
 
         
         public void onComplete(final String response, final Object state) {
             JSONObject jsonObject;
             try {
                 jsonObject = new JSONObject(response);
                 //we get user name and id, picture from jsonObject.
                 final String picURL = jsonObject.getString("picture");
                 final String name = jsonObject.getString("name");
                 Utility.userUID = jsonObject.getString("id");
                 //renew the display.
                 mHandler.post(new Runnable() {
                     public void run() {
                         //mUserPic.setImageBitmap(Utility.getBitmap(picURL));
                     }
                 });
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
     }
     
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (checkLocationService()) {
 			lMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 			Criteria criteria = new Criteria();
 			best = lMgr.getBestProvider(criteria, true); Log.d("location", best);
 			lMgr.requestLocationUpdates(best, 60000, 1, this);
 			new GetLocationNameTask().execute();
 		} else {
 			new AlertDialog.Builder(ReportActivity.this)
 				.setTitle("No Location Service!")
 				.setMessage("Please enable location service beore using th App.")
 				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 					}
 				}).show();
 		}
 	}
 	
 	private boolean checkLocationService() {
 		LocationManager status = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		return (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		lMgr.removeUpdates(this);
 	}
 	
 	private class GetLocationNameTask extends AsyncTask<Void, Void, Void> {
     	private ProgressDialog Dialog = new ProgressDialog(ReportActivity.this);
     	double lat;
     	double lng;
     	
     	@Override
     	protected void onPreExecute() {
     		super.onPreExecute();
     		
     		Location location = lMgr.getLastKnownLocation(best);
 			if (location != null) {
 				lat = location.getLatitude();
 				lng = location.getLongitude();
 			} else {
 				// handle no location found error
 			}
			
     		Dialog.setMessage("Fetching your location...");
     		Dialog.show();
     	}
     	
     	
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			String[] data = {"GetLocationName", String.valueOf(lat), String.valueOf(lng)};
 			JsonHttpContent json = new JsonHttpContent(new JacksonFactory(), data);
 			
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request;
 			try {
 				request = httpRequestFactory.buildPostRequest(new GenericUrl(SERVER_URL), json);
				String result = request.execute().parseAsString();
 				Log.d("query_locationname", result);
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
     	
 		@Override
 		protected void onPostExecute(Void unused) {
 			Dialog.dismiss();
 			Toast.makeText(ReportActivity.this, "done!", Toast.LENGTH_LONG).show();
 		}
     }
 	
 	private String getDescription() {
 		int progress = levelSeekbar.getProgress();
 		switch(weatherSelected) {
 		case SUNNY:
 			if (progress < 25) {
 				return (getString(R.string.sunny_1));
 			} else if (progress < 50) {
 				return (getString(R.string.sunny_2));
 			} else if (progress < 75) {
 				return (getString(R.string.sunny_3));
 			} else {
 				return (getString(R.string.sunny_4));
 			}
 		case CLOUDY:
 			if (progress < 25) {
 				return (getString(R.string.cloudy_1));
 			} else if (progress < 50) {
 				return (getString(R.string.cloudy_2));
 			} else if (progress < 75) {
 				return (getString(R.string.cloudy_3));
 			} else {
 				return (getString(R.string.cloudy_4));
 			}
 		case RAINY:
 			if (progress < 25) {
 				return (getString(R.string.rainy_1));
 			} else if (progress < 50) {
 				return (getString(R.string.rainy_2));
 			} else if (progress < 75) {
 				return (getString(R.string.rainy_3));
 			} else {
 				return (getString(R.string.rainy_4));
 			}
 		}
 		return "";
 	}
 	
 	private void showLoginViews(int visibility) {
 		mLoginButton.setVisibility(visibility);
 		loginPromptText.setVisibility(visibility);
 	}
 }

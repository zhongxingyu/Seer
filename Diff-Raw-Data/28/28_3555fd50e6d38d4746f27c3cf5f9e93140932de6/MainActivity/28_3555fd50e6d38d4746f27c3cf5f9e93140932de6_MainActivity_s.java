 package org.societies.thirdpartyservices.crowdtasking;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.provider.CalendarContract.Events;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.webkit.ConsoleMessage;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.JavascriptInterface;
 import android.webkit.JsResult;
 import android.webkit.WebBackForwardList;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.societies.android.api.cis.directory.ICisDirectory;
 import org.societies.android.api.cis.management.ICisManager;
 import org.societies.android.api.contentproviders.CSSContentProvider;
 import org.societies.api.schema.cis.community.Community;
 import org.societies.api.schema.cis.directory.CisAdvertisementRecord;
 import org.societies.integration.model.SocietiesUser;
 import org.societies.integration.service.CisDirectoryClient;
 import org.societies.integration.service.CommunityManagementClient;
 import org.societies.integration.service.ContextClient;
 import org.societies.integration.service.SocietiesEventsClient;
 import org.societies.security.digsig.api.Sign;
 import org.societies.thirdpartyservices.crowdtasking.tools.SocketClient;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import si.setcce.societies.android.rest.RestTask;
 import si.setcce.societies.crowdtasking.api.RESTful.json.CommunityJS;
import si.setcce.societies.gcm.GcmIntentService;
 
 @SuppressLint("SimpleDateFormat")
 public class MainActivity extends Activity implements SensorEventListener, NfcAdapter.CreateNdefMessageCallback {
 	//private static final String TEST_ACTION = "si.setcce.societies.android.rest.TEST";
     public static final String CHECK_IN_OUT = "si.setcce.societies.android.rest.CHECK_IN_OUT";
     public static final String GET_LOCATION_ACTION = "si.setcce.societies.android.rest.GET_LOCATION";
     private static final String GET_USER = "si.setcce.societies.android.rest.GET_USER";
 	private static final String LOG_EVENT = "si.setcce.societies.android.rest.LOG_EVENT";
 	private static final String LOGIN_USER = "si.setcce.societies.android.rest.LOGIN_USER";
 	private static final String C4CSS_INTENT = "si.setcce.societies.android.rest.C4CSS";
 	private static final String GET_MEETING_ACTION = "si.setcce.societies.android.rest.MEETING";
 	private static final String SET_MEETING_ACTION = "si.setcce.societies.android.rest.SET_MEETING_ID";
 	private static final String TAKE_CONTROL = "si.setcce.societies.android.rest.remote.TAKE_CONTROL";
 	private static final String GCM_REGISTER = "si.setcce.societies.android.rest.GCM";
 	public static final String SET_FOCUS = "si.setcce.societies.android.activity.FOCUS";
 
     private static final String SCHEME ="http";
     private static String SCOPE="";
 //    public static final String DOMAIN = "crowdtaskingtest.appspot.com";
 //    public static final String DOMAIN = "simonix";
 public static final String DOMAIN = "crowdtasking.appspot.com";
    //        public static final String DOMAIN = "192.168.1.30";
     private static final String PORT = "";
 //    private static final String PORT = ":8888";
     public static final String APPLICATION_URL = SCHEME +"://" + DOMAIN + PORT;
     private static final String HOME_URL = APPLICATION_URL + "/menu";
     private static final String HOME_URL_AFTER_REGISTER = APPLICATION_URL + "/register#/menu";
     private static final String MEETING_URL = APPLICATION_URL + "/android/meeting/";
     private static final String C4CSS_API_URL = APPLICATION_URL + "/rest/community/4user";
     private static final String PD_TAKE_CONTROL = APPLICATION_URL + "/rest/remote/takeControl";
     private static final String GCM_REGISTER_URL = APPLICATION_URL + "/rest/gcm";
     public static final String SET_LOCATION_URL = APPLICATION_URL + "/rest/users/location";
     //    private static final String GET_USER_REST_API_URL = APPLICATION_URL + "/rest/users/me";
 	private static final String MEETING_REST_API_URL = APPLICATION_URL + "/rest/meeting/data";
 	private static final String SET_MEETING_ID_REST_API_URL = APPLICATION_URL + "/rest/meeting/communitySign";
     private static final String SCAN_QR_URL = APPLICATION_URL + "/android/scanQR";
     private static final String PICK_TASK_URL = APPLICATION_URL + "/task/view?id=";
     private static final String EVENT_API_URL = APPLICATION_URL + "/rest/event";
     public static final String CREATE_COMMUNITY_API_URL = APPLICATION_URL +
 			"/rest/community/create";
     private static final String SHARE_CS_URL = APPLICATION_URL + "/android/shareCsUrl";
     private static final String SERVICE_CONNECTED = "org.societies.integration.service.CONNECTED";
     private static final int SIGN = 51;
     public static String SERVICE_ID;
     private String startUrl;
 	public String nfcUrl=null;
 	private WebView webView;
 	private ProgressDialog progress;
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private float mAccel;			// acceleration apart from gravity
 	private float mAccelCurrent;	// current acceleration including gravity
 	private float mAccelLast;		// last acceleration including gravity
     long lastRefresh = System.currentTimeMillis();
     private CommunityManagementClient communityManagementClient;
     private CisDirectoryClient cisDirectoryClient;
     private ContextClient contextClient;
     private SocietiesEventsClient societiesEventsClient;
     private boolean isSocietiesUser=false, contextClientRunning, communityManagementClientConnected, cisDirectoryClientConnected, societiesEventsClientConnected;
     private SocietiesUser societiesUser = null;
     private final static String LOG_TAG = "Crowd Tasking Main";
     public static final String PROPERTY_REG_ID = "registration_id";
     private static final String PROPERTY_APP_VERSION = "appVersion";
     private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
     private boolean firstTimeOnMenuPage = true;
     private ProgressDialog progressDialog;
     private String signedUrl;
     private int sessionId;
 	public static String cookies;
 	public static int version;
 	private boolean societiesServices;
 
 	/**
      * This is the Crowd Tasking (GAE) project number (from the API Console)
      */
     String SENDER_ID = "567873389890";
 
     GoogleCloudMessaging gcm;
     String regid;
     int checkedItem = 0;
     JSONArray communitiesForPublicDisplay;
     public MainActivity() {
 	}
 
     /**
      * Check the device to make sure it has the Google Play Services APK. If
      * it doesn't, display a dialog that allows users to download the APK from
      * the Google Play Store or enable it in the device's system settings.
      */
     private boolean checkPlayServices() {
         int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
         if (resultCode != ConnectionResult.SUCCESS) {
             if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                 Log.i(LOG_TAG, "This device is not supported.");
 /*
                 try {
 		            GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
 	            } catch (Exception e) {
 		            e.printStackTrace();
 		            return false;
 	            }
             } else {
                 Log.i(LOG_TAG, "This device is not supported.");
 //                finish();
 */
             }
             return false;
         }
         return true;
     }
 
     /**
      * Gets the current registration ID for application on GCM service, if there is one.
      * If result is empty, the app needs to register.
      *
      * @return registration ID, or empty string if there is no existing
      *         registration ID.
      */
     private String getRegistrationId(Context context) {
         final SharedPreferences prefs = getGcmPreferences();
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");
         if (registrationId.isEmpty()) {
             Log.i(LOG_TAG, "Registration not found.");
             return "";
         }
         // Check if app was updated; if so, it must clear the registration ID
         // since the existing regID is not guaranteed to work with the new
         // app version.
         int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
         int currentVersion = getAppVersion(context);
         if (registeredVersion != currentVersion) {
             Log.i(LOG_TAG, "App version changed.");
             return "";
         }
         return registrationId;
     }
 
     /**
      * @return Application's version code from the {@code PackageManager}.
      */
     private static int getAppVersion(Context context) {
         try {
             PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
             return packageInfo.versionCode;
         } catch (PackageManager.NameNotFoundException e) {
             // should never happen
             throw new RuntimeException("Could not get package name: " + e);
         }
     }
 
     /**
      * @return Application's {@code SharedPreferences}.
      */
     private SharedPreferences getGcmPreferences() {
         return getSharedPreferences(DOMAIN, Context.MODE_PRIVATE);
     }
 
     /**
      * Stores the registration ID and the app versionCode in the application's
      * {@code SharedPreferences}.
      *
      * @param context application's context.
      * @param regId registration ID
      */
     private void storeRegistrationId(Context context, String regId) {
         final SharedPreferences prefs = getGcmPreferences();
         int appVersion = getAppVersion(context);
         Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString(PROPERTY_REG_ID, regId);
         editor.putInt(PROPERTY_APP_VERSION, appVersion);
         editor.commit();
     }
 
     /**
      * Registers the application with GCM servers asynchronously.
      * Stores the registration ID and the app versionCode in the application's
      * shared preferences.
      */
     private void registerInBackground() {
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 String msg;
 
                 try {
                     // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
                     if (!checkPlayServices()) {
                         return "No valid Google Play Services APK found.";
                     }
                     //                    if (gcm == null) {
                         gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
 //                    }
                     regid = gcm.register(SENDER_ID);
                     msg = "Device registered, registration ID=" + regid;
                     sendRegistrationIdToBackend(regid);
                 } catch (IOException ex) {
                     msg = "Error :" + ex.getMessage();
                     // If there is an error, don't just keep trying to register.
                     // Require the user to click a button again, or perform
                     // exponential back-off.
                 }
                 return msg;
             }
 
             @Override
             protected void onPostExecute(String msg) {
                 Log.i(LOG_TAG, msg);
             }
         }.execute(null, null, null);
     }
 
     private void sendRegistrationIdToBackend(String regId) {
         RestTask task = new RestTask(getApplicationContext(), GCM_REGISTER, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
         String url = GCM_REGISTER_URL+"?registrationId="+regId+"&version="+version;
         try {
             task.execute(new HttpGet(new URI(url)));
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public NdefMessage createNdefMessage(NfcEvent event) {
         String text = ("cs:Beam me up, Android!\n\n" +
                 "Beam Time: " + System.currentTimeMillis());
         NdefMessage msg = new NdefMessage(
                 new NdefRecord[] { createMimeRecord(
                         "application/vnd.com.example.android.beam", text.getBytes())
                         /**
                          * The Android Application Record (AAR) is commented out. When a device
                          * receives a push with an AAR in it, the application specified in the AAR
                          * is guaranteed to run. The AAR overrides the tag dispatch system.
                          * You can add it back in to guarantee that this
                          * activity starts when receiving a beamed message. For now, this code
                          * uses the tag dispatch system.
                          */
                         //,NdefRecord.createApplicationRecord("com.example.android.beam")
                 });
         return msg;
     }
 
     public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
         byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
         NdefRecord mimeRecord = new NdefRecord(
         NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
         return mimeRecord;
     }
 
     private void showProgressBar() {
         progressDialog = new ProgressDialog(MainActivity.this);
         progressDialog.setTitle("Crowd tasking");
         progressDialog.setMessage("Connecting to the server...");
 //        progressDialog.setProgressStyle(progressDialog.STYLE_HORIZONTAL);
         progressDialog.setIndeterminate(true);
 //        progressDialog.setProgress(0);
 //        progressDialog.setMax(20);
         progressDialog.show();
 
     }
 
     private void updateProgressBar(String message) {
 	    System.out.println("updateProgressBar: "+message);
 	    progressDialog.setMessage(message);
 /*        final Handler updateBarHandler = new Handler();
         new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     while (progressDialog.getProgress() < progressDialog.getMax()) {
                         try {
                             Thread.sleep(100);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         updateBarHandler.post(new Runnable() {
                             public void run() {
                                 progressDialog.incrementProgressBy(1);
                             }
                         });
 
                         if (progressDialog.getProgress() == progressDialog.getMax()) {
                             progressDialog.dismiss();
                         }
                     }
                     progressDialog.dismiss();
                 } catch (Exception e) {
                 }
             }
         }).start();*/
 //        progressDialog.dismiss();
     }
 
     @Override
 	public void onCreate(Bundle savedInstanceState) {
 	    setScope();
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
 	    sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 	    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 	    mAccel = 0.00f;
 	    mAccelCurrent = SensorManager.GRAVITY_EARTH;
 	    mAccelLast = SensorManager.GRAVITY_EARTH;
         showProgressBar();
         // TODO check internet connection
 
         setup(this);
 /*
         mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
         if (mNfcAdapter == null) {
             Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
         } else {
             // Register callback
             mNfcAdapter.setNdefPushMessageCallback(this, this);
         }
 */
         /*AccountManager am = AccountManager.get(this);
         Account[] accounts = am.getAccounts();
         for(Account ac: accounts)
         {
 	        String acname=ac.name;
 	        String actype = ac.type;
 	        Log.d("accountInfo", acname + ":" + actype);
         }*/
 
 //        loginUser();
 //        checkIntent(getIntent());
     }
 
 	private void setScope() {
 		SharedPreferences preferences  = getSharedPreferences("SOCIETIES", Context.MODE_PRIVATE);
 		if (SCOPE != null) {
 			SharedPreferences.Editor editor = preferences.edit();
 			editor.putString("SCOPE", SCOPE);
 			editor.commit();
 
 		} else {
 			SCOPE = preferences.getString("SCOPE", "");
 		}
 	}
 
 	private void setup(final Context appContext) {
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 /*
 				societiesServices = isSocietiesServiceRunning();
 				if (societiesServices) {
 					connectToSocieties();
 				}
 */
 				CheckUpdateTask checkUpdateTask = new CheckUpdateTask(appContext);
 				checkUpdateTask.execute();
 				return societiesServices;
 			}
 
 			@Override
 			protected void onPostExecute(Boolean societiesServices) {
 				webViewSetup();
 				loginUser(societiesServices);
 			}
 		}.execute(null, null, null);
 	}
 
 
 	public void connectToSocieties() {
 		getServiceId();
 		System.out.println("SERVICE_ID:" + SERVICE_ID);
 		societiesUser = getSocietiesUserData();
 		communityManagementClient = new CommunityManagementClient(this, communityClientReceiver);
 		communityManagementClient.setUpService();
 //		checkLocation();
 		contextClient = new ContextClient(getApplicationContext());
 		contextClient.setUpService();
 
 		societiesEventsClient = new SocietiesEventsClient(this, eventsClientReceiver);
 		societiesEventsClient.setUpService();
 //            cisDirectoryClient = new CisDirectoryClient(this, cisDirectoryClientReceiver);
 //            cisDirectoryClient.setUpService();
 	}
 
 	private boolean getServiceId() {
 		SharedPreferences preferences  = getSharedPreferences("SOCIETIES", Context.MODE_PRIVATE);
 		SERVICE_ID = preferences.getString("SERVICE_ID", "");
 		if (!SERVICE_ID.equalsIgnoreCase("")) {
 			return true;
 		}
 		final CountDownLatch latch = new CountDownLatch(1);
         // TODO make socketClient as AsyncTask?
         SocketClient socketClient = new SocketClient(this);
         Log.i(LOG_TAG, "Socket client created");
         new Thread(socketClient).start();
         Log.i(LOG_TAG, "Thread started");
 
         try {
             latch.await(10000, TimeUnit.MILLISECONDS);
             do {
                 if (SERVICE_ID != null) {
                     Log.i(LOG_TAG, "SERVICE_ID != null");
 	                SharedPreferences.Editor editor = preferences.edit();
 	                editor.putString("SERVICE_ID", SERVICE_ID);
 	                editor.commit();
                     return true;
                 }
             } while (true);
         } catch (InterruptedException e) {
             e.printStackTrace();
             Log.i(LOG_TAG, "InterruptedException "+e.getMessage());
             return false;
         }
     }
 
 /*
     private void checkSession() {
         try {
             RestTask task = new RestTask(getApplicationContext(), GET_USER, CookieManager.getInstance().getCookie("crowdtasking.appspot.com"));
             task.execute(new HttpGet(new URI(GET_USER_REST_API_URL)));
         } catch (URISyntaxException e) {
             e.printStackTrace();
             Toast toast = Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_LONG);
             toast.show();
         }
     }
 */
 
     private boolean isSocietiesServiceRunning() {
         boolean trust=false;
         boolean context=false;
         boolean cis=false;
 
 	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         System.out.println("Are Societies services running?");
 	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 		    if (service.service.getClassName().startsWith("org.societies")) {
 			    System.out.println(service.service.getClassName());
 		    }
 		    if ("org.societies.android.privacytrust.trust.TrustClientRemote".equals(service.service.getClassName())) {
 			    System.out.println("Trust client service is running");
 			    trust = true;
 		    }
 		    if ("org.societies.android.platform.context.ServiceContextBrokerRemote".equals(service.service.getClassName())) {
 			    System.out.println("Context client service is running");
 			    context = true;
 		    }
 		    if ("org.societies.android.platform.cis.CisManagerRemote".equals(service.service.getClassName())) {
 			    System.out.println("CisManager client service is running");
 			    cis = true;
 		    }
 	    }
 	    return trust && context && cis;
 	}
 	
 	@SuppressLint("SetJavaScriptEnabled")
 	private void webViewSetup() {
 		webView = (WebView) findViewById(R.id.webView);
 
 /*
         CookieSyncManager.createInstance(this);
         CookieManager cookieManager = CookieManager.getInstance();
         cookieManager.removeAllCookie();
 */
 
 		webView.setInitialScale(1);
         webView.getSettings().setJavaScriptEnabled(true);
         webView.getSettings().setLoadWithOverviewMode(true);
         webView.getSettings().setUseWideViewPort(true);
         webView.getSettings().setBuiltInZoomControls(true);
         webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
         webView.setScrollbarFadingEnabled(false);
         webView.setWebViewClient(new MyWebViewClient(this));
         webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
         webView.addJavascriptInterface(new JSInterface(webView), "android");
         webView.setWebChromeClient(new WebChromeClient() {
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				Log.d(LOG_TAG, "From the line " + cm.lineNumber()
 						+ " of " + cm.sourceId()+":");
 				Log.i(LOG_TAG, cm.message());
 				return true;
 			}
 
 			@Override
 			public boolean onJsAlert(WebView view, String url, String message,
 					JsResult result) {
 				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
 				return false;
 			}
     	});
 	}
 
 	private void checkIntent(Intent intent) {
         setIntent(null);
         if (intent == null) {
             return;
         }
         if (intent.getAction() == null) {
             return;
         }
         String action = intent.getAction();
         if ("GCM".equals(action)) {
             handleGcmIntent(intent);
             return;
         }
         if (Intent.ACTION_VIEW.equals(action)) {
         	startUrl = intent.getData().toString();
         }
         if ("android.nfc.action.NDEF_DISCOVERED".equals(action)) {
         	nfcUrl = intent.getData().toString();
         }		
     	if (nfcUrl != null) {
     		checkInOut(nfcUrl.replaceFirst("cs", "http"));
 			nfcUrl = null;
     	}
 
         if (intent.getAction().equalsIgnoreCase(CHECK_IN_OUT)) {
             String[] response = getIntent().getStringArrayExtra(RestTask.HTTP_RESPONSE);
             System.out.println(response[1]);
             Toast.makeText(getApplicationContext(), response[1], Toast.LENGTH_SHORT).show();
         }
 
     }
 
     @Override
 	protected void onNewIntent(Intent intent) {
         checkIntent(intent);
     }
 
 	@Override
     public void onResume() {
         super.onResume();
         registerReceiver(receiver, new IntentFilter(C4CSS_INTENT));
         registerReceiver(receiver, new IntentFilter(GET_MEETING_ACTION));
         registerReceiver(receiver, new IntentFilter(SET_MEETING_ACTION));
         registerReceiver(receiver, new IntentFilter(CHECK_IN_OUT));
         registerReceiver(receiver, new IntentFilter(GET_LOCATION_ACTION));
         registerReceiver(receiver, new IntentFilter(GET_USER));
         registerReceiver(receiver, new IntentFilter(LOGIN_USER));
         registerReceiver(receiver, new IntentFilter(TAKE_CONTROL));
         registerReceiver(receiver, new IntentFilter(GCM_REGISTER));
         registerReceiver(receiver, new IntentFilter(SET_FOCUS));
         registerReceiver(receiver, new IntentFilter("android.nfc.action.NDEF_DISCOVERED"));
         registerReceiver(receiver, new IntentFilter(SERVICE_CONNECTED));
         registerReceiver(receiver, new IntentFilter(Sign.ACTION_FINISHED));
         sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 
         checkIntent(getIntent());
     }
 
     private void handleGcmIntent(Intent intent) {
        String message = intent.getStringExtra(GcmIntentService.PARAMETER_MESSAGE);
        String downloadUrl = intent.getStringExtra(GcmIntentService.PARAMETER_URL);
        String meetingId = intent.getStringExtra(GcmIntentService.PARAMETER_MEETING_ID);
 
	    if (!"".equalsIgnoreCase(downloadUrl) && downloadUrl != null) {
 		    Log.i(LOG_TAG, "downloadUrl: "+downloadUrl+", meetingId: "+meetingId);
		    signDocument(downloadUrl, meetingId);
	    }
 //        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
     }
 
	private void signDocument(String downloadUrl, String meetingId) {
		Intent i = new Intent(Sign.ACTION);
 		i.putExtra(Sign.Params.DOC_TO_SIGN_URL, downloadUrl);
         i.putExtra(Sign.Params.COMMUNITY_SIGNATURE_SERVER_URI, downloadUrl);
 
 		ArrayList<String> idsToSign = new ArrayList<String>();
 		idsToSign.add(meetingId);
 		i.putStringArrayListExtra(Sign.Params.IDS_TO_SIGN, idsToSign);
 
 		Log.i(LOG_TAG, "downloadUrl: " + downloadUrl);
 		Log.i(LOG_TAG, "idsToSign: " + idsToSign);
 		startActivityForResult(i, SIGN);
 	}
 
 	@Override
     public void onPause() {
         super.onPause();
     	unregisterReceiver(receiver);
     	sensorManager.unregisterListener(this);
     }
   	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public void onBackPressed()
     {
     	if(webView.canGoBack()) {
         	WebBackForwardList webBackForwardList = webView.copyBackForwardList();
         	//int i = webBackForwardList.getCurrentIndex();
         	String historyUrl = webBackForwardList.getItemAtIndex(webBackForwardList.getCurrentIndex()).getUrl();
         	if (historyUrl.startsWith(HOME_URL) ||
         			historyUrl.startsWith(APPLICATION_URL+"/login")) {
         		super.onBackPressed();
         		return;
         	}
         	if (historyUrl.equalsIgnoreCase(APPLICATION_URL+"/task/new")) {
                 webView.loadUrl("javascript:CrowdTaskingApp.cancelTask()");
                 return;
         	}
         	if (historyUrl.startsWith(APPLICATION_URL+"/community/edit")) {
     			Toast.makeText(getApplicationContext(), "Use Save or Cancel button.", Toast.LENGTH_SHORT).show();
                 return;
         	}
            	webView.goBack();
         }
         else {
             super.onBackPressed();
         }
     }
 
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.getLocation:
 	            if (contextClientRunning) {
 		            contextClient.getSymbolicLocation(societiesUser.getUserId());
 	            } else {
 		            Toast.makeText(getApplicationContext(), "Context service is not running.", Toast.LENGTH_SHORT).show();
 	            }
 	            return true;
 
             case R.id.home:
             	webView.loadUrl(HOME_URL);
             	// reload
             	//webView.loadUrl(webView.getUrl());
             	return true;
             
             case R.id.checkUpdate:
             	//refreshData();
             	/*Intent startIntent=new Intent(this.getApplicationContext(),RemoteControlActivity.class);
             	startActivity(startIntent);*/
                 CheckUpdateTask checkUpdateTask = new CheckUpdateTask(this, true);
                 checkUpdateTask.execute();
                 return true;
 
             case R.id.logout:
 	            if (isSocietiesUser) {
 		            super.onBackPressed();
 	            }
 	            clearCookies();
             	webView.loadUrl(HOME_URL);
                 return true;
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void clearCookies() {
         CookieSyncManager.createInstance(this);
         CookieManager cookieManager = CookieManager.getInstance();
         cookieManager.removeAllCookie();
     }
 
     private void checkInOut(String url) {
 		try {
             url = "http://" + DOMAIN + PORT + "/cs/research.lab/leave";
             RestTask task = new RestTask(getApplicationContext(), CHECK_IN_OUT, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
 			task.execute(new HttpGet(new URI(url)));
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 
     private void loginUser(boolean societiesUser) {
 	    if (societiesUser) {
 		    startUrl = APPLICATION_URL + "/loginSocietiesUser.html";
 	    } else {
 		    startUrl = HOME_URL;
 	    }
 
 	    webView.loadUrl(startUrl);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         if (requestCode == SIGN && resultCode==RESULT_OK) {
             signedUrl = intent.getStringExtra(Sign.Params.SIGNED_DOC_URL);
             sessionId = intent.getIntExtra(Sign.Params.SESSION_ID, -1);
             Toast.makeText(getApplicationContext(), "Document was successfully signed. SIGNED_DOC_URL: "+signedUrl
                     +", \nsessionId: "+sessionId, Toast.LENGTH_LONG).show();
         }
         IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
         if (result != null) {
             String contents = result.getContents();
             if (contents != null) {
                 // TODO: leave this?
                 Toast.makeText(getApplicationContext(), R.string.result_succeeded, Toast.LENGTH_SHORT).show();
                 if (contents.startsWith("http")) {
                     if (contents.startsWith(PICK_TASK_URL)) {
                         HttpPost eventRequest;
                         try {
                             String taskId = contents.substring(PICK_TASK_URL.length());
                             eventRequest = new HttpPost(new URI(EVENT_API_URL));
                             List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                             parameters.add(new BasicNameValuePair("taskId", taskId));
                             eventRequest.setEntity(new UrlEncodedFormEntity(parameters));
                             RestTask task = new RestTask(getApplicationContext(), LOG_EVENT, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
                             task.execute(eventRequest);
                         } catch (URISyntaxException e) {
                             Log.e(LOG_TAG, "Can't log event: " + e.getMessage());
                         } catch (UnsupportedEncodingException e) {
                             Log.e(LOG_TAG, "Can't log event: " + e.getMessage());
                         }
                     }
                     webView.loadUrl(contents);
                 }
                 if (contents.startsWith("cs:")) {
                     checkInOut(contents.replaceFirst("cs", "http"));
                 }
                 if (contents.startsWith("channel:")) {
                     try {
 	                    String kukiji = CookieManager.getInstance().getCookie(DOMAIN);
 	                    if (kukiji == null) {
 		                    kukiji = cookies;
 	                    }
 	                    RestTask task = new RestTask(getApplicationContext(), TAKE_CONTROL, kukiji, DOMAIN);
 	                    String url = PD_TAKE_CONTROL + "?channelNumber=" + contents.substring
 			                    ("channel:".length());
 	                    task.execute(new HttpGet(new URI(url)));
                     } catch (URISyntaxException e) {
                         e.printStackTrace();
                         Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                     }
                 }
             } else {
                 Toast.makeText(getApplicationContext(), R.string.result_failed, Toast.LENGTH_SHORT).show();
                 Toast.makeText(getApplicationContext(), getString(R.string.result_failed_why), Toast.LENGTH_LONG).show();
             }
         }
     }
 
     private BroadcastReceiver cisDirectoryClientReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(ICisDirectory.FIND_ALL_CIS)) {
                 Parcelable[] objects = (Parcelable[])intent.getParcelableArrayExtra(ICisDirectory.INTENT_RETURN_VALUE);
                 for(Parcelable object: objects) {
                     CisAdvertisementRecord advert = (CisAdvertisementRecord) object;
                     Log.i(LOG_TAG, advert.getId());
                     Log.i(LOG_TAG, advert.getName());
                 }
 
                 //Object[] advertismentRecords = intent.getParcelableArrayExtra(ICisDirectory.INTENT_RETURN_VALUE);
             }
         }
     };
 
     private BroadcastReceiver eventsClientReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             System.out.println("intent.getAction():"+intent.getAction());
             System.out.println("intent:"+intent);
         }
     };
 
     private BroadcastReceiver communityClientReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(ICisManager.GET_CIS_LIST)) {
                 Object[] communities = intent.getParcelableArrayExtra(ICisManager.INTENT_RETURN_VALUE);
                 Log.i(LOG_TAG,"package: "+intent.getPackage());
                 List<CommunityJS> societiesCommunities = new ArrayList<CommunityJS>();
                 for(Object community:communities) {
                     societiesCommunities.add(new CommunityJS((Community)community, getSocietiesUserData()));
                 }
                 ((CrowdTasking)getApplication()).setSocietiesCommunities(societiesCommunities);
                 getCommunitiesForUserFromGAE();
             }
         }
     };
 
     private void getCommunitiesForUserFromGAE() {
         try {
             RestTask task = new RestTask(getApplicationContext(), C4CSS_INTENT, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
             task.execute(new HttpGet(new URI(C4CSS_API_URL+"?version="+version)));
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
 			if (progress != null) {
 				progress.dismiss();
 			}
             System.out.println("onReceive action: "+intent.getAction());
 /*
             if (intent.getAction().equalsIgnoreCase(GET_USER)) {
                 String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
                 if ("\"Not authorized!\"".equalsIgnoreCase(response)) {
                     // user is not logged in
                     Toast.makeText(getApplicationContext(), "Logging in user", Toast.LENGTH_SHORT).show();
                     loginUser();
                 }
                 else {
                     webView.loadUrl(startUrl);
                 }
             }
             if (intent.getAction().equalsIgnoreCase(LOGIN_USER)) {
                 String kuki = intent.getStringExtra("cookie");
                 CookieManager.getInstance().setCookie("crowdtasking.appspot.com", kuki);
                 checkSession();
             }
 */
             if (intent.getAction().equalsIgnoreCase(SERVICE_CONNECTED)) {
                 String response = intent.getStringExtra("serviceName");
                 if (response.equalsIgnoreCase("CommunityManagement")) {
 	                updateProgressBar("CommunityManagementClient started");
                     communityManagementClientConnected = true;
 //                    communityManagementClient.listCommunities();
                 }
                 if (response.equalsIgnoreCase("ContextClient")) {
 	                updateProgressBar("ContextClient started");
                     contextClientRunning = true;
                     //contextClient.getSymbolicLocation(societiesUser.getUserId());
                 }
                 if (response.equalsIgnoreCase("CisDirectoryClient")) {
 	                updateProgressBar("CisDirectoryClient started");
                     cisDirectoryClientConnected = true;
                     cisDirectoryClient.findAllCisAdvertismentRecords();
                 }
                 if (response.equalsIgnoreCase("SocietiesEventsClient")) {
 	                updateProgressBar("SocietiesEventsClient started");
                     societiesEventsClientConnected = true;
                     societiesEventsClient.subcribeToLocationChangedEvent();
                 }
                 System.out.println(response);
             }
             if (intent.getAction().equalsIgnoreCase(C4CSS_INTENT)) {
                 String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 if ("\"Not authorized!\"".equalsIgnoreCase(response[1])) {  // user is not logged in yet, try again
                     getCommunitiesForUserFromGAE();
                     return;
                 }
                 if (isResponseOk(response[0])) {
                     ((CrowdTasking)getApplication()).synchronizeCommunities(response[1]);
                 }
             }
             if (intent.getAction().equalsIgnoreCase(GET_MEETING_ACTION)) {
                 String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 sendMeetingEvent(response[1]);
             }
             if (intent.getAction().equalsIgnoreCase(SET_MEETING_ACTION)) {
                 String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 Toast.makeText(getApplicationContext(), response[1], Toast.LENGTH_SHORT).show();
             }
             if (intent.getAction().equalsIgnoreCase(GET_LOCATION_ACTION)) {
 	            checkInOut(intent.getStringExtra(ContextClient.CHECK_IN_URL));
             }
         	if (intent.getAction().equalsIgnoreCase(CHECK_IN_OUT)) {
                 String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 if (!isResponseOk(response[0])) {
                     response[1] = "Please sign in first.";
         		}
                 Toast.makeText(getApplicationContext(), response[1], Toast.LENGTH_SHORT).show();
         	}
         	if (intent.getAction().equalsIgnoreCase(TAKE_CONTROL)) {
         		String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 if ("HTTP/1.1 409 Conflict".equalsIgnoreCase(response[0])) {
                     selectCommunityForPD(response);
                 } else {
                     Toast.makeText(getApplicationContext(), response[1], Toast.LENGTH_LONG).show();
                 }
         	}
         	if (intent.getAction().equalsIgnoreCase(GCM_REGISTER)) {
         		String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
                 if (isResponseOk(response[0])) {
                     // Persist the regID - no need to register again.
                     storeRegistrationId(getApplicationContext(), regid);
                 }
         	}
         	if (intent.getAction().equalsIgnoreCase(LOG_EVENT)) {
                 String[] response = intent.getStringArrayExtra(RestTask.HTTP_RESPONSE);
         		System.out.println(response);
 				Toast.makeText(getApplicationContext(), response[1], Toast.LENGTH_SHORT).show();
         	}
             if (intent.getAction().equalsIgnoreCase(Sign.ACTION_FINISHED)) {
                 boolean success = intent.getBooleanExtra(Sign.Params.SUCCESS, false);
                 boolean uploadSuccess = intent.getBooleanExtra(Sign.Params.UPLOAD_SUCCESS, false);
                 int sid = intent.getIntExtra(Sign.Params.SESSION_ID, -1);
 
                 if (success && sid == MainActivity.this.sessionId) {
                     if (success) {
                         Toast.makeText(getApplicationContext(), "File signed successfully.", Toast.LENGTH_LONG).show();
                     }
                     if (!uploadSuccess) {
                         Toast.makeText(getApplicationContext(), "File NOT uploaded successfully.", Toast.LENGTH_LONG).show();
                     }
                 }
                 else {
                     // Ignore non-relevant notifications, react to failure...
                 }
             }
         	if (intent.getAction().equalsIgnoreCase(SET_FOCUS)) {
                 Intent i = new Intent(getApplicationContext(), MainActivity.class);
                 i.setAction(Intent.ACTION_MAIN);
                 i.addCategory(Intent.CATEGORY_LAUNCHER);
                 startActivity(i);
         	}
         }
     };
 
     private boolean isResponseOk(String response) {
         if ("HTTP/1.1 200 OK".equalsIgnoreCase(response)) {
             return true;
         }
         Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
         return false;
     }
 
     private void selectCommunityForPD(String[] response) {
         try {
             final String[] channelId = { "" };
             JSONArray responseJSON = new JSONArray(response[1]);
             communitiesForPublicDisplay = responseJSON.getJSONArray(0);
             channelId[0] = responseJSON.getString(1);
             String[] communityNames = new String[communitiesForPublicDisplay.length()];
 
             for (int i = 0; i < communitiesForPublicDisplay.length(); i++) {
                 communityNames[i] = communitiesForPublicDisplay.getJSONObject(i).getString("name");
             }
 
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             // Set the dialog title
             builder.setTitle("Select community")
                     // Specify the list array, the items to be selected by default (null for none),
                     // and the listener through which to receive callbacks when items are selected
                     .setSingleChoiceItems(communityNames, checkedItem,
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     checkedItem = which;
                                 }
                             })
                             // Set the action buttons
                     .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
                             showCommunity(channelId[0]);
                         }
                     });
 
             builder.show();
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 
     private void showCommunity(String channelId) {
         try {
             Long id = communitiesForPublicDisplay.getJSONObject(checkedItem).getLong("id");
             String name = communitiesForPublicDisplay.getJSONObject(checkedItem).getString("name");
 //            Toast.makeText(getApplicationContext(), checkedItem+". community selected. Id="+id+", name="+name, Toast.LENGTH_LONG).show();
 	        String url = PD_TAKE_CONTROL + "?channelNumber=" + channelId + "&communityId" +
 			        "=" + communitiesForPublicDisplay.getJSONObject(checkedItem).getString("id");
 	        try {
                 RestTask task = new RestTask(getApplicationContext(), TAKE_CONTROL, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
                 task.execute(new HttpGet(new URI(url)));
             } catch (URISyntaxException e) {
                 e.printStackTrace();
                 Toast.makeText(getApplicationContext(), "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
             }
         } catch (JSONException e) {
             Toast.makeText(getApplicationContext(), "Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
             e.printStackTrace();
         }
     }
 
     private void sendMeetingEvent(String meetingJSON) {
         //String JSON_STRING = "{ \"id\":212001,\"subject\":\"nujen sestanek\",\"description\":\"Urgent!!!\",\"cs\":{\"id\":179001,\"name\":\"test\",\"urlMapping\":\"test\"},\"startTime\":\"Jan 31, 2013 12:00:02 PM\",\"endTime\":\"Jan 31, 2013 2:00:03 PM\",\"organizer\":\"Setcce Research\"}";
     	JSONObject meeting;
     	String space;
     	try {
 			meeting = new JSONObject(meetingJSON);
 			space = meeting.getJSONObject("cs").optString("name");
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return;
 		}
 		//String description = meeting.optString("description");
     	
         
         Calendar beginTime = Calendar.getInstance();
         beginTime.setTime(parseDateString(meeting.optString("startTime")));
 		//beginTime.set(2013, 1, 15, 17, 00);
 		Calendar endTime = Calendar.getInstance();
 		endTime.setTime(parseDateString(meeting.optString("endTime")));
 		//endTime.set(2013, 1, 15, 17, 15);
 		Intent intent = new Intent(Intent.ACTION_EDIT);
 		intent.setType("vnd.android.cursor.item/event");
 		intent.putExtra("title", meeting.optString("subject"));
 		intent.putExtra("description", meeting.optString("description"));
 		intent.putExtra(Events.EVENT_LOCATION, space);
 		//intent.putExtra(Events.DESCRIPTION, meeting.optString("description"));
 		//intent.putExtra(Events.DTSTART, beginTime.getTimeInMillis());
 		//intent.putExtra(Events.DTEND, endTime.getTimeInMillis());
 		intent.putExtra("beginTime", beginTime.getTimeInMillis());
 		intent.putExtra("endTime", endTime.getTimeInMillis());
 		//intent.putExtra("allDay", true);
 		//intent.putExtra("rrule", "FREQ=YEARLY");
 		
 		/*intent.putExtra(Intent.EXTRA_EMAIL,
 				"sjuresa@gmail.com");*/
 //				"simon.juresa@setcce.si, helena.halas@setcce.si, jan.porekar@setcce.si");
 		startActivity(intent);
 
 		//startActivityForResult(intent, RESULT_OK );
 
 
 		/*Intent intent = new Intent(Intent.ACTION_INSERT)
 		.setData(Events.CONTENT_URI)
 		.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
 				beginTime.getTimeInMillis())
 		.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
 				endTime.getTimeInMillis())
 		.putExtra(Events.TITLE, "SOCIETIES Crowd Tasking - evaluation - final steps")
 		.putExtra(Events.DESCRIPTION, "Kako popraviti navodila + evaluacijo, ...")
 		.putExtra(Events.EVENT_LOCATION, "Laboratorij za Am I in wisdom of the crowds")
 		.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
 		.putExtra(Intent.EXTRA_EMAIL,
 				"helena.halas@setcce.si, jan.porekar@setcce.si, simon.juresa@setcce.si");
 		startActivity(intent);*/
     }
 
 	private Date parseDateString(String dateString) {
 		DateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");
 		Date datum;
 		try {
 			datum = formatter.parse(dateString);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			datum = new Date();
 		}
 		return datum;
 	}
 
     public SocietiesUser getSocietiesUserData() {
             /*String columns [] = {CSSContentProvider.CssRecord.CSS_RECORD_CSS_IDENTITY,
                     CSSContentProvider.CssRecord.CSS_RECORD_EMAILID,
                     CSSContentProvider.CssRecord.CSS_RECORD_FORENAME,
                     CSSContentProvider.CssRecord.CSS_RECORD_NAME};*/
         if (societiesUser != null) {
             return societiesUser;
         }
         societiesUser = new SocietiesUser();
         try {
             //Cursor cursor = context.getContentResolver().query(CSSContentProvider.CssRecord.CONTENT_URI, columns, null, null, null);
             Cursor cursor = getContentResolver().query(CSSContentProvider.CssRecord.CONTENT_URI, null, null, null, null);
             cursor.moveToFirst();
 
             societiesUser.setUserId(cursor.getString(cursor.getColumnIndex(CSSContentProvider.CssRecord.CSS_RECORD_CSS_IDENTITY)));
 //            societiesUser.setUserId("eliza.societies.local2.macs.hw.ac.uk");
             societiesUser.setName(cursor.getString(cursor.getColumnIndex(CSSContentProvider.CssRecord.CSS_RECORD_NAME)));
             societiesUser.setForeName(cursor.getString(cursor.getColumnIndex(CSSContentProvider.CssRecord.CSS_RECORD_FORENAME)));
             societiesUser.setEmail(cursor.getString(cursor.getColumnIndex(CSSContentProvider.CssRecord.CSS_RECORD_EMAILID)));
             cursor.close();
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         isSocietiesUser = true;
         return societiesUser;
     }
 
     public class JSInterface {
 		private WebView mWebView;
 		//@JavascriptInterface for API 17
 		public JSInterface(WebView appView) {
 			this.mWebView = appView;
 		}
 
         @JavascriptInterface
 		public void toast(String echo) {
 			Toast toast = Toast.makeText(mWebView.getContext(), echo,
                     Toast.LENGTH_SHORT);
 			toast.show();
 		}
 
         @JavascriptInterface
 		public void goBack() {
 			mWebView.goBack();
 		}
 
         @JavascriptInterface
         public String getScope() {
             return SCOPE;
         }
 
         @JavascriptInterface
         public boolean isSocietiesUser() {
             Log.i("isSocietiesUser()", "isSocietiesUser: " + isSocietiesUser);
             return isSocietiesUser;
         }
 
         @JavascriptInterface
         public String socUser() {
             Log.i("isSocietiesUser()", "isSocietiesUser: " + isSocietiesUser);
             return Boolean.valueOf(isSocietiesUser).toString();
         }
 
         @JavascriptInterface
 		public String getSocietiesUser() {
             SocietiesUser societiesUser = getSocietiesUserData();
             JSONObject societiesUserJSON = new JSONObject();
             try {
                 societiesUserJSON.put("userId", societiesUser.getUserId());
                 societiesUserJSON.put("name", societiesUser.getName());
                 societiesUserJSON.put("foreName", societiesUser.getForeName());
                 societiesUserJSON.put("email", societiesUser.getEmail());
                 societiesUserJSON.put("scope", SCOPE);
             } catch (JSONException e) {
                 e.printStackTrace();
                 return null;
             }
             return societiesUserJSON.toString();
         }
 
         @JavascriptInterface
         public String getSocietiesCommunities() {
             communityManagementClient.listCommunities();
             return ((CrowdTasking)getApplication()).getSocietiesCommunitiesJSON();
         }
 
         @JavascriptInterface
         public void setCommunitySpaces(String community) {
             ((CrowdTasking)getApplication()).setCommunitySpaces(community);
         }
 
         @JavascriptInterface
         public void refreshCommunities() {
             getCommunitiesForUserFromGAE();
         }
 
         @JavascriptInterface
         public String getSocietiesCommunitiesByJids(String[] jids) {
             return ((CrowdTasking)getApplication()).getSocietiesCommunitiesByJids(jids);
         }
 
         @JavascriptInterface
         public void share(String spejs, String action) {
             if (spejs == null || "".equalsIgnoreCase(spejs)) {
 				Toast toast = Toast.makeText(mWebView.getContext(), "Enter URL mapping", Toast.LENGTH_SHORT);
 				toast.show();
 				return;
 			}
 			if (action == null || "".equalsIgnoreCase(action) && !("PD".equalsIgnoreCase(action) || "CI".equalsIgnoreCase(action) || "CO".equalsIgnoreCase(action))) {
 				return;
 			}
 			String schema="cs:";
 			if ("PD".equalsIgnoreCase(action)) {
 				schema="http:";
 			}
             String text = schema + "//" + DOMAIN + PORT + "/cs/" + spejs;
             //			String text = schema+"//crowdtasking.appspot.com/cs/"+spejs;
             if ("CI".equalsIgnoreCase(action)) {
 				text+="/enter";
 			}
 			if ("CO".equalsIgnoreCase(action)) {
 				text+="/leave";
 			}
 			Intent sendIntent = new Intent();
 			sendIntent.setAction(Intent.ACTION_SEND);
 			sendIntent.putExtra(Intent.EXTRA_TEXT, text);
 			sendIntent.setType("text/plain");
 			startActivity(sendIntent);
 		}
 	}
 
     @Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent sensor) {
         float x = sensor.values[0];
         float y = sensor.values[1];
         float z = sensor.values[2];
         mAccelLast = mAccelCurrent;
         mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
         float delta = mAccelCurrent - mAccelLast;
         mAccel = mAccel * 0.9f + delta; // perform low-cut filter
         if (mAccel > 9 && (System.currentTimeMillis() - lastRefresh) > 3000) {
 //            Toast.makeText(getApplicationContext(), "shake detected ("+mAccel+")", Toast.LENGTH_SHORT).show();
         	mAccel = 0;
             lastRefresh = System.currentTimeMillis();
         	refreshData();
         }
 	}
 
 	private void refreshData() {
 		webView.loadUrl("javascript:refreshOnShake()");
 	}
 
 	private class MyWebViewClient extends WebViewClient {
     	Activity parentActivity;
     	
 		public MyWebViewClient(Activity activity) {
 			parentActivity = activity;
 		}
 				
 		@SuppressWarnings("unused")
 		public void onRequestFocus(WebView view) {
 			synchronized (this) {
 				this.notify();
 			}
 		}
 
 		private void addMeetingToCalendar(String url) {
     		String meetingID = url.substring(MEETING_URL.length());
     		System.out.println(meetingID);
     		try
             {
     			HttpGet searchRequest = new HttpGet(new URI(MEETING_REST_API_URL+"?id="+meetingID));
     			RestTask task = new RestTask(parentActivity, GET_MEETING_ACTION, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
     			task.execute(searchRequest);
     		} catch (URISyntaxException e) {
     			e.printStackTrace();
     		}
 		}
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             if (url.equalsIgnoreCase(SCAN_QR_URL)) {
                 IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                 integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
         		return true;
         	}
         	if (url.equalsIgnoreCase(SHARE_CS_URL)) {
                 IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                 integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
         		return true;
         	}
         	if (url.startsWith(MEETING_URL)) {
 /*
                 PopupMenu popupMenu = new PopupMenu(getApplicationContext(), findViewById(R.id.anchor));
                 popupMenu.getMenu().add("Start meeting");
                 popupMenu.getMenu().add("Join meeting");
                 popupMenu.getMenu().add("...");
                 popupMenu.show();
                 //addMeetingToCalendar(url);
 */
                 HttpPost meetingRequest;
                 try {
                     String meetingID = url.substring(MEETING_URL.length());
                     meetingRequest = new HttpPost(new URI(SET_MEETING_ID_REST_API_URL));
                     List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                     parameters.add(new BasicNameValuePair("meetingIdToSign", meetingID));
                     meetingRequest.setEntity(new UrlEncodedFormEntity(parameters));
                     RestTask task = new RestTask(getApplicationContext(), SET_MEETING_ACTION, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
                     task.execute(meetingRequest);
                 } catch (URISyntaxException e) {
                     Log.e(LOG_TAG, "Can't log event: "+e.getMessage());
                 } catch (UnsupportedEncodingException e) {
                     Log.e(LOG_TAG, "Can't log event: "+e.getMessage());
                 }
 
 /*
                 AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
 
                 alert.setTitle("Title");
                 alert.setMessage("Message");
 
                 // Set an EditText view to get user input
                 final EditText input = new EditText(getApplicationContext());
                 alert.setView(input);
 
                 alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         String value = input.getText().toString();
                         Toast.makeText(getApplicationContext(), "value", Toast.LENGTH_LONG);
                     }
                 });
 
                 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         // Canceled.
                     }
                 });
 
                 alert.show();
 */
 /*
                 RestTask task = new RestTask(getApplicationContext(), SET_MEETING_ACTION, CookieManager.getInstance().getCookie(DOMAIN), DOMAIN);
                 try {
                     task.execute(new HttpGet(new URI(SET_MEETING_ID_REST_API_URL+"?meetingId4CommSign="+meetingID)));
                 } catch (URISyntaxException e) {
                     e.printStackTrace();
                 }
 */
 
         		return true;
         	}
             view.loadUrl(url);
             return false;
         }
         
         public void onPageFinished(WebView view, String url) {
 
             if (firstTimeOnMenuPage) {
 	            progressDialog.dismiss();
             }
         	if (url.contains("enter") || url.contains("leave")) {
         		webView.goBack();
         	}
             if (url.equalsIgnoreCase(HOME_URL) || url.equalsIgnoreCase(HOME_URL_AFTER_REGISTER)) {
                 if (firstTimeOnMenuPage) {
                     if (contextClientRunning) {
                         contextClient.getSymbolicLocation(societiesUser.getUserId());
                     }
                     if (communityManagementClientConnected) {
                         communityManagementClient.listCommunities();
                     }
 	                if (societiesServices) {
 		                TrustTask task = new TrustTask(getApplicationContext(), DOMAIN, APPLICATION_URL);
 		                task.execute(societiesUser.getUserId());
 	                }
                     //                    if ("".equalsIgnoreCase(getRegistrationId(getApplicationContext()))) {
                     registerInBackground();
                     //                    }
                     cookies = CookieManager.getInstance().getCookie(url);
                     firstTimeOnMenuPage = false;
                 }
             }
        }
     }
 
     @Override
     protected void onDestroy() {
 	    super.onStop();
         if (isSocietiesUser) {
             clearCookies();
             Log.i(LOG_TAG, "cookies cleared");
         }
         if (communityManagementClientConnected) {
             communityManagementClient.tearDownService();
         }
         if (cisDirectoryClientConnected) {
             cisDirectoryClient.tearDownService();
         }
         if (societiesEventsClientConnected) {
             societiesEventsClient.tearDownService();
         }
     }
 }

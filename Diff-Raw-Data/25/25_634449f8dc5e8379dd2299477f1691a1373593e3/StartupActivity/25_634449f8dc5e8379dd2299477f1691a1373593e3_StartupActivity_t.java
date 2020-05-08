 package jm.org.data.area;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class StartupActivity extends Activity {
 	private static final String TAG = AreaData.class.getSimpleName();
 	
 	protected boolean _active = true;
 	protected int _splashTime = 1; // time to display the splash screen in ms
 	
 	// not sure how this is gonna be used so just placing it here for the time being
 	//private JSONParse mJsonParse;
 	
 	//private APIPull mApiPull;
 	private AreaData appdata;
 	
 	private boolean isOnline;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.startupview);
 
 		//NOT USED
 		// Initialise JSONParser
 		//mJsonParse = new JSONParse(getBaseContext());
 		//mApiPull = new APIPull();
 		
 		// check if app is online
 		isOnline = checkNetworkConnection(getBaseContext());
 		
 		if (!isOnline) {
 			Log.e(StartupActivity.class.toString(), "No Internet connectivity");
 			// TODO show Toast or message dialog with showing no internet connectivity
 			// and send flag to next activity to indicate that we should use offline cache 
 		}
 				
 		// get initial pull of data
 		appdata = new AreaData(getBaseContext());
 		
 		// thread for displaying the SplashScreen
 		Thread splashTread = new Thread() {
 			@Override
 			public void run() {
 				try {
 					
 					//	initial pull of country and indicator data
 					//getCountryList();
 					//getIndicatorsList();
 					
 					//initial pull of country and indicator data
 					appdata.updateAPIs();
 					appdata.updatePeriod();
 					//appdata.updateCountries();
 					
 					// Error when debugging needs to be tested
 					appdata.updateIndicators();
 										
 					/*int waited = 0;
 					while (_active && (waited < _splashTime)) {
 						sleep(100);
 						if (_active) {
 							waited += 100;
 						}
 					}*/
 				} catch (Exception e) {
 					Log.e(TAG,"Exception updating Area Data " + e.toString());
 				} finally {
 					finish();
 					
 					startActivity(new Intent(getApplicationContext(),
							HomeActivity.class));
 					//stop();
 				}
 			}
 		};
 		splashTread.start();
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 	
 	/**
 	 * Checks if the device has Internet connection.
 	 * 
 	 * @return <code>true</code> if the phone is connected to the Internet.
 	 */
 	public static boolean checkNetworkConnection(Context context) {
 		final ConnectivityManager connMgr = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		final android.net.NetworkInfo wifi = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		final android.net.NetworkInfo mobile = connMgr
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		final boolean connected = (wifi != null && mobile != null
 				&& connMgr.getActiveNetworkInfo().isAvailable() && connMgr
 				.getActiveNetworkInfo().isConnected());
 
 		if (connected)
 			return true;
 		else
 			return false;
 	}
 	
 	
 	/*
 	// TODO: default with 0 specify constants for apis
 	
 	private void getCountryList() {
 		int numOfCountries = mJsonParse.getWBTotal(mApiPull.HTTPRequest(0,
 				"http://api.worldbank.org/country?per_page=1&format=json"));
 		if(numOfCountries == 0 ){
 			// error in parsing JSON data
 			Log.e(TAG, "Error In Parsing JSON data");
 		}else{
 			mJsonParse.parseCountries(mApiPull.HTTPRequest(0,
 					"http://api.worldbank.org/country?per_page="+ numOfCountries +"&format=json"));
 		}
 	}// end function
 	
 	private void getIndicatorsList() {
 		
 		int numOfIndicators = mJsonParse.getWBTotal(mApiPull.HTTPRequest(0,
 				"http://api.worldbank.org/topic/1/Indicator?per_page=10&format=json"));
 		if(numOfIndicators == 0 ){
 			// error in parsing JSON data
 			Log.e(TAG, "Error In Parsing JSON data");
 		}else{
 			mJsonParse.parseIndicators(mApiPull.HTTPRequest(0,
 					"http://api.worldbank.org/topic/1/Indicator?per_page="+ numOfIndicators +"&format=json"));
 		}
 	}// end function
 	
 	*/
 }

 package com.mobiquityinc.android.example.alarmclockexample;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 
 
 import android.location.Address;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Messenger;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class AlarmClockMainActivity extends Activity implements LocationListener {
 	static final String TAG = AlarmClockMainActivity.class.getSimpleName();
     static final String PREFERENCES = "AlarmClock";
     private TextView mAmView, mPmView, mSecondsView;
     private Calendar mCalendar;
     private boolean mRegistered = false;
     private ContentObserver mFormatChangeObserver;
     private TextView mTimeDisplay;
     private String mFormat;
     private FrameLayout mAmPm;
     private boolean mShowSeconds=true;
     private Handler mHandler;
     private Runnable mSecRunnable;
     private TextView overallCondition;
     private TextView currentTemp;
     private TextView feelsLike;
     
     
     private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                 mCalendar = Calendar.getInstance();
             }
             updateTime();
         }
     };
     
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.alarmclock_main);
 	    String[] ampm = new DateFormatSymbols().getAmPmStrings();
         mCalendar = Calendar.getInstance();
         mFormatChangeObserver = new FormatChangeObserver();
         mAmPm = (FrameLayout) findViewById(R.id.am_pm);
         mAmView = (TextView) findViewById(R.id.am);
         mPmView = (TextView) findViewById(R.id.pm);
         mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
         mAmView.setText(ampm[0]);
         mPmView.setText(ampm[1]);
         mSecondsView = (TextView) findViewById(R.id.timeSeconds);
 
         overallCondition = (TextView)findViewById(R.id.overallCondition);
         currentTemp =  (TextView)findViewById(R.id.currentTemp);
         feelsLike =  (TextView)findViewById(R.id.feelsLike);
         
         mHandler = new Handler();
         //mHandler = new Handler(Looper.myLooper());
         //mHandler = new Handler(Looper.getMainLooper());
         //Messenger msg = new Messenger(mHandler);
         
         
         Typeface fontFace = Typeface.createFromAsset(this.getAssets(), "Basicdots.TTF");
         mTimeDisplay.setTypeface(fontFace, Typeface.BOLD);
         mSecondsView.setTypeface(fontFace, Typeface.BOLD);
 
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 	    if (mRegistered) return;
 	    mRegistered = true;
         mCalendar = Calendar.getInstance();
         /* monitor time ticks, time changed, timezone */
         IntentFilter filter = new IntentFilter();
         filter.addAction(Intent.ACTION_TIME_TICK);
         filter.addAction(Intent.ACTION_TIME_CHANGED);
         filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
         registerReceiver(mIntentReceiver, filter, null, null);
         getContentResolver().registerContentObserver(
                 Settings.System.CONTENT_URI, true, mFormatChangeObserver);
 
         setDateFormat();
         updateTime();
         setShowSeconds(mShowSeconds);
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         updateLocation();
         new GetCurrentWheather().execute();
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
         if (!mRegistered) return;
         mRegistered = false;
 
         unregisterReceiver(mIntentReceiver);
         getContentResolver().unregisterContentObserver(mFormatChangeObserver);
         setShowSeconds(false);
 	}
 	
     private class FormatChangeObserver extends ContentObserver {
         public FormatChangeObserver() {
             super(new Handler());
         }
         @Override
         public void onChange(boolean selfChange) {
             setDateFormat();
             updateTime();
         }
     }
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
   /**
      * @return true if clock is set to 24-hour mode
      */
     private boolean get24HourMode() {
         return android.text.format.DateFormat.is24HourFormat(this);
     }
     
    
     
     void updateTime(Calendar c) {
         mCalendar = c;
         updateTime();
     }
     
     private final static String M12 = "h:mm";
     private final static String M24 = "kk:mm";
 
     private void updateTime() {
         mCalendar.setTimeInMillis(System.currentTimeMillis());
 
         CharSequence newTime = DateFormat.format(mFormat, mCalendar);
         mTimeDisplay.setText(newTime);
         CharSequence newSec = DateFormat.format(":ss", mCalendar);
     	//mSecondsView.setText(""+mCalendar.get(Calendar.SECOND));
     	mSecondsView.setText(newSec);
     }
     
     private void updateSeconds() {
         mCalendar.setTimeInMillis(System.currentTimeMillis());
     }
     
     public void setShowSeconds(boolean show) {
     	mShowSeconds = show;
     	if (mShowSeconds) {
     		mSecondsView.setVisibility(View.VISIBLE);
     		mSecRunnable = new Runnable(){
     			@Override
     			public void run() {
     				if (mShowSeconds) {
     					updateTime();
     					mHandler.postDelayed(this, 1000);
     				}
     			}
     		};
     		mSecRunnable.run();
     	} else {
     		mSecondsView.setVisibility(View.INVISIBLE);
     		mHandler.removeCallbacks(mSecRunnable);
     		mSecRunnable = null;
     	}
     }
 
     private void setDateFormat() {
         mFormat = get24HourMode() ? M24 : M12;
         if (get24HourMode()) {
         	mAmPm.setVisibility(View.INVISIBLE);
         } else 
         	mAmPm.setVisibility(View.VISIBLE);
         
         if (mCalendar.get(Calendar.AM_PM) == 0) {
             mAmView.setVisibility(View.VISIBLE);	
             mPmView.setVisibility(View.INVISIBLE);	
         } else {
             mAmView.setVisibility(View.INVISIBLE);	
             mPmView.setVisibility(View.VISIBLE);	
         }
     }
     
     
     
 	LocationManager locationManager;
 	private Location currentLocation;
 	private static final int TWO_MINUTES = 1000 * 60 * 2;
 	/** Determines whether one Location reading is better than the current Location fix
 	  * @param location  The new Location that you want to evaluate
 	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 	  */
 	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 	    if (currentBestLocation == null) {
 	        // A new location is always better than no location
 	        return true;
 	    }
 
 	    // Check whether the new location fix is newer or older
 	    long timeDelta = location.getTime() - currentBestLocation.getTime();
 	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 	    boolean isNewer = timeDelta > 0;
 
 	    // If it's been more than two minutes since the current location, use the new location
 	    
 	    // because the user has likely moved
 	    if (isSignificantlyNewer) {
 	        return true;
 	    // If the new location is more than two minutes older, it must be worse
 	    } else if (isSignificantlyOlder) {
 	        return false;
 	    }
 
 	    // Check whether the new location fix is more or less accurate
 	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 	    boolean isLessAccurate = accuracyDelta > 0;
 	    boolean isMoreAccurate = accuracyDelta < 0;
 	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 	    // Check if the old and new location are from the same provider
 	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
 	            currentBestLocation.getProvider());
 
 	    // Determine location quality using a combination of timeliness and accuracy
 	    if (isMoreAccurate) {
 	        return true;
 	    } else if (isNewer && !isLessAccurate) {
 	        return true;
 	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
 	        return true;
 	    }
 	    return false;
 	}
 	private boolean isSameProvider(String provider1, String provider2) {
 	    if (provider1 == null) {
 	      return provider2 == null;
 	    }
 	    return provider1.equals(provider2);
 	}
     
 	public void updateCurrentLocation(Location loc) throws IOException, IllegalArgumentException {
		Log.d(TAG, "updateCurrentLocation");
 		if ((loc != null) && isBetterLocation(loc, currentLocation)) {
 		    currentLocation = loc;
 		}
 	}
 	
 	//User location updates
 	private void updateLocation() {
 		List<String> providers = locationManager.getAllProviders();
 		for(String provider: providers) {
 		    Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
 		    if (lastKnownLocation != null) {
			    try {updateCurrentLocation
			        (lastKnownLocation);
 			    	break;
 		        } catch (IllegalArgumentException e) {
 			        // TODO Auto-generated catch block
 			        e.printStackTrace();
 		        } catch (IOException e) {
 			        // TODO Auto-generated catch block
 			        e.printStackTrace();
 		        }
 		    }
 		}
 		for(String provider: providers) {
 			if (!provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
 			    Log.d(TAG, "provider:"+provider);
 			    locationManager.requestLocationUpdates(provider, 0, 0, this);
 			}
 		}
 		
 		
 		/*
 		    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
 		    if (lastKnownLocation == null) {
 		        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		    }
 		    if (lastKnownLocation == null) {
 		        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	        }
 
 		    try {
 			    dookan.updateCurrentLocation(lastKnownLocation);
 		    } catch (IllegalArgumentException e) {
 			    // TODO Auto-generated catch block
 			    e.printStackTrace();
 		    } catch (IOException e) {
 			    // TODO Auto-generated catch block
 			    e.printStackTrace();
 		    }
 		
 		    // Register the listener with the Location Manager to receive location updates
 		    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
 		    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
 		    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		*/
 	}
 	
     public void onLocationChanged(Location location) {
 		// Called when a new location is found by the network location
 		// provider.
 		try {
 			updateCurrentLocation(location);
 			if ((location.getProvider() == LocationManager.GPS_PROVIDER) ||
 			    (location.getProvider() == LocationManager.PASSIVE_PROVIDER) ||
 			    (location.getProvider() == LocationManager.NETWORK_PROVIDER))
 				locationManager.removeUpdates(this);
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void onStatusChanged(String provider, int status, Bundle extras) {}
     public void onProviderEnabled(String provider) {}
     public void onProviderDisabled(String provider) {}
 
     
     
 	Weather currentWeather;
 	private class GetCurrentWheather extends AsyncTask<Location, Void, Weather> {
 		protected Weather doInBackground(final Location... args) {
			if (currentLocation == null)
				return null;
 			String linkk = "http://api.wunderground.com/api/8885b6e346d54a79/conditions/q/"+
 					currentLocation.getLatitude()+","+ currentLocation.getLongitude()+".json";
 			AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
 			HttpGet request = new HttpGet(linkk);   
 			BufferedReader reader;
 			HttpResponse response;
 			try {
 				response = client.execute(request);
 				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 				StringBuilder builder = new StringBuilder();
 				for (String line = null; (line = reader.readLine()) != null;) {
 				    builder.append(line).append("\n");
 				}
 				JSONTokener tokener = new JSONTokener(builder.toString());
 				JSONObject finalResult = new JSONObject(tokener);
				Log.d(TAG, finalResult.toString());
 				currentWeather = new Weather(finalResult.getJSONObject("current_observation"));
 				client.close();
 				return currentWeather;
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 
 		}
 		protected void onPostExecute(Weather result) {
 			updateWeather(result);
 		}
 	}
 	
 	public void updateWeather() {
 		updateWeather(currentWeather);
 	}
 	public void updateWeather(Weather result) {
		if (result == null) {
			return;
		}
 		currentTemp.setText(""+result.currentTempInF+"\u2109");
 		overallCondition.setText(result.overallCondition);
 		feelsLike.setText("Feels like "+result.feelsLikeInF +"\u2109");
 		
 		if (result.currentTempInF < 55) {
 			overallCondition.setTextColor(0xbF9999FF);
 			feelsLike.setTextColor(0xbF9999FF);
 			currentTemp.setTextColor(0xbF9999FF);
 			mTimeDisplay.setTextColor(0xbF9999FF);
 			mAmView.setTextColor(0xbF9999FF);
 			mPmView.setTextColor(0xbF9999FF);
 			mSecondsView.setTextColor(0xbF9999FF);
 		}
 		
 		if (result.currentTempInF > 75) {
 			overallCondition.setTextColor(0xbFff9900);
 			feelsLike.setTextColor(0xbfff9900);
 			currentTemp.setTextColor(0xbfff9900);
 			mTimeDisplay.setTextColor(0xbfff9900);
 			mAmView.setTextColor(0xbfff9900);
 			mPmView.setTextColor(0xbfff9900);
 			mSecondsView.setTextColor(0xbfff9900);
 		}
 	}
 	
 	private class Weather {
         double currentTempInF; //"temp_f": 50.2,
         String overallCondition; //"weather": "Clear",
         String humidity; //"relative_humidity": "50%",
         String wind;//"wind_string": "Calm",
         double feelsLikeInF;// "feelslike_f": "50.2",
         double visibilityInMiles; //"visibility_mi": "10.0",
         
 		public Weather(JSONObject ja) {
         	try {
         		currentTempInF = ja.getDouble("temp_f");
         		feelsLikeInF = Double.parseDouble(ja.getString("feelslike_f"));
         		visibilityInMiles = Double.parseDouble(ja.getString("visibility_mi"));
         		overallCondition = ja.getString("weather");
         		humidity = ja.getString("relative_humidity");
         		wind = ja.getString("wind_string");
         	} catch (JSONException e) {
         		// TODO Auto-generated catch block
         		e.printStackTrace();
 			}
         }
 	}
 }

 package com.kinfong.weather;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.RotateAnimation;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 
 /**
  * Class to do really cool stuff with the weather.
  * @author Kin
  */
 public class MainActivity extends Activity implements FragmentManager.OnBackStackChangedListener {
 
     public static final String API_KEY = "0692d0f09a1e18c05539495deed088d6";
 
     private static WeatherData mData;
 
     boolean mIsBound;
 
     private Handler mHandler = new Handler();
 
     private static boolean mShowingMain = false;
 
     private static boolean readyToFlip = false;
 
     private BroadcastReceiver mRetrieveLocationReceiver;
     private BroadcastReceiver mGpsDialogReceiver;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.loading_screen);
 
         doBindService();
 
         mGpsDialogReceiver = new GpsDialogReceiver();
 
         mRetrieveLocationReceiver = new RetrieveLocationReceiver();
 
         if (savedInstanceState == null) {
 
             getFragmentManager()
                     .beginTransaction()
                     .add(R.id.container, new LoadingScreenFragment(), "LoadingScreenFragment")
                     .commitAllowingStateLoss();
         } else {
             mShowingMain = (getFragmentManager().getBackStackEntryCount() > 0);
         }
 
         getFragmentManager().addOnBackStackChangedListener(this);
     }
 
     public class RetrieveLocationReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             Bundle b = intent.getExtras();
             Location location = (Location) b.get("location");
             retrieveLocation(location);
         }
     }
 
     public class GpsDialogReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             showGPSAlert(context);
         }
     }
 
     /**
      * Show alert dialog to allow user to enable GPS
      */
     protected void showGPSAlert(final Context context) {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                 context, 4);
         alertDialogBuilder
                 .setMessage(
                         "GPS is disabled on your device. Would you like to enable it?")
                 .setCancelable(false)
                 .setPositiveButton("Open Settings",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 // set intent to open settings
                                 Intent callGPSSettingIntent = new Intent(
                                         android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                 context.startActivity(callGPSSettingIntent);
                             }
                         })
                 .setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                             }
                         });
         AlertDialog alert = alertDialogBuilder.create();
         alert.show();
     }
 
     private void flipCard() {
         if (mShowingMain) {
             getFragmentManager().popBackStack();
             return;
         }
 
         mShowingMain = true;
 
         getFragmentManager()
                 .beginTransaction()
 
                 .setCustomAnimations(
                         R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                         R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                 .replace(R.id.container, new MainFragment(), "MainFragment")
                 .addToBackStack(null)
                 .commitAllowingStateLoss();
 
         mHandler.post(new Runnable() {
             @Override
             public void run() {
                 invalidateOptionsMenu();
             }
         });
     }
 
     @Override
     public void onBackStackChanged() {
         mShowingMain = (getFragmentManager().getBackStackEntryCount() > 0);
 
         invalidateOptionsMenu();
     }
 
     /**
      * A fragment representing the front of the card.
      */
     public class LoadingScreenFragment extends Fragment {
 
         public LoadingScreenFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.loading_screen, container, false);
             ImageView logo = (ImageView) rootView.findViewById(R.id.logo);
 
             final float ROTATE_FROM = 0.0f;
             final float ROTATE_TO = -1.0f * 360.0f;
 
             RotateAnimation r;
             r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
             r.setStartOffset(1000);
             r.setDuration((long) 2500);
             r.setRepeatCount(-1);
             logo.startAnimation(r);
 
             checkIfReadyToFlip();
 
             final TextView loadingScreenText = (TextView) rootView.findViewById(R.id.loading_screen_text);
 
             loadingScreenText.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     loadingScreenText.setText("Looking for location...");
                 }
             }, 10000);
             loadingScreenText.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     loadingScreenText.setText("");
                 }
             }, 20000);
             loadingScreenText.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     loadingScreenText.setText("Still looking for location...");
                 }
             }, 30000);
             loadingScreenText.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     loadingScreenText.setText("There was most likely a problem. You should reset the app.");
                 }
             }, 100000);
 
             return rootView;
         }
     }
 
     @Override
     public void onBackPressed() {
         if(popupUp) {
             popupWindow.dismiss();
             popupUp = false;
         } else {
             finish();
         }
     }
 
     static PopupWindow popupWindow;
     static boolean popupUp;
     /**
      * A fragment representing the back of the card (MainActivity).
      */
     public class MainFragment extends Fragment {
 
         ImageView mainImage;
         TextView mainText;
         TextView temperatureText;
         ImageView hourlyIcon;
         TextView hourlySummary;
         ImageView dailyIcon;
         TextView dailySummary;
         TextView highTemp;
         TextView lowTemp;
         TextView forecastIo;
 
         public MainFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             final View rootView = inflater.inflate(R.layout.activity_main, container, false);
             mainImage = (ImageView) rootView.findViewById(R.id.main_image);
             mainText = (TextView) rootView.findViewById(R.id.main_text);
             temperatureText = (TextView) rootView.findViewById(R.id.temperature);
 
             hourlyIcon = (ImageView) rootView.findViewById(R.id.hourly_icon);
             hourlySummary = (TextView) rootView.findViewById(R.id.hourly_summary);
             dailyIcon = (ImageView) rootView.findViewById(R.id.daily_icon);
             dailySummary = (TextView) rootView.findViewById(R.id.daily_summary);
 
             mainText.setText(mData.getMinutelySummary());
             temperatureText.setText(mData.getCurrentlyTemperature());
             mainImage.setImageDrawable(findIcon(mData.getMinutelyIcon()));
 
             final ImageView popupButton = (ImageView) rootView.findViewById(R.id.popup_button);
             popupButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     LayoutInflater layoutInflater
                             = (LayoutInflater) rootView.getContext()
                             .getSystemService(LAYOUT_INFLATER_SERVICE);
                     final View popupView = layoutInflater.inflate(R.layout.popup, null);
 
                     popupWindow = new PopupWindow(
                             popupView,
                             ViewGroup.LayoutParams.WRAP_CONTENT,
                             ViewGroup.LayoutParams.WRAP_CONTENT);
 
                     popupUp = true;
 
                     hourlyIcon = (ImageView) popupView.findViewById(R.id.hourly_icon);
                     hourlySummary = (TextView) popupView.findViewById(R.id.hourly_summary);
                     dailyIcon = (ImageView) popupView.findViewById(R.id.daily_icon);
                     dailySummary = (TextView) popupView.findViewById(R.id.daily_summary);
                     highTemp = (TextView) popupView.findViewById(R.id.high_temp);
                     lowTemp = (TextView) popupView.findViewById(R.id.low_temp);
                     forecastIo = (TextView) popupView.findViewById(R.id.forecast_io);
 
                     hourlyIcon.setImageDrawable(findIcon(mData.getHourlyIcon()));
                     hourlySummary.setText(mData.getHourlySummary());
                     dailyIcon.setImageDrawable(findIcon(mData.getDailyIcon()));
                     dailySummary.setText(mData.getDailySummary());
                     highTemp.setText("H: " + mData.getHighTemp());
                     lowTemp.setText("L: " + mData.getLowTemp());
                     forecastIo.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forecast.io"));
                             startActivity(intent);
                         }
                     });
 
                     ImageButton unPopup = (ImageButton)popupView.findViewById(R.id.un_popup);
                     unPopup.setOnClickListener(new Button.OnClickListener(){
 
                         @Override
                         public void onClick(View v) {
                             popupWindow.dismiss();
                             popupUp = false;
                         }});
 
                     popupWindow.showAtLocation(popupButton, 119, 0, 0);
 
                 }});
 
 
             final ImageView refreshButton = (ImageView) rootView.findViewById(R.id.refresh);
             refreshButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     readyToFlip = false;
                     flipCard();
                     mBoundService.doRetrieveLocation();
                 }
             });
 
             return rootView;
         }
 
         /**
          * Returns appropriate icon depending on weather conditions.
          * @param input string describing weather conditions
          * @return Drawable icon that matches weather conditions.
          */
         public Drawable findIcon(String input) {
             Drawable d;
             if(input.equals("clear-day")) {
                 d = getResources().getDrawable(R.drawable.clear_day);
             }else if(input.equals("clear-night")) {
                 d = getResources().getDrawable(R.drawable.clear_night);
             }else if(input.equals("rain")) {
                 d = getResources().getDrawable(R.drawable.rain);
             }else if(input.equals("snow")) {
                 d = getResources().getDrawable(R.drawable.snow);
             }else if(input.equals("sleet")) {
                 d = getResources().getDrawable(R.drawable.sleet);
             }else if(input.equals("wind")) {
                 d = getResources().getDrawable(R.drawable.wind);
             }else if(input.equals("fog")) {
                 d = getResources().getDrawable(R.drawable.fog);
             }else if(input.equals("cloudy")) {
                 d = getResources().getDrawable(R.drawable.cloudy);
             }else if(input.equals("partly-cloudy-day")) {
                 d = getResources().getDrawable(R.drawable.partly_cloudy_day);
             }else if(input.equals("partly-cloudy-night")) {
                 d = getResources().getDrawable(R.drawable.partly_cloudy_night);
             }else {
                 d = getResources().getDrawable(R.drawable.weather_default);
             }
             return d;
         }
 
     }
 
     private static LocationService mBoundService;
 
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder binder) {
             mBoundService = ( (LocationService.LocationBinder) binder).getService();
 
             mBoundService.doRetrieveLocation();
         }
         public void onServiceDisconnected(ComponentName className) {
             mBoundService = null;
         }
     };
 
     @Override
     protected void onResume() {
         super.onResume();
        if(getFragmentManager().getBackStackEntryCount() >= 1) {
             mShowingMain = true;
         } else {
             mShowingMain = false;
         }
         readyToFlip = false;
         checkIfReadyToFlip();
         doBindService();
         registerReceiver(mRetrieveLocationReceiver, new IntentFilter("retrieveLocation"));
         registerReceiver(mGpsDialogReceiver, new IntentFilter("showGpsDialog"));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         doUnbindService();
         unregisterReceiver(mRetrieveLocationReceiver);
         unregisterReceiver(mGpsDialogReceiver);
     }
 
     void doBindService() {
         bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
         mIsBound = true;
     }
 
     void doUnbindService() {
         if (mIsBound) {
             unbindService(mConnection);
             mIsBound = false;
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         doUnbindService();
         readyToFlip = false;
     }
 
 
 
     public static void retrieveLocation(Location location) {
         new FetchForecastData(location.getLatitude(), location.getLongitude(), API_KEY);
     }
 
     public static void retrieveForecastData(JSONObject data) {
         extractData(data);
         readyToFlip = true;
     }
 
     /**
      * Waits for data to be parsed before moving on.
      * @param interval long time to delay
      */
     private void checkIfReadyToFlip(long interval) {
         final Handler h = new Handler();
         h.postDelayed(new Runnable() {
             @Override
             public void run() {
                 if (readyToFlip) {
                     flipCard();
                     h.removeCallbacks(this);
                 } else {
                     checkIfReadyToFlip();
                 }
             }
         }, interval);
     }
     private void checkIfReadyToFlip() {
         checkIfReadyToFlip(0);
     }
 
     /**
      * Pulls relevant info from JSONObject
      * @param jRoot JSONObject from Forecast API
      */
     public static void extractData(JSONObject jRoot) {
         mData = new WeatherData();
         try{
             mData.setLatitude(jRoot.getString("latitude"));
             mData.setLongitude(jRoot.getString("longitude"));
             JSONObject currentlyObject = jRoot.getJSONObject("currently");
             mData.setCurrentlyTime(currentlyObject.getString("time"));
             mData.setCurrentlySummary(currentlyObject.getString("summary"));
             mData.setCurrentlyIcon(currentlyObject.getString("icon"));
             mData.setCurrentlyTemperature(currentlyObject.getString("temperature"));
             JSONObject minutelyObject = jRoot.getJSONObject("minutely");
             mData.setMinutelySummary(minutelyObject.getString("summary"));
             mData.setMinutelyIcon(minutelyObject.getString("icon"));
             JSONObject hourlyObject = jRoot.getJSONObject("hourly");
             mData.setHourlySummary(hourlyObject.getString("summary"));
             mData.setHourlyIcon(hourlyObject.getString("icon"));
             JSONObject dailyObject = jRoot.getJSONObject("daily");
             mData.setDailySummary(dailyObject.getString("summary"));
             mData.setDailyIcon(dailyObject.getString("icon"));
             JSONArray dailyDataArray = dailyObject.getJSONArray("data");
             JSONObject dailyDataObject = dailyDataArray.getJSONObject(0);
             mData.setHighTemp(dailyDataObject.getString("temperatureMax"));
             mData.setLowTemp(dailyDataObject.getString("temperatureMin"));
         }catch (Exception e) {
             e.printStackTrace();
         }
     }
 }

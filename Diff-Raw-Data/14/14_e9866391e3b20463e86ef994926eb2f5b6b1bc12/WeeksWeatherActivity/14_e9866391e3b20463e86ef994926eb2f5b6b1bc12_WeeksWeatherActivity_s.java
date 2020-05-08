 package com.test.weatherapp.activities;
 
 import android.app.ProgressDialog;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.ListView;
 import com.test.weatherapp.R;
 import com.test.weatherapp.adapters.WeeksWeatherListAdapter;
 import com.test.weatherapp.callbacks.WeatherAppServiceCallback;
 import com.test.weatherapp.database.WeatherAppDBContract;
 import com.test.weatherapp.service.WeatherAppServiceHelper;
 import com.test.weatherapp.util.WeatherAppConstants;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Aditya Agarwal
  * Date: 12/9/12
  */
 public class WeeksWeatherActivity extends BaseActivity implements WeatherAppServiceCallback {
 
     private static final String LOG_TAG = "WeeksWeatherActivity";
     private String location;
     private ProgressDialog progress_spinner;
     private Cursor cursor;
 
     @Override
     public String getTag() {
         return LOG_TAG;
     }
 
     @Override
     public WeatherAppServiceCallback getServiceCallback() {
         return this;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.weeks_weather);
         Bundle extras = getIntent().getExtras();
 
         if(extras !=null) {
             location = extras.getString(WeatherAppConstants.EXTRAS_LOCATION);
         }
     }
 
     @Override
     public void onResume(){
         super.onResume();
         WeatherAppServiceHelper.getInstance(this).getWeeklyWeather(location);
         progress_spinner = ProgressDialog.show(this, "Please wait", "Getting this weeks weather ..", true);
     }
 
     @Override
     public void onPause(){
         super.onResume();
         progress_spinner.dismiss();
     }
 
     @Override
     public void onStop(){
         super.onStop();
         WeatherAppServiceHelper.getInstance(this).stopService();
         cursor.close();
     }
 
     @Override
     public void onError(String msg) {
         progress_spinner.dismiss();
     }
 
     @Override
     public void onComplete() {
 


         cursor = getContentResolver().query(WeatherAppDBContract.Weather.CONTENT_URI, projection, null, null, null);
         ListView weeksWeatherList = (ListView)findViewById(R.id.weeks_weather_list_view);
 
         WeeksWeatherListAdapter adapter = new WeeksWeatherListAdapter(this,cursor);
         weeksWeatherList.setAdapter(adapter);
         progress_spinner.dismiss();

     }
 }

 package org.gots.weather.service;
 
 import java.util.Calendar;
 
 import org.gots.broadcast.BroadCastMessages;
 import org.gots.weather.WeatherConditionInterface;
 import org.gots.weather.provider.previmeteo.PrevimeteoWeatherProvider;
 import org.gots.weather.provider.previmeteo.WeatherProvider;
 import org.gots.weather.sql.WeatherDBHelper;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class WeatherUpdateService extends Service {
     private static final String TAG = "WeatherUpdateService";
 
     private Intent intent;
 
     private final Handler handler = new Handler();
 
     private boolean isWeatherError = false;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         intent = new Intent(BroadCastMessages.WEATHER_DISPLAY_EVENT);
 
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "getWeatherFromWebService");
 
         // TODO change to async task and update UI in postExecute
         getWeatherFromWebService();
 
        

         return super.onStartCommand(intent, flags, startId);
     }
 
     private Runnable sendUpdatesToUI = new Runnable() {
         public void run() {
             displayWeatherAvailable();
             // handler.postDelayed(this, 5000); // 5 seconds
         }
     };
 
     private void displayWeatherAvailable() {
         Log.d(TAG, "entered displayWeatherAvailable");
 
         intent.putExtra("error", isWeatherError);
         // intent.putExtra("counter", String.valueOf(++counter));
         sendBroadcast(intent);
         stopSelf();
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "onDestroy");
         super.onDestroy();
     }
 
     public void getWeatherFromWebService() {
 
         try {
             for (int forecastDay = 0; forecastDay < 4; forecastDay++) {
 
                 // GoogleWeatherTask(garden.getAddress(), cal.getTime());
                 // WeatherTask wt = new PrevimeteoWeatherProvider(this, garden.getAddress(), cal.getTime());
                 // wt.execute();
 
                new AsyncTask<Integer, Integer, Void>() {
 
                     protected void onPreExecute() {
                     };
 
                     @Override
                    protected Void doInBackground(Integer... params) {
                         Calendar cal = Calendar.getInstance();
                         cal.add(Calendar.DAY_OF_YEAR, params[0]);
                        WeatherProvider previmeteoWeatherProvider = new PrevimeteoWeatherProvider(getApplicationContext());
                        previmeteoWeatherProvider.getCondition(cal.getTime());
                        return null;
                     }
 
                     @Override
                    protected void onPostExecute(Void result) {
                         handler.removeCallbacks(sendUpdatesToUI);
                         handler.postDelayed(sendUpdatesToUI, 0); // 1 second=1000
                        super.onPostExecute(result);
 
                     }
                 }.execute(forecastDay);
                 // if (conditionInterface != null) {
                 // updateCondition(conditionInterface, forecastDay);
                 //
                 // }
                 //
                 // else {
                 // // Toast.makeText(mContext,
                 // // mContext.getResources().getString(R.string.weather_citynotfound),
                 // // 50)
                 // // .show();
                 // Log.d(TAG, garden.getLocality() + " : " + getResources().getString(R.string.weather_citynotfound));
                 // isWeatherError = true;
                 // break;
                 // }
 
             }
 
         } catch (Exception e) {
             if (e.getMessage() != null)
                 Log.e(TAG, e.getMessage());
             Toast.makeText(this, "Try another nearest city", Toast.LENGTH_SHORT).show();
         }
 
     }
 
     private void updateCondition(WeatherConditionInterface condition, int day) {
         WeatherDBHelper helper = new WeatherDBHelper(this);
 
         Calendar conditionDate = Calendar.getInstance();
         conditionDate.add(Calendar.DAY_OF_YEAR, day);
 
         condition.setDate(conditionDate.getTime());
         condition.setDayofYear(conditionDate.get(Calendar.DAY_OF_YEAR));
 
         WeatherConditionInterface wc = helper.getWeatherByDayofyear(conditionDate.get(Calendar.DAY_OF_YEAR));
 
         if (wc == null)
             helper.insertWeather(condition);
         else
             helper.updateWeather(condition);
         return;
 
     }
 }

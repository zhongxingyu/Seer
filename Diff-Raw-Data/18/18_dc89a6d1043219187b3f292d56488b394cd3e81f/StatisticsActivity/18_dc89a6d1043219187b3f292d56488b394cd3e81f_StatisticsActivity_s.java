 package com.chips;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.chips.dataclient.DataPushClient;
 import com.chips.dataclient.StatisticsClient;
 import com.chips.homebar.HomeBar;
 import com.chips.homebar.HomeBarAction;
 import com.chips.user.PersistentUser;
 import com.customwidget.numberpicker.NumberPicker;
 
 public class StatisticsActivity extends Activity implements HomeBar {
     private static final String BASE_URL 
         = "http://cs110chips.phpfogapp.com/index.php/mobile/";
     private static final String GET_MOST_RECENT_STATISTICS
          = BASE_URL + "get_most_recent_user_statistics/";
     private static final String ADD_STATISTICS
     = BASE_URL + "add_new_user_statistics/";
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         HomeBarAction.inflateHomeBarView(this, R.layout.statistics);
         
         pushClient = new DataPushClient();
         setupNumberPickers();
     }
     
     private void setupNumberPickers() {
         statisticsClient = new StatisticsClient();
         statisticsClient.setURL(GET_MOST_RECENT_STATISTICS, 
                 PersistentUser.getSessionID());
         statisticsClient.synchronousLoadClientData();
         
         weightPicker = (NumberPicker) findViewById(R.id.weightPicker);
         heightPicker = (NumberPicker) findViewById(R.id.heightPicker);
         weightPicker.setCurrent(statisticsClient.getWeight());
         heightPicker.setCurrent(statisticsClient.getHeight());
     }
     
     // super calls for basic activity-changing functions.
     @Override
     protected void onStart() {
         super.onStart();
         // The activity is about to become visible.
     }
         
     @Override
     protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         // Another activity is taking focus (this activity is about to be "paused").
     }
     
     @Override
     protected void onStop() {
         super.onStop();
         // The activity is no longer visible (it is now "stopped")
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         // The activity is about to be destroyed.
     }
     
     public void goHomeClicked(View view) {
         HomeBarAction.goHomeClicked(this, view);
     }
     
     public void addFavoriteClicked(View view) {
         HomeBarAction.addFavoriteClicked(this, view);
     }
     
     public void saveStatisticsClicked(View view) {
         ArrayList<String> arguments = new ArrayList<String>();
         arguments.add(PersistentUser.getSessionID());
         arguments.add(heightPicker.getCurrent() + "");
         arguments.add(weightPicker.getCurrent() + "");
         
         pushClient.setURL(ADD_STATISTICS, arguments);
         pushClient.synchronousLoadClientData();
         
         if (pushClient.lastCompletedPushSuccessful()) {
             finish();
         } else {
             Toast.makeText(this, "Statistics update error", 
                     Toast.LENGTH_LONG).show();
         }
         
     }
     
     private NumberPicker weightPicker;
     private NumberPicker heightPicker;
     private StatisticsClient statisticsClient;
     private DataPushClient pushClient;
 }

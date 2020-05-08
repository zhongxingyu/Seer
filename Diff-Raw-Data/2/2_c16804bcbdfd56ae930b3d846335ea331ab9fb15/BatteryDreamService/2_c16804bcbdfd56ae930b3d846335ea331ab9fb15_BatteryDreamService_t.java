 package pl.patrykgrzegorczyk.batterydaydream.service;
 
 import android.service.dreams.DreamService;
 import android.util.Log;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import pl.patrykgrzegorczyk.batterydaydream.BatteryMonitor;
 import pl.patrykgrzegorczyk.batterydaydream.R;
 
 /**
  * Main DayDream service
  */
 public class BatteryDreamService extends DreamService implements BatteryMonitor.BatteryStateListener {
 
     private static final String TAG = "BatteryDreamService";
 
     private BatteryMonitor mBatteryMonitor;
     private TextView mBatteryLevelMajorView;
     private TextView mBatteryLevelMinorView;
     private ProgressBar mBatteryProgressBar;
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "onAttachedToWindow()");
         }
 
         mBatteryMonitor = new BatteryMonitor(this);
         mBatteryMonitor.setBatteryStateListener(this);
 
         setInteractive(false);
         setFullscreen(true);
         setScreenBright(false);
         setContentView(R.layout.battery_daydream);
 
         mBatteryLevelMajorView = (TextView) findViewById(R.id.battery_level_major);
         mBatteryLevelMinorView = (TextView) findViewById(R.id.battery_level_minor);
 
         mBatteryProgressBar = (ProgressBar) findViewById(R.id.battery_progress);
     }
 
     @Override
     public void onDetachedFromWindow() {
         mBatteryMonitor.setBatteryStateListener(null);
         mBatteryMonitor.setContext(null);
     }
 
     @Override
     public void onDreamingStarted() {
         super.onDreamingStarted();
 
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "onDreamingStarted()");
         }
 
         mBatteryMonitor.startListening();
     }
 
     @Override
     public void onDreamingStopped() {
         super.onDreamingStopped();
 
         if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, "onDreamingStopped()");
         }
 
         mBatteryMonitor.stopListening();
     }
 
     @Override
     public void onBatteryStateChanged(BatteryMonitor.BatteryState batteryState) {
         //Update battery level info
 
 
         int batteryLevel = batteryState.getLevel();
        batteryLevel = 90;
         String batteryLevelText = String.valueOf(batteryLevel);
 
         //First digit of battery progress
         String batteryLevelMajor = batteryLevelText.substring(0, 1);
         //Rest of the digits
         String batteryLevelMinor = new String();
 
         if (batteryLevelText.length() > 1) {
             //battery percentage have more than 1 digit
             batteryLevelMinor = batteryLevelText.substring(1);
         }
 
         mBatteryLevelMajorView.setText(batteryLevelMajor);
         mBatteryLevelMinorView.setText(batteryLevelMinor);
 
         if (batteryLevel <= 10) {
             mBatteryLevelMajorView.setTextColor(getResources().getColor(R.color.battery_low));
             mBatteryLevelMinorView.setTextColor(getResources().getColor(R.color.battery_low));
         } else {
             mBatteryLevelMajorView.setTextColor(getResources().getColor(R.color.battery_normal));
             mBatteryLevelMinorView.setTextColor(getResources().getColor(R.color.battery_normal));
         }
 
         //reset full progress
         mBatteryProgressBar.setProgress(0);
 
         if (batteryLevel == batteryState.getScale()) {
             mBatteryLevelMajorView.setTextColor(getResources().getColor(R.color.battery_fully_charged));
             mBatteryLevelMinorView.setTextColor(getResources().getColor(R.color.battery_fully_charged));
             mBatteryProgressBar.setProgress(batteryLevel);
             return;
         }
 
         mBatteryProgressBar.setSecondaryProgress(batteryLevel);
 
     }
 }

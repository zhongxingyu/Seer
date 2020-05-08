 package idrabenia.worktime.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import idrabenia.worktime.R;
 import idrabenia.worktime.domain.date.Time;
 import idrabenia.worktime.domain.calculation.TimerService;
 import idrabenia.worktime.ui.calculation.BackgroundService;
 import idrabenia.worktime.ui.settings.SettingsActivity;
 
 import java.text.MessageFormat;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class WorkTimeManager extends Activity {
     public static final int REFRESH_PERIOD = 30 * 1000; // in milliseconds
     public static final String ELAPSED_TIME_FORMAT = "{0,number,00}:{1,number,00}";
 
     private TimerService timerService;
     private Timer refreshTimer;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         BackgroundService.start(this);
         timerService = TimerService.getInstance(this);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         scheduleRefreshTimer();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
        refreshTimer.cancel();
     }
 
     private void scheduleRefreshTimer() {
         refreshTimer = new Timer("Refresh Timer Value Timer");
         refreshTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 WorkTimeManager.this.runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         refreshTimeValue();
                     }
                 });
             }
         }, 0, REFRESH_PERIOD);
     }
 
     private void refreshTimeValue() {
         if (timerService == null) {
             return;
         }
 
         Time value = timerService.getTimerValue();
 
         String textValue;
         if (value != null) {
             textValue = MessageFormat.format(ELAPSED_TIME_FORMAT, value.hour, value.minute);
         } else {
             textValue = getString(R.string.zero_time_value);
         }
 
         ((TextView) findViewById(R.id.timer_value)).setText(textValue);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         new MenuInflater(this).inflate(R.menu.time_manager_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean resetTimeCalculator(MenuItem item) {
        new AlertDialog.Builder(this)
                 .setTitle(R.string.timer_reset)
                 .setMessage(R.string.approve_reset_message)
                 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         timerService.reset();
                         refreshTimeValue();
                     }
                 })
                 .setNegativeButton(R.string.no, null)
                .create().show();
 
         return true;
     }
 
     public boolean showSettingsActivity(MenuItem item) {
         Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
         return true;
     }
 
 }
 

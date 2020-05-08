 package com.kinnack.nthings.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.kinnack.nthings.R;
 import com.kinnack.nthings.StopWatch;
 import com.kinnack.nthings.model.SoundAlert;
 
 public class CounterActivity extends Activity {
     public static final String INIT_COUNT_KEY = "com.kinnack.nthings.init_count";
     public static final String SHOW_DONE = "com.kinnack.nthings.show_done";
     public static final String MAX_COUNT = "com.kinnack.nthing.max_count";
     public static final String AVG_TIME = "com.kinnack.nthing.time.avg";
     public static final String TOTAL_TIME = "com.kinnack.nthing.time.total";
     public static final String HISTORY = "com.kinnack.nthing.history";
     public static final String IS_TEST = "com.kinnack.nthing.is_test";
     public static final String USE_SUBCOUNT = "com.kinnack.nthing.use_subcount";
     private int count = 0;
     protected int increment = 1;
     protected int neededCount = 0;
     protected StopWatch stopWatch;
     protected long sumTimeBetweenCounts = 0;
     private boolean useSubcount = false;
     protected SoundAlert soundAlert;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         neededCount = extras.getInt(INIT_COUNT_KEY);
         count = neededCount;
         boolean showDone = extras.getBoolean(SHOW_DONE);
         
         
         if (!showDone) { increment = -1; }
         if (showDone) { count = 0; }
         setContentView(getLayout());
         TextView totalCount = (TextView) findViewById(R.id.TotalCount);
         String countText = (count == 0 ? "at least "+neededCount : count+"");
         if (count == 0 && neededCount == 0) countText = "as many as possible";
         totalCount.setText(countText);
         if (showDone) {
             showQuitingOptions();
         }
         if (extras.getBoolean(USE_SUBCOUNT)) {
             findViewById(R.id.SubCountProgress).setVisibility(View.VISIBLE);
             useSubcount = true;
         }
         soundAlert = new SoundAlert(PreferenceManager.getDefaultSharedPreferences(this), this);
         
         if (extras.getBoolean(IS_TEST)) {
             dialogToUser(R.string.is_test_title,R.string.is_test_msg);
         } else if (showDone) {
             dialogToUser(R.string.is_unlimited_title,R.string.is_unlimited_msg);
         }
         
         stopWatch = new StopWatch();
         stopWatch.start();
     }
 
 
     /**
      * 
      */
     protected void showQuitingOptions() {
         Button done = (Button) findViewById(R.id.Done);
         done.setVisibility(View.VISIBLE);
     }
 
 
     /**
      * @return
      */
     protected int getLayout() {
         return R.layout.counter;
     }
 
 
     /**
      * 
      */
     private void dialogToUser(int title_, int message_) {
         new AlertDialog.Builder(this)
             .setTitle(title_)
             .setMessage(message_)
             .setIcon(R.drawable.dialog)
             .setPositiveButton(R.string.positive, new OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog_, int which_) {
                     dialog_.dismiss();
                 }
             })
             .show();
     }
     
     // ??? Is it necessary to stop the counter each time?
     public void count(View target) {
         incrementProgress();
         
         stopWatch.stop();
         int reps = count;
         if (increment == -1) {
             reps = neededCount - count;
         }
         sumTimeBetweenCounts += stopWatch.getElapsedTime();
         
         
         if ((increment  == -1) && (count == 0)) {
             setResult(RESULT_OK,createIntentWithStats(reps));
             soundAlert.finishedBeep();
             finish();
         }
         stopWatch.start();
         soundAlert.progressBeep();
     }
 
 
     /**
      * 
      */
     private void incrementProgress() {
         TextView totalCount = (TextView) findViewById(R.id.TotalCount);
         totalCount.setText(""+(count+=increment));
 
         ProgressBar progress = (ProgressBar) findViewById(R.id.CountProgress);
         int progressPercent = getProgressPercent();
         
         if (useSubcount) {
             ProgressBar subProgress = (ProgressBar)findViewById(R.id.SubCountProgress);
             subProgress.setProgress((int)Math.floor(progressPercent));
             progress.setProgress((progressPercent % 10)*10);
         } else {
             progress.setProgress(progressPercent);
         }
     }
     
     public void done(View target) {
         stopWatch.stop();
         setResult(RESULT_OK,createIntentWithStats(count));
         finish();
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         soundAlert.cleanup();
     }
     
     private int getProgressPercent() {
         int retval = 0;
         if (increment == -1) {
             
             retval = (int) ((1-count*1.0/neededCount)*100);
         } else {
             double percentLeft = count*1.0/neededCount*100;
             retval = (int) (percentLeft > 100 ? 100 : percentLeft);
         }
         return retval;
     }
     
     protected Intent createIntentWithStats(int reps_) {
         Intent intent = new Intent();
         intent.putExtra(MAX_COUNT, reps_);
        intent.putExtra(AVG_TIME, sumTimeBetweenCounts/reps_);
         intent.putExtra(TOTAL_TIME, sumTimeBetweenCounts);
         return intent;
     }
 }

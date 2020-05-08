 package com.kinnack.nthings;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class CounterActivity extends Activity {
     public static final String INIT_COUNT_KEY = "com.kinnack.nthings.init_count";
     public static final String SHOW_DONE = "com.kinnack.nthings.show_done";
     public static final String MAX_COUNT = "com.kinnack.nthing.max_count";
     public static final String HISTORY = "com.kinnack.nthing.history";
     private int count = 0;
     private int increment = 1;
     private int neededCount = 0;
     private ToneGenerator toneGenerator;
     
    
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         neededCount = extras.getInt(INIT_COUNT_KEY);
         count = neededCount;
         boolean showDone = extras.getBoolean(SHOW_DONE);
 
         
         if (!showDone) { increment = -1; }
         if (showDone) { count = 0; }
         setContentView(R.layout.counter);
         TextView totalCount = (TextView) findViewById(R.id.TotalCount);
         totalCount.setText(""+count);
         if (showDone) {
             Button done = (Button) findViewById(R.id.Done);
             done.setVisibility(View.VISIBLE);
         }
         toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
     }
     
     public void count(View target) {
         TextView totalCount = (TextView) findViewById(R.id.TotalCount);
         ProgressBar progress = (ProgressBar) findViewById(R.id.CountProgress);
         totalCount.setText(""+(count+=increment));
         int progressPercent = getProgressPercent();
         Log.d("nthings:CounterActivity", "Setting progress to "+progressPercent);
         progress.setProgress(progressPercent);
         if ((increment  == -1) && (count == 0)) {
             Intent intent = new Intent();
             intent.putExtra(MAX_COUNT, neededCount);
             setResult(RESULT_OK,intent);
             toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
             finish();
         }
         toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
     }
     
     public void done(View target) {
         Intent intent = new Intent();
         intent.putExtra(MAX_COUNT, count);
         setResult(RESULT_OK,intent);
         Log.d("nthings:CounterActivity","Set "+MAX_COUNT+" to "+count);
         finish();
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         toneGenerator.release();
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
 }

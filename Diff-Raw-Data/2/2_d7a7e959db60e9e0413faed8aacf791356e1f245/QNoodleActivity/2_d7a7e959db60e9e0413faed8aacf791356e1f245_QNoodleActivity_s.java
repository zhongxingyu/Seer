 package com.github.hanachin.qnoodle;
 
 import android.app.Activity;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.text.format.Time;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class QNoodleActivity extends Activity {
 	private Handler handler = new Handler();
 	private Runnable runnable;
     
 	private long DEFAULT_TIME = 3 * 60 * 1000;
 	
 	private Time time;
 	private TextView timeView;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         time = new Time();
         time.set(DEFAULT_TIME);
         
         timeView = (TextView)findViewById(R.id.time);
         runnable = new Runnable() {
 			@Override
 			public void run() {
 				time.set((time.minute * 60 + time.second) * 1000 - 1000);
 				updateTime();
				if (time.second <= 0) {
 					ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
 					tone.startTone(ToneGenerator.TONE_PROP_NACK);
 					
 					Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
 					if (vibrator != null) {
 						long[] pattern = {0, 100, 50, 100, 50, 100};
 						vibrator.vibrate(pattern, -1);
 					}
 					
 					Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_LONG).show();
 					
 					handler.removeCallbacks(this);
 				} else {
 					handler.postDelayed(this, 1000);
 				}
 			}
 		};
 		handler.postDelayed(runnable, 1000);
 		
 		updateTime();
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
 		handler.removeCallbacks(runnable);
     }
     
     private void updateTime() {
     	timeView.setText(time.format("%M:%S"));
     }
 }

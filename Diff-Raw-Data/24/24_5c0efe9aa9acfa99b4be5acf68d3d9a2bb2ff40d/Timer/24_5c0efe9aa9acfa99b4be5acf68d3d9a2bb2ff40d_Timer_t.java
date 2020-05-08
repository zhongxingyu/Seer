 package dk.esmann;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 public class Timer extends Activity {
 
     private static final String TAG = "Timer";
     private int rounds, completedRounds;
     private long remainingMillis;
     private TextView roundTextView, countdownTextView, avgTime;
     private Resources res;
     private CountDownTimer countDownTimer;
    private Boolean timerIsRunning = false;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.timer);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
         //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         Intent intent = getIntent();
         rounds = intent.getIntExtra("rounds", 1);
         completedRounds = 0;
         int hours = intent.getIntExtra("hours", 0);
         int minutes = intent.getIntExtra("minutes", 0);
         roundTextView = (TextView) findViewById(R.id.timerRound);
         countdownTextView = (TextView) findViewById(R.id.countdown);
         avgTime = (TextView)findViewById(R.id.avg_round_time);
         remainingMillis = (((hours * 60) + minutes) * 60) * 1000;
         res = getResources();
         roundTextView.setText(String.format(res.getString(R.string.round_x_of_z), completedRounds+1, rounds));
         countdownTextView.setText(String.format(res.getString(R.string.countdown_timer), getHoursFromMillis(remainingMillis), getMinutesFromMillis(remainingMillis), getSecondsFromMillis(remainingMillis)));
         int remainingRounds = (rounds-completedRounds);
         long avgMillis = remainingMillis/remainingRounds;
         avgTime.setText(String.format(res.getString(R.string.avg_round_time), remainingRounds, getHoursFromMillis(avgMillis), getMinutesFromMillis(avgMillis), getSecondsFromMillis(avgMillis) ));
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public void onStart(View view) {
 
             countDownTimer = new CountDownTimer(remainingMillis, 1000) {
                 public void onTick(long millis) {
                     remainingMillis = millis;
                     int remainingRounds = (rounds-completedRounds);
                    if (remainingRounds == 0) {
                        onFinish();
                        return;
                    }
                     long avgMillis = remainingMillis/remainingRounds;
                     roundTextView.setText(String.format(res.getString(R.string.round_x_of_z), completedRounds+1, rounds));
                     countdownTextView.setText(String.format(res.getString(R.string.countdown_timer), getHoursFromMillis(millis), getMinutesFromMillis(millis), getSecondsFromMillis(millis)));
                     avgTime.setText(String.format(res.getString(R.string.avg_round_time), remainingRounds, getHoursFromMillis(avgMillis), getMinutesFromMillis(avgMillis), getSecondsFromMillis(avgMillis) ));
                 }
 
                 public void onFinish() {
                     countdownTextView.setText("done!");
                 }
             };
         countDownTimer.start();
        timerIsRunning = true;
     }
 
     public void onResume() {
         Log.d(TAG, "resuming");
         super.onResume();
     }
 
     public void onPause() {
         Log.d(TAG, "pausing");
         super.onPause();
     }
     @SuppressWarnings("UnusedDeclaration")
     public void onStop(View view) {
        if (timerIsRunning) {
            if (completedRounds < rounds) {
                completedRounds++;
                Log.d(TAG, "completed rounds: " + completedRounds);
            }
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
            timerIsRunning = false;
        }
     }
 
     private long getHoursFromMillis(long millis) {
         return (millis / (1000 * 60 * 60));
     }
 
     private long getMinutesFromMillis(long millis) {
         return ((millis % (1000 * 60 * 60)) / (1000 * 60));
     }
 
     private long getSecondsFromMillis(long millis) {
         return ((millis % (1000 * 60 * 60)) % (1000 * 60) / 1000);
     }
 }

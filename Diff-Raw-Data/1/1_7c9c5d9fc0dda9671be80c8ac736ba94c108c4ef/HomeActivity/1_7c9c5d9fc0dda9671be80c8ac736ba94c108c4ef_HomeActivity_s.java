 package dk.kleistsvendsen;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.inject.Inject;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import roboguice.activity.RoboActivity;
 
 public class HomeActivity extends RoboActivity {
     public static final int MilliSecsPerMinute = 60 * 1000;
     public static final int MilliSecsPerHalf = 20 * MilliSecsPerMinute;
     @Inject
     private IGameTimer gameTimer_;
 
     private TextView timeLeftText_;
     private TextView timePlayedText_;
 
     private final Handler updateHandler_ = new Handler();
     private final Runnable updateViewRunner_ = new Runnable() {
         @Override
         public void run() {
             updateView();
             // Remove all to make sure we don't have two updateViews running.
             updateHandler_.removeCallbacks(updateViewRunner_); 
             if (gameTimer_.isRunning()) {
                 updateHandler_.postDelayed(this, 50);
             }
         }
     };
     final Calendar calendar_ = Calendar.getInstance();
     private final SimpleDateFormat dateFormatter_ = new SimpleDateFormat("m:ss.SSS");
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_layout);
         connectButtons_();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         gameTimer_.saveState(outState);
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle inState) {
         super.onRestoreInstanceState(inState);
         gameTimer_.restoreState(inState);
     }
 
     private void connectButtons_() {
         Button startButton = (Button) findViewById(R.id.start_button);
         timeLeftText_ = (TextView) findViewById(R.id.timeLeftText);
         timePlayedText_ = (TextView) findViewById(R.id.timePlayedText);
         startButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onStartClick_();
             }
         });
 
         Button pauseButton = (Button) findViewById(R.id.pause_button);
         pauseButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onPauseClick_();
             }
         });
 
         Button resetButton = (Button) findViewById(R.id.reset_button);
         resetButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 showConfirmResetDialog_();
             }
         });
     }
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         int action = event.getAction();
         int keyCode = event.getKeyCode();
         switch (keyCode) {
             case KeyEvent.KEYCODE_VOLUME_UP:
                 if (action == KeyEvent.ACTION_DOWN) {
                     onStartClick_();
                 }
                 return true;
             case KeyEvent.KEYCODE_VOLUME_DOWN:
                 if (action == KeyEvent.ACTION_DOWN) {
                     onPauseClick_();
                 }
                 return true;
             default:
                 return super.dispatchKeyEvent(event);
         }
     }
 
     private void onStartClick_() {
         gameTimer_.startTimer();
         long[] pattern = {0,50,50,50,50,50,50,50,50};
         vibrate_(pattern);
         updateHandler_.post(updateViewRunner_); // Ok to call even if it was running already
     }
 
     private void onPauseClick_() {
         gameTimer_.pauseTimer();
         long[] pattern = {0,200,200,200,200,200};
         vibrate_(pattern);
     }
 
     private void vibrate_(long[] pattern) {
         Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         v.vibrate(pattern, -1);
     }
 
     public void showConfirmResetDialog_() {
         new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle(R.string.reset_timer_question_title)
                 .setMessage(R.string.reset_timer_question)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         gameTimer_.resetTimer();
                     }
 
                 })
                 .setNegativeButton(R.string.cancel, null)
                 .show();
     }
 
     public void updateView() {
         Long played = gameTimer_.timePlayed();
         assert(played<= MilliSecsPerHalf);
         Long left = MilliSecsPerHalf - played;
         timeLeftText_.setText(formatTime_(left));
         timePlayedText_.setText(formatTime_(played));
     }
 
     private String formatTime_(Long left) {
         calendar_.setTimeInMillis(left);
         return dateFormatter_.format(calendar_.getTime());
     }
 }

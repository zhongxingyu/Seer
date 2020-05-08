 /**
  *
  */
 package games.pack.protap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author Tariq
  *
  */
 public class PracticeBoxingActivity extends Activity {
     private int mScore = 0;
     private TextView mTimerView;
     protected TextView mCounterView;
     private static final long MILLIS_IN_FUTURE = 10000;
     private static final long COUNTDOWN_INTERVAL = 100;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.boxing);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         mTimerView = (TextView) findViewById(R.id.timer1);
         mCounterView = (TextView) findViewById(R.id.boxing_counter1);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         final ImageView iv = (ImageView) findViewById(R.id.imageView2);
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Tap the ball as many times as you can in 10s. Press GO to start")
                 .setCancelable(false)
                 .setPositiveButton("GO", new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int id) {
                         new CountDownTimer(MILLIS_IN_FUTURE, COUNTDOWN_INTERVAL) {
                             @Override
                             public void onFinish() {
                                 mTimerView.setText("0.0");
                                 iv.setClickable(false);
                                 end();
                             }
 
                             @Override
                             public void onTick(final long millisUntilFinished) {
                                 mTimerView.setText(Double
                                         .toString((double) millisUntilFinished / 1000));
                             }
                         }.start();
                     }
                 });
         final AlertDialog alert = builder.create();
         alert.show();
     }
 
     protected void end() {
         final int finalScore = Integer.parseInt(mCounterView.getText().toString());
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Your score: " + finalScore).setCancelable(true)
                 .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int id) {
                         finish();
                         startActivity(new Intent(PracticeBoxingActivity.this,
                                 PracticeBoxingActivity.class));
                     }
                 }).setNegativeButton("Return to Practice", new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int id) {
                         finish();
                     }
                 });
        builder.create().show();
     }
 
     public void addTap(final View view) {
         mScore++;
         mCounterView.setText(Integer.toString(mScore));
     }
 }

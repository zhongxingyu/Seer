 package com.ad.cow;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Cow extends Activity {
 	private ProgressBar mProgress;
 	private TextView textView;
 	private SharedPreferences mySharedPreferences;
 	private static String MY_PREFS = "MY_PREFS";
 	private FeedCountDownTimer countDownTimer;
 
	private final double perMinute = 0.083;
 	private final double percentByFood = 1.2;
 	private final long interval = 1000;
 
 	private int percent;
 	private long time;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		loadPreferences();
 	}
 
 	private void loadPreferences() {
 		int mode = Activity.MODE_PRIVATE;
 		long currentTime = new Date().getTime();
 
 		mySharedPreferences = getSharedPreferences(MY_PREFS, mode);
 		percent = mySharedPreferences.getInt("percent", 0);
 		time = mySharedPreferences.getLong("time", currentTime);
 
 		long diff = currentTime - time;
		double minutes = diff / 1000 / 60;

		double eatenFood = minutes * perMinute;
 		int cutPercent = (int) Math.round(eatenFood / percentByFood);
 		int newPercent = percent - cutPercent;
 
 		percent = Math.max(0, newPercent);
 
 		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
 		mProgress.setProgress(percent);
 
 		textView = (TextView) findViewById(R.id.textView1);
 
		long timer = (long) ((percent * percentByFood) / perMinute) * 60 * 1000;
 		countDownTimer = new FeedCountDownTimer(timer, interval);
 		countDownTimer.start();
 	}
 
 	public void feed(View view) {
 		int newPercent = mProgress.getProgress() + 10;
 
 		if (newPercent <= 100) {
 			SharedPreferences.Editor editor = mySharedPreferences.edit();
 			editor.putInt("percent", newPercent);
 			editor.putLong("time", new Date().getTime());
 			editor.commit();
 
 			mProgress.setProgress(newPercent);
 
 			countDownTimer.cancel();
			long timer = (long) ((newPercent * percentByFood) / perMinute) * 60 * 1000;
 			countDownTimer = new FeedCountDownTimer(timer, interval);
 			countDownTimer.start();
 		}
 
 		if (newPercent > 50) {
 			Toast.makeText(this,
 					"Ваша корова сыта. Приходите когда она проголодается!",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private class FeedCountDownTimer extends CountDownTimer {
 		public FeedCountDownTimer(long startTime, long interval) {
 			super(startTime, interval);
 		}
 
 		@Override
 		public void onFinish() {
 			textView.setText("Коровка умерла!");
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
			String text = "";
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
 			Date resultdate = new Date(millisUntilFinished);
			text = sdf.format(resultdate);
 
			textView.setText("Возвращайтесь скорее: " + text);
 		}
 	}
 }

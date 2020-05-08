 package com.ad.cow;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ExperienceActivity extends AbstractActivity {
 
 	private SharedPreferences mySharedPreferences;
 	private long time;
	private float newExp;
	private float exp;
 	/**
 	 * Старт активности
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.experience);
 
 		loadPreferences();
 	}
 
 	private void loadPreferences() {
 		int mode = Activity.MODE_PRIVATE;
 		long currentTime = new Date().getTime();
 
 		// Достаем сохраненные данные
 		mySharedPreferences = getPreferences(mode);
		exp  = mySharedPreferences.getFloat("exp", 0.0f);
 		time = mySharedPreferences.getLong("time", currentTime);
 		
 		long diff = currentTime - time; 
 		float hours = diff / 1000 / 60 / 60 ;
		float expPerHour = hours * 10;
		newExp = expPerHour + exp; 
 
 		TextView textView = (TextView) findViewById(R.id.textView1);
		textView.setText("У вас " + newExp + " опыта");
 		
 		TextView textView2 = (TextView) findViewById(R.id.textView2);
 		textView2.setText("У вас " + expPerHour+ " опыта в час");
 		
 		TextView textView3 = (TextView) findViewById(R.id.textView3);
		textView3.setText("У вас " + exp + " было опыта");
 		
 		int percent = 60; 
 		ProgressBar progressView = (ProgressBar) findViewById(R.id.progressBar1);
 		progressView.setProgress((int)percent);
 	}
 
 	/**
 	 * При завершении экшена сохраняем данные
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putFloat("exp", newExp);
 		editor.putLong("time", new Date().getTime());
 		editor.commit();
 	}
 }

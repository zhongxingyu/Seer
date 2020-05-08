 package com.tufts.wmfo;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CalendarView;
 import android.widget.CalendarView.OnDateChangeListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 public class MainActivity extends TabActivity {
 
 	boolean isLive;
 	
 	Timer checkStateTimer = null;
 
 	final static String TAG = "WMFO:SERVICE";
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) 
 	{
 		super.onSaveInstanceState(savedInstanceState);
 
 		savedInstanceState.putBoolean("isLive", this.isLive);
 
 	}
 
 	@Override
 	public void onResume(){
 		super.onResume();
 		//Make sure the volume bar is accurate
 		runOnUiThread(new Runnable(){
 			@Override
 			public void run() {
 				SeekBar volumeBar = (SeekBar) findViewById(R.id.mainscreen_volume_control);
 				AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 				if (volumeBar != null && audioManager != null ){
 					volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
 				}
 
 				final ImageView playButton = (ImageView) findViewById(R.id.mainscreen_Button_play);
 				if (AudioService.isRunning != null && AudioService.isRunning){
 					playButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
 				} else {
 					playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
 				}
 			}});
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		saveStateIndependentSetup();
 
 	}
 
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP ){ 
 			runOnUiThread(new Runnable(){
 				@Override
 				public void run() {
 					SeekBar volumeBar = (SeekBar) findViewById(R.id.mainscreen_volume_control);
 					AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 					if (volumeBar != null && audioManager != null ){
 						volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
 					}
 				}});
 		} 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void saveStateIndependentSetup(){
 
 
 		final ImageView playButton = (ImageView) findViewById(R.id.mainscreen_Button_play);
 		final ImageView phoneButton = (ImageView) findViewById(R.id.mainscreen_Button_phone);
 
 		/*
 		 * Phone in button 
 		 */
 		phoneButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_DIAL);
 				intent.setData(Uri.parse(getResources().getString(R.string.WMFO_PHONE_NUMBER)));
 				if (AudioService.isRunning != null && AudioService.isRunning){
 					stopService(new Intent(MainActivity.this, AudioService.class));
 					playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
 				}
 				MainActivity.this.startActivity(intent);
 			}
 		});
 
 		/*
 		 * Play audio button
 		 * - Show correct icon for service state
 		 * - Add click listener to start/stop service 
 		 */
 		if (AudioService.isRunning != null && AudioService.isRunning){
 			playButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
 		} else {
 			playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
 		}
 
 		playButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (AudioService.isRunning != null && AudioService.isRunning){
 					stopService(new Intent(MainActivity.this, AudioService.class));
 					playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
 				} else {
 					Intent startIntent = new Intent(MainActivity.this, AudioService.class);
 					startIntent.putExtra("source", getString(R.string.WMFO_STREAM_URL_HQ));
 					startService(startIntent);
 					playButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
 				}
 			}
 		});
 
 
 
 		/*
 		 * Volume bar setup
 		 */
 
 		final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		final ImageView volumeIcon = (ImageView) findViewById(R.id.mainscreen_volume_icon);
 		SeekBar volumeBar = (SeekBar) findViewById(R.id.mainscreen_volume_control);
 		volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
 		volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
 		volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
 
 				if (progress > 0 && progress < seekBar.getMax() / 2){
 					volumeIcon.setImageResource(R.drawable.volume_1);
 				} else if (progress == 0){
 					volumeIcon.setImageResource(R.drawable.volume_0);
 				} else {
 					volumeIcon.setImageResource(R.drawable.volume_2);
 				}
 			}
 		});
 
 
 		/*
 		 * Tab setup
 		 */
 		
 		TabHost ourTabHost = getTabHost();
 		
 		TabSpec playlistTab = ourTabHost.newTabSpec("Playlist");
 		playlistTab.setIndicator("Playlist");
 		playlistTab.setContent(new Intent().setClass(this, PlaylistActivity.class));
 		ourTabHost.addTab(playlistTab);
 
 		TabSpec tweetsTab = ourTabHost.newTabSpec("Tweets");
 		tweetsTab.setIndicator("Tweets");
 		tweetsTab.setContent(new Intent().setClass(this, TwitterActivity.class));
 		ourTabHost.addTab(tweetsTab);
 		
 		TabSpec archiveTab = ourTabHost.newTabSpec("Archives");
 		archiveTab.setIndicator("Archives");
 		archiveTab.setContent(new Intent().setClass(this, ArchiveActivity.class));
 		ourTabHost.addTab(archiveTab);
 
 		/*
 		 * Keep the volume meter and play button correct
 		 */
 		
 		checkStateTimer = new Timer();
 		checkStateTimer.schedule(new TimerTask(){
 			@Override
 			public void run() {
 				runOnUiThread(new Runnable(){
 					@Override
 					public void run() {
 						SeekBar volumeBar = (SeekBar) findViewById(R.id.mainscreen_volume_control);
 						AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 						if (volumeBar != null && audioManager != null ){
 							volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
 						}
 
 						final ImageView playButton = (ImageView) findViewById(R.id.mainscreen_Button_play);
 						if (AudioService.isRunning != null && AudioService.isRunning){
 							playButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
 						} else {
 							playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
 						}
 					}});
 			}}, 0, 1000);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			return true;
 		case R.id.menu_about:
 			startActivity(new Intent(this, AboutActivity.class));
 			return true;
 		case R.id.menu_feedback:
 			Intent emailIntent = new Intent(Intent.ACTION_SEND);
 			emailIntent.setType("plain/text");
 	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getResources().getString(R.string.feedback_email) });
 	        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_subject));
 	        startActivity(Intent.createChooser(emailIntent, "Send email using"));
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
 

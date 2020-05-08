 package org.canthack.tris.oyver;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.animation.ObjectAnimator;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class OyVerMain extends Activity implements OnSharedPreferenceChangeListener {
 	private static final String TAG = "OyVer Main";
 
 	private boolean fullscreen = false;
 	
 	private ExecutorService exe = Executors.newCachedThreadPool();
 
 	static class NonConfigurationObject{
 		TalkDownloadTask talkDLTask;
 		Voter voter;
 		Thread voterThread;
 	}
 	
 	private NonConfigurationObject nco;
 
 	private int selectedTalkId = -1;
 	private String selectedTalkTitle = "";
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_oyver_main);
 
 
 		if( (nco = (NonConfigurationObject)getLastNonConfigurationInstance()) != null) {
 			
 			if( nco.talkDLTask != null) {
 				nco.talkDLTask.setContext(this); 
 				if(nco.talkDLTask.getStatus() == AsyncTask.Status.FINISHED)
 					nco.talkDLTask.populateTalks((Spinner)this.findViewById(R.id.spinner1));
 			}
 			else{
 				downloadTalks();
 			}
 
 			if(nco.voter != null){
 				nco.voter.setContext(this);
 			}
 		}
 		else{
 			Log.v(TAG, "Making new NCO");
 			nco = new NonConfigurationObject();
 			
 			nco.voter = new Voter(this);
 			nco.voterThread = new Thread(null, nco.voter, "Voter");
 			nco.voterThread.start();
 			
 			downloadTalks();
 		}
 
 		final Spinner talkSpinner = (Spinner) this.findViewById(R.id.spinner1);
 		final Button goButton = (Button) this.findViewById(R.id.go_button);
 
 		talkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int i, long l) {
 
 				if(i > 0){ //0 is for the "select session" line. Also explains -1s below :-)
 
 					selectedTalkId = nco.talkDLTask.getTalkIds().get(i-1);
 					selectedTalkTitle = nco.talkDLTask.getTalks().talks.get(i-1).title;
 
 					goButton.setEnabled(true);
 
 					Log.v(TAG, "SELECTED " + i + "." + l + "." + selectedTalkId);
 					Log.v(TAG, "SEL: " + selectedTalkTitle);
 				}
 				else{
 					selectedTalkId = -1;
 					selectedTalkTitle = "";
 					goButton.setEnabled(false);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 
 		if (savedInstanceState != null){
 			selectedTalkId = savedInstanceState.getInt("selectedtalk");
 			talkSpinner.setSelection(savedInstanceState.getInt("spinnerSel"));
 			selectedTalkTitle = savedInstanceState.getString("selectedtalkname");
 			fullscreen = savedInstanceState.getBoolean("guimode");
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle bund) {
 		super.onSaveInstanceState(bund);
 		bund.putBoolean("guimode", fullscreen);
 		bund.putInt("selectedtalk", selectedTalkId);
 		
 		final Spinner talkSpinner = (Spinner) this.findViewById(R.id.spinner1);
 		bund.putInt("spinnerSel", talkSpinner.getSelectedItemPosition());
 		
 		bund.putString("selectedtalkname", selectedTalkTitle);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.oy_ver_main, menu);
 		return true;
 	}
 
 	//Handles menu clicks
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.action_refresh:
 			downloadTalks();
 			return true;
 		case R.id.action_settings:
 			startActivity(new Intent(this, Settings.class));
 			return true;
 		default:
 			break;
 
 		}
 		return false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setGuiMode();
 
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		preferences.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause(){
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.unregisterOnSharedPreferenceChangeListener(this);
 		
 		super.onPause();
 	}
 
 	@SuppressLint("NewApi")
 	private void setGuiMode(){
 		View main_layout = this.findViewById(android.R.id.content).getRootView();
 
 		//views that are only to be displayed in fullscreen mode
 		ArrayList<View> fsViews = new ArrayList<View>();
 
 		fsViews.add(this.findViewById(R.id.yay_button));
 		fsViews.add(this.findViewById(R.id.meh_button));
 		fsViews.add(this.findViewById(R.id.nay_button));
 
 		//views that are only to be displayed in non fullscreen mode
 		ArrayList<View> normalViews = new ArrayList<View>();
 
 		normalViews.add(this.findViewById(R.id.textView1));
 		normalViews.add(this.findViewById(R.id.spinner1));
 		normalViews.add(this.findViewById(R.id.go_button));
 
 		if(fullscreen){
 			for(View v: normalViews) v.setVisibility(View.GONE);
 
 			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 			getActionBar().hide();
 			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);	
 
 			for(View v: fsViews) v.setVisibility(View.VISIBLE);
 		}
 		else{
 			for(View v: fsViews) v.setVisibility(View.GONE);
 
 			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 			getActionBar().show();
 			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);	
 
 			for(View v: normalViews) v.setVisibility(View.VISIBLE);
 		}
 	}
 
 	public void startVoting(final View v){
 		if(selectedTalkId >= 0){
 			fullscreen = true;
 			setGuiMode();
 		}
 	}
 
 	public void voteButtonClick(final View v){
 		Log.v(TAG, "voting on " + selectedTalkId);
 		if(selectedTalkId < 0)
 			return;
 
 		ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 0.2f, 1f);
 
 		animator.setDuration(300);
 		animator.start();	
 
 		Runnable voteRunnable = new Runnable(){
 			@Override
 			public void run() {
 				Vote vote = null; 
 
 				switch(v.getId()){
 				case R.id.yay_button:
 					vote = new Vote(Settings.getVotingServerAddress(getBaseContext()), selectedTalkId, Vote.YAY);
 					break;
 
 				case R.id.meh_button:
 					vote = new Vote(Settings.getVotingServerAddress(getBaseContext()), selectedTalkId, Vote.MEH);
 					break;
 
 				case R.id.nay_button:
 					vote = new Vote(Settings.getVotingServerAddress(getBaseContext()), selectedTalkId, Vote.NAY);
 					break;
 				}
 				
 				if(vote != null){
 					nco.voter.queueVote(vote);
 				}
 			}	
 		};
 		
 		exe.execute(voteRunnable);
 	}
 
 	private void downloadTalks(){
 		if(nco.talkDLTask != null) {
 			AsyncTask.Status diStatus = nco.talkDLTask.getStatus();
 
 			if(diStatus != AsyncTask.Status.FINISHED) {
 				Log.v(TAG, "Talks already downloading.");
 				return;
 			}
 			// Since diStatus must be FINISHED, we can try again.
 		}
 
 		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 
 		boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();
 
 		if(!isConnected){
 			Toast.makeText(getApplicationContext(), this.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
 		}
 		else{
 			nco.talkDLTask = new TalkDownloadTask(this);
 
 			try{
 				nco.talkDLTask.execute(Settings.getServerAddress(this));
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// This gets called before onDestroy(). We want to pass forward a reference
 	// to our AsyncTask.
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return nco;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		if (key.equals(Settings.OYVER_SETTING_SERVER)) {
 			downloadTalks();
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if(fullscreen){
 			fullscreen=false;
 			setGuiMode();
 		}
 		else{
 			Log.v(TAG, "Back pressed");
 			if(nco != null && nco.voter != null && nco.voterThread != null){
 				Log.v(TAG, "Stopping threads");
 				nco.voter.stop();
 				try {
 					nco.voterThread.join();
 				} catch (InterruptedException e) {}
 			}
 			
 			super.onBackPressed();
 		}
 	}
 }

 package org.marietjedroid.droidmarietje;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.marietjedroid.connect.MarietjeClient;
 import org.marietjedroid.connect.MarietjeException;
 import org.marietjedroid.connect.MarietjePlaying;
 import org.marietjedroid.connect.MarietjeTrack;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MarietjeDroidActivity extends Activity implements OnClickListener {
 
 	private static MarietjeClient mc = null;
 	private static MarietjeTrack[] playlist = null;
 	MarietjePlaying currentlyPlaying;
 
 	private AutoCompleteTextView requesttxt = null;
 	private TextView txtCurrentlyPlaying = null;
 
 	private Handler mHandler = new Handler();
 	private ArrayList<TextView> durationlist;
 	private RequestListener tc;
 
 	public static MarietjeClient getConnection() {
 		return mc;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		
 		txtCurrentlyPlaying = (TextView) findViewById(R.id.currentlyplaying);
 
 		durationlist = new ArrayList<TextView>();
 
 		try {
 			mc = new MarietjeClient("192.168.56.101", 8080, "");
 		} catch (MarietjeException e) {
			Log.e("Err", e.getMessage());
 			Toast t = Toast.makeText(this.getApplicationContext(),
 					"Kon niet verbinden met marietje", Toast.LENGTH_LONG);
 			t.show();
 		}
 
 		updateCurrentlyPlaying();
 
 		updateQueue();
 
 		requesttxt = (AutoCompleteTextView) findViewById(R.id.requesttext);
 		RequestListener tc = new RequestListener(mc, requesttxt, this.getApplicationContext());
 		requesttxt.addTextChangedListener(tc);
 		this.tc = tc;
 
 		((Button) findViewById(R.id.requestbutton)).setOnClickListener(this);
 		findViewById(R.id.currentlyplayingwrap).requestFocus();
 
 		mHandler.removeCallbacks(mUpdateCurrentlyPlaying);
 		mHandler.postDelayed(mUpdateCurrentlyPlaying, 1000);
 		mHandler.removeCallbacks(mUpdateQueue);
 		mHandler.postDelayed(mUpdateQueue, 800);
 
 	}
 
 	private void updateQueue() {
		if (mc == null){
			return;
		}
 		try {
 			playlist = mc.getQueue();
 
 		} catch (MarietjeException e) {
 			Toast t = Toast.makeText(this.getApplicationContext(),
 					"Kon de afspeellijst niet ophalen", Toast.LENGTH_LONG);
 			t.show();
 		}
 		synchronized (durationlist) {
 			ViewGroup muzieklijst = (ViewGroup) findViewById(R.id.muzieklijstview);
 			SongListener sl = new SongListener();
 			durationlist.clear();
 			muzieklijst.removeAllViews();
 			muzieklijst.refreshDrawableState();
 			double totalLength = (currentlyPlaying.getEndTime() - currentlyPlaying.getServerTime() + 
 					(currentlyPlaying.getServerTime()-System.currentTimeMillis()/1000));
 
 			for (MarietjeTrack mt : playlist) {
 				View v = LayoutInflater.from(this).inflate(R.layout.muziekitem,
 						muzieklijst, false);
 
 				RelativeLayout muzieklistrow = (RelativeLayout) v
 						.findViewById(R.id.muziekitem);
 
 				muzieklistrow.setTag(mt.getTrackKey());
 
 				muzieklistrow.setOnClickListener(sl);
 
 				((TextView) v.findViewById(R.id.title)).setText(mt.getTitle());
 
 				((TextView) v.findViewById(R.id.artist))
 						.setText(mt.getArtist());
 
 				TextView duration = (TextView) v.findViewById(R.id.tracklength);
 
 				duration.setText(militimeToString(totalLength * 1000));
 				totalLength += mt.getLength();
 
 				durationlist.add(duration);
 
 				muzieklijst.addView(v);
 			}
 		}
 
 	}
 
 	private String militimeToString(double time) {
 		double seconds = time / 1000;
 		int minutes = (int) (seconds / 60);
 		int seconds_i = (int) (seconds % 60);
 
 		String sec_s;
 		if (seconds_i < 10) {
 			sec_s = "0" + seconds_i;
 		} else {
 			sec_s = "" + seconds_i;
 		}
 
 		return minutes + ":" + sec_s;
 
 	}
 
 	private Runnable mUpdateCurrentlyPlaying = new Runnable() {
 		public void run() {
 			updateCurrentlyPlaying();
 
 			mHandler.postDelayed(this, 1000);
 		}
 	};
 
 	private Runnable mUpdateQueue = new Runnable() {
 		public void run() {
 			updateQueue();
 
 			mHandler.postDelayed(this, 1000);
 		}
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.mnurequest:
 			startActivity(new Intent(getApplicationContext(),
 					RequestActivity.class));
 
 			return true;
 		case R.id.mnulogin:
 			startActivity(new Intent(getApplicationContext(),
 					LoginActivity.class));
 
 			return true;
 
 		case R.id.mnuupload:
 			startActivity(new Intent(getApplicationContext(),
 					UploadActivity.class));
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void updateCurrentlyPlaying() {
 		String artist = "";
 		String title = "";
 
		if (mc == null) {
			return;
		}
		
 		try {
 			MarietjePlaying mp = mc.getPlaying();
 			if (mp == currentlyPlaying)
 				return;
 			currentlyPlaying = mp;
 			artist = currentlyPlaying.getArtist();
 			title = currentlyPlaying.getTitle();
 		} catch (MarietjeException e) {
 			Toast t = Toast.makeText(this.getApplicationContext(),
 					"Kon niet ophalen", Toast.LENGTH_LONG);
 			t.show();
 		} finally {
 			synchronized (txtCurrentlyPlaying) {
 				txtCurrentlyPlaying.setText(artist + " - " + title);
 			}
 		}
 	}
 
 	public void onClick(View v) {
 		Log.d("Request", requesttxt.getText().toString());
 		try {
 			mc.requestTrack(tc.getReqId());
 		} catch (MarietjeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 	}
 
 }

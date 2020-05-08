 package org.routy;
 
 import org.routy.fragment.OneButtonDialog;
import org.routy.fragment.TwoButtonDialog;
 import org.routy.model.AppProperties;
 import org.routy.service.InternetService;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity {
 
 	private final String TAG = "MainActivity";
 
 	private Context mContext;
	private TwoButtonDialog noInternetErrorDialog;
 	
 	private SoundPool sounds;
 	private int bad;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		mContext = this;
 		
 		sounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
     bad = sounds.load(this, R.raw.routybad, 1);
     
 		initErrorDialog();
 
 		new Handler().postDelayed(new Runnable() {
 
 			@Override
 			public void run() {
 				checkForInternetAndContinue();
 			}
 			
 		}, AppProperties.SPLASH_SCREEN_DELAY_MS);
 	}
 	
 	
 	private void checkForInternetAndContinue() {
 		Log.v(TAG, "Checking for internet connection...");
 		
 		if (!InternetService.deviceHasInternetConnection(mContext)) {
 			Log.v(TAG, "No internet connection.");
 			sounds.play(bad, 1, 1, 1, 0, 1);
 			initErrorDialog();
 			noInternetErrorDialog.show(MainActivity.this.getSupportFragmentManager(), TAG);
 		} else {
 			Log.v(TAG, "Found an internet connection.");
 
 			gotoOriginScreen();
 		}
 	}
 
 
 	private void gotoOriginScreen() {
 	   if(sounds != null) { 
 	      sounds.release(); 
 	      sounds = null; 
 	    }
 		// Start an intent to bring up the origin screen
 		Intent originIntent = new Intent(MainActivity.this, OriginActivity.class);
 		startActivity(originIntent);
 	}
 	
 	
 	private void initErrorDialog() {
		/*noInternetErrorDialog = new OneButtonDialog(getResources().getString(R.string.error_message_title), getResources().getString(R.string.no_internet_error), "Try Again") {
 			@Override
 			public void onButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				checkForInternetAndContinue();
 			}
		};*/
		
		noInternetErrorDialog = new TwoButtonDialog(getResources().getString(R.string.error_message_title), getResources().getString(R.string.no_internet_error), new String[] {"Try Again", "", "Quit"}) {
			
			@Override
			public void onRightButtonClicked(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				checkForInternetAndContinue();
			}
			
			@Override
			public void onLeftButtonClicked(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				MainActivity.this.finish();
			}
 		};
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 }

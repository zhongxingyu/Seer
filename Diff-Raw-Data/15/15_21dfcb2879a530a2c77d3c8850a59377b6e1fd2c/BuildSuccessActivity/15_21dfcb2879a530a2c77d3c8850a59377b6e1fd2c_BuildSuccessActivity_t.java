 package jenkinscalling.client;
 
 import android.app.Activity;
 import android.app.KeyguardManager;
 import android.app.KeyguardManager.KeyguardLock;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class BuildSuccessActivity extends Activity{
 	private Button closeButton;
 	private MediaPlayer mediaPlayer;
 
 	PowerManager pm;
 	WakeLock wl;
 	KeyguardManager km;
 	KeyguardLock kl;
 	
     @Override
 	public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);        
 
         pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
         kl = km.newKeyguardLock("INFO");
         wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "INFO");
         wl.acquire(); //wake up the screen
         kl.disableKeyguard();// dismiss the keyguard
         
         setContentView(R.layout.green_calling);
         
         stopService(new Intent(getApplicationContext(), JenkinsService.class));
         
         closeButton = (Button) findViewById(R.id.gotohell);
         closeButton.setOnClickListener(closeListener);
         
         mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.keyboardcat);
         mediaPlayer.start(); // no need to call prepare(); create() does that for you
     }
     
 	private OnClickListener closeListener = new OnClickListener(){
 		public void onClick(View v) {
 			mediaPlayer.stop();
 			startActivity(new Intent(getApplicationContext(), MainActivity.class));			
 		}
 	};
 	
 	public void onStop(){
 		super.onStop();
		mediaPlayer.stop();
 	}
 	
 	public void onPause(){
 		super.onPause();
		wl.release();		
 		startService(new Intent(getApplicationContext(), JenkinsService.class));
 	}
 	
 	public void onResume(){
 		super.onResume();
 		wl.acquire();
 	}
 }

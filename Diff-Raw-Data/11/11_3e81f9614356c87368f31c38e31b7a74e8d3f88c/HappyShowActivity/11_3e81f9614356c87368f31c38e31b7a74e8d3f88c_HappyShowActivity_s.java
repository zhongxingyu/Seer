 package com.sunlitjiang.happyShow;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 public class HappyShowActivity extends Activity {
     /** Called when the activity is first created. */
 	private static final int DURATIONTIME = 3500;  //millisecond
 	
 	private ImageView showedImage;
 	private AnimationDrawable frameAnimation;
 	private PowerManager powerManager;
 	private WakeLock wakeLock;
 	private BroadcastReceiver phoneStateReciever;
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
         wakeLock.acquire();
 
         // Load the ImageView that will host the animation and
         // set its background to our AnimationDrawable XML resource.
         showedImage = (ImageView) findViewById(R.id.imageView_showedPic);
         showedImage.setBackgroundResource(R.drawable.slides);
         // Get the background, which has been compiled to an AnimationDrawable object.
         frameAnimation = (AnimationDrawable) showedImage.getBackground();
         addPicturesOnExternalStorageIfExist();
         
 		Intent intentForStartService = new Intent(this, HappyShowService.class);
 		startService(intentForStartService);
 		
 		phoneStateReciever = new BroadcastReceiver(){  
 			public void onReceive(Context context, Intent intent) {
 				HappyShowActivity.this.finish();
 			} //onReceive
 		};
 		IntentFilter phoneStateIntentFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
 		phoneStateIntentFilter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
 		registerReceiver(phoneStateReciever, phoneStateIntentFilter);
 		
         Log.d("Activity", "onCreate");
     }
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.d("Activity", "onStart");
 	}
 	
 	@Override
 	public void onWindowFocusChanged (boolean hasFocus){
 		super.onWindowFocusChanged (hasFocus);
		frameAnimation.start();
        Log.d("Activity", "onFocusChanged");
 	}
 	
 	@Override
 	public void onConfigurationChanged (Configuration newConfig){
 		Log.d("Activity", "onConfigurationChanged");
 	}
 	
 	@Override
 	protected void onNewIntent (Intent intent) {
 		Log.d("Activity", "onNewIntent");
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();  // Always call the superclass method first
         Log.d("Activity", "onPause");
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d("Activity", "onResume");
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	        Log.d("Activity", "onTouchDown");
 			this.finish();
 			return true;
 		}
 		return super.onTouchEvent(event);
 	}
 	
 	@Override
 	protected void onStop() {
 	    wakeLock.release();
 	    unregisterReceiver(phoneStateReciever);
 	    super.onStop();  // Always call the superclass method first
 	    Log.d("Activity", "onStop");
 	}
 	
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		Log.d("Activity", "onRestart");
 	}
 	
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		Log.d("Activity", "onDestroy");
 	}
 	
 	private void addPicturesOnExternalStorageIfExist() {
 	    // check if external storage 
 	    String state = Environment.getExternalStorageState();
 	    if ( !(Environment.MEDIA_MOUNTED.equals(state) || 
 	          Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) ) {
 	        return;
 	    } 
 
 	    // check if a directory named as this application
 	    File rootPath = Environment.getExternalStorageDirectory();
 	    // 'happyShow' is the name of directory
 	    File pictureDirectory = new File(rootPath, getString(R.string.app_name)); 
 	    if ( !pictureDirectory.exists() ) {
 	        Log.d("Activity", "NoFoundExternalDirectory");
 	        return;
 	    }
 
 	    // check if there is any picture
 	    //create a FilenameFilter and override its accept-method
 	    FilenameFilter filefilter = new FilenameFilter() {
 	        public boolean accept(File dir, String name) {
 	        return (name.endsWith(".jpeg") || 
 	                name.endsWith(".jpg") || 
 	                name.endsWith(".JPG") || 
 	                name.endsWith(".png") );
 	        }
 	    };
 
 	    String[] sNamelist = pictureDirectory.list(filefilter);
 	    if (sNamelist.length == 0) {
 	        Log.d("Activity", "No pictures in directory.");
 	        return;
 	    }
 
 	    for (String filename : sNamelist) {
 	        Log.d("Activity", pictureDirectory.getPath() + '/' + filename);
 	        frameAnimation.addFrame(
 	                Drawable.createFromPath(pictureDirectory.getPath() + '/' + filename),
 	                DURATIONTIME);
 	    }
 	    return;
 	}
 
 }
 

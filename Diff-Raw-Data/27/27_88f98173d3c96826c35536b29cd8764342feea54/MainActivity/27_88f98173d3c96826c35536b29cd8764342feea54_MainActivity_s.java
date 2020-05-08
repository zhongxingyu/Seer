 package cm.aptoide.pt;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import cm.aptoide.com.nostra13.universalimageloader.core.DisplayImageOptions;
 import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader;
 import cm.aptoide.com.nostra13.universalimageloader.core.assist.FailReason;
 import cm.aptoide.com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
 import cm.aptoide.com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity {
 	
 	private final String SDCARD = Environment.getExternalStorageDirectory()
 			.getPath();
 	private String LOCAL_PATH = SDCARD + "/.aptoide";
 
 	/**
 	 * The thread to process splash screen events
 	 */
 	private Thread mSplashThread;
 	ImageView imageSplash;
 	boolean retry = false;
 	private ImageLoadingListener listener = new ImageLoadingListener() {
 		
 		@Override
 		public void onLoadingStarted() { }
 		
 		@Override
 		public void onLoadingFailed(FailReason failReason) {
 			Log.e("Start-onLoadingFailed","Failed to load splashscreen");
 			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 				saveSplashscreenImageToSDCard("splashscreen_land.png");
 			}else{
 				saveSplashscreenImageToSDCard("splashscreen.png");
 			}
 			showSplash();
 		}
 		
 		@Override
 		public void onLoadingComplete(Bitmap loadedImage) {
 			showSplash();
 		}
 		
 		@Override
 		public void onLoadingCancelled() {	}
 	};
 	
 	
 	private DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(300)).build();
 	
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		SetAptoideTheme.setAptoideTheme(this);
 		super.onCreate(savedInstanceState);
 		File file = new File(LOCAL_PATH + "/icons");
 		if (!file.exists()) {
 			file.mkdirs();
 		}
 		if(ApplicationAptoide.SPLASHSCREEN != null){
 
 			// Splash screen view
 			requestWindowFeature(Window.FEATURE_NO_TITLE);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 			setContentView(R.layout.splash);
 			imageSplash = (ImageView) findViewById(R.id.splashscreen);
 			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
 				ImageLoader.getInstance().displayImage(ApplicationAptoide.SPLASHSCREENLAND, imageSplash, options, listener, null);
 			}else{
 				ImageLoader.getInstance().displayImage(ApplicationAptoide.SPLASHSCREEN, imageSplash, options, listener, null);
 			}
 			
 		}else{
             finish();
 			Intent intent = new Intent();
 			intent.setClass(MainActivity.this, Start.class);
 			startActivity(intent);
 
 		}
 	}
 
 	private void showSplash() {
 		final MainActivity sPlashScreen = this;
 
 		// The thread to wait for splash screen events
 		mSplashThread =  new Thread(){
 			@Override
 			public void run(){
 				try {
 					synchronized(this){
 						// Wait given period of time or exit on touch
 						wait(3000);
 					}
 				}
 				catch(InterruptedException ex){
 				}
 
 				finish();
 
 				// Run next activity
 				Intent intent = new Intent();
 				intent.setClass(sPlashScreen, Start.class);
 				startActivity(intent);
 				//                stop();
 			}
 		};
 
 		mSplashThread.start();
 	}
 
 	/**
 	 * Processes splash screen touch events
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent evt)
 	{
 		if(ApplicationAptoide.PARTNERID != null){
 			if(evt.getAction() == MotionEvent.ACTION_DOWN)
 			{
				synchronized(mSplashThread){
					mSplashThread.notifyAll();
 				}
 			}
 		}
 		return true;
 	}
 
 	private void saveSplashscreenImageToSDCard(String fileName) {
 		// imageSplash.setImageResource(R.drawable.splashscreen_land);
 		BitmapFactory.Options bmOptions;
 		bmOptions = new BitmapFactory.Options();
 		bmOptions.inSampleSize = 1;
 		Bitmap bbicon = null;
 		try {
 				bbicon = BitmapFactory.decodeStream(getAssets().open(fileName));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		String extStorageDirectory = Environment
 				.getExternalStorageDirectory().getAbsolutePath()
 				+ "/.aptoide";
 		File wallpaperDirectory = new File(extStorageDirectory);
 		
 		OutputStream outStream = null;
 		File file = new File(wallpaperDirectory, fileName);
 		// to get resource name
 		// getResources().getResourceEntryName(R.drawable.icon);
 
 		if (file.exists()) {
 			try {
 				imageSplash.setImageBitmap(BitmapFactory
 						.decodeStream(new FileInputStream(file)));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		} else {
 
 			try {
 				outStream = new FileOutputStream(file);
 				bbicon.compress(Bitmap.CompressFormat.PNG, 100,
 						outStream);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					outStream.flush();
 					outStream.close();
 					imageSplash.setImageBitmap(BitmapFactory
 							.decodeStream(new FileInputStream(file)));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }

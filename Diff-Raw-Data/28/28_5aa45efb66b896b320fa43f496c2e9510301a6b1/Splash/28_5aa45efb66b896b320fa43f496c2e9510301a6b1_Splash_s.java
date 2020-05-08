 package net.mgpyone.digitdiction;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
 import android.widget.ImageView;
 
 @SuppressLint("HandlerLeak")
 public class Splash extends Activity {
 
 	private static final int STOPSPLASH = 0;
 	//time in milliseconds
 	private static final long SPLASHTIME = 3000;
 
 	private ImageView splash;
 
	//handler for splash screen
	private Handler splashHandler = new Handler() {
		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				//remove SplashScreen from view
				splash.setVisibility(View.GONE);
				break;
			}
			super.handleMessage(msg);
		}
	};
	
 	//Thread for splash screen
 	Thread splashTread = new Thread() {
 		@Override
 		public void run() {
 			try {
 				Thread.sleep(SPLASHTIME);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				Intent main = new Intent(getApplicationContext(), digitdiction.class);
 				startActivity(main);
 				finish();
 			}
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.splash);
 		splash = (ImageView) findViewById(R.id.splashimg);
 		/*
 		Message msg = new Message();
 		msg.what = STOPSPLASH;
 		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
 		*/
 		splashTread.start();
		
 	}
 }

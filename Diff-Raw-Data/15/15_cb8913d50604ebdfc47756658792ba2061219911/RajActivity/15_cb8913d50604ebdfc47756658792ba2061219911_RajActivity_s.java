 package com.vitaltech.bioink;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import rajawali.RajawaliActivity;
 
 @SuppressWarnings("deprecation")
 public class RajActivity extends RajawaliActivity {
 	private static final String TAG = RajActivity.class.getSimpleName();
 	public static final Boolean DEBUG = MainActivity.DEBUG;
 
 	private DataProcess dp;
 	private Scene scene;
 	private BluetoothManager BTMan;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 //		super.onCreate(savedInstanceState);
 		if(DEBUG) Log.d(TAG, "__onCreate()__");
 		
 		// VIZ SCENE
 		if(scene != null){
 			// scene.close()
 			scene = null; // should not be necessary
 		}
 		scene = new Scene(this);
 		scene.initScene();
 		scene.setSurfaceView(mSurfaceView);
		super.setRenderer(scene);
 		// END VIZ SCENE
 
 		// DATA PROCESSING 
 		if(dp != null){
 			// dp.close()
 			dp = null; // should not be necessary
 		}
 		dp = new DataProcess(1000);
 		dp.addScene(scene);
 		// END DATA PROCESSING
 
 		// BLUETOOTH
 		if(BTMan != null){
 			// BTMan.close()
 			BTMan = null; // should not be necessary
 		}
 //		BTMan = new BluetoothManager(btAdapter, dp); // FIXME
 		// END BLUETOOTH
 
 		// DISPLAY FPS
 		if(DEBUG){
 			LinearLayout ll = new LinearLayout(this);
 			ll.setOrientation(LinearLayout.HORIZONTAL);
 			TextView label = new TextView(this);
 			label.setTextSize(20);
 			ll.addView(label);
 			mLayout.addView(ll);
 			
 			FPSDisplay fps = new FPSDisplay(this,label);
 			scene.setFPSUpdateListener(fps);
 		}
 		// END FPS DISPLAY
 
 		// start data feeding thread for testing
 		new Thread(new Runnable() {
 			public void run() {
 				new DataSimulator(dp).run();
 			}
 		}).start();// debug data
 
 		setContentView(mLayout);
     }
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(DEBUG) Log.d(TAG, "keycode received " + keyCode);
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if(DEBUG) Log.d(TAG, "back keycode received, ending raj viz activity");
 			BTMan = null;
 			dp = null;
 			scene = null;
 			finish();
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void onPause(){
 		if(DEBUG) Log.d(TAG, "__onPause()__");
 	}
 }
 
 
 

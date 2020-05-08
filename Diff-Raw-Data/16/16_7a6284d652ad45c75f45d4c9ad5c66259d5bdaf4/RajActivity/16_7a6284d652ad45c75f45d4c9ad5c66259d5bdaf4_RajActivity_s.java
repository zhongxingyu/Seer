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
 	private static final Boolean DEBUG = MainActivity.DEBUG;
 
 	private DataProcess dp;
 	private Scene scene;
 	private BluetoothManager BTMan;
 	private Thread dataSim;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(DEBUG) Log.d(TAG, "__onCreate()__");
 		
 		// VIZ SCENE
 		if(scene != null){
 			if(DEBUG) Log.d(TAG, "scene not null");
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
 			dp.quitDP();
 			dp = null; // should not be necessary
 		}
 		dp = new DataProcess(100);
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
 		if(dataSim != null){
 			dataSim.interrupt();
 			dataSim = null;
 		}
 		dataSim = new Thread(new Runnable() {
 			public void run() {
 				new DataSimulatorPlus(dp).run();
 			}
 		});// debug data
 		dataSim.start();
 
 		setContentView(mLayout);
     }
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(DEBUG) Log.d(TAG, "keycode received " + keyCode);
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if(DEBUG) Log.d(TAG, "back keycode received, ending raj viz activity");
 			cleanExit();
 			return true;
 		}
 		return false;
 	}
 	
 	private void cleanExit(){
		dataSim.interrupt();
 		dataSim = null;
 //		BTMan = null;
		dp.quitDP();
 		dp = null;
 		scene = null;
 		System.gc();
 		finish();
 	}
 
 	@Override
 	public void onPause(){
 		if(DEBUG) Log.d(TAG, "__onPause()__");
 		super.onPause();
 		cleanExit();
 	}
 
 	@Override
 	public void onResume(){
 		if(DEBUG) Log.d(TAG, "__onResume()__");
 		super.onResume();
 	}
 }
 
 
 

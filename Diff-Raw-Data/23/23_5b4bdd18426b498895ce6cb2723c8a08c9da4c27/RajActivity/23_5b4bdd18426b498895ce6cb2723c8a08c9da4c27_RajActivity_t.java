 package com.vitaltech.bioink;
 
 import android.bluetooth.BluetoothAdapter;
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
 		
 		Bundle settings = this.getIntent().getExtras();
 
 		// DATA PROCESSING 
 		if(dp != null){
 			dp.quitDP();
 			dp = null; // should not be necessary
 		}
 		dp = new DataProcess(100);
 		dp.addScene(scene);
 		dp.setMinHR(settings.getFloat("minHR"));
 		dp.setMaxHR(settings.getFloat("maxHR"));
 		dp.setMinResp(settings.getFloat("minResp"));
 		dp.setMaxResp(settings.getFloat("maxResp"));
 		dp.setColor((BiometricType)settings.getSerializable("colorType"));
 		dp.setEnergy((BiometricType)settings.getSerializable("energyType"));
 		// END DATA PROCESSING
 
 		if(DEBUG){
 			Log.d(TAG, "minHR = " + settings.getFloat("minHR") + ", maxHR = " + settings.getFloat("maxHR"));
 			Log.d(TAG, "minResp = " + settings.getFloat("minResp") + ", maxResp = " + settings.getFloat("maxResp"));
 			Log.d(TAG, "color = " + settings.getSerializable("colorType").toString());
 			Log.d(TAG, "energy = " + settings.getSerializable("energyType").toString());
 		}
 		// BLUETOOTH
 		if(BTMan != null){
 			// BTMan.close()
 			BTMan = null; // should not be necessary
 		}
 		BTMan = new BluetoothManager(BluetoothAdapter.getDefaultAdapter(), dp); // FIXME
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
 				//new DataSimulatorPlusPlus(dp, 5).run();
 				//new DataSimulatorDP(dp).run();
//				new DataSimulatorPlus(dp).run(); // show 2D four corners
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
 		if(dataSim != null){
 			dataSim.interrupt();
 		}
 		dataSim = null;
 
 		if(dp != null){
 			dp.quitDP();
 		}
 		dp = null;
 
 		if(BTMan != null){
 			BTMan.bt_disabled();
 		}
 //		BTMan = null;
 
 		scene = null;
 
 		/** Hack needed to exit RajActivity without crash */
 		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
 		if(bt.isEnabled()){
 			if(DEBUG) Log.w(TAG, "turning off bluetooth before exiting");
 			bt.disable();
 		}
 
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
 		new Thread(
 			new Runnable() {
 				public void run() {
 					BTMan.start();
 				}
 			}
 		).start();
 	}
 }
 
 
 

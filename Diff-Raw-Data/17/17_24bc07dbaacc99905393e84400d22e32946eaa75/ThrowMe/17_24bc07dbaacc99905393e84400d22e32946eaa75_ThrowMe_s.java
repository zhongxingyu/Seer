 package com.thorgaming.throwme;
 
 import android.app.Activity;
import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.thorgaming.throwme.billing.BillingService;
 import com.thorgaming.throwme.drawing.Stage;
 import com.thorgaming.throwme.screens.Main;
 import com.thorgaming.throwme.screens.Screen;
 
 /**
  * The main activity of the application
  * 
  * @author Thomas Cheyney
  * @version 1.0
  */
 public class ThrowMe extends Activity {
 
 	public Stage stage = null;
 	public BillingService billingService = null;
 	private static ThrowMe activity = null;
 	public SharedPreferences customisationSettings = null;
 	public Screen screen;
 	public boolean billingDirty = false;
 
 	/**
 	 * Gets an instance of the activity. This doesn't make one if there isn't one as android has to.
 	 * @return An instance of this activity
 	 */
 	public static ThrowMe getInstance() {
 		return activity;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		activity = this;
 		customisationSettings = getSharedPreferences("customisation", 0);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		stage = (Stage) findViewById(R.id.menu);
 		billingService = new BillingService(this);
 
 		if (savedInstanceState == null || !savedInstanceState.getBoolean("alreadyRunning", false)) {
 			new Main(null);
 		} else {
 			new Main(new Object[] {true});
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putBoolean("alreadyRunning", true);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (!isFinishing()) {
 			stage.sendtouch(event);
 		}
 		if (screen != null) {
 			return screen.onTouch(event);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (screen != null) {
 			return screen.onKeyDown(keyCode, event);
 		}
 		return false;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		BillingService.purchases.close();
		stopService(new Intent(this, BillingService.class));
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		stage.createThread();
 	}
 
 }

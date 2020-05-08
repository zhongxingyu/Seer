 package com.abstracttech.ichiban.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 import com.abstracttech.ichiban.R;
 import com.abstracttech.ichiban.data.BluetoothEx;
 import com.abstracttech.ichiban.data.Data;
 import com.abstracttech.ichiban.data.MainPagerAdapter;
 import com.abstracttech.ichiban.data.Vibrate;
 import com.abstracttech.ichiban.views.graphs.GraphType;
 
 /**
  * main activity
  * manages layout and events
  */
 public class IchibanActivity extends Activity {
 	private BluetoothEx bt=new BluetoothEx();
 	private static List<View> clients = new ArrayList<View>();
 	
 	private PowerManager.WakeLock wl;
 	
 	public static final int _UPDATE_INTERVAL = 100;
 	private static boolean running=false;
 
 	public static boolean isRunning(){
 		return running;
 	}
 
 	/**
 	 * subscribe to running change
 	 * @param v view to notify
 	 */
 	public static void subscribe(View v){
 		clients.add(v);
 	}
 
 	private void notifyClients()
 	{
 		for(View v : clients)
 			v.postInvalidate();
 	}
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pager);
 		
 		Data.vibrator = new Vibrate((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
 		
 		//make sure screen stays on
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 	    wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
 
 		
 		//load pages
 		MainPagerAdapter adapter = new MainPagerAdapter();
 		ViewPager myPager = (ViewPager) findViewById(R.id.mypager);
 		myPager.setAdapter(adapter);
 		myPager.setCurrentItem(1);
 		
 		//Initialize Preference -> get preference data class
 		com.abstracttech.ichiban.data.Preferences.Initalize(PreferenceManager.getDefaultSharedPreferences(this));
 
 		bt.onCreate(this);
 
 		running=false;
 	}
 
 	/**
 	 * toglle running
 	 * @param v sender
 	 */
 	public void powerButtonClick(View v){
 		try
 		{
 			if(running==false)
 			{
 				startCar(v);
 				running=true;
 			}
 			else
 			{
 				stopCar(v);
 				running=false;
 			}
 			notifyClients();
 			Data.vibrator.vibrate(60);
 		}
 		catch(Exception e)
 		{
 			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG).show();
 			Log.e(getPackageName(), "power button clicked and something gone wrong", e);
 		}		
 	}
 
 	/**
 	 * listener for testing button
 	 * @param v sender
 	 */
 	public void test(View v) {
 		Toast.makeText(this, "use IchibanActivity's test() to quickly test something ;)", Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * starts autoupdating
 	 * wheather is it over BT or local, depending on loaded data
 	 * in real application it would also send start command to the car
 	 * @param v view that called it; for use with buttons, not used
 	 */
 	public void startCar(View v) {
 		if(Data.hasLocalData())
 			Data.startAutoupdate(_UPDATE_INTERVAL);
 		else
 			bt.start(v);
 	}
 
 	/**
 	 * stops autoupdating
 	 * wheather is it over BT or local
 	 * in real application it would also send stop command to the car
 	 * @param v view that called it; for use with buttons, not used
 	 */
 	public void stopCar(View v) {
 		bt.stop(v);
 		Data.stopAutoupdate();
 	}
 
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		//wake lock
 		wl.acquire();
 		//start bluetooth activity
 		bt.onStart();
 	}
 
 	@Override
 	public synchronized void onResume() {
 		super.onResume();
 		bt.onResume();
 		
 		//load graph type preferences
 		Data.graphs[1]=com.abstracttech.ichiban.data.Preferences.getGraph1Type();
 		Data.graphs[2]=com.abstracttech.ichiban.data.Preferences.getGraph2Type();
 		Data.graphs[3]=com.abstracttech.ichiban.data.Preferences.getGraph3Type();
 		}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		stopCar(null);
 		wl.release();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		bt.onDestroy();
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		bt.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.option_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		bt.handleBTmenu(item);
 		return false;
 	}
 	/**
 	 * handles morphing buttons on central screen
 	 * @param sender sender button
 	 */
 	public void morphingClick(View sender)
 	{
 		switch(sender.getId())
 		{
 		case R.id.morphToAcc:
 			Data.graphs[0]=GraphType.ACCELERATION;
 			break;
 		case R.id.morphToSpeed:
 			Data.graphs[0]=GraphType.SPEED;
 			break;
 		case R.id.morphToG:
 			Data.graphs[0]=GraphType.TOTAL_ACC;
 			break;
 		}
 	}
 }

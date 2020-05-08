 /**
  * Copyright 2010 Kurtis Nusbaum
  *
  * This file is part of LinkSchedule.  
  *
  * LinkSchedule is free software: you can 
  * redistribute it and/or modify it under the terms of the GNU General Public 
  * License as published by the Free Software Foundation, either version 2 of the 
  * License, or (at your option) any later version.  
  *
  * LinkSchedule is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
  * for more details.  You should have received a copy of the GNU  General 
  * Public License along with LinkSchedule. If not, see 
  * http://www.gnu.org/licenses/.
  */
 
 package org.klnusbaum.linkschedule;
 
 import com.admob.android.ads.AdManager;
 import com.admob.android.ads.AdView;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.content.Intent;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ContextMenu;
 import android.view.View;
 import android.net.Uri;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import org.klnusbaum.aboutlib.AboutActivity;
 
 import java.util.GregorianCalendar;
 
 /**
  * Activity for displaying multiple bus stop times.
  *
  * @author Kurtis Nusbaum
  * @version 1.0
  */
 public class OmniScheduleActivity extends BusStopActivity 
 	implements Refreshable, SharedPreferences.OnSharedPreferenceChangeListener
 {
 	/**
 	 * Reference to the LinkSchedule object the Activity will use.
  	 */
 	private LinkSchedule linkSchedule;
 	
 	/**
 	 * BroadcastReceiver to receive broadcasts when the time changes.
 	 */
 	private TimeChangeReceiver timeChangeReceiver;
 
 	/**
  	 * The 4 views that comprise this activity.
 	 */
 	private ClockView sextonClock, flynntownClock, hccClock, goreckiClock;
 
 	/**
  	 * The bus stop the user currently has selected.
  	 */
 	private String currentSelectedStop;
 
 	/**
 	 * Click listener for each of the ClockViews.
 	 */
 	private View.OnClickListener clockClickListener = new View.OnClickListener(){
 		public void onClick(View v){
 			Intent busStopIntent = 
 				new Intent(OmniScheduleActivity.this, SingleStopActivity.class);
 			busStopIntent.putExtra(BusStopActivity.EXTRA_STOPNAME,
 				((ClockView)v).getStopName());
 			startActivity(busStopIntent);
 		}
 	};
 
   @Override
   protected void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
 		AdManager.setTestDevices( new String[] {
 			AdManager.TEST_EMULATOR});
 
 		AdView adView = (AdView)findViewById(R.id.ad1);
 		adView.requestFreshAd();
 		linkSchedule = LinkSchedule.getLinkSchedule(getResources());
 		timeChangeReceiver = new TimeChangeReceiver(this);
 		timeChangeReceiver.registerIntents(this);
 	
 		flynntownClock = (ClockView)findViewById(R.id.flynntown_clock);
 		hccClock = (ClockView)findViewById(R.id.hcc_clock);
 		goreckiClock = (ClockView)findViewById(R.id.gorecki_clock);
 		sextonClock = (ClockView)findViewById(R.id.sexton_clock);
 
 		initClockView(flynntownClock);
 		initClockView(hccClock);
 		initClockView(goreckiClock);
 		initClockView(sextonClock);
 
 		SharedPreferences settings = 
 			PreferenceManager.getDefaultSharedPreferences(this);
 		settings.registerOnSharedPreferenceChangeListener(this);
 
 		refreshSchedule();
   }
 
 	/**
 	 * Initializes a ClockView.
    *
 	 * @param view ClockView to be initialized.
  	 */
 	private void initClockView(ClockView view){
 		view.setOnClickListener(clockClickListener);
		//view.setTransformationMethod(TimeTransform.getInstance());
 		registerForContextMenu(view);
 	}
 
 	@Override
 	protected void onDestroy(){
 		super.onDestroy();
 		unregisterReceiver(timeChangeReceiver);
 	}
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu){
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.sched_menu, menu);
     return true;
   }
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, 
 		ContextMenu.ContextMenuInfo menuInfo)
 	{
 		currentSelectedStop = ((ClockView)v).getStopName().toString();
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
 		case R.id.menuAbout:
 			Intent showAboutIntent =
 				new Intent(OmniScheduleActivity.this, AboutActivity.class);
 				startActivity(showAboutIntent);
 				return true;
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 
 	@Override
 	public void refreshSchedule(){
 		SharedPreferences settings = 
 			PreferenceManager.getDefaultSharedPreferences(this);
 		boolean showTimeTill = settings.getBoolean(
 			getString(R.string.show_timetill_main_key), false);
 		
 		GregorianCalendar currentTime = showTimeTill ? 
 			LinkSchedule.getCalendarInstance() : null;
 	
 		sextonClock.setClockTime(
 			linkSchedule.getNextTime(getString(R.string.sexton_name)),
 			currentTime);
 		goreckiClock.setClockTime(
 			linkSchedule.getNextTime(getString(R.string.gorecki_name)),
 			currentTime);
 		flynntownClock.setClockTime(
 			linkSchedule.getNextTime(getString(R.string.flynntown_name)),
 			currentTime);
 		hccClock.setClockTime(
 			linkSchedule.getNextTime(getString(R.string.hcc_name)),
 			currentTime);
 	}
 
 	@Override
 	public void resetSchedule(){
 		linkSchedule.reset();
 	}
 
 	@Override
 	public String getCurrentBusStop(){
 		return currentSelectedStop;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(
 		SharedPreferences sharedPreferences,
 		String key)
 	{
 		if(key.equals(getString(R.string.show_timetill_main_key))){
 			refreshSchedule();	
 		}
 	}
 

 }

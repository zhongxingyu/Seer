 package com.kopysoft.chronos;
 
 /**
  * 			Copyright (C) 2011 by Ethan Hall
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  * 	in the Software without restriction, including without limitation the rights
  * 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * 	copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *  
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *  
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  *  
  */
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TabHost;
 
 import com.kopysoft.chronos.content.Chronos;
 import com.kopysoft.chronos.content.Email;
 import com.kopysoft.chronos.content.StaticFunctions;
 import com.kopysoft.chronos.enums.Defines;
 import com.kopysoft.chronos.enums.Verbosity;
 import com.kopysoft.chronos.service.MidnightBroadcast;
 import com.kopysoft.chronos.singelton.ListenerObj;
 import com.kopysoft.chronos.singelton.PreferenceSingelton;
 import com.kopysoft.chronos.singelton.ViewingPayPeriod;
 
 public class mainUI extends TabActivity {
 	/** Called when the activity is first created. */
 	private static final String TAG = Defines.TAG + " - Main";
 
 	private PreferenceSingelton prefs = null;
 	private static final boolean DEBUG_PRINT = Defines.DEBUG_PRINT;
 
 	public void onStop(){
 		super.onStop();
 	}
 
 	public void onDestroy(){
 		super.onDestroy();
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		Intent sentIntent = new Intent(getApplicationContext(), MidnightBroadcast.class);
 		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 
 				Defines.MIDNIGHT_ALARM, sentIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		am.cancel(sender);
 	}
 
 	public void onResume(){
 		super.onResume();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		prefs = new PreferenceSingelton();
 
 		Chronos forUpdate = new Chronos(getApplicationContext());
 		SQLiteDatabase db = forUpdate.getWritableDatabase();
 		db.close();
 		StaticFunctions.fixMidnight(prefs.getStartOfThisPP(getApplicationContext()), 
 				prefs.getWeeksInPP(getApplicationContext()),
 				getApplicationContext());
 		//Chronos chrono = new Chronos(getApplicationContext());
 		//chrono.dropAll();
 		//GenerateContent();
 		//chronoSaver.printAll();
 
 		setUpAlarm();
 
 		GregorianCalendar midnightAlarm = new GregorianCalendar();
 		midnightAlarm.add(Calendar.DAY_OF_YEAR, 1);
 		midnightAlarm.set(Calendar.HOUR_OF_DAY, 0);
 		midnightAlarm.set(Calendar.MINUTE, 0);
 		midnightAlarm.set(Calendar.SECOND, 0);
 		Intent sentIntent = new Intent(getApplicationContext(), MidnightBroadcast.class);
 		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 
 				Defines.MIDNIGHT_ALARM, sentIntent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		am.setRepeating(AlarmManager.RTC, midnightAlarm.getTimeInMillis(), 
 				AlarmManager.INTERVAL_DAY, sender);
 
 		//Resources res = getResources(); // Resource object to get Drawables
 		TabHost tabHost = getTabHost();  // The activity TabHost
 		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
 		Intent intent;  // Reusable Intent for each tab
 
 		// Create an Intent to launch an Activity for the tab (to be reused)
 		intent = new Intent().setClass(this, com.kopysoft.chronos.ClockInAndOut.class);
 
 		// Initialize a TabSpec for each tab and add it to the TabHost
 		//spec = tabHost.newTabSpec("today").setIndicator("Punch",
 		//		res.getDrawable(R.drawable.ic_tab_punch)).setContent(intent);
 		spec = tabHost.newTabSpec("today").setIndicator("Today").setContent(intent);
 		tabHost.addTab(spec);
 
 		intent = new Intent().setClass(this, com.kopysoft.chronos.WeekView.class);
 		spec = tabHost.newTabSpec("edit").setIndicator("Edit").setContent(intent);
 		tabHost.addTab(spec);
 
 
 		intent = new Intent().setClass(this, com.kopysoft.chronos.PastView.class);
 		spec = tabHost.newTabSpec("view").setIndicator("View").setContent(intent);
 		tabHost.addTab(spec);
 
 		tabHost.setCurrentTab(0);
 
 		ListenerObj.getInstance().addPropertyChangeListener(new PropertyChangeListener(){
 			public void propertyChange(PropertyChangeEvent event) {
 				if(DEBUG_PRINT) Log.d(TAG, "Listener");
 				setUpAlarm();
 			}
 		});
 	}
 
 
 	public void setUpAlarm(){
 		//Log.d(TAG, "set up Alarm");
 		// get a Calendar object with current time
 		Calendar cal = Calendar.getInstance();
 		// add 5 minutes to the calendar object
 		StaticFunctions.setUpAlarm(getApplicationContext(), 
 				cal.getTimeInMillis() - 1000, (AlarmManager) getSystemService(ALARM_SERVICE));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		TabHost tabHost = getTabHost();
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 
 		MenuItem editMore = null;
 		MenuItem editNote = null;
 		switch(tabHost.getCurrentTab()){
 		case 0: //Punch Tab
 			editNote = menu.findItem(R.id.addNote);
 			editNote.setVisible(true);
 
 			MenuItem addBreak = menu.findItem(R.id.addBreak);
 			addBreak.setVisible(true);
 			break;
 
 		case 1: //Edit tab
 			editMore = menu.findItem(R.id.editMore);
 			editMore.setVisible(true);
 			break;
 
 		case 2: //View tab
 			editMore = menu.findItem(R.id.editMore);
 			editMore.setVisible(true);
 
 			MenuItem emailMain = menu.findItem(R.id.email);
 			emailMain.setVisible(false);
 			MenuItem emailOption = menu.findItem(R.id.emailOption);
 			emailOption.setVisible(true);
 			break;
 
 		default:
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 
 		int[] startOfViewedPP = null;
 
 		TabHost tabHost = getTabHost();
 		int currentTab = tabHost.getCurrentTab();
 
 		int[] startOfThisPP = prefs.getStartOfThisPP(getApplicationContext());
 		int weeks_in_pp = prefs.getWeeksInPP(getApplicationContext());
 		int[] endOfThisPP = new int[3];
 		{
 			GregorianCalendar cal = new GregorianCalendar(startOfThisPP[0], 
 					startOfThisPP[1], startOfThisPP[2]);
 			cal.add(GregorianCalendar.DAY_OF_YEAR, 7 * weeks_in_pp);
 			endOfThisPP[0] = cal.get(GregorianCalendar.YEAR);
 			endOfThisPP[1] = cal.get(GregorianCalendar.MONTH);
			endOfThisPP[2] = cal.get(GregorianCalendar.DAY_OF_MONTH);
 		}
 		Verbosity verbosLevel = prefs.getReportLevelVerbosity(getApplicationContext());
 
 		//get time for viewed pp
 		ViewingPayPeriod holder = ViewingPayPeriod.getInstance();
 		startOfViewedPP = holder.getWeek();
 		int[] endOfViewed = new int[3];
 		{
 			GregorianCalendar cal = new GregorianCalendar(startOfViewedPP[0], 
 					startOfViewedPP[1], startOfViewedPP[2]);
 			cal.add(GregorianCalendar.DAY_OF_YEAR, 7 * weeks_in_pp);
 			endOfViewed[0] = cal.get(GregorianCalendar.YEAR);
 			endOfViewed[1] = cal.get(GregorianCalendar.MONTH);
			endOfViewed[2] = cal.get(GregorianCalendar.DAY_OF_MONTH);
 		}
 
 		if(Defines.DEBUG_PRINT){
 			if (currentTab == 2){
 				Log.d(TAG, "On tab 2");
 				Log.d(TAG, "Input Date: " + String.format("(%d/%d/%d)", startOfViewedPP[0], startOfViewedPP[1], startOfViewedPP[2]));
 			} else {
 				Log.d(TAG, "Input Date: " + String.format("(%d/%d/%d)", startOfThisPP[0], startOfThisPP[1], startOfThisPP[2]));
 			}
 		} else {
 			//Not printing anythind
 		}
 
 		switch (item.getItemId()) {
 		case R.id.email:
 			if (currentTab == 2){
 				if (Defines.DEBUG_PRINT) Log.d(TAG, "Sending Email from Tab 2");
 				send_email(startOfViewedPP, endOfThisPP, verbosLevel);
 			} else {
 				if (Defines.DEBUG_PRINT) Log.d(TAG, "Sending Email from not Tab 2");
 				send_email(startOfThisPP, endOfThisPP, verbosLevel);
 			}		
 
 			break;
 		case R.id.preferences:
 			Intent prefIntent = new Intent(this, com.kopysoft.chronos.subActivites.Preferences.class);
 			startActivity(prefIntent);
 			break;
 
 		case R.id.editMore:
 			Intent editIntent = new Intent(this, com.kopysoft.chronos.EditPayPeriodOthers.class);
 
 			//put things into the intent
 			if (currentTab == 2){
 				editIntent.putExtra("year",	startOfViewedPP[0]);
 				editIntent.putExtra("month",startOfViewedPP[1]);
 				editIntent.putExtra("day",	startOfViewedPP[2]);
 			} else {
 				editIntent.putExtra("year",	startOfThisPP[0]);
 				editIntent.putExtra("month",startOfThisPP[1]);
 				editIntent.putExtra("day",	startOfThisPP[2]);
 			}
 
 			startActivity(editIntent);
 			break;
 
 		case R.id.emailThis:
 			send_email(startOfViewedPP, endOfViewed, verbosLevel);
 			break;
 
 		case R.id.emailCurrent:
 			send_email(startOfThisPP, endOfThisPP, verbosLevel);
 			break;
 
 		case R.id.addNote:
 			Intent editNote = new Intent(this, com.kopysoft.chronos.note.EditNote.class);
 			//Add values to intent
 			GregorianCalendar tempCal = new GregorianCalendar();
 			int[] sendDate = new int[3];
 			sendDate[0] = tempCal.get(Calendar.YEAR);
 			sendDate[1] = tempCal.get(Calendar.MONTH);
 			sendDate[2] = tempCal.get(Calendar.DAY_OF_MONTH);
 
 			editNote.putExtra("year",	sendDate[0]);
 			editNote.putExtra("month",	sendDate[1]);
 			editNote.putExtra("day",	sendDate[2]);
 
 			//Start Intent
 			startActivity(editNote);
 			break;
 
 		case R.id.addBreak:
 			Intent addBreak = new Intent(this, com.kopysoft.chronos.subActivites.AddBreak.class);
 			startActivity(addBreak);
 			break;
 
 		case R.id.help:
 			Intent helpActivity = new Intent(this, com.kopysoft.chronos.subActivites.HelpActivity.class);
 			startActivity(helpActivity);
 			break;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	public void send_email(int[] startOfThisPP, int[] endOfThisPP, Verbosity verbosLevel){
 		Email newEmail = new Email(startOfThisPP, endOfThisPP, verbosLevel, getApplicationContext());
 		new sendEmailTask().execute(newEmail);
 	}
 
 	private class sendEmailTask extends AsyncTask<Email, Void, Void> {
 		ProgressDialog dialog = null;
 		protected void onPreExecute(){
 			dialog = ProgressDialog.show(mainUI.this, "",
 			"Generating. Please wait..."); 
 		}
 
 		protected void onPostExecute (Void param){
 			dialog.dismiss();
 		}
 
 		@Override
 		protected Void doInBackground(Email... param) {
 			sendEmailInit(param[0]);
 			return null;
 		}
 
 		private void sendEmailInit(Email newEmail){
 			String emailBody = new String("Greetings!\n\tHere is my timecard:\n");
 			emailBody += newEmail.generateEmailText();
 
 			if(Defines.DEBUG_PRINT) Log.d(TAG, emailBody);
 
 			//Create email
 			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Time Card");
 			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
 
 			emailIntent.setType("message/rfc822");
 			startActivity(emailIntent);
 
 		}
 	}
 }

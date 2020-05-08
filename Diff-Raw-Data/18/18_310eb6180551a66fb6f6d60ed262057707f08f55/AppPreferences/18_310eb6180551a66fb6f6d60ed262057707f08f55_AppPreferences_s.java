 package com.typeiisoft.lct.utils;
 
 import android.app.Activity;
import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 /**
  * This class handles the shared preference for the application. It is taken 
  * from this article: http://stackoverflow.com/questions/5734721/android-shared-preferences
  * 
  * @author Michael Reuter
  *
  */
 public class AppPreferences {
 	private static final String APP_SHARED_PREFS = "com.typeiisoft.lct.shared_prefs";
 	private SharedPreferences sharedPrefs;
 	private Editor prefsEditor;
 	
 	/**
 	 * This is the class constructor.
 	 * @param activity : Associated activity
 	 */
 	public AppPreferences(Activity activity) {
 		this.sharedPrefs = activity.getSharedPreferences(APP_SHARED_PREFS, 
 				Activity.MODE_PRIVATE);
 		this.prefsEditor = this.sharedPrefs.edit();
 	}
 	
 	/**
 	 * This function sets the individual components of the observation date 
 	 * and time.
 	 * @param day : The observation day of month.
 	 * @param month : The observation month.
 	 * @param year : The observation year.
 	 * @param hours : The observation hour of day.
 	 * @param minutes : The observation minutes of hour.
 	 * @param seconds : The observation seconds of minute.
 	 * @param offset : The time in hours from UTC.
 	 */
 	public void setDateTime(int day, int month, int year, int hours, 
 			int minutes, int seconds, int offset) {
 		this.prefsEditor.putInt("obsdate_day", day);
 		this.prefsEditor.putInt("obsdate_month", month);
 		this.prefsEditor.putInt("obsdate_year", year);
 		this.prefsEditor.putInt("obsdate_hours", hours);
 		this.prefsEditor.putInt("obsdate_minutes", minutes);
 		this.prefsEditor.putInt("obsdate_seconds", seconds);
 		this.prefsEditor.putInt("obsdate_offset", offset);
 		this.prefsEditor.commit();
 	}
 	
 	/**
 	 * This function gathers the currently held observation date and time into 
 	 * an array. The order of the array is: (day_of_month, month, year, hours,
 	 * minutes, seconds, utc_offset).
 	 * @return : The currently held observation date and time.
 	 */
 	public int[] getDateTime() {
 		int[] currDateTime = {this.sharedPrefs.getInt("obsdate_day", 20),
 				this.sharedPrefs.getInt("obsdate_month", 6),
 				this.sharedPrefs.getInt("obsdate_year", 2012),
 				this.sharedPrefs.getInt("obsdate_hours", 20),
 				this.sharedPrefs.getInt("obsdate_minutes", 0),
 				this.sharedPrefs.getInt("obsdate_seconds", 0),
 				this.sharedPrefs.getInt("obsdate_offset", 0)};
 		return currDateTime;
 	}
 }

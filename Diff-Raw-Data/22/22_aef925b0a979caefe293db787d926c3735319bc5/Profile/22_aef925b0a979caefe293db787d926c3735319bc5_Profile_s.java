 package org.istic.nestor.profile.utils;
 
 import java.util.LinkedHashMap;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.text.format.Time;
 import android.util.Log;
 
 public class Profile {
 	/* Donnees pour le stockage via le ProfileProvider */
 	public static final String AUTHORITY = "org.istic.nestor.profile.provider.ProfileProvider";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 			+ "/profiles");
 	/* Nom des tables */
 	public static final String PROFILE_ID = "id";
 	public static final String PROFILE_NAME = "name";
 	public static final String PROFILE_AUDIO = "audio";
 	public static final String PROFILE_BLUETOOTH = "bluetooth";
 	public static final String PROFILE_RADIO = "radio";
 	public static final String PROFILE_WIFI = "wifi";
 	public static final String PROFILE_GPS = "gps";
 	//	Time
 	public static final String PROFILE_START = "start";
 	public static final String PROFILE_END = "end";
 	
 	//	DAYS
 	public static final String Monday = "monday";
 	public static final String Tuesday = "tuesday";
 	public static final String Wednesday = "wednesday";
 	public static final String Thursday = "thursday";
 	public static final String Friday = "friday";
 	public static final String Saturday = "saturday";
 	public static final String Sunday = "sunday";
 	
 
 	private int id;
 	private int audio; // Audio Mode
 	private boolean radio; // 3G
 	private boolean gps;
 	private boolean wifi;
 	private boolean bluetooth;
 	private String name;
 	private Time startTime;
 	private Time endTime;
 	private LinkedHashMap<Integer, Boolean> dayEnabled;
 	
 /*
 	public static Profile defaultProfile = new Profile(
 			"Profil de test par défaut", 0, false, true, true, true);
 	public static Profile defaultProfile2 = new Profile(
 			"Profil de test par défaut numero 2", 1, false, true, true, true);
 */
 	private Profile() {
 		dayEnabled = new LinkedHashMap<Integer, Boolean>();
 		dayEnabled.put(Time.MONDAY, false);
 		dayEnabled.put(Time.TUESDAY, false);
 		dayEnabled.put(Time.WEDNESDAY, true);	//switch to false
 		dayEnabled.put(Time.THURSDAY, false);
 		dayEnabled.put(Time.FRIDAY, false);
 		dayEnabled.put(Time.SATURDAY, true);//switch to false
 		dayEnabled.put(Time.SUNDAY, false);
 	}
 
 	public Profile(String name, int audio, boolean radio, boolean wifi,
 			boolean bluetooth, boolean gps, Time startTime, Time endTime) {
 		this.name = name;
 		this.audio = audio;
 		this.radio = radio;
 		this.wifi = wifi;
 		this.bluetooth = bluetooth;
 		dayEnabled = new LinkedHashMap<Integer, Boolean>();
 		dayEnabled.put(Time.MONDAY, false);
 		dayEnabled.put(Time.TUESDAY, false);
 		dayEnabled.put(Time.WEDNESDAY, false);
 		dayEnabled.put(Time.THURSDAY, false);
 		dayEnabled.put(Time.FRIDAY, false);
 		dayEnabled.put(Time.SATURDAY, false);
 		dayEnabled.put(Time.SUNDAY, false);
 		this.startTime = startTime;
 		this.endTime = endTime;
 	}
 	public Profile(String name, int audio, boolean radio, boolean wifi,
 			boolean bluetooth, boolean gps, Time startTime, Time endTime,LinkedHashMap<Integer, Boolean> map) {
		new Profile(name, audio, radio, wifi, bluetooth, gps, startTime, endTime);
 		dayEnabled = map;
 	}
 	
 	public void setDayEnabled (int day, boolean isEnable){
 		dayEnabled.put(day, isEnable);
		Log.d("Profile", "Tailel de la hashmpad(7 normalement) = "+dayEnabled.size());
 	}
 
 	// Getters and Setters
 	public int getAudio() {
 		return audio;
 	}
 
 	public void setAudio(int audio) {
 		this.audio = audio;
 	}
 
 	public boolean isRadio() {
 		return radio;
 	}
 
 	public void setRadio(boolean radio) {
 		this.radio = radio;
 	}
 
 	public boolean isWifi() {
 		return wifi;
 	}
 
 	public void setWifi(boolean wifi) {
 		this.wifi = wifi;
 	}
 
 	public boolean isBluetooth() {
 		return bluetooth;
 	}
 
 	public void setBluetooth(boolean bluetooth) {
 		this.bluetooth = bluetooth;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public boolean isGps() {
 		return gps;
 	}
 
 	public void setGps(boolean gps) {
 		this.gps = gps;
 	}
 
 	public Time getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Time startTime) {
 		this.startTime = startTime;
 	}
 
 	public Time getEndTime() {
 		return endTime;
 	}
 
 	public void setEndTime(Time endTime) {
 		this.endTime = endTime;
 	}
 
 	public String tosString() {
 		return name;
 	}
 
 	public Uri getUri() {
 		return Uri.parse(Profile.CONTENT_URI + "/" + id);
 	}
 
 	public LinkedHashMap<Integer, Boolean> getDayEnabled() {
 		return dayEnabled;
 	}
 
 	public static Profile getProfile(Cursor cursor) {
 		Profile profile = new Profile();
 		profile.setId(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_ID)));
 		profile.setAudio(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_AUDIO)));
 		profile.setBluetooth(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_BLUETOOTH)) == 1 ? true
 				: false);
 		profile.setGps(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_GPS)) == 1 ? true
 				: false);
 		profile.setRadio(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_RADIO)) == 1 ? true
 				: false);
 		profile.setWifi(cursor.getInt(cursor
 				.getColumnIndexOrThrow(Profile.PROFILE_WIFI)) == 1 ? true
 				: false);
 		profile.setName(cursor.getString(cursor
 				.getColumnIndexOrThrow(PROFILE_NAME)));
 		profile.setDayEnabled(Time.MONDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Monday))==1 ? true:false);
 		profile.setDayEnabled(Time.TUESDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Tuesday))==1 ? true:false);
 		profile.setDayEnabled(Time.WEDNESDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Wednesday))==1 ? true:false);
 		profile.setDayEnabled(Time.THURSDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Thursday))==1 ? true:false);
 		profile.setDayEnabled(Time.FRIDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Friday))==1 ? true:false);
 		profile.setDayEnabled(Time.SATURDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Saturday))==1 ? true:false);
 		profile.setDayEnabled(Time.SUNDAY, cursor.getInt(cursor.getColumnIndexOrThrow(Profile.Sunday))==1 ? true:false);
 		Log.d("Profile", "Name du constructeur statique "+profile.getName());
 		return profile;
 	}
 }

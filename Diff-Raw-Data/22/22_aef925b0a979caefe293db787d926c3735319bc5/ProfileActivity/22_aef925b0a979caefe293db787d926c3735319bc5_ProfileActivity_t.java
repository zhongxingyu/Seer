 package org.istic.nestor.profile.activity;
 
 import java.util.LinkedHashMap;
 
 import org.istic.nestor.R;
 import org.istic.nestor.profile.utils.Profile;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.widget.CheckBox;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class ProfileActivity extends Activity {
 	private final String TAG = "ProfileActivity";
 	private boolean doInsert;// = false;
 	private int profileId = -1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.profile_edit);
 		Log.d(TAG, "ProfileActivity created");
 		Uri profileUri = getIntent().getData();
 		if (profileUri != null) {
 			Log.d(TAG, profileUri.getLastPathSegment());
 			String[] projection = new String[] { Profile.PROFILE_ID,
 					Profile.PROFILE_AUDIO, Profile.PROFILE_BLUETOOTH,
 					Profile.PROFILE_GPS, Profile.PROFILE_RADIO,
 					Profile.PROFILE_WIFI, Profile.PROFILE_NAME, Profile.Monday, Profile.Tuesday, Profile.Wednesday,Profile.Thursday,Profile.Friday,Profile.Saturday,Profile.Sunday };
 			String selection = Profile.PROFILE_ID + "=?";
 			String[] selectionArgs = new String[] { profileUri
 					.getLastPathSegment() };
 			Cursor cursor = getContentResolver().query(Profile.CONTENT_URI,
 					projection, selection, selectionArgs, null);
 			if (cursor.getCount() >= 1) {
 				Log.d(TAG, "Update mode");
 				cursor.moveToFirst();
 				Profile profile = Profile.getProfile(cursor);
 				setProfile(profile);
 				profileId = profile.getId();
 				doInsert = false;
 			}
 		} else {
 			Log.d(TAG, "Insert mode");
 			doInsert = true;
 		}
 	}
 
 	// Setting UI with the given profile
 	public void setProfile(Profile profile) {
 		ToggleButton wifiButton = ((ToggleButton) findViewById(R.id.wifitoggle));
 		ToggleButton bluetoothButton = ((ToggleButton) findViewById(R.id.bluetoothtoggle));
 		ToggleButton radioButton = ((ToggleButton) findViewById(R.id.edgetoggle));
 		ToggleButton gpsButton = ((ToggleButton) findViewById(R.id.gpstoggle));
 		TextView textView = ((TextView) findViewById(R.id.profile_text));
 
 		int audioButtonId;
 		switch (profile.getAudio()) {
 		case AudioManager.RINGER_MODE_NORMAL:
 			audioButtonId = R.id.normal;
 			break;
 		case AudioManager.RINGER_MODE_SILENT:
 			audioButtonId = R.id.silent;
 			break;
 		case AudioManager.RINGER_MODE_VIBRATE:
 			audioButtonId = R.id.vibrate;
 			break;
 		default:
 			audioButtonId = R.id.normal;
 			break;
 		}
 		RadioButton audioChecked = (RadioButton) findViewById(audioButtonId);
 
 		CheckBox dayCheck;
 		dayCheck =(CheckBox) findViewById(R.id.mondayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.MONDAY));
 		dayCheck =(CheckBox) findViewById(R.id.tuesdayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.TUESDAY));
 		dayCheck =(CheckBox) findViewById(R.id.wednesdayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.WEDNESDAY));
 		dayCheck =(CheckBox) findViewById(R.id.thursdayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.THURSDAY));
 		dayCheck =(CheckBox) findViewById(R.id.fridayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.FRIDAY));
 		dayCheck =(CheckBox) findViewById(R.id.saturdayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.SATURDAY));
 		dayCheck =(CheckBox) findViewById(R.id.sundayCheck);
 		dayCheck.setChecked(profile.getDayEnabled().get(Time.SUNDAY));
 
 		bluetoothButton.setChecked(profile.isBluetooth());
 		radioButton.setChecked(profile.isRadio());
 		gpsButton.setChecked(profile.isGps());
 		wifiButton.setChecked(profile.isWifi());
 		textView.setText(profile.getName());
 		audioChecked.setChecked(true);
 	}
 
 	// Getting audio profile from buttons
 	private Profile getProfile() {
 		Log.d(TAG, "Getting audio profile from screen");
 		String name = ((TextView) findViewById(R.id.profile_text)).getText()
 				.toString();
 		// Switch to correct mode
 		boolean bluetooth = ((ToggleButton) findViewById(R.id.bluetoothtoggle))
 				.isChecked();
 		boolean gps = ((ToggleButton) findViewById(R.id.gpstoggle)).isChecked();
 		boolean radio = ((ToggleButton) findViewById(R.id.edgetoggle))
 				.isChecked();
 		boolean wifi = ((ToggleButton) findViewById(R.id.wifitoggle))
 				.isChecked();
 		int audio;
 		int audioChecked = ((RadioGroup) findViewById(R.id.audio_selected))
 				.getCheckedRadioButtonId();
 		switch (audioChecked) {
 		case R.id.normal:
 			audio = AudioManager.RINGER_MODE_NORMAL;
 			break;
 		case R.id.vibrate:
 			audio = AudioManager.RINGER_MODE_VIBRATE;
 			break;
 		case R.id.silent:
 			audio = AudioManager.RINGER_MODE_SILENT;
 			break;
 		default:
 			audio = AudioManager.RINGER_MODE_NORMAL;
 			break;
 		}
 		Time start = new Time();
 		start.set(0, 30, 13, 0, 0, 0);
 		Time end = new Time();
 		start.set(0, 30, 14, 0, 0, 0);
 		
 		LinkedHashMap<Integer,Boolean> map = new LinkedHashMap<Integer, Boolean>();
 		boolean mondayCheck = ((CheckBox) findViewById(R.id.mondayCheck)).isChecked();
 		map.put(Time.MONDAY, mondayCheck);
 		boolean tuesdayCheck = ((CheckBox) findViewById(R.id.tuesdayCheck)).isChecked();
 		map.put(Time.TUESDAY, tuesdayCheck);
 		boolean wednesdayCheck = ((CheckBox) findViewById(R.id.wednesdayCheck)).isChecked();
 		map.put(Time.WEDNESDAY, wednesdayCheck);
 		boolean thursdayCheck = ((CheckBox) findViewById(R.id.thursdayCheck)).isChecked();
 		map.put(Time.THURSDAY, thursdayCheck);
 		boolean fridayCheck = ((CheckBox) findViewById(R.id.fridayCheck)).isChecked();
 		map.put(Time.FRIDAY, fridayCheck);
 		boolean saturdayCheck = ((CheckBox) findViewById(R.id.saturdayCheck)).isChecked();
 		map.put(Time.SATURDAY, saturdayCheck);
 		boolean sundayCheck = ((CheckBox) findViewById(R.id.sundayCheck)).isChecked();
 		map.put(Time.SUNDAY, sundayCheck);
 
 		Log.d(TAG, "name = "+name);
 		Profile p = new Profile(name, audio, radio, wifi, bluetooth, gps,
 				start, end, map);
		Log.d(TAG,"name apres creation"+p.getName());
 		if (profileId != -1)
 			p.setId(profileId);
 		return p;
 	}
 
 	@Override
 	protected void onPause() {
 		Log.d(TAG, "PAUSE");
 		Profile profile = getProfile();
 		ContentValues values = new ContentValues();
 		values.put(Profile.PROFILE_NAME, profile.getName());
 		values.put(Profile.PROFILE_AUDIO, profile.getAudio());
 		values.put(Profile.PROFILE_BLUETOOTH, profile.isBluetooth() ? 1 : 0);
 		values.put(Profile.PROFILE_GPS, profile.isGps() ? 1 : 0);
 		values.put(Profile.PROFILE_RADIO, profile.isRadio() ? 1 : 0);
 		values.put(Profile.PROFILE_WIFI, profile.isWifi() ? 1 : 0);
 		values.put(Profile.Monday, profile.getDayEnabled().get(Time.MONDAY) ? 1 : 0);
 		values.put(Profile.Tuesday, profile.getDayEnabled().get(Time.TUESDAY) ? 1 : 0);
 		values.put(Profile.Wednesday, profile.getDayEnabled().get(Time.WEDNESDAY) ? 1 : 0);
 		values.put(Profile.Thursday, profile.getDayEnabled().get(Time.THURSDAY) ? 1 : 0);
 		values.put(Profile.Friday, profile.getDayEnabled().get(Time.FRIDAY) ? 1 : 0);
 		values.put(Profile.Saturday, profile.getDayEnabled().get(Time.SATURDAY) ? 1 : 0);
 		values.put(Profile.Sunday, profile.getDayEnabled().get(Time.SUNDAY) ? 1 : 0);
 		if (doInsert) {
 			getContentResolver().insert(Profile.CONTENT_URI, values);
 			Log.d(TAG, "Inserting new data");
 		} else {
 			int result = getContentResolver().update(Profile.CONTENT_URI,
 					values, Profile.PROFILE_ID + " =? ",
 					new String[] { "" + profile.getId() });
 			Log.d(TAG, "Updateing data " + result);
 		}
 		super.onPause();
 	}
 }

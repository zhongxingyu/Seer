 package de.Psychologie.socialintelligence;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class UserSettingActivity extends PreferenceActivity {
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onStart() {
 	super.onStart();
 
 	// Get the xml/prefx.xml preferences
 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	
 	CustomRingtonepreference ringtonepref = (CustomRingtonepreference) findPreference("ringtone");
 	
 	
 	
 	//Import All Ringtones
 	RingtoneManager rm = new RingtoneManager(UserSettingActivity.this);
 	rm.setType(RingtoneManager.TYPE_ALARM|RingtoneManager.TYPE_RINGTONE );
     final Cursor ringtones = rm.getCursor();
     
     List<String> mEntries = new ArrayList<String>();
     List<String> mEntryValues = new ArrayList<String>();
     int i=0;
     for (ringtones.moveToFirst(); !ringtones.isAfterLast(); ringtones.moveToNext()) {
 		mEntries.add(ringtones.getString(RingtoneManager.TITLE_COLUMN_INDEX));
    	Log.i("RingtoneEntry["+i+"]",mEntries.get(i));
		mEntryValues.add(ringtones.getString(RingtoneManager.URI_COLUMN_INDEX));
    	Log.i("RingtoneValue["+i+"]",mEntryValues.get(i));
 		i++;
     }
     //TODO: Import cygnus.ogg for devices without ringtones
     //int MySongName = R.raw.alarmtone;
     //Uri MySongUri = Uri.parse("android.resource://" + getPackageName() + "/"+MySongName);
     
     //mEntries.add(getResources().getResourceEntryName(MySongName));
 	//mEntryValues.add(MySongUri.toString());
 	
     ringtonepref.setEntryValues(mEntryValues.toArray(new CharSequence[mEntryValues.size()]));
     ringtonepref.setEntries(mEntries.toArray(new CharSequence[mEntries.size()]));
     
 	// Set Sleeptime summary to chosen time		
 	String sleeptimesummary = prefs.getString("Sleeptime",	"5 Minuten");
 	Preference sleeptimepref = (Preference) findPreference("Sleeptime");		
 	sleeptimepref.setSummary(sleeptimesummary+ " \tMinuten");	
 	
 	// Set Ringtonepreference summary to chosen title
 	String ringtonename = prefs.getString("ringtone", "");
 	Uri ringtoneUri = null;
 	
 	//Set std Alarm
 	if (ringtonename == ""){
 		// Standart wird gesetzt, falls noch keiner da
 		ringtoneUri=RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(),RingtoneManager.TYPE_ALARM);
 		//if(ringtoneUri==null)
 		//	ringtoneUri= MySongUri;
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString("ringtone", ringtoneUri.toString());
 		editor.commit();
 		//Sets the default Alarm to the chosen Value
 		ringtonepref.setValue(ringtoneUri.toString());
 	}
 	else
 		ringtoneUri = Uri.parse((String) ringtonename);	
 	
 	Ringtone ringtone = RingtoneManager.getRingtone(UserSettingActivity.this, ringtoneUri);	
 	String name = ringtone.getTitle(UserSettingActivity.this);
 
 	//Set summary of Alarm
 	ringtonepref.setSummary(name);
 	}
 
 
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 ActivityRegistry.register(this);
 		
 		addPreferencesFromResource(R.xml.preferences);
 		
 		//TODO:AppBar in Settings
 		//com.markupartist.android.widget.ActionBar actionBar = (com.markupartist.android.widget.ActionBar) findViewById(R.id.actionbar);
 		//actionBar.inflate(getBaseContext(), resource, root)
 		//setup any other views that you have
  		
 		//com.markupartist.android.widget.ActionBar bar = (com.markupartist.android.widget.ActionBar) findViewById(R.id.settings_actionbar);
 		//setContentView(R.layout.settingsbar); //set the contentview. On the layout, you need a listview with the id: @android:id/list
 		
 		Preference button_week = (Preference) findPreference("button_week");
 		button_week.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference arg0) {
 						startActivity(new Intent(UserSettingActivity.this,
 								Week.class));
 						return true;
 					}
 				});
 		Preference button_poll = (Preference) findPreference("button_poll");
 		button_poll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference arg0) {
 						startActivity(new Intent(UserSettingActivity.this,
 								PopPollActivity.class));
 						return true;
 					}
 				});
 		Preference numpicker = (Preference) findPreference("numpicker");
 		numpicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference arg0) {
 						startActivity(new Intent(UserSettingActivity.this,
 								Numpicker.class));
 						return true;
 					}
 				});
 		
 		
 
 		Preference button_test = (Preference) findPreference("button_test");
 		button_test
 		.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference arg0) {
 				startActivity(new Intent(UserSettingActivity.this,	Alarm_Activity.class));
 				return true;
 			}
 				});
 		
 		Preference button_about = (Preference) findPreference("button_about");
 		button_about
 				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference arg0) {
 						AlertDialog ad = new AlertDialog.Builder(
 								UserSettingActivity.this).create();
 						ad.setTitle(getResources().getString(
 								R.string.title_about));
 						ad.setMessage(getResources().getString(
 								R.string.message_about));
 						ad.setButton(getResources().getString(
 								R.string.OK),
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										dialog.dismiss();
 									}
 								});
 						ad.show();
 						return true;
 					}
 				});
 
 		CustomRingtonepreference ringtonepref = (CustomRingtonepreference) findPreference("ringtone");
 		ringtonepref
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,Object newValue) {
 						
 						Log.d("OnprefChange", "newValue="+newValue.toString());
 						Uri ringtoneUri = Uri.parse((String) newValue);
 						Ringtone ringtone = RingtoneManager.getRingtone(UserSettingActivity.this, ringtoneUri);
 						String name = ringtone.getTitle(UserSettingActivity.this);
 
 						preference.setSummary( name);
 						// Save Ringtone to preferences
 						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 						SharedPreferences.Editor editor = prefs.edit();
 						editor.putString("ringtone", ringtoneUri.toString());
 						editor.commit();
 
 						return true;
 					}
 				});
 
 		ListPreference sleeptimechooser = (ListPreference) findPreference("Sleeptime");
 		sleeptimechooser
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						preference.setSummary(((String) newValue) + " \tMinuten");
 						return true;
 					}
 				});
 		
 		Preference button_admin_settings = (Preference) findPreference("button_admin_settings");
 		button_admin_settings
 				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(final Preference arg0) {
 						
 						final AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
 						builder.setTitle(getResources().getString(R.string.title_password_entry));
 						final EditText input = new EditText(UserSettingActivity.this); 
 					    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UserSettingActivity.this);
 											    					 			    
 						input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
 						builder.setView(input)
 				               .setPositiveButton(getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
 				                   public void onClick(DialogInterface dialog, int id) {
 				           			//Passwortberprfung mit Salt
 				                	//Falls kein PW gesetzt ist, ist das standart PW: 
 				                 	if (MD5(input.getText().toString()+getResources().getString(R.string.salt)).equals(settings.getString("password", MD5(getResources().getString(R.string.std_PW)+getResources().getString(R.string.salt))))){
 				                        finish();
 				                 		startActivity(new Intent(UserSettingActivity.this,AdminSettingsActivity.class));
 				                 		overridePendingTransition(0, 0);
 				                  		}	
 				                   else
 				                   {				                	   
 				   						Toast.makeText(getApplicationContext(),getResources().getString(R.string.false_password), Toast.LENGTH_SHORT).show();
 				   						onPreferenceClick(arg0);
 				                   }
 				                   }
 				               })
 				               .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
 				                   public void onClick(DialogInterface dialog, int id) {				                	   
 				                	   dialog.cancel();
 				                   }
 				               }); 
 				        
 						final AlertDialog dialog = builder.create();   
 						
 					      //show keyboard
 						    input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 						        @Override
 						        public void onFocusChange(View v, boolean hasFocus) {
 						            if (hasFocus) {
 						                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 						            }
 						        }
 						    });
 						    
 				        dialog.show();
 						return true;
 					}
 				});
 	}
 
 	@Override
 	public void onBackPressed() {
 		// Wenn es nicht der knoten ist, soll es geschlossen werden
 		if (!this.isTaskRoot())
 			this.finish();
 		else
 			super.onBackPressed();
 	}
 	
 	// MD5 Funktion fr Passwrter
 	public static String MD5(String md5) {
 		try {
 			java.security.MessageDigest md = java.security.MessageDigest
 					.getInstance("MD5");
 			byte[] array = md.digest(md5.getBytes());
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < array.length; ++i) {
 				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
 						.substring(1, 3));
 			}
 			return sb.toString();
 		} catch (java.security.NoSuchAlgorithmException e) {
 		}
 		return null;
 	}
 	
	
	
 }

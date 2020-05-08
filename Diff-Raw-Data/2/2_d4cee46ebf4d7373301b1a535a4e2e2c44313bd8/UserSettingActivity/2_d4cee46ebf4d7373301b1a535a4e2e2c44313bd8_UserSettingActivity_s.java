 package de.Psychologie.socialintelligence;
 
 import java.security.MessageDigest;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.RingtonePreference;
 import android.text.InputType;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class UserSettingActivity extends PreferenceActivity {
 
 	@Override
 	protected void onStart() {
 	super.onStart();
 
 	// Set Ringtonepreference summary to chosen title
 	// Get the xml/prefx.xml preferences
 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 	
	String ringtonename = prefs.getString("ringtone",	"DEFAULT_RINGTONE_URI");
 	Preference ringtonepref = (Preference) findPreference("ringtone");
 	
 	//Get real song title
 	Uri ringtoneUri = Uri.parse((String) ringtonename);
 	Ringtone ringtone = RingtoneManager.getRingtone(UserSettingActivity.this, ringtoneUri);
 	String name = ringtone.getTitle(UserSettingActivity.this);
 	
 	ringtonepref.setSummary(name);
 	
 	// Set Sleeptime summary to chosen time		
 	String sleeptimesummary = prefs.getString("Sleeptime",	"5 Minuten");
 	Preference sleeptimepref = (Preference) findPreference("Sleeptime");		
 	sleeptimepref.setSummary(sleeptimesummary+ " \tMinuten");	
 	
 	}
 
 
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.preferences);
 
 		Preference button_week = (Preference) findPreference("button_week");
 		button_week
 				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
 					@Override
 					public boolean onPreferenceClick(Preference arg0) {
 						startActivity(new Intent(UserSettingActivity.this,
 								Week.class));
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
 
 		RingtonePreference ringtonepref = (RingtonePreference) findPreference("ringtone");
 		ringtonepref
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						Uri ringtoneUri = Uri.parse((String) newValue);
 						Ringtone ringtone = RingtoneManager.getRingtone(
 								UserSettingActivity.this, ringtoneUri);
 						String name = ringtone.getTitle(UserSettingActivity.this);
 
 						preference.setSummary( name);
 
 						return true;
 					}
 				});
 
 		ListPreference sleeptimechooser = (ListPreference) findPreference("Sleeptime");
 		sleeptimechooser
 				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference,
 							Object newValue) {
 						preference
 								.setSummary(((String) newValue) + " \tMinuten");
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

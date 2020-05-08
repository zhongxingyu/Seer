 package org.blanco.lacuenta;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 /***
  * @author Alexandro Blanco <ti3r.bubblenet@gmail.com>
  * Class that will extends the Activity class in order to 
  * present an Interface to the User to control application
  * settings.
  */
 public class SettingsActivity extends Activity {
 
 	public static final String SHARED_PREFS_NAME = "la_cuent_prefs";
 	
 	public static final String SAVE_PREFS_SETTING_NAME = "save_prefs";
 	public static final String SHOW_RES_DIALOG_SETTING_NAME = "show_r_on_dialog";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setting);
 		initComponents();
 		setResult(0);
 	}
 	
 	private void initComponents(){
 		prepareSettingsCheckBox(this.chkPreferences, R.id.settings_activity_chk_save_prefs, SAVE_PREFS_SETTING_NAME);
 		prepareSettingsCheckBox(this.chkShowResultOnDialog, R.id.settings_activity_chk_show_res_on_dialog, SHOW_RES_DIALOG_SETTING_NAME);
 	}
 	
 	/***
 	 * This method retrieves a CheckBox control, stores it in the passed identifier, sets a CheckChanged listener
 	 * to store the vale of the control in the SHARED_PREFS_NAME shared preferences with the passed
 	 * settingName. This in order to relate one CheckBox with one boolean share preference.
 	 * @param controlPtr The Identifier of the object where the retrieved control will be stored
 	 * @param controlId The int number of the control to be retrieved from the current contentView
 	 * @param settingName The String setting name where the state of the combo will be saved.
 	 */
 	private void prepareSettingsCheckBox(CheckBox controlPtr, int controlId, final String settingName){
 		
 		controlPtr = (CheckBox) findViewById(controlId); // retrieve the control
 		//Set the check listener to save the pref when changed.
 		controlPtr.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 				getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit().putBoolean(settingName, arg1).commit();
 			}
 		});
 		//Load the state of the control from the shared preferences
 		controlPtr.setChecked(getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getBoolean(settingName, false));
 	}
 	
 	CheckBox chkPreferences = null;
 	CheckBox chkShowResultOnDialog = null;
 }

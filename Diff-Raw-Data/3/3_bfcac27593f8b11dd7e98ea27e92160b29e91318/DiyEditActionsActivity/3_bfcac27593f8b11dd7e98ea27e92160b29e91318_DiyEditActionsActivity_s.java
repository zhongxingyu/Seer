 package com.diyapp.kreator2;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.ToggleButton;
 
 import com.diyapp.kreator2.R;
 import com.diyapp.lib.DiyDbAdapter;
 
 public class DiyEditActionsActivity extends Activity {
 	ToggleButton maction_wifi;//
 	CheckBox maction_wifi_param_turn_on;//
 	CheckBox  maction_wifi_param_turn_off;//
 	EditText maction_wifi_param_ssid;//
 	ToggleButton maction_notification;//
 	EditText maction_notification_param_title;//
 	EditText maction_notification_param_text;//
 	ToggleButton maction_widgettext;//
 	EditText maction_widgettext_param_text;//
 	CheckBox maction_widgettext_param_display_date;//
 	CheckBox maction_widgettext_param_display_coordinates;//
 	CheckBox maction_widgettext_param_display_address;//
 	CheckBox maction_widgettext_param_display_wifissid;//
 	CheckBox maction_widgettext_param_display_action_description;//
 	CheckBox maction_soundprofile_param_profile_sound;//
 	CheckBox maction_soundprofile_param_profile_vibrations;//
 	ToggleButton maction_soundprofile;//
 	SeekBar maction_soundprofile_param_volume;//
 	CheckBox maction_notification_param_www_switch;//
 	EditText maction_notification_param_www_text;//
 	// TEMPLATE: {field} m{lowercase};//
 	
 	Long mRowId;
 	
 	private DiyDbAdapter mDbHelper;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		mDbHelper = new DiyDbAdapter(this);
 		mDbHelper.open();
 		
 		setContentView(R.layout.activity_diy_edit_actions);
 		setTitle(R.string.edit_diy);
 		
 		maction_wifi = (ToggleButton) findViewById(R.id.action_wifi);//
 		maction_wifi_param_turn_on = (CheckBox) findViewById(R.id.action_wifi_param_turn_on);//
 		maction_wifi_param_turn_off = (CheckBox ) findViewById(R.id.action_wifi_param_turn_off);//
 		maction_wifi_param_ssid = (EditText) findViewById(R.id.action_wifi_param_ssid);//
 		maction_notification = (ToggleButton) findViewById(R.id.action_notification);//
 		maction_notification_param_title = (EditText) findViewById(R.id.action_notification_param_title);//
 		maction_notification_param_text = (EditText) findViewById(R.id.action_notification_param_text);//
 		maction_widgettext = (ToggleButton) findViewById(R.id.action_widgettext);//
 		maction_widgettext_param_text = (EditText) findViewById(R.id.action_widgettext_param_text);//
 		maction_widgettext_param_display_date = (CheckBox) findViewById(R.id.action_widgettext_param_display_date);//
 		maction_widgettext_param_display_coordinates = (CheckBox) findViewById(R.id.action_widgettext_param_display_coordinates);//
 		maction_widgettext_param_display_address = (CheckBox) findViewById(R.id.action_widgettext_param_display_address);//
 		maction_widgettext_param_display_wifissid = (CheckBox) findViewById(R.id.action_widgettext_param_display_wifissid);//
 		maction_widgettext_param_display_action_description = (CheckBox) findViewById(R.id.action_widgettext_param_display_action_description);//
 		maction_soundprofile_param_profile_sound = (CheckBox) findViewById(R.id.action_soundprofile_param_profile_sound);//
 		maction_soundprofile_param_profile_vibrations = (CheckBox) findViewById(R.id.action_soundprofile_param_profile_vibrations);//
 		maction_soundprofile = (ToggleButton) findViewById(R.id.action_soundprofile);//
 		maction_soundprofile_param_volume = (SeekBar) findViewById(R.id.action_soundprofile_param_volume);//
 		maction_notification_param_www_switch = (CheckBox) findViewById(R.id.action_notification_param_www_switch);//
 		maction_notification_param_www_text = (EditText) findViewById(R.id.action_notification_param_www_text);//
 		// TEMPLATE: m{lowercase} = ({field}) findViewById(R.id.{lowercase});//
 	
 		populateFields();
 
 		mRowId = (savedInstanceState == null) ? null :
 			(Long) savedInstanceState.getSerializable(DiyDbAdapter.KEY_ROWID);
 		
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			mRowId = extras != null ? extras.getLong(DiyDbAdapter.KEY_ROWID)
 									: null;
 		}	
 	}
 	
 	private void populateFields() {
 		if (mRowId != null) {
 			Cursor diy = mDbHelper.fetchDiy(mRowId);
 			startManagingCursor(diy);
 			
 			
 			maction_wifi.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIFI)));//
 			maction_wifi_param_turn_on.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIFI_PARAM_TURN_ON)));//
 			maction_wifi_param_turn_off.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIFI_PARAM_TURN_OFF)));//
 			maction_wifi_param_ssid.setText(diy.getString(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIFI_PARAM_SSID)));//
 			maction_notification.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_NOTIFICATION)));//
 			maction_notification_param_title.setText(diy.getString(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_NOTIFICATION_PARAM_TITLE)));//
 			maction_notification_param_text.setText(diy.getString(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_NOTIFICATION_PARAM_TEXT)));//
 			maction_widgettext.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT)));//
 			maction_widgettext_param_text.setText(diy.getString(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_TEXT)));//
 			maction_widgettext_param_display_date.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_DISPLAY_DATE)));//
 			maction_widgettext_param_display_coordinates.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_DISPLAY_COORDINATES)));//
 			maction_widgettext_param_display_address.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_DISPLAY_ADDRESS)));//
 			maction_widgettext_param_display_wifissid.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_DISPLAY_WIFISSID)));//
 			maction_widgettext_param_display_action_description.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_WIDGETTEXT_PARAM_DISPLAY_ACTION_DESCRIPTION)));//
 			maction_soundprofile_param_profile_sound.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_SOUNDPROFILE_PARAM_PROFILE_SOUND)));//
 			maction_soundprofile_param_profile_vibrations.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_SOUNDPROFILE_PARAM_PROFILE_VIBRATIONS)));//
 			maction_soundprofile.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_SOUNDPROFILE)));//
 			maction_soundprofile_param_volume.setProgress( diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_SOUNDPROFILE_PARAM_VOLUME)));//
 			maction_notification_param_www_switch.setChecked( 1 == diy.getInt(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_NOTIFICATION_PARAM_WWW_SWITCH)));//
 			maction_notification_param_www_text.setText(diy.getString(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_ACTION_NOTIFICATION_PARAM_WWW_TEXT)));//
 			// TEMPLATE: m{lowercase}.{diyget}(diy.getColumnIndexOrThrow(DiyDbAdapter.KEY_{uppercase})));//
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		saveState();
 		outState.putSerializable(DiyDbAdapter.KEY_ROWID, mRowId);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveState();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		populateFields();
 	}
 	
 	private void saveState() {
 		mDbHelper.updateDiyActions(mRowId,
 				maction_notification_param_www_text.getText().toString(),//
 				maction_notification_param_www_switch.isChecked(),//
 				
 				// TEMPLATE: m{lowercase}.{retrieve}(),//
 				maction_wifi.isChecked(),//
 				maction_wifi_param_turn_on.isChecked(),//
 				maction_wifi_param_turn_off.isChecked(),//
 				maction_wifi_param_ssid.getText().toString(),//
 				maction_notification.isChecked(),//
 				maction_notification_param_title.getText().toString(),//
 				maction_notification_param_text.getText().toString(),//
 				maction_widgettext.isChecked(),//
 				maction_widgettext_param_text.getText().toString(),//
 				maction_widgettext_param_display_date.isChecked(),//
 				maction_widgettext_param_display_coordinates.isChecked(),//
 				maction_widgettext_param_display_address.isChecked(),//
 				maction_widgettext_param_display_wifissid.isChecked(),//
 				maction_widgettext_param_display_action_description.isChecked(),//
 				maction_soundprofile_param_profile_sound.isChecked(),//
 				maction_soundprofile_param_profile_vibrations.isChecked(),//
				maction_soundprofile.isChecked(),//
 				maction_soundprofile_param_volume.getProgress());//
 
 	}	
 
 }

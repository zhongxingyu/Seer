 /*
  * Copyright 2010-2012  Vasily Romanikhin  bac1ca89@gmail.com
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AS IS'' AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * 3. The name of the author may not be used to endorse or promote
  *    products derived from this software without specific prior written
  *    permission.
  *
  * The advertising clause requiring mention in adverts must never be included.
  */
 
 /*! ---------------------------------------------------------------
  * PROJ: OSLL/geo2tag
  * ---------------------------------------------------------------- */
 
 package org.geo2tag.tracker.preferences;
 
 import org.geo2tag.tracker.gui.RadioButtonDialog;
 import org.geo2tag.tracker.preferences.Settings.ITrackerAppSettings;
 import org.geo2tag.tracker.preferences.Settings.ITrackerNetSettings;
 import org.geo2tag.tracker.services.RequestService;
 import org.geo2tag.tracker.utils.TrackerUtil;
 
 import org.geo2tag.tracker.R;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SettingsActivity extends Activity implements ITrackerNetSettings, ITrackerAppSettings {
 	
 	private final String[] TRACKING_PERIOD_ARGS = {"1", "2", "3", "4", "5", "10", "20", "30", "40", "50", "60"};
 	private final String[] MAP_RADIUS_ARGS = {"1", "5", "10", "20", "50", "100"};
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.settings);
 	
 		setTitle(getResources().getText(R.string.settings_activity_name));
 		initializeFields();
 		initializePickButtons();
 		
 		Button btnOk = (Button) findViewById(R.id.button_ok);
 	    if (TrackerUtil.isServiceRunning(this, RequestService.class)) {
 	    	btnOk.setEnabled(false);		
 	    }else{
 	    	btnOk.setOnClickListener(new View.OnClickListener() {
 	    		@Override
 	    		public void onClick(View v) {
 	    			savePreferences();
 	    			runOnUiThread(m_saveToast);
 	    			finish();
 	    		}
 	    	});
 	    }
 		
 		Button btnCancel = (Button) findViewById(R.id.button_cancel);
 		btnCancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 	}
 	
 	private void initializeFields(){
 		initField(LOGIN, R.id.edit_login);
 		initField(PASSWORD, R.id.edit_password);
 		initField(CHANNEL, R.id.edit_channel);
 		initField(SERVER_URL, R.id.edit_server_address);
 		initCheckBox(IS_HIDE_APP, R.id.checkbox_hide);
 		initCheckBox(IS_SHOW_TICKS, R.id.checkbox_tick);
 	}
 	
 	private void initializePickButtons(){
 		
		initializePickButton(TIME_TICK, R.string.tick_interval_name, R.id.button_settings_tick, TRACKING_PERIOD_ARGS );
		initializePickButton(RADIUS, R.string.map_radius_name, R.id.button_settings_radius, MAP_RADIUS_ARGS );
 	}
 	
 	private void initializePickButton(String prefKey, int dialogTitleId, 
			int buttonId, final String[] args){
 		
		final int value = new Settings(this).getPreferences().getInt(/*TIME_TICK*/prefKey, 0);
 
 		final String dialogTitle = getResources().getText(dialogTitleId).toString();
 		
 		final Button btn = (Button) findViewById(buttonId);
 		btn.setText(Integer.toString(value));
 		btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new RadioButtonDialog(SettingsActivity.this, dialogTitle, args,
 						getIdx(value, args), btn).show();
 			}
 		});
 
 	}
 	
 	private void savePreferences(){
 		Editor prefEditor = new Settings(this).getPreferencesEditor();
 		saveField(LOGIN, R.id.edit_login, prefEditor);
 		saveField(PASSWORD, R.id.edit_password, prefEditor);
 		saveField(CHANNEL, R.id.edit_channel, prefEditor);
 		saveField(SERVER_URL, R.id.edit_server_address, prefEditor);
 		
 		saveIntField(TIME_TICK, R.id.button_settings_tick, prefEditor);
 		saveIntField(RADIUS, R.id.button_settings_radius, prefEditor);
 		//prefEditor.putInt(TIME_TICK, m_timeTick);
 		
 		saveCheckBox(IS_HIDE_APP, R.id.checkbox_hide, prefEditor);
 		saveCheckBox(IS_SHOW_TICKS, R.id.checkbox_tick, prefEditor);
 		
 		prefEditor.commit();
 	}
 	
 	private Runnable m_saveToast = new Runnable() {
 		@Override
 		public void run() {
 			Toast.makeText(SettingsActivity.this, R.string.msg_settigns_saved, Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	private void initField(String key, int idField){
 		SharedPreferences settings = new Settings(this).getPreferences();
 		String str = settings.getString(key, "?????");
 		((EditText) findViewById(idField)).setText(str);
 	}
 
 	private void initCheckBox(String key, int id){
 		SharedPreferences settings = new Settings(this).getPreferences();
 		((CheckBox) findViewById(id)).setChecked(settings.getBoolean(key, false));
 	}
 
 	private void saveField(String key, int idField, Editor prefEditor){
 		String str = ((EditText) findViewById(idField)).getText().toString().trim();
 		prefEditor.putString(key, str);
 	}
 
 	private void saveIntField(String key, int idField, Editor prefEditor){
 		String str = ((Button) findViewById(idField)).getText().toString();
 		int value = Integer.parseInt(str);
 		
 		prefEditor.putInt(key, value);
 	}
 	
 	private void saveCheckBox(String key, int id, Editor prefEditor){
 		boolean status = ((CheckBox) findViewById(id)).isChecked();
 		prefEditor.putBoolean(key, status);
 	}
 
 	
 	private int getIdx(int val, String[] args){
 		for (int i = 0; i < args.length; i++){
 			if (args[i].equals(Integer.toString(val)))
 				return i;
 		}
 		return 0;
 	}
 	
 	
 //	class TimeTickDialog extends RadioButtonDialog{
 //		public TimeTickDialog(Context context, int selectedItem) {
 //			super(context, "Tick interval", trackingPeriodArgs, selectedItem);
 //		}
 //			
 //	}
 }

 package com.hannah.arduinomotiondetector.activity;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 import com.hannah.arduinomotiondetector.NotificationPreferences;
 import com.hannah.arduinomotiondetector.R;
 import com.hannah.arduinomotiondetector.WebSender;
 
 public class SettingsActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.settings);
 
 		final EditText nameText = (EditText) findViewById(R.id.sensor_name_edit_text);
 		final EditText emailText = (EditText) findViewById(R.id.email_edit_text);
 		final EditText phoneText = (EditText) findViewById(R.id.phone_number_edit_text);
 		final EditText ipText = (EditText) findViewById(R.id.ip_edit_text);
 		final CheckBox emailOn = (CheckBox) findViewById(R.id.email_on);
 		final CheckBox photoOn = (CheckBox) findViewById(R.id.photo_on);
 		final CheckBox smsOn = (CheckBox) findViewById(R.id.sms_on);
 		final CheckBox alarmOn = (CheckBox) findViewById(R.id.alarm_on);
 
 		if (NotificationPreferences.hasPrefences(this)) {
 			nameText.setText(NotificationPreferences.getSensorName(this));
 			emailText.setText(NotificationPreferences.getEmail(this));
 			phoneText.setText(NotificationPreferences.getPhone(this));
 			ipText.setText(NotificationPreferences.getIP(this));
 			emailOn.setChecked(NotificationPreferences.getEmailOn(this));
 			photoOn.setChecked(NotificationPreferences.getPhotoOn(this));
 			smsOn.setChecked(NotificationPreferences.getSMSOn(this));
 			alarmOn.setChecked(NotificationPreferences.getAlarmOn(this));
 		}
 
 		Button saveButton = (Button) findViewById(R.id.save_button);
 		saveButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				String name = nameText.getText().toString();
 				String email = emailText.getText().toString();
 				String phone = phoneText.getText().toString();
 				String ip = ipText.getText().toString();
 				NotificationPreferences.saveSensorName(SettingsActivity.this, name);
 				NotificationPreferences.saveEmail(SettingsActivity.this, email);
 				NotificationPreferences.savePhone(SettingsActivity.this, phone);
 				NotificationPreferences.saveIP(SettingsActivity.this, ip);
 				NotificationPreferences.saveAlertsOn(SettingsActivity.this, emailOn.isChecked(), photoOn.isChecked(), smsOn.isChecked(), alarmOn.isChecked());
 
				//new WebSender(ip).execute(WebSender.getSettingsXMLMessage(SettingsActivity.this, name, email, phone));
				new WebSender(ip).execute(WebSender.getAlertXMLMessage(SettingsActivity.this));
 
 				SettingsActivity.this.finish();
 			}
 		});
 	}
 }

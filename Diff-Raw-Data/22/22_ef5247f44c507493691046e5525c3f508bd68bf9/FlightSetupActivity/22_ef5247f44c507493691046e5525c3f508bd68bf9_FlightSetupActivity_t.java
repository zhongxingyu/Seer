 package com.runninghusky.spacetracker;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.markupartist.android.widget.ActionBar;
 
 public class FlightSetupActivity extends Activity implements Runnable {
 	private long flightId;
 	private Boolean isNew, doneLoading;
 	private Button btnSave;
 	private CheckBox cbSendPicture, cbSendSms;
 	private NumberPicker hourPicker, minutePicker, secondPicker;
 	private TextView updateInterval, picUpdateInterval;
 	private AutoCompleteTextView textView;
 	private EditText flightName;
 	private int intHour, intMinute, intSecond;
 	private DataHelper dh;
 	private Flight f = new Flight();
 	private String[] numbers = { "unknown" };
 	private Context ctx;
 	private ActionBar actionBar;
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			if (doneLoading) {
 				HlprUtil.toast("Changes canceled", this, true);
 				goBackIntent();
 			}else{
				HlprUtil.toast("Please wait untill loading is complete", this, true);
 			}
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newflight);
 		doneLoading = false;
 		ctx = this;
 		Bundle b = getIntent().getExtras();
 		flightId = b.getLong("flightId", 0);
 		isNew = b.getBoolean("isNew", false);
 
 		actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar.setTitle((isNew)?"Add Flight":"Edit Flight");
 		autoCompleteStuff();
 		doMoreStuff();
 		Thread thread = new Thread(this);
 		thread.start();
 
 	}
 
 	private void doMoreStuff() {
 		updateInterval = (TextView) findViewById(R.id.TextViewUpdateInterval);
 		picUpdateInterval = (TextView) findViewById(R.id.TextViewPicUpdateInterval);
 		flightName = (EditText) findViewById(R.id.EditTextFlightName);
 
 		cbSendSms = (CheckBox) findViewById(R.id.CheckBoxSendSMS);
 		btnSave = (Button) findViewById(R.id.ButtonSaveFlight);
 		cbSendPicture = (CheckBox) findViewById(R.id.CheckBoxTakePictures);
 
 		btnSave.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				saveFlight();
 			}
 		});
 
 		btnSave.setEnabled(false);
 
 		updateInterval.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showTimeDialog("sms", v);
 			}
 		});
 
 		picUpdateInterval.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showTimeDialog("pic", v);
 			}
 		});
 
 		if (!isNew) {
 			this.dh = new DataHelper(this);
 			f = this.dh.selectFlightHistoryById(flightId);
 			this.dh.close();
 			updateInterval.setText(HlprUtil.convertSecondsToTime(Double
 					.valueOf(f.getSmsDuration())));
 			flightName.setText(f.getName());
 			textView.setText(f.getSmsNumber(false));
 			cbSendPicture.setChecked(f.getTakePic());
 			cbSendSms.setChecked(f.getSendSms());
 			picUpdateInterval.setText(HlprUtil.convertSecondsToTime(Double
 					.valueOf(f.getPicDuration())));
 		}
 	}
 
 	private void autoCompleteStuff() {
 		textView = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextViewSMSNumber);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				R.layout.list_item, numbers);
 		textView.setAdapter(adapter);
 	}
 
 	private void saveFlight() {
 		String err = "";
 		if (String.valueOf(flightName.getText()).equals("")) {
 			err += "Flight name missing...\n";
 		}
 		if (String.valueOf(textView.getText()).equals("")
 				&& cbSendSms.isChecked()) {
 			err += "SMS Number missing...\n";
 		}
 		if (HlprUtil.convertTimeToSeconds(String.valueOf(updateInterval
 				.getText())) < 5
 				&& cbSendSms.isChecked()) {
 			err += "Sms Update Interval must be greater than 5 seconds";
 		}
 		if (HlprUtil.convertTimeToSeconds(String.valueOf(picUpdateInterval
 				.getText())) < 5
 				&& cbSendPicture.isChecked()) {
 			err += "Pic Update Interval must be greater than 55 seconds";
 		}
 		if (err.equals("")) {
 			this.dh = new DataHelper(this);
 			if (isNew) {
 				long l = this.dh.insertFlights(String.valueOf(flightName
 						.getText()), cbSendPicture.isChecked(), cbSendSms
 						.isChecked(), HlprUtil.convertTimeToSeconds(String
 						.valueOf(updateInterval.getText())), String
 						.valueOf(textView.getText()), HlprUtil
 						.convertTimeToSeconds(String.valueOf(picUpdateInterval
 								.getText())));
 			} else {
 
 				this.dh.updateFlights(String.valueOf(flightName.getText()),
 						cbSendPicture.isChecked(), cbSendSms.isChecked(),
 						HlprUtil.convertTimeToSeconds(String
 								.valueOf(updateInterval.getText())), String
 								.valueOf(textView.getText()), HlprUtil
 								.convertTimeToSeconds(String
 										.valueOf(picUpdateInterval.getText())),
 						flightId);
 			}
 			this.dh.close();
 			goBackIntent();
 		} else {
 			HlprUtil.toast(err, this, false);
 		}
 	}
 
 	private void showTimeDialog(String str, View v) {
 		final String s = str;
 		final Dialog dialog = new Dialog(FlightSetupActivity.this);
 		dialog.setContentView(R.layout.timedialog);
 		dialog.setTitle("Enter Update Interval  (hh:mm:ss)");
 		dialog.setCancelable(true);
 
 		// set up button
 		Button buttonCancel = (Button) dialog.findViewById(R.id.ButtonCancel);
 		buttonCancel.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 		Button buttonOk = (Button) dialog.findViewById(R.id.ButtonOk);
 		buttonOk.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				hourPicker = (NumberPicker) dialog
 						.findViewById(R.id.NumberPickHour);
 				minutePicker = (NumberPicker) dialog
 						.findViewById(R.id.NumberPickMinute);
 				secondPicker = (NumberPicker) dialog
 						.findViewById(R.id.NumberPickSecond);
 
 				intHour = hourPicker.getCurrent();
 				intMinute = minutePicker.getCurrent();
 				intSecond = secondPicker.getCurrent();
 				if (s.equals("pic")) {
 					picUpdateTime();
 				} else {
 					updateTime();
 				}
 				dialog.dismiss();
 			}
 		});
 		// now that the dialog is set up, it's time to show it
 		dialog.show();
 	}
 
 	private void updateTime() {
 		updateInterval.setText(HlprUtil
 				.convertSecondsToTime((intHour * 60 * 60) + (intMinute * 60)
 						+ intSecond));
 	}
 
 	private void picUpdateTime() {
 		picUpdateInterval.setText(HlprUtil
 				.convertSecondsToTime((intHour * 60 * 60) + (intMinute * 60)
 						+ intSecond));
 	}
 
 	public void goBackIntent() {
 		Intent myIntent = new Intent(FlightSetupActivity.this,
 				SpaceTrackerActivity.class);
 		FlightSetupActivity.this.startActivity(myIntent);
 		finish();
 	}
 
 	@Override
 	public void run() {
 		ArrayList<String> strList = new ArrayList<String>();
 
 		String[] contacts = new String[] {
 				ContactsContract.Contacts.DISPLAY_NAME,
 				ContactsContract.Contacts.HAS_PHONE_NUMBER,
 				ContactsContract.Contacts._ID };
 		Uri contentUri = ContactsContract.Contacts.CONTENT_URI;
 		Cursor cursor = managedQuery(contentUri, contacts, null, null, null);
 
 		String textContacts = "";
 
 		if (cursor.moveToFirst()) {
 			do {
 				ContentResolver cr = getContentResolver();
 
 				String contactId = cursor.getString(cursor
 						.getColumnIndex(ContactsContract.Contacts._ID));
 				textContacts = cursor
 						.getString(cursor
 								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 				Cursor phones = cr.query(Phone.CONTENT_URI, null,
 						Phone.CONTACT_ID + " = " + contactId, null, null);
 				while (phones.moveToNext()) {
 					String number = phones.getString(phones
 							.getColumnIndex(Phone.NUMBER));
 					try {
 						strList.add(textContacts + " " + number);
 					} catch (Exception e) {
 					}
 				}
 				phones.close();
 			} while (cursor.moveToNext());
 			if (strList != null) {
 				numbers = strList.toArray(new String[strList.size()]);
 			}
 		}
 
 		handler.sendEmptyMessage(0);
 
 	}
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			// pd.dismiss();
 			autoCompleteStuff();
 			btnSave.setEnabled(true);
 			doneLoading = true;
 			HlprUtil.toast("Loading finished...", ctx, true);
 		}
 	};
 }

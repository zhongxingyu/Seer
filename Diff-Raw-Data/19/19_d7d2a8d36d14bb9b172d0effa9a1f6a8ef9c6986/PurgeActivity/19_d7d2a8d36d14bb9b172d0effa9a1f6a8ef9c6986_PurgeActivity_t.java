 package com.yhsif.purge;
 
 import android.app.AlertDialog.Builder;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.CallLog.Calls;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PurgeActivity extends TabActivity
 	implements View.OnClickListener,
 		   DialogInterface.OnClickListener,
 		   DialogInterface.OnCancelListener,
 		   TabHost.OnTabChangeListener {
 
 	public static final String TAG = "purge";
 	public static final String PREF = "com.yhsif.purge";
 
 	public static final int DEFAULT_DAYS = 30;
 
 	public static final String KEY_DAYS = "days";
 	public static final String KEY_CALL_LOG = "call_log";
 	public static final String KEY_SMS = "sms";
 	public static final String KEY_LOCKED_SMS = "locked_sms";
 
 	public static final String KEY_AUTO_DAYS = "auto_days";
 	public static final String KEY_AUTO_CALL_LOG = "auto_call_log";
 	public static final String KEY_AUTO_SMS = "auto_sms";
 	public static final String KEY_AUTO_LOCKED_SMS = "auto_locked_sms";
 	public static final String KEY_AUTO_ENABLED = "auto_enabled";
 
 	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
 	public static final String DATE_FIELD = "date";
 	public static final String LOCKED_FIELD = "locked";
 
 	// Tab tags
 	public static final String TAB_ONCE = "once";
 	public static final String TAB_AUTO = "auto";
 	public static final String DEFAULT_TAB = TAB_ONCE;
 
 	protected String lastTab = "";
 	protected boolean autoSet = false;
 
 	@Override public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		TabHost tabHost = getTabHost();
 		TabHost.TabSpec spec;
 		tabHost.setOnTabChangedListener(this);
 
 		LayoutInflater inflater = LayoutInflater.from(this);
 		inflater.inflate(R.layout.once, getTabHost().getTabContentView());
 		inflater.inflate(R.layout.auto, getTabHost().getTabContentView());
 
 		spec = tabHost.newTabSpec(TAB_ONCE)
 			.setIndicator(getString(R.string.tab_once))
 			.setContent(R.id.layout_once);
 		tabHost.addTab(spec);
 
 		spec = tabHost.newTabSpec(TAB_AUTO)
 			.setIndicator(getString(R.string.tab_auto))
 			.setContent(R.id.layout_auto);
 		tabHost.addTab(spec);
 
 		tabHost.setCurrentTabByTag(DEFAULT_TAB);
 
 		SharedPreferences settings = getSharedPreferences(PREF, 0);
 
 		((EditText)findViewById(R.id.days))
 			.setText(Integer.toString(settings.getInt(KEY_DAYS, DEFAULT_DAYS)));
 		((CheckBox)findViewById(R.id.call_log))
 			.setChecked(settings.getBoolean(KEY_CALL_LOG, true));
 		((CheckBox)findViewById(R.id.sms))
 			.setChecked(settings.getBoolean(KEY_SMS, true));
 		((CheckBox)findViewById(R.id.locked_sms))
 			.setEnabled(settings.getBoolean(KEY_SMS, true));
 		((CheckBox)findViewById(R.id.locked_sms))
 			.setChecked(settings.getBoolean(KEY_LOCKED_SMS, false));
 
 		((EditText)findViewById(R.id.auto_days))
 			.setText(Integer.toString(settings.getInt(KEY_AUTO_DAYS, DEFAULT_DAYS)));
 		((CheckBox)findViewById(R.id.auto_call_log))
 			.setChecked(settings.getBoolean(KEY_AUTO_CALL_LOG, true));
 		((CheckBox)findViewById(R.id.auto_sms))
 			.setChecked(settings.getBoolean(KEY_AUTO_SMS, true));
 		((CheckBox)findViewById(R.id.auto_locked_sms))
 			.setEnabled(settings.getBoolean(KEY_AUTO_SMS, true));
 		((CheckBox)findViewById(R.id.auto_locked_sms))
 			.setChecked(settings.getBoolean(KEY_AUTO_LOCKED_SMS, false));
 		autoSet = settings.getBoolean(KEY_AUTO_ENABLED, false);
 		setStatusText();
 
 		((Button)findViewById(R.id.do_it)).setOnClickListener(this);
 		((Button)findViewById(R.id.close)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.sms)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.locked_sms)).setOnClickListener(this);
 
 		((Button)findViewById(R.id.set_auto)).setOnClickListener(this);
 		((Button)findViewById(R.id.unset_auto)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.auto_sms)).setOnClickListener(this);
 		((CheckBox)findViewById(R.id.auto_locked_sms)).setOnClickListener(this);
 	}
 
 	@Override public void onPause() {
 		super.onPause();
 
 		SharedPreferences settings = getSharedPreferences(PREF, 0);
 		SharedPreferences.Editor editor = settings.edit();
 
 		editor.putInt(KEY_DAYS, getDays(R.id.days));
 		editor.putBoolean(KEY_CALL_LOG, isChecked(R.id.call_log));
 		editor.putBoolean(KEY_SMS, isChecked(R.id.sms));
 		editor.putBoolean(KEY_LOCKED_SMS, isChecked(R.id.locked_sms));
 
 		editor.putInt(KEY_AUTO_DAYS, getDays(R.id.auto_days));
 		editor.putBoolean(KEY_AUTO_CALL_LOG, isChecked(R.id.auto_call_log));
 		editor.putBoolean(KEY_AUTO_SMS, isChecked(R.id.auto_sms));
 		editor.putBoolean(KEY_AUTO_LOCKED_SMS, isChecked(R.id.auto_locked_sms));
 		editor.putBoolean(KEY_AUTO_ENABLED, autoSet);
 
 		editor.commit();
 	}
 
 	protected int getDays(int id) {
 		try {
 			return Integer.parseInt(((EditText)findViewById(id)).getText().toString());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 
 	protected boolean isChecked(int id) {
 		return ((CheckBox)findViewById(id)).isChecked();
 	}
 
 	protected void setStatusText() {
 		String status = autoSet ?
 			getString(R.string.auto_status_set) :
 			getString(R.string.auto_status_unset);
 		((TextView)findViewById(R.id.auto_status))
 			.setText(String.format(getString(R.string.auto_status, status)));
 		if (autoSet)
 			AutoPurge.registerAlarm(this);
 		else
 			AutoPurge.unregisterAlarm(this);
 	}
 
 	/** 
 	 * Do the purge work.
 	 * 
 	 * @param context the context
 	 * @param days days to purge
 	 * @param call_log whether purge call logs or not
 	 * @param sms whether purge sms or not
 	 * @param locked_sms if purge sms, whether purge locked sms or not
 	 */
 	public static void purge(Context context, int days, boolean call_log, boolean sms, boolean locked_sms) {
 		int callsDeleted = 0, smsDeleted = 0;
 
 		if (days <= 0) {
 			Log.e(TAG, String.format("purge: days is %d", days));
 			return;
 		}
 
 		Time now = new Time();
 		now.setToNow();
 		now.hour = 0;
 		now.minute = 0;
 		now.second = 0;
 		now.monthDay -= days;
 		long time = now.toMillis(true);
 		Log.d(TAG, String.format("deletion timestamp = %d", time));
 
 		if(call_log) {
 			String where = new StringBuilder(Calls.DATE)
 				.append(" < ")
 				.append(time)
 				.toString();
 			callsDeleted = context.getContentResolver().delete(Calls.CONTENT_URI, where, null);
 			Log.d(TAG, String.format("%d call logs deleted", callsDeleted));
 		}
 
 		if(sms) {
 			StringBuilder where = new StringBuilder(DATE_FIELD)
 				.append(" < ")
 				.append(time);
 			if(!locked_sms)
 				where.append(" AND ").append(LOCKED_FIELD).append(" = 0");
 			smsDeleted = context.getContentResolver().delete(SMS_CONTENT_URI, where.toString(), null);
 			Log.d(TAG, String.format("%d sms deleted", smsDeleted));
 		}
 
 		String message;
 
 		if(call_log)
 			if(sms)
 				// both
 				message = String.format(context.getString(R.string.both_deleted), callsDeleted, smsDeleted);
 			else
 				// only calls
 				message = String.format(context.getString(R.string.call_deleted), callsDeleted);
 		else
 			if(sms)
 				// only sms
 				message = String.format(context.getString(R.string.sms_deleted), smsDeleted);
 			else
 				// neither
 				message = context.getString(R.string.nothing);
 
 		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 		Log.i(TAG, message);
 	}
 
 	// for View.OnClickListener
 	public void onClick(View view) {
 		switch(view.getId()) {
 			case R.id.close:
 				this.finish();
 				return;
 			case R.id.sms:
 				((CheckBox)findViewById(R.id.locked_sms)).setEnabled(isChecked(R.id.sms));
 				return;
 			case R.id.locked_sms:
 				{
 					if(!isChecked(R.id.locked_sms)) {
 						// do nothing
 						return;
 					}
 					Builder builder = new Builder(this);
 					builder.setMessage(R.string.confirm_locked)
 						.setOnCancelListener(this)
 						.setPositiveButton(R.string.ok, this)
 						.setNegativeButton(R.string.cancel, this)
 						.show();
 				}
 				return;
 			case R.id.auto_sms:
 				((CheckBox)findViewById(R.id.auto_locked_sms)).setEnabled(isChecked(R.id.auto_sms));
 				return;
 			case R.id.auto_locked_sms:
 				{
 					if(!isChecked(R.id.auto_locked_sms)) {
 						// do nothing
 						return;
 					}
 					Builder builder = new Builder(this);
 					builder.setMessage(R.string.confirm_locked)
 						.setOnCancelListener(this)
 						.setPositiveButton(R.string.ok, this)
 						.setNegativeButton(R.string.cancel, this)
 						.show();
 				}
 				return;
 			case R.id.do_it:
 				{
 					int days = getDays(R.id.days);
 					boolean call_log = isChecked(R.id.call_log);
 					boolean sms = isChecked(R.id.sms);
 					boolean locked_sms = isChecked(R.id.locked_sms);
 					purge(this, days, call_log, sms, locked_sms);
 				}
 				return;
 			case R.id.set_auto:
 				autoSet = true;
 				setStatusText();
 				return;
 			case R.id.unset_auto:
 				autoSet = false;
 				setStatusText();
 				return;
 		}
 	}
 
 	// for DialogInterface.OnClickListener
 	public void onClick(DialogInterface dialog, int which) {
 		CheckBox check = null;
		if(TAB_ONCE.equals(lastTab))
 			check = (CheckBox)findViewById(R.id.locked_sms);
		else if(TAB_AUTO.equals(lastTab))
 			check = (CheckBox)findViewById(R.id.auto_locked_sms);
 		else
 			return;
 
 		switch (which){
 			case DialogInterface.BUTTON_POSITIVE:
 				check.setChecked(true);
 				break;
 			case DialogInterface.BUTTON_NEGATIVE:
 				check.setChecked(false);
 				break;
 		}
 	}
 
 	// for DialogInterface.OnCancelListener
 	public void onCancel(DialogInterface dialog) {
		if(TAB_ONCE.equals(lastTab))
 			((CheckBox)findViewById(R.id.locked_sms)).setChecked(false);
		else if(TAB_AUTO.equals(lastTab))
 			((CheckBox)findViewById(R.id.auto_locked_sms)).setChecked(false);
 	}
 
 	// for TabHost.OnTabChangeListener
 	public void onTabChanged(String tabId) {
 		Log.d(TAG, String.format("tab changed from %s to %s", lastTab, tabId));
 		lastTab = tabId;
 	}
 
 }

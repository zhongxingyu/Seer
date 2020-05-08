 package com.imaginea.android.sugarcrm;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.ContentResolver;
 import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 
 import com.imaginea.android.sugarcrm.provider.SugarCRMProvider;
 import com.imaginea.android.sugarcrm.util.Util;
 
 /**
  * SyncConfigActivity
  * 
  * @author chander
  * 
  */
 public class SyncConfigActivity extends Activity {
 
     private TextView mHeaderTextView;
 
     private TextView mTitleTextView;
 
     private Button mStartDateButton;
 
     private Button mEndDateButton;
 
     // cache the time
     private Time mStartTime;
 
     private Time mEndTime;
 
     public static final long THREE_MONTHS = 3 * 30 * 24 * 60 * 60 * 1000;
 
     public static final String TAG = SyncConfigActivity.class.getSimpleName();
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Setup layout
         setContentView(R.layout.sync_config);
         mHeaderTextView = (TextView) findViewById(R.id.headerText);
         mHeaderTextView.setText(R.string.syncFilters);
         // mTitleTextView = (TextView) findViewById(R.id.title);
         mStartDateButton = (Button) findViewById(R.id.start_date);
         mEndDateButton = (Button) findViewById(R.id.end_date);
 
         long time = System.currentTimeMillis();
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         long startTime = pref.getLong(Util.PREF_SYNC_START_TIME, time);
         long endTime = pref.getLong(Util.PREF_SYNC_END_TIME, time - THREE_MONTHS);
         mStartTime = new Time();
         mStartTime.set(startTime);
         mEndTime = new Time();
         mEndTime.set(endTime);
         setDate(mStartDateButton, mStartTime.normalize(true));
         setDate(mEndDateButton, mEndTime.normalize(true));
         populateWhen();
 
     }
 
     /**
      * starts sync for all the modules in the background
      * 
      * @param v
      */
     public void startSync(View v) {
         Bundle extras = new Bundle();
         // extras.putInt(key, value)
         extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
         extras.putInt(Util.SYNC_TYPE, Util.SYNC_MODULES_DATA);
         SugarCrmApp app = (SugarCrmApp) getApplication();
         final String usr = SugarCrmSettings.getUsername(SyncConfigActivity.this).toString();
         ContentResolver.requestSync(app.getAccount(usr), SugarCRMProvider.AUTHORITY, extras);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 
        long startMillis = mStartTime.toMillis(false /* use isDst */);
        long endMillis = mEndTime.toMillis(false /* use isDst */);
        Editor editor = pref.edit();
        editor.putLong(Util.PREF_SYNC_START_TIME, startMillis);
        editor.putLong(Util.PREF_SYNC_END_TIME, endMillis);
        editor.commit();
     }
 
     private void populateWhen() {
         long startMillis = mStartTime.toMillis(false /* use isDst */);
         long endMillis = mEndTime.toMillis(false /* use isDst */);
         setDate(mStartDateButton, startMillis);
         setDate(mEndDateButton, endMillis);
 
         mStartDateButton.setOnClickListener(new DateClickListener(mStartTime));
         mEndDateButton.setOnClickListener(new DateClickListener(mEndTime));
     }
 
     /**
      * DateListener
      */
     private class DateListener implements OnDateSetListener {
         View mView;
 
         public DateListener(View view) {
             mView = view;
         }
 
         public void onDateSet(DatePicker view, int year, int month, int monthDay) {
             // Cache the member variables locally to avoid inner class overhead.
             Time startTime = mStartTime;
             Time endTime = mEndTime;
 
             // Cache the start and end millis so that we limit the number
             // of calls to normalize() and toMillis(), which are fairly
             // expensive.
             long startMillis;
             long endMillis;
             if (mView == mStartDateButton) {
                 // The start date was changed.
                 int yearDuration = endTime.year - startTime.year;
                 int monthDuration = endTime.month - startTime.month;
                 int monthDayDuration = endTime.monthDay - startTime.monthDay;
 
                 startTime.year = year;
                 startTime.month = month;
                 startTime.monthDay = monthDay;
                 startMillis = startTime.normalize(true);
 
                 // Also update the end date to keep the duration constant.
                 endTime.year = year + yearDuration;
                 endTime.month = month + monthDuration;
                 endTime.monthDay = monthDay + monthDayDuration;
                 endMillis = endTime.normalize(true);
 
             } else {
                 // The end date was changed.
                 startMillis = startTime.toMillis(true);
                 endTime.year = year;
                 endTime.month = month;
                 endTime.monthDay = monthDay;
                 endMillis = endTime.normalize(true);
 
                 // Do not allow an event to have an end time before the start time.
                 if (endTime.before(startTime)) {
                     endTime.set(startTime);
                     endMillis = startMillis;
                 }
             }
 
             setDate(mStartDateButton, startMillis);
             setDate(mEndDateButton, endMillis);
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            Editor editor = pref.edit();
            editor.putLong(Util.PREF_SYNC_START_TIME, startMillis);
            editor.putLong(Util.PREF_SYNC_END_TIME, endMillis);
            editor.commit();
         }
     }
 
     /**
      * DateClickListener
      */
     private class DateClickListener implements View.OnClickListener {
         private Time mTime;
 
         public DateClickListener(Time time) {
             mTime = time;
         }
 
         public void onClick(View v) {
             new DatePickerDialog(SyncConfigActivity.this, new DateListener(v), mTime.year, mTime.month, mTime.monthDay).show();
         }
     }
 
     private void setDate(TextView view, long millis) {
         int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                                         | DateUtils.FORMAT_SHOW_WEEKDAY
                                         | DateUtils.FORMAT_ABBREV_MONTH
                                         | DateUtils.FORMAT_ABBREV_WEEKDAY;
         view.setText(DateUtils.formatDateTime(this, millis, flags));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 }

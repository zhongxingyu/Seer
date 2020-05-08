 /**
  *  OpenAlarm - an extensible alarm for Android
  *  Copyright (C) 2010 Liu Yen-Liang (Josh)
  *
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.startsmall.openalarm;
 
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Build;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.TextUtils;
 import java.util.*;
 
 public class OpenAlarmActivity extends ListActivity
                                implements ListView.OnKeyListener,
                                           View.OnClickListener {
     private static final String TAG = "OpenAlarmActivity";
 
     private static final int MENU_ITEM_ID_DELETE = 0;
 
     private static final int DIALOG_ID_ABOUT = 0;
     private static final int DIALOG_ID_REPORT_NEXT_ALARM = 1;
 
     private static final int PICK_FILTER = 0;
 
     private ListView mAlarmListView;
     private TextView mBannerTextView;
     private Button mFilterButton;
     private Animation mSlideInLeft;
     private Animation mSlideOutRight;
 
     private HashMap<String, HandlerInfo> mHandlerInfoMap;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         if (!Alarm.hasAlarms()) {
             Alarm.foreach(this, Alarms.getAlarmUri(-1), new BootService.ScheduleEnabledAlarm());
         }
 
         Notification.getInstance().set(this);
 
         Alarms.is24HourMode = Alarms.is24HourMode(this);
 
         mHandlerInfoMap = HandlerInfo.getMap(this);
 
         updateLayout();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         updateLayout();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.menu_item_add:
             addNewAlarm();
             break;
 
             // Reload database
         case R.id.menu_item_reload:
             // Set banner to application name
             mBannerTextView.setText(R.string.app_name);
             Cursor cursor = managedQuery(Alarms.getAlarmUri(-1),
                                          AlarmColumns.QUERY_COLUMNS, null, null,
                                          AlarmColumns.DEFAULT_SORT_ORDER);
             changeCursorForListView(cursor);
             break;
 
         case R.id.menu_item_report_next_alarm:
             showDialog(DIALOG_ID_REPORT_NEXT_ALARM);
             break;
 
         case R.id.menu_item_send_feedback:
             sendFeedback();
             break;
 
         case R.id.menu_item_about:
             showDialog(DIALOG_ID_ABOUT);
             break;
         }
 
         return true;
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         final int alarmId = item.getGroupId();
         switch(item.getItemId()) {
         case MENU_ITEM_ID_DELETE:
             // This dialog is not managed by Activity but I can't
             // find a way to pass alarmId to the
             // onCreateDialog().
             new AlertDialog.Builder(this)
                 .setMessage(R.string.delete_alarm_message)
                 .setTitle(R.string.confirm_alarm_deletion_title)
                 .setPositiveButton(
                     android.R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             Context context = OpenAlarmActivity.this;
                             Alarm.getInstance(context, alarmId).delete(context);
                         }
                     })
                 .setNegativeButton(android.R.string.cancel, null)
                 .create()
                 .show();
             break;
 
         default:
             break;
         }
 
         return true;
     }
 
     @Override
     public boolean onKey(View v, int keyCode, KeyEvent event) {
         switch (keyCode) {
             // AdapterView doesn't handle DPAD_CENTER and CENTER key
             // event, it propagate key event up to ListView.
         case KeyEvent.KEYCODE_ENTER:
         case KeyEvent.KEYCODE_DPAD_CENTER:
             View selectedAlarmView = mAlarmListView.getSelectedView();
             if (selectedAlarmView != null) {
                 selectedAlarmView.performClick();
             }
             return true;
         }
         return false;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dialog;
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         switch (id) {
         case DIALOG_ID_ABOUT:
             WebView helpWebView = new WebView(this);
             helpWebView.loadUrl("file:///android_asset/" +
                                 getString(R.string.about_html));
             dialog = builder.
                      setTitle(R.string.about_this_application).
                      setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dlg, int which) {
                                                dlg.dismiss();
                                            }
                                        }).
                      setView(helpWebView).
                      create();
             break;
 
         case DIALOG_ID_REPORT_NEXT_ALARM:
             dialog = builder.
                      setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dlg, int which) {
                                                dlg.dismiss();
                                            }
                                        }).
                      setMessage(" "). // two dummy lines to make
                                       // builder insert necessary
                                       // views into dialog.
                      setTitle(" ").
                      create();
             break;
 
         default:
             throw new IllegalArgumentException("illegal dialog id");
         }
         return dialog;
     }
 
     @Override
     public void onPrepareDialog(int id, Dialog dialog) {
         super.onPrepareDialog(id, dialog);
 
         if (id == DIALOG_ID_REPORT_NEXT_ALARM) {
             // Reset title and message
             Alarm nextAlarm = Notification.getInstance().set(this);
             String title = "";
             String message = getString(R.string.no_alarms_enabled);
             if (nextAlarm != null) {
                 message = getString(R.string.alarm_set_notification_content,
                                     nextAlarm.formatSchedule(this));
                 title = nextAlarm.getStringField(Alarm.FIELD_LABEL);
                 if (nextAlarm.isSnoozed(this)) {
                     title = title + " (" + getString(R.string.snoozed) + ")";
                 }
             }
             dialog.setTitle(title);
             ((AlertDialog)dialog).setMessage(message);
         }
     }
 
     public void onClick(View v) {
         int id = v.getId();
         if (id == R.id.filter) {
             startActivityForResult(
                 new Intent().setClass(this, FilterCriteriaActivity.class),
                 PICK_FILTER);
         }
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == PICK_FILTER) {
             Cursor cursor = null;
             if (resultCode == RESULT_FIRST_USER + FilterCriteriaActivity.FILTER_BY_ACTION) {
                 // Set banner to filter label
                 String label = data.getStringExtra(HandlerInfo.EXTRA_KEY_LABEL);
                 mBannerTextView.setText(label);
 
                 String handler = data.getStringExtra(HandlerInfo.EXTRA_KEY_HANDLER);
                 cursor = managedQuery(Alarms.getAlarmUri(-1),
                                       AlarmColumns.QUERY_COLUMNS,
                                      AlarmColumns.HANDLER + "=" + handler, null,
                                       AlarmColumns.DEFAULT_SORT_ORDER);
             } else if (resultCode == RESULT_FIRST_USER + FilterCriteriaActivity.FILTER_BY_REPEAT_DAYS) {
                 int repeatDays = data.getIntExtra(AlarmColumns.REPEAT_DAYS, -1);
                 int operatorId = data.getIntExtra(FilterCriteriaActivity.EXTRA_KEY_FILTER_BY_REPEAT_DAYS_OPERATOR, -1);
 
                 String filterString =
                         Alarms.RepeatWeekdays.toString(repeatDays,
                                                        getString(R.string.repeat_on_everyday),
                                                        getString(R.string.no_repeat_days));
                     String where;
                     if (operatorId == R.id.and) {
                         where =  AlarmColumns.REPEAT_DAYS + " & " + repeatDays + " = " + repeatDays;
                         filterString = filterString.replace(" ", " & ");
                     } else if (operatorId == R.id.or) {
                         where =  AlarmColumns.REPEAT_DAYS + " & " + repeatDays + " != 0";
                         filterString = filterString.replace(" ", " | ");
                     } else {
                         throw new IllegalArgumentException("no operator id for filter");
                     }
 
                     mBannerTextView.setText(filterString);
 
                     cursor = managedQuery(Alarms.getAlarmUri(-1),
                                           AlarmColumns.QUERY_COLUMNS,
                                           where,
                                           null,
                                           AlarmColumns.DEFAULT_SORT_ORDER);
             }
 
             changeCursorForListView(cursor);
         }
     }
 
     private void changeCursorForListView(Cursor cursor) {
         if (cursor != null) {
             CursorAdapter adapter = (CursorAdapter)mAlarmListView.getAdapter();
             if (adapter != null) {
                 Cursor oldCursor = adapter.getCursor();
                 stopManagingCursor(oldCursor);
                 adapter.changeCursor(cursor);
             }
         }
     }
 
     private void updateLayout() {
         setContentView(R.layout.openalarm_activity);
 
         mBannerTextView = (TextView)findViewById(R.id.banner);
 
         mSlideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
         mSlideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
 
         // Initialize ListView for alarm.
         mAlarmListView = getListView();
         mAlarmListView.setOnKeyListener(this);
         CursorAdapter alarmAdapter = (CursorAdapter)mAlarmListView.getAdapter();
         if (alarmAdapter == null) {
             Cursor cursor = managedQuery(Alarms.getAlarmUri(-1),
                                          AlarmColumns.QUERY_COLUMNS, null, null,
                                          AlarmColumns.DEFAULT_SORT_ORDER);
             mAlarmListView.setAdapter(new AlarmAdapter(this, cursor));
         }
 
         mFilterButton = (Button)findViewById(R.id.filter);
         mFilterButton.setOnClickListener(this);
     }
 
     private void sendFeedback() {
         Intent i = new Intent(Intent.ACTION_SENDTO);
         i.setData(Uri.parse("mailto:yenliangl@gmail.com"));
         i.putExtra(Intent.EXTRA_SUBJECT, "[OpenAlarm] ");
 
         String deviceInfo =
             String.format("Build.DISPLAY=%s\nBuild.FINGERPRINT=%s\nBuild.HOST=%s\nBuild.ID=%s\nBuild.MODEL=%s\nBuild.PRODUCT=%s\nBuild.TYPE=%s\nBuild.USER=%s\n",
                           Build.DISPLAY, Build.FINGERPRINT, Build.HOST, Build.ID, Build.MODEL, Build.PRODUCT, Build.TYPE, Build.USER);
 
         i.putExtra(Intent.EXTRA_TEXT,
                    getString(R.string.feedback_mail_content, deviceInfo));
         startActivity(
             Intent.createChooser(
                 i, getString(R.string.menu_item_send_feedback)));
     }
 
     private void addNewAlarm() {
         Alarm alarm = Alarm.getInstance(this);
         editAlarm(alarm.getIntField(Alarm.FIELD_ID));
     }
 
     private void editAlarm(int alarmId) {
         Intent intent = new Intent(this, AlarmSettingsActivity.class);
         intent.putExtra(AlarmColumns._ID, alarmId);
         startActivity(intent);
     }
 
     class AlarmAdapter extends CursorAdapter {
         private View.OnClickListener mOnClickListener;
         private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
         private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
         private Context mContext;
 
         public AlarmAdapter(Context context, Cursor c) {
             super(context, c);
 
             mContext = context;
             mOnClickListener =
                 new View.OnClickListener() {
                     // @Override
                     public void onClick(View view) {
                         DataHolder attachment = (DataHolder)view.getTag();
                         editAlarm(attachment.alarmId);
                     }
                 };
 
             mOnCreateContextMenuListener =
                 new View.OnCreateContextMenuListener() {
                     public void onCreateContextMenu(ContextMenu menu,
                                                     View view,
                                                     ContextMenu.ContextMenuInfo menuInfo) {
                         DataHolder attachment = (DataHolder)view.getTag();
                         final int alarmId = attachment.alarmId;
                         String label = Alarm.getInstance(mContext, alarmId).getStringField(Alarm.FIELD_LABEL);
 
                         menu.setHeaderTitle(label);
                         menu.add(alarmId, MENU_ITEM_ID_DELETE, 0, R.string.menu_item_delete_alarm);
                     }
                 };
 
             mOnCheckedChangeListener =
                 new CompoundButton.OnCheckedChangeListener() {
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         View parent = (View)buttonView.getParent();
                         DataHolder attachment = (DataHolder)parent.getTag();
                         final int alarmId = attachment.alarmId;
                         Alarm alarm = Alarm.getInstance(mContext, alarmId);
                         if (alarm.isValid()) {
                             // Alarm looks good. Enable it or disable it.
                             alarm.update(
                                 mContext,
                                 isChecked,
                                 alarm.getStringField(Alarm.FIELD_LABEL),
                                 alarm.getIntField(Alarm.FIELD_HOUR_OF_DAY),
                                 alarm.getIntField(Alarm.FIELD_MINUTES),
                                 alarm.getIntField(Alarm.FIELD_REPEAT_DAYS),
                                 alarm.getStringField(Alarm.FIELD_HANDLER),
                                 alarm.getStringField(Alarm.FIELD_EXTRA));
 
                             if (isChecked) {
                                 // If alarm is valid and
                                 // enabling, make a toast to tell
                                 // use when it will be triggered.
                                 Toast.makeText(
                                     mContext,
                                     mContext.getString(R.string.alarm_set_notification_content,
                                                        alarm.formatSchedule(mContext)),
                                     Toast.LENGTH_LONG).show();
                             }
                         } else {
                             // Alarm can't be set because its
                             // settings are't good enough. Bring
                             // up its Settings activity
                             // automatically for user to chage.
                             if (isChecked) {
                                 int errCode = alarm.getErrorCode();
                                 int errMsgResId = R.string.alarm_handler_unset_message;
                                 if (errCode == Alarm.ERROR_NO_REPEAT_DAYS) {
                                     errMsgResId = R.string.alarm_repeat_days_unset_message;
                                 }
                                 Toast.makeText(mContext, errMsgResId, Toast.LENGTH_LONG).show();
                                 parent.performClick();
                                 buttonView.setChecked(false);
                             }
                         }
                     }
                 };
         }
 
         public void bindView(View view, Context context, Cursor cursor) {
             Alarm alarm = Alarm.getInstance(cursor);
 
             DataHolder attachment = (DataHolder)view.getTag();
             attachment.alarmId = alarm.getIntField(Alarm.FIELD_ID);
 
             // Label
             attachment.labelView.setText(alarm.getStringField(Alarm.FIELD_LABEL));
 
             // Time of an alarm: hourOfDay and minutes
             attachment.timeAmPmView.setTime(
                 alarm.getIntField(Alarm.FIELD_HOUR_OF_DAY),
                 alarm.getIntField(Alarm.FIELD_MINUTES));
 
             // Enabled?
             final CheckBox enabledCheckBox = attachment.enabledView;
             // Set checkbox's listener to null. If set, the
             // defined listener will try to update the database
             // and make bindView() called which enters an
             // infinite loop.
             enabledCheckBox.setOnCheckedChangeListener(null);
             enabledCheckBox.setChecked(alarm.getBooleanField(Alarm.FIELD_ENABLED));
             enabledCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
 
             // Action
             final TextView actionTextView = attachment.actionView;
             String handler = alarm.getStringField(Alarm.FIELD_HANDLER);
             if (!TextUtils.isEmpty(handler)) {
                 HandlerInfo handlerInfo = mHandlerInfoMap.get(handler);
                 actionTextView.setVisibility(View.VISIBLE);
                 actionTextView.setText(handlerInfo.label);
             } else {
                 actionTextView.setVisibility(View.GONE);
                 actionTextView.setText("");
             }
 
             // RepeatDays
             final LinearLayout repeatDaysView = attachment.repeatDaysView;
             repeatDaysView.removeAllViews();
             Iterator<String> days =
                 Alarms.RepeatWeekdays.toStringList(
                     alarm.getIntField(Alarm.FIELD_REPEAT_DAYS),
                     context.getString(R.string.repeat_on_everyday),
                     context.getString(R.string.no_repeat_days)).iterator();
 
             LinearLayout.LayoutParams params =
                 new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT);
             params.setMargins(1, 1, 1, 1);
 
             while(days.hasNext()) {
                 TextView dayLabel = new TextView(context);
                 dayLabel.setClickable(false);
                 dayLabel.setBackgroundResource(R.drawable.rounded_background);
                 dayLabel.setTextAppearance(context, R.style.RepeatDaysTextAppearance);
                 dayLabel.setText(days.next());
                 repeatDaysView.addView(dayLabel, params);
             }
             repeatDaysView.setVisibility(View.VISIBLE);
 
             // Check if this alarm is ok?
             final TextView indicatorView = attachment.indicatorView;
             if (alarm.isValid()) {
                 indicatorView.setVisibility(View.GONE);
             } else {
                 indicatorView.setVisibility(View.VISIBLE);
             }
         }
 
         public View newView(Context context, Cursor c, ViewGroup parent) {
             View view = LayoutInflater.from(context).inflate(R.layout.alarm_list_item, parent, false);
 
             DataHolder attachment = new DataHolder();
             attachment.labelView = (TextView)view.findViewById(R.id.label);
             attachment.timeAmPmView = (TimeAmPmView)view.findViewById(R.id.time_am_pm);
             attachment.actionView = (TextView)view.findViewById(R.id.action);
             attachment.enabledView = (CheckBox)view.findViewById(R.id.enabled);
             attachment.repeatDaysView = (LinearLayout)view.findViewById(R.id.repeat_days);
             attachment.indicatorView = (TextView)view.findViewById(R.id.indicator);
             view.setTag(attachment);
 
             view.setOnClickListener(mOnClickListener);
 
             // The ContextMenu.ContextMenuInfo object is returned
             // by getContextMenuInfo() overriden by
             // clients. Here, it is null.
             view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
 
             return view;
         }
 
         protected void onContentChanged() {
             super.onContentChanged();
             Notification.getInstance().set(mContext);
         }
 
         public void notifyDataSetChanged() { // requery
             super.notifyDataSetChanged();
             Notification.getInstance().set(mContext);
         }
 
         public void notifyDataSetInvalidated() { // deactivate or close/onStop or onDestroy
             super.notifyDataSetInvalidated();
             Notification.getInstance().set(mContext);
         }
     }
 
     static class DataHolder {
         int alarmId;
         TextView labelView;
         TextView actionView;
         TimeAmPmView timeAmPmView;
         CheckBox enabledView;
         LinearLayout repeatDaysView;
         TextView indicatorView;
     }
 }

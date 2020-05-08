 /**
  * @file   AlarmClockPlus.java
  * @author josh <yenliangl at gmail dot com>
  * @date   Fri Oct  9 16:57:28 2009
  *
  * @brief
  *
  *
  */
 package org.startsmall.alarmclockplus;
 
 import org.startsmall.alarmclockplus.preference.*;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.AdapterView;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.CheckBox;
 import android.text.TextUtils;
 
 import java.util.*;
 
 public class AlarmClockPlus extends ListActivity {
     private static final String TAG = "AlarmClockPlus";
     private static final int MENU_ITEM_DELETE_ID = 0;
 
     private class AlarmAdapter extends CursorAdapter {
         private LayoutInflater mInflater;
         private Alarms.RepeatWeekdays mRepeatOn;
         private View.OnClickListener mOnClickListener;
         private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
         private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
 
         public AlarmAdapter(Context context, Cursor c) {
             super(context, c);
             mInflater = AlarmClockPlus.this.getLayoutInflater();
             mRepeatOn = Alarms.RepeatWeekdays.getInstance();
             mOnClickListener =
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Bundle data = (Bundle)view.getTag();
 
                         int alarmId = data.getInt(Alarms.AlarmColumns._ID);
                         editAlarmSettings(alarmId);
                     }
                 };
             mOnCreateContextMenuListener =
                 new View.OnCreateContextMenuListener() {
                     public void onCreateContextMenu(ContextMenu menu,
                                                     View v,
                                                     ContextMenu.ContextMenuInfo menuInfo) {
                         Bundle data = (Bundle)v.getTag();
 
                         String label = data.getString(Alarms.AlarmColumns.LABEL);
                         int alarmId = data.getInt(Alarms.AlarmColumns._ID);
 
                         menu.setHeaderTitle(label);
                         menu.add(alarmId, MENU_ITEM_DELETE_ID, 0, R.string.menu_item_delete_alarm);
                     }
                 };
             mOnCheckedChangeListener =
                 new CompoundButton.OnCheckedChangeListener() {
                     public void onCheckedChanged(CompoundButton buttonView,
                                                  boolean isChecked) {
                         View parent = (View)buttonView.getParent();
                         Bundle data = (Bundle)parent.getTag();
                         int alarmId = data.getInt(Alarms.AlarmColumns._ID);
 
                         Log.d(TAG, "================> " + alarmId + " Run onCheckedChanged(" + isChecked + ", " + alarmId + ");");
                        // Alarms.setAlarm(getContext(), alarmId, isChecked);

                        // Alarms.setAlarm(AlarmClockPlus.this, alarmId, isChecked);


                        // FIXME: Cursor is listenning the
                        // changes of Content via
                        // c.setNotificationUril(...). If
                        // Alarms.setAlarm is called,
                        // updateAlarm() updates the content, the
                        // cursor is notified and then cursor
                        // adapter is notified to refresh views
                        // (bindView() is called).

                     }
                 };
 
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             Log.d(TAG, "=====> bindView() : " + view);
 
             final int id = cursor.getInt(Alarms.AlarmColumns.PROJECTION_ID_INDEX);
             final String label = cursor.getString(Alarms.AlarmColumns.PROJECTION_LABEL_INDEX);
             final int hourOfDay = cursor.getInt(Alarms.AlarmColumns.PROJECTION_HOUR_INDEX);
             final int minutes = cursor.getInt(Alarms.AlarmColumns.PROJECTION_MINUTES_INDEX);
             final int daysCode = cursor.getInt(Alarms.AlarmColumns.PROJECTION_REPEAT_DAYS_INDEX);
             final boolean enabled = cursor.getInt(Alarms.AlarmColumns.PROJECTION_ENABLED_INDEX) == 1;
 
             Bundle viewBundle = new Bundle();
             viewBundle.putInt(Alarms.AlarmColumns._ID, id);
             viewBundle.putString(Alarms.AlarmColumns.LABEL, label);
             view.setTag(viewBundle);
 
             // Time
             TextView timeView = (TextView)view.findViewById(R.id.time);
             Calendar calendar = Calendar.getInstance();
             calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
             calendar.set(Calendar.MINUTE, minutes);
             Alarms.formatDate("HH:mm", calendar);
             timeView.setText(Alarms.formatDate("HH:mm", calendar));
 
             // Label
             final TextView labelView =
                 (TextView)view.findViewById(R.id.label);
             labelView.setText(label);
 
             // Enable this alarm?
             final CheckBox enabledCheckBox =
                 (CheckBox)view.findViewById(R.id.enabled);
             enabledCheckBox.setChecked(enabled);
 
             // Repeat days
             final TextView repeatDays =
                 (TextView)view.findViewById(R.id.repeat_days);
             mRepeatOn.setCode(daysCode);
             repeatDays.setText(mRepeatOn.toString());
             if(daysCode == 0) {
                 repeatDays.setVisibility(View.GONE);
             } else {
                 repeatDays.setVisibility(View.VISIBLE);
             }
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             View view = mInflater.inflate(R.layout.alarm_list_item,
                                           parent,
                                           false);
 
             Log.d(TAG, "=====> newView() " + view);
 
             /**
              * Should follow Android's design. Click to go to
              * item's default activity and long-click to go to
              * item's extended activities. Here, the first-class
              * activity is to edit item's settings.
              */
             view.setOnClickListener(mOnClickListener);
 
             /**
              * The context menu listener of the view must be set
              * in order for its parent's onCreateMenu() to be
              * called to create context menu. This should be a
              * bug but I'm not very sure.
              *
              * The ContextMenu.ContextMenuInfo object is returned
              * by getContextMenuInfo() overriden by
              * clients. Here, it is null.
              *
              */
             view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
 
             // Enable this alarm?
             final CheckBox enabledCheckBox =
                 (CheckBox)view.findViewById(R.id.enabled);
             enabledCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
             return view;
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Cursor alarmsCursor = Alarms.getAlarmCursor(this, -1);
         // FIXME: Don't know why enabling this line will prevent
         // deleted row from removing from ListView. Need to
         // figure out.
 
         //-> startManagingCursor(cursor); <-
         setListAdapter(new AlarmAdapter(this, alarmsCursor));
 
         // registerForContextMenu(getListView());
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
             addAlarm();
             break;
 
         case R.id.menu_item_preferences:
             editAppPreferences();
             break;
 
         case R.id.menu_item_help:
             showHelpDialog();
             break;
         }
 
         return true;
     }
 
     public boolean onContextItemSelected(MenuItem item) {
         final int alarmId = item.getGroupId();
         switch(item.getItemId()) {
         case MENU_ITEM_DELETE_ID:
             new AlertDialog.Builder(this)
                 .setMessage(R.string.delete_alarm_message)
                 .setTitle(R.string.confirm_alarm_deletion_title)
                 .setPositiveButton(
                     R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             AlarmClockPlus.this.deleteAlarm(alarmId);
                         }
                     })
                 .setNegativeButton(R.string.cancel, null)
                 .create()
                 .show();
             break;
 
         default:
             break;
         }
 
         return true;
     }
 
     private void editAppPreferences() {
         Log.d(TAG, "TODO: Editing application preferences");
     }
 
     private void showHelpDialog() {
         Log.d(TAG, "TODO: Show help dialog and copyright");
     }
 
     private void addAlarm() {
         Uri uri = Alarms.newAlarm(this);
         int alarmId = Integer.parseInt(uri.getLastPathSegment());
         editAlarmSettings(alarmId);
     }
 
     private void editAlarmSettings(int alarmId) {
         Log.d(TAG, "Edit " + Alarms.getAlarmUri(alarmId));
         Intent intent = new Intent(this, AlarmSettings.class);
         intent.putExtra(Alarms.INTENT_EXTRA_ALARM_ID_KEY, alarmId);
         startActivity(intent);
     }
 
     private int deleteAlarm(int alarmId) {
         return Alarms.deleteAlarm(this, alarmId);
     }
 }

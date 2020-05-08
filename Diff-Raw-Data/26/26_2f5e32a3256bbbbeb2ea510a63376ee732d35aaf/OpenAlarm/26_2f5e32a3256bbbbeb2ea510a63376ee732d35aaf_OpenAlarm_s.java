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
 
 /**
  * @file   OpenAlarm.java
  * @author josh <yenliangl at gmail dot com>
  * @date   Fri Oct  9 16:57:28 2009
  *
  * @brief
  *
  *
  */
 package org.startsmall.openalarm;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.TextUtils;
 
 import java.util.*;
 
 public class OpenAlarm extends ListActivity {
     private static final String TAG = "OpenAlarm";
     private static final int MENU_ITEM_DELETE_ID = 0;
     private Cursor mAlarmsCursor;
 
     private class AlarmAdapter extends CursorAdapter {
         private LayoutInflater mInflater;
         private View.OnClickListener mOnClickListener;
         private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
         private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
 
         public AlarmAdapter(Context context, Cursor c) {
             super(context, c);
             mInflater = OpenAlarm.this.getLayoutInflater();
             mOnClickListener =
                 new View.OnClickListener() {
                     // @Override
                     public void onClick(View view) {
                         Bundle attachment = (Bundle)view.getTag();
 
                         int alarmId = attachment.getInt(Alarms.AlarmColumns._ID);
                         editAlarmSettings(alarmId);
                     }
                 };
             mOnCreateContextMenuListener =
                 new View.OnCreateContextMenuListener() {
                     public void onCreateContextMenu(ContextMenu menu,
                                                     View view,
                                                     ContextMenu.ContextMenuInfo menuInfo) {
                         Bundle attachment = (Bundle)view.getTag();
 
                         String label = attachment.getString(Alarms.AlarmColumns.LABEL);
                         int alarmId = attachment.getInt(Alarms.AlarmColumns._ID);
 
                         menu.setHeaderTitle(label);
                         menu.add(alarmId, MENU_ITEM_DELETE_ID, 0, R.string.menu_item_delete_alarm);
                     }
                 };
 
             mOnCheckedChangeListener =
                 new CompoundButton.OnCheckedChangeListener() {
                     public void onCheckedChanged(CompoundButton buttonView,
                                                  boolean isChecked) {
                         View parent = (View)buttonView.getParent();
                         Bundle attachment = (Bundle)parent.getTag();
                         int alarmId =
                             attachment.getInt(Alarms.AlarmColumns._ID);
 
                         // If it was snoozed before,
                         // check/uncheck this button should
                         // disable this snoozed alert first.
                         Alarms.cancelSnoozedAlarm(OpenAlarm.this, alarmId);
 
                         // Enable this alarm again.
                         int errorCode =
                             Alarms.setAlarmEnabled(OpenAlarm.this,
                                                    Alarms.getAlarmUri(alarmId),
                                                    isChecked);
                         // Can't enable this alarm, unchecked
                         // this button view and brought up
                         // AlarmSettings for this alarm.
                         if (isChecked && errorCode != 0) {
                             int errorMsgResId = 0;
                             if (errorCode == Alarms.ERROR_NO_HANDLER_SET) {
                                 errorMsgResId = R.string.alarm_handler_unset_message;
                             } else if (errorCode == Alarms.ERROR_NO_DAYS_SET) {
                                 errorMsgResId = R.string.alarm_repeat_days_unset_message;
                             }
                             Toast.makeText(OpenAlarm.this, errorMsgResId, Toast.LENGTH_LONG).show();
                             parent.performClick();
                             buttonView.setChecked(false);
                         }
                     }
                 };
 
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             // Important note:
             // You have to use all columns in cursor to update this
             // view. Android seems to have an internal stack-alike
             // structure to keep the views and it follows the
             // first-in-last-out order to retrieve the view out of
             // stack. The only thing you can trust is the order of
             // positions is the same which is from 0 to the last.
 
             final int id = cursor.getInt(Alarms.AlarmColumns.PROJECTION_ID_INDEX);
             final String label = cursor.getString(Alarms.AlarmColumns.PROJECTION_LABEL_INDEX);
             final int hourOfDay = cursor.getInt(Alarms.AlarmColumns.PROJECTION_HOUR_INDEX);
             final int minutes = cursor.getInt(Alarms.AlarmColumns.PROJECTION_MINUTES_INDEX);
             final int daysCode = cursor.getInt(Alarms.AlarmColumns.PROJECTION_REPEAT_DAYS_INDEX);
             final boolean enabled = cursor.getInt(Alarms.AlarmColumns.PROJECTION_ENABLED_INDEX) == 1;
             final String handler = cursor.getString(Alarms.AlarmColumns.PROJECTION_HANDLER_INDEX);
 
             Bundle attachment = (Bundle)view.getTag();
             attachment.putInt(Alarms.AlarmColumns._ID, id);
             attachment.putString(Alarms.AlarmColumns.LABEL, label);
 
             // Time
             final CompoundTimeTextView timeTextView =
                 (CompoundTimeTextView)view.findViewById(R.id.time);
             timeTextView.setTime(hourOfDay, minutes);
 
             // Label
             final TextView labelView =
                 (TextView)view.findViewById(R.id.label);
             labelView.setText(label);
 
             // Enable this alarm?
             final CheckBox enabledCheckBox =
                 (CheckBox)view.findViewById(R.id.enabled);
 
             // Set checkbox's listener to null. If set, the
             // listener defined will try to update the database
             // and make bindView() called which enters an
             // infinite loop.
             enabledCheckBox.setOnCheckedChangeListener(null);
             enabledCheckBox.setChecked(enabled);
             enabledCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
             final LinearLayout repeatDaysView =
                 (LinearLayout)view.findViewById(R.id.repeat_days);
             repeatDaysView.removeAllViews();
             Iterator<String> days =
                 Alarms.RepeatWeekdays.toStringList(daysCode).iterator();
 
             LinearLayout.LayoutParams params =
                 new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT);
             params.setMargins(2, 2, 2, 2);
 
             while(days.hasNext()) {
                 TextView dayLabel = new TextView(context);
                 dayLabel.setClickable(false);
                 dayLabel.setBackgroundResource(R.drawable.rounded_background);
                 dayLabel.setTextAppearance(context, android.R.attr.textAppearanceSmall);
                 dayLabel.setText(days.next());
                 repeatDaysView.addView(dayLabel, params);
             }
             repeatDaysView.setVisibility(View.VISIBLE);
 
             // Loads handler's label into R.id.action
             ImageView handlerIconView = (ImageView)view.findViewById(R.id.icon);
            handlerIconView.setImageResource(R.drawable.stat_sys_warning);
             if (!TextUtils.isEmpty(handler)) {
                 PackageManager pm = context.getPackageManager();
                 try {
                     ActivityInfo info = Alarms.getHandlerInfo(pm, handler);
                     Drawable handlerIcon = info.loadIcon(pm);
                     handlerIconView.setImageDrawable(handlerIcon);
                 } catch(PackageManager.NameNotFoundException e) {
                     Log.d(TAG, e.getMessage());
                 }
             }
 
             final String extra = cursor.getString(Alarms.AlarmColumns.PROJECTION_EXTRA_INDEX);
             Log.d(TAG, "===> Bind these alarm settigs to view: id=" + id
                   + ", label=" + label
                   + ", time=" + hourOfDay + ":" + minutes
                   + ", enabled=" + enabledCheckBox.isChecked()
                   + ", repeat on=" + Alarms.RepeatWeekdays.toString(daysCode)
                   + ", handler=" + handler
                   + ", extra=" + extra);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             View view = mInflater.inflate(R.layout.alarm_list_item,
                                           parent,
                                           false);
             Bundle attachment = new Bundle();
             view.setTag(attachment);
 
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
              */
             view.setOnCreateContextMenuListener(
                 mOnCreateContextMenuListener);
 
             return view;
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Set theme from persisted settings.
         setThemeFromPreference();
 
         setContentView(R.layout.main);
 
         mAlarmsCursor =
             Alarms.getAlarmCursor(this, Alarms.getAlarmUri(-1));
 
         // startManagingCursor(cursor);
         setListAdapter(new AlarmAdapter(this, mAlarmsCursor));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public void onDestroy() {
         // Closing the cached cursor;
         mAlarmsCursor.close();
 
         super.onDestroy();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.menu_item_add:
             addAlarm();
             break;
 
         case R.id.menu_item_preferences:
             showApplicationPreferences();
             break;
 
         case R.id.menu_item_send_feedback:
             sendFeedback();
             break;
 
         case R.id.menu_item_about:
             showAboutThisAppDialog();
             break;
         }
 
         return true;
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         final int alarmId = item.getGroupId();
         switch(item.getItemId()) {
         case MENU_ITEM_DELETE_ID:
             new AlertDialog.Builder(this)
                 .setMessage(R.string.delete_alarm_message)
                 .setTitle(R.string.confirm_alarm_deletion_title)
                 .setPositiveButton(
                     android.R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             Alarms.deleteAlarm(OpenAlarm.this, alarmId);
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
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
             // ListView doesn't handle DPAD_CENTER and CENTER key
             // event, we have to handle it in Activity.
         case KeyEvent.KEYCODE_ENTER:
         case KeyEvent.KEYCODE_DPAD_CENTER:
             getListView().getSelectedView().performClick();
             return true;
         }
         return false;
     }
 
     private void showApplicationPreferences() {
         Intent intent = new Intent(this, ApplicationSettings.class);
         startActivity(intent);
     }
 
     private void showAboutThisAppDialog() {
         WebView helpWebView = new WebView(this);
         helpWebView.loadUrl("file:///android_asset/" +
                             getString(R.string.about_me));
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.
             setTitle("About this application").
             setPositiveButton(android.R.string.ok,
                               new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       dialog.dismiss();
                                   }
                               }).
             setView(helpWebView).
             create().show();
     }
 
     private void sendFeedback() {
         Intent i = new Intent(Intent.ACTION_SENDTO);
         i.setData(Uri.parse("mailto:yenliangl@gmail.com"));
         i.putExtra(Intent.EXTRA_SUBJECT, "[OpenAlarm] ");
         i.putExtra(Intent.EXTRA_TEXT, "Hi Josh!" );
         startActivity(Intent.createChooser(i, "Send Feedback"));
     }
 
     private void addAlarm() {
         Uri uri = Alarms.newAlarm(this);
         int alarmId = Integer.parseInt(uri.getLastPathSegment());
         editAlarmSettings(alarmId);
     }
 
     private void editAlarmSettings(int alarmId) {
         Intent intent = new Intent(this, AlarmSettings.class);
         intent.putExtra(Alarms.AlarmColumns._ID, alarmId);
         startActivity(intent);
     }
 
     private void setThemeFromPreference() {
         SharedPreferences sharedPreferences =
             PreferenceManager.getDefaultSharedPreferences(this);
 
         // Get theme ID.
         int themeId = Integer.parseInt(
             sharedPreferences.getString(
                 getString(R.string.application_settings_set_theme_key),
                 "1"));
         switch (themeId) {
         case 0:
             setTheme(android.R.style.Theme_Light);
             break;
         default:
             setTheme(android.R.style.Theme_Black);
             break;
         }
     }
 }

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
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
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
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.TextUtils;
 import java.util.*;
 
 public class OpenAlarm extends ListActivity {
     private static final String TAG = "OpenAlarm";
 
     private static final int MENU_ITEM_ID_DELETE = 0;
 
     private Cursor mAlarmsCursor;
 
     private class AlarmAdapter extends CursorAdapter {
         private LayoutInflater mInflater;
         private View.OnClickListener mOnClickListener;
         private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
         private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
 
         private Animation mScale;
 
         public AlarmAdapter(Context context, Cursor c) {
             super(context, c);
 
             mInflater = (LayoutInflater)context.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
             mOnClickListener =
                 new View.OnClickListener() {
                     // @Override
                     public void onClick(View view) {
                         Bundle attachment = (Bundle)view.getTag();
 
                         int alarmId = attachment.getInt(AlarmColumns._ID);
                         editAlarm(alarmId);
                     }
                 };
             mOnCreateContextMenuListener =
                 new View.OnCreateContextMenuListener() {
                     public void onCreateContextMenu(ContextMenu menu,
                                                     View view,
                                                     ContextMenu.ContextMenuInfo menuInfo) {
                         Bundle attachment = (Bundle)view.getTag();
 
                         int alarmId = attachment.getInt(AlarmColumns._ID);
                         String label = Alarm.getInstance(alarmId).getStringField(Alarm.FIELD_LABEL);
 
                         menu.setHeaderTitle(label);
                         menu.add(alarmId, MENU_ITEM_ID_DELETE, 0, R.string.menu_item_delete_alarm);
                     }
                 };
 
             mOnCheckedChangeListener =
                 new CompoundButton.OnCheckedChangeListener() {
                     public void onCheckedChanged(CompoundButton buttonView,
                                                  boolean isChecked) {
                         View parent = (View)buttonView.getParent();
                         Bundle attachment = (Bundle)parent.getTag();
                         int alarmId = attachment.getInt(AlarmColumns._ID);
                         Alarm alarm = Alarm.getInstance(alarmId);
 
                         // If this alarm is snoozed,
                         // check/uncheck this button should
                         // cancel it first because we need to
                         // reshedule its time.
                         Context context = (Context)OpenAlarm.this;
 
                         if (alarm.isValid()) {
                             // Alarm looks good. Enable it or disable it.
                             alarm.update(
                                 context,
                                 isChecked,
                                 alarm.getStringField(Alarm.FIELD_LABEL),
                                 alarm.getIntField(Alarm.FIELD_HOUR_OF_DAY),
                                 alarm.getIntField(Alarm.FIELD_MINUTES),
                                 alarm.getIntField(Alarm.FIELD_REPEAT_DAYS),
                                 alarm.getStringField(Alarm.FIELD_HANDLER),
                                 alarm.getStringField(Alarm.FIELD_EXTRA));
 
                             Log.d(TAG, "===> scheduled alarm: " +
                                   Alarms.formatDateTime(context, alarm));
 
                             if (isChecked) {
                                 String text =
                                    context.getString(R.string.alarm_set_toast_text,
                                                       alarm.getStringField(Alarm.FIELD_LABEL),
                                                       Alarms.formatSchedule(context, alarm));
                                 Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                             }
                         } else {
                             // Alarm can't be set because its
                             // settings are't good enough. Bring
                             // up its Settings activity
                             // automatically for user to chage.
                             int errCode = alarm.getErrorCode();
                             int errMsgResId = R.string.alarm_handler_unset_message;
                             if (errCode == Alarm.ERROR_NO_REPEAT_DAYS) {
                                 errMsgResId = R.string.alarm_repeat_days_unset_message;
                             }
                             Toast.makeText(context, errMsgResId, Toast.LENGTH_LONG).show();
                             parent.performClick();
                             buttonView.setChecked(false);
                         }
 
                         Alarms.setNotification(context, isChecked);
                     }
                 };
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             // note that this line is very important because it
             // obtains alarm from content and cache it in the
             // internal cache.
             Alarm alarm = Alarm.getInstance(cursor);
 
             Bundle attachment = (Bundle)view.getTag();
             attachment.putInt(AlarmColumns._ID,
                               alarm.getIntField(Alarm.FIELD_ID));
 
             // Label
             final TextView labelView =
                 (TextView)view.findViewById(R.id.label);
             labelView.setText(alarm.getStringField(Alarm.FIELD_LABEL));
 
             // Time of an alarm: hourOfDay and minutes
             final CompoundTimeTextView timeTextView =
                 (CompoundTimeTextView)view.findViewById(R.id.time);
             timeTextView.setTime(
                 alarm.getIntField(Alarm.FIELD_HOUR_OF_DAY),
                 alarm.getIntField(Alarm.FIELD_MINUTES));
 
             // Enabled?
             final CheckBox enabledCheckBox =
                 (CheckBox)view.findViewById(R.id.enabled);
             // Set checkbox's listener to null. If set, the
             // defined listener will try to update the database
             // and make bindView() called which enters an
             // infinite loop.
             enabledCheckBox.setOnCheckedChangeListener(null);
             enabledCheckBox.setChecked(alarm.getBooleanField(Alarm.FIELD_ENABLED));
             enabledCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
 
             // RepeatDays
             final LinearLayout repeatDaysView =
                 (LinearLayout)view.findViewById(R.id.repeat_days);
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
             params.setMargins(2, 2, 2, 2);
 
             while(days.hasNext()) {
                 TextView dayLabel = new TextView(context);
                 dayLabel.setClickable(false);
                 dayLabel.setBackgroundResource(R.drawable.rounded_background);
                 dayLabel.setTextAppearance(context, R.style.RepeatDaysTextAppearance);
                 dayLabel.setText(days.next());
                 repeatDaysView.addView(dayLabel, params);
             }
             repeatDaysView.setVisibility(View.VISIBLE);
 
             // Handler
             // Loads handler's icon
             ImageView handlerIconView = (ImageView)view.findViewById(R.id.icon);
             handlerIconView.setImageResource(R.drawable.null_handler);
             String handler = alarm.getStringField(Alarm.FIELD_HANDLER);
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
 
             // Log.d(TAG, "===> Bind these alarm settigs to view: id=" + id
             //       + ", label=" + label
             //       + ", time=" + hourOfDay + ":" + minutes
             //       + ", enabled=" + enabledCheckBox.isChecked()
             //       + ", repeat on='" + Alarms.RepeatWeekdays.toString(daysCode, getString(R.string.repeat_on_everyday), getString(R.string.no_repeat_days)) + "'"
             //       + ", handler=" + handler
             //       + ", extra=" + extra);
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
 
             // TODO: Cache all child views in the @p view.
 
 
 
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         mAlarmsCursor =
             Alarms.getAlarmCursor(this, Alarms.getAlarmUri(-1));
 
         // startManagingCursor(cursor);
         setListAdapter(new AlarmAdapter(this, mAlarmsCursor));
 
         getListView().setOnItemSelectedListener(
             new AdapterView.OnItemSelectedListener() {
                 public void onNothingSelected(AdapterView<?> parent) {}
                 public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                     Animation scale =
                         AnimationUtils.loadAnimation(
                             OpenAlarm.this, R.anim.scale);
                     v.startAnimation(scale);
                 }
             });
 
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
             addNewAlarm();
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
         case MENU_ITEM_ID_DELETE:
             new AlertDialog.Builder(this)
                 .setMessage(R.string.delete_alarm_message)
                 .setTitle(R.string.confirm_alarm_deletion_title)
                 .setPositiveButton(
                     android.R.string.ok,
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                             Alarm.getInstance(alarmId).delete(OpenAlarm.this);                       }
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
 
     private void showAboutThisAppDialog() {
         WebView helpWebView = new WebView(this);
         helpWebView.loadUrl("file:///android_asset/" +
                             getString(R.string.about_html));
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.
             setTitle(R.string.about_this_application).
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
 
     private void addNewAlarm() {
         Alarm alarm = Alarm.getInstance(this);
         editAlarm(alarm.getIntField(Alarm.FIELD_ID));
     }
 
     private void editAlarm(int alarmId) {
         Intent intent = new Intent(this, AlarmSettings.class);
         intent.putExtra(AlarmColumns._ID, alarmId);
         startActivity(intent);
     }
 }

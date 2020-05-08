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
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ResolveInfo;
 import android.content.pm.PackageManager;
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
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.TextUtils;
 import com.admob.android.ads.AdView;
 import java.util.*;
 
 public class OpenAlarm extends TabActivity
                        implements TabHost.OnTabChangeListener, ListView.OnKeyListener {
	// private static final String TAG = "OpenAlarm";
 
     private static final int MENU_ITEM_ID_DELETE = 0;
     private static final int DIALOG_ID_ABOUT = 0;
 
     // Build tabs by querying alarm handlers
     private static final String GROUP_DATA_KEY_LABEL = "label";
     private static final String GROUP_DATA_KEY_SORT = "sort";
     private static final String GROUP_DATA_KEY_HANDLER = "handler";
     private static final String GROUP_DATA_KEY_ICON = "icon";
 
     private ArrayList<HashMap<String, Object>> mTabData;
 
     private int mShowAdsCount = 0;
     private int mOldTabId = -1;
 
     private ListView mAlarmListView;
     private TextView mBannerTextView;
     private AdView mAdView;
     private Animation mSlideInLeft;
     private Animation mSlideOutRight;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
         if (!Alarm.hasAlarms()) {
             Alarm.foreach(this, Alarms.getAlarmUri(-1), new BootService.ScheduleEnabledAlarm());
         }
 
         Notification.getInstance().set(this);
 
         Alarms.is24HourMode = Alarms.is24HourMode(this);
 
         mBannerTextView = (TextView)findViewById(R.id.banner);
         mAlarmListView = (ListView)getTabHost().getTabContentView().findViewById(android.R.id.list);
         mAlarmListView.setOnKeyListener(this);
         mAdView = (AdView)getTabHost().findViewById(R.id.ad);
         mSlideInLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
         mSlideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
 
         initTabHost();
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
                             Context context = OpenAlarm.this;
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
 
         default:
             throw new IllegalArgumentException("illegal dialog id");
         }
         return dialog;
     }
 
     public void onTabChanged(String tabTag) {
         final int tabId = Integer.parseInt(tabTag);
         final HashMap<String, ?> map = mTabData.get(tabId);
         final String label = (String)map.get(GROUP_DATA_KEY_LABEL);
         mBannerTextView.setText(label);
 
         // Change alarm cursor for current tab.
         final CursorAdapter alarmAdapter = (CursorAdapter)mAlarmListView.getAdapter();
         alarmAdapter.changeCursor(getAlarmCursor(tabId));
 
         // Show ads
         showAdsChecked();
 
         // Animate ListView
         mAlarmListView.startAnimation(tabId > mOldTabId ? mSlideInLeft : mSlideOutRight);
         mOldTabId = tabId;
     }
 
     private void initTabHost() {
         PackageManager pm = getPackageManager();
         List<ResolveInfo> infoList = Alarms.queryAlarmHandlers(pm, true);
         mTabData = new ArrayList<HashMap<String, Object>>(infoList.size() + 1);
         Iterator<ResolveInfo> infoIter = infoList.iterator();
         while (infoIter.hasNext()) {
             ActivityInfo activityInfo = infoIter.next().activityInfo;
 
             HashMap<String, Object> map = new HashMap<String, Object>();
             String label = activityInfo.loadLabel(pm).toString();
             String className = activityInfo.name;
             Drawable icon = activityInfo.loadIcon(pm);
 
             map.put(GROUP_DATA_KEY_LABEL, label.substring(1));
             map.put(GROUP_DATA_KEY_SORT, label.charAt(0));
             map.put(GROUP_DATA_KEY_HANDLER, className);
             map.put(GROUP_DATA_KEY_ICON, icon);
 
             mTabData.add(map);
         }
 
         // Null handler group
         HashMap<String, Object> nullHandlerMap = new HashMap<String, Object>();
         nullHandlerMap.put(GROUP_DATA_KEY_LABEL, getString(R.string.uncategorized));
         nullHandlerMap.put(GROUP_DATA_KEY_HANDLER, "");
         nullHandlerMap.put(GROUP_DATA_KEY_ICON,
                            getResources().getDrawable(R.drawable.null_handler));
         mTabData.add(nullHandlerMap);
 
         // Prepare TabHost.
         final TabHost tabHost = getTabHost();
         final int tabCount = mTabData.size();
         ArrayList<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>(tabCount);
         for (int position = 0; position < tabCount; position++) {
             HashMap<String, ?> map = mTabData.get(position);
             // String label = (String)map.get(GROUP_DATA_KEY_LABEL);
             Drawable icon = (Drawable)map.get(GROUP_DATA_KEY_ICON);
             tabSpecs.add(
                 tabHost.newTabSpec(String.valueOf(position)).setIndicator("", icon)
                                                             .setContent(android.R.id.list));
         }
 
         // Note that every TabHost.setContent() put the passed
         // view in GONE state and addTab() only set VISIBLE state
         // for first tab. Later calls of setContent() will still
         // put the same view in GONE state. To avoid this, defer
         // addTab as late as possible.
         for (int tabId = 0; tabId < tabCount; tabId++) {
             tabHost.addTab(tabSpecs.get(tabId));
         }
 
         // Initialize an CursorAdapter for ListView for first Tab
         CursorAdapter alarmAdapter = (CursorAdapter)mAlarmListView.getAdapter();
         if (alarmAdapter == null) {
             mAlarmListView.setAdapter(
                 new AlarmAdapter(this, getAlarmCursor(0)));
         }
         tabHost.setOnTabChangedListener(this);
     }
 
     private Cursor getAlarmCursor(int position) {
         HashMap<String, ?> map = mTabData.get(position);
         String handler = (String)map.get(GROUP_DATA_KEY_HANDLER);
         Cursor c =
             getContentResolver().query(
                 Alarms.getAlarmUri(-1),
                 AlarmColumns.QUERY_COLUMNS,
                 AlarmColumns.HANDLER + "=?",
                 new String[]{handler},
                 AlarmColumns.DEFAULT_SORT_ORDER);
         return c;
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
         Intent intent = new Intent(this, AlarmSettings.class);
         intent.putExtra(AlarmColumns._ID, alarmId);
         startActivity(intent);
     }
 
     private void showAds(boolean show) {
         if (show) {
             mAdView.setVisibility(View.VISIBLE);
         } else {
             mAdView.setVisibility(View.GONE);
         }
     }
 
     private void showAdsChecked() {
         boolean show = false;
         if (mShowAdsCount > 5) {
             mShowAdsCount = 0;
             show = true;
         }
         showAds(show);
         mShowAdsCount++;
     }
 
     class AlarmAdapter extends CursorAdapter {
         private View.OnClickListener mOnClickListener;
         private View.OnCreateContextMenuListener mOnCreateContextMenuListener;
         private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;
        // private LayoutInflater mInflater;
 
         public AlarmAdapter(Context context, Cursor c) {
             super(context, c);
 
            // mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
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
                         String label = Alarm.getInstance(OpenAlarm.this, alarmId).getStringField(Alarm.FIELD_LABEL);
 
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
                         Context context = (Context)OpenAlarm.this;
                         Alarm alarm = Alarm.getInstance(context, alarmId);
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
 
                             showAdsChecked();
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
                                 Toast.makeText(context, errMsgResId, Toast.LENGTH_LONG).show();
                                 parent.performClick();
                                 buttonView.setChecked(false);
                             }
                         }
                     }
                 };
         }
 
         public void bindView(View view, Context context, Cursor cursor) {
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
         }
 
         public View newView(Context context, Cursor c, ViewGroup parent) {
             LayoutInflater inflater = getLayoutInflater();
             View view = inflater.inflate(R.layout.alarm_list_item, parent, false);
 
             Bundle attachment = new Bundle();
             view.setTag(attachment);
 
             // Should follow Android's design. Click to go to
             // item's default activity and long-click to go to
             // item's extended activities. Here, the first-class
             // activity is to edit item's settings.
             view.setOnClickListener(mOnClickListener);
 
             // The context menu listener of the view must be set
             // in order for its parent's onCreateMenu() to be
             // called to create context menu. This should be a
             // bug but I'm not very sure.
 
             // The ContextMenu.ContextMenuInfo object is returned
             // by getContextMenuInfo() overriden by
             // clients. Here, it is null.
             view.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
 
             return view;
         }
 
         protected void onContentChanged() {
             super.onContentChanged();
             Notification.getInstance().set(OpenAlarm.this);
         }
     }
 }

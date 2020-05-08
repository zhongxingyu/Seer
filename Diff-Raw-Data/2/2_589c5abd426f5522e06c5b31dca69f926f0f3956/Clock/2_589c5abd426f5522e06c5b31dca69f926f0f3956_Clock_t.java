 /*
  * Copyright (C) 2006 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.systemui.statusbar.policy;
 
 import android.app.ActivityManagerNative;
 import android.app.StatusBarManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.os.Handler;
 import android.provider.AlarmClock;
 import android.provider.Settings;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.format.DateFormat;
 import android.text.style.CharacterStyle;
 import android.text.style.RelativeSizeSpan;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.TextView;
 
 import com.android.internal.R;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 /**
  * Digital clock for the status bar.
  */
public class Clock extends TextView implements OnClickListener, OnLongClickListener {
     protected boolean mAttached;
     protected Calendar mCalendar;
     protected String mClockFormatString;
     protected SimpleDateFormat mClockFormat;
     private Locale mLocale;
 
     public static final int AM_PM_STYLE_NORMAL  = 0;
     public static final int AM_PM_STYLE_SMALL   = 1;
     public static final int AM_PM_STYLE_GONE    = 2;
 
     public int AM_PM_STYLE = AM_PM_STYLE_GONE;
 
     protected int mAmPmStyle;
 
     public static final int WEEKDAY_STYLE_GONE   = 0;
     public static final int WEEKDAY_STYLE_SMALL  = 1;
     public static final int WEEKDAY_STYLE_NORMAL = 2;
 
     protected int mWeekdayStyle = WEEKDAY_STYLE_GONE;
 
     public static final int CLOCK_STYLE_RIGHT    = 1;
     public static final int CLOCK_STYLE_CENTER   = 2;
 
     protected int mClockStyle;
 
     protected static int mClockColor = com.android.internal.R.color.holo_blue_light;
     protected static int mExpandedClockColor = com.android.internal.R.color.holo_blue_light;
     protected static int defaultColor, defaultExpandedColor;
 
     Handler mHandler;
 
     protected class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_AM_PM), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUS_BAR_CLOCK_STYLE), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
 		    Settings.System.STATUSBAR_CLOCK_WEEKDAY), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUSBAR_CLOCK_COLOR), false, this);
             resolver.registerContentObserver(Settings.System.getUriFor(
                     Settings.System.STATUSBAR_EXPANDED_CLOCK_COLOR), false, this);
             updateSettings();
         }
 
         @Override public void onChange(boolean selfChange) {
             updateSettings();
         }
     }
 
     public Clock(Context context) {
         this(context, null);
     }
 
     public Clock(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public Clock(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         mHandler = new Handler();
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
         if(isClickable()){
             setOnClickListener(this);
             setOnLongClickListener(this);
         }
         updateSettings();
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mAttached) {
             mAttached = true;
             IntentFilter filter = new IntentFilter();
 
             filter.addAction(Intent.ACTION_TIME_TICK);
             filter.addAction(Intent.ACTION_TIME_CHANGED);
             filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
             filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
             filter.addAction(Intent.ACTION_USER_SWITCHED);
 
             getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
         }
 
         // NOTE: It's safe to do these after registering the receiver since the receiver always runs
         // in the main thread, therefore the receiver can't run before this method returns.
 
         // The time zone may have changed while the receiver wasn't registered, so update the Time
         mCalendar = Calendar.getInstance(TimeZone.getDefault());
 
         // Make sure we update to the current time
         updateClock();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         if (mAttached) {
             getContext().unregisterReceiver(mIntentReceiver);
             mAttached = false;
         }
 
     }
 
     protected final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                 String tz = intent.getStringExtra("time-zone");
                 mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                 if (mClockFormat != null) {
                     mClockFormat.setTimeZone(mCalendar.getTimeZone());
                 }
             } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                 final Locale newLocale = getResources().getConfiguration().locale;
                 if (! newLocale.equals(mLocale)) {
                     mLocale = newLocale;
                     mClockFormatString = ""; // force refresh
                 }
             }
             updateClock();
         }
     };
 
     final void updateClock() {
         mCalendar.setTimeInMillis(System.currentTimeMillis());
         setText(getSmallTime());
     }
 
     private final CharSequence getSmallTime() {
         Context context = getContext();
         boolean b24 = DateFormat.is24HourFormat(context);
         int res;
 
         if (b24) {
             res = R.string.twenty_four_hour_time_format;
         } else {
             res = R.string.twelve_hour_time_format;
         }
 
         final char MAGIC1 = '\uEF00';
         final char MAGIC2 = '\uEF01';
 
         SimpleDateFormat sdf;
         String format = context.getString(res);
         if (!format.equals(mClockFormatString)) {
             mClockFormat = sdf = new SimpleDateFormat(format);
             mClockFormatString = format;
         } else {
             sdf = mClockFormat;
         }
 
         Calendar calendar = Calendar.getInstance();
         int day = calendar.get(Calendar.DAY_OF_WEEK);
 
         String todayIs = null;
         String result = sdf.format(mCalendar.getTime());
 
         if (mWeekdayStyle != WEEKDAY_STYLE_GONE) {
             todayIs = (new SimpleDateFormat("E")).format(mCalendar.getTime()) + " ";
             result = todayIs + result;
         }
 
         SpannableStringBuilder formatted = new SpannableStringBuilder(result);
 
         if (!b24) {
             if (mAmPmStyle != AM_PM_STYLE_NORMAL) {
                 String AmPm;
                 if (format.indexOf("a")==0) {
                     AmPm = (new SimpleDateFormat("a ")).format(mCalendar.getTime());
                 } else {
                     AmPm = (new SimpleDateFormat(" a")).format(mCalendar.getTime());
                 }
                 if (mAmPmStyle == AM_PM_STYLE_GONE) {
                     formatted.delete(result.indexOf(AmPm), result.lastIndexOf(AmPm)+AmPm.length());
                 } else {
                     if (mAmPmStyle == AM_PM_STYLE_SMALL) {
                         CharacterStyle style = new RelativeSizeSpan(0.7f);
                         formatted.setSpan(style, result.indexOf(AmPm), result.lastIndexOf(AmPm)+AmPm.length(),
                                 Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                     }
                 }
             }
         }
         if (mWeekdayStyle != WEEKDAY_STYLE_NORMAL) {
             if (todayIs != null) {
                 if (mWeekdayStyle == WEEKDAY_STYLE_GONE) {
                     formatted.delete(result.indexOf(todayIs), result.lastIndexOf(todayIs)+todayIs.length());
                 } else {
                     if (mWeekdayStyle == WEEKDAY_STYLE_SMALL) {
                         CharacterStyle style = new RelativeSizeSpan(0.7f);
                         formatted.setSpan(style, result.indexOf(todayIs), result.lastIndexOf(todayIs)+todayIs.length(),
                                           Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                     }
                 }
             }
         }
         return formatted;
     }
 
     private void updateParameters() {
         mClockFormatString = null;
     }
 
     protected void updateSettings(){
         ContentResolver resolver = mContext.getContentResolver();
 
         mAmPmStyle = (Settings.System.getInt(resolver,
                 Settings.System.STATUS_BAR_AM_PM, 2));
         mWeekdayStyle = Settings.System.getInt(resolver,
                 Settings.System.STATUSBAR_CLOCK_WEEKDAY, WEEKDAY_STYLE_GONE);
 
         if (mAmPmStyle != AM_PM_STYLE) {
             AM_PM_STYLE = mAmPmStyle;
             mClockFormatString = "";
 
             if (mAttached) {
                 updateClock();
             }
         }
         if (IsShade()) {
             defaultExpandedColor = getCurrentTextColor();
             mExpandedClockColor = Settings.System.getInt(resolver,
                 Settings.System.STATUSBAR_EXPANDED_CLOCK_COLOR, defaultExpandedColor);
             if (mClockColor == Integer.MIN_VALUE) {
                 // flag to reset the color
                 mClockColor = defaultColor;
             }
             setTextColor(mExpandedClockColor);
         } else {
             defaultColor = getCurrentTextColor();
             mClockColor = Settings.System.getInt(resolver,
                 Settings.System.STATUSBAR_CLOCK_COLOR, defaultColor);
             if (mExpandedClockColor == Integer.MIN_VALUE) {
                 // flag to reset the color
                 mExpandedClockColor = defaultExpandedColor;
             }
             setTextColor(mClockColor);
         }
         mClockStyle = (Settings.System.getInt(resolver,Settings.System.STATUS_BAR_CLOCK_STYLE, 1));
         updateClockVisibility(true);  
     }
 
     public boolean IsShade()
     {
         Object o = getTag();
         return (o != null && o.toString().equals("expanded"));
     }
 
     public void updateClockVisibility(boolean show) {
         if (mClockStyle == CLOCK_STYLE_RIGHT)
             setVisibility(show ? View.VISIBLE : View.GONE);
         else
             setVisibility(View.GONE);
     }
 
     private void collapseStartActivity(Intent what) {
         // collapse status bar
         StatusBarManager statusBarManager = (StatusBarManager) getContext().getSystemService(
                 Context.STATUS_BAR_SERVICE);
         statusBarManager.collapsePanels();
 
         // dismiss keyguard in case it was active and no passcode set
         try {
             ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
         } catch (Exception ex) {
             // no action needed here
         }
 
         // start activity
         what.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         mContext.startActivity(what);
     }
 
     @Override
     public void onClick(View v) {
         // start com.android.deskclock/.DeskClock
         ComponentName clock = new ComponentName("com.android.deskclock",
                 "com.android.deskclock.DeskClock");
         Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                 .setComponent(clock);
         collapseStartActivity(intent);
     }
 
     @Override
     public boolean onLongClick(View v) {
         Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
         collapseStartActivity(intent);
 
         // consume event
         return true;
     }
 }
 

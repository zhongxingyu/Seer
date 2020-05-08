 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.internal.widget;
 
 import com.android.internal.R;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.ContentObserver;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.provider.Settings;
 import android.text.format.DateFormat;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.lang.ref.WeakReference;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 
 /**
  * Displays the time Kanji style
  */
 public class KanjiClock extends LinearLayout {
 
     // Dunno what this means, just add from Google translate xD
     private final static String J_0 = "\u96F6";
     private final static String J_1 = "\u4E00";
     private final static String J_2 = "\u4E8C";
     private final static String J_3 = "\u4E09";
     private final static String J_4 = "\u56DB";
     private final static String J_5 = "\u4E94";
     private final static String J_6 = "\u516D";
     private final static String J_7 = "\u4E03";
     private final static String J_8 = "\u516B";
     private final static String J_9 = "\u4E5D";
     private final static String J_10 = "\u5341";
     private final static String J_100 = "\u767E";
     private final static String J_1000 = "\u5343";
     private final static String J_AM = "\u5348\u524D";
     private final static String J_PM = "\u5348\u5F8C";
     private final static String J_HOUR = "\u6642";
     private final static String J_MINUTE = "\u5206";
     private final static String J_SECOND = "\u79D2";
 
     private Calendar mCalendar;
     private String mFormat;
     private TextView mTimeDisplay;
     private ContentObserver mFormatChangeObserver;
     private int mAttached = 0; // for debugging - tells us whether attach/detach is unbalanced
     private boolean fullkanji;
 
     /* called by system on minute ticks */
     private final Handler mHandler = new Handler();
     private BroadcastReceiver mIntentReceiver;
 
     private static class TimeChangedReceiver extends BroadcastReceiver {
         private WeakReference<KanjiClock> mClock;
         private Context mContext;
 
         public TimeChangedReceiver(KanjiClock clock) {
             mClock = new WeakReference<KanjiClock>(clock);
             mContext = clock.getContext();
         }
 
         @Override
         public void onReceive(Context context, Intent intent) {
             // Post a runnable to avoid blocking the broadcast.
             final boolean timezoneChanged =
                     intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
             final KanjiClock clock = mClock.get();
             if (clock != null) {
                 clock.mHandler.post(new Runnable() {
                     public void run() {
                         if (timezoneChanged) {
                             clock.mCalendar = Calendar.getInstance();
                         }
                         clock.updateTime();
                     }
                 });
             } else {
                 try {
                     mContext.unregisterReceiver(this);
                 } catch (RuntimeException e) {
                     // Shouldn't happen
                 }
             }
         }
     };
 
     private static class FormatChangeObserver extends ContentObserver {
         private WeakReference<KanjiClock> mClock;
         private Context mContext;
         public FormatChangeObserver(KanjiClock clock) {
             super(new Handler());
             mClock = new WeakReference<KanjiClock>(clock);
             mContext = clock.getContext();
         }
         @Override
         public void onChange(boolean selfChange) {
             KanjiClock KanjiClock = mClock.get();
             if (KanjiClock != null) {
                 KanjiClock.updateTime();
             } else {
                 try {
                     mContext.getContentResolver().unregisterContentObserver(this);
                 } catch (RuntimeException e) {
                     // Shouldn't happen
                 }
             }
         }
     }
 
     public KanjiClock(Context context) {
         this(context, null);
     }
 
     public KanjiClock(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     @Override
     protected void onFinishInflate() {
         super.onFinishInflate();
 
         mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
 
         fullkanji = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.LOCKSCREEN_FUZZY_CLOCK, 1) == 3;
 
         int font = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.CLOCK_FONT, 0);
         if (font == 0) {
             mTimeDisplay.setTypeface(Typeface.createFromFile("/system/fonts/DroidSans.ttf"));
         } else if (font == 1) {
             mTimeDisplay.setTypeface(Typeface
                     .createFromFile("/system/fonts/DroidSansJapanese.ttf"));
         }
         mCalendar = Calendar.getInstance();
     }
 
     @Override
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         mAttached++;
 
         /* monitor time ticks, time changed, timezone */
         if (mIntentReceiver == null) {
             mIntentReceiver = new TimeChangedReceiver(this);
             IntentFilter filter = new IntentFilter();
             filter.addAction(Intent.ACTION_TIME_TICK);
             filter.addAction(Intent.ACTION_TIME_CHANGED);
             filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
             mContext.registerReceiver(mIntentReceiver, filter);
         }
 
         /* monitor 12/24-hour display preference */
         if (mFormatChangeObserver == null) {
             mFormatChangeObserver = new FormatChangeObserver(this);
             mContext.getContentResolver().registerContentObserver(
                     Settings.System.CONTENT_URI, true, mFormatChangeObserver);
         }
 
         updateTime();
     }
 
     @Override
     protected void onDetachedFromWindow() {
         super.onDetachedFromWindow();
 
         mAttached--;
 
         if (mIntentReceiver != null) {
             mContext.unregisterReceiver(mIntentReceiver);
         }
         if (mFormatChangeObserver != null) {
             mContext.getContentResolver().unregisterContentObserver(
                     mFormatChangeObserver);
         }
 
         mFormatChangeObserver = null;
         mIntentReceiver = null;
     }
 
     void updateTime(Calendar c) {
         mCalendar = c;
         updateTime();
     }
 
     public void updateTime() {
         mCalendar.setTimeInMillis(System.currentTimeMillis());
         int mSeconds = mCalendar.get(mCalendar.SECOND);
         int mMinutes = mCalendar.get(mCalendar.MINUTE);
         int mHour = mCalendar.get(mCalendar.HOUR);
         int mAmPm = mCalendar.get(mCalendar.AM_PM);
         String mTimeString;
 
         fullkanji = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.LOCKSCREEN_FUZZY_CLOCK, 1) == 3;
 
         if (fullkanji) {
           mTimeString = getKanjiHour(mHour) + " " + getKanjiMinute(mMinutes) + " " + getKanjiSecond(mSeconds, mAmPm);
         } else {
           mTimeString = getHours(mHour) + " " + getMinutes(mMinutes) + " " + getSeconds(mSeconds, mAmPm);
         }
 
         //print the time
         mTimeDisplay.setText(mTimeString);
 
     }
 
    private String getKanjiHour(Integer calendarHour) {
        return getKanji(calendarHour.toString()) + J_HOUR;
     }
 
     private String getKanjiMinute(Integer calendarMinute) {
         return getKanji(calendarMinute.toString()) + J_MINUTE;
     }
 
    private String getKanjiSecond(Integer calendarSecond, Integer amPm) {
        return getKanji(calendarSecond.toString()) + J_SECOND + (amPm == Calendar.AM ? J_AM : J_PM);
     }
 
    private String getHours(Integer calendarHour) {
        return calendarHour.toString() + J_HOUR;
     }
 
     private String getMinutes(Integer calendarMinute) {
         return calendarMinute.toString() + J_MINUTE;
     }
 
    private String getSeconds(Integer calendarSecond, Integer amPm) {
        return calendarSecond.toString() + J_SECOND + (amPm == Calendar.AM ? J_AM : J_PM);
     }
 
     private String getKanji(String arabicNumber) {
         try {
             Integer.parseInt(arabicNumber);
         } catch (NumberFormatException ex) {
             return "";
         }
 
         return parseArabicNumber(arabicNumber);
     }
 
     private String parseArabicNumber(String arabicNumber) {
         Integer numberLength = arabicNumber.length();
         Integer currentDigit = 1;
         String kanji = "";
         CharacterIterator it = new StringCharacterIterator(arabicNumber);
 
         for (char digit = it.last(); digit != CharacterIterator.DONE; digit = it.previous()) {
 
             Boolean isPrintableTens = currentDigit > 1 && digit != '0';
             if (isPrintableTens) {
                 switch (currentDigit) {
                     case 2:
                         kanji = J_10 + kanji;
                         break;
                     case 3:
                         kanji = J_100 + kanji;
                         break;
                     case 4:
                         kanji = J_1000 + kanji;
                         break;
                     default:
                         break;
                 }
             }
 
             Boolean isPrintableDigit = true;
 
             Boolean isFirstDigit = currentDigit == 1;
             Boolean isFirstDigitPrintable = numberLength == 1 || digit != '0';
             isPrintableDigit = !isFirstDigit || isFirstDigitPrintable;
 
             if (!isFirstDigit) {
                 isPrintableDigit = digit != '1' && digit != '0';
             }
 
             if (isPrintableDigit) {
                 kanji = digitToKanji(digit) + kanji;
             }
 
             currentDigit++;
         }
 
         return kanji;
     }
 
     private Character iteratorPeek(CharacterIterator iterator) {
         int currentIndex = iterator.getIndex();
         Character peeked = iterator.next();
         iterator.setIndex(currentIndex);
         return peeked;
     }
 
     private String digitToKanji(char digit) {
         String digitString;
         switch (digit) {
             case '0':
                 digitString = J_0;
                 break;
             case '1':
                 digitString = J_1;
                 break;
             case '2':
                 digitString = J_2;
                 break;
             case '3':
                 digitString = J_3;
                 break;
             case '4':
                 digitString = J_4;
                 break;
             case '5':
                 digitString = J_5;
                 break;
             case '6':
                 digitString = J_6;
                 break;
             case '7':
                 digitString = J_7;
                 break;
             case '8':
                 digitString = J_8;
                 break;
             case '9':
                 digitString = J_9;
                 break;
             default:
                 digitString = "";
                 break;
         }
         return digitString;
     }
 }

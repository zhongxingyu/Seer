 /*
  Copyright (c) 2011, Sony Ericsson Mobile Communications AB
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
 
  * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
 
  * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.sonyericsson.extras.liveware.extension.androiddevcamp_kyoto_trendwatch;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.text.format.DateUtils;
 import android.util.Log;
 
 import com.sonyericsson.extras.liveware.aef.widget.Widget;
 import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;
 import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;
 
 /**
  * The sample widget handles the widget on an accessory. This class exists in
  * one instance for every supported host application that we have registered to.
  */
 class SampleWidget extends WidgetExtension {
 
     public static final int WIDTH = 128;
 
     public static final int HEIGHT = 110;
 
     private static final long UPDATE_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS;
 
     private static final SimpleDateFormat TIME_FORMAT_24_H = new SimpleDateFormat("HH:mm",
             new Locale("se"));
 
     private static final SimpleDateFormat TIME_FORMAT_AM_PM = new SimpleDateFormat("hh:mm a",
             new Locale("se"));
     
     private String[] twitterTrend;
     
     private int arrayCnt = 0;
     private String[] stringArray = {"hoge1","hoge2","hoge3","hoge4","hoge5","hoge6","hoge7","hoge8","hoge9","hoge10"};
     private String currentString = "trend";
 
     /**
      * Create sample widget.
      *
      * @param hostAppPackageName Package name of host application.
      * @param context The context.
      */
     SampleWidget(final String hostAppPackageName, final Context context) {
         super(context, hostAppPackageName);
     }
 
     /**
      * Start refreshing the widget. The widget is now visible.
      */
     @Override
     public void onStartRefresh() {
         Log.d(SampleExtensionService.LOG_TAG, "startRefresh");
         // Update now and every 10th second
         cancelScheduledRefresh(SampleExtensionService.EXTENSION_KEY);
         scheduleRepeatingRefresh(System.currentTimeMillis(), UPDATE_INTERVAL,
                 SampleExtensionService.EXTENSION_KEY);
        this.stringArray = getTwitterTrend();
         for (int j = 0; j<stringArray.length; j++) {
         	Log.d("trend", "trend : " + stringArray[j]);
         }
     }
 
     /**
      * Stop refreshing the widget. The widget is no longer visible.
      */
     @Override
     public void onStopRefresh() {
         Log.d(SampleExtensionService.LOG_TAG, "stopRefesh");
 
         // Cancel pending clock updates
         cancelScheduledRefresh(SampleExtensionService.EXTENSION_KEY);
     }
 
     @Override
     public void onScheduledRefresh() {
         Log.d(SampleExtensionService.LOG_TAG, "scheduledRefresh()");
     	setCurrentString();
         updateWidget();
     }
 
     /**
      * Unregister update clock receiver, cancel pending updates
      */
     @Override
     public void onDestroy() {
         Log.d(SampleExtensionService.LOG_TAG, "onDestroy()");
         onStopRefresh();
     }
 
     /**
      * The widget has been touched.
      *
      * @param type The type of touch event.
      * @param x The x position of the touch event.
      * @param y The y position of the touch event.
      */
     @Override
     public void onTouch(final int type, final int x, final int y) {
         Log.d(SampleExtensionService.LOG_TAG, "onTouch() " + type);
         if (!SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.contains(x, y)) {
             Log.d(SampleExtensionService.LOG_TAG, "Ignoring touch outside active area x: " + x
                     + " y: " + y);
             return;
         }
 
         if (type == Widget.Intents.EVENT_TYPE_SHORT_TAP) {
         	/*
             // Change clock mode on short tap.
             setClockMode24h(!isClockMode24h());
             */
             // Update clock widget now
         	addArrayCnt();
         	setCurrentString();
             updateWidget();
         }
     }
     
     private void setCurrentString() {
 		this.currentString = this.stringArray[arrayCnt];
 	}
     
     private void addArrayCnt() {
 		arrayCnt++;
 		if (arrayCnt > (stringArray.length - 1)) {
 			arrayCnt = 0;
 		}
 	}
 
     /**
      * Set clock format
      *
      * @return True if 24-h clock format, false to use 12-h am/pm
      */
     private boolean isClockMode24h() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
         return prefs.getBoolean(mContext.getString(R.string.preference_key_clock_mode), true);
     }
 
     /**
      * Get clock format
      *
      * @param isClockMode24h True if 24-h clock format, false to use 12-h am/pm
      */
     private void setClockMode24h(boolean isClockMode24h) {
         Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
         editor.putBoolean(mContext.getString(R.string.preference_key_clock_mode), isClockMode24h);
         editor.commit();
     }
 
     /**
      * Update the widget.
      */
     private void updateWidget() {
         Log.d(SampleExtensionService.LOG_TAG, "updateWidget");
         // Get time
         /*
         String time = null;
         if (isClockMode24h()) {
             time = TIME_FORMAT_24_H.format(new Date());
         } else {
             time = TIME_FORMAT_AM_PM.format(new Date());
         }
 
         showBitmap(new SmartWatchSampleWidgetImage(mContext, time).getBitmap());
         */
         showBitmap(new SmartWatchSampleWidgetImage(mContext, this.currentString).getBitmap());
     }
     
     /**
      * get trend from Twitter API
      */
    private String[] getTwitterTrend () {
     	Log.d(SampleExtensionService.LOG_TAG, "getTwitterTrend");
     	
     	try {
    		URI uri = new URI("https://api.twitter.com/1/trends/23424856.json");
             HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
             InputStream is = con.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             String line;
             String jsonStr = "";
             while ((line = reader.readLine()) != null) {
                 //Log.d("json", line);
                 jsonStr = jsonStr.concat(line);
             }
             is.close();
             reader.close();
             //Log.d("json", "jsonstr:" + jsonStr);
             generateTwitterTrendString(jsonStr);
         } catch (URISyntaxException e) {
             Log.d(SampleExtensionService.LOG_TAG, "URI Syntax error", e);
         } catch (MalformedURLException e) {
             Log.d(SampleExtensionService.LOG_TAG, "URL Malformed Error", e);
         } catch (IOException e) {
             Log.d(SampleExtensionService.LOG_TAG, "IO error", e);
 		}
 		return twitterTrend;
     }
     
     private void generateTwitterTrendString (String jsonStr) {
     	try {
     		//Log.d("", "jsonstr:" + jsonStr);
     		JSONArray json = new JSONArray(jsonStr);
     		JSONObject jsonobj = json.getJSONObject(0);
     		JSONArray results = jsonobj.getJSONArray("trends");
     		int cnt = results.length();
     		//Log.d("", "jsonarraycnt:"+cnt);
     		twitterTrend = new String[cnt];
     		for (int i = 0; i < cnt; i++) {
     			JSONObject item = results.getJSONObject(i);
     			
     			String name = item.getString("name");
     			//Log.d("trend name", "trendName : " + name);
     			twitterTrend[i] = name;
     		}
     	} catch (Exception e) {
     		Log.d(SampleExtensionService.LOG_TAG, "JSON generate string error", e);
     	}
     }
 }

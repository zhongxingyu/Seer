 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package com.android.systemui.statusbar.popups;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import org.w3c.dom.Document;
 import android.text.format.DateFormat;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import com.android.internal.util.weather.HttpRetriever;
 import com.android.internal.util.weather.WeatherInfo;
 import com.android.internal.util.weather.WeatherXmlParser;
 import com.android.internal.util.weather.YahooPlaceFinder;
 
 import com.android.systemui.R;
 
 /**
  * WeatherPopup clock view for desk docks.
  */
 public class WeatherPopup extends QuickSettings {
     public WeatherPopup(View anchor) {
         super(anchor);
     }
     private static final boolean DEBUG = false;
 
     private static final String TAG = "WeatherPopup";
 
     private TextView mWeatherCity, mWeatherCondition, mWeatherLowHigh, mWeatherTemp, mWeatherUpdateTime;
     private ImageView mWeatherImage;
     private ViewGroup root;
     private Context mContext;
 
     @Override
     protected void onCreate() {
         // give up any internal focus before we switch layouts
         LayoutInflater inflater =
                 (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         mContext = this.anchor.getContext();
 
         root = (ViewGroup)inflater.inflate(R.layout.weatherpopup, null);
 
         mWeatherCity = (TextView) root.findViewById(R.id.weather_city);
         mWeatherCondition = (TextView) root.findViewById(R.id.weather_condition);
         mWeatherTemp = (TextView) root.findViewById(R.id.weather_temp);
         mWeatherLowHigh = (TextView) root.findViewById(R.id.weather_low_high);
         mWeatherUpdateTime = (TextView) root.findViewById(R.id.update_time);
         mWeatherImage = (ImageView) root.findViewById(R.id.weather_image);
 
         mWeatherImage.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (mWeatherCondition != null) {
                     mWeatherCondition.setText(com.android.internal.R.string.weather_refreshing);
                 }
 
                 if (!mHandler.hasMessages(QUERY_WEATHER)) {
                    mHandler.sendEmptyMessage(QUERY_WEATHER);
                 }
             }
         });
 
         this.setContentView(root);
     }
 
 
     /*
      * CyanogenMod Lock screen Weather related functionality
      */
     private static final String URL_YAHOO_API_WEATHER = "http://weather.yahooapis.com/forecastrss?w=%s&u=";
     private static WeatherInfo mWeatherInfo = new WeatherInfo();
     private static final int QUERY_WEATHER = 0;
     private static final int UPDATE_WEATHER = 1;
 
     private Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case QUERY_WEATHER:
                 Thread queryWeather = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         LocationManager locationManager = (LocationManager) mContext.
                                 getSystemService(Context.LOCATION_SERVICE);
                         final ContentResolver resolver = mContext.getContentResolver();
                         boolean useCustomLoc = Settings.System.getInt(resolver,
                                 Settings.System.WEATHER_USE_CUSTOM_LOCATION, 0) == 1;
                         String customLoc = Settings.System.getString(resolver,
                                     Settings.System.WEATHER_CUSTOM_LOCATION);
                         String woeid = null;
 
                         // custom location
                         if (customLoc != null && useCustomLoc) {
                             try {
                                 woeid = YahooPlaceFinder.GeoCode(mContext, customLoc);
                                 if (DEBUG)
                                     Log.d(TAG, "Yahoo location code for " + customLoc + " is " + woeid);
                             } catch (Exception e) {
                                 Log.e(TAG, "ERROR: Could not get Location code");
                                 e.printStackTrace();
                             }
                         // network location
                         } else {
                             Criteria crit = new Criteria();
                             crit.setAccuracy(Criteria.ACCURACY_COARSE);
                             String bestProvider = locationManager.getBestProvider(crit, true);
                             Location loc = null;
                             if (bestProvider != null) {
                                 loc = locationManager.getLastKnownLocation(bestProvider);
                             } else {
                                 loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                             }
                             try {
                                 woeid = YahooPlaceFinder.reverseGeoCode(mContext, loc.getLatitude(),
                                         loc.getLongitude());
                                 if (DEBUG)
                                     Log.d(TAG, "Yahoo location code for current geolocation is " + woeid);
                             } catch (Exception e) {
                                 Log.e(TAG, "ERROR: Could not get Location code");
                                 e.printStackTrace();
                             }
                         }
                         Message msg = Message.obtain();
                         msg.what = UPDATE_WEATHER;
                         msg.obj = woeid;
                         mHandler.sendMessage(msg);
                     }
                 });
                 queryWeather.setPriority(Thread.MIN_PRIORITY);
                 queryWeather.start();
                 break;
             case UPDATE_WEATHER:
                 String woeid = (String) msg.obj;
                 if (woeid != null) {
                     if (DEBUG) {
                         Log.d(TAG, "Location code is " + woeid);
                     }
                     WeatherInfo w = null;
                     try {
                         w = parseXml(getDocument(woeid));
                     } catch (Exception e) {
                     }
                     if (w == null) {
                         setNoWeatherData();
                     } else {
                         setWeatherData(w);
                         mWeatherInfo = w;
                     }
                 } else {
                     if (mWeatherInfo.temp.equals(WeatherInfo.NODATA)) {
                         setNoWeatherData();
                     } else {
                         setWeatherData(mWeatherInfo);
                     }
                 }
                 break;
             }
         }
     };
 
     /**
      * Reload the weather forecast
      */
     private void refreshWeather() {
         final ContentResolver resolver = this.anchor.getContext().getContentResolver();
             final long interval = Settings.System.getLong(resolver,
                    Settings.System.WEATHER_UPDATE_INTERVAL, 0); // Default to manual
             boolean manualSync = (interval == 0);
             if (!manualSync && (((System.currentTimeMillis() - mWeatherInfo.last_sync) / 60000) >= interval)) {
                 mHandler.sendEmptyMessage(QUERY_WEATHER);
             } else if (manualSync && mWeatherInfo.last_sync == 0) {
                 setNoWeatherData();
             } else {
                 setWeatherData(mWeatherInfo);
             }
     }
 
     /**
      * Display the weather information
      * @param w
      */
     private void setWeatherData(WeatherInfo w) {
         final ContentResolver resolver = this.anchor.getContext().getContentResolver();
         final Resources res = this.anchor.getContext().getResources();
         boolean showLocation = Settings.System.getInt(resolver,
                 Settings.System.WEATHER_SHOW_LOCATION, 1) == 1;
         boolean showTimestamp = Settings.System.getInt(resolver,
                 Settings.System.WEATHER_SHOW_TIMESTAMP, 1) == 1;
         boolean invertLowhigh = Settings.System.getInt(resolver,
                 Settings.System.WEATHER_INVERT_LOWHIGH, 0) == 1;
 
             if (mWeatherImage != null) {
                 String conditionCode = w.condition_code;
                 String condition_filename = "weather_" + conditionCode;
                 int resID = res.getIdentifier(condition_filename, "drawable",
                         this.anchor.getContext().getPackageName());
 
                 if (DEBUG)
                     Log.d("Weather", "Condition:" + conditionCode + " ID:" + resID);
 
                 if (resID != 0) {
                     mWeatherImage.setImageDrawable(res.getDrawable(resID));
                 } else {
                     mWeatherImage.setImageResource(com.android.internal.R.drawable.weather_na);
                 }
             }
             if (mWeatherTemp != null) {
                 mWeatherTemp.setText(w.temp);
             }
             if (mWeatherCity != null) {
                 mWeatherCity.setText(w.city);
                 mWeatherCity.setVisibility(showLocation ? View.VISIBLE : View.GONE);
             }
             if (mWeatherCondition != null) {
                 mWeatherCondition.setText(w.condition);
             }
             if (mWeatherLowHigh != null) {
                 mWeatherLowHigh.setText(invertLowhigh ? w.high + " | " + w.low : w.low + " | " + w.high);
             }
             if (mWeatherUpdateTime != null) {
                 Date lastTime = new Date(mWeatherInfo.last_sync);
                 String date = DateFormat.getDateFormat(this.anchor.getContext()).format(lastTime);
                 String time = DateFormat.getTimeFormat(this.anchor.getContext()).format(lastTime);
                 mWeatherUpdateTime.setText(date + " " + time);
                 mWeatherUpdateTime.setVisibility(showTimestamp ? View.VISIBLE : View.GONE);
             }
     }
 
     /**
      * There is no data to display, display 'empty' fields and the
      * 'Tap to reload' message
      */
     private void setNoWeatherData() {
         final ContentResolver resolver = this.anchor.getContext().getContentResolver();
         boolean useMetric = Settings.System.getInt(resolver,
                 Settings.System.WEATHER_USE_METRIC, 1) == 1;
 
             if (mWeatherImage != null) {
                 mWeatherImage.setImageResource(com.android.internal.R.drawable.weather_na);
             }
             if (mWeatherTemp != null) {
                 mWeatherTemp.setVisibility(View.GONE);
             }
             if (mWeatherCity != null) {
                 mWeatherCity.setText(com.android.internal.R.string.weather_no_data);
                 mWeatherCity.setVisibility(View.VISIBLE);
             }
             if (mWeatherCondition != null) {
                 mWeatherCondition.setText(com.android.internal.R.string.weather_tap_to_refresh);
             }
             if (mWeatherLowHigh != null) {
                 mWeatherLowHigh.setVisibility(View.GONE);
             }
             if (mWeatherUpdateTime != null) {
                 mWeatherUpdateTime.setVisibility(View.GONE);
             }
     }
 
     /**
      * Get the weather forecast XML document for a specific location
      * @param woeid
      * @return
      */
     private Document getDocument(String woeid) {
         try {
             boolean celcius = Settings.System.getInt(this.anchor.getContext().getContentResolver(),
                     Settings.System.WEATHER_USE_METRIC, 1) == 1;
             String urlWithDegreeUnit;
 
             if (celcius) {
                 urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "c";
             } else {
                 urlWithDegreeUnit = URL_YAHOO_API_WEATHER + "f";
             }
 
             return new HttpRetriever().getDocumentFromURL(String.format(urlWithDegreeUnit, woeid));
         } catch (IOException e) {
             Log.e(TAG, "Error querying Yahoo weather");
         }
 
         return null;
     }
 
     /**
      * Parse the weather XML document
      * @param wDoc
      * @return
      */
     private WeatherInfo parseXml(Document wDoc) {
         try {
             return new WeatherXmlParser(this.anchor.getContext()).parseWeatherResponse(wDoc);
         } catch (Exception e) {
             Log.e(TAG, "Error parsing Yahoo weather XML document");
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     protected void onShow() {
         refreshWeather();
     }
 
     @Override
     public void dismiss() {
 	this.dismiss();
     }
 }

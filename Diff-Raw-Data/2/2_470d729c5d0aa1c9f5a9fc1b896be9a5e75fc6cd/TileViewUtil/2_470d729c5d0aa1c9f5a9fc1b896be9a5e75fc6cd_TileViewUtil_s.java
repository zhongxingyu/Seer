 /*
  * Copyright (C) 2011 The CyanogenMod Project
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
 
 package com.cyanogenmod.cmparts.utils;
 
 import com.cyanogenmod.cmparts.R;
 
 import android.content.Context;
 import android.provider.Settings;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  * THIS CLASS'S DATA MUST BE KEPT UP-TO-DATE
  * SystemUI PACKAGE.
  */
 public class TileViewUtil {
     public static final String TILE_BATTERY = "toggleBattery";
     public static final String TILE_WIFI = "toggleWifi";
     public static final String TILE_GPS = "toggleGPS";
     public static final String TILE_BLUETOOTH = "toggleBluetooth";
     public static final String TILE_BRIGHTNESS = "toggleBrightness";
     public static final String TILE_RINGER = "toggleSound";
     public static final String TILE_SYNC = "toggleSync";
     public static final String TILE_SLEEP = "toggleSleep";
     public static final String TILE_SCREENSHOT = "toggleScreenshot";
     public static final String TILE_SCREENTIME = "toggleScreentime";
     public static final String TILE_SETTING = "toggleSettings";
     public static final String TILE_TIME = "toggleTime";
     public static final String TILE_WIFIAP = "toggleWifiAp";
     public static final String TILE_MOBILEDATA = "toggleMobileData";
     public static final String TILE_NETWORKMODE = "toggleNetworkMode";
     public static final String TILE_AUTOROTATE = "toggleAutoRotate";
     public static final String TILE_AIRPLANE = "toggleAirplane";
     public static final String TILE_TORCH = "toggleFlashlight";  // Keep old string for compatibility
     public static final String TILE_LOCKSCREEN = "toggleLockscreen";
     public static final String TILE_USER = "toggleUser";
     public static final String TILE_CPU = "toggleCpu";
     public static final String TILE_WEATHER = "toggleWeather";
     public static final String TILE_NOTIF = "toggleNotif";
     public static final String TILE_PROFILE = "toggleProfile";
     public static final String TILE_POWER = "togglePower";
     public static final String TILE_ALARM = "toggleAlarm";
 
     public static final HashMap<String, TileInfo> TILES = new HashMap<String, TileInfo>();
     static {
         TILES.put(TILE_AIRPLANE, new TileViewUtil.TileInfo(
                 TILE_AIRPLANE, R.string.title_toggle_airplane, "com.android.systemui:drawable/ic_qs_airplane_off"));
         TILES.put(TILE_AUTOROTATE, new TileViewUtil.TileInfo(
                 TILE_AUTOROTATE, R.string.title_toggle_autorotate, "com.android.systemui:drawable/ic_qs_auto_rotate"));
         TILES.put(TILE_BLUETOOTH, new TileViewUtil.TileInfo(
                 TILE_BLUETOOTH, R.string.title_toggle_bluetooth, "com.android.systemui:drawable/ic_qs_bluetooth_neutral"));
         TILES.put(TILE_BRIGHTNESS, new TileViewUtil.TileInfo(
                 TILE_BRIGHTNESS, R.string.title_toggle_brightness, "com.android.systemui:drawable/ic_qs_brightness_auto_on"));
         TILES.put(TILE_TORCH, new TileViewUtil.TileInfo(
                 TILE_TORCH, R.string.title_toggle_flashlight, "com.android.systemui:drawable/ic_qs_torch_off"));
         TILES.put(TILE_GPS, new TileViewUtil.TileInfo(
                 TILE_GPS, R.string.title_toggle_gps, "com.android.systemui:drawable/ic_qs_gps_off"));
         TILES.put(TILE_LOCKSCREEN, new TileViewUtil.TileInfo(
                 TILE_LOCKSCREEN, R.string.title_toggle_lockscreen, "com.android.systemui:drawable/ic_qs_lock_screen_off"));
         TILES.put(TILE_MOBILEDATA, new TileViewUtil.TileInfo(
                 TILE_MOBILEDATA, R.string.title_toggle_mobiledata, "com.android.systemui:drawable/ic_qs_signal_full_1"));
         TILES.put(TILE_NETWORKMODE, new TileViewUtil.TileInfo(
                 TILE_NETWORKMODE, R.string.title_toggle_networkmode, "com.android.systemui:drawable/ic_qs_2g3g_on"));
         TILES.put(TILE_SCREENTIME, new TileViewUtil.TileInfo(
                 TILE_SCREENTIME, R.string.title_toggle_screentimeout, "com.android.systemui:drawable/ic_qs_screen_timeout_off"));
         TILES.put(TILE_SLEEP, new TileViewUtil.TileInfo(
                 TILE_SLEEP, R.string.title_toggle_sleep, "com.android.systemui:drawable/ic_qs_sleep"));
         TILES.put(TILE_SCREENSHOT, new TileViewUtil.TileInfo(
                 TILE_SCREENSHOT, R.string.title_toggle_screenshot, "com.android.systemui:drawable/ic_qs_screenshot"));
         TILES.put(TILE_RINGER, new TileViewUtil.TileInfo(
                 TILE_RINGER, R.string.title_toggle_sound, "com.android.systemui:drawable/ic_qs_ring_vibrate_on"));
         TILES.put(TILE_SYNC, new TileViewUtil.TileInfo(
                 TILE_SYNC, R.string.title_toggle_sync, "com.android.systemui:drawable/ic_qs_sync_off"));
         TILES.put(TILE_WIFI, new TileViewUtil.TileInfo(
                 TILE_WIFI, R.string.title_toggle_wifi, "com.android.systemui:drawable/ic_qs_wifi_full_1"));
         TILES.put(TILE_WIFIAP, new TileViewUtil.TileInfo(
                 TILE_WIFIAP, R.string.title_toggle_wifiap, "com.android.systemui:drawable/ic_qs_wifi_ap_off"));
         TILES.put(TILE_SETTING, new TileViewUtil.TileInfo(
                 TILE_SETTING, R.string.title_toggle_setting, "com.android.systemui:drawable/ic_qs_settings"));
         TILES.put(TILE_TIME, new TileViewUtil.TileInfo(
                 TILE_TIME, R.string.title_toggle_time, "com.android.systemui:drawable/ic_qs_clock_circle"));
         TILES.put(TILE_BATTERY, new TileViewUtil.TileInfo(
                 TILE_BATTERY, R.string.title_toggle_battery, "com.android.systemui:drawable/ic_qs_battery_charge_28"));
         TILES.put(TILE_USER, new TileViewUtil.TileInfo(
                 TILE_USER, R.string.title_toggle_user, "com.android.systemui:drawable/ic_menu_allfriends"));
         TILES.put(TILE_CPU, new TileViewUtil.TileInfo(
                 TILE_CPU, R.string.title_toggle_cpu, "com.android.systemui:drawable/ic_settings_performance"));
         TILES.put(TILE_WEATHER, new TileViewUtil.TileInfo(
                 TILE_WEATHER, R.string.title_toggle_weather, "com.android.systemui:drawable/weather_47"));
         TILES.put(TILE_NOTIF, new TileViewUtil.TileInfo(
                 TILE_NOTIF, R.string.title_toggle_notif, "com.android.systemui:drawable/ic_qs_notif_enable"));
         TILES.put(TILE_PROFILE, new TileViewUtil.TileInfo(
                 TILE_PROFILE, R.string.title_toggle_profile, "com.android.systemui:drawable/ic_qs_profiles"));
         TILES.put(TILE_POWER, new TileViewUtil.TileInfo(
                TILE_POWER, R.string.title_toggle_power, "com.android.systemui:drawable/ic_qs_profiles"));
         TILES.put(TILE_ALARM, new TileViewUtil.TileInfo(
                 TILE_ALARM, R.string.title_toggle_alarm, "com.android.systemui:drawable/ic_qs_alarm_on"));
     }
 
     private static final String TILE_DELIMITER = "|";
     private static final String TILES_DEFAULT = TILE_USER
             + TILE_DELIMITER + TILE_CPU
             + TILE_DELIMITER + TILE_WEATHER
             + TILE_DELIMITER + TILE_WIFI
             + TILE_DELIMITER + TILE_BATTERY
             + TILE_DELIMITER + TILE_MOBILEDATA
             + TILE_DELIMITER + TILE_SETTING
             + TILE_DELIMITER + TILE_TIME
             + TILE_DELIMITER + TILE_NETWORKMODE
             + TILE_DELIMITER + TILE_BLUETOOTH
             + TILE_DELIMITER + TILE_BRIGHTNESS
             + TILE_DELIMITER + TILE_GPS
             + TILE_DELIMITER + TILE_SYNC
             + TILE_DELIMITER + TILE_RINGER
             + TILE_DELIMITER + TILE_AIRPLANE
             + TILE_DELIMITER + TILE_AUTOROTATE
             + TILE_DELIMITER + TILE_SCREENSHOT
             + TILE_DELIMITER + TILE_SCREENTIME
             + TILE_DELIMITER + TILE_SLEEP
             + TILE_DELIMITER + TILE_LOCKSCREEN
             + TILE_DELIMITER + TILE_WIFIAP;
 
     public static String getCurrentTiles(Context context) {
         String tiles = Settings.System.getString(context.getContentResolver(), Settings.System.QUICK_SETTINGS_TILES);
         if (tiles == null) {
             tiles = TILES_DEFAULT;
             // And the flashlight too if available
             if (context.getResources().getBoolean(R.bool.has_led_flash)) {
                 tiles += TILE_DELIMITER + TILE_TORCH;
             }
         }
         return tiles;
     }
 
     public static void saveCurrentTiles(Context context, String tiles) {
         Settings.System.putString(context.getContentResolver(),
                 Settings.System.QUICK_SETTINGS_TILES, tiles);
     }
 
     public static void resetCurrentTiles(Context context) {
         Settings.System.putString(context.getContentResolver(),
                 Settings.System.QUICK_SETTINGS_TILES, TILES_DEFAULT);
     }
 
     public static String mergeInNewTileString(String oldString, String newString) {
         ArrayList<String> oldList = getTileListFromString(oldString);
         ArrayList<String> newList = getTileListFromString(newString);
         ArrayList<String> mergedList = new ArrayList<String>();
 
         // add any items from oldlist that are in new list
         for(String tile : oldList) {
             if(newList.contains(tile)) {
                 mergedList.add(tile);
             }
         }
 
         // append anything in newlist that isn't already in the merged list to the end of the list
         for(String tile : newList) {
             if(!mergedList.contains(tile)) {
                 mergedList.add(tile);
             }
         }
 
         // return merged list
         return getTileStringFromList(mergedList);
     }
 
     public static ArrayList<String> getTileListFromString(String tiles) {
         return new ArrayList<String>(Arrays.asList(tiles.split("\\|")));
     }
 
     public static String getTileStringFromList(ArrayList<String> tiles) {
         if(tiles == null || tiles.size() <= 0) {
             return "";
         } else {
             String s = tiles.get(0);
             for(int i = 1; i < tiles.size(); i++) {
                 s += TILE_DELIMITER + tiles.get(i);
             }
             return s;
         }
     }
 
     public static class TileInfo {
         private String mId;
         private int mTitleResId;
         private String mIcon;
 
         public TileInfo(String id, int titleResId, String icon) {
             mId = id;
             mTitleResId = titleResId;
             mIcon = icon;
         }
 
         public String getId() { return mId; }
         public int getTitleResId() { return mTitleResId; }
         public String getIcon() { return mIcon; }
     }
 }

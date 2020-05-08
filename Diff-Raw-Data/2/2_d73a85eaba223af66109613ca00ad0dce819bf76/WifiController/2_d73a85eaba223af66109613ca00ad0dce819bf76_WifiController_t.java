 /*
  * Copyright 2012 akaiosorani(akaiosorani@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package jp.srgtndr.akaiosorani.android.cartain.controller;
 
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.wifi.WifiManager;
 
 public class WifiController {
 
     private static WifiManager getManager(Context context)
     {
         WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
         return manager;
     }
 
     public static boolean setEnabled(Context context, boolean enabled) 
     {
         return getManager(context).setWifiEnabled(enabled);
     }
 
     public static boolean isEnabled(Context context)
     {
         return getManager(context).isWifiEnabled();
     }
 
     public static int getCurrentState(Context context) {
         return getManager(context).getWifiState();
     }
 
     public static boolean isWifiDevice(Context context)
     {
         return getManager(context) != null;
     }
 
     public static IntentFilter getFilter()
     {
        return new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
     }
 }

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
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.content.Context;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 
 public class DataTrafficController {
 
     private static int TARGET_DEVICE_TYPE = ConnectivityManager.TYPE_MOBILE;
     private static String TARGET_DEVICE_TYPE_NAME = "mobile";
 
     private static String ENABLE_METHOD_NAME = "enableDataConnectivity";
     private static String DISABLE_METHOD_NAME = "disableDataConnectivity";
 
     private static ConnectivityManager getManager(Context context)
     {
         ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
         return manager;
     }
 
     private static NetworkInfo getNetworkInfo(Context context, int deviceType, String deviceTypeName)
     {
         ConnectivityManager manager = getManager(context);
         NetworkInfo[] networks = manager.getAllNetworkInfo();
         for(NetworkInfo info : networks)
         {
             if (info.getType() == deviceType && info.getTypeName().equals(deviceTypeName)) {
                 return info;
             }
         }
         return null;
     }
 
     public static boolean isConnected(Context context) {
         NetworkInfo info = getNetworkInfo(context, TARGET_DEVICE_TYPE, TARGET_DEVICE_TYPE_NAME);
         return (info != null) && info.isAvailable() && info.isConnected();
     }
 
     public static boolean isDeviceAvailable(Context context)
     {
         NetworkInfo info = getNetworkInfo(context, TARGET_DEVICE_TYPE, TARGET_DEVICE_TYPE_NAME);
//        return (info != null) && info.isAvailable();
        return (info != null);
     }
 
     public static NetworkInfo.State getState(Context context)
     {
         NetworkInfo info = getNetworkInfo(context, TARGET_DEVICE_TYPE, TARGET_DEVICE_TYPE_NAME);
         return (info != null) ? info.getState() : NetworkInfo.State.UNKNOWN;
     }
 
     private static TelephonyManager getTelephoneyManager(Context context)
     {
         return (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
     }
     
     public static void setMobileEnabled(Context context, boolean enabled)
     {
         int api = Build.VERSION.SDK_INT;
         if (api >= 9)
         {
             setMobileEnabledGB(context, enabled);
         } else if(api == 8){
             setMobileEnabledFroyo(context, enabled);
         } else 
         {
             // not supported
         }
         
     }
     private static void setMobileEnabledFroyo(Context context, boolean enabled)
     {
         TelephonyManager manager = getTelephoneyManager(context);
         boolean current = (manager.getDataState() == TelephonyManager.DATA_CONNECTED);
         if (current == enabled)
         {
             return;
         }
 
         try {
             @SuppressWarnings("rawtypes")
             Class managerClass = Class.forName(manager.getClass().getName());
             Method getITelephonyMethod = managerClass.getDeclaredMethod("getITelephony");
             getITelephonyMethod.setAccessible(true);
             Object iTelephony = getITelephonyMethod.invoke(manager);
             @SuppressWarnings("rawtypes")
             Class iTelephonyClass = Class.forName(iTelephony.getClass().getName());
             String methodName = enabled ? ENABLE_METHOD_NAME : DISABLE_METHOD_NAME;
             Method connectMethod = iTelephonyClass.getDeclaredMethod(methodName);
             connectMethod.setAccessible(true);
             connectMethod.invoke(iTelephony);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
     }
     
     private static void setMobileEnabledGB(Context context, boolean enabled)
     {
         ConnectivityManager manager = getManager(context);
         try {
             Field serviceField = manager.getClass().getDeclaredField("mService");
             serviceField.setAccessible(true);
             Object iConnectivityManager = serviceField.get(manager);
             @SuppressWarnings("rawtypes")
             Class iConnectivityManagerClass = iConnectivityManager.getClass();
             Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
             setMobileDataEnabledMethod.setAccessible(true);
             setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (NoSuchFieldException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
     }
 
     public static IntentFilter getFilter()
     {
         return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
     }
 }

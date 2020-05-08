 /*
        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements.  See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership.  The ASF licenses this file
        to you under the Apache License, Version 2.0 (the
        "License"); you may not use this file except in compliance
        with the License.  You may obtain a copy of the License at
 
          http://www.apache.org/licenses/LICENSE-2.0
 
        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.
 */
 package org.apache.cordova.device;
 
 import java.util.TimeZone;
 
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CordovaInterface;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.util.XLog;
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.provider.Settings;
 import android.util.DisplayMetrics;
 
 public class Device extends CordovaPlugin {
     private static final String CLASS_NAME = Device.class.getSimpleName();
     public static final String TAG = "Device";
 
     public static String cordovaVersion = "dev";              // Cordova version
     public static String uuid;                                // Device UUID
 
     private static final String ANDROID_PLATFORM = "Android";
     private static final String AMAZON_PLATFORM = "amazon-fireos";
     private static final String AMAZON_DEVICE = "Amazon";
 
     /**
      * Constructor.
      */
     public Device() {
     }
 
     /**
      * Sets the context of the Command. This can then be used to do things like
      * get file paths associated with the Activity.
      *
      * @param cordova The context of the main Activity.
      * @param webView The CordovaWebView Cordova is running in.
      */
     public void initialize(CordovaInterface cordova, CordovaWebView webView) {
         super.initialize(cordova, webView);
         Device.uuid = getUuid();
     }
 
     /**
      * Executes the request and returns PluginResult.
      *
      * @param action            The action to execute.
      * @param args              JSONArry of arguments for the plugin.
      * @param callbackContext   The callback id used when calling back into JavaScript.
      * @return                  True if the action was valid, false if not.
      */
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
         if (action.equals("getDeviceInfo")) {
             JSONObject r = new JSONObject();
             try {
                 r.put("uuid", Device.uuid);
                 r.put("version", this.getOSVersion());
                r.put("platform", getPlatform());
                 r.put("cordova", Device.cordovaVersion);
                 r.put("model", this.getModel());
                 r.put("name", android.os.Build.PRODUCT);
                 r.put("xFaceVersion", XConfiguration.getInstance().readEngineVersion());
                 PackageInfo info = this.cordova.getActivity().getPackageManager().getPackageInfo(this.cordova.getActivity().getPackageName(), 0);
                 r.put("productVersion", info.versionName);
                 r.put("width", getWidthPixels(this.cordova.getActivity()));
                 r.put("height", getHeightPixels(this.cordova.getActivity()));
                 callbackContext.success(r);
             } catch (NameNotFoundException e) {
                 XLog.e(CLASS_NAME, e.getMessage(), e);
             }
         }
         else {
             return false;
         }
         return true;
     }
 
     //--------------------------------------------------------------------------
     // LOCAL METHODS
     //--------------------------------------------------------------------------
 
     /**
      * Get the OS name.
     *
      * @return
      */
     public String getPlatform() {
         String platform;
         if (isAmazonDevice()) {
             platform = AMAZON_PLATFORM;
         } else {
             platform = ANDROID_PLATFORM;
         }
         return platform;
     }
 
     /**
      * Get the device's Universally Unique Identifier (UUID).
      *
      * @return
      */
     public String getUuid() {
         String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
         return uuid;
     }
 
     /**
      * Get the Cordova version.
      *
      * @return
      */
     public String getCordovaVersion() {
         return Device.cordovaVersion;
     }
 
     public String getModel() {
         String model = android.os.Build.MODEL;
         return model;
     }
 
     public String getProductName() {
         String productname = android.os.Build.PRODUCT;
         return productname;
     }
 
     /**
      * Get the OS version.
      *
      * @return
      */
     public String getOSVersion() {
         String osversion = android.os.Build.VERSION.RELEASE;
         return osversion;
     }
 
     public String getSDKVersion() {
         @SuppressWarnings("deprecation")
         String sdkversion = android.os.Build.VERSION.SDK;
         return sdkversion;
     }
 
     public String getTimeZoneID() {
         TimeZone tz = TimeZone.getDefault();
         return (tz.getID());
     }
 
     /**
      * Get Device DisplayMetrics
      *
      * @param context
      * @return
      */
     private DisplayMetrics getDisplayMetrics(Context context) {
         return context.getApplicationContext().getResources().getDisplayMetrics();
     }
 
     /**
      * Get Device Screen Width
      *
      * @param context
      * @return
      */
     private int getWidthPixels(Context context) {
         return getDisplayMetrics(context).widthPixels;
     }
 
     /**
      * Get Device Screen Height
      *
      * @param context
      * @return
      */
     private int getHeightPixels(Context context) {
         return getDisplayMetrics(context).heightPixels;
     }
     /**
      * Function to check if the device is manufactured by Amazon
     *
      * @return
      */
     public boolean isAmazonDevice() {
         if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
             return true;
         }
         return false;
     }
 
 }

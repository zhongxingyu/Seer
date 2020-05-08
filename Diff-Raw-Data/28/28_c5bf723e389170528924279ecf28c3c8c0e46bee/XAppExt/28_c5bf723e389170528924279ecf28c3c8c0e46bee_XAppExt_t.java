 /*
  This file was modified from or inspired by Apache Cordova.
 
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
  */
 
 package com.polyvi.xface.extension;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.PixelFormat;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.provider.Settings;
 import android.util.Base64;
 
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.plugin.api.XIWebContext;
 import com.polyvi.xface.util.XAppUtils;
 import com.polyvi.xface.util.XBase64;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XPathResolver;
 import com.polyvi.xface.util.XStringUtils;
 
 public class XAppExt extends XExtension {
     private static final String CLASS_NAME = "XAppExt";
     private static final String COMMAND_EXITAPP = "exitApp";
     private static final String COMMAND_OPEN_URL = "openUrl";
     private static final String COMMAND_INSTALL = "install";
     private static final String COMMAND_START_SYSTEM_COMPONENT = "startSystemComponent";
     private static final String COMMAND_SET_WIFI_SLEEP_POLICY = "setWifiSleepPolicy";
     private static final String COMMAND_LOAD_URL = "loadUrl";
     private static final String COMMAND_BACK_HISTORY = "backHistory";
     private static final String COMMAND_CLEAR_HISTORY = "clearHistory";
     private static final String COMMAND_CLEAR_CACHE = "clearCache";
     private static final String COMMAND_START_NATIVE_APP = "startNativeApp";
     private static final String COMMAND_IS_NATIVE_APP_INSTALLED = "isNativeAppInstalled";
     private static final String COMMAND_TEL_LINK_ENABLE = "telLinkEnable";
     private static final String COMMAND_QUERY_INSTALLED_NATIVEAPP = "queryInstalledNativeApp";
     private static final String COMMAND_UNINSTALL_NATIVEAPP = "uninstallNativeApp";
 
     private static final String APK_TYPE = "application/vnd.android.package-archive";
 
     /** 定义一些tag常量 */
     private static final String TAG_APP_NAME = "name";
     private static final String TAG_APP_ID = "id";
     private static final String TAG_APP_ICON = "icon";
 
     private BroadcastReceiver mInstallerReceiver;
     private String uninstallPackageName;
 
     /** 要启动的component名字 */
     public enum SysComponent {
         VPN_CONFIG, // vpn设置界面
         WIRELESS_CONFIG, // 网络设置界面
         GPS_CONFIG, // gps设置界面
         UNKNOWN // 未知的组件名
     };
 
     /** wifi休眠3种方式 */
     private static final String WIFI_SLEEP_POLICY_DEFAULT = "wifi_sleep_policy_default";
     private static final String WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED = "wifi_sleep_policy_never_while_plugged";
     private static final String WIFI_SLEEP_POLICY_NEVER = "wifi_sleep_policy_never";
 
     @Override
     public void sendAsyncResult(String result) {
     }
 
     @Override
     public boolean isAsync(String action) {
         if (action.equals(COMMAND_START_SYSTEM_COMPONENT)
                 || action.equals(COMMAND_OPEN_URL)) {
             return false;
         }
         return true;
     }
 
     @Override
     public XExtensionResult exec(String action, JSONArray args,
             XCallbackContext callbackCtx) throws JSONException {
         XExtensionResult.Status status = XExtensionResult.Status.OK;
         String result = "";
         try {
             if (action.equals(COMMAND_EXITAPP)) {
                 exitApp();
             } else if (COMMAND_OPEN_URL.equals(action)) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 String url = args.getString(0);
                 Uri uri = getUrlFromPath(mWebContext, url);
                 if (null == uri) {
                     status = XExtensionResult.Status.ERROR;
                     result = "file not exist";
                     return new XExtensionResult(status, result);
                 }
                 setDirPermisionUntilWorkspace(mWebContext, uri);
                 setIntentByUri(intent, uri);
                 getContext().startActivity(intent);
             } else if (COMMAND_INSTALL.equals(action)) {
                 XPathResolver pathResolver = new XPathResolver(
                         args.getString(0), mWebContext.getWorkSpace());
                 install(pathResolver.resolve(), callbackCtx);
                 return new XExtensionResult(XExtensionResult.Status.NO_RESULT);
             } else if (COMMAND_START_SYSTEM_COMPONENT.equals(action)) {
                 if (!startSystemComponent(args.getInt(0))) {
                     status = XExtensionResult.Status.ERROR;
                     result = "Unsupported component:: " + args.getString(0);
                 }
             } else if (COMMAND_SET_WIFI_SLEEP_POLICY.equals(action)) {
                 if (!setWifiSleepPolicy(args.getString(0))) {
                     status = XExtensionResult.Status.ERROR;
                     result = "set wifi sleep policy error";
                 }
             } else if (COMMAND_LOAD_URL.equals(action)) {
                 boolean openExternal = Boolean.valueOf(args.getString(1));
                 boolean clearHistory = Boolean.valueOf(args.getString(2));
                 loadUrl(args.getString(0), openExternal, clearHistory,
                         mWebContext);
             } else if (COMMAND_BACK_HISTORY.equals(action)) {
                 backHistory(mWebContext);
             } else if (COMMAND_CLEAR_HISTORY.equals(action)) {
                 clearHistory(mWebContext);
             } else if (COMMAND_CLEAR_CACHE.equalsIgnoreCase(action)) {
                 clearCache(mWebContext);
             } else if (COMMAND_START_NATIVE_APP.equalsIgnoreCase(action)) {
                 if (!XAppUtils.startNativeApp(mExtensionContext
                         .getSystemContext().getContext(), args.getString(0),
                         XConstant.TAG_APP_START_PARAMS, args.getString(1))) {
                     status = XExtensionResult.Status.ERROR;
                 }
             } else if (COMMAND_IS_NATIVE_APP_INSTALLED.equals(action)) {
                 boolean installResult = false;
                 if (isAppInstalled(args.getString(0))) {
                     installResult = true;
                 }
                 return new XExtensionResult(status, installResult);
             } else if (COMMAND_TEL_LINK_ENABLE.equals(action)) {
                 XConfiguration.getInstance().setTelLinkEnabled(
                         args.optBoolean(0, true));
             } else if (COMMAND_QUERY_INSTALLED_NATIVEAPP.equals(action)) {
                 return new XExtensionResult(status,
                         queryInstalledNativeApp(args.getString(0)));
             } else if (COMMAND_UNINSTALL_NATIVEAPP.equals(action)) {
                 uninstallNativeApp(args.getString(0), callbackCtx);
                 return new XExtensionResult(XExtensionResult.Status.NO_RESULT);
             } else {
                 status = XExtensionResult.Status.INVALID_ACTION;
                 result = "Unsupported Operation: " + action;
             }
             return new XExtensionResult(status, result);
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
             return new XExtensionResult(XExtensionResult.Status.ERROR);
         }
     }
 
     private void setIntentByUri(Intent intent, Uri uri) {
         if (!XConstant.FILE_SCHEME.contains(uri.getScheme())) {
             intent.setData(uri);
         } else {
             String mimeType = XFileUtils.getMIMEType(uri.toString());
             intent.setDataAndType(uri,
                     XStringUtils.isEmptyString(mimeType) ? "*/*" : mimeType);
         }
     }
 
     private void setDirPermisionUntilWorkspace(XIWebContext webContext, Uri uri) {
         // 设置播放文件的权限
         // 注意:需要设置到workspace这一层
         if (!XConstant.FILE_SCHEME.contains(uri.getScheme())) {
             return;
         }
         String filePath = uri.getPath();
         String workspace = new File(webContext.getWorkSpace())
                 .getAbsolutePath();
         File fileObj = new File(filePath);
         do {
             String path = fileObj.getAbsolutePath();
             // 设置文件权限
             XFileUtils.setPermission(XFileUtils.ALL_PERMISSION, path);
             fileObj = new File(fileObj.getParent());
         } while (!fileObj.getAbsolutePath().equals(workspace));
     }
 
     /**
      * 退出App
      */
     public void exitApp() {
         mExtensionContext.getSystemContext().finish();
     }
 
     /**
      * 安装apk
      *
      * @param path
      *            要安装的apk本地文件的路径
      */
     public void install(String path, XCallbackContext callbackCtx) {
         registerInstallerReceiver(callbackCtx);
         Intent intent = new Intent(Intent.ACTION_VIEW);
         // 修改文件的权限为其它用户可读, 否则系统apk安装程序无法安装
         XFileUtils.setPermission(XFileUtils.READABLE_BY_OTHER, path);
         intent.setDataAndType(Uri.fromFile(new File(path)), APK_TYPE);
         getContext().startActivity(intent);
     }
 
     /**
      * 开启一个系统componen
      *
      * @param componenName
      *            要启动的componen名字 return true：启动成功,false：启动失败。
      */
     private boolean startSystemComponent(int componentCode) {
         SysComponent componentName = SysComponent.UNKNOWN;
         try {
             componentName = SysComponent.values()[componentCode];
         } catch (ArrayIndexOutOfBoundsException e) {
             XLog.d(CLASS_NAME, "unkown component name!", e);
             return false;
         }
         Intent intent = null;
         boolean success = false;
         switch (componentName) {
         case VPN_CONFIG:
             intent = new Intent("android.net.vpn.SETTINGS");
             break;
         case WIRELESS_CONFIG:
             intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
             break;
         case GPS_CONFIG:
             intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
             break;
         default:
             XLog.d(CLASS_NAME, "unkown component name!");
             break;
         }
         if (null != intent) {
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             getContext().startActivity(intent);
             success = true;
         }
         return success;
     }
 
     /**
      * 获取给定path的uri
      *
      * @param app
      *            应用
      * @param path
      *            url路径 return Uri 成功:uri,失败:null。
      */
     private Uri getUrlFromPath(XIWebContext webContext, String path) {
         XPathResolver fileResolver = new XPathResolver(path,
                 webContext.getWorkSpace());
         String absPath = fileResolver.resolve();
         Uri uri = null;
         if (path.startsWith(XConstant.HTTP_SCHEME)
                 || path.startsWith(XConstant.HTTPS_SCHEME)) {
             uri = Uri.parse(absPath);
         } else {
             File file = new File(absPath);
             if (file.exists()) {
                 uri = Uri.fromFile(file);
             }
         }
         return uri;
     }
 
     /**
      * 设置wifi休眠策略
      *
      * @param wifiSleepPolicy
      *            休眠策略
      * @return true:设置成功,false:设置失败
      */
     private boolean setWifiSleepPolicy(String wifiSleepPolicy) {
         if (WIFI_SLEEP_POLICY_DEFAULT.equals(wifiSleepPolicy)) {
             return Settings.System.putInt(getContext().getContentResolver(),
                     Settings.System.WIFI_SLEEP_POLICY,
                     Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
         } else if (WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED
                 .equals(wifiSleepPolicy)) {
             return Settings.System.putInt(getContext().getContentResolver(),
                     Settings.System.WIFI_SLEEP_POLICY,
                     Settings.System.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED);
         } else if (WIFI_SLEEP_POLICY_NEVER.equals(wifiSleepPolicy)) {
             return Settings.System.putInt(getContext().getContentResolver(),
                     Settings.System.WIFI_SLEEP_POLICY,
                     Settings.System.WIFI_SLEEP_POLICY_NEVER);
         } else {
             return false;
         }
     }
 
     /**
      * 加载的url.
      *
      * @param url
      *            要加载的URL
      * @param props
      *            要传给loadUrl的配置属性 (i.e. openExternal,clearHistory)
      * @param webContext
      *            具体的应用
      * @throws JSONException
      */
     public void loadUrl(String url, boolean openExternal, boolean clearHistory,
             XIWebContext webContext) throws JSONException {
         XLog.d("App", "App.loadUrl(" + url + ",openExternal:" + openExternal
                 + ",clearHistory:" + clearHistory + ")");
         webContext.getApplication().loadUrl(url, openExternal, clearHistory,
                 getContext());
     }
 
     /**
      * 返回到前一页面，函数功能就和android上的back按钮相同。
      *
      * @param webContext
      *            要加载的应用
      */
     public void backHistory(final XIWebContext webContext) {
         mExtensionContext.getSystemContext().runOnUiThread(new Runnable() {
             public void run() {
                 webContext.getApplication().backHistory();
             }
         });
     }
 
     /**
      * 清理页面浏览历史.
      */
     public void clearHistory(XIWebContext webContext) {
         webContext.getApplication().clearHistory();
     }
 
     /**
      * 清理资源缓存.
      */
     public void clearCache(XIWebContext webContext) {
        webContext.getApplication().clearCache(true);
     }
 
     /**
      * 判断程序是否安装成功
      *
      * @param appId
      *            程序id号码
      * @return true：系统已经安装，程序未安装
      */
     public boolean isAppInstalled(String appId) {
         if (null == appId) {
             return false;
         }
 
         try {
             getContext().getPackageManager().getApplicationInfo(appId,
                     PackageManager.GET_UNINSTALLED_PACKAGES);
             return true;
         } catch (NameNotFoundException e) {
             return false;
         }
     }
 
     /**
      * 获取安装的程序列表包含系统应用和用户安装的应用
      *
      * @param type
      *            应用类型。"0":代表所有应用，"1"：代表用户安装的应用,"2":代表系统应用
      * @return 包含应用信息的应用列表
      * @throws JSONException
      */
     public JSONArray queryInstalledNativeApp(String type) throws JSONException {
         JSONArray appArray = new JSONArray();
         int appType = Integer.valueOf(type);
         PackageManager pm = getContext().getPackageManager();
         List<PackageInfo> packages = pm.getInstalledPackages(0);
         for (PackageInfo packageInfo : packages) {
             if (getContext().getPackageName().equals(
                     packageInfo.applicationInfo.packageName))
                 continue;
             switch (appType) {
             case 0:
                 JSONObject obj = new JSONObject();
                 obj.put(TAG_APP_NAME,
                         pm.getApplicationLabel(packageInfo.applicationInfo)
                                 .toString());
                 obj.put(TAG_APP_ID, packageInfo.applicationInfo.packageName);
                 obj.put(TAG_APP_ICON, drawableToBase64(pm
                         .getApplicationIcon(packageInfo.applicationInfo)));
                 appArray.put(obj);
                 break;
             case 1:
                 if ((packageInfo.applicationInfo.flags & 0x1) != 0)
                     continue;
                 JSONObject userAppObj = new JSONObject();
                 userAppObj.put(TAG_APP_NAME,
                         pm.getApplicationLabel(packageInfo.applicationInfo)
                                 .toString());
                 userAppObj.put(TAG_APP_ID,
                         packageInfo.applicationInfo.packageName);
                 userAppObj.put(TAG_APP_ICON, drawableToBase64(pm
                         .getApplicationIcon(packageInfo.applicationInfo)));
                 appArray.put(userAppObj);
 
                 break;
             case 2:
                 if ((packageInfo.applicationInfo.flags & 0x1) == 0)
                     continue;
                 JSONObject sysAppObj = new JSONObject();
                 sysAppObj.put(TAG_APP_NAME,
                         pm.getApplicationLabel(packageInfo.applicationInfo)
                                 .toString());
                 sysAppObj.put(TAG_APP_ID,
                         packageInfo.applicationInfo.packageName);
                 sysAppObj.put(TAG_APP_ICON, drawableToBase64(pm
                         .getApplicationIcon(packageInfo.applicationInfo)));
                 appArray.put(sysAppObj);
             }
         }
         return appArray;
     }
 
     /**
      * 卸载native应用
      *
      * @param appId
      *            应用的包名
      */
     public void uninstallNativeApp(String appId, XCallbackContext callbackCtx) {
         registerInstallerReceiver(callbackCtx);
         uninstallPackageName = appId;
         Uri packageURI = Uri.parse("package:" + appId);
         Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
         getContext().startActivity(uninstallIntent);
     }
 
     private String drawableToBase64(Drawable drawable) {
         int width = drawable.getIntrinsicWidth();
         int height = drawable.getIntrinsicHeight();
         Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                 .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                 : Bitmap.Config.RGB_565);
         Canvas canvas = new Canvas(bitmap);
         drawable.setBounds(0, 0, width, height);
         drawable.draw(canvas);
         String result = null;
         ByteArrayOutputStream baos = null;
         try {
             if (bitmap != null) {
                 baos = new ByteArrayOutputStream();
                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                 baos.flush();
                 baos.close();
                 byte[] bitmapBytes = baos.toByteArray();
                 result = XBase64.encodeToString(bitmapBytes, Base64.DEFAULT);
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (baos != null) {
                     baos.flush();
                     baos.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return result;
     }
 
     private void registerInstallerReceiver(final XCallbackContext callbackCtx) {
         if (null == mInstallerReceiver) {
             mInstallerReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     if (intent.getAction().equals(
                             "android.intent.action.PACKAGE_REMOVED")
                             && (uninstallPackageName.equals(intent
                                     .getDataString().substring(8)))) {
                         XExtensionResult result = new XExtensionResult(
                                 XExtensionResult.Status.OK);
                         callbackCtx.sendExtensionResult(result);
                     }
                     if (intent.getAction().equals(
                             "android.intent.action.PACKAGE_ADDED")) {
                         XExtensionResult result = new XExtensionResult(
                                 XExtensionResult.Status.OK);
                         callbackCtx.sendExtensionResult(result);
                     }
                 }
             };
             IntentFilter filter = new IntentFilter();
             filter.addAction(Intent.ACTION_PACKAGE_ADDED);
             filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
             filter.addDataScheme("package");
             getContext().registerReceiver(mInstallerReceiver, filter);
         }
     }
 
     @Override
     public void destroy() {
         getContext().unregisterReceiver(mInstallerReceiver);
     }
 }

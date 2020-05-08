 /***************************************************************************
  * Copyright 2012 by SONR
  *
  **************************************************************************/
 
 package com.sonrlabs.test.sonr;
 
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.os.Build;
 import com.parse.Parse;
 import com.parse.ParseObject;
 
 import com.sonrlabs.prod.sonr.R;
 
 /**
  *  TODO: replace this junk with javadoc for this class.
  */
 public class SonrAppInformationLogger {
 
    public String getDeviceName() {
       String manufacturer = Build.MANUFACTURER;
       String model = Build.MODEL;
       if (model.startsWith(manufacturer)) {
         return capitalize(model);
       } else {
         return capitalize(manufacturer) + " " + model;
       }
     }
 
 
     private String capitalize(String s) {
       if (s == null || s.length() == 0) {
         return "";
       }
       char first = s.charAt(0);
       if (Character.isUpperCase(first)) {
         return s;
       } else {
         return Character.toUpperCase(first) + s.substring(1);
       }
     } 
    
    private void setApplicationVersion(Context context,ParseObject appInfo)
    {
       PackageInfo info;
       try {
          info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
          //StringBuffer buf = new StringBuffer();
          appInfo.put("AppVersion",info.versionCode);
          appInfo.put("AppVersionName",info.versionName);
          //appInfo.put("Permissions",info.permissions.toString());
          //return(buf.toString());
          
       } catch (NameNotFoundException e) {
          // TODO Auto-generated catch block
          //throw new RuntimeException(e);
          return;
       }
    }
    
    private void setSystemVersion(ParseObject appInfo)
    {
       
     
       appInfo.put("ReleaseVersion",Build.VERSION.RELEASE);
       appInfo.put("IncrementalVersion", Build.VERSION.INCREMENTAL);
       appInfo.put("CodeName",Build.VERSION.CODENAME);
       appInfo.put("SDK",Build.VERSION.SDK);
       appInfo.put("Board",Build.BOARD);
       appInfo.put("Brand",Build.BRAND);
       appInfo.put("Device",Build.DEVICE);
       appInfo.put("FingerPrint",Build.FINGERPRINT);
       appInfo.put("Host",Build.HOST);
       appInfo.put("ID",Build.ID);
       appInfo.put("DeviceName", getDeviceName());
       /*
       StringBuffer buf = new StringBuffer();
 
       buf.append("ReleaseVersion {"+Build.VERSION.RELEASE+"}");
       buf.append("\\nIncrementalVersion {"+Build.VERSION.INCREMENTAL+"}");
       buf.append("\\nSDKVersion {"+Build.VERSION.SDK+"}");
       buf.append("\\nBoard {"+Build.BOARD+"}");
       buf.append("\\nBrand {"+Build.BRAND+"}");
       buf.append("\\nDevice {"+Build.DEVICE+"}");
       buf.append("\\nFingerPrint {"+Build.FINGERPRINT+"}");
       buf.append("\\nHost {"+Build.HOST+"}");
       buf.append("\\nID {"+Build.ID+"}");
 
       return(buf.toString());
       */
    }
    
    private final void parseInit(Context context, String className, String errorString) {
       Resources resources = context.getResources();
       String appKey = resources.getString(R.string.PARSE_APP_KEY);
       String clientKey = resources.getString(R.string.PARSE_CLIENT_KEY);
      Parse.initialize(context, appKey, clientKey); 
       ParseObject appInfo = new ParseObject(className);
       setSystemVersion(appInfo);
       setApplicationVersion(context,appInfo);
       if (errorString != null) {
          appInfo.put("ErrorCode", errorString);
       }
       appInfo.saveInBackground();
    }
    
    public void uploadAppInformation(Context context) {
       parseInit(context, "SONRAppDiagnostics", null);
    }
    
    public void uploadErrorAppInformation(Context context) {
       parseInit(context, "SONRCouldNotConnectDiagnostics", null);
    }
    
    public void uploadErrorAppInformationWithErrorString(Context context,String errorString) {
       parseInit(context, "SONRCouldNotConnectDiagnostics", errorString);
    }
 }

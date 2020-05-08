 package com.darkprograms.gaio.device;
 
 import com.darkprograms.gaio.adb.AdbManager;
 import com.darkprograms.gaio.util.Constants;
 
 /**
  * Created with IntelliJ IDEA.
  * User: theshadow
  * Date: 12/9/12
  * Time: 4:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DeviceManager {
 
     private static DeviceManager instance;
 
     private DeviceManager() {
 
     }
 
     public static DeviceManager getInstance() {
         if (instance == null) {
             instance = new DeviceManager();
         }
         return instance;
     }
 
 
     public boolean deviceHasRoot() {
        return !getAdbManager().executeAdbCommand("ls /system/xbin/su").toLowerCase().contains("no");
     }
 
     public boolean isDeviceConnected() {
         Process process = getAdbManager().executeAdb(Runtime.getRuntime(), "devices");
         int length = getAdbManager().getAdbResponse(process).split("\n").length;
 
         return length != 1;
     }
 
     public String getDeviceType() {
 
         return getProperty(Constants.RO_DEVICE_NAME);
     }
 
     public String getBuildNumber() {
         return getProperty(Constants.RO_DISPLAY_ID);
     }
 
     public String getSoftwareVersion() {
         return getProperty(Constants.RO_VERSION_INC).split("\\.")[0];
     }
 
     private String getProperty(String key) {
         String splitResponse[] = getAdbManager().executeAdbCommand(Constants.BUILD_PROP_COMMMAND).split("\n");
 
         for (int i = 0; i < splitResponse.length; i++) {
             if (splitResponse[i].contains(key)) {
                 return splitResponse[i].split("=")[1].trim();
             }
         }
         return null;
     }
 
 
     private AdbManager getAdbManager() {
         return AdbManager.getInstance();
     }
 
 }

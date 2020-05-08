 /*
 ** Copyright (C) 2011 The Liquid Settings Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License"); 
 ** you may not use this file except in compliance with the License. 
 ** You may obtain a copy of the License at 
 **
 **     http://www.apache.org/licenses/LICENSE-2.0 
 **
 ** Unless required by applicable law or agreed to in writing, software 
 ** distributed under the License is distributed on an "AS IS" BASIS, 
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 ** See the License for the specific language governing permissions and 
 ** limitations under the License.
 */
 
 package com.liquid.settings.utilities;
 
 import android.util.Log;
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public class RootHelper {
 
     private static final String TAG = "LGB*DEBUG*";
     private static final String SHOWBUILD_PATH = "/system/tmp/showbuild";
     private static final String PROP_EXISTS_CMD = "grep -q %s /system/build.prop";
     private static final String LOGCAT_ALIVE_PATH = "/system/etc/init.d/73-propmodder_logcat_alive";
     private static final String REMOUNT_CMD = "busybox mount -o %s,remount -t yaffs2 /dev/block/mtdblock1 /system";
     private static final String LOGCAT_ALIVE_SCRIPT = "#!/system/bin/sh\nBB=/system/xbin/busybox\nLOGCAT=$(BB grep -o logcat.alive /system/build.prop)\nif BB [ -n $LOGCAT ]\nthen\nrm -f /dev/log/main\nelse\ntouch /dev/log/main\nfi";
     
     public static boolean runRootCommand(String command) {
         Log.d(TAG, "runRootCommand started");
         Log.d(TAG, "Attempting to run: " + command);
         Process process = null;
         DataOutputStream os = null;
         try {
             Log.d(TAG, "attempt to get root");
             process = Runtime.getRuntime().exec("su");
             os = new DataOutputStream(new BufferedOutputStream(process.getOutputStream()));
             os.writeBytes(command + "\n");
             os.writeBytes("exit\n");
             os.flush();
             return process.waitFor() == 0;
         } catch (IOException e) {
             Log.e(TAG, "IOException while flushing stream:", e);
             return false;
         } catch (InterruptedException e) {
             Log.e(TAG, "InterruptedException while executing process:", e);
             return false;
         } finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (IOException e) {
                     Log.e(TAG, "IOException while closing stream:", e);
                 }
             }
             if (process != null) {
                 process.destroy();
             }
         }
     }
 
     public static boolean propExists(String prop) {
         Log.d(TAG, "Checking if prop " + prop + " exists in /system/build.prop");
         return runRootCommand(String.format(PROP_EXISTS_CMD, prop));
     }
 
     public static boolean backupBuildProp() {
         Log.d(TAG, "Backing up build.prop to /system/tmp/pm_build.prop");
         return runRootCommand("cp /system/build.prop /system/tmp/pm_build.prop");
     }
     
     public static boolean restoreBuildProp() {
         Log.d(TAG, "Restoring build.prop from /system/tmp/pm_build.prop");
         return runRootCommand("cp /system/tmp/pm_build.prop /system/build.prop");
     }
 
     public static boolean remountRW() {
         if (!runRootCommand(String.format(REMOUNT_CMD, "rw"))) {
             throw new RuntimeException("Could not remount /system rw");
         } else {
             return true;
         }
     }
 
     public static boolean remountRO() {
         Log.d(TAG, "Remounting /system ro");
         if (!runRootCommand(String.format(REMOUNT_CMD, "ro"))) {
             throw new RuntimeException("Could not remount /system ro");
         } else {
             return true;
         }
     }
 
     public static void updateShowBuild() {
         Log.d(TAG, "Setting up /system/tmp/showbuild");
         runRootCommand("cp /system/build.prop " + SHOWBUILD_PATH);
         runRootCommand("chmod 777 " + SHOWBUILD_PATH);
     }
 
     public static boolean killProp(String prop) {
         Log.d(TAG, String.format("User wants to disable %s", prop));
         return runRootCommand(prop);
     }
 
     public static boolean logcatAlive() {
         Log.d(TAG, "Installing script to control logcat persistance");
         runRootCommand(String.format("echo %s > %s", LOGCAT_ALIVE_SCRIPT, LOGCAT_ALIVE_PATH));
         return runRootCommand("chmod 777 " + LOGCAT_ALIVE_PATH);
     }
 
     public static boolean recovery() {
         Log.d(TAG, "rebooting into recovery");
        return runRootCommand(String.format("echo 1 > /data/.recovery_mode", mode)) && runRootCommand("reboot");
     }
 }

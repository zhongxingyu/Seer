 package com.darkprograms.gaio.recovery;
 
 import com.darkprograms.gaio.adb.AdbManager;
 import com.darkprograms.gaio.device.DeviceManager;
 import com.darkprograms.gaio.network.NetworkUtil;
 
 /**
  * Created with IntelliJ IDEA.
  * User: theshadow
  * Date: 12/30/12
  * Time: 11:52 AM
  * To change this template use File | Settings | File Templates.
  */
 public class RecoveryInstallThread implements Runnable {
 
     public void run() {
         RecoveryManager recoveryManager = RecoveryManager.getInstance();
 
         String recoveryName = recoveryManager.getRecoveryFileName(recoveryManager.getRecoveryIndex());
 
         NetworkUtil.getInstance().downloadFile(recoveryName);
        recoveryManager.setStatus("Downloading...");
 
         recoveryManager.setStatus("Pushing recovery to device...");
 
         AdbManager.getInstance().adbPush(DeviceManager.getInstance().getTmpDir() + "/gaio/" + recoveryName, "/sdcard/" + recoveryName);
 
         recoveryManager.setStatus("Erasing recovery partition...");
 
         AdbManager.getInstance().executeAdbCommandWaitFor("dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery");
 
         recoveryManager.setStatus("Installing " + recoveryName + "...");
 
         AdbManager.getInstance().executeAdbCommandWaitFor("dd if=/sdcard/" + recoveryName + "of=/dev/block/platform/msm_sdcc.1/by-name/recovery");
         AdbManager.getInstance().executeAdbCommandWaitFor("rm /sdcard/" + recoveryName);
 
         recoveryManager.setStatus("Complete!");
 
         recoveryManager.setComplete(true);
     }
 
 }

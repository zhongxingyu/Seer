 package com.darkprograms.gaio.restore;
 
 import com.darkprograms.gaio.adb.AdbManager;
 import com.darkprograms.gaio.device.DeviceManager;
 import com.darkprograms.gaio.network.NetworkUtil;
 import com.darkprograms.gaio.util.Constants;
 
 /**
  * Created with IntelliJ IDEA.
  * User: theshadow
  * Date: 12/30/12
  * Time: 11:52 AM
  * To change this template use File | Settings | File Templates.
  */
 public class RestoreInstallThread implements Runnable {
 
     public void run() {
         RestoreManager restoreManager = RestoreManager.getInstance();
 
         NetworkUtil.getInstance().downloadFile(Constants.BOOT_IMG);
        restoreManager.setStatus("Downloading...");
 
         restoreManager.setStatus("Pushing boot.img to device...");
 
         AdbManager.getInstance().adbPush(DeviceManager.getInstance().getTmpDir() + "/gaio/boot.img", "/sdcard/boot.img");
 
         restoreManager.setStatus("Erasing boot partition...");
 
         AdbManager.getInstance().executeAdbCommandWaitFor("dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot");
 
         restoreManager.setStatus("Installing unlocked boot.img");
 
         AdbManager.getInstance().executeAdbCommandWaitFor("dd if=/sdcard/boot.img of=/dev/block/platform/msm_sdcc.1/by-name/boot");
         AdbManager.getInstance().executeAdbCommandWaitFor("rm /sdcard/boot.img");
 
         restoreManager.setStatus("Complete!");
 
         restoreManager.setComplete(true);
     }
 
 }

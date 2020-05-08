 package com.darkprograms.gaio.lgdriver;
 
 import com.darkprograms.gaio.network.NetworkUtil;
 
 /**
  * Created with IntelliJ IDEA.
  * User: theshadow
  * Date: 12/30/12
  * Time: 11:52 AM
  * To change this template use File | Settings | File Templates.
  */
 public class DriverInstallThread implements Runnable {
 
     public void run() {
         DriverManager driverManager = DriverManager.getInstance();
 
         String driverName = driverManager.getDriverName();
 
        driverManager.setStatus("Downloading...");
         NetworkUtil.getInstance().downloadFile(driverName);
 
         driverManager.setStatus("Running installer...");
 
         driverManager.installDriver(driverName);
 
         driverManager.setStatus("Complete!");
 
         driverManager.setComplete(true);
     }
 
 }

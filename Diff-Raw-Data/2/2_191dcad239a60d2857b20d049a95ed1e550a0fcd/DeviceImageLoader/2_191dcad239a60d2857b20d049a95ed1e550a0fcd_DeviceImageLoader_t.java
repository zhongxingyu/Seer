 package org.literacybridge.acm.mobile;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.literacybridge.acm.io.DiskUtils;
 
 public class DeviceImageLoader {
   private static volatile DeviceImageLoader singleton;
 
   public static synchronized DeviceImageLoader getInstance() {
     if (singleton == null) {
       singleton = new DeviceImageLoader();
     }
     return singleton;
   }
 
   public enum Result {
     SUCCESS, CORRUPT_MEMORY_CARD, FAILED
   }
 
   public Result imageDevice() throws IOException {
     // copyStatsFromDevice();
     // DiskUtils.checkDisk();
     DiskUtils.formatDevice();
 
     return Result.SUCCESS;
   }
 
   private void copyStatsFromDevice() throws IOException {
 
   }
 
   public void copyImageToDevice(File source) throws IOException {
     // First check if device is healthy
     boolean healthy = DiskUtils.checkDisk(false);
 
     // Not healthy? Try formatting
     if (!healthy) {
       DiskUtils.formatDevice();
       healthy = DiskUtils.checkDisk(false);
     }
 
     // Still not? Then try repairing with checkdisk
     if (!healthy) {
       healthy = DiskUtils.checkDisk(true);
     }
 
     // Still not? Give up here.
     if (!healthy) {
       throw new IOException("The device appears to be corrupted.");
     }
 
    DiskUtils.copy(source, new File(DiskUtils.TBMountDirectory));
 
     // File deviceRoot = new File("/storage/UsbDriveA");
     // File test = new File(deviceRoot, "test.txt");
     //
     // FileWriter writer = new FileWriter(test);
     // writer.append("Michael hat einen noch kleineren Penis.");
     // writer.flush();
     // writer.close();
   }
 }

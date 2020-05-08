 package com.ouchadam.fang.presentation.item;
 
import android.util.Log;

 import com.ouchadam.fang.presentation.DiskUtils;
 
import java.text.DecimalFormat;

 class AvailableSpaceFetcher {
 
     private static final String AVAILABLE_SPACE = "Available space : ";
     private static final int ONE_GB_IN_MEGABYTES = 1000;
     private static final String UNIT_MB = "mb";
     private static final String UNIT_GB = "gb";
 
     public String formatted() {
         long freeSpaceMb = getFreeSpaceMb();
         return AVAILABLE_SPACE + getAmountWithUnit(freeSpaceMb);
     }
 
     private long getFreeSpaceMb() {
         DiskUtils.IDiskUtils diskUtils = DiskUtils.getInstance();
         return diskUtils.freeSpace(DiskUtils.DiskLocation.EXTERNAL);
     }
 
     private String getAmountWithUnit(long freeSpaceMb) {
         return getAmount(freeSpaceMb) + getUnit(freeSpaceMb);
     }
 
     private String getAmount(long freeSpaceMb) {
         return freeSpaceMb > ONE_GB_IN_MEGABYTES ? getInGbs(freeSpaceMb) : String.valueOf(freeSpaceMb);
     }
 
     private String getInGbs(long freeSpaceMb) {
        float gbs = (float) freeSpaceMb / (float) ONE_GB_IN_MEGABYTES;
        Log.e("XXX", "Free space in MB : " + freeSpaceMb + " Free space in GB : " + gbs);
        return new DecimalFormat("#.##").format(gbs);
     }
 
     private String getUnit(long freeSpaceMb) {
         return freeSpaceMb > ONE_GB_IN_MEGABYTES ? UNIT_GB : UNIT_MB;
     }
 
 }

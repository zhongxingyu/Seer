 package com.cyanogenmod.cmparts.activities;
 
 import com.cyanogenmod.cmparts.R;
 import android.os.StatFs;
 import android.text.format.Formatter;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.preference.CheckBoxPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 public class App2extActivity extends PreferenceActivity {
 
     private Resources res;
 
     private static final String S2E_DIR = "/data/data/com.cyanogenmod.cmparts";
     private static final String SCRIPT_STATUS_DIR = S2E_DIR + "/status";
 
     private static final String PREF_PARTITIONS = "partitions";
     private static final String PREF_SPACE_DATA = "space_data";
     private static final String PREF_SPACE_EXT = "space_ext";
     private static final String PREF_SPACE_CACHE = "space_cache";
     private static final String PREF_APP = "app";
     private static final String PREF_APP_PRIVATE = "app-private";
     private static final String PREF_DALVIK_CACHE = "dalvik-cache";
     private static final String PREF_DOWNLOAD = "download";
 
 
     private Preference mPartitions;
     private Preference mSpace_data;
     private Preference mSpace_ext;
     private Preference mSpace_cache;
     private CheckBoxPreference mApp;
     private CheckBoxPreference mApp_private;
     private CheckBoxPreference mDalvik_cache;
     private CheckBoxPreference mDownload;
 
 
 
     private HashMap<String, Boolean> statuses;
     private HashMap<String, Integer> sizes;
 
     private String [] mTargets = {
         "app",
         "app-private",
         "dalvik-cache",
         "download"
     };
 
     private String [] mPartitionsPath = {
        "/data",
        "/sd-ext",
        "/cache"
     };
 
     private int[] spacesData;
     private int[] spacesExt;
     private int[] spacesCache;
 
     private boolean checkStatus(String target) {
         File status = new File(SCRIPT_STATUS_DIR, target);
         return status.exists();
     }
 
 
     private String getPath(String target) {
         if (target.equals("download")) return "/cache/" + target;
         else return "/data/" + target;
     }
 
     private void setupStatuses() {
         statuses = new HashMap<String, Boolean>();
         for (String target : mTargets) {
             statuses.put(target, checkStatus(target));
         }
     }
 
     private String getFreeSpace(String PartitionPath){
         String retstr;
         File extraPath = new File(PartitionPath);
         StatFs extraStat = new StatFs(extraPath.getPath());
         long eBlockSize = extraStat.getBlockSize();
 
         retstr = Formatter.formatFileSize(this, extraStat.getAvailableBlocks() * eBlockSize);
         return retstr;
     }
 
     private String getTotalSpace(String PartitionPath){
         String retstr;
         File extraPath = new File(PartitionPath);
         StatFs extraStat = new StatFs(extraPath.getPath());
         long eBlockSize = extraStat.getBlockSize();
         long eTotalBlocks = extraStat.getBlockCount();
 
         retstr = Formatter.formatFileSize(this, (eTotalBlocks * eBlockSize));
         return retstr;
     }
 
     private String getUsedSpace(String PartitionPath){
         String retstr;
         File extraPath = new File(PartitionPath);
         StatFs extraStat = new StatFs(extraPath.getPath());
         long eBlockSize = extraStat.getBlockSize();
         long eTotalBlocks = extraStat.getBlockCount();
 
         retstr = Formatter.formatFileSize(this, (eTotalBlocks * eBlockSize) - (extraStat.getAvailableBlocks() * eBlockSize));
         return retstr;
     }
     
     private  long dirSize(File dir) {
         long result = 0;
         File[] fileList = dir.listFiles();

         for(int i = 0; i < fileList.length; i++) {
             if(fileList[i].isDirectory()) {
                 result += dirSize(fileList [i]);
             } else {
                 result += fileList[i].length();
             }
         }
         return result; 
     }
 
     private String getFolderSize(String folderPath)
     {
         File extraPath = new File(folderPath);
         return  Formatter.formatFileSize(this, dirSize(extraPath));
     }
 
     @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             res = getResources();
             addPreferencesFromResource(R.xml.app_to_ext);
             PreferenceScreen prefSet = getPreferenceScreen();
 
             mPartitions = (Preference) prefSet.findPreference(PREF_PARTITIONS);
             mSpace_data = (Preference) prefSet.findPreference(PREF_SPACE_DATA);
             mSpace_ext = (Preference) prefSet.findPreference(PREF_SPACE_EXT);
             mSpace_cache = (Preference) prefSet.findPreference(PREF_SPACE_CACHE);
             mApp = (CheckBoxPreference) prefSet.findPreference(PREF_APP);
             mApp_private = (CheckBoxPreference) prefSet.findPreference(PREF_APP_PRIVATE);
             mDalvik_cache = (CheckBoxPreference) prefSet.findPreference(PREF_DALVIK_CACHE);
             mDownload = (CheckBoxPreference) prefSet.findPreference(PREF_DOWNLOAD);
 
             setupStatuses();
 
             mPartitions.setSummary(
                     "Data: " + getFreeSpace(mPartitionsPath[0])  + " " 
                     + "Ext: " +  getFreeSpace(mPartitionsPath[1]) + " "
                     + "Cache: " + getFreeSpace(mPartitionsPath[1])
                     );
 
             mSpace_data.setSummary(res.getString(R.string.size) + ": " +  
                         getTotalSpace(mPartitionsPath[0]) + "\n" +
                     res.getString(R.string.used) + ": " +  
                         getUsedSpace(mPartitionsPath[0]) + "\n" +
                     res.getString(R.string.free) + ": " +  
                         getFreeSpace(mPartitionsPath[0])
                         );
 
              mSpace_ext.setSummary(res.getString(R.string.size) + ": " +  
                         getTotalSpace(mPartitionsPath[1]) + "\n" +
                     res.getString(R.string.used) + ": " +  
                         getUsedSpace(mPartitionsPath[1]) + "\n" +
                     res.getString(R.string.free) + ": " +  
                         getFreeSpace(mPartitionsPath[1])
                         );
             
              mSpace_cache.setSummary(res.getString(R.string.size) + ": " +  
                         getTotalSpace(mPartitionsPath[2]) + "\n" +
                     res.getString(R.string.used) + ": " +  
                         getUsedSpace(mPartitionsPath[2]) + "\n" +
                     res.getString(R.string.free) + ": " +  
                         getFreeSpace(mPartitionsPath[2])
                         );
 
             
              mApp.setSummary(
                             res.getString(R.string.location) + ": /sd-ext/" + mTargets[0] + "\n" +
                             res.getString(R.string.size) + ": " + getFolderSize(getPath(mTargets[0]))
                             );
             
              mApp_private.setSummary(
                             res.getString(R.string.location) + ": /sd-ext/" + mTargets[1] + "\n" +
                             res.getString(R.string.size) + ": " + getFolderSize(getPath(mTargets[1]))
                             );
 
              mDalvik_cache.setSummary(
                             res.getString(R.string.location) + ": /cache/" + mTargets[2] + "\n" +
                             res.getString(R.string.size) + ": " + getFolderSize(getPath(mTargets[2]))
                             );
 
              mDownload.setSummary(
                             res.getString(R.string.location) + ": /cache/" + mTargets[3] + "\n" +
                             res.getString(R.string.size) + ": " + getFolderSize(getPath(mTargets[3]))
                             );
 
         }
 
 }

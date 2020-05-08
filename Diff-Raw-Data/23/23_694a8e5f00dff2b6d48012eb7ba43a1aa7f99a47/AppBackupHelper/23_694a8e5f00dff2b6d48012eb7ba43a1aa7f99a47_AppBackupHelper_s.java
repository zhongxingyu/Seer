 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.backup;
 
 import java.io.IOException;
 
 import android.app.backup.BackupAgentHelper;
 import android.app.backup.BackupDataInput;
 import android.app.backup.BackupDataOutput;
 import android.app.backup.FileBackupHelper;
 import android.app.backup.SharedPreferencesBackupHelper;
 import android.os.ParcelFileDescriptor;
 
 import com.zapta.apps.maniana.persistence.ModelPersistence;
 import com.zapta.apps.maniana.util.LogUtil;
 
 /**
  * Google Backup/Restore service agent. Must be listed in the Android Manifest file of the app.
  * 
  * @author Tal Dayan
  * 
  *         Note: to Force backup run the following after a data change:<br>
  *         adb shell bmgr run
  */
 public class AppBackupHelper extends BackupAgentHelper {
 
     /** Unique key of the data file backup helper */
     private static final String DATA_FILE_HELPER_KEY = "data_helper_key";
 
     /** Unique key of the preferences file backup helper */
     private static final String PREFERENCES_HELPER_KEY = "pref_helper_key";
 
     @Override
     public void onCreate() {
         LogUtil.info("Google backup agent: onCreate()");
 
         // Helper for serialized model data file.
         final FileBackupHelper dataFileHelper = new FileBackupHelper(this,
                 ModelPersistence.DATA_FILE_NAME);
         addHelper(DATA_FILE_HELPER_KEY, dataFileHelper);
 
         // Helper for shared preferences file
         final String defaultPrefFile = getPackageName() + "_preferences";
        // LogUtil.info("AppBackupHelper.onCreate(), Default pref file: %s, %s", defaultPrefFile,
        // this);
         final SharedPreferencesBackupHelper preferencesFileHelper = new SharedPreferencesBackupHelper(
                 getApplicationContext(), defaultPrefFile);
         addHelper(PREFERENCES_HELPER_KEY, preferencesFileHelper);
     }
 
     @Override
     public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
             ParcelFileDescriptor newState) throws IOException {
         LogUtil.info("Google backup agent: onBackup()");
         // TODO: do we need a lock also for the shared preferences?
         synchronized (ModelPersistence.sDataFileLock) {
             // Synchronized access to the data file to avoid corruption.
             super.onBackup(oldState, data, newState);
         }
     }
 
     @Override
     public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
             throws IOException {
         LogUtil.info("Google backup agent: onRestore()");
         // TODO: do we need a lock also for the shared preferences?
         synchronized (ModelPersistence.sDataFileLock) {
             // Synchronized access to the data file to avoid corruption.
             super.onRestore(data, appVersionCode, newState);
         }
     }
 }

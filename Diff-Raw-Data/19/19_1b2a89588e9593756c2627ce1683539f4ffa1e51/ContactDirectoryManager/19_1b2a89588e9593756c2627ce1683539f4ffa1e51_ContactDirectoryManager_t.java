 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 
 package com.android.providers.contacts;
 
 import com.android.providers.contacts.ContactsDatabaseHelper.Tables;
 import com.google.android.collect.Lists;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ProviderInfo;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Message;
 import android.os.Process;
 import android.os.SystemClock;
 import android.provider.ContactsContract.Directory;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Manages the contents of the {@link Directory} table.
  */
 public class ContactDirectoryManager extends HandlerThread {
 
     private static final String TAG = "ContactDirectoryManager";
 
     private static final int MESSAGE_SCAN_ALL_PROVIDERS = 0;
     private static final int MESSAGE_SCAN_PACKAGES_BY_UID = 1;
 
     private static final String PROPERTY_DIRECTORY_SCAN_COMPLETE = "directoryScanComplete";
     private static final String CONTACT_DIRECTORY_META_DATA = "android.content.ContactDirectory";
 
     public class DirectoryInfo {
         long id;
         String packageName;
         String authority;
         String accountName;
         String accountType;
         String displayName;
         int typeResourceId;
         int exportSupport = Directory.EXPORT_SUPPORT_NONE;
         int shortcutSupport = Directory.SHORTCUT_SUPPORT_NONE;
     }
 
     private final static class DirectoryQuery {
         public static final String[] PROJECTION = {
             Directory.ACCOUNT_NAME,
             Directory.ACCOUNT_TYPE,
             Directory.DISPLAY_NAME,
             Directory.TYPE_RESOURCE_ID,
             Directory.EXPORT_SUPPORT,
             Directory.SHORTCUT_SUPPORT,
         };
 
         public static final int ACCOUNT_NAME = 0;
         public static final int ACCOUNT_TYPE = 1;
         public static final int DISPLAY_NAME = 2;
         public static final int TYPE_RESOURCE_ID = 3;
         public static final int EXPORT_SUPPORT = 4;
         public static final int SHORTCUT_SUPPORT = 5;
     }
 
     private final ContactsProvider2 mContactsProvider;
     private Context mContext;
     private Handler mHandler;
 
     public ContactDirectoryManager(ContactsProvider2 contactsProvider) {
         super("DirectoryManager", Process.THREAD_PRIORITY_BACKGROUND);
         this.mContactsProvider = contactsProvider;
         this.mContext = contactsProvider.getContext();
     }
 
     /**
      * Launches an asynchronous scan of all packages.
      */
     @Override
     public void start() {
         super.start();
        scheduleScanAllPackages(false);
     }
 
     /**
      * Launches an asynchronous scan of all packages owned by the current calling UID.
      */
     public void scheduleDirectoryUpdateForCaller() {
         final int callingUid = Binder.getCallingUid();
         if (isAlive()) {
             Handler handler = getHandler();
             handler.sendMessage(handler.obtainMessage(MESSAGE_SCAN_PACKAGES_BY_UID, callingUid, 0));
         } else {
             scanPackagesByUid(callingUid);
         }
     }
 
     protected Handler getHandler() {
         if (mHandler == null) {
             mHandler = new Handler(getLooper()) {
                 @Override
                 public void handleMessage(Message msg) {
                     ContactDirectoryManager.this.handleMessage(msg);
                 }
             };
         }
         return mHandler;
     }
 
     protected void handleMessage(Message msg) {
         switch(msg.what) {
             case MESSAGE_SCAN_ALL_PROVIDERS:
                 scanAllPackagesIfNeeded();
                 break;
             case MESSAGE_SCAN_PACKAGES_BY_UID:
                 scanPackagesByUid(msg.arg1);
                 break;
         }
     }
 
     /**
      * Scans all packages owned by the specified calling UID looking for contact
      * directory providers.
      */
     public void scanPackagesByUid(int callingUid) {
         final PackageManager pm = mContext.getPackageManager();
         final String[] callerPackages = pm.getPackagesForUid(callingUid);
         if (callerPackages != null) {
             for (int i = 0; i < callerPackages.length; i++) {
                 onPackageChanged(callerPackages[i]);
             }
         }
     }
 
     /**
      * Scans all packages for directory content providers.
      */
     private void scanAllPackagesIfNeeded() {
         ContactsDatabaseHelper dbHelper =
                 (ContactsDatabaseHelper) mContactsProvider.getDatabaseHelper();
 
         String scanComplete = dbHelper.getProperty(PROPERTY_DIRECTORY_SCAN_COMPLETE, "0");
         if (!"0".equals(scanComplete)) {
             return;
         }
 
         long start = SystemClock.currentThreadTimeMillis();
         int count = scanAllPackages();
         dbHelper.setProperty(PROPERTY_DIRECTORY_SCAN_COMPLETE, "1");
         long end = SystemClock.currentThreadTimeMillis();
         Log.i(TAG, "Discovered " + count + " contact directories in " + (end - start) + "ms");
 
         // Announce the change to listeners of the contacts authority
         mContactsProvider.notifyChange(false);
     }
 
    public void scheduleScanAllPackages(boolean rescan) {
        if (rescan) {
            ContactsDatabaseHelper dbHelper =
                    (ContactsDatabaseHelper) mContactsProvider.getDatabaseHelper();
            dbHelper.setProperty(PROPERTY_DIRECTORY_SCAN_COMPLETE, "0");
        }
         getHandler().sendEmptyMessage(MESSAGE_SCAN_ALL_PROVIDERS);
     }
 
     /* Visible for testing */
     int scanAllPackages() {
         int count = 0;
         PackageManager pm = mContext.getPackageManager();
         List<PackageInfo> packages = pm.getInstalledPackages(
                 PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
         for (PackageInfo packageInfo : packages) {
             // Check all packages except the one containing ContactsProvider itself
             if (!packageInfo.packageName.equals(mContext.getPackageName())) {
                 count += updateDirectoriesForPackage(packageInfo, true);
             }
         }
         return count;
     }
 
     /**
      * Scans the specified package for content directories.  The package may have
      * already been removed, so packageName does not necessarily correspond to
      * an installed package.
      */
     public void onPackageChanged(String packageName) {
         PackageManager pm = mContext.getPackageManager();
         PackageInfo packageInfo = null;
 
         try {
             packageInfo = pm.getPackageInfo(packageName,
                     PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
         } catch (NameNotFoundException e) {
             // The package got removed
             packageInfo = new PackageInfo();
             packageInfo.packageName = packageName;
         }
 
         updateDirectoriesForPackage(packageInfo, false);
   }
 
     /**
      * Scans the specified package for content directories and updates the {@link Directory}
      * table accordingly.
      */
     private int updateDirectoriesForPackage(PackageInfo packageInfo, boolean initialScan) {
         ArrayList<DirectoryInfo> directories = Lists.newArrayList();
 
         ProviderInfo[] providers = packageInfo.providers;
         if (providers != null) {
             for (ProviderInfo provider : providers) {
                 Bundle metaData = provider.metaData;
                 if (metaData != null) {
                     Object trueFalse = metaData.get(CONTACT_DIRECTORY_META_DATA);
                     if (trueFalse != null && Boolean.TRUE.equals(trueFalse)) {
                         queryDirectoriesForAuthority(directories, provider);
                     }
                 }
             }
         }
 
         if (directories.size() == 0 && initialScan) {
             return 0;
         }
 
         SQLiteDatabase db = ((ContactsDatabaseHelper) mContactsProvider.getDatabaseHelper())
                 .getWritableDatabase();
         db.beginTransaction();
         try {
             updateDirectories(db, directories);
             // Clear out directories that are no longer present
             StringBuilder sb = new StringBuilder(Directory.PACKAGE_NAME + "=?");
             if (!directories.isEmpty()) {
                 sb.append(" AND " + Directory._ID + " NOT IN(");
                 for (DirectoryInfo info: directories) {
                     sb.append(info.id).append(",");
                 }
                 sb.setLength(sb.length() - 1);  // Remove the extra comma
                 sb.append(")");
             }
             db.delete(Tables.DIRECTORIES, sb.toString(), new String[] { packageInfo.packageName });
             db.setTransactionSuccessful();
         } finally {
             db.endTransaction();
         }
 
         mContactsProvider.resetDirectoryCache();
         return directories.size();
     }
 
     /**
      * Sends a {@link Directory#CONTENT_URI} request to a specific contact directory
      * provider and appends all discovered directories to the directoryInfo list.
      */
     protected void queryDirectoriesForAuthority(
             ArrayList<DirectoryInfo> directoryInfo, ProviderInfo provider) {
         Uri uri = new Uri.Builder().scheme("content")
                 .authority(provider.authority).appendPath("directories").build();
         Cursor cursor = null;
         try {
             cursor = mContext.getContentResolver().query(
                     uri, DirectoryQuery.PROJECTION, null, null, null);
             if (cursor == null) {
                 Log.i(TAG, providerDescription(provider) + " returned a NULL cursor.");
             } else {
                 while (cursor.moveToNext()) {
                     DirectoryInfo info = new DirectoryInfo();
                     info.packageName = provider.packageName;
                     info.authority = provider.authority;
                     info.accountName = cursor.getString(DirectoryQuery.ACCOUNT_NAME);
                     info.accountType = cursor.getString(DirectoryQuery.ACCOUNT_TYPE);
                     info.displayName = cursor.getString(DirectoryQuery.DISPLAY_NAME);
                     if (!cursor.isNull(DirectoryQuery.TYPE_RESOURCE_ID)) {
                         info.typeResourceId = cursor.getInt(DirectoryQuery.TYPE_RESOURCE_ID);
                     }
                     if (!cursor.isNull(DirectoryQuery.EXPORT_SUPPORT)) {
                         int exportSupport = cursor.getInt(DirectoryQuery.EXPORT_SUPPORT);
                         switch (exportSupport) {
                             case Directory.EXPORT_SUPPORT_NONE:
                             case Directory.EXPORT_SUPPORT_SAME_ACCOUNT_ONLY:
                             case Directory.EXPORT_SUPPORT_ANY_ACCOUNT:
                                 info.exportSupport = exportSupport;
                                 break;
                             default:
                                 Log.e(TAG, providerDescription(provider)
                                         + " - invalid export support flag: " + exportSupport);
                         }
                     }
                     if (!cursor.isNull(DirectoryQuery.SHORTCUT_SUPPORT)) {
                         int shortcutSupport = cursor.getInt(DirectoryQuery.SHORTCUT_SUPPORT);
                         switch (shortcutSupport) {
                             case Directory.SHORTCUT_SUPPORT_NONE:
                             case Directory.SHORTCUT_SUPPORT_DATA_ITEMS_ONLY:
                             case Directory.SHORTCUT_SUPPORT_FULL:
                                 info.shortcutSupport = shortcutSupport;
                                 break;
                             default:
                                 Log.e(TAG, providerDescription(provider)
                                         + " - invalid shortcut support flag: " + shortcutSupport);
                         }
                     }
                     directoryInfo.add(info);
                 }
             }
         } catch (Throwable t) {
             Log.e(TAG, providerDescription(provider) + " exception", t);
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }
     }
 
     /**
      * Updates the directories tables in the database to match the info received
      * from directory providers.
      */
     private void updateDirectories(SQLiteDatabase db, ArrayList<DirectoryInfo> directoryInfo) {
 
         // Insert or replace existing directories.
         // This happens so infrequently that we can use a less-then-optimal one-a-time approach
         for (DirectoryInfo info : directoryInfo) {
             ContentValues values = new ContentValues();
             values.put(Directory.PACKAGE_NAME, info.packageName);
             values.put(Directory.DIRECTORY_AUTHORITY, info.authority);
             values.put(Directory.ACCOUNT_NAME, info.accountName);
             values.put(Directory.ACCOUNT_TYPE, info.accountType);
             values.put(Directory.TYPE_RESOURCE_ID, info.typeResourceId);
             values.put(Directory.DISPLAY_NAME, info.displayName);
             values.put(Directory.EXPORT_SUPPORT, info.exportSupport);
             values.put(Directory.SHORTCUT_SUPPORT, info.shortcutSupport);
 
             Cursor cursor = db.query(Tables.DIRECTORIES, new String[] { Directory._ID },
                     Directory.PACKAGE_NAME + "=? AND " + Directory.DIRECTORY_AUTHORITY + "=? AND "
                             + Directory.ACCOUNT_NAME + "=? AND " + Directory.ACCOUNT_TYPE + "=?",
                     new String[] {
                             info.packageName, info.authority, info.accountName, info.accountType },
                     null, null, null);
             try {
                 long id;
                 if (cursor.moveToFirst()) {
                     id = cursor.getLong(0);
                     db.update(Tables.DIRECTORIES, values, Directory._ID + "=?",
                             new String[] { String.valueOf(id) });
                 } else {
                     id = db.insert(Tables.DIRECTORIES, null, values);
                 }
                 info.id = id;
             } finally {
                 cursor.close();
             }
         }
     }
 
     protected String providerDescription(ProviderInfo provider) {
         return "Directory provider " + provider.packageName + "(" + provider.authority + ")";
     }
 }

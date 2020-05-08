 /*
  * Copyright (C) 2013 The Evervolv Project
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
  * limitations under the License.
  */
 
 package com.evervolv.updater.misc;
 
 public class Constants {
 
     public static final boolean DEBUG = false;
 
     public static final String TAG = "EVUpdates";
 
    public static final String APP_NAME = "EVToolbox";
 
     public static final String DOWNLOAD_DIRECTORY = "EVUpdates/";
 
     public static final String API_URL = "http://evervolv.com/api/v1/list/";
     public static final String FETCH_URL = "http://evervolv.com/get/";
 
     public static final String API_URL_NIGHTLY = API_URL + "n/";
     public static final String API_URL_RELEASE = API_URL + "r/";
     public static final String API_URL_TESTING = API_URL + "t/";
     public static final String API_URL_GAPPS   = API_URL + "g/";
 
     public static final String PREF_LAST_UPDATE_CHECK_NIGHTLY = "pref_last_update_check_nightly";
     public static final String PREF_LAST_UPDATE_CHECK_RELEASE = "pref_last_update_check_release";
     public static final String PREF_LAST_UPDATE_CHECK_TESTING = "pref_last_update_check_testing";
 
     public static final String PREF_UPDATE_SCHEDULE_NIGHTLY = "pref_updates_nightly_schedule";
     public static final String PREF_UPDATE_SCHEDULE_RELEASE = "pref_updates_release_schedule";
     public static final String PREF_UPDATE_SCHEDULE_TESTING = "pref_updates_testing_schedule";
 
     public static final int UPDATE_CHECK_NEVER   = -2;
     public static final int UPDATE_CHECK_ONBOOT  = -1;
     public static final int UPDATE_CHECK_DAILY   = 86400;
     public static final int UPDATE_CHECK_WEEKLY  = 604800;
     public static final int UPDATE_CHECK_MONTHLY = 2419200;
 
     public static final int UPDATE_DEFAULT_NIGHTLY = UPDATE_CHECK_DAILY;
     public static final int UPDATE_DEFAULT_RELEASE = UPDATE_CHECK_MONTHLY;
     public static final int UPDATE_DEFAULT_TESTING = UPDATE_CHECK_NEVER;
 
     /* Download subdirectory and database table names */
     public static final String BUILD_TYPE_RELEASE   = "release";
     public static final String BUILD_TYPE_NIGHTLIES = "nightly";
     public static final String BUILD_TYPE_TESTING   = "testing";
     public static final String BUILD_TYPE_GAPPS     = "gapps";
 
     /* Intent extra fields */
     public static final String EXTRA_DOWNLOAD_ID            = "download_id";
     public static final String EXTRA_DOWNLOAD_STATUS        = "download_status";
     public static final String EXTRA_DOWNLOAD_PROGRESS      = "download_progress";
     public static final String EXTRA_MANIFEST_ENTRY         = "manifest_entry";
     public static final String EXTRA_MANIFEST_ERROR         = "manifest_error";
     public static final String EXTRA_SCHEDULE_UPDATE        = "update_schedule";
     public static final String EXTRA_UPDATE_NON_INTERACTIVE = "update_non_interactive";
 
     /* Intent actions */
     public static final String ACTION_BOOT_COMPLETED        = "com.evervolv.updater.actions.BOOT_COMPLETED";
     public static final String ACTION_START_DOWNLOAD        = "com.evervolv.updater.actions.START_DOWNLOAD";
     public static final String ACTION_UPDATE_CHECK_NIGHTLY  = "com.evervolv.updater.actions.UPDATE_CHECK_NIGHTLY";
     public static final String ACTION_UPDATE_CHECK_RELEASE  = "com.evervolv.updater.actions.UPDATE_CHECK_RELEASE";
     public static final String ACTION_UPDATE_CHECK_TESTING  = "com.evervolv.updater.actions.UPDATE_CHECK_TESTING";
     public static final String ACTION_UPDATE_CHECK_GAPPS    = "com.evervolv.updater.actions.UPDATE_CHECK_GAPPS";
     public static final String ACTION_UPDATE_CHECK_FINISHED = "com.evervolv.updater.actions.UPDATE_CHECK_FINISHED";
     public static final String ACTION_UPDATE_DOWNLOAD       = "com.evervolv.updater.actions.UPDATE_DOWNLOAD";
     public static final String ACTION_UPDATE_NOTIFY_NEW     = "com.evervolv.updater.actions.UPDATE_NOTIFY_NEW";
 
 
 
 }

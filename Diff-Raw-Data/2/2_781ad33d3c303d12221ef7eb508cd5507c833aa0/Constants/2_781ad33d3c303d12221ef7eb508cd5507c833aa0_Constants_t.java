 /*******************************************************************************
  * Copyright (c) 2011 Andreas Storlien and Anders Kristiansen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Andreas Storlien and Anders Kristiansen - initial API and implementation
  ******************************************************************************/
 package com.bjorsond.android.timeline.utilities;
 
 import java.io.File;
 
 import org.apache.http.HttpHost;
 
 import android.os.Environment;
 
 public class Constants {
 
 	public static final String ALL_TIMELINES_DATABASE_NAME = "allTimelinesDatabase.db";
 	public static final String INTENT_ACTION_NEW_TIMELINE = "com.bjorsond.android.timeline.intent.NEW_TIMELINE";
 	public static final String INTENT_ACTION_ADD_TO_TIMELINE = "com.bjorsond.android.timeline.intent.ADD_TIMELINE";
 	public static final String INTENT_ACTION_OPEN_MAP_VIEW_FROM_TIMELINE = "com.bjorsond.android.intent.OPEN_MAP_TIMELINE";
 	public static final String INTENT_ACTION_OPEN_MAP_VIEW_FROM_DASHBOARD = "com.bjorsond.android.intent.OPEN_MAP_DASHBOARD";
 	public static final String INTENT_ACTION_NEW_TAG = "com.bjorsond.android.intent.NEWTAG";
 	
     public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
     public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 1;
     public static final int RECORD_AUDIO_ACTIVITY_REQUEST_CODE = 2;
     public static final int CREATE_NOTE_ACTIVITY_REQUEST_CODE = 3;
     public static final int ATTACHMENT_ACTIVITY_REQUEST_CODE = 4;
     public static final int CREATE_REFLECTION_ACTIVITY_REQUEST_CODE = 10; //added for reflection note
     public static final int MAP_VIEW_ACTIVITY_REQUEST_CODE = 14;
     public static final int ALL_EXPERIENCES_MAP_ACTIVITY_REQUEST_CODE = 15;
     
     public static final int SELECT_PICTURE = 5;
     public static final int SELECT_VIDEO = 6;
     public static final int CAPTURE_BARCODE = 0x0ba7c0de;
     public static final int BROWSE_FILES = 8;
     public static final int EDIT_NOTE = 9;
     public static final int EDIT_REFLECTION = 11;
     
     
     public static final String REQUEST_CODE = "REQUEST_CODE";
     
     public static final String DATABASE_NAME = "timelineDB.db";
     public static final int DATABASE_VERSION = 1; //var 24
 	public static final int USER_GROUP_DATABASE_VERSION = 1;
 	
     public static final String DATABASENAME_REQUEST = "DATABASENAME";
     public static final String EXPERIENCEID_REQUEST = "EXPERIENCEID";
     public static final String EXPERIENCECREATOR_REQUEST = "EXPERIENCECREATOR";
     public static final String SHARED_REQUEST = "SHAREDEXPERIENCE";
     public static final String SHARED_WITH_REQUEST = "SHAREDWITH";
 
 	public static final String USER_GROUP_DATABASE_NAME = "user_group_database.db";
 
 	public static final String GOOGLE_APP_ENGINE_URL = "timelinegamified.appspot.com";
 	
 	public static final String MEDIASTORE_URL = "http://129.241.106.215/upload/upload.php";
 //	public static final String MEDIASTORE_URL = "http://timelinegamified.appspot.com/files/";
 	
 	public static final HttpHost targetHost = new HttpHost(Constants.GOOGLE_APP_ENGINE_URL, 80, "http");
     
     
     public static float HOUR_IN_MILLIS = 3600000;
     public static float DAY_IN_MILLIS = 86400000;
     public static float WEEK_IN_MILLIS = DAY_IN_MILLIS*7;
     
     public static int EVENT_TAG_KEY = 100;
     public static int ACTIVITY_TAG_KEY = 101;
     
     final public static int SHARED_FALSE = 0;
     final public static int SHARED_TRUE = 1;
     final public static int SHARED_ALL = 2;
 
 	final public static int NEW_TAG_REQUESTCODE = 746;
 
     static File sdCardDirectory = Environment.getExternalStorageDirectory();
     public static String IMAGE_STORAGE_FILEPATH = sdCardDirectory.getPath()+"/data/com.bjorsond.android.timeline/images/";
     public static String VIDEO_STORAGE_FILEPATH = sdCardDirectory.getPath()+"/data/com.bjorsond.android.timeline/videos/";
     public static String RECORDING_STORAGE_FILEPATH = sdCardDirectory.getPath()+"/data/com.bjorsond.android.timeline/recordings/";
 
     
     /**
      * Gamification constants
      */
     //LEVELS AND SCALING
    final public static int[] LEVEL = new int[] {0, 100, 150, 225, 340, 500, 760, 1140, 1700, 2560, 3840};
 
     final public static int NOTE_BONUS_NUMBER = 1;    
     final public static int PICTURE_BONUS_NUMBER = 2;    
     final public static int AUDIO_BONUS_NUMBER = 3;    
     final public static int VIDEO_BONUS_NUMBER = 4;    
     final public static int MOOD_BONUS_NUMBER = 5;    
     
     final public static int BONUS_MULTIPLIER = 3;
     
 	
 	final public static int leaderboardID = 8629;
 	
 	//Achievements
 	final public static int VideoAchievement = 10955;
 	final public static int AudioAchievement = 10957;
 	final public static int PictureAchievement = 10953;
 	final public static int NoteAchievement = 10951;
 	final public static int ReflectionNoteAchievement = 11467;
 	final public static int NoteTenAchievement = 10969;
 	final public static int ReflectionNoteTenAchievement = 11469;
 	final public static int PictureTenAchievement = 10971;
 	final public static int AudioTenAchievement = 10975;
 	final public static int VideoTenAchievement = 10973;
 	final public static int FirstElementAchievement = 10987;
 	final public static int FirstMoodAchievement = 10959;
 	final public static int TenthMoodAchievement = 10977;
 	
 	
 	//Points
 	final public static int VideoPoints = 20;
 	final public static int NotePoints = 10;
 	final public static int AudioPoints = 15;
 	final public static int ReflectionPoints = 100;
 	final public static int PicturePoints = 20;
 	final public static int MoodPoints = 5;
     
 }

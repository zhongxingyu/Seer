 /*   
  * Copyright (C) 2011 The Android Open Source Project
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
 
 package com.brent.nprdroid;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.net.Uri;
 import android.util.Log;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Retrieves and organizes media to play. Before being used, you must call {@link #prepare()},
  * which will retrieve all of the music on the user's device (by performing a query on a content
  * resolver). After that, it's ready to retrieve a random song, with its title and URI, upon
  * request.
  */
 public class MusicRetriever {
     final String TAG = "MusicRetriever";
 
     ContentResolver mContentResolver;
 
     // the items (songs) we have queried
     List<Item> mItems = new ArrayList<Item>();
     int listPosition = 0;
     Random mRandom = new Random();
 
     public MusicRetriever(ContentResolver cr) {
         mContentResolver = cr;
     }
 
     /**
      * Loads music data. This method may take long, so be sure to call it asynchronously without
      * blocking the main thread.
      */
     public void prepare() {
    	mItems.clear();
     	String sdPath = "/sdcard/Android/data/com.brent.nprdroid/files/";
 		Log.i(TAG , sdPath);
 		File sdPathFile = new File(sdPath);
 		File[] files = sdPathFile.listFiles();
 		if (files.length > 0) {
 			for (File file : files) {
 				Log.i(TAG, file.getName());
 				mItems.add(new Item(0, null, file.getName(), null, 0));
 			}
 		}
     }
 
     public ContentResolver getContentResolver() {
         return mContentResolver;
     }
 
     /** Returns a random Item. If there are no items available, returns null. */
     public Item getRandomItem() {
         if (mItems.size() <= 0) return null;
         return mItems.get(mRandom.nextInt(mItems.size()));
     }
     
     public Item getNextItem() {
     	if (mItems.size() <= 0 || listPosition == (mItems.size() - 1)) return null;
         return mItems.get(listPosition++);
     }
 
     public static class Item {
         long id;
         String artist;
         String title;
         String album;
         long duration;
 
         public Item(long id, String artist, String title, String album, long duration) {
             this.id = id;
             this.artist = artist;
             this.title = title;
             this.album = album;
             this.duration = duration;
         }
 
         public long getId() {
             return id;
         }
 
         public String getArtist() {
             return artist;
         }
 
         public String getTitle() {
             return title;
         }
 
         public String getAlbum() {
             return album;
         }
 
         public long getDuration() {
             return duration;
         }
 
         public Uri getURI() {
             return ContentUris.withAppendedId(
                     android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
         }
     }
 }

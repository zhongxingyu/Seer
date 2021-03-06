 // Copyright 2010 Google Inc. All Rights Reserved.
 
 package com.android.gallery3d.data;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.MediaStore.Images;
 import android.provider.MediaStore.Video;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.provider.MediaStore.Video.VideoColumns;
 
 import com.android.gallery3d.app.GalleryContext;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 public class RootMediaSet extends DatabaseMediaSet {
     private static final String TITLE = "RootSet";
 
     // Must preserve order between these indices and the order of the terms in
     // BUCKET_PROJECTION_IMAGES, BUCKET_PROJECTION_VIDEOS.
     // Not using SortedHashMap for efficiency reasons.
     private static final int BUCKET_ID_INDEX = 0;
     private static final int BUCKET_NAME_INDEX = 1;
 
     private static final String[] PROJECTION_IMAGE_BUCKETS = {
             ImageColumns.BUCKET_ID,
             ImageColumns.BUCKET_DISPLAY_NAME };
 
     private static final String[] PROJECTION_VIDEO_BUCKETS = {
             VideoColumns.BUCKET_ID,
             VideoColumns.BUCKET_DISPLAY_NAME };
 
     private int mTotalCountCached = -1;
 
     private final ArrayList<BucketMediaSet>
             mSubsets = new ArrayList<BucketMediaSet>();
 
     private HashMap<Integer, String> mLoadBuffer;
 
     public RootMediaSet(GalleryContext context) {
         super(context);
         invalidate();
     }
 
     public MediaItem[] getCoverMediaItems() {
         return new MediaItem[0];
     }
 
     public MediaItem getMediaItem(int index) {
         throw new IndexOutOfBoundsException();
     }
 
     public synchronized int getMediaItemCount() {
         return 0;
     }
 
     public synchronized MediaSet getSubMediaSet(int index) {
         return mSubsets.get(index);
     }
 
     public synchronized int getSubMediaSetCount() {
         return mSubsets.size();
     }
 
     public String getTitle() {
         return TITLE;
     }
 
     public int getTotalMediaItemCount() {
         if (mTotalCountCached >= 0) return mTotalCountCached;
         int total = 0;
         for (MediaSet subset : mSubsets) {
             total += subset.getTotalMediaItemCount();
         }
         mTotalCountCached = total;
         return total;
     }
 
     @Override
     protected void onLoadFromDatabase() {
 
         ContentResolver resolver = mContext.getContentResolver();
         HashMap<Integer, String> map = new HashMap<Integer, String>();
         mLoadBuffer = map;
 
         Uri uriImages = Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
                 appendQueryParameter("distinct", "true").build();
         Cursor cursor = resolver.query(
                 uriImages, PROJECTION_IMAGE_BUCKETS, null, null, null);
         if (cursor == null) throw new NullPointerException();
         try {
             while (cursor.moveToNext()) {
                 map.put(cursor.getInt(BUCKET_ID_INDEX),
                         cursor.getString(BUCKET_NAME_INDEX));
             }
         } finally {
             cursor.close();
         }
 
         Uri uriVideos = Video.Media.EXTERNAL_CONTENT_URI.buildUpon().
                 appendQueryParameter("distinct", "true").build();
         cursor = resolver.query(
                 uriVideos, PROJECTION_VIDEO_BUCKETS, null, null, null);
         if (cursor == null) throw new NullPointerException();
         try {
             while (cursor.moveToNext()) {
                 map.put(cursor.getInt(BUCKET_ID_INDEX),
                         cursor.getString(BUCKET_NAME_INDEX));
             }
         } finally {
             cursor.close();
         }
     }
 
     @Override
     protected void onUpdateContent() {
         HashMap<Integer, String> map = mLoadBuffer;
         if (map == null) throw new IllegalStateException();
 
         GalleryContext context = mContext;
         for (Map.Entry<Integer, String> entry : map.entrySet()) {
             mSubsets.add(new BucketMediaSet(
                     context, entry.getKey(), entry.getValue()));
         }
         mLoadBuffer = null;
 
         Collections.sort(mSubsets, BucketMediaSet.sNameComparator);
 
         for (BucketMediaSet mediaset : mSubsets) {
             mediaset.invalidate();
         }
     }
 }

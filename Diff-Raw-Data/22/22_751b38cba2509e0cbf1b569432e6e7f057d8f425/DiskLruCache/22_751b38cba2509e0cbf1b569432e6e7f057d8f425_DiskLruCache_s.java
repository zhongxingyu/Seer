 ///*
 // * Copyright (C) 2012 The Android Open Source Project
 // *
 // * Licensed under the Apache License, Version 2.0 (the "License");
 // * you may not use this file except in compliance with the License.
 // * You may obtain a copy of the License at
 // *
 // *      http://www.apache.org/licenses/LICENSE-2.0
 // *
 // * Unless required by applicable law or agreed to in writing, software
 // * distributed under the License is distributed on an "AS IS" BASIS,
 // * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // * See the License for the specific language governing permissions and
 // * limitations under the License.
 // * refer to http://code.google.com/p/android/issues/detail?id=29400
 // */
 
 package com.quanleimu.util;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.util.Log;
 import android.view.WindowManager;
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimerTask;
 import java.util.Map.Entry;
 import java.util.Timer;
 
 import com.quanleimu.activity.QuanleimuApplication;
 
 /**
  * A simple disk LRU bitmap cache to illustrate how a disk cache would be used for bitmap caching. A
  * much more robust and efficient disk LRU cache solution can be found in the ICS source code
  * (libcore/luni/src/main/java/libcore/io/DiskLruCache.java) and is preferable to this simple
  * implementation.
  */
 public class DiskLruCache {
     private static final String TAG = "DiskLruCache";
     private static final String CACHE_FILENAME_PREFIX = "__cache_";
     private static final int MAX_REMOVALS = 256;
     private static final int INITIAL_CAPACITY = 256;
     private static final float LOAD_FACTOR = 0.75f;
     
     private static final long DUMP_TO_FILE_ELAPSE_MS = 60*1000;
 
     private final File mCacheDir;
     private int cacheSize = 0;
     private int cacheByteSize = 0;
     private final int maxCacheItemSize = 0xFFFFFF; // 0xFFFFFF item default
     private long maxCacheByteSize = 1024 * 1024 * 5; // 5MB default
     private CompressFormat mCompressFormat = CompressFormat.JPEG;
     private int mCompressQuality = 70;
 
     private Map<String, String> mLinkedHashMap = null;
     
     private boolean mNeedDumpToFile = false;
     private Timer timer = new Timer();
 
     /**
      * A filename filter to use to identify the cache filenames which have CACHE_FILENAME_PREFIX
      * prepended.
      */
     private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
         @Override
         public boolean accept(File dir, String filename) {
             return filename.startsWith(CACHE_FILENAME_PREFIX);
         }
     };
 
     /**
      * Used to fetch an instance of DiskLruCache.
      *
      * @param context
      * @param cacheDir
      * @param maxByteSize
      * @return
      */
     public static DiskLruCache openCache(Context context, File cacheDir, long maxFreeByteSize) {
         if (!cacheDir.exists()) {
             cacheDir.mkdirs();
         }
 
         if (cacheDir.isDirectory() && cacheDir.canWrite()
                 && BitmapUtils.getUsableSpace(cacheDir) > maxFreeByteSize) {
             return new DiskLruCache(cacheDir, maxFreeByteSize);
         }
 
         return null;
     }
 
 	private class TimedIndexDumping extends TimerTask{
 		private boolean mCanceled = false;
 		
 		@Override
 		public boolean cancel(){
 			super.cancel();
 			
 			mCanceled = true;
 			return true;
 		}
 		
 		@Override
 		public void run(){
 			if(!mCanceled){
 				DiskLruCache.this.dumpCacheIndexToFile();
 			}
 		}
 	};
 	
     /**
      * Constructor that should not be called directly, instead use
      * {@link DiskLruCache#openCache(Context, File, long)} which runs some extra checks before
      * creating a DiskLruCache instance.
      *
      * @param cacheDir
      * @param maxByteSize
      */
     private DiskLruCache(File cacheDir, long maxFreeByteSize) {
         mCacheDir = cacheDir;
         maxCacheByteSize = maxFreeByteSize;
         
         rebuildFromExistingFiles(mCacheDir);
 
 		timer.scheduleAtFixedRate(new TimedIndexDumping(), DUMP_TO_FILE_ELAPSE_MS, DUMP_TO_FILE_ELAPSE_MS);
     }
 
     /**
      * Add a bitmap to the disk cache.
      *
      * @param key A unique identifier for the bitmap.
      * @param data The bitmap to store.
      */
     public void put(String key, Bitmap data) {
         synchronized (mLinkedHashMap) {
             /*if (mLinkedHashMap.get(key) == null)*/ {
                 try {
                     final String file = createFilePath(mCacheDir, key);
                     if (writeBitmapToFile(data, file)) {
                         put(key, file);
                         flushCache();
                     }
                 } catch (final FileNotFoundException e) {
                     Log.e(TAG, "Error in put: " + e.getMessage());
                 } catch (final IOException e) {
                     Log.e(TAG, "Error in put: " + e.getMessage());
                 }
             }
         }
     }
     
     public void put(String key, InputStream in){
         synchronized (mLinkedHashMap) {
             /*if (mLinkedHashMap.get(key) == null)*/ {	
             	
 			BufferedInputStream bis = null;			
 			BufferedOutputStream bos = null;
 			
 			try
 			{
 				final String file = createFilePath(mCacheDir, key);
 				bis = new BufferedInputStream(in);
 				bos = new BufferedOutputStream(new FileOutputStream(file), BitmapUtils.IO_BUFFER_SIZE);
 				
 				byte[] buffer = new byte[BitmapUtils.IO_BUFFER_SIZE];
 				int length = 0;
 				while((length = bis.read(buffer)) != -1)
 				{
 					bos.write(buffer, 0, length);
 				}
 				
                 put(key, file);
                 flushCache();
 				
 			} catch (Exception e){
 				
 			}finally{
 				
 				try{
 					if(null != bis){
 						bis.close();
 					}
 					
 					if(null != bos){
 							bos.flush();
 							bos.close();
 						
 					}
 					
 				}catch (IOException e){
 					e.printStackTrace();
 				}
 			}
         }
       }
     }
 
     private void put(String key, String file) {
     	//Log.d("LruDiskCache", "currently put key: "+key);
     	
         mLinkedHashMap.put(key, file);
         cacheSize = mLinkedHashMap.size();
         cacheByteSize += new File(file).length();
         
         mNeedDumpToFile = true;
     }
 
     /**
      * Flush the cache, removing oldest entries if the total size is over the specified cache size.
      * Note that this isn't keeping track of stale files in the cache directory that aren't in the
      * HashMap. If the images and keys in the disk cache change often then they probably won't ever
      * be removed.
      */
     private void flushCache() {
         Entry<String, String> eldestEntry;
         File eldestFile;
         long eldestFileSize;
         int count = 0;
 
         while (count < MAX_REMOVALS &&
                 (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
            eldestEntry = mLinkedHashMap.entrySet().iterator().next();
            //Log.d("LruDiskCache", "currently deleted key: "+ eldestEntry.getKey());
             eldestFile = new File(eldestEntry.getValue());
             eldestFileSize = eldestFile.length();
             mLinkedHashMap.remove(eldestEntry.getKey());
             eldestFile.delete();
             cacheSize = mLinkedHashMap.size();
             cacheByteSize -= eldestFileSize;
             count++;
 //            if (BuildConfig.DEBUG) {
 //                //Log.d(TAG, "flushCache - Removed cache file, " + eldestFile + ", "
 //                        + eldestFileSize);
 //            }           
         }
     }
 
 	public Bitmap decodeBitmapFromFile(String fileName){
 		return BitmapUtils.decodeSampledBitmapFromFile(fileName);
 	}
 	
 	public String getFilePath(String key){
 		return mLinkedHashMap.get(key);
 	}
 	
     /**
      * Get an image from the disk cache.
      *
      * @param key The unique identifier for the bitmap
      * @return The bitmap or null if not found
      */
     public Bitmap get(String key) {
 
         synchronized (mLinkedHashMap) {
             final String file = mLinkedHashMap.get(key);
             if (file != null) {
 //                if (BuildConfig.DEBUG) {
 //                    //Log.d(TAG, "Disk cache hit");
 //                }
             	mNeedDumpToFile = true;
             	
             	//Log.d("LruDiskCache", "currently used key: "+key+", found in disk cache!");
             	
                 return decodeBitmapFromFile(file);//BitmapFactory.decodeFile(file, o);
             } else {
                 final String existingFile = createFilePath(mCacheDir, key);
                 if (new File(existingFile).exists()) {
                     put(key, existingFile);
 //                    if (BuildConfig.DEBUG) {
 //                        //Log.d(TAG, "Disk cache hit (existing file)");
 //                    }
                     //Log.d("LruDiskCache", "currently used key: "+key+", found in file list!");
                     
                     return decodeBitmapFromFile(file);//BitmapFactory.decodeFile(existingFile, o);
                 }
             }
             
             //Log.d("LruDiskCache", "currently used key: "+key+", not found!!");
             return null;
         }
     }
 
     /**
      * Checks if a specific key exist in the cache.
      *
      * @param key The unique identifier for the bitmap
      * @return true if found, false otherwise
      */
     public boolean containsKey(String key) {
         // See if the key is in our HashMap
         if (mLinkedHashMap.containsKey(key)) {
             return true;
         }
 
         // Now check if there's an actual file that exists based on the key
         final String existingFile = createFilePath(mCacheDir, key);
         if (new File(existingFile).exists()) {
             // File found, add it to the HashMap for future use
             put(key, existingFile);
             return true;
         }
         return false;
     }
 
     /**
      * Removes all disk cache entries from this instance cache dir
      */
     public void clearCache() {
         DiskLruCache.clearCache(mCacheDir);
     }
 
     /**
      * Removes all disk cache entries from the application cache directory in the uniqueName
      * sub-directory.
      *
      * @param context The context to use
      * @param uniqueName A unique cache directory name to append to the app cache directory
      */
     public static void clearCache(Context context, String uniqueName) {
         File cacheDir = getDiskCacheDir(context, uniqueName);
         clearCache(cacheDir);
     }
 
     /**
      * Removes all disk cache entries from the given directory. This should not be called directly,
      * call {@link DiskLruCache#clearCache(Context, String)} or {@link DiskLruCache#clearCache()}
      * instead.
      *
      * @param cacheDir The directory to remove the cache files from
      */
     private static void clearCache(File cacheDir) {
         final File[] files = cacheDir.listFiles(cacheFileFilter);
         for (int i=0; i<files.length; i++) {
             files[i].delete();
         }
     }
 
     /**
      * Get a usable cache directory (external if available, internal otherwise).
      *
      * @param context The context to use
      * @param uniqueName A unique directory name to append to the cache dir
      * @return The cache dir
      */
     public static File getDiskCacheDir(Context context, String uniqueName) {
 
         // Check if media is mounted or storage is built-in, if so, try and use external cache dir
         // otherwise use internal cache dir
         final String cachePath =
                 Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                         !BitmapUtils.isExternalStorageRemovable() ?
                         BitmapUtils.getExternalCacheDir(context).getPath() :
                         context.getCacheDir().getPath();
 
         File diskCacheDir = new File(cachePath + File.separator + uniqueName);
         
         if (!diskCacheDir.exists()) {
         	diskCacheDir.mkdirs();
         }        
         
         if(!BitmapUtils.isPathValidForDiskCache(diskCacheDir) || BitmapUtils.getUsableSpace(diskCacheDir) < 0){
         	diskCacheDir = new File(context.getCacheDir().getPath() + File.separator + uniqueName);
         }
         
         return diskCacheDir;
     }
 
     /**
      * Creates a constant cache file path given a target cache directory and an image key.
      *
      * @param cacheDir
      * @param key
      * @return
      */
     public static String createFilePath(File cacheDir, String key) {
         try {
             // Use URLEncoder to ensure we have a valid filename, a tad hacky but it will do for
             // this example
             return cacheDir.getAbsolutePath() + File.separator +
                     CACHE_FILENAME_PREFIX + URLEncoder.encode(key.replace("*", ""), "UTF-8");
         } catch (final UnsupportedEncodingException e) {
             Log.e(TAG, "createFilePath - " + e);
         }
 
         return null;
     }
 
     /**
      * Create a constant cache file path using the current cache directory and an image key.
      *
      * @param key
      * @return
      */
     public String createFilePath(String key) {
         return createFilePath(mCacheDir, key);
     }
 
     /**
      * Sets the target compression format and quality for images written to the disk cache.
      *
      * @param compressFormat
      * @param quality
      */
     public void setCompressParams(CompressFormat compressFormat, int quality) {
         mCompressFormat = compressFormat;
         mCompressQuality = quality;
     }
     
 
     public void dumpCacheIndexToFile(){
     	if(mNeedDumpToFile){
 	    	File mapIndex = new File(createFilePath("__mapIndex_dump_file__.bin"));
 	    	ObjectOutputStream oos = null;
 			try {
 				oos = new ObjectOutputStream( new FileOutputStream(mapIndex));
 		    	oos.writeObject(mLinkedHashMap);	    	
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}finally{
 				if(oos != null){
 					try {
 						oos.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			mNeedDumpToFile = false;
     	}
     }
     
     /**
      * Writes a bitmap to a file. Call {@link DiskLruCache#setCompressParams(CompressFormat, int)}
      * first to set the target bitmap compression and format.
      *
      * @param bitmap
      * @param file
      * @return
      */
     private boolean writeBitmapToFile(Bitmap bitmap, String file)
             throws IOException, FileNotFoundException {
 
         OutputStream out = null;
         try {
             out = new BufferedOutputStream(new FileOutputStream(file), BitmapUtils.IO_BUFFER_SIZE);
             return bitmap.compress(mCompressFormat, mCompressQuality, out);
         } finally {
             if (out != null) {
                 out.close();
             }
         }
     }
     
     private boolean rebuildFromExistingFiles(File cacheDir) {
    	
     	File mapIndex = new File(createFilePath("__mapIndex_dump_file__.bin"));
     	ObjectInputStream ois = null;
 		try {
 			ois = new ObjectInputStream( new FileInputStream(mapIndex));
 			mLinkedHashMap = (Map<String, String>)ois.readObject();//Collections.synchronizedMap(() );	  
 			
 			if(null != mLinkedHashMap){
 //				Iterator<Entry<String, String>> iter = mLinkedHashMap.entrySet().iterator();
 //				while (iter.hasNext()) {
 //					Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
 //					String val = entry.getValue();
 //					
 //					File file = new File(val);
 //					/*if(file.exists()){
 //						cacheByteSize += file.length();
 //					}else*/{
 //						mLinkedHashMap.remove(entry.getKey());
 //					}
 //				}
 //				
 				Object[] keyArray= mLinkedHashMap.keySet().toArray();
 				if(null != keyArray){
 					for(Object key : keyArray){
 						String fileName = mLinkedHashMap.get(key.toString());
 						if(null != fileName){
 							File file = new File(fileName);
 							if(file.exists()){
 								cacheByteSize += file.length();
 							}else{
 								mLinkedHashMap.remove(key.toString());
 							}
 						}
 					}
 				}
 				
 				maxCacheByteSize += cacheByteSize;
 				mNeedDumpToFile = true;
 			}
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			if(ois != null)
 				try {
 					ois.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			
 			if(null == mLinkedHashMap){
 				 mLinkedHashMap =  Collections.synchronizedMap(new LinkedHashMap<String, String>(
 				                    INITIAL_CAPACITY, LOAD_FACTOR, true));
 			}
 		}
 		
 //      final File[] files = cacheDir.listFiles(cacheFileFilter);
 //      
 //      for(int n = 0; n < files.length; ++n){
 //      	synchronized (mLinkedHashMap) {
 //      		mLinkedHashMap.put(, files[n].getAbsolutePath());
 //      	}
 //      }
 		
 		flushCache();
         
     	return true;
     }
     
     @Override
     public void finalize(){
     	timer.cancel();
     	dumpCacheIndexToFile();
     	try {
 			super.finalize();
 		} catch (Throwable e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }

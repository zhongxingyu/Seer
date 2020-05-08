 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.ref.WeakReference;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Hashtable;
 
 import de.raptor2101.GalDroid.Config.GalDroidPreference;
 
 
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 public class GalleryCache {
 	private static final String ClassTag = "GalleryCache";
 	private static long mMaxCacheSize = 50 * 1024 *1024; // convert MByte to Byte
 	private File mCacheDir; 
 	private MessageDigest mDigester;
 	private Hashtable<String,WeakReference<Bitmap>> mCachedBitmaps;
 	
 	public GalleryCache(Context context) throws NoSuchAlgorithmException {
 		Log.d(ClassTag, "Recreate cache");
 		if (mCacheDir == null) {
 			mCacheDir = context.getExternalCacheDir();
 			
 			if(mCacheDir == null){
 				mCacheDir = context.getCacheDir();
 			}
 		}
 		if(!mCacheDir.exists()){
 			mCacheDir.mkdirs();
 		}
 		mCachedBitmaps = new Hashtable<String,WeakReference<Bitmap>>(50);
 		mDigester = MessageDigest.getInstance("MD5");
 	}
 	
 	public Bitmap getBitmap(String sourceUrl)
 	{
 		String hash = buildHash(sourceUrl);
 		WeakReference<Bitmap> reference;
 		
 		synchronized (mCachedBitmaps) {
 			reference = mCachedBitmaps.get(hash);
 		}
 		
 		if(reference == null){
 			Log.d(ClassTag, "Cache Miss Hash Reference " + sourceUrl);
 			return null;
 		}
 		Bitmap bitmap = reference.get();
 		if(bitmap == null || bitmap.isRecycled()){
 			if(bitmap != null) {
 				Log.d(ClassTag, "Bitmap Recycled " + sourceUrl);
 			} else {
 				Log.d(ClassTag, "Cache Miss Object Reference " + sourceUrl);
 			}
 			
 			synchronized (mCachedBitmaps) {
 				mCachedBitmaps.remove(hash);
 			}
 			return null;
 		}
 		Log.d(ClassTag, "Cache Hit Reference " + sourceUrl);
 		return bitmap;
 	}
 
 	public void storeBitmap(String sourceUrl, Bitmap bitmap) {
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		
 		synchronized (mCachedBitmaps) {
 			mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
 		}
 		
 		Log.d(ClassTag, "Bitmap referenced " + sourceUrl);
 		
 		if (cacheFile.exists()) {
 			cacheFile.delete();
 		}
 		try {
 			cacheFile.createNewFile();
 			
 			FileOutputStream output = new FileOutputStream(cacheFile);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
 			output.close();
 			Log.d(ClassTag, "Bitmap stored local " + sourceUrl);
 			GalDroidPreference.accessCacheObject(hash, cacheFile.length());
 		} catch (IOException e) {
 			Log.e(ClassTag, "Error while storing");
 		}	
 	}
 	
 	public void cacheBitmap(String sourceUrl, Bitmap bitmap) {
 		String hash = buildHash(sourceUrl);
 		synchronized (mCachedBitmaps) {
 			mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
 		}
 		Log.d(ClassTag, "Bitmap referenced " + sourceUrl);
 	}
 	
 	public FileInputStream getFileStream(String sourceUrl) {
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		if (GalDroidPreference.cacheObjectExists(hash)&&cacheFile.exists()) {
 			try {
 				Log.d(ClassTag, "Cache Hit " + sourceUrl);
 				GalDroidPreference.accessCacheObject(hash, cacheFile.length());
 				return new FileInputStream(cacheFile);
 				
 			} catch (IOException e) {
 				Log.e(ClassTag, "Error while accessing");
 				return null;				
 			}	
 		}
 		Log.d(ClassTag, "Cache Mis " + sourceUrl);
 		return null;
 	}
 	
 	public File getFile(String sourceUrl) {
 		String hash = buildHash(sourceUrl);
 		return new File(mCacheDir, hash);
 	}
 	
 	public OutputStream createCacheFile(String sourceUrl) throws IOException{
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		
		if (cacheFile.exists()) {
			cacheFile.delete();
 		}
		
		try {
			Log.d(ClassTag, "Create CacheFile " + sourceUrl);
			cacheFile.createNewFile();
			return new FileOutputStream(cacheFile);
			
		} catch (IOException e) {
			Log.e(ClassTag, "Error while accessing");
			throw e;				
		}	
 	}
 	
 	public void removeCacheFile(String uniqueId) {
 		String hash = buildHash(uniqueId);
 		File cacheFile = new File(mCacheDir, hash);
 		
 		if (cacheFile.exists()) {
 			cacheFile.delete();
 		}
 		
 	}
 
 	public void refreshCacheFile(String sourceUrl) {
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		
 		if (cacheFile.exists()) {
 			GalDroidPreference.accessCacheObject(hash, cacheFile.length());
 		}
 	}
 	
 	private String buildHash(String sourceUrl)
 	{
 		synchronized (mDigester) {
 			mDigester.reset();
 			byte[] bytes = sourceUrl.getBytes();
 			mDigester.update(bytes, 0, bytes.length);
 			byte[] diggest = mDigester.digest();
 			StringBuffer returnString = new StringBuffer(diggest.length);
 			for (byte b : diggest) {
 				returnString.append(Integer.toHexString(0xFF & b));
 			}
 			return returnString.toString();
 		}
 	}
 
 	public void clearCachedBitmaps() {
 		for(WeakReference<Bitmap> reference:mCachedBitmaps.values()) {
 			Bitmap bitmap = reference.get();
 			if(bitmap != null){
 				bitmap.recycle();
 			}
 		}
 		mCachedBitmaps.clear();
 	}
 	
 	public File[] ListCachedFiles(){
 		if(mCacheDir.exists()){
 			return mCacheDir.listFiles();
 		} else {
 			return new File[0];
 		}
 	}
 
 	public long getMaxCacheSize() {
 		return mMaxCacheSize;
 	}
 	
 	public boolean isCleanUpNeeded() {
 		long currentCacheSize = GalDroidPreference.getCacheSpaceNeeded();
 		return currentCacheSize > mMaxCacheSize;
 	}
 
 	public File getCacheDir() {
 		return mCacheDir;
 	}
 }

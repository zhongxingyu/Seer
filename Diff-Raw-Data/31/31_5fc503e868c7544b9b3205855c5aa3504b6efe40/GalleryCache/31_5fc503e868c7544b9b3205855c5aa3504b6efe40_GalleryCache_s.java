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
 	private static long mMaxCacheSize = 50 * 1024 *1024; // convert MByte to Byte
 	private File mCacheDir; 
 	private MessageDigest mDigester;
 	private Hashtable<String,WeakReference<Bitmap>> mCachedBitmaps;
 	
 	public GalleryCache(Context context) throws NoSuchAlgorithmException {
 		if (mCacheDir == null) {
 			mCacheDir = context.getExternalCacheDir();
 			
 			if(mCacheDir == null){
 				mCacheDir = context.getCacheDir();
 			}
 		}
		
 		mCachedBitmaps = new Hashtable<String,WeakReference<Bitmap>>(50);
 		mDigester = MessageDigest.getInstance("MD5");
 	}
 	
 	public Bitmap getBitmap(String sourceUrl)
 	{
 		String hash = buildHash(sourceUrl);
 		WeakReference<Bitmap> reference = mCachedBitmaps.get(hash);
 		if(reference == null){
 			return null;
 		}
 		Bitmap bitmap = reference.get();
 		if(bitmap == null || bitmap.isRecycled()){
 			mCachedBitmaps.remove(hash);
 			return null;
 		}
 		Log.d("GalleryCache", "Cache Hit Reference " + sourceUrl);
 		return bitmap;
 	}
 
 	public void storeBitmap(String sourceUrl, Bitmap bitmap) {
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
 		
 		if (cacheFile.exists()) {
 			cacheFile.delete();
 		}
 		try {
 			cacheFile.createNewFile();
 			
 			FileOutputStream output = new FileOutputStream(cacheFile);
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
 			output.close();
 			
 			GalDroidPreference.AccessCacheObject(hash, cacheFile.length());
 		} catch (IOException e) {
 			Log.e("GalleryCache", "Error while storing");
 		}	
 	}
 	
 	public void cacheBitmap(String sourceUrl, Bitmap bitmap) {
 		String hash = buildHash(sourceUrl);
 		mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
 	}
 	
 	public FileInputStream getFileStream(String sourceUrl) {
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		if (cacheFile.exists()) {
 			try {
 				Log.d("GalleryCache", "Cache Hit " + sourceUrl);
 				GalDroidPreference.AccessCacheObject(hash, cacheFile.length());
 				return new FileInputStream(cacheFile);
 				
 			} catch (IOException e) {
 				Log.e("GalleryCache", "Error while accessing");
 				return null;				
 			}	
 		}
 		Log.d("GalleryCache", "Cache Mis " + sourceUrl);
 		return null;
 	}
 	
 	public OutputStream createCacheFile(String sourceUrl) throws IOException{
 		String hash = buildHash(sourceUrl);
 		File cacheFile = new File(mCacheDir, hash);
 		
 		if (!cacheFile.exists()) {
 			try {
 				Log.d("GalleryCache", "Create CacheFile " + sourceUrl);
 				cacheFile.createNewFile();
 				return new FileOutputStream(cacheFile);
 				
 			} catch (IOException e) {
 				Log.e("GalleryCache", "Error while accessing");
 				throw e;				
 			}	
 		}
 		Log.d("GalleryCache", "File already exist " + sourceUrl);
 		throw new IOException("File already exist " + sourceUrl);
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
 			GalDroidPreference.AccessCacheObject(hash, cacheFile.length());
 		}
 	}
 	
 	private String buildHash(String sourceUrl)
 	{
		mDigester.reset();
		byte[] bytes = sourceUrl.getBytes();
		mDigester.update(bytes,0,bytes.length);
		byte[] diggest = mDigester.digest();
		StringBuffer returnString = new StringBuffer(diggest.length);
		for (byte b : diggest) {
			returnString.append(Integer.toHexString(0xFF & b));
 		}
		
		return returnString.toString();
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

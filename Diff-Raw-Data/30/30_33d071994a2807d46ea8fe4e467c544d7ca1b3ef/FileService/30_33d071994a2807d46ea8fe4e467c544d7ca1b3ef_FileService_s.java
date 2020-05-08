 package nz.gen.wellington.guardian.android.api.caching;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.Date;
 
 import nz.gen.wellington.guardian.android.dates.DateTimeHelper;
 import android.content.Context;
 import android.util.Log;
 
 public class FileService {
 	
 	private static final String TAG = "FileService";
 
 	public static final int INTERNAL_CACHE = 1;
 	public static final int SDCARD = 2;
 	public static final int EXTERNAL_SDCARD_SAMSUNG_I7500 = 3;
 	private static final String VERSION_SUFFIX = "v2";
 	
 	
 	public static FileOutputStream getFileOutputStream(Context context, String url) throws FileNotFoundException {
 		final String filepath = FileService.getLocalFilename(url);
 		File file = new File(getCacheDir(context) + "/" + filepath);
 		//Log.i(TAG, "Opening output stream to: " + file.getAbsolutePath());
 		return new FileOutputStream(file);
 	}
 		
 	public static FileInputStream getFileInputStream(Context context, String url) throws FileNotFoundException {
 		final String filepath = FileService.getLocalFilename(url);
 		File file = new File(getCacheDir(context) + "/" + filepath);
 		//Log.i(TAG, "Opening input stream to: " + file.getAbsolutePath());
 		return new FileInputStream(file);
 	}
 
 	public static boolean isLocallyCached(Context context, String apiUrl) {
 		File localFile = new File(getCacheDir(context), getLocalFilename(apiUrl));
 		//Log.i(TAG, "Checking for local cache file at: " + localFile.getAbsolutePath());
 		return localFile.exists() && localFile.canRead();
 	}
 	
 	public static Date getModificationTime(Context context, String apiUrl) {
 		File localFile = new File(getCacheDir(context), getLocalFilename(apiUrl));
 		//Log.i(TAG, "Checking mod time for file at: " + localFile.getAbsolutePath());
 		if (localFile.exists()) {
 			return calculateFileModTime(localFile);
 		}
 		return null;
 	}
 	
	public static void touchFile(Context context, String apiUrl) {
 		File localFile = new File(getCacheDir(context), getLocalFilename(apiUrl));
 		if (localFile.exists()) {
 			//Log.i(TAG, "Touching mod time for file at: " + localFile.getAbsolutePath());
			touchFileModTime(localFile);
 		}
 	}
 	
 	public static String getLocalFilename(String url) {
 		return url.replaceAll("/", "").replaceAll(":", "") + VERSION_SUFFIX;
 	}
 
 	public static void clear(Context context, String apiUrl) {
 		File localFile = new File(getCacheDir(context), getLocalFilename(apiUrl));
 		//Log.i(TAG, "Clearing: " + localFile.getAbsolutePath());
 		localFile.delete();
 	}
 
 	
 	public static void clearAll(Context context) {		
 		//Log.i(TAG, "Clearing all cache files");				
 		FileFilter allFilesFilter = new FileFilter() {				
 			@Override
 			public boolean accept(File file) {
 				return true;
 			}
 		};
 		deleteFiles(context, allFilesFilter);
 	}
 	
 	
 	public static void clearAllArticleSets(Context context) {		
 		//Log.i(TAG, "Clearing all article set cache files");
 				
 		FileFilter jsonFilesFilter = new FileFilter() {				
 			@Override
 			public boolean accept(File file) {
 				return file.getPath().endsWith("json") && !file.getPath().endsWith("sections.json");
 			}
 		};		
 		deleteFiles(context, jsonFilesFilter);
 	}
 
 	
 	public static void clearExpiredCacheFiles(Context context) {
 		Log.i(TAG, "Clearing all article set cache files more than 24 hours old");
 		FileFilter jsonFilesFilter = new FileFilter() {				
 			@Override
 			public boolean accept(File file) {
 				return calculateFileModTime(file).before(DateTimeHelper.yesterday());
 			}
 		};
 		deleteFiles(context, jsonFilesFilter);
 	}
 	
 	
 	
 	private static void deleteFiles(Context context, FileFilter jsonFilesFilter) {		
 		File cacheDir = getCacheDir(context);
 		if (cacheDir == null) {
 			Log.w(TAG, "No cache folder found");
 			return;
 		}
 		
 		File[] listFiles = cacheDir.listFiles(jsonFilesFilter);
 		for (int i = 0; i < listFiles.length; i++) {
 			File cacheFile = listFiles[i];
 			//Log.i(TAG, "Found cache file: " + cacheFile.getAbsolutePath());
 			if (cacheFile.delete()) {
 				//Log.i(TAG, "Deleted cache file: " + cacheFile.getAbsolutePath());				
 			}		
 		}
 	}
 	
 	
 	protected static File getCacheDir(Context context) {				
 		return context.getCacheDir();
 	}
 	
 	
 	private static Date calculateFileModTime(File localFile) {
 		Date modTime = new Date(localFile.lastModified());
 		return modTime;
 	}
 	
 	
	private static void touchFileModTime(File localFile) {
		Date modTime = DateTimeHelper.now();
 		localFile.setLastModified(modTime.getTime());
 	}
 	
 }

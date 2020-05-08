 package com.levelup.picturecache;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import st.gaw.db.InMemoryDbHelper;
 import st.gaw.db.InMemoryDbOperation;
 import st.gaw.db.InMemoryHashmapDb;
 import st.gaw.db.Logger;
 import st.gaw.db.LoggerDefault;
 import st.gaw.db.MapEntry;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.ReceiverCallNotAllowedException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.text.TextUtils;
 
 import com.levelup.FileUtils;
 import com.levelup.picturecache.DownloadManager.JobsMonitor;
 import com.levelup.picturecache.loaders.ImageViewLoader;
 import com.levelup.picturecache.loaders.PrecacheImageLoader;
 import com.levelup.picturecache.loaders.RemoteViewLoader;
 
 /**
  * base class to use the picture cache to load images and keep a persistent cache 
  */
 public abstract class PictureCache extends InMemoryHashmapDb<CacheKey,CacheItem> implements JobsMonitor {
 
 	public static final String TAG = "PictureCache";
 	final static boolean DEBUG_CACHE = false & BuildConfig.DEBUG;
 
 	private static final int MIN_ADD_BEFORE_PURGE = 7;
 
 	/**
 	 * size in bytes of the amount of storage available for files of the specified {@link LifeSpan}
 	 * @param lifeSpan type of {@link LifeSpan}
 	 * @return the amount available in bytes
 	 * @see {@link #notifyStorageSizeChanged()}
 	 */
 	protected abstract int getCacheMaxSize(LifeSpan lifeSpan);
 
 	/**
 	 * return a different uuid for when the original uuid just got a new URL. this way we can keep the old and new versions in the cache
 	 * @param uuid base UUID
 	 * @param URL old URL
 	 * @return different UUID to stored the old cached version
 	 */
 	abstract protected String getOldPicUUID(String uuid, String URL);
 
 	/**
 	 * the App name used to export the pictures in the gallery
 	 * @return the app name that will show up in the Gallery or null if you don't plan to use {@link #saveInGallery(String, int, boolean, boolean, int)}
 	 */
 	abstract protected String getAppName();
 
 	protected String getOldCacheFolder() {
 		return null;
 	}
 
 	private static final String DATABASE_NAME = "PictureCachev2.sqlite";
 	private static final String OLD_DATABASE_NAME = "PictureCache.sqlite"; 
 	private static final int DATABASE_VERSION = 1;
 	private static final String TABLE_NAME = "Pictures";
 
 	private static final String CREATE_TABLE = 
 			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " + 
 					"(UUID VARCHAR, " +                  // key: the unique ID representing this item in the DB
 					"SRC_URL VARCHAR not null, " +       // the source URL
 					"TYPE INTEGER DEFAULT 0, " +         // the type of URL (short term 0 / long term 1 / eternal 2)
 					"PATH VARCHAR, " +                   // the path in the cached picture file
 					"REMOTE_DATE LONG DEFAULT 0, " +     // the last remote date using to the item (if applicable)
 					"DATE LONG not null DEFAULT -1, " +  // the date of last access to the item
 					"PRIMARY KEY (UUID));";
 
 	private static Boolean mDirAsserted = Boolean.FALSE;
 
 	private final File mCacheFolder;
 	final UIHandler postHandler;
 	final OutOfMemoryHandler ooHandler;
 
 	private DownloadManager mJobManager;
 	private Context mContext;
 
 	private AtomicInteger mPurgeCounterLongterm = new AtomicInteger();
 	private AtomicInteger mPurgeCounterShortterm = new AtomicInteger();
 
 	@Override
 	protected String getMainTableName() {
 		return TABLE_NAME;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE);
 	}
 
 	@Override
 	protected Entry<CacheKey, CacheItem> getEntryFromCursor(Cursor c) {
 		int indexPath = c.getColumnIndex("PATH");
 		int indexURL = c.getColumnIndex("SRC_URL");
 		int indexType = c.getColumnIndex("TYPE");
 		int indexRemoteDate = c.getColumnIndex("REMOTE_DATE");
 		int indexDate = c.getColumnIndex("DATE");
 		int indexUUID = c.getColumnIndex("UUID");
 
 		if (indexRemoteDate==-1) {
 			// updating from an old DB
 			indexRemoteDate = c.getColumnIndex("TOUIT_ID");
 
 			int indexPathRounded = c.getColumnIndex("PATHR");
 			int indexHeight = c.getColumnIndex("HEIGHT");
 			int indexWidthBased = c.getColumnIndex("WIBASE");
 
 			String path = c.getString(indexPath);
 			String pathr = c.getString(indexPathRounded);
 			boolean widthBased;
 			if (indexWidthBased<0)
 				widthBased = false;
 			else
 				widthBased = c.getInt(indexWidthBased)!=0;
 
 			if (!TextUtils.isEmpty(path)) {
 				CacheItem val = new CacheItem(new File(path), c.getString(indexURL));
 				if (val.path.exists()) {
 					val.lifeSpan = LifeSpan.fromStorage(c.getInt(indexType));
 					val.remoteDate = c.getLong(indexRemoteDate);
 					val.lastAccessDate = c.getLong(indexDate);
 
 					CacheKey key = CacheKey.newUUIDBasedKey(c.getString(indexUUID), c.getInt(indexHeight), widthBased, StorageType.AUTO, null);
 
 					put(key, val);
 				}
 			}
 
 			if (!TextUtils.isEmpty(pathr)) {
 				CacheItem val = new CacheItem(new File(pathr), c.getString(indexURL));
 				if (val.path.exists()) {
 					val.lifeSpan = LifeSpan.fromStorage(c.getInt(indexType));
 					val.remoteDate = c.getLong(indexRemoteDate);
 					val.lastAccessDate = c.getLong(indexDate);
 
 					CacheKey key = CacheKey.newUUIDBasedKey(c.getString(indexUUID), c.getInt(indexHeight), widthBased, StorageType.AUTO, "_r");
 
 					put(key, val);
 				}
 			}
 
 			return null; // already done manually
 		} else {
 			final String path = c.getString(indexPath);
 			if (TextUtils.isEmpty(path)) {
 				LogManager.logger.w(TAG, "trying to load an empty cache item for "+c.getString(indexURL));
 				return null;
 			}
 			CacheItem val = new CacheItem(new File(path), c.getString(indexURL));
 			val.lifeSpan = LifeSpan.fromStorage(c.getInt(indexType));
 			val.remoteDate = c.getLong(indexRemoteDate);
 			val.lastAccessDate = c.getLong(indexDate);
 
 			CacheKey key = CacheKey.unserialize(c.getString(indexUUID));
 
 			return new MapEntry<CacheKey, CacheItem>(key, val);
 		}
 	}
 
 	@Override
 	protected ContentValues getValuesFromData(Entry<CacheKey, CacheItem> data, SQLiteDatabase dbToFill) throws RuntimeException {
 		if (data.getValue().path==null) {
 			LogManager.logger.w(TAG, "cache item has an empty path :"+data.getKey()+" / "+data.getValue());
 			throw new RuntimeException("empty path for "+data);
 		}
 
 		ContentValues values = new ContentValues(6);
 		values.put("UUID", data.getKey().serialize());
 		values.put("SRC_URL", data.getValue().URL);
 		values.put("TYPE", data.getValue().lifeSpan.toStorage());
 		values.put("PATH", data.getValue().path.getAbsolutePath());
 		values.put("REMOTE_DATE", data.getValue().remoteDate);
 		values.put("DATE", data.getValue().lastAccessDate);
 
 		return values;
 	}
 
 	@Override
 	protected String getKeySelectClause(CacheKey key) {
 		return "UUID=?";
 	}
 
 	@Override
 	protected String[] getKeySelectArgs(CacheKey key) {
 		return new String[] {key.serialize()};
 	}
 
 	/**
 	 * constructor of a PictureCache
 	 * @param context context of the application, may also be used to get a {@link ContentResolver}
 	 * @param postHandler handler to run some code in the UI thread and also determine if we're in the UI thread or not
 	 * @param logger a {@link Logger} object used to send all the logs generated inside the cache, may be null
 	 * @param ooHandler a {@link OutOfMemoryHandler} object used to notify when we are short on memory, may be null
 	 */
 	protected PictureCache(Context context, UIHandler postHandler, Logger logger, OutOfMemoryHandler ooHandler) {
 		super(context, DATABASE_NAME, DATABASE_VERSION, logger);
 
 		LogManager.setLogger(logger==null ? new LoggerDefault() : logger);
 		this.mContext = context;
 		this.postHandler = postHandler;
 		if (ooHandler==null)
 			this.ooHandler = new OutOfMemoryHandler() {
 			// do nothing
 			@Override
 			public void onOutOfMemoryError(OutOfMemoryError e) {}
 		};
 		else
 			this.ooHandler = ooHandler;
 
 		File olddir = new File(Environment.getExternalStorageDirectory(), "/Android/data/"+context.getPackageName()+"/cache");
 		if (olddir.exists())
 			mCacheFolder = olddir;
 		else {
 			File newdir = null;
 			try {
 				newdir = ApiLevel8.getPrivatePictureDir(context);
 			} catch (VerifyError e) {
 			} catch (NoSuchFieldError e) {
 			} finally {
 				if (newdir==null)
 					newdir = olddir;
 			}
 			mCacheFolder = newdir;
 		}
 
 		mJobManager = new DownloadManager();
 		mJobManager.setMonitor(this);
 
 		File olddb = context.getDatabasePath(OLD_DATABASE_NAME);
 		if (olddb.exists()) {
 			/* TODO: SQLiteDatabase oldDB = context.openOrCreateDatabase(OLD_DATABASE_NAME, 0, null);
 			reloadFromDB(oldDB, TABLE_NAME);
 			oldDB.close();
 			context.deleteDatabase(OLD_DATABASE_NAME);*/
 		}
 
 		//getWritableDatabase().setLockingEnabled(false); // we do our own thread protection
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		LogManager.logger.w(TAG, "Upgrading PictureCache from " + oldVersion + " to " + newVersion);
 	}
 
 	File getCachedFilepath(CacheKey key) throws SecurityException, IOException
 	{
 		// TODO: handle the switch between phone memory and SD card
 		assertFolderExists();
 		return new File(mCacheFolder, key.getFilename());
 	}
 
 	public File getTempDir()
 	{
 		try {
 			assertFolderExists();
 		} catch (SecurityException e) {
 			LogManager.logger.e(TAG, "getTempDir() cannot access the dir ", e);
 		} catch (IOException e) {
 			LogManager.logger.e(TAG, "getTempDir() cannot access the dir ", e);
 		}
 		return mCacheFolder;
 	}
 
 	/**
 	 * get a directory to store temporary files that should always be available (ie even when the sdcard is not present)
 	 * @return
 	 */
 	public File getAvailaibleTempDir() {
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
 			return getTempDir();
 
 		return getContext().getCacheDir();
 	}
 
 	public File getPictureDir()
 	{
 		File dstDir = null;
 		String appName = getAppName();
 		if (!TextUtils.isEmpty(appName)) {
 			try {
 				try {
 					dstDir = new File(ApiLevel8.getPublicPictureDir(), appName);
 				} catch (VerifyError e) {
 					dstDir = new File(Environment.getExternalStorageDirectory()+"/DCIM", appName);
 				} catch (NoSuchFieldError e) {
 					dstDir = new File(Environment.getExternalStorageDirectory()+"/DCIM", appName);
 				}
 				dstDir.mkdirs();
 			} catch (SecurityException e) {
 				LogManager.logger.e(TAG, "getPictureDir() cannot access the dir ", e);
 			}
 		}
 		return dstDir;
 	}
 
 	private void assertFolderExists() throws IOException, SecurityException 
 	{
 		//LogManager.logger.e(TAG, "assertFolderExists " +DirAsserted);
 		synchronized (mDirAsserted) {
 			if (!mDirAsserted) {
 				//LogManager.logger.i("data dir=" + Environment.getDataDirectory().getAbsolutePath());
 				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 					//LogManager.logger.w(TAG, "cache dir=" + dir.getAbsolutePath()+" exists:"+dir.exists());
 					if (mCacheFolder.exists() && mCacheFolder.isDirectory())
 						mDirAsserted = Boolean.TRUE;
 					else {
 						mDirAsserted = mCacheFolder.mkdirs();
 						//LogManager.logger.w(TAG, "cache dir=" + dir.getAbsolutePath()+" asserted:"+DirAsserted);
 						if (mDirAsserted) {
 							new File(mCacheFolder, ".nomedia").createNewFile();
 						}
 					}
 
 					String oldFolder = getOldCacheFolder();
 					if (oldFolder!=null) {
 						final File oldDir = new File(Environment.getExternalStorageDirectory(), oldFolder);
 						if (oldDir.exists()) {
 							new Thread() {
 								public void run() {
 									FileUtils.deleteDirectory(oldDir);
 								}
 							}.start();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private long getCacheSize(LifeSpan lifeSpan) {
 		long result = 0;
 		mDataLock.lock();
 		try {
 			Iterator<Entry<CacheKey, CacheItem>> v = getMap().entrySet().iterator();
 			Entry<CacheKey, CacheItem> k;
 			while (v.hasNext()) {
 				k = v.next();
 				if (k.getValue().lifeSpan!=lifeSpan) continue;
 				result += k.getValue().getFileSize();
 			}
 		} catch (Throwable e) {
 			// workaround to avoid locking mData during read/write in the DB
 			LogManager.logger.e(TAG, "getCacheSize failed", e);
 		} finally {
 			mDataLock.unlock();
 		}
 		return result;
 	}
 
 	private Entry<CacheKey, CacheItem> getCacheOldestEntry(LifeSpan lifeSpan) {
 		//LogManager.logger.d(TAG, "getCacheOldest in");
 		Entry<CacheKey, CacheItem> result = null;
 		for (Entry<CacheKey, CacheItem> entry : getMap().entrySet()) {
 			final CacheItem item = entry.getValue();
 			if (lifeSpan==item.lifeSpan && (result==null || result.getValue().lastAccessDate > item.lastAccessDate))
 				result = entry;
 		}
 		//LogManager.logger.e(TAG, "getCacheOldest out with "+result);
 		return result;
 	}
 
 	private static class RemoveExpired implements InMemoryDbOperation<Map.Entry<CacheKey,CacheItem>> {
 
 		private final LifeSpan lifeSpan;
 
 		RemoveExpired() {
 			this.lifeSpan = null;
 		}
 		RemoveExpired(LifeSpan cacheType) {
 			this.lifeSpan = cacheType;
 		}
 
 		@Override
 		public void runInMemoryDbOperation(InMemoryDbHelper<Entry<CacheKey, CacheItem>> db) {
 			PictureCache cache = (PictureCache) db;
 			if (lifeSpan!=null)
 				makeRoom(cache, lifeSpan);
 			else {
 				for (LifeSpan lifeSpan : LifeSpan.values())
 					makeRoom(cache, lifeSpan);
 			}
 		}
 
 		private static void makeRoom(PictureCache cache, LifeSpan lifeSpan) {
 			try {
 				long TotalSize = cache.getCacheSize(lifeSpan);
 				int MaxSize = cache.getCacheMaxSize(lifeSpan);
 				if (MaxSize!=0 && TotalSize > MaxSize) {
 					// make room in the DB/cache for this new element
 					while (TotalSize > MaxSize) {
 						//if (type != k.getValue().type) continue;
 						//long deleted = 0;
 						Entry<CacheKey, CacheItem> entry;
 						cache.mDataLock.lock();
 						try {
 							entry = cache.getCacheOldestEntry(lifeSpan);
 							if (entry==null)
 								break;
 						} finally {
 							cache.mDataLock.unlock();
 						}
 
 						if (DEBUG_CACHE) LogManager.logger.i(TAG, "remove "+entry+" from the cache for "+lifeSpan);
 						CacheItem item = cache.remove(entry.getKey());
 						if (item!=null) {
 							File f = item.path;
 							if (f!=null && f.exists()) {
 								long fSize = f.length();
 								if (f.delete()) {
 									TotalSize -= fSize;
 									//deleted += fSize;
 								}
 							}
 						}
 						//LogManager.logger.d(TAG, "makeroom");
 					}
 				}
 			} catch (NullPointerException e) {
 				LogManager.logger.w(TAG, "can't make room for type:"+lifeSpan,e);
 			}
 			//LogManager.logger.d(TAG, "makeroom done");
 		}
 	}
 
 	/**
 	 * 
 	 * @param URL
 	 * @param key
 	 * @param itemDate use to store the previous item for the same {@link key}
 	 * @param loader
 	 * @param lifeSpan see {@link LifeSpan}
 	 */
 	void getPicture(String URL, CacheKey key, long itemDate, PictureLoaderHandler loader, LifeSpan lifeSpan)
 	{
 		mDataLock.lock();
 		try {
 			//LogManager.logger.d(TAG, "getting picture "+URL+" into "+target+" key:"+key);
 			if (TextUtils.isEmpty(URL)) {
 				// get the URL matching the UUID if we don't have a forced one
 				CacheItem v = getMap().get(key);
 				if (v!=null)
 					URL = v.URL;
 				//LogManager.logger.i("no URL specified for "+key+" using "+URL);
 			}
 			if (TextUtils.isEmpty(URL)) {
 				LogManager.logger.d(TAG, "no URL specified/known for "+key+" using default");
 				removePictureLoader(loader, null);
 				loader.drawDefaultPicture(null, postHandler);
 				return;
 			}
 
 			//LogManager.logger.v(TAG, "load "+URL+" in "+target+" key:"+key);
 			String previouslyLoading = loader.setLoadingURL(URL); 
 			if (URL.equals(previouslyLoading)) {
 				//LogManager.logger.v(TAG, loader+" no need to draw anything");
 				return; // no need to do anything the image is the same or downloading for it
 			}
 			
 			if (previouslyLoading!=null) {
 				mJobManager.cancelDownloadForLoader(loader, previouslyLoading);
 			}
 
 			/*if (URL.startsWith("android.resource://")) {
 			URL = URL.substring(19);
 			int resId = Integer.valueOf(URL.substring(URL.indexOf('/')+1));
 			target.setImageResource(resId);
 			return;
 		}*/
 
 			File file = getCachedFile(key, URL, itemDate);
 			if (file!=null && file.exists() && file.canRead() && loader.canDirectLoad(file, postHandler)) {
 				try {
 					Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
 					if (bmp!=null) {
 						//LogManager.logger.d(TAG, "using direct file for URL "+URL+" file:"+file);
 						loader.drawBitmap(bmp, URL, postHandler);
 						return;
 					}
 				} catch (OutOfMemoryError e) {
 					loader.drawDefaultPicture(URL, postHandler);
 					LogManager.logger.w(TAG, "can't decode "+file,e);
 					ooHandler.onOutOfMemoryError(e);
 					return;
 				}
 			}
 
 			loader.drawDefaultPicture(URL, postHandler);
 
 			// we could not read from the cache, load the URL
 			if (key!=null)
 				mJobManager.addDownloadTarget(this, URL, loader, key, itemDate, lifeSpan);
 		} finally {
 			mDataLock.unlock();
 		}
 	}
 
 	/**
 	 * helper method to load a height based picture using the cache
 	 * @see {@link PictureJob}
 	 * @param handler the handler used to display the loaded bitmap/placeholder on the target, see {@link ImageViewLoader}, {@link RemoteViewLoader} or {@link PrecacheImageLoader}
 	 * @param URL the bitmap URL to load into the handler (may be null if UUID is not null)
 	 * @param UUID a unique ID representing the element in the cache (may be null if URL is not null)
 	 * @param itemDate the date in which the item was created, this is used to purge images older than this one from the cache
 	 * @param lifeSpan how long the item should remain in the cache, can be {@link LifeSpan#SHORTTERM},  {@link LifeSpan#LONGTERM} or {@link LifeSpan#ETERNAL}
 	 * @param height the height of the image to store in the cache
 	 * @param extensionMode the kind of file type we are loading, can be {@link StorageType#AUTO}, {@link StorageType#PNG} or {@link StorageType#JPEG}
 	 */
 	public void loadPictureWithFixedHeight(PictureLoaderHandler handler, String URL, String UUID, long itemDate, LifeSpan lifeSpan, int height, StorageType extensionMode) {
 		PictureJob pictureJob = new PictureJob.Builder(handler)
 			.setURL(URL).setUUID(UUID)
 			.setFreshDate(itemDate)
 			.setLifeType(lifeSpan)
 			.setExtensionMode(extensionMode)
 			.setDimension(height, false)
 			.build();
 		
 		try {
 			pictureJob.startLoading(this);
 		} catch (NoSuchAlgorithmException e) {
 			LogManager.logger.d(TAG, "can't load picture", e);
 		}
 	}
 
 	/**
 	 * helper method to load a width based picture using the cache
 	 * @see {@link PictureJob}
 	 * @param handler the handler used to display the loaded bitmap/placeholder on the target, see {@link ImageViewLoader}, {@link RemoteViewLoader} or {@link PrecacheImageLoader}
 	 * @param URL the bitmap URL to load into the handler (may be null if UUID is not null)
 	 * @param UUID a unique ID representing the element in the cache (may be null if URL is not null)
 	 * @param itemDate the date in which the item was created, this is used to purge images older than this one from the cache
 	 * @param lifeSpan how long the item should remain in the cache, can be {@link LifeSpan#SHORTTERM},  {@link LifeSpan#LONGTERM} or {@link LifeSpan#ETERNAL}
 	 * @param width the width of the image to store in the cache
 	 * @param extensionMode the kind of file type we are loading, can be {@link StorageType#AUTO}, {@link StorageType#PNG} or {@link StorageType#JPEG}
 	 */
 	public void loadPictureWithMaxWidth(PictureLoaderHandler handler, String URL, String UUID, long itemDate, LifeSpan lifeSpan, int width, StorageType extensionMode) {
 		PictureJob pictureJob = new PictureJob.Builder(handler)
 			.setURL(URL)
 			.setUUID(UUID)
 			.setFreshDate(itemDate)
 			.setLifeType(lifeSpan)
 			.setExtensionMode(extensionMode)
 			.setDimension(width, true)
 			.build();
 		try {
 			pictureJob.startLoading(this);
 		} catch (NoSuchAlgorithmException e) {
 			LogManager.logger.d(TAG, "can't load picture", e);
 		}
 	}
 
 	/**
 	 * stop loading for that {@link loader} target, keep the target marked for the previously loading URL
 	 * @param loader
 	 * @param oldURL
 	 */
 	public void cancelPictureLoader(PictureLoaderHandler loader, String oldURL) {
 		if (loader!=null)
 			mJobManager.cancelDownloadForLoader(loader, oldURL);
 	}
 
 	/**
 	 * stop loading for that {@link loader} target, reset loading URL marked on that target
 	 * @param loader
 	 * @param oldURL
 	 */
 	public void removePictureLoader(PictureLoaderHandler loader, String oldURL) {
 		if (loader!=null) {
 			if (DEBUG_CACHE) LogManager.logger.i(TAG, "removePictureLoader "+loader+" with old URL "+oldURL);
 			loader.setLoadingURL(null);
 			mJobManager.cancelDownloadForLoader(loader, oldURL);
 		}
 	}
 
 	public boolean saveInGallery(String UUID, int width, boolean widthBased, boolean Rounded, StorageType extensionMode) throws IOException, SecurityException {
 		boolean succeeded = false;
 		CacheKey key = CacheKey.newUUIDBasedKey(UUID, width, widthBased, extensionMode, Rounded?"_r":null);
 		mDataLock.lock();
 		try {
 			CacheItem v = getMap().get(key);
 			if (v!=null && v.path!=null) {
 				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 					File dst = new File(getPictureDir(), key.getFilename());
 					FileUtils.copyFile(v.path, dst, TAG);
 					succeeded = true;
 
 					try {
 						GalleryScanner saver = new GalleryScanner(getContext());
 						saver.scan(dst);
 					} catch (ReceiverCallNotAllowedException e) {
 						LogManager.logger.w(TAG, "could not start the gallery scanning");
 					}
 				}
 			}
 		} finally {
 			mDataLock.unlock();
 		}
 		return succeeded;
 	}
 
 	@Override
 	protected void onDataCleared() {
 		super.onDataCleared();
 		try {
 			FileUtils.deleteDirectory(mCacheFolder);
 			synchronized (mDirAsserted) {
 				mDirAsserted = Boolean.FALSE;
 			}
 			assertFolderExists();
 		} catch (SecurityException e) {
 			LogManager.logger.e(TAG, "clearCache exception", e);
 		} catch (IOException e) {
 			LogManager.logger.e(TAG, "clearCache could not recreate the cache folder", e);
 		}
 	}
 
 	private CacheItem getCacheItem(String UUID, int Height, boolean widthBased, boolean rounded) {
 		CacheKey key = CacheKey.newUUIDBasedKey(UUID, Height, widthBased, StorageType.AUTO, rounded?"_r":null);
 		return getMap().get(key);
 	}
 
 	protected String getCachePath(String UUID, int height, boolean widthBased, boolean rounded) {
 		mDataLock.lock();
 		try {
 			CacheItem cacheItem = getCacheItem(UUID, height, widthBased, rounded);
 			if (cacheItem!=null) {
 				File file = cacheItem.path;
 				if (file!=null && file.exists())
 					return file.getAbsolutePath();
 			}
 		} finally {
 			mDataLock.unlock();
 		}
 		return null;
 	}
 
 	private boolean moveCachedFiles(CacheKey srcKey, CacheKey dstKey, LifeSpan lifeSpan) {
 		LogManager.logger.v(TAG, "Copy "+srcKey+" to "+dstKey);
 		if (getMap().containsKey(dstKey)) {
 			LogManager.logger.d(TAG, "item "+dstKey+" already exists in the DB");
 			return false;
 		}
 
 		try {
 			CacheItem v = getMap().get(srcKey);
 			if (v!=null) {
 				File src = v.path;
 				if (src!=null && src.exists()) {
 					File dst = getCachedFilepath(dstKey);
 					dst.delete();
 
 					if (src.renameTo(dst)) {
 						v = v.copyWithNewPath(dst);
 						v.lifeSpan = lifeSpan;
 						return put(dstKey, v)!=null;
 					} else {
 						LogManager.logger.e(TAG, "Failed to rename path "+src+" to "+dst);
 					}
 					//else LogManager.logger.d(TAG, false, "keep the old version of "+newKey);
 				}
 			}
 		} catch (Throwable e) {
 			LogManager.logger.e(TAG, "failed to copy " + srcKey + " to " + dstKey, e);
 		}
 		return false;
 	}
 
 	/**
 	 * indicate that the values returned by {@link #getCacheMaxSize(LifeSpan)} have changed
 	 */
 	protected void notifyStorageSizeChanged() {
 		scheduleCustomOperation(new RemoveExpired());
 	}
 
 	@Override
 	public void onNewBitmapLoaded(HashMap<CacheVariant,Bitmap> newBitmaps, String url, long remoteDate, LifeSpan lifeSpan) {
 		// handle the storing and adding to the cache
 		// save the bitmap for later use
 		long fileSizeAdded = 0;
 		for (CacheVariant variant : newBitmaps.keySet()) {
 			try {
 				if (variant.path.exists())
 					variant.path.delete();
 				FileOutputStream fos = new FileOutputStream(variant.path, false);
 				Bitmap bmp = newBitmaps.get(variant);
 				bmp.compress(variant.key.getCompression(), variant.key.getCompRatio(), fos);
 				fos.close();
 
 				mDataLock.lock();
 				try {
 					CacheItem val = getMap().get(variant.key);
 					if (val!=null) {
 						if (val.remoteDate < remoteDate)
 							val.remoteDate = remoteDate;
 
 						if (val.lifeSpan.compare(lifeSpan)<0)
 							val.lifeSpan = lifeSpan;
 
 						val.lastAccessDate = System.currentTimeMillis();
 						notifyItemChanged(variant.key);
 						/*if (!changed && url.equals(val.URL))
 							LogManager.logger.v(TAG, "image " + key.toString()+" unchanged");
 						else
 							LogManager.logger.v(TAG, "image " + key.toString()+" already exists, adjusting the touitDate:"+val.touitID+" bmpIsNew:"+bmpIsNew+" rbmpIsNew:"+rbmpIsNew+" url:"+url);*/
 					} else {
 						val = new CacheItem(variant.path, url);
 						val.remoteDate = remoteDate;
 						val.lifeSpan = lifeSpan;
 						val.lastAccessDate = System.currentTimeMillis();
 						//LogManager.logger.v(TAG, "adding image " + key.toString() +" type:"+type+" bmpIsNew:"+bmpIsNew+" rbmpIsNew:"+rbmpIsNew+" url:"+url);
 						put(variant.key, val);
 					}
 
 					fileSizeAdded += variant.path.length();
 				} finally {
 					mDataLock.unlock();
 				}
 
 				//LogManager.logger.i("saved bmp to "+outFile.getAbsolutePath());
 			} catch (IOException e) {
 				LogManager.logger.i(TAG, "failed to save "+url+" as "+variant, e);
 			}
 		}
 
 		//LogManager.logger.i("BitmapLoaded outFile:"+outFile);
 		if (fileSizeAdded!=0) {
 			final boolean needsPurge;
 			if (lifeSpan==LifeSpan.LONGTERM)
 				needsPurge = (mPurgeCounterLongterm.incrementAndGet() > MIN_ADD_BEFORE_PURGE);
 			else if (lifeSpan == LifeSpan.SHORTTERM)
 				needsPurge = (mPurgeCounterShortterm.incrementAndGet() > MIN_ADD_BEFORE_PURGE);
 			else
 				needsPurge = false;
 
 			if (needsPurge) {
 				if (lifeSpan==LifeSpan.LONGTERM)
 					mPurgeCounterLongterm.set(0);
 				else if (lifeSpan==LifeSpan.SHORTTERM)
 					mPurgeCounterShortterm.set(0);
 				scheduleCustomOperation(new RemoveExpired(lifeSpan));
 			}
 		}
 	}
 
 
 	File getCachedFile(CacheKey key, String URL, long itemDate) {
 		//if (URL!=null && !URL.contains("/profile_images/"))
 		//LogManager.logger.v(TAG, " getPicture URL:"+URL + " key:"+key);
 		if (key != null) {
 			mDataLock.lock();
 			try {
 				CacheItem v = getMap().get(key);
 
 				//if (URL!=null && !URL.contains("/profile_images/"))
 				//LogManager.logger.v(TAG, " found cache item "+v);
 				if (v!=null) {
 					try {
 						if (URL!=null && !URL.equals(v.URL)) {
 							// the URL for the cached item changed
 							//LogManager.logger.v(TAG, key+" changed from "+v.URL+" to "+URL+" v.touitID:"+v.touitDate +" touitDate:"+touitDate);
 							if (v.remoteDate < itemDate) {
 								// the item in the Cache is older than this request, the image changed for a newer one
 								// we need to mark the old one as short term with a UUID that has the picture ID inside
 								String dstUUID = getOldPicUUID(key.getUUID(), v.URL);
 								CacheKey oldVersionKey = key.copyWithNewUuid(dstUUID);
 								v = getMap().get(oldVersionKey);
 								if (v==null)
 									// the old version doesn't exist in the cache, copy the current content in there
 									moveCachedFiles(key, oldVersionKey, LifeSpan.SHORTTERM);
 								remove(key); // this one is not valid anymore
								v = null; // don't use the old file
 							} else {
 								// use the old image from the cache
 								String dstUUID = getOldPicUUID(key.getUUID(), URL);
 								key = key.copyWithNewUuid(dstUUID);
 								v = getMap().get(key);
 							}
 						}
 
 						// check if the URL matches, otherwise we have to load it again
 						if (v!=null)
 							return v.path;
 					} catch (SecurityException e) {
 						LogManager.logger.e(TAG, "getPicture exception:" + e.getMessage(), e);
 					} catch (OutOfMemoryError e) {
 						LogManager.logger.w(TAG, "Could not decode image " + URL, e);
 						ooHandler.onOutOfMemoryError(e);
 					}
 				}
 				//else LogManager.logger.i(key.toString()+" not found in "+mData.size()+" cache elements");
 			} finally {
 				mDataLock.unlock();
 			}
 		}
 		return null;
 	}
 
 	public Context getContext() {
 		return mContext;
 	}
 }

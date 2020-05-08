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
 import st.gaw.db.MapEntry;
 import uk.co.senab.bitmapcache.BitmapLruCache;
 import uk.co.senab.bitmapcache.BitmapLruCache.Builder;
 import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
 import uk.co.senab.bitmapcache.BitmapLruCache.RecyclePolicy;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.ReceiverCallNotAllowedException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Environment;
 import android.text.TextUtils;
 
 import com.levelup.FileUtils;
 import com.levelup.picturecache.DownloadManager.JobsMonitor;
 import com.levelup.picturecache.loaders.PrecacheImageLoader;
 import com.levelup.picturecache.loaders.RemoteViewLoader;
 import com.levelup.picturecache.loaders.ViewLoader;
 
 /**
  * base class to use the picture cache to load images and keep a persistent cache 
  */
 public abstract class PictureCache extends InMemoryHashmapDb<CacheKey,CacheItem> implements JobsMonitor {
 
 	public static final String LOG_TAG = "PictureCache";
 	final static boolean DEBUG_CACHE = false & BuildConfig.DEBUG;
 
 	/**
 	 * How many new items need to be added to the database before a purge is done
 	 */
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
 
 	final BitmapLruCache mBitmapCache;
 
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
 
 		final String url = c.getString(indexURL);
 
 		if (indexRemoteDate == -1) {
 			// updating from an old DB
 			indexRemoteDate = c.getColumnIndex("TOUIT_ID");
 
 			int indexPathRounded = c.getColumnIndex("PATHR");
 			int indexHeight = c.getColumnIndex("HEIGHT");
 			int indexWidthBased = c.getColumnIndex("WIBASE");
 
 			String path = c.getString(indexPath);
 			String pathr = c.getString(indexPathRounded);
 			boolean widthBased;
 			if (indexWidthBased < 0)
 				widthBased = false;
 			else
 				widthBased = c.getInt(indexWidthBased) != 0;
 
 			if (!TextUtils.isEmpty(path)) {
 				CacheItem val = new CacheItem(new File(path), url);
 				if (val.path.exists()) {
 					val.lifeSpan = LifeSpan.fromStorage(c.getInt(indexType));
 					val.remoteDate = c.getLong(indexRemoteDate);
 					val.lastAccessDate = c.getLong(indexDate);
 
 					CacheKey key = CacheKey.newUUIDBasedKey(c.getString(indexUUID), c.getInt(indexHeight), widthBased, StorageType.AUTO, null);
 
 					put(key, val);
 				} else {
 					if (DEBUG_CACHE) LogManager.logger.d(LOG_TAG, "missing cache file for undated item "+path);
 				}
 			}
 
 			if (!TextUtils.isEmpty(pathr)) {
 				CacheItem val = new CacheItem(new File(pathr), url);
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
 			final CacheKey key = CacheKey.unserialize(c.getString(indexUUID));
 			final String path = c.getString(indexPath);
 			if (TextUtils.isEmpty(path)) {
				LogManager.logger.w(LOG_TAG, "trying to load an empty cache item for "+c.getString(indexURL));
 				return null;
 			}
 			File picSrc = new File(path);
			if (!picSrc.exists() || !picSrc.isFile()) {
				LogManager.logger.w(LOG_TAG, "trying to load a missing file for "+c.getString(indexURL));
				return null;
			}
			CacheItem val = new CacheItem(picSrc, c.getString(indexURL));
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
 			LogManager.logger.w(LOG_TAG, "cache item has an empty path :"+data.getKey()+" / "+data.getValue());
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
 		return new String[] { key.serialize() };
 	}
 
 	/**
 	 * Constructor of a PictureCache
 	 * @param context Context of the application, may also be used to get a {@link ContentResolver}
 	 * @param postHandler Handler to run some code in the UI thread and also determine if we're in the UI thread or not
 	 * @param logger A {@link Logger} object used to send all the logs generated inside the cache, may be null
 	 * @param ooHandler A {@link OutOfMemoryHandler} object used to notify when we are short on memory, may be null
 	 * @param bitmapCacheSize The size to use in memory for the Bitmaps cache, 0 for no memory cache, -1 for heap size based
 	 */
 	protected PictureCache(Context context, UIHandler postHandler, Logger logger, OutOfMemoryHandler ooHandler, int bitmapCacheSize) {
 		super(context, DATABASE_NAME, DATABASE_VERSION, logger);
 
 		LogManager.setLogger(logger==null ? new LogManager.LoggerDefault() : logger);
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
 		if (bitmapCacheSize==0)
 			this.mBitmapCache = null;
 		else {
 			Builder builder = new BitmapLruCache.Builder(context).
 					setDiskCacheEnabled(false)
 					.setMemoryCacheEnabled(true)
 					.setRecyclePolicy(RecyclePolicy.DISABLED);
 			if (bitmapCacheSize < 0)
 				builder.setMemoryCacheMaxSizeUsingHeapSize();
 			else
 				builder.setMemoryCacheMaxSize(bitmapCacheSize);
 			this.mBitmapCache = builder.build();
 		}
 
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
 				if (newdir == null)
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
 		LogManager.logger.w(LOG_TAG, "Upgrading PictureCache from " + oldVersion + " to " + newVersion);
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
 			LogManager.logger.e(LOG_TAG, "getTempDir() cannot access the dir ", e);
 		} catch (IOException e) {
 			LogManager.logger.e(LOG_TAG, "getTempDir() cannot access the dir ", e);
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
 
 	public File getPictureDir() {
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
 				LogManager.logger.e(LOG_TAG, "getPictureDir() cannot access the dir ", e);
 			}
 		}
 		return dstDir;
 	}
 
 	private void assertFolderExists() throws IOException, SecurityException {
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
 					if (oldFolder != null) {
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
 			LogManager.logger.e(LOG_TAG, "getCacheSize failed", e);
 		} finally {
 			mDataLock.unlock();
 		}
 		return result;
 	}
 
 	private Entry<CacheKey, CacheItem> getCacheOldestEntry(LifeSpan lifeSpan) {
 		// LogManager.logger.d(TAG, "getCacheOldest in");
 		Entry<CacheKey, CacheItem> result = null;
 		for (Entry<CacheKey, CacheItem> entry : getMap().entrySet()) {
 			final CacheItem item = entry.getValue();
 			if (lifeSpan==item.lifeSpan && (result==null || result.getValue().lastAccessDate > item.lastAccessDate))
 				result = entry;
 		}
 		// LogManager.logger.e(TAG, "getCacheOldest out with "+result);
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
 			if (lifeSpan != null)
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
 				if (MaxSize != 0 && TotalSize > MaxSize) {
 					// make room in the DB/cache for this new element
 					while (TotalSize > MaxSize) {
 						//if (type != k.getValue().type) continue;
 						//long deleted = 0;
 						Entry<CacheKey, CacheItem> entry;
 						cache.mDataLock.lock();
 						try {
 							entry = cache.getCacheOldestEntry(lifeSpan);
 							if (entry == null)
 								break;
 						} finally {
 							cache.mDataLock.unlock();
 						}
 
 						if (DEBUG_CACHE) LogManager.logger.i(LOG_TAG, "remove "+entry+" from the cache for "+lifeSpan);
 						CacheItem item = cache.remove(entry.getKey());
 						if (item != null) {
 							File f = item.path;
 							if (f != null && f.exists()) {
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
 				LogManager.logger.w(LOG_TAG, "can't make room for type:"+lifeSpan,e);
 			}
			//LogManager.logger.d(TAG, "makeroom done");
 		}
 	}
 
 	static String keyToBitmapCacheKey(CacheKey key, String url, PictureLoaderHandler loader) {
 		final StringBuilder bitmapKey = new StringBuilder(key.getUUID());
 		bitmapKey.append(url);
 		if (loader != null) {
 			if (loader.getStorageTransform() != null)
 				bitmapKey.append(loader.getStorageTransform().getVariantPostfix());
 			if (loader.getDisplayTransform() != null)
 				bitmapKey.append(loader.getDisplayTransform().getVariant());
 		}
 		return bitmapKey.toString();
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
 			if (DEBUG_CACHE) LogManager.logger.d(LOG_TAG, "getting picture "+URL+" into "+loader+" key:"+key);
 			if (TextUtils.isEmpty(URL)) {
 				// get the URL matching the UUID if we don't have a forced one
 				CacheItem v = getMap().get(key);
 				if (v!=null)
 					URL = v.URL;
 				//LogManager.logger.i("no URL specified for "+key+" using "+URL);
 			}
 			if (TextUtils.isEmpty(URL)) {
 				LogManager.logger.i(LOG_TAG, "no URL specified/known for "+key+" using default");
 				removePictureLoader(loader, null);
 				loader.drawDefaultPicture(null, postHandler, mBitmapCache);
 				return;
 			}
 
 			//LogManager.logger.v(TAG, "load "+URL+" in "+target+" key:"+key);
 			String wasPreviouslyLoading = loader.setLoadingURL(URL, mBitmapCache); 
 			if (URL.equals(wasPreviouslyLoading)) {
 				if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, loader+" no need to draw anything");
 				return; // no need to do anything the image is the same or downloading for it
 			}
 
 			if (wasPreviouslyLoading!=null) {
 				// cancel the loading of the previous URL for this loader
 				mJobManager.cancelDownloadForLoader(loader, wasPreviouslyLoading);
 			}
 
 			/*if (URL.startsWith("android.resource://")) {
 			URL = URL.substring(19);
 			int resId = Integer.valueOf(URL.substring(URL.indexOf('/')+1));
 			target.setImageResource(resId);
 			return;
 		}*/
 
 			key = getStoredKey(key, URL, itemDate);
 
 			final String bitmapCacheKey = mBitmapCache!=null ? keyToBitmapCacheKey(key, URL, loader) : null;
 			if (mBitmapCache!=null) {
 				CacheableBitmapDrawable cachedBmp = mBitmapCache.get(bitmapCacheKey);
 				if (cachedBmp!=null) {
 					if (!cachedBmp.getBitmap().isRecycled()) {
 						if (DEBUG_CACHE) LogManager.logger.d(LOG_TAG, "using cached bitmap for URL "+URL+" key:"+bitmapCacheKey);
 						loader.drawBitmap(cachedBmp, URL, postHandler, mBitmapCache);
 						return;
 					}
 					LogManager.logger.w(LOG_TAG, "try to draw bitmap "+key+" already recycled in "+loader+" URL:"+URL);
 				}
 			}
 
 			File file = getCachedFile(key);
 			if (file!=null) {
 				if (!file.exists() || !file.isFile()) {
 					LogManager.logger.w(LOG_TAG, "File "+file+" disappeared for "+key);
 					remove(key);
 				}
 				else if (loader.canDirectLoad(file, postHandler)) {
 					try {
 						Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
 						if (bmp!=null) {
 							if (null != loader.getDisplayTransform())
 								bmp = loader.getDisplayTransform().transformBitmap(bmp);
 
 							Drawable cachedBmp = null;
 							if (mBitmapCache!=null && loader.canKeepBitmapInMemory(bmp))
 								cachedBmp = mBitmapCache.put(bitmapCacheKey, bmp);
 							if (cachedBmp==null)
 								cachedBmp = new BitmapDrawable(mContext.getResources(), bmp);
 							if (DEBUG_CACHE) LogManager.logger.d(LOG_TAG, "using direct file for URL "+URL+" file:"+file);
 							loader.drawBitmap(cachedBmp, URL, postHandler, mBitmapCache);
 							return;
 						}
 					} catch (OutOfMemoryError e) {
 						loader.drawDefaultPicture(URL, postHandler, mBitmapCache);
 						LogManager.logger.w(LOG_TAG, "can't decode "+file,e);
 						ooHandler.onOutOfMemoryError(e);
 						return;
 					}
 				}
 			}
 
 			loader.drawDefaultPicture(URL, postHandler, mBitmapCache);
 
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
 	 * @param handler the handler used to display the loaded bitmap/placeholder on the target, see {@link ViewLoader}, {@link RemoteViewLoader} or {@link PrecacheImageLoader}
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
 			LogManager.logger.d(LOG_TAG, "can't load picture", e);
 		}
 	}
 
 	/**
 	 * helper method to load a width based picture using the cache
 	 * @see {@link PictureJob}
 	 * @param handler the handler used to display the loaded bitmap/placeholder on the target, see {@link ViewLoader}, {@link RemoteViewLoader} or {@link PrecacheImageLoader}
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
 			LogManager.logger.d(LOG_TAG, "can't load picture", e);
 		}
 	}
 
 	/**
 	 * stop loading for that {@link loader} target, keep the target marked for the previously loading URL
 	 * @param loader
 	 * @param oldURL
 	 */
 	public void cancelPictureLoader(PictureLoaderHandler loader, String oldURL) {
 		if (loader != null)
 			mJobManager.cancelDownloadForLoader(loader, oldURL);
 	}
 
 	/**
 	 * stop loading for that {@link loader} target, reset loading URL marked on that target
 	 * @param loader
 	 * @param oldURL
 	 */
 	public void removePictureLoader(PictureLoaderHandler loader, String oldURL) {
 		if (loader != null) {
 			if (DEBUG_CACHE) LogManager.logger.i(LOG_TAG, "removePictureLoader "+loader+" with old URL "+oldURL);
 			loader.setLoadingURL(null, mBitmapCache);
 			mJobManager.cancelDownloadForLoader(loader, oldURL);
 		}
 	}
 
 	public boolean saveInGallery(String UUID, int width, boolean widthBased, boolean Rounded, StorageType extensionMode) throws IOException, SecurityException {
 		boolean succeeded = false;
 		CacheKey key = CacheKey.newUUIDBasedKey(UUID, width, widthBased, extensionMode, Rounded?"_r":null);
 		mDataLock.lock();
 		try {
 			CacheItem v = getMap().get(key);
 			if (v != null && v.path != null) {
 				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 					File dst = new File(getPictureDir(), key.getFilename());
 					FileUtils.copyFile(v.path, dst, LOG_TAG);
 					succeeded = true;
 
 					try {
 						GalleryScanner saver = new GalleryScanner(getContext());
 						saver.scan(dst);
 					} catch (ReceiverCallNotAllowedException e) {
 						LogManager.logger.w(LOG_TAG, "could not start the gallery scanning");
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
 			LogManager.logger.e(LOG_TAG, "clearCache exception", e);
 		} catch (IOException e) {
 			LogManager.logger.e(LOG_TAG, "clearCache could not recreate the cache folder", e);
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
 			if (cacheItem != null) {
 				File file = cacheItem.path;
 				if (file != null && file.exists())
 					return file.getAbsolutePath();
 			}
 		} finally {
 			mDataLock.unlock();
 		}
 		return null;
 	}
 
 	private boolean moveCachedFiles(CacheKey srcKey, CacheKey dstKey, LifeSpan lifeSpan) {
 		if (getMap().containsKey(dstKey)) {
			LogManager.logger.d(LOG_TAG, "item "+dstKey+" already exists in the DB");
 			return false;
 		}
 
 		try {
 			CacheItem v = getMap().get(srcKey);
 			if (v != null) {
 				File src = v.path;
 				if (src != null && src.exists()) {
 					File dst = getCachedFilepath(dstKey);
 					dst.delete();
 
 					if (src.renameTo(dst)) {
 						v = v.copyWithNewPath(dst);
 						v.lifeSpan = lifeSpan;
 						return put(dstKey, v) != null;
 					} else {
 						LogManager.logger.e(LOG_TAG, "Failed to rename path "+src+" to "+dst);
 					}
 					//else LogManager.logger.d(TAG, false, "keep the old version of "+newKey);
 				}
 			}
 		} catch (Throwable e) {
 			LogManager.logger.e(LOG_TAG, "failed to copy " + srcKey + " to " + dstKey, e);
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
 	public void onNewBitmapLoaded(HashMap<CacheVariant,Drawable> newBitmaps, String url, long remoteDate, LifeSpan lifeSpan) {
 		// handle the storing and adding to the cache
 		// save the bitmap for later use
 		long fileSizeAdded = 0;
 		for (CacheVariant variant : newBitmaps.keySet()) {
 			try {
 				if (variant.path.exists())
 					variant.path.delete();
 				FileOutputStream fos = new FileOutputStream(variant.path, false);
 				Drawable drawable = newBitmaps.get(variant);
 				Bitmap bmp = ViewLoader.drawableToBitmap(drawable);
 				bmp.compress(variant.key.getCompression(), variant.key.getCompRatio(), fos);
 				fos.close();
 
 				if (DEBUG_CACHE) LogManager.logger.d(LOG_TAG, "stored "+variant.key+" from "+url+" as "+variant.path); 
 
 				mDataLock.lock();
 				try {
 					CacheItem val = getMap().get(variant.key);
 					if (val != null) {
 						if (val.remoteDate < remoteDate)
 							val.remoteDate = remoteDate;
 
 						if (val.lifeSpan.compare(lifeSpan) < 0)
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
 				LogManager.logger.i(LOG_TAG, "failed to save "+url+" as "+variant, e);
 			}
 		}
 
 		//LogManager.logger.i("BitmapLoaded outFile:"+outFile);
 		if (fileSizeAdded != 0) {
 			final boolean needsPurge;
 			if (lifeSpan == LifeSpan.LONGTERM)
 				needsPurge = (mPurgeCounterLongterm.incrementAndGet() > MIN_ADD_BEFORE_PURGE);
 			else if (lifeSpan == LifeSpan.SHORTTERM)
 				needsPurge = (mPurgeCounterShortterm.incrementAndGet() > MIN_ADD_BEFORE_PURGE);
 			else
 				needsPurge = false;
 
 			if (needsPurge) {
 				if (lifeSpan == LifeSpan.LONGTERM)
 					mPurgeCounterLongterm.set(0);
 				else if (lifeSpan == LifeSpan.SHORTTERM)
 					mPurgeCounterShortterm.set(0);
 				scheduleCustomOperation(new RemoveExpired(lifeSpan));
 			}
 		}
 	}
 
 	/**
 	 * Get the correct storage key for the given key, URL and itemDate.
 	 * It may differ from the source key if it's referring an older or newer version of the key/URL combo compared to the one already stored
 	 * @param key The source key we want to use in the database
 	 * @param URL The URL associated with the key in storage
 	 * @param itemDate The date corresponding to the key/URL combo (can be 0)
 	 * @return a key corresponding to the right item in the database to load/store
 	 */
 	private CacheKey getStoredKey(CacheKey key, String URL, long itemDate) {
 		if (key != null) {
 			mDataLock.lock();
 			try {
 				CacheItem v = getMap().get(key);
 
 				//if (URL!=null && !URL.contains("/profile_images/"))
 				if (v != null) {
 					if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, key+" found cache item "+v+" for key "+key+" URL:"+URL);
 					try {
 						if (URL != null && !URL.equals(v.URL)) {
 							// the URL for the cached item changed
 							if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, key+" changed from "+v.URL+" to "+URL+" remoteDate:"+v.remoteDate+" was "+itemDate);
 							if (v.remoteDate <= itemDate) { // '=' favor the newer url when dates are 0
 								// the item in the Cache is older than this request, the image changed for a newer one
 								// we need to mark the old one as short term with a UUID that has the picture ID inside
 								String deprecatedUUID = getOldPicUUID(key.getUUID(), v.URL);
 								CacheKey oldVersionKey = key.copyWithNewUuid(deprecatedUUID);
 								// move the current content to the deprecated key
 								moveCachedFiles(key, oldVersionKey, LifeSpan.SHORTTERM);
 								if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, key+" moved to "+oldVersionKey);
 							} else {
 								// use the old image from the cache with that URL
 								String dstUUID = getOldPicUUID(key.getUUID(), URL);
 								key = key.copyWithNewUuid(dstUUID);
 								if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, key+" will be used for that old version");
 							}
 						}
 					} catch (SecurityException e) {
 						LogManager.logger.e(LOG_TAG, "getPicture exception:" + e.getMessage(), e);
 					} catch (OutOfMemoryError e) {
 						LogManager.logger.w(LOG_TAG, "Could not decode image " + URL, e);
 						ooHandler.onOutOfMemoryError(e);
 					}
 				}
 				//else LogManager.logger.i(key.toString()+" not found in "+mData.size()+" cache elements");
 			} finally {
 				mDataLock.unlock();
 			}
 		}
 		return key;
 	}
 
 	File getCachedFile(CacheKey key) {
 		//if (URL!=null && !URL.contains("/profile_images/"))
 		//LogManager.logger.v(TAG, " getPicture URL:"+URL + " key:"+key);
 		if (key != null) {
 			mDataLock.lock();
 			try {
 				CacheItem v = getMap().get(key);
 
 				//if (URL!=null && !URL.contains("/profile_images/"))
 				if (DEBUG_CACHE) LogManager.logger.v(LOG_TAG, key+" found cache item "+v);
 				if (null!=v && null!=v.path && v.path.exists() && v.path.isFile()) {
 					return v.path;
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
 
 	static String resourcePath;
 
 	synchronized Drawable loadResourceDrawable(String url) {
 		if (resourcePath==null)
 			resourcePath = "android.resource://"+mContext.getPackageName()+"/";
 		if (!url.startsWith(resourcePath))
 			return null;
 		return mContext.getResources().getDrawable(Integer.valueOf(url.substring(resourcePath.length())));
 	}
 }

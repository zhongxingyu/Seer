 /**
  * 
  */
 package com.levelup.picturecache.internal;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.util.FloatMath;
 
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.NetworkLoader;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureLoaderHandler;
 import com.levelup.picturecache.loaders.ViewLoader;
 
 public class BitmapDownloader implements Runnable {
 
 	private static final boolean DEBUG_BITMAP_DOWNLOADER = false;
 
 	private static class DownloadTarget {
 		final PictureLoaderHandler loadHandler;
 		final CacheKey mKey;
 		File fileInCache;
 		DownloadTarget(PictureLoaderHandler handler, CacheKey key) {
 			this.loadHandler = handler;
 			this.mKey = key;
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (this==o) return true;
 			if (!(o instanceof DownloadTarget)) return false;
 			DownloadTarget d = (DownloadTarget) o;
 			return mKey.equals(d.mKey) && loadHandler.equals(d.loadHandler);
 		}
 
 		@Override
 		public int hashCode() {
 			return mKey.hashCode() * 31 + loadHandler.hashCode();
 		}
 
 		@Override
 		public String toString() {
 			return "DownloadTarget:"+loadHandler;
 		}
 	}
 
 	final String mURL;
 	final NetworkLoader networkLoader;
 	final Object mCookie;
 	final PictureCache mCache;
 	final CopyOnWriteArrayList<DownloadTarget> mTargets = new CopyOnWriteArrayList<DownloadTarget>();
 	final DownloadManager mMonitor;
 
 	// locked by mTargets
 	/** see {@link LifeSpan} values */
 	private LifeSpan mLifeSpan;
 	private long mItemDate;
 
 	private boolean mCanDownload;
 	private boolean mAborting;
 
 	private static final int CONNECT_TIMEOUT_DL = 10000; // 10s
 
 	BitmapDownloader(String URL, NetworkLoader loader, Object cookie, PictureCache cache, DownloadManager monitor) {
 		if (URL==null) throw new NullPointerException("How are we supposed to download a null URL?");
 		this.mURL = URL;
 		this.networkLoader = loader;
 		this.mCookie = cookie;
 		this.mCache = cache;
 		this.mMonitor = monitor;
 	}
 
 	LifeSpan getLifeSpan() {
 		return mLifeSpan;
 	}
 	long getItemDate() {
 		return mItemDate;
 	}
 
 	@Override
 	public String toString() {
 		return "BitmapLoader:"+mURL+"@"+super.hashCode();
 	}
 
 	public void run() {
 		//LogManager.getLogger().v( "start image load in cache: " + mURL);
 		final HashMap<CacheKey,Drawable> targetBitmaps = new HashMap<CacheKey, Drawable>();
 		final HashMap<CacheVariant,Drawable> targetNewBitmaps = new HashMap<CacheVariant, Drawable>();
 		File downloadToFile = null;
 		boolean downloaded = false;
 		try {
 			BitmapFactory.Options tmpFileOptions = new BitmapFactory.Options();
 			tmpFileOptions.inJustDecodeBounds = false;
 
 			for (int i=0;i<mTargets.size();++i) {
 				DownloadTarget target = mTargets.get(i);
 				checkAbort();
 
 				target.fileInCache = mCache.getCachedFile(target.mKey);
 				boolean bitmapWasInCache = target.fileInCache!=null;
 				if (!bitmapWasInCache) {
 					// we can't use the older version, download the file and create the stored file again
 					if (target.fileInCache!=null)
 						target.fileInCache.delete();
 					target.fileInCache = mCache.getCachedFilepath(target.mKey);
 					if (downloadToFile==null && mCanDownload) {
 						if (target.loadHandler.getStorageTransform()==null)
 							downloadToFile = target.fileInCache;
 						else
 							downloadToFile = new File(mCache.getAvailaibleTempDir(), "tmp_"+target.mKey.getFilename());
 					}
 				}
 
 				if (target.fileInCache!=null) {
 					Drawable displayDrawable;
 					if (!bitmapWasInCache) {
 						displayDrawable = null;
 					} else if (mCache.getBitmapCache()!=null) {
 						displayDrawable = mCache.getBitmapCache().put(keyToBitmapCacheKey(target.mKey, mURL, target.loadHandler), target.fileInCache, getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.mKey));
 					} else {
 						displayDrawable = new BitmapDrawable(mCache.getContext().getResources(), target.fileInCache.getAbsolutePath());
 					}
 
 					if (displayDrawable==null) {
 						// we don't have that final file yet, use the download file to generate it
 						displayDrawable = targetBitmaps.get(target.mKey);
 						if (displayDrawable==null) {
 							displayDrawable = loadResourceDrawable(mURL);
 
 							if (displayDrawable!=null) {
 								if (target.loadHandler.getStorageTransform()!=null)
 									displayDrawable = new BitmapDrawable(target.loadHandler.getStorageTransform().transformBitmapForStorage(ViewLoader.drawableToBitmap(displayDrawable)));
 								else
 									bitmapWasInCache = true; // do not store the drawable as a bitmap as it is equal to the source
 							}
 						}
 
 						if (displayDrawable==null && downloadToFile!=null && !downloaded) {
 							try {
 								downloadInTempFile(downloadToFile);
 								// we need the dimensions of the downloaded file
 								tmpFileOptions.inJustDecodeBounds = true;
 								BitmapFactory.decodeFile(downloadToFile.getAbsolutePath(), tmpFileOptions);
 								if (DEBUG_BITMAP_DOWNLOADER && tmpFileOptions.outHeight <= 0) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" failed to get dimensions from "+downloadToFile);
 								downloaded = true;
 							} finally {
 								if (!downloaded && downloadToFile!=target.fileInCache)
 									downloadToFile.delete();
 							}
 							checkAbort();
 						}
 
 						if (downloaded) {
 							Bitmap bitmap;
 							if (mCache.getBitmapCache()!=null) {
 								CacheableBitmapDrawable cachedDrawable = mCache.getBitmapCache().put(keyToBitmapCacheKey(target.mKey, mURL, target.loadHandler), downloadToFile, getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.mKey));
 								bitmap = cachedDrawable.getBitmap();
 							} else {
 								bitmap = BitmapFactory.decodeFile(downloadToFile.getAbsolutePath(), getOutputOptions(tmpFileOptions.outWidth, tmpFileOptions.outHeight, target.mKey));
 							}
 							if (bitmap!=null) {
 								int finalHeight = target.mKey.getBitmapHeight(bitmap.getWidth(), bitmap.getHeight());
 								if (finalHeight!=0 && finalHeight != bitmap.getHeight()) {
 									//LogManager.getLogger().v(" source size:"+bmp.getWidth()+"x"+bmp.getHeight());
 									Bitmap newBmp = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * finalHeight) / bitmap.getHeight(), finalHeight, true);
 									/*if (bitmap!=newBmp)
 										bitmap.recycle();*/
 									bitmap = newBmp;
 								}
 
 								if (target.loadHandler.getStorageTransform()!=null)
 									bitmap = target.loadHandler.getStorageTransform().transformBitmapForStorage(bitmap);
 
 								displayDrawable = new BitmapDrawable(mCache.getContext().getResources(), bitmap);
 							}
 						}
 					}
 
 					if (displayDrawable!=null) {
 						targetBitmaps.put(target.mKey, displayDrawable);
 						if (!bitmapWasInCache) {
 							CacheVariant variant = new CacheVariant(target.fileInCache, target.mKey);
 							targetNewBitmaps.put(variant, displayDrawable);
 						}
 					} else {
 						if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" failed to get a bitmap for:"+target);
 						targetBitmaps.remove(target.mKey);
 					}
 				}
 
 				if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" target:"+target+" fileInCache:"+target.fileInCache+" bitmap:"+targetBitmaps.get(target.mKey));
 			}
 		} catch (OutOfMemoryError e) {
 			mCache.getOutOfMemoryHandler().onOutOfMemoryError(e);
 			LogManager.getLogger().e(PictureCache.LOG_TAG, "Failed to load " + mURL, e);
 			/*} catch (InterruptedException e) {
 			LogManager.getLogger().e(PictureCache.TAG, "Interrupted while loading " + mURL, e);*/
 		} catch (DownloadFailureException e) {
 			// do nothing
 		} catch (Throwable e) {
 			LogManager.getLogger().e(PictureCache.LOG_TAG, "exception on "+mURL, e);
 		} finally {
 			try {
 				// tell the monitor we are done
 				//LogManager.getLogger().i(PictureCache.TAG, "finished download thread for " + mURL + " bmp:"+bmp + " rbmp:"+rbmp);
 				//LogManager.getLogger().i(PictureCache.TAG, "send display bitmap "+mURL+" aborted:"+abortRequested.get()+" size:"+reqTargets.size());
 				//LogManager.getLogger().i(PictureCache.TAG, "ViewUpdate loop "+mURL+" aborted:"+abortRequested.get()+" size:"+reqTargets.size()+" bmp:"+bmp+" rbmp:"+rbmp);
 				synchronized (mTargets) {
 					if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" finished loading targets:"+mTargets+" bitmaps:"+targetBitmaps);
 
 					mAborting = true; // after this point new targets are not OK for this job
 					for (DownloadTarget target : mTargets) {
 						//LogManager.getLogger().i(PictureCache.TAG, false, "ViewUpdate "+mURL);
 						PictureLoaderHandler j = target.loadHandler;
 						Drawable drawable = targetBitmaps.get(target.mKey);
 
 						if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+" display "+drawable+" in "+target.loadHandler+" file:"+target.fileInCache+" key:"+target.mKey);
 						if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().v(PictureCache.LOG_TAG, this+"  targets:"+mTargets+" bitmaps:"+targetBitmaps);
 						//LogManager.getLogger().i(PictureCache.TAG, "display "+mURL+" in "+j+" abort:"+abortRequested);
 						if (drawable!=null) {
 							Bitmap bitmap = ViewLoader.drawableToBitmap(drawable);
 							if (j.getDisplayTransform()!=null)
 								bitmap = j.getDisplayTransform().transformBitmap(bitmap);
 
 							Drawable cacheableBmp;
 							if (drawable instanceof BitmapDrawable && ((BitmapDrawable) drawable).getBitmap()==bitmap)
 								cacheableBmp = drawable;
 							else
 								cacheableBmp = new BitmapDrawable(mCache.getContext().getResources(), bitmap);
 							j.drawBitmap(cacheableBmp, mURL, mCookie, mCache.getBitmapCache(), false);
 						} else
 							j.drawErrorPicture(mURL, mCache.getBitmapCache());
 					}
 					mTargets.clear();
 				}
 
 				if (mMonitor!=null)
 					mMonitor.onJobFinishedWithNewBitmaps(this, targetNewBitmaps);
 			} finally {
 				if (downloadToFile!=null)
 					downloadToFile.delete();
 			}
 		}
 	}
 
 	/**
 	 * add a handler for when the URL is downloaded and start the download+processing if it wasn't started
 	 * @param loadHandler
 	 * @param key
 	 * @param itemDate
 	 * @param lifeSpan
 	 * @return
 	 */
 	boolean addTarget(PictureLoaderHandler loadHandler, CacheKey key, long itemDate, LifeSpan lifeSpan)
 	{
 		DownloadTarget newTarget = new DownloadTarget(loadHandler, key);
 		//LogManager.getLogger().i(PictureCache.TAG, "add recipient view "+view+" for " + mURL);
 		if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" addTarget "+loadHandler+" key:"+key);
 		synchronized (mTargets) {
 			if (mAborting) {
 				if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().w(PictureCache.LOG_TAG, this+ " is aborting");
 				return false;
 			}
 
 			if (mTargets.contains(newTarget)) {
 				// TODO: update the rounded/rotation status
 				if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" target "+newTarget+" already pending");
 				return true;
 			}
 			mTargets.add(newTarget);
 
 			mCanDownload |= loadHandler.isDownloadAllowed();
 
 			if (mItemDate < itemDate)
 				mItemDate = itemDate;
 
 			if (mLifeSpan==null)
 				mLifeSpan = lifeSpan;
 			else if (mLifeSpan.compare(lifeSpan)<0)
 				mLifeSpan = lifeSpan;
 		}
 		return true;
 	}
 
 	boolean removeTarget(PictureLoaderHandler target) {
 		synchronized (mTargets) {
 
 			boolean deleted = false;
 			if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" removeTarget "+target);
 			for (int i=0;i<mTargets.size();++i) {
 				if (mTargets.get(i).loadHandler.equals(target)) {
 					deleted = mTargets.remove(i)!=null;
 					break;
 				}
 			}
 
 			if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().e(PictureCache.LOG_TAG, this+" removeTarget "+target+" = "+deleted+" remains:"+mTargets.size());
 			if (deleted) {
 				//LogManager.getLogger().v(" deleted job view:"+target+" for "+mURL);
 				//target.setLoadingURL(mCache, mURL);
 				target.drawDefaultPicture(mURL, mCache.getBitmapCache());
 			}
 			//else LogManager.getLogger().i(PictureCache.TAG, " keep downloading URL:" + mURL + " remaining views:" + reqViews.size() + " like view:"+reqViews.get(0));
 			return deleted;
 		}
 	}
 
 	private BitmapFactory.Options getOutputOptions(int srcWidth, int srcHeight, CacheKey key) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 		if (srcHeight <= 0) {
 			LogManager.getLogger().i(PictureCache.LOG_TAG, "could not get the dimension for " + mURL+" use raw decoding");
 		} else {
 			int finalHeight = key.getBitmapHeight(srcWidth, srcHeight);
 
 			if (finalHeight>0 && srcHeight > finalHeight*2) {
 				//LogManager.getLogger().e(PictureCache.TAG, " Picture scaling by: " + scale +" from Height:" + opts.outHeight + " to "+finalHeight+" for "+mURL);
 				opts.inSampleSize = (int) FloatMath.floor((float)srcHeight / finalHeight);
 			}
 			else opts.inSampleSize = 1;
 		}
 		//opts.inInputShareable = true;
 		//opts.inPurgeable = true;
 
 		return opts;
 	}
 
 	private static class AbortDownload extends DownloadFailureException {
 		private static final long serialVersionUID = 5568245153235248681L;
 	}
 
 	/**
 	 * @throws AbortDownload if we should not download or decode any further
 	 */
 	private void checkAbort() throws AbortDownload {
 		synchronized (mTargets) {
 			if (mTargets.isEmpty()) {
 				if (DEBUG_BITMAP_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, this+ " no more targets, aborting");
 				mAborting = true;
 				throw new AbortDownload();
 			}
 		}
 	}
 
 	protected boolean isEmpty() {
 		synchronized (mTargets) {
 			return mTargets.isEmpty();
 		}
 	}
 
 	private void downloadInTempFile(File tmpFile) throws DownloadFailureException {
 		//LogManager.getLogger().i(PictureCache.TAG, "loading "+mURL);
 		InputStream is = null;
 		try {
 			try {
 				is = mCache.getContext().getContentResolver().openInputStream(Uri.parse(mURL));
 				//LogManager.getLogger().v("using the content resolver for "+mURL);
 			} catch (FileNotFoundException e) {
 				//LogManager.getLogger().d(PictureCache.TAG, false, "cache error trying ContentResolver on "+mURL);
 				if (null!=networkLoader)
 					is = networkLoader.loadURL(mURL);
 
 				if (null==is) {
 					URL url = new URL(mURL);
 					URLConnection conn = url.openConnection();
 					conn.setConnectTimeout(CONNECT_TIMEOUT_DL);
 					conn.setUseCaches(false);
 					conn.setRequestProperty("Accept-Encoding", "identity");
 					//LogManager.getLogger().e(PictureCache.TAG, conn.getContentEncoding()+" encoding for "+mURL);
 					checkAbort();
 					try {
 						is = conn.getInputStream();
 					} catch (FileNotFoundException ee) {
 						throw new DownloadFailureException("cache URL not found "+mURL, e);
 					} catch (Exception ee) {
 						throw new DownloadFailureException("cache error opening "+mURL, e);
 					}
 				}
 			}
 
 			if (is==null) {
 				throw new DownloadFailureException("impossible to get a stream for "+mURL);
 			}
 
 			checkAbort();
 			// store the stream in a temp file
 			BufferedInputStream bis = new BufferedInputStream(is);
 			FileOutputStream out = new FileOutputStream(tmpFile);
 			try {
 				byte[] data = new byte[1422];
 				int readAmount = is.read(data);
 				while (readAmount >= 0) {
 					out.write(data, 0, readAmount);
 					checkAbort();
 					readAmount = is.read(data);
 				}
 			} finally {
 				bis.close();
 				out.flush();
 				out.close();
 			}
 
 			//LogManager.getLogger().v(" got direct:"+bmp);
 		} catch (MalformedURLException e) {
 			throw new DownloadFailureException("bad URL " + mURL, e);
 		} catch (UnknownHostException e) {
 			throw new DownloadFailureException("host not found in "+mURL, e);
 		} catch (OutOfMemoryError e) {
 			mCache.getOutOfMemoryHandler().onOutOfMemoryError(e);
 			throw new DownloadFailureException("Could not decode image " + mURL, e);
 		} catch (IOException e) {
 			throw new DownloadFailureException("Could not read " + mURL, e);
 		} finally {
 			try {
 				if (is!=null)
 					is.close();
 			} catch (IOException e) {
 				LogManager.getLogger().e(PictureCache.LOG_TAG, "Could not close " + is, e);
 			}
 		}
 	}
 
 	public static String keyToBitmapCacheKey(CacheKey key, String url, PictureLoaderHandler loader) {
		final StringBuilder bitmapKey = new StringBuilder(key.toString());
 		bitmapKey.append(url);
 		if (loader != null) {
 			if (loader.getStorageTransform() != null)
 				bitmapKey.append(loader.getStorageTransform().getVariantPostfix());
 			if (loader.getDisplayTransform() != null)
 				bitmapKey.append(loader.getDisplayTransform().getVariant());
 		}
 		return bitmapKey.toString();
 	}
 
 	private String resourcePath;
 
 	private synchronized Drawable loadResourceDrawable(String url) {
 		if (resourcePath==null)
 			resourcePath = "android.resource://"+mCache.getContext().getPackageName()+"/";
 		if (!url.startsWith(resourcePath))
 			return null;
 		return mCache.getContext().getResources().getDrawable(Integer.valueOf(url.substring(resourcePath.length())));
 	}
 
 }

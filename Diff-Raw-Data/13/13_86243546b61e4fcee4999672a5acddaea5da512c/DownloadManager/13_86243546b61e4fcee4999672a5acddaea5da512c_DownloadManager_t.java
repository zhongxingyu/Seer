 /**
  * 
  */
 package com.levelup.picturecache.internal;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import android.graphics.drawable.Drawable;
 import android.text.TextUtils;
 
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureJob;
 
 public class DownloadManager {
 
 	private static final boolean DEBUG_DOWNLOADER = false;
 
 	private final static int THREAD_POOL_SIZE = 2;
 
 	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(20);
 
 	private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 1, TimeUnit.SECONDS, sPoolWorkQueue) {
 		@Override
 		protected void beforeExecute(Thread t, Runnable r) {
 			super.beforeExecute(t, r);
 			if (r instanceof PictureJobList) {
 				t.setName("PictureDL-"+((PictureJobList) r).url.hashCode());
 			} else {
 				t.setName("PictureDL-"+t);
 			}
 			t.setPriority(Thread.MIN_PRIORITY);
 		}
 	};
 
 	/** Running downloads per URL/joblist pair */
 	private final Hashtable<String, PictureJobList> mDownloadJobs = new Hashtable<String, PictureJobList>();
 	private final PictureCache mCache;
 
 	public DownloadManager(PictureCache pictureCache) {
 		this.mCache = pictureCache;
 	}
 
 	public void addDownloadTarget(PictureJob job) {
 		// find out if that URL is already loading, if so add the view to the recipient
 		boolean isNewJobList = false;
 		PictureJobList jobList = null;
 		synchronized (mDownloadJobs) {
 			// add job by URL
 			jobList = mDownloadJobs.get(job.url);
			isNewJobList = null!=jobList && jobList.isEmpty();
			if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, "add job:"+job+" to downloader:"+jobList+" need restart:"+isNewJobList);
 			final boolean jobAdded = jobList!=null && jobList.addJob(job);
 			if (!jobAdded) {
 				if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, "add new downloader for "+job.url+" key:"+job.key+" job:"+job+" jobs:"+mDownloadJobs);
 				// create a fresh new one if an old one is not ready to accept our loadHandler
 				jobList = new PictureJobList(job, mCache, this);
 				jobList.addJob(job);
 				mDownloadJobs.put(job.url, jobList);
 				isNewJobList = true;
 			}
 		}
 
 		if (isNewJobList)
 			try {
 				threadPool.execute(jobList);
 			} catch (RejectedExecutionException e) {
 				LogManager.getLogger().w(PictureCache.LOG_TAG, "can't execute "+jobList, e);
 			}
 
 		if (DEBUG_DOWNLOADER) {
 			LogManager.getLogger().e(PictureCache.LOG_TAG, "downloader for "+job.url+" = "+jobList+" new task:"+isNewJobList);
 		}
 	}
 
 	/**
 	 * has to be done before a new {@link PictureLoaderHandler.setLoadingNewURL(DownloadManager, String, SimpleLogger)}
 	 * @param job
 	 * @param URL TODO
 	 */
 	public void removeDownloadTarget(PictureJob job, String URL) {
 		if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, "cancelDownloadForLoader for "+URL+" job:"+job);
 
 		synchronized (mDownloadJobs) {
 			if (!TextUtils.isEmpty(URL)) {
 				PictureJobList downloader = mDownloadJobs.get(URL);
 				if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, " cancelDownloadForLoader job:"+job+" found:"+downloader);
 				if (downloader!=null) {
 					//LogManager.getLogger().d(PictureCache.TAG, "cancelDownloadForTarget for URL " + URL+" for "+loader);
 					downloader.removeJob(job);
 					if (downloader.isEmpty())
 						threadPool.remove(downloader);
 				}
				return;
 			}
 
 			// find the target by view
 			//LogManager.getLogger().w(PictureCache.TAG, "cancelDownloadForTarget by key " + loader);
 			Enumeration<PictureJobList> downloaders = mDownloadJobs.elements();
 			while (downloaders.hasMoreElements()) {
 				PictureJobList downloader = downloaders.nextElement();
 				if (downloader.removeJob(job)) {
 					if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, " cancelDownloadForLoader loadHandler:"+job+" deleted on:"+downloader/*+" url:"+url*/);
 					if (downloader.isEmpty())
 						threadPool.remove(downloader);
					return;
 				}
 			}
 		}

 		if (DEBUG_DOWNLOADER) LogManager.getLogger().w(PictureCache.LOG_TAG, "cancelDownloadForLoader do nothing for loadHandler:"+job);
 	}
 
 	void onJobFinishedWithNewBitmaps(PictureJobList downloader, HashMap<CacheVariant,Drawable> newBitmaps) {
 		mCache.onNewBitmapLoaded(newBitmaps, downloader.url, downloader.getItemDate(), downloader.getLifeSpan());
 
 		synchronized (mDownloadJobs) {
 			if (null != mDownloadJobs.remove(downloader.url)) {
 				if (DEBUG_DOWNLOADER) LogManager.getLogger().i(PictureCache.LOG_TAG, "Job Finishing for "+downloader.url + " remaining:"+mDownloadJobs);
 			} else {
 				LogManager.getLogger().w(PictureCache.LOG_TAG, "Unknown job finishing for "+downloader.url + " remaining:"+mDownloadJobs);
 			}
 		}
 	}
 }

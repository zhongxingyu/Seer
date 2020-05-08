 package com.joy.launcher2.wallpaper;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.json.JSONObject;
 
 import com.joy.launcher2.cache.ImageMemoryCache;
 import com.joy.launcher2.cache.JsonFile;
 import com.joy.launcher2.cache.UnLimitedImageFileCache;
 import com.joy.launcher2.network.handler.WallpaperHandler;
 import com.joy.launcher2.network.impl.Service;
 import com.joy.launcher2.network.util.ClientHttp;
 import com.joy.launcher2.util.Constants;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.ColorFilter;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.util.Log;
 import android.widget.ImageView;
 
 /**
  * 通过线程加载图片，获得json，保存文件缓存和内存缓存对象
  * @author huangming
  *
  */
 public class ImageLoader {
 	
 	private static ImageLoader mInstance;
 	private UnLimitedImageFileCache mDiscCache;
 	private ImageMemoryCache mMemoryCache;
 	private boolean mAllowTask = true;
 	private ClientHttp mClientHttp;
 	private Map<String, ImageView> mImageMap;
 	private final static String TAG = "ImageLoader";
 	private final static boolean DEBUG=false;
 	
 	private Map<Integer, WeakReference<Callback>> mCallbacks;
 	private Handler mHandler = new Handler();
 	private ExecutorService mShortTimeExecutors;
 	private ExecutorService mLongTimeExecutors;
 	private JsonFile mJsonFile;
 	
 	private Map<Integer, String> mCacheKey = Collections.synchronizedMap(new HashMap<Integer, String>());
 
 	public static int screenWidth;
 	public static int screenHeight;
 
 	private Map<Integer, ArrayList<WallpaperInfo>> mAllInfos;
 	private List<CategoryInfo> mCategoryInfos;
 	
 
 	private ImageLoader(Context context)
 	{
 		mAllInfos =  Collections.synchronizedMap(new HashMap<Integer, ArrayList<WallpaperInfo>>());
 		mJsonFile = new JsonFile(context);
 		mCategoryInfos = Collections.synchronizedList(new ArrayList<CategoryInfo>());
 	
 		int numCPU = Runtime.getRuntime().availableProcessors();
 		if(DEBUG)Log.e(TAG, " cpu num :" + numCPU);
 		mCallbacks =  Collections.synchronizedMap(new HashMap<Integer, WeakReference<Callback>>());
 		mDiscCache = new UnLimitedImageFileCache(context);
 		mMemoryCache = new ImageMemoryCache(context);
 		mImageMap = Collections.synchronizedMap(new HashMap<String, ImageView>());
 		mShortTimeExecutors = Executors.newSingleThreadExecutor();
 		mLongTimeExecutors = Executors.newFixedThreadPool(Math.max((numCPU - 1), 1));
 		mClientHttp = new ClientHttp();
 	}
 	
 	public void setScreenSize(int width, int height)
 	{
 		if(screenWidth > 0 && screenHeight > 0)return;
 		screenWidth = width;
 		screenHeight = height;
 	}
 	
 	public void addLoadAndDisplayTask(WallpaperInfo wi, ImageView image)
 	{
 		prepareLoadTask(image, wi.url);
 		Bitmap bm = mMemoryCache.getBitmapFromCache(wi.url);
 		if(bm != null)
 		{
 			if(image.getParent() instanceof PreviewFrameLayout)
 			{
 				((PreviewFrameLayout)image.getParent()).dismissProgressBar();
 			}
 			//image.setImageBitmap(bm);	
 			WallpaperDrawable d = new WallpaperDrawable(image.getResources(), bm);
 			//d.setBitmap(bm);
 			image.setImageDrawable(d);
 		}
 		else
 		{
 			mImageMap.put(Integer.toString(image.hashCode()), image);
 			if(mAllowTask)
 			{
 				doTask();
 			}			
 		}		
 	}
 	
 	private void doTask()
 	{
 		Collection<ImageView> images = mImageMap.values();
 		for(ImageView image: images)
 		{
 			if(image != null)
 			{
 				if(image.getTag() != null)
 				{
 					loadAndDisplay((WallpaperInfo)image.getTag(), image);
 				}
 			}
 		}
 		mImageMap.clear();
 	}
 	
 	public String getDirFileName(WallpaperInfo wi)
 	{
 		String dirFileName = (wi.isNative?Constants.NATIVE:Constants.ONLINE) + (wi.isThumbnail?Constants.THUMBNAIL : "");
 		return dirFileName;
 	}
 	
 	public void downloadOriginWallpaper(final int activityType, final WallpaperInfo wi)
 	{
 		mLongTimeExecutors.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				boolean successful = false;
 				String originalUrl = wi.urls[1];
 				try {
 					InputStream is = Service.getInstance().getWallpaperInputStream(originalUrl);
 					successful = saveDownloadFile(is, originalUrl);
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				final boolean s = successful;
 					mHandler.post(new Runnable() {
 						
 						@Override
 						public void run() {
 							// TODO Auto-generated method stub
 							Callback callback = getCallback(activityType);
 							if(callback != null)
 							{
 								callback.success(s, wi);
 							}
 						}
 					});
 					if(s)
 					{
 						saveOnlineToNative(activityType, wi);
 					}
 			}
 		});
 		
 		
 	}
 	
 	public InputStream getDownloadedInputStream(String fileName)
 	{
 		return mDiscCache.getInputStreamFromFileCache(fileName);
 	}
 	
 	public boolean saveDownloadFile(InputStream is, String fileName)
 	{
 		return mDiscCache.saveInputStreamToFile(is, fileName);
 	}
 	
 	public boolean isApplyOrDown(WallpaperInfo wi)
 	{
 		return mDiscCache.isImageOnDiscCache(wi.urls[1]);
 	}
 	
 	
 	public void saveOnlineToNative(final int activityType, final WallpaperInfo wi)
 	{
 		mShortTimeExecutors.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				ArrayList<WallpaperInfo> wis = mAllInfos.get(-1);
 				if(wis == null)
 				{
 					wis = new ArrayList<WallpaperInfo>();
 				}
 				ArrayList<WallpaperInfo> wisThum = mAllInfos.get(1);
 				if(wisThum == null)
 				{
 					wisThum = new ArrayList<WallpaperInfo>();
 				}
 				
 				//判断是否重复添加
 				if(DEBUG)Log.e(TAG, "apply wallpaper 1: judge whether repeat add.");
 				for(WallpaperInfo w : wis)
 				{
 					if(w.url!=null&& wi.url!= null)
 					{
 						if(w.url.equals(wi.url))
 						{
 							if(DEBUG)Log.e(TAG, "apply wallpaper 1 warning:repeat add " +w.url + "  , and stop.");
 							return;
 						}
 					}
 				}
 				
 				
 				//添加到本地json文件
 				if(DEBUG)Log.e(TAG, "apply wallpaper 2: add the info to json file.");
 				WallpaperHandler.putJsonToNative(wi, mJsonFile);
 				
 				//添加信息到native（mAllInfos）
 				if(DEBUG)Log.e(TAG, "apply wallpaper 3: add the info to mAllInfos.");
 				WallpaperInfo info;
 				info = new WallpaperInfo();
 				info.id = wi.id;
 				info.size = wi.size;
 				info.wallpaperName = wi.wallpaperName;
 				info.isNative = true;
 				info.isThumbnail = false;
 				info.url = wi.url;
 				info.urls[0] = wi.urls[0];
 				info.urls[1] = wi.urls[1];
 				wis.add(info);
 				
 				info = new WallpaperInfo();
 				info.id = wi.id;
 				info.size = wi.size;
 				info.wallpaperName = wi.wallpaperName;
 				info.isNative = true;
 				info.isThumbnail = true;
				info.url = wi.urls[0];
 				
 				wisThum.add(info);
 				
 				
 				//将缩略图和预览图放入本地文件夹
 				if(DEBUG)Log.e(TAG, "apply wallpaper 4: add thum and preview image to native file.");
 				mDiscCache.copyFileFormOnlineToNative(wi.url, false);
 				mDiscCache.copyFileFormOnlineToNative(wi.urls[0], true);
 				
 			}
 		});
 	}
 	
 	public Bitmap getCurrentBitmap(WallpaperInfo wi)
 	{
 		Bitmap bm = mMemoryCache.getBitmapFromCache(wi.url);
 		if(bm == null)
 		{
 			String dirFileName = getDirFileName(wi);
 			File dirFile = mDiscCache.getOrMakeFileDir(dirFileName);
 			bm = mDiscCache.getBitmapFromFileCache(dirFile, wi.url);
 		}
 		return bm;
 	}
 	
 	void loadAndDisplay(WallpaperInfo wi, ImageView image)
 	{
 		boolean isImageOnDisc = mDiscCache.isImageOnDiscCache(wi.url, getDirFileName(wi));
 		LoadAndDisplayImageTask task = new LoadAndDisplayImageTask(wi, image);
 		if(isImageOnDisc)
 		{
 			mShortTimeExecutors.execute(task);
 		}
 		else
 		{
 			mLongTimeExecutors.execute(task);
 		}
 	}
 	
 	class LoadAndDisplayImageTask implements Runnable
 	{
 
 		final ImageView image;
 		final WallpaperInfo wi;
 		
 		public LoadAndDisplayImageTask(WallpaperInfo wi, ImageView image)
 		{
 			this.image = image;
 			this.wi = wi;
 		}
 		@Override
 		public void run() {
 			if(checkTaskIsRunning())return;
 			Bitmap bm = null;
 			final String url = wi.url;
 			String dirFileName = getDirFileName(wi);
 			boolean isImageOnDisc = mDiscCache.isImageOnDiscCache(url, dirFileName);
 			File dirFile = mDiscCache.getOrMakeFileDir(dirFileName);
 			if(DEBUG)Log.e(TAG, url + " on " + dirFile.getPath() + " : " + isImageOnDisc);
 			if(isImageOnDisc)
 			{
 				bm = mDiscCache.getBitmapFromFileCache(dirFile, url);
 				if(bm != null)
 				{
 					mMemoryCache.addBitmapToCache(url, bm);
 				}
 			}
 			if(bm == null)
 			{
 				if(!checkTaskIsRunning())
 				{
 					try {
 						bm = Service.getInstance().getWallpaperBitmap(url);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	
 				}
 				if(bm != null)
 				{
 					mDiscCache.saveBitmapToFile(bm, dirFile, url);
 					mMemoryCache.addBitmapToCache(url, bm);
 				}
 			}
 			final Bitmap finalBm = bm;
 			final ImageView i = image;
 			final boolean isThum = wi.isThumbnail;
 			
 				i.post(new Runnable() {
 					
 					@Override
 					public void run() {
 
 						String currentUrl = getLoadingUri(i);
 						if(currentUrl != null && currentUrl.equals(url))
 						{							
 							if(i.getParent() instanceof PreviewFrameLayout)
 							{
 								((PreviewFrameLayout)i.getParent()).dismissProgressBar();
 							}
 							if(finalBm != null)
 							{
 								//i.setImageBitmap(finalBm);
 								WallpaperDrawable d = new WallpaperDrawable(i.getResources(), finalBm);
 								//d.setBitmap(finalBm);
 								i.setImageDrawable(d);
 							    cancelTask(i);
 							}														
 						}
 						
 					}
 				});
 		
 
 		}
 		
 		boolean checkTaskIsRunning()
 		{
 			String currentUrl = getLoadingUri(image);
 			return !wi.url.equals(currentUrl);
 		}
 		
 	}
 	
 	
 	
 	private String getLoadingUri(ImageView image)
 	{
 		return mCacheKey.get(image.hashCode());
 	}
 	
 	private void prepareLoadTask(ImageView image, String url)
 	{
 		mCacheKey.put(image.hashCode(), url);
 	}
 	
 	private void cancelTask(ImageView image)
 	{
 		mCacheKey.remove(image.hashCode());
 	}
 
 	
 	interface Callback
 	{
 		void setAdapter(ArrayList<WallpaperInfo> wis, int categoryType, boolean loadSuccess);
 		void setCategoryNameAndDis(String name, String dis);
 
 		void setRecommend(List<CategoryInfo> cis);
 		void success(boolean s, WallpaperInfo wi);
 	}
 
 	public static ImageLoader getInstance(Context context)
 	{
 		if(mInstance == null)
 		{
 			mInstance = new ImageLoader(context);
 		}
 		mInstance.initThreadPool();
 		return mInstance;
 	}
 	
 	private void initThreadPool()
 	{
 		int numCPU = Runtime.getRuntime().availableProcessors();
 		if(DEBUG)Log.e(TAG, " cpu num :" + numCPU);
 		
 		
 		if(mShortTimeExecutors == null || mShortTimeExecutors.isShutdown())
 		{
 			mShortTimeExecutors = Executors.newSingleThreadExecutor();
 		}
 		
 		if(mLongTimeExecutors == null || mLongTimeExecutors.isShutdown())
 		{
 			mLongTimeExecutors = Executors.newFixedThreadPool(Math.max((numCPU - 1), 1));
 		}
 	}
 	
 	public void recommendJson(final int activityType)
 	{
 		mLongTimeExecutors.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				if(DEBUG)Log.e(TAG, "recommend 1 : get json.");
 				JSONObject json = null;;
 				try {
 					json = Service.getInstance().getWallpaperCategoryJson();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				if(DEBUG)Log.e(TAG, "recommend 2 : get category info list.");
 				WallpaperHandler.wallpaperCategoryList(mCategoryInfos, json);
 				
 				if(DEBUG)Log.e(TAG, "recommend 3 : get the bitmaps.");
 				final List<CategoryInfo> cis = mCategoryInfos;
 				for(int i = 0; i < cis.size(); i++)
 				{
 					CategoryInfo ci = cis.get(i);
 					try {
 						ci.bm = Service.getInstance().getWallpaperBitmap(ci.url);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 				if(DEBUG)Log.e(TAG, "recommend 4 : load the view on main thread.");
 				mHandler.post(new Runnable() {
 					
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						Callback callback = getCallback(activityType);
 						if(callback != null)
 						{
 							callback.setRecommend(cis);
 						}
 					}
 				});
 				
 			}
 			
 		});
 		
 		
 	}
 	
 	public boolean isCategoryloaded(final int category)
 	{
 		ArrayList<WallpaperInfo> wis = mAllInfos.get(category);
 		if(wis != null && wis.size() > 0)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public  ArrayList<WallpaperInfo> getInfos(final int category)
 	{
 		return mAllInfos.get(category);
 	}
 	
 	public void parseJSON(final int activityType, final int category, final int previousPage)
 	{
 		if(category < 0)
 		{
 			if(mAllInfos != null && mAllInfos.get(category) != null)
 			{
 				ArrayList<WallpaperInfo> wis = mAllInfos.get(category);
 				if(wis == null)
 				{
 					wis = new ArrayList<WallpaperInfo>();
 				}
 				Callback callback = getCallback(activityType);
 				if(callback != null)
 				{
 					callback.setAdapter(wis, category, true);
 				}
 			}
 		}
 		else if(category > 0)
 		{
 			mShortTimeExecutors.execute(new Runnable() {
 				
 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 						final ArrayList<WallpaperInfo> wis;
 						final ArrayList<WallpaperInfo> wisThum;
 						if(mAllInfos == null)
 						{
 							mAllInfos =  Collections.synchronizedMap(new HashMap<Integer, ArrayList<WallpaperInfo>>());
 						}
 						//初始化缩略图信息
 						if(mAllInfos.get(category) != null)
 						{
 							wisThum = mAllInfos.get(category);
 						}
 						else
 						{
 							wisThum = new ArrayList<WallpaperInfo>();
 							mAllInfos.put(category, wisThum);
 						}
 						//初始化大图信息
 						if(mAllInfos.get(-category) != null)
 						{
 							wis = mAllInfos.get(-category);
 						}
 						else
 						{
 							wis = new ArrayList<WallpaperInfo>();
 							mAllInfos.put(-category, wis);
 						}
 						
 						
 						boolean isNative = true;
 						
 						if(category == 1)
 						{
 							isNative = true;
 							//加载本地壁纸
 						}
 						else if(category >= 2)
 						{
 							isNative = false;
 							//加载在线分类壁纸
 						}
 						if(DEBUG)Log.e(TAG, "1:get the json object form native or online(category = " + (category - 2)+").");
 						JSONObject json = null;
 						if(category > 1)
 						{
 							try {
 								json = Service.getInstance().getWallPaperListJson(category-2, previousPage);
 							} catch (Exception e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						else
 						{
 							json = WallpaperHandler.createWPListJson(mJsonFile, isNative);
 						}	
 						if(DEBUG)Log.e(TAG, "2:get the wallpaper list info form json(category = " + (category - 2)+").");
 					    final boolean loadSuccess = (WallpaperHandler.wallpaperList(wis, wisThum, json, isNative) > 0);
 					    if(DEBUG)Log.e(TAG, "3:load the view on main thread(category = " + (category - 2)+").");
 			            final int c = category;
 						mHandler.post(new Runnable() {
 							
 							@Override
 							public void run() {
 								// TODO Auto-generated method stub
 								Callback callback = getCallback(activityType);
 								if(callback != null)
 								{
 									callback.setAdapter(wisThum, c,loadSuccess);
 								}
 							}
 						});
 				}
 			});
 		}
 		
 		
 	}
 	
 	Callback getCallback(int categroy)
 	{
 		if(mCallbacks != null)
 		{
 			WeakReference<Callback> wCallback = mCallbacks.get(categroy);
 			if(wCallback != null)
 			{
 				return wCallback.get();
 			}
 		}
 		return null;
 	}
 	
 	
 	public void setCallback(Callback callback, int activityType)
 	{
 		if(mCallbacks == null)
 		{
 			mCallbacks = new HashMap<Integer, WeakReference<Callback>>();
 		}
 		mCallbacks.put(activityType, new WeakReference<Callback>(callback));
 	}
 	
 	public void clearCache()
 	{
 		if(mShortTimeExecutors != null)mShortTimeExecutors.shutdownNow();
 		if(mLongTimeExecutors != null)mLongTimeExecutors.shutdownNow();
 		if(mImageMap != null)
 		{
 			mImageMap.clear();
 		}
 		if(mCacheKey != null)
 		{
 			mCacheKey.clear();
 		}
 		if(mMemoryCache != null)
 		{
 			mMemoryCache.clearCache();
 		}
 		if(mAllInfos != null)
 		{
 			Set<Integer> keys = mAllInfos.keySet();
 			for(Integer key :keys)
 			{
 				ArrayList<WallpaperInfo> wis = mAllInfos.get(key);
 				if(wis != null)
 				{
 					wis.clear();
 				}
 			}
 			mAllInfos.clear();
 		}
 		
 		if(mCallbacks != null)
 		{
 			mCallbacks.clear();
 		}
 		
 		if(mCategoryInfos != null)
 		{
 			mCategoryInfos.clear();
 		}
 	}
 	
 	public void lock()
 	{
 		mAllowTask = false;
 	}
 	
 	public void unlock()
 	{
 		mAllowTask = true;
 		doTask();
 	}
 	
 	static class WallpaperDrawable extends BitmapDrawable {
 
         Bitmap bitmap;
         int width;
         int height;
 
         public WallpaperDrawable(Resources res, Bitmap bm)
         {
         	super(res, bm);
         	bitmap = bm;
         	if(bm != null)
         	{
         		width = bm.getWidth();
         		height = bm.getHeight();
         	}	
         }
 
         @Override
         public void draw(Canvas canvas) {
             if (bitmap == null) return;
             if(bitmap.isRecycled())return;
             if(width <= 0 || width <= 0)return;
             super.draw(canvas);
         }
 
         @Override
         public int getOpacity() {
             return android.graphics.PixelFormat.OPAQUE;
         }
 
         @Override
         public void setAlpha(int alpha) {
             // Ignore
         }
 
         @Override
         public void setColorFilter(ColorFilter cf) {
             // Ignore
         }
     }
 
 }

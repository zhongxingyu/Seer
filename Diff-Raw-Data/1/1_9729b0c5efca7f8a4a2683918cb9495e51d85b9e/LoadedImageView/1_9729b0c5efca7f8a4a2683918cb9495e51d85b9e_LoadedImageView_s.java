 package com.levelup.picturecache.widget;
 
 import java.io.File;
 import java.security.NoSuchAlgorithmException;
 
 import uk.co.senab.bitmapcache.BitmapLruCache;
 import uk.co.senab.bitmapcache.CacheableImageView;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 
 import com.levelup.picturecache.IPictureLoadConcurrency;
 import com.levelup.picturecache.IPictureLoaderRender;
 import com.levelup.picturecache.IPictureLoaderTransforms;
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureJob;
 import com.levelup.picturecache.UIHandler;
 
 public class LoadedImageView extends CacheableImageView implements IPictureLoadConcurrency, IPictureLoaderRender {
 /*
 	public class BaseImageViewDrawHandler implements IPictureLoaderRender {
 		@Override
 		public void drawBitmap(Drawable drawable, String url, Object cookie, BitmapLruCache drawableCache, boolean immediate) {
 			setImageDrawable(drawable);
 		}
 
 		@Override
 		public void drawDefaultPicture(String url, BitmapLruCache drawableCache) {
 			// do nothing by default
 		}
 
 		@Override
 		public void drawErrorPicture(String url, BitmapLruCache drawableCache) {
 			// do nothing by default
 		}
 	}
 */
 	// IPictureLoadConcurrency
 	private String currentURL;
 
 	@Override
 	public boolean isDownloadAllowed() {
 		// TODO implement a cache/wide setting
 		return true;
 	}
 
 	@Override
 	public String setLoadingURL(String url, BitmapLruCache mBitmapCache) {
 		String oldURL = currentURL;
 		currentURL = url;
 		return oldURL;
 	}
 	
 	@Override
 	public boolean canDirectLoad(File file) {
 		return false;
 	}
 	
 	// IPictureLoaderRender
     private boolean isInLayout;
 	private IPictureLoaderRender currentRender;
 
     @Override
 	public void drawDefaultPicture(final String url, final BitmapLruCache drawableCache) {
 		UIHandler.assertUIThread();
 		if (!url.equals(currentURL)) {
 			// we don't care about this anymore
 			return;
 		}
 		
 		if (isInLayout) {
 			post(new Runnable() {
 				@Override
 				public void run() {
 					drawDefaultPicture(url, drawableCache);
 				}
 			});
 			return;
 		}
 		
 		currentRender.drawDefaultPicture(url, drawableCache);
 	}
 
 	@Override
 	public void drawErrorPicture(final String url, final BitmapLruCache drawableCache) {
 		UIHandler.assertUIThread();
 		if (!url.equals(currentURL)) {
 			// we don't care about this anymore
 			return;
 		}
 		
 		if (isInLayout) {
 			post(new Runnable() {
 				@Override
 				public void run() {
 					drawErrorPicture(url, drawableCache);
 				}
 			});
 			return;
 		}
 		
 		currentRender.drawErrorPicture(url, drawableCache);
 	}
 
 	@Override
 	public void drawBitmap(final Drawable drawable, final String url, final Object cookie, final BitmapLruCache drawableCache, final boolean immediate) {
 		UIHandler.assertUIThread();
 		if (!url.equals(currentURL)) {
 			// we don't care about this anymore
 			return;
 		}
 		
 		if (isInLayout) {
 			post(new Runnable() {
 				@Override
 				public void run() {
 					drawBitmap(drawable, url, cookie, drawableCache, immediate);
 				}
 			});
 			return;
 		}
 		
 		currentRender.drawBitmap(drawable, url, cookie, drawableCache, immediate);
 	}
 
 
 	private PictureJob currentJob;
 
 	public LoadedImageView(Context context) {
 		super(context);
 	}
 
 	public LoadedImageView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public LoadedImageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 /*
 	public void loadImageURL(PictureCache cache, String url) {
 		loadImageURL(cache, url, null, null, 0, 0, null, null, null);
 	}
 /*
 	public void loadImageURL(PictureCache cache, String url, String UUID, LifeSpan cacheLifespan, int maxWidth, int maxHeight, final StorageTransform bitmapStorageTransform, final BitmapTransform bitmapTransform, Object cookie) {
 		loadImageURL(cache, url, UUID, new BaseImageViewDrawHandler(), null, maxWidth, maxHeight, new IPictureLoaderTransforms() {
 			@Override
 			public StorageTransform getStorageTransform() {
 				return bitmapStorageTransform;
 			}
 
 			@Override
 			public BitmapTransform getDisplayTransform() {
 				return bitmapTransform;
 			}
 		}, null);
 	}
 */
 	public void loadImageURL(PictureCache cache, String url, String UUID, IPictureLoaderRender drawHandler, LifeSpan cacheLifespan, int maxWidth, int maxHeight, IPictureLoaderTransforms transforms, Object cookie) {
 		UIHandler.assertUIThread();
 
 		PictureJob.Builder newJobBuilder = new PictureJob.Builder(this, transforms, this);
 		newJobBuilder.setURL(url)
 		.setUUID(UUID)
 		.setCookie(cookie)
 		.setLifeType(cacheLifespan);
 		if (0==maxHeight)
 			newJobBuilder.setDimension(maxWidth, true);
 		else
 			newJobBuilder.setDimension(maxHeight, false);
 
 		PictureJob newJob = newJobBuilder.build();
 		if (null!=currentJob && currentJob.equals(newJob) && null!=currentRender && currentRender.equals(drawHandler)) {
 			// nothing to do, we're already on it
 			return;
 		}
 
 		if (null!=currentRender) {
 			// TODO should cancel using the currentJob 
 			cache.cancelPictureLoader(this, null);
 		}
 
 		currentJob = newJob;
 		currentRender = drawHandler;
 		try {
 			currentJob.startLoading(cache);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void resetImageURL(PictureCache cache) {
 		UIHandler.assertUIThread();
 		cache.cancelPictureLoader(currentRender, currentURL);
 	}
 	
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
 		isInLayout = true;
 		super.onLayout(changed, left, top, right, bottom);
 		isInLayout = false;
 	}
 
 	@Override
 	public void setImageResource(final int resId) {
 		UIHandler.assertUIThread();
 		if (isInLayout) {
             post(new Runnable() {
                 @Override
                 public void run() {
                     LoadedImageView.super.setImageResource(resId);
                 }
             });
             return;
 		}
 		
 		super.setImageResource(resId);
 	}
 
 	@Override
 	public void setImageDrawable(final Drawable drawable) {
 		UIHandler.assertUIThread();
 		if (isInLayout) {
             post(new Runnable() {
                 @Override
                 public void run() {
                 	LoadedImageView.super.setImageDrawable(drawable);
                 }
             });
             return;
 		}
 		
 		super.setImageDrawable(drawable);
 	}
 
 	@Override
 	public void setImageBitmap(final Bitmap bm) {
 		UIHandler.assertUIThread();
 		if (isInLayout) {
             post(new Runnable() {
                 @Override
                 public void run() {
                 	LoadedImageView.super.setImageBitmap(bm);
                 }
             });
             return;
 		}
 		
 		super.setImageBitmap(bm);
 	}
 }

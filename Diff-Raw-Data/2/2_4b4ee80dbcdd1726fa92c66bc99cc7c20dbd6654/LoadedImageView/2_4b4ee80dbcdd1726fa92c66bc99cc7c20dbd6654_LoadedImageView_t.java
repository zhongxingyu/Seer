 package com.levelup.picturecache.widget;
 
 import java.io.File;
 import java.security.InvalidParameterException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import uk.co.senab.bitmapcache.BitmapLruCache;
 import uk.co.senab.bitmapcache.CacheableImageView;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.levelup.picturecache.BuildConfig;
 import com.levelup.picturecache.IPictureLoadConcurrency;
 import com.levelup.picturecache.IPictureLoaderRender;
 import com.levelup.picturecache.IPictureLoaderTransforms;
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.NetworkLoader;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureJob;
 import com.levelup.picturecache.UIHandler;
 import com.levelup.picturecache.loaders.ViewLoader;
 import com.levelup.picturecache.loaders.internal.DrawType;
 
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
 	private DrawType currentDrawType; // TODO should be reset if the default/error displaying is different between the calls
 
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
 
 		drawInView(DrawType.DEFAULT, url, drawableCache, null, true, currentJob.mDisplayHandler, null);
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
 
 		drawInView(DrawType.ERROR, url, drawableCache, null, true, currentJob.mDisplayHandler, null);
 	}
 
 	@Override
 	public void drawBitmap(final Drawable drawable, final String url, final Object drawCookie, final BitmapLruCache drawableCache, final boolean immediate) {
 		UIHandler.assertUIThread();
 		if (!url.equals(currentURL)) {
 			// we don't care about this anymore
 			return;
 		}
 
 		if (isInLayout) {
 			post(new Runnable() {
 				@Override
 				public void run() {
 					drawBitmap(drawable, url, drawCookie, drawableCache, immediate);
 				}
 			});
 			return;
 		}
 
 		drawInView(DrawType.LOADED_DRAWABLE, url, drawableCache, drawable, immediate, currentJob.mDisplayHandler, drawCookie);
 	}
 
 	private static boolean drawsEnqueued;
 	private static final HashMap<LoadedImageView,Runnable> pendingDraws = new HashMap<LoadedImageView,Runnable>();
 	private static final Runnable batchDisplay = new Runnable() {
 		@Override
 		public void run() {
 			for (Entry<LoadedImageView, Runnable> displayJob : pendingDraws.entrySet()) {
 				displayJob.getValue().run();
 			}
 			pendingDraws.clear();
 			drawsEnqueued = false;
 		}
 	};
 
 	private void drawInView(final DrawType type, final String url, final BitmapLruCache drawableCache, final Drawable drawable, boolean immediate, final IPictureLoaderRender renderer, final Object drawCookie) {
 		pendingDraws.put(this, new Runnable() {
 			@Override
 			public void run() {
 				if (type==DrawType.DEFAULT) {
 					if (currentDrawType!=DrawType.DEFAULT) {
 						renderer.drawDefaultPicture(url, drawableCache);
 						currentDrawType = DrawType.DEFAULT;
 					} else {
 						if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" saved default display");
 					}
 				}
 
 				else if (type==DrawType.ERROR) {
 					if (currentDrawType!=DrawType.ERROR) {
 						renderer.drawErrorPicture(url, drawableCache);
 						currentDrawType = DrawType.ERROR;
 					} else {
 						if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" saved error display");
 					}
 				}
 
 				else if (type==DrawType.LOADED_DRAWABLE) {
 					renderer.drawBitmap(drawable, url, drawCookie, drawableCache, true);
 					currentDrawType = DrawType.LOADED_DRAWABLE;
 				}
 			}
 		});
 
 		if (immediate) {
 			removeCallbacks(batchDisplay);
 			batchDisplay.run();
 			drawsEnqueued = false;
 		} else if (!drawsEnqueued) {
 			postDelayed(batchDisplay, 100);
 			drawsEnqueued = true;
 		}
 	}
 
 
 	private PictureJob currentJob;
 	private PictureCache currentCache;
 
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
 	public void loadImageURL(PictureCache cache, String url, String UUID, NetworkLoader networkLoader, IPictureLoaderRender drawHandler, long urlFreshness, LifeSpan cacheLifespan, int maxWidth, int maxHeight, IPictureLoaderTransforms transforms, Object drawCookie) {
 		UIHandler.assertUIThread();
 		if (null==url && null==UUID) {
 			throw new IllegalArgumentException("We need either a url or a uuid to display, did you mean resetImageURL()?");
 		}
 		if (null==drawHandler) {
 			throw new IllegalArgumentException("We need a drawHandler to draw");
 		}
 
 		PictureJob.Builder newJobBuilder = new PictureJob.Builder(drawHandler, transforms, this);
 		newJobBuilder.setURL(url)
 		.setUUID(UUID)
 		.setDrawCookie(drawCookie)
 		.setLifeType(cacheLifespan)
 		.setFreshDate(urlFreshness)
 		.setNetworkLoader(networkLoader);
 		if (0==maxHeight)
 			newJobBuilder.setDimension(maxWidth, true);
 		else
 			newJobBuilder.setDimension(maxHeight, false);
 
 		PictureJob newJob = newJobBuilder.build();
 		if (null!=currentJob && currentJob.equals(newJob)) {
 			// nothing to do, we're already on it
 			return;
 		}
 
 		if (null!=currentJob) {
 			currentJob.stopLoading(currentCache, false);
 			currentURL = null; // TODO should be done for every cancel or better handled with setLoadingURL()
 		}
 
 		currentJob = newJob;
 		currentCache = cache;
 		currentJob.startLoading(currentCache);
 	}
 
 	public void resetImageURL(IPictureLoaderRender defaultDrawHandler) {
 		UIHandler.assertUIThread();
 		pendingDraws.remove(this);
 		if (null!=currentJob) {
 			currentJob.stopLoading(currentCache, false);
 		}
 		if (null!=defaultDrawHandler) {
			if (null!=currentJob && currentJob.mDisplayHandler != defaultDrawHandler) {
 				// TODO rebuild a PictureJob with this default handler
 				if (BuildConfig.DEBUG) throw new InvalidParameterException("can't change the default drawer yet");
 			}
 			defaultDrawHandler.drawDefaultPicture(currentURL, null!=currentCache ? currentCache.getBitmapCache() : null);
 		}
 		if (currentDrawType!=DrawType.LOADED_DRAWABLE)
 			currentURL = null;
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
 		isInLayout = true;
 		super.onLayout(changed, left, top, right, bottom);
 		isInLayout = false;
 	}
 
 	@Override
 	public void requestLayout() {
 		if (isInLayout) throw new IllegalStateException();
 		super.requestLayout();
 	}
 
 	@Override
 	protected void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		loadImageDrawable(null);
 	}
 
 	/**
 	 * Load a resource as an image in the View, similar to {@link ImageView#setImageResource(int) setImageResource(int)} but canceling the previous network load if there was any
 	 * @param resId the resource identifier of the drawable
 	 */
 	public void loadImageResource(int resId) {
 		resetImageURL(null);
 		super.setImageResource(resId);
 		currentDrawType = null;
 	}
 
 	/**
 	 * Load a Drawable as an image in the View, similar to {@link ImageView#setImageDrawable(Drawable) setImageDrawable(Drawable)} but canceling the previous network load if there was any
 	 * @param drawable The drawable to set
 	 */
 	public void loadImageDrawable(Drawable drawable) {
 		resetImageURL(null);
 		super.setImageDrawable(drawable);
 		currentDrawType = null;
 	}
 
 	/**
 	 * Load a Bitmap as an image in the View, similar to {@link ImageView#setImageBitmap(Bitmap) setImageBitmap(Bitmap)} but canceling the previous network load if there was any
 	 * @param bm The bitmap to set
 	 */
 	public void loadImageBitmap(Bitmap bm) {
 		resetImageURL(null);
 		super.setImageBitmap(bm);
 		currentDrawType = null;
 	}
 
 	/**
 	 * @deprecated use {@link #loadImageResource(int)}
 	 */
 	@Deprecated
 	@Override
 	public final void setImageResource(final int resId) {
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
 
 	/**
 	 * @deprecated use {@link #loadImageDrawable(Drawable)}
 	 */
 	@Deprecated
 	@Override
 	public final void setImageDrawable(final Drawable drawable) {
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
 
 	/**
 	 * @deprecated use {@link #loadImageBitmap(Bitmap)}
 	 */
 	@Deprecated
 	@Override
 	public final void setImageBitmap(final Bitmap bm) {
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

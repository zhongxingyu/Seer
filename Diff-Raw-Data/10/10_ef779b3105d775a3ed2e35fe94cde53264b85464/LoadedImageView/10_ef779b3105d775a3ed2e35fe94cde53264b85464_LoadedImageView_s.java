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
 import com.levelup.picturecache.LifeSpan;
 import com.levelup.picturecache.LogManager;
 import com.levelup.picturecache.NetworkLoader;
 import com.levelup.picturecache.PictureCache;
 import com.levelup.picturecache.PictureJob;
 import com.levelup.picturecache.PictureJobConcurrency;
 import com.levelup.picturecache.PictureJobRenderer;
 import com.levelup.picturecache.PictureJobTransforms;
 import com.levelup.picturecache.UIHandler;
 import com.levelup.picturecache.loaders.ViewLoader;
 import com.levelup.picturecache.loaders.internal.DrawType;
 
 /**
  * {@code ImageView} subclass that allow loading pictures from a URL
  * @see {@link #loadImageURL(PictureCache, String)}
  * @see {@link #loadImageURL(PictureCache, String, String, LoadedImageViewRender, int, int, PictureJobTransforms, long, LifeSpan, NetworkLoader)}
  */
 public class LoadedImageView extends CacheableImageView implements PictureJobConcurrency, PictureJobRenderer {
 	private final static boolean DEBUG_STATE = BuildConfig.DEBUG && false;
 	
 	/**
 	 * Callback for custom {@link LoadedImageView} rendering  
 	 */
 	public interface LoadedImageViewRender {
 		/**
 		 * Called when the downloaded {@link Drawable} should be displayed 
 		 *
 		 * @param view The {@link LoadedImageView} in which to display the drawable 
 		 * @param drawable Drawable to display
 		 */
 		void renderDrawable(LoadedImageView view, Drawable drawable);
 
 		/**
 		 * Called when the loading state should be displayed, while the bitmap is loading
 		 * 
 		 * @param view The {@link LoadedImageView} in which to display the default display 
 		 */
 		void renderLoading(LoadedImageView view);
 
 		/**
 		 * Called when the download failed and an error should be displayed
 		 * 
 		 * @param view The {@link LoadedImageView} in which to display the error 
 		 */
 		void renderError(LoadedImageView view);
 	}
 	
 	public static class BaseLoadedImageViewRender implements LoadedImageViewRender {
 		@Override
 		public void renderDrawable(LoadedImageView view, Drawable drawable) {
 			view.setImageDrawable(drawable);
 		}
 
 		@Override
 		public void renderLoading(LoadedImageView view) {
 			view.setImageDrawable(null);
 		}
 
 		@Override
 		public void renderError(LoadedImageView view) {
 			renderLoading(view);
 		}
 	}
 
 	public static final LoadedImageViewRender DefaultRenderer = new BaseLoadedImageViewRender();
 	
 	// PictureJobConcurrency
 	private String currentURL;
 
 	@Override
 	public boolean isDownloadAllowed() {
 		// TODO implement a cache/wide setting
 		return true;
 	}
 
 	/**
 	 * DO NOT CALL, internal code
 	 */
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
 
 	// PictureJobRenderer
 	private boolean isInLayout;
 	private DrawType currentDrawType; // TODO should be reset if the default/error displaying is different between the calls
 
 	/**
 	 * DO NOT CALL, internal code
 	 */
 	@Override
 	public final void drawDefaultPicture(final String url, final BitmapLruCache drawableCache) {
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
 
 		drawInView(DrawType.DEFAULT, url, drawableCache, null, true, currentDrawer);
 	}
 
 	/**
 	 * DO NOT CALL, internal code
 	 */
 	@Override
 	public final void drawErrorPicture(final String url, final BitmapLruCache drawableCache) {
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
 
 		drawInView(DrawType.ERROR, url, drawableCache, null, true, currentDrawer);
 	}
 
 	/**
 	 * DO NOT CALL, internal code
 	 */
 	@Override
 	public final void drawBitmap(final Drawable drawable, final String url, final Object drawCookie, final BitmapLruCache drawableCache, final boolean immediate) {
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
 
 		drawInView(DrawType.LOADED_DRAWABLE, url, drawableCache, drawable, immediate, currentDrawer);
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
 
 	private void drawInView(final DrawType type, final String url, final BitmapLruCache drawableCache, final Drawable drawable, boolean immediate, final LoadedImageViewRender renderer) {
 		pendingDraws.put(this, new Runnable() {
 			@Override
 			public void run() {
 				if (type==DrawType.DEFAULT) {
 					if (currentDrawType!=DrawType.DEFAULT) {
 						renderer.renderLoading(LoadedImageView.this);
 						currentDrawType = DrawType.DEFAULT;
 					} else {
 						if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" saved default display");
 					}
 				}
 
 				else if (type==DrawType.ERROR) {
 					if (currentDrawType!=DrawType.ERROR) {
 						renderer.renderError(LoadedImageView.this);
 						currentDrawType = DrawType.ERROR;
 					} else {
 						if (ViewLoader.DEBUG_VIEW_LOADING) LogManager.getLogger().d(PictureCache.LOG_TAG, this+" saved error display");
 					}
 				}
 
 				else if (type==DrawType.LOADED_DRAWABLE) {
 					renderer.renderDrawable(LoadedImageView.this, drawable);
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
 	private LoadedImageViewRender currentDrawer;
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
 	
 	/**
 	 * Load the given URL from the network using the provided cache
 	 * <p>An empty drawable is displayed while the picture is loading</p>
 	 * 
 	 * @param cache The {@link PictureCache} to use to download/store the picture stream
 	 * @param url The URL of the picture stream to display
 	 * 
 	 * @see #loadImageURL(PictureCache, String, String, LoadedImageViewRender, int, int, PictureJobTransforms, long, LifeSpan, NetworkLoader) full loadImageURL() call
 	 * @see #resetImageURL(LoadedImageViewRender)
 	 */
 	public void loadImageURL(PictureCache cache, String url) {
 		loadImageURL(cache, url, null, DefaultRenderer, 0, 0, null, 0, null, null);
 	}
 	
 	/**
 	 * Load the given URL from the network using the provided cache
 	 * 
 	 * @param cache The {@link PictureCache} to use to download/store the picture stream
 	 * @param url The URL of the picture stream to display, can be {@code null} if UUID is not {@code null}
 	 * @param UUID A unique identifier for this URL/transform combination, useful to retrieve pictures from the cache without knowing the URL, can be {@code null}
 	 * @param renderer The interface used to display the downloaded bitmap, the loading view or the download error
 	 * @param maxWidth The max width of the resulting bitmap, may be 0
 	 * @param maxHeight The max height of the resulting bitmap, may be 0
 	 * @param transforms {@link PictureJobTransforms Transformations} that may be applied to the downloaded bitmap before displaying it
 	 * @param urlFreshness Date of the URL in case the same UUID has different URLs, may be 0
 	 * @param cacheLifespan The {@link LifeSpan} of the item if the item should be stored {@link LifeSpan#SHORTTERM shortly}, {@link LifeSpan#ETERNAL eternally} or {@link LifeSpan#LONGTERM normally}
 	 * @param networkLoader {@link NetworkLoader} for special network load handling, can be {@code null}
 	 * @see #loadImageURL(PictureCache, String, String, LoadedImageViewRender, int, int, PictureJobTransforms, long, LifeSpan, NetworkLoader) simplified loadImageURL() call
 	 * @see #resetImageURL(LoadedImageViewRender)
 	 */
 	public void loadImageURL(PictureCache cache, String url, String UUID, LoadedImageViewRender renderer, int maxWidth, int maxHeight, PictureJobTransforms transforms, long urlFreshness, LifeSpan cacheLifespan, NetworkLoader networkLoader) {
 		UIHandler.assertUIThread();
 		if (null==url && null==UUID) {
 			throw new IllegalArgumentException("We need either a url or a uuid to display, did you mean resetImageURL()?");
 		}
 		if (null==renderer) {
 			throw new IllegalArgumentException("We need a drawHandler to draw");
 		}
 
 		if (DEBUG_STATE) LogManager.getLogger().d(VIEW_LOG_TAG, this+" loadImageURL "+url);
 		PictureJob.Builder newJobBuilder = new PictureJob.Builder(this, this);
 		newJobBuilder.setURL(url)
 		.setTransforms(transforms)
 		.setUUID(UUID)
 		.setLifeType(cacheLifespan)
 		.setFreshDate(urlFreshness)
 		.setNetworkLoader(networkLoader);
 		if (0==maxHeight)
 			newJobBuilder.setDimension(maxWidth, true);
 		else
 			newJobBuilder.setDimension(maxHeight, false);
 
 		PictureJob newJob = newJobBuilder.build();
 		if (null!=currentJob && currentJob.equals(newJob) && renderer.equals(currentDrawer)) {
 			// nothing to do, we're already on it
 			if (DEBUG_STATE) LogManager.getLogger().i(VIEW_LOG_TAG, this+" same job, do nothing");
 			return;
 		}
 
 		if (null!=currentJob) {
 			currentJob.stopLoading(currentCache, false);
 			currentURL = null; // TODO should be done for every cancel or better handled with setLoadingURL()
 		}
 
 		currentDrawer = renderer;
 		currentJob = newJob;
 		currentCache = cache;
 		currentJob.startLoading(currentCache);
 	}
 
 	/**
 	 * Stop the currently loading URL job and do not display it. If a {@code viewRenderer} is provided, it will be used to reset the display to the default view
 	 * 
 	 * @param viewRenderer The renderer to use to reset bring back the display to the loading view, can be {@code null}
 	 */
 	public void resetImageURL(LoadedImageViewRender viewRenderer) {
 		UIHandler.assertUIThread();
 		if (DEBUG_STATE) LogManager.getLogger().d(VIEW_LOG_TAG, this+" resetImageURL");
 		pendingDraws.remove(this);
 		if (null!=currentJob) {
 			currentJob.stopLoading(currentCache, false);
 		}
 		if (null!=viewRenderer) {
 			if (null!=currentDrawer && !viewRenderer.equals(currentDrawer)) {
 				// TODO rebuild a PictureJob with this default handler
 				if (BuildConfig.DEBUG) throw new InvalidParameterException("can't change the default drawer yet");
 			}
 			currentDrawer = viewRenderer;
 			drawDefaultPicture(currentURL, null!=currentCache ? currentCache.getBitmapCache() : null);
 		}
 		if (currentDrawType!=DrawType.LOADED_DRAWABLE)
 			currentURL = null;
 		currentJob = null;
 		// not safe for now currentDrawer = null;
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
 	 * 
 	 * @param resId the resource identifier of the drawable
 	 */
 	public void loadImageResource(int resId) {
 		resetImageURL(null);
 		super.setImageResource(resId);
 		currentDrawType = null;
 	}
 
 	/**
 	 * Load a Drawable as an image in the View, similar to {@link ImageView#setImageDrawable(Drawable) setImageDrawable(Drawable)} but canceling the previous network load if there was any
 	 * 
 	 * @param drawable The drawable to set
 	 */
 	public void loadImageDrawable(Drawable drawable) {
 		resetImageURL(null);
 		super.setImageDrawable(drawable);
 		currentDrawType = null;
 	}
 
 	/**
 	 * Load a Bitmap as an image in the View, similar to {@link ImageView#setImageBitmap(Bitmap) setImageBitmap(Bitmap)} but canceling the previous network load if there was any
 	 * 
 	 * @param bm The bitmap to set
 	 */
 	public void loadImageBitmap(Bitmap bm) {
 		resetImageURL(null);
 		super.setImageBitmap(bm);
 		currentDrawType = null;
 	}
 
 	/**
 	 * @deprecated use {@link #loadImageResource(int)} to load a resource and cancel a possible URL loading
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
 	 * @deprecated use {@link #loadImageDrawable(Drawable)} to load a drawable and cancel a possible URL loading
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
 	 * @deprecated use {@link #loadImageBitmap(Bitmap)} to load a bitmap and cancel a possible URL loading
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

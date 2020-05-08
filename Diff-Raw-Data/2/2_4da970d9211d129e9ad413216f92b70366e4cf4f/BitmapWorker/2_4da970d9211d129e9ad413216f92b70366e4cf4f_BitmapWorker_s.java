 package net.analogyc.wordiary.models;
 
 import android.content.res.Resources;
 import android.graphics.*;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.util.LruCache;
 import android.widget.ImageView;
 
 import java.lang.ref.WeakReference;
 
 public class BitmapWorker extends Fragment {
 
 	private static final String TAG = "BitmapWorker";
 	private LruCache<String, Bitmap> mMemoryCache;
 
 	/**
 	 * Sets the max memory usage for the LRU cache
 	 */
 	public BitmapWorker() {
 
 		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 		final int cacheSize = maxMemory / 8;
 
 		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
 			@Override
 			protected int sizeOf(String key, Bitmap bitmap) {
 				// The cache size will be measured in kilobytes rather than number of items.
 				// must do it like this because bitmap.getByteCount was added in API Level 12
 				return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
 			}
 		};
 	}
 
 	/**
 	 * Retrieves a BitmapWorker if one is already available, with sticky LRU cache
 	 *
 	 * @param fm The FragmentManager
 	 * @return The BitmapWorker fragment
 	 */
 	public static BitmapWorker findOrCreateBitmapWorker(FragmentManager fm) {
 		BitmapWorker fragment = (BitmapWorker) fm.findFragmentByTag(TAG);
 
 		// create the fragment on request
 		if (fragment == null) {
 			FragmentTransaction ft = fm.beginTransaction();
 			fragment = new BitmapWorker();
 			ft.add(fragment, TAG);
 			ft.commit();
 		}
 
 		return fragment;
 	}
 
 	/**
 	 * Sets the fragment to retain the instance, so we can grab it unchanged with findOrCreateBitmapWorker()
 	 *
 	 * @param savedInstanceState
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 
 	/**
 	 * Adds the bitmap to the cache
 	 *
 	 * @param key Unique key for the modified image
 	 * @param bitmap The image
 	 */
 	protected void addBitmapToMemoryCache(String key, Bitmap bitmap) {
 		if (getBitmapFromMemCache(key) == null) {
 			mMemoryCache.put(key, bitmap);
 		}
 	}
 
 	/**
 	 * Returns the image associated with a key
 	 *
 	 * @param key Unique key for the modified image
 	 * @return The modified image
 	 */
 	protected Bitmap getBitmapFromMemCache(String key) {
 		return mMemoryCache.get(key);
 	}
 
 	/**
 	 * Clears the image from the cache, given the path
 	 *
 	 * @param path The path of the bitmap
 	 */
 	public void clearBitmapFromMemCache(String path) {
 		mMemoryCache.remove("models.EntryAdapter.thumbnails." + path);
 	}
 
 	/**
 	 * Returns a new builder to prepare the BitmapWorkerTask
 	 *
 	 * @param imageView The view in which the image will be displayed
 	 * @param path The path to the image
 	 * @return The builder object to prepare the task
 	 */
 	public BitmapWorkerTaskBuilder createTask(ImageView imageView, String path) {
 		return new BitmapWorkerTaskBuilder(imageView, path);
 	}
 
 	/**
 	 * Builder for the task, allows chained settings
 	 */
 	public class BitmapWorkerTaskBuilder {
 
 		protected ImageView imageView;
 		protected String path;
 		protected Bitmap defaultBitmap;
 		protected int targetWidth;
 		protected int targetHeight;
 		protected boolean centerCrop = false;
 		protected boolean highQuality = true;
 		protected int roundedCorner;
 		protected String prefix = "";
 
 		public BitmapWorkerTaskBuilder(ImageView imageView, String path) {
 			this.imageView = imageView;
 			this.path = path;
 		}
 
 		/**
 		 * Clones the builder so it can't be modified further
 		 *
 		 * @param b
 		 */
 		protected BitmapWorkerTaskBuilder(BitmapWorkerTaskBuilder b) {
 			path = b.getPath();
 			defaultBitmap = b.getDefaultBitmap();
 			targetWidth = b.getTargetWidth();
 			targetHeight = b.getTargetHeight();
 			centerCrop = b.isCenterCrop();
 			highQuality = b.isHighQuality();
 			roundedCorner = b.getRoundedCorner();
 			prefix = b.getPrefix();
 		}
 
 		public String getPath() {
 			return path;
 		}
 
 		public Bitmap getDefaultBitmap() {
 			return defaultBitmap;
 		}
 
 		public BitmapWorkerTaskBuilder setDefaultBitmap(Bitmap defaultBitmap) {
 			this.defaultBitmap = defaultBitmap;
 			return this;
 		}
 
 		public int getTargetWidth() {
 			return targetWidth;
 		}
 
 		public BitmapWorkerTaskBuilder setTargetWidth(int targetWidth) {
 			this.targetWidth = targetWidth;
 			return this;
 		}
 
 		public int getTargetHeight() {
 			return targetHeight;
 		}
 
 		public BitmapWorkerTaskBuilder setTargetHeight(int targetHeight) {
 			this.targetHeight = targetHeight;
 			return this;
 		}
 
 		public boolean isCenterCrop() {
 			return centerCrop;
 		}
 
 		public BitmapWorkerTaskBuilder setCenterCrop(boolean centerCrop) {
 			this.centerCrop = centerCrop;
 			return this;
 		}
 
 		public boolean isHighQuality() {
 			return highQuality;
 		}
 
 		public BitmapWorkerTaskBuilder setHighQuality(boolean highQuality) {
 			this.highQuality = highQuality;
 			return this;
 		}
 
 		public int getRoundedCorner() {
 			return roundedCorner;
 		}
 
 		public BitmapWorkerTaskBuilder setRoundedCorner(int roundedCorner) {
 			this.roundedCorner = roundedCorner;
 			return this;
 		}
 
 		public BitmapWorkerTaskBuilder setPrefix(String prefix) {
 			this.prefix = prefix;
 			return this;
 		}
 
 
 		public String getPrefix() {
 			return prefix;
 		}
 
 		public void execute() {
 			// stop the previous task if we're going to use this drawable with another Bitmap
 			if (imageView.getDrawable() instanceof AsyncDrawable) {
 				BitmapWorkerTask oldTask = ((AsyncDrawable) imageView.getDrawable()).getBitmapWorkerTask();
 				if (oldTask != null) {
 					oldTask.cancel(true);
 
 					// don't reload the image if it's the same as in the drawable
 					if (getPath().equals(oldTask.getBuilderCopy().getPath())) {
 						return;
 					}
 				}
 			}
 
 			BitmapWorkerTask task = new BitmapWorkerTask(imageView, this);
 			imageView.setImageDrawable(new AsyncDrawable(getResources(), defaultBitmap, task));
 			task.execute();
 		}
 	}
 
 	/**
 	 * Support class that we use to bind a task to the drawable in order to know if it was occupied by another task
 	 */
 	class AsyncDrawable extends BitmapDrawable {
 		// we aren't using weak references because we keep reusing this data to reduce flickering in the view
 		private final BitmapWorkerTask bitmapWorkerTask;
 
 		public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
 			super(res, bitmap);
 			this.bitmapWorkerTask = bitmapWorkerTask;
 		}
 
 		public BitmapWorkerTask getBitmapWorkerTask() {
 			return bitmapWorkerTask;
 		}
 	}
 
 	/**
 	 * The actual task in charge of editing the image
 	 */
 	public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
 		private final WeakReference<ImageView> imageViewReference;
 		private final BitmapWorkerTaskBuilder bwtb;
 
 		public BitmapWorkerTask(ImageView imageView, BitmapWorkerTaskBuilder bwtb) {
 			imageViewReference = new WeakReference<ImageView>(imageView);
 			this.bwtb = bwtb;
 		}
 
 		/**
 		 * Returns a copy of the Builder to read the info. The imageView can't be accessed from this.
 		 *
 		 * @return
 		 */
 		public BitmapWorkerTaskBuilder getBuilderCopy() {
 			return new BitmapWorkerTaskBuilder(bwtb);
 		}
 
 		/**
 		 * Resize image in background
 		 */
 		@Override
 		protected Bitmap doInBackground(Integer... params) {
 			Bitmap image = getBitmapFromMemCache("models.EntryAdapter.thumbnails." + bwtb.getPrefix() + bwtb.getPath());
 
 			if (image != null) {
 				return image;
 			}
 
 			Bitmap bmp;
 
 			// just use lower inSampleSize
 			if (bwtb.getTargetWidth() != 0) {
 				// get the image width and height without loading it in memory
 				BitmapFactory.Options options = new BitmapFactory.Options();
 				options.inJustDecodeBounds = true;
 				BitmapFactory.decodeFile(bwtb.getPath(), options);
 				int height = options.outHeight;
 				int width = options.outWidth;
 
 				// reduce the amount of data allocated in memory with higher inSampleSize
 				options = new BitmapFactory.Options();
 				options.inSampleSize = 1;
 				if (height > bwtb.getTargetHeight() || width > bwtb.getTargetWidth()) {
 					final int heightRatio = Math.round((float) height / (float) bwtb.getTargetHeight());
 					final int widthRatio = Math.round((float) width / (float) bwtb.getTargetWidth());
 					options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 				}
 
 				bmp = BitmapFactory.decodeFile(bwtb.getPath(), options);
 			} else {
 				bmp = BitmapFactory.decodeFile(bwtb.getPath());
 			}
 
 			if (bwtb.isCenterCrop()) {
 				// center crop so it's square and pretty
 				// credits to http://stackoverflow.com/a/6909144/644504
 				// for the solution to "center crop" resize
 				if (bmp.getWidth() >= bmp.getHeight()) {
 					bmp = Bitmap.createBitmap(
 						bmp,
 						bmp.getWidth() / 2 - bmp.getHeight() / 2,
 						0,
 						bmp.getHeight(),
 						bmp.getHeight()
 					);
 				} else {
 					bmp = Bitmap.createBitmap(
 						bmp,
 						0,
 						bmp.getHeight() / 2 - bmp.getWidth() / 2,
 						bmp.getWidth(),
 						bmp.getWidth()
 					);
 				}
 			}
 
 			if (bwtb.getTargetWidth() != 0 && bwtb.isHighQuality()) {
 				bmp = Bitmap.createScaledBitmap(bmp, bwtb.getTargetWidth(), bwtb.getTargetHeight(), false);
 			}
 
 			if (bwtb.getRoundedCorner() != 0) {
 				bmp = getRoundedCornerBitmap(bmp, bwtb.getRoundedCorner());
 			}
 
 			return bmp;
 		}
 
 		/**
 		 * Applies rounded corners
 		 * Sets background color to black and blurs image borders
 		 * Inspired by: http://stackoverflow.com/a/3292810/644504
 		 *
 		 * @param bitmap
 		 * @return Bitmap with rounded corners
 		 */
 		public Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundedPixels) {
 			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
 				.getHeight(), Bitmap.Config.ARGB_8888);
 			Canvas canvas = new Canvas(output);
 
 			Paint paint;
 			RectF rectF;
 			Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 			rectF = new RectF(rect);
 
 			paint = new Paint();
 			paint.setAntiAlias(true);
 			paint.setColor(Color.BLACK);
 			canvas.drawRoundRect(rectF, roundedPixels, roundedPixels, paint);
 			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
 			canvas.drawBitmap(bitmap, rect, rect, paint);
 
 			return output;
 		}
 
 		/**
 		 * Returns the edited image unless the imageView has been occupied by another image
 		 *
 		 * @param bitmap
 		 */
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
			if (!isCancelled() && imageViewReference != null && bitmap != null) {
 				addBitmapToMemoryCache("models.EntryAdapter.thumbnails."  + bwtb.getPrefix() + bwtb.getPath(), bitmap);
 				final ImageView imageView = imageViewReference.get();
 				if (imageView != null) {
 					imageView.setImageDrawable(new AsyncDrawable(getResources(), bitmap, this));
 				}
 			}
 		}
 	}
 }

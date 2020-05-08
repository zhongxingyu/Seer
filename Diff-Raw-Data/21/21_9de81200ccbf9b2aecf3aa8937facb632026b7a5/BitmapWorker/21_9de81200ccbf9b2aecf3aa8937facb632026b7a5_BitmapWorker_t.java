 package net.analogyc.wordiary.models;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.util.LruCache;
 import android.util.DisplayMetrics;
import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 import android.widget.ImageView;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 
 public class BitmapWorker extends Fragment {
 
     private static final String TAG = "BitmapWorker";
     private LruCache<String, Bitmap> mMemoryCache;
 
     private static final int FULL_SIZE_AVATAR = 512;
     private static final int HALF_SIZE_AVATAR = 256;
     private Bitmap[] mAvatars;
     private float mDensity;
 
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
 
         Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         DisplayMetrics dm = new DisplayMetrics();
         display.getMetrics(dm);
         mDensity = dm.density < 1.0f ? 1.0f : (Math.round(dm.density * 100f)) / 100f;
 
         try {
             String[] avatar_paths = getActivity().getAssets().list("avatars");
             mAvatars = new Bitmap[avatar_paths.length];
             for (int i = 0; i < avatar_paths.length; i++) {
                 mAvatars[i] = BitmapFactory
                         .decodeStream(getActivity().getAssets().open("avatars/" + avatar_paths[i]));
 
                 // reduce ram usage on devices with ldpi by using half size avatars
                 if (dm.density < 1.5f) {
                     mAvatars[i] = Bitmap.createScaledBitmap(mAvatars[i], HALF_SIZE_AVATAR, HALF_SIZE_AVATAR, false);
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 
     }
 
     /**
      * Adds the bitmap to the cache
      *
      * @param key    Unique key for the modified image
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
      * Clears the image from the cache, given the mPath
      *
      * @param path The mPath of the bitmap
      */
     public void clearBitmapFromMemCache(String prefix, String path) {
         mMemoryCache.remove(prefix + path);
     }
 
     /**
      * Returns a new builder to prepare the BitmapWorkerTask
      *
      * @param imageView The view in which the image will be displayed
      * @param path      The mPath to the image
      * @return The builder object to prepare the task
      */
     public BitmapWorkerTaskBuilder createTask(ImageView imageView, String path) {
         return new BitmapWorkerTaskBuilder(imageView, path);
     }
 
     /**
      * Builder for the task, allows chained settings
      */
     public class BitmapWorkerTaskBuilder {
 
         protected ImageView mImageView;
         protected String mPath;
         protected int mShowDefault;
         protected int mTargetWidth;
         protected int mTargetHeight;
         protected boolean mCenterCrop = false;
         protected boolean mHighQuality = true;
         protected int mRoundedCorner;
         protected String mPrefix = "";
 
         public BitmapWorkerTaskBuilder(ImageView imageView, String path) {
             mImageView = imageView;
             mPath = path;
         }
 
         /**
          * Clones the builder so it can't be modified further
          *
          * @param b The task builder to duplicate
          */
         protected BitmapWorkerTaskBuilder(BitmapWorkerTaskBuilder b) {
             mPath = b.getPath();
             mShowDefault = b.getShowDefault();
             mTargetWidth = b.getTargetWidth();
             mTargetHeight = b.getTargetHeight();
             mCenterCrop = b.isCenterCrop();
             mHighQuality = b.isHighQuality();
             mRoundedCorner = b.getRoundedCorner();
             mPrefix = b.getPrefix();
         }
 
         /**
          * Returns the path for the image file
          *
          * @return The path
          */
         public String getPath() {
             return mPath;
         }
 
         /**
          * Gets an int that is used to determine which default image to display
          *
          * @return An integer that allows defining which default image to display
          */
         public int getShowDefault() {
             return mShowDefault;
         }
 
         /**
          * Set a number to know
          *
          * @param showDefault
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setShowDefault(int showDefault) {
             mShowDefault = showDefault;
             return this;
         }
 
         /**
          * Get the target width for the resize
          *
          * @return The target width
          */
         public int getTargetWidth() {
             return mTargetWidth;
         }
 
         /**
          * Set the target width for the resized image
          *
          * @param targetWidth The target width
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setTargetWidth(int targetWidth) {
             mTargetWidth = Math.round(targetWidth * mDensity);
             return this;
         }
 
         /**
          * Get the target height for the resize
          *
          * @return The target height
          */
         public int getTargetHeight() {
             return mTargetHeight;
         }
 
         /**
          * Set the target width for the resized image
          *
          * @param targetHeight The target height
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setTargetHeight(int targetHeight) {
             mTargetHeight = Math.round(targetHeight * mDensity);
             return this;
         }
 
         /**
          * Whether the image will be cropped in a square block
          *
          * @return True if the image will be cropped in a square block, false otherwise
          */
         public boolean isCenterCrop() {
             return mCenterCrop;
         }
 
         /**
          * Whether the image should be cropped in a square block
          *
          * @param centerCrop True if it should be cropped in a square block
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setCenterCrop(boolean centerCrop) {
             mCenterCrop = centerCrop;
             return this;
         }
 
         /**
          * Whether we're going to resize in high quality or not
          *
          * @return True if high quality, false if low quality
          */
         public boolean isHighQuality() {
             return mHighQuality;
         }
 
         /**
          * Set whether the image should be resized with fine quality but slower
          *
          * @param highQuality True for high quality, false for low quality
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setHighQuality(boolean highQuality) {
             mHighQuality = highQuality;
             return this;
         }
 
         /**
          * Gets the radius of the rounded corner
          *
          * @return The radius
          */
         public int getRoundedCorner() {
             return mRoundedCorner;
         }
 
         /**
          * Sets the radius of the rounded corner
          *
          * @param roundedCorner
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setRoundedCorner(int roundedCorner) {
             mRoundedCorner = roundedCorner;
             return this;
         }
 
         /**
          * Set the prefix to use for distinguishing cache
          *
          * @param prefix
          * @return The current object
          */
         public BitmapWorkerTaskBuilder setPrefix(String prefix) {
             mPrefix = prefix;
             return this;
         }
 
         /**
          * Gets the prefix to use for distinguishing cache
          *
          * @return The prefix
          */
         public String getPrefix() {
             return mPrefix;
         }
 
         /**
          * Runs the worker and inserts a default image in the imageView. Inserts the resized bitmap in the ImageView when done.
          */
         public void execute() {
             // stop the previous task if we're going to use this drawable with another Bitmap
             if (mImageView.getDrawable() instanceof AsyncDrawable) {
                 BitmapWorkerTask oldTask = ((AsyncDrawable) mImageView.getDrawable()).getBitmapWorkerTask();
                 if (oldTask != null) {
                     oldTask.cancel(true);
 
                     // don't reload the image if it's the same as in the drawable
                     if (getPath() != null && getPath().equals(oldTask.getBuilderCopy().getPath())) {
                        BitmapWorkerTask task = new BitmapWorkerTask(mImageView, this);
                        mImageView.setImageDrawable(new AsyncDrawable(getResources(), task));
                        task.execute();
                         return;
                     }
                 }
             }
 
             Bitmap avatar = mAvatars[getShowDefault() % mAvatars.length];
 
             if (getPath() == null) {
                 mImageView.setImageBitmap(avatar);
             } else {
                 BitmapWorkerTask task = new BitmapWorkerTask(mImageView, this);
                 mImageView.setImageDrawable(new AsyncDrawable(getResources(), avatar, task));
                 task.execute();
             }
         }
     }
 
     /**
      * Support class that we use to bind a task to the drawable in order to know if it was occupied by another task
      */
     class AsyncDrawable extends BitmapDrawable {
         // we aren't using weak references because we keep reusing this data to reduce flickering in the view
         private final BitmapWorkerTask mBitmapWorkerTask;
 
        public AsyncDrawable(Resources res, BitmapWorkerTask bitmapWorkerTask) {
            super(res);
            mBitmapWorkerTask = bitmapWorkerTask;
        }

         public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
             super(res, bitmap);
             mBitmapWorkerTask = bitmapWorkerTask;
         }
 
         public BitmapWorkerTask getBitmapWorkerTask() {
             return mBitmapWorkerTask;
         }
     }
 
     /**
      * The actual task in charge of editing the image
      */
     class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
         private final WeakReference<ImageView> mImageViewReference;
         private final BitmapWorkerTaskBuilder mBwtb;
 
         public BitmapWorkerTask(ImageView imageView, BitmapWorkerTaskBuilder bwtb) {
             mImageViewReference = new WeakReference<ImageView>(imageView);
             mBwtb = bwtb;
         }
 
         /**
          * Returns a copy of the Builder to read the info. The imageView can't be accessed from this.
          *
          * @return The task builder
          */
         public BitmapWorkerTaskBuilder getBuilderCopy() {
             return new BitmapWorkerTaskBuilder(mBwtb);
         }
 
         /**
          * Resize image in background
          */
         @Override
         protected Bitmap doInBackground(Integer... params) {
             Bitmap image = getBitmapFromMemCache(mBwtb.getPrefix() + mBwtb.getPath());
 
             if (image != null) {
                 return image;
             }
 
             Bitmap bmp;
 
             // just use lower inSampleSize
             if (mBwtb.getTargetWidth() > 0) {
                 // get the image width and height without loading it in memory
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inJustDecodeBounds = true;
                 BitmapFactory.decodeFile(mBwtb.getPath(), options);
                 int height = options.outHeight;
                 int width = options.outWidth;
 
                 // reduce the amount of data allocated in memory with higher inSampleSize
                 options = new BitmapFactory.Options();
                 options.inSampleSize = 1;
                 if (height > mBwtb.getTargetHeight() && width > mBwtb.getTargetWidth()) {
                     final int heightRatio = Math.round((float) height / (float) mBwtb.getTargetHeight());
                     final int widthRatio = Math.round((float) width / (float) mBwtb.getTargetWidth());
                     options.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
                 }
 
                 bmp = BitmapFactory.decodeFile(mBwtb.getPath(), options);
             } else {
                 bmp = BitmapFactory.decodeFile(mBwtb.getPath());
             }
 
             // if the image isn't found, don't set anything, cancel the thread and return null
             if (bmp == null) {
                 cancel(true);
                 return null;
             }
 
             if (mBwtb.isCenterCrop()) {
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
 
             if (mBwtb.getTargetWidth() != 0 && mBwtb.isHighQuality()) {
                 bmp = Bitmap.createScaledBitmap(bmp, mBwtb.getTargetWidth(), mBwtb.getTargetHeight(), true);
             }
 
             if (mBwtb.getRoundedCorner() != 0) {
                 bmp = getRoundedCornerBitmap(bmp, mBwtb.getRoundedCorner());
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
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
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
             super.onPostExecute(bitmap);
             if (!isCancelled() && isAdded() && mImageViewReference != null && bitmap != null) {
                 addBitmapToMemoryCache(mBwtb.getPrefix() + mBwtb.getPath(), bitmap);
                 final ImageView imageView = mImageViewReference.get();
                 if (imageView != null) {
                     imageView.setImageDrawable(new AsyncDrawable(getResources(), bitmap, this));
                 }
             }
         }
     }
 }

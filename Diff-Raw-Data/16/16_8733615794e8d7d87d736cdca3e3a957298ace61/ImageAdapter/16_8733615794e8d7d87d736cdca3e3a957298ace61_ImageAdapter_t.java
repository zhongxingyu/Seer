 package com.trydish.find;
 import java.lang.ref.WeakReference;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.util.LruCache;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.trydish.main.R;
 
 public class ImageAdapter extends BaseAdapter {
 	public final static double REGULAR = 1.0;
 	public final static double HALF = 0.5;
 	
 	private Context mContext;
 	private int screenWidth;
 	private int screenHeight;
 	private int imageDimension;
 	private Bitmap mPlaceHolderBitmap;
 	private LruCache<String, Bitmap> mMemoryCache;
 
 	public ImageAdapter(Context c, double scale) {
 		mContext = c;
 		screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
 		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
 		imageDimension = (int) Math.round((screenWidth / 1.5 - 10) * scale);
 		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
 		mPlaceHolderBitmap = Bitmap.createBitmap(screenWidth, imageDimension, conf);
 		
 	    // Get max available VM memory, exceeding this amount will throw an
 	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
 	    // int in its constructor.
 	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
 
 	    // Use 1/8th of the available memory for this memory cache.
 	    final int cacheSize = maxMemory / 8;
 
 	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
 	        @Override
 	        protected int sizeOf(String key, Bitmap bitmap) {
 	            // The cache size will be measured in kilobytes rather than
 	            // number of items.
 	            return bitmap.getByteCount() / 1024;
 	        }
 	    };
 	}
 	
 	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
 	    if (getBitmapFromMemCache(key) == null) {
 	        mMemoryCache.put(key, bitmap);
 	    }
 	}
 
 	public Bitmap getBitmapFromMemCache(String key) {
 	    return mMemoryCache.get(key);
 	}
 
 	public int getCount() {
 		return mThumbIds.length;
 	}
 
 	public Object getItem(int position) {
 		return null;
 	}
 
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	// create a new ImageView for each item referenced by the Adapter
 	// snippet from :
 	// http://stackoverflow.com/questions/7706913/overlay-text-over-imageview-in-framelayout-progrmatically-android
 	
 	@SuppressLint("NewApi")
 	public View getView(int position, View convertView, ViewGroup parent) {
 		RelativeLayout rLayout;
 		ImageView imageView;
 		if (convertView == null) {
 			// Setting up the parent RelativeLayout
 			rLayout = new RelativeLayout(mContext);
			GridView.LayoutParams rParams = new GridView.LayoutParams(screenWidth, imageDimension);
 			rLayout.setLayoutParams(rParams);
 		} else {
 			rLayout = (RelativeLayout) convertView;
 		}
 		// Setting up our ImageView
 		imageView = new ImageView(mContext);
		imageView.setLayoutParams(new GridView.LayoutParams(screenWidth, imageDimension)); // 255, 200
 		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 		loadBitmap(mThumbIds[position], imageView);
 		
 		// Creating our different TextViews
 		TextView foodText = foodNameText(titles[position], restaurants[position]);
 		// TextView restaurant = restaurantText(restaurants[position]);
 		
 		// Hardcoding rating bar
 		RatingBar ratingBar = new RatingBar(mContext, null, android.R.attr.ratingBarStyleIndicator);
 		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
 				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		tParams.setMargins(0, 55, 0, 0);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
 		ratingBar.setLayoutParams(tParams);
 		ratingBar.setRating((float)3.5);
 		ratingBar.setBackgroundColor(Color.argb(100, 0, 0, 0));
 		ratingBar.setPadding(0, 0, 0, 5);
 		
 		// Adding everything to the RelativeLayout
 		rLayout.addView(imageView);
 		rLayout.addView(foodText);
 		// rLayout.addView(restaurant);
 		rLayout.addView(ratingBar);
 		rLayout.addView(distanceText(distances[position]));
 
 		return rLayout;
 	}
 	
 	@SuppressLint("NewApi")
 	public TextView distanceText(String location) {
 		// Setting the RelativeLayout parameters for the Food Name TextView
 		TextView text = new TextView(mContext);
 		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
 				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		tParams.setMargins(0, 0, 0, 5);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
 		text.setLayoutParams(tParams);
 		// These are TextView specific parameters (we're done messing with RelativeLayout here)
 		text.setPadding(10, 5, 10, 5);
 		text.setBackgroundColor(Color.argb(100, 0, 0, 0));
 		text.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
 		text.setText(Html.fromHtml(location + " <i><small>miles away</small></i>"));
 		text.setTextColor(Color.WHITE);                            
 		text.setTypeface(Typeface.DEFAULT_BOLD);
 		text.setTextSize(20);
 		
 		return text;
 	}
 	
 	@SuppressLint("NewApi")
 	public TextView foodNameText(String foodName, String restaurant) {
 		// Setting the RelativeLayout parameters for the Food Name TextView
 		TextView text = new TextView(mContext);
 		RelativeLayout.LayoutParams tParams = new RelativeLayout.LayoutParams
 				(LayoutParams.WRAP_CONTENT, 50);
 		tParams.setMargins(0, 5, 0, 0);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
 		tParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
 		text.setLayoutParams(tParams);
 		// These are TextView specific parameters (we're done messing with RelativeLayout here)
 		text.setPadding(10, 0, 10, 5);
 		text.setBackgroundColor(Color.argb(100, 0, 0, 0));
 		text.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
 		text.setText(Html.fromHtml(foodName + 
 				" <i><small><small>from</small></small></i> " + 
 				"<small>" + restaurant + "</small>"));
 		text.setTextColor(Color.WHITE);                            
 		text.setTypeface(Typeface.DEFAULT_BOLD);
 		text.setTextSize(25);
 		
 		return text;
 	}
 	
 	public void loadBitmap(int resId, ImageView imageView) {
 		if (cancelPotentialWork(resId, imageView)) {
 		    final String imageKey = String.valueOf(resId);
 		    final Bitmap bitmap = getBitmapFromMemCache(imageKey);
 		    if (bitmap != null) {
 		    	imageView.setImageBitmap(bitmap);
 		    } else {
 		    	final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
 		    	final AsyncDrawable asyncDrawable =
 		    			new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
 		    	imageView.setImageDrawable(asyncDrawable);
 		    	task.execute(resId, screenWidth, imageDimension);
 		    }
 		}
 	}
 	
 	public boolean cancelPotentialWork(int data, ImageView imageView) {
 		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
 		
 		if (bitmapWorkerTask != null) {
 			final int bitmapData = bitmapWorkerTask.getData();
 			if (bitmapData != data) {
 				// cancel previous task
 				bitmapWorkerTask.cancel(true);
 			} else {
 				// the same work already in progress
 				return false;
 			}
 		}
 		
 		// no task associated with the ImageView, or an existing task was cancelled
 		return true;
 	}
 	
 	public BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
 		if (imageView != null) {
 			final Drawable drawable = imageView.getDrawable();
 			if (drawable instanceof AsyncDrawable) {
 				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
 				return asyncDrawable.getBitmapWorkerTask();
 			}
 		}
 		return null;
 	}
 
 	// references to our images
 	private Integer[] mThumbIds = {
 			R.drawable.food, R.drawable.wings,
 			R.drawable.dimsum, R.drawable.burger,
 			R.drawable.food, R.drawable.wings,
 			R.drawable.dimsum, R.drawable.burger,
 	};
 	
 	private String[] titles = {
 			"Super Awesome Spaghetti",
 			"Fried Chicken Wings",
 			"Amazingly Asian Dimsum",
 			"Super Fatty Hamburger",
 			"Super Awesome Spaghetti",
 			"Fried Chicken Wings",
 			"Amazingly Asian Dimsum",
 			"Super Fatty Hamburger",
 	};
 	
 	private String[] restaurants = {
 			"Spaghetti Me",
 			"Wing Go",
 			"The Chinese",
 			"Heartattack Grill",
 			"Spaghetti Me",
 			"Wing Go",
 			"The Chinese",
 			"Heartattack Grill",
 	};
 	
 	private String[] distances = {
 			"0.4",
 			"0.6",
 			"1.2",
 			"1.5",
 			"0.4",
 			"0.6",
 			"1.2",
 			"1.5",
 	};
 	
 	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
 		private final WeakReference<ImageView> imageViewReference;
 		private int data = 0;
 		private int width = 0;
 		private int height = 0;
 		
 		public BitmapWorkerTask(ImageView imageView) {
 			// user WeakReference to ensure the ImageView can be garbage collected
 			imageViewReference = new WeakReference<ImageView>(imageView);
 		}
 		
 		// Decode image in background
 		@Override
 		protected Bitmap doInBackground(Integer... params) {
 			data = params[0];
 			width = params[1];
 			height = params[2];
 			final Bitmap bitmap = decodeSampledBitmapFromResource(imageViewReference.get().getContext().getResources(),
 					data, width, height);
 			addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
 			return bitmap;
 		}
 		
 		@Override
 		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
 			if (imageViewReference != null && bitmap != null) {
 				final ImageView imageView = imageViewReference.get();
 				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
 				if (this == bitmapWorkerTask && imageView != null) {
 					imageView.setImageBitmap(bitmap);
 				}
 			}
 		}
 		
 		public int getData() {
 			return data;
 		}
 		
 		public int calculateInSampleSize(BitmapFactory.Options options, 
 				int reqWidth, int reqHeight) {
 			
 			// setting raw width and raw heights
 			final int rawWidth = options.outWidth;
 			final int rawHeight = options.outHeight;
 			int sampleSize = 1;
 			if (reqWidth < rawWidth || reqHeight < reqHeight) {
 		        // Calculate ratios of height and width to requested height and width
 				final int widthRatio = Math.round((float) rawWidth / (float) reqWidth);
 				final int heightRatio = Math.round((float) rawHeight / (float) reqHeight);
 		        // Choose the smallest ratio as inSampleSize value, this will guarantee
 		        // a final image with both dimensions larger than or equal to the
 		        // requested height and width.
 				sampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 			}
 			return sampleSize;
 		}
 		
 		public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, 
 				int reqWidth, int reqHeight) {
 
 		    // First decode with inJustDecodeBounds=true to check dimensions
 		    final BitmapFactory.Options options = new BitmapFactory.Options();
 		    options.inJustDecodeBounds = true;
 		    BitmapFactory.decodeResource(res, resId, options);
 
 		    // Calculate inSampleSize
 		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
 
 		    // Decode bitmap with inSampleSize set
 		    options.inJustDecodeBounds = false;
 		    return BitmapFactory.decodeResource(res, resId, options);
 		}
 	}
 	
 	class AsyncDrawable extends BitmapDrawable {
 		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
 		
 		public AsyncDrawable(Resources res, Bitmap bitmap,
 				BitmapWorkerTask bitmapWorkerTask) {
 			super(res, bitmap);
 			bitmapWorkerTaskReference =
 					new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
 		}
 		
 		public BitmapWorkerTask getBitmapWorkerTask() {
 			return bitmapWorkerTaskReference.get();
 		}
 	}
 }

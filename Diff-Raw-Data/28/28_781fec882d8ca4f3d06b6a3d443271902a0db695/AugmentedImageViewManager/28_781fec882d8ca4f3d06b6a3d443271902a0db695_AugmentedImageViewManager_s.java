 package com.parworks.mars.view.siteexplorer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.parworks.androidlibrary.response.AugmentImageResultResponse;
 import com.parworks.arviewer.ARViewerActivity;
 import com.parworks.arviewer.utils.AugmentedDataUtils;
 import com.parworks.arviewer.utils.ImageUtils;
 import com.parworks.mars.R;
 import com.parworks.mars.cache.BitmapCache;
 import com.parworks.mars.cache.BitmapWorkerTask;
 import com.parworks.mars.cache.BitmapWorkerTask.BitmapWorkerListener;
 import com.parworks.mars.model.db.AugmentedImagesTable;
 import com.parworks.mars.utils.JsonMapper;
 
 public class AugmentedImageViewManager {
 	
 	public static final String TAG = AugmentedImageViewManager.class.getName();
 	
 	private final String mSiteId;
 	private final ProgressBar mAugmentedImagesProgressBar;
 	private final LinearLayout mAugmentedImagesLayout;
 	private final Context mContext;
 	private final AugmentedImageAdapter mAdapter;
 	private final List<Bitmap> mBitmaps;
 	private final TextView mAugmentedImagesTotalTextView;
 	
 	private final static int AUGMENTED_IMAGE_HORIZONTAL_MARGINS = 8; //pixels
 	private final static int AUGMENTED_IMAGE_VERTICAL_MARGINS = 16;
 	
	public AugmentedImageViewManager(String siteId, Context context, ProgressBar augmentedImagesProgressBar, LinearLayout augmentedImagesLayout, TextView augmentedImagesTotalTextView) {
 		mSiteId = siteId;
		mContext = context;
 		mAugmentedImagesProgressBar = augmentedImagesProgressBar;
 		mBitmaps = new ArrayList<Bitmap>();
		mAdapter = new AugmentedImageAdapter(context,mBitmaps);
 		mAugmentedImagesLayout = augmentedImagesLayout;
 		mAugmentedImagesTotalTextView = augmentedImagesTotalTextView;	
 	}
 	
 	public void setAugmentedImages(Cursor data) {
 		// TODO limit this to be 20 or 10, otherwise it will run out of memory
 		// if there are too many images
 		// Or, the horizontal scroll view should cache property. That's the best
 		// solution
 		
 		for(data.moveToFirst();!data.isAfterLast();data.moveToNext()) {
 	    	final String url = data.getString(data.getColumnIndex(AugmentedImagesTable.COLUMN_GALLERY_SIZE_URL));	    	
 	    	final String augmentedData = data.getString(data.getColumnIndex(AugmentedImagesTable.COLUMN_CONTENT));
 	    	final String contentUrl = data.getString(data.getColumnIndex(AugmentedImagesTable.COLUMN_CONTENT_SIZE_URL));
 			final int width = data.getInt(data.getColumnIndex(AugmentedImagesTable.COLUMN_WIDTH));
 			final int height = data.getInt(data.getColumnIndex(AugmentedImagesTable.COLUMN_HIEGHT));
 			final String imageId = data.getString(data.getColumnIndex(AugmentedImagesTable.COLUMN_IMAGE_ID));
 			
 	    	if(url != null) {
 	    		Log.d(TAG,"url is: " + url);
 	    		Bitmap augmentedBitmap = BitmapCache.get().getBitmap(BitmapCache.getImageKeyFromURL(url));
 	    		if(augmentedBitmap == null) {
 					Log.d(TAG, "Bitmap not found in cache, start to download it.");
 					new BitmapWorkerTask(url, new BitmapWorkerListener() {					
 						@Override
 						public void bitmapLoaded(Bitmap bitmap) {
 							addBitmap(bitmap, imageId, contentUrl, augmentedData, width, height);							
 						}
 					}).execute();
 	    		} else {
 	    			addBitmap(augmentedBitmap, imageId, contentUrl, augmentedData, width, height);
 	    		}	    		
 	    	} else {
 	    		continue;
 	    	}
 	    }
 	    showAugmentedImagesView();
 	    setAugmentedImagesTotalTextView(data.getCount());		
 	}
 	
 	private void addBitmap(final Bitmap bitmap, final String imageId, final String contentUrl, 
 			final String augmentedData, final int width, final int height) {
 		
 		ImageView imageView = new ImageView(mContext);
 		imageView.setImageBitmap(bitmap);
		LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 		imageViewParams.setMargins(AUGMENTED_IMAGE_HORIZONTAL_MARGINS, AUGMENTED_IMAGE_VERTICAL_MARGINS, AUGMENTED_IMAGE_HORIZONTAL_MARGINS, AUGMENTED_IMAGE_VERTICAL_MARGINS);
 		imageView.setLayoutParams(imageViewParams);
 		imageView.setScaleType(ScaleType.CENTER_CROP);
 		imageView.setBackground(mContext.getResources().getDrawable(R.drawable.activity_explore_augmented_photos_border));
 		
 		imageView.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {				
 				Bitmap bitmap = BitmapCache.get().getBitmap(BitmapCache.getImageKeyFromURL(contentUrl));
 				if (bitmap != null) {
 					showARViewer(imageId, bitmap, augmentedData, width, height);
 				} else {
 					new BitmapWorkerTask(contentUrl, new BitmapWorkerListener() {				
 						@Override
 						public void bitmapLoaded(Bitmap bitmap) {
 							showARViewer(imageId, bitmap, augmentedData, width, height);
 						}
 					}).execute();
 				}
 			}
 		});
 		
 		mAugmentedImagesLayout.addView(imageView);
 	}
 	
 	private void showARViewer(String imageId, Bitmap bitmap, String augmentedData, int width, int height) {
 		try {			
 			final Intent intent = new Intent(mContext, ARViewerActivity.class);
 			intent.putExtra("site-id", mSiteId);
 			intent.putExtra("image-id", imageId);
 			intent.putExtra("file-path", ImageUtils.saveBitmapAsFile(bitmap, null));
 			AugmentImageResultResponse data = JsonMapper.get().readValue(augmentedData, AugmentImageResultResponse.class);
 			intent.putExtra("augmented-data", AugmentedDataUtils.convertAugmentResultResponse(imageId, data));
 			intent.putExtra("original-size", width + "x" + height);
 			mContext.startActivity(intent);
 		} catch (IOException e) {
 			Log.e(TAG, "Failed to load the augmented image data", e);
 		}
 	}
 	
 	private void showAugmentedImagesView() {
 		mAugmentedImagesLayout.setVisibility(View.VISIBLE);
 		mAugmentedImagesProgressBar.setVisibility(View.INVISIBLE);
 	}
 	
 	private void setAugmentedImagesTotalTextView(int imagesTotal) {
 		// TODO to enable the total number of augmented images,
 		// we need more reliable image loading, rather than loading
 		// all the image together at the same time
 		
 //		String text;
 //		if(imagesTotal == 1) {
 //			text = " augmented image";
 //		} else {
 //			text = " augmented images";
 //		}
 //		mAugmentedImagesTotalTextView.setText(imagesTotal + text);
 //		mAugmentedImagesTotalTextView.setText("Recently augmented images");
 	}
 }

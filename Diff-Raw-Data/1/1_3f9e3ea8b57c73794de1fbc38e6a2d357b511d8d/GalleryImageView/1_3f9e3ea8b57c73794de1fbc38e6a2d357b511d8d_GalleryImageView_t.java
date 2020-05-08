 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery;
 
 import java.lang.ref.WeakReference;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.Typeface;
 import android.os.AsyncTask.Status;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;
 
 public class GalleryImageView extends LinearLayout implements ImageLoaderTaskListener{
 	private static final String CLASS_TAG = "GalleryImageView";
 	private final ProgressBar mProgressBar;
 	private final ImageView mImageView;
 	private final TextView mTitleTextView;
 	private final boolean mShowTitle;
 	private GalleryObject mGalleryObject;
 	private Bitmap mBitmap;
 	private ImageLoaderTask mImageLoaderTask;
 	private WeakReference<ImageLoaderTaskListener> mListener;
 	
 	public GalleryImageView(Context context, android.view.ViewGroup.LayoutParams layoutParams, boolean showTitle) {
 		super(context);
 		mShowTitle = showTitle;
 		
 		mImageView = CreateImageView(context);
 		
 		mProgressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
 		mProgressBar.setVisibility(GONE);
 		this.addView(mProgressBar);
 		
 		this.setOrientation(VERTICAL);
 		this.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
 		this.setLayoutParams(layoutParams);
 		
 		this.addView(mImageView);
 		
 		
 		
 		if(mShowTitle){
 			mTitleTextView = new TextView(context);
 			mTitleTextView.setTextSize(16);
 			mTitleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
 			mTitleTextView.setTypeface(Typeface.create("Tahoma", Typeface.BOLD));
 			this.addView(mTitleTextView);
 		}
 		else{
 			mTitleTextView = null;
 		}
 		
 		mImageLoaderTask = null;
		mListener = new WeakReference<ImageLoaderTaskListener>(null);
 	}
 	
 	public void setGalleryObject(GalleryObject galleryObject)
 	{
 		mGalleryObject = galleryObject;
 		this.setTitle(galleryObject.getTitle());
 	}
 	
 	public GalleryObject getGalleryObject(){
 		return mGalleryObject;
 	}
 		
 	public void setTitle(String title)
 	{
 		if(mTitleTextView != null){
 			mTitleTextView.setText(title);
 		}
 	}
 	
 	private ImageView CreateImageView(Context context) {
 		ImageView imageView = new ImageView(context);
 		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
         imageView.setPadding(5, 5, 5, 5);
         imageView.setDrawingCacheEnabled(false);
         
 		return imageView;
 	}
 
 	public void recylceBitmap(){
 		if (mBitmap != null) {
 			Log.d(CLASS_TAG, String.format("Recycle %s",mGalleryObject.getObjectId()));
 			mImageView.setImageBitmap(null);
 			mBitmap.recycle();
 			mBitmap = null;
 		}
 	}
 	
 	public Matrix getImageMatrix(){
 		return mImageView.getMatrix();
 	}
 	
 	public void setImageMatrix(Matrix matrix) {
 		mImageView.setScaleType(ImageView.ScaleType.MATRIX);
 		mImageView.setImageMatrix(matrix);
 	}
 
 	public void cancelImageLoaderTask(){
 		
 		if(mImageLoaderTask != null){
 			Log.d(CLASS_TAG, String.format("Cancel downloadTask %s",mGalleryObject.getObjectId()));
 			mImageLoaderTask.cancel(true);
 			mImageLoaderTask = null;
 		}
 	}
 
 	public void setImageLoaderTask(ImageLoaderTask downloadTask) {
 		Log.d(CLASS_TAG, String.format("Reference downloadTask %s",mGalleryObject.getObjectId()));
 		mImageLoaderTask = downloadTask;		
 	}
 
 	public boolean isLoaded() {
 		return mBitmap != null;
 	}
 
 	public boolean isLoading() {
 		return mImageLoaderTask != null && mImageLoaderTask.getStatus() != Status.FINISHED;
 	}
 
 	public String getObjectId() {
 		return mGalleryObject.getObjectId();
 	}
 
 	public void onLoadingStarted(String uniqueId) {
 		mProgressBar.setVisibility(VISIBLE);
 		Log.d(CLASS_TAG, String.format("Loading started %s",uniqueId));
 		
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null) {
 			listener.onLoadingStarted(uniqueId);
 		}
 	}
 	
 	public void onLoadingProgress(String uniqueId, int currentValue, int maxValue) {
 		mProgressBar.setMax(maxValue);
 		mProgressBar.setProgress(currentValue);
 		
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null) {
 			listener.onLoadingProgress(uniqueId, currentValue, maxValue);
 		}
 	}
 
 	public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
 		mProgressBar.setVisibility(GONE);
 		mImageView.setImageBitmap(bitmap);
 		mBitmap = bitmap;
 		mImageLoaderTask = null;
 		Log.d(CLASS_TAG, String.format("Loading done %s",uniqueId));
 		
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null) {
 			listener.onLoadingCompleted(uniqueId, bitmap);
 		}
 	}
 
 	public void onLoadingCancelled(String uniqueId) {
 		Log.d(CLASS_TAG, String.format("DownloadTask was cancalled %s",uniqueId));
 		mImageLoaderTask = null;
 		mProgressBar.setVisibility(GONE);
 		mBitmap = null;
 		
 		ImageLoaderTaskListener listener = mListener.get();
 		if(listener != null) {
 			listener.onLoadingCancelled(uniqueId);
 		}
 	}
 
 	public void setListener(ImageLoaderTaskListener listener) {
 		mListener = new WeakReference<ImageLoaderTaskListener>(listener);
 	}
 
 	
 }
 

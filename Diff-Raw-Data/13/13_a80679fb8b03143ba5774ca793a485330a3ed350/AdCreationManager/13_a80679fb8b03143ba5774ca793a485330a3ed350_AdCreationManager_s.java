 package com.example.maple_android;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import com.example.ad_creation.*;
 import com.example.filters.*;
 import com.example.custom_views.ProgressView;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.net.Uri;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 
 /**
  * This class exists to manage the creation of an ad between activities. It
  * stores various metadata about the ad and also stores a stack of all versions
  * of the ad.
  * 
  * You can access this from any activity by calling
  * getApplication().getAdCreationManager
  * 
  */
 public class AdCreationManager {
 	public enum Filters {
 		GAUSSIAN("Gaussian"), POSTERIZE("Posterize"), NONE("None");
 
 		private final String text;
 
 		private Filters(final String text) {
 			this.text = text;
 		}
 
 		@Override
 		public String toString() {
 			return text;
 		}
 	}
 	
 	/* The application context */
 	private Context mContext;
 
 	/* The order of the ad creation funnel */
 	Class<?>[] mFunnel = { 
 			CropActivity.class,
 			CompanyTagActivity.class,
 			ColorAdjustmentActivity.class,
 			FilterActivity.class,
 			LogoActivity.class,
 			TextActivity.class,
 			PublishActivity.class
 	};
 	// keeps track of which stage of the funnel we
 	// are in as an index into mFunnel. 
 	// -1 for funnel not started yet.
 	private int mCurrentStage; 
 	
 	private static final float AD_DISPLAY_SCALE = 0.4f;
 
 	private String mCompanyName;
 	
 	// Array of logos for tagged company
 	private ArrayList<Bitmap> mCompanyLogos;
 	
 	// the logo the user has chosen to use
 	private Bitmap mLogo;
 
 	// Stack of bitmap layers
 	private Stack<Bitmap> mBitmapStack;
 
 	// Origin bitmap
 	private Bitmap mOriginalBitmap;
 
 	// File path of saved bitmap image
 	private Uri mFileUri;
 
 	// Current filter applied to image
 	private Filters mFilter;
 
 	public AdCreationManager(Context context, Bitmap currBitmap, Uri fileUri) {
 		mBitmapStack = new Stack<Bitmap>();
 		mBitmapStack.push(currBitmap);
 
 		mOriginalBitmap = currBitmap;
 
 		mFileUri = fileUri;
 
 		mCompanyLogos = new ArrayList<Bitmap>();
 
 		mCompanyName = null;
 
 		mFilter = Filters.NONE;
 		
 		mContext = context;
 
 		
 		mCurrentStage = -1; // -1 means the funnel hasn't been launched yet
 	}
 	
 	/** 
 	 * Set which company this ad is tagged with
 	 * @param name The company name
 	 */
 	public void setCompanyName(String name){
 		mCompanyName = name;
 		// start loading logos for this company
 		if(name != null) mCompanyLogos = CompanyList.getCompanyLogosFromServer(mContext, name);
 	}
 	
 	/**
 	 * Returns what company the ad has been tagged with, or
 	 * null if no tag has been set yet.
 	 * 
 	 * @return The company name
 	 */
 	public String getCompanyName(){
 		return mCompanyName;
 	}
 	
 	/**
 	 * Gets the arraylist of logos for the tagged company
 	 */
 	public ArrayList<Bitmap> getCompanyLogoList(){
 		return mCompanyLogos;
 	}
 	
 	/**
 	 * Sets which logo the user wants to use in 
 	 * the ad
 	 * @param logo The chosen logo
 	 */
 	public void setCompanyLogo(Bitmap logo){
 		mLogo = logo;
 	}
 	
 	/**
 	 * Returns the logo the user has chosen to
 	 * use for this ad.
 	 * @return The chosen logo. Null if none selected yet
 	 */
 	public Bitmap getCompanyLogo(){
 		return mLogo;
 	}
 
 	/**
 	 * Go to the next stage in the ad creation funnel. You must push the updated
 	 * ad before calling this if you want to save the modifications from the
 	 * current stage. 
 	 * 
 	 * @param context
 	 *            The activity context
 	 * @param bitmap The ad bitmap we want to save from the current stage
 	 */
 	public void nextStage(Context context, Bitmap bitmap) {
 		// if we are in the last stage already
 		// don't do anything
 		if(mCurrentStage + 1 > mFunnel.length) return;
 		
 		// push bitmap onto stack
 		pushBitmap(bitmap);
 		
 		// increment stage counter and start that activity
 		Intent intent = new Intent(context, mFunnel[++mCurrentStage]);
 		context.startActivity(intent);
 	}
 
 	/**
 	 * Go to the previous stage in the ad funnel
 	 * 
 	 * @param context
 	 *            The activity context
 	 */
 	public void previousStage(Context context) {
 		// if we are in the first stage there is nothing to go back to
 		if(mCurrentStage <= 0) return;
 		
 		// pop last ad to revert changes
 		popBitmap();
 		
 		// decrement stage counter and start that activity
 		Intent intent = new Intent(context, mFunnel[--mCurrentStage]);
 		context.startActivity(intent);
 	}
 
 	/**
 	 * Get the current bitmap
 	 * 
 	 * @return The current bitmap
 	 */
 	public Bitmap getCurrentBitmap() {
 		return mBitmapStack.peek();
 	}
 
 	/**
 	 * Pushes a bitmap onto the stack of changes that has taken place on the ad.
 	 * 
 	 * @param bitmap
 	 *            The bitmap to put on the stack.
 	 * @return The bitmap that is being pushed onto the stack
 	 */
 	private Bitmap pushBitmap(Bitmap bitmap) {
 		return mBitmapStack.push(bitmap);
 	}
 	
 	/**
 	 * Returns the top bitmap on the stack
 	 * @return The last ad pushed to the stack
 	 */
 	private Bitmap popBitmap(){
 		return mBitmapStack.pop();
 	}
 
 	/**
 	 * Gets the very original bitmap that was saved from the camera.
 	 * 
 	 * @return The original bitmap saved from camera
 	 */
 	public Bitmap getOriginalBitmap() {
 		return mOriginalBitmap;
 	}
 
 	/**
 	 * This will get the file uri where the original bitmap is stored. This way
 	 * we can save the ad to just one location.
 	 * 
 	 * @return The file path of the original photo taken by the camera.
 	 */
 	public Uri getFileUri() {
 		return mFileUri;
 	}
 
 	/**
 	 * Adds a filter to the given ad. The supplied ad is
 	 * unchanged, and the result is returned.
 	 * 
 	 * @param strFilter
 	 *            Name of the filter we want to use
 	 * @result The bitmap after the filter is applied            
 	 *           
 	 */
 	public Bitmap addFilter(Bitmap ad, int filter) {
 		MapleFilter mapleFilter = null;
 
 		if (filter == 1) {
 			mapleFilter = new MapleGaussianFilter();
 			mFilter = Filters.GAUSSIAN;
 		} else if (filter == 2) {
 			mapleFilter = new MaplePosterizeFilter();
 			mFilter = Filters.POSTERIZE;
 		} else if (filter == 0) {
 			this.pushBitmap(this.getOriginalBitmap());
 			mFilter = Filters.NONE;
 			return ad;
 		}
 
 		return mapleFilter.filterBitmap(ad);
 
 	}
 
 	/**
 	 * Gets the current filter so we can set that filter in the spinner.
 	 * 
 	 * @return The Filter enum that is being applied
 	 */
 	public Filters getCurrentFilter() {
 		return mFilter;
 	}
 
 	/**
 	 * Gets the current stage number in the ad creation funnel. This is 1-indexed
 	 * 
 	 * @return the current step in the ad creation funnel
 	 */
 	public int getReadableCurrentStage() {
 		return mCurrentStage + 1;
 	}
 
 	public int getCurrentStage() {
 		return mCurrentStage;
 	}
 	/**
 	 * Gets the number of steps in the ad creation funnel
 	 * 
 	 * @return the number of steps in the ad creation funnel
 	 */
 	public int getNumStages() {
 		return mFunnel.length;
 	}
 	
 	/**
 	 * Sets up some initial settings for most of the funnel views. Namely the progressBar and the Ad.
 	 * Must be called after setContentView
 	 */
 	public void setup(Activity activity) {
 		/* Hides action bar */
 		ActionBar actionBar = activity.getActionBar();
 		actionBar.hide();
 		
 		/* Sets up progress bar */
 		ProgressView progressBar = (ProgressView) activity.findViewById(R.id.progressBar);
 		
 		progressBar.setCurrentStage(this.getCurrentStage());
 		progressBar.setNumStages(this.getNumStages());
 		/* End progress bar setup */
 		
 		/* Ad setup */
 		// Deprecated for API level 13 but our min is 11 so we'll have to use this for now
 		int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
 		
 		ImageView ad = (ImageView) activity.findViewById(R.id.ad);	
 		
 		/* Will scaled the image view by a constant */
 		if (ad != null) {
 			ad.setImageBitmap(this.getCurrentBitmap());
 			int newHeight = (int) (AD_DISPLAY_SCALE * screenHeight);
 			int width = ad.getDrawable().getIntrinsicWidth();
 			int height = ad.getDrawable().getIntrinsicHeight();
 	
 			int newWidth = (int) Math.floor((width * newHeight) / height);
 	
 			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 			    newWidth, newHeight);
 			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.addRule(RelativeLayout.BELOW, R.id.headerText);
 			ad.setLayoutParams(params);
 			ad.setScaleType(ImageView.ScaleType.CENTER_CROP);
 		}
 		
 		/* End Ad setup */
 	}
 	
 }

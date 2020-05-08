 package com.example.ad_creation;
 
 import com.example.maple_android.AdCreationManager;
 import com.example.maple_android.MapleApplication;
 import com.example.maple_android.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 
 /**
  * This activity allows the user to adjust gamma and 
  * brightness of the ad.
  *
  */
 public class ColorAdjustmentActivity extends Activity {
 	/* Global app */
 	private MapleApplication mApp;
 	private AdCreationManager mAdCreationManager;
 
 	private Bitmap mOriginalAd; // the bitmap that we are starting with 
 	private Bitmap mAdjustedAd; // any changes to the original are stored here
 	private ImageView mAdView; // mAdjustedAd is displayed to the user through this view
 	
 	private SeekBar mGammaSeek; 
 	private SeekBar mBrightnessSeek;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_color_adjustment);
 
 		// Init app
 		mApp = (MapleApplication) this.getApplication();
 		mAdCreationManager = mApp.getAdCreationManager();
 
 		// get most recent ad of stack
 		// initialize adjusted ad to the original
 		mOriginalAd = mAdCreationManager.getCurrentBitmap();
 		mAdjustedAd = Bitmap.createBitmap(mOriginalAd);
 		
 		mAdView = (ImageView) findViewById(R.id.colorAdjustPhoto);
 		mAdView.setImageBitmap(mOriginalAd);
 
 		// set up gamma slider callback
 		mGammaSeek = ((SeekBar) this.findViewById(R.id.gammaSeekBar));
 		mGammaSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						double progress = seekBar.getProgress() / 10.0;
 						doGamma(progress);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 
 					}
 				});
 
 		// set up brightness slider callback
 		mBrightnessSeek = ((SeekBar) this.findViewById(R.id.brightnessSeekBar));
 		mBrightnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 					@Override
 					public void onStopTrackingTouch(SeekBar seekBar) {
 						int progress = seekBar.getProgress() - 255;
 						doBrightness(progress);
 					}
 
 					@Override
 					public void onStartTrackingTouch(SeekBar seekBar) {
 						// TODO Auto-generated method stub
 
 					}
 
 					@Override
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean fromUser) {
 
 					}
 				});
 
 	}
 
 	/**
 	 * Updates the current ad with the given gamma value
 	 */
 	private void doBrightness(int value) {
 		// image size
 		int width = mAdjustedAd.getWidth();
 		int height = mAdjustedAd.getHeight();
 		// create output bitmap
 		Bitmap bmOut = Bitmap.createBitmap(width, height,
 				mAdjustedAd.getConfig());
 		// color information
 		int A, R, G, B;
 		int pixel;
 
 		// scan through all pixels
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y) {
 				// get pixel color
 				pixel = mAdjustedAd.getPixel(x, y);
 				A = Color.alpha(pixel);
 				R = Color.red(pixel);
 				G = Color.green(pixel);
 				B = Color.blue(pixel);
 
 				// increase/decrease each channel
 				R += value;
 				if (R > 255) {
 					R = 255;
 				} else if (R < 0) {
 					R = 0;
 				}
 
 				G += value;
 				if (G > 255) {
 					G = 255;
 				} else if (G < 0) {
 					G = 0;
 				}
 
 				B += value;
 				if (B > 255) {
 					B = 255;
 				} else if (B < 0) {
 					B = 0;
 				}
 
 				// apply new pixel color to output bitmap
 				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
 			}
 		}
 
 		// update image
 		mAdjustedAd = bmOut;
 		mAdView.setImageBitmap(mAdjustedAd);
 	}
 
 	/**
 	 * Updates the current ad with the given gamma value
 	 */
 	private void doGamma(double level) {
 		// create output image
 		Bitmap bmOut = Bitmap.createBitmap(mOriginalAd.getWidth(),
 				mOriginalAd.getHeight(), mOriginalAd.getConfig());
 		// get image size
 		int width = mOriginalAd.getWidth();
 		int height = mOriginalAd.getHeight();
 		// color information
 		int A, R, G, B;
 		int pixel;
 		// constant value curve
 		final int MAX_SIZE = 256;
 		final double MAX_VALUE_DBL = 255.0;
 		final int MAX_VALUE_INT = 255;
 		final double REVERSE = 1.0;
 
 		// gamma arrays
 		int[] gammaR = new int[MAX_SIZE];
 		int[] gammaG = new int[MAX_SIZE];
 		int[] gammaB = new int[MAX_SIZE];
 
 		// setting values for every gamma channels
 		for (int i = 0; i < MAX_SIZE; ++i) {
 			gammaR[i] = (int) Math.min(
 					MAX_VALUE_INT,
 					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE
 							/ level)) + 0.5));
 			gammaG[i] = (int) Math.min(
 					MAX_VALUE_INT,
 					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE
 							/ level)) + 0.5));
 			gammaB[i] = (int) Math.min(
 					MAX_VALUE_INT,
 					(int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE
 							/ level)) + 0.5));
 		}
 
 		// apply gamma table
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y) {
 				// get pixel color
 				pixel = mOriginalAd.getPixel(x, y);
 				A = Color.alpha(pixel);
 				// look up gamma
 				R = gammaR[Color.red(pixel)];
 				G = gammaG[Color.green(pixel)];
 				B = gammaB[Color.blue(pixel)];
 				// set new color to output bitmap
 				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
 			}
 		}
 		
 		// reset brightness slider to middle
 		mBrightnessSeek.setProgress(255);
 
 		// update image
 		mAdjustedAd = bmOut;
 		mAdView.setImageBitmap(mAdjustedAd);
 	}
 	
 	/**
 	 * Push the updated bitmap and continue to the next 
 	 * stage in the funnel
 	 * @param view
 	 */
 	public void nextStage(View view){
 		mAdCreationManager.pushBitmap(mAdjustedAd);
		mAdCreationManager.previousStage(this);
 	}
 	
 	/** 
 	 * Return to the previous stage without 
 	 * saving any changes
 	 * @param view
 	 */
 	public void prevStage(View view){
 		mAdCreationManager.previousStage(this);
 	}
 	
 	/**
 	 * Clear any changes made during this stage
 	 * @param view
 	 */
 	public void reset(View view){
 		// restore original ad and update ImageView
 		mAdjustedAd = Bitmap.createBitmap(mOriginalAd);		
 		mAdView = (ImageView) findViewById(R.id.colorAdjustPhoto);
 		mAdView.setImageBitmap(mOriginalAd); 
 		
 		// reset seekers to middle
 		mGammaSeek.setProgress(10);
 		mBrightnessSeek.setProgress(255);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.color_adjustment, menu);
 		return true;
 	}
 
 }

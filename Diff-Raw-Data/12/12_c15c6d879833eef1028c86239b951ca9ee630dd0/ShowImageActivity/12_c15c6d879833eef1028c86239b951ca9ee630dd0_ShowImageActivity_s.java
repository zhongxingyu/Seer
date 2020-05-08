 package com.friday_countdown.andriod;
 
 import android.R.color;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.webkit.WebView;
 import android.widget.RatingBar;
 import android.widget.RatingBar.OnRatingBarChangeListener;
 import android.widget.Toast;
 
 /**
 * Show random Friaday image after click to countdown widget
  * Check and download all missed content 
  * @author VMaximenko
  *
  */
 public class ShowImageActivity extends Activity {
 
 	private static final String TAG = "ShowImageActivity";
 	
 	private WebView mWebView;
 	private RatingBar mRatingbar;
 	private ImageStore mImageStore;
 	private ProgressDialog mProgressDialog;
 	
 	private SensorManager mSensorManager;
 	private ShakeEventListener mSensorListener;
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		Log.d(TAG, "Create sctivity");
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.show_image);
 
         mSensorListener = new ShakeEventListener();
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mSensorManager.registerListener(mSensorListener,
             mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
             SensorManager.SENSOR_DELAY_UI);
 
         mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
 // load next random image on shake device
 			public void onShake() {
 				Log.d(TAG, "Shake detected, load new image");
 
 				loadImage();
 				setWebViewOrientationScale();
 			}
         });
         
         mWebView = (WebView) findViewById(R.id.friday_image_view);
         mWebView.setBackgroundColor(color.black);
         
         loadImage();
     }
 	
 	private void loadImage() {
 		Log.d(TAG, "Load random Friday image");
 		
 		mImageStore = new ImageStore(this);
         try {
 // check already loaded images        	
 			if ( !mImageStore.checkIsContentExists() )
 				downloadMissingContent();
 			else {
 				mImageStore.loadRandomImage();
 				initializeUI();
 			}
 			
 		} catch (LocalizedException e) {
 			Toast.makeText( getApplicationContext(), 
 					e.getString( getApplicationContext() ),
 					Toast.LENGTH_LONG ).show();
 		}
 		
 	}
 
 // try to download not exists images
 	private void downloadMissingContent() {
 		mProgressDialog = new ProgressDialog(this);
 		
 		new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(R.string.missing_content_dialog_title)
 				.setMessage(R.string.missing_content_dialog_message)
 				.setPositiveButton(R.string.yes_button,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 // start task on download missed images, showing the progress	
 								new DownloadImagesTask().execute();
 							}
 
 						})
 				.setNegativeButton(R.string.no_button,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 // show default image					
 								mImageStore.loadDefaultImage();
 				            	initializeUI();
 							}
 						}).show();
 		
 	}
 
 	class DownloadImagesTask extends AsyncTask<Object, Integer, Boolean> {
 		private LocalizedException e;
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			
 			mProgressDialog.setTitle(R.string.missing_content_progress);
 			mProgressDialog.setIndeterminate(false);
 			mProgressDialog.setMax( mImageStore.missedImages.size() );
 			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mProgressDialog.show();
 		}
 		
         @Override
         protected Boolean doInBackground(Object... params) {
 			publishProgress(0);
 
 			int total = mImageStore.missedImages.size();
 			try {
 				int piece = 100 / total;
 				for (int i = 0; i < total; i++) {
 					mImageStore.downloadImage( mImageStore.missedImages.get(i) );
 					publishProgress(piece*(i + 1));
 				}
 			} catch (LocalizedException e) {
 				this.e = e;
 				return false;
 			}
 
 			return true;
         }
         
         @Override
         protected void onProgressUpdate(Integer... progress) {
              super.onProgressUpdate( progress[0] );
              
              mProgressDialog.setProgress( progress[0] );
         }
         
         @Override
         protected void onPostExecute(Boolean result) {
              super.onPostExecute(result);
              
              mProgressDialog.dismiss();
              
              if (result) {
             	 mImageStore.loadRandomImage();
             	 initializeUI();
              }
              else
             	 Toast.makeText(getApplicationContext(),
             			 e.getString( getApplicationContext() ), 
             			 Toast.LENGTH_LONG)
  						.show();
         }
     }
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		Log.i(TAG, "Configuration changed " + newConfig.orientation);
 		
 	    super.onConfigurationChanged(newConfig);
 
 	    setWebViewOrientationScale();
 	}	
 	
     private void initializeUI() {
     	Log.d(TAG, "Initialize app UI");
     	
     	if (mImageStore.fridayImage == null)
     		return;
     	
     	mWebView.getSettings().setSupportZoom(true);
     	mWebView.getSettings().setBuiltInZoomControls(true);
     	
     	mWebView.setScrollbarFadingEnabled(true);
     	mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
 
     	mWebView.setPadding(0, 0, 0, 0);
     	
     	mRatingbar = (RatingBar) findViewById(R.id.image_rating);
 
     	mRatingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
             public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
 
             	mImageStore.setRating(rating);
             	
                 Toast.makeText(ShowImageActivity.this, 
                 		R.string.image_rating_changed, 
                 		Toast.LENGTH_SHORT).show();
             }
         });        
 
     	setWebViewOrientationScale();
     }
   
     private void setWebViewOrientationScale() {
     	Log.d(TAG, "Scale picture depend device orientation");
     	
     	if (mImageStore.fridayImage == null)
     		return;
     	
     	Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
     	
     	int width = display.getWidth(); 
         int height = display.getHeight(); 
 
         Log.d(TAG, "Display view " + width + "x" + height);
 
         int picWidth = mImageStore.fridayImage.width;
         int picHeight = mImageStore.fridayImage.height;
         
         Log.d(TAG, "Picture dimensions " + picWidth + "x" + picHeight);
 
 // scale web view depend of rotate device        
         Double val = 1d;
 
 		if (picHeight > height)
			val = new Double(height) / new Double(picHeight);
         
     	val = val * 100d;
     	Log.i(TAG, "Scale to " + val);
     
     	mWebView.setInitialScale( val.intValue() );
 		mWebView.loadDataWithBaseURL("/", mImageStore.getHtml(), "text/html", "UTF-8", null);
 		mWebView.refreshDrawableState();
		
		mRatingbar.setRating(mImageStore.fridayImage.rating);
     }
 	
     @Override
     protected void onResume() {
       super.onResume();
 
       mSensorManager.registerListener(mSensorListener,
           mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
           SensorManager.SENSOR_DELAY_UI);
     }
 
     @Override
     protected void onStop() {
       mSensorManager.unregisterListener(mSensorListener);
       super.onStop();
     }
 }

 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.camera;
 
 import com.android.camera.gallery.Cancelable;
 import com.android.camera.gallery.IImage;
 import com.android.camera.gallery.IImageList;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.text.format.DateFormat;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ZoomButtonsController;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 /**
  * Activity of the Camera which used to see preview and take pictures.
  */
 public class Camera extends Activity implements View.OnClickListener,
         ShutterButton.OnShutterButtonListener, SurfaceHolder.Callback {
 
     private static final String TAG = "camera";
 
     private static final int CROP_MSG = 1;
     private static final int FIRST_TIME_INIT = 2;
     private static final int RESTART_PREVIEW = 3;
     private static final int CLEAR_SCREEN_DELAY = 4;
     private static final int STORE_IMAGE_DONE = 5;
 
     private static final int SCREEN_DELAY = 2 * 60 * 1000;
     private static final int FOCUS_BEEP_VOLUME = 100;
 
     public static final int MENU_SWITCH_TO_VIDEO = 0;
     public static final int MENU_SWITCH_TO_CAMERA = 1;
     public static final int MENU_FLASH_SETTING = 2;
     public static final int MENU_FLASH_AUTO = 3;
     public static final int MENU_FLASH_ON = 4;
     public static final int MENU_FLASH_OFF = 5;
     public static final int MENU_SETTINGS = 6;
     public static final int MENU_GALLERY_PHOTOS = 7;
     public static final int MENU_GALLERY_VIDEOS = 8;
     public static final int MENU_SAVE_SELECT_PHOTOS = 30;
     public static final int MENU_SAVE_NEW_PHOTO = 31;
     public static final int MENU_SAVE_GALLERY_PHOTO = 34;
     public static final int MENU_SAVE_GALLERY_VIDEO_PHOTO = 35;
     public static final int MENU_SAVE_CAMERA_DONE = 36;
     public static final int MENU_SAVE_CAMERA_VIDEO_DONE = 37;
 
     private android.hardware.Camera.Parameters mParameters;
     private int mZoomIndex = 0;  // The index of the current zoom value.
     private String[] mZoomValues;  // All possible zoom values.
 
     // The parameter strings to communicate with camera driver.
     public static final String PARM_ZOOM = "zoom";
     public static final String PARM_WHITE_BALANCE = "whitebalance";
     public static final String PARM_EFFECT = "effect";
     public static final String PARM_BRIGHTNESS = "exposure-offset";
     public static final String PARM_PICTURE_SIZE = "picture-size";
     public static final String PARM_JPEG_QUALITY = "jpeg-quality";
     public static final String PARM_ISO = "iso";
     public static final String PARM_ROTATION = "rotation";
     public static final String PARM_GPS_LATITUDE = "gps-latitude";
     public static final String PARM_GPS_LONGITUDE = "gps-longitude";
     public static final String PARM_GPS_ALTITUDE = "gps-altitude";
     public static final String PARM_GPS_TIMESTAMP = "gps-timestamp";
     public static final String SUPPORTED_ZOOM = "zoom-values";
     public static final String SUPPORTED_WHITE_BALANCE = "whitebalance-values";
     public static final String SUPPORTED_EFFECT = "effect-values";
     public static final String SUPPORTED_BRIGHTNESS = "exposure-offset-values";
     public static final String SUPPORTED_PICTURE_SIZE = "picture-size-values";
     public static final String SUPPORTED_ISO = "iso-values";
 
     private OrientationEventListener mOrientationListener;
     private int mLastOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
     private SharedPreferences mPreferences;
 
     private static final int IDLE = 1;
     private static final int SNAPSHOT_IN_PROGRESS = 2;
     private static final int SNAPSHOT_COMPLETED = 3;
 
     private int mStatus = IDLE;
     private static final String sTempCropFilename = "crop-temp";
 
     private android.hardware.Camera mCameraDevice;
     private VideoPreview mSurfaceView;
     private SurfaceHolder mSurfaceHolder = null;
     private ShutterButton mShutterButton;
     private FocusRectangle mFocusRectangle;
     private ImageView mGpsIndicator;
     private ToneGenerator mFocusToneGenerator;
     private ZoomButtonsController mZoomButtons;
 
     // mPostCaptureAlert, mLastPictureButton, mThumbController
     // are non-null only if isImageCaptureIntent() is true.
     private View mPostCaptureAlert;
     private ImageView mLastPictureButton;
     private ThumbnailController mThumbController;
 
     private int mOriginalViewFinderWidth, mOriginalViewFinderHeight;
     private int mViewFinderWidth, mViewFinderHeight;
 
     private ImageCapture mImageCapture = null;
 
     private boolean mPreviewing;
     private boolean mPausing;
     private boolean mFirstTimeInitialized;
     private boolean mPendingFirstTimeInit;
     private boolean mKeepAndRestartPreview;
     private boolean mIsImageCaptureIntent;
     private boolean mRecordLocation;
 
     private static final int FOCUS_NOT_STARTED = 0;
     private static final int FOCUSING = 1;
     private static final int FOCUSING_SNAP_ON_FINISH = 2;
     private static final int FOCUS_SUCCESS = 3;
     private static final int FOCUS_FAIL = 4;
     private int mFocusState = FOCUS_NOT_STARTED;
 
     private ContentResolver mContentResolver;
     private boolean mDidRegister = false;
 
     private final ArrayList<MenuItem> mGalleryItems = new ArrayList<MenuItem>();
 
     private LocationManager mLocationManager = null;
 
     // Use OneShotPreviewCallback to measure the time between
     // JpegPictureCallback and preview.
     private final OneShotPreviewCallback mOneShotPreviewCallback =
             new OneShotPreviewCallback();
     private final ShutterCallback mShutterCallback = new ShutterCallback();
     private final RawPictureCallback mRawPictureCallback =
             new RawPictureCallback();
     private final AutoFocusCallback mAutoFocusCallback =
             new AutoFocusCallback();
     private long mFocusStartTime;
     private long mFocusCallbackTime;
     private long mCaptureStartTime;
     private long mShutterCallbackTime;
     private long mRawPictureCallbackTime;
     private long mJpegPictureCallbackTime;
     private int mPicturesRemaining;
 
     //Add the camera latency time
     public static long mAutoFocusTime;
     public static long mShutterLag;
     public static long mShutterAndRawPictureCallbackTime;
     public static long mJpegPictureCallbackTimeLag;
     public static long mRawPictureAndJpegPictureCallbackTime;
 
     // Focus mode. Options are pref_camera_focusmode_entryvalues.
     private String mFocusMode;
 
     private Thread mStoreImageThread = null;
     private final Handler mHandler = new MainHandler();
 
     private interface Capturer {
         Uri getLastCaptureUri();
         void onSnap();
         void dismissFreezeFrame();
     }
 
     /**
      * This Handler is used to post message back onto the main thread of the
      * application
      */
     private class MainHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case RESTART_PREVIEW: {
                     if (mStatus == SNAPSHOT_IN_PROGRESS) {
                         // We are still in the processing of taking the picture,
                         // wait. This is strange.  Why are we polling?
                         // TODO: remove polling
                         mHandler.sendEmptyMessageDelayed(RESTART_PREVIEW, 100);
                     } else if (mStatus == SNAPSHOT_COMPLETED){
                         mImageCapture.dismissFreezeFrame();
                         hidePostCaptureAlert();
                     }
                     break;
                 }
 
                 case STORE_IMAGE_DONE: {
                     if (!mIsImageCaptureIntent) {
                         setLastPictureThumb((byte[])msg.obj, mImageCapture.getLastCaptureUri());
                         if (!mThumbController.isUriValid()) {
                             updateLastImage();
                         }
                         mThumbController.updateDisplayIfNeeded();
                     } else {
                         showPostCaptureAlert();
                     }
                     mStoreImageThread = null;
                     break;
                 }
 
                 case CLEAR_SCREEN_DELAY: {
                     getWindow().clearFlags(
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                     break;
                 }
 
                 case FIRST_TIME_INIT: {
                     initializeFirstTime();
                     break;
                 }
             }
         }
     }
 
     // This method will be called after surfaceChanged. Snapshots can only be
     // taken after this is called. It should be called once only. We could have
     // done these things in onCreate() but we want to make preview screen appear
     // as soon as possible.
     void initializeFirstTime() {
         if (mFirstTimeInitialized) return;
 
         // Create orientation listenter. This should be done first because it
         // takes some time to get first orientation.
         mOrientationListener =
                 new OrientationEventListener(Camera.this) {
             @Override
             public void onOrientationChanged(int orientation) {
                 // We keep the last known orientation. So if the user
                 // first orient the camera then point the camera to
                 // floor/sky, we still have the correct orientation.
                 if (orientation != ORIENTATION_UNKNOWN) {
                     mLastOrientation = orientation;
                 }
             }
         };
         mOrientationListener.enable();
 
         // Initialize location sevice.
         mLocationManager = (LocationManager)
                 getSystemService(Context.LOCATION_SERVICE);
         readPreference();
         if (mRecordLocation) startReceivingLocationUpdates();
 
         // Initialize last picture button.
         mContentResolver = getContentResolver();
         if (!mIsImageCaptureIntent)  {
             findViewById(R.id.video_button).setOnClickListener(this);
             mLastPictureButton = (ImageView) findViewById(R.id.review_button);
             mLastPictureButton.setOnClickListener(this);
             mThumbController = new ThumbnailController(
                     mLastPictureButton, mContentResolver);
             mThumbController.loadData(ImageManager.getLastImageThumbPath());
             // Update last image thumbnail.
             if (!mThumbController.isUriValid()) {
                 updateLastImage();
             }
             mThumbController.updateDisplayIfNeeded();
         } else {
             findViewById(R.id.review_button).setVisibility(View.INVISIBLE);
             findViewById(R.id.video_button).setVisibility(View.INVISIBLE);
             ViewGroup cameraView = (ViewGroup) findViewById(R.id.camera);
             getLayoutInflater().inflate(
                     R.layout.post_picture_panel, cameraView);
             mPostCaptureAlert = findViewById(R.id.post_picture_panel);
         }
 
         findViewById(R.id.photo_indicator).setVisibility(View.VISIBLE);
         // Initialize shutter button.
         mShutterButton = (ShutterButton) findViewById(R.id.camera_button);
         mShutterButton.setOnShutterButtonListener(this);
         mShutterButton.setVisibility(View.VISIBLE);
 
         mFocusRectangle = (FocusRectangle) findViewById(R.id.focus_rectangle);
         updateFocusIndicator();
 
         // Initialize GPS indicator.
         mGpsIndicator = (ImageView) findViewById(R.id.gps_indicator);
         mGpsIndicator.setImageResource(R.drawable.ic_gps_active_camera);
 
         ImageManager.ensureOSXCompatibleFolder();
 
         calculatePicturesRemaining();
 
         installIntentFilter();
 
         initializeFocusTone();
 
         initializeZoom();
 
         mFirstTimeInitialized = true;
     }
 
     // If the activity is paused and resumed, this method will be called in
     // onResume.
     void initializeSecondTime() {
         // Start orientation listener as soon as possible because it takes
         // some time to get first orientation.
         mOrientationListener.enable();
 
         // Start location update if needed.
         readPreference();
         if (mRecordLocation) startReceivingLocationUpdates();
 
         installIntentFilter();
 
         initializeFocusTone();
     }
 
     private void initializeZoom() {
         String zoomValuesStr = mParameters.get(SUPPORTED_ZOOM);
         if (zoomValuesStr == null) return;
 
         mZoomValues = getZoomValues(zoomValuesStr);
         if (mZoomValues == null) return;
 
         mZoomButtons = new ZoomButtonsController(mSurfaceView);
         mZoomButtons.setAutoDismissed(true);
         mZoomButtons.setOnZoomListener(
                 new ZoomButtonsController.OnZoomListener() {
             public void onVisibilityChanged(boolean visible) {
                 if (visible) {
                     updateZoomButtonsEnabled();
                 }
             }
 
             public void onZoom(boolean zoomIn) {
                 if (zoomIn) {
                     zoomIn();
                 } else {
                     zoomOut();
                 }
                 updateZoomButtonsEnabled();
             }
         });
     }
 
     private void zoomIn() {
         if (mZoomIndex < mZoomValues.length - 1) {
             mZoomIndex++;
             mParameters.set(PARM_ZOOM, mZoomValues[mZoomIndex]);
             mCameraDevice.setParameters(mParameters);
         }
     }
 
     private void zoomOut() {
         if (mZoomIndex > 0) {
             mZoomIndex--;
             mParameters.set(PARM_ZOOM, mZoomValues[mZoomIndex]);
             mCameraDevice.setParameters(mParameters);
         }
     }
 
     private void updateZoomButtonsEnabled() {
         mZoomButtons.setZoomInEnabled(mZoomIndex < mZoomValues.length - 1);
         mZoomButtons.setZoomOutEnabled(mZoomIndex > 0);
     }
 
     private String[] getZoomValues(String zoomValuesStr) {
         ArrayList<String> list = new ArrayList<String>();
         String[] zoomValues = null;
         StringTokenizer tokenizer = new StringTokenizer(zoomValuesStr, ",");
 
         while (tokenizer.hasMoreElements()) {
             list.add(tokenizer.nextToken());
         }
         if (list.size() > 0) {
             zoomValues = list.toArray(new String[list.size()]);
         }
         return zoomValues;
     }
 
 
     LocationListener [] mLocationListeners = new LocationListener[] {
             new LocationListener(LocationManager.GPS_PROVIDER),
             new LocationListener(LocationManager.NETWORK_PROVIDER)
     };
 
 
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                 // SD card available
                 updateStorageHint(calculatePicturesRemaining());
             } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
                     action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                 // SD card unavailable
                 mPicturesRemaining = MenuHelper.NO_STORAGE_ERROR;
                 updateStorageHint(mPicturesRemaining);
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                 Toast.makeText(Camera.this,
                         getResources().getString(R.string.wait), 5000);
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                 updateStorageHint();
             }
         }
     };
 
     private class LocationListener
             implements android.location.LocationListener {
         Location mLastLocation;
         boolean mValid = false;
         String mProvider;
 
         public LocationListener(String provider) {
             mProvider = provider;
             mLastLocation = new Location(mProvider);
         }
 
         public void onLocationChanged(Location newLocation) {
             if (newLocation.getLatitude() == 0.0
                     && newLocation.getLongitude() == 0.0) {
                 // Hack to filter out 0.0,0.0 locations
                 return;
             }
             // If GPS is available before start camera, we won't get status
             // update so update GPS indicator when we receive data.
             if (mRecordLocation
                     && LocationManager.GPS_PROVIDER.equals(mProvider)) {
                 mGpsIndicator.setVisibility(View.VISIBLE);
             }
             mLastLocation.set(newLocation);
             mValid = true;
         }
 
         public void onProviderEnabled(String provider) {
         }
 
         public void onProviderDisabled(String provider) {
             mValid = false;
         }
 
         public void onStatusChanged(
                 String provider, int status, Bundle extras) {
             switch(status) {
                 case LocationProvider.OUT_OF_SERVICE:
                 case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                     mValid = false;
                     if (mRecordLocation &&
                             LocationManager.GPS_PROVIDER.equals(provider)) {
                         mGpsIndicator.setVisibility(View.INVISIBLE);
                     }
                     break;
                 }
             }
         }
 
         public Location current() {
             return mValid ? mLastLocation : null;
         }
     }
 
     private boolean mImageSavingItem = false;
 
     private final class OneShotPreviewCallback
             implements android.hardware.Camera.PreviewCallback {
         public void onPreviewFrame(byte[] data,
                                    android.hardware.Camera camera) {
             long now = System.currentTimeMillis();
             if (mJpegPictureCallbackTime != 0) {
                 mJpegPictureCallbackTimeLag = now - mJpegPictureCallbackTime;
                 Log.v(TAG, "mJpegPictureCallbackTimeLag = "
                         + mJpegPictureCallbackTimeLag + "ms");
                 mJpegPictureCallbackTime = 0;
             }
         }
     }
 
     private final class ShutterCallback
             implements android.hardware.Camera.ShutterCallback {
         public void onShutter() {
             mShutterCallbackTime = System.currentTimeMillis();
             mShutterLag = mShutterCallbackTime - mCaptureStartTime;
             Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
             clearFocusState();
             // We are going to change the size of surface view and show captured
             // image. Set it to invisible now and set it back to visible in
             // surfaceChanged() so that users won't see the image is resized on
             // the screen.
             mSurfaceView.setVisibility(View.INVISIBLE);
             // Resize the SurfaceView to the aspect-ratio of the still image
             // and so that we can see the full image that was taken.
             Size pictureSize = mParameters.getPictureSize();
             mSurfaceView.setAspectRatio(pictureSize.width, pictureSize.height);
         }
     }
 
     private final class RawPictureCallback implements PictureCallback {
         public void onPictureTaken(
                 byte [] rawData, android.hardware.Camera camera) {
             mRawPictureCallbackTime = System.currentTimeMillis();
             mShutterAndRawPictureCallbackTime =
                 mRawPictureCallbackTime - mShutterCallbackTime;
             Log.v(TAG, "mShutterAndRawPictureCallbackTime = "
                     + mShutterAndRawPictureCallbackTime + "ms");
         }
     }
 
     private final class JpegPictureCallback implements PictureCallback {
         Location mLocation;
 
         public JpegPictureCallback(Location loc) {
             mLocation = loc;
         }
 
         public void onPictureTaken(
                 final byte [] jpegData, final android.hardware.Camera camera) {
             if (mPausing) {
                 return;
             }
 
             mJpegPictureCallbackTime = System.currentTimeMillis();
             mRawPictureAndJpegPictureCallbackTime =
                 mJpegPictureCallbackTime - mRawPictureCallbackTime;
             Log.v(TAG, "mRawPictureAndJpegPictureCallbackTime = "
                     + mRawPictureAndJpegPictureCallbackTime +"ms");
             if (jpegData != null) {
                 mStoreImageThread = new Thread() {
                      public void run() {
                          mImageCapture.storeImage(jpegData, camera, mLocation);
                      }
                 };
                 mStoreImageThread.start();
             }
             mStatus = SNAPSHOT_COMPLETED;
 
             if (mKeepAndRestartPreview) {
                 long delay = 1500 - (
                         System.currentTimeMillis() - mRawPictureCallbackTime);
                 mHandler.sendEmptyMessageDelayed(
                         RESTART_PREVIEW, Math.max(delay, 0));
             }
         }
     }
 
     private final class AutoFocusCallback
             implements android.hardware.Camera.AutoFocusCallback {
         public void onAutoFocus(
                 boolean focused, android.hardware.Camera camera) {
             mFocusCallbackTime = System.currentTimeMillis();
             mAutoFocusTime = mFocusCallbackTime - mFocusStartTime;
             Log.v(TAG, "mAutoFocusTime = " + mAutoFocusTime + "ms");
             if (mFocusState == FOCUSING_SNAP_ON_FINISH
                     && mImageCapture != null) {
                 // Take the picture no matter focus succeeds or fails. No need
                 // to play the AF sound if we're about to play the shutter
                 // sound.
                 if (focused) {
                     mFocusState = FOCUS_SUCCESS;
                 } else {
                     mFocusState = FOCUS_FAIL;
                 }
                 mImageCapture.onSnap();
             } else if (mFocusState == FOCUSING) {
                 // User is half-pressing the focus key. Play the focus tone.
                 // Do not take the picture now.
                 ToneGenerator tg = mFocusToneGenerator;
                 if (tg != null) {
                     tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
                 }
                 if (focused) {
                     mFocusState = FOCUS_SUCCESS;
                 } else {
                     mFocusState = FOCUS_FAIL;
                 }
             } else if (mFocusState == FOCUS_NOT_STARTED) {
                 // User has released the focus key before focus completes.
                 // Do nothing.
             }
             updateFocusIndicator();
         }
     }
 
     private class ImageCapture implements Capturer {
 
         private boolean mCancel = false;
 
         private Uri mLastContentUri;
         private Cancelable<Void> mAddImageCancelable;
 
         Bitmap mCaptureOnlyBitmap;
 
         public void dismissFreezeFrame() {
             if (mStatus == SNAPSHOT_IN_PROGRESS) {
                 // If we are still in the process of taking a picture,
                 // then just post a message.
                 mHandler.sendEmptyMessage(RESTART_PREVIEW);
             } else {
                 restartPreview();
             }
         }
 
         private void storeImage(byte[] data, Location loc) {
             try {
                 long dateTaken = System.currentTimeMillis();
                 String name = createName(dateTaken) + ".jpg";
                 mLastContentUri = ImageManager.addImage(
                         mContentResolver,
                         name,
                         dateTaken,
                         loc, // location for the database goes here
                         0, // the dsp will use the right orientation so
                            // don't "double set it"
                         ImageManager.CAMERA_IMAGE_BUCKET_NAME,
                         name);
                 if (mLastContentUri == null) {
                     // this means we got an error
                     mCancel = true;
                 }
                 if (!mCancel) {
                     mAddImageCancelable = ImageManager.storeImage(
                             mLastContentUri, mContentResolver,
                             0, null, data);
                     mAddImageCancelable.get();
                     mAddImageCancelable = null;
                     ImageManager.setImageSize(mContentResolver, mLastContentUri,
                             new File(ImageManager.CAMERA_IMAGE_BUCKET_NAME,
                             name).length());
                 }
             } catch (Exception ex) {
                 Log.e(TAG, "Exception while compressing image.", ex);
             }
         }
 
         public void storeImage(
                 final byte[] data, android.hardware.Camera camera, Location loc) {
             boolean captureOnly = mIsImageCaptureIntent;
             Message msg = mHandler.obtainMessage(STORE_IMAGE_DONE);
             if (!captureOnly) {
                 storeImage(data, loc);
                 sendBroadcast(new Intent(
                         "com.android.camera.NEW_PICTURE", mLastContentUri));
                 msg.obj = data;
             } else {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 4;
 
                 mCaptureOnlyBitmap = BitmapFactory.decodeByteArray(
                         data, 0, data.length, options);
 
                 cancelAutomaticPreviewRestart();
             }
             mHandler.sendMessage(msg);
         }
 
         /**
          * Initiate the capture of an image.
          */
         public void initiate() {
             if (mCameraDevice == null) {
                 return;
             }
 
             mCancel = false;
 
             capture();
         }
 
         public Uri getLastCaptureUri() {
             return mLastContentUri;
         }
 
         public Bitmap getLastBitmap() {
             return mCaptureOnlyBitmap;
         }
 
         private void capture() {
             mPreviewing = false;
             mCaptureOnlyBitmap = null;
 
             int orientation = mLastOrientation;
             if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                 orientation += 90;
             }
             orientation = ImageManager.roundOrientation(orientation);
             Log.v(TAG, "mLastOrientation = " + mLastOrientation
                     + ", orientation = " + orientation);
 
             mParameters.set(PARM_ROTATION, orientation);
 
             Location loc = mRecordLocation ? getCurrentLocation() : null;
 
             mParameters.remove(PARM_GPS_LATITUDE);
             mParameters.remove(PARM_GPS_LONGITUDE);
             mParameters.remove(PARM_GPS_ALTITUDE);
             mParameters.remove(PARM_GPS_TIMESTAMP);
 
             if (loc != null) {
                 double lat = loc.getLatitude();
                 double lon = loc.getLongitude();
                 boolean hasLatLon = (lat != 0.0d) || (lon != 0.0d);
 
                 if (hasLatLon) {
                     String latString = String.valueOf(lat);
                     String lonString = String.valueOf(lon);
                     mParameters.set(PARM_GPS_LATITUDE,  latString);
                     mParameters.set(PARM_GPS_LONGITUDE, lonString);
                     if (loc.hasAltitude()) {
                         mParameters.set(PARM_GPS_ALTITUDE,
                                         String.valueOf(loc.getAltitude()));
                     } else {
                         // for NETWORK_PROVIDER location provider, we may have
                         // no altitude information, but the driver needs it, so
                         // we fake one.
                         mParameters.set(PARM_GPS_ALTITUDE,  "0");
                     }
                     if (loc.getTime() != 0) {
                         // Location.getTime() is UTC in milliseconds.
                         // gps-timestamp is UTC in seconds.
                         long utcTimeSeconds = loc.getTime() / 1000;
                         mParameters.set(PARM_GPS_TIMESTAMP,
                                         String.valueOf(utcTimeSeconds));
                     }
                 } else {
                     loc = null;
                 }
             }
 
             mCameraDevice.setParameters(mParameters);
 
             mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
                     new JpegPictureCallback(loc));
         }
 
         public void onSnap() {
             if (mPausing) {
                 return;
             }
             mCaptureStartTime = System.currentTimeMillis();
 
             // If we are already in the middle of taking a snapshot then we
             // should just save
             // the image after we have returned from the camera service.
             if (mStatus == SNAPSHOT_IN_PROGRESS
                     || mStatus == SNAPSHOT_COMPLETED) {
                 mKeepAndRestartPreview = true;
                 mHandler.sendEmptyMessage(RESTART_PREVIEW);
                 return;
             }
 
             // Don't check the filesystem here, we can't afford the latency.
             // Instead, check the cached value which was calculated when the
             // preview was restarted.
             if (mPicturesRemaining < 1) {
                 updateStorageHint(mPicturesRemaining);
                 return;
             }
 
             mStatus = SNAPSHOT_IN_PROGRESS;
 
             mKeepAndRestartPreview = true;
 
             mImageCapture.initiate();
         }
 
         private void clearLastBitmap() {
             if (mCaptureOnlyBitmap != null) {
                 mCaptureOnlyBitmap.recycle();
                 mCaptureOnlyBitmap = null;
             }
         }
     }
 
     private void setLastPictureThumb(byte[] data, Uri uri) {
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inSampleSize = 16;
         Bitmap lastPictureThumb =
                 BitmapFactory.decodeByteArray(data, 0, data.length, options);
         mThumbController.setData(uri, lastPictureThumb);
     }
 
     private static String createName(long dateTaken) {
         return DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString();
     }
 
     public static Matrix getDisplayMatrix(Bitmap b, ImageView v) {
         Matrix m = new Matrix();
         float bw = b.getWidth();
         float bh = b.getHeight();
         float vw = v.getWidth();
         float vh = v.getHeight();
         float scale, x, y;
         if (bw * vh > vw * bh) {
             scale = vh / bh;
             x = (vw - scale * bw) * 0.5F;
             y = 0;
         } else {
             scale = vw / bw;
             x = 0;
             y = (vh - scale * bh) * 0.5F;
         }
         m.setScale(scale, scale, 0.5F, 0.5F);
         m.postTranslate(x, y);
         return m;
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         /*
          * To reduce startup time, we open camera device in another thread.
          * We make sure the camera is opened at the end of onCreate.
          */
         Thread openCameraThread = new Thread(new Runnable() {
             public void run() {
                 mCameraDevice = CameraHolder.instance().open();
             }
         });
         openCameraThread.start();
 
         mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         Window win = getWindow();
         win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         setContentView(R.layout.camera);
 
         mSurfaceView = (VideoPreview) findViewById(R.id.camera_preview);
 
         // don't set mSurfaceHolder here. We have it set ONLY within
         // surfaceCreated / surfaceDestroyed, other parts of the code
         // assume that when it is set, the surface is also set.
         SurfaceHolder holder = mSurfaceView.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         mIsImageCaptureIntent = isImageCaptureIntent();
         getLayoutInflater().inflate(
                 R.layout.button_bar, (ViewGroup) findViewById(R.id.camera));
 
         // Make sure the services are loaded.
         try {
             openCameraThread.join();
         } catch (InterruptedException ex) {
             // ignore
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         Thread t = new Thread(new Runnable() {
             public void run() {
                 final boolean storageOK = calculatePicturesRemaining() > 0;
                 if (!storageOK) {
                     mHandler.post(new Runnable() {
                         public void run() {
                             updateStorageHint(mPicturesRemaining);
                         }
                     });
                 }
             }
         });
         t.start();
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.video_button:
                 MenuHelper.gotoVideoMode(this);
                 break;
             case R.id.review_button:
                 if (mStatus == IDLE && mFocusState == FOCUS_NOT_STARTED) {
                     // Make sure image storing has completed before viewing 
                     // last image.
                     waitForStoreImageThread();
                     viewLastImage();
                 }
                 break;
             case R.id.attach:
                 doAttach();
                 break;
             case R.id.cancel:
                 doCancel();
         }
     }
 
     private void doAttach() {
         if (mPausing) {
             return;
         }
         Bitmap bitmap = mImageCapture.getLastBitmap();
 
         String cropValue = null;
         Uri saveUri = null;
 
         Bundle myExtras = getIntent().getExtras();
         if (myExtras != null) {
             saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
             cropValue = myExtras.getString("crop");
         }
 
 
         if (cropValue == null) {
             // First handle the no crop case -- just return the value.  If the
             // caller specifies a "save uri" then write the data to it's
             // stream. Otherwise, pass back a scaled down version of the bitmap
             // directly in the extras.
             if (saveUri != null) {
                 OutputStream outputStream = null;
                 try {
                     outputStream = mContentResolver.openOutputStream(saveUri);
                     bitmap.compress(Bitmap.CompressFormat.JPEG, 75,
                             outputStream);
                     outputStream.close();
 
                     setResult(RESULT_OK);
                     finish();
                 } catch (IOException ex) {
                     // ignore exception
                 } finally {
                     if (outputStream != null) {
                         try {
                             outputStream.close();
                         } catch (IOException ex) {
                             // ignore exception
                         }
                     }
                 }
             } else {
                 float scale = .5F;
                 Matrix m = new Matrix();
                 m.setScale(scale, scale);
 
                 bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                         bitmap.getWidth(),
                         bitmap.getHeight(),
                         m, true);
 
                 setResult(RESULT_OK,
                         new Intent("inline-data").putExtra("data", bitmap));
                 finish();
             }
         } else {
             // Save the image to a temp file and invoke the cropper
             Uri tempUri = null;
             FileOutputStream tempStream = null;
             try {
                 File path = getFileStreamPath(sTempCropFilename);
                 path.delete();
                 tempStream = openFileOutput(sTempCropFilename, 0);
                 bitmap.compress(Bitmap.CompressFormat.JPEG, 75, tempStream);
                 tempStream.close();
                 tempUri = Uri.fromFile(path);
             } catch (FileNotFoundException ex) {
                 setResult(Activity.RESULT_CANCELED);
                 finish();
                 return;
             } catch (IOException ex) {
                 setResult(Activity.RESULT_CANCELED);
                 finish();
                 return;
             } finally {
                 if (tempStream != null) {
                     try {
                         tempStream.close();
                     } catch (IOException ex) {
                         // ignore exception
                     }
                 }
             }
 
             Bundle newExtras = new Bundle();
             if (cropValue.equals("circle")) {
                 newExtras.putString("circleCrop", "true");
             }
             if (saveUri != null) {
                 newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, saveUri);
             } else {
                 newExtras.putBoolean("return-data", true);
             }
 
             Intent cropIntent = new Intent();
             cropIntent.setClass(Camera.this, CropImage.class);
             cropIntent.setData(tempUri);
             cropIntent.putExtras(newExtras);
 
             startActivityForResult(cropIntent, CROP_MSG);
         }
     }
 
     private void doCancel() {
         setResult(RESULT_CANCELED, new Intent());
         finish();
     }
 
     public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
         if (mPausing) {
             return;
         }
         switch (button.getId()) {
             case R.id.camera_button:
                 if (mStoreImageThread == null) {
                     doFocus(pressed);
                 } else {
                     Toast.makeText(Camera.this,
                             getResources().getString(R.string.wait), 
                             Toast.LENGTH_SHORT);
                 }
                 break;
         }
     }
 
     public void onShutterButtonClick(ShutterButton button) {
         if (mPausing) {
             return;
         }
         switch (button.getId()) {
             case R.id.camera_button:
                 if (mStoreImageThread == null) {
                     doSnap();
                 } else {
                     Toast.makeText(Camera.this,
                             getResources().getString(R.string.wait), 
                             Toast.LENGTH_SHORT);
                 }
                 break;
         }
     }
 
     private void updateStorageHint() {
       updateStorageHint(MenuHelper.calculatePicturesRemaining());
     }
 
     private OnScreenHint mStorageHint;
 
     private void updateStorageHint(int remaining) {
         String noStorageText = null;
 
         if (remaining == MenuHelper.NO_STORAGE_ERROR) {
             String state = Environment.getExternalStorageState();
             if (state == Environment.MEDIA_CHECKING) {
                 noStorageText = getString(R.string.preparing_sd);
             } else {
                 noStorageText = getString(R.string.no_storage);
             }
         } else if (remaining < 1) {
             noStorageText = getString(R.string.not_enough_space);
         }
 
         if (noStorageText != null) {
             if (mStorageHint == null) {
                 mStorageHint = OnScreenHint.makeText(this, noStorageText);
             } else {
                 mStorageHint.setText(noStorageText);
             }
             mStorageHint.show();
         } else if (mStorageHint != null) {
             mStorageHint.cancel();
             mStorageHint = null;
         }
     }
 
     void installIntentFilter() {
         // install an intent filter to receive SD card related events.
         IntentFilter intentFilter =
                 new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
         intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
         intentFilter.addDataScheme("file");
         registerReceiver(mReceiver, intentFilter);
         mDidRegister = true;
     }
 
     void initializeFocusTone() {
         // Initialize focus tone generator.
         try {
             mFocusToneGenerator = new ToneGenerator(
                     AudioManager.STREAM_SYSTEM, FOCUS_BEEP_VOLUME);
         } catch (RuntimeException e) {
             Log.w(TAG, "Exception caught while creating local tone generator: "
                     + e);
             mFocusToneGenerator = null;
         }
     }
 
     void readPreference() {
         mRecordLocation = mPreferences.getBoolean(
                 "pref_camera_recordlocation_key", false);
         mFocusMode = mPreferences.getString(
                 CameraSettings.KEY_FOCUS_MODE,
                 getString(R.string.pref_camera_focusmode_default));
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         mPausing = false;
         mJpegPictureCallbackTime = 0;
         mImageCapture = new ImageCapture();
 
         // If first time initialization is pending, put it in the message queue.
         if (mPendingFirstTimeInit) {
             mHandler.sendEmptyMessage(FIRST_TIME_INIT);
             mPendingFirstTimeInit = false;
         } else if (mFirstTimeInitialized) {
             // If first time initilization is done and the activity is
             // paused and resumed, we have to start the preview and do some
             // initialization.
             mSurfaceView.setAspectRatio(VideoPreview.DONT_CARE);
            setViewFinder(mOriginalViewFinderWidth, mOriginalViewFinderHeight);
             mStatus = IDLE;
 
             initializeSecondTime();
         }
 
         mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
     }
 
     private static ImageManager.DataLocation dataLocation() {
         return ImageManager.DataLocation.EXTERNAL;
     }
 
     private void waitForStoreImageThread() {
         if (mStoreImageThread != null) {
             try {
                 mStoreImageThread.join();
             } catch (InterruptedException ex) {
                 // Ignore this exception.
                 Log.e(TAG, "", ex);
             } finally {
                 mStoreImageThread = null;
             }
         }
     }
 
     @Override
     protected void onPause() {
         mPausing = true;
         stopPreview();
         // Close the camera now because other activities may need to use it.
         closeCamera();
 
         waitForStoreImageThread();
 
         if (mFirstTimeInitialized) {
             mOrientationListener.disable();
             mGpsIndicator.setVisibility(View.INVISIBLE);
             if (!mIsImageCaptureIntent) {
                 mThumbController.storeData(
                         ImageManager.getLastImageThumbPath());
             }
             hidePostCaptureAlert();
         }
 
         if (mDidRegister) {
             unregisterReceiver(mReceiver);
             mDidRegister = false;
         }
         stopReceivingLocationUpdates();
 
         if (mFocusToneGenerator != null) {
             mFocusToneGenerator.release();
             mFocusToneGenerator = null;
         }
 
         if (mStorageHint != null) {
             mStorageHint.cancel();
             mStorageHint = null;
         }
 
         // If we are in an image capture intent and has taken
         // a picture, we just clear it in onPause.
         mImageCapture.clearLastBitmap();
         mImageCapture = null;
 
         // This is necessary to make the ZoomButtonsController unregister
         // its configuration change receiver.
         if (mZoomButtons != null) {
             mZoomButtons.setVisible(false);
         }
 
         // Remove the messages in the event queue.
         mHandler.removeMessages(CLEAR_SCREEN_DELAY);
         mHandler.removeMessages(RESTART_PREVIEW);
         mHandler.removeMessages(STORE_IMAGE_DONE);
         if (mHandler.hasMessages(FIRST_TIME_INIT)) {
             mHandler.removeMessages(FIRST_TIME_INIT);
             mPendingFirstTimeInit = true;
         }
 
         super.onPause();
     }
 
     @Override
     protected void onActivityResult(
             int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case CROP_MSG: {
                 Intent intent = new Intent();
                 if (data != null) {
                     Bundle extras = data.getExtras();
                     if (extras != null) {
                         intent.putExtras(extras);
                     }
                 }
                 setResult(resultCode, intent);
                 finish();
 
                 File path = getFileStreamPath(sTempCropFilename);
                 path.delete();
 
                 break;
             }
         }
     }
 
     private void autoFocus() {
         if (mFocusState != FOCUSING && mFocusState != FOCUSING_SNAP_ON_FINISH) {
             if (mCameraDevice != null) {
                 mFocusStartTime = System.currentTimeMillis();
                 mFocusState = FOCUSING;
                 mCameraDevice.autoFocus(mAutoFocusCallback);
             }
         }
         updateFocusIndicator();
     }
 
     private void clearFocusState() {
         mFocusState = FOCUS_NOT_STARTED;
         updateFocusIndicator();
     }
 
     private void updateFocusIndicator() {
         if (mFocusRectangle == null) return;
 
         if (mFocusState == FOCUSING || mFocusState == FOCUSING_SNAP_ON_FINISH) {
             mFocusRectangle.showStart();
         } else if (mFocusState == FOCUS_SUCCESS) {
             mFocusRectangle.showSuccess();
         } else if (mFocusState == FOCUS_FAIL) {
             mFocusRectangle.showFail();
         } else {
             mFocusRectangle.clear();
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
         switch (keyCode) {
             case KeyEvent.KEYCODE_BACK:
                 if (mStatus == SNAPSHOT_IN_PROGRESS) {
                     // ignore backs while we're taking a picture
                     return true;
                 }
                 break;
             case KeyEvent.KEYCODE_FOCUS:
                 if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                     doFocus(true);
                 }
                 return true;
             case KeyEvent.KEYCODE_CAMERA:
                 if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                     doSnap();
                 }
                 return true;
             case KeyEvent.KEYCODE_DPAD_CENTER:
                 // If we get a dpad center event without any focused view, move
                 // the focus to the shutter button and press it.
                 if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                     // Start auto-focus immediately to reduce shutter lag. After
                     // the shutter button gets the focus, doFocus() will be
                     // called again but it is fine.
                     doFocus(true);
                     if (mShutterButton.isInTouchMode()) {
                         mShutterButton.requestFocusFromTouch();
                     } else {
                         mShutterButton.requestFocus();
                     }
                     mShutterButton.setPressed(true);
                 }
                 return true;
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_FOCUS:
                 if (mFirstTimeInitialized) {
                     doFocus(false);
                 }
                 return true;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (mPausing) return true;
 
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 if (mZoomButtons != null) {
                     mZoomButtons.setVisible(true);
                 }
                 return true;
         }
         return super.onTouchEvent(event);
     }
 
     private void doSnap() {
         // If the user has half-pressed the shutter and focus is completed, we
         // can take the photo right away. If the focus mode is infinity, we can
         // also take the photo.
         if (mFocusMode.equals(getString(
                 R.string.pref_camera_focusmode_value_infinity))
                 || (mFocusState == FOCUS_SUCCESS || mFocusState == FOCUS_FAIL)
                 || !mPreviewing) {
             if (mImageCapture != null) {
                 mImageCapture.onSnap();
             }
         } else if (mFocusState == FOCUSING) {
             // Half pressing the shutter (i.e. the focus button event) will
             // already have requested AF for us, so just request capture on
             // focus here.
             mFocusState = FOCUSING_SNAP_ON_FINISH;
         } else if (mFocusState == FOCUS_NOT_STARTED) {
             // Focus key down event is dropped for some reasons. Just ignore.
         }
     }
 
     private void doFocus(boolean pressed) {
         // Do the focus if the mode is auto. No focus needed in infinity mode.
         if (mFocusMode.equals(getString(
                 R.string.pref_camera_focusmode_value_auto))) {
             if (pressed) {  // Focus key down.
                 if (mPreviewing) {
                     autoFocus();
                 } else if (mImageCapture != null) {
                     // Save and restart preview
                     mImageCapture.onSnap();
                 }
             } else {  // Focus key up.
                 if (mFocusState != FOCUSING_SNAP_ON_FINISH) {
                     // User releases half-pressed focus key.
                     clearFocusState();
                 }
             }
         }
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         mSurfaceView.setVisibility(View.VISIBLE);
        // Start the preview.
        setViewFinder(w, h);
         boolean creating = holder.isCreating();
         // If the surface is creating, send a message to do first time
         // initialization later. We want to finish surfaceChanged as soon as
         // possible to let user see preview images first.
         if (creating && !mFirstTimeInitialized) {
             mHandler.sendEmptyMessage(FIRST_TIME_INIT);
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         mSurfaceHolder = holder;
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         stopPreview();
         mSurfaceHolder = null;
     }
 
     private void closeCamera() {
         if (mCameraDevice != null) {
             CameraHolder.instance().release();
             mCameraDevice = null;
             mPreviewing = false;
         }
     }
 
     private boolean ensureCameraDevice() {
         if (mCameraDevice == null) {
             mCameraDevice = CameraHolder.instance().open();
         }
         return mCameraDevice != null;
     }
 
     private void updateLastImage() {
         IImageList list = ImageManager.allImages(
             mContentResolver,
             dataLocation(),
             ImageManager.INCLUDE_IMAGES,
             ImageManager.SORT_ASCENDING,
             ImageManager.CAMERA_IMAGE_BUCKET_ID);
         int count = list.getCount();
         if (count > 0) {
             IImage image = list.getImageAt(count - 1);
             Uri uri = image.fullSizeImageUri();
             mThumbController.setData(uri, image.miniThumbBitmap());
         } else {
             mThumbController.setData(null, null);
         }
         list.deactivate();
     }
 
     private void restartPreview() {
         VideoPreview surfaceView = mSurfaceView;
 
         // make sure the surfaceview fills the whole screen when previewing
         surfaceView.setAspectRatio(VideoPreview.DONT_CARE);
        setViewFinder(mOriginalViewFinderWidth, mOriginalViewFinderHeight);
         mStatus = IDLE;
 
         // Calculate this in advance of each shot so we don't add to shutter
         // latency. It's true that someone else could write to the SD card in
         // the mean time and fill it, but that could have happened between the
         // shutter press and saving the JPEG too.
         calculatePicturesRemaining();
 
 
     }
 
    private void setViewFinder(int w, int h) {
         if (mPausing) return;
 
         if (mPreviewing && w == mViewFinderWidth && h == mViewFinderHeight) {
             return;
         }
 
         if (!ensureCameraDevice()) return;
 
         if (mSurfaceHolder == null) return;
 
         if (isFinishing()) return;
 
         if (mPausing) return;
 
         // remember view finder size
         mViewFinderWidth = w;
         mViewFinderHeight = h;
         if (mOriginalViewFinderHeight == 0) {
             mOriginalViewFinderWidth = w;
             mOriginalViewFinderHeight = h;
         }
 
        // start the preview
         //
         // we want to start the preview and we're previewing already,
         // stop the preview first (this will blank the screen).
         if (mPreviewing) stopPreview();
 
         // this blanks the screen if the surface changed, no-op otherwise
         try {
             mCameraDevice.setPreviewDisplay(mSurfaceHolder);
         } catch (IOException exception) {
             CameraHolder.instance().release();
             mCameraDevice = null;
             // TODO: add more exception handling logic here
             return;
         }
 
         setCameraParameter();
 
         final long wallTimeStart = SystemClock.elapsedRealtime();
         final long threadTimeStart = Debug.threadCpuTimeNanos();
 
         // Set one shot preview callback for latency measurement.
         mCameraDevice.setOneShotPreviewCallback(mOneShotPreviewCallback);
 
         try {
             mCameraDevice.startPreview();
         } catch (Throwable e) {
             // TODO: change Throwable to IOException once
             //      android.hardware.Camera.startPreview properly declares
             //      that it throws IOException.
         }
         mPreviewing = true;
 
         long threadTimeEnd = Debug.threadCpuTimeNanos();
         long wallTimeEnd = SystemClock.elapsedRealtime();
         if ((wallTimeEnd - wallTimeStart) > 3000) {
             Log.w(TAG, "startPreview() to " + (wallTimeEnd - wallTimeStart)
                     + " ms. Thread time was"
                     + (threadTimeEnd - threadTimeStart) / 1000000 + " ms.");
         }
     }
 
     private void setCameraParameter() {
         // request the preview size, the hardware may not honor it,
         // if we depended on it we would have to query the size again
         mParameters = mCameraDevice.getParameters();
         mParameters.setPreviewSize(mViewFinderWidth, mViewFinderHeight);
 
         // Set white balance parameter.
         String whiteBalance = mPreferences.getString(
                 CameraSettings.KEY_WHITE_BALANCE,
                 getString(R.string.pref_camera_whitebalance_default));
         mParameters.set(PARM_WHITE_BALANCE, whiteBalance);
 
         // Set effect parameter.
         String effect = mPreferences.getString(
                 CameraSettings.KEY_EFFECT,
                 getString(R.string.pref_camera_effect_default));
         mParameters.set(PARM_EFFECT, effect);
 
         // Set picture size parameter.
         String pictureSize = mPreferences.getString(
                 CameraSettings.KEY_PICTURE_SIZE,
                 getString(R.string.pref_camera_picturesize_default));
         mParameters.set(PARM_PICTURE_SIZE, pictureSize);
 
         // Set JPEG quality parameter.
         String jpegQuality = mPreferences.getString(
                 CameraSettings.KEY_JPEG_QUALITY,
                 getString(R.string.pref_camera_jpegquality_default));
         mParameters.set(PARM_JPEG_QUALITY, jpegQuality);
 
         // Set ISO parameter.
         String iso = mPreferences.getString(
                 CameraSettings.KEY_ISO,
                 getString(R.string.pref_camera_iso_default));
         mParameters.set(PARM_ISO, iso);
 
         // Set zoom.
         if (mZoomValues != null) {
             mParameters.set(PARM_ZOOM, mZoomValues[mZoomIndex]);
         }
 
         mCameraDevice.setParameters(mParameters);
     }
 
     private void stopPreview() {
         if (mCameraDevice != null && mPreviewing) {
             mCameraDevice.stopPreview();
         }
         mPreviewing = false;
         // If auto focus was in progress, it would have been canceled.
         clearFocusState();
     }
 
     void gotoGallery() {
         MenuHelper.gotoCameraImageGallery(this);
     }
 
     private void viewLastImage() {
         if (mThumbController.isUriValid()) {
             Uri targetUri = mThumbController.getUri();
             targetUri = targetUri.buildUpon().appendQueryParameter(
                     "bucketId", ImageManager.CAMERA_IMAGE_BUCKET_ID).build();
             Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
             intent.putExtra(MediaStore.EXTRA_FULL_SCREEN, true);
             intent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, true);
             intent.putExtra("com.android.camera.ReviewMode", true);
             try {
                 startActivity(intent);
             } catch (ActivityNotFoundException ex) {
                 Log.e(TAG, "review image fail", ex);
             }
         } else {
             Log.e(TAG, "Can't view last image.");
         }
     }
 
     private void startReceivingLocationUpdates() {
         if (mLocationManager != null) {
             try {
                 mLocationManager.requestLocationUpdates(
                         LocationManager.NETWORK_PROVIDER,
                         1000,
                         0F,
                         mLocationListeners[1]);
             } catch (java.lang.SecurityException ex) {
                 Log.i(TAG, "fail to request location update, ignore", ex);
             } catch (IllegalArgumentException ex) {
                 Log.d(TAG, "provider does not exist " + ex.getMessage());
             }
             try {
                 mLocationManager.requestLocationUpdates(
                         LocationManager.GPS_PROVIDER,
                         1000,
                         0F,
                         mLocationListeners[0]);
             } catch (java.lang.SecurityException ex) {
                 Log.i(TAG, "fail to request location update, ignore", ex);
             } catch (IllegalArgumentException ex) {
                 Log.d(TAG, "provider does not exist " + ex.getMessage());
             }
         }
     }
 
     private void stopReceivingLocationUpdates() {
         if (mLocationManager != null) {
             for (int i = 0; i < mLocationListeners.length; i++) {
                 try {
                     mLocationManager.removeUpdates(mLocationListeners[i]);
                 } catch (Exception ex) {
                     Log.i(TAG, "fail to remove location listners, ignore", ex);
                 }
             }
         }
     }
 
     private Location getCurrentLocation() {
         // go in best to worst order
         for (int i = 0; i < mLocationListeners.length; i++) {
             Location l = mLocationListeners[i].current();
             if (l != null) return l;
         }
         return null;
     }
 
     @Override
     public void onOptionsMenuClosed(Menu menu) {
         super.onOptionsMenuClosed(menu);
         if (mImageSavingItem) {
             // save the image if we presented the "advanced" menu
             // which happens if "menu" is pressed while in
             // SNAPSHOT_IN_PROGRESS  or SNAPSHOT_COMPLETED modes
             mHandler.sendEmptyMessage(RESTART_PREVIEW);
         }
     }
 
     @Override
     public boolean onMenuOpened(int featureId, Menu menu) {
         if (featureId == Window.FEATURE_OPTIONS_PANEL) {
             if (mStatus == SNAPSHOT_IN_PROGRESS) {
                 cancelAutomaticPreviewRestart();
             }
         }
         return super.onMenuOpened(featureId, menu);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         for (int i = 1; i <= MenuHelper.MENU_ITEM_MAX; i++) {
             if (i != MenuHelper.GENERIC_ITEM) {
                 menu.setGroupVisible(i, false);
             }
         }
 
         if (mStatus == SNAPSHOT_IN_PROGRESS || mStatus == SNAPSHOT_COMPLETED) {
             menu.setGroupVisible(MenuHelper.IMAGE_SAVING_ITEM, true);
             mImageSavingItem = true;
         } else {
             menu.setGroupVisible(MenuHelper.IMAGE_MODE_ITEM, true);
             mImageSavingItem = false;
         }
 
         return true;
     }
 
     private void cancelAutomaticPreviewRestart() {
         mKeepAndRestartPreview = false;
         mHandler.removeMessages(RESTART_PREVIEW);
     }
 
     private boolean isImageCaptureIntent() {
         String action = getIntent().getAction();
         return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action));
     }
 
     private void showPostCaptureAlert() {
         if (mIsImageCaptureIntent) {
             mPostCaptureAlert.setVisibility(View.VISIBLE);
             int[] pickIds = {R.id.attach, R.id.cancel};
             for (int id : pickIds) {
                 View view = mPostCaptureAlert.findViewById(id);
                 view.setOnClickListener(this);
                 Animation animation = new AlphaAnimation(0F, 1F);
                 animation.setDuration(500);
                 view.setAnimation(animation);
             }
         }
     }
 
     private void hidePostCaptureAlert() {
         if (mIsImageCaptureIntent) {
             mPostCaptureAlert.setVisibility(View.INVISIBLE);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         if (mIsImageCaptureIntent) {
             // No options menu for attach mode.
             return false;
         } else {
             addBaseMenuItems(menu);
         }
         return true;
     }
 
     private int calculatePicturesRemaining() {
         mPicturesRemaining = MenuHelper.calculatePicturesRemaining();
         return mPicturesRemaining;
     }
 
     private void addBaseMenuItems(Menu menu) {
         MenuHelper.addSwitchModeMenuItem(menu, this, true);
         {
             MenuItem gallery = menu.add(
                     MenuHelper.IMAGE_MODE_ITEM, MENU_GALLERY_PHOTOS, 0,
                     R.string.camera_gallery_photos_text)
                     .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                 public boolean onMenuItemClick(MenuItem item) {
                     gotoGallery();
                     return true;
                 }
             });
             gallery.setIcon(android.R.drawable.ic_menu_gallery);
             mGalleryItems.add(gallery);
         }
         {
             MenuItem gallery = menu.add(
                     MenuHelper.VIDEO_MODE_ITEM, MENU_GALLERY_VIDEOS, 0,
                     R.string.camera_gallery_photos_text)
                     .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                 public boolean onMenuItemClick(MenuItem item) {
                     gotoGallery();
                     return true;
                 }
             });
             gallery.setIcon(android.R.drawable.ic_menu_gallery);
             mGalleryItems.add(gallery);
         }
 
         MenuItem item = menu.add(MenuHelper.GENERIC_ITEM, MENU_SETTINGS,
                 0, R.string.settings)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
             public boolean onMenuItemClick(MenuItem item) {
                 // Do not go to camera settings during capture.
                 if (mStatus == IDLE && mFocusState == FOCUS_NOT_STARTED) {
                     Intent intent = new Intent();
                     intent.setClass(Camera.this, CameraSettings.class);
                     startActivity(intent);
                 }
                 return true;
             }
         });
         item.setIcon(android.R.drawable.ic_menu_preferences);
     }
 }
 
 class FocusRectangle extends View {
 
     @SuppressWarnings("unused")
     private static final String TAG = "FocusRectangle";
 
     public FocusRectangle(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     private void setDrawable(int resid) {
         BitmapDrawable d = (BitmapDrawable) getResources().getDrawable(resid);
         // We do this because we don't want the bitmap to be scaled.
         d.setGravity(Gravity.CENTER);
         setBackgroundDrawable(d);
     }
 
     public void showStart() {
         setDrawable(R.drawable.frame_autofocus_rectangle);
     }
 
     public void showSuccess() {
         setDrawable(R.drawable.frame_focused_rectangle);
     }
 
     public void showFail() {
         setDrawable(R.drawable.frame_nofocus_rectangle);
     }
 
     public void clear() {
         setBackgroundDrawable(null);
     }
 }

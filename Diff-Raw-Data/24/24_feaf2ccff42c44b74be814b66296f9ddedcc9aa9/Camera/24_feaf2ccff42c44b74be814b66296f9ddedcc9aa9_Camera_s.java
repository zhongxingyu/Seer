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
 
 import com.android.camera.ui.CameraHeadUpDisplay;
 import com.android.camera.ui.CameraPicker;
 import com.android.camera.ui.FocusRectangle;
 import com.android.camera.ui.GLRootView;
 import com.android.camera.ui.HeadUpDisplay;
 import com.android.camera.ui.IndicatorWheel;
 import com.android.camera.ui.ZoomControllerListener;
 import com.android.camera.ui.ZoomPicker;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ContentProviderClient;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.os.MessageQueue;
 import android.os.SystemClock;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewStub;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 /** The Camera activity which can preview and take pictures. */
 public class Camera extends ActivityBase implements View.OnClickListener,
         View.OnTouchListener, ShutterButton.OnShutterButtonListener,
         SurfaceHolder.Callback, Switcher.OnSwitchListener {
 
     private static final String TAG = "camera";
 
     private static final String LAST_THUMB_PATH =
             Storage.THUMBNAILS + "/image_last_thumb";
 
     private static final int CROP_MSG = 1;
     private static final int FIRST_TIME_INIT = 2;
     private static final int RESTART_PREVIEW = 3;
     private static final int CLEAR_SCREEN_DELAY = 4;
     private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 5;
     private static final int CHECK_DISPLAY_ROTATION = 6;
 
     // The subset of parameters we need to update in setCameraParameters().
     private static final int UPDATE_PARAM_INITIALIZE = 1;
     private static final int UPDATE_PARAM_ZOOM = 2;
     private static final int UPDATE_PARAM_PREFERENCE = 4;
     private static final int UPDATE_PARAM_ALL = -1;
 
     // When setCameraParametersWhenIdle() is called, we accumulate the subsets
     // needed to be updated in mUpdateSet.
     private int mUpdateSet;
 
     // The brightness settings used when it is set to automatic in the system.
     // The reason why it is set to 0.7 is just because 1.0 is too bright.
     private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;
 
     private static final int SCREEN_DELAY = 2 * 60 * 1000;
     private static final int FOCUS_BEEP_VOLUME = 100;
 
     private static final int ZOOM_STOPPED = 0;
     private static final int ZOOM_START = 1;
     private static final int ZOOM_STOPPING = 2;
 
     private int mZoomState = ZOOM_STOPPED;
     private boolean mSmoothZoomSupported = false;
     private int mZoomValue;  // The current zoom value.
     private int mZoomMax;
     private int mTargetZoomValue;
     private ZoomPicker mZoomPicker;
 
     private Parameters mParameters;
     private Parameters mInitialParams;
 
     private MyOrientationEventListener mOrientationListener;
     // The device orientation in degrees. Default is unknown.
     private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
     // The orientation compensation for icons and thumbnails.
     private int mOrientationCompensation = 0;
     private ComboPreferences mPreferences;
 
     private static final int IDLE = 1;
     private static final int SNAPSHOT_IN_PROGRESS = 2;
 
     private static final boolean SWITCH_CAMERA = true;
     private static final boolean SWITCH_VIDEO = false;
 
     private int mStatus = IDLE;
     private static final String sTempCropFilename = "crop-temp";
 
     private android.hardware.Camera mCameraDevice;
     private ContentProviderClient mMediaProviderClient;
     private SurfaceView mSurfaceView;
     private SurfaceHolder mSurfaceHolder = null;
     private ShutterButton mShutterButton;
     private ToneGenerator mFocusToneGenerator;
     private GestureDetector mPopupGestureDetector;
     private SwitcherSet mSwitcher;
     private boolean mStartPreviewFail = false;
 
     private View mPreviewFrame;  // Preview frame area.
     private View mPreviewBorder;
     private FocusRectangle mFocusRectangle;
     private Rect mFocusArea = new Rect();  // focus area in driver format
     private boolean mTouchFocusEnabled;
 
     private GLRootView mGLRootView;
 
     // The last captured picture.
     private RotateImageView mThumbnailButton;
 
     // mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.
     private String mCropValue;
     private Uri mSaveUri;
 
     // GPS on-screen indicator
     private View mGpsNoSignalView;
     private View mGpsHasSignalView;
 
     // Front/Back camera pciker for xlarge layout
     private CameraPicker mCameraPicker;
 
     /**
      * An unpublished intent flag requesting to return as soon as capturing
      * is completed.
      *
      * TODO: consider publishing by moving into MediaStore.
      */
     private final static String EXTRA_QUICK_CAPTURE =
             "android.intent.extra.quickCapture";
 
     private boolean mPreviewing;
     // The display rotation in degrees. This is only valid when mPreviewing is
     // true.
     private int mDisplayRotation;
     private boolean mPausing;
     private boolean mFirstTimeInitialized;
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
 
     private final ShutterCallback mShutterCallback = new ShutterCallback();
     private final PostViewPictureCallback mPostViewPictureCallback =
             new PostViewPictureCallback();
     private final RawPictureCallback mRawPictureCallback =
             new RawPictureCallback();
     private final AutoFocusCallback mAutoFocusCallback =
             new AutoFocusCallback();
     private final ZoomListener mZoomListener = new ZoomListener();
     private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
 
     private long mFocusStartTime;
     private long mFocusCallbackTime;
     private long mCaptureStartTime;
     private long mShutterCallbackTime;
     private long mPostViewPictureCallbackTime;
     private long mRawPictureCallbackTime;
     private long mJpegPictureCallbackTime;
     private long mOnResumeTime;
     private long mPicturesRemaining;
     private byte[] mJpegImageData;
 
     // These latency time are for the CameraLatency test.
     public long mAutoFocusTime;
     public long mShutterLag;
     public long mShutterToPictureDisplayedTime;
     public long mPictureDisplayedToJpegCallbackTime;
     public long mJpegCallbackFinishTime;
 
     // Focus mode. Options are pref_camera_focusmode_entryvalues.
     private String mFocusMode;
     private String mSceneMode;
     private Toast mNotSelectableToast;
 
     private final Handler mHandler = new MainHandler();
     // xlarge devices use indicator wheel. Other devices use head-up display.
     private CameraHeadUpDisplay mHeadUpDisplay;
     private IndicatorWheel mIndicatorWheel;
     private PreferenceGroup mPreferenceGroup;
 
     // multiple cameras support
     private int mNumberOfCameras;
     private int mCameraId;
     private int mFrontCameraId;
     private int mBackCameraId;
 
     private boolean mQuickCapture;
 
     /**
      * This Handler is used to post message back onto the main thread of the
      * application
      */
     private class MainHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case RESTART_PREVIEW: {
                     restartPreview();
                     if (mJpegPictureCallbackTime != 0) {
                         long now = System.currentTimeMillis();
                         mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
                         Log.v(TAG, "mJpegCallbackFinishTime = "
                                 + mJpegCallbackFinishTime + "ms");
                         mJpegPictureCallbackTime = 0;
                     }
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
 
                 case SET_CAMERA_PARAMETERS_WHEN_IDLE: {
                     setCameraParametersWhenIdle(0);
                     break;
                 }
 
                 case CHECK_DISPLAY_ROTATION: {
                     // Restart the preview if display rotation has changed.
                     // Sometimes this happens when the device is held upside
                     // down and camera app is opened. Rotation animation will
                     // take some time and the rotation value we have got may be
                     // wrong. Framework does not have a callback for this now.
                     if (Util.getDisplayRotation(Camera.this) != mDisplayRotation
                             && isCameraIdle()) {
                         restartPreview();
                     }
                     if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                         mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
                     }
                     break;
                 }
             }
         }
     }
 
     private void resetExposureCompensation() {
         String value = mPreferences.getString(CameraSettings.KEY_EXPOSURE,
                 CameraSettings.EXPOSURE_DEFAULT_VALUE);
         if (!CameraSettings.EXPOSURE_DEFAULT_VALUE.equals(value)) {
             Editor editor = mPreferences.edit();
             editor.putString(CameraSettings.KEY_EXPOSURE, "0");
             editor.apply();
             if (mHeadUpDisplay != null) {
                 mHeadUpDisplay.reloadPreferences();
             }
             if (mIndicatorWheel != null) {
                 mIndicatorWheel.reloadPreferences();
             }
         }
     }
 
     private void keepMediaProviderInstance() {
         // We want to keep a reference to MediaProvider in camera's lifecycle.
         // TODO: Utilize mMediaProviderClient instance to replace
         // ContentResolver calls.
         if (mMediaProviderClient == null) {
             mMediaProviderClient = getContentResolver()
                     .acquireContentProviderClient(MediaStore.AUTHORITY);
         }
     }
 
     // Snapshots can only be taken after this is called. It should be called
     // once only. We could have done these things in onCreate() but we want to
     // make preview screen appear as soon as possible.
     private void initializeFirstTime() {
         if (mFirstTimeInitialized) return;
 
         // Create orientation listenter. This should be done first because it
         // takes some time to get first orientation.
         mOrientationListener = new MyOrientationEventListener(Camera.this);
         mOrientationListener.enable();
 
         // Initialize location sevice.
         mLocationManager = (LocationManager)
                 getSystemService(Context.LOCATION_SERVICE);
         mRecordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
         initGpsOnScreenIndicator();
         if (mRecordLocation) startReceivingLocationUpdates();
 
         keepMediaProviderInstance();
         checkStorage();
 
         // Initialize last picture button.
         mContentResolver = getContentResolver();
         if (!mIsImageCaptureIntent)  {
             findViewById(R.id.camera_switch).setOnClickListener(this);
             initThumbnailButton();
         }
 
         // Initialize shutter button.
         mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
         mShutterButton.setOnShutterButtonListener(this);
         mShutterButton.setVisibility(View.VISIBLE);
 
         // Initialize focus UI.
         mPreviewFrame = findViewById(R.id.camera_preview);
         mPreviewFrame.setOnTouchListener(this);
         mPreviewBorder = (View) findViewById(R.id.preview_border);
         updateFocusIndicator();
 
         initializeScreenBrightness();
         installIntentFilter();
         initializeFocusTone();
         initializeZoom();
         // xlarge devices use indicator wheel. Other devices use head-up display.
         if (mIndicatorWheel == null) {
             mHeadUpDisplay = new CameraHeadUpDisplay(this);
             mHeadUpDisplay.setListener(new MyHeadUpDisplayListener());
             initializeHeadUpDisplay();
         }
         mFirstTimeInitialized = true;
         changeHeadUpDisplayState();
         addIdleHandler();
     }
 
     private void addIdleHandler() {
         MessageQueue queue = Looper.myQueue();
         queue.addIdleHandler(new MessageQueue.IdleHandler() {
             public boolean queueIdle() {
                 Storage.ensureOSXCompatible();
                 return false;
             }
         });
     }
 
     private void initThumbnailButton() {
         mThumbnailButton =
                 (RotateImageView) findViewById(R.id.review_thumbnail);
         if (mThumbnailButton != null) {
             mThumbnailButton.setOnClickListener(this);
             mThumbnailButton.loadData(LAST_THUMB_PATH);
             updateThumbnailButton();
         }
     }
 
     private void updateThumbnailButton() {
         if (mThumbnailButton == null) return;
         // Update last image if URI is invalid and the storage is ready.
         if (!mThumbnailButton.isUriValid() && mPicturesRemaining >= 0) {
             Storage.Thumbnail thumbnail =
                     Storage.getLastImageThumbnail(mContentResolver);
             if (thumbnail != null) {
                 mThumbnailButton.setData(thumbnail.getOriginalUri(),
                         thumbnail.getBitmap(mContentResolver));
             } else {
                 mThumbnailButton.setData(null, null);
             }
         }
         mThumbnailButton.setVisibility(
                 (mThumbnailButton.getUri() != null) ? View.VISIBLE : View.GONE);
     }
 
     // If the activity is paused and resumed, this method will be called in
     // onResume.
     private void initializeSecondTime() {
         // Start orientation listener as soon as possible because it takes
         // some time to get first orientation.
         mOrientationListener.enable();
 
         // Start location update if needed.
         mRecordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
         if (mRecordLocation) startReceivingLocationUpdates();
 
         installIntentFilter();
         initializeFocusTone();
         initializeZoom();
         changeHeadUpDisplayState();
 
         keepMediaProviderInstance();
         checkStorage();
 
         if (!mIsImageCaptureIntent) {
             updateThumbnailButton();
         }
     }
 
     private void initializeZoom() {
         if (!mParameters.isZoomSupported()) return;
 
         mZoomMax = mParameters.getMaxZoom();
         mSmoothZoomSupported = mParameters.isSmoothZoomSupported();
         if (mZoomPicker != null) {
             mZoomPicker.setZoomRatios(getZoomRatios());
             mZoomPicker.setZoomIndex(mParameters.getZoom());
             mZoomPicker.setSmoothZoomSupported(mSmoothZoomSupported);
             mZoomPicker.setOnZoomChangeListener(
                     new ZoomPicker.OnZoomChangedListener() {
                 // only for immediate zoom
                 public void onZoomValueChanged(int index) {
                     Camera.this.onZoomValueChanged(index);
                 }
 
                 // only for smooth zoom
                 public void onZoomStateChanged(int state) {
                     if (mPausing) return;
 
                     Log.v(TAG, "zoom picker state=" + state);
                     if (state == mZoomPicker.ZOOM_IN) {
                         Camera.this.onZoomValueChanged(mZoomMax);
                     } else if (state == mZoomPicker.ZOOM_OUT){
                         Camera.this.onZoomValueChanged(0);
                     } else {
                         mTargetZoomValue = -1;
                         if (mZoomState == ZOOM_START) {
                             mZoomState = ZOOM_STOPPING;
                             mCameraDevice.stopSmoothZoom();
                         }
                     }
                 }
             });
         }
 
         mCameraDevice.setZoomChangeListener(mZoomListener);
     }
 
     private void onZoomValueChanged(int index) {
         // Not useful to change zoom value when the activity is paused.
         if (mPausing) return;
 
         if (mSmoothZoomSupported) {
             if (mTargetZoomValue != index && mZoomState != ZOOM_STOPPED) {
                 mTargetZoomValue = index;
                 if (mZoomState == ZOOM_START) {
                     mZoomState = ZOOM_STOPPING;
                     mCameraDevice.stopSmoothZoom();
                 }
             } else if (mZoomState == ZOOM_STOPPED && mZoomValue != index) {
                 mTargetZoomValue = index;
                 mCameraDevice.startSmoothZoom(index);
                 mZoomState = ZOOM_START;
             }
         } else {
             mZoomValue = index;
             setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);
         }
     }
 
     private float[] getZoomRatios() {
         if(!mParameters.isZoomSupported()) return null;
         List<Integer> zoomRatios = mParameters.getZoomRatios();
         float result[] = new float[zoomRatios.size()];
         for (int i = 0, n = result.length; i < n; ++i) {
             result[i] = (float) zoomRatios.get(i) / 100f;
         }
         return result;
     }
 
     private int mLocation[] = new int[2];
     private class PopupGestureListener extends
             GestureDetector.SimpleOnGestureListener {
         public boolean onDown(MotionEvent e) {
             // Check if the popup window is visible.
             View v = mIndicatorWheel.getActivePopupWindow();
             if (v == null) return false;
 
             int x = Math.round(e.getX());
             int y = Math.round(e.getY());
 
             // Dismiss the popup window if users touch on the outside.
             v.getLocationOnScreen(mLocation);
             if (x < mLocation[0] || (x > mLocation[0] + v.getWidth())
                     || y < mLocation[1] || (y > mLocation[1] + v.getHeight())) {
                 // Let indicator wheel handle its own event.
                 mIndicatorWheel.getLocationOnScreen(mLocation);
                 if (x < mLocation[0] || (x > mLocation[0] + mIndicatorWheel.getWidth())
                         || y < mLocation[1] || (y > mLocation[1] + mIndicatorWheel.getHeight())) {
                     mIndicatorWheel.dismissSettingPopup();
                 }
                 // Let event fall through.
             }
             return false;
         }
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent m) {
         // Check if the popup window should be dismissed first.
         if (mPopupGestureDetector != null && mPopupGestureDetector.onTouchEvent(m)) {
             return true;
         }
 
         return super.dispatchTouchEvent(m);
     }
 
     LocationListener [] mLocationListeners = new LocationListener[] {
             new LocationListener(LocationManager.GPS_PROVIDER),
             new LocationListener(LocationManager.NETWORK_PROVIDER)
     };
 
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                     || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                     || action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                 checkStorage();
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                 checkStorage();
                 if (!mIsImageCaptureIntent)  {
                     updateThumbnailButton();
                 }
             }
         }
     };
 
     private void initializeCameraPicker() {
         mCameraPicker = (CameraPicker) findViewById(R.id.camera_picker);
         if (mCameraPicker != null) {
             mCameraPicker.setImageResource(R.drawable.camera_toggle);
             ListPreference pref = mPreferenceGroup.findPreference(
                     CameraSettings.KEY_CAMERA_ID);
             if (pref != null) {
                 mCameraPicker.initialize(pref);
                 mCameraPicker.setListener(new MyCameraPickerListener());
             }
         }
     }
 
     private void initGpsOnScreenIndicator() {
         mGpsNoSignalView = findViewById(R.id.onscreen_gps_indicator_no_signal);
         mGpsHasSignalView = findViewById(R.id.onscreen_gps_indicator_on);
     }
 
     private void showGpsOnScreenIndicator(boolean hasSignal) {
         if (hasSignal) {
             if (mGpsNoSignalView != null) mGpsNoSignalView.setVisibility(View.INVISIBLE);
             if (mGpsHasSignalView != null) mGpsHasSignalView.setVisibility(View.VISIBLE);
         } else {
             if (mGpsNoSignalView != null) mGpsNoSignalView.setVisibility(View.VISIBLE);
             if (mGpsHasSignalView != null) mGpsHasSignalView.setVisibility(View.INVISIBLE);
         }
     }
 
     private void hideGpsOnScreenIndicator() {
         if (mGpsNoSignalView != null) mGpsNoSignalView.setVisibility(View.INVISIBLE);
         if (mGpsHasSignalView != null) mGpsHasSignalView.setVisibility(View.INVISIBLE);
     }
 
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
                 if (mHeadUpDisplay != null) {
                     mHeadUpDisplay.setGpsHasSignal(true);
                 }
                 showGpsOnScreenIndicator(true);
             }
             if (!mValid) {
                 Log.d(TAG, "Got first location.");
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
                         if (mHeadUpDisplay != null) {
                             mHeadUpDisplay.setGpsHasSignal(false);
                         }
                         showGpsOnScreenIndicator(false);
                     }
                     break;
                 }
             }
         }
 
         public Location current() {
             return mValid ? mLastLocation : null;
         }
     }
 
     private final class ShutterCallback
             implements android.hardware.Camera.ShutterCallback {
         public void onShutter() {
             mShutterCallbackTime = System.currentTimeMillis();
             mShutterLag = mShutterCallbackTime - mCaptureStartTime;
             Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
             clearFocusState();
         }
     }
 
     private final class PostViewPictureCallback implements PictureCallback {
         public void onPictureTaken(
                 byte [] data, android.hardware.Camera camera) {
             mPostViewPictureCallbackTime = System.currentTimeMillis();
             Log.v(TAG, "mShutterToPostViewCallbackTime = "
                     + (mPostViewPictureCallbackTime - mShutterCallbackTime)
                     + "ms");
         }
     }
 
     private final class RawPictureCallback implements PictureCallback {
         public void onPictureTaken(
                 byte [] rawData, android.hardware.Camera camera) {
             mRawPictureCallbackTime = System.currentTimeMillis();
             Log.v(TAG, "mShutterToRawCallbackTime = "
                     + (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
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
             // If postview callback has arrived, the captured image is displayed
             // in postview callback. If not, the captured image is displayed in
             // raw picture callback.
             if (mPostViewPictureCallbackTime != 0) {
                 mShutterToPictureDisplayedTime =
                         mPostViewPictureCallbackTime - mShutterCallbackTime;
                 mPictureDisplayedToJpegCallbackTime =
                         mJpegPictureCallbackTime - mPostViewPictureCallbackTime;
             } else {
                 mShutterToPictureDisplayedTime =
                         mRawPictureCallbackTime - mShutterCallbackTime;
                 mPictureDisplayedToJpegCallbackTime =
                         mJpegPictureCallbackTime - mRawPictureCallbackTime;
             }
             Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
                     + mPictureDisplayedToJpegCallbackTime + "ms");
 
             if (!mIsImageCaptureIntent) {
                 enableCameraControls(true);
 
                 // We want to show the taken picture for a while, so we wait
                 // for at least 1.2 second before restarting the preview.
                 long delay = 1200 - mPictureDisplayedToJpegCallbackTime;
                 if (delay < 0) {
                     restartPreview();
                 } else {
                     mHandler.sendEmptyMessageDelayed(RESTART_PREVIEW, delay);
                 }
             }
             storeImage(jpegData, camera, mLocation);
 
             // Check this in advance of each shot so we don't add to shutter
             // latency. It's true that someone else could write to the SD card in
             // the mean time and fill it, but that could have happened between the
             // shutter press and saving the JPEG too.
             checkStorage();
 
             if (!mHandler.hasMessages(RESTART_PREVIEW)) {
                 long now = System.currentTimeMillis();
                 mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
                 Log.v(TAG, "mJpegCallbackFinishTime = "
                         + mJpegCallbackFinishTime + "ms");
                 mJpegPictureCallbackTime = 0;
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
             if (mFocusState == FOCUSING_SNAP_ON_FINISH) {
                 // Take the picture no matter focus succeeds or fails. No need
                 // to play the AF sound if we're about to play the shutter
                 // sound.
                 if (focused) {
                     mFocusState = FOCUS_SUCCESS;
                 } else {
                     mFocusState = FOCUS_FAIL;
                 }
                 capture();
             } else if (mFocusState == FOCUSING) {
                 // User is half-pressing the focus key. Play the focus tone.
                 // Do not take the picture now.
                 if (focused) {
                     mFocusState = FOCUS_SUCCESS;
                     if (mFocusToneGenerator != null) {
                         mFocusToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2);
                     }
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
 
     private final class ZoomListener
             implements android.hardware.Camera.OnZoomChangeListener {
         public void onZoomChange(
                 int value, boolean stopped, android.hardware.Camera camera) {
             Log.v(TAG, "Zoom changed: value=" + value + ". stopped="+ stopped);
             mZoomValue = value;
 
             // Update the UI when we get zoom value.
             if (mZoomPicker != null) mZoomPicker.setZoomIndex(value);
 
             // Keep mParameters up to date. We do not getParameter again in
             // takePicture. If we do not do this, wrong zoom value will be set.
             mParameters.setZoom(value);
 
             if (stopped && mZoomState != ZOOM_STOPPED) {
                 if (mTargetZoomValue != -1 && value != mTargetZoomValue) {
                     mCameraDevice.startSmoothZoom(mTargetZoomValue);
                     mZoomState = ZOOM_START;
                 } else {
                     mZoomState = ZOOM_STOPPED;
                 }
             }
         }
     }
 
     public void storeImage(final byte[] data,
             android.hardware.Camera camera, Location loc) {
         if (!mIsImageCaptureIntent) {
             long dateTaken = System.currentTimeMillis();
             String title = createName(dateTaken);
 
             Storage.Thumbnail thumbnail = Storage.addImage(
                     mContentResolver, title, dateTaken, loc, data);
 
             if (thumbnail != null && mThumbnailButton != null) {
                 mThumbnailButton.setData(thumbnail.getOriginalUri(),
                         thumbnail.getBitmap(mContentResolver));
                 mThumbnailButton.setVisibility(View.VISIBLE);
 
                 sendBroadcast(new Intent("com.android.camera.NEW_PICTURE",
                         thumbnail.getOriginalUri()));
             }
         } else {
             mJpegImageData = data;
             if (!mQuickCapture) {
                 showPostCaptureAlert();
             } else {
                 doAttach();
             }
         }
     }
 
     private void capture() {
         // If we are already in the middle of taking a snapshot then ignore.
         if (mPausing || mStatus == SNAPSHOT_IN_PROGRESS || mCameraDevice == null) {
             return;
         }
         mCaptureStartTime = System.currentTimeMillis();
         mPostViewPictureCallbackTime = 0;
         enableCameraControls(false);
         mStatus = SNAPSHOT_IN_PROGRESS;
         mJpegImageData = null;
 
         // See android.hardware.Camera.Parameters.setRotation for
         // documentation.
         int rotation = 0;
         if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
             CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
             if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                 rotation = (info.orientation - mOrientation + 360) % 360;
             } else {  // back-facing camera
                 rotation = (info.orientation + mOrientation) % 360;
             }
         }
         mParameters.setRotation(rotation);
 
         // Clear previous GPS location from the parameters.
         mParameters.removeGpsData();
 
         // We always encode GpsTimeStamp
         mParameters.setGpsTimestamp(System.currentTimeMillis() / 1000);
 
         // Set GPS location.
         Location loc = mRecordLocation ? getCurrentLocation() : null;
         if (loc != null) {
             double lat = loc.getLatitude();
             double lon = loc.getLongitude();
             boolean hasLatLon = (lat != 0.0d) || (lon != 0.0d);
 
             if (hasLatLon) {
                 Log.d(TAG, "Set gps location");
                 mParameters.setGpsLatitude(lat);
                 mParameters.setGpsLongitude(lon);
                 mParameters.setGpsProcessingMethod(loc.getProvider().toUpperCase());
                 if (loc.hasAltitude()) {
                     mParameters.setGpsAltitude(loc.getAltitude());
                 } else {
                     // for NETWORK_PROVIDER location provider, we may have
                     // no altitude information, but the driver needs it, so
                     // we fake one.
                     mParameters.setGpsAltitude(0);
                 }
                 if (loc.getTime() != 0) {
                     // Location.getTime() is UTC in milliseconds.
                     // gps-timestamp is UTC in seconds.
                     long utcTimeSeconds = loc.getTime() / 1000;
                     mParameters.setGpsTimestamp(utcTimeSeconds);
                 }
             } else {
                 loc = null;
             }
         }
 
         mCameraDevice.setParameters(mParameters);
 
         mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
                 mPostViewPictureCallback, new JpegPictureCallback(loc));
         mPreviewing = false;
     }
 
     private boolean saveDataToFile(String filePath, byte[] data) {
         FileOutputStream f = null;
         try {
             f = new FileOutputStream(filePath);
             f.write(data);
         } catch (IOException e) {
             return false;
         } finally {
             Util.closeSilently(f);
         }
         return true;
     }
 
     private String createName(long dateTaken) {
         Date date = new Date(dateTaken);
         SimpleDateFormat dateFormat = new SimpleDateFormat(
                 getString(R.string.image_file_name_format));
 
         return dateFormat.format(date);
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         mIsImageCaptureIntent = isImageCaptureIntent();
         if (mIsImageCaptureIntent) {
             setContentView(R.layout.camera_attach);
         } else {
             setContentView(R.layout.camera);
         }
         mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
         mFocusRectangle = (FocusRectangle) findViewById(R.id.focus_rectangle);
 
         mPreferences = new ComboPreferences(this);
         CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
 
         mCameraId = CameraSettings.readPreferredCameraId(mPreferences);
 
         // Testing purpose. Launch a specific camera through the intent extras.
         int intentCameraId = Util.getCameraFacingIntentExtras(this);
         if (intentCameraId != -1) {
             mCameraId = intentCameraId;
         }
 
         mPreferences.setLocalId(this, mCameraId);
         CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
 
         mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();
         mQuickCapture = getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
 
         // we need to reset exposure for the preview
         resetExposureCompensation();
         /*
          * To reduce startup time, we start the preview in another thread.
          * We make sure the preview is started at the end of onCreate.
          */
         Thread startPreviewThread = new Thread(new Runnable() {
             public void run() {
                 try {
                     mStartPreviewFail = false;
                     startPreview();
                 } catch (CameraHardwareException e) {
                     // In eng build, we throw the exception so that test tool
                     // can detect it and report it
                     if ("eng".equals(Build.TYPE)) {
                         throw new RuntimeException(e);
                     }
                     mStartPreviewFail = true;
                 }
             }
         });
         startPreviewThread.start();
 
         // don't set mSurfaceHolder here. We have it set ONLY within
         // surfaceChanged / surfaceDestroyed, other parts of the code
         // assume that when it is set, the surface is also set.
         SurfaceHolder holder = mSurfaceView.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         if (mIsImageCaptureIntent) {
             setupCaptureParams();
 
             View reviewControl = findViewById(R.id.review_control);
             reviewControl.setVisibility(View.VISIBLE);
             reviewControl.findViewById(R.id.btn_cancel).setOnClickListener(this);
             reviewControl.findViewById(R.id.btn_retake).setOnClickListener(this);
             reviewControl.findViewById(R.id.btn_done).setOnClickListener(this);
         } else {
             mSwitcher = (SwitcherSet) findViewById(R.id.camera_switch);
             mSwitcher.setVisibility(View.VISIBLE);
             mSwitcher.setOnSwitchListener(this);
         }
 
         // Make sure preview is started.
         try {
             startPreviewThread.join();
             if (mStartPreviewFail) {
                 showCameraErrorAndFinish();
                 return;
             }
         } catch (InterruptedException ex) {
             // ignore
         }
 
         mBackCameraId = CameraHolder.instance().getBackCameraId();
         mFrontCameraId = CameraHolder.instance().getFrontCameraId();
 
         // Do this after starting preview because it depends on camera
         // parameters.
         initializeIndicatorWheel();
         initializeCameraPicker();
 
         mZoomPicker = (ZoomPicker) findViewById(R.id.zoom_picker);
         if (mZoomPicker != null) mZoomPicker.setEnabled(true); // disabled initially in xml
     }
 
     private void changeHeadUpDisplayState() {
         if (mHeadUpDisplay == null) return;
         // If the camera resumes behind the lock screen, the orientation
         // will be portrait. That causes OOM when we try to allocation GPU
         // memory for the GLSurfaceView again when the orientation changes. So,
         // we delayed initialization of HeadUpDisplay until the orientation
         // becomes landscape.
         Configuration config = getResources().getConfiguration();
         if (config.orientation == Configuration.ORIENTATION_LANDSCAPE
                 && !mPausing && mFirstTimeInitialized) {
             if (mGLRootView == null) attachHeadUpDisplay();
         } else if (mGLRootView != null) {
             detachHeadUpDisplay();
         }
     }
 
     private void overrideCameraSettings(final String flashMode,
             final String whiteBalance, final String focusMode) {
         if (mHeadUpDisplay != null) {
             mHeadUpDisplay.overrideSettings(
                     CameraSettings.KEY_FLASH_MODE, flashMode,
                     CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
                     CameraSettings.KEY_FOCUS_MODE, focusMode);
         }
         if (mIndicatorWheel != null) {
             mIndicatorWheel.overrideSettings(
                     CameraSettings.KEY_FLASH_MODE, flashMode,
                     CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
                     CameraSettings.KEY_FOCUS_MODE, focusMode);
         }
     }
 
     private void updateSceneModeUI() {
         // If scene mode is set, we cannot set flash mode, white balance, and
         // focus mode, instead, we read it from driver
         if (!Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
             overrideCameraSettings(mParameters.getFlashMode(),
                     mParameters.getWhiteBalance(), mParameters.getFocusMode());
         } else {
             overrideCameraSettings(null, null, null);
         }
     }
 
     private void loadCameraPreferences() {
         CameraSettings settings = new CameraSettings(this, mInitialParams,
                 mCameraId, CameraHolder.instance().getCameraInfo());
         mPreferenceGroup = settings.getPreferenceGroup(R.xml.camera_preferences);
     }
 
     private void initializeIndicatorWheel() {
         mIndicatorWheel = (IndicatorWheel) findViewById(R.id.indicator_wheel);
         if (mIndicatorWheel == null) return;
         loadCameraPreferences();
 
         String[] keys = new String[]{CameraSettings.KEY_FLASH_MODE,
             CameraSettings.KEY_WHITE_BALANCE,
             CameraSettings.KEY_COLOR_EFFECT,
             CameraSettings.KEY_SCENE_MODE};
         mIndicatorWheel.initialize(this, mPreferenceGroup, keys, true);
         mIndicatorWheel.setListener(new MyIndicatorWheelListener());
         mPopupGestureDetector = new GestureDetector(this,
                 new PopupGestureListener());
         updateSceneModeUI();
     }
 
     private void initializeHeadUpDisplay() {
         if (mHeadUpDisplay == null) return;
         loadCameraPreferences();
 
         // If we have zoom picker, do not show zoom control on head-up display.
         float[] zoomRatios = null;
         if (mZoomPicker == null) zoomRatios = getZoomRatios();
         mHeadUpDisplay.initialize(this, mPreferenceGroup,
                 zoomRatios, mOrientationCompensation);
         if (mZoomPicker == null && mParameters.isZoomSupported()) {
             mHeadUpDisplay.setZoomListener(new ZoomControllerListener() {
                 public void onZoomChanged(
                         int index, float ratio, boolean isMoving) {
                     onZoomValueChanged(index);
                 }
             });
         }
         updateSceneModeUI();
     }
 
     private void attachHeadUpDisplay() {
         mHeadUpDisplay.setOrientation(mOrientationCompensation);
         if (mParameters.isZoomSupported()) {
             mHeadUpDisplay.setZoomIndex(mZoomValue);
         }
         ViewGroup frame = (ViewGroup) findViewById(R.id.frame);
         mGLRootView = new GLRootView(this);
         mGLRootView.setContentPane(mHeadUpDisplay);
         frame.addView(mGLRootView);
     }
 
     private void detachHeadUpDisplay() {
         mHeadUpDisplay.setGpsHasSignal(false);
         mHeadUpDisplay.collapse();
         ((ViewGroup) mGLRootView.getParent()).removeView(mGLRootView);
         mGLRootView = null;
     }
 
     private boolean collapseCameraControls() {
         if (mHeadUpDisplay != null && mHeadUpDisplay.collapse()) {
             return true;
         }
         if (mIndicatorWheel != null && mIndicatorWheel.dismissSettingPopup()) {
             return true;
         }
         return false;
     }
 
     private void enableCameraControls(boolean enable) {
         if (mHeadUpDisplay != null) mHeadUpDisplay.setEnabled(enable);
         if (mIndicatorWheel != null) mIndicatorWheel.setEnabled(enable);
         if (mCameraPicker != null) mCameraPicker.setEnabled(enable);
         if (mZoomPicker != null) mZoomPicker.setEnabled(enable);
         if (mSwitcher != null) mSwitcher.setEnabled(enable);
     }
 
     public static int roundOrientation(int orientation) {
         return ((orientation + 45) / 90 * 90) % 360;
     }
 
     private class MyOrientationEventListener
             extends OrientationEventListener {
         public MyOrientationEventListener(Context context) {
             super(context);
         }
 
         @Override
         public void onOrientationChanged(int orientation) {
             // We keep the last known orientation. So if the user first orient
             // the camera then point the camera to floor or sky, we still have
             // the correct orientation.
             if (orientation == ORIENTATION_UNKNOWN) return;
             mOrientation = roundOrientation(orientation);
             // When the screen is unlocked, display rotation may change. Always
             // calculate the up-to-date orientationCompensation.
             int orientationCompensation = mOrientation
                     + Util.getDisplayRotation(Camera.this);
             if (mOrientationCompensation != orientationCompensation) {
                 mOrientationCompensation = orientationCompensation;
                 if (!mIsImageCaptureIntent) {
                     setOrientationIndicator(mOrientationCompensation);
                 }
                 if (mHeadUpDisplay != null) {
                     mHeadUpDisplay.setOrientation(mOrientationCompensation);
                 }
             }
         }
     }
 
     private void setOrientationIndicator(int degree) {
         RotateImageView icon = (RotateImageView) findViewById(
                 R.id.review_thumbnail);
         if (icon != null) icon.setDegree(degree);
 
         icon = (RotateImageView) findViewById(R.id.camera_switch_icon);
         if (icon != null) icon.setDegree(degree);
         icon = (RotateImageView) findViewById(R.id.video_switch_icon);
         if (icon != null) icon.setDegree(degree);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         if (!mIsImageCaptureIntent) {
             mSwitcher.setSwitch(SWITCH_CAMERA);
         }
     }
 
     @Override
     public void onStop() {
         super.onStop();
         if (mMediaProviderClient != null) {
             mMediaProviderClient.release();
             mMediaProviderClient = null;
         }
     }
 
     private void checkStorage() {
         mPicturesRemaining = Storage.getAvailableSpace();
         if (mPicturesRemaining > 0) {
             mPicturesRemaining /= 1500000;
         }
         updateStorageHint();
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.btn_retake:
                 hidePostCaptureAlert();
                 restartPreview();
                 break;
             case R.id.review_thumbnail:
                 if (isCameraIdle()) {
                     viewImage(mThumbnailButton);
                 }
                 break;
             case R.id.btn_done:
                 doAttach();
                 break;
             case R.id.btn_cancel:
                 doCancel();
                 break;
             case R.id.btn_gallery:
                 gotoGallery();
                 break;
         }
     }
 
     private void doAttach() {
         if (mPausing) {
             return;
         }
 
         byte[] data = mJpegImageData;
 
         if (mCropValue == null) {
             // First handle the no crop case -- just return the value.  If the
             // caller specifies a "save uri" then write the data to it's
             // stream. Otherwise, pass back a scaled down version of the bitmap
             // directly in the extras.
             if (mSaveUri != null) {
                 OutputStream outputStream = null;
                 try {
                     outputStream = mContentResolver.openOutputStream(mSaveUri);
                     outputStream.write(data);
                     outputStream.close();
 
                     setResultEx(RESULT_OK);
                     finish();
                 } catch (IOException ex) {
                     // ignore exception
                 } finally {
                     Util.closeSilently(outputStream);
                 }
             } else {
                 int orientation = Exif.getOrientation(data);
                 Bitmap bitmap = Util.makeBitmap(data, 50 * 1024);
                 bitmap = Util.rotate(bitmap, orientation);
                 setResultEx(RESULT_OK,
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
                 tempStream.write(data);
                 tempStream.close();
                 tempUri = Uri.fromFile(path);
             } catch (FileNotFoundException ex) {
                 setResultEx(Activity.RESULT_CANCELED);
                 finish();
                 return;
             } catch (IOException ex) {
                 setResultEx(Activity.RESULT_CANCELED);
                 finish();
                 return;
             } finally {
                 Util.closeSilently(tempStream);
             }
 
             Bundle newExtras = new Bundle();
             if (mCropValue.equals("circle")) {
                 newExtras.putString("circleCrop", "true");
             }
             if (mSaveUri != null) {
                 newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, mSaveUri);
             } else {
                 newExtras.putBoolean("return-data", true);
             }
 
             Intent cropIntent = new Intent("com.android.camera.action.CROP");
 
             cropIntent.setData(tempUri);
             cropIntent.putExtras(newExtras);
 
             startActivityForResult(cropIntent, CROP_MSG);
         }
     }
 
     private void doCancel() {
         setResultEx(RESULT_CANCELED, new Intent());
         finish();
     }
 
     public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
         if (mPausing) {
             return;
         }
         switch (button.getId()) {
             case R.id.shutter_button:
                 doFocus(pressed);
                 break;
         }
     }
 
     public void onShutterButtonClick(ShutterButton button) {
         if (mPausing) {
             return;
         }
         switch (button.getId()) {
             case R.id.shutter_button:
                 doSnap();
                 break;
         }
     }
 
     private OnScreenHint mStorageHint;
 
     private void updateStorageHint() {
         String noStorageText = null;
 
         if (mPicturesRemaining == Storage.UNAVAILABLE) {
             noStorageText = getString(R.string.no_storage);
         } else if (mPicturesRemaining == Storage.PREPARING) {
             noStorageText = getString(R.string.preparing_sd);
         } else if (mPicturesRemaining == Storage.UNKNOWN_SIZE) {
             noStorageText = getString(R.string.access_sd_fail);
         } else if (mPicturesRemaining < 1L) {
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
 
     private void installIntentFilter() {
         // install an intent filter to receive SD card related events.
         IntentFilter intentFilter =
                 new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
         intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
         intentFilter.addDataScheme("file");
         registerReceiver(mReceiver, intentFilter);
         mDidRegister = true;
     }
 
     private void initializeFocusTone() {
         // Initialize focus tone generator.
         try {
             mFocusToneGenerator = new ToneGenerator(
                     AudioManager.STREAM_SYSTEM, FOCUS_BEEP_VOLUME);
         } catch (Throwable ex) {
             Log.w(TAG, "Exception caught while creating tone generator: ", ex);
             mFocusToneGenerator = null;
         }
     }
 
     private void initializeScreenBrightness() {
         Window win = getWindow();
         // Overright the brightness settings if it is automatic
         int mode = Settings.System.getInt(
                 getContentResolver(),
                 Settings.System.SCREEN_BRIGHTNESS_MODE,
                 Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
         if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
             WindowManager.LayoutParams winParams = win.getAttributes();
             winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
             win.setAttributes(winParams);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         mPausing = false;
         mJpegPictureCallbackTime = 0;
         mZoomValue = 0;
 
         // Start the preview if it is not started.
         if (!mPreviewing && !mStartPreviewFail) {
             resetExposureCompensation();
             if (!restartPreview()) return;
         }
 
         if (mSurfaceHolder != null) {
             // If first time initialization is not finished, put it in the
             // message queue.
             if (!mFirstTimeInitialized) {
                 mHandler.sendEmptyMessage(FIRST_TIME_INIT);
             } else {
                 initializeSecondTime();
             }
         }
         keepScreenOnAwhile();
 
         if (mPreviewing) {
             mOnResumeTime = SystemClock.uptimeMillis();
             mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration config) {
         super.onConfigurationChanged(config);
         changeHeadUpDisplayState();
     }
 
     @Override
     protected void onPause() {
         mPausing = true;
         stopPreview();
         // Close the camera now because other activities may need to use it.
         closeCamera();
         resetScreenOn();
         collapseCameraControls();
         changeHeadUpDisplayState();
 
         if (mFirstTimeInitialized) {
             mOrientationListener.disable();
             if (!mIsImageCaptureIntent) {
                 if (mThumbnailButton != null) {
                     mThumbnailButton.storeData(LAST_THUMB_PATH);
                 }
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
         mJpegImageData = null;
 
         // Remove the messages in the event queue.
         mHandler.removeMessages(RESTART_PREVIEW);
         mHandler.removeMessages(FIRST_TIME_INIT);
         mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
 
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
                 setResultEx(resultCode, intent);
                 finish();
 
                 File path = getFileStreamPath(sTempCropFilename);
                 path.delete();
 
                 break;
             }
         }
     }
 
     private boolean canTakePicture() {
         return isCameraIdle() && mPreviewing && (mPicturesRemaining > 0);
     }
 
     private void autoFocus() {
         // Initiate autofocus only when preview is started and snapshot is not
         // in progress.
         if (canTakePicture()) {
             enableCameraControls(false);
             Log.v(TAG, "Start autofocus.");
             mFocusStartTime = System.currentTimeMillis();
             mFocusState = FOCUSING;
             updateFocusIndicator();
             mCameraDevice.autoFocus(mAutoFocusCallback);
         }
     }
 
     private void cancelAutoFocus() {
         // User releases half-pressed focus key.
         if (mStatus != SNAPSHOT_IN_PROGRESS && (mFocusState == FOCUSING
                 || mFocusState == FOCUS_SUCCESS || mFocusState == FOCUS_FAIL)) {
             Log.v(TAG, "Cancel autofocus.");
             enableCameraControls(true);
             mCameraDevice.cancelAutoFocus();
         }
         if (mFocusState != FOCUSING_SNAP_ON_FINISH) {
             clearFocusState();
         }
     }
 
     private void clearFocusState() {
         mFocusState = FOCUS_NOT_STARTED;
         resetTouchFocus();
         updateFocusIndicator();
     }
 
     private void updateFocusIndicator() {
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
 
     // Preview area is touched. Handle touch focus.
     @Override
     public boolean onTouch(View v, MotionEvent e) {
         if (mPausing || !mFirstTimeInitialized || !canTakePicture()) {
             return false;
         }
 
         // Take a picture if metering area or focus area is supported.
         if (!isMeteringAreaSupported() && (!isFocusAreaSupported()
                 || (!mFocusMode.equals(Parameters.FOCUS_MODE_AUTO) &&
                     !mFocusMode.equals(Parameters.FOCUS_MODE_MACRO) &&
                     !mFocusMode.equals(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)))) {
             return false;
         }
 
         // Calculate the position of the focus rectangle.
         int x = Math.round(e.getX());
         int y = Math.round(e.getY());
         int focusWidth = mFocusRectangle.getWidth();
         int focusHeight = mFocusRectangle.getHeight();
         int left = Util.clamp(x - focusWidth / 2, 0,
                 mPreviewFrame.getWidth() - focusWidth);
         int top = Util.clamp(y - focusHeight / 2, 0,
                 mPreviewFrame.getHeight() - focusHeight);
         Log.d(TAG, "x=" + x + ". y=" + y);
         Log.d(TAG, "Margin left=" + left + " . Margin top=" + top);
         Log.d(TAG, "mPreviewFrame width=" + mPreviewFrame.getWidth() +
                 "mPreviewFrame.getHeight()="+mPreviewFrame.getHeight());
 
         // Convert the coordinates to driver format. The coordinates range from
         // -1000 to 1000.
         mFocusArea.left = (int) ((double) left / mPreviewFrame.getWidth() * 2000 - 1000);
         mFocusArea.top = (int) ((double) top / mPreviewFrame.getHeight() * 2000 - 1000);
         mFocusArea.right = (int) ((double) (left + focusWidth)
                 / mPreviewFrame.getWidth() * 2000 - 1000);
         mFocusArea.bottom = (int) ((double) (top + focusHeight)
                 / mPreviewFrame.getHeight() * 2000 - 1000);
 
         // Use margin to set the focus rectangle to the touched area.
         RelativeLayout.LayoutParams p =
                 (RelativeLayout.LayoutParams) mFocusRectangle.getLayoutParams();
         p.setMargins(left + mPreviewBorder.getPaddingLeft(),
                 top + mPreviewBorder.getPaddingTop(), 0, 0);
         // Disable "center" rule because we no longer want to put it in the center.
         int[] rules = p.getRules();
         rules[RelativeLayout.CENTER_IN_PARENT] = 0;
 
         // Set the focus area and take a picture.
         mTouchFocusEnabled = true;
         setCameraParameters(UPDATE_PARAM_PREFERENCE);
         doFocus(true);
         doSnap();
         doFocus(false);
 
         return true;
     }
 
     void resetTouchFocus() {
         // Put focus rectangle to the center.
         if (mTouchFocusEnabled) {
             RelativeLayout.LayoutParams p =
                     (RelativeLayout.LayoutParams) mFocusRectangle.getLayoutParams();
             int[] rules = p.getRules();
             rules[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
             p.setMargins(0, 0, 0, 0);
             mTouchFocusEnabled = false;
         }
     }
 
     boolean isFocusAreaSupported() {
         try {
             return mParameters.getInt("max-num-focus-areas") > 0;
         } catch (NumberFormatException e) {
         }
         return false;
     }
 
     void setAreasParameters() {
         if (!isFocusAreaSupported() && !isMeteringAreaSupported()) return;
 
         Rect rect;
         int weight;
         if (mTouchFocusEnabled) {
             rect = mFocusArea;
             weight = 1;
         } else {
             rect = new Rect();
             weight = 0;
         }
 
         if (isFocusAreaSupported()) {
             mParameters.set("focus-areas", "(" + rect.left + "," + rect.top + ","
                     + rect.right + "," + rect.bottom + "," + weight + ")");
             Log.d(TAG, "Parameter focus areas=" + mParameters.get("focus-areas"));
         }
 
         if (isMeteringAreaSupported()) {
             mParameters.set("metering-areas", "(" + rect.left + "," + rect.top + ","
                     + rect.right + "," + rect.bottom + "," + weight + ")");
         }
     }
 
     boolean isMeteringAreaSupported() {
         try {
             return mParameters.getInt("max-num-metering-areas") > 0;
         } catch (NumberFormatException e) {
         }
         return false;
     }
 
     @Override
     public void onBackPressed() {
         if (!isCameraIdle()) {
             // ignore backs while we're taking a picture
             return;
         } else if (!collapseCameraControls()) {
             super.onBackPressed();
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
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
                     if (collapseCameraControls()) return true;
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
 
     private void doSnap() {
         if (collapseCameraControls()) return;
 
         Log.v(TAG, "doSnap: mFocusState=" + mFocusState);
         // If the user has half-pressed the shutter and focus is completed, we
         // can take the photo right away. If the focus mode is infinity, we can
         // also take the photo.
         if (mFocusMode.equals(Parameters.FOCUS_MODE_INFINITY)
                 || mFocusMode.equals(Parameters.FOCUS_MODE_FIXED)
                 || mFocusMode.equals(Parameters.FOCUS_MODE_EDOF)
                 || (mFocusState == FOCUS_SUCCESS
                 || mFocusState == FOCUS_FAIL)) {
             capture();
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
         // Do the focus if the mode is not infinity.
         if (collapseCameraControls()) return;
         if (!(mFocusMode.equals(Parameters.FOCUS_MODE_INFINITY)
                   || mFocusMode.equals(Parameters.FOCUS_MODE_FIXED)
                   || mFocusMode.equals(Parameters.FOCUS_MODE_EDOF))) {
             if (pressed) {  // Focus key down.
                 autoFocus();
             } else {  // Focus key up.
                 cancelAutoFocus();
             }
         }
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Make sure we have a surface in the holder before proceeding.
         if (holder.getSurface() == null) {
             Log.d(TAG, "holder.getSurface() == null");
             return;
         }
 
         Log.v(TAG, "surfaceChanged. w=" + w + ". h=" + h);
 
         // We need to save the holder for later use, even when the mCameraDevice
         // is null. This could happen if onResume() is invoked after this
         // function.
         mSurfaceHolder = holder;
 
         // The mCameraDevice will be null if it fails to connect to the camera
         // hardware. In this case we will show a dialog and then finish the
         // activity, so it's OK to ignore it.
         if (mCameraDevice == null) return;
 
         // Sometimes surfaceChanged is called after onPause or before onResume.
         // Ignore it.
         if (mPausing || isFinishing()) return;
 
         // Set preview display if the surface is being created. Preview was
         // already started. Also restart the preview if display rotation has
         // changed. Sometimes this happens when the device is held in portrait
         // and camera app is opened. Rotation animation takes some time and
         // display rotation in onCreate may not be what we want.
         if (mPreviewing && (Util.getDisplayRotation(this) == mDisplayRotation)
                 && holder.isCreating()) {
             // Set preview display if the surface is being created and preview
             // was already started. That means preview display was set to null
             // and we need to set it now.
             setPreviewDisplay(holder);
         } else {
             // 1. Restart the preview if the size of surface was changed. The
             // framework may not support changing preview display on the fly.
             // 2. Start the preview now if surface was destroyed and preview
             // stopped.
             restartPreview();
         }
 
         // If first time initialization is not finished, send a message to do
         // it later. We want to finish surfaceChanged as soon as possible to let
         // user see preview first.
         if (!mFirstTimeInitialized) {
             mHandler.sendEmptyMessage(FIRST_TIME_INIT);
         } else {
             initializeSecondTime();
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         stopPreview();
         mSurfaceHolder = null;
     }
 
     private void closeCamera() {
         if (mCameraDevice != null) {
             CameraHolder.instance().release();
             mCameraDevice.setZoomChangeListener(null);
             mCameraDevice = null;
             mPreviewing = false;
         }
     }
 
     private void ensureCameraDevice() throws CameraHardwareException {
         if (mCameraDevice == null) {
             mCameraDevice = CameraHolder.instance().open(mCameraId);
             mInitialParams = mCameraDevice.getParameters();
         }
     }
 
     private void showCameraErrorAndFinish() {
         Resources ress = getResources();
         Util.showFatalErrorAndFinish(Camera.this,
                 ress.getString(R.string.camera_error_title),
                 ress.getString(R.string.cannot_connect_camera));
     }
 
     private boolean restartPreview() {
         try {
             startPreview();
         } catch (CameraHardwareException e) {
             showCameraErrorAndFinish();
             return false;
         }
         return true;
     }
 
     private void setPreviewDisplay(SurfaceHolder holder) {
         try {
             mCameraDevice.setPreviewDisplay(holder);
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("setPreviewDisplay failed", ex);
         }
     }
 
     private void startPreview() throws CameraHardwareException {
         if (mPausing || isFinishing()) return;
 
         resetTouchFocus();
 
         ensureCameraDevice();
         mCameraDevice.setErrorCallback(mErrorCallback);
 
         // If we're previewing already, stop the preview first (this will blank
         // the screen).
         if (mPreviewing) stopPreview();
 
         setPreviewDisplay(mSurfaceHolder);
         mDisplayRotation = Util.getDisplayRotation(this);
         Util.setCameraDisplayOrientation(mDisplayRotation, mCameraId, mCameraDevice);
         setCameraParameters(UPDATE_PARAM_ALL);
 
 
         try {
             Log.v(TAG, "startPreview");
             mCameraDevice.startPreview();
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("startPreview failed", ex);
         }
         mPreviewing = true;
         mZoomState = ZOOM_STOPPED;
         mStatus = IDLE;
     }
 
     private void stopPreview() {
         if (mCameraDevice != null && mPreviewing) {
             Log.v(TAG, "stopPreview");
             mCameraDevice.stopPreview();
         }
         mPreviewing = false;
         // If auto focus was in progress, it would have been canceled.
         clearFocusState();
     }
 
     private static boolean isSupported(String value, List<String> supported) {
         return supported == null ? false : supported.indexOf(value) >= 0;
     }
 
     private void updateCameraParametersInitialize() {
         // Reset preview frame rate to the maximum because it may be lowered by
         // video camera application.
         List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
         if (frameRates != null) {
             Integer max = Collections.max(frameRates);
             mParameters.setPreviewFrameRate(max);
         }
 
     }
 
     private void updateCameraParametersZoom() {
         // Set zoom.
         if (mParameters.isZoomSupported()) {
             mParameters.setZoom(mZoomValue);
         }
     }
 
     private void updateCameraParametersPreference() {
         setAreasParameters();
 
         // Set picture size.
         String pictureSize = mPreferences.getString(
                 CameraSettings.KEY_PICTURE_SIZE, null);
         if (pictureSize == null) {
             CameraSettings.initialCameraPictureSize(this, mParameters);
         } else {
             List<Size> supported = mParameters.getSupportedPictureSizes();
             CameraSettings.setCameraPictureSize(
                     pictureSize, supported, mParameters);
         }
 
         // Set the preview frame aspect ratio according to the picture size.
         Size size = mParameters.getPictureSize();
         PreviewFrameLayout frameLayout =
                 (PreviewFrameLayout) findViewById(R.id.frame_layout);
         frameLayout.setAspectRatio((double) size.width / size.height);
 
         // Set a preview size that is closest to the viewfinder height and has
         // the right aspect ratio.
         List<Size> sizes = mParameters.getSupportedPreviewSizes();
         Size optimalSize = Util.getOptimalPreviewSize(this,
                 sizes, (double) size.width / size.height);
         Size original = mParameters.getPreviewSize();
         if (!original.equals(optimalSize)) {
             mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
 
             // Zoom related settings will be changed for different preview
             // sizes, so set and read the parameters to get lastest values
             mCameraDevice.setParameters(mParameters);
             mParameters = mCameraDevice.getParameters();
         }
         Log.v(TAG, "Preview size is " + optimalSize.width + "x" + optimalSize.height);
 
         // Since change scene mode may change supported values,
         // Set scene mode first,
         mSceneMode = mPreferences.getString(
                 CameraSettings.KEY_SCENE_MODE,
                 getString(R.string.pref_camera_scenemode_default));
         if (isSupported(mSceneMode, mParameters.getSupportedSceneModes())) {
             if (!mParameters.getSceneMode().equals(mSceneMode)) {
                 mParameters.setSceneMode(mSceneMode);
                 mCameraDevice.setParameters(mParameters);
 
                 // Setting scene mode will change the settings of flash mode,
                 // white balance, and focus mode. Here we read back the
                 // parameters, so we can know those settings.
                 mParameters = mCameraDevice.getParameters();
             }
         } else {
             mSceneMode = mParameters.getSceneMode();
             if (mSceneMode == null) {
                 mSceneMode = Parameters.SCENE_MODE_AUTO;
             }
         }
 
         // Set JPEG quality.
         String jpegQuality = mPreferences.getString(
                 CameraSettings.KEY_JPEG_QUALITY,
                 getString(R.string.pref_camera_jpegquality_default));
         mParameters.setJpegQuality(JpegEncodingQualityMappings.getQualityNumber(jpegQuality));
 
         // For the following settings, we need to check if the settings are
         // still supported by latest driver, if not, ignore the settings.
 
         // Set color effect parameter.
         String colorEffect = mPreferences.getString(
                 CameraSettings.KEY_COLOR_EFFECT,
                 getString(R.string.pref_camera_coloreffect_default));
         if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
             mParameters.setColorEffect(colorEffect);
         }
 
         // Set exposure compensation
         String exposure = mPreferences.getString(
                 CameraSettings.KEY_EXPOSURE,
                 getString(R.string.pref_exposure_default));
         try {
             int value = Integer.parseInt(exposure);
             int max = mParameters.getMaxExposureCompensation();
             int min = mParameters.getMinExposureCompensation();
             if (value >= min && value <= max) {
                 mParameters.setExposureCompensation(value);
             } else {
                 Log.w(TAG, "invalid exposure range: " + exposure);
             }
         } catch (NumberFormatException e) {
             Log.w(TAG, "invalid exposure: " + exposure);
         }
 
         if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
             // Set flash mode.
             String flashMode = mPreferences.getString(
                     CameraSettings.KEY_FLASH_MODE,
                     getString(R.string.pref_camera_flashmode_default));
             List<String> supportedFlash = mParameters.getSupportedFlashModes();
             if (isSupported(flashMode, supportedFlash)) {
                 mParameters.setFlashMode(flashMode);
             } else {
                 flashMode = mParameters.getFlashMode();
                 if (flashMode == null) {
                     flashMode = getString(
                             R.string.pref_camera_flashmode_no_flash);
                 }
             }
 
             // Set white balance parameter.
             String whiteBalance = mPreferences.getString(
                     CameraSettings.KEY_WHITE_BALANCE,
                     getString(R.string.pref_camera_whitebalance_default));
             if (isSupported(whiteBalance,
                     mParameters.getSupportedWhiteBalance())) {
                 mParameters.setWhiteBalance(whiteBalance);
             } else {
                 whiteBalance = mParameters.getWhiteBalance();
                 if (whiteBalance == null) {
                     whiteBalance = Parameters.WHITE_BALANCE_AUTO;
                 }
             }
 
             // Set focus mode.
             mFocusMode = mPreferences.getString(
                     CameraSettings.KEY_FOCUS_MODE,
                     getString(R.string.pref_camera_focusmode_default));
             if (isSupported(mFocusMode, mParameters.getSupportedFocusModes())) {
                 mParameters.setFocusMode(mFocusMode);
             } else {
                 mFocusMode = mParameters.getFocusMode();
                 if (mFocusMode == null) {
                     mFocusMode = Parameters.FOCUS_MODE_AUTO;
                 }
             }
         } else {
             mFocusMode = mParameters.getFocusMode();
         }
     }
 
     // We separate the parameters into several subsets, so we can update only
     // the subsets actually need updating. The PREFERENCE set needs extra
     // locking because the preference can be changed from GLThread as well.
     private void setCameraParameters(int updateSet) {
         mParameters = mCameraDevice.getParameters();
 
         if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
             updateCameraParametersInitialize();
         }
 
         if ((updateSet & UPDATE_PARAM_ZOOM) != 0) {
             updateCameraParametersZoom();
         }
 
         if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
             updateCameraParametersPreference();
         }
 
         mCameraDevice.setParameters(mParameters);
     }
 
     // If the Camera is idle, update the parameters immediately, otherwise
     // accumulate them in mUpdateSet and update later.
     private void setCameraParametersWhenIdle(int additionalUpdateSet) {
         mUpdateSet |= additionalUpdateSet;
         if (mCameraDevice == null) {
             // We will update all the parameters when we open the device, so
             // we don't need to do anything now.
             mUpdateSet = 0;
             return;
         } else if (isCameraIdle()) {
             setCameraParameters(mUpdateSet);
             updateSceneModeUI();
             mUpdateSet = 0;
         } else {
             if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
                 mHandler.sendEmptyMessageDelayed(
                         SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
             }
         }
     }
 
     private void gotoGallery() {
         MenuHelper.gotoCameraImageGallery(this);
     }
 
     private void viewImage(RotateImageView view) {
         if(!view.isUriValid()) {
             Log.e(TAG, "Uri invalid. uri=" + view.getUri());
             return;
         }
 
         try {
             startActivity(new Intent(
                     Util.REVIEW_ACTION, view.getUri()));
         } catch (ActivityNotFoundException ex) {
             try {
                 startActivity(new Intent(
                         Intent.ACTION_VIEW, view.getUri()));
             } catch (ActivityNotFoundException e) {
                 Log.e(TAG, "review image fail. uri=" + view.getUri(), e);
             }
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
                 showGpsOnScreenIndicator(false);
            } catch (java.lang.SecurityException ex) {
                 Log.i(TAG, "fail to request location update, ignore", ex);
             } catch (IllegalArgumentException ex) {
                 Log.d(TAG, "provider does not exist " + ex.getMessage());
             }
             Log.d(TAG, "startReceivingLocationUpdates");
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
             Log.d(TAG, "stopReceivingLocationUpdates");
         }
         hideGpsOnScreenIndicator();
     }
 
     private Location getCurrentLocation() {
         // go in best to worst order
         for (int i = 0; i < mLocationListeners.length; i++) {
             Location l = mLocationListeners[i].current();
             if (l != null) return l;
         }
         Log.d(TAG, "No location received yet.");
         return null;
     }
 
     private boolean isCameraIdle() {
         return mStatus == IDLE && mFocusState == FOCUS_NOT_STARTED;
     }
 
     private boolean isImageCaptureIntent() {
         String action = getIntent().getAction();
         return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action));
     }
 
     private void setupCaptureParams() {
         Bundle myExtras = getIntent().getExtras();
         if (myExtras != null) {
             mSaveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
             mCropValue = myExtras.getString("crop");
         }
     }
 
     private void showPostCaptureAlert() {
         if (mIsImageCaptureIntent) {
             if (mIndicatorWheel == null) {
                 mShutterButton.setVisibility(View.INVISIBLE);
             } else {
                 mShutterButton.setEnabled(false);
             }
             int[] pickIds = {R.id.btn_retake, R.id.btn_done};
             for (int id : pickIds) {
                 View button = findViewById(id);
                 ((View) button.getParent()).setVisibility(View.VISIBLE);
             }
 
             // Remove the text of the cancel button
             View view = findViewById(R.id.btn_cancel);
             if (view instanceof Button) ((Button) view).setText("");
         }
     }
 
     private void hidePostCaptureAlert() {
         if (mIsImageCaptureIntent) {
             if (mIndicatorWheel == null) {
                 mShutterButton.setVisibility(View.VISIBLE);
             } else {
                 mShutterButton.setEnabled(true);
             }
             int[] pickIds = {R.id.btn_retake, R.id.btn_done};
             for (int id : pickIds) {
                 View button = findViewById(id);
                 ((View) button.getParent()).setVisibility(View.GONE);
             }
             enableCameraControls(true);
 
             // Restore the text of the cancel button
             View view = findViewById(R.id.btn_cancel);
             if (view instanceof Button) {
                 ((Button) view).setText(R.string.review_cancel);
             }
         }
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         // Only show the menu when camera is idle.
         for (int i = 0; i < menu.size(); i++) {
             menu.getItem(i).setVisible(isCameraIdle());
         }
 
         return true;
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
 
     private void addBaseMenuItems(Menu menu) {
         MenuHelper.addSwitchModeMenuItem(menu, true, new Runnable() {
             public void run() {
                 switchToVideoMode();
             }
         });
         MenuItem gallery = menu.add(Menu.NONE, Menu.NONE,
                 MenuHelper.POSITION_GOTO_GALLERY,
                 R.string.camera_gallery_photos_text)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
             public boolean onMenuItemClick(MenuItem item) {
                 gotoGallery();
                 return true;
             }
         });
         gallery.setIcon(android.R.drawable.ic_menu_gallery);
         mGalleryItems.add(gallery);
 
         if (mNumberOfCameras > 1) {
             menu.add(Menu.NONE, Menu.NONE,
                     MenuHelper.POSITION_SWITCH_CAMERA_ID,
                     R.string.switch_camera_id)
                     .setOnMenuItemClickListener(new OnMenuItemClickListener() {
                 public boolean onMenuItemClick(MenuItem item) {
                     CameraSettings.writePreferredCameraId(mPreferences,
                             ((mCameraId == mFrontCameraId)
                             ? mBackCameraId : mFrontCameraId));
                     onSharedPreferenceChanged();
                     return true;
                 }
             }).setIcon(android.R.drawable.ic_menu_camera);
         }
     }
 
     private boolean switchToVideoMode() {
         if (isFinishing() || !isCameraIdle()) return false;
         MenuHelper.gotoVideoMode(Camera.this);
         mHandler.removeMessages(FIRST_TIME_INIT);
         finish();
         return true;
     }
 
     public boolean onSwitchChanged(Switcher source, boolean onOff) {
         if (onOff == SWITCH_VIDEO) {
             return switchToVideoMode();
         } else {
             return true;
         }
     }
 
     private void onSharedPreferenceChanged() {
         // ignore the events after "onPause()"
         if (mPausing) return;
 
         boolean recordLocation;
 
         recordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
 
         if (mRecordLocation != recordLocation) {
             mRecordLocation = recordLocation;
             if (mRecordLocation) {
                 startReceivingLocationUpdates();
             } else {
                 stopReceivingLocationUpdates();
             }
         }
         int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
         if (mCameraId != cameraId) {
             // Restart the activity to have a crossfade animation.
             // TODO: Use SurfaceTexture to implement a better and faster
             // animation.
             if (mIsImageCaptureIntent) {
                 // If the intent is camera capture, stay in camera capture mode.
                 MenuHelper.gotoCameraMode(this, getIntent());
             } else {
                 MenuHelper.gotoCameraMode(this);
             }
 
             finish();
         } else {
             setCameraParametersWhenIdle(UPDATE_PARAM_PREFERENCE);
         }
     }
 
     @Override
     public void onUserInteraction() {
         super.onUserInteraction();
         keepScreenOnAwhile();
     }
 
     private void resetScreenOn() {
         mHandler.removeMessages(CLEAR_SCREEN_DELAY);
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
 
     private void keepScreenOnAwhile() {
         mHandler.removeMessages(CLEAR_SCREEN_DELAY);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
     }
 
     private class MyHeadUpDisplayListener implements HeadUpDisplay.Listener {
 
         public void onSharedPreferenceChanged() {
             Camera.this.onSharedPreferenceChanged();
         }
 
         public void onRestorePreferencesClicked() {
             Camera.this.onRestorePreferencesClicked();
         }
 
         public void onPopupWindowVisibilityChanged(int visibility) {
         }
     }
 
     protected void onRestorePreferencesClicked() {
         if (mPausing) return;
         Runnable runnable = new Runnable() {
             public void run() {
                 restorePreferences();
             }
         };
         MenuHelper.confirmAction(this,
                 getString(R.string.confirm_restore_title),
                 getString(R.string.confirm_restore_message),
                 runnable);
     }
 
     private void restorePreferences() {
         // Reset the zoom. Zoom value is not stored in preference.
         if (mParameters.isZoomSupported()) {
             mZoomValue = 0;
             setCameraParametersWhenIdle(UPDATE_PARAM_ZOOM);
             if (mZoomPicker != null) mZoomPicker.setZoomIndex(0);
         }
 
         if (mHeadUpDisplay != null) {
             mHeadUpDisplay.restorePreferences(mParameters);
         }
 
         if (mIndicatorWheel != null) {
             mIndicatorWheel.dismissSettingPopup();
             CameraSettings.restorePreferences(Camera.this, mPreferences,
                     mParameters);
             initializeIndicatorWheel();
             onSharedPreferenceChanged();
         }
     }
 
     protected void onOverriddenPreferencesClicked() {
         if (mPausing) return;
         if (mNotSelectableToast == null) {
             String str = getResources().getString(R.string.not_selectable_in_scene_mode);
             mNotSelectableToast = Toast.makeText(Camera.this, str, Toast.LENGTH_SHORT);
         }
         mNotSelectableToast.cancel();
         mNotSelectableToast.show();
     }
 
     private class MyIndicatorWheelListener implements IndicatorWheel.Listener {
         public void onSharedPreferenceChanged() {
             Camera.this.onSharedPreferenceChanged();
         }
 
         public void onRestorePreferencesClicked() {
             Camera.this.onRestorePreferencesClicked();
         }
 
         public void onOverriddenPreferencesClicked() {
             Camera.this.onOverriddenPreferencesClicked();
         }
     }
 
     private class MyCameraPickerListener implements CameraPicker.Listener {
         public void onSharedPreferenceChanged() {
             Camera.this.onSharedPreferenceChanged();
         }
     }
 }

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
 
 import com.android.camera.ui.CameraPicker;
 import com.android.camera.ui.FaceView;
 import com.android.camera.ui.IndicatorControlContainer;
 import com.android.camera.ui.RotateImageView;
 import com.android.camera.ui.RotateLayout;
 import com.android.camera.ui.SharePopup;
 import com.android.camera.ui.ZoomControl;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ContentProviderClient;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Face;
 import android.hardware.Camera.FaceDetectionListener;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.location.Location;
 import android.media.CameraProfile;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.os.MessageQueue;
 import android.os.SystemClock;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.AnimationUtils;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Formatter;
 import java.util.List;
 
 /** The Camera activity which can preview and take pictures. */
 public class Camera extends ActivityBase implements FocusManager.Listener,
         View.OnTouchListener, ShutterButton.OnShutterButtonListener,
         SurfaceHolder.Callback, ModePicker.OnModeChangeListener,
         FaceDetectionListener, CameraPreference.OnPreferenceChangedListener,
         LocationManager.Listener {
 
     private static final String TAG = "camera";
 
     private static final int CROP_MSG = 1;
     private static final int FIRST_TIME_INIT = 2;
     private static final int CLEAR_SCREEN_DELAY = 3;
     private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 4;
     private static final int CHECK_DISPLAY_ROTATION = 5;
     private static final int SHOW_TAP_TO_FOCUS_TOAST = 6;
     private static final int DISMISS_TAP_TO_FOCUS_TOAST = 7;
 
     // The subset of parameters we need to update in setCameraParameters().
     private static final int UPDATE_PARAM_INITIALIZE = 1;
     private static final int UPDATE_PARAM_ZOOM = 2;
     private static final int UPDATE_PARAM_PREFERENCE = 4;
     private static final int UPDATE_PARAM_ALL = -1;
 
     // When setCameraParametersWhenIdle() is called, we accumulate the subsets
     // needed to be updated in mUpdateSet.
     private int mUpdateSet;
 
     private static final int SCREEN_DELAY = 2 * 60 * 1000;
 
     private static final int ZOOM_STOPPED = 0;
     private static final int ZOOM_START = 1;
     private static final int ZOOM_STOPPING = 2;
 
     private int mZoomState = ZOOM_STOPPED;
     private boolean mSmoothZoomSupported = false;
     private int mZoomValue;  // The current zoom value.
     private int mZoomMax;
     private int mTargetZoomValue;
     private ZoomControl mZoomControl;
 
     private Parameters mParameters;
     private Parameters mInitialParams;
     private boolean mFocusAreaSupported;
     private boolean mMeteringAreaSupported;
    private boolean mAwbLockSupported;
    private boolean mAeLockSupported;
    private boolean mAeAwbLock;
 
     private MyOrientationEventListener mOrientationListener;
     // The degrees of the device rotated clockwise from its natural orientation.
     private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
     // The orientation compensation for icons and thumbnails. Ex: if the value
     // is 90, the UI components should be rotated 90 degrees counter-clockwise.
     private int mOrientationCompensation = 0;
     private ComboPreferences mPreferences;
 
     private static final String sTempCropFilename = "crop-temp";
 
     private android.hardware.Camera mCameraDevice;
     private ContentProviderClient mMediaProviderClient;
     private SurfaceHolder mSurfaceHolder = null;
     private ShutterButton mShutterButton;
     private GestureDetector mPopupGestureDetector;
     private boolean mOpenCameraFail = false;
     private boolean mCameraDisabled = false;
 
     private View mPreviewPanel;  // The container of PreviewFrameLayout.
     private PreviewFrameLayout mPreviewFrameLayout;
     private View mPreviewFrame;  // Preview frame area.
 
     // A popup window that contains a bigger thumbnail and a list of apps to share.
     private SharePopup mSharePopup;
     // The bitmap of the last captured picture thumbnail and the URI of the
     // original picture.
     private Thumbnail mThumbnail;
     // An imageview showing showing the last captured picture thumbnail.
     private RotateImageView mThumbnailView;
     private ModePicker mModePicker;
     private FaceView mFaceView;
     private RotateLayout mFocusIndicator;
 
     // mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.
     private String mCropValue;
     private Uri mSaveUri;
 
     // On-screen indicator
     private View mGpsNoSignalIndicator;
     private View mGpsHasSignalIndicator;
     private TextView mExposureIndicator;
 
     private final StringBuilder mBuilder = new StringBuilder();
     private final Formatter mFormatter = new Formatter(mBuilder);
     private final Object[] mFormatterArgs = new Object[1];
 
     /**
      * An unpublished intent flag requesting to return as soon as capturing
      * is completed.
      *
      * TODO: consider publishing by moving into MediaStore.
      */
     private final static String EXTRA_QUICK_CAPTURE =
             "android.intent.extra.quickCapture";
 
     // The display rotation in degrees. This is only valid when mCameraState is
     // not PREVIEW_STOPPED.
     private int mDisplayRotation;
     // The value for android.hardware.Camera.setDisplayOrientation.
     private int mDisplayOrientation;
     private boolean mPausing;
     private boolean mFirstTimeInitialized;
     private boolean mIsImageCaptureIntent;
 
     private static final int PREVIEW_STOPPED = 0;
     private static final int IDLE = 1;  // preview is active
     // Focus is in progress. The exact focus state is in Focus.java.
     private static final int FOCUSING = 2;
     private static final int SNAPSHOT_IN_PROGRESS = 3;
     private int mCameraState = PREVIEW_STOPPED;
 
     private ContentResolver mContentResolver;
     private boolean mDidRegister = false;
 
     private final ArrayList<MenuItem> mGalleryItems = new ArrayList<MenuItem>();
 
     private LocationManager mLocationManager;
 
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
 
     // This handles everything about focus.
     private FocusManager mFocusManager;
     private String mSceneMode;
     private Toast mNotSelectableToast;
     private Toast mNoShareToast;
 
     private final Handler mHandler = new MainHandler();
     private IndicatorControlContainer mIndicatorControlContainer;
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
                         startPreview();
                     }
                     if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                         mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
                     }
                     break;
                 }
 
                 case SHOW_TAP_TO_FOCUS_TOAST: {
                     showTapToFocusToast();
                     break;
                 }
 
                 case DISMISS_TAP_TO_FOCUS_TOAST: {
                     View v = findViewById(R.id.tap_to_focus_prompt);
                     v.setVisibility(View.GONE);
                     v.setAnimation(AnimationUtils.loadAnimation(Camera.this,
                             R.anim.on_screen_hint_exit));
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
             if (mIndicatorControlContainer != null) {
                 mIndicatorControlContainer.reloadPreferences();
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
         boolean recordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
         initOnScreenIndicator();
         mLocationManager.recordLocation(recordLocation);
 
         keepMediaProviderInstance();
         checkStorage();
 
         // Initialize last picture button.
         mContentResolver = getContentResolver();
         if (!mIsImageCaptureIntent) {  // no thumbnail in image capture intent
             initThumbnailButton();
         }
 
         // Initialize shutter button.
         mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
         mShutterButton.setOnShutterButtonListener(this);
         mShutterButton.setVisibility(View.VISIBLE);
 
         // Initialize focus UI.
         mPreviewFrame = findViewById(R.id.camera_preview);
         mPreviewFrame.setOnTouchListener(this);
         mFocusIndicator = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
         mFocusManager.initialize(mFocusIndicator, mPreviewFrame, mFaceView, this);
         mFocusManager.initializeToneGenerator();
         Util.initializeScreenBrightness(getWindow(), getContentResolver());
         installIntentFilter();
         initializeZoom();
         // Show the tap to focus toast if this is the first start.
         if (mFocusAreaSupported &&
                 mPreferences.getBoolean(CameraSettings.KEY_TAP_TO_FOCUS_PROMPT_SHOWN, true)) {
             // Delay the toast for one second to wait for orientation.
             mHandler.sendEmptyMessageDelayed(SHOW_TAP_TO_FOCUS_TOAST, 1000);
         }
 
         mFirstTimeInitialized = true;
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
         // Load the thumbnail from the disk.
         mThumbnail = Thumbnail.loadFrom(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
         updateThumbnailButton();
     }
 
     private void updateThumbnailButton() {
         // Update last image if URI is invalid and the storage is ready.
         if ((mThumbnail == null || !Util.isUriValid(mThumbnail.getUri(), mContentResolver))
                 && mPicturesRemaining >= 0) {
             mThumbnail = Thumbnail.getLastThumbnail(mContentResolver);
         }
         if (mThumbnail != null) {
             mThumbnailView.setBitmap(mThumbnail.getBitmap());
         } else {
             mThumbnailView.setBitmap(null);
         }
     }
 
     // If the activity is paused and resumed, this method will be called in
     // onResume.
     private void initializeSecondTime() {
         // Start orientation listener as soon as possible because it takes
         // some time to get first orientation.
         mOrientationListener.enable();
 
         // Start location update if needed.
         boolean recordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
         mLocationManager.recordLocation(recordLocation);
 
         installIntentFilter();
         mFocusManager.initializeToneGenerator();
         initializeZoom();
         keepMediaProviderInstance();
         checkStorage();
         hidePostCaptureAlert();
 
         if (!mIsImageCaptureIntent) {
             updateThumbnailButton();
             mModePicker.setCurrentMode(ModePicker.MODE_CAMERA);
         }
     }
 
     private class ZoomChangeListener implements ZoomControl.OnZoomChangedListener {
         // only for immediate zoom
         @Override
         public void onZoomValueChanged(int index) {
             Camera.this.onZoomValueChanged(index);
         }
 
         // only for smooth zoom
         @Override
         public void onZoomStateChanged(int state) {
             if (mPausing) return;
 
             Log.v(TAG, "zoom picker state=" + state);
             if (state == ZoomControl.ZOOM_IN) {
                 Camera.this.onZoomValueChanged(mZoomMax);
             } else if (state == ZoomControl.ZOOM_OUT) {
                 Camera.this.onZoomValueChanged(0);
             } else {
                 mTargetZoomValue = -1;
                 if (mZoomState == ZOOM_START) {
                     mZoomState = ZOOM_STOPPING;
                     mCameraDevice.stopSmoothZoom();
                 }
             }
         }
     }
 
     private void initializeZoom() {
         if (!mParameters.isZoomSupported()) return;
         mZoomMax = mParameters.getMaxZoom();
         // Currently we use immediate zoom for fast zooming to get better UX and
         // there is no plan to take advantage of the smooth zoom.
         mZoomControl.setZoomMax(mZoomMax);
         mZoomControl.setZoomIndex(mParameters.getZoom());
         mZoomControl.setSmoothZoomSupported(mSmoothZoomSupported);
         mZoomControl.setOnZoomChangeListener(new ZoomChangeListener());
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
 
     @Override
     public void startFaceDetection() {
         if (mParameters.getMaxNumDetectedFaces() > 0) {
             mFaceView = (FaceView) findViewById(R.id.face_view);
             mFaceView.clear();
             mFaceView.setVisibility(View.VISIBLE);
             mFaceView.setDisplayOrientation(mDisplayOrientation);
             CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
             mFaceView.setMirror(info.facing == CameraInfo.CAMERA_FACING_FRONT);
             mFaceView.resume();
             mCameraDevice.setFaceDetectionListener(this);
             mCameraDevice.startFaceDetection();
         }
     }
 
     @Override
     public void stopFaceDetection() {
         if (mParameters.getMaxNumDetectedFaces() > 0) {
             mCameraDevice.setFaceDetectionListener(null);
             mCameraDevice.stopFaceDetection();
             if (mFaceView != null) mFaceView.clear();
         }
     }
 
     private class PopupGestureListener
             extends GestureDetector.SimpleOnGestureListener {
         @Override
         public boolean onDown(MotionEvent e) {
             // Check if the popup window is visible.
             View popup = mIndicatorControlContainer.getActiveSettingPopup();
             if (popup == null) return false;
 
 
             // Let popup window, indicator control or preview frame handle the
             // event by themselves. Dismiss the popup window if users touch on
             // other areas.
             if (!Util.pointInView(e.getX(), e.getY(), popup)
                     && !Util.pointInView(e.getX(), e.getY(), mIndicatorControlContainer)
                     && !Util.pointInView(e.getX(), e.getY(), mPreviewFrame)) {
                 mIndicatorControlContainer.dismissSettingPopup();
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
 
     private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             Log.d(TAG, "Received intent action=" + action);
             if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                     || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                     || action.equals(Intent.ACTION_MEDIA_CHECKING)) {
                 checkStorage();
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                 checkStorage();
                 if (!mIsImageCaptureIntent) {
                     updateThumbnailButton();
                 }
             }
         }
     };
 
     private void initOnScreenIndicator() {
         mGpsNoSignalIndicator = findViewById(R.id.onscreen_gps_indicator_no_signal);
         mGpsHasSignalIndicator = findViewById(R.id.onscreen_gps_indicator_on);
         mExposureIndicator = (TextView) findViewById(R.id.onscreen_exposure_indicator);
     }
 
     @Override
     public void showGpsOnScreenIndicator(boolean hasSignal) {
         if (hasSignal) {
             if (mGpsNoSignalIndicator != null) {
                 mGpsNoSignalIndicator.setVisibility(View.GONE);
             }
             if (mGpsHasSignalIndicator != null) {
                 mGpsHasSignalIndicator.setVisibility(View.VISIBLE);
             }
         } else {
             if (mGpsNoSignalIndicator != null) {
                 mGpsNoSignalIndicator.setVisibility(View.VISIBLE);
             }
             if (mGpsHasSignalIndicator != null) {
                 mGpsHasSignalIndicator.setVisibility(View.GONE);
             }
         }
     }
 
     @Override
     public void hideGpsOnScreenIndicator() {
         if (mGpsNoSignalIndicator != null) mGpsNoSignalIndicator.setVisibility(View.GONE);
         if (mGpsHasSignalIndicator != null) mGpsHasSignalIndicator.setVisibility(View.GONE);
     }
 
     private void updateExposureOnScreenIndicator(int value) {
         if (mExposureIndicator == null) return;
 
         if (value == 0) {
             mExposureIndicator.setText("");
             mExposureIndicator.setVisibility(View.GONE);
         } else {
             float step = mParameters.getExposureCompensationStep();
             mFormatterArgs[0] = value * step;
             mBuilder.delete(0, mBuilder.length());
             mFormatter.format("%+1.1f", mFormatterArgs);
             String exposure = mFormatter.toString();
             mExposureIndicator.setText(exposure);
             mExposureIndicator.setVisibility(View.VISIBLE);
         }
     }
 
     private final class ShutterCallback
             implements android.hardware.Camera.ShutterCallback {
         public void onShutter() {
             mShutterCallbackTime = System.currentTimeMillis();
             mShutterLag = mShutterCallbackTime - mCaptureStartTime;
             Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
             mFocusManager.onShutter();
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
 
                 startPreview();
             }
 
             if (!mIsImageCaptureIntent) {
                 storeImage(jpegData, mLocation);
             } else {
                 mJpegImageData = jpegData;
                 if (!mQuickCapture) {
                     showPostCaptureAlert();
                 } else {
                     doAttach();
                 }
             }
 
             // Check this in advance of each shot so we don't add to shutter
             // latency. It's true that someone else could write to the SD card in
             // the mean time and fill it, but that could have happened between the
             // shutter press and saving the JPEG too.
             checkStorage();
 
             long now = System.currentTimeMillis();
             mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
             Log.v(TAG, "mJpegCallbackFinishTime = "
                     + mJpegCallbackFinishTime + "ms");
             mJpegPictureCallbackTime = 0;
         }
     }
 
     private final class AutoFocusCallback
             implements android.hardware.Camera.AutoFocusCallback {
         public void onAutoFocus(
                 boolean focused, android.hardware.Camera camera) {
             if (mPausing) return;
 
             mAutoFocusTime = System.currentTimeMillis() - mFocusStartTime;
             Log.v(TAG, "mAutoFocusTime = " + mAutoFocusTime + "ms");
             mFocusManager.onAutoFocus(focused);
             // If focus completes and the snapshot is not started, enable the
             // controls.
             if (mFocusManager.isFocusCompleted()) {
                 enableCameraControls(true);
             }
         }
     }
 
     private final class ZoomListener
             implements android.hardware.Camera.OnZoomChangeListener {
         @Override
         public void onZoomChange(
                 int value, boolean stopped, android.hardware.Camera camera) {
             Log.v(TAG, "Zoom changed: value=" + value + ". stopped="+ stopped);
             mZoomValue = value;
 
             // Update the UI when we get zoom value.
             mZoomControl.setZoomIndex(value);
 
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
 
     private void storeImage(final byte[] data, Location loc) {
         long dateTaken = System.currentTimeMillis();
         String title = Util.createJpegName(dateTaken);
         int orientation = Exif.getOrientation(data);
         Uri uri = Storage.addImage(mContentResolver, title, dateTaken,
                 loc, orientation, data);
         if (uri != null) {
             // Create a thumbnail whose width is equal or bigger than that of the preview.
             int ratio = (int) Math.ceil((double) mParameters.getPictureSize().width
                     / mPreviewFrameLayout.getWidth());
             int inSampleSize = Integer.highestOneBit(ratio);
             mThumbnail = Thumbnail.createThumbnail(data, orientation, inSampleSize, uri);
             if (mThumbnail != null) {
                 mThumbnailView.setBitmap(mThumbnail.getBitmap());
             }
             Util.broadcastNewPicture(this, uri);
         }
     }
 
     @Override
     public boolean capture() {
         // If we are already in the middle of taking a snapshot then ignore.
         if (mCameraState == SNAPSHOT_IN_PROGRESS || mCameraDevice == null) {
             return false;
         }
         mCaptureStartTime = System.currentTimeMillis();
         mPostViewPictureCallbackTime = 0;
         enableCameraControls(false);
         mJpegImageData = null;
 
         // Set rotation and gps data.
         Util.setRotationParameter(mParameters, mCameraId, mOrientation);
         Location loc = mLocationManager.getCurrentLocation();
         Util.setGpsParameters(mParameters, loc);
         mCameraDevice.setParameters(mParameters);
 
         mCameraDevice.takePicture(mShutterCallback, mRawPictureCallback,
                 mPostViewPictureCallback, new JpegPictureCallback(loc));
         mCameraState = SNAPSHOT_IN_PROGRESS;
         return true;
     }
 
     @Override
     public void setFocusParameters() {
         setCameraParameters(UPDATE_PARAM_PREFERENCE);
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
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         mIsImageCaptureIntent = isImageCaptureIntent();
         setContentView(R.layout.camera);
         if (mIsImageCaptureIntent) {
             findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
         } else {
             mThumbnailView = (RotateImageView) findViewById(R.id.thumbnail);
             mThumbnailView.setVisibility(View.VISIBLE);
         }
 
         mPreferences = new ComboPreferences(this);
         CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
         mFocusManager = new FocusManager(mPreferences,
                 getString(R.string.pref_camera_focusmode_default));
 
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
                     mCameraDevice = Util.openCamera(Camera.this, mCameraId);
                     initializeCapabilities();
                     startPreview();
                 } catch (CameraHardwareException e) {
                     mOpenCameraFail = true;
                 } catch (CameraDisabledException e) {
                     mCameraDisabled = true;
                 }
             }
         });
         startPreviewThread.start();
 
         Util.enterLightsOutMode(getWindow());
 
         // don't set mSurfaceHolder here. We have it set ONLY within
         // surfaceChanged / surfaceDestroyed, other parts of the code
         // assume that when it is set, the surface is also set.
         SurfaceView preview = (SurfaceView) findViewById(R.id.camera_preview);
         SurfaceHolder holder = preview.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         if (mIsImageCaptureIntent) {
             setupCaptureParams();
         } else {
             mModePicker = (ModePicker) findViewById(R.id.mode_picker);
             mModePicker.setVisibility(View.VISIBLE);
             mModePicker.setOnModeChangeListener(this);
             mModePicker.setCurrentMode(ModePicker.MODE_CAMERA);
         }
 
         mZoomControl = (ZoomControl) findViewById(R.id.zoom_control);
         mLocationManager = new LocationManager(this, this);
 
         // Make sure preview is started.
         try {
             startPreviewThread.join();
             if (mOpenCameraFail) {
                 Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
                 return;
             } else if (mCameraDisabled) {
                 Util.showErrorAndFinish(this, R.string.camera_disabled);
                 return;
             }
         } catch (InterruptedException ex) {
             // ignore
         }
 
         mBackCameraId = CameraHolder.instance().getBackCameraId();
         mFrontCameraId = CameraHolder.instance().getFrontCameraId();
 
         // Do this after starting preview because it depends on camera
         // parameters.
         initializeIndicatorControl();
     }
 
     private void overrideCameraSettings(final String flashMode,
             final String whiteBalance, final String focusMode) {
         if (mIndicatorControlContainer != null) {
             mIndicatorControlContainer.overrideSettings(
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
 
     private void initializeIndicatorControl() {
         // setting the indicator buttons.
         mIndicatorControlContainer =
                 (IndicatorControlContainer) findViewById(R.id.indicator_control);
         if (mIndicatorControlContainer == null) return;
         loadCameraPreferences();
         final String[] SETTING_KEYS = {
                 CameraSettings.KEY_FLASH_MODE,
                 CameraSettings.KEY_WHITE_BALANCE,
                 CameraSettings.KEY_SCENE_MODE};
         final String[] OTHER_SETTING_KEYS = {
                 CameraSettings.KEY_RECORD_LOCATION,
                 CameraSettings.KEY_FOCUS_MODE,
                 CameraSettings.KEY_EXPOSURE,
                 CameraSettings.KEY_PICTURE_SIZE};
 
         CameraPicker.setImageResourceId(R.drawable.ic_switch_photo_facing_holo_light);
         mIndicatorControlContainer.initialize(this, mPreferenceGroup,
                 mParameters.isZoomSupported(),
                 SETTING_KEYS, OTHER_SETTING_KEYS);
         updateSceneModeUI();
         mIndicatorControlContainer.setListener(this);
     }
 
     private boolean collapseCameraControls() {
         if ((mIndicatorControlContainer != null)
                 && mIndicatorControlContainer.dismissSettingPopup()) {
             return true;
         }
         return false;
     }
 
     private void enableCameraControls(boolean enable) {
         if (mIndicatorControlContainer != null) {
             mIndicatorControlContainer.setEnabled(enable);
         }
         if (mModePicker != null) mModePicker.setEnabled(enable);
         if (mZoomControl != null) mZoomControl.setEnabled(enable);
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
                 setOrientationIndicator(mOrientationCompensation);
             }
 
             // Show the toast after getting the first orientation changed.
             if (mHandler.hasMessages(SHOW_TAP_TO_FOCUS_TOAST)) {
                 mHandler.removeMessages(SHOW_TAP_TO_FOCUS_TOAST);
                 showTapToFocusToast();
             }
         }
     }
 
     private void setOrientationIndicator(int degree) {
         if (mThumbnailView != null) mThumbnailView.setDegree(degree);
         if (mModePicker != null) mModePicker.setDegree(degree);
         if (mSharePopup != null) mSharePopup.setOrientation(degree);
         if (mIndicatorControlContainer != null) mIndicatorControlContainer.setDegree(degree);
         if (mZoomControl != null) mZoomControl.setDegree(degree);
         if (mFocusIndicator != null) mFocusIndicator.setOrientation(degree);
         if (mFaceView != null) mFaceView.setOrientation(degree);
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
 
     @OnClickAttr
     public void onThumbnailClicked(View v) {
         if (isCameraIdle() && mThumbnail != null) {
             showSharePopup();
         }
     }
 
     @OnClickAttr
     public void onRetakeButtonClicked(View v) {
         hidePostCaptureAlert();
         startPreview();
     }
 
     @OnClickAttr
     public void onDoneButtonClicked(View v) {
         doAttach();
     }
 
     @OnClickAttr
     public void onCancelButtonClicked(View v) {
         doCancel();
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
         switch (button.getId()) {
             case R.id.shutter_button:
                 doFocus(pressed);
                 break;
         }
     }
 
     public void onShutterButtonClick(ShutterButton button) {
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
 
     @Override
     protected void onResume() {
         super.onResume();
         mPausing = false;
         if (mOpenCameraFail || mCameraDisabled) return;
 
         mJpegPictureCallbackTime = 0;
         mZoomValue = 0;
 
         // Start the preview if it is not started.
         if (mCameraState == PREVIEW_STOPPED) {
             try {
                 mCameraDevice = Util.openCamera(this, mCameraId);
                 initializeCapabilities();
                 resetExposureCompensation();
                 startPreview();
             } catch(CameraHardwareException e) {
                 Util.showErrorAndFinish(this, R.string.cannot_connect_camera);
                 return;
             } catch(CameraDisabledException e) {
                 Util.showErrorAndFinish(this, R.string.camera_disabled);
                 return;
             }
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
 
         if (mCameraState == IDLE) {
             mOnResumeTime = SystemClock.uptimeMillis();
             mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
         }
     }
 
     @Override
     protected void onPause() {
         mPausing = true;
         stopPreview();
         // Close the camera now because other activities may need to use it.
         closeCamera();
         resetScreenOn();
 
         // Clear UI.
         collapseCameraControls();
         if (mSharePopup != null) mSharePopup.dismiss();
         if (mFaceView != null) mFaceView.clear();
 
         if (mFirstTimeInitialized) {
             mOrientationListener.disable();
             if (!mIsImageCaptureIntent) {
                 if (mThumbnail != null) {
                     mThumbnail.saveTo(new File(getFilesDir(), Thumbnail.LAST_THUMB_FILENAME));
                 }
             }
         }
 
         if (mDidRegister) {
             unregisterReceiver(mReceiver);
             mDidRegister = false;
         }
         mLocationManager.recordLocation(false);
         updateExposureOnScreenIndicator(0);
 
         mFocusManager.releaseToneGenerator();
 
         if (mStorageHint != null) {
             mStorageHint.cancel();
             mStorageHint = null;
         }
 
         // If we are in an image capture intent and has taken
         // a picture, we just clear it in onPause.
         mJpegImageData = null;
 
         // Remove the messages in the event queue.
         mHandler.removeMessages(FIRST_TIME_INIT);
         mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
         mFocusManager.removeMessages();
 
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
         return isCameraIdle() && (mPicturesRemaining > 0);
     }
 
     @Override
     public void autoFocus() {
         mFocusStartTime = System.currentTimeMillis();
         mCameraDevice.autoFocus(mAutoFocusCallback);
         mCameraState = FOCUSING;
         enableCameraControls(false);
     }
 
     @Override
     public void cancelAutoFocus() {
         mCameraDevice.cancelAutoFocus();
         mCameraState = IDLE;
         enableCameraControls(true);
         setCameraParameters(UPDATE_PARAM_PREFERENCE);
     }
 
     // Preview area is touched. Handle touch focus.
     @Override
     public boolean onTouch(View v, MotionEvent e) {
         if (mPausing || mCameraDevice == null || !mFirstTimeInitialized
                 || mCameraState == SNAPSHOT_IN_PROGRESS) {
             return false;
         }
 
         // Do not trigger touch focus if popup window is opened.
         if (collapseCameraControls()) return false;
 
         // Check if metering area or focus area is supported.
         if (!mFocusAreaSupported && !mMeteringAreaSupported) return false;
 
         return mFocusManager.onTouch(e);
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
         if (mPausing || collapseCameraControls()) return;
 
         // Do not take the picture if there is not enough storage.
         if (mPicturesRemaining <= 0) {
             Log.i(TAG, "Not enough space or storage not ready. remaining=" + mPicturesRemaining);
             return;
         }
 
         Log.v(TAG, "doSnap: mCameraState=" + mCameraState);
         mFocusManager.doSnap();
     }
 
     private void doFocus(boolean pressed) {
         if (mPausing || collapseCameraControls()) return;
 
         // Do not do focus if there is not enough storage.
         if (pressed && !canTakePicture()) return;
 
        // Lock AE and AWB so users can half-press shutter and recompose.
        mAeAwbLock = pressed;
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
         mFocusManager.doFocus(pressed);
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
         if (mCameraState != PREVIEW_STOPPED
                 && (Util.getDisplayRotation(this) == mDisplayRotation)
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
             startPreview();
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
             mCameraDevice.setFaceDetectionListener(null);
             mCameraDevice = null;
             mCameraState = PREVIEW_STOPPED;
             mFocusManager.onCameraReleased();
         }
     }
 
     private void setPreviewDisplay(SurfaceHolder holder) {
         try {
             mCameraDevice.setPreviewDisplay(holder);
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("setPreviewDisplay failed", ex);
         }
     }
 
     private void startPreview() {
         if (mPausing || isFinishing()) return;
 
         mFocusManager.resetTouchFocus();
 
         mCameraDevice.setErrorCallback(mErrorCallback);
 
         // If we're previewing already, stop the preview first (this will blank
         // the screen).
         if (mCameraState != PREVIEW_STOPPED) stopPreview();
 
         setPreviewDisplay(mSurfaceHolder);
         mDisplayRotation = Util.getDisplayRotation(this);
         mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, mCameraId);
         mCameraDevice.setDisplayOrientation(mDisplayOrientation);
         if (mFaceView != null) {
             mFaceView.setDisplayOrientation(mDisplayOrientation);
         }
        mAeAwbLock = false; // Always unlock AE and AWB before start.
         setCameraParameters(UPDATE_PARAM_ALL);
 
         try {
             Log.v(TAG, "startPreview");
             mCameraDevice.startPreview();
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("startPreview failed", ex);
         }
 
         startFaceDetection();
         mZoomState = ZOOM_STOPPED;
         mCameraState = IDLE;
         mFocusManager.onPreviewStarted();
     }
 
     private void stopPreview() {
         if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
             Log.v(TAG, "stopPreview");
             mCameraDevice.stopPreview();
         }
         mCameraState = PREVIEW_STOPPED;
         mFocusManager.onPreviewStopped();
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
 
         mParameters.setRecordingHint(false);
     }
 
     private void updateCameraParametersZoom() {
         // Set zoom.
         if (mParameters.isZoomSupported()) {
             mParameters.setZoom(mZoomValue);
         }
     }
 
     private void updateCameraParametersPreference() {
        if (mAwbLockSupported) {
            mParameters.setAutoWhiteBalanceLock(mAeAwbLock);
        }

        if (mAeLockSupported) {
            mParameters.setAutoExposureLock(mAeAwbLock);
        }

         if (mFocusAreaSupported) {
             mParameters.setFocusAreas(mFocusManager.getTapArea());
         }
 
         if (mMeteringAreaSupported) {
             // Use the same area for focus and metering.
             mParameters.setMeteringAreas(mFocusManager.getTapArea());
         }
 
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
 
         mPreviewPanel = findViewById(R.id.frame_layout);
         mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
         mPreviewFrameLayout.setAspectRatio((double) size.width / size.height);
 
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
         int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                 CameraProfile.QUALITY_HIGH);
         mParameters.setJpegQuality(jpegQuality);
 
         // For the following settings, we need to check if the settings are
         // still supported by latest driver, if not, ignore the settings.
 
         // Set exposure compensation
         int value = CameraSettings.readExposure(mPreferences);
         int max = mParameters.getMaxExposureCompensation();
         int min = mParameters.getMinExposureCompensation();
         if (value >= min && value <= max) {
             mParameters.setExposureCompensation(value);
         } else {
             Log.w(TAG, "invalid exposure range: " + value);
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
             mFocusManager.overrideFocusMode(null);
             mParameters.setFocusMode(mFocusManager.getFocusMode());
         } else {
             mFocusManager.overrideFocusMode(mParameters.getFocusMode());
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
 
     private boolean isCameraIdle() {
         return (mCameraState == IDLE) || (mFocusManager.isFocusCompleted());
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
             Util.fadeOut(mIndicatorControlContainer);
             Util.fadeOut(mShutterButton);
 
             int[] pickIds = {R.id.btn_retake, R.id.btn_done};
             for (int id : pickIds) {
                 Util.fadeIn(findViewById(id));
             }
         }
     }
 
     private void hidePostCaptureAlert() {
         if (mIsImageCaptureIntent) {
             enableCameraControls(true);
 
             int[] pickIds = {R.id.btn_retake, R.id.btn_done};
             for (int id : pickIds) {
                 Util.fadeOut(findViewById(id));
             }
 
             Util.fadeIn(mShutterButton);
             Util.fadeIn(mIndicatorControlContainer);
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
         MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_VIDEO, new Runnable() {
             public void run() {
                 switchToOtherMode(ModePicker.MODE_VIDEO);
             }
         });
         MenuHelper.addSwitchModeMenuItem(menu, ModePicker.MODE_PANORAMA, new Runnable() {
             public void run() {
                 switchToOtherMode(ModePicker.MODE_PANORAMA);
             }
         });
         MenuItem gallery = menu.add(R.string.camera_gallery_photos_text)
                 .setOnMenuItemClickListener(new OnMenuItemClickListener() {
             public boolean onMenuItemClick(MenuItem item) {
                 gotoGallery();
                 return true;
             }
         });
         gallery.setIcon(android.R.drawable.ic_menu_gallery);
         mGalleryItems.add(gallery);
 
         if (mNumberOfCameras > 1) {
             menu.add(R.string.switch_camera_id)
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
 
     private boolean switchToOtherMode(int mode) {
         if (isFinishing() || !isCameraIdle()) return false;
         MenuHelper.gotoMode(mode, Camera.this);
         mHandler.removeMessages(FIRST_TIME_INIT);
         finish();
         return true;
     }
 
     public boolean onModeChanged(int mode) {
         if (mode != ModePicker.MODE_CAMERA) {
             return switchToOtherMode(mode);
         } else {
             return true;
         }
     }
 
     public void onSharedPreferenceChanged() {
         // ignore the events after "onPause()"
         if (mPausing) return;
 
         boolean recordLocation = RecordLocationPreference.get(
                 mPreferences, getContentResolver());
         mLocationManager.recordLocation(recordLocation);
 
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
 
         int exposureValue = CameraSettings.readExposure(mPreferences);
         updateExposureOnScreenIndicator(exposureValue);
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
 
     public void onRestorePreferencesClicked() {
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
             mZoomControl.setZoomIndex(0);
         }
         if (mIndicatorControlContainer != null) {
             mIndicatorControlContainer.dismissSettingPopup();
             CameraSettings.restorePreferences(Camera.this, mPreferences,
                     mParameters);
             mIndicatorControlContainer.reloadPreferences();
             onSharedPreferenceChanged();
         }
     }
 
     public void onOverriddenPreferencesClicked() {
         if (mPausing) return;
         if (mNotSelectableToast == null) {
             String str = getResources().getString(R.string.not_selectable_in_scene_mode);
             mNotSelectableToast = Toast.makeText(Camera.this, str, Toast.LENGTH_SHORT);
         }
         mNotSelectableToast.show();
     }
 
     private void showSharePopup() {
         Uri uri = mThumbnail.getUri();
         if (mSharePopup == null || !uri.equals(mSharePopup.getUri())) {
             // SharePopup window takes the mPreviewPanel as its size reference.
             mSharePopup = new SharePopup(this, uri, mThumbnail.getBitmap(),
                     mOrientationCompensation, mPreviewPanel);
         }
         mSharePopup.showAtLocation(mThumbnailView, Gravity.NO_GRAVITY, 0, 0);
     }
 
     @Override
     public void onFaceDetection(Face[] faces, android.hardware.Camera camera) {
         mFaceView.setFaces(faces);
     }
 
     private void showTapToFocusToast() {
         // Show the toast.
         RotateLayout v = (RotateLayout) findViewById(R.id.tap_to_focus_prompt);
         v.setOrientation(mOrientationCompensation);
         v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.on_screen_hint_enter));
         v.setVisibility(View.VISIBLE);
         mHandler.sendEmptyMessageDelayed(DISMISS_TAP_TO_FOCUS_TOAST, 5000);
         // Clear the preference.
         Editor editor = mPreferences.edit();
         editor.putBoolean(CameraSettings.KEY_TAP_TO_FOCUS_PROMPT_SHOWN, false);
         editor.apply();
     }
 
     private void initializeCapabilities() {
         mInitialParams = mCameraDevice.getParameters();
         mFocusManager.initializeParameters(mInitialParams);
         mFocusAreaSupported = (mInitialParams.getMaxNumFocusAreas() > 0
                 && isSupported(Parameters.FOCUS_MODE_AUTO,
                         mInitialParams.getSupportedFocusModes()));
         mMeteringAreaSupported = (mInitialParams.getMaxNumMeteringAreas() > 0);
        mAwbLockSupported = mInitialParams.isAutoWhiteBalanceLockSupported();
        mAeLockSupported = mInitialParams.isAutoExposureLockSupported();
     }
 }

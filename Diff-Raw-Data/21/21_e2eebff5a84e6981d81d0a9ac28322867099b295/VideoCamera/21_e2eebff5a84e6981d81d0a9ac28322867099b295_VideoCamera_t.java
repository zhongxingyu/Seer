 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.android.camera;
 
 import com.android.camera.ui.CamcorderHeadUpDisplay;
 import com.android.camera.ui.CameraPicker;
 import com.android.camera.ui.GLRootView;
 import com.android.camera.ui.HeadUpDisplay;
 import com.android.camera.ui.IndicatorWheel;
 
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.media.ThumbnailUtils;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.os.SystemClock;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.provider.MediaStore.Video;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The Camcorder activity.
  */
 public class VideoCamera extends ActivityBase
         implements View.OnClickListener,
         ShutterButton.OnShutterButtonListener, SurfaceHolder.Callback,
         MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener,
         Switcher.OnSwitchListener, PreviewFrameLayout.OnSizeChangedListener {
 
     private static final String TAG = "videocamera";
 
     private static final String LAST_THUMB_PATH =
             Storage.THUMBNAILS + "/video_last_thumb";
 
     private static final int CHECK_DISPLAY_ROTATION = 3;
     private static final int CLEAR_SCREEN_DELAY = 4;
     private static final int UPDATE_RECORD_TIME = 5;
     private static final int ENABLE_SHUTTER_BUTTON = 6;
 
     private static final int SCREEN_DELAY = 2 * 60 * 1000;
 
     // The brightness settings used when it is set to automatic in the system.
     // The reason why it is set to 0.7 is just because 1.0 is too bright.
     private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;
 
     private static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;
 
     private static final boolean SWITCH_CAMERA = true;
     private static final boolean SWITCH_VIDEO = false;
 
     private static final long SHUTTER_BUTTON_TIMEOUT = 500L; // 500ms
 
     private static final int[] TIME_LAPSE_VIDEO_QUALITY = {
             CamcorderProfile.QUALITY_TIME_LAPSE_1080P,
             CamcorderProfile.QUALITY_TIME_LAPSE_720P,
             CamcorderProfile.QUALITY_TIME_LAPSE_480P,
             CamcorderProfile.QUALITY_TIME_LAPSE_CIF,
             CamcorderProfile.QUALITY_TIME_LAPSE_QCIF};
 
     private static final int[] VIDEO_QUALITY = {
             CamcorderProfile.QUALITY_1080P,
             CamcorderProfile.QUALITY_720P,
             CamcorderProfile.QUALITY_480P,
             CamcorderProfile.QUALITY_CIF,
             CamcorderProfile.QUALITY_QCIF};
 
     /**
      * An unpublished intent flag requesting to start recording straight away
      * and return as soon as recording is stopped.
      * TODO: consider publishing by moving into MediaStore.
      */
     private final static String EXTRA_QUICK_CAPTURE =
             "android.intent.extra.quickCapture";
 
     private android.hardware.Camera mCameraDevice;
     private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
 
     private ComboPreferences mPreferences;
     private PreferenceGroup mPreferenceGroup;
 
     private PreviewFrameLayout mPreviewFrameLayout;
     private SurfaceView mVideoPreview;
     private SurfaceHolder mSurfaceHolder = null;
     private ImageView mVideoFrame;
     private GLRootView mGLRootView;
     // xlarge devices use indicator wheel. Other devices use head-up display.
     private CamcorderHeadUpDisplay mHeadUpDisplay;
     private IndicatorWheel mIndicatorWheel;
     // Front/back camera picker for xlarge layout.
     private CameraPicker mCameraPicker;
     private View mReviewControl;
 
     private boolean mIsVideoCaptureIntent;
     private boolean mQuickCapture;
 
     // The last recorded video.
     private RotateImageView mThumbnailButton;
 
     private boolean mOpenCameraFail = false;
 
     private long mStorageSpace;
 
     private MediaRecorder mMediaRecorder;
     private boolean mMediaRecorderRecording = false;
     private long mRecordingStartTime;
     private long mOnResumeTime;
     // The video file that the hardware camera is about to record into
     // (or is recording into.)
     private String mVideoFilename;
     private ParcelFileDescriptor mVideoFileDescriptor;
 
     // The video file that has already been recorded, and that is being
     // examined by the user.
     private String mCurrentVideoFilename;
     private Uri mCurrentVideoUri;
     private ContentValues mCurrentVideoValues;
 
     private CamcorderProfile mProfile;
     // The array of video quality profiles supported by each camera(s). Here the
     // cameraId is the index of the array to get the profile map which contain
     // the set of quality string and its real quality of a camera.
     private HashMap<String, Integer>[] mProfileQuality;
     private HashMap<String, Integer>[] mTimeLapseProfileQuality;
 
     // The video duration limit. 0 menas no limit.
     private int mMaxVideoDurationInMs;
 
     // Time Lapse parameters.
     private boolean mCaptureTimeLapse = false;
     // Default 0. If it is larger than 0, the camcorder is in time lapse mode.
     private int mTimeBetweenTimeLapseFrameCaptureMs = 0;
     private View mTimeLapseLabel;
     private View mPreviewBorder;
 
     private int mDesiredPreviewWidth;
     private int mDesiredPreviewHeight;
 
     boolean mPausing = false;
     boolean mPreviewing = false; // True if preview is started.
     // The display rotation in degrees. This is only valid when mPreviewing is
     // true.
     private int mDisplayRotation;
 
     private ContentResolver mContentResolver;
 
     private ShutterButton mShutterButton;
     private TextView mRecordingTimeView;
     private SwitcherSet mSwitcher;
     private boolean mRecordingTimeCountsDown = false;
 
     private final ArrayList<MenuItem> mGalleryItems = new ArrayList<MenuItem>();
 
     private final Handler mHandler = new MainHandler();
     private Parameters mParameters;
 
     // multiple cameras support
     private int mNumberOfCameras;
     private int mCameraId;
     private int mFrontCameraId;
     private int mBackCameraId;
 
     private GestureDetector mPopupGestureDetector;
 
     private MyOrientationEventListener mOrientationListener;
     // The device orientation in degrees. Default is unknown.
     private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
     // The orientation compensation for icons and thumbnails.
     private int mOrientationCompensation = 0;
     private int mOrientationHint; // the orientation hint for video playback
 
     // This Handler is used to post message back onto the main thread of the
     // application
     private class MainHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
 
                 case ENABLE_SHUTTER_BUTTON:
                     mShutterButton.setEnabled(true);
                     break;
 
                 case CLEAR_SCREEN_DELAY: {
                     getWindow().clearFlags(
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                     break;
                 }
 
                 case UPDATE_RECORD_TIME: {
                     updateRecordingTime();
                     break;
                 }
 
                 case CHECK_DISPLAY_ROTATION: {
                     // Restart the preview if display rotation has changed.
                     // Sometimes this happens when the device is held upside
                     // down and camera app is opened. Rotation animation will
                     // take some time and the rotation value we have got may be
                     // wrong. Framework does not have a callback for this now.
                     if ((Util.getDisplayRotation(VideoCamera.this) != mDisplayRotation)
                             && !mMediaRecorderRecording) {
                         startPreview();
                     }
                     if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                         mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
                     }
                     break;
                 }
 
                 default:
                     Log.v(TAG, "Unhandled message: " + msg.what);
                     break;
             }
         }
     }
 
     private BroadcastReceiver mReceiver = null;
 
     private class MyBroadcastReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                 updateAndShowStorageHint();
                 stopVideoRecording();
             } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                 updateAndShowStorageHint();
                 updateThumbnailButton();
             } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                 // SD card unavailable
                 // handled in ACTION_MEDIA_EJECT
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                 Toast.makeText(VideoCamera.this,
                         getResources().getString(R.string.wait), 5000);
             } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                 updateAndShowStorageHint();
             }
         }
     }
 
     private String createName(long dateTaken) {
         Date date = new Date(dateTaken);
         SimpleDateFormat dateFormat = new SimpleDateFormat(
                 getString(R.string.video_file_name_format));
 
         return dateFormat.format(date);
     }
 
     private void showCameraErrorAndFinish() {
         Resources ress = getResources();
         Util.showFatalErrorAndFinish(VideoCamera.this,
                 ress.getString(R.string.camera_error_title),
                 ress.getString(R.string.cannot_connect_camera));
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
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
 
         mPreferences = new ComboPreferences(this);
         CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
         mCameraId = CameraSettings.readPreferredCameraId(mPreferences);
 
         //Testing purpose. Launch a specific camera through the intent extras.
         int intentCameraId = Util.getCameraFacingIntentExtras(this);
         if (intentCameraId != -1) {
             mCameraId = intentCameraId;
         }
 
         mPreferences.setLocalId(this, mCameraId);
         CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
 
         mNumberOfCameras = CameraHolder.instance().getNumberOfCameras();
 
         /*
          * To reduce startup time, we start the preview in another thread.
          * We make sure the preview is started at the end of onCreate.
          */
         Thread startPreviewThread = new Thread(new Runnable() {
             public void run() {
                 mOpenCameraFail = !openCamera();
                 // In eng build, we throw the exception so that test tool
                 // can detect it and report it
                 if (mOpenCameraFail && "eng".equals(Build.TYPE)) {
                     throw new RuntimeException("openCamera failed");
                 }
                 readVideoPreferences();
                 startPreview();
             }
         });
         startPreviewThread.start();
 
         mContentResolver = getContentResolver();
 
         requestWindowFeature(Window.FEATURE_PROGRESS);
         mIsVideoCaptureIntent = isVideoCaptureIntent();
         if (mIsVideoCaptureIntent) {
             setContentView(R.layout.video_camera_attach);
 
             mReviewControl = findViewById(R.id.review_control);
             mReviewControl.setVisibility(View.VISIBLE);
            findViewById(R.id.btn_cancel).setOnClickListener(this);
            findViewById(R.id.btn_done).setOnClickListener(this);
             findViewById(R.id.btn_play).setOnClickListener(this);
            View retake = findViewById(R.id.btn_retake);
             retake.setOnClickListener(this);
             if (retake instanceof ImageView) {
                 ((ImageView) retake).setImageResource(R.drawable.btn_ic_review_retake_video);
             } else {
                 ((Button) retake).setCompoundDrawablesWithIntrinsicBounds(
                         R.drawable.ic_switch_video_holo_dark, 0, 0, 0);
             }
         } else {
             setContentView(R.layout.video_camera);
 
             initThumbnailButton();
             mSwitcher = (SwitcherSet) findViewById(R.id.camera_switch);
             mSwitcher.setVisibility(View.VISIBLE);
             mSwitcher.setOnSwitchListener(this);
         }
 
         mPreviewFrameLayout = (PreviewFrameLayout)
                 findViewById(R.id.frame_layout);
         mPreviewFrameLayout.setOnSizeChangedListener(this);
 
         mVideoPreview = (SurfaceView) findViewById(R.id.camera_preview);
         mVideoFrame = (ImageView) findViewById(R.id.video_frame);
 
         // don't set mSurfaceHolder here. We have it set ONLY within
         // surfaceCreated / surfaceDestroyed, other parts of the code
         // assume that when it is set, the surface is also set.
         SurfaceHolder holder = mVideoPreview.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         mQuickCapture = getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
 
         mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
         mShutterButton.setImageResource(R.drawable.btn_ic_video_record);
         mShutterButton.setOnShutterButtonListener(this);
         mShutterButton.requestFocus();
 
         mRecordingTimeView = (TextView) findViewById(R.id.recording_time);
         mOrientationListener = new MyOrientationEventListener(VideoCamera.this);
         mTimeLapseLabel = findViewById(R.id.time_lapse_label);
         mPreviewBorder = findViewById(R.id.preview_border);
 
         // Make sure preview is started.
         try {
             startPreviewThread.join();
             if (mOpenCameraFail) {
                 showCameraErrorAndFinish();
                 return;
             }
         } catch (InterruptedException ex) {
             // ignore
         }
 
         showTimeLapseUI(mCaptureTimeLapse);
         resizeForPreviewAspectRatio();
 
         mBackCameraId = CameraHolder.instance().getBackCameraId();
         mFrontCameraId = CameraHolder.instance().getFrontCameraId();
 
         // Initialize after startPreview becuase this need mParameters.
         initializeIndicatorWheel();
         // xlarge devices use indicator wheel. Other devices use head-up display.
         if (mIndicatorWheel == null) {
             mHeadUpDisplay = new CamcorderHeadUpDisplay(this);
             mHeadUpDisplay.setListener(new MyHeadUpDisplayListener());
             initializeHeadUpDisplay();
         }
         initializeCameraPicker();
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
                 && !mPausing && mGLRootView == null) {
             attachHeadUpDisplay();
         } else if (mGLRootView != null) {
             detachHeadUpDisplay();
         }
     }
 
     private void initializeCameraPicker() {
         mCameraPicker = (CameraPicker) findViewById(R.id.camera_picker);
         if (mCameraPicker != null) {
             mCameraPicker.setImageResource(R.drawable.camera_toggle_video);
             ListPreference pref = mPreferenceGroup.findPreference(
                     CameraSettings.KEY_CAMERA_ID);
             if (pref != null) {
                 mCameraPicker.initialize(pref);
                 mCameraPicker.setListener(new MyCameraPickerListener());
             }
         }
     }
 
     private void loadCameraPreferences() {
         CameraSettings settings = new CameraSettings(this, mParameters,
                 mCameraId, CameraHolder.instance().getCameraInfo());
         mPreferenceGroup = settings.getPreferenceGroup(R.xml.video_preferences);
     }
 
     private void initializeHeadUpDisplay() {
         if (mHeadUpDisplay == null) return;
         loadCameraPreferences();
 
         if (mIsVideoCaptureIntent) {
             mPreferenceGroup = filterPreferenceScreenByIntent(mPreferenceGroup);
         }
         mHeadUpDisplay.initialize(this, mPreferenceGroup,
                 mOrientationCompensation, mCaptureTimeLapse);
     }
 
     private void attachHeadUpDisplay() {
         mHeadUpDisplay.setOrientation(mOrientationCompensation);
         ViewGroup frame = (ViewGroup) findViewById(R.id.frame);
         mGLRootView = new GLRootView(this);
         frame.addView(mGLRootView);
         mGLRootView.setContentPane(mHeadUpDisplay);
     }
 
     private void detachHeadUpDisplay() {
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
         if (mSwitcher != null) mSwitcher.setEnabled(enable);
     }
 
     private void initializeIndicatorWheel() {
         mIndicatorWheel = (IndicatorWheel) findViewById(R.id.indicator_wheel);
         if (mIndicatorWheel == null) return;
         loadCameraPreferences();
 
         String[] keys = new String[]{CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
             CameraSettings.KEY_WHITE_BALANCE,
             CameraSettings.KEY_COLOR_EFFECT,
             CameraSettings.KEY_VIDEO_QUALITY,
             CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL};
         mIndicatorWheel.initialize(this, mPreferenceGroup, keys, false);
         mIndicatorWheel.setListener(new MyIndicatorWheelListener());
         mPopupGestureDetector = new GestureDetector(this,
                 new PopupGestureListener());
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
             if (mMediaRecorderRecording) return;
             // We keep the last known orientation. So if the user first orient
             // the camera then point the camera to floor or sky, we still have
             // the correct orientation.
             if (orientation == ORIENTATION_UNKNOWN) return;
             mOrientation = roundOrientation(orientation);
             // When the screen is unlocked, display rotation may change. Always
             // calculate the up-to-date orientationCompensation.
             int orientationCompensation = mOrientation
                     + Util.getDisplayRotation(VideoCamera.this);
             if (mOrientationCompensation != orientationCompensation) {
                 mOrientationCompensation = orientationCompensation;
                 if (!mIsVideoCaptureIntent) {
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
     protected void onStart() {
         super.onStart();
         if (!mIsVideoCaptureIntent) {
             mSwitcher.setSwitch(SWITCH_VIDEO);
         }
     }
 
     private void startPlayVideoActivity() {
         Intent intent = new Intent(Intent.ACTION_VIEW, mCurrentVideoUri);
         try {
             startActivity(intent);
         } catch (android.content.ActivityNotFoundException ex) {
             Log.e(TAG, "Couldn't view video " + mCurrentVideoUri, ex);
         }
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.btn_retake:
                 deleteCurrentVideo();
                 hideAlert();
                 break;
             case R.id.btn_play:
                 startPlayVideoActivity();
                 break;
             case R.id.btn_done:
                 doReturnToCaller(true);
                 break;
             case R.id.btn_cancel:
                 stopVideoRecording();
                 doReturnToCaller(false);
                 break;
             case R.id.review_thumbnail:
                 if (!mMediaRecorderRecording) viewVideo(mThumbnailButton);
                 break;
             case R.id.btn_gallery:
                 gotoGallery();
                 break;
         }
     }
 
     public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
         // Do nothing (everything happens in onShutterButtonClick).
     }
 
     private void onStopVideoRecording(boolean valid) {
         stopVideoRecording();
         if (mIsVideoCaptureIntent) {
             if (mQuickCapture) {
                 doReturnToCaller(valid);
             } else {
                 showAlert();
             }
         } else {
             getThumbnail();
         }
     }
 
     public void onShutterButtonClick(ShutterButton button) {
         switch (button.getId()) {
             case R.id.shutter_button:
                 if (collapseCameraControls()) return;
                 boolean stop = mMediaRecorderRecording;
 
                 if (stop) {
                     onStopVideoRecording(true);
                 } else {
                     startVideoRecording();
                 }
                 mShutterButton.setEnabled(false);
 
                 // Keep the shutter button disabled when in video capture intent
                 // mode and recording is stopped. It'll be re-enabled when
                 // re-take button is clicked.
                 if (!(mIsVideoCaptureIntent && stop)) {
                     mHandler.sendEmptyMessageDelayed(
                             ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
                 }
                 break;
         }
     }
 
     private OnScreenHint mStorageHint;
 
     private void updateAndShowStorageHint() {
         mStorageSpace = Storage.getAvailableSpace();
         showStorageHint();
     }
 
     private void showStorageHint() {
         String errorMessage = null;
         if (mStorageSpace == Storage.UNAVAILABLE) {
             errorMessage = getString(R.string.no_storage);
         } else if (mStorageSpace == Storage.PREPARING) {
             errorMessage = getString(R.string.preparing_sd);
         } else if (mStorageSpace == Storage.UNKNOWN_SIZE) {
             errorMessage = getString(R.string.access_sd_fail);
         } else if (mStorageSpace < LOW_STORAGE_THRESHOLD) {
             errorMessage = getString(R.string.spaceIsLow_content);
         }
 
         if (errorMessage != null) {
             if (mStorageHint == null) {
                 mStorageHint = OnScreenHint.makeText(this, errorMessage);
             } else {
                 mStorageHint.setText(errorMessage);
             }
             mStorageHint.show();
         } else if (mStorageHint != null) {
             mStorageHint.cancel();
             mStorageHint = null;
         }
     }
 
     private void readVideoPreferences() {
         String quality = mPreferences.getString(
                 CameraSettings.KEY_VIDEO_QUALITY,
                 CameraSettings.DEFAULT_VIDEO_QUALITY_VALUE);
         boolean videoQualityHigh =
                 CameraSettings.getVideoQuality(this, quality);
 
         // Set video quality.
         Intent intent = getIntent();
         if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
             int extraVideoQuality =
                     intent.getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
             if (extraVideoQuality > 0) {
                 quality = getString(R.string.pref_video_quality_high);
             } else {  // 0 is mms.
                 quality = getString(R.string.pref_video_quality_mms);
             }
         }
 
         // Set video duration limit. The limit is read from the preference,
         // unless it is specified in the intent.
         if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
             int seconds =
                     intent.getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0);
             mMaxVideoDurationInMs = 1000 * seconds;
         } else {
             mMaxVideoDurationInMs =
                     CameraSettings.getVideoDurationInMillis(this, quality);
         }
 
         // Read time lapse recording interval.
         String frameIntervalStr = mPreferences.getString(
                 CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                 getString(R.string.pref_video_time_lapse_frame_interval_default));
         mTimeBetweenTimeLapseFrameCaptureMs = Integer.parseInt(frameIntervalStr);
 
         mCaptureTimeLapse = (mTimeBetweenTimeLapseFrameCaptureMs != 0);
         int profileQuality = getProfileQuality(mCameraId, quality, mCaptureTimeLapse);
         mProfile = CamcorderProfile.get(mCameraId, profileQuality);
         getDesiredPreviewSize();
     }
 
     int getProfileQuality(int cameraId, String quality, boolean captureTimeLapse) {
         HashMap<String, Integer>[] qualityMap;
         if (captureTimeLapse) {
             if (mTimeLapseProfileQuality == null) {
                 mTimeLapseProfileQuality = new HashMap[
                         CameraHolder.instance().getNumberOfCameras()];
             }
             qualityMap = mTimeLapseProfileQuality;
             if (qualityMap[cameraId] == null) {
                 qualityMap[cameraId] = buildProfileQuality(cameraId, TIME_LAPSE_VIDEO_QUALITY);
             }
         } else {
             if (mProfileQuality == null) {
                 mProfileQuality = new HashMap[
                         CameraHolder.instance().getNumberOfCameras()];
             }
             qualityMap = mProfileQuality;
             if (qualityMap[cameraId] == null) {
                 qualityMap[cameraId] = buildProfileQuality(cameraId, VIDEO_QUALITY);
             }
         }
         return qualityMap[cameraId].get(quality);
     }
 
     HashMap<String, Integer> buildProfileQuality(int cameraId,
             int qualityList[]) {
         HashMap<String, Integer> qualityMap = new HashMap<String, Integer>();
         int highestQuality = -1, secondHighestQuality = -1,
                 lastEffectiveQuality = -1;
         for (int i = 0; i < qualityList.length; i++) {
             if (CamcorderProfile.hasProfile(cameraId, qualityList[i])) {
                 if (highestQuality == -1) {
                     highestQuality = qualityList[i];
                 } else if (secondHighestQuality == -1) {
                     secondHighestQuality = qualityList[i];
                     break;
                 }
                 lastEffectiveQuality = qualityList[i];
             }
         }
         if (secondHighestQuality == -1) {
             secondHighestQuality = highestQuality;
         }
         qualityMap.put(getString(R.string.pref_video_quality_high), highestQuality);
         qualityMap.put(getString(R.string.pref_video_quality_low), secondHighestQuality);
         qualityMap.put(getString(R.string.pref_video_quality_youtube), highestQuality);
         qualityMap.put(getString(R.string.pref_video_quality_mms), lastEffectiveQuality);
         return qualityMap;
     }
 
     private void getDesiredPreviewSize() {
         mParameters = mCameraDevice.getParameters();
         if (mParameters.getSupportedVideoSizes() == null) {
             mDesiredPreviewWidth = mProfile.videoFrameWidth;
             mDesiredPreviewHeight = mProfile.videoFrameHeight;
         } else {  // Driver supports separates outputs for preview and video.
             List<Size> sizes = mParameters.getSupportedPreviewSizes();
             Size preferred = mParameters.getPreferredPreviewSizeForVideo();
             int product = preferred.width * preferred.height;
             Iterator it = sizes.iterator();
             // Remove the preview sizes that are not preferred.
             while (it.hasNext()) {
                 Size size = (Size) it.next();
                 if (size.width * size.height > product) {
                     it.remove();
                 }
             }
             Size optimalSize = Util.getOptimalPreviewSize(this, sizes,
                 (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
             mDesiredPreviewWidth = optimalSize.width;
             mDesiredPreviewHeight = optimalSize.height;
         }
         Log.v(TAG, "mDesiredPreviewWidth=" + mDesiredPreviewWidth +
                 ". mDesiredPreviewHeight=" + mDesiredPreviewHeight);
     }
 
     private void resizeForPreviewAspectRatio() {
         mPreviewFrameLayout.setAspectRatio(
                 (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mPausing = false;
 
         // Start orientation listener as soon as possible because it takes
         // some time to get first orientation.
         mOrientationListener.enable();
         if (!mPreviewing && !mOpenCameraFail) {
             if (!openCamera()) return;
             readVideoPreferences();
             resizeForPreviewAspectRatio();
             startPreview();
         }
         keepScreenOnAwhile();
 
         // install an intent filter to receive SD card related events.
         IntentFilter intentFilter =
                 new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
         intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
         intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
         intentFilter.addDataScheme("file");
         mReceiver = new MyBroadcastReceiver();
         registerReceiver(mReceiver, intentFilter);
         mStorageSpace = Storage.getAvailableSpace();
 
         mHandler.postDelayed(new Runnable() {
             public void run() {
                 showStorageHint();
             }
         }, 200);
 
         changeHeadUpDisplayState();
 
         // Update the last video thumbnail.
         if (!mIsVideoCaptureIntent) {
             updateThumbnailButton();
         }
 
         if (mPreviewing) {
             mOnResumeTime = SystemClock.uptimeMillis();
             mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
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
 
     private boolean openCamera() {
         try {
             if (mCameraDevice == null) {
                 mCameraDevice = CameraHolder.instance().open(mCameraId);
             }
         } catch (CameraHardwareException e) {
             showCameraErrorAndFinish();
             return false;
         }
         return true;
     }
 
     private void startPreview() {
         Log.v(TAG, "startPreview");
         mCameraDevice.setErrorCallback(mErrorCallback);
 
         if (mPreviewing == true) {
             mCameraDevice.stopPreview();
             mPreviewing = false;
         }
         setPreviewDisplay(mSurfaceHolder);
         mDisplayRotation = Util.getDisplayRotation(this);
         Util.setCameraDisplayOrientation(mDisplayRotation, mCameraId, mCameraDevice);
         setCameraParameters();
 
         try {
             mCameraDevice.startPreview();
             mPreviewing = true;
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("startPreview failed", ex);
         }
     }
 
     private void closeCamera() {
         Log.v(TAG, "closeCamera");
         if (mCameraDevice == null) {
             Log.d(TAG, "already stopped.");
             return;
         }
         // If we don't lock the camera, release() will fail.
         mCameraDevice.lock();
         CameraHolder.instance().release();
         mCameraDevice = null;
         mPreviewing = false;
     }
 
     private void finishRecorderAndCloseCamera() {
         // This is similar to what mShutterButton.performClick() does,
         // but not quite the same.
         if (mMediaRecorderRecording) {
             if (mIsVideoCaptureIntent) {
                 stopVideoRecording();
                 showAlert();
             } else {
                 stopVideoRecording();
                 getThumbnail();
             }
         } else {
             stopVideoRecording();
         }
         closeCamera();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mPausing = true;
 
         changeHeadUpDisplayState();
         if (mIndicatorWheel != null) mIndicatorWheel.dismissSettingPopup();
 
         finishRecorderAndCloseCamera();
 
         if (mReceiver != null) {
             unregisterReceiver(mReceiver);
             mReceiver = null;
         }
         resetScreenOn();
 
         if (!mIsVideoCaptureIntent && mThumbnailButton != null) {
             mThumbnailButton.storeData(LAST_THUMB_PATH);
         }
 
         if (mStorageHint != null) {
             mStorageHint.cancel();
             mStorageHint = null;
         }
 
         mOrientationListener.disable();
 
         mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
     }
 
     @Override
     public void onUserInteraction() {
         super.onUserInteraction();
         if (!mMediaRecorderRecording) keepScreenOnAwhile();
     }
 
     @Override
     public void onBackPressed() {
         if (mPausing) return;
         if (mMediaRecorderRecording) {
             onStopVideoRecording(false);
         } else if (!collapseCameraControls()) {
             super.onBackPressed();
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Do not handle any key if the activity is paused.
         if (mPausing) {
             return true;
         }
 
         switch (keyCode) {
             case KeyEvent.KEYCODE_CAMERA:
                 if (event.getRepeatCount() == 0) {
                     mShutterButton.performClick();
                     return true;
                 }
                 break;
             case KeyEvent.KEYCODE_DPAD_CENTER:
                 if (event.getRepeatCount() == 0) {
                     mShutterButton.performClick();
                     return true;
                 }
                 break;
             case KeyEvent.KEYCODE_MENU:
                 if (mMediaRecorderRecording) return true;
                 break;
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_CAMERA:
                 mShutterButton.setPressed(false);
                 return true;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Make sure we have a surface in the holder before proceeding.
         if (holder.getSurface() == null) {
             Log.d(TAG, "holder.getSurface() == null");
             return;
         }
 
         Log.v(TAG, "surfaceChanged. w=" + w + ". h=" + h);
 
         mSurfaceHolder = holder;
 
         if (mPausing) {
             // We're pausing, the screen is off and we already stopped
             // video recording. We don't want to start the camera again
             // in this case in order to conserve power.
             // The fact that surfaceChanged is called _after_ an onPause appears
             // to be legitimate since in that case the lockscreen always returns
             // to portrait orientation possibly triggering the notification.
             return;
         }
 
         // The mCameraDevice will be null if it is fail to connect to the
         // camera hardware. In this case we will show a dialog and then
         // finish the activity, so it's OK to ignore it.
         if (mCameraDevice == null) return;
 
         // Set preview display if the surface is being created. Preview was
         // already started. Also restart the preview if display rotation has
         // changed. Sometimes this happens when the device is held in portrait
         // and camera app is opened. Rotation animation takes some time and
         // display rotation in onCreate may not be what we want.
         if (mPreviewing && (Util.getDisplayRotation(this) == mDisplayRotation)
                 && holder.isCreating()) {
             setPreviewDisplay(holder);
         } else {
             stopVideoRecording();
             startPreview();
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         mSurfaceHolder = null;
     }
 
     private void gotoGallery() {
         MenuHelper.gotoCameraVideoGallery(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         if (mIsVideoCaptureIntent) {
             // No options menu for attach mode.
             return false;
         } else {
             addBaseMenuItems(menu);
         }
         return true;
     }
 
     private boolean isVideoCaptureIntent() {
         String action = getIntent().getAction();
         return (MediaStore.ACTION_VIDEO_CAPTURE.equals(action));
     }
 
     private void doReturnToCaller(boolean valid) {
         Intent resultIntent = new Intent();
         int resultCode;
         if (valid) {
             resultCode = RESULT_OK;
             resultIntent.setData(mCurrentVideoUri);
         } else {
             resultCode = RESULT_CANCELED;
         }
         setResult(resultCode, resultIntent);
         finish();
     }
 
     private void cleanupEmptyFile() {
         if (mVideoFilename != null) {
             File f = new File(mVideoFilename);
             if (f.length() == 0 && f.delete()) {
                 Log.v(TAG, "Empty video file deleted: " + mVideoFilename);
                 mVideoFilename = null;
             }
         }
     }
 
     // Prepares media recorder.
     private void initializeRecorder() {
         Log.v(TAG, "initializeRecorder");
         // If the mCameraDevice is null, then this activity is going to finish
         if (mCameraDevice == null) return;
 
         if (mSurfaceHolder == null) {
             Log.v(TAG, "Surface holder is null. Wait for surface changed.");
             return;
         }
 
         Intent intent = getIntent();
         Bundle myExtras = intent.getExtras();
 
         long requestedSizeLimit = 0;
         if (mIsVideoCaptureIntent && myExtras != null) {
             Uri saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
             if (saveUri != null) {
                 try {
                     mVideoFileDescriptor =
                             mContentResolver.openFileDescriptor(saveUri, "rw");
                     mCurrentVideoUri = saveUri;
                 } catch (java.io.FileNotFoundException ex) {
                     // invalid uri
                     Log.e(TAG, ex.toString());
                 }
             }
             requestedSizeLimit = myExtras.getLong(MediaStore.EXTRA_SIZE_LIMIT);
         }
         mMediaRecorder = new MediaRecorder();
 
         // Unlock the camera object before passing it to media recorder.
         mCameraDevice.unlock();
         mMediaRecorder.setCamera(mCameraDevice);
         if (!mCaptureTimeLapse) {
             mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
         }
         mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mMediaRecorder.setProfile(mProfile);
         mMediaRecorder.setMaxDuration(mMaxVideoDurationInMs);
         if (mCaptureTimeLapse) {
             mMediaRecorder.setCaptureRate((1000 / (double) mTimeBetweenTimeLapseFrameCaptureMs));
         }
 
         // Set output file.
         // Try Uri in the intent first. If it doesn't exist, use our own
         // instead.
         if (mVideoFileDescriptor != null) {
             mMediaRecorder.setOutputFile(mVideoFileDescriptor.getFileDescriptor());
         } else {
             generateVideoFilename(mProfile.fileFormat);
             mMediaRecorder.setOutputFile(mVideoFilename);
         }
 
         mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
 
         // Set maximum file size.
         // remaining >= LOW_STORAGE_THRESHOLD at this point, reserve a quarter
         // of that to make it more likely that recording can complete
         // successfully.
         long maxFileSize = mStorageSpace - LOW_STORAGE_THRESHOLD / 4;
         if (requestedSizeLimit > 0 && requestedSizeLimit < maxFileSize) {
             maxFileSize = requestedSizeLimit;
         }
 
         try {
             mMediaRecorder.setMaxFileSize(maxFileSize);
         } catch (RuntimeException exception) {
             // We are going to ignore failure of setMaxFileSize here, as
             // a) The composer selected may simply not support it, or
             // b) The underlying media framework may not handle 64-bit range
             // on the size restriction.
         }
 
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
         mMediaRecorder.setOrientationHint(rotation);
         mOrientationHint = rotation;
 
         try {
             mMediaRecorder.prepare();
         } catch (IOException e) {
             Log.e(TAG, "prepare failed for " + mVideoFilename, e);
             releaseMediaRecorder();
             throw new RuntimeException(e);
         }
 
         mMediaRecorder.setOnErrorListener(this);
         mMediaRecorder.setOnInfoListener(this);
     }
 
     private void releaseMediaRecorder() {
         Log.v(TAG, "Releasing media recorder.");
         if (mMediaRecorder != null) {
             cleanupEmptyFile();
             mMediaRecorder.reset();
             mMediaRecorder.release();
             mMediaRecorder = null;
         }
         mVideoFilename = null;
         if (mVideoFileDescriptor != null) {
             try {
                 mVideoFileDescriptor.close();
             } catch (IOException e) {
                 Log.e(TAG, "Fail to close fd", e);
             }
             mVideoFileDescriptor = null;
         }
         // Take back the camera object control from media recorder. Camera
         // device may be null if the activity is paused.
         if (mCameraDevice != null) mCameraDevice.lock();
     }
 
     private void generateVideoFilename(int outputFileFormat) {
         long dateTaken = System.currentTimeMillis();
         String title = createName(dateTaken);
         String filename = title + ".3gp"; // Used when emailing.
         String mime = "video/3gpp";
         if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
             filename = title + ".mp4";
             mime = "video/mp4";
         }
         mVideoFilename = Storage.DIRECTORY + '/' + filename;
         mCurrentVideoValues = new ContentValues(7);
         mCurrentVideoValues.put(Video.Media.TITLE, title);
         mCurrentVideoValues.put(Video.Media.DISPLAY_NAME, filename);
         mCurrentVideoValues.put(Video.Media.DATE_TAKEN, dateTaken);
         mCurrentVideoValues.put(Video.Media.MIME_TYPE, mime);
         mCurrentVideoValues.put(Video.Media.DATA, mVideoFilename);
         Log.v(TAG, "New video filename: " + mVideoFilename);
     }
 
     private void registerVideo() {
         if (mVideoFileDescriptor == null) {
             Uri videoTable = Uri.parse("content://media/external/video/media");
             mCurrentVideoValues.put(Video.Media.SIZE,
                     new File(mCurrentVideoFilename).length());
             long duration = SystemClock.uptimeMillis() - mRecordingStartTime;
             if (duration > 0) {
                 if (mCaptureTimeLapse) {
                     duration = getTimeLapseVideoLength(duration);
                 }
                 mCurrentVideoValues.put(Video.Media.DURATION, duration);
             } else {
                 Log.w(TAG, "Video duration <= 0 : " + duration);
             }
             try {
                 mCurrentVideoUri = mContentResolver.insert(videoTable,
                         mCurrentVideoValues);
             } catch (Exception e) {
                 // We failed to insert into the database. This can happen if
                 // the SD card is unmounted.
                 mCurrentVideoUri = null;
                 mCurrentVideoFilename = null;
             } finally {
                 Log.v(TAG, "Current video URI: " + mCurrentVideoUri);
             }
         }
         mCurrentVideoValues = null;
     }
 
     private void deleteCurrentVideo() {
         // Remove the video and the uri if the uri is not passed in by intent.
         if (mCurrentVideoFilename != null) {
             deleteVideoFile(mCurrentVideoFilename);
             mCurrentVideoFilename = null;
             if (mCurrentVideoUri != null) {
                 mContentResolver.delete(mCurrentVideoUri, null, null);
                 mCurrentVideoUri = null;
             }
         }
         updateAndShowStorageHint();
     }
 
     private void deleteVideoFile(String fileName) {
         Log.v(TAG, "Deleting video " + fileName);
         File f = new File(fileName);
         if (!f.delete()) {
             Log.v(TAG, "Could not delete " + fileName);
         }
     }
 
     private void addBaseMenuItems(Menu menu) {
         MenuHelper.addSwitchModeMenuItem(menu, false, new Runnable() {
             public void run() {
                 switchToCameraMode();
             }
         });
         MenuItem gallery = menu.add(Menu.NONE, Menu.NONE,
                 MenuHelper.POSITION_GOTO_GALLERY,
                 R.string.camera_gallery_photos_text)
                 .setOnMenuItemClickListener(
                     new OnMenuItemClickListener() {
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
 
     private PreferenceGroup filterPreferenceScreenByIntent(
             PreferenceGroup screen) {
         Intent intent = getIntent();
         if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
             CameraSettings.removePreferenceFromScreen(screen,
                     CameraSettings.KEY_VIDEO_QUALITY);
         }
 
         if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
             CameraSettings.removePreferenceFromScreen(screen,
                     CameraSettings.KEY_VIDEO_QUALITY);
         }
         return screen;
     }
 
     // from MediaRecorder.OnErrorListener
     public void onError(MediaRecorder mr, int what, int extra) {
         if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
             // We may have run out of space on the sdcard.
             stopVideoRecording();
             updateAndShowStorageHint();
         }
     }
 
     // from MediaRecorder.OnInfoListener
     public void onInfo(MediaRecorder mr, int what, int extra) {
         if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
             if (mMediaRecorderRecording) onStopVideoRecording(true);
         } else if (what
                 == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
             if (mMediaRecorderRecording) onStopVideoRecording(true);
 
             // Show the toast.
             Toast.makeText(VideoCamera.this, R.string.video_reach_size_limit,
                            Toast.LENGTH_LONG).show();
         }
     }
 
     /*
      * Make sure we're not recording music playing in the background, ask the
      * MediaPlaybackService to pause playback.
      */
     private void pauseAudioPlayback() {
         // Shamelessly copied from MediaPlaybackService.java, which
         // should be public, but isn't.
         Intent i = new Intent("com.android.music.musicservicecommand");
         i.putExtra("command", "pause");
 
         sendBroadcast(i);
     }
 
     private void startVideoRecording() {
         Log.v(TAG, "startVideoRecording");
 
         updateAndShowStorageHint();
         if (mStorageSpace < LOW_STORAGE_THRESHOLD) {
             Log.v(TAG, "Storage issue, ignore the start request");
             return;
         }
 
         initializeRecorder();
         if (mMediaRecorder == null) {
             Log.e(TAG, "Fail to initialize media recorder");
             return;
         }
 
         pauseAudioPlayback();
 
         try {
             mMediaRecorder.start(); // Recording is now started
         } catch (RuntimeException e) {
             Log.e(TAG, "Could not start media recorder. ", e);
             releaseMediaRecorder();
             return;
         }
         enableCameraControls(false);
 
         mMediaRecorderRecording = true;
         mRecordingStartTime = SystemClock.uptimeMillis();
         showRecordingUI(true);
 
         updateRecordingTime();
         keepScreenOn();
     }
 
     private void showRecordingUI(boolean recording) {
         if (recording) {
             mShutterButton.setImageDrawable(getResources().getDrawable(
                     R.drawable.btn_ic_video_record_stop));
             mShutterButton.setBackgroundResource(R.drawable.btn_shutter_recording);
             mRecordingTimeView.setText("");
             mRecordingTimeView.setVisibility(View.VISIBLE);
             if (mReviewControl != null) mReviewControl.setVisibility(View.GONE);
             if (mCaptureTimeLapse) {
                 mIndicatorWheel.startTimeLapseAnimation(
                         mTimeBetweenTimeLapseFrameCaptureMs,
                         mRecordingStartTime);
             }
         } else {
             mShutterButton.setImageDrawable(getResources().getDrawable(
                     R.drawable.btn_ic_video_record));
             mShutterButton.setBackgroundResource(R.drawable.btn_shutter);
             mRecordingTimeView.setVisibility(View.GONE);
             if (mReviewControl != null) mReviewControl.setVisibility(View.VISIBLE);
             if (mCaptureTimeLapse) {
                 mIndicatorWheel.stopTimeLapseAnimation();
             }
         }
     }
 
     private void getThumbnail() {
         acquireVideoThumb();
     }
 
     private void showAlert() {
         if (mIndicatorWheel == null) {
             fadeOut(findViewById(R.id.shutter_button));
         }
         if (mCurrentVideoFilename != null) {
             Bitmap src = ThumbnailUtils.createVideoThumbnail(
                     mCurrentVideoFilename, Video.Thumbnails.MINI_KIND);
             // MetadataRetriever already rotates the thumbnail. We should rotate
             // it back (and mirror if it is front-facing camera).
             CameraInfo[] info = CameraHolder.instance().getCameraInfo();
             if (info[mCameraId].facing == CameraInfo.CAMERA_FACING_BACK) {
                 src = Util.rotateAndMirror(src, -mOrientationHint, false);
             } else {
                 src = Util.rotateAndMirror(src, -mOrientationHint, true);
             }
             mVideoFrame.setImageBitmap(src);
             mVideoFrame.setVisibility(View.VISIBLE);
         }
         int[] pickIds = {R.id.btn_retake, R.id.btn_done, R.id.btn_play};
         for (int id : pickIds) {
             View button = findViewById(id);
             fadeIn(((View) button.getParent()));
         }
 
         // Remove the text of the cancel button
         View view = findViewById(R.id.btn_cancel);
         if (view instanceof Button) ((Button) view).setText("");
         showTimeLapseUI(false);
     }
 
     private void hideAlert() {
         mVideoFrame.setVisibility(View.INVISIBLE);
         fadeIn(findViewById(R.id.shutter_button));
         mShutterButton.setEnabled(true);
         enableCameraControls(true);
 
         // Restore the text of the cancel button
         View view = findViewById(R.id.btn_cancel);
         if (view instanceof Button) {
             ((Button) view).setText(R.string.review_cancel);
         }
 
         int[] pickIds = {R.id.btn_retake, R.id.btn_done, R.id.btn_play};
         for (int id : pickIds) {
             View button = findViewById(id);
             ((View) button.getParent()).setVisibility(View.GONE);
         }
         if (mCaptureTimeLapse) {
             showTimeLapseUI(true);
         }
     }
 
     private static void fadeIn(View view) {
         view.setVisibility(View.VISIBLE);
         Animation animation = new AlphaAnimation(0F, 1F);
         animation.setDuration(500);
         view.startAnimation(animation);
     }
 
     private static void fadeOut(View view) {
         view.setVisibility(View.INVISIBLE);
         Animation animation = new AlphaAnimation(1F, 0F);
         animation.setDuration(500);
         view.startAnimation(animation);
     }
 
     private boolean isAlertVisible() {
         return this.mVideoFrame.getVisibility() == View.VISIBLE;
     }
 
     private void viewVideo(RotateImageView view) {
         if(view.isUriValid()) {
             Intent intent = new Intent(Util.REVIEW_ACTION, view.getUri());
             try {
                 startActivity(intent);
             } catch (ActivityNotFoundException ex) {
                 try {
                     intent = new Intent(Intent.ACTION_VIEW, view.getUri());
                     startActivity(intent);
                 } catch (ActivityNotFoundException e) {
                     Log.e(TAG, "review video fail. uri=" + view.getUri(), e);
                 }
             }
         } else {
             Log.e(TAG, "Uri invalid. uri=" + view.getUri());
         }
     }
 
     private void stopVideoRecording() {
         Log.v(TAG, "stopVideoRecording");
         if (mMediaRecorderRecording) {
             boolean needToRegisterRecording = false;
             mMediaRecorder.setOnErrorListener(null);
             mMediaRecorder.setOnInfoListener(null);
             try {
                 mMediaRecorder.stop();
                 mCurrentVideoFilename = mVideoFilename;
                 Log.v(TAG, "Setting current video filename: "
                         + mCurrentVideoFilename);
                 needToRegisterRecording = true;
             } catch (RuntimeException e) {
                 Log.e(TAG, "stop fail",  e);
                 if (mVideoFilename != null) deleteVideoFile(mVideoFilename);
             }
             mMediaRecorderRecording = false;
             showRecordingUI(false);
             if (!mIsVideoCaptureIntent) {
                 enableCameraControls(true);
             }
             keepScreenOnAwhile();
             if (needToRegisterRecording && mStorageSpace >= LOW_STORAGE_THRESHOLD) {
                 registerVideo();
             }
         }
         releaseMediaRecorder();  // always release media recorder
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
 
     private void keepScreenOn() {
         mHandler.removeMessages(CLEAR_SCREEN_DELAY);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     }
 
     private void acquireVideoThumb() {
         if (mThumbnailButton != null) {
             Bitmap videoFrame = ThumbnailUtils.createVideoThumbnail(
                     mCurrentVideoFilename, Video.Thumbnails.MINI_KIND);
             mThumbnailButton.setData(mCurrentVideoUri, videoFrame);
             if (videoFrame != null) {
                 mThumbnailButton.setVisibility(View.VISIBLE);
             }
         }
     }
 
     private void initThumbnailButton() {
         mThumbnailButton = (RotateImageView)findViewById(R.id.review_thumbnail);
         if (mThumbnailButton != null) {
             mThumbnailButton.setOnClickListener(this);
             mThumbnailButton.loadData(LAST_THUMB_PATH);
         }
     }
 
     private void updateThumbnailButton() {
         if (mThumbnailButton == null) return;
         if (!mThumbnailButton.isUriValid()) {
             Storage.Thumbnail thumbnail =
                     Storage.getLastVideoThumbnail(mContentResolver);
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
 
     private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
         long seconds = milliSeconds / 1000; // round down to compute seconds
         long minutes = seconds / 60;
         long hours = minutes / 60;
         long remainderMinutes = minutes - (hours * 60);
         long remainderSeconds = seconds - (minutes * 60);
 
         StringBuilder timeStringBuilder = new StringBuilder();
 
         // Hours
         if (hours > 0) {
             if (hours < 10) {
                 timeStringBuilder.append('0');
             }
             timeStringBuilder.append(hours);
 
             timeStringBuilder.append(':');
         }
 
         // Minutes
         if (remainderMinutes < 10) {
             timeStringBuilder.append('0');
         }
         timeStringBuilder.append(remainderMinutes);
         timeStringBuilder.append(':');
 
         // Seconds
         if (remainderSeconds < 10) {
             timeStringBuilder.append('0');
         }
         timeStringBuilder.append(remainderSeconds);
 
         // Centi seconds
         if (displayCentiSeconds) {
             timeStringBuilder.append('.');
             long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
             if (remainderCentiSeconds < 10) {
                 timeStringBuilder.append('0');
             }
             timeStringBuilder.append(remainderCentiSeconds);
         }
 
         return timeStringBuilder.toString();
     }
 
     private long getTimeLapseVideoLength(long deltaMs) {
         // For better approximation calculate fractional number of frames captured.
         // This will update the video time at a higher resolution.
         double numberOfFrames = (double) deltaMs / mTimeBetweenTimeLapseFrameCaptureMs;
         return (long) (numberOfFrames / mProfile.videoFrameRate * 1000);
     }
 
     private void updateRecordingTime() {
         if (!mMediaRecorderRecording) {
             return;
         }
         long now = SystemClock.uptimeMillis();
         long delta = now - mRecordingStartTime;
 
         // Starting a minute before reaching the max duration
         // limit, we'll countdown the remaining time instead.
         boolean countdownRemainingTime = (mMaxVideoDurationInMs != 0
                 && delta >= mMaxVideoDurationInMs - 60000);
 
         long deltaAdjusted = delta;
         if (countdownRemainingTime) {
             deltaAdjusted = Math.max(0, mMaxVideoDurationInMs - deltaAdjusted) + 999;
         }
         String text;
 
         long targetNextUpdateDelay;
         if (!mCaptureTimeLapse) {
             text = millisecondToTimeString(deltaAdjusted, false);
             targetNextUpdateDelay = 1000;
         } else {
             // The length of time lapse video is different from the length
             // of the actual wall clock time elapsed. Display the video length
             // only in format hh:mm:ss.dd, where dd are the centi seconds.
             text = millisecondToTimeString(getTimeLapseVideoLength(delta), true);
             targetNextUpdateDelay = mTimeBetweenTimeLapseFrameCaptureMs;
         }
 
         mRecordingTimeView.setText(text);
 
         if (mRecordingTimeCountsDown != countdownRemainingTime) {
             // Avoid setting the color on every update, do it only
             // when it needs changing.
             mRecordingTimeCountsDown = countdownRemainingTime;
 
             int color = getResources().getColor(countdownRemainingTime
                     ? R.color.recording_time_remaining_text
                     : R.color.recording_time_elapsed_text);
 
             mRecordingTimeView.setTextColor(color);
         }
 
         long actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
         mHandler.sendEmptyMessageDelayed(
                 UPDATE_RECORD_TIME, actualNextUpdateDelay);
     }
 
     private static boolean isSupported(String value, List<String> supported) {
         return supported == null ? false : supported.indexOf(value) >= 0;
     }
 
     private void setCameraParameters() {
         mParameters = mCameraDevice.getParameters();
 
         mParameters.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
         mParameters.setPreviewFrameRate(mProfile.videoFrameRate);
 
         // Set flash mode.
         String flashMode = mPreferences.getString(
                 CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                 getString(R.string.pref_camera_video_flashmode_default));
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
 
         // Set color effect parameter.
         String colorEffect = mPreferences.getString(
                 CameraSettings.KEY_COLOR_EFFECT,
                 getString(R.string.pref_camera_coloreffect_default));
         if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
             mParameters.setColorEffect(colorEffect);
         }
 
         mCameraDevice.setParameters(mParameters);
         // Keep preview size up to date.
         mParameters = mCameraDevice.getParameters();
     }
 
     private boolean switchToCameraMode() {
         if (isFinishing() || mMediaRecorderRecording) return false;
         MenuHelper.gotoCameraMode(VideoCamera.this);
         finish();
         return true;
     }
 
     public boolean onSwitchChanged(Switcher source, boolean onOff) {
         if (onOff == SWITCH_CAMERA) {
             return switchToCameraMode();
         } else {
             return true;
         }
     }
 
     @Override
     public void onConfigurationChanged(Configuration config) {
         super.onConfigurationChanged(config);
 
         // If the camera resumes behind the lock screen, the orientation
         // will be portrait. That causes OOM when we try to allocation GPU
         // memory for the GLSurfaceView again when the orientation changes. So,
         // we delayed initialization of HeadUpDisplay until the orientation
         // becomes landscape.
         changeHeadUpDisplayState();
     }
 
     public void onSizeChanged() {
         // TODO: update the content on GLRootView
     }
 
     private class MyHeadUpDisplayListener implements HeadUpDisplay.Listener {
         public void onSharedPreferenceChanged() {
             mHandler.post(new Runnable() {
                 public void run() {
                     VideoCamera.this.onSharedPreferenceChanged();
                 }
             });
         }
 
         public void onRestorePreferencesClicked() {
             mHandler.post(new Runnable() {
                 public void run() {
                     VideoCamera.this.onRestorePreferencesClicked();
                 }
             });
         }
 
         public void onPopupWindowVisibilityChanged(final int visibility) {
         }
     }
 
     private void onRestorePreferencesClicked() {
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
         if (mHeadUpDisplay != null) {
             mHeadUpDisplay.restorePreferences(mParameters);
         }
 
         if (mIndicatorWheel != null) {
             mIndicatorWheel.dismissSettingPopup();
             CameraSettings.restorePreferences(VideoCamera.this, mPreferences,
                     mParameters);
             initializeIndicatorWheel();
             onSharedPreferenceChanged();
         }
     }
 
     private void onSharedPreferenceChanged() {
         // ignore the events after "onPause()" or preview has not started yet
         if (mPausing) return;
         synchronized (mPreferences) {
             // If mCameraDevice is not ready then we can set the parameter in
             // startPreview().
             if (mCameraDevice == null) return;
 
             // Check if camera id is changed.
             int cameraId = CameraSettings.readPreferredCameraId(mPreferences);
             if (mCameraId != cameraId) {
                 // Restart the activity to have a crossfade animation.
                 // TODO: Use SurfaceTexture to implement a better and faster
                 // animation.
                 if (mIsVideoCaptureIntent) {
                     // If the intent is video capture, stay in video capture mode.
                     MenuHelper.gotoVideoMode(this, getIntent());
                 } else {
                     MenuHelper.gotoVideoMode(this);
                 }
                 finish();
             } else {
                 readVideoPreferences();
                 // We need to restart the preview if preview size is changed.
                 Size size = mParameters.getPreviewSize();
                 if (size.width != mDesiredPreviewWidth
                         || size.height != mDesiredPreviewHeight) {
                     mCameraDevice.stopPreview();
                     resizeForPreviewAspectRatio();
                     startPreview(); // Parameters will be set in startPreview().
                 } else {
                     setCameraParameters();
                 }
             }
             showTimeLapseUI(mCaptureTimeLapse);
         }
     }
 
     private void showTimeLapseUI(boolean enable) {
         if (mTimeLapseLabel != null) {
             mTimeLapseLabel.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
         }
         if (mPreviewBorder != null) {
             mPreviewBorder.setBackgroundResource(enable
                     ? R.drawable.border_preview_time_lapse_holo
                     : R.drawable.border_preview_holo);
         }
 
     }
 
     private class MyIndicatorWheelListener implements IndicatorWheel.Listener {
         public void onSharedPreferenceChanged() {
             VideoCamera.this.onSharedPreferenceChanged();
         }
 
         public void onRestorePreferencesClicked() {
             VideoCamera.this.onRestorePreferencesClicked();
         }
 
         public void onOverriddenPreferencesClicked() {
         }
     }
 
     private class MyCameraPickerListener implements CameraPicker.Listener {
         public void onSharedPreferenceChanged() {
             VideoCamera.this.onSharedPreferenceChanged();
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
 
     private int mLocation[] = new int[2];
     private class PopupGestureListener extends
             GestureDetector.SimpleOnGestureListener {
         @Override
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
 }

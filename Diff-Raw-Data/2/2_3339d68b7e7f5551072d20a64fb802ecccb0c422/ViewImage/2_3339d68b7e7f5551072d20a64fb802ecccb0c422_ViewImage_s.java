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
 import com.android.camera.gallery.VideoObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ZoomButtonsController;
 
 import java.util.Random;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 
 // This activity can display a whole picture and navigate them in a specific
 // gallery. It has two modes: normal mode and slide show mode. In normal mode
 // the user view one image at a time, and can click "previous" and "next"
 // button to see the previous or next image. In slide show mode it shows one
 // image after another, with some transition effect.
 public class ViewImage extends Activity implements View.OnClickListener {
     private static final String PREF_SLIDESHOW_REPEAT =
             "pref_gallery_slideshow_repeat_key";
     private static final String PREF_SHUFFLE_SLIDESHOW =
             "pref_gallery_slideshow_shuffle_key";
     private static final String STATE_URI = "uri";
     private static final String STATE_SLIDESHOW = "slideshow";
     private static final String EXTRA_SLIDESHOW = "slideshow";
     private static final String TAG = "ViewImage";
 
     private static final boolean AUTO_DISMISS = true;
     private static final boolean NO_AUTO_DISMISS = false;
 
     private ImageGetter mGetter;
     private Uri mSavedUri;
 
     // Choices for what adjacents to load.
     private static final int[] sOrderAdjacents = new int[] {0, 1, -1};
     private static final int[] sOrderSlideshow = new int[] {0};
 
     final LocalHandler mHandler = new LocalHandler();
 
     private final Random mRandom = new Random(System.currentTimeMillis());
     private int [] mShuffleOrder = null;
     private boolean mUseShuffleOrder = false;
     private boolean mSlideShowLoop = false;
 
     static final int MODE_NORMAL = 1;
     static final int MODE_SLIDESHOW = 2;
     private int mMode = MODE_NORMAL;
 
     private boolean mFullScreenInNormalMode;
     private boolean mShowActionIcons;
     private View mActionIconPanel;
 
     private int mSlideShowInterval;
     private int mLastSlideShowImage;
     private ThumbnailController mThumbController;
     int mCurrentPosition = 0;
 
     // represents which style animation to use
     private int mAnimationIndex;
     private Animation [] mSlideShowInAnimation;
     private Animation [] mSlideShowOutAnimation;
 
     private SharedPreferences mPrefs;
 
     private View mNextImageView;
     private View mPrevImageView;
     private final Animation mHideNextImageViewAnimation = new AlphaAnimation(1F, 0F);
     private final Animation mHidePrevImageViewAnimation = new AlphaAnimation(1F, 0F);
     private final Animation mShowNextImageViewAnimation = new AlphaAnimation(0F, 1F);
     private final Animation mShowPrevImageViewAnimation = new AlphaAnimation(0F, 1F);
 
     static final int PADDING = 20;
     static final int HYSTERESIS = PADDING * 2;
     static final int BASE_SCROLL_DURATION = 1000; // ms
 
     public static final String KEY_IMAGE_LIST = "image_list";
 
     IImageList mAllImages;
 
     private int mSlideShowImageCurrent = 0;
     private final ImageViewTouchBase [] mSlideShowImageViews =
             new ImageViewTouchBase[2];
 
     GestureDetector mGestureDetector;
     private ZoomButtonsController mZoomButtonsController;
 
     // The image view displayed for normal mode.
     private ImageViewTouch mImageView;
     // This is the cache for thumbnail bitmaps.
     private BitmapCache mCache;
     private MenuHelper.MenuItemsResult mImageMenuRunnable;
 
     private Runnable mDismissOnScreenControlsRunnable;
     private boolean mCameraReviewMode;
 
     private void updateNextPrevControls() {
         boolean showPrev = mCurrentPosition > 0;
         boolean showNext = mCurrentPosition < mAllImages.getCount() - 1;
 
         boolean prevIsVisible = mPrevImageView.getVisibility() == View.VISIBLE;
         boolean nextIsVisible = mNextImageView.getVisibility() == View.VISIBLE;
 
         if (showPrev && !prevIsVisible) {
             Animation a = mShowPrevImageViewAnimation;
             a.setDuration(500);
             mPrevImageView.startAnimation(a);
             mPrevImageView.setVisibility(View.VISIBLE);
         } else if (!showPrev && prevIsVisible) {
             Animation a = mHidePrevImageViewAnimation;
             a.setDuration(500);
             mPrevImageView.startAnimation(a);
             mPrevImageView.setVisibility(View.GONE);
         }
 
         if (showNext && !nextIsVisible) {
             Animation a = mShowNextImageViewAnimation;
             a.setDuration(500);
             mNextImageView.startAnimation(a);
             mNextImageView.setVisibility(View.VISIBLE);
         } else if (!showNext && nextIsVisible) {
             Animation a = mHideNextImageViewAnimation;
             a.setDuration(500);
             mNextImageView.startAnimation(a);
             mNextImageView.setVisibility(View.GONE);
         }
     }
 
     private void showOnScreenControls(final boolean autoDismiss) {
         // If the view has not been attached to the window yet, the
         // zoomButtonControls will not able to show up. So delay it until the
         // view has attached to window.
         if (mActionIconPanel.getWindowToken() == null) {
             mHandler.postGetterCallback(new Runnable() {
                 public void run() {
                     showOnScreenControls(autoDismiss);
                 }
             });
             return;
         }
         mHandler.removeCallbacks(mDismissOnScreenControlsRunnable);
         updateNextPrevControls();
 
         IImage image = mAllImages.getImageAt(mCurrentPosition);
         if (image instanceof VideoObject) {
             mZoomButtonsController.setVisible(false);
         } else {
             updateZoomButtonsEnabled();
             mZoomButtonsController.setVisible(true);
         }
 
         if (mShowActionIcons
                 && mActionIconPanel.getVisibility() != View.VISIBLE) {
             Animation animation = new AlphaAnimation(0, 1);
             animation.setDuration(500);
             mActionIconPanel.startAnimation(animation);
             mActionIconPanel.setVisibility(View.VISIBLE);
         }
         if (autoDismiss) scheduleDismissOnScreenControls();
     }
 
     @Override
     public boolean dispatchTouchEvent(MotionEvent m) {
         boolean sup = super.dispatchTouchEvent(m);
 
         // This is a hack to show the on screen controls. We should make sure
         // this event is not handled by others(ie. sup == false), and listen for
         // the events on zoom/prev/next buttons.
         // However, since we have no other pressable views, it is OK now.
         // TODO: Fix the above issue.
         if (mMode == MODE_NORMAL) {
             switch (m.getAction()) {
                 case MotionEvent.ACTION_DOWN:
                     showOnScreenControls(NO_AUTO_DISMISS);
                     break;
                 case MotionEvent.ACTION_UP:
                     scheduleDismissOnScreenControls();
                     break;
             }
         }
 
         if (sup == false) {
             mGestureDetector.onTouchEvent(m);
             return true;
         }
         return true;
     }
 
     private void scheduleDismissOnScreenControls() {
         mHandler.removeCallbacks(mDismissOnScreenControlsRunnable);
         mHandler.postDelayed(mDismissOnScreenControlsRunnable, 1500);
     }
 
     private void updateZoomButtonsEnabled() {
         ImageViewTouch imageView = mImageView;
         float scale = imageView.getScale();
         mZoomButtonsController.setZoomInEnabled(scale < imageView.mMaxZoom);
         mZoomButtonsController.setZoomOutEnabled(scale > 1);
     }
 
     @Override
     protected void onDestroy() {
 
         // This is necessary to make the ZoomButtonsController unregister
         // its configuration change receiver.
         if (mZoomButtonsController != null) {
             mZoomButtonsController.setVisible(false);
         }
 
         super.onDestroy();
     }
 
     private void setupZoomButtonController(View rootView) {
         mGestureDetector = new GestureDetector(this, new MyGestureListener());
         mZoomButtonsController = new ZoomButtonsController(rootView);
         mZoomButtonsController.setAutoDismissed(false);
         mZoomButtonsController.setOnZoomListener(
                 new ZoomButtonsController.OnZoomListener() {
             public void onVisibilityChanged(boolean visible) {
                 if (visible) {
                     updateZoomButtonsEnabled();
                 }
             }
 
             public void onZoom(boolean zoomIn) {
                 if (zoomIn) {
                     mImageView.zoomIn();
                 } else {
                     mImageView.zoomOut();
                 }
                 updateZoomButtonsEnabled();
             }
         });
     }
 
     private class MyGestureListener extends
             GestureDetector.SimpleOnGestureListener {
 
         @Override
         public boolean onScroll(MotionEvent e1, MotionEvent e2,
                 float distanceX, float distanceY) {
             ImageViewTouch imageView = mImageView;
             if (imageView.getScale() > 1F) {
                 imageView.postTranslateCenter(-distanceX, -distanceY);
             }
             return true;
         }
 
         @Override
         public boolean onSingleTapUp(MotionEvent e) {
             setMode(MODE_NORMAL);
             return true;
         }
     }
 
     private void setupDismissOnScreenControlRunnable() {
         mDismissOnScreenControlsRunnable = new Runnable() {
             public void run() {
                 if (mNextImageView.getVisibility() == View.VISIBLE) {
                     Animation a = mHideNextImageViewAnimation;
                     a.setDuration(500);
                     mNextImageView.startAnimation(a);
                     mNextImageView.setVisibility(View.INVISIBLE);
                 }
 
                 if (mPrevImageView.getVisibility() == View.VISIBLE) {
                     Animation a = mHidePrevImageViewAnimation;
                     a.setDuration(500);
                     mPrevImageView.startAnimation(a);
                     mPrevImageView.setVisibility(View.INVISIBLE);
                 }
                 mZoomButtonsController.setVisible(false);
 
                 if (mShowActionIcons
                         && mActionIconPanel.getVisibility() == View.VISIBLE) {
                     Animation animation = new AlphaAnimation(1, 0);
                     animation.setDuration(500);
                     mActionIconPanel.startAnimation(animation);
                     mActionIconPanel.setVisibility(View.INVISIBLE);
                 }
             }
         };
     }
 
     boolean isPickIntent() {
         String action = getIntent().getAction();
         return (Intent.ACTION_PICK.equals(action)
                 || Intent.ACTION_GET_CONTENT.equals(action));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         if (!mCameraReviewMode) {
             MenuItem item = menu.add(Menu.CATEGORY_SECONDARY, 203, 0,
                                      R.string.slide_show);
             item.setOnMenuItemClickListener(
                     new MenuItem.OnMenuItemClickListener() {
                 public boolean onMenuItemClick(MenuItem item) {
                     setMode(MODE_SLIDESHOW);
                     mLastSlideShowImage = mCurrentPosition;
                     loadNextImage(mCurrentPosition, 0, true);
                     return true;
                 }
             });
             item.setIcon(android.R.drawable.ic_menu_slideshow);
         }
 
         final SelectedImageGetter selectedImageGetter =
                 new SelectedImageGetter() {
             public IImage getCurrentImage() {
                 return mAllImages.getImageAt(mCurrentPosition);
             }
 
             public Uri getCurrentImageUri() {
                 return mAllImages.getImageAt(mCurrentPosition)
                         .fullSizeImageUri();
             }
         };
 
         mImageMenuRunnable = MenuHelper.addImageMenuItems(
                 menu,
                 MenuHelper.INCLUDE_ALL,
                 true,
                 ViewImage.this,
                 mHandler,
                 mDeletePhotoRunnable,
                 new MenuHelper.MenuInvoker() {
                     public void run(final MenuHelper.MenuCallback cb) {
                         setMode(MODE_NORMAL);
                         Thread t = new Thread() {
                             @Override
                             public void run() {
                                cb.run(selectedImageGetter.getCurrentImageUri(),
                                        selectedImageGetter.getCurrentImage());
                                mHandler.post(new Runnable() {
                                  public void run() {
                                      mImageView.clear();
                                      setImage(mCurrentPosition);
                                  }
                                });
                             }
                         };
                         t.start();
                     }
                 });
 
         if (true) {
             MenuItem item = menu.add(Menu.CATEGORY_SECONDARY, 203, 1000,
                     R.string.camerasettings);
             item.setOnMenuItemClickListener(
                     new MenuItem.OnMenuItemClickListener() {
                 public boolean onMenuItemClick(MenuItem item) {
                     Intent preferences = new Intent();
                     preferences.setClass(ViewImage.this, GallerySettings.class);
                     startActivity(preferences);
                     return true;
                 }
             });
             item.setAlphabeticShortcut('p');
             item.setIcon(android.R.drawable.ic_menu_preferences);
         }
 
         // Hidden menu just so the shortcut will bring up the zoom controls
         // the string resource is a placeholder
         menu.add(Menu.CATEGORY_SECONDARY, 203, 0, R.string.camerasettings)
                 .setOnMenuItemClickListener(
                 new MenuItem.OnMenuItemClickListener() {
             public boolean onMenuItemClick(MenuItem item) {
                 showOnScreenControls(AUTO_DISMISS);
                 return true;
             }
         })
         .setAlphabeticShortcut('z')
         .setVisible(false);
 
         return true;
     }
 
     protected Runnable mDeletePhotoRunnable = new Runnable() {
         public void run() {
             mAllImages.removeImageAt(mCurrentPosition);
             if (mCameraReviewMode) {
                 updateLastImage();
                 mThumbController.updateDisplayIfNeeded();
             }
             if (mAllImages.getCount() == 0) {
                 finish();
             } else {
                 if (mCurrentPosition == mAllImages.getCount()) {
                     mCurrentPosition -= 1;
                 }
             }
             mImageView.clear();
             mCache.clear();  // Because the position number is changed.
             setImage(mCurrentPosition);
         }
     };
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
         setMode(MODE_NORMAL);
 
         if (mImageMenuRunnable != null) {
             mImageMenuRunnable.gettingReadyToOpen(menu,
                     mAllImages.getImageAt(mCurrentPosition));
         }
 
         Uri uri = mAllImages.getImageAt(mCurrentPosition).fullSizeImageUri();
         MenuHelper.enableShareMenuItem(menu, !MenuHelper.isMMSUri(uri));
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         boolean b = super.onMenuItemSelected(featureId, item);
         if (mImageMenuRunnable != null) {
             mImageMenuRunnable.aboutToCall(item,
                     mAllImages.getImageAt(mCurrentPosition));
         }
         return b;
     }
 
     void setImage(int pos) {
         mCurrentPosition = pos;
 
         Bitmap b = mCache.getBitmap(pos);
         if (b != null) {
             mImageView.setImageBitmapResetBase(b, true);
             updateZoomButtonsEnabled();
         }
 
         ImageGetterCallback cb = new ImageGetterCallback() {
             public void completed(boolean wasCanceled) {
                 if (!mShowActionIcons) {
                     mImageView.setFocusableInTouchMode(true);
                     mImageView.requestFocus();
                 }
             }
 
             public boolean wantsThumbnail(int pos, int offset) {
                 return !mCache.hasBitmap(pos + offset);
             }
 
             public boolean wantsFullImage(int pos, int offset) {
                 return offset == 0;
             }
 
             public int fullImageSizeToUse(int pos, int offset) {
                 // TODO
                 // this number should be bigger so that we can zoom.  we may
                 // need to get fancier and read in the fuller size image as the
                 // user starts to zoom.  use -1 to get the full full size image.
                 // for now use 480 so we don't run out of memory
                 final int imageViewSize = 480;
                 return imageViewSize;
             }
 
             public int [] loadOrder() {
                 return sOrderAdjacents;
             }
 
             public void imageLoaded(int pos, int offset, Bitmap bitmap,
                                     boolean isThumb) {
                 // shouldn't get here after onPause()
 
                 // We may get a result from a previous request. Ignore it.
                 if (pos != mCurrentPosition) {
                     bitmap.recycle();
                     return;
                 }
 
                 if (isThumb) {
                     mCache.put(pos + offset, bitmap);
                 }
                 if (offset == 0) {
                     // isThumb: We always load thumb bitmap first, so we will
                     // reset the supp matrix for then thumb bitmap, and keep
                     // the supp matrix when the full bitmap is loaded.
                     mImageView.setImageBitmapResetBase(bitmap, isThumb);
                     updateZoomButtonsEnabled();
                 }
             }
         };
 
         // Could be null if we're stopping a slide show in the course of pausing
         if (mGetter != null) {
             mGetter.setPosition(pos, cb);
         }
         updateActionIcons();
         showOnScreenControls(AUTO_DISMISS);
     }
 
     @Override
     public void onCreate(Bundle instanceState) {
         super.onCreate(instanceState);
 
         Intent intent = getIntent();
         mCameraReviewMode = intent.getBooleanExtra(
                 "com.android.camera.ReviewMode", false);
         mFullScreenInNormalMode = intent.getBooleanExtra(
                 MediaStore.EXTRA_FULL_SCREEN, true);
         mShowActionIcons = intent.getBooleanExtra(
                 MediaStore.EXTRA_SHOW_ACTION_ICONS, true);
 
         mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.viewimage);
 
         mImageView = (ImageViewTouch) findViewById(R.id.image);
         mImageView.setEnableTrackballScroll(!mShowActionIcons);
         mCache = new BitmapCache(3);
         mImageView.setRecycler(mCache);
 
         makeGetter();
 
         mAnimationIndex = -1;
 
         mSlideShowInAnimation = new Animation[] {
             makeInAnimation(R.anim.transition_in),
             makeInAnimation(R.anim.slide_in),
             makeInAnimation(R.anim.slide_in_vertical),
         };
 
         mSlideShowOutAnimation = new Animation[] {
             makeOutAnimation(R.anim.transition_out),
             makeOutAnimation(R.anim.slide_out),
             makeOutAnimation(R.anim.slide_out_vertical),
         };
 
         mSlideShowImageViews[0] =
                 (ImageViewTouchBase) findViewById(R.id.image1_slideShow);
         mSlideShowImageViews[1] =
                 (ImageViewTouchBase) findViewById(R.id.image2_slideShow);
         for (ImageViewTouchBase v : mSlideShowImageViews) {
             v.setVisibility(View.INVISIBLE);
             v.setRecycler(mCache);
         }
 
         mActionIconPanel = findViewById(R.id.action_icon_panel);
 
         Uri uri = getIntent().getData();
         IImageList imageList = getIntent().getParcelableExtra(KEY_IMAGE_LIST);
         boolean slideshow = intent.getBooleanExtra(EXTRA_SLIDESHOW, false);
 
         if (instanceState != null) {
             uri = instanceState.getParcelable(STATE_URI);
             slideshow = instanceState.getBoolean(STATE_SLIDESHOW, false);
         }
 
         if (!init(uri, imageList)) {
             finish();
             return;
         }
 
         // We don't show action icons for MMS uri because we don't have
         // delete and share action icons for MMS. It is obvious that we don't
         // need the "delete" action, but for the share part, although we get
         // read permission (for the image) from the MMS application, we cannot
         // pass the permission to other activities due to the current framework
         // design.
         if (MenuHelper.isMMSUri(uri)) {
             mShowActionIcons = false;
         }
 
         if (mShowActionIcons) {
             int[] pickIds = {R.id.attach, R.id.cancel};
             int[] reviewIds = {R.id.gallery, R.id.setas, R.id.play, R.id.share,
                     R.id.discard};
             int[] normalIds = {R.id.setas, R.id.play, R.id.share, R.id.discard};
             int[] connectIds = isPickIntent()
                     ? pickIds
                     : mCameraReviewMode ? reviewIds : normalIds;
             for (int id : connectIds) {
                 View view = mActionIconPanel.findViewById(id);
                 view.setVisibility(View.VISIBLE);
                 view.setOnClickListener(this);
             }
         }
 
         if (slideshow) {
             setMode(MODE_SLIDESHOW);
         } else {
             if (mFullScreenInNormalMode) {
                 getWindow().addFlags(
                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
             }
             if (mShowActionIcons) {
                 mActionIconPanel.setVisibility(View.VISIBLE);
             }
         }
 
         setupZoomButtonController(findViewById(R.id.mainPanel));
         setupDismissOnScreenControlRunnable();
 
         mNextImageView = findViewById(R.id.next_image);
         mPrevImageView = findViewById(R.id.prev_image);
         mNextImageView.setOnClickListener(this);
         mPrevImageView.setOnClickListener(this);
 
         if (mShowActionIcons) {
             mNextImageView.setFocusable(true);
             mPrevImageView.setFocusable(true);
         }
 
         if (mCameraReviewMode) {
             ViewGroup buttonBar = (ViewGroup) findViewById(R.id.button_bar);
             buttonBar.setVisibility(View.VISIBLE);
             buttonBar.findViewById(
                     R.id.review_indicator).setVisibility(View.VISIBLE);
             ImageView reviewButton = (ImageView)
                     buttonBar.findViewById(R.id.review_button);
             reviewButton.setClickable(false);
             reviewButton.setFocusable(false);
             mThumbController = new ThumbnailController(
                     reviewButton, getContentResolver());
             buttonBar.findViewById(R.id.camera_button).setOnClickListener(this);
             buttonBar.findViewById(R.id.video_button).setOnClickListener(this);
         }
     }
 
     private void updateActionIcons() {
         if (isPickIntent()) return;
 
         IImage image = mAllImages.getImageAt(mCurrentPosition);
         View panel = mActionIconPanel;
         if (image instanceof VideoObject) {
             panel.findViewById(R.id.setas).setVisibility(View.GONE);
             panel.findViewById(R.id.play).setVisibility(View.VISIBLE);
         } else {
             panel.findViewById(R.id.setas).setVisibility(View.VISIBLE);
             panel.findViewById(R.id.play).setVisibility(View.GONE);
         }
     }
 
     private void updateLastImage() {
         int count = mAllImages.getCount();
         if (count > 0) {
             IImage image = mAllImages.getImageAt(count - 1);
             Uri uri = image.fullSizeImageUri();
             mThumbController.setData(uri, image.miniThumbBitmap());
         } else {
             mThumbController.setData(null, null);
         }
     }
 
     private Animation makeInAnimation(int id) {
         Animation inAnimation = AnimationUtils.loadAnimation(this, id);
         return inAnimation;
     }
 
     private Animation makeOutAnimation(int id) {
         Animation outAnimation = AnimationUtils.loadAnimation(this, id);
         return outAnimation;
     }
 
     private static int getPreferencesInteger(
             SharedPreferences prefs, String key, int defaultValue) {
         String value = prefs.getString(key, null);
         try {
             return value == null ? defaultValue : Integer.parseInt(value);
         } catch (NumberFormatException ex) {
             Log.e(TAG, "couldn't parse preference: " + value, ex);
             return defaultValue;
         }
     }
 
     void setMode(int mode) {
         if (mMode == mode) {
             return;
         }
         View slideshowPanel = findViewById(R.id.slideShowContainer);
         View normalPanel = findViewById(R.id.abs);
 
         Window win = getWindow();
         mMode = mode;
         if (mode == MODE_SLIDESHOW) {
             slideshowPanel.setVisibility(View.VISIBLE);
             normalPanel.setVisibility(View.GONE);
 
             win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                     | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
             mImageView.clear();
             mActionIconPanel.setVisibility(View.GONE);
 
             slideshowPanel.getRootView().requestLayout();
 
             // The preferences we want to read:
             //   mUseShuffleOrder
             //   mSlideShowLoop
             //   mAnimationIndex
             //   mSlideShowInterval
 
             mUseShuffleOrder = mPrefs.getBoolean(PREF_SHUFFLE_SLIDESHOW, false);
             mSlideShowLoop = mPrefs.getBoolean(PREF_SLIDESHOW_REPEAT, false);
             mAnimationIndex = getPreferencesInteger(
                     mPrefs, "pref_gallery_slideshow_transition_key", 0);
             mSlideShowInterval = getPreferencesInteger(
                     mPrefs, "pref_gallery_slideshow_interval_key", 3) * 1000;
             if (mUseShuffleOrder) {
                 generateShuffleOrder();
             }
         } else {
             slideshowPanel.setVisibility(View.GONE);
             normalPanel.setVisibility(View.VISIBLE);
 
             win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
             if (mFullScreenInNormalMode) {
                 win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             } else {
                 win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             }
 
             if (mGetter != null) {
                 mGetter.cancelCurrent();
             }
 
             if (mShowActionIcons) {
                 Animation animation = new AlphaAnimation(0F, 1F);
                 animation.setDuration(500);
                 mActionIconPanel.setAnimation(animation);
                 mActionIconPanel.setVisibility(View.VISIBLE);
             }
 
             ImageViewTouchBase dst = mImageView;
             dst.mLastXTouchPos = -1;
             dst.mLastYTouchPos = -1;
 
             for (ImageViewTouchBase ivt : mSlideShowImageViews) {
                 ivt.clear();
             }
 
             mShuffleOrder = null;
 
             // mGetter null is a proxy for being paused
             if (mGetter != null) {
                 setImage(mCurrentPosition);
             }
         }
     }
 
     private void generateShuffleOrder() {
         if (mShuffleOrder == null
                 || mShuffleOrder.length != mAllImages.getCount()) {
             mShuffleOrder = new int[mAllImages.getCount()];
             for (int i = 0, n = mShuffleOrder.length; i < n; i++) {
                 mShuffleOrder[i] = i;
             }
         }
 
         for (int i = mShuffleOrder.length - 1; i >= 0; i--) {
             int r = mRandom.nextInt(i + 1);
             if (r != i) {
                 int tmp = mShuffleOrder[r];
                 mShuffleOrder[r] = mShuffleOrder[i];
                 mShuffleOrder[i] = tmp;
             }
         }
     }
 
     private void loadNextImage(final int requestedPos, final long delay,
                                final boolean firstCall) {
         if (firstCall && mUseShuffleOrder) {
             generateShuffleOrder();
         }
 
         final long targetDisplayTime = System.currentTimeMillis() + delay;
 
         ImageGetterCallback cb = new ImageGetterCallback() {
             public void completed(boolean wasCanceled) {
             }
 
             public boolean wantsThumbnail(int pos, int offset) {
                 return true;
             }
 
             public boolean wantsFullImage(int pos, int offset) {
                 return false;
             }
 
             public int [] loadOrder() {
                 return sOrderSlideshow;
             }
 
             public int fullImageSizeToUse(int pos, int offset) {
                 return 480; // TODO compute this
             }
 
             public void imageLoaded(final int pos, final int offset,
                     final Bitmap bitmap, final boolean isThumb) {
                 long timeRemaining = Math.max(0,
                         targetDisplayTime - System.currentTimeMillis());
                 mHandler.postDelayedGetterCallback(new Runnable() {
                     public void run() {
                         if (mMode == MODE_NORMAL) {
                             return;
                         }
 
                         ImageViewTouchBase oldView =
                                 mSlideShowImageViews[mSlideShowImageCurrent];
 
                         if (++mSlideShowImageCurrent
                                 == mSlideShowImageViews.length) {
                             mSlideShowImageCurrent = 0;
                         }
 
                         ImageViewTouchBase newView =
                                 mSlideShowImageViews[mSlideShowImageCurrent];
                         newView.setVisibility(View.VISIBLE);
                         newView.setImageBitmapResetBase(bitmap, true);
                         newView.bringToFront();
 
                         int animation = 0;
 
                         if (mAnimationIndex == -1) {
                             int n = mRandom.nextInt(
                                     mSlideShowInAnimation.length);
                             animation = n;
                         } else {
                             animation = mAnimationIndex;
                         }
 
                         Animation aIn = mSlideShowInAnimation[animation];
                         newView.startAnimation(aIn);
                         newView.setVisibility(View.VISIBLE);
 
                         Animation aOut = mSlideShowOutAnimation[animation];
                         oldView.setVisibility(View.INVISIBLE);
                         oldView.startAnimation(aOut);
 
                         mCurrentPosition = requestedPos;
 
                         if (mCurrentPosition == mLastSlideShowImage
                                 && !firstCall) {
                             if (mSlideShowLoop) {
                                 if (mUseShuffleOrder) {
                                     generateShuffleOrder();
                                 }
                             } else {
                                 setMode(MODE_NORMAL);
                                 return;
                             }
                         }
 
                         loadNextImage(
                                 (mCurrentPosition + 1) % mAllImages.getCount(),
                                 mSlideShowInterval, false);
                     }
                 }, timeRemaining);
             }
         };
         // Could be null if we're stopping a slide show in the course of pausing
         if (mGetter != null) {
             int pos = requestedPos;
             if (mShuffleOrder != null) {
                 pos = mShuffleOrder[pos];
             }
             mGetter.setPosition(pos, cb);
         }
     }
 
     private void makeGetter() {
         mGetter = new ImageGetter(this);
     }
 
     private IImageList buildImageListFromUri(Uri uri) {
         String sortOrder = mPrefs.getString(
                 "pref_gallery_sort_key", "descending");
         int sort = (mCameraReviewMode || sortOrder.equals("ascending"))
                 ? ImageManager.SORT_ASCENDING
                 : ImageManager.SORT_DESCENDING;
         return ImageManager.makeImageList(uri, getContentResolver(), sort);
     }
 
     private boolean init(Uri uri, IImageList imageList) {
         if (uri == null) return false;
         mAllImages = (imageList == null)
                 ? buildImageListFromUri(uri)
                 : imageList;
         mAllImages.open(getContentResolver());
         IImage image = mAllImages.getImageForUri(uri);
         if (image == null) return false;
         mCurrentPosition = mAllImages.getImageIndex(image);
         mLastSlideShowImage = mCurrentPosition;
         return true;
     }
 
     private Uri getCurrentUri() {
         IImage image = mAllImages.getImageAt(mCurrentPosition);
         return image.fullSizeImageUri();
     }
 
     @Override
     public void onSaveInstanceState(Bundle b) {
         super.onSaveInstanceState(b);
         b.putParcelable(STATE_URI,
                 mAllImages.getImageAt(mCurrentPosition).fullSizeImageUri());
         b.putBoolean(STATE_SLIDESHOW, mMode == MODE_SLIDESHOW);
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         init(mSavedUri, mAllImages);
         if (mCameraReviewMode) {
             updateLastImage();
         }
 
         // normally this will never be zero but if one "backs" into this
         // activity after removing the sdcard it could be zero.  in that
         // case just "finish" since there's nothing useful that can happen.
         int count = mAllImages.getCount();
         if (count == 0) {
             finish();
         } else if (count <= mCurrentPosition) {
             mCurrentPosition = count - 1;
         }
 
         if (mGetter == null) {
             makeGetter();
         }
 
         if (mMode == MODE_SLIDESHOW) {
             loadNextImage(mCurrentPosition, 0, true);
         } else {  // MODE_NORMAL
             setImage(mCurrentPosition);
         }
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         mGetter.cancelCurrent();
         mGetter.stop();
         mGetter = null;
         setMode(MODE_NORMAL);
 
         // removing all callback in the message queue
         mHandler.removeAllGetterCallbacks();
 
         mSavedUri = getCurrentUri();
 
         mAllImages.deactivate();
         mDismissOnScreenControlsRunnable.run();
         if (mDismissOnScreenControlsRunnable != null) {
             mHandler.removeCallbacks(mDismissOnScreenControlsRunnable);
         }
 
         mImageView.clear();
         mCache.clear();
 
         for (ImageViewTouchBase iv : mSlideShowImageViews) {
             iv.clear();
         }
     }
 
     private void startShareMediaActivity(IImage image) {
         boolean isVideo = image instanceof VideoObject;
         Intent intent = new Intent();
         intent.setAction(Intent.ACTION_SEND);
         intent.setType(image.getMimeType());
         intent.putExtra(Intent.EXTRA_STREAM, image.fullSizeImageUri());
         try {
             startActivity(Intent.createChooser(intent, getText(
                     isVideo ? R.string.sendVideo : R.string.sendImage)));
         } catch (android.content.ActivityNotFoundException ex) {
             Toast.makeText(this, isVideo
                     ? R.string.no_way_to_share_image
                     : R.string.no_way_to_share_video,
                     Toast.LENGTH_SHORT).show();
         }
     }
 
     private void startPlayVideoActivity() {
         IImage image = mAllImages.getImageAt(mCurrentPosition);
         Intent intent = new Intent(
                 Intent.ACTION_VIEW, image.fullSizeImageUri());
         try {
             startActivity(intent);
         } catch (android.content.ActivityNotFoundException ex) {
             Log.e(TAG, "Couldn't view video " + image.fullSizeImageUri(), ex);
         }
     }
 
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.camera_button:
                 MenuHelper.gotoCameraMode(this);
                 break;
             case R.id.video_button:
                 MenuHelper.gotoVideoMode(this);
                 break;
             case R.id.gallery:
                 MenuHelper.gotoCameraImageGallery(this);
                 break;
             case R.id.discard:
                 MenuHelper.deletePhoto(this, mDeletePhotoRunnable);
                 break;
             case R.id.play:
                 startPlayVideoActivity();
                 break;
             case R.id.share: {
                 IImage image = mAllImages.getImageAt(mCurrentPosition);
                 if (MenuHelper.isMMSUri(image.fullSizeImageUri())) {
                     return;
                 }
                 startShareMediaActivity(image);
                 break;
             }
             case R.id.setas: {
                 Uri u = mAllImages.getImageAt(mCurrentPosition).fullSizeImageUri();
                 Intent intent = new Intent(Intent.ACTION_ATTACH_DATA, u);
                 try {
                     startActivity(Intent.createChooser(
                             intent, getText(R.string.setImage)));
                 } catch (android.content.ActivityNotFoundException ex) {
                     Toast.makeText(this, R.string.no_way_to_share_video,
                             Toast.LENGTH_SHORT).show();
                 }
                 break;
             }
             case R.id.next_image:
                 moveNextOrPrevious(1);
                 break;
             case R.id.prev_image:
                 moveNextOrPrevious(-1);
                 break;
         }
     }
 
     private void moveNextOrPrevious(int delta) {
         int nextImagePos = mCurrentPosition + delta;
         if ((0 <= nextImagePos) && (nextImagePos < mAllImages.getCount())) {
             setImage(nextImagePos);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         switch (requestCode) {
             case MenuHelper.RESULT_COMMON_MENU_CROP:
                 if (resultCode == RESULT_OK) {
                     // The CropImage activity passes back the Uri of the
                     // cropped image as the Action rather than the Data.
                     mSavedUri = Uri.parse(data.getAction());
                 }
                 break;
         }
     }
 
     static class LocalHandler extends Handler {
         private static final int IMAGE_GETTER_CALLBACK = 1;
 
         @Override
         public void handleMessage(Message message) {
             switch(message.what) {
                 case IMAGE_GETTER_CALLBACK:
                     ((Runnable) message.obj).run();
                     break;
             }
         }
 
         public void postGetterCallback(Runnable callback) {
            postDelayedGetterCallback(callback, 0);
         }
 
         public void postDelayedGetterCallback(Runnable callback, long delay) {
             if (callback == null) {
                 throw new NullPointerException();
             }
             Message message = Message.obtain();
             message.what = IMAGE_GETTER_CALLBACK;
             message.obj = callback;
             sendMessageDelayed(message, delay);
         }
 
         public void removeAllGetterCallbacks() {
             removeMessages(IMAGE_GETTER_CALLBACK);
         }
     }
 }
 
 class ImageViewTouch extends ImageViewTouchBase {
     private final ViewImage mViewImage;
     private boolean mEnableTrackballScroll;
 
     public ImageViewTouch(Context context) {
         super(context);
         mViewImage = (ViewImage) context;
     }
 
     public ImageViewTouch(Context context, AttributeSet attrs) {
         super(context, attrs);
         mViewImage = (ViewImage) context;
     }
 
     public void setEnableTrackballScroll(boolean enable) {
         mEnableTrackballScroll = enable;
     }
 
     protected void postTranslateCenter(float dx, float dy) {
         super.postTranslate(dx, dy);
         center(true, true);
     }
 
     static final float PAN_RATE = 20;
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Don't respond to arrow keys if trackball scrolling is not enabled
         if (!mEnableTrackballScroll) {
             if ((keyCode >= KeyEvent.KEYCODE_DPAD_UP)
                     && (keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT)) {
                 return super.onKeyDown(keyCode, event);
             }
         }
 
         int current = mViewImage.mCurrentPosition;
 
         int nextImagePos = -2; // default no next image
         try {
             switch (keyCode) {
                 case KeyEvent.KEYCODE_DPAD_CENTER: {
                     if (mViewImage.isPickIntent()) {
                         IImage img = mViewImage.mAllImages
                                 .getImageAt(mViewImage.mCurrentPosition);
                         mViewImage.setResult(ViewImage.RESULT_OK,
                                  new Intent().setData(img.fullSizeImageUri()));
                         mViewImage.finish();
                     }
                     break;
                 }
                 case KeyEvent.KEYCODE_DPAD_LEFT: {
                     int maxOffset = (current == 0) ? 0 : ViewImage.HYSTERESIS;
                     if (getScale() <= 1F
                             || isShiftedToNextImage(true, maxOffset)) {
                         nextImagePos = current - 1;
                     } else {
                         panBy(PAN_RATE, 0);
                         center(true, false);
                     }
                     return true;
                 }
                 case KeyEvent.KEYCODE_DPAD_RIGHT: {
                     int maxOffset =
                             (current == mViewImage.mAllImages.getCount() - 1)
                             ? 0
                             : ViewImage.HYSTERESIS;
                     if (getScale() <= 1F
                             || isShiftedToNextImage(false, maxOffset)) {
                         nextImagePos = current + 1;
                     } else {
                         panBy(-PAN_RATE, 0);
                         center(true, false);
                     }
                     return true;
                 }
                 case KeyEvent.KEYCODE_DPAD_UP: {
                     panBy(0, PAN_RATE);
                     center(false, true);
                     return true;
                 }
                 case KeyEvent.KEYCODE_DPAD_DOWN: {
                     panBy(0, -PAN_RATE);
                     center(false, true);
                     return true;
                 }
                 case KeyEvent.KEYCODE_DEL:
                     MenuHelper.deletePhoto(
                             mViewImage, mViewImage.mDeletePhotoRunnable);
                     break;
             }
         } finally {
             if (nextImagePos >= 0
                     && nextImagePos < mViewImage.mAllImages.getCount()) {
                 synchronized (mViewImage) {
                     mViewImage.setMode(ViewImage.MODE_NORMAL);
                     mViewImage.setImage(nextImagePos);
                 }
            } else if (nextImagePos != -2) {
                center(true, true);
            }
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     protected boolean isShiftedToNextImage(boolean left, int maxOffset) {
         boolean retval;
         Bitmap bitmap = mBitmapDisplayed;
         Matrix m = getImageViewMatrix();
         if (left) {
             float [] t1 = new float[] { 0, 0 };
             m.mapPoints(t1);
             retval = t1[0] > maxOffset;
         } else {
             int width = bitmap != null ? bitmap.getWidth() : getWidth();
             float [] t1 = new float[] { width, 0 };
             m.mapPoints(t1);
             retval = t1[0] + maxOffset < getWidth();
         }
         return retval;
     }
 }
 
 /*
  * Here's the loading strategy.  For any given image, load the thumbnail
  * into memory and post a callback to display the resulting bitmap.
  *
  * Then proceed to load the full image bitmap.   Three things can
  * happen at this point:
  *
  * 1.  the image fails to load because the UI thread decided
  * to move on to a different image.  This "cancellation" happens
  * by virtue of the UI thread closing the stream containing the
  * image being decoded.  BitmapFactory.decodeStream returns null
  * in this case.
  *
  * 2.  the image loaded successfully.  At that point we post
  * a callback to the UI thread to actually show the bitmap.
  *
  * 3.  when the post runs it checks to see if the image that was
  * loaded is still the one we want.  The UI may have moved on
  * to some other image and if so we just drop the newly loaded
  * bitmap on the floor.
  */
 
 interface ImageGetterCallback {
     public void imageLoaded(int pos, int offset, Bitmap bitmap,
                             boolean isThumb);
     public boolean wantsThumbnail(int pos, int offset);
     public boolean wantsFullImage(int pos, int offset);
     public int fullImageSizeToUse(int pos, int offset);
     public void completed(boolean wasCanceled);
     public int [] loadOrder();
 }
 
 class ImageGetter {
 
     @SuppressWarnings("unused")
     private static final String TAG = "ImageGetter";
 
     // The thread which does the work.
     private final Thread mGetterThread;
 
     // The base position that's being retrieved.  The actual images retrieved
     // are this base plus each of the offets.
     private int mCurrentPosition = -1;
 
     // The callback to invoke for each image.
     private ImageGetterCallback mCB;
 
     // This is the loader cancelable that gets set while we're loading an image.
     // If we change position we can cancel the current load using this.
     private Cancelable<Bitmap> mLoad;
 
     // True if we're canceling the current load.
     private boolean mCancelCurrent = false;
 
     // True when the therad should exit.
     private boolean mDone = false;
 
     // True when the loader thread is waiting for work.
     private boolean mReady = false;
 
     // The ViewImage this ImageGetter belongs to
     ViewImage mViewImage;
 
     void cancelCurrent() {
         synchronized (this) {
             if (!mReady) {
                 mCancelCurrent = true;
                 Cancelable<Bitmap> load = mLoad;
                 if (load != null) {
                     load.requestCancel();
                 }
                 mCancelCurrent = false;
             }
         }
     }
 
     private class ImageGetterRunnable implements Runnable {
         private Runnable callback(final int position, final int offset,
                                   final boolean isThumb, final Bitmap bitmap) {
             return new Runnable() {
                 public void run() {
                     // check for inflight callbacks that aren't applicable
                     // any longer before delivering them
                     if (!isCanceled() && position == mCurrentPosition) {
                         mCB.imageLoaded(position, offset, bitmap, isThumb);
                     } else if (bitmap != null) {
                         bitmap.recycle();
                     }
                 }
             };
         }
 
         private Runnable completedCallback(final boolean wasCanceled) {
             return new Runnable() {
                 public void run() {
                     mCB.completed(wasCanceled);
                 }
             };
         }
 
         public void run() {
             int lastPosition = -1;
             while (!mDone) {
                 synchronized (ImageGetter.this) {
                     mReady = true;
                     ImageGetter.this.notify();
 
                     if (mCurrentPosition == -1
                             || lastPosition == mCurrentPosition) {
                         try {
                             ImageGetter.this.wait();
                         } catch (InterruptedException ex) {
                             continue;
                         }
                     }
 
                     lastPosition = mCurrentPosition;
                     mReady = false;
                 }
 
                 if (lastPosition != -1) {
                     int imageCount = mViewImage.mAllImages.getCount();
 
                     int [] order = mCB.loadOrder();
                     for (int i = 0; i < order.length; i++) {
                         int offset = order[i];
                         int imageNumber = lastPosition + offset;
                         if (imageNumber >= 0 && imageNumber < imageCount) {
                             IImage image = mViewImage.mAllImages
                                     .getImageAt(lastPosition + offset);
                             if (image == null || isCanceled()) {
                                 break;
                             }
                             if (mCB.wantsThumbnail(lastPosition, offset)) {
                                 Bitmap b = image.thumbBitmap();
                                 mViewImage.mHandler.postGetterCallback(
                                         callback(lastPosition, offset,
                                         true, b));
                             }
                         }
                     }
 
                     for (int i = 0; i < order.length; i++) {
                         int offset = order[i];
                         int imageNumber = lastPosition + offset;
                         if (imageNumber >= 0 && imageNumber < imageCount) {
                             IImage image = mViewImage.mAllImages
                                     .getImageAt(lastPosition + offset);
                             if (mCB.wantsFullImage(lastPosition, offset)
                                     && !(image instanceof VideoObject)) {
                                 int sizeToUse = mCB.fullImageSizeToUse(
                                         lastPosition, offset);
                                 if (image != null && !isCanceled()) {
                                     mLoad = image.fullSizeBitmapCancelable(
                                             sizeToUse);
                                 }
                                 if (mLoad != null) {
                                     // The return value could be null if the
                                     // bitmap is too big, or we cancelled it.
                                     Bitmap b;
                                     try {
                                         b = mLoad.get();
                                     } catch (InterruptedException e) {
                                         b = null;
                                     } catch (ExecutionException e) {
                                         throw new RuntimeException(e);
                                     } catch (CancellationException e) {
                                         b = null;
                                     }
                                     mLoad = null;
                                     if (b != null) {
                                         if (isCanceled()) {
                                             b.recycle();
                                         } else {
                                             Runnable cb = callback(
                                                     lastPosition, offset,
                                                     false, b);
                                             mViewImage.mHandler
                                                     .postGetterCallback(cb);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     mViewImage.mHandler.postGetterCallback(
                             completedCallback(isCanceled()));
                 }
             }
         }
     }
 
     public ImageGetter(ViewImage viewImage) {
         mViewImage = viewImage;
         mGetterThread = new Thread(new ImageGetterRunnable());
         mGetterThread.setName("ImageGettter");
         mGetterThread.start();
     }
 
     private boolean isCanceled() {
         synchronized (this) {
             return mCancelCurrent;
         }
     }
 
     public void setPosition(int position, ImageGetterCallback cb) {
         synchronized (this) {
             if (!mReady) {
                 try {
                     mCancelCurrent = true;
                     // if the thread is waiting before loading the full size
                     // image then this will free it up
                     BitmapManager.instance()
                             .cancelThreadDecoding(mGetterThread);
                     ImageGetter.this.notify();
                     ImageGetter.this.wait();
                     BitmapManager.instance()
                             .allowThreadDecoding(mGetterThread);
                     mCancelCurrent = false;
                 } catch (InterruptedException ex) {
                     // not sure what to do here
                 }
             }
         }
 
         mCurrentPosition = position;
         mCB = cb;
 
         synchronized (this) {
             ImageGetter.this.notify();
         }
     }
 
     public void stop() {
         synchronized (this) {
             mDone = true;
             ImageGetter.this.notify();
         }
         try {
             BitmapManager.instance().cancelThreadDecoding(mGetterThread);
             mGetterThread.join();
         } catch (InterruptedException ex) {
             // Ignore the exception
         }
     }
 }
 
 // This is a cache for Bitmap displayed in ViewImage (normal mode, thumb only).
 class BitmapCache implements ImageViewTouchBase.Recycler {
     public static class Entry {
         int mPos;
         Bitmap mBitmap;
         public Entry() {
             clear();
         }
         public void clear() {
             mPos = -1;
             mBitmap = null;
         }
     }
 
     private final Entry[] mCache;
 
     public BitmapCache(int size) {
         mCache = new Entry[size];
         for (int i = 0; i < mCache.length; i++) {
             mCache[i] = new Entry();
         }
     }
 
     // Given the position, find the associated entry. Returns null if there is
     // no such entry.
     private Entry findEntry(int pos) {
         for (Entry e : mCache) {
             if (pos == e.mPos) {
                 return e;
             }
         }
         return null;
     }
 
     // Returns the thumb bitmap if we have it, otherwise return null.
     public synchronized Bitmap getBitmap(int pos) {
         Entry e = findEntry(pos);
         if (e != null) {
             return e.mBitmap;
         }
         return null;
     }
 
     public synchronized void put(int pos, Bitmap bitmap) {
         // First see if we already have this entry.
         if (findEntry(pos) != null) {
             return;
         }
 
         // Find the best entry we should replace.
         // See if there is any empty entry.
         // Otherwise assuming sequential access, kick out the entry with the
         // greatest distance.
         Entry best = null;
         int maxDist = -1;
         for (Entry e : mCache) {
             if (e.mPos == -1) {
                 best = e;
                 break;
             } else {
                 int dist = Math.abs(pos - e.mPos);
                 if (dist > maxDist) {
                     maxDist = dist;
                     best = e;
                 }
             }
         }
 
         // Recycle the image being kicked out.
         // This only works because our current usage is sequential, so we
         // do not happen to recycle the image being displayed.
         if (best.mBitmap != null) {
             best.mBitmap.recycle();
         }
 
         best.mPos = pos;
         best.mBitmap = bitmap;
     }
 
     // Recycle all bitmaps in the cache and clear the cache.
     public synchronized void clear() {
         for (Entry e : mCache) {
             if (e.mBitmap != null) {
                 e.mBitmap.recycle();
             }
             e.clear();
         }
     }
 
     // Returns whether the bitmap is in the cache.
     public synchronized boolean hasBitmap(int pos) {
         Entry e = findEntry(pos);
         return (e != null);
     }
 
     // Recycle the bitmap if it's not in the cache.
     // The input must be non-null.
     public synchronized void recycle(Bitmap b) {
         for (Entry e : mCache) {
             if (e.mPos != -1) {
                 if (e.mBitmap == b) {
                     return;
                 }
             }
         }
         b.recycle();
     }
 }

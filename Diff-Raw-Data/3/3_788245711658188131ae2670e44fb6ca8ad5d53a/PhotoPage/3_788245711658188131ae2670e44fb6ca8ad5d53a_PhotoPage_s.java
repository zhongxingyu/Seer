 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.gallery3d.app;
 
 import android.app.ActionBar.OnMenuVisibilityListener;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ShareActionProvider;
 import android.widget.Toast;
 
 import com.android.gallery3d.R;
 import com.android.gallery3d.data.DataManager;
 import com.android.gallery3d.data.MediaDetails;
 import com.android.gallery3d.data.MediaItem;
 import com.android.gallery3d.data.MediaObject;
 import com.android.gallery3d.data.MediaSet;
 import com.android.gallery3d.data.MtpDevice;
 import com.android.gallery3d.data.Path;
 import com.android.gallery3d.data.SnailSource;
 import com.android.gallery3d.picasasource.PicasaSource;
 import com.android.gallery3d.ui.DetailsHelper;
 import com.android.gallery3d.ui.DetailsHelper.CloseListener;
 import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
 import com.android.gallery3d.ui.GLCanvas;
 import com.android.gallery3d.ui.GLView;
 import com.android.gallery3d.ui.ImportCompleteListener;
 import com.android.gallery3d.ui.MenuExecutor;
 import com.android.gallery3d.ui.PhotoView;
 import com.android.gallery3d.ui.ScreenNail;
 import com.android.gallery3d.ui.ScreenNailHolder;
 import com.android.gallery3d.ui.SelectionManager;
 import com.android.gallery3d.ui.SynchronizedHandler;
 import com.android.gallery3d.ui.UserInteractionListener;
 import com.android.gallery3d.util.GalleryUtils;
 
 public class PhotoPage extends ActivityState
         implements PhotoView.PhotoTapListener, UserInteractionListener {
     private static final String TAG = "PhotoPage";
 
     private static final int MSG_HIDE_BARS = 1;
 
     private static final int HIDE_BARS_TIMEOUT = 3500;
 
     private static final int REQUEST_SLIDESHOW = 1;
     private static final int REQUEST_CROP = 2;
     private static final int REQUEST_CROP_PICASA = 3;
 
     public static final String KEY_MEDIA_SET_PATH = "media-set-path";
     public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
     public static final String KEY_INDEX_HINT = "index-hint";
     public static final String KEY_OPEN_ANIMATION_RECT = "open-animation-rect";
     public static final String KEY_SCREENNAIL_HOLDER = "screennail-holder";
 
     private GalleryApp mApplication;
     private SelectionManager mSelectionManager;
 
     private PhotoView mPhotoView;
     private PhotoPage.Model mModel;
     private DetailsHelper mDetailsHelper;
     private boolean mShowDetails;
     private Path mPendingSharePath;
     private Path mScreenNailItemPath;
 
     // mMediaSet could be null if there is no KEY_MEDIA_SET_PATH supplied.
     // E.g., viewing a photo in gmail attachment
     private MediaSet mMediaSet;
     private Menu mMenu;
 
     private final Intent mResultIntent = new Intent();
     private int mCurrentIndex = 0;
     private Handler mHandler;
     private boolean mShowBars = true;
     private GalleryActionBar mActionBar;
     private MyMenuVisibilityListener mMenuVisibilityListener;
     private PageTapListener mPageTapListener;
     private boolean mIsMenuVisible;
     private boolean mIsInteracting;
     private MediaItem mCurrentPhoto = null;
     private MenuExecutor mMenuExecutor;
     private boolean mIsActive;
     private ShareActionProvider mShareActionProvider;
     private String mSetPathString;
     private ScreenNailHolder mScreenNailHolder;
     private ScreenNail mScreenNail;
 
     private NfcAdapter mNfcAdapter;
 
     public static interface Model extends PhotoView.Model {
         public void resume();
         public void pause();
         public boolean isEmpty();
         public MediaItem getCurrentMediaItem();
         public int getCurrentIndex();
         public void setCurrentPhoto(Path path, int indexHint);
     }
 
     private class MyMenuVisibilityListener implements OnMenuVisibilityListener {
         public void onMenuVisibilityChanged(boolean isVisible) {
             mIsMenuVisible = isVisible;
             refreshHidingMessage();
         }
     }
 
     public interface PageTapListener {
         // Return true if the tap is consumed.
         public boolean onSingleTapUp(int x, int y);
     }
 
     public void setPageTapListener(PageTapListener listener) {
         mPageTapListener = listener;
     }
 
     private final GLView mRootPane = new GLView() {
 
         @Override
         protected void renderBackground(GLCanvas view) {
             view.clearBuffer();
         }
 
         @Override
         protected void onLayout(
                 boolean changed, int left, int top, int right, int bottom) {
             mPhotoView.layout(0, 0, right - left, bottom - top);
             if (mShowDetails) {
                 mDetailsHelper.layout(left, mActionBar.getHeight(), right, bottom);
             }
         }
     };
 
     @Override
     public void onCreate(Bundle data, Bundle restoreState) {
         mActionBar = mActivity.getGalleryActionBar();
         mSelectionManager = new SelectionManager(mActivity, false);
         mMenuExecutor = new MenuExecutor(mActivity, mSelectionManager);
 
         mPhotoView = new PhotoView(mActivity);
         mPhotoView.setPhotoTapListener(this);
         mRootPane.addComponent(mPhotoView);
         mApplication = (GalleryApp)((Activity) mActivity).getApplication();
 
         mSetPathString = data.getString(KEY_MEDIA_SET_PATH);
         mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity.getAndroidContext());
         Path itemPath = Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH));
 
         if (mSetPathString != null) {
             mScreenNailHolder =
                 (ScreenNailHolder) data.getParcelable(KEY_SCREENNAIL_HOLDER);
             if (mScreenNailHolder != null) {
                 mScreenNail = mScreenNailHolder.attach();
 
                 // Get the ScreenNail from ScreenNailHolder and register it.
                 int id = SnailSource.registerScreenNail(mScreenNail);
                 Path screenNailSetPath = SnailSource.getSetPath(id);
                 mScreenNailItemPath = SnailSource.getItemPath(id);
 
                 // Combine the original MediaSet with the one for CameraScreenNail.
                 mSetPathString = "/combo/item/{" + screenNailSetPath +
                         "," + mSetPathString + "}";
 
                 // Start from the screen nail.
                 itemPath = mScreenNailItemPath;
             }
 
             mMediaSet = mActivity.getDataManager().getMediaSet(mSetPathString);
             mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
             mMediaSet = (MediaSet)
                     mActivity.getDataManager().getMediaObject(mSetPathString);
             if (mMediaSet == null) {
                 Log.w(TAG, "failed to restore " + mSetPathString);
             }
             PhotoDataAdapter pda = new PhotoDataAdapter(
                     mActivity, mPhotoView, mMediaSet, itemPath, mCurrentIndex);
             mModel = pda;
             mPhotoView.setModel(mModel);
 
             mResultIntent.putExtra(KEY_INDEX_HINT, mCurrentIndex);
             setStateResult(Activity.RESULT_OK, mResultIntent);
 
             pda.setDataListener(new PhotoDataAdapter.DataListener() {
 
                 @Override
                 public void onPhotoChanged(int index, Path item) {
                     mCurrentIndex = index;
                     mResultIntent.putExtra(KEY_INDEX_HINT, index);
                     if (item != null) {
                         mResultIntent.putExtra(KEY_MEDIA_ITEM_PATH, item.toString());
                         MediaItem photo = mModel.getCurrentMediaItem();
                         if (photo != null) updateCurrentPhoto(photo);
                     } else {
                         mResultIntent.removeExtra(KEY_MEDIA_ITEM_PATH);
                     }
                     setStateResult(Activity.RESULT_OK, mResultIntent);
                 }
 
                 @Override
                 public void onLoadingFinished() {
                     GalleryUtils.setSpinnerVisibility((Activity) mActivity, false);
                     if (!mModel.isEmpty()) {
                         MediaItem photo = mModel.getCurrentMediaItem();
                         if (photo != null) updateCurrentPhoto(photo);
                     } else if (mIsActive) {
                         mActivity.getStateManager().finishState(PhotoPage.this);
                     }
                 }
 
                 @Override
                 public void onLoadingStarted() {
                     GalleryUtils.setSpinnerVisibility((Activity) mActivity, true);
                 }
             });
         } else {
             // Get default media set by the URI
             MediaItem mediaItem = (MediaItem)
                     mActivity.getDataManager().getMediaObject(itemPath);
             mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
             mPhotoView.setModel(mModel);
             updateCurrentPhoto(mediaItem);
         }
 
         mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
             @Override
             public void handleMessage(Message message) {
                 switch (message.what) {
                     case MSG_HIDE_BARS: {
                         hideBars();
                         break;
                     }
                     default: throw new AssertionError(message.what);
                 }
             }
         };
 
         // start the opening animation only if it's not restored.
         if (restoreState == null) {
             mPhotoView.setOpenAnimationRect((Rect) data.getParcelable(KEY_OPEN_ANIMATION_RECT));
         }
     }
 
     private void updateShareURI(Path path) {
         if (mShareActionProvider != null) {
             DataManager manager = mActivity.getDataManager();
             int type = manager.getMediaType(path);
             Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType(MenuExecutor.getMimeType(type));
             intent.putExtra(Intent.EXTRA_STREAM, manager.getContentUri(path));
             mShareActionProvider.setShareIntent(intent);
             if (mNfcAdapter != null) {
                 mNfcAdapter.setBeamPushUris(new Uri[]{manager.getContentUri(path)},
                         (Activity)mActivity);
             }
             mPendingSharePath = null;
         } else {
             // This happens when ActionBar is not created yet.
             mPendingSharePath = path;
         }
     }
 
     private void updateCurrentPhoto(MediaItem photo) {
         if (mCurrentPhoto == photo) return;
         mCurrentPhoto = photo;
         if (mCurrentPhoto == null) return;
         updateMenuOperations();
         // Hide the action bar when going back to camera preview.
         if (photo.getPath() == mScreenNailItemPath) hideBars();
         updateTitle();
         if (mShowDetails) {
             mDetailsHelper.reloadDetails(mModel.getCurrentIndex());
         }
         mPhotoView.showVideoPlayIcon(
                 photo.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO);
 
         if ((photo.getSupportedOperations() & MediaItem.SUPPORT_SHARE) != 0) {
             updateShareURI(photo.getPath());
         }
     }
 
     private void updateTitle() {
         if (mCurrentPhoto == null) return;
         boolean showTitle = mActivity.getAndroidContext().getResources().getBoolean(
                 R.bool.show_action_bar_title);
         if (showTitle && mCurrentPhoto.getName() != null)
             mActionBar.setTitle(mCurrentPhoto.getName());
         else
             mActionBar.setTitle("");
     }
 
     private void updateMenuOperations() {
         if (mMenu == null) return;
         MenuItem item = mMenu.findItem(R.id.action_slideshow);
         if (item != null) {
             item.setVisible(canDoSlideShow());
         }
         if (mCurrentPhoto == null) return;
         int supportedOperations = mCurrentPhoto.getSupportedOperations();
         if (!GalleryUtils.isEditorAvailable((Context) mActivity, "image/*")) {
             supportedOperations &= ~MediaObject.SUPPORT_EDIT;
         }
 
         MenuExecutor.updateMenuOperation(mMenu, supportedOperations);
     }
 
     private boolean canDoSlideShow() {
         if (mMediaSet == null || mCurrentPhoto == null) {
             return false;
         }
         if (mCurrentPhoto.getMediaType() != MediaObject.MEDIA_TYPE_IMAGE) {
             return false;
         }
         if (mMediaSet instanceof MtpDevice) {
             return false;
         }
         return true;
     }
 
     private void showBars() {
         if (mShowBars) return;
         mShowBars = true;
         mActionBar.show();
         WindowManager.LayoutParams params = ((Activity) mActivity).getWindow().getAttributes();
         params.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;
         ((Activity) mActivity).getWindow().setAttributes(params);
     }
 
     private void hideBars() {
         if (!mShowBars) return;
         mShowBars = false;
         mActionBar.hide();
         WindowManager.LayoutParams params = ((Activity) mActivity).getWindow().getAttributes();
         params.systemUiVisibility = View. SYSTEM_UI_FLAG_LOW_PROFILE;
         ((Activity) mActivity).getWindow().setAttributes(params);
     }
 
     private void refreshHidingMessage() {
         mHandler.removeMessages(MSG_HIDE_BARS);
         if (!mIsMenuVisible && !mIsInteracting) {
             mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
         }
     }
 
     @Override
     public void onUserInteraction() {
         showBars();
         refreshHidingMessage();
     }
 
     public void onUserInteractionTap() {
         if (mShowBars) {
             hideBars();
             mHandler.removeMessages(MSG_HIDE_BARS);
         } else {
             showBars();
             refreshHidingMessage();
         }
     }
 
     @Override
     public void onUserInteractionBegin() {
         showBars();
         mIsInteracting = true;
         refreshHidingMessage();
     }
 
     @Override
     public void onUserInteractionEnd() {
         mIsInteracting = false;
 
         // This function could be called from GL thread (in SlotView.render)
         // and post to the main thread. So, it could be executed while the
         // activity is paused.
         if (mIsActive) refreshHidingMessage();
     }
 
     @Override
     protected void onBackPressed() {
         if (mShowDetails) {
             hideDetails();
         } else if (mScreenNail == null
                 || !switchWithCaptureAnimation(-1)) {
             super.onBackPressed();
         }
     }
 
     // Switch to the previous or next picture using the capture animation.
     // The offset is -1 to switch to the previous picture, 1 to switch to
     // the next picture.
     public boolean switchWithCaptureAnimation(int offset) {
         return mPhotoView.switchWithCaptureAnimation(offset);
     }
 
     @Override
     protected boolean onCreateActionBar(Menu menu) {
         MenuInflater inflater = ((Activity) mActivity).getMenuInflater();
         inflater.inflate(R.menu.photo, menu);
         mShareActionProvider = GalleryActionBar.initializeShareActionProvider(menu);
         if (mPendingSharePath != null) updateShareURI(mPendingSharePath);
         mMenu = menu;
         mShowBars = true;
         updateMenuOperations();
         updateTitle();
         return true;
     }
 
     @Override
     protected boolean onItemSelected(MenuItem item) {
         MediaItem current = mModel.getCurrentMediaItem();
 
         if (current == null) {
             // item is not ready, ignore
             return true;
         }
 
         int currentIndex = mModel.getCurrentIndex();
         Path path = current.getPath();
 
         DataManager manager = mActivity.getDataManager();
         int action = item.getItemId();
         boolean needsConfirm = false;
         switch (action) {
             case android.R.id.home: {
                 if (mSetPathString != null) {
                     if (mActivity.getStateManager().getStateCount() > 1) {
                         onBackPressed();
                     } else {
                         // We're in view mode so set up the stacks on our own.
                         Bundle data = new Bundle(getData());
                         data.putString(AlbumPage.KEY_MEDIA_PATH, mSetPathString);
                         data.putString(AlbumPage.KEY_PARENT_MEDIA_PATH,
                                 mActivity.getDataManager().getTopSetPath(
                                         DataManager.INCLUDE_ALL));
                         mActivity.getStateManager().switchState(this, AlbumPage.class, data);
                     }
                 }
                 return true;
             }
             case R.id.action_slideshow: {
                 Bundle data = new Bundle();
                 data.putString(SlideshowPage.KEY_SET_PATH, mMediaSet.getPath().toString());
                 data.putString(SlideshowPage.KEY_ITEM_PATH, path.toString());
                 data.putInt(SlideshowPage.KEY_PHOTO_INDEX, currentIndex);
                 data.putBoolean(SlideshowPage.KEY_REPEAT, true);
                 mActivity.getStateManager().startStateForResult(
                         SlideshowPage.class, REQUEST_SLIDESHOW, data);
                 return true;
             }
             case R.id.action_crop: {
                 Activity activity = (Activity) mActivity;
                 Intent intent = new Intent(CropImage.CROP_ACTION);
                 intent.setClass(activity, CropImage.class);
                 intent.setData(manager.getContentUri(path));
                 activity.startActivityForResult(intent, PicasaSource.isPicasaImage(current)
                         ? REQUEST_CROP_PICASA
                         : REQUEST_CROP);
                 return true;
             }
             case R.id.action_details: {
                 if (mShowDetails) {
                     hideDetails();
                 } else {
                     showDetails(currentIndex);
                 }
                 return true;
             }
             case R.id.action_delete:
                 needsConfirm = true;
             case R.id.action_setas:
             case R.id.action_rotate_ccw:
             case R.id.action_rotate_cw:
             case R.id.action_show_on_map:
             case R.id.action_edit:
                 mSelectionManager.deSelectAll();
                 mSelectionManager.toggle(path);
                 mMenuExecutor.onMenuClicked(item, needsConfirm, null);
                 return true;
             case R.id.action_import:
                 mSelectionManager.deSelectAll();
                 mSelectionManager.toggle(path);
                 mMenuExecutor.onMenuClicked(item, needsConfirm,
                         new ImportCompleteListener(mActivity));
                 return true;
             default :
                 return false;
         }
     }
 
     private void hideDetails() {
         mShowDetails = false;
         mDetailsHelper.hide();
     }
 
     private void showDetails(int index) {
         mShowDetails = true;
         if (mDetailsHelper == null) {
             mDetailsHelper = new DetailsHelper(mActivity, mRootPane, new MyDetailsSource());
             mDetailsHelper.setCloseListener(new CloseListener() {
                 public void onClose() {
                     hideDetails();
                 }
             });
         }
         mDetailsHelper.reloadDetails(index);
         mDetailsHelper.show();
     }
 
     public void onSingleTapUp(int x, int y) {
         if (mPageTapListener != null) {
             if (mPageTapListener.onSingleTapUp(x, y)) return;
         }
 
         MediaItem item = mModel.getCurrentMediaItem();
         if (item == null) {
             // item is not ready, ignore
             return;
         }
 
         boolean playVideo =
                 (item.getSupportedOperations() & MediaItem.SUPPORT_PLAY) != 0;
 
         if (playVideo) {
             // determine if the point is at center (1/6) of the photo view.
             // (The position of the "play" icon is at center (1/6) of the photo)
             int w = mPhotoView.getWidth();
             int h = mPhotoView.getHeight();
             playVideo = (Math.abs(x - w / 2) * 12 <= w)
                 && (Math.abs(y - h / 2) * 12 <= h);
         }
 
         if (playVideo) {
             playVideo((Activity) mActivity, item.getPlayUri(), item.getName());
         } else {
             onUserInteractionTap();
         }
     }
 
     public static void playVideo(Activity activity, Uri uri, String title) {
         try {
             Intent intent = new Intent(Intent.ACTION_VIEW)
                     .setDataAndType(uri, "video/*");
             intent.putExtra(Intent.EXTRA_TITLE, title);
             activity.startActivity(intent);
         } catch (ActivityNotFoundException e) {
             Toast.makeText(activity, activity.getString(R.string.video_err),
                     Toast.LENGTH_SHORT).show();
         }
     }
 
     @Override
     protected void onStateResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_CROP:
                 if (resultCode == Activity.RESULT_OK) {
                     if (data == null) break;
                     Path path = mApplication.getDataManager()
                             .findPathByUri(data.getData(), data.getType());
                     if (path != null) {
                         mModel.setCurrentPhoto(path, mCurrentIndex);
                     }
                 }
                 break;
             case REQUEST_CROP_PICASA: {
                 int message = resultCode == Activity.RESULT_OK
                         ? R.string.crop_saved
                         : R.string.crop_not_saved;
                 Toast.makeText(mActivity.getAndroidContext(),
                         message, Toast.LENGTH_SHORT).show();
                 break;
             }
             case REQUEST_SLIDESHOW: {
                 if (data == null) break;
                 String path = data.getStringExtra(SlideshowPage.KEY_ITEM_PATH);
                 int index = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                 if (path != null) {
                     mModel.setCurrentPhoto(Path.fromString(path), index);
                 }
             }
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mIsActive = false;
         DetailsHelper.pause();
         mPhotoView.pause();
         mModel.pause();
         mHandler.removeMessages(MSG_HIDE_BARS);
         mActionBar.removeOnMenuVisibilityListener(mMenuVisibilityListener);
 
         mMenuExecutor.pause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mIsActive = true;
         setContentPane(mRootPane);
 
         mModel.resume();
         mPhotoView.resume();
         if (mMenuVisibilityListener == null) {
             mMenuVisibilityListener = new MyMenuVisibilityListener();
         }
         mActionBar.setDisplayOptions(mSetPathString != null, true);
         mActionBar.addOnMenuVisibilityListener(mMenuVisibilityListener);
 
         onUserInteraction();
     }
 
     @Override
     protected void onDestroy() {
         if (mScreenNailHolder != null) {
             // Unregister the ScreenNail and notify mScreenNailHolder.
             SnailSource.unregisterScreenNail(mScreenNail);
             mScreenNailHolder.detach();
             mScreenNailHolder = null;
             mScreenNail = null;
         }
         super.onDestroy();
     }
 
     private class MyDetailsSource implements DetailsSource {
         private int mIndex;
 
         @Override
         public MediaDetails getDetails() {
             return mModel.getCurrentMediaItem().getDetails();
         }
 
         @Override
         public int size() {
             return mMediaSet != null ? mMediaSet.getMediaItemCount() : 1;
         }
 
         @Override
         public int findIndex(int indexHint) {
             mIndex = indexHint;
             return indexHint;
         }
 
         @Override
         public int getIndex() {
             return mIndex;
         }
     }
 
 }

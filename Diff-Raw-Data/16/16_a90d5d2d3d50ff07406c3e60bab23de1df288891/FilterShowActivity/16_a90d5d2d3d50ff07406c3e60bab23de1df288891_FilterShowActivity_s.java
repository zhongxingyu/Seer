 
 package com.android.gallery3d.filtershow;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.ShareActionProvider;
 import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
 
 import com.android.gallery3d.R;
 import com.android.gallery3d.filtershow.cache.ImageLoader;
 import com.android.gallery3d.filtershow.filters.ImageFilter;
 import com.android.gallery3d.filtershow.filters.ImageFilterBorder;
 import com.android.gallery3d.filtershow.filters.ImageFilterParametricBorder;
 import com.android.gallery3d.filtershow.filters.ImageFilterRS;
 import com.android.gallery3d.filtershow.imageshow.ImageBorder;
 import com.android.gallery3d.filtershow.imageshow.ImageCrop;
 import com.android.gallery3d.filtershow.imageshow.ImageFlip;
 import com.android.gallery3d.filtershow.imageshow.ImageRotate;
 import com.android.gallery3d.filtershow.imageshow.ImageShow;
 import com.android.gallery3d.filtershow.imageshow.ImageSmallFilter;
 import com.android.gallery3d.filtershow.imageshow.ImageStraighten;
 import com.android.gallery3d.filtershow.imageshow.ImageZoom;
 import com.android.gallery3d.filtershow.presets.ImagePreset;
 import com.android.gallery3d.filtershow.presets.ImagePresetBW;
 import com.android.gallery3d.filtershow.presets.ImagePresetBWBlue;
 import com.android.gallery3d.filtershow.presets.ImagePresetBWGreen;
 import com.android.gallery3d.filtershow.presets.ImagePresetBWRed;
 import com.android.gallery3d.filtershow.presets.ImagePresetOld;
 import com.android.gallery3d.filtershow.presets.ImagePresetSaturated;
 import com.android.gallery3d.filtershow.presets.ImagePresetXProcessing;
 import com.android.gallery3d.filtershow.provider.SharedImageProvider;
 import com.android.gallery3d.filtershow.tools.SaveCopyTask;
 import com.android.gallery3d.filtershow.ui.ImageCurves;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.Vector;
 
 @TargetApi(16)
 public class FilterShowActivity extends Activity implements OnItemClickListener,
         OnShareTargetSelectedListener {
 
     private final PanelController mPanelController = new PanelController();
     private ImageLoader mImageLoader = null;
     private ImageShow mImageShow = null;
     private ImageCurves mImageCurves = null;
     private ImageBorder mImageBorders = null;
     private ImageStraighten mImageStraighten = null;
     private ImageZoom mImageZoom = null;
     private ImageCrop mImageCrop = null;
     private ImageRotate mImageRotate = null;
     private ImageFlip mImageFlip = null;
 
     private View mListFx = null;
     private View mListBorders = null;
     private View mListGeometry = null;
     private View mListColors = null;
     private View mListFilterButtons = null;
 
     private ImageButton mFxButton = null;
     private ImageButton mBorderButton = null;
     private ImageButton mGeometryButton = null;
     private ImageButton mColorsButton = null;
 
     private static final int SELECT_PICTURE = 1;
     private static final String LOGTAG = "FilterShowActivity";
     protected static final boolean ANIMATE_PANELS = true;
 
     private boolean mShowingHistoryPanel = false;
     private boolean mShowingImageStatePanel = false;
 
     private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();
     private final Vector<View> mListViews = new Vector<View>();
     private final Vector<ImageButton> mBottomPanelButtons = new Vector<ImageButton>();
 
     private ShareActionProvider mShareActionProvider;
     private File mSharedOutputFile = null;
 
     private boolean mSharingImage = false;
 
     private WeakReference<ProgressDialog> mSavingProgressDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         ImageFilterRS.setRenderScriptContext(this);
 
         setContentView(R.layout.filtershow_activity);
         ActionBar actionBar = getActionBar();
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
         actionBar.setCustomView(R.layout.filtershow_actionbar);
 
         actionBar.getCustomView().setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 saveImage();
             }
         });
 
         mImageLoader = new ImageLoader(getApplicationContext());
 
         LinearLayout listFilters = (LinearLayout) findViewById(R.id.listFilters);
         LinearLayout listBorders = (LinearLayout) findViewById(R.id.listBorders);
 
         mImageShow = (ImageShow) findViewById(R.id.imageShow);
         mImageCurves = (ImageCurves) findViewById(R.id.imageCurves);
         mImageBorders = (ImageBorder) findViewById(R.id.imageBorder);
         mImageStraighten = (ImageStraighten) findViewById(R.id.imageStraighten);
         mImageZoom = (ImageZoom) findViewById(R.id.imageZoom);
         mImageCrop = (ImageCrop) findViewById(R.id.imageCrop);
         mImageRotate = (ImageRotate) findViewById(R.id.imageRotate);
         mImageFlip = (ImageFlip) findViewById(R.id.imageFlip);
 
         mImageViews.add(mImageShow);
         mImageViews.add(mImageCurves);
         mImageViews.add(mImageBorders);
         mImageViews.add(mImageStraighten);
         mImageViews.add(mImageZoom);
         mImageViews.add(mImageCrop);
         mImageViews.add(mImageRotate);
         mImageViews.add(mImageFlip);
 
         mListFx = findViewById(R.id.fxList);
         mListBorders = findViewById(R.id.bordersList);
         mListGeometry = findViewById(R.id.geometryList);
         mListFilterButtons = findViewById(R.id.filterButtonsList);
         mListColors = findViewById(R.id.colorsFxList);
         mListViews.add(mListFx);
         mListViews.add(mListBorders);
         mListViews.add(mListGeometry);
         mListViews.add(mListFilterButtons);
         mListViews.add(mListColors);
 
         mFxButton = (ImageButton) findViewById(R.id.fxButton);
         mBorderButton = (ImageButton) findViewById(R.id.borderButton);
         mGeometryButton = (ImageButton) findViewById(R.id.geometryButton);
         mColorsButton = (ImageButton) findViewById(R.id.colorsButton);
 
         mImageShow.setImageLoader(mImageLoader);
         mImageCurves.setImageLoader(mImageLoader);
         mImageCurves.setMaster(mImageShow);
         mImageBorders.setImageLoader(mImageLoader);
         mImageBorders.setMaster(mImageShow);
         mImageStraighten.setImageLoader(mImageLoader);
         mImageStraighten.setMaster(mImageShow);
         mImageZoom.setImageLoader(mImageLoader);
         mImageZoom.setMaster(mImageShow);
         mImageCrop.setImageLoader(mImageLoader);
         mImageCrop.setMaster(mImageShow);
         mImageRotate.setImageLoader(mImageLoader);
         mImageRotate.setMaster(mImageShow);
         mImageFlip.setImageLoader(mImageLoader);
         mImageFlip.setMaster(mImageShow);
 
         mPanelController.addImageView(findViewById(R.id.imageShow));
         mPanelController.addImageView(findViewById(R.id.imageCurves));
         mPanelController.addImageView(findViewById(R.id.imageBorder));
         mPanelController.addImageView(findViewById(R.id.imageStraighten));
         mPanelController.addImageView(findViewById(R.id.imageCrop));
         mPanelController.addImageView(findViewById(R.id.imageRotate));
         mPanelController.addImageView(findViewById(R.id.imageFlip));
         mPanelController.addImageView(findViewById(R.id.imageZoom));
 
         mPanelController.addPanel(mFxButton, mListFx, 0);
         mPanelController.addPanel(mBorderButton, mListBorders, 1);
 
         mPanelController.addPanel(mGeometryButton, mListGeometry, 2);
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.straightenButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.cropButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.rotateButton));
         mPanelController.addComponent(mGeometryButton, findViewById(R.id.flipButton));
 
         mPanelController.addPanel(mColorsButton, mListColors, 3);
         mPanelController.addComponent(mColorsButton, findViewById(R.id.vignetteButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.curvesButtonRGB));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.sharpenButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.vibranceButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.contrastButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.saturationButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.tintButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.exposureButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.shadowRecoveryButton));
         mPanelController.addComponent(mColorsButton, findViewById(R.id.redEyeButton));
 
         mPanelController.addView(findViewById(R.id.resetEffect));
         mPanelController.addView(findViewById(R.id.applyEffect));
 
         findViewById(R.id.compareWithOriginalImage).setOnTouchListener(createOnTouchShowOriginalButton());
 
         findViewById(R.id.resetOperationsButton).setOnClickListener(
                 createOnClickResetOperationsButton());
 
         ListView operationsList = (ListView) findViewById(R.id.operationsList);
         operationsList.setAdapter(mImageShow.getHistoryAdapter());
         operationsList.setOnItemClickListener(this);
         ListView imageStateList = (ListView) findViewById(R.id.imageStateList);
         imageStateList.setAdapter(mImageShow.getImageStateAdapter());
         mImageLoader.setAdapter((HistoryAdapter) mImageShow.getHistoryAdapter());
 
         fillListImages(listFilters);
         fillListBorders(listBorders);
 
         SeekBar seekBar = (SeekBar) findViewById(R.id.filterSeekBar);
         seekBar.setMax(200);
         mImageShow.setSeekBar(seekBar);
         mImageZoom.setSeekBar(seekBar);
         mPanelController.setRowPanel(findViewById(R.id.secondRowPanel));
         mPanelController.setUtilityPanel(this, findViewById(R.id.filterButtonsList),
                 findViewById(R.id.compareWithOriginalImage),
                 findViewById(R.id.applyEffect));
         mPanelController.setMasterImage(mImageShow);
         mPanelController.setCurrentPanel(mFxButton);
         Intent intent = getIntent();
         String data = intent.getDataString();
         if (data != null) {
             Uri uri = Uri.parse(data);
             mImageLoader.loadBitmap(uri);
         } else {
             pickImage();
         }
     }
 
     private void showSavingProgress() {
         ProgressDialog progress;
         if (mSavingProgressDialog != null) {
             progress = mSavingProgressDialog.get();
             if (progress != null) {
                 progress.show();
                 return;
             }
         }
         // TODO: Allow cancellation of the saving process
         progress = ProgressDialog.show(this, "", getString(R.string.saving_image), true, false);
         mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
     }
 
     private void hideSavingProgress() {
         if (mSavingProgressDialog != null) {
             ProgressDialog progress = mSavingProgressDialog.get();
             if (progress != null) progress.dismiss();
         }
     }
 
     public void completeSaveImage(Uri saveUri) {
         if (mSharingImage && mSharedOutputFile != null) {
             // Image saved, we unblock the content provider
             Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                     Uri.encode(mSharedOutputFile.getAbsolutePath()));
             ContentValues values = new ContentValues();
             values.put(SharedImageProvider.PREPARE, false);
             getContentResolver().insert(uri, values);
         }
         setResult(RESULT_OK, new Intent().setData(saveUri));
         hideSavingProgress();
         finish();
     }
 
     @Override
     public boolean onShareTargetSelected(ShareActionProvider arg0, Intent arg1) {
         // First, let's tell the SharedImageProvider that it will need to wait
         // for the image
         Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                 Uri.encode(mSharedOutputFile.getAbsolutePath()));
         ContentValues values = new ContentValues();
         values.put(SharedImageProvider.PREPARE, true);
         getContentResolver().insert(uri, values);
         mSharingImage = true;
 
         // Process and save the image in the background.
         showSavingProgress();
         mImageShow.saveImage(this, mSharedOutputFile);
         return true;
     }
 
     private Intent getDefaultShareIntent() {
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
         intent.setType(SharedImageProvider.MIME_TYPE);
         mSharedOutputFile = SaveCopyTask.getNewFile(this, mImageLoader.getUri());
         Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                 Uri.encode(mSharedOutputFile.getAbsolutePath()));
         intent.putExtra(Intent.EXTRA_STREAM, uri);
         return intent;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.filtershow_activity_menu, menu);
         MenuItem showHistory = menu.findItem(R.id.operationsButton);
         if (mShowingHistoryPanel) {
             showHistory.setTitle(R.string.hide_history_panel);
         } else {
             showHistory.setTitle(R.string.show_history_panel);
         }
         MenuItem showState = menu.findItem(R.id.showImageStateButton);
         if (mShowingImageStatePanel) {
             showState.setTitle(R.string.hide_imagestate_panel);
         } else {
             showState.setTitle(R.string.show_imagestate_panel);
         }
         mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share)
                 .getActionProvider();
         mShareActionProvider.setShareIntent(getDefaultShareIntent());
         mShareActionProvider.setOnShareTargetSelectedListener(this);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.undoButton: {
                 HistoryAdapter adapter = (HistoryAdapter) mImageShow
                         .getHistoryAdapter();
                 int position = adapter.undo();
                 mImageShow.onItemClick(position);
                 mImageShow.showToast("Undo");
                 invalidateViews();
                 return true;
             }
             case R.id.redoButton: {
                 HistoryAdapter adapter = (HistoryAdapter) mImageShow
                         .getHistoryAdapter();
                 int position = adapter.redo();
                 mImageShow.onItemClick(position);
                 mImageShow.showToast("Redo");
                 invalidateViews();
                 return true;
             }
             case R.id.resetHistoryButton: {
                 resetHistory();
                 return true;
             }
             case R.id.showImageStateButton: {
                 toggleImageStatePanel();
                 return true;
             }
             case R.id.operationsButton: {
                 toggleHistoryPanel();
                 return true;
             }
             case android.R.id.home: {
                 saveImage();
                 return true;
             }
         }
         return false;
     }
 
     private void fillListImages(LinearLayout listFilters) {
         // TODO: use listview
         // TODO: load the filters straight from the filesystem
         ImagePreset[] preset = new ImagePreset[9];
         int p = 0;
         preset[p++] = new ImagePreset();
         preset[p++] = new ImagePresetSaturated();
         preset[p++] = new ImagePresetOld();
         preset[p++] = new ImagePresetXProcessing();
         preset[p++] = new ImagePresetBW();
         preset[p++] = new ImagePresetBWRed();
         preset[p++] = new ImagePresetBWGreen();
         preset[p++] = new ImagePresetBWBlue();
 
         for (int i = 0; i < p; i++) {
             ImageSmallFilter filter = new ImageSmallFilter(getBaseContext());
             preset[i].setIsFx(true);
             filter.setImagePreset(preset[i]);
             filter.setController(this);
             filter.setImageLoader(mImageLoader);
             listFilters.addView(filter);
         }
 
         // Default preset (original)
         mImageShow.setImagePreset(preset[0]);
     }
 
     private void fillListBorders(LinearLayout listBorders) {
         // TODO: use listview
         // TODO: load the borders straight from the filesystem
         int p = 0;
         ImageFilter[] borders = new ImageFilter[7];
         borders[p++] = new ImageFilterBorder(null);
 
         borders[p++] = new ImageFilterParametricBorder(Color.WHITE, 100, 0);
         borders[p++] = new ImageFilterParametricBorder(Color.BLACK, 100, 0);
         borders[p++] = new ImageFilterParametricBorder(Color.WHITE, 100, 100);
         borders[p++] = new ImageFilterParametricBorder(Color.BLACK, 100, 100);
         Drawable npd3 = getResources().getDrawable(R.drawable.filtershow_border_film3);
         borders[p++] = new ImageFilterBorder(npd3);
         Drawable npd = getResources().getDrawable(
                 R.drawable.filtershow_border_scratch3);
         borders[p++] = new ImageFilterBorder(npd);
 
         for (int i = 0; i < p; i++) {
             ImageSmallFilter filter = new ImageSmallFilter(getBaseContext());
             filter.setImageFilter(borders[i]);
             filter.setController(this);
             filter.setBorder(true);
             filter.setImageLoader(mImageLoader);
             filter.setShowTitle(false);
             listBorders.addView(filter);
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // Some utility functions
     // TODO: finish the cleanup.
 
     public void showOriginalViews(boolean value) {
         for (ImageShow views : mImageViews) {
             views.showOriginal(value);
         }
     }
 
     public void invalidateViews() {
         for (ImageShow views : mImageViews) {
             views.invalidate();
             views.updateImage();
         }
     }
 
     public void hideListViews() {
         for (View view : mListViews) {
             view.setVisibility(View.GONE);
         }
     }
 
     public void hideImageViews() {
         mImageShow.setShowControls(false); // reset
         for (View view : mImageViews) {
             view.setVisibility(View.GONE);
         }
     }
 
     public void unselectBottomPanelButtons() {
         for (ImageButton button : mBottomPanelButtons) {
             button.setSelected(false);
         }
     }
 
     public void unselectPanelButtons(Vector<ImageButton> buttons) {
         for (ImageButton button : buttons) {
             button.setSelected(false);
         }
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // Click handlers for the top row buttons
 
     private OnTouchListener createOnTouchShowOriginalButton() {
         return new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 boolean show = false;
                 if ((event.getActionMasked() != MotionEvent.ACTION_UP)
                         || (event.getActionMasked() == MotionEvent.ACTION_CANCEL)) {
                     show = true;
                 }
                 showOriginalViews(show);
                 return true;
             }
         };
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // imageState panel...
 
     private void toggleImageStatePanel() {
         final View view = findViewById(R.id.mainPanel);
         final View viewList = findViewById(R.id.imageStatePanel);
 
         if (mShowingHistoryPanel) {
             findViewById(R.id.historyPanel).setVisibility(View.INVISIBLE);
             mShowingHistoryPanel = false;
         }
 
         if (!mShowingImageStatePanel) {
             mShowingImageStatePanel = true;
             view.animate().setDuration(200).x(-viewList.getWidth())
                     .withLayer().withEndAction(new Runnable() {
                         @Override
                         public void run() {
                             viewList.setAlpha(0);
                             viewList.setVisibility(View.VISIBLE);
                             viewList.animate().setDuration(100)
                                     .alpha(1.0f).start();
                         }
                     }).start();
         } else {
             mShowingImageStatePanel = false;
             viewList.setVisibility(View.INVISIBLE);
             view.animate().setDuration(200).x(0).withLayer()
                     .start();
         }
         invalidateOptionsMenu();
     }
 
     // //////////////////////////////////////////////////////////////////////////////
     // history panel...
 
     private void toggleHistoryPanel() {
         final View view = findViewById(R.id.mainPanel);
         final View viewList = findViewById(R.id.historyPanel);
 
         if (mShowingImageStatePanel) {
             findViewById(R.id.imageStatePanel).setVisibility(View.INVISIBLE);
             mShowingImageStatePanel = false;
         }
 
         if (!mShowingHistoryPanel) {
             mShowingHistoryPanel = true;
             view.animate().setDuration(200).x(-viewList.getWidth())
                     .withLayer().withEndAction(new Runnable() {
                         @Override
                         public void run() {
                             viewList.setAlpha(0);
                             viewList.setVisibility(View.VISIBLE);
                             viewList.animate().setDuration(100)
                                     .alpha(1.0f).start();
                         }
                     }).start();
         } else {
             mShowingHistoryPanel = false;
             viewList.setVisibility(View.INVISIBLE);
             view.animate().setDuration(200).x(0).withLayer()
                     .start();
         }
         invalidateOptionsMenu();
     }
 
     private void resetHistory() {
         HistoryAdapter adapter = (HistoryAdapter) mImageShow
                 .getHistoryAdapter();
         adapter.reset();
         ImagePreset original = new ImagePreset(adapter.getItem(0));
         mImageShow.setImagePreset(original);
         invalidateViews();
     }
 
     // reset button in the history panel.
     private OnClickListener createOnClickResetOperationsButton() {
         return new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 resetHistory();
             }
         };
     }
 
     // //////////////////////////////////////////////////////////////////////////////
 
     public float getPixelsFromDip(float value) {
         Resources r = getResources();
         return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                 r.getDisplayMetrics());
     }
 
     public void useImagePreset(ImagePreset preset) {
         if (preset == null) {
             return;
         }
         ImagePreset copy = new ImagePreset(preset);
         mImageShow.setImagePreset(copy);
         if (preset.isFx()) {
             // if it's an FX we rest the curve adjustment too
             mImageCurves.resetCurve();
         }
         invalidateViews();
     }
 
     public void useImageFilter(ImageFilter imageFilter, boolean setBorder) {
         if (imageFilter == null) {
             return;
         }
         ImagePreset oldPreset = mImageShow.getImagePreset();
         ImagePreset copy = new ImagePreset(oldPreset);
         // TODO: use a numerical constant instead.
         if (setBorder) {
             copy.setHistoryName("Border");
             copy.setBorder(imageFilter);
         } else {
             copy.add(imageFilter);
         }
         mImageShow.setImagePreset(copy);
         invalidateViews();
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position,
             long id) {
         mImageShow.onItemClick(position);
         invalidateViews();
     }
 
     public void pickImage() {
         Intent intent = new Intent();
         intent.setType("image/*");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                 SELECT_PICTURE);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.v(LOGTAG, "onActivityResult");
         if (resultCode == RESULT_OK) {
             if (requestCode == SELECT_PICTURE) {
                 Uri selectedImageUri = data.getData();
                 mImageLoader.loadBitmap(selectedImageUri);
             }
         }
     }
 
     public void saveImage() {
         showSavingProgress();
         mImageShow.saveImage(this, null);
     }
 
     static {
         System.loadLibrary("jni_filtershow_filters");
     }
 
 }

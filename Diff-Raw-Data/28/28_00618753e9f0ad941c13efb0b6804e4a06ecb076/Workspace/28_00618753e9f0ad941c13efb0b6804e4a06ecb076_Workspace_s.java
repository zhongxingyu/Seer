 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.launcher2;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.animation.TimeInterpolator;
 import android.animation.ValueAnimator;
 import android.animation.Animator.AnimatorListener;
 import android.animation.ValueAnimator.AnimatorUpdateListener;
 import android.app.AlertDialog;
 import android.app.WallpaperManager;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.ClipData;
 import android.content.ClipDescription;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.Camera;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Region.Op;
 import android.graphics.drawable.Drawable;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.Pair;
 import android.view.Display;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.DecelerateInterpolator;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.launcher.R;
 import com.android.launcher2.FolderIcon.FolderRingAnimator;
 import com.android.launcher2.InstallWidgetReceiver.WidgetMimeTypeHandlerData;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * The workspace is a wide area with a wallpaper and a finite number of pages.
  * Each page contains a number of icons, folders or widgets the user can
  * interact with. A workspace is meant to be used with a fixed width only.
  */
 public class Workspace extends SmoothPagedView
         implements DropTarget, DragSource, DragScroller, View.OnTouchListener,
         DragController.DragListener {
     @SuppressWarnings({"UnusedDeclaration"})
     private static final String TAG = "Launcher.Workspace";
 
     // Y rotation to apply to the workspace screens
     private static final float WORKSPACE_ROTATION = 12.5f;
 
     // These are extra scale factors to apply to the mini home screens
     // so as to achieve the desired transform
     private static final float EXTRA_SCALE_FACTOR_0 = 0.972f;
     private static final float EXTRA_SCALE_FACTOR_1 = 1.0f;
     private static final float EXTRA_SCALE_FACTOR_2 = 1.10f;
 
     private static final int CHILDREN_OUTLINE_FADE_OUT_DELAY = 0;
     private static final int CHILDREN_OUTLINE_FADE_OUT_DURATION = 375;
     private static final int CHILDREN_OUTLINE_FADE_IN_DURATION = 100;
 
     private static final int BACKGROUND_FADE_OUT_DURATION = 350;
     private static final int BACKGROUND_FADE_IN_DURATION = 350;
 
     // These animators are used to fade the children's outlines
     private ObjectAnimator mChildrenOutlineFadeInAnimation;
     private ObjectAnimator mChildrenOutlineFadeOutAnimation;
     private float mChildrenOutlineAlpha = 0;
 
     // These properties refer to the background protection gradient used for AllApps and Customize
     private ValueAnimator mBackgroundFadeInAnimation;
     private ValueAnimator mBackgroundFadeOutAnimation;
     private Drawable mBackground;
     boolean mDrawBackground = true;
     private float mBackgroundAlpha = 0;
     private float mOverScrollMaxBackgroundAlpha = 0.0f;
     private int mOverScrollPageIndex = -1;
 
     private final WallpaperManager mWallpaperManager;
     private IBinder mWindowToken;
 
     private int mDefaultPage;
 
     /**
      * CellInfo for the cell that is currently being dragged
      */
     private CellLayout.CellInfo mDragInfo;
 
     /**
      * Target drop area calculated during last acceptDrop call.
      */
     private int[] mTargetCell = new int[2];
 
     /**
      * The CellLayout that is currently being dragged over
      */
     private CellLayout mDragTargetLayout = null;
 
     private Launcher mLauncher;
     private IconCache mIconCache;
     private DragController mDragController;
 
     // These are temporary variables to prevent having to allocate a new object just to
     // return an (x, y) value from helper functions. Do NOT use them to maintain other state.
     private int[] mTempCell = new int[2];
     private int[] mTempEstimate = new int[2];
     private float[] mDragViewVisualCenter = new float[2];
     private float[] mTempDragCoordinates = new float[2];
     private float[] mTempTouchCoordinates = new float[2];
     private float[] mTempCellLayoutCenterCoordinates = new float[2];
     private float[] mTempDragBottomRightCoordinates = new float[2];
     private Matrix mTempInverseMatrix = new Matrix();
 
     private SpringLoadedDragController mSpringLoadedDragController;
     private float mSpringLoadedShrinkFactor;
 
     private static final int DEFAULT_CELL_COUNT_X = 4;
     private static final int DEFAULT_CELL_COUNT_Y = 4;
 
     // State variable that indicates whether the pages are small (ie when you're
     // in all apps or customize mode)
 
     enum State { NORMAL, SPRING_LOADED, SMALL };
     private State mState;
     private boolean mIsSwitchingState = false;
 
     private boolean mSwitchStateAfterFirstLayout = false;
     private State mStateAfterFirstLayout;
 
     private AnimatorSet mAnimator;
     private AnimatorListener mShrinkAnimationListener;
     private AnimatorListener mUnshrinkAnimationListener;
 
     boolean mAnimatingViewIntoPlace = false;
     boolean mIsDragOccuring = false;
     boolean mChildrenLayersEnabled = true;
 
     /** Is the user is dragging an item near the edge of a page? */
     private boolean mInScrollArea = false;
 
     private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
     private Bitmap mDragOutline = null;
     private final Rect mTempRect = new Rect();
     private final int[] mTempXY = new int[2];
 
     // Paint used to draw external drop outline
     private final Paint mExternalDragOutlinePaint = new Paint();
 
     // Camera and Matrix used to determine the final position of a neighboring CellLayout
     private final Matrix mMatrix = new Matrix();
     private final Camera mCamera = new Camera();
     private final float mTempFloat2[] = new float[2];
 
     enum WallpaperVerticalOffset { TOP, MIDDLE, BOTTOM };
     int mWallpaperWidth;
     int mWallpaperHeight;
     WallpaperOffsetInterpolator mWallpaperOffset;
     boolean mUpdateWallpaperOffsetImmediately = false;
     boolean mSyncWallpaperOffsetWithScroll = true;
     private Runnable mDelayedResizeRunnable;
 
     // Variables relating to the creation of user folders by hovering shortcuts over shortcuts
     private static final int FOLDER_CREATION_TIMEOUT = 250;
     private final Alarm mFolderCreationAlarm = new Alarm();
     private FolderRingAnimator mDragFolderRingAnimator = null;
     private View mLastDragOverView = null;
     private boolean mCreateUserFolderOnDrop = false;
 
     // Variables relating to touch disambiguation (scrolling workspace vs. scrolling a widget)
     private float mXDown;
     private float mYDown;
     final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
     final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
     final static float TOUCH_SLOP_DAMPING_FACTOR = 4;
 
     /**
      * Used to inflate the Workspace from XML.
      *
      * @param context The application's context.
      * @param attrs The attributes set containing the Workspace's customization values.
      */
     public Workspace(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     /**
      * Used to inflate the Workspace from XML.
      *
      * @param context The application's context.
      * @param attrs The attributes set containing the Workspace's customization values.
      * @param defStyle Unused.
      */
     public Workspace(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         mContentIsRefreshable = false;
 
         // With workspace, data is available straight from the get-go
         setDataIsReady();
 
         if (!LauncherApplication.isScreenLarge()) {
             mFadeInAdjacentScreens = false;
         }
 
         mWallpaperManager = WallpaperManager.getInstance(context);
 
         int cellCountX = DEFAULT_CELL_COUNT_X;
         int cellCountY = DEFAULT_CELL_COUNT_Y;
 
         TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.Workspace, defStyle, 0);
 
         final Resources res = context.getResources();
         if (LauncherApplication.isScreenLarge()) {
             // Determine number of rows/columns dynamically
             // TODO: This code currently fails on tablets with an aspect ratio < 1.3.
             // Around that ratio we should make cells the same size in portrait and
             // landscape
             TypedArray actionBarSizeTypedArray =
                 context.obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
             final float actionBarHeight = actionBarSizeTypedArray.getDimension(0, 0f);
             final float systemBarHeight = res.getDimension(R.dimen.status_bar_height);
             final float smallestScreenDim = res.getConfiguration().smallestScreenWidthDp;
 
             cellCountX = 1;
             while (CellLayout.widthInPortrait(res, cellCountX + 1) <= smallestScreenDim) {
                 cellCountX++;
             }
 
             cellCountY = 1;
             while (actionBarHeight + CellLayout.heightInLandscape(res, cellCountY + 1)
                 <= smallestScreenDim - systemBarHeight) {
                 cellCountY++;
             }
         }
 
         mSpringLoadedShrinkFactor =
             res.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f;
 
         // if the value is manually specified, use that instead
         cellCountX = a.getInt(R.styleable.Workspace_cellCountX, cellCountX);
         cellCountY = a.getInt(R.styleable.Workspace_cellCountY, cellCountY);
         mDefaultPage = a.getInt(R.styleable.Workspace_defaultScreen, 1);
         a.recycle();
 
         LauncherModel.updateWorkspaceLayoutCells(cellCountX, cellCountY);
         setHapticFeedbackEnabled(false);
 
         initWorkspace();
 
         // Disable multitouch across the workspace/all apps/customize tray
         setMotionEventSplittingEnabled(true);
     }
 
     public void onDragStart(DragSource source, Object info, int dragAction) {
         mIsDragOccuring = true;
         updateChildrenLayersEnabled();
         mLauncher.lockScreenOrientation();
     }
 
     public void onDragEnd() {
         mIsDragOccuring = false;
         updateChildrenLayersEnabled();
         mLauncher.unlockScreenOrientation();
     }
 
     /**
      * Initializes various states for this workspace.
      */
     protected void initWorkspace() {
         Context context = getContext();
         mCurrentPage = mDefaultPage;
         Launcher.setScreen(mCurrentPage);
         LauncherApplication app = (LauncherApplication)context.getApplicationContext();
         mIconCache = app.getIconCache();
         mExternalDragOutlinePaint.setAntiAlias(true);
         setWillNotDraw(false);
 
         try {
             final Resources res = getResources();
             mBackground = res.getDrawable(R.drawable.apps_customize_bg);
         } catch (Resources.NotFoundException e) {
             // In this case, we will skip drawing background protection
         }
 
         mUnshrinkAnimationListener = new AnimatorListenerAdapter() {
             @Override
             public void onAnimationStart(Animator animation) {
                 mIsSwitchingState = true;
             }
 
             @Override
             public void onAnimationEnd(Animator animation) {
                 mIsSwitchingState = false;
                 mSyncWallpaperOffsetWithScroll = true;
                 mWallpaperOffset.setOverrideHorizontalCatchupConstant(false);
                 mAnimator = null;
                 updateChildrenLayersEnabled();
             }
         };
         mShrinkAnimationListener = new AnimatorListenerAdapter() {
             @Override
             public void onAnimationStart(Animator animation) {
                 mIsSwitchingState = true;
             }
             @Override
             public void onAnimationEnd(Animator animation) {
                 mIsSwitchingState = false;
                 mWallpaperOffset.setOverrideHorizontalCatchupConstant(false);
                 mAnimator = null;
             }
         };
         mSnapVelocity = 600;
         mWallpaperOffset = new WallpaperOffsetInterpolator();
     }
 
     @Override
     protected int getScrollMode() {
         return SmoothPagedView.X_LARGE_MODE;
     }
 
     private void onAddView(View child) {
         if (!(child instanceof CellLayout)) {
             throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
         }
         CellLayout cl = ((CellLayout) child);
         cl.setOnInterceptTouchListener(this);
         cl.setClickable(true);
         cl.enableHardwareLayers();
     }
 
     @Override
     public void addView(View child, int index, LayoutParams params) {
         onAddView(child);
         super.addView(child, index, params);
     }
 
     @Override
     public void addView(View child) {
         onAddView(child);
         super.addView(child);
     }
 
     @Override
     public void addView(View child, int index) {
         onAddView(child);
         super.addView(child, index);
     }
 
     @Override
     public void addView(View child, int width, int height) {
         onAddView(child);
         super.addView(child, width, height);
     }
 
     @Override
     public void addView(View child, LayoutParams params) {
         onAddView(child);
         super.addView(child, params);
     }
 
     /**
      * @return The open folder on the current screen, or null if there is none
      */
     Folder getOpenFolder() {
         DragLayer dragLayer = mLauncher.getDragLayer();
         int count = dragLayer.getChildCount();
         for (int i = 0; i < count; i++) {
             View child = dragLayer.getChildAt(i);
             if (child instanceof Folder) {
                 Folder folder = (Folder) child;
                 if (folder.getInfo().opened)
                     return folder;
             }
         }
         return null;
     }
 
     boolean isTouchActive() {
         return mTouchState != TOUCH_STATE_REST;
     }
 
     /**
      * Adds the specified child in the specified screen. The position and dimension of
      * the child are defined by x, y, spanX and spanY.
      *
      * @param child The child to add in one of the workspace's screens.
      * @param screen The screen in which to add the child.
      * @param x The X position of the child in the screen's grid.
      * @param y The Y position of the child in the screen's grid.
      * @param spanX The number of cells spanned horizontally by the child.
      * @param spanY The number of cells spanned vertically by the child.
      */
     void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY) {
         addInScreen(child, container, screen, x, y, spanX, spanY, false);
     }
 
     /**
      * Adds the specified child in the specified screen. The position and dimension of
      * the child are defined by x, y, spanX and spanY.
      *
      * @param child The child to add in one of the workspace's screens.
      * @param screen The screen in which to add the child.
      * @param x The X position of the child in the screen's grid.
      * @param y The Y position of the child in the screen's grid.
      * @param spanX The number of cells spanned horizontally by the child.
      * @param spanY The number of cells spanned vertically by the child.
      * @param insert When true, the child is inserted at the beginning of the children list.
      */
     void addInScreen(View child, long container, int screen, int x, int y, int spanX, int spanY,
             boolean insert) {
         if (container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
             if (screen < 0 || screen >= getChildCount()) {
                 Log.e(TAG, "The screen must be >= 0 and < " + getChildCount()
                     + " (was " + screen + "); skipping child");
                 return;
             }
         }
 
         final CellLayout layout;
         if (container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
             layout = mLauncher.getHotseat().getLayout();
 
             if (screen < 0) {
                 screen = mLauncher.getHotseat().getOrderInHotseat(x, y);
             } else {
                 // Note: We do this to ensure that the hotseat is always laid out in the orientation
                 // of the hotseat in order regardless of which orientation they were added
                 x = mLauncher.getHotseat().getCellXFromOrder(screen);
                 y = mLauncher.getHotseat().getCellYFromOrder(screen);
             }
         } else {
             layout = (CellLayout) getChildAt(screen);
         }
 
         CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
         if (lp == null) {
             lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
         } else {
             lp.cellX = x;
             lp.cellY = y;
             lp.cellHSpan = spanX;
             lp.cellVSpan = spanY;
         }
 
         if (spanX < 0 && spanY < 0) {
             lp.isLockedToGrid = false;
         }
 
         // Get the canonical child id to uniquely represent this view in this screen
         int childId = LauncherModel.getCellLayoutChildId(container, screen, x, y, spanX, spanY);
         boolean markCellsAsOccupied = !(child instanceof Folder);
         if (!layout.addViewToCellLayout(child, insert ? 0 : -1, childId, lp, markCellsAsOccupied)) {
             // TODO: This branch occurs when the workspace is adding views
             // outside of the defined grid
             // maybe we should be deleting these items from the LauncherModel?
             Log.w(TAG, "Failed to add to item at (" + lp.cellX + "," + lp.cellY + ") to CellLayout");
         }
 
         if (!(child instanceof Folder)) {
             child.setHapticFeedbackEnabled(false);
             child.setOnLongClickListener(mLongClickListener);
         }
         if (child instanceof DropTarget) {
             mDragController.addDropTarget((DropTarget) child);
         }
     }
 
     /**
      * Check if the point (x, y) hits a given page.
      */
     private boolean hitsPage(int index, float x, float y) {
         final View page = getChildAt(index);
         if (page != null) {
             float[] localXY = { x, y };
             mapPointFromSelfToChild(page, localXY);
             return (localXY[0] >= 0 && localXY[0] < page.getWidth()
                     && localXY[1] >= 0 && localXY[1] < page.getHeight());
         }
         return false;
     }
 
     @Override
     protected boolean hitsPreviousPage(float x, float y) {
         // mNextPage is set to INVALID_PAGE whenever we are stationary.
         // Calculating "next page" this way ensures that you scroll to whatever page you tap on
         final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;
         return hitsPage(current - 1, x, y);
     }
 
     @Override
     protected boolean hitsNextPage(float x, float y) {
         // mNextPage is set to INVALID_PAGE whenever we are stationary.
         // Calculating "next page" this way ensures that you scroll to whatever page you tap on
         final int current = (mNextPage == INVALID_PAGE) ? mCurrentPage : mNextPage;
         return hitsPage(current + 1, x, y);
     }
 
     /**
      * Called directly from a CellLayout (not by the framework), after we've been added as a
      * listener via setOnInterceptTouchEventListener(). This allows us to tell the CellLayout
      * that it should intercept touch events, which is not something that is normally supported.
      */
     @Override
     public boolean onTouch(View v, MotionEvent event) {
         return (isSmall() || mIsSwitchingState);
     }
 
     protected void onWindowVisibilityChanged (int visibility) {
         mLauncher.onWindowVisibilityChanged(visibility);
     }
 
     @Override
     public boolean dispatchUnhandledMove(View focused, int direction) {
         if (isSmall() || mIsSwitchingState) {
             // when the home screens are shrunken, shouldn't allow side-scrolling
             return false;
         }
         return super.dispatchUnhandledMove(focused, direction);
     }
 
     @Override
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         if (ev.getAction() == MotionEvent.ACTION_DOWN) {
             mXDown = ev.getX();
             mYDown = ev.getY();
         }
 
         return super.onInterceptTouchEvent(ev);
     }
 
     @Override
     protected void determineScrollingStart(MotionEvent ev) {
         if (!isSmall() && !mIsSwitchingState) {
             float deltaX = Math.abs(ev.getX() - mXDown);
             float deltaY = Math.abs(ev.getY() - mYDown);
 
             if (Float.compare(deltaX, 0f) == 0) return;
 
             float slope = deltaY / deltaX;
             float theta = (float) Math.atan(slope);
 
             if (deltaX > mTouchSlop || deltaY > mTouchSlop) {
                 cancelCurrentPageLongPress();
             }
 
             if (theta > MAX_SWIPE_ANGLE) {
                 // Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the workspace
                 return;
             } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
                 // Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE, we want to
                 // increase the touch slop to make it harder to begin scrolling the workspace. This 
                 // results in vertically scrolling widgets to more easily. The higher the angle, the
                 // more we increase touch slop.
                 theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
                 float extraRatio = (float)
                         Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
                 super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
             } else {
                 // Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything special
                 super.determineScrollingStart(ev);
             }
         }
     }
 
     @Override
     protected boolean isScrollingIndicatorEnabled() {
         return mState != State.SPRING_LOADED;
     }
 
     protected void onPageBeginMoving() {
         super.onPageBeginMoving();
 
         if (isHardwareAccelerated()) {
             updateChildrenLayersEnabled();
         } else {
             if (mNextPage != INVALID_PAGE) {
                 // we're snapping to a particular screen
                 enableChildrenCache(mCurrentPage, mNextPage);
             } else {
                 // this is when user is actively dragging a particular screen, they might
                 // swipe it either left or right (but we won't advance by more than one screen)
                 enableChildrenCache(mCurrentPage - 1, mCurrentPage + 1);
             }
         }
 
         // Only show page outlines as we pan if we are on large screen
         if (LauncherApplication.isScreenLarge()) {
             showOutlines();
         }
     }
 
     protected void onPageEndMoving() {
         super.onPageEndMoving();
 
         if (isHardwareAccelerated()) {
             updateChildrenLayersEnabled();
         } else {
             clearChildrenCache();
         }
 
         // Hide the outlines, as long as we're not dragging
         if (!mDragController.dragging()) {
             // Only hide page outlines as we pan if we are on large screen
             if (LauncherApplication.isScreenLarge()) {
                 hideOutlines();
             }
         }
         mOverScrollMaxBackgroundAlpha = 0.0f;
         mOverScrollPageIndex = -1;
 
         if (mDelayedResizeRunnable != null) {
             mDelayedResizeRunnable.run();
             mDelayedResizeRunnable = null;
         }
     }
 
     @Override
     protected void notifyPageSwitchListener() {
         super.notifyPageSwitchListener();
         Launcher.setScreen(mCurrentPage);
     };
 
     // As a ratio of screen height, the total distance we want the parallax effect to span
     // vertically
     private float wallpaperTravelToScreenHeightRatio(int width, int height) {
         return 1.1f;
     }
 
     // As a ratio of screen height, the total distance we want the parallax effect to span
     // horizontally
     private float wallpaperTravelToScreenWidthRatio(int width, int height) {
         float aspectRatio = width / (float) height;
 
         // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
         // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
         // We will use these two data points to extrapolate how much the wallpaper parallax effect
         // to span (ie travel) at any aspect ratio:
 
         final float ASPECT_RATIO_LANDSCAPE = 16/10f;
         final float ASPECT_RATIO_PORTRAIT = 10/16f;
         final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
         final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;
 
         // To find out the desired width at different aspect ratios, we use the following two
         // formulas, where the coefficient on x is the aspect ratio (width/height):
         //   (16/10)x + y = 1.5
         //   (10/16)x + y = 1.2
         // We solve for x and y and end up with a final formula:
         final float x =
             (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
             (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
         final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
         return x * aspectRatio + y;
     }
 
     // The range of scroll values for Workspace
     private int getScrollRange() {
         return getChildOffset(getChildCount() - 1) - getChildOffset(0);
     }
 
     protected void setWallpaperDimension() {
         Display display = mLauncher.getWindowManager().getDefaultDisplay();
         DisplayMetrics displayMetrics = new DisplayMetrics();
         display.getRealMetrics(displayMetrics);
         final int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
         final int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
 
         // We need to ensure that there is enough extra space in the wallpaper for the intended
         // parallax effects
         mWallpaperWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
         mWallpaperHeight = (int)(maxDim * wallpaperTravelToScreenHeightRatio(maxDim, minDim));
         new Thread("setWallpaperDimension") {
             public void run() {
                 mWallpaperManager.suggestDesiredDimensions(mWallpaperWidth, mWallpaperHeight);
             }
         }.start();
     }
 
     public void setVerticalWallpaperOffset(float offset) {
         mWallpaperOffset.setFinalY(offset);
     }
     public float getVerticalWallpaperOffset() {
         return mWallpaperOffset.getCurrY();
     }
     public void setHorizontalWallpaperOffset(float offset) {
         mWallpaperOffset.setFinalX(offset);
     }
     public float getHorizontalWallpaperOffset() {
         return mWallpaperOffset.getCurrX();
     }
 
     private float wallpaperOffsetForCurrentScroll() {
         Display display = mLauncher.getWindowManager().getDefaultDisplay();
         final boolean isStaticWallpaper = (mWallpaperManager.getWallpaperInfo() == null);
         // The wallpaper travel width is how far, from left to right, the wallpaper will move
         // at this orientation (for example, in portrait mode we don't move all the way to the
         // edges of the wallpaper, or otherwise the parallax effect would be too strong)
         int wallpaperTravelWidth = (int) (display.getWidth() *
                 wallpaperTravelToScreenWidthRatio(display.getWidth(), display.getHeight()));
         if (!isStaticWallpaper) {
             wallpaperTravelWidth = mWallpaperWidth;
         }
 
         // Set wallpaper offset steps (1 / (number of screens - 1))
         // We have 3 vertical offset states (centered, and then top/bottom aligned
         // for all apps/customize)
         mWallpaperManager.setWallpaperOffsetSteps(1.0f / (getChildCount() - 1), 1.0f / (3 - 1));
 
         int scrollRange = getScrollRange();
         float scrollProgressOffset = 0;
 
         // Account for overscroll: you only see the absolute edge of the wallpaper if
         // you overscroll as far as you can in landscape mode. Only do this for static wallpapers
         // because live wallpapers (and probably 3rd party wallpaper providers) rely on the offset
         // being even intervals from 0 to 1 (eg [0, 0.25, 0.5, 0.75, 1])
         if (isStaticWallpaper) {
             int overscrollOffset = (int) (maxOverScroll() * display.getWidth());
             scrollProgressOffset += overscrollOffset / (float) getScrollRange();
             scrollRange += 2 * overscrollOffset;
         }
 
         float scrollProgress =
             mScrollX / (float) scrollRange + scrollProgressOffset;
         float offsetInDips = wallpaperTravelWidth * scrollProgress +
             (mWallpaperWidth - wallpaperTravelWidth) / 2; // center it
         float offset = offsetInDips / (float) mWallpaperWidth;
         return offset;
     }
     private void syncWallpaperOffsetWithScroll() {
         final boolean enableWallpaperEffects = isHardwareAccelerated();
         if (enableWallpaperEffects) {
             mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());
         }
     }
 
     public void updateWallpaperOffsetImmediately() {
         mUpdateWallpaperOffsetImmediately = true;
     }
 
     private void updateWallpaperOffsets() {
         boolean updateNow = false;
         boolean keepUpdating = true;
         if (mUpdateWallpaperOffsetImmediately) {
             updateNow = true;
             keepUpdating = false;
             mWallpaperOffset.jumpToFinal();
             mUpdateWallpaperOffsetImmediately = false;
         } else {
             updateNow = keepUpdating = mWallpaperOffset.computeScrollOffset();
         }
         if (updateNow) {
             if (mWindowToken != null) {
                 mWallpaperManager.setWallpaperOffsets(mWindowToken,
                         mWallpaperOffset.getCurrX(), mWallpaperOffset.getCurrY());
             }
         }
         if (keepUpdating) {
             fastInvalidate();
         }
     }
 
     class WallpaperOffsetInterpolator {
         float mFinalHorizontalWallpaperOffset = 0.0f;
         float mFinalVerticalWallpaperOffset = 0.5f;
         float mHorizontalWallpaperOffset = 0.0f;
         float mVerticalWallpaperOffset = 0.5f;
         long mLastWallpaperOffsetUpdateTime;
         boolean mIsMovingFast;
         boolean mOverrideHorizontalCatchupConstant;
         float mHorizontalCatchupConstant = 0.35f;
         float mVerticalCatchupConstant = 0.35f;
 
         public WallpaperOffsetInterpolator() {
         }
 
         public void setOverrideHorizontalCatchupConstant(boolean override) {
             mOverrideHorizontalCatchupConstant = override;
         }
 
         public void setHorizontalCatchupConstant(float f) {
             mHorizontalCatchupConstant = f;
         }
 
         public void setVerticalCatchupConstant(float f) {
             mVerticalCatchupConstant = f;
         }
 
         public boolean computeScrollOffset() {
             if (Float.compare(mHorizontalWallpaperOffset, mFinalHorizontalWallpaperOffset) == 0 &&
                     Float.compare(mVerticalWallpaperOffset, mFinalVerticalWallpaperOffset) == 0) {
                 mIsMovingFast = false;
                 return false;
             }
             Display display = mLauncher.getWindowManager().getDefaultDisplay();
             boolean isLandscape = display.getWidth() > display.getHeight();
 
             long currentTime = System.currentTimeMillis();
             long timeSinceLastUpdate = currentTime - mLastWallpaperOffsetUpdateTime;
             timeSinceLastUpdate = Math.min((long) (1000/30f), timeSinceLastUpdate);
             timeSinceLastUpdate = Math.max(1L, timeSinceLastUpdate);
 
             float xdiff = Math.abs(mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset);
             if (!mIsMovingFast && xdiff > 0.07) {
                 mIsMovingFast = true;
             }
 
             float fractionToCatchUpIn1MsHorizontal;
             if (mOverrideHorizontalCatchupConstant) {
                 fractionToCatchUpIn1MsHorizontal = mHorizontalCatchupConstant;
             } else if (mIsMovingFast) {
                 fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.5f : 0.75f;
             } else {
                 // slow
                 fractionToCatchUpIn1MsHorizontal = isLandscape ? 0.27f : 0.5f;
             }
             float fractionToCatchUpIn1MsVertical = mVerticalCatchupConstant;
 
             fractionToCatchUpIn1MsHorizontal /= 33f;
             fractionToCatchUpIn1MsVertical /= 33f;
 
             final float UPDATE_THRESHOLD = 0.00001f;
             float hOffsetDelta = mFinalHorizontalWallpaperOffset - mHorizontalWallpaperOffset;
             float vOffsetDelta = mFinalVerticalWallpaperOffset - mVerticalWallpaperOffset;
             boolean jumpToFinalValue = Math.abs(hOffsetDelta) < UPDATE_THRESHOLD &&
                 Math.abs(vOffsetDelta) < UPDATE_THRESHOLD;
             if (jumpToFinalValue) {
                 mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
                 mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
             } else {
                 float percentToCatchUpVertical =
                     Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsVertical);
                 float percentToCatchUpHorizontal =
                     Math.min(1.0f, timeSinceLastUpdate * fractionToCatchUpIn1MsHorizontal);
                 mHorizontalWallpaperOffset += percentToCatchUpHorizontal * hOffsetDelta;
                 mVerticalWallpaperOffset += percentToCatchUpVertical * vOffsetDelta;
             }
 
             mLastWallpaperOffsetUpdateTime = System.currentTimeMillis();
             return true;
         }
 
         public float getCurrX() {
             return mHorizontalWallpaperOffset;
         }
 
         public float getFinalX() {
             return mFinalHorizontalWallpaperOffset;
         }
 
         public float getCurrY() {
             return mVerticalWallpaperOffset;
         }
 
         public float getFinalY() {
             return mFinalVerticalWallpaperOffset;
         }
 
         public void setFinalX(float x) {
             mFinalHorizontalWallpaperOffset = Math.max(0f, Math.min(x, 1.0f));
         }
 
         public void setFinalY(float y) {
             mFinalVerticalWallpaperOffset = Math.max(0f, Math.min(y, 1.0f));
         }
 
         public void jumpToFinal() {
             mHorizontalWallpaperOffset = mFinalHorizontalWallpaperOffset;
             mVerticalWallpaperOffset = mFinalVerticalWallpaperOffset;
         }
     }
 
     @Override
     public void computeScroll() {
         super.computeScroll();
         if (mSyncWallpaperOffsetWithScroll) {
             syncWallpaperOffsetWithScroll();
         }
     }
 
     void showOutlines() {
         if (!isSmall() && !mIsSwitchingState) {
             if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
             if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
             mChildrenOutlineFadeInAnimation = ObjectAnimator.ofFloat(this, "childrenOutlineAlpha", 1.0f);
             mChildrenOutlineFadeInAnimation.setDuration(CHILDREN_OUTLINE_FADE_IN_DURATION);
             mChildrenOutlineFadeInAnimation.start();
         }
     }
 
     void hideOutlines() {
         if (!isSmall() && !mIsSwitchingState) {
             if (mChildrenOutlineFadeInAnimation != null) mChildrenOutlineFadeInAnimation.cancel();
             if (mChildrenOutlineFadeOutAnimation != null) mChildrenOutlineFadeOutAnimation.cancel();
             mChildrenOutlineFadeOutAnimation = ObjectAnimator.ofFloat(this, "childrenOutlineAlpha", 0.0f);
             mChildrenOutlineFadeOutAnimation.setDuration(CHILDREN_OUTLINE_FADE_OUT_DURATION);
             mChildrenOutlineFadeOutAnimation.setStartDelay(CHILDREN_OUTLINE_FADE_OUT_DELAY);
             mChildrenOutlineFadeOutAnimation.start();
         }
     }
 
     public void showOutlinesTemporarily() {
         if (!mIsPageMoving && !isTouchActive()) {
             snapToPage(mCurrentPage);
         }
     }
 
     public void setChildrenOutlineAlpha(float alpha) {
         mChildrenOutlineAlpha = alpha;
         for (int i = 0; i < getChildCount(); i++) {
             CellLayout cl = (CellLayout) getChildAt(i);
             cl.setBackgroundAlpha(alpha);
         }
     }
 
     public float getChildrenOutlineAlpha() {
         return mChildrenOutlineAlpha;
     }
 
     void disableBackground() {
         mDrawBackground = false;
     }
     void enableBackground() {
         mDrawBackground = true;
     }
 
     private void showBackgroundGradientForAllApps() {
         showBackgroundGradient();
     }
 
     private void showBackgroundGradient() {
         if (mBackground == null) return;
         if (mBackgroundFadeOutAnimation != null) mBackgroundFadeOutAnimation.cancel();
         if (mBackgroundFadeInAnimation != null) mBackgroundFadeInAnimation.cancel();
         mBackgroundFadeInAnimation = ValueAnimator.ofFloat(getBackgroundAlpha(), 1f);
         mBackgroundFadeInAnimation.addUpdateListener(new AnimatorUpdateListener() {
             public void onAnimationUpdate(ValueAnimator animation) {
                 setBackgroundAlpha(((Float) animation.getAnimatedValue()).floatValue());
             }
         });
         mBackgroundFadeInAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
         mBackgroundFadeInAnimation.setDuration(BACKGROUND_FADE_IN_DURATION);
         mBackgroundFadeInAnimation.start();
     }
 
     private void hideBackgroundGradient(float finalAlpha) {
         if (mBackground == null) return;
         if (mBackgroundFadeInAnimation != null) mBackgroundFadeInAnimation.cancel();
         if (mBackgroundFadeOutAnimation != null) mBackgroundFadeOutAnimation.cancel();
         mBackgroundFadeOutAnimation = ValueAnimator.ofFloat(getBackgroundAlpha(), finalAlpha);
         mBackgroundFadeOutAnimation.addUpdateListener(new AnimatorUpdateListener() {
             public void onAnimationUpdate(ValueAnimator animation) {
                 setBackgroundAlpha(((Float) animation.getAnimatedValue()).floatValue());
             }
         });
         mBackgroundFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
         mBackgroundFadeOutAnimation.setDuration(BACKGROUND_FADE_OUT_DURATION);
         mBackgroundFadeOutAnimation.start();
     }
 
     public void setBackgroundAlpha(float alpha) {
         if (alpha != mBackgroundAlpha) {
             mBackgroundAlpha = alpha;
             invalidate();
         }
     }
 
     public float getBackgroundAlpha() {
         return mBackgroundAlpha;
     }
 
     /**
      * Due to 3D transformations, if two CellLayouts are theoretically touching each other,
      * on the xy plane, when one is rotated along the y-axis, the gap between them is perceived
      * as being larger. This method computes what offset the rotated view should be translated
      * in order to minimize this perceived gap.
      * @param degrees Angle of the view
      * @param width Width of the view
      * @param height Height of the view
      * @return Offset to be used in a View.setTranslationX() call
      */
     private float getOffsetXForRotation(float degrees, int width, int height) {
         mMatrix.reset();
         mCamera.save();
         mCamera.rotateY(Math.abs(degrees));
         mCamera.getMatrix(mMatrix);
         mCamera.restore();
 
         mMatrix.preTranslate(-width * 0.5f, -height * 0.5f);
         mMatrix.postTranslate(width * 0.5f, height * 0.5f);
         mTempFloat2[0] = width;
         mTempFloat2[1] = height;
         mMatrix.mapPoints(mTempFloat2);
         return (width - mTempFloat2[0]) * (degrees > 0.0f ? 1.0f : -1.0f);
     }
 
     float backgroundAlphaInterpolator(float r) {
         float pivotA = 0.1f;
         float pivotB = 0.4f;
         if (r < pivotA) {
             return 0;
         } else if (r > pivotB) {
             return 1.0f;
         } else {
             return (r - pivotA)/(pivotB - pivotA);
         }
     }
 
     float overScrollBackgroundAlphaInterpolator(float r) {
         float threshold = 0.08f;
 
         if (r > mOverScrollMaxBackgroundAlpha) {
             mOverScrollMaxBackgroundAlpha = r;
         } else if (r < mOverScrollMaxBackgroundAlpha) {
             r = mOverScrollMaxBackgroundAlpha;
         }
 
         return Math.min(r / threshold, 1.0f);
     }
 
     @Override
     protected void screenScrolled(int screenCenter) {
         super.screenScrolled(screenCenter);
 
         // If the screen is not xlarge, then don't rotate the CellLayouts
         // NOTE: If we don't update the side pages alpha, then we should not hide the side pages.
         //       see unshrink().
         if (!LauncherApplication.isScreenLarge()) return;
 
         final int halfScreenSize = getMeasuredWidth() / 2;
 
         for (int i = 0; i < getChildCount(); i++) {
             CellLayout cl = (CellLayout) getChildAt(i);
             if (cl != null) {
                 int totalDistance = getScaledMeasuredWidth(cl) + mPageSpacing;
                 int delta = screenCenter - (getChildOffset(i) -
                         getRelativeChildOffset(i) + halfScreenSize);
 
                 float scrollProgress = delta / (totalDistance * 1.0f);
                 scrollProgress = Math.min(scrollProgress, 1.0f);
                 scrollProgress = Math.max(scrollProgress, -1.0f);
 
                 // If the current page (i) is being overscrolled, we use a different
                 // set of rules for setting the background alpha multiplier.
                 if ((mScrollX < 0 && i == 0) || (mScrollX > mMaxScrollX &&
                         i == getChildCount() -1 )) {
                     cl.setBackgroundAlphaMultiplier(
                             overScrollBackgroundAlphaInterpolator(Math.abs(scrollProgress)));
                     mOverScrollPageIndex = i;
                 } else if (mOverScrollPageIndex != i) {
                     cl.setBackgroundAlphaMultiplier(
                             backgroundAlphaInterpolator(Math.abs(scrollProgress)));
                 }
 
                 float rotation = WORKSPACE_ROTATION * scrollProgress;
                 float translationX = getOffsetXForRotation(rotation, cl.getWidth(), cl.getHeight());
                 cl.setTranslationX(translationX);
 
                 cl.setRotationY(rotation);
             }
         }
     }
 
     protected void onAttachedToWindow() {
         super.onAttachedToWindow();
         mWindowToken = getWindowToken();
         computeScroll();
         mDragController.setWindowToken(mWindowToken);
     }
 
     protected void onDetachedFromWindow() {
         mWindowToken = null;
     }
 
     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
             mUpdateWallpaperOffsetImmediately = true;
         }
         super.onLayout(changed, left, top, right, bottom);
 
         // if shrinkToBottom() is called on initialization, it has to be deferred
         // until after the first call to onLayout so that it has the correct width
         if (mSwitchStateAfterFirstLayout) {
             mSwitchStateAfterFirstLayout = false;
             // shrink can trigger a synchronous onLayout call, so we
             // post this to avoid a stack overflow / tangled onLayout calls
             post(new Runnable() {
                 public void run() {
                     shrink(mStateAfterFirstLayout, false);
                 }
             });
         }
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         updateWallpaperOffsets();
 
         // Draw the background gradient if necessary
         if (mBackground != null && mBackgroundAlpha > 0.0f && mDrawBackground) {
             int alpha = (int) (mBackgroundAlpha * 255);
             mBackground.setAlpha(alpha);
             mBackground.setBounds(mScrollX, 0, mScrollX + getMeasuredWidth(),
                     getMeasuredHeight());
             mBackground.draw(canvas);
         }
 
         super.onDraw(canvas);
     }
 
     @Override
     protected void dispatchDraw(Canvas canvas) {
         super.dispatchDraw(canvas);
 
         if (mInScrollArea && !LauncherApplication.isScreenLarge()) {
             final int width = getWidth();
             final int height = getHeight();
             final int pageHeight = getChildAt(0).getHeight();
 
             // This determines the height of the glowing edge: 90% of the page height
             final int padding = (int) ((height - pageHeight) * 0.5f + pageHeight * 0.1f);
 
             final CellLayout leftPage = (CellLayout) getChildAt(mCurrentPage - 1);
             final CellLayout rightPage = (CellLayout) getChildAt(mCurrentPage + 1);
 
             if (leftPage != null && leftPage.getIsDragOverlapping()) {
                 final Drawable d = getResources().getDrawable(R.drawable.page_hover_left_holo);
                 d.setBounds(mScrollX, padding, mScrollX + d.getIntrinsicWidth(), height - padding);
                 d.draw(canvas);
             } else if (rightPage != null && rightPage.getIsDragOverlapping()) {
                 final Drawable d = getResources().getDrawable(R.drawable.page_hover_right_holo);
                 d.setBounds(mScrollX + width - d.getIntrinsicWidth(), padding, mScrollX + width, height - padding);
                 d.draw(canvas);
             }
         }
     }
 
     @Override
     protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
         if (!mLauncher.isAllAppsVisible()) {
             final Folder openFolder = getOpenFolder();
             if (openFolder != null) {
                 return openFolder.requestFocus(direction, previouslyFocusedRect);
             } else {
                 return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
             }
         }
         return false;
     }
 
     @Override
     public int getDescendantFocusability() {
         if (isSmall()) {
             return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
         }
         return super.getDescendantFocusability();
     }
 
     @Override
     public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
         if (!mLauncher.isAllAppsVisible()) {
             final Folder openFolder = getOpenFolder();
             if (openFolder != null) {
                 openFolder.addFocusables(views, direction);
             } else {
                 super.addFocusables(views, direction, focusableMode);
             }
         }
     }
 
     public boolean isSmall() {
         return mState == State.SMALL || mState == State.SPRING_LOADED;
     }
 
     void enableChildrenCache(int fromPage, int toPage) {
         if (fromPage > toPage) {
             final int temp = fromPage;
             fromPage = toPage;
             toPage = temp;
         }
 
         final int screenCount = getChildCount();
 
         fromPage = Math.max(fromPage, 0);
         toPage = Math.min(toPage, screenCount - 1);
 
         for (int i = fromPage; i <= toPage; i++) {
             final CellLayout layout = (CellLayout) getChildAt(i);
             layout.setChildrenDrawnWithCacheEnabled(true);
             layout.setChildrenDrawingCacheEnabled(true);
         }
     }
 
     void clearChildrenCache() {
         final int screenCount = getChildCount();
         for (int i = 0; i < screenCount; i++) {
             final CellLayout layout = (CellLayout) getChildAt(i);
             layout.setChildrenDrawnWithCacheEnabled(false);
         }
     }
 
     private boolean childLayersEnabled() {
         boolean isSmallOrSpringloaded =
             isSmall() || mIsSwitchingState || mState == State.SPRING_LOADED;
         return isSmallOrSpringloaded || isPageMoving() || mIsDragOccuring;
     }
 
     private void updateChildrenLayersEnabled() {
         boolean small =
             isSmall() || mIsSwitchingState || mState == State.SPRING_LOADED;
         boolean dragging = mAnimatingViewIntoPlace || mIsDragOccuring;
         boolean enableChildrenLayers = small || dragging || isPageMoving();
 
         if (enableChildrenLayers != mChildrenLayersEnabled) {
             mChildrenLayersEnabled = enableChildrenLayers;
             for (int i = 0; i < getPageCount(); i++) {
                 ((ViewGroup)getChildAt(i)).setChildrenLayersEnabled(enableChildrenLayers);
             }
         }
     }
 
     @Override
     protected void onWallpaperTap(MotionEvent ev) {
         final int[] position = mTempCell;
         getLocationOnScreen(position);
 
         int pointerIndex = ev.getActionIndex();
         position[0] += (int) ev.getX(pointerIndex);
         position[1] += (int) ev.getY(pointerIndex);
 
         mWallpaperManager.sendWallpaperCommand(getWindowToken(),
                 ev.getAction() == MotionEvent.ACTION_UP
                         ? WallpaperManager.COMMAND_TAP : WallpaperManager.COMMAND_SECONDARY_TAP,
                 position[0], position[1], 0, null);
     }
 
     private float getYScaleForScreen(int screen) {
         int x = Math.abs(screen - 2);
 
         // TODO: This should be generalized for use with arbitrary rotation angles.
         switch(x) {
             case 0: return EXTRA_SCALE_FACTOR_0;
             case 1: return EXTRA_SCALE_FACTOR_1;
             case 2: return EXTRA_SCALE_FACTOR_2;
         }
         return 1.0f;
     }
 
     public void shrink(State shrinkState) {
         shrink(shrinkState, true);
     }
 
     // we use this to shrink the workspace for the all apps view and the customize view
     public void shrink(State shrinkState, boolean animated) {
         if (mFirstLayout) {
             // (mFirstLayout == "first layout has not happened yet")
             // if we get a call to shrink() as part of our initialization (for example, if
             // Launcher is started in All Apps mode) then we need to wait for a layout call
             // to get our width so we can layout the mini-screen views correctly
             mSwitchStateAfterFirstLayout = true;
             mStateAfterFirstLayout = shrinkState;
             return;
         }
 
         // Stop any scrolling, move to the current page right away
         setCurrentPage((mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage);
 
         CellLayout currentPage = (CellLayout) getChildAt(mCurrentPage);
         if (currentPage == null) {
             Log.w(TAG, "currentPage is NULL! mCurrentPage " + mCurrentPage
                     + " mNextPage " + mNextPage);
             return;
         }
         if (currentPage.getBackgroundAlphaMultiplier() < 1.0f) {
             currentPage.setBackgroundAlpha(0.0f);
         }
         currentPage.setBackgroundAlphaMultiplier(1.0f);
 
         mState = shrinkState;
         updateChildrenLayersEnabled();
 
         // we intercept and reject all touch events when we're small, so be sure to reset the state
         mTouchState = TOUCH_STATE_REST;
         mActivePointerId = INVALID_POINTER;
 
         final Resources res = getResources();
         final int screenWidth = getWidth();
         final int screenHeight = getHeight();
 
         // How much the workspace shrinks when we enter all apps or customization mode
         final float shrinkFactor = res.getInteger(R.integer.config_workspaceShrinkPercent) / 100.0f;
 
         // Making the assumption that all pages have the same width as the 0th
         final int pageWidth = getChildAt(0).getMeasuredWidth();
         final int pageHeight = getChildAt(0).getMeasuredHeight();
 
         final int scaledPageWidth = (int) (shrinkFactor * pageWidth);
         final int scaledPageHeight = (int) (shrinkFactor * pageHeight);
         final float extraScaledSpacing = res.getDimension(R.dimen.smallScreenExtraSpacing);
 
         final int screenCount = getChildCount();
         float totalWidth = screenCount * scaledPageWidth + (screenCount - 1) * extraScaledSpacing;
 
         boolean isPortrait = getMeasuredHeight() > getMeasuredWidth();
         float y = (isPortrait ?
                 getResources().getDimension(R.dimen.allAppsSmallScreenVerticalMarginPortrait) :
                 getResources().getDimension(R.dimen.allAppsSmallScreenVerticalMarginLandscape));
         float finalAlpha = 1.0f;
         float extraShrinkFactor = 1.0f;
 
         // We shrink and disappear to nothing
         y = screenHeight - y - scaledPageHeight;
         finalAlpha = 0.0f;
 
         int duration = res.getInteger(R.integer.config_appsCustomizeWorkspaceShrinkTime);
 
         // We animate all the screens to the centered position in workspace
         // At the same time, the screens become greyed/dimmed
 
         // newX is initialized to the left-most position of the centered screens
         float x = mScroller.getFinalX() + screenWidth / 2 - totalWidth / 2;
 
         // We are going to scale about the center of the view, so we need to adjust the positions
         // of the views accordingly
         x -= (pageWidth - scaledPageWidth) / 2.0f;
         y -= (pageHeight - scaledPageHeight) / 2.0f;
 
         if (mAnimator != null) {
             mAnimator.cancel();
         }
 
         mAnimator = new AnimatorSet();
 
         final int childCount = getChildCount();
         final float[] oldXs = new float[childCount];
         final float[] oldYs = new float[childCount];
         final float[] oldScaleXs = new float[childCount];
         final float[] oldScaleYs = new float[childCount];
         final float[] oldBackgroundAlphas = new float[childCount];
         final float[] oldAlphas = new float[childCount];
         final float[] oldRotationYs = new float[childCount];
         final float[] newXs = new float[childCount];
         final float[] newYs = new float[childCount];
         final float[] newScaleXs = new float[childCount];
         final float[] newScaleYs = new float[childCount];
         final float[] newBackgroundAlphas = new float[childCount];
         final float[] newAlphas = new float[childCount];
         final float[] newRotationYs = new float[childCount];
 
         for (int i = 0; i < screenCount; i++) {
             final CellLayout cl = (CellLayout) getChildAt(i);
 
             float rotation = (-i + 2) * WORKSPACE_ROTATION;
             float rotationScaleX = (float) (1.0f / Math.cos(Math.PI * rotation / 180.0f));
             float rotationScaleY = getYScaleForScreen(i);
 
             oldAlphas[i] = cl.getAlpha();
             newAlphas[i] = finalAlpha;
             if (animated && (oldAlphas[i] != 0f || newAlphas[i] != 0f)) {
                 // if the CellLayout will be visible during the animation, force building its
                 // hardware layer immediately so we don't see a blip later in the animation
                 cl.buildChildrenLayer();
             }
             if (animated) {
                 oldXs[i] = cl.getX();
                 oldYs[i] = cl.getY();
                 oldScaleXs[i] = cl.getScaleX();
                 oldScaleYs[i] = cl.getScaleY();
                 oldBackgroundAlphas[i] = cl.getBackgroundAlpha();
                 oldRotationYs[i] = cl.getRotationY();
                 newXs[i] = x;
                 newYs[i] = y;
                 newScaleXs[i] = shrinkFactor * rotationScaleX * extraShrinkFactor;
                 newScaleYs[i] = shrinkFactor * rotationScaleY * extraShrinkFactor;
                 newBackgroundAlphas[i] = finalAlpha;
                 newRotationYs[i] = rotation;
             } else {
                 cl.setX((int)x);
                 cl.setY((int)y);
                 cl.setScaleX(shrinkFactor * rotationScaleX * extraShrinkFactor);
                 cl.setScaleY(shrinkFactor * rotationScaleY * extraShrinkFactor);
                 cl.setBackgroundAlpha(finalAlpha);
                 cl.setAlpha(finalAlpha);
                 cl.setRotationY(rotation);
                 mShrinkAnimationListener.onAnimationEnd(null);
             }
             // increment newX for the next screen
             x += scaledPageWidth + extraScaledSpacing;
         }
 
         float wallpaperOffset = 0.5f;
         Display display = mLauncher.getWindowManager().getDefaultDisplay();
         int wallpaperTravelHeight = (int) (display.getHeight() *
                 wallpaperTravelToScreenHeightRatio(display.getWidth(), display.getHeight()));
         float offsetFromCenter = (wallpaperTravelHeight / (float) mWallpaperHeight) / 2f;
         boolean isLandscape = display.getWidth() > display.getHeight();
 
         // on phones, don't scroll the wallpaper horizontally or vertically when switching
         // to/from all apps
         final boolean enableWallpaperEffects =
             isHardwareAccelerated() && LauncherApplication.isScreenLarge();
         if (enableWallpaperEffects) {
             switch (shrinkState) {
                 // animating in
                 case SPRING_LOADED:
                     wallpaperOffset = 0.5f;
                     mWallpaperOffset.setVerticalCatchupConstant(isLandscape ? 0.34f : 0.32f);
                     break;
                 case SMALL:
                     // allapps
                     wallpaperOffset = 0.5f - offsetFromCenter;
                     mWallpaperOffset.setVerticalCatchupConstant(isLandscape ? 0.34f : 0.32f);
                     break;
             }
         }
 
         setLayoutScale(1.0f);
         if (animated) {
             if (enableWallpaperEffects) {
                 mWallpaperOffset.setHorizontalCatchupConstant(0.46f);
                 mWallpaperOffset.setOverrideHorizontalCatchupConstant(true);
             }
 
             mSyncWallpaperOffsetWithScroll = false;
 
             ValueAnimator animWithInterpolator =
                 ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
             animWithInterpolator.setInterpolator(mZoomOutInterpolator);
 
             final float oldHorizontalWallpaperOffset = getHorizontalWallpaperOffset();
             final float oldVerticalWallpaperOffset = getVerticalWallpaperOffset();
             final float newHorizontalWallpaperOffset = 0.5f;
             final float newVerticalWallpaperOffset = wallpaperOffset;
             animWithInterpolator.addUpdateListener(new LauncherAnimatorUpdateListener() {
                 public void onAnimationUpdate(float a, float b) {
                     if (b == 0f) {
                         // an optimization, and required for correct behavior.
                         return;
                     }
                     invalidate();
                     if (enableWallpaperEffects) {
                         setHorizontalWallpaperOffset(
                             a * oldHorizontalWallpaperOffset + b * newHorizontalWallpaperOffset);
                         setVerticalWallpaperOffset(
                             a * oldVerticalWallpaperOffset + b * newVerticalWallpaperOffset);
                     }
                     for (int i = 0; i < screenCount; i++) {
                         final CellLayout cl = (CellLayout) getChildAt(i);
                         cl.fastInvalidate();
                         cl.setFastX(a * oldXs[i] + b * newXs[i]);
                         cl.setFastY(a * oldYs[i] + b * newYs[i]);
                         cl.setFastScaleX(a * oldScaleXs[i] + b * newScaleXs[i]);
                         cl.setFastScaleY(a * oldScaleYs[i] + b * newScaleYs[i]);
                         cl.setFastBackgroundAlpha(
                                 a * oldBackgroundAlphas[i] + b * newBackgroundAlphas[i]);
                         cl.setFastAlpha(a * oldAlphas[i] + b * newAlphas[i]);
                         cl.setFastRotationY(a * oldRotationYs[i] + b * newRotationYs[i]);
                     }
                 }
             });
             mAnimator.playTogether(animWithInterpolator);
             mAnimator.addListener(mShrinkAnimationListener);
             mAnimator.start();
         } else if (enableWallpaperEffects) {
             setVerticalWallpaperOffset(wallpaperOffset);
             setHorizontalWallpaperOffset(0.5f);
             updateWallpaperOffsetImmediately();
         }
         setChildrenDrawnWithCacheEnabled(true);
 
         showBackgroundGradientForAllApps();
     }
 
     /*
      * This interpolator emulates the rate at which the perceived scale of an object changes
      * as its distance from a camera increases. When this interpolator is applied to a scale
      * animation on a view, it evokes the sense that the object is shrinking due to moving away
      * from the camera. 
      */
     static class ZInterpolator implements TimeInterpolator {
         private float focalLength;
 
         public ZInterpolator(float foc) {
             focalLength = foc;
         }
 
         public float getInterpolation(float input) {
             return (1.0f - focalLength / (focalLength + input)) /
                 (1.0f - focalLength / (focalLength + 1.0f));
         }
     }
 
     /*
      * The exact reverse of ZInterpolator.
      */
     static class InverseZInterpolator implements TimeInterpolator {
         private ZInterpolator zInterpolator;
         public InverseZInterpolator(float foc) {
             zInterpolator = new ZInterpolator(foc);
         }
         public float getInterpolation(float input) {
             return 1 - zInterpolator.getInterpolation(1 - input);
         }
     }
 
     /*
      * ZInterpolator compounded with an ease-out.
      */
     static class ZoomOutInterpolator implements TimeInterpolator {
         private final ZInterpolator zInterpolator = new ZInterpolator(0.2f);
         private final DecelerateInterpolator decelerate = new DecelerateInterpolator(1.8f);
 
         public float getInterpolation(float input) {
             return decelerate.getInterpolation(zInterpolator.getInterpolation(input));
         }
     }
 
     /*
      * InvereZInterpolator compounded with an ease-out.
      */
     static class ZoomInInterpolator implements TimeInterpolator {
         private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
         private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);
 
         public float getInterpolation(float input) {
             return decelerate.getInterpolation(inverseZInterpolator.getInterpolation(input));
         }
     }
 
     private final ZoomOutInterpolator mZoomOutInterpolator = new ZoomOutInterpolator();
     private final ZoomInInterpolator mZoomInInterpolator = new ZoomInInterpolator();
 
     /*
     *
     * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
     * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
     *
     * These methods mark the appropriate pages as accepting drops (which alters their visual
     * appearance).
     *
     */
     public void onDragStartedWithItem(View v) {
         final Canvas canvas = new Canvas();
 
         // We need to add extra padding to the bitmap to make room for the glow effect
         final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
 
         // The outline is used to visualize where the item will land if dropped
         mDragOutline = createDragOutline(v, canvas, bitmapPadding);
     }
 
     public void onDragStartedWithItemSpans(int spanX, int spanY, Bitmap b) {
         final Canvas canvas = new Canvas();
 
         // We need to add extra padding to the bitmap to make room for the glow effect
         final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
 
         CellLayout cl = (CellLayout) getChildAt(0);
 
         int[] size = cl.cellSpansToSize(spanX, spanY);
 
         // The outline is used to visualize where the item will land if dropped
         mDragOutline = createDragOutline(b, canvas, bitmapPadding, size[0], size[1]);
     }
 
     // we call this method whenever a drag and drop in Launcher finishes, even if Workspace was
     // never dragged over
     public void onDragStopped(boolean success) {
         // In the success case, DragController has already called onDragExit()
         if (!success) {
             doDragExit(null);
         }
     }
 
     // We call this when we trigger an unshrink by clicking on the CellLayout cl
     public void unshrink(CellLayout clThatWasClicked) {
         unshrink(clThatWasClicked, false);
     }
 
     public void unshrink(CellLayout clThatWasClicked, boolean springLoaded) {
         int newCurrentPage = indexOfChild(clThatWasClicked);
         if (isSmall()) {
             if (springLoaded) {
                 setLayoutScale(mSpringLoadedShrinkFactor);
             }
             scrollToNewPageWithoutMovingPages(newCurrentPage);
             unshrink(true, springLoaded);
         }
     }
 
 
     public void enterSpringLoadedDragMode(CellLayout clThatWasClicked) {
         unshrink(clThatWasClicked, true);
     }
 
     public void exitSpringLoadedDragMode(State shrinkState) {
         shrink(shrinkState);
     }
 
     public void exitWidgetResizeMode() {
         DragLayer dragLayer = mLauncher.getDragLayer();
         dragLayer.clearAllResizeFrames();
     }
 
     void unshrink(boolean animated) {
         unshrink(animated, false);
     }
 
     void unshrink(boolean animated, boolean springLoaded) {
         if (isSmall()) {
             float finalScaleFactor = 1.0f;
             float finalBackgroundAlpha = 0.0f;
             if (springLoaded) {
                 finalScaleFactor = mSpringLoadedShrinkFactor;
                 finalBackgroundAlpha = 1.0f;
                 mState = State.SPRING_LOADED;
             } else {
                 mState = State.NORMAL;
             }
             if (mAnimator != null) {
                 mAnimator.cancel();
             }
 
             mAnimator = new AnimatorSet();
             final int screenCount = getChildCount();
 
             final int duration = getResources().getInteger(R.integer.config_workspaceUnshrinkTime);
 
             final float[] oldTranslationXs = new float[getChildCount()];
             final float[] oldTranslationYs = new float[getChildCount()];
             final float[] oldScaleXs = new float[getChildCount()];
             final float[] oldScaleYs = new float[getChildCount()];
             final float[] oldBackgroundAlphas = new float[getChildCount()];
             final float[] oldBackgroundAlphaMultipliers = new float[getChildCount()];
             final float[] oldAlphas = new float[getChildCount()];
             final float[] oldRotationYs = new float[getChildCount()];
             final float[] newTranslationXs = new float[getChildCount()];
             final float[] newTranslationYs = new float[getChildCount()];
             final float[] newScaleXs = new float[getChildCount()];
             final float[] newScaleYs = new float[getChildCount()];
             final float[] newBackgroundAlphas = new float[getChildCount()];
             final float[] newBackgroundAlphaMultipliers = new float[getChildCount()];
             final float[] newAlphas = new float[getChildCount()];
             final float[] newRotationYs = new float[getChildCount()];
 
             for (int i = 0; i < screenCount; i++) {
                 final CellLayout cl = (CellLayout)getChildAt(i);
                 float finalAlphaValue = 0f;
                 float rotation = 0f;
                 if (LauncherApplication.isScreenLarge()) {
                     finalAlphaValue = (i == mCurrentPage) ? 1.0f : 0.0f;
 
                     if (i < mCurrentPage) {
                         rotation = WORKSPACE_ROTATION;
                     } else if (i > mCurrentPage) {
                         rotation = -WORKSPACE_ROTATION;
                     }
                 } else {
                     // Don't hide the side panes on the phone if we don't also update the side pages
                     // alpha.  See screenScrolled().
                     finalAlphaValue = 1f;
                 }
                 float finalAlphaMultiplierValue = 1f;
 
                 float translation = 0f;
 
                 // If the screen is not xlarge, then don't rotate the CellLayouts
                 // NOTE: If we don't update the side pages alpha, then we should not hide the side
                 //       pages. see unshrink().
                 if (LauncherApplication.isScreenLarge()) {
                     translation = getOffsetXForRotation(rotation, cl.getWidth(), cl.getHeight());
                 }
 
                 oldAlphas[i] = cl.getAlpha();
                 newAlphas[i] = finalAlphaValue;
                 if (animated) {
                     oldTranslationXs[i] = cl.getTranslationX();
                     oldTranslationYs[i] = cl.getTranslationY();
                     oldScaleXs[i] = cl.getScaleX();
                     oldScaleYs[i] = cl.getScaleY();
                     oldBackgroundAlphas[i] = cl.getBackgroundAlpha();
                     oldBackgroundAlphaMultipliers[i] = cl.getBackgroundAlphaMultiplier();
                     oldRotationYs[i] = cl.getRotationY();
 
                     newTranslationXs[i] = translation;
                     newTranslationYs[i] = 0f;
                     newScaleXs[i] = finalScaleFactor;
                     newScaleYs[i] = finalScaleFactor;
                     newBackgroundAlphas[i] = finalBackgroundAlpha;
                     newBackgroundAlphaMultipliers[i] = finalAlphaMultiplierValue;
                     newRotationYs[i] = rotation;
                 } else {
                     cl.setTranslationX(translation);
                     cl.setTranslationY(0.0f);
                     cl.setScaleX(finalScaleFactor);
                     cl.setScaleY(finalScaleFactor);
                     cl.setBackgroundAlpha(0.0f);
                     cl.setBackgroundAlphaMultiplier(finalAlphaMultiplierValue);
                     cl.setAlpha(finalAlphaValue);
                     cl.setRotationY(rotation);
                     mUnshrinkAnimationListener.onAnimationEnd(null);
                 }
             }
             Display display = mLauncher.getWindowManager().getDefaultDisplay();
             boolean isLandscape = display.getWidth() > display.getHeight();
             // on phones, don't scroll the wallpaper horizontally or vertically when switching
             // to/from all apps
             final boolean enableWallpaperEffects =
                 isHardwareAccelerated() && LauncherApplication.isScreenLarge();
             if (enableWallpaperEffects) {
                 switch (mState) {
                     // animating out
                     case SPRING_LOADED:
                         if (animated) {
                             mWallpaperOffset.setHorizontalCatchupConstant(isLandscape ? 0.49f : 0.46f);
                             mWallpaperOffset.setVerticalCatchupConstant(isLandscape ? 0.49f : 0.46f);
                             mWallpaperOffset.setOverrideHorizontalCatchupConstant(true);
                         }
                         break;
                     case SMALL:
                         // all apps
                         if (animated) {
                             mWallpaperOffset.setHorizontalCatchupConstant(isLandscape ? 0.65f : 0.65f);
                             mWallpaperOffset.setVerticalCatchupConstant(isLandscape ? 0.65f : 0.65f);
                             mWallpaperOffset.setOverrideHorizontalCatchupConstant(true);
                         }
                         break;
                 }
             }
             if (animated) {
                 ValueAnimator animWithInterpolator =
                     ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
                 animWithInterpolator.setInterpolator(mZoomInInterpolator);
 
                 final float oldHorizontalWallpaperOffset = enableWallpaperEffects ?
                         getHorizontalWallpaperOffset() : 0;
                 final float oldVerticalWallpaperOffset = enableWallpaperEffects ?
                         getVerticalWallpaperOffset() : 0;
                 final float newHorizontalWallpaperOffset = enableWallpaperEffects ?
                         wallpaperOffsetForCurrentScroll() : 0;
                 final float newVerticalWallpaperOffset = enableWallpaperEffects ? 0.5f : 0;
                 animWithInterpolator.addUpdateListener(new LauncherAnimatorUpdateListener() {
                     public void onAnimationUpdate(float a, float b) {
                         if (b == 0f) {
                             // an optimization, but not required
                             return;
                         }
                         invalidate();
                         if (enableWallpaperEffects) {
                             setHorizontalWallpaperOffset(a * oldHorizontalWallpaperOffset
                                     + b * newHorizontalWallpaperOffset);
                             setVerticalWallpaperOffset(a * oldVerticalWallpaperOffset
                                     + b * newVerticalWallpaperOffset);
                         }
                         for (int i = 0; i < screenCount; i++) {
                             final CellLayout cl = (CellLayout) getChildAt(i);
                             cl.fastInvalidate();
                             cl.setFastTranslationX(
                                     a * oldTranslationXs[i] + b * newTranslationXs[i]);
                             cl.setFastTranslationY(
                                     a * oldTranslationYs[i] + b * newTranslationYs[i]);
                             cl.setFastScaleX(a * oldScaleXs[i] + b * newScaleXs[i]);
                             cl.setFastScaleY(a * oldScaleYs[i] + b * newScaleYs[i]);
                             cl.setFastBackgroundAlpha(
                                     a * oldBackgroundAlphas[i] + b * newBackgroundAlphas[i]);
                             cl.setBackgroundAlphaMultiplier(a * oldBackgroundAlphaMultipliers[i] +
                                     b * newBackgroundAlphaMultipliers[i]);
                             cl.setFastAlpha(a * oldAlphas[i] + b * newAlphas[i]);
                         }
                     }
                 });
 
                 ValueAnimator rotationAnim =
                     ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
                 rotationAnim.setInterpolator(new DecelerateInterpolator(2.0f));
                 rotationAnim.addUpdateListener(new LauncherAnimatorUpdateListener() {
                     public void onAnimationUpdate(float a, float b) {
                         // don't invalidate workspace because we did it above
                         if (b == 0f) {
                             // an optimization, but not required
                             return;
                         }
                         for (int i = 0; i < screenCount; i++) {
                             final CellLayout cl = (CellLayout) getChildAt(i);
                             cl.setFastRotationY(a * oldRotationYs[i] + b * newRotationYs[i]);
                         }
                     }
                 });
 
                 mAnimator.playTogether(animWithInterpolator, rotationAnim);
                 // If we call this when we're not animated, onAnimationEnd is never called on
                 // the listener; make sure we only use the listener when we're actually animating
                 mAnimator.addListener(mUnshrinkAnimationListener);
                 mAnimator.start();
             } else {
                 if (enableWallpaperEffects) {
                     setHorizontalWallpaperOffset(wallpaperOffsetForCurrentScroll());
                     setVerticalWallpaperOffset(0.5f);
                     updateWallpaperOffsetImmediately();
                 }
             }
         }
 
         hideBackgroundGradient(springLoaded ? getResources().getInteger(
                 R.integer.config_appsCustomizeSpringLoadedBgAlpha) / 100f : 0f);
     }
 
     /**
      * Draw the View v into the given Canvas.
      *
      * @param v the view to draw
      * @param destCanvas the canvas to draw on
      * @param padding the horizontal and vertical padding to use when drawing
      */
     private void drawDragView(View v, Canvas destCanvas, int padding, boolean pruneToDrawable) {
         final Rect clipRect = mTempRect;
         v.getDrawingRect(clipRect);
 
         destCanvas.save();
         if (v instanceof TextView && pruneToDrawable) {
             Drawable d = ((TextView) v).getCompoundDrawables()[1];
             clipRect.set(0, 0, d.getIntrinsicWidth() + padding, d.getIntrinsicHeight() + padding);
             destCanvas.translate(padding / 2, padding / 2);
             d.draw(destCanvas);
         } else {
             if (v instanceof FolderIcon) {
                 clipRect.bottom = getResources().getDimensionPixelSize(R.dimen.folder_preview_size);
             } else if (v instanceof BubbleTextView) {
                 final BubbleTextView tv = (BubbleTextView) v;
                 clipRect.bottom = tv.getExtendedPaddingTop() - (int) BubbleTextView.PADDING_V +
                         tv.getLayout().getLineTop(0);
             } else if (v instanceof TextView) {
                 final TextView tv = (TextView) v;
                 clipRect.bottom = tv.getExtendedPaddingTop() - tv.getCompoundDrawablePadding() +
                         tv.getLayout().getLineTop(0);
             }
             destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
             destCanvas.clipRect(clipRect, Op.REPLACE);
             v.draw(destCanvas);
         }
         destCanvas.restore();
     }
 
     /**
      * Returns a new bitmap to show when the given View is being dragged around.
      * Responsibility for the bitmap is transferred to the caller.
      */
     public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
         final int outlineColor = getResources().getColor(R.color.drag_outline_color);
         Bitmap b;
 
         if (v instanceof TextView) {
             Drawable d = ((TextView) v).getCompoundDrawables()[1];
             b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
                     d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);
         } else {
             b = Bitmap.createBitmap(
                     v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
         }
 
         canvas.setBitmap(b);
         drawDragView(v, canvas, padding, true);
         mOutlineHelper.applyOuterBlur(b, canvas, outlineColor);
 
         return b;
     }
 
     /**
      * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
      * Responsibility for the bitmap is transferred to the caller.
      */
     private Bitmap createDragOutline(View v, Canvas canvas, int padding) {
         final int outlineColor = getResources().getColor(R.color.drag_outline_color);
         final Bitmap b = Bitmap.createBitmap(
                 v.getWidth() + padding, v.getHeight() + padding, Bitmap.Config.ARGB_8888);
 
         canvas.setBitmap(b);
         drawDragView(v, canvas, padding, false);
         mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
         return b;
     }
 
     /**
      * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
      * Responsibility for the bitmap is transferred to the caller.
      */
     private Bitmap createDragOutline(Bitmap orig, Canvas canvas, int padding, int w, int h) {
         final int outlineColor = getResources().getColor(R.color.drag_outline_color);
         final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         canvas.setBitmap(b);
 
         Rect src = new Rect(0, 0, orig.getWidth(), orig.getHeight());
         float scaleFactor = Math.min((w - padding) / (float) orig.getWidth(),
                 (h - padding) / (float) orig.getHeight());
         int scaledWidth = (int) (scaleFactor * orig.getWidth());
         int scaledHeight = (int) (scaleFactor * orig.getHeight());
         Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);
 
         // center the image
         dst.offset((w - scaledWidth) / 2, (h - scaledHeight) / 2);
 
         Paint p = new Paint();
         p.setFilterBitmap(true);
         canvas.drawBitmap(orig, src, dst, p);
         mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
 
         return b;
     }
 
     /**
      * Creates a drag outline to represent a drop (that we don't have the actual information for
      * yet).  May be changed in the future to alter the drop outline slightly depending on the
      * clip description mime data.
      */
     private Bitmap createExternalDragOutline(Canvas canvas, int padding) {
         Resources r = getResources();
         final int outlineColor = r.getColor(R.color.drag_outline_color);
         final int iconWidth = r.getDimensionPixelSize(R.dimen.workspace_cell_width);
         final int iconHeight = r.getDimensionPixelSize(R.dimen.workspace_cell_height);
         final int rectRadius = r.getDimensionPixelSize(R.dimen.external_drop_icon_rect_radius);
         final int inset = (int) (Math.min(iconWidth, iconHeight) * 0.2f);
         final Bitmap b = Bitmap.createBitmap(
                 iconWidth + padding, iconHeight + padding, Bitmap.Config.ARGB_8888);
 
         canvas.setBitmap(b);
         canvas.drawRoundRect(new RectF(inset, inset, iconWidth - inset, iconHeight - inset),
                 rectRadius, rectRadius, mExternalDragOutlinePaint);
         mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
         return b;
     }
 
     void startDrag(CellLayout.CellInfo cellInfo) {
         View child = cellInfo.cell;
 
         // Make sure the drag was started by a long press as opposed to a long click.
         if (!child.isInTouchMode()) {
             return;
         }
 
         mDragInfo = cellInfo;
 
         CellLayout current = getParentCellLayoutForView(cellInfo.cell);
         current.onDragChild(child);
 
         child.clearFocus();
         child.setPressed(false);
 
         final Canvas canvas = new Canvas();
 
         // We need to add extra padding to the bitmap to make room for the glow effect
         final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
 
         // The outline is used to visualize where the item will land if dropped
         mDragOutline = createDragOutline(child, canvas, bitmapPadding);
         beginDragShared(child, this);
     }
 
     public void beginDragShared(View child, DragSource source) {
         // We need to add extra padding to the bitmap to make room for the glow effect
         final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
 
         // The drag bitmap follows the touch point around on the screen
         final Bitmap b = createDragBitmap(child, new Canvas(), bitmapPadding);
 
         final int bmpWidth = b.getWidth();
         final int bmpHeight = b.getHeight();
 
         mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
         final int dragLayerX = (int) mTempXY[0] + (child.getWidth() - bmpWidth) / 2;
         int dragLayerY = (int) mTempXY[1] + (child.getHeight() - bmpHeight) / 2;
         dragLayerY -= (child.getHeight() - b.getHeight()) / 2;
 
         Rect dragRect = null;
         if (child instanceof BubbleTextView) {
             int iconSize = getResources().getDimensionPixelSize(R.dimen.app_icon_size);
             int top = child.getPaddingTop();
             int left = (bmpWidth - iconSize) / 2;
             int right = left + iconSize;
             int bottom = top + iconSize;
             dragRect = new Rect(left, top, right, bottom);
         } else if (child instanceof FolderIcon) {
             int previewSize = getResources().getDimensionPixelSize(R.dimen.folder_preview_size);
             dragRect = new Rect(0, 0, child.getWidth(), previewSize);
         }
 
         mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                 DragController.DRAG_ACTION_MOVE, dragRect);
         b.recycle();
     }
 
     void addApplicationShortcut(ShortcutInfo info, CellLayout target, long container, int screen,
             int cellX, int cellY, boolean insertAtFirst, int intersectX, int intersectY) {
         View view = mLauncher.createShortcut(R.layout.application, target, (ShortcutInfo) info);
 
         final int[] cellXY = new int[2];
         target.findCellForSpanThatIntersects(cellXY, 1, 1, intersectX, intersectY);
         addInScreen(view, container, screen, cellXY[0], cellXY[1], 1, 1, insertAtFirst);
         LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen, cellXY[0],
                 cellXY[1]);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean acceptDrop(DragObject d) {
         // If it's an external drop (e.g. from All Apps), check if it should be accepted
         if (d.dragSource != this) {
             // Don't accept the drop if we're not over a screen at time of drop
             if (mDragTargetLayout == null) {
                 return false;
             }
 
             mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
                     d.dragView, mDragViewVisualCenter);
 
             int spanX = 1;
             int spanY = 1;
             View ignoreView = null;
             if (mDragInfo != null) {
                 final CellLayout.CellInfo dragCellInfo = mDragInfo;
                 spanX = dragCellInfo.spanX;
                 spanY = dragCellInfo.spanY;
                 ignoreView = dragCellInfo.cell;
             } else {
                 final ItemInfo dragInfo = (ItemInfo) d.dragInfo;
                 spanX = dragInfo.spanX;
                 spanY = dragInfo.spanY;
             }
 
             mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                     (int) mDragViewVisualCenter[1], spanX, spanY, mDragTargetLayout, mTargetCell);
             if (willCreateUserFolder((ItemInfo) d.dragInfo, mDragTargetLayout, mTargetCell, true)) {
                 return true;
             }
             if (willAddToExistingUserFolder((ItemInfo) d.dragInfo, mDragTargetLayout,
                     mTargetCell)) {
                 return true;
             }
 
 
             // Don't accept the drop if there's no room for the item
             if (!mDragTargetLayout.findCellForSpanIgnoring(null, spanX, spanY, ignoreView)) {
                 mLauncher.showOutOfSpaceMessage();
                 return false;
             }
         }
         return true;
     }
 
     boolean willCreateUserFolder(ItemInfo info, CellLayout target, int[] targetCell,
             boolean considerTimeout) {
         View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
 
         boolean hasntMoved = false;
         if (mDragInfo != null) {
             CellLayout cellParent = getParentCellLayoutForView(mDragInfo.cell);
             hasntMoved = (mDragInfo.cellX == targetCell[0] &&
                     mDragInfo.cellY == targetCell[1]) && (cellParent == target);
         }
 
         if (dropOverView == null || hasntMoved || (considerTimeout && !mCreateUserFolderOnDrop)) {
             return false;
         }
 
         boolean aboveShortcut = (dropOverView.getTag() instanceof ShortcutInfo);
         boolean willBecomeShortcut =
                 (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                 info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT);
 
         return (aboveShortcut && willBecomeShortcut);
     }
 
     boolean willAddToExistingUserFolder(Object dragInfo, CellLayout target, int[] targetCell) {
         View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
         if (dropOverView instanceof FolderIcon) {
             FolderIcon fi = (FolderIcon) dropOverView;
             if (fi.acceptDrop(dragInfo)) {
                 return true;
             }
         }
         return false;
     }
 
     boolean createUserFolderIfNecessary(View newView, long container, CellLayout target,
             int[] targetCell, boolean external, DragView dragView, Runnable postAnimationRunnable) {
         View v = target.getChildAt(targetCell[0], targetCell[1]);
         boolean hasntMoved = mDragInfo != null
                 && (mDragInfo.cellX == targetCell[0] && mDragInfo.cellY == targetCell[1]);
 
         if (v == null || hasntMoved || !mCreateUserFolderOnDrop) return false;
         mCreateUserFolderOnDrop = false;
         final int screen = (targetCell == null) ? mDragInfo.screen : indexOfChild(target);
 
         boolean aboveShortcut = (v.getTag() instanceof ShortcutInfo);
         boolean willBecomeShortcut = (newView.getTag() instanceof ShortcutInfo);
 
         if (aboveShortcut && willBecomeShortcut) {
             ShortcutInfo sourceInfo = (ShortcutInfo) newView.getTag();
             ShortcutInfo destInfo = (ShortcutInfo) v.getTag();
             // if the drag started here, we need to remove it from the workspace
             if (!external) {
                 getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
             }
 
             Rect folderLocation = new Rect();
             float scale = mLauncher.getDragLayer().getDescendantRectRelativeToSelf(v, folderLocation);
             target.removeView(v);
 
             FolderIcon fi =
                 mLauncher.addFolder(target, container, screen, targetCell[0], targetCell[1]);
             destInfo.cellX = -1;
             destInfo.cellY = -1;
             sourceInfo.cellX = -1;
             sourceInfo.cellY = -1;
 
             fi.performCreateAnimation(destInfo, v, sourceInfo, dragView, folderLocation, scale,
                     postAnimationRunnable);
             return true;
         }
         return false;
     }
 
     boolean addToExistingFolderIfNecessary(View newView, CellLayout target, int[] targetCell,
             DragObject d, boolean external) {
         View dropOverView = target.getChildAt(targetCell[0], targetCell[1]);
         if (dropOverView instanceof FolderIcon) {
             FolderIcon fi = (FolderIcon) dropOverView;
             if (fi.acceptDrop(d.dragInfo)) {
                 fi.onDrop(d);
 
                 // if the drag started here, we need to remove it from the workspace
                 if (!external) {
                     getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                 }
                 return true;
             }
         }
         return false;
     }
 
     public void onDrop(DragObject d) {
         mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, d.dragView,
                 mDragViewVisualCenter);
 
         // We want the point to be mapped to the dragTarget.
         if (mDragTargetLayout != null) {
             if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
                 mapPointFromSelfToSibling(mLauncher.getHotseat(), mDragViewVisualCenter);
             } else {
                 mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
             }
         }
 
         // When you are in customization mode and drag to a particular screen, make that the
         // new current/default screen, so any subsequent taps add items to that screen
         if (!mLauncher.isAllAppsVisible()) {
             int dragTargetIndex = indexOfChild(mDragTargetLayout);
             if (mCurrentPage != dragTargetIndex && (isSmall() || mIsSwitchingState)) {
                 scrollToNewPageWithoutMovingPages(dragTargetIndex);
             }
         }
         CellLayout dropTargetLayout = mDragTargetLayout;
 
         if (d.dragSource != this) {
             final int[] touchXY = new int[] { (int) mDragViewVisualCenter[0],
                     (int) mDragViewVisualCenter[1] };
             onDropExternal(touchXY, d.dragInfo, dropTargetLayout, false, d);
         } else if (mDragInfo != null) {
             final View cell = mDragInfo.cell;
 
             boolean continueDrop = true;
             if (mLauncher.isHotseatLayout(mDragTargetLayout) && d.dragInfo instanceof ItemInfo) {
                 ItemInfo info = (ItemInfo) d.dragInfo;
                 if (info.spanX > 1 || info.spanY > 1) {
                     continueDrop = false;
                     Toast.makeText(getContext(), R.string.invalid_hotseat_item,
                             Toast.LENGTH_SHORT).show();
                 }
             }
 
             if (continueDrop && dropTargetLayout != null) {
                 // Move internally
                 long container = mLauncher.isHotseatLayout(dropTargetLayout) ?
                         LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                         LauncherSettings.Favorites.CONTAINER_DESKTOP;
                 int screen = (mTargetCell[0] < 0) ?
                         mDragInfo.screen : indexOfChild(dropTargetLayout);
                 int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                 int spanY = mDragInfo != null ? mDragInfo.spanY : 1;
                 // First we find the cell nearest to point at which the item is
                 // dropped, without any consideration to whether there is an item there.
                 mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                         mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                 // If the item being dropped is a shortcut and the nearest drop
                 // cell also contains a shortcut, then create a folder with the two shortcuts.
                 boolean dropInscrollArea = mCurrentPage != screen && screen > -1;
 
                 if (!dropInscrollArea && createUserFolderIfNecessary(cell, container,
                         dropTargetLayout, mTargetCell, false, d.dragView, null)) {
                     return;
                 }
 
                 if (addToExistingFolderIfNecessary(cell, dropTargetLayout, mTargetCell, d, false)) {
                     return;
                 }
 
                 // Aside from the special case where we're dropping a shortcut onto a shortcut,
                 // we need to find the nearest cell location that is vacant
                 mTargetCell = findNearestVacantArea((int) mDragViewVisualCenter[0],
                         (int) mDragViewVisualCenter[1], mDragInfo.spanX, mDragInfo.spanY, cell,
                         dropTargetLayout, mTargetCell);
 
                 if (dropInscrollArea && mState != State.SPRING_LOADED) {
                     snapToPage(screen);
                 }
 
 
                 if (mTargetCell[0] >= 0 && mTargetCell[1] >= 0) {
                     if (screen != mDragInfo.screen) {
                         // Reparent the view
                         getParentCellLayoutForView(cell).removeView(cell);
                         addInScreen(cell, container, screen, mTargetCell[0], mTargetCell[1],
                                 mDragInfo.spanX, mDragInfo.spanY);
                     }
 
                     // update the item's position after drop
                     final ItemInfo info = (ItemInfo) cell.getTag();
                     CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                     dropTargetLayout.onMove(cell, mTargetCell[0], mTargetCell[1]);
                     lp.cellX = mTargetCell[0];
                     lp.cellY = mTargetCell[1];
                     cell.setId(LauncherModel.getCellLayoutChildId(container, mDragInfo.screen,
                             mTargetCell[0], mTargetCell[1], mDragInfo.spanX, mDragInfo.spanY));
 
                     if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                             cell instanceof LauncherAppWidgetHostView) {
                         final CellLayout cellLayout = dropTargetLayout;
                         // We post this call so that the widget has a chance to be placed
                         // in its final location
 
                         final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                         AppWidgetProviderInfo pinfo = hostView.getAppWidgetInfo();
                         if (pinfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE) {
                             final Runnable resizeRunnable = new Runnable() {
                                 public void run() {
                                     DragLayer dragLayer = mLauncher.getDragLayer();
                                     dragLayer.addResizeFrame(info, hostView, cellLayout);
                                 }
                             };
                             post(new Runnable() {
                                 public void run() {
                                     if (!isPageMoving()) {
                                         resizeRunnable.run();
                                     } else {
                                         mDelayedResizeRunnable = resizeRunnable;
                                     }
                                 }
                             });
                         }
                     }
 
                     LauncherModel.moveItemInDatabase(mLauncher, info, container, screen, lp.cellX,
                             lp.cellY);
                 }
             }
 
             final CellLayout parent = (CellLayout) cell.getParent().getParent();
 
             // Prepare it to be animated into its new position
             // This must be called after the view has been re-parented
             final Runnable disableHardwareLayersRunnable = new Runnable() {
                 @Override
                 public void run() {
                     mAnimatingViewIntoPlace = false;
                     updateChildrenLayersEnabled();
                 }
             };
             mAnimatingViewIntoPlace = true;
             mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, cell,
                     disableHardwareLayersRunnable);
             parent.onDropChild(cell);
         }
     }
 
     public void getViewLocationRelativeToSelf(View v, int[] location) {
         getLocationInWindow(location);
         int x = location[0];
         int y = location[1];
 
         v.getLocationInWindow(location);
         int vX = location[0];
         int vY = location[1];
 
         location[0] = vX - x;
         location[1] = vY - y;
     }
 
     public void onDragEnter(DragObject d) {
         if (mDragTargetLayout != null) {
             mDragTargetLayout.setIsDragOverlapping(false);
             mDragTargetLayout.onDragExit();
         }
         mDragTargetLayout = getCurrentDropLayout();
         mDragTargetLayout.setIsDragOverlapping(true);
         mDragTargetLayout.onDragEnter();
 
         // Because we don't have space in the Phone UI (the CellLayouts run to the edge) we
         // don't need to show the outlines
         if (LauncherApplication.isScreenLarge()) {
             showOutlines();
         }
     }
 
     private void doDragExit(DragObject d) {
         // Clean up folders
         cleanupFolderCreation(d);
 
         // Reset the scroll area and previous drag target
         onResetScrollArea();
 
         if (mDragTargetLayout != null) {
             mDragTargetLayout.setIsDragOverlapping(false);
             mDragTargetLayout.onDragExit();
         }
         mLastDragOverView = null;
 
         if (!mIsPageMoving) {
             hideOutlines();
         }
     }
 
     public void onDragExit(DragObject d) {
         doDragExit(d);
     }
 
     public DropTarget getDropTargetDelegate(DragObject d) {
         return null;
     }
 
     /**
      * Tests to see if the drop will be accepted by Launcher, and if so, includes additional data
      * in the returned structure related to the widgets that match the drop (or a null list if it is
      * a shortcut drop).  If the drop is not accepted then a null structure is returned.
      */
     private Pair<Integer, List<WidgetMimeTypeHandlerData>> validateDrag(DragEvent event) {
         final LauncherModel model = mLauncher.getModel();
         final ClipDescription desc = event.getClipDescription();
         final int mimeTypeCount = desc.getMimeTypeCount();
         for (int i = 0; i < mimeTypeCount; ++i) {
             final String mimeType = desc.getMimeType(i);
             if (mimeType.equals(InstallShortcutReceiver.SHORTCUT_MIMETYPE)) {
                 return new Pair<Integer, List<WidgetMimeTypeHandlerData>>(i, null);
             } else {
                 final List<WidgetMimeTypeHandlerData> widgets =
                     model.resolveWidgetsForMimeType(mContext, mimeType);
                 if (widgets.size() > 0) {
                     return new Pair<Integer, List<WidgetMimeTypeHandlerData>>(i, widgets);
                 }
             }
         }
         return null;
     }
 
     /**
      * Global drag and drop handler
      */
     @Override
     public boolean onDragEvent(DragEvent event) {
         final ClipDescription desc = event.getClipDescription();
         final CellLayout layout = (CellLayout) getChildAt(mCurrentPage);
         final int[] pos = new int[2];
         layout.getLocationOnScreen(pos);
         // We need to offset the drag coordinates to layout coordinate space
         final int x = (int) event.getX() - pos[0];
         final int y = (int) event.getY() - pos[1];
 
         switch (event.getAction()) {
         case DragEvent.ACTION_DRAG_STARTED: {
             // Validate this drag
             Pair<Integer, List<WidgetMimeTypeHandlerData>> test = validateDrag(event);
             if (test != null) {
                 boolean isShortcut = (test.second == null);
                 if (isShortcut) {
                     // Check if we have enough space on this screen to add a new shortcut
                     if (!layout.findCellForSpan(pos, 1, 1)) {
                         mLauncher.showOutOfSpaceMessage();
                         return false;
                     }
                 }
             } else {
                 // Show error message if we couldn't accept any of the items
                 Toast.makeText(mContext, mContext.getString(R.string.external_drop_widget_error),
                         Toast.LENGTH_SHORT).show();
                 return false;
             }
 
             // Create the drag outline
             // We need to add extra padding to the bitmap to make room for the glow effect
             final Canvas canvas = new Canvas();
             final int bitmapPadding = HolographicOutlineHelper.MAX_OUTER_BLUR_RADIUS;
             mDragOutline = createExternalDragOutline(canvas, bitmapPadding);
 
             // Show the current page outlines to indicate that we can accept this drop
             showOutlines();
             layout.setIsDragOccuring(true);
             layout.onDragEnter();
             layout.visualizeDropLocation(null, mDragOutline, x, y, 1, 1);
 
             return true;
         }
         case DragEvent.ACTION_DRAG_LOCATION:
             // Visualize the drop location
             layout.visualizeDropLocation(null, mDragOutline, x, y, 1, 1);
             return true;
         case DragEvent.ACTION_DROP: {
             // Try and add any shortcuts
             final LauncherModel model = mLauncher.getModel();
             final ClipData data = event.getClipData();
 
             // We assume that the mime types are ordered in descending importance of
             // representation. So we enumerate the list of mime types and alert the
             // user if any widgets can handle the drop.  Only the most preferred
             // representation will be handled.
             pos[0] = x;
             pos[1] = y;
             Pair<Integer, List<WidgetMimeTypeHandlerData>> test = validateDrag(event);
             if (test != null) {
                 final int index = test.first;
                 final List<WidgetMimeTypeHandlerData> widgets = test.second;
                 final boolean isShortcut = (widgets == null);
                 final String mimeType = desc.getMimeType(index);
                 if (isShortcut) {
                     final Intent intent = data.getItemAt(index).getIntent();
                     Object info = model.infoFromShortcutIntent(mContext, intent, data.getIcon());
                     onDropExternal(new int[] { x, y }, info, layout, false);
                 } else {
                     if (widgets.size() == 1) {
                         // If there is only one item, then go ahead and add and configure
                         // that widget
                         final AppWidgetProviderInfo widgetInfo = widgets.get(0).widgetInfo;
                         final PendingAddWidgetInfo createInfo =
                                 new PendingAddWidgetInfo(widgetInfo, mimeType, data);
                         mLauncher.addAppWidgetFromDrop(createInfo,
                             LauncherSettings.Favorites.CONTAINER_DESKTOP, mCurrentPage, null, pos);
                     } else {
                         // Show the widget picker dialog if there is more than one widget
                         // that can handle this data type
                         final InstallWidgetReceiver.WidgetListAdapter adapter =
                             new InstallWidgetReceiver.WidgetListAdapter(mLauncher, mimeType,
                                     data, widgets, layout, mCurrentPage, pos);
                         final AlertDialog.Builder builder =
                             new AlertDialog.Builder(mContext);
                         builder.setAdapter(adapter, adapter);
                         builder.setCancelable(true);
                         builder.setTitle(mContext.getString(
                                 R.string.external_drop_widget_pick_title));
                         builder.setIcon(R.drawable.ic_no_applications);
                         builder.show();
                     }
                 }
             }
             return true;
         }
         case DragEvent.ACTION_DRAG_ENDED:
             // Hide the page outlines after the drop
             layout.setIsDragOccuring(false);
             layout.onDragExit();
             hideOutlines();
             return true;
         }
         return super.onDragEvent(event);
     }
 
     /*
     *
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     */
    void mapPointFromSelfToChild(View v, float[] xy) {
        mapPointFromSelfToChild(v, xy, null);
    }
 
    /*
     *
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     * if cachedInverseMatrix is not null, this method will just use that matrix instead of
     * computing it itself; we use this to avoid redundant matrix inversions in
     * findMatchingPageForDragOver
     *
     */
    void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        if (cachedInverseMatrix == null) {
            v.getMatrix().invert(mTempInverseMatrix);
            cachedInverseMatrix = mTempInverseMatrix;
        }
        xy[0] = xy[0] + mScrollX - v.getLeft();
        xy[1] = xy[1] + mScrollY - v.getTop();
        cachedInverseMatrix.mapPoints(xy);
    }
 
    /*
     * Maps a point from the Workspace's coordinate system to another sibling view's. (Workspace
     * covers the full screen)
     */
    void mapPointFromSelfToSibling(View v, float[] xy) {
        xy[0] = xy[0] - v.getLeft();
        xy[1] = xy[1] - v.getTop();
    }
 
    /*
     *
     * Convert the 2D coordinate xy from this CellLayout's coordinate space to
     * the parent View's coordinate space. The argument xy is modified with the return result.
     *
     */
    void mapPointFromChildToSelf(View v, float[] xy) {
        v.getMatrix().mapPoints(xy);
        xy[0] -= (mScrollX - v.getLeft());
        xy[1] -= (mScrollY - v.getTop());
    }
 
    static private float squaredDistance(float[] point1, float[] point2) {
         float distanceX = point1[0] - point2[0];
         float distanceY = point2[1] - point2[1];
         return distanceX * distanceX + distanceY * distanceY;
    }
 
     /*
      *
      * Returns true if the passed CellLayout cl overlaps with dragView
      *
      */
     boolean overlaps(CellLayout cl, DragView dragView,
             int dragViewX, int dragViewY, Matrix cachedInverseMatrix) {
         // Transform the coordinates of the item being dragged to the CellLayout's coordinates
         final float[] draggedItemTopLeft = mTempDragCoordinates;
         draggedItemTopLeft[0] = dragViewX;
         draggedItemTopLeft[1] = dragViewY;
         final float[] draggedItemBottomRight = mTempDragBottomRightCoordinates;
         draggedItemBottomRight[0] = draggedItemTopLeft[0] + dragView.getDragRegionWidth();
         draggedItemBottomRight[1] = draggedItemTopLeft[1] + dragView.getDragRegionHeight();
 
         // Transform the dragged item's top left coordinates
         // to the CellLayout's local coordinates
         mapPointFromSelfToChild(cl, draggedItemTopLeft, cachedInverseMatrix);
         float overlapRegionLeft = Math.max(0f, draggedItemTopLeft[0]);
         float overlapRegionTop = Math.max(0f, draggedItemTopLeft[1]);
 
         if (overlapRegionLeft <= cl.getWidth() && overlapRegionTop >= 0) {
             // Transform the dragged item's bottom right coordinates
             // to the CellLayout's local coordinates
             mapPointFromSelfToChild(cl, draggedItemBottomRight, cachedInverseMatrix);
             float overlapRegionRight = Math.min(cl.getWidth(), draggedItemBottomRight[0]);
             float overlapRegionBottom = Math.min(cl.getHeight(), draggedItemBottomRight[1]);
 
             if (overlapRegionRight >= 0 && overlapRegionBottom <= cl.getHeight()) {
                 float overlap = (overlapRegionRight - overlapRegionLeft) *
                          (overlapRegionBottom - overlapRegionTop);
                 if (overlap > 0) {
                     return true;
                 }
              }
         }
         return false;
     }
 
     /*
      *
      * This method returns the CellLayout that is currently being dragged to. In order to drag
      * to a CellLayout, either the touch point must be directly over the CellLayout, or as a second
      * strategy, we see if the dragView is overlapping any CellLayout and choose the closest one
      *
      * Return null if no CellLayout is currently being dragged over
      *
      */
     private CellLayout findMatchingPageForDragOver(
            DragView dragView, int originX, int originY, int offsetX, int offsetY, boolean exact) {
         // We loop through all the screens (ie CellLayouts) and see which ones overlap
         // with the item being dragged and then choose the one that's closest to the touch point
         final int screenCount = getChildCount();
         CellLayout bestMatchingScreen = null;
         float smallestDistSoFar = Float.MAX_VALUE;
 
         for (int i = 0; i < screenCount; i++) {
             CellLayout cl = (CellLayout) getChildAt(i);
 
            final float[] touchXy = mTempTouchCoordinates;
            touchXy[0] = originX + offsetX;
            touchXy[1] = originY + offsetY;

             // Transform the touch coordinates to the CellLayout's local coordinates
             // If the touch point is within the bounds of the cell layout, we can return immediately
             cl.getMatrix().invert(mTempInverseMatrix);
             mapPointFromSelfToChild(cl, touchXy, mTempInverseMatrix);
 
             if (touchXy[0] >= 0 && touchXy[0] <= cl.getWidth() &&
                     touchXy[1] >= 0 && touchXy[1] <= cl.getHeight()) {
                 return cl;
             }
 
            if (!exact && overlaps(cl, dragView, originX, originY, mTempInverseMatrix)) {
                 // Get the center of the cell layout in screen coordinates
                 final float[] cellLayoutCenter = mTempCellLayoutCenterCoordinates;
                 cellLayoutCenter[0] = cl.getWidth()/2;
                 cellLayoutCenter[1] = cl.getHeight()/2;
                 mapPointFromChildToSelf(cl, cellLayoutCenter);
 
                touchXy[0] = originX + offsetX;
                touchXy[1] = originY + offsetY;
 
                 // Calculate the distance between the center of the CellLayout
                 // and the touch point
                 float dist = squaredDistance(touchXy, cellLayoutCenter);
 
                 if (dist < smallestDistSoFar) {
                     smallestDistSoFar = dist;
                     bestMatchingScreen = cl;
                 }
             }
         }
         return bestMatchingScreen;
     }
 
     // This is used to compute the visual center of the dragView. This point is then
     // used to visualize drop locations and determine where to drop an item. The idea is that
     // the visual center represents the user's interpretation of where the item is, and hence
     // is the appropriate point to use when determining drop location.
     private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
             DragView dragView, float[] recycle) {
         float res[];
         if (recycle == null) {
             res = new float[2];
         } else {
             res = recycle;
         }
 
         // First off, the drag view has been shifted in a way that is not represented in the
         // x and y values or the x/yOffsets. Here we account for that shift.
         x += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
         y += getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY);
 
         // These represent the visual top and left of drag view if a dragRect was provided.
         // If a dragRect was not provided, then they correspond to the actual view left and
         // top, as the dragRect is in that case taken to be the entire dragView.
         // R.dimen.dragViewOffsetY.
         int left = x - xOffset;
         int top = y - yOffset;
 
         // In order to find the visual center, we shift by half the dragRect
         res[0] = left + dragView.getDragRegion().width() / 2;
         res[1] = top + dragView.getDragRegion().height() / 2;
 
         return res;
     }
 
     public void onDragOver(DragObject d) {
         // Skip drag over events while we are dragging over side pages
         if (mInScrollArea) return;
         if (mIsSwitchingState) return;
 
         CellLayout layout = null;
         ItemInfo item = (ItemInfo) d.dragInfo;
 
         // Ensure that we have proper spans for the item that we are dropping
         if (item.spanX < 0 || item.spanY < 0) throw new RuntimeException("Improper spans found");
 
         // Identify whether we have dragged over a side page
         if (isSmall()) {
             int left = d.x - d.xOffset;
             int top = d.y - d.yOffset;
            layout = findMatchingPageForDragOver(d.dragView, left, top, d.xOffset, d.yOffset, true);
             if (layout != mDragTargetLayout) {
                 // Cancel all intermediate folder states
                 cleanupFolderCreation(d);
 
                 if (mDragTargetLayout != null) {
                     mDragTargetLayout.setIsDragOverlapping(false);
                     mDragTargetLayout.onDragExit();
                 }
                 mDragTargetLayout = layout;
                 if (mDragTargetLayout != null) {
                     mDragTargetLayout.setIsDragOverlapping(true);
                     mDragTargetLayout.onDragEnter();
                 } else {
                     mLastDragOverView = null;
                 }
 
                 boolean isInSpringLoadedMode = (mState == State.SPRING_LOADED);
                 if (isInSpringLoadedMode) {
                     mSpringLoadedDragController.setAlarm(mDragTargetLayout);
                 }
             }
         } else {
             // Test to see if we are over the hotseat otherwise just use the current page
             Rect r = new Rect();
             if (mLauncher.getHotseat() != null) {
                 mLauncher.getHotseat().getHitRect(r);
                 if (r.contains(d.x, d.y)) {
                     layout = mLauncher.getHotseat().getLayout();
                 }
             }
             if (layout == null) {
                 layout = getCurrentDropLayout();
             }
             if (layout != mDragTargetLayout) {
                 if (mDragTargetLayout != null) {
                     mDragTargetLayout.setIsDragOverlapping(false);
                     mDragTargetLayout.onDragExit();
                 }
                 mDragTargetLayout = layout;
                 mDragTargetLayout.setIsDragOverlapping(true);
                 mDragTargetLayout.onDragEnter();
             }
         }
 
         // Handle the drag over
         if (mDragTargetLayout != null) {
             final View child = (mDragInfo == null) ? null : mDragInfo.cell;
 
            mDragViewVisualCenter = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset,
                    d.dragView, mDragViewVisualCenter);

             // We want the point to be mapped to the dragTarget.
             if (mLauncher.isHotseatLayout(mDragTargetLayout)) {
                 mapPointFromSelfToSibling(mLauncher.getHotseat(), mDragViewVisualCenter);
             } else {
                 mapPointFromSelfToChild(mDragTargetLayout, mDragViewVisualCenter, null);
             }
             ItemInfo info = (ItemInfo) d.dragInfo;
 
             mTargetCell = findNearestArea((int) mDragViewVisualCenter[0],
                     (int) mDragViewVisualCenter[1], 1, 1, mDragTargetLayout, mTargetCell);
             final View dragOverView = mDragTargetLayout.getChildAt(mTargetCell[0],
                     mTargetCell[1]);
 
             boolean userFolderPending = willCreateUserFolder(info, mDragTargetLayout,
                     mTargetCell, false);
             boolean isOverFolder = dragOverView instanceof FolderIcon;
             if (dragOverView != mLastDragOverView) {
                 cancelFolderCreation();
                 if (mLastDragOverView != null && mLastDragOverView instanceof FolderIcon) {
                     ((FolderIcon) mLastDragOverView).onDragExit(d.dragInfo);
                 }
             }
 
             if (userFolderPending && dragOverView != mLastDragOverView) {
                 mFolderCreationAlarm.setOnAlarmListener(new
                         FolderCreationAlarmListener(mDragTargetLayout, mTargetCell[0], mTargetCell[1]));
                 mFolderCreationAlarm.setAlarm(FOLDER_CREATION_TIMEOUT);
             }
 
             if (dragOverView != mLastDragOverView && isOverFolder) {
                 ((FolderIcon) dragOverView).onDragEnter(d.dragInfo);
                 if (mDragTargetLayout != null) {
                     mDragTargetLayout.clearDragOutlines();
                 }
             }
             mLastDragOverView = dragOverView;
 
             if (!mCreateUserFolderOnDrop && !isOverFolder) {
                 mDragTargetLayout.visualizeDropLocation(child, mDragOutline,
                         (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                         item.spanX, item.spanY);
             }
         }
     }
 
     private void cleanupFolderCreation(DragObject d) {
         if (mDragFolderRingAnimator != null && mCreateUserFolderOnDrop) {
             mDragFolderRingAnimator.animateToNaturalState();
         }
         if (mLastDragOverView != null && mLastDragOverView instanceof FolderIcon) {
             if (d != null) {
                 ((FolderIcon) mLastDragOverView).onDragExit(d.dragInfo);
             }
         }
         mFolderCreationAlarm.cancelAlarm();
     }
 
     private void cancelFolderCreation() {
         if (mDragFolderRingAnimator != null && mCreateUserFolderOnDrop) {
             mDragFolderRingAnimator.animateToNaturalState();
         }
         mCreateUserFolderOnDrop = false;
         mFolderCreationAlarm.cancelAlarm();
     }
 
     class FolderCreationAlarmListener implements OnAlarmListener {
         CellLayout layout;
         int cellX;
         int cellY;
 
         public FolderCreationAlarmListener(CellLayout layout, int cellX, int cellY) {
             this.layout = layout;
             this.cellX = cellX;
             this.cellY = cellY;
         }
 
         public void onAlarm(Alarm alarm) {
             if (mDragFolderRingAnimator == null) {
                 mDragFolderRingAnimator = new FolderRingAnimator(mLauncher, null);
             }
             mDragFolderRingAnimator.setCell(cellX, cellY);
             mDragFolderRingAnimator.setCellLayout(layout);
             mDragFolderRingAnimator.animateToAcceptState();
             layout.showFolderAccept(mDragFolderRingAnimator);
             layout.clearDragOutlines();
             mCreateUserFolderOnDrop = true;
         }
     }
 
     @Override
     public void getHitRect(Rect outRect) {
         // We want the workspace to have the whole area of the display (it will find the correct
         // cell layout to drop to in the existing drag/drop logic.
         final Display d = mLauncher.getWindowManager().getDefaultDisplay();
         outRect.set(0, 0, d.getWidth(), d.getHeight());
     }
 
     /**
      * Add the item specified by dragInfo to the given layout.
      * @return true if successful
      */
     public boolean addExternalItemToScreen(ItemInfo dragInfo, CellLayout layout) {
         if (layout.findCellForSpan(mTempEstimate, dragInfo.spanX, dragInfo.spanY)) {
             onDropExternal(dragInfo.dropPos, (ItemInfo) dragInfo, (CellLayout) layout, false);
             return true;
         }
         mLauncher.showOutOfSpaceMessage();
         return false;
     }
 
     private void onDropExternal(int[] touchXY, Object dragInfo,
             CellLayout cellLayout, boolean insertAtFirst) {
         onDropExternal(touchXY, dragInfo, cellLayout, insertAtFirst, null);
     }
 
     /**
      * Drop an item that didn't originate on one of the workspace screens.
      * It may have come from Launcher (e.g. from all apps or customize), or it may have
      * come from another app altogether.
      *
      * NOTE: This can also be called when we are outside of a drag event, when we want
      * to add an item to one of the workspace screens.
      */
     private void onDropExternal(final int[] touchXY, final Object dragInfo,
             final CellLayout cellLayout, boolean insertAtFirst, DragObject d) {
         final Runnable exitSpringLoadedRunnable = new Runnable() {
             @Override
             public void run() {
                 mLauncher.exitSpringLoadedDragModeDelayed(false);
             }
         };
 
         ItemInfo info = (ItemInfo) dragInfo;
         int spanX = info.spanX;
         int spanY = info.spanY;
         if (mDragInfo != null) {
             spanX = mDragInfo.spanX;
             spanY = mDragInfo.spanY;
         }
 
         final long container = mLauncher.isHotseatLayout(cellLayout) ?
                 LauncherSettings.Favorites.CONTAINER_HOTSEAT :
                     LauncherSettings.Favorites.CONTAINER_DESKTOP;
         final int screen = indexOfChild(cellLayout);
         if (!mLauncher.isHotseatLayout(cellLayout) && screen != mCurrentPage
                 && mState != State.SPRING_LOADED) {
             snapToPage(screen);
         }
 
         if (info instanceof PendingAddItemInfo) {
             final PendingAddItemInfo pendingInfo = (PendingAddItemInfo) dragInfo;
 
             mTargetCell = findNearestVacantArea(touchXY[0], touchXY[1], spanX, spanY, null,
                     cellLayout, mTargetCell);
             Runnable onAnimationCompleteRunnable = new Runnable() {
                 @Override
                 public void run() {
                     // When dragging and dropping from customization tray, we deal with creating
                     // widgets/shortcuts/folders in a slightly different way
                     switch (pendingInfo.itemType) {
                     case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
                         mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingInfo,
                                 container, screen, mTargetCell, null);
                         break;
                     case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                         mLauncher.processShortcutFromDrop(pendingInfo.componentName,
                                 container, screen, mTargetCell, null);
                         break;
                     default:
                         throw new IllegalStateException("Unknown item type: " +
                                 pendingInfo.itemType);
                     }
                     cellLayout.onDragExit();
                 }
             };
 
             // Now we animate the dragView, (ie. the widget or shortcut preview) into its final
             // location and size on the home screen.
             int loc[] = new int[2];
             cellLayout.cellToPoint(mTargetCell[0], mTargetCell[1], loc);
 
             RectF r = new RectF();
             cellLayout.cellToRect(mTargetCell[0], mTargetCell[1], spanX, spanY, r);
             float cellLayoutScale =
                     mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(cellLayout, loc);
 
             float dragViewScale =  r.width() / d.dragView.getMeasuredWidth();
             // The animation will scale the dragView about its center, so we need to center about
             // the final location.
             loc[0] -= (d.dragView.getMeasuredWidth() - cellLayoutScale * r.width()) / 2;
             loc[1] -= (d.dragView.getMeasuredHeight() - cellLayoutScale * r.height()) / 2;
 
             mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, loc,
                     dragViewScale * cellLayoutScale, onAnimationCompleteRunnable);
         } else {
             // This is for other drag/drop cases, like dragging from All Apps
             View view = null;
 
             switch (info.itemType) {
             case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
             case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                 if (info.container == NO_ID && info instanceof ApplicationInfo) {
                     // Came from all apps -- make a copy
                     info = new ShortcutInfo((ApplicationInfo) info);
                 }
                 view = mLauncher.createShortcut(R.layout.application, cellLayout,
                         (ShortcutInfo) info);
                 break;
             case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
                 view = FolderIcon.fromXml(R.layout.folder_icon, mLauncher, cellLayout,
                         (FolderInfo) info, mIconCache);
                 break;
             default:
                 throw new IllegalStateException("Unknown item type: " + info.itemType);
             }
 
             // First we find the cell nearest to point at which the item is
             // dropped, without any consideration to whether there is an item there.
             if (touchXY != null) {
                 mTargetCell = findNearestArea((int) touchXY[0], (int) touchXY[1], spanX, spanY,
                         cellLayout, mTargetCell);
                 d.postAnimationRunnable = exitSpringLoadedRunnable;
                 if (createUserFolderIfNecessary(view, container, cellLayout, mTargetCell, true,
                         d.dragView, d.postAnimationRunnable)) {
                     return;
                 }
                 if (addToExistingFolderIfNecessary(view, cellLayout, mTargetCell, d, true)) {
                     return;
                 }
             }
 
             if (touchXY != null) {
                 // when dragging and dropping, just find the closest free spot
                 mTargetCell = findNearestVacantArea(touchXY[0], touchXY[1], 1, 1, null,
                         cellLayout, mTargetCell);
             } else {
                 cellLayout.findCellForSpan(mTargetCell, 1, 1);
             }
             addInScreen(view, container, screen, mTargetCell[0], mTargetCell[1], info.spanX,
                     info.spanY, insertAtFirst);
             cellLayout.onDropChild(view);
             cellLayout.animateDrop();
             CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
             cellLayout.getChildrenLayout().measureChild(view);
 
             LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container, screen,
                     lp.cellX, lp.cellY);
 
             if (d.dragView != null) {
                 mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, view,
                     exitSpringLoadedRunnable);
             }
         }
     }
 
     /**
      * Return the current {@link CellLayout}, correctly picking the destination
      * screen while a scroll is in progress.
      */
     public CellLayout getCurrentDropLayout() {
         return (CellLayout) getChildAt(mNextPage == INVALID_PAGE ? mCurrentPage : mNextPage);
     }
 
     /**
      * Return the current CellInfo describing our current drag; this method exists
      * so that Launcher can sync this object with the correct info when the activity is created/
      * destroyed
      *
      */
     public CellLayout.CellInfo getDragInfo() {
         return mDragInfo;
     }
 
     /**
      * Calculate the nearest cell where the given object would be dropped.
      *
      * pixelX and pixelY should be in the coordinate system of layout
      */
     private int[] findNearestVacantArea(int pixelX, int pixelY,
             int spanX, int spanY, View ignoreView, CellLayout layout, int[] recycle) {
         return layout.findNearestVacantArea(
                 pixelX, pixelY, spanX, spanY, ignoreView, recycle);
     }
 
     /**
      * Calculate the nearest cell where the given object would be dropped.
      *
      * pixelX and pixelY should be in the coordinate system of layout
      */
     private int[] findNearestArea(int pixelX, int pixelY,
             int spanX, int spanY, CellLayout layout, int[] recycle) {
         return layout.findNearestArea(
                 pixelX, pixelY, spanX, spanY, recycle);
     }
 
     void setup(Launcher launcher, DragController dragController) {
         mLauncher = launcher;
         mSpringLoadedDragController = new SpringLoadedDragController(mLauncher);
         mDragController = dragController;
 
 
         // hardware layers on children are enabled on startup, but should be disabled until
         // needed
         updateChildrenLayersEnabled();
         setWallpaperDimension();
     }
 
     /**
      * Called at the end of a drag which originated on the workspace.
      */
     public void onDropCompleted(View target, DragObject d, boolean success) {
         if (success) {
             if (target != this) {
                 if (mDragInfo != null) {
                     getParentCellLayoutForView(mDragInfo.cell).removeView(mDragInfo.cell);
                     if (mDragInfo.cell instanceof DropTarget) {
                         mDragController.removeDropTarget((DropTarget) mDragInfo.cell);
                     }
                 }
             }
         } else if (mDragInfo != null) {
             // NOTE: When 'success' is true, onDragExit is called by the DragController before
             // calling onDropCompleted(). We call it ourselves here, but maybe this should be
             // moved into DragController.cancelDrag().
             doDragExit(null);
             CellLayout cellLayout;
             if (mLauncher.isHotseatLayout(target)) {
                 cellLayout = mLauncher.getHotseat().getLayout();
             } else {
                 cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
             }
             cellLayout.onDropChild(mDragInfo.cell);
         }
         mDragOutline = null;
         mDragInfo = null;
     }
 
     @Override
     public void onDragViewVisible() {
         ((View) mDragInfo.cell).setVisibility(View.GONE);
     }
 
     public boolean isDropEnabled() {
         return true;
     }
 
     @Override
     protected void onRestoreInstanceState(Parcelable state) {
         super.onRestoreInstanceState(state);
         Launcher.setScreen(mCurrentPage);
     }
 
     @Override
     public void scrollLeft() {
         if (!isSmall() && !mIsSwitchingState) {
             super.scrollLeft();
         }
         Folder openFolder = getOpenFolder();
         if (openFolder != null) {
             openFolder.completeDragExit();
         }
     }
 
     @Override
     public void scrollRight() {
         if (!isSmall() && !mIsSwitchingState) {
             super.scrollRight();
         }
         Folder openFolder = getOpenFolder();
         if (openFolder != null) {
             openFolder.completeDragExit();
         }
     }
 
     @Override
     public void onEnterScrollArea(int direction) {
         if (!isSmall() && !mIsSwitchingState) {
             mInScrollArea = true;
 
             final int page = mCurrentPage + (direction == DragController.SCROLL_LEFT ? -1 : 1);
             final CellLayout layout = (CellLayout) getChildAt(page);
             cancelFolderCreation();
 
             if (layout != null) {
                 // Exit the current layout and mark the overlapping layout
                 if (mDragTargetLayout != null) {
                     mDragTargetLayout.setIsDragOverlapping(false);
                     mDragTargetLayout.onDragExit();
                 }
                 mDragTargetLayout = layout;
                 mDragTargetLayout.setIsDragOverlapping(true);
 
                 // Workspace is responsible for drawing the edge glow on adjacent pages,
                 // so we need to redraw the workspace when this may have changed.
                 invalidate();
             }
         }
     }
 
     @Override
     public void onExitScrollArea() {
         if (mInScrollArea) {
             if (mDragTargetLayout != null) {
                 // Unmark the overlapping layout and re-enter the current layout
                 mDragTargetLayout.setIsDragOverlapping(false);
                 mDragTargetLayout = getCurrentDropLayout();
                 mDragTargetLayout.onDragEnter();
 
                 // Workspace is responsible for drawing the edge glow on adjacent pages,
                 // so we need to redraw the workspace when this may have changed.
                 invalidate();
             }
             mInScrollArea = false;
         }
     }
 
     private void onResetScrollArea() {
         if (mDragTargetLayout != null) {
             // Unmark the overlapping layout
             mDragTargetLayout.setIsDragOverlapping(false);
 
             // Workspace is responsible for drawing the edge glow on adjacent pages,
             // so we need to redraw the workspace when this may have changed.
             invalidate();
         }
         mInScrollArea = false;
     }
 
     /**
      * Returns a specific CellLayout
      */
     CellLayout getParentCellLayoutForView(View v) {
         ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
         for (CellLayout layout : layouts) {
             if (layout.getChildrenLayout().indexOfChild(v) > -1) {
                 return layout;
             }
         }
         return null;
     }
 
     /**
      * Returns a list of all the CellLayouts in the workspace.
      */
     ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
         ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
         int screenCount = getChildCount();
         for (int screen = 0; screen < screenCount; screen++) {
             layouts.add(((CellLayout) getChildAt(screen)));
         }
         if (mLauncher.getHotseat() != null) {
             layouts.add(mLauncher.getHotseat().getLayout());
         }
         return layouts;
     }
 
     /**
      * We should only use this to search for specific children.  Do not use this method to modify
      * CellLayoutChildren directly.
      */
     ArrayList<CellLayoutChildren> getWorkspaceAndHotseatCellLayoutChildren() {
         ArrayList<CellLayoutChildren> childrenLayouts = new ArrayList<CellLayoutChildren>();
         int screenCount = getChildCount();
         for (int screen = 0; screen < screenCount; screen++) {
             childrenLayouts.add(((CellLayout) getChildAt(screen)).getChildrenLayout());
         }
         if (mLauncher.getHotseat() != null) {
             childrenLayouts.add(mLauncher.getHotseat().getLayout().getChildrenLayout());
         }
         return childrenLayouts;
     }
 
     public Folder getFolderForTag(Object tag) {
         ArrayList<CellLayoutChildren> childrenLayouts = getWorkspaceAndHotseatCellLayoutChildren();
         for (CellLayoutChildren layout: childrenLayouts) {
             int count = layout.getChildCount();
             for (int i = 0; i < count; i++) {
                 View child = layout.getChildAt(i);
                 CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
                 if (child instanceof Folder) {
                     Folder f = (Folder) child;
                     if (f.getInfo() == tag && f.getInfo().opened) {
                         return f;
                     }
                 }
             }
         }
         return null;
     }
 
     public View getViewForTag(Object tag) {
         ArrayList<CellLayoutChildren> childrenLayouts = getWorkspaceAndHotseatCellLayoutChildren();
         for (CellLayoutChildren layout: childrenLayouts) {
             int count = layout.getChildCount();
             for (int i = 0; i < count; i++) {
                 View child = layout.getChildAt(i);
                 if (child.getTag() == tag) {
                     return child;
                 }
             }
         }
         return null;
     }
 
     void clearDropTargets() {
         ArrayList<CellLayoutChildren> childrenLayouts = getWorkspaceAndHotseatCellLayoutChildren();
         for (CellLayoutChildren layout: childrenLayouts) {
             int childCount = layout.getChildCount();
             for (int j = 0; j < childCount; j++) {
                 View v = layout.getChildAt(j);
                 if (v instanceof DropTarget) {
                     mDragController.removeDropTarget((DropTarget) v);
                 }
             }
         }
     }
 
     void removeItems(final ArrayList<ApplicationInfo> apps) {
         final int screenCount = getChildCount();
         final AppWidgetManager widgets = AppWidgetManager.getInstance(getContext());
 
         final HashSet<String> packageNames = new HashSet<String>();
         final int appCount = apps.size();
         for (int i = 0; i < appCount; i++) {
             packageNames.add(apps.get(i).componentName.getPackageName());
         }
 
         ArrayList<CellLayout> cellLayouts = getWorkspaceAndHotseatCellLayouts();
         for (final CellLayout layoutParent: cellLayouts) {
             final ViewGroup layout = layoutParent.getChildrenLayout();
 
             // Avoid ANRs by treating each screen separately
             post(new Runnable() {
                 public void run() {
                     final ArrayList<View> childrenToRemove = new ArrayList<View>();
                     childrenToRemove.clear();
 
                     int childCount = layout.getChildCount();
                     for (int j = 0; j < childCount; j++) {
                         final View view = layout.getChildAt(j);
                         Object tag = view.getTag();
 
                         if (tag instanceof ShortcutInfo) {
                             final ShortcutInfo info = (ShortcutInfo) tag;
                             final Intent intent = info.intent;
                             final ComponentName name = intent.getComponent();
 
                             if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                 for (String packageName: packageNames) {
                                     if (packageName.equals(name.getPackageName())) {
                                         LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                         childrenToRemove.add(view);
                                     }
                                 }
                             }
                         } else if (tag instanceof FolderInfo) {
                             final FolderInfo info = (FolderInfo) tag;
                             final ArrayList<ShortcutInfo> contents = info.contents;
                             final int contentsCount = contents.size();
                             final ArrayList<ShortcutInfo> appsToRemoveFromFolder =
                                     new ArrayList<ShortcutInfo>();
 
                             for (int k = 0; k < contentsCount; k++) {
                                 final ShortcutInfo appInfo = contents.get(k);
                                 final Intent intent = appInfo.intent;
                                 final ComponentName name = intent.getComponent();
 
                                 if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                                     for (String packageName: packageNames) {
                                         if (packageName.equals(name.getPackageName())) {
                                             appsToRemoveFromFolder.add(appInfo);
                                         }
                                     }
                                 }
                             }
                             for (ShortcutInfo item: appsToRemoveFromFolder) {
                                 info.remove(item);
                                 LauncherModel.deleteItemFromDatabase(mLauncher, item);
                             }
                         } else if (tag instanceof LauncherAppWidgetInfo) {
                             final LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) tag;
                             final AppWidgetProviderInfo provider =
                                     widgets.getAppWidgetInfo(info.appWidgetId);
                             if (provider != null) {
                                 for (String packageName: packageNames) {
                                     if (packageName.equals(provider.provider.getPackageName())) {
                                         LauncherModel.deleteItemFromDatabase(mLauncher, info);
                                         childrenToRemove.add(view);
                                     }
                                 }
                             }
                         }
                     }
 
                     childCount = childrenToRemove.size();
                     for (int j = 0; j < childCount; j++) {
                         View child = childrenToRemove.get(j);
                         // Note: We can not remove the view directly from CellLayoutChildren as this
                         // does not re-mark the spaces as unoccupied.
                         layoutParent.removeViewInLayout(child);
                         if (child instanceof DropTarget) {
                             mDragController.removeDropTarget((DropTarget)child);
                         }
                     }
 
                     if (childCount > 0) {
                         layout.requestLayout();
                         layout.invalidate();
                     }
                 }
             });
         }
     }
 
     void updateShortcuts(ArrayList<ApplicationInfo> apps) {
         ArrayList<CellLayoutChildren> childrenLayouts = getWorkspaceAndHotseatCellLayoutChildren();
         for (CellLayoutChildren layout: childrenLayouts) {
             int childCount = layout.getChildCount();
             for (int j = 0; j < childCount; j++) {
                 final View view = layout.getChildAt(j);
                 Object tag = view.getTag();
                 if (tag instanceof ShortcutInfo) {
                     ShortcutInfo info = (ShortcutInfo)tag;
                     // We need to check for ACTION_MAIN otherwise getComponent() might
                     // return null for some shortcuts (for instance, for shortcuts to
                     // web pages.)
                     final Intent intent = info.intent;
                     final ComponentName name = intent.getComponent();
                     if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                             Intent.ACTION_MAIN.equals(intent.getAction()) && name != null) {
                         final int appCount = apps.size();
                         for (int k = 0; k < appCount; k++) {
                             ApplicationInfo app = apps.get(k);
                             if (app.componentName.equals(name)) {
                                 info.setIcon(mIconCache.getIcon(info.intent));
                                 ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(null,
                                         new FastBitmapDrawable(info.getIcon(mIconCache)),
                                         null, null);
                                 }
                         }
                     }
                 }
             }
         }
     }
 
     void moveToDefaultScreen(boolean animate) {
         if (isSmall() || mIsSwitchingState) {
             mLauncher.showWorkspace(animate, (CellLayout) getChildAt(mDefaultPage));
         } else if (animate) {
             snapToPage(mDefaultPage);
         } else {
             setCurrentPage(mDefaultPage);
         }
         getChildAt(mDefaultPage).requestFocus();
     }
 
     @Override
     public void syncPages() {
     }
 
     @Override
     public void syncPageItems(int page) {
     }
 
     @Override
     protected String getCurrentPageDescription() {
         int page = (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
         return String.format(mContext.getString(R.string.workspace_scroll_format),
                 page + 1, getChildCount());
     }
 
     public void getLocationInDragLayer(int[] loc) {
         mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
     }
 
     /**
      * Return true because we want the scrolling indicator to stretch to fit the space.
      */
     protected boolean hasElasticScrollIndicator() {
         return true;
     }
 }

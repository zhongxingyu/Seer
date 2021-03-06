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
 
 package com.android.camera.ui;
 
 import com.android.camera.ComboPreferences;
 import com.android.camera.IconListPreference;
 import com.android.camera.PreferenceGroup;
 import com.android.camera.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.widget.ImageView;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.ViewGroup;
 import android.view.View;
 
 import java.lang.Math;
 import java.util.ArrayList;
 
 /**
  * A view that contains shutter button and camera setting indicators. The
  * indicators are spreaded around the shutter button. The first child is always
  * the shutter button.
  */
 public class IndicatorWheel extends ViewGroup implements
         BasicSettingPopup.Listener, OtherSettingsPopup.Listener {
     private static final String TAG = "IndicatorWheel";
     // The width of the edges on both sides of the wheel, which has less alpha.
     private static final float EDGE_STROKE_WIDTH = 6f;
     private static final int HIGHLIGHT_WIDTH = 4;
     private static final int HIGHLIGHT_DEGREE = 30;
     private static final int TIME_LAPSE_ARC_WIDTH = 6;
 
     private final int HIGHLIGHT_COLOR;
     private final int DISABLED_COLOR;
     private final int TIME_LAPSE_ARC_COLOR;
 
     private Listener mListener;
     // The center of the shutter button.
     private int mCenterX, mCenterY;
     // The width of the wheel stroke.
     private int mStrokeWidth = 60;
     private View mShutterButton;
     private double mShutterButtonRadius;
     private double mWheelRadius;
     private double mSectorInitialRadians[];
     private Paint mBackgroundPaint;
     private RectF mBackgroundRect;
     // The index of the indicator that is currently selected.
     private int mSelectedIndex = -1;
     // The index of the indicator that has been just de-selected. If users click
     // on the same indicator, we want to dismiss the popup window without
     // opening it again.
     private int mJustDeselectedIndex = -1;
 
     // Time lapse recording variables.
     private int mTimeLapseInterval;  // in ms
     private long mRecordingStartTime = 0;
     private long mNumberOfFrames = 0;
     private float mLastEndAngle;
 
     private Context mContext;
     private PreferenceGroup mPreferenceGroup;
     private ArrayList<String> mPreferenceKeys;
     private BasicSettingPopup[] mBasicSettingPopups;
     private OtherSettingsPopup mOtherSettingsPopup;
 
     private Handler mHandler = new Handler();
     private final Runnable mRunnable = new Runnable() {
         public void run() {
             invalidate();
         }
     };
 
     static public interface Listener {
         public void onSharedPreferenceChanged();
         public void onRestorePreferencesClicked();
         public void onOverriddenPreferencesClicked();
     }
 
     public void setListener(Listener listener) {
         mListener = listener;
     }
 
     public IndicatorWheel(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
         Resources resources = context.getResources();
         HIGHLIGHT_COLOR = resources.getColor(R.color.review_control_pressed_color);
         DISABLED_COLOR = resources.getColor(R.color.icon_disabled_color);
         TIME_LAPSE_ARC_COLOR = resources.getColor(R.color.time_lapse_arc);
         setWillNotDraw(false);
 
         mBackgroundPaint = new Paint();
         mBackgroundPaint.setStyle(Paint.Style.STROKE);
         mBackgroundPaint.setAntiAlias(true);
 
         mBackgroundRect = new RectF();
     }
 
     public boolean onInterceptTouchEvent(MotionEvent ev) {
         // If the event will go to shutter button, dismiss the popup window now.
         // If not, handle it in onTouchEvent.
         if (ev.getAction() == MotionEvent.ACTION_DOWN) {
             float x = ev.getX();
             float y = ev.getY();
             float shutterButtonX = mShutterButton.getX();
             float shutterButtonY = mShutterButton.getY();
             if (x >= shutterButtonX && y >= shutterButtonY
                     && (x < shutterButtonX + mShutterButton.getWidth())
                     && (y < shutterButtonY + mShutterButton.getHeight()))
                 dismissSettingPopup();
         }
         return false;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (!isEnabled()) return false;
 
         int count = getChildCount();
         if (count <= 1) return false;
 
         // Check if any setting is pressed.
         int action = event.getAction();
         if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
             return false;
         }
 
         double dx = event.getX() - mCenterX;
         double dy = mCenterY - event.getY();
         double radius = Math.sqrt(dx * dx + dy * dy);
         // Ignore the event if it's too near to the shutter button or too far
         // from the shutter button.
 
         if (radius >= mShutterButtonRadius && radius <= mWheelRadius + mStrokeWidth) {
             double delta = Math.atan2(dy, dx);
             if (delta < 0) delta += Math.PI * 2;
             // Check which sector is pressed.
             if (delta > mSectorInitialRadians[0]) {
                 for (int i = 1; i < count; i++) {
                     if (delta < mSectorInitialRadians[i]) {
                         // If the touch is moving around the same indicator with
                         // popup opened, return now to avoid redundent works.
                         if (action == MotionEvent.ACTION_MOVE && (mSelectedIndex == i - 1)) {
                             return false;
                         }
 
                         int selectedIndex = mSelectedIndex;
                         dismissSettingPopup();
 
                         // Do nothing if scene mode overrides the setting.
                         View child = getChildAt(i);
                         if (child instanceof IndicatorButton) {
                             if (((IndicatorButton) child).isOverridden()) {
                                 // Do not notify in ACTION_MOVE to avoid lots of
                                 // toast being displayed.
                                 if (action == MotionEvent.ACTION_DOWN && mListener != null) {
                                     mListener.onOverriddenPreferencesClicked();
                                 }
                                 return true;
                             }
                         }
                         if (action == MotionEvent.ACTION_DOWN
                                 && (selectedIndex == i - 1) && (mJustDeselectedIndex != i - 1)) {
                             // The same indicator is pressed with popup opened.
                             mJustDeselectedIndex = i - 1;
                         } else {
                             if ((mJustDeselectedIndex != i - 1)
                                     || (selectedIndex == -1 && action == MotionEvent.ACTION_DOWN)) {
                                 showSettingPopup(i - 1);
                                 mJustDeselectedIndex = -1;
                             }
                         }
                         return true;
                     }
                 }
             }
         }
         dismissSettingPopup();
         mJustDeselectedIndex = -1;
         return false;
     }
 
     @Override
     protected void onFinishInflate() {
         super.onFinishInflate();
         // The first view is shutter button.
         mShutterButton = getChildAt(0);
         mHandler.postDelayed(mRunnable, 0);
     }
 
     public void removeIndicators() {
         // Remove everything but the shutter button.
         int count = getChildCount();
         if (count > 1) {
             removeViews(1, count - 1);
         }
     }
 
     @Override
     protected void onMeasure(int widthSpec, int heightSpec) {
         // Measure all children.
         int childCount = getChildCount();
         int freeSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
         for (int i = 0; i < childCount; i++) {
             getChildAt(i).measure(freeSpec, freeSpec);
         }
 
         // Measure myself.
         int desiredWidth = (int)(mShutterButton.getMeasuredWidth() * 3);
         int desiredHeight = (int)(mShutterButton.getMeasuredHeight() * 4.5) + 2;
         int widthMode = MeasureSpec.getMode(widthSpec);
         int heightMode = MeasureSpec.getMode(heightSpec);
         int measuredWidth, measuredHeight;
         if (widthMode == MeasureSpec.UNSPECIFIED) {
             measuredWidth = desiredWidth;
         } else if (widthMode == MeasureSpec.AT_MOST) {
             measuredWidth = Math.min(desiredWidth, MeasureSpec.getSize(widthSpec));
         } else {  // MeasureSpec.EXACTLY
             measuredWidth = MeasureSpec.getSize(widthSpec);
         }
         if (heightMode == MeasureSpec.UNSPECIFIED) {
             measuredHeight = desiredHeight;
         } else if (heightMode == MeasureSpec.AT_MOST) {
             measuredHeight = Math.min(desiredHeight, MeasureSpec.getSize(heightSpec));
         } else {  // MeasureSpec.EXACTLY
             measuredHeight = MeasureSpec.getSize(heightSpec);
         }
         setMeasuredDimension(measuredWidth, measuredHeight);
     }
 
     @Override
     protected void onLayout(
             boolean changed, int left, int top, int right, int bottom) {
         int count = getChildCount();
         if (count == 0) return;
 
         // Layout the shutter button.
         int shutterButtonWidth = mShutterButton.getMeasuredWidth();
         mShutterButtonRadius = shutterButtonWidth / 2.0;
         int shutterButtonHeight = mShutterButton.getMeasuredHeight();
         mStrokeWidth = (int) (mShutterButtonRadius * 1.05);
         int innerRadius = (int) (mShutterButtonRadius + mStrokeWidth * 0.84);
         // 64 is the requirement by UI design. The distance between the center
         // and the border is 64 pixels. This has to be consistent with the
         // background.
         mCenterX = right - left - 64;
        mCenterY = (bottom - top) / 2 + 3;
         mShutterButton.layout(mCenterX - shutterButtonWidth / 2,
                 mCenterY - shutterButtonHeight / 2,
                 mCenterX + shutterButtonWidth / 2,
                 mCenterY + shutterButtonHeight / 2);
 
         // Layout the settings. The icons are spreaded on the left side of the
         // shutter button. So the angle starts from 90 to 270 degrees.
         if (count == 1) return;
         mWheelRadius = innerRadius + mStrokeWidth * 0.5;
         double intervalDegrees = (count == 2) ? 90.0 : 180.0 / (count - 2);
         double initialDegrees = 90.0;
         int index = 0;
         for (int i = 0; i < count; i++) {
             View view = getChildAt(i);
             if (view == mShutterButton) continue;
             double degree = initialDegrees + intervalDegrees * index;
             double radian = Math.toRadians(degree);
             int x = mCenterX + (int)(mWheelRadius * Math.cos(radian));
             int y = mCenterY - (int)(mWheelRadius * Math.sin(radian));
             int width = view.getMeasuredWidth();
             int height = view.getMeasuredHeight();
             view.layout(x - width / 2, y - height / 2, x + width / 2,
                     y + height / 2);
             index++;
         }
 
         // Store the radian intervals for each icon.
         mSectorInitialRadians = new double[count];
         mSectorInitialRadians[0] = Math.toRadians(
                 initialDegrees - intervalDegrees / 2.0);
         for (int i = 1; i < count; i++) {
             mSectorInitialRadians[i] = mSectorInitialRadians[i - 1]
                     + Math.toRadians(intervalDegrees);
         }
     }
 
     public void startTimeLapseAnimation(int timeLapseInterval, long startTime) {
         mTimeLapseInterval = timeLapseInterval;
         mRecordingStartTime = startTime;
         mNumberOfFrames = 0;
         mLastEndAngle = 0f;
         invalidate();
     }
 
     public void stopTimeLapseAnimation() {
         mTimeLapseInterval = 0;
         invalidate();
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         // Draw highlight.
         float delta = mStrokeWidth * 0.5f;
         float radius = (float) (mWheelRadius + mStrokeWidth * 0.5 + EDGE_STROKE_WIDTH);
         mBackgroundRect.set((float)(mCenterX - radius),
                 (float)(mCenterY - radius),
                 (float)(mCenterX + radius),
                 (float)(mCenterY + radius));
         if (mSelectedIndex >= 0) {
             int count = getChildCount();
             float initialDegrees = 90.0f;
             float intervalDegrees = (count <= 2) ? 0.0f : 180.0f / (count - 2);
             float degree = initialDegrees + intervalDegrees * mSelectedIndex;
             mBackgroundPaint.setStrokeWidth(HIGHLIGHT_WIDTH);
             mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
             mBackgroundPaint.setColor(HIGHLIGHT_COLOR);
             canvas.drawArc(mBackgroundRect, -degree - HIGHLIGHT_DEGREE / 2,
                     HIGHLIGHT_DEGREE, false, mBackgroundPaint);
         }
 
         // Draw arc shaped indicator in time lapse recording.
         if (mTimeLapseInterval != 0) {
             // Setup rectangle and paint.
             mBackgroundRect.set((float)(mCenterX - mShutterButtonRadius),
                     (float)(mCenterY - mShutterButtonRadius),
                     (float)(mCenterX + mShutterButtonRadius),
                     (float)(mCenterY + mShutterButtonRadius));
             mBackgroundRect.inset(3f, 3f);
             mBackgroundPaint.setStrokeWidth(TIME_LAPSE_ARC_WIDTH);
             mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
             mBackgroundPaint.setColor(TIME_LAPSE_ARC_COLOR);
 
             // Compute the start angle and sweep angle.
             long timeDelta = SystemClock.uptimeMillis() - mRecordingStartTime;
             long numberOfFrames = timeDelta / mTimeLapseInterval;
             float sweepAngle, startAngle;
             float endAngle = ((float) timeDelta) % mTimeLapseInterval / mTimeLapseInterval * 360f;
             if (numberOfFrames > mNumberOfFrames) {
                 // The arc just acrosses 0 degree. Draw the arc from the degree
                 // of last onDraw. Otherwise some parts of the arc will be never
                 // drawn.
                 startAngle = mLastEndAngle;
                 sweepAngle = endAngle + 360f - mLastEndAngle;
                 mNumberOfFrames = numberOfFrames;
             } else {
                 startAngle = 0;
                 sweepAngle = endAngle;
             }
             mLastEndAngle = endAngle;
 
             canvas.drawArc(mBackgroundRect, startAngle, sweepAngle, false, mBackgroundPaint);
             mHandler.postDelayed(mRunnable, 0);
         }
 
         super.onDraw(canvas);
     }
 
     // Scene mode may override other camera settings (ex: flash mode).
     public void overrideSettings(String key, String value) {
         int count = getChildCount();
         for (int j = 1; j < count; j++) {
             View v = getChildAt(j);
             if (v instanceof IndicatorButton) {  // skip the button of "other settings"
                 IndicatorButton indicator = (IndicatorButton) v;
                 if (key.equals(indicator.getKey())) {
                     indicator.overrideSettings(value);
                     setEnabled(indicator, (value == null));
                     break;
                 }
             }
         }
     }
 
     // Sets/unsets highlight on the specified setting icon
     private void setHighlight(int index, boolean enabled) {
         if ((index < 0) || (index >= getChildCount() - 1)) return;
         ImageView child = (ImageView) getChildAt(index + 1);
         if (enabled) {
             child.setColorFilter(HIGHLIGHT_COLOR);
         } else {
             child.clearColorFilter();
         }
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         int count = getChildCount();
         for (int i = 1; i < count; i++) {
             setEnabled((ImageView) getChildAt(i), enabled);
         }
     }
 
     private void setEnabled(ImageView view, boolean enabled) {
         // Do not enable the button if it is overridden by scene mode.
         if ((view instanceof IndicatorButton) && ((IndicatorButton) view).isOverridden()) {
             enabled = false;
         }
 
         // Don't do anything if state is not changed so not to interfere with
         // the "highlight" state.
         if (view.isEnabled() ^ enabled) {
             view.setEnabled(enabled);
             if (enabled) {
                 view.clearColorFilter();
             } else {
                 view.setColorFilter(DISABLED_COLOR);
             }
         }
     }
 
     protected boolean addIndicator(
             Context context, PreferenceGroup group, String key) {
         IconListPreference pref = (IconListPreference) group.findPreference(key);
         if (pref == null) return false;
         IndicatorButton b = new IndicatorButton(context, pref);
         addView(b);
         return true;
     }
 
     private void addOtherSettingIndicator(Context context) {
         ImageView b = new ImageView(context);
         b.setImageResource(R.drawable.ic_viewfinder_settings);
         b.setClickable(false);
         addView(b);
     }
 
     public void initialize(Context context, PreferenceGroup group,
             String[] keys, boolean enableOtherSettings) {
         // Reset the variables and states.
         dismissSettingPopup();
         removeIndicators();
         mOtherSettingsPopup = null;
         mSelectedIndex = -1;
         mPreferenceKeys = new ArrayList<String>();
 
         // Initialize all variables and icons.
         mPreferenceGroup = group;
         for (int i = 0; i < keys.length; i++) {
             if (addIndicator(context, group, keys[i])) {
                 mPreferenceKeys.add(keys[i]);
             }
         }
         mBasicSettingPopups = new BasicSettingPopup[mPreferenceKeys.size()];
 
         if (enableOtherSettings) {
             addOtherSettingIndicator(context);
         }
         requestLayout();
     }
 
     public void onOtherSettingChanged() {
         if (mListener != null) {
             mListener.onSharedPreferenceChanged();
         }
     }
 
     public void onRestorePreferencesClicked() {
         if (mListener != null) {
             mListener.onRestorePreferencesClicked();
         }
     }
 
     public void onSettingChanged() {
         // Update indicator.
         IndicatorButton indicator = (IndicatorButton) getChildAt(mSelectedIndex + 1);
         indicator.reloadPreference();
         if (mListener != null) {
             mListener.onSharedPreferenceChanged();
         }
     }
 
     private void initializeSettingPopup(int index) {
         IconListPreference pref = (IconListPreference)
                 mPreferenceGroup.findPreference(mPreferenceKeys.get(index));
 
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         ViewGroup root = (ViewGroup) getRootView().findViewById(R.id.app_root);
         BasicSettingPopup popup = (BasicSettingPopup) inflater.inflate(
                 R.layout.basic_setting_popup, root, false);
         mBasicSettingPopups[index] = popup;
         popup.setSettingChangedListener(this);
         popup.initialize(pref);
         root.addView(popup);
     }
 
     private void initializeOtherSettingPopup() {
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                 Context.LAYOUT_INFLATER_SERVICE);
         ViewGroup root = (ViewGroup) getRootView().findViewById(R.id.app_root);
         mOtherSettingsPopup = (OtherSettingsPopup) inflater.inflate(
                 R.layout.other_setting_popup, root, false);
         mOtherSettingsPopup.setOtherSettingChangedListener(this);
         mOtherSettingsPopup.initialize(mPreferenceGroup);
         root.addView(mOtherSettingsPopup);
     }
 
     private void showSettingPopup(int index) {
         if (index == mSelectedIndex) return;
 
         if (index < mBasicSettingPopups.length) {
             if (mBasicSettingPopups[index] == null) {
                 initializeSettingPopup(index);
             }
         } else if (mOtherSettingsPopup == null) {
             initializeOtherSettingPopup();
         }
 
         if (index == mBasicSettingPopups.length) {
             mOtherSettingsPopup.setVisibility(View.VISIBLE);
         } else {
             mBasicSettingPopups[index].setVisibility(View.VISIBLE);
         }
         setHighlight(index, true);
         mSelectedIndex = index;
         invalidate();
     }
 
     public boolean dismissSettingPopup() {
         if (mSelectedIndex >= 0) {
             if (mSelectedIndex == mBasicSettingPopups.length) {
                 mOtherSettingsPopup.setVisibility(View.INVISIBLE);
             } else {
                 mBasicSettingPopups[mSelectedIndex].setVisibility(View.INVISIBLE);
             }
             setHighlight(mSelectedIndex, false);
             mSelectedIndex = -1;
             invalidate();
             return true;
         }
         return false;
     }
 
     public View getActivePopupWindow() {
         if (mSelectedIndex >= 0) {
             if (mSelectedIndex == mBasicSettingPopups.length) {
                 return mOtherSettingsPopup;
             } else {
                 return mBasicSettingPopups[mSelectedIndex];
             }
         } else {
             return null;
         }
     }
 
     // Scene mode may override other camera settings (ex: flash mode).
     public void overrideSettings(final String ... keyvalues) {
         if (keyvalues.length % 2 != 0) {
             throw new IllegalArgumentException();
         }
 
         if (mOtherSettingsPopup == null) {
             initializeOtherSettingPopup();
         }
 
         for (int i = 0; i < keyvalues.length; i += 2) {
             String key = keyvalues[i];
             String value = keyvalues[i + 1];
             overrideSettings(key, value);
             mOtherSettingsPopup.overrideSettings(key, value);
         }
     }
 }

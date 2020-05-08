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
 
 package com.android.systemui.statusbar.navbar;
 
 import android.animationing.Animator;
 import android.animationing.AnimatorSet;
 import android.animationing.ObjectAnimator;
 import android.database.ContentObserver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.graphics.PorterDuff;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.widget.ImageView;
 
 import java.math.BigInteger;
 import com.android.internal.util.ArrayUtils;
 import com.android.systemui.R;
 
 public class KeyButtonView extends ImageView {
     private static final String TAG = "StatusBar.KeyButtonView";
 
     final float GLOW_MAX_SCALE_FACTOR = 1.8f;
     float BUTTON_QUIESCENT_ALPHA = 1.0f;
 
     int mTouchSlop;
     Drawable mGlowBG;
     int mGlowWidth, mGlowHeight;
     float mGlowAlpha = 0f, mGlowScale = 1f, mDrawingAlpha = 1f, mOldDrawingAlpha = 1f;
     RectF mRect = new RectF(0f,0f,0f,0f);
     AnimatorSet mPressedAnim;
     Context mContext;
     Handler mHandler;
 
     private int mOverColor;
     private int mGlowingColor;
     private boolean mOverColorEnable;
    private boolean mPressed;
 
     Runnable mCheckLongPress = new Runnable() {
         public void run() {
             if (isPressed()) {
                mPressed = false;
                 performLongClick();
             }
         }
     };
 
     class SettingsObserver extends ContentObserver {
         SettingsObserver(Handler handler) {
             super(handler);
         }
 
         void observe() {
             ContentResolver resolver = mContext.getContentResolver();
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.ENABLE_OVERICON_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.OVERICON_COLOR), false, this);
             resolver.registerContentObserver(
                     Settings.System.getUriFor(Settings.System.NAVBAR_GLOWING_COLOR), false, this);
             onChange(true);
         }
 
         @Override
         public void onChange(boolean selfChange) {
             ContentResolver resolver = mContext.getContentResolver();
             int defValuesColor = mContext.getResources().getInteger(com.android.internal.R.color.color_default_cyanmobile);
             mOverColorEnable = (Settings.System.getInt(resolver, Settings.System.ENABLE_OVERICON_COLOR, 1) == 1);
             mOverColor = Settings.System.getInt(resolver, Settings.System.OVERICON_COLOR, defValuesColor);
             mGlowingColor = Settings.System.getInt(resolver, Settings.System.NAVBAR_GLOWING_COLOR, defValuesColor);
             updateButtonColor();
             updateGlowColor();
         }
     }
 
     public KeyButtonView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public KeyButtonView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs);
 
         mContext = context;
         mHandler = new Handler();
 
         TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyButtonView,
                 defStyle, 0);
 
         mGlowBG = a.getDrawable(R.styleable.KeyButtonView_glowBackground);
         if (mGlowBG != null) {
             setDrawingAlpha(BUTTON_QUIESCENT_ALPHA);
             mGlowWidth = mGlowBG.getIntrinsicWidth();
             mGlowHeight = mGlowBG.getIntrinsicHeight();
         }
 
         setFocusable(true);
         setClickable(true);
        setLongClickable(true);
 
         a.recycle();
         mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
 
         clearColorFilter();
         BUTTON_QUIESCENT_ALPHA = 0.70f;
         setDrawingAlpha(BUTTON_QUIESCENT_ALPHA);
 
         SettingsObserver settingsObserver = new SettingsObserver(mHandler);
         settingsObserver.observe();
 
         updateButtonColor();
         updateGlowColor();
     }
 
     private void updateButtonColor() {
          if (!mOverColorEnable) {
              clearColorFilter();
              BUTTON_QUIESCENT_ALPHA = 0.70f;
              setDrawingAlpha(BUTTON_QUIESCENT_ALPHA);
          } else {
              setColorFilter(extractRGB(mOverColor) | 0xFF000000, PorterDuff.Mode.SRC_ATOP);
              BUTTON_QUIESCENT_ALPHA = (float) extractAlpha(mOverColor) / 255f;
              setDrawingAlpha(BUTTON_QUIESCENT_ALPHA);
         }
     }
 
     private void updateGlowColor() {
         if (!mOverColorEnable) {
             mGlowBG.clearColorFilter();
         } else {
             mGlowBG.setColorFilter(mGlowingColor, PorterDuff.Mode.SRC_ATOP);
         }
      }
 
     private int extractAlpha(int color) {
         return (color >> 24) & 0x000000FF;
     }
 
     private int extractRGB(int color) {
         return color & 0x00FFFFFF;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         if (mGlowBG != null) {
             canvas.save();
             final int w = getWidth();
             final int h = getHeight();
             final float aspect = (float)mGlowWidth / mGlowHeight;
             final int drawW = (int)(h*aspect);
             final int drawH = h;
             final int margin = (drawW-w)/2;
             canvas.scale(mGlowScale, mGlowScale, w*0.5f, h*0.5f);
             mGlowBG.setBounds(-margin, 0, drawW-margin, drawH);
             mGlowBG.setAlpha((int)(mDrawingAlpha * mGlowAlpha * 255));
             mGlowBG.draw(canvas);
             canvas.restore();
             mRect.right = w;
             mRect.bottom = h;
         }
         super.onDraw(canvas);
     }
 
     public float getDrawingAlpha() {
         if (mGlowBG == null) return 0;
         return mDrawingAlpha;
     }
 
     public void setDrawingAlpha(float x) {
         if (mGlowBG == null) return;
         // Calling setAlpha(int), which is an ImageView-specific
         // method that's different from setAlpha(float). This sets
         // the alpha on this ImageView's drawable directly
         setAlpha((int) (x * 255));
         mDrawingAlpha = x;
     }
 
     public float getGlowAlpha() {
         if (mGlowBG == null) return 0;
         return mGlowAlpha;
     }
 
     public void setGlowAlpha(float x) {
         if (mGlowBG == null) return;
         mGlowAlpha = x;
         invalidate();
     }
 
     public float getGlowScale() {
         if (mGlowBG == null) return 0;
         return mGlowScale;
     }
 
     public void setGlowScale(float x) {
         if (mGlowBG == null) return;
         mGlowScale = x;
         final float w = getWidth();
         final float h = getHeight();
         if (GLOW_MAX_SCALE_FACTOR <= 1.0f) {
             // this only works if we know the glow will never leave our bounds
             invalidate();
         } else {
             final float rx = (w * (GLOW_MAX_SCALE_FACTOR - 1.0f)) / 2.0f + 1.0f;
             final float ry = (h * (GLOW_MAX_SCALE_FACTOR - 1.0f)) / 2.0f + 1.0f;
             invalidateGlobalRegion(
                     this,
                     new RectF(getLeft() - x,
                               getTop() - x,
                               getRight() + x,
                               getBottom() + x));
             ((View)getParent()).invalidate();
         }
     }
 
     private void invalidateGlobalRegion(View view, RectF childBounds) {
         childBounds.offset(view.getLeft(), view.getTop());
         while (view.getParent() != null && view.getParent() instanceof View) {
             view = (View) view.getParent();
             view.invalidate((int) Math.floor(childBounds.left),
                             (int) Math.floor(childBounds.top),
                             (int) Math.ceil(childBounds.right),
                             (int) Math.ceil(childBounds.bottom));
         }
     }
 
     public void setPressed(boolean pressed) {
         if (mGlowBG != null) {
             if (pressed != isPressed()) {
                 if (mPressedAnim != null && mPressedAnim.isRunning()) {
                     mPressedAnim.cancel();
                 }
                 final AnimatorSet as = mPressedAnim = new AnimatorSet();
                 if (pressed) {
                     if (mGlowScale < GLOW_MAX_SCALE_FACTOR) 
                         mGlowScale = GLOW_MAX_SCALE_FACTOR;
                     if (mGlowAlpha < BUTTON_QUIESCENT_ALPHA)
                         mGlowAlpha = BUTTON_QUIESCENT_ALPHA;
                     setDrawingAlpha(1f);
                     as.playTogether(
                         ObjectAnimator.ofFloat(this, "glowAlpha", 1f),
                         ObjectAnimator.ofFloat(this, "glowScale", GLOW_MAX_SCALE_FACTOR)
                     );
                     as.setDuration(50);
                 } else {
                     mOldDrawingAlpha = BUTTON_QUIESCENT_ALPHA;
                     as.playTogether(
                         ObjectAnimator.ofFloat(this, "glowAlpha", 0f),
                         ObjectAnimator.ofFloat(this, "glowScale", 1f),
                         ObjectAnimator.ofFloat(this, "drawingAlpha", BUTTON_QUIESCENT_ALPHA)
                     );
                     as.addListener( new Animator.AnimatorListener() {
                         @Override
                         public void onAnimationStart(Animator animation) { }
                         @Override
                         public void onAnimationCancel(Animator animation) { }
                         @Override
                         public void onAnimationRepeat(Animator animation) { }
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             setDrawingAlpha(BUTTON_QUIESCENT_ALPHA);
                         }});
                     as.setDuration(500);
                 }
                 as.start();
             }
         }
         super.setPressed(pressed);
     }
 
     public boolean onTouchEvent(MotionEvent ev) {
         final int action = ev.getAction();
         int x, y;
 
         switch (action) {
             case MotionEvent.ACTION_DOWN:
                 setPressed(true);
                mPressed = true;
                 removeCallbacks(mCheckLongPress);
                 postDelayed(mCheckLongPress, ViewConfiguration.getLongPressTimeout());
                 break;
             case MotionEvent.ACTION_MOVE:
                 x = (int)ev.getX();
                 y = (int)ev.getY();
                 setPressed(x >= -mTouchSlop
                          && x < getWidth() + mTouchSlop
                          && y >= -mTouchSlop
                          && y < getHeight() + mTouchSlop);
                 break;
             case MotionEvent.ACTION_CANCEL:
                 setPressed(false);
                 removeCallbacks(mCheckLongPress);
                 break;
             case MotionEvent.ACTION_UP:
                 setPressed(false);
                 removeCallbacks(mCheckLongPress);
                if (mPressed) {
                    performClick();
                    mPressed = false;
                }
                 break;
         }
 
         return true;
     }
 }
 
 

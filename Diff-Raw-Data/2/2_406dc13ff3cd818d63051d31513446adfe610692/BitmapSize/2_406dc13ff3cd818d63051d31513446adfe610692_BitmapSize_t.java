 package com.froop.app.kegs;
 
 import android.graphics.Rect;
 import android.util.Log;
 
 class BitmapSize {
   static class Const {
     public static final int A2Width = 640 + 32 + 32;   // kegs defcomm.h
     public static final int A2Height = 400 + 32 + 30;  // kegs defcomm.h
   }
 
   private int mWidth = 0;
   private int mHeight = 0;
 
   private boolean mScaled = false;
   private boolean mCropped = false;
   private float mScaleFactorX = 1.0f;
   private float mScaleFactorY = 1.0f;
 
   public BitmapSize(int width, int height) {
     mWidth = width;
     mHeight = height;
     calculateScale(width, height);
   }
 
   public boolean showActionBar() {
     if (mHeight < ((400 + 64) * mScaleFactorY)) {
       return false;
     } else {
       return true;
     }
   }
 
   public int getViewWidth() {
     return (int)(Const.A2Width * mScaleFactorX);
   }
 
   public int getViewHeight() {
     if (!doCropBorder()) {
       return (int)(Const.A2Height * mScaleFactorY);
     } else {
       return (int)((Const.A2Height - 32) * mScaleFactorY);
     }
   }
 
   private boolean doCropBorder() {
     return mCropped;
   }
 
   public boolean isScaled() {
     return (mScaleFactorX != 1.0f || mScaleFactorY != 1.0f);
   }
 
   public float getScaleX() {
     return mScaleFactorX;
   }
 
   public float getScaleY() {
     return mScaleFactorY;
   }
 
   public Rect getRectSrc() {
     if (doCropBorder()) {
       return new Rect(0, 32, Const.A2Width, Const.A2Height);
     } else {
       return new Rect(0, 0, Const.A2Width, Const.A2Height);
     }
   }
 
   public Rect getRectDst() {
     if (doCropBorder()) {
       return new Rect(0, 0, Const.A2Width, Const.A2Height - 32);
     } else {
       return new Rect(0, 0, Const.A2Width, Const.A2Height);
     }
   }
 
 // If we can fit at least 90% of a scaled screen into the display area, do it.
 // If we hit less than 100% height, turn off system action bar and title.
 // If ((400 + 32) * scale) > height, then crop border.
   private void calculateScale(int width, int height) {
     float scaleX = 1.0f;
     float scaleY = 1.0f;
     boolean crop = false;
 
     // Force integer scaling on X axis.
     scaleX = (float)Math.round((width * 0.9) / 640);
     // TODO: Fix '48' hack being used for system buttons or soft buttons.
     scaleY = Math.min(scaleX, (height - 48) / 400.0f);
 
     // If Y would be compressed in a weird way, reduce the scale and use 1:1.
     if ((scaleX - scaleY) > 0.5) {
       scaleX = Math.max(1, scaleX - 1);
       scaleY = scaleX;
     }
 
     // TODO: Fix '32' and '64' for software buttons and window decorations.
     if (height < ((400 + 32 + 64) * scaleY)) {
       crop = true;
     }
 
     mCropped = crop;
     mScaleFactorX = scaleX;
     mScaleFactorY = scaleY;
    Log.w("kegs", "using scale " + scaleX + ":" + scaleY + " " + crop + " from screen " + width + "x" + height);
   }
 
 // call us when you update your screen size/configuration
 
 // helper to calculate view area for KegsView:onMeasure
 
 // helper to create size struct
 
 // helper struct for scale factors (&isScaled), crop info, source/dest rects
 //    KegsView can get this and pass it into the thread.
 }

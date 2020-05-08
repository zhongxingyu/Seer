 package com.xengine.android.media.graphics;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.util.DisplayMetrics;
 import android.view.WindowManager;
 
 /**
  * Created by 赵之韵.
  * Date: 12-2-29
  * Time: 上午9:00
  */
 public class XAndroidScreen implements XScreen{
 
 
     /**
      * 小屏幕宽高，用于把图片转换为小图标
      */
     public static int SMALL_SCREEN_WIDTH = 200;// 单位：pixel
     public static int SMALL_SCREEN_HEIGHT = 200;// 单位：pixel
 
     /**
      * normal大小的屏幕宽度（dp）
      */
     public final int NORMAL_WIDTH = 320;
 
     /**
      * normal大小的屏幕高度（dp）
      */
     public final int NORMAL_HEIGHT = 480;
 
     /**
      * 屏幕的宽度（px）
      */
     private int screenWidth = 0;
 
     /**
      * 屏幕的高度（px）
      */
     private int screenHeight = 0;
 
     /**
      * 屏幕的宽度（dp）
      */
     private int screenWidthDp = 0;
 
     /**
      * 屏幕的高度（dp）
      */
     private int screenHeightDp = 0;
 
     /**
      * 屏幕的大小，small normal large xlarge
      */
     private int screenSize;
 
     /**
      * 是否是长屏
      */
     private boolean isLong;
 
     /**
      * DisplayMetrics
      */
     private static DisplayMetrics displayMetrics;
 
     public XAndroidScreen(Context context) {
         // 获取屏幕的尺寸
         WindowManager wm    = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
         displayMetrics      = new DisplayMetrics();
         wm.getDefaultDisplay().getMetrics(displayMetrics);
         screenWidth         = wm.getDefaultDisplay().getWidth();
         screenHeight        = wm.getDefaultDisplay().getHeight();
         screenWidthDp       = (int) px2dp(screenWidth);
         screenHeightDp      = (int) px2dp(screenHeight);
         Configuration config = context.getResources().getConfiguration();
         screenSize = config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
         isLong = (config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) == Configuration.SCREENLAYOUT_LONG_YES;
     }
 
     @Override
     public int getScreenWidth() {
         return screenWidth;
     }
 
     @Override
     public int getScreenHeight() {
         return screenHeight;
     }
 
     @Override
     public int getScreenWidthDp() {
         return screenWidthDp;
     }
 
     @Override
     public int getScreenHeightDp() {
         return screenHeightDp;
     }
 
     @Override
     public int dp2px(float dp) {
        return (int) (dp*displayMetrics.density);
     }
 
     @Override
     public float px2dp(float px) {
        return px/displayMetrics.density;
     }
 
     @Override
     public boolean isScreenLongerThanNormal() {
         return isLong;
     }
 }

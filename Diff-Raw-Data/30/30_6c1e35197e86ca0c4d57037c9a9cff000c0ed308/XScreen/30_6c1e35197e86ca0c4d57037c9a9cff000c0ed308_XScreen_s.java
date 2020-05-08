 package com.xengine.android.media.graphics;
 
 /**
  * Created by 赵之韵.
  * Date: 12-2-29
  * Time: 上午1:01
  */
 public interface XScreen {
     /**
      * 获取屏幕的宽度（单位：像素）
      */
     int getScreenWidth();
 
     /**
      * 获取屏幕的高度（单位：像素）
      */
     int getScreenHeight();
 
     /**
      * 获取屏幕的宽度（单位：dp）
      */
     int getScreenWidthDp();
 
     /**
      * 获取屏幕的高度（单位：dp）
      */
     int getScreenHeightDp();
 
     /**
      * 将dp值转换成px像素值
      */
     int dp2px(float dp);
 
     /**
      * 将px值转换成dp值
      */
     float px2dp(float px);
 
     /**
      * 显示屏幕是否比正常定义的长度要长，比如正常情况下屏幕应该是320X480dp的，但是小米手机就是320X569dp的。
      * @return 如果比正常大小要长就返回true
      */
     boolean isScreenLongerThanNormal();
 }

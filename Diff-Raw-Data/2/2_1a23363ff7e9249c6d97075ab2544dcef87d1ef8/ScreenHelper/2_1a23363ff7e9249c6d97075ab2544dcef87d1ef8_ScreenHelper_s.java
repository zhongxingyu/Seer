 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.screen;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.WindowManager;
 
 public class ScreenHelper
 {
     private static int[] wh;
 
     public static boolean isLarge(Context context)
     {
         return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE;
     }
 
     public static boolean isNormal(Context context)
     {
         return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
     }
 
     public static boolean isSmall(Context context)
     {
         return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL;
     }
 
     public static boolean isPortrait(Context context)
     {
         return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
     }
 
     public static boolean isLandscape(Context context)
     {
         return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
     }
 
     public static boolean isSqare(Context context)
     {
         return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_SQUARE;
     }
 
     public static int getWidth(Context context)
     {
         return getWidthHeight(context)[0];
     }
 
     public static int getHeight(Context context)
     {
         return getWidthHeight(context)[1];
     }
 
     /**
      * Get screen height, considering current orientation. If screen in landscape his current height equals width and current
      * width equals height
      * 
      * 
      * @param context
      * @return screen height
      */
     public static int getCurrentHeight(Context context)
     {
         if(isPortrait(context))
         {
             return getWidthHeight(context)[1];
         }
         else
         {
             return getWidthHeight(context)[0];
         }
     }
 
     /**
      * Get screen width, considering current orientation. If screen in landscape his current height equals width and current
      * width equals height
      * 
      * 
      * @param context
      * @return screen width
      */
     public static int getCurrentWidth(Context context)
     {
        if(isLandscape(context))
         {
             return getWidthHeight(context)[0];
         }
         else
         {
             return getWidthHeight(context)[1];
         }
     }
 
     public static int[] getWidthHeight(Context context)
     {
         if(wh == null)
         {
             wh = new int[2];
             WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
             Display d = wm.getDefaultDisplay();
             if(d.getWidth() < d.getHeight())
             {
                 wh[0] = d.getWidth();
                 wh[1] = d.getHeight();
             }
             else
             {
                 wh[1] = d.getWidth();
                 wh[0] = d.getHeight();
             }
         }
 
         return wh;
     }
 
     /**
      * Helper method to get DisplayMetrics.
      * 
      * @param context
      * @return DisplayMetrics
      */
     public static DisplayMetrics getDisplayMetrics(Context context)
     {
         Display d = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         DisplayMetrics res = new DisplayMetrics();
         d.getMetrics(res);
         return res;
     }
 
     public static boolean isLowDensity(Context context)
     {
         return getDisplayMetrics(context).densityDpi == DisplayMetrics.DENSITY_LOW;
     }
 
     public static boolean isMediumDensity(Context context)
     {
         return getDisplayMetrics(context).densityDpi == DisplayMetrics.DENSITY_MEDIUM;
     }
 
     public static boolean isHighDensity(Context context)
     {
         return getDisplayMetrics(context).densityDpi == DisplayMetrics.DENSITY_HIGH;
     }
 }

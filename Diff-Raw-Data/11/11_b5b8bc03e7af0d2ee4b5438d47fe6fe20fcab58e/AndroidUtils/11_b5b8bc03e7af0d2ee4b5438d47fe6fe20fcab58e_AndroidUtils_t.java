 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 /* SetFromMap is:
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
 
 package com.janrain.android.utils;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.util.Log;
 import android.webkit.ConsoleMessage;
 import com.janrain.android.engage.JREngage;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 /**
  * @internal
  *
  * @class AndroidUtils
  **/
 public class AndroidUtils {
     public static final String TAG = AndroidUtils.class.getSimpleName();
     private AndroidUtils() {}
 
     public static boolean isSmallNormalOrLargeScreen() {
         int screenConfig = getScreenSize();
 
         // Galaxy Tab 7" (the first one) reports SCREENLAYOUT_SIZE_NORMAL
         // Motorola Xoom reports SCREENLAYOUT_SIZE_XLARGE
         // Nexus S reports SCREENLAYOUT_SIZE_NORMAL
 
         return screenConfig == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                 screenConfig == Configuration.SCREENLAYOUT_SIZE_SMALL ||
                 screenConfig == Configuration.SCREENLAYOUT_SIZE_LARGE;
     }
 
     public static boolean isCupcake() {
         return Build.VERSION.RELEASE.startsWith("1.5");
     }
 
     private static int getAndroidSdkInt() {
         try {
             return Build.VERSION.class.getField("SDK_INT").getInt(null);
         } catch (NoSuchFieldException e) {
             // Must be Cupcake
             return 3;
         } catch (IllegalAccessException e) {
             // Not expected
             throw new RuntimeException(e);
         }
     }
 
     public static String urlEncode(String s) {
         try {
             return URLEncoder.encode(s, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static String urlDecode(String s) {
         try {
             return URLDecoder.decode(s, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static final int SDK_INT = getAndroidSdkInt();
 
     public static ApplicationInfo getApplicationInfo() {
         String packageName = JREngage.getApplicationContext().getPackageName();
         try {
             return JREngage.getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
         } catch (PackageManager.NameNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static int scaleDipToPixels(int dip) {
         Context c = JREngage.getApplicationContext();
         final float scale = c.getResources().getDisplayMetrics().density;
         return (int) (((float) dip) * scale);
     }
 
     private static int getScreenSize() {
         int screenConfig = JREngage.getApplicationContext().getResources().getConfiguration().screenLayout;
         screenConfig &= Configuration.SCREENLAYOUT_SIZE_MASK;
         return screenConfig;
     }
 
     public static boolean isXlarge() {
         return (getScreenSize() == Configuration.SCREENLAYOUT_SIZE_XLARGE);
     }
 
     public static boolean isLandscape() {
         return JREngage.getApplicationContext().getResources().getConfiguration().orientation ==
                 Configuration.ORIENTATION_LANDSCAPE;
     }
     
     public static void activitySetFinishOnTouchOutside(Activity activity, boolean finish) {
         try {
            Method m = activity.getClass().getMethod("setFinishOnTouchOutside", boolean.class);
             m.invoke(activity, finish);
         } catch (NoSuchMethodException e) {
            Log.e(TAG, "[setFinishOnTouchOutside]", e);
         } catch (InvocationTargetException e) {
            Log.e(TAG, "[setFinishOnTouchOutside]", e);
         } catch (IllegalAccessException e) {
            Log.e(TAG, "[setFinishOnTouchOutside]", e);
         }
     }
 
     public static Object activityGetActionBar(Activity a) {
         try {
             Method getActionBar = a.getClass().getMethod("getActionBar");
             return getActionBar.invoke(a);
         } catch (NoSuchMethodException ignore) {
         } catch (InvocationTargetException ignore) {
         } catch (IllegalAccessException ignore) {
         }
 
         return null;
     }
 
     public static void actionBarSetDisplayHomeAsUpEnabled(Object actionBar, boolean arg) {
         if (actionBar == null) return;
 
         try {
             Method m = actionBar.getClass().getMethod("setDisplayHomeAsUpEnabled", boolean.class);
             m.invoke(actionBar, arg);
         } catch (NoSuchMethodException ignore) {
         } catch (InvocationTargetException ignore) {
         } catch (IllegalAccessException ignore) {
         }
     }
 
     public static Drawable newBitmapDrawable(Context c, Bitmap icon) {
         try {
             Class bitmapDrawableClass = Class.forName("android.graphics.drawable.BitmapDrawable");
             Constructor bitmapDrawableConstructor =
                     bitmapDrawableClass.getDeclaredConstructor(Resources.class, Bitmap.class);
             return (BitmapDrawable) bitmapDrawableConstructor.newInstance(c.getResources(), icon);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static void bitmapSetDensity(Bitmap icon, int density) {
         try {
             Method setDensity = icon.getClass().getDeclaredMethod("setDensity", int.class);
             setDensity.invoke(icon, density);
         } catch (NoSuchMethodException e) {
             Log.e(TAG, "Unexpected: " + e);
         } catch (IllegalAccessException e) {
             Log.e(TAG, "Unexpected: " + e);
         } catch (InvocationTargetException e) {
             Log.e(TAG, "Unexpected: " + e);
         }
     }
 
     public static String consoleMessageGetMessage(ConsoleMessage consoleMessage) {
         try {
             Method message = consoleMessage.getClass().getMethod("message");
             return (String) message.invoke(consoleMessage);
             // + consoleMessage.sourceId() + consoleMessage.lineNumber();
         } catch (NoSuchMethodException ignore) {
         } catch (InvocationTargetException ignore) {
         } catch (IllegalAccessException ignore) {
         }
 
         Log.e(TAG, "[consoleMessageGetMessage] unexpected reflection exception");
         return null;
     }
 
     public static String readAsset(Context c, String fileName) {
         try {
             InputStream is = c.getAssets().open(fileName);
             byte[] buffer = new byte[is.available()];
             //noinspection ResultOfMethodCallIgnored
             is.read(buffer); // buffer is exactly the right size, a guarantee of asset files
             return new String(buffer);
         } catch (IOException ignore) {
         }
         return null;
     }
 
     //public static int getScreenWidth() {
     //    DisplayMetrics metrics = new DisplayMetrics();
     //    JREngage.getApplicationContext().getWindowManager().getDefaultDisplay().getMetrics(metrics);
     //    metrics.get
     //}
 
     public static int colorDrawableGetColor(ColorDrawable d) {
         try {
             Method getColor = d.getClass().getMethod("getColor");
             return (Integer) getColor.invoke(d);
         } catch (NoSuchMethodException ignore) {
         } catch (InvocationTargetException ignore) {
         } catch (IllegalAccessException ignore) {
         } catch (ClassCastException ignore) {
         }
 
         // For some reason the following doesn't work on Android 15, but the above does, and the below
         // works for Android <= 10, so the function as a whole works but is a dirty hack.
 
         Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
         d.draw(new Canvas(b));
         return b.getPixel(0, 0);
     }
 
     /**
      * @param context a Context for this application, or null to use a cached context if available
      * @return true if this application was compiled as debuggable, false if not or if a Context was not
      *         available to evaluate.
      */
     public static boolean isApplicationDebuggable(Context context) {
         if (context != null) {
             return 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
         } else {
             context = JREngage.getApplicationContext();
             return context != null && isApplicationDebuggable(context);
         }
     }
 }

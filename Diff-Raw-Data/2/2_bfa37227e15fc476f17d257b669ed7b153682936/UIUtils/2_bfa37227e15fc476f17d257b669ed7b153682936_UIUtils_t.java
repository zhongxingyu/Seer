 /*
  * Copyright 2011 Google Inc.
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
 
 package com.bigpupdev.synodroid.utils;
 
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Build;
 
 /**
  * An assortment of UI helpers.
  */
 public class UIUtils {
 
 	public static boolean isHoneycomb() {
         return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
     }
 
 	public static boolean isICS() {
         return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
     }
 	
 	public static boolean isJB() {
         return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
     }
 
     public static boolean isTablet(Context context) {
         return (context.getResources().getConfiguration().screenLayout
                 & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
     }
 
     public static boolean isHoneycombTablet(Context context) {
         return isHoneycomb() && isTablet(context);
     }
 
 }

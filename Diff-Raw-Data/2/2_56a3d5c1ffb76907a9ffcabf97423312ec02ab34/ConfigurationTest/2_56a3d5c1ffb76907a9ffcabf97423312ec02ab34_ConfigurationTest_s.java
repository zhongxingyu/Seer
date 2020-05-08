 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package android.dpi.cts;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.test.AndroidTestCase;
 import android.view.Display;
 import android.view.WindowManager;
 import android.util.DisplayMetrics;
 
 import java.lang.Integer;
 import java.util.EnumSet;
 
 /**
  * This is verifying that the device under test is running a supported
  * resolution, and is being classified as the right Screen Layout
  * Size.
  */
 public class ConfigurationTest extends AndroidTestCase {
 
     private enum Density {
         // It is important to keep these sorted
         INVALID_LOW(Integer.MIN_VALUE, 99),
         LOW (100, 140),
         MEDIUM (141, 190),
         HIGH (191, 250),
         INVALID_HIGH(251, Integer.MAX_VALUE);
 
         private int low;
         private int high;
 
         Density(int low, int high) {
             this.low = low;
             this.high = high;
         }
 
         public static Density findDensity(int value) {
             Density density = INVALID_LOW;
             for (Density d : EnumSet.range(Density.INVALID_LOW, Density.INVALID_HIGH)) {
                 if (value >= d.low && value <= d.high) {
                     density = d;
                     break;
                 }
             }
             return density;
         }
     };
 
     /**
      * Holds information on the current active screen's configuration.
      */
     private static class ActiveScreenConfiguration {
         private final int width;
         private final int height;
         private final Density density;
 
         /**
          * Create a new ActiveScreenConfiguration.
          *
          * @param width the width of the screen
          * @param height the height of the screen
          * @param density the scaling factor for DIP from standard
          * density (160.0)
          */
         public ActiveScreenConfiguration(int width,
                                          int height,
                                          float density) {
             // 160 DIP is the "standard" density
             this(width, height, Density.findDensity((int) (160.0f * density)));
         }
 
         protected ActiveScreenConfiguration(int width,
                                             int height,
                                             Density density) {
             this.width = width;
             this.height = height;
             this.density = density;
         }
 
         public Density getDensity() {
             return density;
         }
 
         public int getWidth() {
             return width;
         }
 
         public int getHeight() {
             return height;
         }
     }
 
     private static class ScreenConfiguration extends ActiveScreenConfiguration {
         private final int screenLayout;
         private final boolean isWide;
 
         public ScreenConfiguration(int width,
                                    int height,
                                    Density density,
                                    int screenLayout,
                                    boolean isWide) {
             super(width, height, density);
             this.screenLayout = screenLayout;
             this.isWide = isWide;
         }
 
         public ScreenConfiguration(int width,
                                    int height,
                                    Density density,
                                    int screenLayout) {
             this(width, height, density, screenLayout, false);
         }
 
         public int getScreenLayout() {
             return screenLayout;
         }
 
         public boolean isWide() {
             return isWide;
         }
     };
 
     private static boolean areConfigsEqual(ActiveScreenConfiguration active,
                                            ScreenConfiguration screenConfig) {
         if (screenConfig.isWide()) {
             // For widescreen configs, the height is fixed but the
             // width only specifies a minimum.  But since the device
             // can be both landscape and portrait, we have to search
             // for which way it is.
             if (active.getHeight() == screenConfig.getHeight()) {
                 // active height matches config height.  Make sure
                 // that the active width is at least the config width.
                 return active.getWidth() >= screenConfig.getWidth();
             } else if (active.getWidth() == screenConfig.getHeight()) {
                 // directions are swapped
                 return active.getHeight() >= screenConfig.getWidth();
             } else {
                 return false;
             }
         } else {
             if (active.getWidth() == screenConfig.getWidth() &&
                 active.getHeight() == screenConfig.getHeight() &&
                 active.getDensity().equals(screenConfig.getDensity())) {
                 return true;
             }
             // It is also possible that the device is in landscape
             // mode, which flips the active w/h.
             if (active.getHeight() == screenConfig.getWidth() &&
                 active.getWidth() == screenConfig.getHeight() &&
                 active.getDensity().equals(screenConfig.getDensity())) {
                 return true;
             }
             // nope.
             return false;
         }
     }
 
     /**
      * Here's the current configuration table:
      *
      * Resoluion | Density          | Size
      * QVGA      | low (100-140)    | small
      * WQVGA     | low (100-140)    | normal
      * HVGA      | medium (141-190) | normal
      * WVGA      | high (191-250)   | normal
      * FWVGA     | high (191-250)   | normal
      * WSVGA     | high (191-250)   | large
 
      * VGA       | medium (141-190) | large
      * WVGA      | medium (141-190) | large
      * FWVGA     | medium (141-190) | large
      *
      * Any changes to allow additional resolutions will need to update this table
      */
 
     private static final ScreenConfiguration[] SUPPORTED_SCREEN_CONFIGS = {
         // QVGA      | low (100-140)    | small
         new ScreenConfiguration(240, 320, Density.LOW, Configuration.SCREENLAYOUT_SIZE_SMALL),
         // WQVGA     | low (100-140)    | normal
        new ScreenConfiguration(240, 320, Density.LOW, Configuration.SCREENLAYOUT_SIZE_SMALL, true),
         // HVGA      | medium (141-190) | normal
         new ScreenConfiguration(480, 320, Density.MEDIUM, Configuration.SCREENLAYOUT_SIZE_NORMAL),
         new ScreenConfiguration(640, 240, Density.MEDIUM, Configuration.SCREENLAYOUT_SIZE_NORMAL),
         // WVGA      | high (191-250)   | normal
         new ScreenConfiguration(640, 480, Density.HIGH, Configuration.SCREENLAYOUT_SIZE_NORMAL, true),
         // FWVGA     | high (191-250)   | normal
         new ScreenConfiguration(864, 480, Density.HIGH, Configuration.SCREENLAYOUT_SIZE_NORMAL),
         // WSVGA     | high (191-250)   | large
         new ScreenConfiguration(1024, 600, Density.HIGH, Configuration.SCREENLAYOUT_SIZE_LARGE),
 
         // VGA       | medium (141-190) | large
         new ScreenConfiguration(640, 480, Density.MEDIUM, Configuration.SCREENLAYOUT_SIZE_LARGE),
         // WVGA      | medium (141-190) | large
         new ScreenConfiguration(640, 480, Density.MEDIUM, Configuration.SCREENLAYOUT_SIZE_LARGE, true),
         // FWVGA     | medium (141-190) | large
         new ScreenConfiguration(864, 480, Density.MEDIUM, Configuration.SCREENLAYOUT_SIZE_LARGE),
 
     };
 
     private ActiveScreenConfiguration getCurrentScreenConfig() {
         WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         DisplayMetrics dm = new DisplayMetrics();
         display.getMetrics(dm);
         return new ActiveScreenConfiguration(display.getWidth(),
                                              display.getHeight(),
                                              dm.density);
     }
 
     /**
      * Get the current screen configuration, make sure it is a
      * supported screen configuration and that the screenlayout size
      * is being set correctly according to the compatibility
      * definition.
      */
     public void testScreenLayoutSize() {
         ActiveScreenConfiguration currentScreenConfig = getCurrentScreenConfig();
         // Make sure we have a valid density for the current screent.
         assertFalse(Density.INVALID_LOW.equals(currentScreenConfig.getDensity()));
         assertFalse(Density.INVALID_HIGH.equals(currentScreenConfig.getDensity()));
 
         // Look up the ScreenConfig in the supported table and make
         // sure we find a match.
         for (ScreenConfiguration screenConfig: SUPPORTED_SCREEN_CONFIGS) {
             if (areConfigsEqual(currentScreenConfig, screenConfig)) {
                 Configuration config = getContext().getResources().getConfiguration();
                 int size = config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                 if (screenConfig.getScreenLayout() == size) {
                     // we have a match, this is a supported device.
                     return;
                 }
             }
         }
         fail("Current screen configuration is not supported.");
     }
 }

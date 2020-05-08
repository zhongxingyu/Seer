 package com.jotabout.screeninfo;
 
 /**
  * ScreenInfo
  * 
  * Display the screen configuration parameters for an Android device.
  * 
  * Copyright (c) 2011 Michael J. Portuesi (http://www.jotabout.com)
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 import android.content.res.Configuration;
 import android.graphics.ImageFormat;
 import android.graphics.PixelFormat;
 import android.graphics.Point;
 import android.os.Build;
 import android.util.DisplayMetrics;
 import android.view.Surface;
 
 /**
  * Screen is a model object that summarizes information about the
  * device's display.
  * 
  * It unifies information from a few disparate Android APIs (Display,
  * DisplayMetrics, Configuration), and derives some additional device
  * statistics.  It also provides helpers to format data as strings
  * for display.
  * 
  */
 @SuppressWarnings("deprecation")	// Tell Lint to STFU about deprecated APIs - they are necessary for backwards compatibility
 public class Screen {
 	public static final int UNSUPPORTED = -255; /* will never be returned as a valid value */
 
 	private final InfoActivity ctx;
 	private final DisplayMetrics mMetrics;
 	private final Configuration mConfig;
  	
 	public final int mSizeClass;
 	
 	public final int widthPx, heightPx;
 	  /* Usable (application-accessible) dimensions of screen in pixels */
 	public final int realWidthPx, realHeightPx;
 	  /* Real dimensions of screen, in pixels (all usable space, including system-reserved space) */
 
 	public final int widthDp;
 	public final int heightDp;
 	public final int smallestDp;
 	 /* Smallest screen dimension in dp, (smallestWidthDp used in layout classification)
 		(see: http://android-developers.blogspot.com/2011/07/new-tools-for-managing-screen-sizes.html) */
 	public final int densityDpi; /* nominal screen density */
 	public final float xdpi, ydpi; /* actual screen density */
 	public double density;
 	public float scaledDensity;
 	 /* Scaling factor for fonts used on the display (DisplayMetrics.scaledDensity) */
 
 	public final double xyPhysicalWidth, xyPhysicalHeight;
 	public final double screenPhysicalWidth, screenPhysicalHeight;
 	public final double xyDiagonalSizeInches, xyDiagonalSizeMillimeters;
 	public final double screenDiagonalSizeInches, screenDiagonalSizeMillimeters;
 	public final double xyWidthSizeInches, xyHeightSizeInches;
 	public final double xyWidthSizeMillimeters, xyHeightSizeMillimeters;
 	public final double screenWidthSizeInches, screenHeightSizeInches;
 	public final double screenWidthSizeMillimeters, screenHeightSizeMillimeters;
 
     public final int screenLayout;
     public final int touchScreen;
     
     public final int defaultOrientation;
 	 /* Default, or "natural" screen orientation of the device. */
 	public final int currentOrientation;
         
     public final int pixelFormat;
     public final float refreshRate;
 
 	private static class CodeName
 	  /* mapping a constant value to a descriptive string resource ID. */
 	  {
 		public final int Value, ResID;
 
 		public CodeName
 		  (
 			int Value,
 			int ResID
 		  )
 		  {
 			this.Value = Value;
 			this.ResID = ResID;
 		  } /*CodeName*/
 
 	  } /*CodeName*/;
 
 	private String GetCodeName
 	  (
 		int Value,
 		CodeName[] Table
 	  )
 	  /* returns the string resource ID from the Table entry with the specified Value. */
 	  {
 		int Result = 0;
 		for (int i = 0;;)
 		  {
 			if (i == Table.length)
 				break;
 			if (Table[i].Value == Value)
 			  {
 				Result = Table[i].ResID;
 				break;
 			  } /*if*/
 			++i;
 		  } /*for*/
 		return
             Result != 0 ?
     			ctx.getString(Result)
             :
                 String.format(ctx.getString(R.string.nosuch), Value);
 	  } /*GetCodeName*/
 
 	@android.annotation.SuppressLint("NewApi")
 	public Screen( InfoActivity ctx ) {
 		this.ctx = ctx;
 		final android.view.Display mDisplay =
 			((android.view.WindowManager)ctx.getSystemService(android.content.Context.WINDOW_SERVICE))
 				.getDefaultDisplay();
  		mMetrics = new DisplayMetrics();
 		mDisplay.getMetrics(mMetrics);
         mConfig = ctx.getResources().getConfiguration();
 
         // Screen Size classification
 		mSizeClass = mConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
 		
 		  {
 			final Point pt = new Point();
 
 			// Usable Screen dimensions
 			try
 			  {
 				// Try to get size without the Status bar, if we can (API level 13)
 				mDisplay.getClass().getMethod("getSize", Point.class).invoke(mDisplay, pt);
 			  }
 			catch (NoSuchMethodException ignore)
 			  {
 				// Use older APIs
 				pt.x = mDisplay.getWidth();
 				pt.y = mDisplay.getHeight();
               }
             catch (IllegalAccessException err)
               {
                 throw new RuntimeException(err.toString());
               }
             catch (java.lang.reflect.InvocationTargetException err)
               {
                 throw new RuntimeException(err.toString());
 			  } /*try*/
 			widthPx = pt.x;
 			heightPx = pt.y;
 
 			try
 			  {
 				// Total (real) screen dimensions (as of Android 4.2, API 17)
 				DisplayMetrics metrics = new DisplayMetrics();
 				mDisplay.getClass().getMethod("getRealMetrics", DisplayMetrics.class)
 					.invoke(mDisplay, metrics);
 				pt.x = metrics.widthPixels;
 				pt.y = metrics.heightPixels;
 			  }
 			catch (NoSuchMethodException ignore)
 			  {
 				pt.x = UNSUPPORTED;
 				pt.y = UNSUPPORTED;
               }
             catch (IllegalAccessException err)
               {
                 throw new RuntimeException(err.toString());
               }
             catch (java.lang.reflect.InvocationTargetException err)
               {
                 throw new RuntimeException(err.toString());
 			  } /*try*/
 			realWidthPx = pt.x;
 			realHeightPx = pt.y;
 		  }
 
 		  {
 			final Point pt = new Point();
 			int smallest;
 			try
 			  {
 				// Screen sizes in device-independent pixels (dp) (as of API 13)
				final Class<Configuration> ConfigClass = (Class<Configuration>)mConfig.getClass();
 				pt.x = ConfigClass.getField("screenWidthDp").getInt(mConfig);
 				pt.y = ConfigClass.getField("screenHeightDp").getInt(mConfig);
 				smallest = ConfigClass.getField("smallestScreenWidthDp").getInt(mConfig);
 			  }
 			catch (NoSuchFieldException ignore)
 			  {
 				pt.x = (int)(
 						(
 							(double)(realWidthPx == UNSUPPORTED ? widthPx : realWidthPx)
 						/
 							mMetrics.density
 						)
 					+
 						0.5
 					);
 				pt.y = (int)(
 						(
 							(double)(realHeightPx == UNSUPPORTED ? heightPx : realHeightPx)
 						/
 							mMetrics.density
 						)
 					+
 						0.5
 					);
 				smallest = pt.x > pt.y ? pt.y : pt.x;
               }
             catch (IllegalAccessException err)
               {
                 throw new RuntimeException(err.toString());
 			  } /*try*/
 			widthDp = pt.x;
 			heightDp = pt.y;
 			smallestDp = smallest;
 		  }
 
 		// nominal DPI
 		densityDpi = mMetrics.densityDpi;
 		// nominal screen density scaling factors
 		density = mMetrics.density;
 		scaledDensity = mMetrics.scaledDensity; /* for fonts, may differ from density under user control */
 	  /* actual x & y pixel density (may not be exactly the same in both axes): */
 		// Normalize the xdpi/ydpi for the next set of calculations
 		// Guard against divide-by-zero, possible with lazy device manufacturers
 		// who set these fields incorrectly. Set the density to our best guess.
 		xdpi = mMetrics.xdpi < 1.0f ? mMetrics.densityDpi : mMetrics.xdpi;
 		ydpi = mMetrics.ydpi < 1.0f ? mMetrics.densityDpi : mMetrics.ydpi;
 
 		// Calculate physical screen width/height
 		xyPhysicalWidth = (float)mMetrics.widthPixels / xdpi;
 		xyPhysicalHeight = (float)mMetrics.heightPixels / ydpi;
 		screenPhysicalWidth = (float)mMetrics.widthPixels / mMetrics.densityDpi;
 		screenPhysicalHeight = (float)mMetrics.heightPixels / mMetrics.densityDpi;
 
 		// Calculate width and height screen size, in Metric units
 		xyWidthSizeInches = Math.floor( xyPhysicalWidth * 10.0 + 0.5 ) / 10.0;
 		xyHeightSizeInches = Math.floor( xyPhysicalHeight * 10.0 + 0.5 ) / 10.0;
 		xyWidthSizeMillimeters = Math.floor( xyPhysicalWidth * 25.4 + 0.5 );
 		xyHeightSizeMillimeters = Math.floor( xyPhysicalHeight * 25.4 + 0.5 );
 		screenWidthSizeInches = Math.floor( screenPhysicalWidth * 10.0 + 0.5 ) / 10.0;
 		screenHeightSizeInches = Math.floor( screenPhysicalHeight * 10.0 + 0.5 ) / 10.0;
 		screenWidthSizeMillimeters = Math.floor( screenPhysicalWidth * 25.4 + 0.5 );
 		screenHeightSizeMillimeters = Math.floor( screenPhysicalHeight * 25.4 + 0.5 );
 		
 		// Calculate diagonal screen size, in both U.S. and Metric units
 		final double xyRawDiagonalSizeInches = Math.sqrt(Math.pow(xyPhysicalWidth, 2) + Math.pow(xyPhysicalHeight, 2));
 		xyDiagonalSizeInches = Math.floor( xyRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
 		xyDiagonalSizeMillimeters = Math.floor( xyRawDiagonalSizeInches * 25.4 + 0.5 );
 		final double screenRawDiagonalSizeInches = Math.sqrt(Math.pow(screenPhysicalWidth, 2) + Math.pow(screenPhysicalHeight, 2));
 		screenDiagonalSizeInches = Math.floor( screenRawDiagonalSizeInches * 10.0 + 0.5 ) / 10.0;
 		screenDiagonalSizeMillimeters = Math.floor( screenRawDiagonalSizeInches * 25.4 + 0.5 );
 		
 		// Long/wide
         screenLayout = mConfig.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
         // Orientation
         defaultOrientation = mConfig.orientation;
 		// Do the best job we can to find out which way the screen is currently rotated.
 		  {
 			int rotation;
 			try
 			  {
 				// First, try the Display#getRotation() call, which was introduced in Froyo.
 				// Reference: http://android-developers.blogspot.com/2010/09/one-screen-turn-deserves-another.html
 				rotation = (Integer)mDisplay.getClass().getMethod("getRotation").invoke(mDisplay);
 			  }
 			catch (NoSuchMethodException ignore)
 			  {
 				// Fall back on the deprecated Display#getOrientation method from earlier releases of Android.
 				rotation = mDisplay.getOrientation();
               }
             catch (IllegalAccessException err)
               {
                 throw new RuntimeException(err.toString());
               }
             catch (java.lang.reflect.InvocationTargetException err)
               {
                 throw new RuntimeException(err.toString());
 			  } /*try*/
 			currentOrientation = rotation;
 		  }
         
         // Touchscreen type
         touchScreen = mConfig.touchscreen;
         
         // Pixel format (deprecated as of Android 4.2, API 17)
         if ( Build.VERSION.SDK_INT < 17 )
 		  {
         	pixelFormat = mDisplay.getPixelFormat();
           }
 		else
 		  {
 	        pixelFormat = UNSUPPORTED;
 		  } /*if*/
 		
 		// Refresh rate
         refreshRate = mDisplay.getRefreshRate();
 	}
  	
  	/**
 	 * Model name of device.
 	 * @return
 	 */
 	public String deviceModel() {
 		return Build.MODEL;
 	}
 	
 	/**
 	 * Version of Android (e.g. "2.3.5").
 	 * 
 	 * @return
 	 */
 	public String androidVersion() {
 		return Build.VERSION.RELEASE;
 	}
 	
 	private static final CodeName[] SizeCodes = new CodeName[]
 		{
 			new CodeName(Configuration.SCREENLAYOUT_SIZE_SMALL, R.string.small),
 			new CodeName(Configuration.SCREENLAYOUT_SIZE_NORMAL, R.string.normal),
 			new CodeName(Configuration.SCREENLAYOUT_SIZE_LARGE, R.string.large),
 			new CodeName(Configuration.SCREENLAYOUT_SIZE_XLARGE, R.string.xlarge),
 			new CodeName(Configuration.SCREENLAYOUT_SIZE_UNDEFINED, R.string.undefined),
 		};
 
 	public String GetSizeName()
 	  {
 		return
 			GetCodeName(mConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK, SizeCodes);
 	  } /*GetSizeName*/
 
 	private static final CodeName[] DensityCodes = new CodeName[]
 		{
 			new CodeName(DisplayMetrics.DENSITY_LOW, R.string.ldpi),
 			new CodeName(DisplayMetrics.DENSITY_MEDIUM, R.string.mdpi),
 			new CodeName(DisplayMetrics.DENSITY_TV, R.string.tvdpi),
 			new CodeName(DisplayMetrics.DENSITY_HIGH, R.string.hdpi),
 			new CodeName(DisplayMetrics.DENSITY_XHIGH, R.string.xhdpi),
 			new CodeName(DisplayMetrics.DENSITY_XXHIGH, R.string.xxhdpi),
 			new CodeName(DisplayMetrics.DENSITY_XXXHIGH, R.string.xxxhdpi),
 		};
 
 	public String GetDensityName()
 	  {
 		return
 			GetCodeName(mMetrics.densityDpi, DensityCodes);
 	  } /*GetDensityName*/
 
 	public String GetRealWidthPx()
 	  {
 		return
 			realWidthPx != UNSUPPORTED ?
 				Integer.toString(realWidthPx)
 			:
 				ctx.getString(R.string.unsupported);
 	  } /*GetRealWidthPx*/
 
 	public String GetRealHeightPx()
 	  {
 		return
 			realHeightPx != UNSUPPORTED ?
 				Integer.toString(realHeightPx)
 			:
 				ctx.getString(R.string.unsupported);
 	  } /*GetRealHeightPx*/
 
 	private static final CodeName[] LongWideCodes = new CodeName[]
 		{
 			new CodeName(Configuration.SCREENLAYOUT_LONG_YES, R.string.yes),
 			new CodeName(Configuration.SCREENLAYOUT_LONG_NO, R.string.no),
 			new CodeName(Configuration.SCREENLAYOUT_LONG_UNDEFINED, R.string.undefined),
 		};
 	
 	/**
 	 * Screen layout, as text
 	 */
 	public String screenLayoutText() {
 		return
 			GetCodeName(mConfig.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK, LongWideCodes);
 	}
 
 	private static final CodeName[] OrientationCodes = new CodeName[]
 		{
 			new CodeName(Configuration.ORIENTATION_LANDSCAPE, R.string.orientation_landscape),
 			new CodeName(Configuration.ORIENTATION_PORTRAIT, R.string.orientation_portrait),
 			new CodeName(Configuration.ORIENTATION_SQUARE, R.string.orientation_square),
 			new CodeName(Configuration.ORIENTATION_UNDEFINED, R.string.undefined),
 		};
 	
 	/**
 	 * Default orientation as text
 	 */
 	public String defaultOrientationText() {
 		return
 			GetCodeName(defaultOrientation, OrientationCodes);
 	}
 
 	private static final CodeName[] RotationAngles = new CodeName[]
 		{
 			new CodeName(Surface.ROTATION_0, R.string.degrees_0),
 			new CodeName(Surface.ROTATION_90, R.string.degrees_90),
 			new CodeName(Surface.ROTATION_180, R.string.degrees_180),
 			new CodeName(Surface.ROTATION_270, R.string.degrees_270),
 		};
 	
 	/**
 	 * Current orientation as text
 	 */
 	public String currentOrientationText() {
 		return
 			GetCodeName(currentOrientation, RotationAngles);
 	}
 
 	private static final CodeName[] TouchScreenCodes = new CodeName[]
 		{
 			new CodeName(Configuration.TOUCHSCREEN_FINGER, R.string.touchscreen_finger),
 			new CodeName(Configuration.TOUCHSCREEN_STYLUS, R.string.touchscreen_stylus),
 			new CodeName(Configuration.TOUCHSCREEN_NOTOUCH, R.string.touchscreen_none),
 			new CodeName(Configuration.TOUCHSCREEN_UNDEFINED, R.string.undefined),
 		};
 	
 	/**
 	 * Touchscreen properties as text
 	 */
 	public String touchScreenText() {
 		return
 			GetCodeName(touchScreen, TouchScreenCodes);
 	}
 
     private static final CodeName[] PixelFormatCodes = new CodeName[]
         {
             new CodeName(PixelFormat.A_8, R.string.a_8),
             new CodeName(PixelFormat.JPEG, R.string.jpeg),
             new CodeName(PixelFormat.L_8, R.string.l_8),
             new CodeName(PixelFormat.LA_88, R.string.la_88),
             new CodeName(PixelFormat.OPAQUE, R.string.opaque),
             new CodeName(PixelFormat.RGB_332, R.string.rgb_332),
             new CodeName(PixelFormat.RGB_565, R.string.rgb_565),
             new CodeName(PixelFormat.RGB_888, R.string.rgb_888),
             new CodeName(PixelFormat.RGBA_4444, R.string.rgba_4444),
             new CodeName(PixelFormat.RGBA_5551, R.string.rgba_5551),
             new CodeName(PixelFormat.RGBA_8888, R.string.rgba_8888),
             new CodeName(PixelFormat.RGBX_8888, R.string.rgbx_8888),
             new CodeName(5, R.string.bgra_8888), /* see platform/system/core/include/system/graphics.h */
             new CodeName(PixelFormat.TRANSLUCENT, R.string.translucent),
             new CodeName(PixelFormat.TRANSPARENT, R.string.transparent),
             new CodeName(PixelFormat.UNKNOWN, R.string.unknown),
             new CodeName(UNSUPPORTED, R.string.unsupported),
             new CodeName(ImageFormat.NV21, R.string.nv21),
             new CodeName(ImageFormat.YUY2, R.string.yuy2),
             new CodeName(ImageFormat.NV16, R.string.nv16),
         };
 	
 	/**
 	 * Pixel format as text
 	 */
 	public String pixelFormatText() {
 		return
 			GetCodeName(pixelFormat, PixelFormatCodes);
 	}
 
 	/**
 	 * Return a string containing a text-based summary, suitable
 	 * to share, email, save to SD card, etc.
 	 * 
 	 * @return
 	 */
 	public String summaryText() {
 		StringBuilder sb = new StringBuilder();
 		for
 		  (
 			InfoActivity.InfoMember Member :
 				new InfoActivity.InfoMember[]
 					{
 						ctx.new InfoMethod(this, "deviceModel", R.string.device_label),
 						ctx.new InfoMethod(this, "androidVersion", R.string.os_version_label),
 						ctx.new InfoMethod(this, "GetSizeName", R.string.screen_class_label),
 						ctx.new InfoMethod(this, "GetDensityName", R.string.density_class_label),
 						ctx.new InfoMethod(this, "GetRealWidthPx", R.string.total_width_pixels_label),
 						ctx.new InfoMethod(this, "GetRealHeightPx", R.string.total_height_pixels_label),
 						ctx.new InfoField(this, "widthPx", R.string.width_pixels_label),
 						ctx.new InfoField(this, "heightPx", R.string.height_pixels_label),
 						ctx.new InfoField(this, "widthDp", R.string.width_dp_label),
 						ctx.new InfoField(this, "heightDp", R.string.height_dp_label),
 						ctx.new InfoField(this, "smallestDp", R.string.smallest_dp_label),
 						ctx.new InfoMethod(this, "screenLayoutText", R.string.long_wide_label),
 						ctx.new InfoMethod(this, "defaultOrientationText", R.string.natural_orientation_label),
 						ctx.new InfoMethod(this, "currentOrientationText", R.string.current_orientation_label),
 						ctx.new InfoMethod(this, "touchScreenText", R.string.touchscreen_label),
 						ctx.new InfoField(this, "densityDpi", R.string.nominal_dpi_label),
 						ctx.new InfoField(this, "xdpi", R.string.actual_xdpi_label),
 						ctx.new InfoField(this, "ydpi", R.string.actual_ydpi_label),
 						ctx.new InfoField(this, "density", R.string.logical_density_label),
 						ctx.new InfoField(this, "scaledDensity", R.string.font_scale_density_label),
 						ctx.new InfoField(this, "xyDiagonalSizeInches", R.string.xy_diagonal_size_inches_label),
 						ctx.new InfoField(this, "xyDiagonalSizeMillimeters", R.string.xy_diagonal_size_mm_label),
 						ctx.new InfoField(this, "xyWidthSizeInches", R.string.xy_width_size_inches_label),
 						ctx.new InfoField(this, "xyHeightSizeInches", R.string.xy_height_size_inches_label),
 						ctx.new InfoField(this, "xyWidthSizeMillimeters", R.string.xy_width_size_mm_label),
 						ctx.new InfoField(this, "xyHeightSizeMillimeters", R.string.xy_height_size_mm_label),
 						ctx.new InfoField(this, "screenDiagonalSizeInches", R.string.screen_diagonal_size_inches_label),
 						ctx.new InfoField(this, "screenDiagonalSizeMillimeters", R.string.screen_diagonal_size_mm_label),
 						ctx.new InfoField(this, "screenWidthSizeInches", R.string.screen_width_size_inches_label),
 						ctx.new InfoField(this, "screenHeightSizeInches", R.string.screen_height_size_inches_label),
 						ctx.new InfoField(this, "screenWidthSizeMillimeters", R.string.screen_width_size_mm_label),
 						ctx.new InfoField(this, "screenHeightSizeMillimeters", R.string.screen_height_size_mm_label),
 						ctx.new InfoMethod(this, "pixelFormatText", R.string.pixel_format_label),
 						ctx.new InfoField(this, "refreshRate", R.string.refresh_rate_label),
 					}
 		  )
 		  {
 			Member.AppendValue(sb);
 		  } /*for*/
 		sb
 			.append("\n")
 			.append(ctx.getString(R.string.play_store_link)).append("\n");
 		return sb.toString();
 	}
 
 }

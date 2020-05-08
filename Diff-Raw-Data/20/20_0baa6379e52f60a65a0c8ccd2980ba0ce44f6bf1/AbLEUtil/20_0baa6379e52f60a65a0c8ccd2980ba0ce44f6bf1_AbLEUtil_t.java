 /*
  * Copyright 2013 Phil Brown
 
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
 
 package self.philbrown.AbLE;
 
 import java.lang.reflect.Method;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 
 /**
  * Provides shortcut methods for layouts, logging, etc
  * @author Phil Brown
  *
  */
 public class AbLEUtil 
 {
 
 	/**
 	 * Concatenates Strings together using a StringBuilder. This is faster than using the +-operator.
 	 * @param args the Objects to append
 	 * @return a concatenation of the Objects in {@code args}
 	 */
 	public static String buildString(Object... args)
 	{
 		StringBuilder b = new StringBuilder();
 		for (Object obj : args)
 		{
 			b.append(obj);
 		}
 		return b.toString();
 	}
 	
 	/**
 	 * Handles String format for the default (US) Locale. This should be used for locale-agnostic
 	 * messages to prevent locale errors, such as commas used instead of decimals.
 	 * @param format
 	 * @param args
 	 * @return
 	 */
 	public static String format(String format, Object... args)
 	{
 		return String.format(Locale.US, format, args);
 	}
 	
 	/**
 	 * Convenience method for writing a warning to the logcat
 	 * @param warning
 	 * @param args
 	 */
 	public static void warn(String warning, Object... args)
 	{
 		try {
 			Method m = Log.class.getMethod("w", new Class<?>[]{String.class, String.class});
 			log(m, warning, args);
 		} catch (Exception e)
 		{
			Log.w("AbLE", "Log Failed", e);
 		}
 	}
 	
 	/**
 	 * Convenience method for writing information to the logcat
 	 * @param information
 	 * @param args
 	 */
 	public static void info(String information, Object... args)
 	{
 		try {
 			Method m = Log.class.getMethod("i", new Class<?>[]{String.class, String.class});
 			log(m, information, args);
 		} catch (Exception e)
 		{
			Log.w("AbLE", "Log Failed", e);
 		}
 	}
 	
 	/**
 	 * Convenience method for writing an error message to the logcat
 	 * @param error
 	 * @param args
 	 */
 	public static void err(String error, Object... args)
 	{
 		try {
 			Method m = Log.class.getMethod("e", new Class<?>[]{String.class, String.class});
 			log(m, error, args);
 		} catch (Exception e)
 		{
			Log.w("AbLE", "Log Failed", e);
 		}
 	}
 	
 	/**
 	 * Convenience method for writing a debug message to the logcat
 	 * @param debug
 	 * @param args
 	 */
 	public static void debug(String debug, Object... args)
 	{
 		try {
 			Method m = Log.class.getMethod("d", new Class<?>[]{String.class, String.class});
 			log(m, debug, args);
 		} catch (Exception e)
 		{
			Log.w("AbLE", "Log Failed", e);
 		}
 	}
 	
 	/**
 	 * Convenience method for writing a verbose message to the logcat
 	 * @param verbose
 	 * @param args
 	 */
 	public static void log(String verbose, Object... args)
 	{
 		try {
 			Method m = Log.class.getMethod("v", new Class<?>[]{String.class, String.class});
 			log(m, verbose, args);
 		} catch (Exception e)
 		{
			Log.w("AbLE", "Log Failed", e);
 		}
 	}
 	
 
 	
 	/**
 	 * This method is used by the core logging functions to nicely format output to the logcat
 	 * @param logMethod the method to call. Should be {@link Log#i(String, String)} or another {@link Log} static method
 	 * @param format the text to display, using java string format arguments
 	 * @param args the arguments to show in the format string.
 	 */
 	private static void log(Method logMethod, String format, Object... args)
 	{
 		try{ 
 			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
 			StringBuilder b = new StringBuilder();
 			b.append(format).append(", ");
 			int index = 1;
 			String name = null;
 			boolean loop = true;
 			do {
 				index++;
 				if (trace != null && index < trace.length)
 				{
 					name = trace[index].getClassName();
 					
 					if (name != null)
 					{
 						if (!name.contains("self.philbrown.AbLE.AbLEUtil"))
 						{
 							loop = false;
 						}
 					}
 				}
 				else
 				{
 					index = 1;
 					loop = false;
 				}
 				
 			} while(loop);
 			b.append(formatStackTrace(trace[index]));
 			b.append(buildCommaSeparatedString(args));
 			try {
				logMethod.invoke(null, "AbLE", String.format(Locale.US, b.toString(), args));
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		} catch (Throwable t)
 		{
			Log.w("AbLE", "Log Failed", t);
 		}
 	}
 	
 
 	/**
 	 * Calculates the containing Rect of the given View Object
 	 * @param v the view to query
 	 * @return the bounds of {@code v}.
 	 */
 	public static Rect absoluteBounds(View v)
 	{
 		int[] loc = new int[2];
 		v.getLocationOnScreen(loc);
 		if (AbLEActivity.isScreenSizeCustomized())
 		{
 			//modify based on screen size changes.
 			int[] contentLoc = new int[2];
 			AbLEActivity.getContentLocationOnScreen(contentLoc);
 			int w = loc[0] - contentLoc[0];
 			int h = loc[1] - contentLoc[1];
 			return new Rect(w, h, w + (v.getRight() - v.getLeft()), h + (v.getBottom() - v.getTop()));
 		}
 		return new Rect(loc[0], loc[1], loc[0] + (v.getRight() - v.getLeft()), loc[1] + (v.getBottom() - v.getTop()));
 	}
 	
 	/**
 	 * Uses the device's screen size to determine if the current device is a tablet or is tablet-sized.
 	 * @param context required to provide access to the device configuration information
 	 * @return true if the device has a large enough screen to be considered a tablet (for UI purposes).
 	 */
 	public static boolean isTablet(Context context) {
 	    boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
 	    boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
 	    return (xlarge || large);
 	}//isTablet
 	
 	/**
 	 * Places the x and y pixel dimensions of the screen into the given {@code int[]}.
 	 * @param out the array in which to place the screen x and y size (in pixels), respectively
 	 */
 	public static void getScreenSize(Context context, int[] out)
 	{
 		if (out == null)
         {
 			AbLEUtil.err("Could not place size into null argument.");
 			return;
         }
 		if (AbLEActivity.isScreenSizeCustomized())
 		{
 			out[0] = AbLEActivity.getCustomWidth();
 			out[1] = AbLEActivity.getCustomHeight();
 		}
 		else
 		{
 
 			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();     
 			
 			out[0] = display.getWidth();
 	        out[1] = display.getHeight();
 		}
 	}
 	
 
 	/**
 	 * Formats a stack trace into a single line that provides relevant information for debugging
 	 * @param element the element to format
 	 * @return a well-formatted stack-trace line containing the class name, method name, and line number
 	 * that, when clicked in the logcat, will display the line or source from where the message originated.
 	 */
 	public static String formatStackTrace(StackTraceElement element)
 	{
 		StringBuilder b = new StringBuilder();
 		
 		b.append(" at ");
 		String clazz = element.getClassName();
 		b.append(clazz).append(".");
 		b.append(element.getMethodName()).append("(");
 		b.append(clazz.substring(clazz.lastIndexOf(".") + 1)).append(".java:");
 		b.append(element.getLineNumber()).append(")").append(" , ##");
 		return b.toString();
 	}
 	
 	/**
 	 * Takes a list of Objects and calls their {@link #toString()} methods to get their string representation, then
 	 * inserts a comma between all of them
 	 * @param args a list of Objects to get as a comma-separated list 
 	 * @return a comma-separated list of the given {@code args}
 	 */
 	public static String buildCommaSeparatedString(Object... args)
 	{
 		if (args == null) return "";
 		StringBuilder b = new StringBuilder();
 		for (int i = 0; i < args.length; i++)
 		{
 			b.append(args[i]);
 			if (i != args.length-1)
 			{
 				b.append(", ");
 			}
 		}
 		return b.toString();
 	}
 	
 
 	/**
 	 * Returns a color formatted as a rgb string
 	 * TODO alpha?
 	 * @param color
 	 * @return
 	 */
 	public static String ColorToString(int color)
 	{
 		return format("#%06X", (0xFFFFFF & color));
 	}
 	
 	/**
 	 * Returns the complimentary color of the given color
 	 * @param color
 	 * @return
 	 */
 	public static int complimentaryColor(int color) {
 		return Color.argb(Color.alpha(color), 
         		          (~Color.red(color)) & 0xff, 
         		          (~Color.green(color)) & 0xff, 
         		          (~Color.blue(color)) & 0xff);
 	}
 	
 	/**
 	 * Calculates the <em>brightness</em> of a color, based on its RGB values.
 	 * @param color the color whose brightness is in question
 	 * @return a brightness value in the range [0-255].
 	 * @see <a href="http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx">Calculating the Perceived Brightness of a Color</a>
 	 */
 	public static int colorBrightness(int color)
 	{
 		int r2 = Color.red(color) * Color.red(color);
 		int g2 = Color.green(color) * Color.green(color);
 		int b2 = Color.blue(color) * Color.blue(color);
 		
 	   return (int) Math.sqrt(r2 * 0.241f 
 			                + g2 * 0.691f
 			                + b2 * 0.068f);
 	}
 }

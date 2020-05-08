 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.utils;
 
 import java.util.Date;
 
 import com.bigpupdev.synodroid.data.TaskDetail;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Bitmap.Config;
 import android.graphics.PorterDuff.Mode;
 
 /**
  * As usual a utility class
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class Utils {
 
	public static String validateSSID(String ssid){
		if (ssid == null){
			return ssid;
		}
		
		if (ssid.startsWith("\"") && ssid.endsWith("\"")){
			return ssid.substring(1, ssid.length()-1);
		}
		
		return ssid;
	}
		
 	/**
 	 * Compute the time left in the following format d h m s
 	 * 
 	 * @param etaP
 	 * @return
 	 */
 	public static String computeTimeLeft(String etaP) {
 		try {
 			if (etaP.equals("-1")) {
 				etaP = "0";
 			}
 			int eta = Integer.parseInt(etaP);
 			return computeTimeLeft(eta);
 		}
 		// Nothing to do : just return ""
 		catch (NumberFormatException ex) {
 		}
 		return "";
 	}
 
 	/**
 	 * Compute the time left in the following format d h m s
 	 * 
 	 * @param etaP
 	 * @return
 	 */
 	public static String computeTimeLeft(long etaP) {
 		String result = "";
 		// Only if time left is known
 		if (etaP != -1) {
 			// Days
 			long d = etaP / (60 * 60 * 24);
 			if (d > 0) {
 				result += d + "d ";
 				etaP -= d * (60 * 60 * 24);
 			}
 			// Hours
 			long h = etaP / (60 * 60);
 			if (h > 0 || d > 0) {
 				result += h + "h ";
 				etaP -= h * (60 * 60);
 			}
 			// Minutes
 			long m = etaP / 60;
 			if (m > 0 || h > 0 || m > 0) {
 				result += m + "m ";
 				etaP -= m * 60;
 			}
 			// Secondes
 			result += etaP + "s";
 		}
 		return result;
 	}
 
 	/**
 	 * Return a localized date computed
 	 * 
 	 * @param secondP
 	 * @return
 	 */
 	public static String computeDate(String secondP) {
 		String result = "";
 		if (secondP != null && secondP.length() > 0) {
 			try {
 				long milli = Long.parseLong(secondP) * 1000;
 				Date date = new Date(milli);
 				result = date.toLocaleString();
 			}
 			// Nothing to do: not a number
 			catch (NumberFormatException ex) {
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Utility method to convert a string into an int and log if an error occured
 	 * 
 	 * @param valueP
 	 * @return
 	 */
 	public static Long toLong(String valueP) {
 		Long result = null;
 		try {
 			result = Long.parseLong(valueP);
 		}
 		// Not a number
 		catch (NumberFormatException ex) {
 			result = 0l;
 		}
 		return result;
 	}
 
 	/**
 	 * Utility method to convert a string into an double and log if an error occured
 	 * 
 	 * @param valueP
 	 * @return
 	 */
 	public static double toDouble(String valueP) {
 		double result = 0;
 		try {
 			result = Double.parseDouble(valueP);
 		}
 		// Not a number
 		catch (NumberFormatException ex) {
 			result = 0.0d;
 		}
 		return result;
 	}
 
 	/**
 	 * Extract from percent string (with the caracter '%') the percentage int value
 	 * 
 	 * @param percentP
 	 * @return
 	 */
 	public static int percent2int(String percentP) {
 		int result = 0;
 		if (percentP != null && percentP.length() > 0) {
 			String p = percentP.replace('%', ' ').trim();
 			try {
 				result = (int) Double.parseDouble(p);
 			}
 			// Nothing to do: it is not an integer, os just return the default value
 			catch (NumberFormatException ex) {
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Convert a file size representation in a long size bytes
 	 * 
 	 * @param sizeP
 	 * @return
 	 */
 	public static long fileSizeToBytes(String sizeP) {
 		long result = -1;
 		sizeP = sizeP.trim();
 		// Search for the size unit separator
 		int index = sizeP.indexOf(" ");
 		if (index != -1) {
 			String valStr = sizeP.substring(0, index - 1);
 			String unitStr = sizeP.substring(index + 1).toLowerCase();
 			try {
 				double size = Double.parseDouble(valStr);
 				if (unitStr.equals("kb")) {
 					size = size * 1000;
 				} else if (unitStr.equals("mb")) {
 					size = size * 1000 * 1000;
 				} else if (unitStr.equals("gb")) {
 					size = size * 1000 * 1000 * 1000;
 				} else if (unitStr.equals("tb")) {
 					size = size * 1000 * 1000 * 1000 * 1000;
 				}
 				result = (long) size;
 			}
 			// Not a number
 			catch (NumberFormatException ex) {
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Convert a file size in bytes to a string representation
 	 * 
 	 * @param bytes
 	 * @return
 	 */
 	public static String bytesToFileSize(long bytes, boolean si, String fail) {
 	    try{
 			int unit = si ? 1000 : 1024;
 		    if (bytes < unit) return bytes + " B";
 		    int exp = (int) (Math.log(bytes) / Math.log(unit));
 		    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1)+"";
 		    return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
 	    } catch (Exception e){
 	    	return fail;
 	    }
 	}
 
 	/**
 	 * Compute the upload percentage according to the filesize and the ratio
 	 * 
 	 * @param detailP
 	 * @return Return an integer could have been compute otherwise it returns null
 	 */
 	public static Integer computeUploadPercent(TaskDetail detailP) {
 		Integer result = null;
 		long uploaded = detailP.bytesUploaded;
 		double ratio = ((double) (detailP.seedingRatio)) / 100.0d;
 		// If seeding ratio is 0, we suppose it is 100 => When a task is paused then
 		// the server returns 0 which is not the correct anwser even if the task is
 		// paused
 		if (detailP.seedingRatio == 0) {
 			ratio = 1.0d;
 		}
 		if (ratio != 0 && detailP.fileSize != -1) {
 			try {
 				result = Integer.valueOf((int) ((uploaded * 100) / (detailP.fileSize * ratio)));
 			} catch (ArithmeticException e) {
 				result = Integer.valueOf(100);
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * Create a rounded bitmap
 	 * 
 	 * @param bitmap
 	 *            The original bitmap
 	 * @return
 	 */
 	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
 		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
 		Canvas canvas = new Canvas(output);
 
 		final int color = 0xff424242;
 		final Paint paint = new Paint();
 		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
 		final RectF rectF = new RectF(rect);
 
 		paint.setAntiAlias(true);
 		canvas.drawARGB(0, 0, 0, 0);
 		paint.setColor(color);
 		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
 
 		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
 		canvas.drawBitmap(bitmap, rect, rect, paint);
 		return output;
 	}
 
 }

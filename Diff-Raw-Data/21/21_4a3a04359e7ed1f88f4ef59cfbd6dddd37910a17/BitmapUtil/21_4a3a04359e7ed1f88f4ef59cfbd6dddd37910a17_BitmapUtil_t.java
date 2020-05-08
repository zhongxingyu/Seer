 package com.photonoter.imaging;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.media.ExifInterface;
 import android.os.Build;
 import android.os.Environment;
 import android.util.Log;
 
 import com.photonoter.PhotoBackWriterApp;
 
 public class BitmapUtil {
 
 	private static final String LOG_TAG = "BitmapUtil";
 
 	private static final int COMPRESSED_QUALITY = 70;
 
 	public static void savePing(final Bitmap aBitmap, final String aPath) {
 		if (null == aBitmap || null == aPath) {
 			return;
 		}
 
 		try {
 
 			FileOutputStream out = new FileOutputStream(aPath);
 			aBitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSED_QUALITY, out);
 			out.flush();
 			out.close();
 
 			Log.i(LOG_TAG, "Saved writing to: " + aPath);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void saveJpeg(final Bitmap aBitmap, final String aPath) {
 		if (null == aBitmap || null == aPath) {
 			return;
 		}
 
 		try {
 
 			FileOutputStream out = new FileOutputStream(aPath);
 			aBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSED_QUALITY,
 					out);
 			out.flush();
 			out.close();
 
 			Log.i(LOG_TAG, "Saved writing to: " + aPath);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void copyExif(final String sourcePath, final String destPath,
 			final Integer newWidth, final Integer newHeight) {
 		try {
 			// copy paste exif information from original file to new
 			// file
 			ExifInterface oldexif = new ExifInterface(sourcePath);
 			ExifInterface newexif = new ExifInterface(destPath);
 
 			int build = Build.VERSION.SDK_INT;
 
 			// From API 11
 			if (build >= 11) {
 				if (oldexif.getAttribute("FNumber") != null) {
 					newexif.setAttribute("FNumber",
 							oldexif.getAttribute("FNumber"));
 				}
 				if (oldexif.getAttribute("ExposureTime") != null) {
 					newexif.setAttribute("ExposureTime",
 							oldexif.getAttribute("ExposureTime"));
 				}
 				if (oldexif.getAttribute("ISOSpeedRatings") != null) {
 					newexif.setAttribute("ISOSpeedRatings",
 							oldexif.getAttribute("ISOSpeedRatings"));
 				}
 			}
 			// From API 9
 			if (build >= 9) {
 				if (oldexif.getAttribute("GPSAltitude") != null) {
 					newexif.setAttribute("GPSAltitude",
 							oldexif.getAttribute("GPSAltitude"));
 				}
 				if (oldexif.getAttribute("GPSAltitudeRef") != null) {
 					newexif.setAttribute("GPSAltitudeRef",
 							oldexif.getAttribute("GPSAltitudeRef"));
 				}
 			}
 			// From API 8
 			if (build >= 8) {
 				if (oldexif.getAttribute("FocalLength") != null) {
 					newexif.setAttribute("FocalLength",
 							oldexif.getAttribute("FocalLength"));
 				}
 				if (oldexif.getAttribute("GPSDateStamp") != null) {
 					newexif.setAttribute("GPSDateStamp",
 							oldexif.getAttribute("GPSDateStamp"));
 				}
 				if (oldexif.getAttribute("GPSProcessingMethod") != null) {
 					newexif.setAttribute("GPSProcessingMethod",
 							oldexif.getAttribute("GPSProcessingMethod"));
 				}
 				if (oldexif.getAttribute("GPSTimeStamp") != null) {
 					newexif.setAttribute("GPSTimeStamp",
 							"" + oldexif.getAttribute("GPSTimeStamp"));
 				}
 			}
 			if (oldexif.getAttribute("DateTime") != null) {
 				newexif.setAttribute("DateTime",
 						oldexif.getAttribute("DateTime"));
 			}
 			if (oldexif.getAttribute("Flash") != null) {
 				newexif.setAttribute("Flash", oldexif.getAttribute("Flash"));
 			}
 			if (oldexif.getAttribute("GPSLatitude") != null) {
 				newexif.setAttribute("GPSLatitude",
 						oldexif.getAttribute("GPSLatitude"));
 			}
 			if (oldexif.getAttribute("GPSLatitudeRef") != null) {
 				newexif.setAttribute("GPSLatitudeRef",
 						oldexif.getAttribute("GPSLatitudeRef"));
 			}
 			if (oldexif.getAttribute("GPSLongitude") != null) {
 				newexif.setAttribute("GPSLongitude",
 						oldexif.getAttribute("GPSLongitude"));
 			}
 			if (oldexif.getAttribute("GPSLatitudeRef") != null) {
 				newexif.setAttribute("GPSLongitudeRef",
 						oldexif.getAttribute("GPSLongitudeRef"));
 			}
 			// Need to update it, with your new height width
 			if ( null != newHeight ) {
 				newexif.setAttribute("ImageLength", Integer.toString(newHeight));
 			}
 			if ( null != newWidth ) {				
 				newexif.setAttribute("ImageWidth", Integer.toString(newWidth));
 			}
 
 			if (oldexif.getAttribute("Make") != null) {
 				newexif.setAttribute("Make", oldexif.getAttribute("Make"));
 			}
 			if (oldexif.getAttribute("Model") != null) {
 				newexif.setAttribute("Model", oldexif.getAttribute("Model"));
 			}
 			if (oldexif.getAttribute("Orientation") != null) {
 				newexif.setAttribute("Orientation",
 						oldexif.getAttribute("Orientation"));
 			}
 			if (oldexif.getAttribute("WhiteBalance") != null) {
 				newexif.setAttribute("WhiteBalance",
 						oldexif.getAttribute("WhiteBalance"));
 			}
 
 			newexif.saveAttributes();
 
 		} catch (Exception e) {
 
 			Log.e(e.getClass().getName(), e.getMessage(), e);
 		}
 
 	}
 
 	public static String getCombinedImagePath(final Context aContext) {
 		return getCombinedImagePath(aContext, PhotoBackWriterApp.pickedImageId);
 	}
 	
 	public static String getFrontImagePath(final Context aContext) {
 		return getFrontImagePath(aContext, PhotoBackWriterApp.pickedImageId);
 	}
 	
 	public static String getBackImagePath(final Context aContext) {
 		return getBackImagePath(aContext, PhotoBackWriterApp.pickedImageId);
 	}
	
	public static String getBackPingPath(final Context aContext) {
		return aContext.getFilesDir().getPath() 
				+ PhotoBackWriterApp.pickedImageId + "-back.png";
	}
 
 	public static String getCombinedImagePath(final Context aContext, final int aImageId) {
 		File ext = Environment.getExternalStorageDirectory();
 		if (!ext.exists()) {
 			return null;
 		}
 		return ext.getPath() 
 				+ "/.photonoter/" + aImageId + ".jpg";
 	}
 	
 	public static String getFrontImagePath(final Context aContext, final int aImageId) {
 		return aContext.getFilesDir().getPath() 
 				+ aImageId + "-front.png";
 	}
 	
	public static String getBackPingPath(final Context aContext, final int aImageId) {
		return aContext.getFilesDir().getPath() 
				+ aImageId + "-back.png";
	}
	
 	public static String getBackImagePath(final Context aContext, final int aImageId) {
 		return aContext.getFilesDir().getPath() 
 				+ aImageId + "-back.jpg";
 	}
 	
 	public static boolean hasNotes(final Context aContext, final int aImageId) {
 		final String frontImage = getFrontImagePath(aContext, aImageId);
 		if ( new File(frontImage).exists() ) {
 			return true;
 		}
 		
 		final String backImage = getBackImagePath(aContext, aImageId);
 		if ( new File(backImage).exists() ) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public static boolean createNonmediaFile() {
 	    String path = Environment.getExternalStorageDirectory().getPath() + "/.photonoter/.nonmedia";
 	    String f = Environment.getExternalStorageDirectory().getPath() + "/.photonoter";
 	    FileOutputStream fos;
 	    try {
 	        File folder = new File(f);
 	        boolean success=false;
 	        if (!folder.exists()) {
 	            success = folder.mkdir();
 	        }
 	        if (true==success) {
 	            File yourFile = new File(path);
 	            if(!yourFile.exists()) {
 	                yourFile.createNewFile();
 	            } 
 	        } else {
 	        	return false;
 	        }
 	        fos = new FileOutputStream(path);
 	        fos.write("NONEMEDIA".getBytes());
 	        fos.close();
         	return true;
 	    } catch (FileNotFoundException e) {
 	        e.printStackTrace();
         	return false;
 	    } catch (IOException e) {
 	        e.printStackTrace();
         	return false;
 	    }
 	}
 	
 }

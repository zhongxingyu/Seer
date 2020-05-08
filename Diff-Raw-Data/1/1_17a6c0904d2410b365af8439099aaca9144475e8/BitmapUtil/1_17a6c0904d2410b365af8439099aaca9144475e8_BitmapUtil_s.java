 package com.example.camerademo.util;
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BitmapUtil {
 	public static final String TAG = "BPUTIL";
 	public static final Bitmap saveAndCreateBitmap(byte[] data, int roatation, Context context, String savedPath, boolean isFrontCam) {
 		try {
             FileOutputStream fos = new FileOutputStream(savedPath);
 //            fos.write(data);
             
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inPurgeable = true;
             options.outWidth = 200;//FIXME: Hard code. 
             options.outHeight = 200;//FIXME: hardcode
             Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
             Bitmap bp = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
 
             bitmap.recycle();
             
             boolean succed = bp.compress(CompressFormat.JPEG, 70, fos);
             fos.flush();
             fos.close();
         } catch (FileNotFoundException e) {
             Log.d(TAG, "File not found: " + e.getMessage());
         } catch (IOException e) {
             Log.d(TAG, "Error accessing file: " + e.getMessage());
         }
         
         Toast.makeText(context, "take pic succed", Toast.LENGTH_LONG).show();
         
         BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
         opts.inPurgeable = true;
         
         Bitmap source;
 		try {
 			source = BitmapFactory.decodeStream(new FileInputStream(savedPath));
 			Matrix m = new Matrix();
 			if (isFrontCam) {
 				m.setValues(new float[] {-1, 0, 0, 
 										 0, 1, 0, 
 										 0, 0, 1});
 			}
 			m.postRotate(roatation);
 			
 			
 			Bitmap out = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, false);
 			return out;
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
         
         //FIXME: need to write the rotate bitmap back to the dest file.
 //        ExifInterface exif = null;
 //		try {
 //			exif = new ExifInterface(savedPath);
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}     //Since API Level 5
 //        
 //		String realOrien = getOrientation();
 //		if (exif != null && realOrien != null) {
 //    		String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 //    		Log.d(TAG, "succed save file to " + pictureFile.getAbsolutePath() + "------------" + exifOrientation);
 //    		exif.setAttribute(ExifInterface.TAG_ORIENTATION, realOrien);
 //    		try {
 //				exif.saveAttributes();
 //				exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 //			} catch (IOException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //    		Log.d(TAG, "change orientation to " + exifOrientation);
 //    	}
 //		else
 //		{
 //			Log.d(TAG, "succed save file to " + pictureFile.getAbsolutePath());
 //		}
 	}
 	
 }

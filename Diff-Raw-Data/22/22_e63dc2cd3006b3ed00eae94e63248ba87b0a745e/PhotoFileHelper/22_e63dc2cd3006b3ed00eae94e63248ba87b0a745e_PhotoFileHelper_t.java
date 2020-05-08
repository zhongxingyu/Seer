 package ua.com.imalur.authographeer.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ua.com.imalur.authographeer.R;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Environment;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 public class PhotoFileHelper {
 
 	public static final String FILENAME_RAW_PREFIX = "RawAuthoPhoto_";
 	public static final String FILENAME_PREFIX = "AuthoPhoto_";
 	
 	public static final String FILENAME_EXTENSION = ".jpg";
 	
 	/*
 	 *   ,    ,  
 	 */
 	public static Uri preparePhotoToSave(String prefix) {
 	    	//      
 	    	//  -    
 	    	String dateTime =  new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
 			String filename = prefix + dateTime + FILENAME_EXTENSION;
			
//			//      ,
//			//    ,   SD 
//	    	File storageFile = new File(
//					Environment.getExternalStoragePublicDirectory(
//							Environment.DIRECTORY_PICTURES),
//					filename);			    	
			File rootsd = Environment.getExternalStorageDirectory();
			File storageFile = new File(rootsd.getAbsolutePath() + "/DCIM/" + filename);
 			return Uri.fromFile(storageFile);							
 		}
 	
 	public static Uri prepareRawPhotoToSave(){
 		return preparePhotoToSave(FILENAME_RAW_PREFIX);
 	}
 	
 	public static Uri prepareDrawingToSave(){
 		return preparePhotoToSave(FILENAME_PREFIX);
 	}
 	
 	/*
 	 *    
 	 */
 	public static void addPhotoToGallery(Context context, Uri uri) {
 		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 		mediaScanIntent.setData(uri);
 	    context.sendBroadcast(mediaScanIntent);
 	}
 	
 	/*
 	 *    canvas    
 	 */
 	public static void saveDrawingToExternalCard(Context context, View view) {
 		//		1.	set Drawing Cache Enabled
 		//		2.	Draw whatever you want
 		//		3.	Get Bitmap object from view
 		//		4.	Compress and save the image file		
 		view.setDrawingCacheEnabled(true);
 		view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
 		Bitmap bitmap = view.getDrawingCache();
 		Uri filename = PhotoFileHelper.prepareDrawingToSave();
 		File file = new File(filename.getEncodedPath());
 		FileOutputStream ostream;
 		try {
 		    file.createNewFile();
 		    ostream = new FileOutputStream(file);
 		    bitmap.compress(CompressFormat.JPEG, 100, ostream);
 		    ostream.flush();
 		    ostream.close();
 		    Toast.makeText(context, R.string.toast_image_saved, 5000).show();
 		} catch (Exception e) {
 		    e.printStackTrace();
 		    Toast.makeText(context, R.string.toast_error_saving_image, 5000).show();
 		}
 		// add drawing to gallery
 		PhotoFileHelper.addPhotoToGallery(context, filename);
 	}
 	
 	/*
 	 *        
 	 */
 	public static Bitmap getScaledBitmapFromFile(String photoPath, int screenWidth, int screenHeight) {
     	if (TextUtils.isEmpty(photoPath)){
     		return null;
     	}
     	Bitmap rotatedBmp =  null;
     	try{
 		// Get the dimensions of the bitmap
 		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
 		bmOptions.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(photoPath, bmOptions);
 		int photoW = Math.min(bmOptions.outWidth, bmOptions.outHeight);
 		int photoH = Math.max(bmOptions.outWidth, bmOptions.outHeight);
 		// Determine how much to scale down the image
 		int scaleFactor = Math.min(photoW/screenWidth, photoH/screenHeight);		
 		// create a matrix for the manipulation
         Matrix matrix = new Matrix();
         // resize the bit map
 //        matrix.postScale(scaleFactor, scaleFactor);
         // rotate the Bitmap if photo in landscape orientation
         if (bmOptions.outWidth > bmOptions.outHeight)
         	matrix.postRotate(90);
 
 		// Decode the image file into a Bitmap sized to fill the View
 		bmOptions.inJustDecodeBounds = false;
 		bmOptions.inSampleSize = scaleFactor;
 		bmOptions.inPurgeable = true;
 		Bitmap originalBitmap =  BitmapFactory.decodeFile(photoPath, bmOptions);		
 //		return originalBitmap;
     	// recreate the new Bitmap
         rotatedBmp =  Bitmap.createBitmap(	originalBitmap, 0, 0,
 							        		bmOptions.outWidth, bmOptions.outHeight, 
 							        		matrix, true);
     	}
     	catch(Exception e){
     		Log.e("",e.getMessage());
     	}
         return rotatedBmp;
     }
 }

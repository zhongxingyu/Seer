 package com.example.maple_android;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.example.browsing.BrowseActivity;
 import com.example.browsing.PersonalAdsActivity;
 import com.example.browsing.PopularAdsActivity;
 import com.facebook.Session;
 
 public class Utility {
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 	
 	/* 
 	 * retrieveBitmap
 	 * --------------
 	 * This will take a Uri and then if possible will return the bitmap saved on disk. This is important because android
 	 * saves the fullsize image from the camera on the SD card. The width and height parameters are the size of the
 	 * retreived bitmap.
 	 */
 	public static Bitmap retrieveBitmap(Uri fileUri, int width, int height) {
 		
         int imageExifOrientation = 0;
         // Samsung Galaxy Note 2 and S III doesn't return the image in the correct orientation, therefore rotate it based on the data held in the exif.
 
         try
         {
 
 		    ExifInterface exif;
 		    exif = new ExifInterface(fileUri.getPath());
 	        imageExifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
 		                                    ExifInterface.ORIENTATION_NORMAL);
 	    }
 	    catch (IOException e1)
 	    {
 	        e1.printStackTrace();
 	    }
 
 	    int rotationAmount = 0;
 
 	    if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
 	    {
 	        // Need to do some rotating here...
 	        rotationAmount = 270;
 	    }
 	    if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
 	    {
 	        // Need to do some rotating here...
 	        rotationAmount = 90;
 	    }
 	    if (imageExifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
 	    {
 	        // Need to do some rotating here...
 	        rotationAmount = 180;
 	    }       
 
 	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
 	    bmOptions.inJustDecodeBounds = true;
 	    BitmapFactory.decodeFile(fileUri.getPath(), bmOptions);
 	    int photoWidth = bmOptions.outWidth;
 	    int photoHeight = bmOptions.outHeight;
 
 	    int scaleFactor = Math.min(photoWidth/width, photoHeight/height);
 
 	    bmOptions.inJustDecodeBounds = false;
 	    bmOptions.inSampleSize = scaleFactor;
 	    bmOptions.inPurgeable = true;
 
 	    Bitmap scaledDownBitmap = BitmapFactory.decodeFile(fileUri.getPath(), bmOptions);
 
 	    if (rotationAmount != 0)
 	    {
 	        Matrix mat = new Matrix();
 	        mat.postRotate(rotationAmount);
 	        scaledDownBitmap = Bitmap.createBitmap(scaledDownBitmap, 0, 0, scaledDownBitmap.getWidth(), scaledDownBitmap.getHeight(), mat, true);
 	    }       
 	    return scaledDownBitmap;
 
 	}
 	
 	/* 
 	 * saveBitmap
 	 * ----------
 	 * This will take a Uri and bitmap and a context and save it to the android SD card in JPEG format
 	 */
 	
 	public static boolean saveBitmap(Uri fileUri, Bitmap bm, Context context) {
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 
 		bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
 		byte[] photoByteArray = stream.toByteArray();
 		OutputStream photoOS;
 
 		try {
 			photoOS = context.getContentResolver().openOutputStream(fileUri);
 			photoOS.write(photoByteArray);
 			photoOS.flush();
 			photoOS.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	
 	public static Uri getOutputMediaFileUri(int type){
         return Uri.fromFile(getOutputMediaFile(type));
     }
 	
 	public static Bitmap getBitmapFromUrl(String src) {
 	    try {
 	        URL url = new URL(src);
 	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 	        connection.setDoInput(true);
 	        connection.connect();
 	        InputStream input = connection.getInputStream();
 	        Bitmap myBitmap = BitmapFactory.decodeStream(input);
 	        return myBitmap;
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	        return null;
 	    }
 	}
 	
 	/** Create a bitmap from a byte array
 	 * 
 	 *@return Returns a bitmap if successful and null otherwise
 	 */
 	public static Bitmap byteArrayToBitmap(byte[] byteArray){
 		if(byteArray == null) return null;
 		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
 	}
 	
 	/** Convert a bitmap to a byte array in PNG format
 	 *@return Returns a byte[] if successful and null otherwise 
 	 */
 	public static byte[] bitmapToByteArray(Bitmap bitmap){
 		if(bitmap == null) return null;
 		
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();        
 		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
         return stream.toByteArray();
 	}
 	
     /** Create a File for saving an image or video */
     public static File getOutputMediaFile(int type){
         // To be safe, you should check that the SDCard is mounted
         // using Environment.getExternalStorageState() before doing this.
 
         File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                   Environment.DIRECTORY_PICTURES), "MapleSyrup");
         // This location works best if you want the created images to be shared
         // between applications and persist after your app has been uninstalled.
 
         // Create the storage directory if it does not exist
         if (! mediaStorageDir.exists()){
             if (! mediaStorageDir.mkdirs()){
                 Log.d("MapleSyrup", "failed to create directory");
                 return null;
             }
         }
 
         // Create a media file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         File mediaFile;
         if (type == MEDIA_TYPE_IMAGE){
             mediaFile = new File(mediaStorageDir.getPath() + File.separator +
             "IMG_"+ timeStamp + ".jpg");
         } else if(type == MEDIA_TYPE_VIDEO) {
             mediaFile = new File(mediaStorageDir.getPath() + File.separator +
             "VID_"+ timeStamp + ".mp4");
         } else {
             return null;
         }
 
         return mediaFile;
     }
     
 	/**
 	 * Logs the user out
 	 */
 	public static void onClickLogout(Activity activity) {
 		Session session = Session.getActiveSession();
 		if (session != null && !session.isClosed()) {
 			session.closeAndClearTokenInformation();
 		}
 		Intent i = new Intent(activity, LoginActivity.class);
 		activity.startActivity(i);
 	}
 	
     public static boolean myOnOptionsItemSelected(final Activity activity, MenuItem item) {
     	Intent intent = null;
     	// respond to menu item selection
     	switch (item.getItemId()) {
 		case R.id.logout:
 			onClickLogout(activity);
 			return true;
 		case R.id.new_ad:
 			// TODO: This is pretty bad to assume BrowseActivity but we need for passing the fileUri
 			// of the photo. There are ways to do it via android, but it's very buggy and only works on some
 			// phones. We should just figure out a better way to handle it.
 			AdCreationDialog dialog = new AdCreationDialog((BrowseActivity) activity);
 			dialog.show();
 			return true;
 		case R.id.personal:
 			intent = new Intent(activity, PersonalAdsActivity.class);
 			activity.startActivity(intent);
 			return true;
 		case R.id.popular:
 			intent = new Intent(activity, PopularAdsActivity.class);
 			activity.startActivity(intent);
 			return true;
 		default:
			return activity.onOptionsItemSelected(item);
 		}
     }
     
     public static void createHelpDialog(final Activity activity, final String message, final String title) {
     	// 1. Instantiate an AlertDialog.Builder with its constructor
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 
 		// 2. Chain together various setter methods to set the dialog characteristics
 		builder.setMessage(message)
 		       .setTitle(title);
 		
 		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) { /* User cancelled the dialog */ }
 	    });
 		
 		// 3. Get the AlertDialog from create()
 		AlertDialog dialog = builder.create();
 		dialog.show();
     }
 }

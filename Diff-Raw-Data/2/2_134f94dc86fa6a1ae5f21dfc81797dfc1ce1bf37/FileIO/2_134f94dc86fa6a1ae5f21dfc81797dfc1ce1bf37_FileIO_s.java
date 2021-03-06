 /*    Catroid: An on-device graphical programming language for Android devices
  *    Copyright (C) 2010  Catroid development team
  *    (<http://code.google.com/p/catroid/wiki/Credits>)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.tugraz.ist.paintroid;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.media.MediaScannerConnection;
 import android.media.MediaScannerConnection.MediaScannerConnectionClient;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 
 /**
  * Helper class for saving an image to the sdcard.
  * 
  * Status: refactored 04.02.2011
  * @author PaintroidTeam
  * @version 6.0b
  */
 public class FileIO {
 
 	private Context callerContext;
 	
 	private final String paintroidImagesFolder = "/Paintroid/";
 	
 	FileIO(Context context){
 		callerContext = context;
 	}
 	
 	/**
 	 * This class is responsible for reloading the media gallery
 	 * after we added a new file. A new instance should be created
 	 * before adding a new file.
 	 */
 	private static class MediaScannerNotifier implements 
 	MediaScannerConnectionClient { 
 	    private MediaScannerConnection mConnection; 
 	    private String mPath; 
 	    private String mMimeType; 
 
 	    public MediaScannerNotifier(Context context, String path, String mimeType) { 
 	        mPath = path; 
 	        mMimeType = mimeType; 
 	        mConnection = new MediaScannerConnection(context, this); 
 	        mConnection.connect(); 
 	    } 
 
 	    public void onMediaScannerConnected() { 
 	    	Log.d("PAINTROID", "onMediaScannerConnected");
 	        mConnection.scanFile(mPath, mMimeType); 
 	    } 
 
 	    public void onScanCompleted(String path, Uri uri) { 
 	    	if(uri == null)
 	    	{
 	    		Log.d("PAINTROID", "onScanCompleted failed");
 	    	}
 	    	else
 	    	{
 	    		Log.d("PAINTROID", "onScanCompleted successful");
 	    	}
 	    	mConnection.disconnect();
 	    } 
 	}
 	
     /** 
      * Get the real path from an Android gallery-URI. This is a static
      * method that can be called without creating an instance of this class.
      *
      * @param cr           ContentResolver from the calling activity 
      * @param contentUri   Gallery-URI to convert into real path
      * @return             real Path as a STring
      */
     static public String getRealPathFromURI(ContentResolver cr, Uri contentUri) {
 
     	String[] data = { MediaStore.Images.Media.DATA };
         Cursor cursor = cr.query(contentUri, data, null, null, null);
         
         int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         String realPath = cursor.getString(columnIndex);
         
         return realPath;
     }
     
     
     /** 
      * Saves a Bitmap to a file in the standard pictures folder on the sdcard.
      *
      * @param cr          ContentResolver from the calling activity 
      * @param save_name   Save name for the bitmap
      * @param bitmap      Bitmap to save
      * 
      * @return            0 on success, otherwise -1
      */
 	 public Uri saveBitmapToSDCard(ContentResolver cr, String savename, Bitmap bitmap){
 				
 		// checking whether media (sdcard) is available
 		boolean mExternalStorageAvailable = false;
 		boolean mExternalStorageWriteable = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 		    // We can read and write the media
 		    mExternalStorageAvailable = mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 		    // We can only read the media
 		    mExternalStorageAvailable = true;
 		    mExternalStorageWriteable = false;
 		} else {
 		    // Something else is wrong. It may be one of many other states, but all we need
 		    //  to know is we can neither read nor write
 		    mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 		if(!mExternalStorageAvailable || !mExternalStorageWriteable) {
 			Log.d("PAINTROID", "Error: SDCard not available!");
 			return null;
 		}
 		
 		String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();
 	
 		String paintroidImagesDirectory = externalStorageDirectory + paintroidImagesFolder;
 		
 		File newPaintroidImagesDirectory = new File(paintroidImagesDirectory);
 		
 		if(!newPaintroidImagesDirectory.mkdirs())
 		{
 			Log.d("PAINTROID", "Error: Could not create directory structure to save picture.");
 		}
 		
 		File outputFile = new File(newPaintroidImagesDirectory, savename + ".png");
 	
 		try {
 			FileOutputStream out = new FileOutputStream(outputFile);		
 			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
 			  
 			 out.flush();
 			 out.close();
 			 Log.d("PAINTROID", "FileIO: Bitmap saved with name: " + savename);
 		} catch (FileNotFoundException e) {
 			Log.d("PAINTROID", "FileNotFoundException: " + e);
 			return null;
 		} catch (IOException e) {
 			Log.d("PAINTROID", "FileNotFoundException: " + e);
 			return null;
 		}
 		
 		// Add new file to the media gallery
 		new MediaScannerNotifier(callerContext, outputFile.getAbsolutePath(), null);
 		
 		return Uri.fromFile(outputFile);
 	}		
 }

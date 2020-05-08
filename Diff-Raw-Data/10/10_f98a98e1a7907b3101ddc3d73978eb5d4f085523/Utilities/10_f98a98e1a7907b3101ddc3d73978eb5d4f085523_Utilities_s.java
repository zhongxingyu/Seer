 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package ca.ualberta.cs.c301f13t13.backend;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.Settings;
 import android.util.Base64;
 
 /**
  * Class meant for general functions that can be used by any class.
  * 
  * @author Stephanie Gil
  * 
  */
 public class Utilities {
 
 	/**
 	 * Takes an array of objects and converts them all to Stories.
 	 * 
 	 * @param objects
 	 * @return
 	 */
 	public static ArrayList<Story> objectsToStories(ArrayList<Object> objects) {
 		ArrayList<Story> stories = new ArrayList<Story>();
 
 		for (Object obj : objects) {
 			stories.add((Story) obj);
 		}
 
 		return stories;
 	}
 
 	/**
 	 * Takes an array of objects and converts them all to Chapters.
 	 * 
 	 * @param objects
 	 * @return
 	 */
 	public static ArrayList<Chapter> objectsToChapters(ArrayList<Object> objects) {
 		ArrayList<Chapter> chapters = new ArrayList<Chapter>();
 
 		for (Object obj : objects) {
 			chapters.add((Chapter) obj);
 		}
 
 		return chapters;
 	}
 
 	/**
 	 * Takes an array of objects and converts them all to Choice objects.
 	 * 
 	 * @param objects
 	 * @return
 	 */
 	public static ArrayList<Choice> objectsToChoices(ArrayList<Object> objects) {
 		ArrayList<Choice> choices = new ArrayList<Choice>();
 
 		for (Object obj : objects) {
 			choices.add((Choice) obj);
 		}
 
 		return choices;
 	}
 
 	/**
 	 * Takes an array of objects and converts them all to Media objects.
 	 * 
 	 * @param objects
 	 * @return
 	 */
 	public static ArrayList<Media> objectsToMedia(ArrayList<Object> objects) {
 		ArrayList<Media> medias = new ArrayList<Media>();
 
 		for (Object obj : objects) {
 			medias.add((Media) obj);
 		}
 
 		return medias;
 	}
 
 	/**
 	 * Creates a new File to put an image in and a path and Uri to it.
 	 * 
 	 * CODE REUSE:
 	 * CameraTest demo code from CMPUT 301 Lab. 
 	 * 
 	 * Date: Nov. 2, 2013
 	 * 
 	 * @author Abram Hindle
 	 * @author Chenlei Zhang
 	 */
 	public static Uri createImageUri() {
 		Uri imageFileUri;
 		String folder = Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		String imageFilePath = folder + "/"
 				+ String.valueOf(System.currentTimeMillis()) + "jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		return imageFileUri;
 	}
 
 	/**
 	 * This functions converts Bitmap picture to a string which can be
 	 * JSONified.
 	 * 
 	 * CODE REUSE:
 	 * This code is taken straight from:
 	 * 
 	 * URL: http://mobile.cs.fsu.edu/converting-images-to-json-objects/
 	 * Date: Nov. 4th, 2013
 	 * Author: Manav
 	 */
 	public static String getStringFromBitmap(Bitmap bitmapPicture) {
 		final int COMPRESSION_QUALITY = 100;
 		String encodedImage;
 		ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
		bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
 				byteArrayBitmapStream);
 		byte[] b = byteArrayBitmapStream.toByteArray();
 		encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
 		return encodedImage;
 	}	
 
 	/**
 	 * This functions converts a string to a Bitmap picture.
 	 * 
 	 * CODE REUSE:
 	 * This code is taken straight from:
 	 * 
 	 * URL: http://mobile.cs.fsu.edu/converting-images-to-json-objects/
 	 * Date: Nov. 4th, 2013
 	 * Author: Manav
 	 */
 	public static Bitmap getBitmapFromString(String string) {
 
 		byte[] decodedString = Base64.decode(string, Base64.DEFAULT);
 		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
 		return decodedByte;
 	}
 
 	/**
 	 * This functions gets the id of the device and returns it as a string
 	 * 
 	 * CODE REUSE:
 	 * This code is modified from:
 	 * 
 	 * URL: http://developer.samsung.com/android/technical-docs/How-to-retrieve-the-Device-Unique-ID-from-android-device
 	 * Date: Nov. 5th, 2013
 	 * 
 	 * 
 	 */
 	public static String getPhoneId(Context context) {
 		String PhoneId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
 		return PhoneId;
 
 	}
 	
 	/**
 	 * Calculates size for bitmap.
 	 * 
 	 * CODE REUSE
 	 * URL: http://android-er.blogspot.ca/2012/07/implement-gallery-like.html
 	 * Date: Nov. 7, 2013
 	 * Author: Andr.oid Eric
 	 */		
 	public static int calculateInSampleSize(BitmapFactory.Options options, 
 			int reqWidth, int reqHeight) {
 		// Raw height and width of image
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		if (height > reqHeight || width > reqWidth) {
 			if (width > height) {
 				inSampleSize = Math.round((float)height / (float)reqHeight);   
 			} else {
 				inSampleSize = Math.round((float)width / (float)reqWidth);   
 			}   
 		}
 
 		return inSampleSize;   
 	}	
 	
 	/**
 	 * CODE REUSE
 	 * URL: http://android-er.blogspot.ca/2012/07/implement-gallery-like.html
 	 * Date: Nov. 7, 2013
 	 * Author: Andr.oid Eric
 	 */	
 	public static Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth, int reqHeight) {
 		Bitmap bm = null;
 
 		// First decode with inJustDecodeBounds=true to check dimensions
 		final BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(uri.getPath(), options);
 
 		// Calculate inSampleSize
 		options.inSampleSize = Utilities.calculateInSampleSize(options, reqWidth, reqHeight);
 
 		// Decode bitmap with inSampleSize set
 		options.inJustDecodeBounds = false;
 		bm = BitmapFactory.decodeFile(uri.getPath(), options); 
 
 		return bm;  
 	}	
 }

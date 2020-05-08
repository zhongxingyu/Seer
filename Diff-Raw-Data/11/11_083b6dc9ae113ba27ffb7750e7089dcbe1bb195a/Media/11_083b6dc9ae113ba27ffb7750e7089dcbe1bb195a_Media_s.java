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
 
 package ca.ualberta.cmput301f13t13.storyhoard.dataClasses;
 
 import java.io.ByteArrayOutputStream;
 import java.util.UUID;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 
 /**
  * Role: A data class to hold media information. The media can be a photo or
  * an illustration. 
  * 
  * @author Stephanie Gil
  * @author Ashley Brown
  * 
  */
 public class Media {
 	private UUID id;
 	private UUID chapterId;
 	private String path;
 	private String type;
 	private String bitmapString;
 	private String text;
 
 	// CONSTANTS
 	public static final String PHOTO = "photo";
 	public static final String ILLUSTRATION = "illustration";
 
 	/**
 	 * Initializes a new Media object without an id.
 	 * 
 	 * @param chapterId
 	 * @param path
 	 * @param type
 	 * @param text TODO
 	 */
 	public Media(UUID chapterId, String path, String type, String text) {
 		this.id = UUID.randomUUID();
 		this.chapterId = chapterId;
 		this.type = type;
 		this.text = text;
 		bitmapString = "";
 		if (path != null) {
 			this.path = path;
 		} else {
 			this.path = "";
 		}
 	}
 
 	/**
 	 * Initializes a new Media object with an id, used for making a search
 	 * criteria media object or for making a media object from the database
 	 * data.
 	 * 
 	 * @param id
 	 * @param chapterId
 	 * @param path
 	 * @param type
 	 * @param text TODO
 	 */
 	public Media(UUID id, UUID chapterId, String path, String type, String text) {
 		this.id = id;
 		this.chapterId = chapterId;
 		this.type = type;
 		this.text = text;
 		bitmapString = "";
 		if (path != null) {
 			this.path = path;		
 		} else {
 			this.path = "";	
 		}
 	}
 
 	// GETTERS
 
 	/**
 	 * Returns the id of the media.
 	 * 
 	 * @return id
 	 */
 	public UUID getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the chapter id the media belongs to.
 	 * 
 	 * @return chapterId
 	 */
 	public UUID getChapterId() {
 		return this.chapterId;
 	}
 
 	/**
 	 * Returns the path of the media.
 	 * 
 	 * @return path
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * Returns the media's type (PHOTO, ILLUSTRATION, AUDIO, or VIDEO).
 	 * 
 	 * @return type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * Returns the media's bitmap.
 	 * 
 	 * @return bitmap
 	 */
 	public Bitmap getBitmap() {
		return BitmapFactory.decodeFile(path);
 	}
 	
 	/**
 	 * Returns the media's bitmap as an encoded String
 	 * 
 	 * @return bitmap string
 	 */
 	public String getBitmapString() {
 		return bitmapString;
 	}	
 	
 	/**
 	 * Returns the media's bitmap, but does so by converting the bitmap
 	 * from string to bitmap.
 	 * 
 	 * @return bitmap 
 	 */
 	public Bitmap getBitmapFromString() {
 		return getBitmapFromString(bitmapString);
 	}	
 
 	public String getText() {
 		return text;
 	}
 	// SETTERS
 
 	/**
 	 * Sets the media's id.
 	 * 
 	 * @param id
 	 */
 	public void setId(UUID id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets the chapter id the media belongs to.
 	 * 
 	 * @param chapterId
 	 */
 	public void setChapterId(UUID chapterId) {
 		this.chapterId = chapterId;
 	}
 
 	/**
 	 * Sets the path of the media. Also updates the bitmap.
 	 * 
 	 * @param path
 	 */
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	/**
 	 * Sets the media's bitmapString from a bitmap.
 	 * 
 	 * @param bitmap
 	 */
 	public void setBitmapString(Bitmap bitmap) {
 		this.bitmapString = getStringFromBitmap(bitmap);
 	}
 	
 	/**
 	 * Sets the media's type.
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public void setText(String text) {
 		this.text = text;
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
 	private String getStringFromBitmap(Bitmap bitmapPicture) {
 		final int COMPRESSION_QUALITY = 100;
 		ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
 		bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 
 				COMPRESSION_QUALITY, byteArrayBitmapStream);
 		byte[] b = byteArrayBitmapStream.toByteArray();
 		return Base64.encodeToString(b, Base64.DEFAULT);
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
 	private Bitmap getBitmapFromString(String string) {
 		byte[] decodedString = Base64.decode(string, Base64.DEFAULT);
 		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 
 				0, decodedString.length);
 		return decodedByte;
 	}
 }

 /*
 Adventure App - Allows you to create an Adventure Book, or Download
 	books from other authors.
 Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.uofa.adventure_app.model;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 
 import com.uofa.adventure_app.enums.MediaType;
 import com.uofa.adventure_app.interfaces.UniqueId;
 
 public class Media extends UniqueId implements Serializable {
 
 	private String image;
    private static final int IMAGE_MAX = 200;
 	// private MediaType mediaType;
 
 	public Media( String image) {
 
 		this.image = image;
 	}
 	public Media(){
 		this.image = null;
 	}
 
 	public void setMedia(String image) {
 		this.image = image;
 	}
 
 	public String getMedia() {
 		return this.image;
 	}
     /**
      * Rescales an image to set the height to match the max image height.
      * 
      * @param bitmap
      *            bitmap to be rescaled.
      * @return rescaled bitmap.
      */
     public static Bitmap resizeImage(Bitmap bitmap) {
            float width = bitmap.getWidth();
             float height = bitmap.getHeight();
             float scale = 1;
 
             scale = IMAGE_MAX / height;
 
             float newWidth = width * scale;
             float newHeight = height * scale;
             int newWidthInt = (int) newWidth;
             int newHeightInt = (int) newHeight;
             Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidthInt,
                             newHeightInt, false);
             return resizedBitmap;
     }
 	/**
 	 * Always treat de-serialization as a full-blown constructor, by validating
 	 * the final state of the de-serialized object.
 	 */
 	private void readObject(ObjectInputStream aInputStream)
 			throws ClassNotFoundException, IOException {
 		// always perform the default de-serialization first
 		aInputStream.defaultReadObject();
 
 	}
 
 	/**
 	 * This is the default implementation of writeObject. Customise if
 	 * necessary.
 	 */
 	private void writeObject(ObjectOutputStream aOutputStream)
 			throws IOException {
 		// perform the default serialization for all non-transient, non-static
 		// fields
 		aOutputStream.defaultWriteObject();
 	}
 
 	public Media localCopy() {
 		Media newMedia = new Media();
 		newMedia.setMedia(this.image);
 		return newMedia;
 	}
     /**
      * Encodes bitmap into base64 string.
      * 
      * @param bm
      *            bitmap to be encoded.
      * @return encoded string.
      */
     public static  String encodeToBase64(Bitmap bm) {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
             byte[] bArray = baos.toByteArray();
             String imageEncoded = Base64.encodeToString(bArray, Base64.DEFAULT);
             return imageEncoded;
     }
     /**
      * Decodes string into bitmap
      * 
      * @param bArray
      *            encoded string.
      * @return decoded bitmap.
      */
     public static Bitmap decodeBase64(String bArray) {
             byte[] decodedByte = Base64.decode(bArray, 0);
             return BitmapFactory
                             .decodeByteArray(decodedByte, 0, decodedByte.length);
     }
 
 }

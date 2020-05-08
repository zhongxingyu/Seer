 package org.ivan.simple;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.SparseArray;
 
 public class ImageProvider {
 	public static Resources resources;
 	private static SparseArray<Bitmap> images = new SparseArray<Bitmap>();
 
 	private ImageProvider() {
 	}
 	
 	public static Bitmap getBitmap(int id) {
 		if(images.get(id) == null) {
 			images.put(id, BitmapFactory.decodeResource(resources, id));
 		}
 		return images.get(id);
 	}
 
 }

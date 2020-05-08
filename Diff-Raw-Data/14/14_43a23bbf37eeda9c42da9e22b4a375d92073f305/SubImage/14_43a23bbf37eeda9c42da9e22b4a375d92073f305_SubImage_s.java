 package com.quimian.setalyzer.util;
 
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 
 public class SubImage {
 	float[] dest = {
 			0, 0,
 			100, 0,
 			100, 150,
 			0, 150
 	};
 
 	Bitmap image;
 	public SubImage(Bitmap base) {
 		this.image = base;
 	}
 	
 	public Bitmap getSubImage(float[] roi) {
 		// Rotate 90 degrees if needed.
 		double first = Math.sqrt(Math.pow(roi[0] - roi[2], 2) + Math.pow(roi[1] - roi[3], 2));
 		double second = Math.sqrt(Math.pow(roi[2] - roi[4], 2) + Math.pow(roi[3] - roi[5], 2));
 		if (second < first) {
 			float f = roi[0];
 			float g = roi[1];
 			for (int i = 2; i < 8; i++) {
 				roi[i - 2] = roi[i];
 			}
 			roi[6] = f;
 			roi[7] = g;
 		}
 		
 		Matrix transform = new Matrix();
 		transform.setPolyToPoly(roi, 0, dest, 0, 4);
 
 		return Bitmap.createBitmap(image, 0, 0, 100, 150, transform, true);
 	}
 }

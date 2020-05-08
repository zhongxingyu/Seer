 package com.girarda.sudosolve.sudograb;
 
 import org.opencv.android.Utils;
 import org.opencv.core.Mat;
 import org.opencv.imgproc.Imgproc;
 
 import android.graphics.Bitmap;
 
 public class SudokuGrabber {
 	
 	private Bitmap originalImg;
 	private Mat matrix = new Mat();
 	
 	public SudokuGrabber(Bitmap bitmapImg) {
 		originalImg = bitmapImg;
 		bitmapToMatrix(originalImg, matrix);
 	}
 	
     private void bitmapToMatrix(Bitmap bitmapImg, Mat matrix) {
     	Utils.bitmapToMat(bitmapImg, matrix);
     }
     
     private void matrixToBitmap(Mat matrix, Bitmap bitmap) {
     	Mat result = new Mat();
     	Imgproc.cvtColor(matrix, result, Imgproc.COLOR_RGB2BGRA);
     	Utils.matToBitmap(result, bitmap);
     }
     
     public Bitmap getConvertedResult() {
    	matrixToBitmap(matrix, originalImg);
     	return originalImg;
     }
 
 }

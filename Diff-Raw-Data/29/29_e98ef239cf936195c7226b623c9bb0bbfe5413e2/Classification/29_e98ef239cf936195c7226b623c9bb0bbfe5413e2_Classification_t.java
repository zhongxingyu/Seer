 package com.duckie;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.opencv.core.*;
 import org.opencv.imgproc.Imgproc;
 import org.opencv.imgproc.Moments;
 
import android.util.Log;

 public class Classification {	
 	
 	public static String classify(Mat src, Mat object) {
 		// Find height of frame and hand
 		int imgHeight = src.rows();
 		int handHeight = object.rows();
		
		if (object.size().area() == 0)
			return "Input Hand";
		else if (isHeightClass(imgHeight, handHeight)) {
 			return "Height";
 		}
 		else {
 			if (isCentroidClass(object)){
 				return "Centroid";
 			}
 			else
 				return "Width";
 		}
 	}
 
 	Mat furtherClean(Mat image) {
 		Mat mask = image.clone();
 		Mat cleaned = Mat.zeros(image.size(), CvType.CV_8UC3);
 		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2HSV);
 		Core.inRange(mask, new Scalar(90, 50, 50), new Scalar(160, 255, 255), mask);
 		Imgproc.threshold(mask, mask, 0, 255, Imgproc.THRESH_BINARY_INV);
 		image.copyTo(cleaned, mask);
 		return cleaned;
 	}
 
 	public static boolean isHeightClass(int imgHeight, int handHeight) {
 		float thresh = 0.8f;
 		if ((float) handHeight / imgHeight > thresh)
 			return true;
 		else
 			return false;
 	}
 
 	public static boolean isCentroidClass(Mat image) {
 		Mat binary = new Mat();
 		// Find contours from binary image
 		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
 		Mat hierarchy = new Mat();
 		int handWidth = image.cols();
 
 		// Convert to B&W image
 		Imgproc.threshold(image, binary, 0, 255, Imgproc.THRESH_BINARY);
 
 		// Convert to binary image, 1 channel
 		Imgproc.cvtColor(binary, binary, Imgproc.COLOR_RGB2GRAY);
 
 		// Find contour
 		Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE,
 				Imgproc.CHAIN_APPROX_SIMPLE);
 
 		// Get the moment
 		Moments mu = new Moments();
 		Iterator<MatOfPoint> each = contours.iterator();
 		int ctr = 0;
 		while (each.hasNext()){
 			MatOfPoint wrapper = each.next();
 			if (ctr==0)
 				mu = Imgproc.moments(wrapper, false);
 			ctr++;
 		}
 		
 		//  Get the centroid or mass center
 		Point mc;
 		mc =  new Point(mu.get_m10() / mu.get_m00(), mu.get_m01() / mu.get_m00());
 
 //		// Plot centroid
 //		Core.circle(image, mc, 4, new Scalar(0, 255, 0), -1, 8, 0);
 	
 		if ((float) mc.x / handWidth < 0.55)
 			return true;
 		else
 			return false;
 	}
 }

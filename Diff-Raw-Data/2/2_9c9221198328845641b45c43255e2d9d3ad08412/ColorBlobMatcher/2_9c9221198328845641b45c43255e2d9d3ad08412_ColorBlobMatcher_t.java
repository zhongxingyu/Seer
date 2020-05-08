 package com.android;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint;
 import org.opencv.core.MatOfPoint2f;
 import org.opencv.core.Point;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.imgproc.Imgproc;
 
 import android.util.Log;
 
 public class ColorBlobMatcher {
 	
 	
 	
 	
     // Lower and Upper bounds for range checking in HSV color space
     private Scalar mLowerBound = new Scalar(0);
     private Scalar mUpperBound = new Scalar(0);
     // Minimum contour area in percent for contours filtering
     private static double mMinContourArea = 0.1;
     // Color radius for range checking in HSV color space
     private Scalar mColorRadius = new Scalar(25,50,50,0);
     private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
 
     // Cache
     Mat mPyrDownMat = new Mat();
     Mat mHsvMat = new Mat();
     Mat mMask = new Mat();
     Mat mDilatedMask = new Mat();
     Mat mHierarchy = new Mat();
	public static  double SHAPE_DIFF_THRESHOLD = 0.15d;
 	public static double DESC_DIFF_THRESHOLD = (3.0d*DescriptorDataset.NORMALIZATION_MAX*(double)DescriptorHandler.HISTOGRAM_BINS)*0.03d;
 	
 
     public void setColorRadius(Scalar radius) {
         mColorRadius = radius;
     }
 
     public void setHsvColor(Scalar hsvColor) {
         double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
         double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;
 
         mLowerBound.val[0] = minH;
         mUpperBound.val[0] = maxH;
 
         mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
         mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];
 
         mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
         mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];
 
         mLowerBound.val[3] = 0;
         mUpperBound.val[3] = 255;
 
     }
 
 
     public void setMinContourArea(double area) {
         mMinContourArea = area;
     }
 
     public void process(Mat rgbaImage) {
     	
     	
         Imgproc.pyrDown(rgbaImage, mPyrDownMat);
         Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
         Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
 
         Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
         Imgproc.dilate(mMask, mDilatedMask, new Mat());
 
         List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
 
         Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
 
         // Find max contour area
 
         Iterator<MatOfPoint> each = contours.iterator();
 
         mContours.clear();
         
 //    	Log.d(ColorBlobMatcher.class.getSimpleName(), "contours:"+contours.size());
         
         while (each.hasNext())
         {
             MatOfPoint contour = each.next();
             Core.multiply(contour, new Scalar(4,4), contour);
             
             Rect boundingRect = Imgproc.boundingRect(contour);
             Mat subimg = rgbaImage.submat(boundingRect);
 //            Mat subimghsv = new Mat();
 //        	Imgproc.cvtColor(subimg, subimghsv, Imgproc.COLOR_RGB2HSV_FULL);
 //
 //        	Scalar blobColorHsv = Core.sumElems(subimghsv);
 //        	int pointCount = boundingRect.width * boundingRect.height;
 //        	for (int i = 0; i < blobColorHsv.val.length; i++)
 //        		blobColorHsv.val[i] /= pointCount;
             	
             
             double maxArea = rgbaImage.width()*rgbaImage.height();
             
         	Descriptor a = DescriptorHandler.createDescriptor(subimg, contour, null);
 
             double area = Imgproc.contourArea(contour);
             if ( area > maxArea*0.01 &&  area < maxArea*0.8 )
             {
                 for ( Descriptor d : DescriptorDataset.training)
                 {
                 	double match = Imgproc.matchShapes(contour, d.contour, Imgproc.CV_CONTOURS_MATCH_I3, 0);
                 	
                 	if ( match < SHAPE_DIFF_THRESHOLD  )
                 	{
 //                		Log.d(ColorBlobMatcher.class.getSimpleName(), "contourMatch:"+match);
                 		
                     	double dist = DescriptorDataset.getMinDist(a);
                     	
 //                    	Log.d(ColorBlobMatcher.class.getSimpleName(), "Descriptor diff: "+dist+ " max: "+DESC_DIFF_THRESHOLD);
                     	
                     	if (dist < DESC_DIFF_THRESHOLD) {
                     		mContours.add(contour);
                     	}
                 		
                 	}
                 }
             }
         }
     }
     
 
     public List<MatOfPoint> getContours() {
         return mContours;
     }
 }

 package com.oryzone.mvdetector.differenceStrategy;
 
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public interface IDifferenceStrategy
 {
 
     public int calculateDifference(IplImage prevImage, IplImage currImage);
 
     public int getDifferenceAmount();
 
     public float getDifferencePercent();
 
     public IplImage getDifferenceImage();
 }

 package com.oryzone.mvdetector.differenceStrategy;
 
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
/**
 * Interface that defines the methods of the difference strategy classes.
 * Difference strategy classes are class that should provide a methods
 * to calculate the difference betwen two image of the same size, and
 * also provide numerical values to represent the amount of difference.
 * @author Luciano Mammino<lmammino@oryzone.com>
 * @author Andrea Mangano<amangano@oryzone.com>
 * @version 1.0
 */
 public interface IDifferenceStrategy
 {
 
    /**
     * 
     * @param prevImage
     * @param currImage
     * @return
     */
     public int calculateDifference(IplImage prevImage, IplImage currImage);
 
     public int getDifferenceAmount();
 
     public float getDifferencePercent();
 
     public IplImage getDifferenceImage();
 }

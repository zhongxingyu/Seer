 package com.innovatrics.iseglib;
 
 /**
  * Contains a segmented fingerprint.
  * @author Martin Vysny
  */
 public class SegmentedFingerprint {
     /**
     *  Detected finger will be stored (sequence going from left to right). Image will be stored here as uncompressed raw image, the size of this array will be equal to outWidth * outHeight
      */
     public RawImage rawImage;
     /**
      * Contains coordinates of rectangle where detected finger is lying.
      */
     public Rect roundingBox;
 }

 /*
  * Copyright 2007 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.zxing.common;
 
 import com.google.zxing.MonochromeBitmapSource;
 import com.google.zxing.ReaderException;
 
 /**
  * Implementations of this class can, given locations of finder patterns for a QR code in an
  * image, sample the right points in the image to reconstruct the QR code, accounting for
  * perspective distortion. It is abstracted since it is relatively expensive and should be allowed
  * to take advantage of platform-specific optimized implementations, like Sun's Java Advanced
  * Imaging library, but which may not be available in other environments such as J2ME, and vice
  * versa.
  *
  * The implementation used can be controlled by calling {@link #setGridSampler(GridSampler)}
  * with an instance of a class which implements this interface.
  *
  * @author srowen@google.com (Sean Owen)
  */
 public abstract class GridSampler {
 
   private static GridSampler gridSampler;
 
   /**
    * Sets the implementation of {@link GridSampler} used by the library. One global
    * instance is stored, which may sound problematic. But, the implementation provided
    * ought to be appropriate for the entire platform, and all uses of this library
    * in the whole lifetime of the JVM. For instance, an Android activity can swap in
    * an implementation that takes advantage of native platform libraries.
    * 
    * @param newGridSampler
    */
   public static void setGridSampler(GridSampler newGridSampler) {
     if (newGridSampler == null) {
       throw new IllegalArgumentException();
     }
     gridSampler = newGridSampler;
   }
 
   /**
    * @return the current implementation of {@link GridSampler}
    */
   public static GridSampler getInstance() {
     if (gridSampler == null) {
       gridSampler = new DefaultGridSampler();
     }
     return gridSampler;
   }
 
   /**
    * <p>Samples an image for a square matrix of bits of the given dimension. This is used to extract the
    * black/white modules of a 2D barcode like a QR Code found in an image. Because this barcode may be
    * rotated or perspective-distorted, the caller supplies four points in the source image that define
    * known points in the barcode, so that the image may be sampled appropriately.</p>
    *
    * <p>The last eight "from" parameters are four X/Y coordinate pairs of locations of points in
    * the image that define some significant points in the image to be sample. For example,
    * these may be the location of finder pattern in a QR Code.</p>
    *
    * <p>The first eight "to" parameters are four X/Y coordinate pairs measured in the destination
    * {@link BitMatrix}, from the top left, where the known points in the image given by the "from" parameters
    * map to.</p>
    *
    * <p>These 16 parameters define the transformation needed to sample the image.</p>
    *
    * @param image image to sample
    * @param dimension width/height of {@link BitMatrix} to sample from iamge
    * @return {@link BitMatrix} representing a grid of points sampled from the image within a region
    *  defined by the "from" parameters
    * @throws ReaderException if image can't be sampled, for example, if the transformation defined by
    *  the given points is invalid or results in sampling outside the image boundaries
    */
   public abstract BitMatrix sampleGrid(MonochromeBitmapSource image,
                                        int dimension,
                                        float p1ToX, float p1ToY,
                                        float p2ToX, float p2ToY,
                                        float p3ToX, float p3ToY,
                                        float p4ToX, float p4ToY,
                                        float p1FromX, float p1FromY,
                                        float p2FromX, float p2FromY,
                                        float p3FromX, float p3FromY,
                                        float p4FromX, float p4FromY) throws ReaderException;
 
   /**
    * <p>Checks a set of points that have been transformed to sample points on an image against
    * the image's dimensions to see if the endpoints are even within the image.
    * This method actually only checks the endpoints since the points are assumed to lie
    * on a line.</p>
    *
    * <p>This method will actually "nudge" the endpoints back onto the image if they are found to be barely
    * (less than 1 pixel) off the image. This accounts for imperfect detection of finder patterns in an image
    * where the QR Code runs all the way to the image border.</p>
    *
    * @param image image into which the points should map
    * @param points actual points in x1,y1,...,xn,yn form
    * @throws ReaderException if an endpoint is lies outside the image boundaries
    */
   protected static void checkEndpoint(MonochromeBitmapSource image, float[] points) throws ReaderException {
     int width = image.getWidth();
     int height = image.getHeight();
    checkOneEndpoint(points, (int) points[0], (int) points[1], width, height);
    checkOneEndpoint(points, (int) points[points.length - 2], (int) points[points.length - 1], width, height);
   }
 
  private static void checkOneEndpoint(float[] points, int x, int y, int width, int height) throws ReaderException {
     if (x < -1 || x > width || y < -1 || y > height) {
       throw new ReaderException("Transformed point out of bounds at " + x + ',' + y);
     }
     if (x == -1) {
      points[0] = 0.0f;
     }
     if (y == -1) {
      points[1] = 0.0f;
    }
    if (x == width) {
      points[0] = width - 1;
    }
    if (y == height) {
      points[1] = height - 1;
     }
   }
 
 }

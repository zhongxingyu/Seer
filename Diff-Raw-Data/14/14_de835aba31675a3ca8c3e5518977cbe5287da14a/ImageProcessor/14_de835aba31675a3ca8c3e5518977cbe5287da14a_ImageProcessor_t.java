 /**
  * Copyright (c) 2012 Jun Mei
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package org.nevralia;
 
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
 import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
 import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
 import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_SCALE_IMAGE;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.RecursiveAction;
 
 import com.googlecode.javacv.cpp.opencv_core.CvPoint;
 import com.googlecode.javacv.cpp.opencv_core.CvRect;
 import com.googlecode.javacv.cpp.opencv_core.CvScalar;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import com.googlecode.javacv.cpp.opencv_objdetect.CascadeClassifier;
 
 @SuppressWarnings("serial")
 public class ImageProcessor extends RecursiveAction {
     private final List<String> _imagePaths;
     private final int _start;
     private final int _end;
     private final CascadeClassifier _classifier;
     private final CvSize _minSize;
     private final CvSize _maxSize;
     private static final List<CvScalar> COLORS;
 
     static {
         ArrayList<CvScalar> colors = new ArrayList<>();
         colors.add(CV_RGB(49, 140, 231)); // Bleu de France
         colors.add(CV_RGB(255, 255, 53)); // Banana Yellow
         colors.add(CV_RGB(127, 255, 212)); // Aquamarine
         colors.add(CV_RGB(255, 153, 102)); // Atomic Tangerine
         colors.add(CV_RGB(160, 32, 240)); // Veronica
         colors.add(CV_RGB(167, 252, 0)); // Spring Bud
         colors.add(CV_RGB(255, 36, 0)); // Scarlet
         colors.add(CvScalar.WHITE);
         colors.add(CvScalar.BLACK);
         colors.trimToSize();
         COLORS = colors;
     }
 
     /**
      * Initializes a new instance of the {@code ImageProcessor} class
      * 
      * @param classifier
      *            The cascade classifier
      * @param imagePaths
      *            The list of images to be analyzed
      * @param start
      *            The starting index
      * @param end
      *            The terminal index (analysis ends at {@code end - 1})
      * @param minSize
      *            Minimum possible object size; objects smaller than this are
      *            ignored
      * @param maxSize
      *            Maximum possible object size; objects larger than this are
      *            ignored
      */
     public ImageProcessor(final CascadeClassifier classifier,
         final List<String> imagePaths, final int start, final int end,
         final CvSize minSize, final CvSize maxSize) {
         super();
 
         _classifier = classifier;
         _imagePaths = imagePaths;
         _start = start;
         _end = end;
         _minSize = minSize;
         _maxSize = maxSize;
     }
 
     @Override
     protected void compute() {
         final short threshold = 16;
         if (_start - _end > threshold) {
             int midPoint = (_start + _end) >>> 1;
             ImageProcessor firstHalf = new ImageProcessor(_classifier,
                 _imagePaths, _start, midPoint, _minSize, _maxSize);
             ImageProcessor secondHalf = new ImageProcessor(_classifier,
                 _imagePaths, midPoint, _end, _minSize, _maxSize);
             super.invokeAll(firstHalf, secondHalf);
         } else {
             for (int i = _start; i < _end; ++i) {
                 String imagePath = _imagePaths.get(i);
                 List<CvRect> matches = searchObjects(imagePath);
                 print(imagePath, matches);
 
                 if (matches.size() > 0) {
                     draw(imagePath, matches);
                 }
             }
         }
     }
 
     /**
      * Draws a bounding box for each detected object in the given image and
      * saves the final image in a new file prefixed with "_Z_" in the same
      * directory as the source image
      * 
      * @param filePath
      * @param objects
      */
     private static void draw(String filePath, List<CvRect> objects) {
         final int thinLine = 1;
         final int connectedLine = 4;
         final int noShift = 0;
 
         IplImage image = null;
 
         try {
             image = cvLoadImage(filePath);
 
             for (int i = 0; i < objects.size(); ++i) {
                 CvRect obj = objects.get(i);
                 CvPoint topLeftCorner = new CvPoint(obj.x(), obj.y());
                 CvPoint bottomRightCorner = new CvPoint(obj.x() + obj.width(),
                     obj.y() + obj.height());
                int j = i % COLORS.size();
                CvScalar color = COLORS.get(j);
                 cvRectangle(image, topLeftCorner, bottomRightCorner, color,
                     thinLine, connectedLine, noShift);
             }
 
             File f = new File(filePath);
             String path = String.format("%1$s%2$s_Z_%3$s", f.getParent(),
                 File.separator, f.getName());
             cvSaveImage(path, image);
         } finally {
             if (image != null) {
                 cvReleaseImage(image);
             }
         }
     }
 
     /**
      * Prints a basic summary about the detected features for a specific image
      * 
      * @param image
      *            The image (file name or path) where object recognition
      *            analysis has been performed
      * @param objects
      *            Collection of all detected features for the given image
      */
     private static void print(String image, Collection<CvRect> objects) {
         System.out.printf("%1$s %2$d match(es)\n", image, objects.size());
     }
 
     /**
      * Loads the given image and searches for objects; returns the bounding box
      * of each detected feature as a list of {@code CvRect} objects
      * 
      * @param imagePath
      *            File path to the image to be analyzed
      * @return List of bounding boxes where each represents a detected feature
      */
     private List<CvRect> searchObjects(String imagePath) {
         final double reductionFactor = 1.1d;
         final int minNeighbors = 2;
         final int searchFlags = 0 | CV_HAAR_SCALE_IMAGE;
 
         CvRect objects = new CvRect(null);
         IplImage image = null;
 
         try {
             image = cvLoadImage(imagePath, CV_LOAD_IMAGE_GRAYSCALE);
             _classifier.detectMultiScale(image, objects, reductionFactor,
                 minNeighbors, searchFlags, _minSize, _maxSize);
         } finally {
             if (image != null) {
                 cvReleaseImage(image);
             }
         }
 
         int numObjects = objects.capacity();
         List<CvRect> result = new ArrayList<>(numObjects);
 
         for (int i = 0; i < numObjects; ++i) {
            CvRect o = objects.position(i);
            result.add(cvRect(o.x(), o.y(), o.width(), o.height()));
         }
 
         return result;
     }
 }

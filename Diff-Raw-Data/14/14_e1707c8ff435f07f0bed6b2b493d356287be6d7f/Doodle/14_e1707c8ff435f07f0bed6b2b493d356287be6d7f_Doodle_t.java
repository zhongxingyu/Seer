 package edu.osu.cse.doodleLock;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import android.gesture.Gesture;
 import android.gesture.GestureStroke;
 import android.util.Log;
 
 /**
  * Representation of a "doodle" - a gesture used for authentication
  *
  * @author David
  *
  */
 public class Doodle {
 
     /**
      * The maximum size of the representation of the doodle Must be a multiple
      * of 8
      */
     final int REP_SIZE = 96;
 
     /**
      * Contains numerical representation of training gestures for doodle
      */
     ArrayList<double[]> numericalRep = new ArrayList<double[]>();
 
     /**
      * The confidence threshold at which to pass/fail auth Double between 0.0
      * and 1.0
      */
     final double THRESHOLD = 0.001;
 
     /**
      * An integer representing how "loosely" the algorithm will accept Value
      * should be between 1 and 10, 1 = strict, 10 = lenient
      */
     final int TOLERANCE = 5;
 
     /**
      * The mean of each dimension in numericalRep
      */
     double[] means = new double[REP_SIZE];
 
     /**
      * The variance of each dimension in numericalRep
      */
     double[] variances = new double[REP_SIZE];
 
     /**
      * Constructs a new doodle from a list of gestures
      *
      * @param gestureList
      *            Training gestures for doodle
      */
     public Doodle(ArrayList<Gesture> gestureList) {
 
         // iterate over the provided gestures and convert them to an array of
         // doubles
         for (Gesture gesture : gestureList) {
             numericalRep.add(gestureToArray(gesture));
         }
 
         // calculate the mean and variance over each dimension
         for (int i = 0; i < REP_SIZE; i++) {
 
             double sum = 0.0;
 
             for (double[] gestureRep : numericalRep) {
                 sum += gestureRep[i];
             }
 
             means[i] = sum / numericalRep.size();
         }
 
         for (int i = 0; i < REP_SIZE; i++) {
 
             double temp = 0;
             for (double[] gestureRep : numericalRep) {
                 temp += (means[i] - gestureRep[i]) * (means[i] - gestureRep[i]);
             }
 
             variances[i] = temp / numericalRep.size();
 
         }
 
     }
 
     /**
      * Function used to validate whether or not a user's gesture matches the
      * training values that they had previously stored
      *
      * @param testGesture
      *            Gesture used to authenticate
      * @return True if gesture is acceptable
      */
     public boolean authenticate(Gesture testGesture) {
 
         // convert the input gesture into our numerical representation
         double[] gestureRep = gestureToArray(testGesture);
 
         double[] confidences = new double[REP_SIZE];
 
         // calculate the confidence product over each dimension
         double confidence = 1.0;
         // keep track of how many approximately strokes there are
         int count = 0;
 
         for (int i = 0; i < REP_SIZE; i++) {
             // check to see if the variance is 0 so no divide by zero issues
             if (variances[i] != 0) {
                 confidences[i] = gauss(gestureRep[i], means[i], variances[i]);
                 confidence *= confidences[i];
                 count++;
             } else if (gestureRep[i] != 0) {
                 // if the variance is 0, and the user has data, they exceeded
                 // the allowed number of strokes
                 return false;
             }
 
         }
 
         // multiply the confidence by the number of values used so that the
         // confidence is not as biased against patterns with many strokes
         confidence *= count;
 
         // return true if the confidence is above the defined threshold
         return (confidence >= THRESHOLD);
     }
 
     /**
      * Converts a gesture object to an arbitrary numerical representation
      *
      * @param gesture
      *            The gesture to convert
      * @return REP_SIZE length array of doubles representing the gesture
      */
     private double[] gestureToArray(Gesture gesture) {
 
         double[] gestureValues = new double[REP_SIZE];
 
         ArrayList<GestureStroke> strokes = gesture.getStrokes();
 
         for (int i = 0; i < REP_SIZE / 8; i++) {
 
             if (i < strokes.size()) {
 
                 GestureStroke currentStroke = strokes.get(i);
                 // 0 - Stroke Length
                 gestureValues[8 * i + 0] = currentStroke.length;
                 // 1 - Stroke start point - x
                 gestureValues[8 * i + 1] = currentStroke.points[0];
                 // 2 - Stroke start point - y
                 gestureValues[8 * i + 2] = currentStroke.points[1];
                // 3 - Stroke end point - x
                 gestureValues[8 * i + 3] = currentStroke.points[currentStroke.points.length - 2];
                // 4 - Stroke end point - y
                 gestureValues[8 * i + 4] = currentStroke.points[currentStroke.points.length - 1];
                // 5 - Stroke width
                 gestureValues[8 * i + 5] = currentStroke.boundingBox.width();
                // 6 - Stroke height
                 gestureValues[8 * i + 6] = currentStroke.boundingBox.height();
 
                 // Disclaimer: The code below is really bad practice and should
                 // not reflect on
                 // my abilities as a programmer. - David
                 long duration = 0;
 
                 try {
                     Field f = currentStroke.getClass().getDeclaredField(
                             "timestamps");
                     f.setAccessible(true);
 
                     long[] timestamps = (long[]) f.get(currentStroke);
                     duration = timestamps[timestamps.length - 1]
                             - timestamps[0];
                 } catch (Exception e) {
                     // That's right, I am catching any and all exceptions. I'm
                     // like a honey badger.
                     // What are we gonna do about these exceptions? Nothing.
                     Log.e("ERROR", "Reflection didn't work");
                 }
 
                // 7 - Stroke Duration (time)
                 gestureValues[8 * i + 7] = duration;
 
             } else {
                 for (int j = 0; j < 8; j++) {
                     gestureValues[8 * i + j] = 0.0;
                 }
             }
 
         }
 
         return gestureValues;
     }
 
     /**
      * Returns the value of the Gaussian function with the following parameters
      * Note: a = 1
      *
      * @param x
      *            Input to the function
      * @param mean
      *            Center of the function
      * @param variance
      *            Constant parameter
      * @return Value between 0.0 and 1.0
      */
     private double gauss(double x, double mean, double variance) {
         return Math.exp(-(x - mean) * (x - mean) / (TOLERANCE * 2 * variance));
     }
 }

 package eu.imagine.framework;
 
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint;
 import org.opencv.core.MatOfPoint2f;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tamino
  * Date: 9/7/13
  * Time: 2:08 PM
  */
 public class Marker {
 
     /**
      * Stores bool representation of pattern
      */
     private boolean[][] pattern;
     /**
      *
      */
     protected Mat grayTexture;
     /**
      * Contains angle.
      */
     private int angle;
     /**
      * The ID of the marker. Default is -1, which signifies that no ID has
      * been assigned yet.
      */
     private int id;
     /**
      * Area of marker.
      */
     private int area;
     /**
      * Contains the original corner coordinates.
      */
     private MatOfPoint2f originalCorners;
 
     private float[] translation;
 
     /**
      * Constructor – WARNING, NULL fields possible as some values are not set
      * . Use setDebugParameters to set pattern and texture.
      *
      * @param angle
      * @param id
      */
     public Marker(MatOfPoint2f corners, int angle, int id) {
         this.id = id;
         this.angle = angle;
         this.originalCorners = new MatOfPoint2f(corners.toArray());
     }
 
     protected void setDebugParameters(boolean[][] pattern, Mat grayTexture) {
         this.grayTexture = grayTexture.clone();
         this.pattern = pattern;
     }
 
     /**
      * Function for easy access to MatOfPoint representation of corner points.
      *
      * @return
      */
     protected MatOfPoint getMOPCorners() {
         return new MatOfPoint(originalCorners.toArray());
     }
 
     public boolean[][] getPattern() {
         return this.pattern;
     }
 
     public String toString() {
         return "ID: " + id + " | angle: " + angle + "°";
     }
 
     public int getID() {
         return id;
     }
 
     protected MatOfPoint2f getCorners() {
         return originalCorners;
     }
 
     protected void setRotTranslation(float[] transVec) {
         this.translation = transVec;
     }
 
     public float[] getTranslation() {
         return this.translation;
     }
 
     public void setArea(int area) {
         this.area = area;
     }
 
     public int getArea() {
         return this.area;
     }
 }

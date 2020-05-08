 package balle.world;
 
 public class Orientation {
 
     private final double angleInRadians;
 
     /**
      * Initialises orientation. The angle must be set to radians if useRadians
      * is true else the angle should be set to radians
      * 
      * @param angle
      * @param useRadians
      */
     public Orientation(double angle, boolean useRadians) {
 
         if (useRadians) {
             angle = angle % (2*Math.PI);
            angle += 2 * Math.PI;
             angleInRadians = angle;
         } else {
         	angle = angle % 360;
             angleInRadians = (angle * Math.PI) / 180;
         }
 
     }
 
     /**
      * Initialises orientation
      * 
      * @param angle
      *            in radians
      */
     public Orientation(double angle) {
         this(angle, true);
     }
 
     /**
      * Return the angle in radians
      * 
      * @return radians
      */
     public double radians() {
         return angleInRadians;
     }
 
     public double atan2styleradians() {
         if (radians() <= Math.PI)
             return radians();
         else
             return -1 * (2 * Math.PI - radians());
 
     }
 
     public double atan2styledegrees() {
         if (radians() <= Math.PI)
             return degrees();
         else
             return -1 * (360 - degrees());
 
     }
 
     /**
      * Return the angle in degrees
      * 
      * @return angle in degrees
      */
     public double degrees() {
         return (angleInRadians * 180) / Math.PI;
     }
 
 }

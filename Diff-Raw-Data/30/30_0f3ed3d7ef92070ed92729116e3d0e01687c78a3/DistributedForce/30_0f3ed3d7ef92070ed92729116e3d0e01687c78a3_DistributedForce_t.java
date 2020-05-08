 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.distributed.objects;
 
 import edu.gatech.statics.exercise.DiagramKey;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class DistributedForce implements DiagramKey {
 
     private Beam surface;
     private Point startPoint;
     private Point endPoint;
     /**
      * peak indicates both the magnitude and unit of the force, as well as the direction.
      * The magnitude corresponds to the value of the distribution at its maximum.
      * So, this will be for the tip of a triangular load, or the side of a quarter circle, or 
      * just the value of a constant distribution.
      */
     private Vector peak;
     private String name;
 
     public String getName() {
         return name;
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
     /**
      * @deprecated for persistence. Do not use!
      */
     public DistributedForce(String name) {
         this.name = name;
     }
 
     /**
      * Right now, DistributedForce requires the endpoints of the beam that makes up the surface to be known.
      * We cannot handle instances of UnknownPoint at this stage.
      * @param surface
      * @param startPoint
      * @param endPoint
      * @param peak
      */
     public DistributedForce(String name, Beam surface, Point startPoint, Point endPoint, Vector peak) {
         this.name = name;
         this.surface = surface;
         this.startPoint = startPoint;
         this.endPoint = endPoint;
         this.peak = peak;
     }
 
     /**
      * Returns a number that is the fraction between the start and end points 
      * where the resultant is located. 
      * @return
      */
     abstract protected BigDecimal getPositionMultiplier();
 
     /**
      * Returns a number that is used to determine the magnitude of the resultant.
      * If the peak value of the distributed force is F and its length is L, then
      * the magnitude of the resultant will be A*F*L, where A is some number less than
      * or equal to 1. This method returns the number A.
      * @return
      */
     abstract protected BigDecimal getMagnitudeMultiplier();
 
     /**
      * Returns the distance that should be the value of the measurement between
      * the start point and the actual resultant position.
      * Result is given in world units.
      * @return
      */
     public BigDecimal getResultantOffset() {
 
        Vector3bd start = getSurface().getEndpoint1();
        Vector3bd end = getResultantPosition();
 
        //BigDecimal alpha = getPositionMultiplier();
 
         MathContext mc = new MathContext(Unit.getPrecision(), RoundingMode.HALF_UP);
 
         BigDecimal length = new BigDecimal(start.distance(end), mc);
        //BigDecimal result = new BigDecimal(length.multiply(alpha).doubleValue(), mc);
 
        length = length.divide(Unit.distance.getDisplayScale(), mc);
 
        return length;
     }
 
     /**
      * This is an affine quantity representing the magnitude of the resultant force.
      * The method is to be used for the checking process.
      * @return
      */
     public BigDecimal getResultantMagnitude() {
         BigDecimal peakMagnitude = getPeak().getDiagramValue();
         BigDecimal magnitudeMultiplier = getMagnitudeMultiplier();
 
         MathContext mc = new MathContext(Unit.getPrecision(), RoundingMode.HALF_UP);
 
         Vector3bd start = getStartPoint().getPosition();
         Vector3bd end = getEndPoint().getPosition();
         double length = start.distance(end) / Unit.distance.getDisplayScale().floatValue();
         BigDecimal lengthbd = new BigDecimal(length, mc);
         BigDecimal result = peakMagnitude.multiply(magnitudeMultiplier).multiply(lengthbd);
 
         result = new BigDecimal(result.doubleValue(), mc);
 
         return result;
     }
 
     /**
      * Returns the point that should designate the resultant anchor.
      * @return
      */
     public Vector3bd getResultantPosition() {
 
         Vector3bd start = getStartPoint().getPosition();
         Vector3bd end = getEndPoint().getPosition();
 
         BigDecimal alpha = getPositionMultiplier();
         BigDecimal oneMinusAlpha = BigDecimal.ONE.subtract(alpha);
 
         Vector3bd position =
                 start.mult(oneMinusAlpha).add(
                 end.mult(alpha));
         return position;
     }
 
     public Point getEndPoint() {
         return endPoint;
     }
 
     public Vector getPeak() {
         return peak;
     }
 
     public Point getStartPoint() {
         return startPoint;
     }
 
     public Beam getSurface() {
         return surface;
     }
 
     /**
      * This is used to abstractly define the shape of the distribution curve,
      * where x=0 represents startPoint, and x=1 represents endPoint.
      * @param x
      * @return
      */
     abstract float getCurveValue(float x);
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final DistributedForce other = (DistributedForce) obj;
         if (this.surface != other.surface && (this.surface == null || !this.surface.equals(other.surface))) {
             return false;
         }
         if (this.startPoint != other.startPoint && (this.startPoint == null || !this.startPoint.equals(other.startPoint))) {
             return false;
         }
         if (this.endPoint != other.endPoint && (this.endPoint == null || !this.endPoint.equals(other.endPoint))) {
             return false;
         }
         if (this.peak != other.peak && (this.peak == null || !this.peak.equals(other.peak))) {
             return false;
         }
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 59 * hash + (this.surface != null ? this.surface.hashCode() : 0);
         hash = 59 * hash + (this.startPoint != null ? this.startPoint.hashCode() : 0);
         hash = 59 * hash + (this.endPoint != null ? this.endPoint.hashCode() : 0);
         hash = 59 * hash + (this.peak != null ? this.peak.hashCode() : 0);
         hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 }

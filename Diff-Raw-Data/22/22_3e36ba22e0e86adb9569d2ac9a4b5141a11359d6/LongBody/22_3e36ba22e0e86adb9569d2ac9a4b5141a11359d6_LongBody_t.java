 /*
  * TwoForceMember.java
  * 
  * Created on Oct 1, 2007, 12:59:06 PM
  * 
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.objects.bodies;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Point;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class LongBody extends Body {
 
     private Vector3bd end1;
     private Vector3bd end2;
    private Point end1Obj;
    private Point end2Obj;
 
     public Vector3bd getEndpoint1() {
         return end1;
     }
 
     public Vector3bd getEndpoint2() {
         return end2;
     }
 
    public Point getEnd1() {
        return end1Obj;
    }

    public Point getEnd2() {
        return end2Obj;
    }

     public LongBody(String name) {
         this(name, Vector3bd.ZERO, Vector3bd.UNIT_Y);
     }
 
     public LongBody(String name, Vector3bd end1, Vector3bd end2) {
         super(name);
         setByEndpoints(end1, end2);
     }
 
     public LongBody(String name, Point end1, Point end2) {
         super(name);
         setByEndpoints(end1.getPosition(), end2.getPosition());
         addObject(end1);
         addObject(end2);
        this.end1Obj = end1;
        this.end2Obj = end2;
     }
 
     public void setByEndpoints(Vector3bd end1, Vector3bd end2) {
         this.end1 = end1;
         this.end2 = end2;
 
         // add a default center of mass point.
         // this may be changed later.
 
         //setCenterOfMassPoint(new Point(end1.add(end2).mult(new BigDecimal(".5"))));
         Vector3f translation = end1.add(end2).mult(new BigDecimal(".5")).toVector3f();
 
         setTranslation(translation);
 
         Vector3f direction = end2.subtract(end1).toVector3f();
 
         Matrix3f mat = new Matrix3f();
         mat.fromStartEndVectors(Vector3f.UNIT_Y, direction.normalize());
 
         Matrix3f rotation = new Matrix3f();
         rotation.fromStartEndVectors(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
         mat.multLocal(rotation);
 
         setRotation(mat);
 
         setDimensions(0, direction.length(), 0);
     }
 
     /**
      * Returns a Vector3bd denoting the direction that the body points in, from the end specified.
      * If the body is a beam from A to B, getDirectionFrom(A) will return a vector pointing towards B.
      * 
      * @param end
      * @return
      */
     public Vector3bd getDirectionFrom(Vector3bd end) {
         if (end.equals(end1)) {
             return end2.subtract(end1).normalize();
         } else if (end.equals(end2)) {
             return end1.subtract(end2).normalize();
         }
         return Vector3bd.ZERO;
     }
 
     /**
      * This is like the method that requires a Vector3bd, but takes a Point instead.
      * @param end
      * @return
      */
     public Vector3bd getDirectionFrom(Point end) {
         return getDirectionFrom(end.getPosition());
     }
 }

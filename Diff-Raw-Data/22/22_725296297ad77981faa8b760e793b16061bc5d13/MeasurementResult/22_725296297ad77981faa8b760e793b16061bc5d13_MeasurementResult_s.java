 /*
  * MeasurementResult.java
  *
  * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
  *
  * This file is part of Ikayaki.
  *
  * Ikayaki is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Ikayaki is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Ikayaki; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package ikayaki;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 /**
  * A set of X, Y and Z values measured by the magnetometer. The raw XYZ values will be rotated in 3D space by using a
  * transformation matrix. The project will set and update the transformation whenever its parameters are changed.
  * <p/>
  * All units are mA/m.
  *
  * @author Esko Luontola
  */
 public class MeasurementResult {
 
     /**
      * The type of this result.
      */
     private final Type type;
 
     /**
      * The rotation that the sample holder was in when this result was measured. The value is in range 0..360 degrees.
      */
     private final int rotation;
 
     /**
      * The unmodified measurements recieved from the squid. Will not change after it has been once set.
      */
     private final Vector3d rawVector = new Vector3d();
 
     /**
      * The measurements in sample coordinates. Has the rotation, noise and holder fixes applied to itself.
      */
     private final Vector3d sampleVector = new Vector3d();
 
     /**
      * The measurements in geographic coordinates. Equals the sample coordinates with the transformation matrix
      * applied.
      */
     private final Vector3d geographicVector = new Vector3d();
 
     /**
      * Creates a new measurement result. All units are mA/m.
      * <p/>
      * The sample and geographic coordinates are NOT set when a MeasurementResult is created.
      *
      * @param type     the type (background or rotation) of this result.
      * @param rotation the rotation of the sample holder in degrees (0..360).
      * @param x        the measured X coordinate value.
      * @param y        the measured Y coordinate value.
      * @param z        the measured Z coordinate value.
      * @throws NullPointerException     if type is null.
      * @throws IllegalArgumentException if the type is NOISE or HOLDER, but rotation is non-zero.
      */
     public MeasurementResult(Type type, int rotation, double x, double y, double z) {
         if (type == null) {
             throw new NullPointerException();
         }
         if ((type == Type.NOISE || type == Type.HOLDER) && rotation != 0) {
             throw new IllegalArgumentException("type = " + type + ", rotation = " + rotation);
         }
         this.type = type;
         this.rotation = rotation % 360;
         rawVector.set(x, y, z);
         applyFixes(null);
         setTransform(null);
     }
 
     /**
      * Creates a measurement result from the specified element.
      * <p/>
      * The sample and geographic coordinates are NOT set when a MeasurementResult is created.
      *
      * @param element the element from which this result will be created.
      * @throws NullPointerException     if element is null.
      * @throws IllegalArgumentException if the element was not in the right format.
      */
     public MeasurementResult(Element element) {
         if (element == null) {
             throw new NullPointerException();
         }
 
         // verify tag name
         if (!element.getTagName().equals("result")) {
             throw new IllegalArgumentException("Invalid tag name: " + element.getTagName());
         }
 
         // get type
         String s = element.getAttribute("type");
         try {
             this.type = Type.valueOf(s);
         } catch (IllegalArgumentException e) {
             throw new IllegalArgumentException("Invalid type: " + s, e);
         }
 
         // get rotation
         s = element.getAttribute("rotation");
         try {
             int rotation = Integer.parseInt(s) % 360;
             while (rotation < 0) {
                 rotation += 360;
             }
             this.rotation = rotation;
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Invalid rotation: " + s, e);
         }
 
         // verify type and rotation
         if ((type == Type.NOISE || type == Type.HOLDER) && rotation != 0) {
             throw new IllegalArgumentException("type = " + type + ", rotation = " + rotation);
         }
 
         // get x, y, z
         try {
             rawVector.set(Double.parseDouble(element.getAttribute("x")),
                     Double.parseDouble(element.getAttribute("y")),
                     Double.parseDouble(element.getAttribute("z")));
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Invalid x, y or z: " + e.getMessage(), e);
         }
 
         // initialize sampleVector and geographicVector
         applyFixes(null);
         setTransform(null);
     }
 
     /**
      * Exports this result to a DOM element.
      *
      * @param document the document that will contain this element.
      */
     public Element getElement(Document document) {
         Element element = document.createElement("result");
 
         element.setAttribute("type", type.name());
         element.setAttribute("rotation", Integer.toString(rotation));
         element.setAttribute("x", Double.toString(rawVector.x));
         element.setAttribute("y", Double.toString(rawVector.y));
         element.setAttribute("z", Double.toString(rawVector.z));
 
         return element;
     }
 
     /**
      * Applies the holder, noise and rotation fixes and saves the results as the sample vector. Resets the geographic
      * vector to a copy of the sample vector. This method must be called before setTransform().
      *
      * @param step the measurement step that includes the holder and noise calibration values. If null, the holder and
      *             noise fixes are not applied.
      */
     protected void applyFixes(MeasurementStep step) {
         sampleVector.set(rawVector);
 
         // apply rotation fix
         if (rotation % 90 == 0) {
             // accurate and fast algorithm for trivial angles
             switch (rotation) {
             case 0:
                 // NO NEED TO ROTATE
                 break;
             case 90:
                 sampleVector.set(sampleVector.y, -sampleVector.x, sampleVector.z);
                 break;
             case 180:
                 sampleVector.set(-sampleVector.x, -sampleVector.y, sampleVector.z);
                 break;
             case 270:
                 sampleVector.set(-sampleVector.y, sampleVector.x, sampleVector.z);
                 break;
             default:
                 assert false;
                 throw new IllegalStateException("rotation = " + rotation);
             }
         } else {
             // rotate all non-trivial angles by using a matrix
             Matrix3d rotate = new Matrix3d();
             rotate.rotZ(Math.toRadians(-rotation));
             rotate.transform(sampleVector);
         }
 
         if (step != null) {
            // apply the noise fix
            Vector3d noise = step.getNoise();
            sampleVector.sub(noise);

             // apply the +/-Z fix
             if (step.getProject() != null && step.getProject().getOrientation() == Project.Orientation.MINUS_Z) {
                 sampleVector.set(sampleVector.x, -sampleVector.y, -sampleVector.z);
             }
 
             // apply the sample holder fix
             Vector3d holder = step.getHolder();     // will be zero, if this project is the holder calibration project
             sampleVector.sub(holder);
         }
 
         // reset geographic vector
         setTransform(null);
     }
 
     /**
      * Applies a transformation matrix to the sample vector and saves the results as the geographic vector. This method
      * must be called after applyFixes().
      *
      * @param transform the matrix to be applied. If null, will assume identity matrix.
      */
     protected void setTransform(Matrix3d transform) {
         if (transform != null) {
             transform.transform(sampleVector, geographicVector);
         } else {
             geographicVector.set(sampleVector);
         }
     }
 
     /**
      * Returns the type of this result.
      */
     public Type getType() {
         return type;
     }
 
     /**
      * Returns the rotation of this result. The value is in range 0..360 degrees.
      */
     public int getRotation() {
         return rotation;
     }
 
     /**
      * Returns the noise fixed, rotated and transformed X coordinate of this result. The value is in geographic
      * coordinates.
      *
      * @throws IllegalStateException if this result's type is not SAMPLE, in which case it should make no sense to call
      *                               this method.
      */
     public double getGeographicX() {
         if (type != Type.SAMPLE) {
             throw new IllegalStateException();
         }
         return geographicVector.x;
     }
 
     /**
      * Returns the noise fixed, rotated and transformed Y coordinate of this result. The value is in geographic
      * coordinates.
      *
      * @throws IllegalStateException if this result's type is not SAMPLE, in which case it should make no sense to call
      *                               this method.
      */
     public double getGeographicY() {
         if (type != Type.SAMPLE) {
             throw new IllegalStateException();
         }
         return geographicVector.y;
     }
 
     /**
      * Returns the noise fixed, rotated and transformed Z coordinate of this result. The value is in geographic
      * coordinates.
      *
      * @throws IllegalStateException if this result's type is not SAMPLE, in which case it should make no sense to call
      *                               this method.
      */
     public double getGeographicZ() {
         if (type != Type.SAMPLE) {
             throw new IllegalStateException();
         }
         return geographicVector.z;
     }
 
     /**
      * Returns a pointer to the geographic vector. WARNING! No modification to the returned object should be made. They
      * should be done on a copy of the object than the object itself.
      */
     protected Vector3d getGeographicVector() {
         return geographicVector;
     }
 
     /**
      * Returns the noise fixed and rotated X coordinate of this result. The value is in sample coordinates.
      */
     public double getSampleX() {
         return sampleVector.x;
     }
 
     /**
      * Returns the noise fixed and rotated Y coordinate of this result. The value is in sample coordinates.
      */
     public double getSampleY() {
         return sampleVector.y;
     }
 
     /**
      * Returns the noise fixed and rotated Z coordinate of this result. The value is in sample coordinates.
      */
     public double getSampleZ() {
         return sampleVector.z;
     }
 
     /**
      * Returns a pointer to the sample vector. WARNING! No modification to the returned object should be made. They
      * should be done on a copy of the object than the object itself.
      */
     protected Vector3d getSampleVector() {
         return sampleVector;
     }
 
     /**
      * Returns the unmodified X coordinate of this result. The value is in magnetometer coordinates.
      */
     public double getRawX() {
         return rawVector.x;
     }
 
     /**
      * Returns the unmodified Y coordinate of this result. The value is in magnetometer coordinates.
      */
     public double getRawY() {
         return rawVector.y;
     }
 
     /**
      * Returns the unmodified Z coordinate of this result. The value is in magnetometer coordinates.
      */
     public double getRawZ() {
         return rawVector.z;
     }
 
     /**
      * Returns a pointer to the raw vector. WARNING! No modification to the returned object should be made. They should
      * be done on a copy of the object than the object itself.
      */
     protected Vector3d getRawVector() {
         return rawVector;
     }
 
     public enum Type {
         SAMPLE, HOLDER, NOISE
     }
 }

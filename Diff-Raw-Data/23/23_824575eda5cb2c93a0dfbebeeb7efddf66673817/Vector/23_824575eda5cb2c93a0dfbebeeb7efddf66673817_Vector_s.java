 /*
  * Vector.java
  *
  * Created on June 7, 2007, 4:21 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.math;
 
 import java.math.BigDecimal;
 
 /**
  * This class contains the guts of a vector, its pure mathematical representation.
  * The idea is that this describes the underlying math behind a vector, but does not
  * contain class or context information regarding what the vector is for. The class
  * VectorObject represents a vector within world.
  * @see edu.gatech.statics.objects.VectorObject
  * @author Calvin Ashmore
  */
 final public class Vector implements Quantified {
 
     private Vector3bd value; // normalized value
 
     private Quantity magnitude;
 
     public Unit getUnit() {
         return magnitude.getUnit();
     }
 
     public boolean isKnown() {
         return magnitude.isKnown();
     }
 
     public boolean isSymbol() {
         return magnitude.isSymbol();
     }
 
     public void setKnown(boolean known) {
         magnitude.setKnown(known);
     }
 
     public void setSymbol(String symbolName) {
         magnitude.setSymbol(symbolName);
     }
 
     public double doubleValue() {
         return magnitude.doubleValue();
     }
 
     public Quantity getQuantity() {
         return magnitude.getUnmodifiableQuantity();
     }
 
     public void setDiagramValue(BigDecimal magnitude) {
 
         if (magnitude.signum() < 0) {
             magnitude = magnitude.negate();
             setVectorValue(value.negate());
         }
 
         this.magnitude.setDiagramValue(magnitude);
     }
 
     public BigDecimal getDiagramValue() {
         return magnitude.getDiagramValue();
     }
 
     public Vector negate() {
         Vector r = new Vector(getUnit(), value.negate(), magnitude.getDiagramValue());
         return r;
     }
 
     /**
      * This unitizes value and assigns the result to the vector part.
      * To set the magnitude as well, use setMagnitude(value.length())
      * @param value
      */
     public void setVectorValue(Vector3bd value) {
         this.value = value.normalize();
     //positivizeZeroes();
     }
 
     public Vector3bd getVectorValue() {
         return value;
     }
 
     /** Creates a new instance of Vector */
     public Vector(Unit unit, Vector3bd value, BigDecimal magnitude) {
         //constructQuantity();
         this.magnitude = new Quantity(unit, magnitude);
         setVectorValue(value);
     }
 
     public Vector(Unit unit, Vector3bd value, String symbolName) {
         //constructQuantity();
         magnitude = new Quantity(unit, symbolName);
         //setValue(value.length());
         setVectorValue(value);
     //positivizeZeroes();
     }
 
     public Vector(Vector vector) {
         //constructQuantity();
         magnitude = new Quantity(vector.getQuantity());
         //setValue(vector.doubleValue());
         setVectorValue(vector.getVectorValue());
     }
 
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Vector other = (Vector) obj;
         if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
             return false;
         }
         if (this.magnitude != other.magnitude && (this.magnitude == null || !this.magnitude.equals(other.magnitude))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
         hash = 59 * hash + (this.magnitude != null ? this.magnitude.hashCode() : 0);
         return hash;
     }
 
     /*@Override
     public boolean equals(Object obj) {
     if (obj == null || obj.getClass() != this.getClass()) {
     return false;
     }
     Vector v = (Vector) obj;
     // this is the low tech, ghetto way of doing things
     // but it should suffice for now.
     return valuesCloseEnough(v.value, value);
     }
     @Override
     public int hashCode() {
     int hash = 7;
     //hash = 23 * hash + (this.anchor != null ? this.anchor.hashCode() : 0);
     return hash;
     }*/
     public String getSymbolName() {
         return magnitude.getSymbolName();
     }
 
     // returns true if the vectors are close enough
     // close enough means that they have the same values in their decimal places
     // up to the decimal precision for forces in Units.
     /*private boolean valuesCloseEnough(Vector3f v1, Vector3f v2) {
     double power = Math.pow(10, getUnit().getDecimalPrecision());
     double xdiff = Math.floor(v1.x * power) - Math.floor(v2.x * power);
     double ydiff = Math.floor(v1.y * power) - Math.floor(v2.y * power);
     double zdiff = Math.floor(v1.z * power) - Math.floor(v2.z * power);
     return Math.abs(xdiff) + Math.abs(ydiff) + Math.abs(zdiff) < .1;
     }*/
 
     // QUESTION:
     // should there be a thing for configuring the precision of equals-tests?
     /**
      * Compare two vectors accounting for them both being symbols
      * This means that they are equal if they are equal or opposite
      */
     public boolean equalsSymbolic(Vector v) {
         if (v.isKnown() && isKnown()) {
 
             return value.equals(v.value);
         }
 
         //return valuesCloseEnough(v.value, value) ||
         //        valuesCloseEnough(v.value, value.negate());
         return value.equals(v.value) || value.equals(v.value.negate());
     }
 
     @Override
     public String toString() {
         String r = value.toString();
         r += " " + magnitude;
         if (magnitude.isSymbol()) {
             r += " \"" + magnitude.getSymbolName() + "\"";
         }
         if (magnitude.isKnown()) {
             r += " SOLVED";
         }
         return r;
     }
     /*private void positivizeZeroes() {
     if(value.x == -0f) value.x = 0f;
     if(value.y == -0f) value.y = 0f;
     if(value.z == -0f) value.z = 0f;
     }*/
 }

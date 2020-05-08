 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eel.seprphase2.Utilities;
 
 /**
  *
  * @author drm
  */
 public class Velocity {
 
     private double metresPerSecond;
 
     public Velocity() {
         metresPerSecond = 0;
     }
     
     public Velocity(double metresPerSecond) {
         this.metresPerSecond = metresPerSecond;
     }
     
     public double inMetresPerSecond() {
         return metresPerSecond;
     }
     
     public Velocity plus(Velocity other) {
         return new Velocity(metresPerSecond + other.metresPerSecond);
     }
     
     public Velocity minus(Velocity other) {
        return new Velocity(metresPerSecond - other.metresPerSecond);
     }
 
     @Override
     public String toString() {
         return Format.toThreeDecimalPlaces(metresPerSecond) + " m/s";
     }
 
     @Override
     public int hashCode() {
         return (int)metresPerSecond;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Velocity other = (Velocity)obj;
         if (Double.doubleToLongBits(this.metresPerSecond) != Double.doubleToLongBits(other.metresPerSecond)) {
             return false;
         }
         return true;
     }
     
 }

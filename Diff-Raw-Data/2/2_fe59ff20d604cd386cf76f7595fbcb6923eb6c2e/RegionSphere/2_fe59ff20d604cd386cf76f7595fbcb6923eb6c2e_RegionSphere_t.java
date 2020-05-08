 package com.bencvt.minecraft.buildregion.region;
 
 import libshapedraw.primitive.ReadonlyVector3;
 import libshapedraw.primitive.Vector3;
 
 /**
  * Represent a sphere -- actually an ellipsoid -- specified by an origin
  * 3-tuple and a radii 3-tuple. The components of each tuple are reduced to
  * half units. Radii are non-negative and >=0.5.
  * 
  * @author bencvt
  */
 public class RegionSphere extends RegionBase {
     private final Vector3 radii;
 
     public RegionSphere(ReadonlyVector3 origin, ReadonlyVector3 radii) {
         super(origin);
         this.radii = radii.copy().absolute();
         enforceHalfUnits(this.radii);
     }
 
     @Override
     public RegionType getRegionType() {
         return RegionType.SPHERE;
     }
 
     @Override
     public RegionBase copyUsing(ReadonlyVector3 origin, Axis axis) {
         // ignore axis
         return new RegionSphere(origin, radii);
     }
 
     @Override
     protected void onOriginUpdate() {
         enforceHalfUnits(getOrigin());
     }
 
     @Override
     public boolean isInsideRegion(double x, double y, double z) {
         return    Math.pow(((int) x) - getOrigin().getX(), 2.0) / Math.pow(radii.getX(), 2.0)
                 + Math.pow(((int) y) - getOrigin().getY(), 2.0) / Math.pow(radii.getY(), 2.0)
                 + Math.pow(((int) z) - getOrigin().getZ(), 2.0) / Math.pow(radii.getZ(), 2.0)
                < 1.0;
     }
 
     @Override
     public double size() {
         return 4.0 / 3.0 * Math.PI * radii.getX() * radii.getY() * radii.getZ();
     }
 
     @Override
     public boolean getAABB(Vector3 lower, Vector3 upper) {
         lower.set(getOriginReadonly()).subtract(radii);
         upper.set(getOriginReadonly()).add(radii);
         return true;
     }
 
     @Override
     public String toString() {
         double r = radii.getX();
         if (r == radii.getY() && r == radii.getZ()) {
             return "sphere @ " + strXYZ(getOrigin()) + "\nradius " + r;
         } else {
             return "ellipsoid @ " + strXYZ(getOrigin()) + "\nradius " + strXYZ(radii);
         }
     }
 
     public ReadonlyVector3 getRadiiReadonly() {
         return radii;
     }
 
     public double getRadiusX() {
         return radii.getX();
     }
     public void setRadiusX(double radiusX) {
         enforceHalfUnits(radii.setX(Math.max(0.5, Math.abs(radiusX))));
     }
 
     public double getRadiusY() {
         return radii.getY();
     }
     public void setRadiusY(double radiusY) {
         enforceHalfUnits(radii.setY(Math.max(0.5, Math.abs(radiusY))));
     }
 
     public double getRadiusZ() {
         return radii.getZ();
     }
     public void setRadiusZ(double radiusZ) {
         enforceHalfUnits(radii.setZ(Math.max(0.5, Math.abs(radiusZ))));
     }
 }

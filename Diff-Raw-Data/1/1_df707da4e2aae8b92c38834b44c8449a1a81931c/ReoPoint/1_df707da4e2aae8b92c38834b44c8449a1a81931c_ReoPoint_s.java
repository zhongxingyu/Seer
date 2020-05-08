 package com.tihiy.reo;
 
 class ReoPoint{
     private final double x;
     private final double y;
     private final double z;
 
     ReoPoint(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
     }
 
     ReoPoint(double x, double y) {
         this.x = x;
         this.y = y;
         z = 0;
     }
 
 
     public double getX() {
         return x;
     }
 
     public double getY() {
         return y;
     }
 
     public double getZ() {
         return z;
     }
 }

 package com.pathfinder.internal;
 
 import static java.lang.Math.abs;
 import static java.lang.String.format;
 
 class Coordinate {
     private final double longitude;
     private final double latitude;
 
     public Coordinate(double longitude, double latitude) {
         this.longitude = longitude;
         this.latitude = latitude;
     }
 
     public double distanceTo(Coordinate coordinate){
         return Math.sqrt(pow2(minimumLongitudinalDistance(coordinate)) + pow2(this.latitude - coordinate.latitude));
     }
 
     private double minimumLongitudinalDistance(Coordinate coordinate) {
        return abs(this.longitude) - abs(coordinate.longitude);
     }
 
     private double pow2(double n){
         return n * n;
     }
 
     @Override
     public String toString() {
         return format("(%.6f, %.6f)", longitude, latitude);
     }
 }

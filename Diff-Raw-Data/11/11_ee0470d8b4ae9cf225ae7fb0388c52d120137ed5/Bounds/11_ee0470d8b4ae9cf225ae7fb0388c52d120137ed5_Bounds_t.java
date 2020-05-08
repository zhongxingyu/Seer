 package com.akzia.googleapi.common;
 
 import com.google.gson.annotations.SerializedName;
 
 public class Bounds {
 
     @SerializedName("northeast")
     private GeoPoint northEast;
 
     @SerializedName("southwest")
     private GeoPoint southWest;
 
     public Bounds() {
     }
 
     public Bounds(GeoPoint northEast, GeoPoint southWest) {
         this.northEast = northEast;
         this.southWest = southWest;
     }
 
     public GeoPoint getNorthEast() {
         return northEast;
     }
 
     public void setNorthEast(GeoPoint northEast) {
         this.northEast = northEast;
     }
 
     public GeoPoint getSouthWest() {
         return southWest;
     }
 
     public void setSouthWest(GeoPoint southWest) {
         this.southWest = southWest;
     }
 
     @Override
     public String toString() {
        return southWest + "|" + northEast;
     }
 }

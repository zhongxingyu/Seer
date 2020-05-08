 package com.tw.techradar.model;
 
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
 import java.io.Serializable;
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class RadarArc implements Serializable{
     private int r;
     private String name;
 
     public boolean isRadarItemInArc(RadarItem radarItem){
        return radarItem.getRadius() <= getRadius();
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getRadius() {
         return r;
     }
 
     public void setR(int r) {
         this.r = r;
     }
 }

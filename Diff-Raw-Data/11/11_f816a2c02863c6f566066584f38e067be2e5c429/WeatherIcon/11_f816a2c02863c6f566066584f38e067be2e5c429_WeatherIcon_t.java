 package com.fear_airsoft.json;
 
 import java.io.Serializable;
 
 class WeatherIcon implements Serializable{
     private String value;
     
     public boolean equals(WeatherIcon other) {
         return value.equals(other.value);
     }
     
     public String toString() {
        return "<"+this.getClass().getName()+":value="+value+">";
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 }

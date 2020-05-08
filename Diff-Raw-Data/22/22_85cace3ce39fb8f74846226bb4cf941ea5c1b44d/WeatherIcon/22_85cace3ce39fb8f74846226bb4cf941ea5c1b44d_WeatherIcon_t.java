 package com.fear_airsoft.json;
 
import java.io.Serializable;

class WeatherIcon implements Serializable{
     private String value;
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 }

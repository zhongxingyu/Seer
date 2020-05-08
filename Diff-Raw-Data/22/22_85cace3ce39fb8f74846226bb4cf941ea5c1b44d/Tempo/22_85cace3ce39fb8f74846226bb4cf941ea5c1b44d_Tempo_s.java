 package com.fear_airsoft.json;
 
 import java.util.List;
 
public class Tempo {
     private Info data;
 
     public Info getData() {
         return data;
     }
 
     public void setData(Info data) {
         this.data = data;
     }
 
     class Info {
         private List<Weather> weather;
 
         public List<Weather> getWeather() {
             return weather;
         }
 
         public void setWeather(List<Weather> weather) {
             this.weather = weather;
         }
     }
 }

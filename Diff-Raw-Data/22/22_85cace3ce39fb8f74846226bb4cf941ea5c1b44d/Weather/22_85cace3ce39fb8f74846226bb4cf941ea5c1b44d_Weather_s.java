 package com.fear_airsoft.json;
 
 import java.util.List;
 
class Weather {
     private String date;
     private String precipMM;
     private String tempMaxC;
     private String tempMinC;
     private List<WeatherIcon> weatherIconUrl;
     private String windspeedKmph;
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getPrecipMM() {
         return precipMM;
     }
 
     public void setPrecipMM(String precipMM) {
         this.precipMM = precipMM;
     }
 
     public String getTempMaxC() {
         return tempMaxC;
     }
 
     public void setTempMaxC(String tempMaxC) {
         this.tempMaxC = tempMaxC;
     }
 
     public String getTempMinC() {
         return tempMinC;
     }
 
     public void setTempMinC(String tempMinC) {
         this.tempMinC = tempMinC;
     }
 
     public List<WeatherIcon> getWeatherIconUrl() {
         return weatherIconUrl;
     }
 
     public void setWeatherIconUrl(List<WeatherIcon> weatherIconUrl) {
         this.weatherIconUrl = weatherIconUrl;
     }
 
     public String getWindspeedKmph() {
         return windspeedKmph;
     }
 
     public void setWindspeedKmph(String windspeedKmph) {
         this.windspeedKmph = windspeedKmph;
     }
 }

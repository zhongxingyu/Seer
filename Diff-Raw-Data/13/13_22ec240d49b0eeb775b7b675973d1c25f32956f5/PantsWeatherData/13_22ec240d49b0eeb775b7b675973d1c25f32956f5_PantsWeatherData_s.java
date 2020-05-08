 package com.weatherornot.weatherornot;
 
 import android.location.Location;
 
 /**
 * Created by cheryl on 9/3/13.
 */
 public class PantsWeatherData {
 
 
     private Double mtime;
     private Double mCurrentTemp;
     private Double[] mHourly;
     private String[] mHourlyData;
    public String myCityName;
     public Location myGeoLocation;
     public static int theIcon;
 
     public static int getTheIcon() {
         return theIcon;
     }
 
 
 
 
 
 
 
 
 
 
     public PantsWeatherData() {
 
     }
 
 
     public String getIcon(){
         return icon.toString();
     }
 
 
 
 
     public void setIcon(String icon) {
         this.icon = icon;
     }
 
 
 
     public String icon;
 
 
 
 //    public PantsWeatherData getCityData() {
 //        return cityData;
 //    }
 //
 //    public void setCityData(PantsWeatherData cityData) {
 //        this.cityData = cityData;
 //    }
 //
 //    public PantsWeatherData cityData = new PantsWeatherData();
 
 
 
 
     public String getGiveDate() {
         return giveDate;
     }
 
     public void setGiveDate(String giveDate) {
         this.giveDate = giveDate;
     }
 
     private String giveDate;
 
 
     public String[] getmHourlyData() {
         return mHourlyData;
     }
 
     public void setmHourlyData(String[] mHourlyData) {
         this.mHourlyData = mHourlyData;
     }
 
     public String getmCurrentTempString(){
         return mCurrentTemp.toString();
     }
     public Double getMtime() {
         return mtime;
     }
 
 
     public void setMtime(Double mtime) {
         this.mtime = mtime;
     }
 
     public Double getmCurrentTemp() {
         return mCurrentTemp;
     }
 
     public void setmCurrentTemp(Double mCurrentTemp) {
         this.mCurrentTemp = mCurrentTemp;
     }
 
     public Double[] getmHourly() {
         return mHourly;
     }
 
     public void setmHourly(Double[] mHourly) {
         this.mHourly = mHourly;
     }
 
 
     public void setMyCityName(String name) {
     }
 }

 package it.unipi.models;
 
 import java.util.*;
import java.text.SimpleDateFormat;
 
 /**
  * Store forecasts data
  */
 public class Forecast {
     private Date datetime;
     private String description;
     private int temperature; // in Celsius
 
     public Forecast( Date datetime, int temperature, String description ) {
         this.datetime = datetime;
         this.temperature = temperature;
         this.description = description;
     }
 
     public Forecast( long timestamp, int temperature, String description ) {
         this.datetime = new Date( timestamp );
         this.temperature = temperature;
         this.description = description;
     }
 
     /** Getters */
     public Date getDatetime()      { return this.datetime; }
     public String getDescription() { return this.description; }
     public int getTemperature()    { return this.temperature; }
 
     public long getTimestamp()     { return this.datetime.getTime(); }
 
     /** Output getters */
     public String getDatetimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, E dd MMM yyyy");
        return formatter.format( datetime );
     }
 
     public String getDescriptionString() {
         return description;
     }
 
     public String getTemperatureString() {
         return temperature + "° C";
     }
 }

 package com.rcs.liferaysense.service.commonsense;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Data measured by the sensor.
  * @author juan
  */
 public class SensorValue implements Serializable {
     
     private static final long serialVersionUID = 1L;
     
    private int id;
     private int sensorId;
     private String value;
     private double date;
     private int week;
     private int month;
     private int year;
 
     public SensorValue() {
     }
     
    public int getId() {
         return id;
     }
 
    public void setId(int id) {
         this.id = id;
     }
 
     public int getMonth() {
         return month;
     }
 
     public void setMonth(int month) {
         this.month = month;
     }
 
     public int getSensorId() {
         return sensorId;
     }
 
     public void setSensorId(int sensorId) {
         this.sensorId = sensorId;
     }
     
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
     
     /**
      * The date of the value in unix-timestamp seconds.
      * @return 
      */
     public double getDate() {
         return date;
     }
 
     public void setDate(double date) {
         this.date = date;
     }
     
     public Date getAsDate() {
         return new Date((long)date*1000);
     }
     
     public int getWeek() {
         return week;
     }
 
     public void setWeek(int week) {
         this.week = week;
     }
 
     public int getYear() {
         return year;
     }
 
     public void setYear(int year) {
         this.year = year;
     }
     
     /**
      * Try to parse the value as a double, if the value cannot be parsed, then 0
      * is returned.
      * @return 
      */
     public double getValueAsDouble() {
         try {
             return Double.parseDouble(value);
         } catch (NumberFormatException ex) {
             return 0;
         }
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SensorValue other = (SensorValue) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.id;
        return hash;
    }
     
 }

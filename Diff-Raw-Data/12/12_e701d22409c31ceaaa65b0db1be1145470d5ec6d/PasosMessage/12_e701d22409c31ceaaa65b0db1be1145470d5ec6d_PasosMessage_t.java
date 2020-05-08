 
 package org.inftel.tms.mobile.pasos;
 
 import java.util.Calendar;
 
 public class PasosMessage {
     private String type;
     private Integer temperature;
     private Integer batteryLevel;
     private Double longitude;
     private Double latitude;
     private Boolean charging;
     private String date;
     private String time;
    private String key = "&RK123456"; // TODO configurable?
 
     public static class Builder {
         private String _type;
         private Integer _temperature;
         private Integer _batteryLevel;
         private Double _longitude;
         private Double _latitude;
         private Boolean _charging;
 
         public Builder(String alarmType) {
             _type = alarmType;
         }
 
         public Builder temperature(int temperature) {
             _temperature = temperature;
             return this;
         }
 
         public Builder battery(int battery) {
             _batteryLevel = battery;
             return this;
         }
 
         public Builder location(double latitude, double longitude) {
             _latitude = latitude;
             _longitude = longitude;
             return this;
         }
 
         public Builder charging(boolean charging) {
             _charging = charging;
             return this;
         }
 
         public PasosMessage build() {
             PasosMessage message = new PasosMessage();
             message.setType(_type);
             message.setTemperature(_temperature);
             message.setBatteryLevel(_batteryLevel);
             message.setLongitude(_longitude);
             message.setLatitude(_latitude);
             message.setCharging(_charging);
             return message;
         }
 
     }
 
     /**
      * Nueva instancia mensaje PaSOS con datos Data y Time inicializados.
      */
     private PasosMessage() {
         initializeDateTime();
     }
 
     public static Builder buildUserAlarm(Double latitude, Double longitude) {
         return new Builder(PasosMessageType.USER_ALARM).location(latitude, longitude);
     }
 
     public static Builder buildDeviceAlarmHighTemp(int temperature) {
         return new Builder(PasosMessageType.DEVICE_ALARM_HIGHTEMP).temperature(temperature);
     }
 
     public static Builder buildDeviceAlarmLowTemp(int temperature) {
         return new Builder(PasosMessageType.DEVICE_ALARM_LOWTEMP).temperature(temperature);
     }
 
     public static Builder buildTechnicalAlarm() {
         return new Builder(PasosMessageType.TECHNICAL_ALARM);
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public Integer getTemperature() {
         return temperature;
     }
 
     public void setTemperature(Integer temperature) {
         this.temperature = temperature;
     }
 
     public Integer getBatteryLevel() {
         return batteryLevel;
     }
 
     public void setBatteryLevel(Integer batteryLevel) {
         this.batteryLevel = batteryLevel;
     }
 
     public Double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(Double longitude) {
         this.longitude = longitude;
     }
 
     public Double getLatitude() {
         return latitude;
     }
 
     public void setLatitude(Double latitude) {
         this.latitude = latitude;
     }
 
     public Boolean isCharging() {
         return charging;
     }
 
     public void setCharging(Boolean charging) {
         this.charging = charging;
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getTime() {
         return time;
     }
 
     public void setTime(String time) {
         this.time = time;
     }
 
     /**
      * Set Date and Time to actual, in PASOS format
      */
     public void initializeDateTime() {
         Calendar calendar = Calendar.getInstance();
 
         String dayString = String.format("%td", calendar);
         String monthString = String.format("%tm", calendar);
         String hourString = String.format("%tH", calendar);
         String minsString = String.format("%tM", calendar);
         String secsString = String.format("%tS", calendar);
         String yearString = String.valueOf(calendar.get(Calendar.YEAR));
 
         this.date = "&LD" + yearString + monthString + dayString;
         this.time = "&LH" + hourString + minsString + secsString;
     }
 
     @Override
     public String toString() {
         StringBuilder message = new StringBuilder();
        // Valores por defecto, tipo, clave y fecha de envio
        message.append(type).append(key).append(date).append(time);
 
         // Localización
         if (latitude != null && longitude != null) {
             message.append("&LT").append(latitude);
             message.append("&LN").append(longitude);
         }
 
         // Cargando
         if (charging != null && charging == true) {
             message.append("&PC999");
         } else if (charging != null) {
             message.append("&PC000");
         }
 
         // Nivel de batería
         if (batteryLevel != null) {
             message.append("&PB").append(batteryLevel);
         }
 
         // Temperatura
         if (temperature != null) {
             message.append("&DT").append(temperature);
         }
 
         return message.append("#").toString();
     }
 }

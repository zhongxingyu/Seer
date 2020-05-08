 package no.helsenorge.nipen.model;
 
 public class HeartRate extends DataType {
     private int value;
     private String unit;
 
     public HeartRate(long userId, String timestamp, int value, String unit) {
        setId(userId);
         setTimestamp(timestamp);
         this.value = value;
         this.unit = unit;
     }
 
     public long getValue() {
         return value;
     }
 
     public String getUnit() {
         return unit;
     }
 }

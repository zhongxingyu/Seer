 package no.helsenorge.nipen.model;
 
 public class Weight extends DataType {
     private int value;
     private String unit;
 
     public Weight(long userId, String timestamp, int value, String unit) {
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

 package com.xerox.amazonws.fps;
 
 import java.io.Serializable;
 
 /**
  * @author J. Bernard
  * @author Elastic Grid, LLC.
  * @author jerome.bernard@elastic-grid.com
  */
 public class DescriptorPolicy implements Serializable {
     private final SoftDescriptorType softDescriptorType;
     private final CSNumberOf csNumberOf;
 
     public DescriptorPolicy(SoftDescriptorType softDescriptorType, CSNumberOf csNumberOf) {
         this.softDescriptorType = softDescriptorType;
         this.csNumberOf = csNumberOf;
     }
 
     public SoftDescriptorType getSoftDescriptorType() {
         return softDescriptorType;
     }
 
     public CSNumberOf getCSNumberOf() {
         return csNumberOf;
     }
 
    public enum SoftDescriptorType implements Serializable {
         DYNAMIC("Dynamic"),
         STATIC("Static");
 
         private final String value;
 
         SoftDescriptorType(String v) {
             value = v;
         }
 
         public String value() {
             return value;
         }
 
         public static SoftDescriptorType fromValue(String v) {
             for (SoftDescriptorType c : SoftDescriptorType.values()) {
                 if (c.value.equals(v)) {
                     return c;
                 }
             }
             throw new IllegalArgumentException(v);
         }
     }
 
    public enum CSNumberOf implements Serializable {
         CALLER("Caller"),
         RECIPIENT("Recipient");
 
         private final String value;
 
         CSNumberOf(String v) {
             value = v;
         }
 
         public String value() {
             return value;
         }
 
         public static CSNumberOf fromValue(String v) {
             for (CSNumberOf c : CSNumberOf.values()) {
                 if (c.value.equals(v)) {
                     return c;
                 }
             }
             throw new IllegalArgumentException(v);
         }
     }
 }

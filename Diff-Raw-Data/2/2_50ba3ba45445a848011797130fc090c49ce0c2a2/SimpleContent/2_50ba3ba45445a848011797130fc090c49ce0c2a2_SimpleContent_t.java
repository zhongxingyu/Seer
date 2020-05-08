 package com.spartansoftwareinc.otter;
 
 /**
 * Base class for all non-tag content objects in a TUV.
  */
 public abstract class SimpleContent implements TUVContent {
     private String value;
     SimpleContent(String value) {
         this.value = value;
     }
     public String getValue() {
         return value;
     }
     public void setValue(String value) {
         this.value = value;
     }
     
     @Override
     public boolean equals(Object o) {
         if (o == this) return true;
         if (o == null || !(o instanceof SimpleContent)) return false;
         return value.equals(((SimpleContent)o).value);
     }
     
     @Override
     public int hashCode() {
         return value.hashCode();
     }
     
     @Override
     public String toString() {
         return value;
     }
 
 }

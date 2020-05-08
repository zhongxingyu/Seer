 package com.zenika.dorm.core.graph.impl;
 
 import com.zenika.dorm.core.exception.CoreException;
 
 /**
  * Similar to Configuration in ivy or Scope in maven
  * Maybe find better name for that ?
  *
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class Usage {
 
     private static final String DEFAULT = "default";
     private static final String INTERNAL_KEYWORD = "dorm_internal_";
 
     private String name;
 
     public static Usage create() {
         return new Usage(Usage.DEFAULT);
     }
 
     public static Usage create(String name) {
 
         // check if name is not reserved for internal usages
        if (name.length() >= Usage.INTERNAL_KEYWORD.length() &&
                name.substring(Usage.INTERNAL_KEYWORD.length()).equalsIgnoreCase(Usage.INTERNAL_KEYWORD)) {
             throw new CoreException(name + " is reserved for internal usages. Use the factory methods.");
         }
 
         return new Usage(name);
     }
 
     public static Usage createInternal(String name) {
         return new Usage(INTERNAL_KEYWORD + name);
     }
 
     /**
      * @param name
      * @deprecated use the factory methods
      */
     public Usage(String name) {
 
         if (null == name) {
             name = Usage.DEFAULT;
         }
 
         this.name = name;
     }
 
     @Override
     public String toString() {
         return name;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Usage usage = (Usage) o;
 
         if (!name.equals(usage.name)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     public String getName() {
         return name;
     }
 }

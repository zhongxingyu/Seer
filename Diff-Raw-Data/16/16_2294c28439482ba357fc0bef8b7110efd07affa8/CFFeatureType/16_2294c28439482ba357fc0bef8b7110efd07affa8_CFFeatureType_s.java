 package com.axiomalaska.cf4j;
 
 /**
  * Simple class to hold info for CF Feature types.
 * @see <a href="http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html">http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html</a>.  
  */
 public class CFFeatureType implements Comparable<CFFeatureType> {
     private String name;
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public CFFeatureType(String name) {
         super();
         this.name = name;
     }
 
     public int compareTo(CFFeatureType o) {
         return this.name.compareTo( o.getName() );
     }
 }

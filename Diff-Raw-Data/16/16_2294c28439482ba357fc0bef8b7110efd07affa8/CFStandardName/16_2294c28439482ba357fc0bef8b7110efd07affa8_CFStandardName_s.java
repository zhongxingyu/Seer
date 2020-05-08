 package com.axiomalaska.cf4j;
 
 /**
  * Simple class to hold info for CF standard variable names.
 * @see <a href="http://cf-pcmdi.llnl.gov/documents/cf-standard-names/">http://cf-pcmdi.llnl.gov/documents/cf-standard-names/</a>.  
  */
 public class CFStandardName implements Comparable<CFStandardName>{
     private String name;
     private String canonicalUnits;
     private String description;
     private String grib;
     private String amip;
     
     public CFStandardName(String name, String canonicalUnits,
             String description, String grib, String amip) {
         super();
         this.name = name;
         this.canonicalUnits = canonicalUnits;
         this.description = description;
         this.grib = grib;
         this.amip = amip;
     }
 
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public String getCanonicalUnits() {
         return canonicalUnits;
     }
     public void setCanonicalUnits(String canonicalUnits) {
         this.canonicalUnits = canonicalUnits;
     }
     public String getDescription() {
         return description;
     }
     public void setDescription(String description) {
         this.description = description;
     }
     
     /**
      * Get the GRIB name for this variable, if one exists.
      * @return the GRIB equivalent name
      */    
     public String getGrib() {
         return grib;
     }
     public void setGrib(String grib) {
         this.grib = grib;
     }
     
     /**
      * Get the Atmosphereic Model Intercomparison Project (AMIP) name for this variable, if one exists.
      * @return the AMIP equivalent name
      */
     public String getAmip() {
         return amip;
     }
     public void setAmip(String amip) {
         this.amip = amip;
     }
 
     public int compareTo(CFStandardName o) {
         return this.name.compareTo( o.getName() );
     }
 }

 package com.nequissimus.library.os;
 
 /**
  * Enumeration of operating system that can determine the currently used one.
  * @author Tim Steinbach
  */
 public enum OSystem {
 
     /**
      * Linux.
      */
     LINUX("nux"),
 
     /**
      * Apple Mac OS X.
      */
     MACOSX("mac"),
 
     /**
      * UNIX.
      */
     UNIX("nix"),
 
     /**
      * Unknown OS.
      */
    UNKNOWN(""),

    /**
     * Microsoft Window.
     */
    WINDOWS("win");
 
     /**
      * Search string.
      */
     private final String search;
 
     /**
      * Create new enumeration instance.
      * @param searchString Search string
      */
     private OSystem(final String searchString) {
 
         this.search = searchString;
 
     }
 
     /**
      * Get the currently used operating system.
      * @return Current OS
      */
     public static OSystem getCurrentOs() {
 
         final String system = System.getProperty("os.name").toLowerCase();
 
         final OSystem[] values = OSystem.values();
 
         for (final OSystem osystem : values) {
 
            if (system.indexOf(osystem.search) > -1) {
                 return osystem;
             }
 
         }
 
         return UNKNOWN;
 
     }
 
 }

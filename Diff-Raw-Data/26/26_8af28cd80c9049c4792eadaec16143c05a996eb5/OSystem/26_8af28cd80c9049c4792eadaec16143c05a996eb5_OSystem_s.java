 package com.nequissimus.library.os;
 
 /**
  * Enumeration of operating system that can determine the currently used one.
  * @author Tim Steinbach
  */
 public enum OSystem {
 
     /**
     * Microsoft Window.
     */
    WINDOWS("win"),

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
    UNKNOWN("");
 
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
 
            if (system.indexOf(osystem.search) > 0) {
                 return osystem;
             }
 
         }
 
         return UNKNOWN;
 
     }
 
 }

 package com.osdiab.patient_organizer;
 
 /**
  * Author: odiab
  * Date: 8/8/13
  * Time: 12:23 PM
  * Represents a doctor to schedule, and the folder prefixes associated with them
  */
 public enum Doctor {
     DIAB("DIAB M.D.", "", true),
     IGNAT("IGNAT MD", "Ignat "),
     SEGAL("SEGAL D.O.", "Segal ");
 
     private final String recordName;
     private final String prefix;
    private final boolean useOpgitionalSuffix;
 
 
     private Doctor(String recordName, String prefix)
     {
         this.recordName = recordName;
         this.prefix = prefix;
         this.useOptionalSuffix = false;
     }
 
     private Doctor(String recordName, String prefix, boolean useOptionalSuffix)
     {
         this.recordName = recordName;
         this.prefix = prefix;
         this.useOptionalSuffix = useOptionalSuffix;
     }
 
     public String getPrefix()
     {
         return prefix;
     }
 
     public String getRecordName()
     {
         return recordName;
     }
 
     public boolean shouldUseOptionalSuffix()
     {
         return useOptionalSuffix;
     }
 }

 package fr.cg95.cvq.business.users;
 
 public enum NationalityType {
 
     FRENCH("French"),
     EUROPEAN_UNION("EuropeanUnion"),
     OUTSIDE_EUROPEAN_UNION("OutsideEuropeanUnion");
 
     private String legacyLabel;
 
     public String getLegacyLabel() {
         return legacyLabel;
     }
 
     private NationalityType(String legacyLabel) {
         this.legacyLabel = legacyLabel;
     }
 
     /**
      * @deprecated only for backward, use values()
      */
     @Deprecated
     public static NationalityType[] allNationalityTypes = NationalityType.values();
     
     public static NationalityType getDefaultNationalityType() {
         return FRENCH;
     }
     
     public static NationalityType forString(String enumAsString) {
         if (enumAsString == null || enumAsString.equals(""))
             return FRENCH;
         
         if (enumAsString.equals(FRENCH.toString()))
             return FRENCH;
         else if (enumAsString.equals(EUROPEAN_UNION.toString()))
             return EUROPEAN_UNION;
         else if (enumAsString.equals(OUTSIDE_EUROPEAN_UNION))
             return OUTSIDE_EUROPEAN_UNION;
         
         return FRENCH;
     }

    @Override
    public String toString() {
        return legacyLabel;
    }
 }

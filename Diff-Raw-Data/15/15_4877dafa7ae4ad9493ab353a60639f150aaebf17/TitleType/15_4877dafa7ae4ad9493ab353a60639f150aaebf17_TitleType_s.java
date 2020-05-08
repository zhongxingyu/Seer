 package fr.cg95.cvq.business.users;
 
 /**
  * @author bor@zenexity.fr
  */
 public enum TitleType {
     MISTER("Mister"),
     MADAM("Madam"),
     MISS("Miss"),
     AGENCY("Agency"),
     UNKNOWN("Unknown");
 
     private String legacyLabel;
 
     public String getLegacyLabel() {
         return legacyLabel;
     }
 
     /**
      * Prevent instantiation and subclassing with a private constructor.
      */
     private TitleType(String legacyLabel) {
         this.legacyLabel = legacyLabel;
     }
 
     /**
      * @deprecated only for backward, use values() instead
      */
     public static final TitleType[] allTitleTypes = TitleType.values();
 
     public static TitleType getDefaultTitleType() {
         return UNKNOWN;
     }
 
     public static TitleType forString(String enumAsString) {
         if (enumAsString == null || enumAsString.equals(""))
             return UNKNOWN;
 
         if (enumAsString.equals(MISTER.toString()))
             return MISTER;
         else if (enumAsString.equals(MADAM.toString()))
             return MADAM;
         else if (enumAsString.equals(MISS.toString()))
             return MISS;
         else if (enumAsString.equals(AGENCY.toString()))
             return AGENCY;
 
         return UNKNOWN;
     }

    @Override
    public String toString() {
        return legacyLabel;
    }
 }

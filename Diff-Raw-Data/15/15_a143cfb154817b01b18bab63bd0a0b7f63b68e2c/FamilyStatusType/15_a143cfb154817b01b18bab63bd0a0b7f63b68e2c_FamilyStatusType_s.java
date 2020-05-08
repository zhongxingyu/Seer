 package fr.cg95.cvq.business.users;
 
 /**
  * @author bor@zenexity.fr
  */
 public enum FamilyStatusType {
 
 
     SINGLE("Single"),
     DIVORCED("Divorced"),
     WIDOW("Widow"),
     MARRIED("Married"),
     COMMON_LAW_MARRIAGE("CommonLawMarriage"),
     SEPARATED("Separated"),
     PACS("PACS"),
     OTHER("Other");
 
     private String legacyLabel;
 
     public String getLegacyLabel() {
         return legacyLabel;
     }
 
     /**
      * Prevent instantiation and subclassing with a private constructor.
      */
     private FamilyStatusType(String legacyLabel) {
         this.legacyLabel = legacyLabel;
     }
 
 
     /**
      * @deprecated only for backward, use FamilyStatusType.values() instead
      */
     public static final FamilyStatusType[] allFamilyStatusTypes = FamilyStatusType.values();
 
     public static FamilyStatusType getDefaultFamilyStatusType() {
         return OTHER;
     }
 
     public static FamilyStatusType forString(String enumAsString) {
         if (enumAsString == null || enumAsString.equals(""))
             return OTHER;
 
         if (enumAsString.equals(SINGLE.toString()))
             return SINGLE;
         else if (enumAsString.equals(DIVORCED.toString()))
             return DIVORCED;
         else if (enumAsString.equals(WIDOW.toString()))
             return WIDOW;
         else if (enumAsString.equals(MARRIED.toString()))
             return MARRIED;
         else if (enumAsString.equals(COMMON_LAW_MARRIAGE.toString()))
             return COMMON_LAW_MARRIAGE;
         else if (enumAsString.equals(SEPARATED.toString()))
             return SEPARATED;
         else if (enumAsString.equals(PACS.toString()))
             return PACS;
         else if (enumAsString.equals(OTHER.toString()))
             return OTHER;
 
         return OTHER;
     }

    @Override
    public String toString() {
        return legacyLabel;
    }
 }

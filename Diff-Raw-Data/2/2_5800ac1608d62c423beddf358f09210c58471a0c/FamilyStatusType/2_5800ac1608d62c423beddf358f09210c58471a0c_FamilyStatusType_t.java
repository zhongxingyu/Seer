 package fr.cg95.cvq.business.users;
 
 import fr.cg95.cvq.dao.hibernate.PersistentStringEnum;
 
 /**
  * @author bor@zenexity.fr
  */
 public final class FamilyStatusType extends PersistentStringEnum {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final FamilyStatusType SINGLE = new FamilyStatusType("Single");
     public static final FamilyStatusType DIVORCED = new FamilyStatusType("Divorced");
     public static final FamilyStatusType WIDOW = new FamilyStatusType("Widow");
     public static final FamilyStatusType MARRIED = new FamilyStatusType("Married");
     public static final FamilyStatusType COMMON_LAW_MARRIAGE = new FamilyStatusType("CommonLawMarriage");
    public static final FamilyStatusType PACS = new FamilyStatusType("PACS");
     public static final FamilyStatusType OTHER = new FamilyStatusType("Other");
 
     /**
      * Prevent instantiation and subclassing with a private constructor.
      */
     private FamilyStatusType(String status) {
         super(status);
     }
 
     public FamilyStatusType() {}
 
     public static final FamilyStatusType[] allFamilyStatusTypes = {
         SINGLE,
         DIVORCED,
         WIDOW,
         MARRIED,
         COMMON_LAW_MARRIAGE,
         PACS,
         OTHER,
     };
 
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
         else if (enumAsString.equals(PACS.toString()))
             return PACS;
         else if (enumAsString.equals(OTHER.toString()))
             return OTHER;
 
         return OTHER;
     }
 }

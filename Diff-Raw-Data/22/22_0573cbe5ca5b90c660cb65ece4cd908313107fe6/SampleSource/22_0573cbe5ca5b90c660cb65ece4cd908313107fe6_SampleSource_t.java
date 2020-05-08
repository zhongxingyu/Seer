 package org.pharmgkb.enums;
 
 import org.pharmgkb.util.ExtendedEnum;
 import org.pharmgkb.util.ExtendedEnumHelper;
 
 import java.util.Collection;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 9/4/12
  */
 public enum SampleSource implements ExtendedEnum {
  UNKNOWN(0, "0", "Unknown"),
   SERUM(1, "1", "Serum"),
   PLASMA(2, "2", "Plasma"),
   TISSUE(3, "3", "Tissue"),
   OTHERS(4, "4", "Others");
 
   private static ExtendedEnumHelper<SampleSource> s_extendedEnumHelper;
   private int m_id;
   private String m_shortName;
   private String m_displayName;
 
   SampleSource(int id, String shortName, String displayName) {
     m_id = id;
     m_shortName = shortName;
     m_displayName = displayName;
     init();
   }
 
   private synchronized void init() {
     if (s_extendedEnumHelper == null) {
       s_extendedEnumHelper = new ExtendedEnumHelper<SampleSource>();
     }
     s_extendedEnumHelper.add(this, m_id, m_shortName, m_displayName);
   }
 
   /**
    * Gets the Id of this enum.
    */
   public int getId() {
 
     return m_id;
   }
 
   /**
    * Gets the short name of this enum.
    */
   public String getShortName() {
 
     return m_shortName;
   }
 
   /**
    * Gets the display name of this enum.  Will return short name if no display name is defined.
    */
   public String getDisplayName() {
 
     if (m_displayName != null) {
       return m_displayName;
     }
     return m_shortName;
   }
 
 
   /**
    * Looks for the enum with the given Id.
    */
   public static SampleSource lookupById(int id) {
 
     return s_extendedEnumHelper.lookupById(id);
   }
 
   /**
    * Looks for the enum with the given name.
    */
   public static SampleSource lookupByName(String text) {
 
     return s_extendedEnumHelper.lookupByName(text);
   }
 
 
   /**
    * Gets all the enums sorted by Id.
    */
   public static Collection<SampleSource> getAllSortedById() {
 
     return s_extendedEnumHelper.getAllSortedById();
   }
 
   /**
    * Gets all the enums sorted by name.
    */
   public static Collection<SampleSource> getAllSortedByName() {
 
     return s_extendedEnumHelper.getAllSortedByName();
   }
 
 
   @Override
   public final String toString() {
 
     return getDisplayName();
   }
 
   }

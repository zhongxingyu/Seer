 package models.competition;
 
 import com.avaje.ebean.annotation.EnumValue;
 import models.EMessages;
 
 import java.util.LinkedHashMap;
 
 /**
  * An enumeration of contests types.
  *
  * @author Kevin Stobbelaar
  */
 public enum CompetitionType {
 
     /**
      * unrestricted
      */
     @EnumValue("UNRESTRICTED")
     UNRESTRICTED,
 
     /**
      * restricted
      */
     @EnumValue("RESTRICTED")
     RESTRICTED,
 
     /**
      * anonymous
      */
     @EnumValue("ANONYMOUS")
     ANONYMOUS;
 
    /**
     * Translate this enum
     * @return translation
     */
    public String translate(){
         return EMessages.get("competition.type." + this.name().toLowerCase());
     }
 
     /**
      * Returns the options for a select in template.
      * @return options map
      */
     public static LinkedHashMap<String, String> options() {
         LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put(RESTRICTED.name(), RESTRICTED.translate());
        options.put(UNRESTRICTED.name(), UNRESTRICTED.translate());
        options.put(ANONYMOUS.name(), ANONYMOUS.translate());
         return options;
     }
 
 }

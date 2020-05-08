 package gov.nih.nci.caintegrator2.application.study;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Possible wild card type values for <code>StringComparisonCriterion.wildCardType</code>.
  */
 public enum WildCardTypeEnum {
 
     /**
      * No Wild Card.
      */
     WILDCARD_OFF("wildCardOff"),
 
     /**
      * Wild Card after the given string.
      */
     WILDCARD_BEFORE_STRING("wildCardBeforeString"),
     
     /**
      * Wild Card before the given string.
      */
     WILDCARD_AFTER_STRING("wildCardAfterString"),
     
     /**
      * Wild Card before and after the given string.
      */
     WILDCARD_BEFORE_AND_AFTER_STRING("wildCardBeforeAndAfterString");
     
     private static Map<String, WildCardTypeEnum> valueToTypeMap = 
                                         new HashMap<String, WildCardTypeEnum>();
 
     private String value;
     
     private WildCardTypeEnum(String value) {
         this.value = value;
     }
 
     /**
      * @return the value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * @param value the value to set
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     private static Map<String, WildCardTypeEnum> getValueToTypeMap() {
         if (valueToTypeMap.isEmpty()) {
             for (WildCardTypeEnum type : values()) {
                 valueToTypeMap.put(type.getValue(), type);
             }
         }
         return valueToTypeMap;
     }
     
     /**
     * Returns the <code>WildCardTypeEnum</code> corresponding to the given value. Returns null
      * for null value.
      * 
      * @param value the value to match
      * @return the matching type.
      */
     public static WildCardTypeEnum getByValue(String value) {
         checkType(value);
         return getValueToTypeMap().get(value);
     }
 
     /**
      * Checks to see that the value given is a legal <code>AssayType</code> value.
      * 
      * @param value the value to check;
      */
     public static void checkType(String value) {
         if (value != null && !getValueToTypeMap().containsKey(value)) {
             throw new IllegalArgumentException("No matching type for " + value);
         }
     }
 }

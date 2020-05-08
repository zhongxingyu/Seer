 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import gov.nih.nci.cabig.ctms.domain.CodedEnum;
 import org.apache.commons.lang.StringUtils;
 
 import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.getByClassAndCode;
 import static gov.nih.nci.cabig.ctms.domain.CodedEnumHelper.register;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author Saurabh Agrawal
  */
 public enum Gender implements CodedEnum<String> {
 
    NOT_REPORTED("Not Reported", new String[]{"not reported"}),
    UNKNOWN("Unknown", new String[]{"unknown"}),
    FEMALE("Female", new String[]{"f", "female"}),
    MALE("Male", new String[]{"m", "male"});
 
     private String displayName;
     private String[] variations;
 
     private Gender(final String displayName, final String[] variations) {
         this.displayName = displayName;
         this.variations = variations;
         register(this);
 
     }
 
     public String getCode() {
         return getDisplayName();
     }
 
     public String[] getVariations() {
         return variations;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public static Map<String, String> getGenderMap() {
         Map<String, String> genders = new TreeMap<String, String>();
 
         genders.put(Gender.MALE.getCode(), Gender.MALE.getDisplayName());
         genders.put(Gender.FEMALE.getCode(), Gender.FEMALE.getDisplayName());
         genders.put(Gender.NOT_REPORTED.getCode(), Gender.NOT_REPORTED.getDisplayName());
         genders.put(Gender.UNKNOWN.getCode(), Gender.UNKNOWN.getDisplayName());
 
         return genders;
     }
     
 
     public static Map<String, String[]> getGenderValueMap() {
         Map<String, String[]> genders = new TreeMap<String, String[]>();
 
         genders.put(Gender.MALE.getCode(), Gender.MALE.getVariations());
         genders.put(Gender.FEMALE.getCode(), Gender.FEMALE.getVariations());
         genders.put(Gender.NOT_REPORTED.getCode(), Gender.NOT_REPORTED.getVariations());
         genders.put(Gender.UNKNOWN.getCode(), Gender.UNKNOWN.getVariations());
 
         return genders;
     }    
 
     
     public static String getCodeByVariation(String code) {
         Map<String, String[]> keyVariations = Gender.getGenderValueMap();
         for (String key : keyVariations.keySet()) {
             String[] variations = keyVariations.get(key);
             for (String variation : variations) {
                if (variation.equals(code.toLowerCase())) {
                     return key;
                 }
             }
         }
         return null;
     }
 
     public static Gender getByCode(String code) {
         Gender gender = getByClassAndCode(Gender.class, code);
         if (gender == null) {
             if (code != null && code.length()>0) {
                 String keyCode = getCodeByVariation(code);
                 if (keyCode == null) {
                     throw new StudyCalendarValidationException(
                         "The specified gender '%s' is invalid: Please check the spelling.", code);
                 } else {
                     gender = getByCode(keyCode);
                 }
             }
         }
         return gender;
     }
 }
 

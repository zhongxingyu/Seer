 package com.github.croesch.partimana.i18n;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import com.github.croesch.annotate.NotNull;
 
 /**
  * This class provides access to the text properties file.
  * 
  * @author croesch
  * @since Date: Mar 07, 2011
  */
 public enum Text {
 
   /** the name of the program */
   PARTIMANA (0),
   /** the string that contains the version */
   VERSION (1),
 
   /** the name of the selected language */
   LANGUAGE (10),
   /** the constant yes */
   YES (11),
   /** the constant no */
   NO (12),
   /** the constant cancel */
   CANCEL (13),
   /** the constant ok */
   OK (14),
   /** the constant apply */
   APPLY (15),
   /** the constant close */
   CLOSE (16),
   /** the constant exit */
   EXIT (17),
   /** the constant save */
   SAVE (18),
 
   /** the name for the file menu */
   FILE (20),
 
   /** the name for entry create new participant */
   PARTICIPANT_NEW (31),
   /** the name for entry delete selected participant */
   PARTICIPANT_DELETE (32),
 
   /** the name for participant */
   PARTICIPANT (100),
   /** the name for the id of participant */
   PARTICIPANT_ID (101),
   /** the name for the last name of the participant */
   PARTICIPANT_LASTNAME (102),
   /** the name for the first name of the participant */
   PARTICIPANT_FORENAME (103),
   /** the name for the gender of the participant */
   PARTICIPANT_GENDER (104),
   /** the name of the denomination */
   PARTICIPANT_DENOMINTAION (105),
   /** the name of the birthday */
   PARTICIPANT_BIRTHDAY (106),
   /** the name for living address */
   PARTICIPANT_ADDRESS_LIVING (107),
   /** the name for postal address */
   PARTICIPANT_ADDRESS_POSTAL (108),
   /** the name for phone number */
   PARTICIPANT_PHONE (109),
   /** the name for fax number */
   PARTICIPANT_FAX (110),
   /** the name for mobile phone number */
   PARTICIPANT_MOBILE_PHONE (111),
   /** the name for phone number of parents */
   PARTICIPANT_PHONE_OF_PARENTS (112),
   /** the name for mail address */
   PARTICIPANT_MAIL_ADDRESS (113),
   /** the name for county council */
   PARTICIPANT_COUNTY_COUNCIL (114),
   /** the name for bank code number */
   PARTICIPANT_BANK_CODE_NUMBER (115),
   /** the name for bank */
   PARTICIPANT_BANK_NAME (116),
   /** the name for bank account number */
   PARTICIPANT_BANK_ACCOUNT_NUMBER (117),
   /** the name for comment */
   PARTICIPANT_COMMENT (118),
   /** the name for date since in */
   PARTICIPANT_DATE_SINCE (119),
   /** the name for date until in */
   PARTICIPANT_DATE_UNTIL (120),
   /** the name for camp participant */
   PARTICIPANT_CAMP_PARTICIPANT (121),
   /** the name for staff general */
   PARTICIPANT_STAFF_GENERAL (122),
   /** the name for staff youth */
   PARTICIPANT_STAFF_YOUTH (123),
   /** the name for board */
   PARTICIPANT_BOARD (124),
   /** the name for extended board */
   PARTICIPANT_EXTENDED_BOARD (125),
   /** the name for MAK */
   PARTICIPANT_MAK (126),
   /** the name for AGE */
   PARTICIPANT_AGE (127),
   /** the name for camp kitchen */
   PARTICIPANT_CAMP_KITCHEN (128),
   /** the name for seminar */
   PARTICIPANT_SEMINAR (129),
   /** the name for misc */
   PARTICIPANT_MISC (130),
   /** the name for heading since when the participant is in the system */
   PARTICIPANT_SINCE (131),
   /** the name for heading until when the participant is in the system */
   PARTICIPANT_UNTIL (132),
   /** the name for heading possible functions of the participant */
   PARTICIPANT_FUNCTIONS (133),
 
   /** the name for camp */
   CAMP (200),
   /** the name for the id of the camp */
   CAMP_ID (201),
   /** the name for the name of the camp */
   CAMP_NAME (202),
   /** the name for the location of the camp */
   CAMP_LOCATION (203),
 
   /** constant for male */
   MALE (2000),
   /** constant for female */
   FEMALE (2001),
 
   /** constant for evangelic */
   EVANGELIC (2010),
   /** constant for catholic */
   CATHOLIC (2011),
   /** constant for orthodox */
   ORTHODOX (2012),
   /** constant for muslim */
   MUSLIM (2013),
   /** constant for free church */
   FREE_CHURCH (2014),
   /** constant for jewish */
   JEWISH (2015),
   /** constant for other denomination */
   OTHER_DENOMINATION (2016),
   /** constant for no denomination */
   NO_DENOMINATION (2017),
 
   /** constant for street */
   STREET (2020),
   /** constant for post code */
   POST_CODE (2021),
   /** constant for city */
   CITY (2022),
 
   /** constant for unknown county council */
   UNKNOWN (2030),
   /** constant for county of alzey */
   COUNTY_ALZEY (2031),
   /** constant for county of bad kreuznach */
   COUNTY_BAD_KREUZNACH (2032),
   /** constant for county of bad duerkheim */
   COUNTY_BAD_DUERKHEIM (2033),
   /** constant for city of frankenthal */
   CITY_FRANKENTHAL (2034),
   /** constant for county of germersheim */
   COUNTY_GERMERSHEIM (2035),
   /** constant for city of kaiserslautern */
   CITY_KAISERSLAUTERN (2036),
   /** constant for county of kaiserslautern */
   COUNTY_KAISERSLAUTERN (2037),
   /** constant for county of kirchheimbolanden */
   COUNTY_KIRCHHEIMBOLANDEN (2038),
   /** constant for county of kusel */
   COUNTY_KUSEL (2039),
   /** constant for city of landau */
   CITY_LANDAU (2040),
   /** constant for city of ludwigshafen */
   CITY_LUDWIGSHAFEN (2041),
   /** constant for county of rhein-pfalz */
   COUNTY_RHEIN_PFALZ (2042),
   /** constant for city of neustadt */
   CITY_NEUSTADT (2043),
   /** constant for city of pirmasens */
   CITY_PIRMASENS (2044),
   /** constant for city of speyer */
   CITY_SPEYER (2045),
   /** constant for county of suedliche weinstrasse */
   COUNTY_SUEDLICHE_WEINSTRASSE (2046),
   /** constant for county of */
   COUNTY_SUEDWESTPFALZ (2047),
   /** constant for city of zweibruecken */
   CITY_ZWEIBRUECKEN (2048),
   /** constant for another county council */
   OTHER (2049),
 
   /** the message to show which language is selected */
   DEBUG_SELECTED_LANGUAGE (10000),
   /** the message to show which look and feel is selected */
   DEBUG_SELECTED_LAF (10001),
   /** the message to indicate that program is starting */
   DEBUG_PROGRAM_STARTING (10005),
   /** the message to indicate that program is sending the message to exit */
   DEBUG_PROGRAM_EXIT_NOTIFICATION (10009),
   /** the message to indicate that program is exiting */
   DEBUG_PROGRAM_EXITING (10010),
 
   /** information that participant has been saved */
   INFO_PARTICIPANT_SAVED (11010),
 
   /** warning for an unknown action */
   WARN_UNKNOWN_ACTION (12000),
 
   /** error - an exception */
   ERROR_EXCEPTION (13000),
   /** error when participant cannot be saved */
   ERROR_PARTICIPANT_NOT_SAVED (13010);
 
   /** the value of this instance */
   @NotNull
   private final String string;
 
   /**
    * Constructs a new instance of a text that is part of the i18n. Each key will be searched in the file
    * 'lang/text*.properties' (where '*' is a string build from the locales properties language, country and variant, so
    * there will be four file names and the specific will be searched first). The name of this enumeration is the suffix
    * of the key where underscores will be replaced by dots. The prefix is {@code tetris.txt.}.
    * 
    * @author croesch
   * @since Date: 20.02.2011 11:23:31
    * @param k the key number for the message to lookup
    */
   private Text(final int k) {
     final ResourceBundle b = ResourceBundle.getBundle("lang/text", new XMLBundleControl());
     final String key = String.valueOf(k);
     String value;
     try {
       value = b.getString(key);
     } catch (final MissingResourceException mre) {
       value = "!!missing-key=" + key + "!!";
     }
     this.string = value;
   }
 
   @Override
   @NotNull
   public String toString() {
     return text();
   }
 
   /**
    * String representation of this object
    * 
    * @return the String that represents the object
    */
   @NotNull
   public String text() {
     return this.string;
   }
 
   /**
    * String representation of this object, but {x} will be replaced by argument number x starting to count from 0.
    * 
    * @param s the replacements
    * @return the String that represents the object with replaced placeholders
    */
   @NotNull
   public String text(@NotNull final Object ... s) {
     String text = this.string;
     for (int i = 0; i < s.length; ++i) {
       // prevent exceptions with using $
       final String param = s[i].toString().replaceAll("\\$", "\\\\\\$");
       text = text.replaceAll("(^|[^{])\\{" + i + "\\}", "$1" + param);
       text = text.replaceAll("\\{\\{" + i + "\\}", "\\{" + i + "\\}");
     }
     return text;
   }
 }

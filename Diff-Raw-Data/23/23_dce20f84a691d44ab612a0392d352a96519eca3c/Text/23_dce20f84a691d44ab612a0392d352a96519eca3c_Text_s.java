 package com.github.croesch.partimana.i18n;
 
 import com.github.croesch.annotate.NotNull;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * This class provides access to the text properties file.
  *
  * @author croesch
  * @since Date: Mar 07, 2011
  */
 public enum Text {
 
   /** the name of the program */
   PARTIMANA(0),
   /** the string that contains the version */
   VERSION(1),
 
   /** the name of the selected language */
   LANGUAGE(10),
   /** the constant yes */
   YES(11),
   /** the constant no */
   NO(12),
   /** the constant cancel */
   CANCEL(13),
   /** the constant ok */
   OK(14),
   /** the constant apply */
   APPLY(15),
   /** the constant close */
   CLOSE(16),
   /** the constant exit */
   EXIT(17),
   /** the constant save */
   SAVE(18),
   /** the constant select */
   SELECT(19),
 
   /** the name for the file menu */
   FILE(20),
 
   /** the name for entry create new participant */
   PARTICIPANT_NEW(31),
   /** the name for entry delete selected participant */
   PARTICIPANT_DELETE(32),
 
   /** the name for the search menu */
   SEARCH(50),
   /** the title of the search frame */
   SEARCH_TITLE(51),
   /** the name for the menu item to search for participants */
   SEARCH_PARTICIPANT(100),
   /** the name for the menu item to search for camps */
   SEARCH_CAMP(200),
 
   /** warning title for the user */
   USER_WARNING(60),
   /** message that continuing leads in overriding a file */
   CONTINUE_OVERRIDES_FILE(61),
   /** message that continuing leads in removing the selected participant from the camp. */
   CONTINUE_REMOVES_PARTICIPANT_FROM_CAMP(62),
 
   /** the name for participant */
   PARTICIPANT(100),
   /** the name for the id of participant */
   PARTICIPANT_ID(101),
   /** the name for the last name of the participant */
   PARTICIPANT_LASTNAME(102),
   /** the name for the first name of the participant */
   PARTICIPANT_FORENAME(103),
   /** the name for the gender of the participant */
   PARTICIPANT_GENDER(104),
   /** the name of the denomination */
  PARTICIPANT_DENOMINTAION(105),
   /** the name of the birthday */
   PARTICIPANT_BIRTHDAY(106),
   /** the name for living address */
   PARTICIPANT_ADDRESS_LIVING(107),
   /** the name for postal address */
   PARTICIPANT_ADDRESS_POSTAL(108),
   /** the name for phone number */
   PARTICIPANT_PHONE(109),
   /** the name for fax number */
   PARTICIPANT_FAX(110),
   /** the name for mobile phone number */
   PARTICIPANT_MOBILE_PHONE(111),
   /** the name for phone number of parents */
   PARTICIPANT_PHONE_OF_PARENTS(112),
   /** the name for mail address */
   PARTICIPANT_MAIL_ADDRESS(113),
   /** the name for county council */
   PARTICIPANT_COUNTY_COUNCIL(114),
   /** the name for bank code number */
   PARTICIPANT_BANK_CODE_NUMBER(115),
   /** the name for bank */
   PARTICIPANT_BANK_NAME(116),
   /** the name for bank account number */
   PARTICIPANT_BANK_ACCOUNT_NUMBER(117),
   /** the name for comment */
   PARTICIPANT_COMMENT(118),
   /** the name for date since in */
   PARTICIPANT_DATE_SINCE(119),
   /** the name for date until in */
   PARTICIPANT_DATE_UNTIL(120),
   /** the name for heading since when the participant is in the system */
   PARTICIPANT_SINCE(121),
   /** the name for heading until when the participant is in the system */
   PARTICIPANT_UNTIL(122),
 
   /** the name for entry create new camp */
   CAMP_NEW(31),
   /** the name for entry delete selected camp */
   CAMP_DELETE(32),
   /** the name for entry cancel selected camp */
   CAMP_CANCEL(33),
   /** the name for entry save camp to csv */
   CAMP_TO_CSV(34),
 
   /** the name for camp */
   CAMP(200),
   /** the name for the id of the camp */
   CAMP_ID(201),
   /** the name for the name of the camp */
   CAMP_NAME(202),
   /** the name for the location of the camp */
   CAMP_LOCATION(203),
   /** the text for the beginning date of the camp */
   CAMP_DATE_FROM(204),
   /** the text for the ending date of the camp */
   CAMP_DATE_TO(205),
   /** the text for the rate of the camp */
   CAMP_RATE_PER_PARTICIPANT(206),
   /** the text for the per-day-rate of the camp */
   CAMP_RATE_PER_DAY(207),
   /** the text for the date when a date has been cancelled */
   CAMP_CANCELLED_ON(208),
 
   /** the text for the role the participant has in a camp */
   CAMP_PARTICIPANT_ROLE(300),
   /** the text for the role direction */
   CAMP_PARTICIPANT_DIRECTION(301),
   /** the text for the role staff */
   CAMP_PARTICIPANT_STAFF(302),
   /** the text for the role kitchen staff */
   CAMP_PARTICIPANT_KITCHEN_STAFF(303),
   /** the text for the role participant */
   CAMP_PARTICIPANT_PARTICIPANT(304),
   /** the text for the role day-child. */
   CAMP_PARTICIPANT_DAY_CHILD(305),
 
   /** constant for male */
   MALE(2000),
   /** constant for female */
   FEMALE(2001),
 
   /** constant for evangelic */
   EVANGELIC(2010),
   /** constant for catholic */
   CATHOLIC(2011),
   /** constant for orthodox */
   ORTHODOX(2012),
   /** constant for muslim */
   MUSLIM(2013),
   /** constant for free church */
   FREE_CHURCH(2014),
   /** constant for jewish */
   JEWISH(2015),
   /** constant for other denomination */
   OTHER_DENOMINATION(2016),
   /** constant for no denomination */
   NO_DENOMINATION(2017),
 
   /** constant for street */
   STREET(2020),
   /** constant for post code */
   POST_CODE(2021),
   /** constant for city */
   CITY(2022),
 
   /** description of the and-filter group */
   FILTER_AND(3010),
   /** description of the or-filter group */
   FILTER_OR(3011),
 
   /** description of category <em>bank account number</em> of a participant */
   FILTER_CAT_PARTICIPANT_BANK_ACC_NUM(117),
   /** description of category <em>bank code number</em> of a participant */
   FILTER_CAT_PARTICIPANT_BANK_CODE_NUM(115),
   /** description of category <em>bank name</em> of a participant */
   FILTER_CAT_PARTICIPANT_BANK_NAME(116),
   /** description of category <em>birthday</em> of a participant */
   FILTER_CAT_PARTICIPANT_BIRTHDAY(106),
   /** description of category <em>can be AGE</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_AGE(127),
   /** description of category <em>can be board</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_BOARD(124),
   /** description of category <em>can be extended board</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_EXTENDED_BOARD(125),
   /** description of category <em>can be kitchen</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_KITCHEN(128),
   /** description of category <em>can be MAK</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_MAK(126),
   /** description of category <em>can be misc</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_MISC(130),
   /** description of category <em>can be participant</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_PARTICIPANT(121),
   /** description of category <em>can be seminar</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_SEMINAR(129),
   /** description of category <em>can be staff</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_STAFF(122),
   /** description of category <em>can be staff youth</em> of a participant */
   FILTER_CAT_PARTICIPANT_CAN_BE_STAFF_YOUTH(123),
   /** description of category <em>comment</em> of a participant */
   FILTER_CAT_PARTICIPANT_COMMENT(118),
   /** description of category <em>county council</em> of a participant */
   FILTER_CAT_PARTICIPANT_COUNTY_COUNCIL(114),
   /** description of category <em>date since</em> of a participant */
   FILTER_CAT_PARTICIPANT_DATE_SINCE(119),
   /** description of category <em>date until</em> of a participant */
   FILTER_CAT_PARTICIPANT_DATE_UNTIL(120),
   /** description of category <em>denomination</em> of a participant */
   FILTER_CAT_PARTICIPANT_DENOMINATION(105),
   /** description of category <em>fax</em> of a participant */
   FILTER_CAT_PARTICIPANT_FAX(110),
   /** description of category <em>fore name</em> of a participant */
   FILTER_CAT_PARTICIPANT_FORE_NAME(103),
   /** description of category <em>gender</em> of a participant */
   FILTER_CAT_PARTICIPANT_GENDER(104),
   /** description of category <em>last name</em> of a participant */
   FILTER_CAT_PARTICIPANT_LAST_NAME(102),
   /** description of category <em>living city</em> of a participant */
   FILTER_CAT_PARTICIPANT_LIVING_CITY(3100),
   /** description of category <em>living postal code</em> of a participant */
   FILTER_CAT_PARTICIPANT_LIVING_POSTAL_CODE(3101),
   /** description of category <em>living street</em> of a participant */
   FILTER_CAT_PARTICIPANT_LIVING_STREET(3102),
   /** description of category <em>mail address</em> of a participant */
   FILTER_CAT_PARTICIPANT_MAIL_ADDRESS(113),
   /** description of category <em>mobile phone</em> of a participant */
   FILTER_CAT_PARTICIPANT_MOBILE_PHONE(111),
   /** description of category <em>phone</em> of a participant */
   FILTER_CAT_PARTICIPANT_PHONE(109),
   /** description of category <em>phone of parents</em> of a participant */
   FILTER_CAT_PARTICIPANT_PHONE_OF_PARENTS(112),
   /** description of category <em>postal city</em> of a participant */
   FILTER_CAT_PARTICIPANT_POSTAL_CITY(3103),
   /** description of category <em>postal postal code</em> of a participant */
   FILTER_CAT_PARTICIPANT_POSTAL_POSTAL_CODE(3104),
   /** description of category <em>postal street</em> of a participant */
   FILTER_CAT_PARTICIPANT_POSTAL_STREET(3105),
 
   /** description of category <em>from</em> of a camp */
   FILTER_CAT_CAMP_FROM(204),
   /** description of category <em>location</em> of a camp */
   FILTER_CAT_CAMP_LOCATION(203),
   /** description of category <em>name</em> of a camp */
   FILTER_CAT_CAMP_NAME(202),
   /** description of category <em>rate per day</em> of a camp */
   FILTER_CAT_CAMP_RATE_PER_DAY(207),
   /** description of category <em>rate per participant</em> of a camp */
   FILTER_CAT_CAMP_RATE_PER_PART(206),
   /** description of category <em>until</em> of a camp */
   FILTER_CAT_CAMP_UNTIL(205),
 
   /** description of filtering objects equal to a specific object */
   FILTER_TYPE_EQUALS(3300),
   /** description of filtering objects not equal to a specific object */
   FILTER_TYPE_NOT_EQUALS(3301),
   /** description of filtering strings containing a specific string */
   FILTER_TYPE_CONTAINS(3310),
   /** description of filtering strings starting with a specific string */
   FILTER_TYPE_STARTS_WITH(3311),
   /** description of filtering strings ends with a specific string */
   FILTER_TYPE_ENDS_WITH(3312),
   /** description of filtering strings equal to a specific string (case-insensitive) */
   FILTER_TYPE_EQUALS_IGNORE_CASE(3315),
   /** description of filtering strings not equal to a specific string (case-insensitive) */
   FILTER_TYPE_NOT_EQUALS_IGNORE_CASE(3316),
   /** description of filtering integers greater than a specific integer */
   FILTER_TYPE_GREATER_THAN(3320),
   /** description of filtering integers less than a specific integer */
   FILTER_TYPE_LESS_THAN(3321),
   /** description of filtering dates before a specific date */
   FILTER_TYPE_BEFORE(3330),
   /** description of filtering dates after a specific date */
   FILTER_TYPE_AFTER(3331),
 
   /** constant for unknown county council */
   UNKNOWN(2030),
   /** constant for county of alzey */
   COUNTY_ALZEY(2031),
   /** constant for county of bad kreuznach */
   COUNTY_BAD_KREUZNACH(2032),
   /** constant for county of bad duerkheim */
   COUNTY_BAD_DUERKHEIM(2033),
   /** constant for city of frankenthal */
   CITY_FRANKENTHAL(2034),
   /** constant for county of germersheim */
   COUNTY_GERMERSHEIM(2035),
   /** constant for city of kaiserslautern */
   CITY_KAISERSLAUTERN(2036),
   /** constant for county of kaiserslautern */
   COUNTY_KAISERSLAUTERN(2037),
   /** constant for county of kirchheimbolanden */
   COUNTY_KIRCHHEIMBOLANDEN(2038),
   /** constant for county of kusel */
   COUNTY_KUSEL(2039),
   /** constant for city of landau */
   CITY_LANDAU(2040),
   /** constant for city of ludwigshafen */
   CITY_LUDWIGSHAFEN(2041),
   /** constant for county of rhein-pfalz */
   COUNTY_RHEIN_PFALZ(2042),
   /** constant for city of neustadt */
   CITY_NEUSTADT(2043),
   /** constant for city of pirmasens */
   CITY_PIRMASENS(2044),
   /** constant for city of speyer */
   CITY_SPEYER(2045),
   /** constant for county of suedliche weinstrasse */
   COUNTY_SUEDLICHE_WEINSTRASSE(2046),
   /** constant for county of */
   COUNTY_SUEDWESTPFALZ(2047),
   /** constant for city of zweibruecken */
   CITY_ZWEIBRUECKEN(2048),
   /** constant for another county council */
   OTHER(2049),
 
   /** the message to show which language is selected */
   DEBUG_SELECTED_LANGUAGE(10000),
   /** the message to show which look and feel is selected */
   DEBUG_SELECTED_LAF(10001),
   /** the message to indicate that program is starting */
   DEBUG_PROGRAM_STARTING(10005),
   /** the message to indicate that program is sending the message to exit */
   DEBUG_PROGRAM_EXIT_NOTIFICATION(10009),
   /** the message to indicate that program is exiting */
   DEBUG_PROGRAM_EXITING(10010),
 
   /** information that participant has been saved */
   INFO_PARTICIPANT_SAVED(11010),
   /** information that camp has been saved */
   INFO_CAMP_SAVED(11011),
   /** no camp selected to be saved */
   INFO_NO_CAMP_SELECTED(11012),
   /** camp successfully saved to csv */
   INFO_CAMP_SAVED_TO_CSV(11013),
 
   /** warning for an unknown action */
   WARN_UNKNOWN_ACTION(12000),
   /** warning for an unconsidered case */
   WARN_UNCONSIDERED_CASE(12001),
 
   /** error - an exception */
   ERROR_EXCEPTION(13000),
   /** error when participant cannot be saved */
   ERROR_PARTICIPANT_NOT_SAVED(13010),
   /** error when camp cannot be saved */
   ERROR_CAMP_NOT_SAVED(13011),
   /** error when referenced participant couldn't be loaded from database */
   ERROR_PARTICIPANT_NOT_IN_DB(13020);
 
   /** the value of this instance */
   @NotNull
   private final String string;
 
   /**
    * Constructs a new instance of a text that is part of the i18n. Each key will be searched in the file
    * 'lang/text*.properties' (where '*' is a string build from the locales properties language, country and variant, so
    * there will be four file names and the specific will be searched first). The name of this enumeration is the suffix
    * of the key where underscores will be replaced by dots. The prefix is {@code tetris.txt.}.
    *
    * @param k the key number for the message to lookup
    * @since Date: Feb 2, 2011
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
   public String text(final Object... s) {
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

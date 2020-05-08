 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.utilities;
 
 import org.apache.log4j.Logger;
 
 import java.lang.reflect.Field;
 
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 /**
  * This class is able to check a given String against some validation pattern.
  *
  * @author   mscholl
  * @version  1.3
  */
 public class NameValidator {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(NameValidator.class);
 
     /**
      * This type is to check a String whether it is a valid Java package name.<br/>
      * <br/>
      * A valid package name starts with a lower case letter and contains only letters from a-Z, numbers from 0-9 and the
      * character "_" in any combination.
      */
     public static final int NAME_PACKAGE = 1;
    static final String NAME_PACKAGE_REGEX = "[a-z][\\w]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices high security name pattern.<br/>
      * <br/>
      * A high security name pattern starts with a letter (a-Z) and contains only letters from a-Z, numbers from 0-9 and
      * the character "_" in any combination.
      */
     public static final int NAME_HIGH = 2;
     static final String NAME_HIGH_REGEX = "[a-zA-Z][\\w]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices high security name pattern but permits one single dot in
      * between to allow for schema namespaces.<br/>
      * <br/>
      * A high security schema pattern can start with a letter (a-Z) and contains only letters from a-Z, numbers from 0-9
      * and the character "_" in any combination, then a dot and then the same pattern as before.
      */
     public static final int SCHEMA_HIGH = 7;
     static final String SCHEMA_HIGH_REGEX = "([a-zA-Z][\\w]*\\.)?[a-zA-Z][\\w]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices medium security name pattern.<br/>
      * <br/>
      * A medium security name pattern contains only letters from a-Z, numbers from 0-9 and the characters "_" and "-" in
      * any combination.
      */
     public static final int NAME_MEDIUM = 3;
     static final String NAME_MEDIUM_REGEX = "[\\w\\-]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices medium security name pattern with additional german special
      * character support.<br/>
      * <br/>
      * A medium security german name pattern contains only letters from a-Z, numbers from 0-9, the characters "_", "-",
      * ".", any whitespace character, and the german special characters "Ää", "Öö", "Üü", "ß" in any combination.
      */
     public static final int NAME_MEDIUM_GERMAN = 4;
     static final String NAME_MEDIUM_GERMAN_REGEX = "[\\w\\-äöüÄÖÜß\\s\\.]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices low security name pattern with additional german special
      * character support.<br/>
      * <br/>
      * A low security german name pattern contains only letters from a-Z, numbers from 0-9, the characters "_", "-",
      * ".", "/", any whitespace character and the german special characters "Ää", "Öö", "Üü", "ß" in any combination.
      */
     public static final int NAME_LOW_GERMAN = 5;
     static final String NAME_LOW_GERMAN_REGEX = "[\\w\\-äöüÄÖÜß\\s/\\.]*"; // NOI18N
 
     /**
      * This type is to check a String whether it suffices medium security name pattern and allows whitespace characters.
      * <br/>
      * <br/>
      * A medium security name pattern contains only letters from a-Z, numbers from 0-9, any whitespace character and the
      * characters "_" and "-" in any combination.
      */
     public static final int NAME_MEDIUM_WHITESPACE = 6;
     static final String NAME_MEDIUM_WHITESPACE_REGEX = "[\\s\\w\\-]*"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Pattern pattern;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of NameValidator that can validate against the given pattern.
      *
      * @param  validationType  the type of pattern to be used within this NameValidator instance (one of the declared
      *                         static fields)
      */
     public NameValidator(final int validationType) {
         pattern = createPattern(validationType);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Validates a given name against the pattern that was chosen during creation of the NameValidator instance.
      *
      * @param   name  the String that shall be validated
      *
      * @return  true if the String suffices the chosen pattern, false otherwise. name == null or name.lenght() == 0
      *          always returns false.
      */
     public boolean isValid(final String name) {
         if ((name == null) || (name.length() == 0)) {
             return false;
         }
 
         return pattern.matcher(name).matches();
     }
 
     /**
      * Validates a given name against a given pattern.
      *
      * @param   name            the String that shall be validated
      * @param   validationType  the type of pattern to be used to validate the name (one of the declared static fields)
      *
      * @return  true if the String suffices the chosen pattern, false otherwise. name == null or name.lenght() == 0
      *          always returns false.
      */
     public static boolean isValid(final String name, final int validationType) {
         if ((name == null) || (name.length() == 0)) {
             return false;
         }
 
         return createPattern(validationType).matcher(name).matches();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   valType  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IllegalArgumentException  DOCUMENT ME!
      */
     private static Pattern createPattern(final int valType) {
         final Field[] fields = NameValidator.class.getFields();
         try {
             for (final Field field : fields) {
                 if (field.getInt(null) == valType) {
                     final String regex = NameValidator.class.getDeclaredField(field.getName() + "_REGEX")
                                 .get(null)
                                 .toString(); // NOI18N
 
                     return Pattern.compile(regex);
                 }
             }
         } catch (final PatternSyntaxException ex) {
             LOG.error("pattern could not be compiled", ex);      // NOI18N
             throw ex;
         } catch (final Exception ex) {
             final String message = "unsupported field modifier"; // NOI18N
             LOG.error(message, ex);
             throw new IllegalArgumentException(message, ex);
         }
         LOG.error("validation type not known: " + valType);      // NOI18N
         throw new IllegalArgumentException("validation type not known: " + valType); // NOI18N
     }
 }

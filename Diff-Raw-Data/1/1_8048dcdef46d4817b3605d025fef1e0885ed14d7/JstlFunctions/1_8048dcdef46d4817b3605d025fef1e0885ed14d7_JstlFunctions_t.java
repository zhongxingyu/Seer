 package eionet.eunis.util;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.URLEncoder;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ro.finsiel.eunis.WebContentManagement;
 import ro.finsiel.eunis.jrfTables.species.references.ReferencesJoinDomain;
 import ro.finsiel.eunis.jrfTables.species.references.ReferencesJoinPersist;
 import ro.finsiel.eunis.search.Utilities;
 import ro.finsiel.eunis.utilities.EunisUtil;
 
 /**
  * Collection of eunis JSTL functions.
  *
  * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
  */
 public class JstlFunctions {
 
     private static final Logger logger = Logger.getLogger(JstlFunctions.class);
 
     /**
      * jstl wrapper to factsheet.exists();
      *
      * @param suspicious
      * @return String
      */
     public static boolean exists(Object suspicious) {
        if(suspicious == null) return false;
         try {
             Method exists = suspicious.getClass().getMethod("exists");
             boolean result = false;
 
             if (exists != null) {
                 result = (Boolean) exists.invoke(suspicious);
             }
             return result;
         } catch (Exception e) {
             logger.error(e);
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get the text for a token in the content table.
      *
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cms(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cms(key) : key;
     }
 
     /**
      * Get the text for a token in the content table. Tokens can't contain apostrophes
      * Not to be used inside HTML attribute values. If we're in edit mode, then show an edit-icon.
      *
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cmsText(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cms(key) : key;
     }
 
 
     /**
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cmsTitle(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsTitle(key) : key;
     }
 
     /**
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cmsInput(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsInput(key) : key;
     }
 
     /**
      * @param cms
      * @param key
      * @return String
      */
     public static String cmsLabel(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsLabel(key) : key;
     }
 
     /**
      * Look up a short phrase (potentially HTML) in the content table.
      *
      * The cmsPhrase() is used for short strings - typically one liners. The argument is the phrase.
      * When the argument is looked up in the database, it is MD5 encoded first. This ensures that the
      * key kan fit in the ID_PAGE column in wiki:eunis_web_content.  In order to save resource we
      * bypass the database lookup when the language is English.
      *
      * @param cms - The class for web content
      * @param key - phrase to look up.
      * @return String - phrase to display on webpage.
      */
     public static String cmsPhrase(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsPhrase(key) : key;
     }
 
     /**
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cmsMsg(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsMsg(key) : key;
     }
 
     /**
      * @param cms - The class for web content
      * @param key - The token to look up
      * @return String
      */
     public static String cmsAlt(WebContentManagement cms, String key) {
         if (key == null) {
             throw new NullPointerException("key cannot be null");
         }
         return cms != null ? cms.cmsAlt(key) : key;
     }
 
     /**
      * @param cms
      * @return String
      */
     public static String br(WebContentManagement cms) {
         return cms.br();
     }
 
     /**
      * Replace [ with &lt;i&gt; and ] with &lt;/i&gt;. Used for habitat names
      * and descriptions, where there is a convention to display species names
      * in italics.
      *
      * @param inStr - input string
      * @return - string with replacements.
      */
     public static String bracketsToItalics(String inStr) {
         if (inStr.contains("[") || inStr.contains("]")) {
             inStr = inStr.replaceAll("\\[","<i>").replaceAll("]","</i>");
         }
         return inStr;
     }
 
     /**
      *
      * @param in
      * @return String
      */
     public static String replaceTags(String in) {
         return EunisUtil.replaceTags(in, true, true);
     }
 
     /**
      *
      * @param in
      * @param dontCreateHTMLAnchors
      * @param dontCreateHTMLLineBreaks
      * @return String
      */
     public static String replaceTags(String in, boolean dontCreateHTMLAnchors, boolean dontCreateHTMLLineBreaks) {
         return EunisUtil.replaceTags(in, dontCreateHTMLAnchors, dontCreateHTMLLineBreaks);
     }
 
     /**
      * @param object
      * @param defaultValue
      * @return String
      */
     public static String formatString(Object object, String defaultValue) {
         return Utilities.formatString(object, defaultValue);
     }
 
     /**
      * @param val
      * @param dec
      * @return String
      */
     public static String formatDecimal(Object val, Integer dec) {
         String val2 = "";
         int decimals = dec.intValue();
 
         decimals++;
         try {
             if (val != null) {
                 val2 = val.toString();
                 int pos = val2.indexOf(".");
 
                 if (pos > 0 && pos + decimals <= val2.length()) {
                     val2 = val2.substring(0, pos + decimals);
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return val2;
     }
 
     /**
      * Translate the SOURCE_DB field from CHM62EDT_SITES in human readable language.
      *
      * @param sourceDB Source db.
      * @return Source database.
      */
     public static String translateSourceDB(String sourceDB) {
         if (null == sourceDB) {
             return "n/a";
         }
         String result =
                 sourceDB.replaceAll("CDDA_NATIONAL", "CDDA National").replaceAll("CDDA_INTERNATIONAL", "CDDA International")
                         .replaceAll("NATURA2000", "Natura 2000").replaceAll("CORINE", "Corine")
                         .replaceAll("DIPLOMA", "European diploma").replaceAll("BIOGENETIC", "Biogenetic reserves")
                         .replaceAll("NATURENET", "NatureNet").replaceAll("EMERALD", "Emerald");
 
         return result;
     }
 
     /**
      * Replace characters having special meaning inside HTML tags with their escaped equivalents, using character entities.
      *
      * @param str String to be parsed
      * @return Processed string.
      */
     public static String treatURLSpecialCharacters(String str) {
         if (str == null) {
             return "";
         }
         String result = str;
 
         result = result.replaceAll("&", "&amp;");
         result = result.replaceAll("<", "&lt;");
         result = result.replaceAll(">", "&gt;");
         result = result.replaceAll("\"", "&quot;");
         result = result.replaceAll("'", "&#039;");
         result = result.replaceAll("\\\\", "&#092;");
         result = result.replaceAll("%", "&#037;");
 
         return result;
     }
 
     /**
      * This is a direct call to {@link Utilities#formatArea(String, int, int, String, String)},
      * see the JavaDoc of that method for more information.
      *
      * @return The formatted string
      */
     public static String formatArea(String input, int left, int right, String blank, String cssStyle) {
         return Utilities.formatArea(input, left, right, blank, cssStyle);
     }
 
     /**
      * This is a direct call to {@link Utilities#formatArea(String, int, int, String)},
      * see the JavaDoc of that method for more information.
      *
      * @param input
      * @param left
      * @param right
      * @param blank
      * @return The formatted string
      */
     public static String formatArea(String input, int left, int right, String blank) {
         return Utilities.formatArea(input, left, right, blank);
     }
 
     /**
      * Find a reference by an idDc and return a vector with two elements , first element contains author of that reference and
      * second element contains url of reference.
      *
      * @param idDc idDC of reference
      * @return author
      */
     public static String getAuthorAndUrlByIdDc(String idDc) {
         String author = "";
 
         try {
             List references = new ReferencesJoinDomain().findWhere("ID_DC = " + idDc);
 
             if (references != null && references.size() > 0) {
                 author =
                         (((ReferencesJoinPersist) references.get(0)).getSource() == null ? ""
                                 : ((ReferencesJoinPersist) references.get(0)).getSource());
                 author = treatURLSpecialCharacters(author);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return author;
     }
 
     /**
      *
      * @param input the input string
      * @param what to replace
      * @param replacement
      * @return String
      */
     public static String replaceAll(String input, String what, String replacement) {
         return input.replaceAll(what, replacement);
     }
 
     /**
      *
      * @param input the input string
      * @return String
      */
     public static String encode(String input) {
         String ret = "";
 
         try {
             ret = URLEncoder.encode(input, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             logger.error(e);
             e.printStackTrace();
         }
         return ret;
     }
 
     /**
      *
      * @param countryName
      * @return boolean
      */
     public static boolean isCountry(String countryName) {
         return Utilities.isCountry(countryName);
     }
 
     /**
      *
      * @param idDc
      * @return String
      */
     public static String getReferencesByIdDc(String idDc) {
         return Utilities.getReferencesByIdDc(idDc);
     }
 
     /**
      * Return yes/no depending on integer value
      *
      * @param value
      * @return String
      */
     public static String getYesNo(Integer value) {
         String ret = "no";
         if (value != null && value == 1) {
             ret = "yes";
         }
         return ret;
     }
 
     /**
      * Executes any method that takes one Integer param and returns String
      *
      * @param className - name of the class where method exists
      * @param methodName - name of the method to be executed
      * @param param - param of type Integer
      * @return String
      */
     public static Object execMethodParamInteger(String className, String methodName, Integer param) {
         Object ret = null;
         try {
             Class<?> c = Class.forName(className);
             Object t = c.getClass();
             Class<?>[] parameterTypes = new Class[] {Integer.class};
             Method method = c.getDeclaredMethod(methodName, parameterTypes);
             Integer[] params = new Integer[] {param};
             ret = method.invoke(t, params);
             if (ret instanceof String) {
                 ret = Utilities.formatString(ret);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 
     /**
      * Executes any method that takes one String param and returns String
      *
      * @param className - name of the class where method exists
      * @param methodName - name of the method to be executed
      * @param param - param of type String
      * @return String
      */
     public static Object execMethodParamString(String className, String methodName, String param) {
         Object ret = null;
         try {
             Class<?> c = Class.forName(className);
             Object t = c.getClass();
             Class<?>[] parameterTypes = new Class[] {String.class};
             Method method = c.getDeclaredMethod(methodName, parameterTypes);
             String[] params = new String[] {param};
             ret = method.invoke(t, params);
             if (ret instanceof String) {
                 ret = Utilities.formatString(ret);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return ret;
     }
 }

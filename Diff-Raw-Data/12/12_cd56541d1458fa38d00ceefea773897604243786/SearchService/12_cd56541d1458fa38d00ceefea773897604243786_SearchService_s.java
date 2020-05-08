 package com.computas.sublima.query.service;
 
 import com.ibm.icu.text.Normalizer;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A service class with methods to support advanced and free text search
  *
  * @author mha
  * @version 1.0
  */
 
 public class SearchService {
 
   private static Logger logger = Logger.getLogger(SearchService.class);
 
   private String defaultBooleanOperator;
 
   public SearchService() {
   }
 
   public SearchService(String booleanOperator) {
     setDefaultBooleanOperator(booleanOperator);
   }
 
 
   /**
    * Takes the search string and transform it using the default boolean operator AND/OR
    * This is done programmaticly since LARQ does not support setting another default boolean operator than OR
    *
    * @param searchstring The given search string
    * @return A transformed search string, using AND/OR based on the configuration
    */
   //todo More advanced check on search string. Ie. - + NOT OR AND if defined in the search term by the user
   public String buildSearchString(String searchstring, boolean truncate) {
 
     // Lucene gives certain characters a meaning, which may cause malformed queries, so remove them
    searchstring = searchstring.replaceAll("[:(){}\\[\\]~\\*\\^]" , "");
     Pattern p = Pattern.compile("(\\S+)|(\\\"[^\\\"]+\\\")");
     Matcher m = p.matcher(searchstring);
     List<String> terms = new ArrayList<String>();
     while (m.find()) {
       terms.add(m.group());
     }
     String actual = "";
     for (String term : terms) {
       actual += ("".equals(actual) ? "" : " " + defaultBooleanOperator + " ") + term;
     }
 
     // Split the search string, and add * after each word that isn't a part of a phrase
     boolean partOfPhrase = false;
     StringBuffer stringBuffer = new StringBuffer();
     String[] partialSearchString = actual.split(" ");
 
     for (int i = 0; i < partialSearchString.length; i++) {
       if (partialSearchString[i].startsWith("\"")) {
         partOfPhrase = true;
       }
 
       if ("AND".equalsIgnoreCase(partialSearchString[i]) ||
               "OR".equalsIgnoreCase(partialSearchString[i])) {
 
         if (!partOfPhrase) {
           stringBuffer.append(partialSearchString[i] + " ");
         }
 
       } else {
         if (partOfPhrase || !truncate || partialSearchString[i].length() <= 2) {
           stringBuffer.append(partialSearchString[i] + " ");
         } else {
           stringBuffer.append(partialSearchString[i] + "* ");
         }
 
         if (partialSearchString[i].endsWith("\"")) {
           partOfPhrase = false;
         }
       }
     }
 
    actual = stringBuffer.toString();
    return actual.trim();
   }
 
   /**
    * Method to escape characters \ and " in a String
    *
    * @param raw
    * @return String with characters escaped
    */
   public String escapeString(String raw) {
     raw = raw.replace("\\", "\\\\");
     raw = raw.replace("\"", "\\\"");
 
     return raw;
   }
 
   public String sanitizeStringForURI(String raw) {
     // Normalizer normalizer = new
     String out = Normalizer.normalize(raw, Normalizer.NFD);
     out = out.replaceAll("[^\\p{ASCII}]", ""); // Removes all fluff on chars
     out = out.toLowerCase();
     out = out.replaceAll("\\s+", "-"); // All spaces become one -
     out = out.replaceAll("[^\\w-]", ""); // Remove all now not a alphanumeric or -
     return out;
   }
 
   public void setDefaultBooleanOperator(String defaultBooleanOperator) {
     this.defaultBooleanOperator = defaultBooleanOperator;
   }
 
   public String getDefaultBooleanOperator() {
     return this.defaultBooleanOperator;
   }
 }

 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.kernel.nls;
 
 import com.google.common.collect.Maps;
 import sirius.kernel.commons.Strings;
 
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aha
  * Date: 21.07.13
  * Time: 22:48
  * To change this template use File | Settings | File Templates.
  */
 public class Translation {
     private boolean autocreated;
     private String file;
     private String key;
     private boolean accessed;
     private Map<String, String> translationTable = Maps.newTreeMap();
 
     protected Translation(String key) {
         this.key = key;
     }
 
     public boolean isAutocreated() {
         return autocreated;
     }
 
     protected void setFile(String file) {
         this.file = file;
     }
 
     public String getFile() {
         return file;
     }
 
     public void addTranslation(String lang, String value) {
         if (translationTable.containsKey(lang) && !Strings.areEqual(translationTable.get(lang), value)) {
             Babelfish.LOG
                     .WARN("Overriding translation for '%s' in language %s: '%s' --> '%s'",
                            key,
                            lang,
                            translationTable.get(lang),
                            value);
         }
         translationTable.put(lang, value);
     }
 
     public String getKey() {
         return key;
     }
 
     public boolean isAccessed() {
         return accessed;
     }
 
     protected void setAutocreated(boolean autocreated) {
         this.autocreated = autocreated;
     }
 
     public String translate(String lang) {
         this.accessed = true;
         String result = translationTable.get(lang);
         if (result == null) {
             result = translationTable.get(NLS.getDefaultLanguage());
         }
         if (result == null) {
             return key;
         }
         return result;
     }
 
     public String translateWithoutFallback(String lang) {
         return translationTable.get(lang);
     }
 
     public boolean hasTranslation(String lang) {
         return translationTable.containsKey(lang);
     }
 
     /**
      * Checks if the given filter is either contained in the key or in one of the translations
      *
      * @param effectiveFilter the filter to be applied. <tt>null</tt> indicates that no filtering should take place
      * @return <tt>true</tt> if the given filter was <tt>null</tt> or if it either occurred in the key
      *         or in one of the translations.
      */
     protected boolean containsText(String effectiveFilter) {
         if (effectiveFilter == null) {
             return true;
         }
         if (key.toLowerCase().contains(effectiveFilter)) {
             return true;
         }
         for (String string : translationTable.values()) {
             if (string.toLowerCase().contains(effectiveFilter)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Writes all translations into the given map
      *
      * @param propMap the target map with maps two-letter language codes to <tt>StoredProperties</tt>
      */
     protected void writeTo(Map<String, SortedProperties> propMap) {
         for (Map.Entry<String, String> entry : translationTable.entrySet()) {
             if (Strings.isFilled(entry.getValue())) {
                 SortedProperties props = propMap.get(entry.getKey());
                 if (props == null) {
                     props = new SortedProperties();
                     propMap.put(entry.getKey(), props);
                 }
                 props.setProperty(key, entry.getValue());
             }
         }
     }
 }

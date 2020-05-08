 package net.codjo.i18n.common;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class TranslationManager {
     public static final String TRANSLATION_MANAGER_PROPERTY = "TranslationManager";
 
     private Map<Language, List<ResourceBundle>> languageToBundles = new HashMap<Language, List<ResourceBundle>>();
 
 
     public void addBundle(ResourceBundle resourceBundle, Language language) {
         List<ResourceBundle> bundles = languageToBundles.get(language);
         if (bundles == null) {
             bundles = new ArrayList<ResourceBundle>();
             languageToBundles.put(language, bundles);
         }
         bundles.add(resourceBundle);
 
         // TODO : checker que les cls de ce ResourceBundle ne font pas doublons...
     }
 
 
     public void addBundle(String baseName, Language language) {
        addBundle(ResourceBundle.getBundle(baseName, language.getLocale()), language);
     }
 
 
     public void addBundleOnlyIfNeeded(String baseName, Language language, String key) {
         try {
             translate(key, language);
         }
         catch (IllegalArgumentException e) {
             addBundle(baseName, language);
         }
         catch (MissingResourceException e) {
             addBundle(baseName, language);
         }
     }
 
 
     public String translate(String key, Language language) {
         List<ResourceBundle> bundles = languageToBundles.get(language);
         if (bundles == null) {
             throw new IllegalArgumentException("Can't find bundle for '" + language + "' language.");
         }
         MissingResourceException exception = null;
         for (ResourceBundle bundle : bundles) {
             try {
                 return bundle.getString(key);
             }
             catch (MissingResourceException e) {
                 exception = e;
             }
         }
         throw exception;
     }
 }

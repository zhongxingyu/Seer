 package net.codjo.i18n.common;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
import java.util.Locale;
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
        // Hack: this is strange but ResourceBundle.getBundle takes into account the default locale
        // instead of the locale passed as a parameter on Unix-side only...
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(language.getLocale());
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, language.getLocale());
        addBundle(bundle, language);
        Locale.setDefault(oldLocale);
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

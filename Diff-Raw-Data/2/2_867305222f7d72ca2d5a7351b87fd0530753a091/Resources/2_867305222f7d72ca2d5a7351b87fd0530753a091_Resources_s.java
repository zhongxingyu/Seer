 package edu.kpi.pzks.gui.utils;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  *
  * @author asmirnova
  */
 public class Resources {
 
     public enum ResourceName {
 
         Settings, Strings, Colors, Menu, Locale
     };
     public static final ResourceBundle localeBundle = ResourceBundle.getBundle(ResourceName.Locale.toString(), Locale.getDefault());
     public static final Locale locale = Locale.forLanguageTag(localeBundle.getString("locale"));
    public static final ResourceBundle defaults = ResourceBundle.getBundle(ResourceName.Settings.toString(), locale);
     public static final ResourceBundle strings = ResourceBundle.getBundle(ResourceName.Strings.toString(), locale);
     public static final ResourceBundle colors = ResourceBundle.getBundle(ResourceName.Colors.toString(), locale);
     public static final ResourceBundle menu = ResourceBundle.getBundle(ResourceName.Menu.toString(), locale);
     private static Map<Locale, ResourceBundle> propertiesMap = new HashMap<>();
 
     public static String getLocalizedProperty(String key, Locale locale, ResourceName resourceName) {
         if (!propertiesMap.containsKey(locale)) {
             propertiesMap.put(locale, ResourceBundle.getBundle(resourceName.toString(), locale));
         }
         return propertiesMap.get(locale).getString(key);
     }
 }

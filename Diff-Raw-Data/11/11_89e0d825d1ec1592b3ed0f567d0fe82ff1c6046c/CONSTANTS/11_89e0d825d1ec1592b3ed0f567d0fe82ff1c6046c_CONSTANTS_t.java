 package edu.kpi.pzks.gui.utils;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 public class CONSTANTS {
     private static ResourceBundle defaults = ResourceBundle.getBundle("settings", Locale.getDefault());
 
     public static final String MAIN_TITLE = defaults.getString("main.title");
    public static final String ERROR_VALIDATION_TITLE = defaults.getString("error.validation.title");
 
     public static final int NODE_WIDTH = Integer.parseInt(defaults.getString("node.width"));
     public static final int NODE_HEIGHT = Integer.parseInt(defaults.getString("node.height"));
 
     public static final String FONT_FAMILY = defaults.getString("font.family");
     public static final int FONT_SIZE = Integer.parseInt(defaults.getString("font.size"));
     public static final int FONT_WEIGHT = Integer.parseInt(defaults.getString("font.weight"));
 
     private static Map<Locale, ResourceBundle> propertiesMap = new HashMap<>();
 
     public static String getLocalizedProperty(String key, Locale locale) {
         if (!propertiesMap.containsKey(locale)) {
             propertiesMap.put(locale, ResourceBundle.getBundle("settings", locale));
         }
         return propertiesMap.get(locale).getString(key);
     }
 }

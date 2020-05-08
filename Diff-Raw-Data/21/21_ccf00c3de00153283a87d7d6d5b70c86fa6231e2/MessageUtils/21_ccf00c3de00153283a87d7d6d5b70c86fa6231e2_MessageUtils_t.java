 package org.jggug.kobo.gircbot.core;
 
import java.util.Locale;
 import java.util.ResourceBundle;
 
 public class MessageUtils {
 
     protected static final String BUNDLE_NAME = "messages";
     private static final ResourceBundle.Control control = new UTF8ResouceBundleControl();
     private static ResourceBundle bundle;
 
     protected static void loadBundle(String bundleName) {
        loadBundle(bundleName, Locale.getDefault());
    }

    protected static void loadBundle(String bundleName, Locale locale) {
        bundle = ResourceBundle.getBundle(bundleName, locale, control);
     }
 
     static {
        loadBundle(BUNDLE_NAME); // for default locale
     }
 
     public static String getMessage(String key, Object... args) {
         return String.format(bundle.getString(key), args);
     }
 }

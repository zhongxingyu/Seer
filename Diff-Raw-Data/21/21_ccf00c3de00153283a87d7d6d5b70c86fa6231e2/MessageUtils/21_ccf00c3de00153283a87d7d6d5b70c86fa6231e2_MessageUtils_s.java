 package org.jggug.kobo.gircbot.core;
 
 import java.util.ResourceBundle;
 
 public class MessageUtils {
 
     protected static final String BUNDLE_NAME = "messages";
     private static final ResourceBundle.Control control = new UTF8ResouceBundleControl();
     private static ResourceBundle bundle;
 
     protected static void loadBundle(String bundleName) {
        bundle = ResourceBundle.getBundle(bundleName, control);
     }
 
     static {
        loadBundle(BUNDLE_NAME);
     }
 
     public static String getMessage(String key, Object... args) {
         return String.format(bundle.getString(key), args);
     }
 }

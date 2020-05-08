 package org.hackystat.sensor.eclipse;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * Provides internationalization resources from resource bundle.
  *
  * @author Takuya Yamashita
  * @version $Id: EclipseSensorI18n.java,v 1.1.1.1 2005/10/20 23:56:56 johnson Exp $
  */
 public class EclipseSensorI18n {
  /** the resource bundle file name. */
   private static final String RESOURCE_BUNDLE = "org.hackystat.sensor.eclipse.Resources";
   /** the resource bundle instance. */
   private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
 
   /**
    * Prevents clients from instantiating this class.
    */
   private EclipseSensorI18n() {
   }
   
   /**
    * Returns the string from the plugin's resource bundle,
    * or 'key' if not found.
    * @param key the key to search value in the resource bundle.
    * @return the string from the plugin's resource bundle,
    * or 'key' if not found.
    */
   public static String getString(String key) {
     try {
       return bundle.getString(key);
     } 
     catch (MissingResourceException e) {
       return key;
     }
   }
 }

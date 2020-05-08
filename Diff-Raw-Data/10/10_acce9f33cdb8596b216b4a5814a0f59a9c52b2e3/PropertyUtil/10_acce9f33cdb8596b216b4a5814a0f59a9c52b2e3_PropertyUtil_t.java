 /**
  * 
  */
 package com.github.ansell.propertyutil;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.ResourceBundle.Control;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PropertyUtil
 {
     /**
      * Defines podd.properties as the properties resource bundle name.
      * 
      * Default value: podd
      */
     public static final String DEFAULT_PROPERTIES_BUNDLE_NAME = "podd";
     
     /**
      * A constant to indicate the default preference for caching properties, or not caching
      * properties.
      */
     public static final boolean DEFAULT_USE_CACHE = true;
     
     private static PropertyUtil instance;
     
     static
     {
         PropertyUtil.instance = new PropertyUtil();
     }
     
     public static PropertyUtil getInstance()
     {
         return PropertyUtil.instance;
     }
     
     private volatile ResourceBundle bundle;
     
     private String bundleName = PropertyUtil.DEFAULT_PROPERTIES_BUNDLE_NAME;
     
     /**
      * Internal property cache, used if and when users indicate that they want to use the cache.
      */
     private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();
     
     private boolean useCache = PropertyUtil.DEFAULT_USE_CACHE;
     
     public PropertyUtil()
     {
     }
     
     public PropertyUtil(final String bundleName)
     {
         this.bundleName = bundleName;
     }
     
     /**
      * Clears the internal property cache.
      */
     public final void clearPropertyCache()
     {
         this.cache.clear();
     }
     
     /**
      * Checks for the key first in the system vm properties, then in the localisation properties
      * file, by default, "oas.properties", then uses the defaultValue if the location is still
      * unknown.
      * 
      * By default, this method uses the value of PropertyUtils.DEFAULT_USE_CACHE to determine
      * whether to cache results.
      * 
      * @param key
      *            The key to check for first in system vm properties and then in the localisation
      *            properties file
      * @param defaultValue
      *            The value to return if the key does not match any configured value
      * @return the string matching the key
      */
     public String get(final String key, final String defaultValue)
     {
         return this.getSystemOrPropertyString(key, defaultValue, this.useCache);
     }
     
     /**
      * 
      * @return The property bundle name to be used for fetching properties.
      */
     public String getPropertyBundleName()
     {
         return this.bundleName;
     }
     
     /**
      * Checks for the key first in the system vm properties, then in the localisation properties
      * file, by default, "oas.properties", then uses the defaultValue if the location is still
      * unknown.
      * 
      * @param key
      *            The key to check for first in system vm properties and then in the localisation
      *            properties file
      * @param defaultValue
      *            The value to return if the key does not match any configured value
      * @param useCache
      *            Determines whether the result will be fetched from the cache, or if found another
      *            way, will be put into the cache
      * @return the string matching the key
      */
     public String getSystemOrPropertyString(final String key, final String defaultValue, final boolean useCache)
     {
         // if they want to use the cache, and the cache contains this key, return the value
         if(useCache && this.cache.containsKey(key))
         {
             return this.cache.get(key);
         }
         
         // Try to get the property from the system configuration, for example, from, 'java
         // -Dkey=value'
         String result = System.getProperty(key);
         
         if(result == null)
         {
             // If we were unsuccessful in the cache and the system properties, try to fetch from
             // properties file on the class path
             final ResourceBundle nextBundle = this.getBundle();
             if(nextBundle != null)
             {
                try
                {
                    result = nextBundle.getString(key);
                }
                catch(MissingResourceException e)
                {
                    // Do nothing, will use default
                    ;
                }
             }
         }
         
         // if the property didn't exist, replace it with the default value
         if(result == null)
         {
             result = defaultValue;
         }
         
         // Do not create anything in the cache if they show an intention not to use the cache
         if(useCache && result != null)
         {
             final String putIfAbsent = this.cache.putIfAbsent(key, result);
             
             // Last call wins
             if(putIfAbsent != null && !putIfAbsent.equals(result))
             {
                 this.cache.put(key, result);
             }
         }
         
         return result;
     }
     
     /**
      * Sets the property bundle name to be used for fetching properties to a new value.
      * 
      * @param newPropertyBundleName
      *            The new property bundle name to use for fetching properties.
      */
     public void setPropertyBundleName(final String newPropertyBundleName)
     {
         if(newPropertyBundleName == null || newPropertyBundleName.isEmpty())
         {
             this.bundleName = PropertyUtil.DEFAULT_PROPERTIES_BUNDLE_NAME;
         }
         else
         {
             this.bundleName = newPropertyBundleName;
         }
         
         this.cache.clear();
         synchronized(this)
         {
             this.bundle = null;
         }
     }
     
     private ResourceBundle getBundle()
     {
         ResourceBundle result = this.bundle;
         if(this.bundle == null)
         {
             synchronized(this)
             {
                 result = this.bundle;
                 if(this.bundle == null)
                 {
                     try
                     {
                         // Try to resolve bundle on classpath
                         this.bundle = ResourceBundle.getBundle(this.bundleName);
                     }
                     catch(final MissingResourceException mre)
                     {
                         // Do nothing, will try other options
                         ;
                     }
                 }
                 if(this.bundle == null)
                 {
                     // Try to resolve bundle in the current user directory
                     final String userDir = System.getProperty("user.dir");
                     if(userDir != null)
                     {
                         final Path userDirPath = Paths.get(userDir);
                         if(Files.exists(userDirPath))
                         {
                             try
                             {
                                 final URL[] urls = { userDirPath.toUri().toURL() };
                                 final ClassLoader loader = new URLClassLoader(urls);
                                 this.bundle =
                                         ResourceBundle.getBundle(this.bundleName, Locale.getDefault(), loader,
                                                 Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));
                             }
                             catch(final MalformedURLException e)
                             {
                                 
                             }
                             catch(final MissingResourceException mre)
                             {
                                 // Do nothing, will try other options
                                 ;
                             }
                         }
                     }
                 }
                 if(this.bundle == null)
                 {
                     // Try to resolve bundle in the current user home directory
                     final String userHome = System.getProperty("user.home");
                     if(userHome != null)
                     {
                         final Path userHomePath = Paths.get(userHome);
                         if(Files.exists(userHomePath))
                         {
                             try
                             {
                                 final URL[] urls = { userHomePath.toUri().toURL() };
                                 final ClassLoader loader = new URLClassLoader(urls);
                                 this.bundle =
                                         ResourceBundle.getBundle(this.bundleName, Locale.getDefault(), loader,
                                                 Control.getNoFallbackControl(Control.FORMAT_PROPERTIES));
                             }
                             catch(final MalformedURLException e)
                             {
                                 
                             }
                             catch(final MissingResourceException mre)
                             {
                                 // Do nothing, will try other options
                                 ;
                             }
                         }
                     }
                 }
                 result = this.bundle;
             }
         }
         return result;
     }
 }

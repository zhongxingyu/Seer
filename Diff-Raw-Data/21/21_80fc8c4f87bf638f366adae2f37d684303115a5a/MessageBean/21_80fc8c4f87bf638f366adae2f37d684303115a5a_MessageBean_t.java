 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.beans;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.value.renderer.FxValueRendererFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * <p>A generic localization beans for messages displayed in the UI. The MessageBean wraps
  * one or more {@link ResourceBundle ResourceBundles} that provide localized messages for
  * web applications or plugins. By providing a localized resource bundle with a fixed name
  * in your plugin/application JAR file, these messages will be automatically detected during
  * startup and can be accessed through the message beans.</p>
  * <p/>
  * <p>
  * Include the resource bundle with one of the following base names in the root directory
  * of a JAR file deployed with the application:
  * <p/>
  * <table>
  * <tr>
  * <th>{@link #BUNDLE_APPLICATIONS}</th>
  * <td>for web applications</td>
  * </tr>
  * <tr>
  * <th>{@link #BUNDLE_PLUGINS}</th>
  * <td>for plugins (or other JAR files providing localized messages)</td>
  * </tr>
  * </table>
  * <p/>
  * Currently both resource bundle types are treated equally, except that all application resource
  * bundles are queried before the first plugin resource bundle.
  * </p>
  * <p/>
  * <p><b>Usage:</b> fxMessageBean[key] to get the translation
  * of the given property name in the user's language.
  * Parameters in the localized message can be replaced too, by placing EL expressions inside the lookup string:
  * Using a message declaration of "my.message.key=1+1 is: {0}", the placeholder {0} will be replaced
  * using the following EL code:
  * <pre>
  * fxMessageBean['my.message.key,#{1+1}']
  * </pre>
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class MessageBean extends HashMap {
     private static final long serialVersionUID = 2176514561683264331L;
 
     private static class MessageKey {
         private final Locale locale;
         private final String key;
 
         private MessageKey(Locale locale, String key) {
             this.locale = locale;
             this.key = key;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             MessageKey that = (MessageKey) o;
 
             if (!key.equals(that.key)) return false;
             if (!locale.equals(that.locale)) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result;
             result = locale.hashCode();
             result = 31 * result + key.hashCode();
             return result;
         }
     }
 
     /**
      * Resource bundle name for plugin packages.
      */
     public static final String BUNDLE_PLUGINS = "PluginMessages";
     /**
      * Resource bundle name for web applications.
      */
     public static final String BUNDLE_APPLICATIONS = "ApplicationMessages";
 
     private static final Log LOG = LogFactory.getLog(MessageBean.class);
     private static final List<BundleReference> resourceBundles = new CopyOnWriteArrayList<BundleReference>();
     private static final ConcurrentMap<String, ResourceBundle> cachedBundles = new ConcurrentHashMap<String, ResourceBundle>();
     private static final ConcurrentMap<MessageKey, String> cachedMessages = new ConcurrentHashMap<MessageKey, String>();
     private static volatile boolean initialized = false;
 
     /**
      * Return the managed instance of the message beans.
      *
      * @return the managed instance of the message beans.
      */
     public static MessageBean getInstance() {
         return (MessageBean) FxJsfUtils.getManagedBean("fxMessageBean");
     }
 
     /**
      * Return the localized message, replacing {0}...{n} with the given args.
      * < b/>
      * Parameters may be contained in the key and are comma separated. JSF EL expressions
      * are evaluated in the current faces context.<br/>
      * Examples: <br/>
      * key="xx.yy.myMessage,myParam1,myParam2" <br/>
      * key="xx.yy.myMessage,#{1+2},#{myBean.value},'some literal string value'"
      *
      * @param key the message key (optional with parameters)
      * @return the formatted message
      */
     @Override
     public Object get(Object key) {
         // The key may contain parameters, which are comma separated
         String sKey = String.valueOf(key);
         String sParams[] = null;
         try {
             String[] arr = FxSharedUtils.splitLiterals(sKey);
             sKey = arr[0];
             if (arr.length > 1) {
                 sParams = new String[arr.length - 1];
                 System.arraycopy(arr, 1, sParams, 0, sParams.length);
                 for (int i = 1; i < arr.length; i++) {
                     // evaluate parameters
                    Object value;
                    try {
                        value = FxJsfUtils.evalObject(arr[i]);
                    } catch (Exception e) {
                        LOG.warn("Failed to evaluate parameter " + arr[i] + ": " + e.getMessage(), e);
                        value = "";
                    }
                     //noinspection unchecked
                     sParams[i - 1] = value != null
                             ? (value instanceof FxValue
                             ? FxValueRendererFactory.getInstance().format((FxValue) value)
                             : value.toString())
                             : null;
                 }
             }
         } catch (Throwable t) {
             LOG.error("Failed to convert parameters (ignored): " + t.getMessage(), t);
         }
 
         return getMessage(sKey, (Object[]) sParams);
     }
 
 
     /**
      * Return the localized message, replacing {0}...{n} with the given args. FxString
      * objects will be translated in the user's locale automatically.
      *
      * @param key  message key
      * @param args optional arguments for
      * @return the formatted message
      */
     public String getMessage(String key, Object... args) {
         String result;
         try {
             result = getResource(key);
             if (args != null && args.length > 0) {
                 result = FxFormatUtils.formatResource(result, FxContext.get().getLanguage().getId(), args);
             }
             return result;
         } catch (MissingResourceException e) {
             LOG.warn("Unknown message key: " + key);
             return "??" + key + "??";
         }
     }
 
     /**
      * Returns the resource bundle, which is cached within the request.
      *
      * @param key resource key
      * @return the resource bundle
      */
     public String getResource(String key) {
         final Locale locale = FxContext.get().getLocale();
         if (!initialized) {
             initialize();
         }
         final MessageKey messageKey = new MessageKey(locale, key);
         if (cachedMessages.containsKey(messageKey)) {
             return cachedMessages.get(messageKey);
         }
         for (BundleReference bundleReference : resourceBundles) {
             try {
                 final ResourceBundle bundle = getResources(bundleReference, locale);
                 final String message = bundle.getString(key);
                 cachedMessages.putIfAbsent(messageKey, message);
                 return message;
             } catch (MissingResourceException e) {
                 // continue with next bundle
             }
         }
         throw new MissingResourceException("Resource not found", "MessageBean", key);
     }
 
 
     /**
      * Return the resource bundle in the given locale. Uses caching to speed up
      * lookups.
      *
      * @param bundleReference the bundle reference object
      * @param locale          the requested locale
      * @return the resource bundle in the requested locale
      */
     private ResourceBundle getResources(BundleReference bundleReference, Locale locale) {
         final String key = bundleReference.getCacheKey(locale);
         if (cachedBundles.get(key) == null) {
             cachedBundles.putIfAbsent(key, bundleReference.getBundle(locale));
         }
         return cachedBundles.get(key);
     }
 
 
     /**
      * Initialize the application resource bundles. Scans the classpath for resource bundles
      * for a predefined set of names ({@link #BUNDLE_APPLICATIONS} and {@link #BUNDLE_PLUGINS}),
      * and then adds resource references that use {@link URLClassLoader URLClassLoaders} for loading
      * the associated resource bundles.
      */
     private static synchronized void initialize() {
         if (initialized) {
             return;
         }
         try {
             addResources(BUNDLE_APPLICATIONS);
             addResources(BUNDLE_PLUGINS);
         } catch (IOException e) {
             LOG.error("Failed to initialize plugin message resources: " + e.getMessage(), e);
         } finally {
             initialized = true;
         }
     }
 
     /**
      * Add a resource reference for the given resource base name.
      *
      * @param baseName the resource name (e.g. "ApplicationResources")
      * @throws IOException if an I/O error occured while looking for resources
      */
     private static void addResources(String baseName) throws IOException {
         // scan classpath
         final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(baseName + ".properties");
         while (resources.hasMoreElements()) {
             final URL resourceURL = resources.nextElement();
             try {
                 // expected format: file:/some/path/to/file.jar!{baseName}.properties
                 final int jarDelim = resourceURL.getPath().lastIndexOf(".jar!");
                 if (jarDelim == -1) {
                     LOG.warn("Cannot use message resources because they are not stored in a jar file: " + resourceURL.getPath());
                     continue;
                 }
                 if (!resourceURL.getPath().startsWith("file:")) {
                     LOG.warn("Cannot use message resources because they are not served from the file system: " + resourceURL.getPath());
                     continue;
                 }
 
                 // "file:" and everything after ".jar" gets stripped for the class loader URL
                 final URL jarURL = new URL("file", null, resourceURL.getPath().substring("file:".length(), jarDelim + 4));
                 addResourceBundle(baseName, new URLClassLoader(new URL[]{jarURL}));
 
                 LOG.info("Added plugin message resources for " + jarURL.getPath());
             } catch (Exception e) {
                 LOG.error("Failed to add plugin resources for URL " + resourceURL.getPath() + ": " + e.getMessage(), e);
             }
         }
     }
 
     /**
      * Add a resource bundle with the given name and classloader.
      *
      * @param baseName the resource base name
      * @param loader   the class loader to be used
      */
     private static void addResourceBundle(String baseName, ClassLoader loader) {
         resourceBundles.add(new BundleReference(baseName, loader));
     }
 
     /**
      * A resource bundle reference.
      */
     private static class BundleReference {
         private final String baseName;
         private final ClassLoader classLoader;
 
         /**
          * Create a new bundle reference.
          *
          * @param baseName    the fully qualified base name (e.g. "ApplicationResources")
          * @param classLoader the class loader to be used for loading the resource bundle. If null,
          *                    the context class loader will be used.
          */
         private BundleReference(String baseName, ClassLoader classLoader) {
             this.baseName = baseName;
             this.classLoader = classLoader;
         }
 
         /**
          * Returns the base name of the resource bundle (e.g. "ApplicationResources").
          *
          * @return the base name of the resource bundle (e.g. "ApplicationResources").
          */
         public String getBaseName() {
             return baseName;
         }
 
         /**
          * Returns the class loader to be used for loading the bundle.
          *
          * @return the class loader to be used for loading the bundle.
          */
         public ClassLoader getClassLoader() {
             return classLoader;
         }
 
         /**
          * Return the resource bundle in the given locale.
          *
          * @param locale the requested locale
          * @return the resource bundle in the given locale.
          */
         public ResourceBundle getBundle(Locale locale) {
             if (this.classLoader == null) {
                 return ResourceBundle.getBundle(baseName, locale);
             } else {
                 return ResourceBundle.getBundle(baseName, locale, classLoader);
             }
         }
 
         /**
          * Return a cache key unique for this resource bundle and locale.
          *
          * @param locale the requested locale
          * @return a cache key unique for this resource bundle and locale.
          */
         public String getCacheKey(Locale locale) {
             final String localeSuffix = locale == null ? "" : "_" + locale.toString();
             if (this.classLoader == null) {
                 return baseName + localeSuffix;
             } else {
                 return baseName + this.toString() + localeSuffix;
             }
         }
     }
 
 }

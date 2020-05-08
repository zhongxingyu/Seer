 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.shared.exceptions;
 
 import com.flexive.shared.*;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * Localized Exception message handling
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxExceptionMessage implements Serializable {
     private static final long serialVersionUID = -545689173017222846L;
     private static final Log LOG = LogFactory.getLog(FxExceptionMessage.class);
 
     /**
      * Default language of a resource if no _locale file is present
      */
     private static final String EXCEPTION_BUNDLE = "FxExceptionMessages";
     private static final String PLUGIN_BUNDLE = "PluginMessages";
     private String key;
     private Object[] values;
 
     private static final List<FxSharedUtils.BundleReference> resourceBundles = new CopyOnWriteArrayList<FxSharedUtils.BundleReference>();
     private static final ConcurrentMap<String, ResourceBundle> cachedBundles = new ConcurrentHashMap<String, ResourceBundle>();
     private static final ConcurrentMap<FxSharedUtils.MessageKey, String> cachedMessages = new ConcurrentHashMap<FxSharedUtils.MessageKey, String>();
     private static volatile boolean initialized = false;
 
     /**
      * Initialize the application resource bundles. Scans the classpath for resource bundles
      * for a predefined set of names ({@link #EXCEPTION_BUNDLE} and {@link #PLUGIN_BUNDLE}),
      * and then adds resource references that use {@link java.net.URLClassLoader URLClassLoaders} for loading
      * the associated resource bundles.
      */
     private static synchronized void initialize() {
         if (initialized) {
             return;
         }
         try {
             resourceBundles.addAll(FxSharedUtils.addMessageResources(EXCEPTION_BUNDLE));
             resourceBundles.addAll(FxSharedUtils.addMessageResources(PLUGIN_BUNDLE));
         } catch (IOException e) {
             LOG.error("Failed to initialize plugin message resources: " + e.getMessage(), e);
         } finally {
             initialized = true;
         }
     }
 
     /**
      * Returns the resource bundle, which is cached within the request.
      *
      * @param key resource key
      * @return the resource bundle
      */
     public String getResource(String key) {
         final UserTicket ticket = FxContext.getUserTicket();
         final Locale locale = ticket != null ? ticket.getLanguage().getLocale() : Locale.getDefault();
         if (!initialized) {
             initialize();
         }
         final FxSharedUtils.MessageKey messageKey = new FxSharedUtils.MessageKey(locale, key);
         if (cachedMessages.containsKey(messageKey)) {
             return cachedMessages.get(messageKey);
         }
         for (FxSharedUtils.BundleReference bundleReference : resourceBundles) {
             try {
                 final ResourceBundle bundle = getResources(bundleReference, locale);
                 final String message = bundle.getString(key);
                 cachedMessages.putIfAbsent(messageKey, message);
                 return message;
             } catch (MissingResourceException e) {
                 // continue with next bundle
             }
         }
         if (!locale.equals(Locale.ENGLISH)) {
             //try to find the locale in english as last resort
             //this is a fix for using PropertyResourceBundles which can only handle one locale (have to use them thanks to JBoss 5...)
             for (FxSharedUtils.BundleReference bundleReference : resourceBundles) {
                 try {
                     final ResourceBundle bundle = getResources(bundleReference, Locale.ENGLISH);
                     final String message = bundle.getString(key);
                     cachedMessages.putIfAbsent(messageKey, message);
                     return message;
                 } catch (MissingResourceException e) {
                     // continue with next bundle
                 }
             }
         }
         throw new MissingResourceException("Resource not found", FxExceptionMessage.class.getCanonicalName(), key);
     }
 
     /**
      * Return the resource bundle in the given locale. Uses caching to speed up
      * lookups.
      *
      * @param bundleReference the bundle reference object
      * @param locale          the requested locale
      * @return the resource bundle in the requested locale
      */
     private ResourceBundle getResources(FxSharedUtils.BundleReference bundleReference, Locale locale) {
         final String key = bundleReference.getCacheKey(locale);
         if (cachedBundles.get(key) == null) {
             cachedBundles.putIfAbsent(key, bundleReference.getBundle(locale));
         }
         return cachedBundles.get(key);
     }
 
     /**
      * Ctor
      *
      * @param key    resource key
      * @param values optional values for placeholders in key ({x})
      */
     public FxExceptionMessage(String key, Object... values) {
         this.key = key;
         this.values = values != null ? values.clone() : new Object[0];
     }
 
     /**
      * Getter for the key
      *
      * @return key resource key
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Get the localized message for a given language code
      *
      * @param localeId locale id of the desired output
      * @return localized message
      */
     public String getLocalizedMessage(long localeId) {
         String result;
         try {
             result = getResource(key);
             if (values != null && values.length > 0) {
                 result = FxFormatUtils.formatResource(result, localeId, values);
             }
             return result;
         } catch (MissingResourceException e) {
             LOG.warn("Unknown message key: " + key);
             return "??" + key + "??";
         }
     }
 
     /**
      * Get the localized message for given ISO code
      *
      * @param localeIso requested ISO code for desired output
      * @return localized message
      */
     public String getLocalizedMessage(String localeIso) {
         FxLanguage locale;
         try {
             locale = EJBLookup.getLanguageEngine().load(localeIso);
         } catch (FxApplicationException e) {
             String msg = "[Invalid locale code (" + localeIso + ") requested for " + key + "!]";
             LOG.warn(msg);
             return msg;
         }
         return getLocalizedMessage(locale.getId());
     }
 
     /**
      * Get the localized message for a given language
      *
      * @param locale locale of the desired output
      * @return localized message
      */
     public String getLocalizedMessage(FxLanguage locale) {
         return getLocalizedMessage(locale.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof FxExceptionMessage))
             return false;
         FxExceptionMessage o = (FxExceptionMessage) obj;
         return o.getKey().equals(this.getKey()) && ArrayUtils.isEquals(o.values, this.values);
     }
 
     @Override
     public int hashCode() {
         int result;
         result = key.hashCode();
         result = 31 * result + (values != null ? Arrays.hashCode(values) : 0);
         return result;
     }
 }
 

 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.tools.i18n;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to internationalize all CompSystem strings
  * @author BREDEX GmbH
  * @created 02.01.2007
  */
 public class CompSystemI18n {  
     /** the logger */
     private static Logger log = LoggerFactory.getLogger(CompSystemI18n.class);
     
     /** List of ResourceBundles */
     private static final List PLUGIN_BUNDLES = new LinkedList();
     
     /** 
      * mapping from i18n keys (String) to i18n values (String)
      */
     private static final Map I18N_MAP = new HashMap();
     
     /**
      * Constructor
      */
     private CompSystemI18n() {
        // private constructor to prevent instantiation of class utility
     }
     
     /**
      * Adds the given {@link ResourceBundle}
      * @param bundle a {@link ResourceBundle}
      */
     public static void addResourceBundle(ResourceBundle bundle) {
         if (bundle == null) {
             log.error("ResourceBundle is null!"); //$NON-NLS-1$
             return;
         }
         PLUGIN_BUNDLES.add(bundle);
     }
     
     
     
     /**
      * Gets the internationalized String by a given key.
      * 
      * @param key
      *            the key for the internationalized String.
      * @return a internationalized <code>String</code>.
      */
     public static String getString(String key) {
         return getString(key, false);
     }
 
     /**
      * Gets the internationalized String by a given key.
      * 
      * @param key
      *            the key for the internationalized String.
      * @param fallBack
      *            returns the key if no value found
      * @return a internationalized <code>String</code>.
      */
     public static String getString(String key, boolean fallBack) {
         try {
             return getStringInternal(key);
         } catch (MissingResourceException mre) {
             if (!fallBack) {
                 logError(key, mre);
             }
         }
         return key;
     }
     
     /**
      * Logs in error log
      * 
      * @param key
      *            the I18n-key
      * @param throwable
      *            the throwable
      */
     private static void logError(String key, Throwable throwable) {
         log.error("Cannot find I18N-key in resource bundles: " + key, throwable); //$NON-NLS-1$
     }
     
     /**
      * Searches for the value of the given key in all bundles.<br>
      * throws MissingResourceException if the key was not found. 
      * @param key the key
      * @return the value for the given key
      */
     private static String getStringInternal(String key) {
         
         if (key == null) {
             return StringUtils.EMPTY;
         }
         
         String value = (String)I18N_MAP.get(key);
         if (value != null) {
             return value;
         }
 
         Iterator bundleIter = PLUGIN_BUNDLES.iterator();
         while (bundleIter.hasNext()) {
             ResourceBundle bundle = (ResourceBundle)bundleIter.next();
             try {
                 value = bundle.getString(key);
                 I18N_MAP.put(key, value);
                 return value;
             } catch (MissingResourceException ex) {
                 // ok here, we search in multiple bundles
             }
         }
         
         I18N_MAP.put(key, key);
         throw new MissingResourceException("No entry found for key: " + key, //$NON-NLS-1$
             CompSystemI18n.class.getName(), key);
     }
     
     /**
      * returns an internationalized string for the given key
      * @param key the key
      * @param args the arguments needed to generate the string
      * @return the internationalized string
      */
     public static String getString(String key, Object[] args) {
         try {
             MessageFormat formatter =
                 new MessageFormat(getStringInternal(key));
             return formatter.format(args);
         } catch (MissingResourceException e) {
             logError(key, e);
             StringBuffer buf = new StringBuffer(key);
             for (int i = 0; i < args.length; i++) {
                 if (args[i] != null) {
                     buf.append(" "); //$NON-NLS-1$
                     buf.append(args[i]);
                 }
             }
             return buf.toString();
         }
     }
     
 
     
     /**
      * Sets an new language for the I18N.
      * @param lang a <code>String</code> value. The new language.
      */
     public static void setLanguage(String lang) {
         Locale.setDefault(new Locale(lang, StringConstants.EMPTY));
     }
     
     /**
      * 
      * @return a String representation of the ResourceBundles to use for
      * fromString(String string)
      * @see fromString(String string)
      */
     public static String bundlesToString() {
         final String keyValueSeparator = "="; //$NON-NLS-1$
         final String lineBreak = "\n"; //$NON-NLS-1$
         final StringBuffer entries = new StringBuffer();
         for (Iterator bundlesIt = PLUGIN_BUNDLES.iterator(); 
             bundlesIt.hasNext();) {
             
             final ResourceBundle bundle = (ResourceBundle)bundlesIt.next();
             for (Enumeration keys = bundle.getKeys(); keys.hasMoreElements();) {
                 final String key = String.valueOf(keys.nextElement());
                 final String value = bundle.getString(key);
                 entries.append(key)
                         .append(keyValueSeparator)
                         .append(value)
                         .append(lineBreak);
             }
         }
         return entries.toString();
     }
     
     /**
      * Creates a ResourceBundle from the given String.<br>
      * The given String must have the specification of a properties-file:<br>
      * key=value<br>
      * key=value<br>
      * ...<br>
      * with a line break (\n) after every value.
      * @param string a String from bundleToString
      */
     public static void fromString(String string) {
         final ByteArrayInputStream stream = new ByteArrayInputStream(
             string.getBytes());
         try {
             final PropertyResourceBundle bundle = new PropertyResourceBundle(
                 stream);
             PLUGIN_BUNDLES.clear();
             addResourceBundle(bundle);
         } catch (IOException e) {
             log.error(e.getLocalizedMessage(), e);
         }
     }
 
 }

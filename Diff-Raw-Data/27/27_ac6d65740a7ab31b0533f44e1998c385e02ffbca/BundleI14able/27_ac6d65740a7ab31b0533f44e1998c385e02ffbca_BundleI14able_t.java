 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2005  Jose San Leandro Armendriz
                              chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabaas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendriz
  *
  * Description: Represents Throwable instances with support for messages
  *              in different languages.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *                  ("Name" means no concrete version has been checked out)
  *
  * $Id$
  *
  */
 package org.acmsl.commons;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.commons.patterns.I14able;
 import org.acmsl.commons.utils.ReflectionUtils;
 import org.acmsl.commons.utils.StringValidator;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.Throwable;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /*
  * Importing Commons Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Represents instances able to display messages in different languages.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro Armendariz</a>
  * @version $Revision$ at $Date$ by $Author$
  */
 public class BundleI14able
     implements  I14able
 {
     /**
      * An empty object array.
      */
     protected static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
 
     /**
      * The message key.
      */
     private String m__strMessageKey;
 
     /**
      * The message params.
      */
     private Object[] m__aParams;
 
     /**
      * The System property.
      */
     private String m__strSystemProperty;
 
     /**
      * The bundle name.
      */
     private String m__strBundleName;
 
     /**
      * Configures whether to use class loaders or not.
      */
    private boolean m__bUseClassLoader;
 
     /**
      * Creates a <code>BundleI14able</code> with given message.
      * @param messageKey the key to build the exception message.
      * @param params the parameters to build the exception message.
      * @param systemProperty the name of the system property pointing to the
      * bundle.
      * @param bundleName the name of the bundle.
      * @param useClassLoader whether to use class loaders explicitly.
      * @precondition messageKey != null
      * @precondition params != null
      * @precondition (systemProperty != null) || (bundleName != null)
      */
     protected BundleI14able(
         final String messageKey,
         final Object[] params,
         final String systemProperty,
         final String bundleName,
         final boolean useClassLoader)
     {
         immutableSetMessageKey(messageKey);
         immutableSetParams(params);
         immutableSetSystemProperty(systemProperty);
         immutableSetBundleName(bundleName);
         immutableSetUsingClassLoader(useClassLoader);
     }
 
     /**
      * Creates a <code>BundleI14able</code> with given message.
      * @param messageKey the key to build the exception message.
      * @param params the parameters to build the exception message.
      * @param systemProperty the name of the system property pointing to the
      * bundle.
      * @param bundleName the name of the bundle.
      * @precondition messageKey != null
      * @precondition params != null
      * @precondition (systemProperty != null) || (bundleName != null)
      */
     protected BundleI14able(
         final String messageKey,
         final Object[] params,
         final String systemProperty,
         final String bundleName)
     {
        this(messageKey, params, systemProperty, bundleName, false);
     }
 
     /**
      * Creates a <code>BundleI14able</code> with given message bundle.
      * @param messageKey the key to build the exception message.
      * @param systemProperty the name of the bundle.
      * @param bundleName the bundle name.
      * @precondition messageKey != null
      * @precondition (systemProperty != null) || (bundleName != null)
      */
     protected BundleI14able(
         final String messageKey,
         final String systemProperty,
         final String bundleName)
     {
         this(messageKey, EMPTY_OBJECT_ARRAY, systemProperty, bundleName);
     }
 
     /**
      * Specifies the message key.
      * @param key the message key.
      * @precondition key != null
      */
     private void immutableSetMessageKey(final String key)
     {
         m__strMessageKey = key;
     }
 
     /**
      * Specifies the message key.
      * @param key the message key.
      * @precondition key != null
      */
     protected void setMessageKey(final String key)
     {
         immutableSetMessageKey(key);
     }
 
     /**
      * Retrieves the message key.
      * @return such key.
      */
     public String getMessageKey()
     {
         return m__strMessageKey;
     }
 
     /**
      * Specifies the parameters to build the internationalized message.
      * @param params the parameters.
      * @precondition params != null
      */
      private void immutableSetParams(final Object[] params)
      {
          m__aParams = params;
      }
 
     /**
      * Specifies the parameters to build the internationalized message.
      * @param params the parameters.
      * @precondition params != null
      */
      protected void setParams(final Object[] params)
      {
          immutableSetParams(params);
      }
 
     /**
      * Retrieves the parameters needed to build the internationalized message.
      * @return such parameters.
      */
     public Object[] getParams()
     {
         return m__aParams;
     }
 
     /**
      * Specifies the system property name.
      * @param name the property.
      * @precondition name != null
      */
     private void immutableSetSystemProperty(final String name)
     {
         m__strSystemProperty = name;
     }
 
     /**
      * Specifies the system property name
      * @param name the property.
      * @precondition name != null
      */
     protected void setSystemProperty(final String name)
     {
         immutableSetSystemProperty(name);
     }
 
     /**
      * Retrieves the system property name.
      * @return such property.
      */
     public String getSystemProperty()
     {
         return m__strSystemProperty;
     }
 
     /**
      * Specifies the bundle name.
      * @param name the bundle name.
      * @precondition name != null
      */
     private void immutableSetBundleName(final String name)
     {
         m__strBundleName = name;
     }
 
     /**
      * Specifies the bundle name.
      * @param name the bundle name.
      * @precondition name != null
      */
     protected void setBundleName(final String name)
     {
         immutableSetBundleName(name);
     }
 
     /**
      * Retrieves the bundle name.
      * @return such name.
      */
     public String getBundleName()
     {
         return m__strBundleName;
     }
 
     /**
      * Specifies whether to use class loader or not.
      * @param flag such flag.
      */
     protected final void immutableSetUsingClassLoader(final boolean flag)
     {
         m__bUseClassLoader = flag;
     }
     
     /**
      * Specifies whether to use class loader or not.
      * @param flag such flag.
      */
     public void setUsingClassLoader(final boolean flag)
     {
         immutableSetUsingClassLoader(flag);
     }
 
     /**
      * Retrieves whether the engine is using a class loader or not.
      * @return such flag.
      */
     public boolean isUsingClassLoader()
     {
         return m__bUseClassLoader;
     }
     
     /**
      * Retrieves the internationalized message.
      * @return such message.
      */
     public String toString()
     {
         return toString(Locale.getDefault());
     }
 
     /**
      * Retrieves the internationalized message for given locale.
      * @param locale the desired locale.
      * @return such message.
      * @precondition locale != null
      */
     public String toString(final Locale locale)
     {
         return
             buildMessage(
                 getMessageKey(),
                 getParams(),
                 locale,
                 getBundleName(),
                 getSystemProperty(),
                 isUsingClassLoader());
     }
 
     /**
      * Builds an internationalized message for given key, parameters
      * and locale.
      * @param key the message key.
      * @param params the message parameters.
      * @param locale the locale.
      * @param bundleName the bundle name.
      * @param systemProperty the name of the bundle.
      * @param useClassLoader whether to use class loader or not.
      * @return the customized message.
      * @precondition key != null
      * @precondition params != null
      * @precondition locale != null
      * @precondition (bundleName != null) || (systemProperty != null)
      */
     protected String buildMessage(
         final String key,
         final Object[] params,
         final Locale locale,
         final String bundleName,
         final String systemProperty,
         final boolean useClassLoader)
     {
         ClassLoader t_ClassLoader = getClass().getClassLoader();
 
         if  (useClassLoader)
         {
             // Identify the class loader we will be using
             ClassLoader t_AnotherClassLoader = 
                 (ClassLoader)
                     AccessController.doPrivileged(
                         new PrivilegedAction()
                         {
                             public Object run()
                             {
                                 Object result = null;
                                     
                                 try
                                 {
                                     result = getContextClassLoader();
                                 }
                                 catch  (final IllegalAccessException exception)
                                 {
                                     // We'll use the Class.getClassLoader();
                                 }
                                 catch  (final InvocationTargetException exception)
                                 {
                                     // We'll use the Class.getClassLoader();
                                 }
 
                                 return result;
                             }
                         });
 
             if  (t_AnotherClassLoader != null)
             {
                 t_ClassLoader = t_AnotherClassLoader;
             }
         }
 
         return
             buildMessage(
                 key,
                 params,
                 retrieveSystemPropertyBundle(
                     systemProperty,
                     locale,
                     t_ClassLoader),
                 retrieveBundle(
                     bundleName,
                     locale,
                     t_ClassLoader));
     }
 
     /**
      * Retrieves the bundle from the environment using given information.
      * @param locale the locale.
      * @param systemProperty the system property.
      * @param classLoader the class loader.
      * @return the bundle.
      * @precondition locale != null
      * @precondition systemProperty != null
      */
     protected ResourceBundle retrieveSystemPropertyBundle(
         final String systemProperty,
         final Locale locale,
         final ClassLoader classLoader)
     {
         ResourceBundle result = null;
         
         try
         {
             String t_strBundleName =
                 System.getProperty(systemProperty);
 
             result = retrieveBundle(t_strBundleName, locale, classLoader);
         }
         catch  (final SecurityException securityException)
         {
             LogFactory.getLog(getClass()).info(
                 "Could not load environment property " + systemProperty,
                 securityException);
         }
 
         return result;
     }
     
     /**
      * Retrieves the bundle using a concrete class loader.
      * @param bundleName the bundle name.
      * @param locale the locale.
      * @param classLoader the class loader.
      * @return the bundle.
      * @precondition locale != null
      * @precondition classLoader != null
      */
     protected ResourceBundle retrieveBundle(
         final String bundleName,
         final Locale locale,
         final ClassLoader classLoader)
     {
         ResourceBundle result = null;
         
         if  (bundleName != null)
         {
             try
             {
                 result =
                     ResourceBundle.getBundle(bundleName, locale, classLoader);
             }
             catch  (final MissingResourceException missingResourceException)
             {
                 try
                 {
                    result =
                        ResourceBundle.getBundle(
                            "/" + bundleName, locale, classLoader);
                 }
                catch  (final MissingResourceException mrException)
                 {
                    try
                    {
                        LogFactory.getLog(getClass()).debug(
                            "Could not retrieve bundle " + bundleName,
                            mrException);
                    }
                    catch  (final Throwable throwable)
                    {
                        // Class-loading problem.
                    }
                 }
             }
         }
         
         return result;
     }
 
     /**
      * Builds an internationalized message for given key, parameters
      * and locale.
      * @param key the message key.
      * @param params the message parameters.
      * @param firstBundle the first resource bundle.
      * @param secondBundle the second resource bundle.
      * @return the customized message.
      * @precondition key != null
      * @precondition params != null
      * @precondition (firstBundle != null) || (secondBundle != null)
      */
     protected String buildMessage(
         final String key,
         final Object[] params,
         final ResourceBundle firstBundle,
         final ResourceBundle secondBundle)
     {
         String result = null;
 
         ResourceBundle t_ActualBundle = firstBundle;
 
         String t_strValue = null;
 
         if  (firstBundle != null)
         {
             t_strValue = firstBundle.getString(key);
         }
 
         if  (   (t_strValue == null)
              && (secondBundle != null))
         {
             t_strValue = secondBundle.getString(key);
 
             if  (t_strValue != null)
             {
                 t_ActualBundle = secondBundle;
             }
         }
         
         Object[] t_aParams = EMPTY_OBJECT_ARRAY;
 
         if  (t_ActualBundle != null)
         {
             t_aParams =
                 translateParams(
                     params,
                     t_ActualBundle,
                     StringValidator.getInstance());
         }
 
         if  (t_strValue != null)
         {
             result = buildMessage(t_strValue, t_aParams);
         }
 
         return result;
     }
 
     /**
      * Translates any elements of given array for which translation is
      * defined in the bundle.
      * @param params the parameters.
      * @param bundle the bundle.
      * @param stringValidator the StringValidator instance.
      * @return the translated parameters.
      * @precondition params != null
      * @precondition bundle != null
      * @precondition stringValidator != null
      */
     protected Object[] translateParams(
         final Object[] params,
         final ResourceBundle bundle,
         final StringValidator stringValidator)
     {
         Collection t_cResult = new ArrayList();
 
         for  (int t_iIndex = 0; t_iIndex < params.length; t_iIndex++)
         {
             String t_strTranslation = null;
 
             try
             {
                 t_strTranslation = bundle.getString("" + params[t_iIndex]);
             }
             catch  (final MissingResourceException exception)
             {
                 LogFactory.getLog(getClass()).debug(
                     "No parameter translation found for: " + params[t_iIndex]);
             }
 
             if  (stringValidator.isEmpty(t_strTranslation))
             {
                 t_cResult.add(params[t_iIndex]);
             }
             else
             {
                 t_cResult.add(t_strTranslation);
             }
         }
 
         return t_cResult.toArray(params);
     }
 
     /**
      * Builds an internationalized message for given key, parameters and
      * locale.
      * @param message the possibly parameterized message.
      * @param params the message parameters.
      * @return the customized message.
      * @precondition message != null
      * @precondition params != null
      */
     protected String buildMessage(final String message, final Object[] params)
     {
         return new MessageFormat(message).format(params);
     }
 
     /**
      * Returns the thread context class loader if available.
      * The thread context class loader is available for JDK 1.2
      * or later, if certain security conditions are met.
      * Note: This logic is adapted from Commons-Logging.
      * @return the class loader.
      * @throws IllegalAccessException when trying to access
      * <code>Thread.getContextClassLoader()</code> via reflection.
      * @throws InvocationTargetException when trying to access
      * <code>Thread.getContextClassLoader()</code> via reflection, and
      * the target exception is not a <code>SecurityException</code>..
      */
     protected ClassLoader getContextClassLoader()
         throws IllegalAccessException,
                InvocationTargetException
     {
         return getContextClassLoader(ReflectionUtils.getInstance());
     }
     
     /**
      * Returns the thread context class loader if available.
      * The thread context class loader is available for JDK 1.2
      * or later, if certain security conditions are met.
      * Note: This logic is adapted from Commons-Logging.
      * @param reflectionUtils the <code>ReflectionUtils</code> instance.
      * @return the class loader.
      * @throws IllegalAccessException when trying to access
      * <code>Thread.getContextClassLoader()</code> via reflection.
      * @throws InvocationTargetException when trying to access
      * <code>Thread.getContextClassLoader()</code> via reflection, and
      * the target exception is not a <code>SecurityException</code>..
      */
     protected ClassLoader getContextClassLoader(
         final ReflectionUtils reflectionUtils)
       throws IllegalAccessException,
              InvocationTargetException
     {
         return reflectionUtils.getContextClassLoader();
     }
 }

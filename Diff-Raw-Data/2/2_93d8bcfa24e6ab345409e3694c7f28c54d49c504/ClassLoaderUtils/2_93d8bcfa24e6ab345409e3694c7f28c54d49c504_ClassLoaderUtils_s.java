 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2005  Jose San Leandro Armend&aacute;riz
                              chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecaba&ntilde;as
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armend&aacute;riz
  *
  * Description: Provides some useful methods when working with ClassLoaders.
  *
  * Last modified by: $Author: chous $ at $Date: 2006-06-14 21:01:54 +0200 (Wed, 14 Jun 2006) $
  *
  * File version: $Revision: 550 $
  *
  * Project version: $Name$
  *
  * $Id: ReflectionUtils.java 550 2006-06-14 19:01:54Z chous $
  *
  */
 package org.acmsl.commons.utils;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.commons.patterns.Singleton;
 import org.acmsl.commons.patterns.Utils;
 
 /*
  * Importing Commons-Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipInputStream;
 
 /**
  * Provides some useful methods when working with ClassLoaders.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro</a>
  * @version $Revision: 550 $ at $Date: 2006-06-14 21:01:54 +0200 (Wed, 14 Jun 2006) $
  */
 public class ClassLoaderUtils
     implements  Utils,
                 Singleton
 {
     /**
      * A cached empty class array.
      */
     public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
     
     /**
      * A cached empty object array.
      */
     public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
     
     /**
      * Singleton implemented to avoid the double-checked locking.
      */
     private static class ClassLoaderUtilsSingletonContainer
     {
         /**
          * The actual singleton.
          */
         public static final ClassLoaderUtils SINGLETON =
             new ClassLoaderUtils();
     }
 
     /**
      * Default protected constructor to avoid accidental instantiation.
      */
     protected ClassLoaderUtils() {};
 
     /**
      * Retrieves a <code>ClassLoaderUtils</code> instance.
      * @return such instance.
      */
     public static ClassLoaderUtils getInstance()
     {
         return ClassLoaderUtilsSingletonContainer.SINGLETON;
     }
 
     /**
      * Tries to find the location where given class was loaded.
      * @param classInstance the class to check.
      * @return the location.
      * @precondition classInstance != null
      */
     public String findLocation(final Class classInstance)
     {
         return findLocation(classInstance, false);
     }
 
     /**
      * Tries to find the location where given class was loaded.
      * @param classInstance the class to check.
      * @param fullSearch whether to perform a full search or get
      * the first match.
      * @return the location.
      * @precondition classInstance != null
      */
     public String findLocation(
         final Class classInstance, final boolean fullSearch)
     {
         String result = null;
 
         String className = classInstance.getName();
 
         ClassLoader classLoader = classInstance.getClassLoader();
 
         if  (classLoader == null)
         {
             classLoader = ClassLoader.getSystemClassLoader();
         }
 
         if  (classLoader != null)
         {
             URL url = classLoader.getResource(className);
 
             if  (url == null)
             {
                 url = ClassLoader.getSystemResource(className);
             }
 
             if  (url != null)
             {
                 result = url.toString();
             }
             else
             {
                 result =
                     findLocation(
                         classInstance,
                         printClassPath(classLoader),
                         fullSearch);
             }
         }
 
         return result;
     }
 
     /**
      * Finds the location in given classpath.
      * @param classInstance the class.
      * @param classPath the classpath.
      * @param fullSearch whether to perform a full search or get
      * the first match.
      * @return such location.
      * @precondition classInstance != null
      * @precondition classPath != null
      */
     protected String findLocation(
         final Class classInstance,
         final String classPath,
         final boolean fullSearch)
     {
         StringBuffer result = new StringBuffer();
 
         String actualClassPath = trimBrackets(classPath);
 
         StringTokenizer t_Tokenizer =
            new StringTokenizer(actualClassPath, ",[]", false);
         
         String element;
 
         boolean nonFirstItem = false;
 
         while  (t_Tokenizer.hasMoreTokens())
         {
             element = t_Tokenizer.nextToken();
 
             if  (   (element != null)
                  && (pathContainsClass(element, classInstance.getName())))
             {
                 if  (fullSearch)
                 {
                     if  (nonFirstItem)
                     {
                         result.append(",");
                     }
 
                     nonFirstItem = true;
                 }
 
                 result.append(element);
 
                 if  (!fullSearch)
                 {
                     break;
                 }
             }
         }
 
         return result.toString();
     }
 
     /**
      * Prints the classpath defined by given class loader.
      * @param classLoader the class loader to print.
      * @return such information.
      * @precondition classLoader != null
      */
     public String printClassPath(final ClassLoader classLoader)
     {
         StringBuffer result = new StringBuffer();
 
         result.append(printAntClassPath(classLoader));
 
         result.append(printURLClassPath(classLoader));
 
         result.append(printSunClassPath(classLoader));
 
         ClassLoader parent = classLoader.getParent();
 
         if  (parent != null)
         {
             result.append(printClassPath(parent));
         }
 
         return result.toString();
     }
 
     /**
      * Prints the classpath defined by given class loader,
      * assuming it's a AntClassLoader instance.
      * @param classLoader the class loader to print.
      * @return such information.
      * @precondition classLoader != null
      */
     protected String printAntClassPath(final ClassLoader classLoader)
     {
         StringBuffer result = new StringBuffer();
 
         try
         {
             Class classInstance = classLoader.getClass();
 
             Method method;
 
             while  (classInstance != null)
             {
                 method = null;
 
                 try
                 {
                     method =
                         classInstance.getMethod(
                             "getClasspath", EMPTY_CLASS_ARRAY);
 
                 }
                 catch  (final NoSuchMethodException firstNoSuchMethodException)
                 {
                     try
                     {
                         method =
                             classInstance.getDeclaredMethod(
                                 "getClasspath", EMPTY_CLASS_ARRAY);
                     }
                     catch  (final NoSuchMethodException otherException)
                     {
                         // Left blank on purpose, since we don't know which
                         // ClassLoader implementation we have.
                     }
                 }
 
                 if  (method != null)
                 {
                     //method.setAccessible(true);
 
                     result.append("[");
                     result.append(
                         method.invoke(classLoader, EMPTY_OBJECT_ARRAY));
                     result.append("]");
 
                     break;
                 }
 
                 classInstance = classInstance.getSuperclass();
             }
         }
         catch  (final SecurityException securityException)
         {
             // Left blank on purpose. Nothing to do if we cannot access
             // the classloader using reflection.
         }
         catch  (final InvocationTargetException invocationTargetException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final IllegalArgumentException illegalArgumentException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final IllegalAccessException illegalAccessException)
         {
             // Left blank on purpose. Nothing to do if we cannot invoke
             // the method on the classloader using reflection.
         }
 
         return result.toString();
     }
 
     /**
      * Prints the classpath defined by given class loader,
      * assuming it's a URLClassLoader instance.
      * @param instance the instance to print.
      * @return such information.
      * @precondition instance != null
      */
     protected String printURLClassPath(final Object instance)
     {
         StringBuffer result = new StringBuffer();
 
         try
         {
             Class classInstance = instance.getClass();
 
             Method method;
 
             while  (classInstance != null)
             {
                 method = null;
 
                 try
                 {
                     method =
                         classInstance.getMethod("getURLs", EMPTY_CLASS_ARRAY);
 
                 }
                 catch  (final NoSuchMethodException firstNoSuchMethodException)
                 {
                     try
                     {
                         method =
                             classInstance.getDeclaredMethod(
                                 "getURLs", EMPTY_CLASS_ARRAY);
                     }
                     catch  (final NoSuchMethodException otherException)
                     {
                         // Left blank on purpose, since we don't know which
                         // ClassLoader implementation we have.
                     }
                 }
 
                 if  (method != null)
                 {
                     //method.setAccessible(true);
 
                     result.append(
                         printURLs(
                             (URL[])
                                 method.invoke(instance, EMPTY_OBJECT_ARRAY)));
 
                     break;
                 }
 
                 classInstance = classInstance.getSuperclass();
             }
         }
         catch  (final SecurityException securityException)
         {
             // Left blank on purpose. Nothing to do if we cannot access
             // the classloader using reflection.
         }
         catch  (final InvocationTargetException invocationTargetException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final IllegalArgumentException illegalArgumentException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final IllegalAccessException illegalAccessException)
         {
             // Left blank on purpose. Nothing to do if we cannot invoke
             // the method on the classloader using reflection.
         }
 
         return result.toString();
     }
 
     /**
      * Prints given array of URLs.
      * @param urls the URLs to print.
      * @return the formatted text containing the URL locations.
      * @precondition urls != null
      */
     protected String printURLs(final URL[] urls)
     {
         StringBuffer result = new StringBuffer();
 
         result.append("[");
 
         int count = (urls != null) ? urls.length : 0;
 
         for  (int index = 0; index < count; index++)
         {
             if  (index > 0)
             {
                 result.append(",");
             }
 
             result.append(urls[index]);
         }
 
         result.append("]");
 
         return result.toString();
     }
 
     /**
      * Prints the classpath defined by given class loader,
      * assuming it's a Sun (sun.*) instance.
      * @param classLoader the class loader to print.
      * @return such information.
      * @precondition classLoader != null
      */
     protected String printSunClassPath(final ClassLoader classLoader)
     {
         StringBuffer result = new StringBuffer();
 
         try
         {
             Class classInstance = classLoader.getClass();
 
             Method method;
 
             while  (classInstance != null)
             {
                 method = null;
 
                 try
                 {
                     method =
                         classInstance.getMethod(
                             "getBootstrapClassPath", EMPTY_CLASS_ARRAY);
                 }
                 catch  (final NoSuchMethodException firstNoSuchMethodException)
                 {
                     try
                     {
                         method =
                             classInstance.getDeclaredMethod(
                                 "getBootstrapClassPath", EMPTY_CLASS_ARRAY);
                     }
                     catch  (final NoSuchMethodException otherNoSuchMethodException)
                     {
                         // Left blank on purpose, since we don't know which
                         // ClassLoader implementation we have.
                     }
                 }
 
                 if  (method != null)
                 {
                     method.setAccessible(true);
 
                     result.append(
                         printURLClassPath(
                             method.invoke(classLoader, EMPTY_OBJECT_ARRAY)));
 
                     break;
                 }
 
                 classInstance = classInstance.getSuperclass();
             }
         }
         catch  (final SecurityException securityException)
         {
             // Left blank on purpose. Nothing to do if we cannot access
             // the classloader using reflection.
         }
         catch  (final IllegalArgumentException illegalArgumentException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final InvocationTargetException invocationTargetException)
         {
             // Left blank on purpose, since the getClasspath method
             // returned an error.
         }
         catch  (final IllegalAccessException illegalAccessException)
         {
             // Left blank on purpose. Nothing to do if we cannot invoke
             // the method on the classloader using reflection.
         }
 
         return result.toString();
     }
 
     /**
      * Checks whether given path contains a concrete class.
      * @param path the path.
      * @param className the name of the class.
      * @return <code>true</code> in such case.
      * @precondition path != null
      * @precondition className != null
      */
     public boolean pathContainsClass(final String path, final String className)
     {
         return pathContainsResource(path, className, ".class");
     }
 
     /**
      * Checks whether given path contains a concrete resource.
      * @param path the path.
      * @param resource the name of the resource.
      * @param suffix the suffix.
      * @return <code>true</code> in such case.
      * @precondition path != null
      * @precondition resource != null
      */
     public boolean pathContainsResource(
         final String path, final String resource, final String suffix)
     {
         // TODO: Add support for URL resources.
         return pathContainsFileResource(path, resource, suffix);
     }
 
     /**
      * Checks whether given (File) path contains a concrete resource.
      * @param path the path.
      * @param resource the name of the resource.
      * @param suffix the suffix.
      * @return <code>true</code> in such case.
      * @precondition path != null
      * @precondition resource != null
      */
     protected boolean pathContainsFileResource(
         final String path, final String resource, final String suffix)
     {
         boolean result = false;
 
         String auxSuffix = suffix;
 
         if  (auxSuffix == null)
         {
             auxSuffix = "";
         }
 
         String actualPath = path;
 
         if  (actualPath.startsWith("file:"))
         {
             actualPath = actualPath.substring(5);
         }
 
         while  (actualPath.startsWith("//"))
         {
             actualPath = actualPath.substring(1);
         }
             
         File file = new File(actualPath);
 
         if  (   (file.exists())
              && (file.canRead()))
         {
             if  (file.isDirectory())
             {
                 file =
                     new File(
                           removeTrailingSlash(file.getAbsolutePath())
                         + "/" + replace(resource, "\\.", "/")
                         + auxSuffix);
 
                 result =
                     (   (file.exists())
                      && (file.canRead()));
             }
             else
             {
                 try
                 {
                     InputStream inputStream = new FileInputStream(file);
 
                     ZipInputStream zipInputStream =
                         new ZipInputStream(inputStream);
 
                     String entryName =
                         replace(resource, "\\.", "/") + auxSuffix;
 
                     ZipEntry entry;
 
                     while  (zipInputStream.available() > 0)
                     {
                         entry = zipInputStream.getNextEntry();
 
                         if  (   (entry != null)
                              && (entryName.equals(entry.getName())))
                         {
                             result = true;
                             break;
                         }
                     }
 
                     zipInputStream.close();
 
                     inputStream.close();
                 }
                 catch  (final ZipException zipException)
                 {
                     // It's not a zip
                 }
                 catch  (final IOException ioException)
                 {
                     // Cannot read it or it's corrupt
                 }
                 catch  (final SecurityException securityException)
                 {
                     // I'm not allowed to access it
                 }
                 catch  (final IllegalArgumentException illegalArgumentException)
                 {
                     // For some other reason.
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Replaces given values.
      * @param text the text.
      * @param original the original token.
      * @param replacement the replacement.
      * @return the modified text.
      * @precondition text != null
      * @preocndition original != null
      * @preocndition replacement != null
      */
     protected String replace(
         final String text, final String original, final String replacement)
     {
         return text.replaceAll(original, replacement);
     }
 
     /**
      * Removes surrounding brackets.
      * @param text the text.
      * @return the trimmed text.
      * @precondition text != null
      */
     protected String trimBrackets(final String text)
     {
         String result = text;
 
         int length = (result != null) ? result.length() : 0;
 
         if  (length > 0)
         {
             while  (result.startsWith("["))
             {
                 result = result.substring(1);
             }
         }
 
         while  (   (result.endsWith("]"))
                 && (length > 1))
         {
             result = result.substring(0, length - 1);
             length = result.length();
         }
 
         return result;
     }
 
     /**
      * Removes any trailing slash.
      * @param text the text.
      * @return the trimmed text.
      * @precondition text != null
      */
     protected String removeTrailingSlash(final String text)
     {
         String result = text;
 
         int length = (result != null) ? result.length() : 0;
 
         while  (   (length > 1)
                 && (result.endsWith("/")))
         {
             result = result.substring(0, length - 1);
             length = result.length();
         }
 
         return result;
     }
 }

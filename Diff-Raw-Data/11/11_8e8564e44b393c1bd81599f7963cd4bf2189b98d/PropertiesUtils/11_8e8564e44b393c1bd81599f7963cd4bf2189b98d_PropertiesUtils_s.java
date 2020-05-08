 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.util;
 
 import java.util.Properties;
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 /**
  * Useful things to do with properties files.
  */
 public final class PropertiesUtils {
 
   private PropertiesUtils() {}
 
   /**
    * Get a {@link Properties} object from a file.
    * 
    * @param path a {@link File} path name
    * @return a {@link Properties} object
    * @throws IOException if there is a problem loading the file
    */
   public static Properties fromFile(File path) throws IOException {
     InputStream data = new FileInputStream(path);
     Properties them = new Properties();
     try {
       them.load(data);
     } catch (IOException e) {
       throw new IOException("Corrupt properties file `" + path + "': " +
       e.getMessage());
     }
 
     return them;
   }
 
   /**
    * Get a {@link Properties} object from a {@link Class}.
    * 
    * 
    * @param clazz the {@link Class} to look up
    * @param name the property file name
    * @return a {@link Properties} object
    * @throws IOException if the file cannot load or is not found
    */
   public static Properties fromResource(Class clazz, String name)
       throws IOException {
     InputStream is = clazz.getResourceAsStream(name);
 
     if (is == null)
       throw new FileNotFoundException(name + ": is it in CLASSPATH?");
 
     Properties them = new Properties();
     try {
       them.load(is);
     } catch (IOException e) {
       throw new IOException("Corrupt properties file `" + name + "': " +
       e.getMessage());
     }
 
     return them;
   }
 
   /**
    * Return a property.
    * 
    * @param properties the {@link Properties} object to look in 
    * @param propertyName the property to get 
    * @return the property value
    * @throws NoSuchPropertyException if the property is not set
    */
   public static String getOrDie(Properties properties, String propertyName)
       throws NoSuchPropertyException {
     String value = properties.getProperty(propertyName);
     if (value == null)
       throw new NoSuchPropertyException(properties, propertyName);
     return value;
   }
 
   /**
    * Get a property or return the supplied default.
    * 
    * @param properties the {@link Properties} object to look in 
    * @param propertyName the property to get 
    * @param def the default to return if not found
    * @return the property value
    */
   public static String getOrDefault(Properties properties, 
                                     String propertyName, String def) {
     String value = properties.getProperty(propertyName);
     if (value == null) return def;
     return value;
   }
 
   /**
    * Get an Integer property.
    * 
    * @param properties the {@link Properties} object to look in 
    * @param propertyName the property to get 
    * @return the int property value 
    * @throws NoSuchPropertyException if it is not found
    * @throws FormatPropertyException if it is not an Integer 
    */
   public static int getOrDie_int(Properties properties, String propertyName)
       throws NoSuchPropertyException, FormatPropertyException {
     String string = getOrDie(properties, propertyName);
     try {
       return Integer.parseInt(string);
     }
     catch (NumberFormatException e) {
       throw new FormatPropertyException(properties, propertyName, string,
       "an integer", e);
     }
   }
 
   /**
    * Get an Integer property from a {@link Properties} object or make a fuss. 
    * 
    * @param properties a {@link Properties} 
    * @param propertyName the name of the property
    * @param def cater for multiple definitions, with increment numbers
    * @return the property as an int 
    * @throws NoSuchPropertyException if it is not found
    * @throws FormatPropertyException if it is not an Integer 
    */
   public static int getOrDefault_int(Properties properties, 
                                      String propertyName, int def)
       throws NoSuchPropertyException, FormatPropertyException {
     String string = getOrDefault(properties, propertyName, ""+def);
     try {
       return Integer.parseInt(string);
     }
     catch (NumberFormatException e) {
       throw new FormatPropertyException(properties, propertyName, string,
       "an integer", e);
     }
   }
 
   /**
    * Instantiate an interface.
    * 
    * @param className the name of the class
    * @param interfaceClassName the interface Class name
    * @return a new object
    * @throws InstantiationPropertyException 
    *   if the named class does not descend from the interface
    */
   public static Object instanceOfNamedClass(String className, String interfaceClassName)
       throws InstantiationPropertyException {
     try {
       Class interfaceClass = Class.forName(interfaceClassName);
       Class clazz = Class.forName(className);
       if (!interfaceClass.isAssignableFrom(clazz))
         throw new ClassCastException(
                 clazz + " is not descended from " + interfaceClass);
         return clazz.newInstance();
     } catch (Exception e) {
       throw new InstantiationPropertyException(className, e);
     }
   }
 
   /**
    * Instantiate a Class.
    * 
    * @param properties a {@link Properties} 
    * @param propertyName the name of the property
    * @param interfaceClassName     the interface name
    * @param defaultName  a default concrete class if the property is undefined
    * @return a new Object
    * @throws InstantiationPropertyException if there is a problem
    */
   public static Object instanceOfNamedClass(Properties properties, 
                                             String propertyName,
                                             String interfaceClassName, 
                                             String defaultName)
   throws InstantiationPropertyException {
     String className =  (String)properties.get(propertyName);
     if (className == null)
       try {
         Class defaultClass = Class.forName(defaultName);
         return defaultClass.newInstance();
       }
      catch (Exception e) {
        throw new RuntimeException(e.toString());
      }
     return instanceOfNamedClass(className, interfaceClassName);
   }
 }

 package org.carlspring.ioc;
 
 /**
  * Copyright 2012 Martin Todorov.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.*;
 
 /**
  * @author mtodorov
  */
 public class PropertyValueInjector
 {
 
     /**
      * A list of cached properties.
      */
     static Map<String, Properties> cachedProperties = new LinkedHashMap<String, Properties>();
 
 
     public static void inject(Object target)
             throws IOException
     {
         Class clazz = target.getClass();
 
         loadPropertyResources(clazz);
 
         injectProperties(target, clazz);
     }
 
     private static void injectProperties(Object target,
                                          Class clazz)
             throws IOException
     {
         final List<Field> fields = new ArrayList<Field>();
 
         getAllFields(fields, clazz);
 
         for (Field field : fields)
         {
             PropertyValue propertyValue = field.getAnnotation(PropertyValue.class);
 
             if (propertyValue != null)
             {
                 String key = propertyValue.key();
 
                 if (key.trim().equals(""))
                 {
                     // Put a warning here when the logging has been set up.
 
                     // System.out.println("WARN: Ignoring @PropertyValue annotation on field '" + field.getName() +
                     //                    "' in class '" +clazz.getName() + "' as it has an empty key.");
 
                     continue;
                 }
 
                 String value;
                 if (System.getProperty(key) != null)
                 {
                     value = System.getProperty(key);
                 }
                 else
                 {
                     if (propertyValue.resource().trim().equals(""))
                     {
                         value = getMergedProperties().getProperty(key);
                     }
                     else
                     {
                         value = getValue(key, propertyValue.resource());
                     }
                 }
 
                setField(field, target, value);
             }
         }
     }
 
     public static List<Field> getAllFields(List<Field> fields,
                                            Class<?> clazz)
     {
         Collections.addAll(fields, clazz.getDeclaredFields());
 
         if (clazz.getSuperclass() != null)
         {
             fields = getAllFields(fields, clazz.getSuperclass());
         }
 
         return fields;
     }
 
     private static void loadPropertyResources(Class clazz)
             throws IOException
     {
         final List<Annotation> annotations = new ArrayList<Annotation>();
 
         getAllAnnotations(annotations, clazz);
 
         for (Annotation annotation : annotations)
         {
             if (annotation instanceof PropertiesResources)
             {
                 loadProperties(((PropertiesResources) annotation).resources());
             }
         }
     }
 
     public static List<Annotation> getAllAnnotations(List<Annotation> annotations,
                                                      Class<?> clazz)
     {
         Collections.addAll(annotations, clazz.getAnnotations());
 
         if (clazz.getSuperclass() != null)
         {
             annotations = getAllAnnotations(annotations, clazz.getSuperclass());
         }
 
         return annotations;
     }
 
     public static Properties getMergedProperties()
     {
         Properties merged = new Properties();
 
         for (String key : cachedProperties.keySet())
         {
             Properties properties = cachedProperties.get(key);
 
             for (Object o : properties.keySet())
             {
                 String propertyKey = (String) o;
                 merged.put(propertyKey, properties.getProperty(propertyKey));
             }
         }
 
         return merged;
     }
 
     public static String getValue(String key,
                                   String resource)
             throws IOException
     {
         final Properties properties = getProperties(resource);
 
         return properties.getProperty(key);
     }
 
     public static Properties getProperties(String resource)
             throws IOException
     {
         Properties properties;
         if (!cachedProperties.containsKey(resource))
         {
             loadProperties(resource);
         }
 
         properties = cachedProperties.get(resource);
 
         return properties;
     }
 
     private static void loadProperties(String[] resources)
             throws IOException
     {
         for (String resource : resources)
         {
             loadProperties(resource);
         }
     }
 
     private static void loadProperties(String resource)
             throws IOException
     {
         if (!cachedProperties.containsKey(resource))
         {
             Properties properties = new Properties();
             InputStream is = null;
             try
             {
                 is = PropertyValueInjector.class.getClassLoader().getResourceAsStream(resource);
                 properties.load(is);
             }
             finally
             {
                 if (is != null)
                 {
                     is.close();
                 }
             }
 
             cachedProperties.put(resource, properties);
         }
     }
 
     private static void setField(Field field,
                                  Object target,
                                  Object value)
     {
         if (!Modifier.isPublic(field.getModifiers()))
         {
             field.setAccessible(true);
         }
 
         try
         {
             field.set(target, value);
         }
         catch (IllegalAccessException iae)
         {
             throw new IllegalArgumentException("Could not set field " + field, iae);
         }
     }
 
 }

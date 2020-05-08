 // $Id$
 package org.yajul.bean;
 
 import org.apache.log4j.Logger;
 import org.yajul.util.ExceptionList;
 import org.yajul.util.ReflectionUtil;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents the property accessor methods for a given class, and provides methods
  * for accessing the properties at run-time.
  * @author josh Apr 4, 2004 9:11:19 AM
  */
 public class BeanProperties
 {
     private static Logger log = Logger.getLogger(BeanProperties.class);
     private Map accessorsByName = new HashMap();
     private Class clazz;
 
     /**
      * Constructs a set of property accessors from the class.
      * @param c the class
      */
     public BeanProperties(Class c)
     {
         clazz = c;
         Method[] methods = c.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
             Method method = methods[i];
             if (isGetter(method))
                 continue;
             String propertyName = ReflectionUtil.setterPropertyName(method);
             if (propertyName != null)
             {
                 PropertyAccessors accessors = getAccessorMethods(propertyName);
                 accessors.setSetter(method);
                 accessors.setType(method.getParameterTypes()[0]);
             }
         }
     }
 
     /**
      * Returns the name of the class.
      * @return the name of the class.
      */
     public String getClassName()
     {
         return clazz.getName();
     }
 
     /**
      * Returns the number of properties in the class.
      * @return the number of properties in the class.
      */
     public int size()
     {
         return accessorsByName.size();
     }
 
     /**
      * Returns the value of the specified property.
      * @param bean the object
      * @param propertyName the name of the property to get
      * @return the value of the property in the specified bean
      */
     public Object getProperty(Object bean,String propertyName)
     {
         try
         {
             PropertyAccessors accessorMethods = getAccessorMethods(bean, propertyName);
             return accessorMethods.invokeGetter(bean);
         }
         catch (NoSuchMethodException e)
         {
             log.error(e,e);
             return null;
         }
         catch (IllegalAccessException e)
         {
             log.error(e,e);
             return null;
         }
         catch (InvocationTargetException e)
         {
             log.error(e,e);
             return null;
         }
     }
 
     /**
      * Sets the property in the bean to the specified value.
      * @param bean the object to set the property in
      * @param propertyName the name of the property
      * @param value the new property value
      * @return the new property value
      */
     public Object setProperty(Object bean,String propertyName,Object value)
     {
         try
         {
             PropertyAccessors accessorMethods = getAccessorMethods(bean, propertyName);
             return accessorMethods.invokeSetter(bean,value);
         }
         catch (Exception e)
         {
             log.error(e,e);
             return null;
         }
     }
 
     /**
      * Returns an iterator that returns the String names of all properties in the class.
      * @return an iterator that returns the String names of all properties in the class.
      */
     public Iterator propertyNames()
     {
         return accessorsByName.keySet().iterator();
     }
 
     /**
      * Returns the set of all the property name Strings in the class.
      * @return the set of all the property name Strings in the class.
      */
     public Set getPropertyNames()
     {
         return accessorsByName.keySet();
     }
 
     /**
      * Returns the pair of accessor methods for the given property name.
      * @param propertyName the property name.
      * @return the accessor methods for the property
      */
     public PropertyAccessors getAccessorMethods(String propertyName)
     {
         PropertyAccessors methods = (PropertyAccessors) accessorsByName.get(propertyName);
         if (methods == null)
         {
             methods = new PropertyAccessors(this, propertyName);
             accessorsByName.put(propertyName, methods);
         }
         return methods;
     }
 
     /**
      * Returns an iterator that returns all AccessorMethods for all properties in the class.
      * @return an iterator that returns all AccessorMethods for all properties in the class.
      */
     public Iterator accessorMethods()
     {
         return accessorsByName.values().iterator();
     }
 
     /**
      * Returns a double property value.
      * @param bean the bean
      * @param propertyName the property name
      * @return the property value as a double
      */
     public double getDoubleProperty(Object bean, String propertyName)
     {
         Double value = (Double) getProperty(bean,propertyName);
         return value.doubleValue();
     }
 
     /**
      * Returns an int property value.
      * @param bean the bean
      * @param propertyName the property name
      * @return the int property value
      */
     public int getIntProperty(Object bean, String propertyName)
     {
         Integer value = (Integer) getProperty(bean,propertyName);
         return value.intValue();
     }
 
     /**
      * Returns a collection of all of the property values in the specified bean.
      * @param bean the bean
      * @return a collection of all of the property values in the specified bean
      * @throws Exception if a property cannot be accessed.
      */
     public Collection values(Object bean) throws Exception
     {
         ArrayList values = new ArrayList(size());
         Iterator iter = accessorMethods();
         ExceptionList exceptions = new ExceptionList();

         while (iter.hasNext())
         {
             PropertyAccessors accessorMethods = (PropertyAccessors) iter.next();
             try
             {
                 values.add(accessorMethods.invokeGetter(bean));
             }
             catch (Exception e)
             {
                 exceptions.add(e);
             }
         }
         exceptions.throwIfException();
         return values;
     }
 
     /**
      * Copies all of the properties from one bean into another.
      * The two beans must be of the same class.
      * @param bean the source bean.
      * @param copy the target bean.
      * @throws Exception if a property could not be accessed.
      */
     public void copy(Object bean,Object copy) throws Exception
     {
         Iterator iter = accessorMethods();
         ExceptionList exceptions = new ExceptionList();
         while (iter.hasNext())
         {
             PropertyAccessors accessorMethods = (PropertyAccessors) iter.next();
             try
             {
                 Object value = accessorMethods.invokeGetter(bean);
                 accessorMethods.invokeSetter(copy,value);
             }
             catch (Exception e)
             {
                 exceptions.add(e);
             }
         }
         exceptions.throwIfException();
     }
 
     // --- Implementation methods ---
 
     private PropertyAccessors getAccessorMethods(Object bean, String key)
             throws NoSuchMethodException
     {
         checkBean(bean);
         // Look up the Method for the property name.
         PropertyAccessors accessorMethods = getAccessorMethods(key);
         if (accessorMethods == null)
             throw new NoSuchMethodException("No property " + key + " in class " + getClassName());
         return accessorMethods;
     }
 
     private void checkBean(Object bean)
     {
         if (bean == null)
             throw new NullPointerException("Bean cannot be null!");
         if (bean.getClass() != clazz)
             throw new IllegalArgumentException("Bean class "
                     + bean.getClass().getName() + " does not match class " + getClassName());
     }
 
     private boolean isGetter(Method method)
     {
         String propertyName = ReflectionUtil.getterPropertyName(method);
         if (propertyName != null)
         {
             PropertyAccessors accessors = getAccessorMethods(propertyName);
             accessors.setGetter(method);
             accessors.setType(method.getReturnType());
             return true;
         }
         else
             return false;
     }
 
     // --- Inner classes ---
 
 }

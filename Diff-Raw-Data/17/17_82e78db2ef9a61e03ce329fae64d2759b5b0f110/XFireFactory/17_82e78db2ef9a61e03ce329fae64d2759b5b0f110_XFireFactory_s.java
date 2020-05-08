 package org.codehaus.xfire;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 
 /**
  * <p/>
  * The XFireFactory class allows you to embed XFire within your apps easily. </p>
  * <p/>
  * This class assumes one XFire instance per JVM. To create many XFire instances you must use your own configuration and
  * instantiation mechanism. </p>
  * <p/>
  * If you want to provide your own Factory you must: <ul> <li>Register your factory via
 * <code>registerFactory</code></li> <li>Implment <code>public static XFireFactory createInstance()</code> </ul> </p>
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class XFireFactory
 {
     private static XFireFactory standalone;
     private static Class defaultFacClass;
     private static HashMap factories = new HashMap();
     private static HashMap factoryClasses = new HashMap();
 
     private XFire xfire;
 
     protected XFireFactory()
            throws Exception
     {
         xfire = new DefaultXFire();
     }
 
     protected XFireFactory(XFire xfire)
            throws Exception
     {
        xfire = new DefaultXFire();
     }
 
     protected static XFireFactory createInstance()
            throws Exception
     {
         return new XFireFactory();
     }
 
     public static XFireFactory newInstance()
            throws Exception
     {
         if (standalone == null)
         {
             synchronized (XFireFactory.class)
             {
                 if (defaultFacClass != null)
                 {
                     standalone = loadFactory(defaultFacClass);
                 }
                 else
                 {
                     standalone = new XFireFactory();
                 }
             }
         }
         return standalone;
     }
 
     private static XFireFactory loadFactory(Class clazz)
     {
         try
         {
             Method m = clazz.getMethod("createInstance", new Class[0]);
 
             return (XFireFactory) m.invoke(null, new Object[0]);
         }
         catch (SecurityException e)
         {
             throw new XFireRuntimeException("Couldn't load " + clazz.getName(), e);
         }
         catch (NoSuchMethodException e)
         {
             throw new XFireRuntimeException("Factory doesn't implement createInstance(): " + clazz.getName(), e);
         }
         catch (IllegalArgumentException e)
         {
             throw new XFireRuntimeException("Factory doesn't implement createInstance(): " + clazz.getName(), e);
         }
         catch (IllegalAccessException e)
         {
             throw new XFireRuntimeException("Couldn't load " + clazz.getName(), e);
         }
         catch (InvocationTargetException e)
         {
             throw new XFireRuntimeException("Couldn't load factory " + clazz.getName(), e);
         }
 
     }
 
     public static XFireFactory newInstance(String selector)
            throws Exception
     {
         XFireFactory fac = (XFireFactory) factories.get(selector);
         if (fac == null)
         {
             synchronized (XFireFactory.class)
             {
                 Class clazz = (Class) factoryClasses.get(selector);
                 if (clazz == null)
                     return null;
 
                 fac = loadFactory(clazz);
             }
         }
         return fac;
     }
 
     /**
      * Register an XFireFactory class.
      *
      * @param factoryClass
      * @param def          Whether or not this should be the default factory.
      */
     public static void registerFactory(Class factoryClass, boolean def)
     {
         if (def)
             defaultFacClass = factoryClass;
 
         factoryClasses.put(factoryClass.getName(), factoryClass);
     }
 
     public XFire getXFire()
            throws Exception
     {
         return xfire;
     }
 }

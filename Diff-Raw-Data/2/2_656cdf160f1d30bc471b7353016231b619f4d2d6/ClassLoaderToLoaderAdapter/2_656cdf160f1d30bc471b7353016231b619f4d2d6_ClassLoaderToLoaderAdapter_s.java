 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.classloader.plugins.loader;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.security.*;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Set;
 
 import org.jboss.classloader.spi.CacheLoader;
 import org.jboss.classloader.spi.base.BaseClassLoader;
 import org.jboss.classloader.spi.base.BaseClassLoaderSource;
 import org.jboss.logging.Logger;
 
 /**
  * ClassLoaderToLoaderAdapter.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="ales.justin@jboss.org">Ales Justin</a>
  * @version $Revision: 1.1 $
  */
 public class ClassLoaderToLoaderAdapter extends BaseClassLoaderSource implements CacheLoader
 {
    /** The log */
    private static final Logger log = Logger.getLogger(ClassLoaderToLoaderAdapter.class);
    
    /** The access control context of the creator of this adapter */
    private AccessControlContext accessControlContext;
    
    /** The get package method */
    private static Method getPackage;
    
    /** The get packages method */
    private static Method getPackages;
    
    /** The find class method */
    private static Method findLoadedClass;
 
    static
    {
       AccessController.doPrivileged(new PrivilegedAction<Object>()
       {
          public Object run()
          {
             try
             {
                getPackage = ClassLoader.class.getDeclaredMethod("getPackage", String.class);
                getPackage.setAccessible(true);
             }
             catch (Exception e)
             {
                log.warn("Unable to set accessible on ClassLoader.getPackage()", e);
             }
             try
             {
                getPackages = ClassLoader.class.getDeclaredMethod("getPackages");
                getPackages.setAccessible(true);
             }
             catch (Exception e)
             {
                log.warn("Unable to set accessible on ClassLoader.getPackages()", e);
             }
             try
             {
                findLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
                findLoadedClass.setAccessible(true);
             }
             catch (Exception e)
             {
               log.warn("Unable to set accessible on ClassLoader.getPackages()", e);
             }
             return null;
          }
       });
    }
    
    /**
     * Create a new ClassLoaderToLoaderAdapter.
     * 
     * @param classLoader the classloader
     */
    public ClassLoaderToLoaderAdapter(ClassLoader classLoader)
    {
       super(classLoader);
       accessControlContext = AccessController.getContext();
    }
 
    public URL getResource(final String name)
    {
       final ClassLoader classLoader = getClassLoader();
       URL url;
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
       {
          url = AccessController.doPrivileged(new PrivilegedAction<URL>()
          {
             public URL run()
             {
                return classLoader.getResource(name);
             }
          }, accessControlContext);
          
       }
       else
       {
          url = classLoader.getResource(name);
       }
       
       if (log.isTraceEnabled())
       {
          if (url != null)
             log.trace("Resource " + name + " found in " + classLoader);
          else
             log.trace("Resource " + name + " NOT found in " + classLoader);
       }
       return url;
    }
 
    // FindBugs: The Set doesn't use equals/hashCode
    public void getResources(final String name, Set<URL> urls) throws IOException
    {
       final ClassLoader classLoader = getClassLoader();
       Enumeration<URL> enumeration;
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
       {
          try
          {
             enumeration = AccessController.doPrivileged(new PrivilegedExceptionAction<Enumeration<URL>>()
             {
                public Enumeration<URL> run() throws Exception
                {
                   return classLoader.getResources(name);
                }
             }, accessControlContext);
          }
          catch (PrivilegedActionException e)
          {
             Exception e1 = e.getException();
             if (e1 instanceof RuntimeException)
                throw (RuntimeException) e1;
             if (e1 instanceof IOException)
                throw (IOException) e1;
             IOException e2 = new IOException("Unexpected error");
             e2.initCause(e1);
             throw e2;
          }
       }
       else
       {
          enumeration = classLoader.getResources(name);
       }
       while (enumeration.hasMoreElements())
          urls.add(enumeration.nextElement());
    }
 
    public Class<?> loadClass(String className)
    {
       final ClassLoader classLoader = getClassLoader();
       try
       {
          return Class.forName(className, false, classLoader);
       }
       catch (ClassNotFoundException e)
       {
          return null;
       }
    }
 
    public Package getPackage(String name)
    {
       if (getPackage == null)
          return null;
 
       final ClassLoader classLoader = getClassLoader();
       try
       {
          return (Package) getPackage.invoke(classLoader, name);
       }
       catch (Exception e)
       {
          log.warn("Unexpected error retrieving package " + name + " from classloader " + classLoader, e);
          return null;
       }
    }
 
    public void getPackages(Set<Package> packages)
    {
       if (getPackages == null)
          return;
 
       final ClassLoader classLoader = getClassLoader();
       try
       {
          Package[] pckgs = (Package[]) getPackages.invoke(classLoader);
          if (pckgs != null)
             packages.addAll(Arrays.asList(pckgs));
       }
       catch (Exception e)
       {
          log.warn("Unexpected error retrieving packages from classloader " + classLoader, e);
       }
    }
 
    public Class<?> checkClassCache(BaseClassLoader bcl, String name, String path, boolean allExports)
    {
       if (findLoadedClass == null)
          return null;
 
       final ClassLoader classLoader = getClassLoader();
       try
       {
          Class<?> clazz = (Class<?>) findLoadedClass.invoke(classLoader, name);
 
          if (clazz != null && log.isTraceEnabled())
             log.trace("Found " + name + " in cache: " + this);
 
          return clazz;
       }
       catch (Exception e)
       {
          log.warn("Unexpected error retrieving found class " + name + " from classloader " + classLoader, e);
          return null;
       }
    }
 
    @Override
    public String toString()
    {
       return getClassLoader().toString();
    }
 }

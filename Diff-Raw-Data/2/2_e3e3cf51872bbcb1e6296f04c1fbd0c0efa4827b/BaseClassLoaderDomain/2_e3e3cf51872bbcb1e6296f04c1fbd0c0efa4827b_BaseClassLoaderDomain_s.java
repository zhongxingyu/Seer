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
 package org.jboss.classloader.spi.base;
 
 import java.io.IOException;
 import java.net.URL;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.jboss.classloader.plugins.ClassLoaderUtils;
 import org.jboss.classloader.spi.ClassLoaderPolicy;
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.classloader.spi.Loader;
 import org.jboss.logging.Logger;
 
 /**
  * BaseClassLoaderDomain.<p>
  * 
  * This class hides some of the implementation details and allows
  * package access to the protected methods.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class BaseClassLoaderDomain implements Loader
 {
    /** The log */
    private static final Logger log = Logger.getLogger(BaseClassLoaderDomain.class);
 
    /** The classloader system to which we belong */
    private BaseClassLoaderSystem system;
    
    /** The classloaders  in the order they were registered */
    private List<ClassLoaderInformation> classLoaders = new CopyOnWriteArrayList<ClassLoaderInformation>();
 
    /** The classloader information by classloader */
    private Map<ClassLoader, ClassLoaderInformation> infos = new ConcurrentHashMap<ClassLoader, ClassLoaderInformation>();
    
    /** The classloaders by package name */
    private Map<String, List<ClassLoaderInformation>> classLoadersByPackageName = new ConcurrentHashMap<String, List<ClassLoaderInformation>>();
    
    /** The global class cache */
    private Map<String, Loader> globalClassCache = new ConcurrentHashMap<String, Loader>();
    
    /** The global class black list */
    private Map<String, String> globalClassBlackList = new ConcurrentHashMap<String, String>();
    
    /** The global resource cache */
    private Map<String, URL> globalResourceCache = new ConcurrentHashMap<String, URL>();
    
    /** The global resource black list */
    private Map<String, String> globalResourceBlackList = new ConcurrentHashMap<String, String>();
    
    /** Keep track of the added order */
    private int order = 0;
    
    /**
     * Flush the internal caches
     */
    public void flushCaches()
    {
       globalClassCache.clear();
       globalClassBlackList.clear();
       globalResourceCache.clear();
       globalResourceBlackList.clear();
    }
 
    public int getClassBlackListSize()
    {
       return globalClassBlackList.size();
    }
 
    public int getClassCacheSize()
    {
       return globalClassCache.size();
    }
 
    public int getResourceBlackListSize()
    {
       return globalClassBlackList.size();
    }
 
    public int getResourceCacheSize()
    {
       return globalClassCache.size();
    }
    
    public Set<String> listClassBlackList()
    {
       return Collections.unmodifiableSet(globalClassBlackList.keySet());
    }
 
    public Map<String, String> listClassCache()
    {
       Map<String, String> result = new HashMap<String, String>(globalClassCache.size());
       for (Map.Entry<String, Loader> entry : globalClassCache.entrySet())
          result.put(entry.getKey(), entry.getValue().toString());
       return result;
    }
 
    public Set<String> listResourceBlackList()
    {
       return Collections.unmodifiableSet(globalResourceBlackList.keySet());
    }
 
    public Map<String, URL> listResourceCache()
    {
       return Collections.unmodifiableMap(globalResourceCache);
    }
 
    /**
     * Get the classloader system
     * 
     * @return the classloader system
     */
    protected synchronized BaseClassLoaderSystem getClassLoaderSystem()
    {
       return system;
    }
    
    /**
     * Get the classloader system
     * 
     * @param system the classloader system
     */
    synchronized void setClassLoaderSystem(BaseClassLoaderSystem system)
    {
       if (system == null)
          shutdownDomain();
       this.system = system;
    }
 
    /**
     * Shutdown the domain<p>
     * 
     * The default implementation just unregisters all classloaders
     */
    protected void shutdownDomain()
    {
       log.debug(toString() + " shutdown!");
 
       // Unregister all classloaders
       while (true)
       {
          Iterator<ClassLoaderInformation> iterator = classLoaders.iterator();
          if (iterator.hasNext() == false)
             break;
 
          while (iterator.hasNext())
          {
             ClassLoaderInformation info = iterator.next();
             if (info != null)
                unregisterClassLoader(info.getClassLoader());
          }
       }
       
       flushCaches();
    }
    
    /**
     * Whether the domain has classloaders
     * 
     * @return true when the domain has classloaders
     */
    public boolean hasClassLoaders()
    {
       return classLoaders.isEmpty() == false;
    }
    
    /**
     * Whether to use load class for parent
     * 
     * @return true to load class on the parent loader
     */
    public abstract boolean isUseLoadClassForParent();
    
    /**
     * Transform the byte code<p>
     * 
     * By default, this delegates to the classloader system
     * 
     * @param classLoader the classloader
     * @param className the class name
     * @param byteCode the byte code
     * @param protectionDomain the protection domain
     * @return the transformed byte code
     * @throws Exception for any error
     */
    protected byte[] transform(ClassLoader classLoader, String className, byte[] byteCode, ProtectionDomain protectionDomain) throws Exception
    {
       BaseClassLoaderSystem system = getClassLoaderSystem();
       if (system != null)
          return system.transform(classLoader, className, byteCode, protectionDomain);
       return byteCode;
    }
 
    /**
     * Load a class from the domain
     * 
     * @param classLoader the classloader
     * @param name the class name
     * @param allExports whether we should look at all exports
     * @return the class
     * @throws ClassNotFoundException for any error
     */
    protected Class<?> loadClass(BaseClassLoader classLoader, String name, boolean allExports) throws ClassNotFoundException
    {
       boolean trace = log.isTraceEnabled();
 
       boolean findInParent = (isUseLoadClassForParent() == false);
       
       // Should we directly load from the parent?
       if (findInParent == false)
       {
          Class<?> clazz = loadClassBefore(name);
          if (clazz != null)
             return clazz;
       }
       
       String path = ClassLoaderUtils.classNameToPath(name);
       
       Loader loader = findLoader(classLoader, path, allExports, findInParent);
       if (loader != null)
       {
          Thread thread = Thread.currentThread();
          ClassLoadingTask task = new ClassLoadingTask(name, classLoader, thread);
          ClassLoaderManager.scheduleTask(task, loader, false);
          return ClassLoaderManager.process(thread, task);
       }
       
       // Should we directly load from the parent?
       if (findInParent == false)
       {
          Class<?> clazz = loadClassAfter(name);
          if (clazz != null)
             return clazz;
       }
 
       // Finally see whether this is the JDK assuming it can load its classes from any classloader
       if (classLoader != null)
       {
          BaseClassLoaderPolicy policy = classLoader.getPolicy();
          ClassLoader hack = policy.isJDKRequest(name);
          if (hack != null)
          {
             if (trace)
                log.trace(this + " trying to load " + name + " using hack " + hack);
             Class<?> result = Class.forName(name, false, hack);
             if (result != null)
             {
                if (trace)
                   log.trace(this + " loaded from hack " + hack + " " + ClassLoaderUtils.classToString(result));
                return result;
             }
          }
       }
       
       // Didn't find it
       return null;
    }
 
    /**
     * Find a loader for a class
     * 
     * @param name the class resource name
     * @return the loader
     */
    protected Loader findLoader(String name)
    {
       return findLoader(null, name, true, true);
    }
 
    /**
     * Find a loader for a class
     * 
     * @param classLoader the classloader
     * @param name the class resource name
     * @param allExports whether we should look at all exports
     * @return the loader
     */
    Loader findLoader(BaseClassLoader classLoader, String name, boolean allExports, boolean findInParent)
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace(this + " findLoader " + name + " classLoader=" + classLoader + " allExports=" + allExports + " findInParent=" + findInParent);
       
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
       
       // Try the before attempt (e.g. from the parent)
       Loader loader = null;
       if (findInParent)
          loader = findBeforeLoader(name);
       if (loader != null)
          return loader;
 
       // Work out the rules
       ClassLoaderInformation info = null;
       BaseClassLoaderPolicy policy;
       if (classLoader != null)
       {
          info = infos.get(classLoader);
          policy = classLoader.getPolicy();
          if (policy.isImportAll())
             allExports = true;
       }
 
       // Next we try the old "big ball of mud" model      
       if (allExports)
       {
          loader = findLoaderInExports(classLoader, name, trace);
          if (loader != null)
             return loader;
       }
       else if (trace)
          log.trace(this + " not loading " + name + " from all exports");
       
       // Next we try the imports
       if (info != null)
       {
          loader = findLoaderInImports(info, name, trace);
          if (loader != null)
             return loader;
       }
 
       // Next use any requesting classloader, this will look at everything not just what it exports
       if (classLoader != null)
       {
          if (trace)
             log.trace(this + " trying to load " + name + " from requesting " + classLoader);
          if (classLoader.getResourceLocally(name) != null)
             return classLoader.getLoader();
       }
 
       // Try the after attempt (e.g. from the parent)
       if (findInParent)
          return findAfterLoader(name);
       
       return null;
    }
    
    /**
     * Load a resource from the domain
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param allExports whether we should look at all exports
     * @return the url
     */
    URL getResource(BaseClassLoader classLoader, String name, boolean allExports)
    {
       boolean trace = log.isTraceEnabled();
 
       // Try the classloader first
       if (classLoader != null)
       {
          if (trace)
             log.trace(this + " trying to get resource " + name + " from requesting " + classLoader);
          URL result = classLoader.getResourceLocally(name);
          if (result != null)
          {
             if (trace)
                log.trace(this + " got resource from requesting " + classLoader + " " + result);
             return result;
          }
       }
 
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
 
       // Try the before attempt
       URL result = beforeGetResource(name);
       if (result != null)
          return result;
 
       // Work out the rules
       ClassLoaderInformation info = null;
       BaseClassLoaderPolicy policy;
       if (classLoader != null)
       {
          policy = classLoader.getPolicy();
          info = infos.get(classLoader);
          if (policy.isImportAll())
             allExports = true;
       }
 
       // Next we try the old "big ball of mud" model      
       if (allExports)
       {
          result = getResourceFromExports(classLoader, name, trace);
          if (result != null)
             return result;
       }
       else if (trace)
          log.trace(this + " not getting resource " + name + " from all exports");
       
       // Next we try the imports
       if (info != null)
       {
          result = getResourceFromImports(info, name, trace);
          if (result != null)
             return result;
       }
 
       // Try the after attempt
       result = afterGetResource(name);
       if (result != null)
          return result;
       
       // Didn't find it
       return null;
    }
    
    /**
     * Load resources from the domain
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param allExports whether we should look at all exports
     * @param urls the urls to add to
     * @throws IOException for any error
     */
    void getResources(BaseClassLoader classLoader, String name, Set<URL> urls, boolean allExports) throws IOException
    {
       boolean trace = log.isTraceEnabled();
 
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
 
       // Try the before attempt
       beforeGetResources(name, urls);
 
       // Work out the rules
       ClassLoaderInformation info = null;
       BaseClassLoaderPolicy policy;
       if (classLoader != null)
       {
          policy = classLoader.getPolicy();
          info = infos.get(classLoader);
          if (policy.isImportAll())
             allExports = true;
       }
 
       // Next we try the old "big ball of mud" model      
       if (allExports)
          getResourcesFromExports(classLoader, name, urls, trace);
       else if (trace)
          log.trace(this + " not getting resource " + name + " from all exports");
       
       // Next we try the imports
       if (info != null)
          getResourcesFromImports(info, name, urls, trace);
 
       // Finally use any requesting classloader
       if (classLoader != null)
       {
          if (trace)
             log.trace(this + " trying to get resources " + name + " from requesting " + classLoader);
          classLoader.getResourcesLocally(name, urls);
       }
 
       // Try the after attempt
       afterGetResources(name, urls);
    }
    
    /**
     * Load a package from the domain
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param allExports whether we should look at all exports
     * @return the package
     */
    Package getPackage(BaseClassLoader classLoader, String name, boolean allExports)
    {
       boolean trace = log.isTraceEnabled();
 
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
 
       // Try the before attempt
       Package result = beforeGetPackage(name);
       if (result != null)
          return result;
 
       // Work out the rules
       ClassLoaderInformation info = null;
       BaseClassLoaderPolicy policy;
       if (classLoader != null)
       {
          policy = classLoader.getPolicy();
          info = infos.get(classLoader);
          if (policy.isImportAll())
             allExports = true;
       }
 
       // Next we try the old "big ball of mud" model      
       if (allExports)
       {
          result = getPackageFromExports(classLoader, name, trace);
          if (result != null)
             return result;
       }
       else if (trace)
          log.trace(this + " not getting package " + name + " from all exports");
       
       // Next we try the imports
       if (info != null)
       {
          result = getPackageFromImports(info, name, trace);
          if (result != null)
             return result;
       }
 
       // Finally use any requesting classloader
       if (classLoader != null)
       {
          if (trace)
             log.trace(this + " trying to get package " + name + " from requesting " + classLoader);
          result = classLoader.getPackageLocally(name);
          if (result != null)
          {
             if (trace)
                log.trace(this + " got package from requesting " + classLoader + " " + result);
             return result;
          }
       }
 
       // Try the after attempt
       result = afterGetPackage(name);
       if (result != null)
          return result;
       
       // Didn't find it
       return null;
    }
    
    /**
     * Load packages from the domain
     * 
     * @param classLoader the classloader
     * @param name the package name
     * @param allExports whether we should look at all exports
     * @param packages the packages to add to
     */
    void getPackages(BaseClassLoader classLoader, Set<Package> packages, boolean allExports)
    {
       boolean trace = log.isTraceEnabled();
 
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
 
       // Try the before attempt
       beforeGetPackages(packages);
 
       // Work out the rules
       ClassLoaderInformation info = null;
       BaseClassLoaderPolicy policy;
       if (classLoader != null)
       {
          policy = classLoader.getPolicy();
          info = infos.get(classLoader);
          if (policy.isImportAll())
             allExports = true;
       }
 
       // Next we try the old "big ball of mud" model      
       if (allExports)
          getPackagesFromExports(classLoader, packages, trace);
       else if (trace)
          log.trace(this + " not getting packages from all exports");
       
       // Next we try the imports
       if (info != null)
          getPackagesFromImports(info, packages, trace);
 
       // Finally use any requesting classloader
       if (classLoader != null)
       {
          if (trace)
             log.trace(this + " trying to get packages from requesting " + classLoader);
          classLoader.getPackagesLocally(packages);
       }
 
       // Try the after attempt
       afterGetPackages(packages);
    }
    
    /**
     * Find a loader for class in exports
     * 
     * @param classLoader the classloader
     * @param name the class resource name
     * @param trace whether trace is enabled
     * @return the loader
     */
    private Loader findLoaderInExports(BaseClassLoader classLoader, String name, boolean trace)
    {
       Loader loader = globalClassCache.get(name);
       if (loader != null)
       {
          if (trace)
             log.trace(this + " found in global class cache " + name);
 
          return loader;
       }
 
       if (globalClassBlackList.containsKey(name))
       {
          if (trace)
             log.trace(this + " class is black listed " + name);
          return null;
       }
       boolean canCache = true;
       boolean canBlackList = true;
       
       String packageName = ClassLoaderUtils.getResourcePackageName(name);
       List<ClassLoaderInformation> list = classLoadersByPackageName.get(packageName);
       if (trace)
          log.trace(this + " trying to load " + name + " from all exports of package " + packageName + " " + list);
       if (list != null && list.isEmpty() == false)
       {
          for (ClassLoaderInformation info : list)
          {
             BaseDelegateLoader exported = info.getExported();
             
             // See whether the policies allow caching/blacklisting
             BaseClassLoaderPolicy loaderPolicy = exported.getPolicy();
             if (loaderPolicy == null || loaderPolicy.isCacheable() == false)
                canCache = false;
             if (loaderPolicy == null || loaderPolicy.isBlackListable() == false)
                canBlackList = false;
 
             if (exported.getResource(name) != null)
             {
                if (canCache)
                   globalClassCache.put(name, exported);
                return exported;
             }
          }
       }
       // Here is not found in the exports so can we blacklist it?
       if (canBlackList)
          globalClassBlackList.put(name, name);
       
       return null;
    }
    
    /**
     * Load a resource from the exports
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param trace whether trace is enabled
     * @return the url
     */
    private URL getResourceFromExports(BaseClassLoader classLoader, String name, boolean trace)
    {
       URL result = globalResourceCache.get(name);
       if (result != null)
       {
          if (trace)
             log.trace(this + " got resource from cache " + name);
       }
       
       if (globalResourceBlackList.containsKey(name))
       {
          if (trace)
             log.trace(this + " resource is black listed, not looking at exports " + name);
          return null;
       }
       boolean canCache = true;
       boolean canBlackList = true;
       
       String packageName = ClassLoaderUtils.getResourcePackageName(name);
       List<ClassLoaderInformation> list = classLoadersByPackageName.get(packageName);
       if (trace)
          log.trace(this + " trying to get resource " + name + " from all exports " + list);
       if (list != null && list.isEmpty() == false)
       {
          for (ClassLoaderInformation info : list)
          {
             BaseDelegateLoader loader = info.getExported();
             
             // See whether the policies allow caching/blacklisting
             BaseClassLoaderPolicy loaderPolicy = loader.getPolicy();
             if (loaderPolicy == null || loaderPolicy.isCacheable() == false)
                canCache = false;
             if (loaderPolicy == null || loaderPolicy.isBlackListable() == false)
                canBlackList = false;
 
             result = loader.getResource(name);
             if (result != null)
             {
                if (canCache)
                   globalResourceCache.put(name, result);
                return result;
             }
          }
       }
       // Here is not found in the exports so can we blacklist it?
       if (canBlackList)
          globalResourceBlackList.put(name, name);
       return null;
    }
    
    /**
     * Load resources from the exports
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param urls the urls to add to
     * @param trace whether trace is enabled
     * @throws IOException for any error
     */
    void getResourcesFromExports(BaseClassLoader classLoader, String name, Set<URL> urls, boolean trace) throws IOException
    {
       String packageName = ClassLoaderUtils.getResourcePackageName(name);
       List<ClassLoaderInformation> list = classLoadersByPackageName.get(packageName);
       if (trace)
          log.trace(this + " trying to get resources " + name + " from all exports " + list);
       if (list != null && list.isEmpty() == false)
       {
          for (ClassLoaderInformation info : list)
          {
             BaseDelegateLoader loader = info.getExported();
             loader.getResources(name, urls);
          }
       }
    }
    
    /**
     * Load a package from the exports
     * 
     * @param classLoader the classloader
     * @param name the package name
     * @param trace whether trace is enabled
     * @return the package
     */
    private Package getPackageFromExports(BaseClassLoader classLoader, String name, boolean trace)
    {
       List<ClassLoaderInformation> list = classLoadersByPackageName.get(name);
       if (trace)
          log.trace(this + " trying to get package " + name + " from all exports " + list);
       if (list != null && list.isEmpty() == false)
       {
          for (ClassLoaderInformation info : list)
          {
             BaseDelegateLoader loader = info.getExported();
 
             Package result = loader.getPackage(name);
             if (result != null)
                return result;
          }
       }
       return null;
    }
    
    /**
     * Load packages from the exports
     * 
     * @param classLoader the classloader
     * @param packages the packages to add to
     * @param trace whether trace is enabled
     */
    void getPackagesFromExports(BaseClassLoader classLoader, Set<Package> packages, boolean trace)
    {
       List<ClassLoaderInformation> list = classLoaders;
       if (trace)
          log.trace(this + " trying to get all packages from all exports " + list);
       if (list != null && list.isEmpty() == false)
       {
          for (ClassLoaderInformation info : list)
          {
             BaseDelegateLoader loader = info.getExported();
             loader.getPackages(packages);
          }
       }
    }
 
    /**
     * Find a loader for a class in imports
     * 
     * @param info the classloader information
     * @param name the class resource name
     * @param trace whether trace is enabled
     * @return the loader
     */
    Loader findLoaderInImports(ClassLoaderInformation info, String name, boolean trace)
    {
       List<? extends DelegateLoader> delegates = info.getDelegates();
       if (delegates == null || delegates.isEmpty())
       {
          if (trace)
             log.trace(this + " not loading " + name + " from imports it has no delegates");
          return null;
       }
 
       Loader loader = info.getCachedLoader(name);
       if (loader != null)
       {
          if (trace)
             log.trace(this + " found in import cache " + name);
          return loader;
       }
       
       if (info.isBlackListedClass(name))
       {
          if (trace)
             log.trace(this + " class is black listed in imports " + name);
          return null;
       }
       
       for (DelegateLoader delegate : delegates)
       {
          if (trace)
             log.trace(this + " trying to load " + name + " from import " + delegate + " for " + info.getClassLoader());
          if (delegate.getResource(name) != null)
          {
             info.cacheLoader(name, delegate);
             return delegate;
          }
       }
       info.blackListClass(name);
       return null;
    }
    
    /**
     * Load a resource from the imports
     * 
     * @param info the classloader information
     * @param name the resource name
     * @param trace whether trace is enabled
     * @return the url
     */
    private URL getResourceFromImports(ClassLoaderInformation info, String name, boolean trace)
    {
       List<? extends DelegateLoader> delegates = info.getDelegates();
       if (delegates == null || delegates.isEmpty())
       {
          if (trace)
             log.trace(this + " not getting resource " + name + " from imports it has no delegates");
          return null;
       }
 
       URL url = info.getCachedResource(name);
       if (url != null)
       {
          if (trace)
             log.trace(this + " found resource in import cache " + name);
          return url;
       }
       
       if (info.isBlackListedResource(name))
       {
          if (trace)
             log.trace(this + " resource is black listed in imports " + name);
          return null;
       }
 
       if (trace)
          log.trace(this + " trying to get resource " + name + " from imports " + delegates + " for " + info.getClassLoader());
 
       for (DelegateLoader delegate : delegates)
       {
          URL result = delegate.getResource(name);
          if (result != null)
          {
             info.cacheResource(name, result);
             return result;
          }
       }
       info.blackListResource(name);
       return null;
    }
    
    /**
     * Load resources from the imports
     * 
     * @param info the classloader info
     * @param name the resource name
     * @param urls the urls to add to
     * @param trace whether trace is enabled
     * @throws IOException for any error
     */
    void getResourcesFromImports(ClassLoaderInformation info, String name, Set<URL> urls, boolean trace) throws IOException
    {
       List<? extends DelegateLoader> delegates = info.getDelegates();
       if (delegates == null || delegates.isEmpty())
       {
          if (trace)
             log.trace(this + " not getting resource " + name + " from imports it has no delegates");
          return;
       }
       if (trace)
          log.trace(this + " trying to get resources " + name + " from imports " + delegates + " for " + info.getClassLoader());
       for (DelegateLoader delegate : delegates)
          delegate.getResources(name, urls);
    }
    
    /**
     * Load a package from the imports
     * 
     * @param info the classloader information
     * @param name the pacakge name
     * @param trace whether trace is enabled
     * @return the package
     */
    private Package getPackageFromImports(ClassLoaderInformation info, String name, boolean trace)
    {
       List<? extends DelegateLoader> delegates = info.getDelegates();
       if (delegates == null || delegates.isEmpty())
       {
          if (trace)
             log.trace(this + " not getting package " + name + " from imports it has no delegates");
          return null;
       }
 
       if (trace)
          log.trace(this + " trying to get package " + name + " from imports " + delegates + " for " + info.getClassLoader());
 
       for (DelegateLoader delegate : delegates)
       {
          Package result = delegate.getPackage(name);
          if (result != null)
             return result;
       }
       return null;
    }
    
    /**
     * Load packages from the imports
     * 
     * @param info the classloader info
     * @param packages the packages to add to
     * @param trace whether trace is enabled
     */
    void getPackagesFromImports(ClassLoaderInformation info, Set<Package> packages, boolean trace)
    {
       List<? extends DelegateLoader> delegates = info.getDelegates();
       if (delegates == null || delegates.isEmpty())
       {
          if (trace)
             log.trace(this + " not getting all packages from imports it has no delegates");
          return;
       }
       if (trace)
          log.trace(this + " trying to get all pacakges from imports " + delegates + " for " + info.getClassLoader());
       for (DelegateLoader delegate : delegates)
          delegate.getPackages(packages);
    }
 
    /**
     * Invoked before classloading is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param name the class name
     * @return the loader if found or null otherwise
     */
    protected abstract Class<?> loadClassBefore(String name);
    
    /**
     * Invoked after classloading is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param name the class name
     * @return the loader if found or null otherwise
     */
    protected abstract Class<?> loadClassAfter(String name);
 
    /**
     * Invoked before classloading is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param name the class resource name
     * @return the loader if found or null otherwise
     */
    protected abstract Loader findBeforeLoader(String name);
    
    /**
     * Invoked after classloading is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param name the class resource name
     * @return the loader if found or null otherwise
     */
    protected abstract Loader findAfterLoader(String name);
    
    /**
     * Invoked before getResources is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param name the resource name
     * @param urls the urls to add to
     * @throws IOException for any error
     */
    protected abstract void beforeGetResources(String name,  Set<URL> urls) throws IOException;
    
    /**
     * Invoked after getResources is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param name the resource name
     * @param urls the urls to add to
     * @throws IOException for any error
     */
    protected abstract void afterGetResources(String name, Set<URL> urls) throws IOException;
    
    /**
     * Invoked before getResource is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param name the resource name
     * @return the url if found or null otherwise
     */
    protected abstract URL beforeGetResource(String name);
    
    /**
     * Invoked after getResource is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param name the resource name
     * @return the url if found or null otherwise
     */
    protected abstract URL afterGetResource(String name);
    
    /**
     * Invoked before getPackages is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param packages the packages to add to
     */
    protected abstract void beforeGetPackages(Set<Package> packages);
    
    /**
     * Invoked after getPackages is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param packages the packages to add to
     */
    protected abstract void afterGetPackages(Set<Package> packages);
    
    /**
     * Invoked before getPackage is attempted to allow a preload attempt, e.g. from the parent
     * 
     * @param name the package name
     * @return the package if found or null otherwise
     */
    protected abstract Package beforeGetPackage(String name);
    
    /**
     * Invoked after getPackage is attempted to allow a postload attempt, e.g. from the parent
     * 
     * @param name the package name
     * @return the url if found or null otherwise
     */
    protected abstract Package afterGetPackage(String name);
    
    public Class<?> loadClass(String name)
    {
       try
       {
          return loadClass(null, name, true);
       }
       catch (ClassNotFoundException e)
       {
          return null;
       }
    }
    
    /**
     * Load a class from the domain
     * 
     * @param classLoader the classloader
     * @param name the class name
     * @return the class
     * @throws ClassNotFoundException for any error
     */
    Class<?> loadClass(BaseClassLoader classLoader, String name) throws ClassNotFoundException
    {
       return loadClass(classLoader, name, false);
    }
    
    public URL getResource(String name)
    {
       return getResource(null, name, true);
    }
    
    /**
     * Get a resource from the domain
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @return the url
     */
    URL getResource(BaseClassLoader classLoader, String name)
    {
       return getResource(classLoader, name, false);
    }
    
    public void getResources(String name, Set<URL> urls) throws IOException
    {
       getResources(null, name, urls, true);
    }
    
    /**
     * Get a resource from the domain
     * 
     * @param classLoader the classloader
     * @param name the resource name
     * @param urls the urls to add to
     * @throws IOException for any error
     */
    void getResources(BaseClassLoader classLoader, String name, Set<URL> urls) throws IOException
    {
       getResources(classLoader, name, urls, false);
    }
    
    public Package getPackage(String name)
    {
       return getPackage(null, name, true);
    }
    
    /**
     * Get a package from the specified classloader
     * 
     * @param classLoader the classloader
     * @param name the package name
     * @return the package
     */
    Package getPackage(BaseClassLoader classLoader, String name)
    {
       return getPackage(classLoader, name, false);
    }
    
    public void getPackages(Set<Package> packages)
    {
       getPackages(null, packages, true);
    }
    
    /**
     * Get the packages from a specified classloader 
     * 
     * @param classLoader the classloader
     * @param packages the packages
     */
    void getPackages(BaseClassLoader classLoader, Set<Package> packages)
    {
       getPackages(classLoader, packages, false);
    }
 
    /**
     * A long version of toString()
     * 
     * @return the long string
     */
    public String toLongString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append(getClass().getSimpleName());
       builder.append("@").append(Integer.toHexString(System.identityHashCode(this)));
       builder.append("{");
       toLongString(builder);
       builder.append('}');
       return builder.toString();
    }
    
    /**
     * For subclasses to add information for toLongString()
     * 
     * @param builder the builder
     */
    protected void toLongString(StringBuilder builder)
    {
    }
    
    /**
     * Invoked before adding a classloader policy 
     * 
     * @param classLoader the classloader
     * @param policy the classloader policy
     */
    protected void beforeRegisterClassLoader(ClassLoader classLoader, ClassLoaderPolicy policy)
    {
       // nothing
    }
 
    /**
     * Invoked after adding a classloader policy 
     * 
     * @param classLoader the classloader
     * @param policy the classloader policy
     */
    protected void afterRegisterClassLoader(ClassLoader classLoader, ClassLoaderPolicy policy)
    {
       // nothing
    }
    
    /**
     * Invoked before adding a classloader policy 
     * 
     * @param classLoader the classloader
     * @param policy the classloader policy
     */
    protected void beforeUnregisterClassLoader(ClassLoader classLoader, ClassLoaderPolicy policy)
    {
       // nothing
    }
    
    /**
     * Invoked after adding a classloader policy 
     * 
     * @param classLoader the classloader
     * @param policy the classloader policy
     */
    protected void afterUnregisterClassLoader(ClassLoader classLoader, ClassLoaderPolicy policy)
    {
       // nothing
    }
 
    /**
     * Get the parent classloader
     * 
     * @return the parent classloader
     */
    protected ClassLoader getParentClassLoader()
    {
       return getClass().getClassLoader();
    }
 
    /**
     * Register a classloader 
     * 
     * @param classLoader the classloader
     */
    void registerClassLoader(BaseClassLoader classLoader)
    {
       log.debug(this + " registerClassLoader " + classLoader.toString());
 
       if (getClassLoaderSystem() == null)
          throw new IllegalStateException("Domain is not registered with a classloader system: " + toLongString());
       
       ClassLoaderPolicy policy = classLoader.getPolicy();
       BaseDelegateLoader exported = policy.getExported();
       if (exported != null && exported.getPolicy() == null)
          throw new IllegalStateException("The exported delegate " + exported + " is too lazy for " + policy.toLongString());
       
       try
       {
          beforeRegisterClassLoader(classLoader, policy);
       }
       catch (Throwable t)
       {
          log.warn("Error in beforeRegisterClassLoader: " + this + " classLoader=" + classLoader.toLongString(), t);
       }
       
       BaseClassLoaderPolicy basePolicy = classLoader.getPolicy();
       basePolicy.setClassLoaderDomain(this);
 
       synchronized (classLoaders)
       {
          // Create the information
          ClassLoaderInformation info = new ClassLoaderInformation(classLoader, policy, order++);
          classLoaders.add(info);
          infos.put(classLoader, info);
 
          // Index the packages
          String[] packageNames = policy.getPackageNames();
          if (packageNames != null && info.getExported() != null)
          {
             for (String packageName : packageNames)
             {
                List<ClassLoaderInformation> list = classLoadersByPackageName.get(packageName);
                if (list == null)
                {
                   list = new CopyOnWriteArrayList<ClassLoaderInformation>();
                   classLoadersByPackageName.put(packageName, list);
                }
                list.add(info);
                log.trace("Registered " + policy + " as providing package=" + packageName);
             }
          }
          
          flushCaches();
       }
 
       try
       {
          afterRegisterClassLoader(classLoader, classLoader.getPolicy());
       }
       catch (Throwable t)
       {
          log.warn("Error in afterRegisterClassLoader: " + this + " classLoader=" + classLoader.toLongString(), t);
       }
    }
    
    /**
     * Remove a classloader 
     * 
     * @param classLoader the classloader
     */
   synchronized void unregisterClassLoader(BaseClassLoader classLoader)
    {
       log.debug(this + " unregisterClassLoader " + classLoader.toString());
 
       try
       {
          beforeUnregisterClassLoader(classLoader, classLoader.getPolicy());
       }
       catch (Throwable t)
       {
          log.warn("Error in beforeUnegisterClassLoader: " + this + " classLoader=" + classLoader.toLongString(), t);
       }
 
       BaseClassLoaderPolicy policy = classLoader.getPolicy();
       policy.unsetClassLoaderDomain(this);
 
       synchronized (classLoaders)
       {
          // Remove the classloader
          ClassLoaderInformation info = infos.remove(classLoader);
          classLoaders.remove(info);
          
          // Remove the package index
          String[] packageNames = policy.getPackageNames();
          if (packageNames != null && info.getExported() != null)
          {
             for (String packageName : packageNames)
             {
                List<ClassLoaderInformation> list = classLoadersByPackageName.get(packageName);
                if (list != null)
                {
                   list.remove(info);
                   log.trace("Unregistered " + policy + " as providing package=" + packageName);
                   if (list.isEmpty())
                      classLoadersByPackageName.remove(packageName);
                }
             }
          }
 
          flushCaches();
       }
 
       try
       {
          afterUnregisterClassLoader(classLoader, classLoader.getPolicy());
       }
       catch (Throwable t)
       {
          log.warn("Error in afterUnegisterClassLoader: " + this + " classLoader=" + classLoader.toLongString(), t);
       }
    }
 
    /**
     * Get all the classloaders
     * 
     * @return the list of classloaders
     */
    protected List<ClassLoader> getAllClassLoaders()
    {
       List<ClassLoader> result = new ArrayList<ClassLoader>();
       for (ClassLoaderInformation info : classLoaders)
          result.add(info.getClassLoader());
       return result;
    }
 
    /**
     * Get a map of packages to classloader
     * 
     * @return a map of packages to a list of classloaders for that package
     */
    protected Map<String, List<ClassLoader>> getClassLoadersByPackage()
    {
       HashMap<String, List<ClassLoader>> result = new HashMap<String, List<ClassLoader>>();
       for (Entry<String, List<ClassLoaderInformation>> entry : classLoadersByPackageName.entrySet())
       {
          List<ClassLoader> cls = new ArrayList<ClassLoader>();
          for (ClassLoaderInformation info : entry.getValue())
             cls.add(info.getClassLoader());
          result.put(entry.getKey(), cls);
       }
       return result;
    }
 
    protected List<ClassLoader> getClassLoaders(String packageName)
    {
       if (packageName == null)
          throw new IllegalArgumentException("Null package name");
       
       List<ClassLoader> result = new ArrayList<ClassLoader>();
       List<ClassLoaderInformation> infos = classLoadersByPackageName.get(packageName);
       if (infos != null)
       {
          for (ClassLoaderInformation info : infos)
             result.add(info.getClassLoader());
       }
       return result;
    }
 
    /**
     * Cleans the entry with the given name from the blackList
     *
     * @param name the name of the resource to clear from the blackList
     */
    protected void clearBlackList(String name)
    {
       if (globalClassBlackList != null)
       {
          globalClassBlackList.remove(name);
       }
       if (globalResourceBlackList != null)
       {
          globalResourceBlackList.remove(name);
       }
    }
    
 }

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
 
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.classloader.spi.ImportType;
 import org.jboss.classloader.spi.Loader;
 import org.jboss.util.collection.ConcurrentSet;
 
 /**
  * ClassLoaderInformation.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="ales.justin@jboss.org">Ales Justin</a>
  * @version $Revision: 1.1 $
  */
 public class ClassLoaderInformation
 {
    /** The classloader */
    private BaseClassLoader classLoader;
    
    /** The policy */
    private BaseClassLoaderPolicy policy;
 
    /** The order */
    private int order;
    
    /** The exports of the classloader */
    private BaseDelegateLoader exported;
 
    /** The delegates */
    private volatile Map<ImportType, List<DelegateLoader>> delegates;
    
    /** The class cache */
    private volatile Map<String, Loader> classCache;
    
    /** The class black list */
    private volatile Set<String> classBlackList;
    
    /** The resource cache */
    private volatile Map<String, URL> resourceCache;
    
    /** The resource black list */
    private volatile Set<String> resourceBlackList;
 
    /** The # of delegates who cant cache */
    private int cantCache;
 
    /** The # of delegates who cant blacklist */
    private int cantBlacklist;
 
    /**
     * Create a new ClassLoaderInformation.
     * 
     * @param classLoader the classloader
     * @param policy the policy
     * @param order the added order
     * @throws IllegalArgumentException for a null parameter
     */
    public ClassLoaderInformation(BaseClassLoader classLoader, BaseClassLoaderPolicy policy, int order)
    {
       if (classLoader == null)
          throw new IllegalArgumentException("Null classloader");
       if (policy == null)
          throw new IllegalArgumentException("Null policy");
       this.classLoader = classLoader;
       this.policy = policy;
       this.order = order;
       this.exported = policy.getExported();
       
       boolean canCache = policy.isCacheable();
       boolean canBlackList = policy.isBlackListable();
 
       List<? extends DelegateLoader> delegates = policy.getDelegates();
       if (delegates != null && delegates.isEmpty() == false)
       {
          Map<ImportType, List<DelegateLoader>> temp = new HashMap<ImportType, List<DelegateLoader>>();
          // prepare ALL
          List<DelegateLoader> all = new CopyOnWriteArrayList<DelegateLoader>();
          temp.put(ImportType.ALL, all);
 
          for (DelegateLoader delegate : delegates)
          {
             if (delegate == null)
                throw new IllegalStateException(policy + " null delegate in " + delegates);
 
             ImportType importType = delegate.getImportType();
             List<DelegateLoader> loaders = temp.get(importType);
             if (loaders == null)
             {
                loaders = new CopyOnWriteArrayList<DelegateLoader>();
                temp.put(importType, loaders);
             }
             loaders.add(delegate); // add to specific type
             all.add(delegate); // add to all
 
             BaseDelegateLoader baseDelegate = delegate;
             BaseClassLoaderPolicy delegatePolicy = baseDelegate.getPolicy();
             if (delegatePolicy == null || delegatePolicy.isCacheable() == false)
             {
                canCache = false;
                cantCache++;
             }
             if (delegatePolicy == null || delegatePolicy.isBlackListable() == false)
             {
                canBlackList = false;
                cantBlacklist++;
             }
          }
 
          this.delegates = Collections.synchronizedMap(temp);
       }
 
       if (canCache)
       {
          restoreCache();
       }
       
       if (canBlackList)
       {
          restoreBlackList();
       }
    }
 
    private void restoreCache()
    {
       classCache = new ConcurrentHashMap<String, Loader>();
       resourceCache = new ConcurrentHashMap<String, URL>();
    }
 
    private void destroyCache()
    {
       classCache = null;
       resourceCache = null;
    }
 
    private void restoreBlackList()
    {
       classBlackList = new ConcurrentSet<String>();
       resourceBlackList = new ConcurrentSet<String>();
    }
 
    private void destroyBlackList()
    {
       classBlackList = null;
       resourceBlackList = null;
    }
 
    /**
     * Flush the caches
     */
    public void flushCaches()
    {
       if (classCache != null)
          classCache.clear();
       if (classBlackList != null)
          classBlackList.clear();
       if (resourceCache != null)
          resourceCache.clear();
       if (resourceBlackList != null)
          resourceBlackList.clear();
    }
    
    /**
     * Get the classLoader.
     * 
     * @return the classLoader.
     */
    public BaseClassLoader getClassLoader()
    {
       return classLoader;
    }
 
    /**
     * Get the policy.
     * 
     * @return the policy.
     */
    public BaseClassLoaderPolicy getPolicy()
    {
       return policy;
    }
 
    /**
     * Get the order.
     * 
     * @return the order.
     */
    public int getOrder()
    {
       return order;
    }
 
    /**
     * Get the exported.
     * 
     * @return the exported.
     */
    public BaseDelegateLoader getExported()
    {
       return exported;
    }
 
    /**
     * Get the delegates.
     * 
     * @return the delegates.
     * @deprecated use same method with import type parameter
     */
    @Deprecated
    public List<? extends DelegateLoader> getDelegates()
    {
       return getDelegates(ImportType.BEFORE);
    }
    
    /**
     * Get the delegates.
     *
     * @param type the import type
     * @return the delegates.
     */
    public List<? extends DelegateLoader> getDelegates(ImportType type)
    {
       if (delegates == null)
          return Collections.emptyList();
 
       return delegates.get(type);
    }
 
    /**
     * Add a delegate loader.
     *
     * @param loader the delegate loader
     */
    void addDelegate(DelegateLoader loader)
    {
       if (loader == null)
          throw new IllegalArgumentException("Null delegate");
 
       if (delegates == null)
          delegates = Collections.synchronizedMap(new HashMap<ImportType, List<DelegateLoader>>());
 
       BaseDelegateLoader baseDelegate = loader;
       BaseClassLoaderPolicy policy = baseDelegate.getPolicy();
       boolean canCache = (policy != null && policy.isCacheable());
       boolean canBlackList = (policy != null && policy.isBlackListable());
 
       ImportType type = loader.getImportType();
       //noinspection SynchronizeOnNonFinalField
       synchronized (delegates)
       {
          List<DelegateLoader> list = delegates.get(type);
          if (list == null)
          {
             list = new CopyOnWriteArrayList<DelegateLoader>();
             delegates.put(type, list);
          }
          list.add(0, loader); // add at the begining
          // all
          List<DelegateLoader> all = delegates.get(ImportType.ALL);
          if (all == null)
          {
             all = new CopyOnWriteArrayList<DelegateLoader>();
             delegates.put(ImportType.ALL, all);
          }
          all.add(loader);
 
          if (canCache == false)
          {
             // we can cache atm, but the new one can't
             if (cantCache == 0)
                destroyCache();
 
             cantCache++;
          }
 
          if (canBlackList == false)
          {
             // we can blacklist atm, but the new one can't
             if (cantBlacklist == 0)
               destroyBlackList();
 
             cantBlacklist++;
          }
       }
    }
 
    /**
     * Remove a delegate loader.
     *
     * @param loader the delegate loader
     */
    void removeDelegate(DelegateLoader loader)
    {
       if (loader == null)
          throw new IllegalArgumentException("Null delegate");
 
       if (delegates == null)
          return;
 
       BaseDelegateLoader baseDelegate = loader;
       BaseClassLoaderPolicy policy = baseDelegate.getPolicy();
       boolean canCache = (policy != null && policy.isCacheable());
       boolean canBlackList = (policy != null && policy.isBlackListable());
 
       ImportType type = loader.getImportType();
       //noinspection SynchronizeOnNonFinalField
       synchronized (delegates)
       {
          List<DelegateLoader> list = delegates.get(type);
          if (list != null)
          {
             if (list.remove(loader) && list.isEmpty())
                delegates.remove(type);
          }
 
          boolean member = false;
          // all
          List<DelegateLoader> all = delegates.get(ImportType.ALL);
          if (all != null)
          {
             member = all.remove(loader);
             if (member && all.isEmpty())
                delegates.remove(ImportType.ALL);
          }
 
          // make sure we only handle our members
          if (member)
          {
             if (canCache == false)
             {
                cantCache--;
 
                // we can again cache
                if (cantCache == 0)
                   restoreCache();
             }
 
             if (canBlackList == false)
             {
                cantBlacklist--;
 
                // we can again blacklist
                if (cantBlacklist == 0)
                   restoreBlackList();
             }
          }
       }
    }
 
    /**
     * Get the cached loader for a class 
     * 
     * @param name the class name
     * @return any cached loader
     */
    public Loader getCachedLoader(String name)
    {
       Map<String, Loader> classCache = this.classCache;
       if (classCache != null)
          return classCache.get(name);
       return null;
    }
    
    /**
     * Cache a loader for a class
     * 
     * @param name the class name
     * @param loader the cached loader
     */
    public void cacheLoader(String name, Loader loader)
    {
       Map<String, Loader> classCache = this.classCache;
       if (classCache != null)
          classCache.put(name, loader);
    }
    
    /**
     * Check whether this is a black listed class
     * 
     * @param name the class name
     * @return true when black listed
     */
    public boolean isBlackListedClass(String name)
    {
       Set<String> classBlackList = this.classBlackList;
       return classBlackList != null && classBlackList.contains(name);
    }
    
    /**
     * Blacklist a class
     * 
     * @param name the class name to black list
     */
    public void blackListClass(String name)
    {
       Set<String> classBlackList = this.classBlackList;
       if (classBlackList != null)
          classBlackList.add(name);
    }
    
    /**
     * Get the cached url for a resource 
     * 
     * @param name the resource name
     * @return any cached url
     */
    public URL getCachedResource(String name)
    {
       Map<String, URL> resourceCache = this.resourceCache;
       if (resourceCache != null)
          return resourceCache.get(name);
       return null;
    }
    
    /**
     * Cache a url for a resource
     * 
     * @param name the resource name
     * @param url the cached url
     */
    public void cacheResource(String name, URL url)
    {
       Map<String, URL> resourceCache = this.resourceCache;
       if (resourceCache != null)
          resourceCache.put(name, url);
    }
    
    /**
     * Check whether this is a black listed resource
     * 
     * @param name the resource name
     * @return true when black listed
     */
    public boolean isBlackListedResource(String name)
    {
       Set<String> resourceBlackList = this.resourceBlackList;
       return resourceBlackList != null && resourceBlackList.contains(name);
    }
    
    /**
     * Blacklist a resource
     * 
     * @param name the resource name to black list
     */
    public void blackListResource(String name)
    {
       Set<String> resourceBlackList = this.resourceBlackList;
       if (resourceBlackList != null)
          resourceBlackList.add(name);
    }
    
    /**
     * Cleans the entry with the given name from the blackList
     *
     * @param name the name of the resource to clear from the blackList
     */
    public void clearBlackList(String name)
    {
       if (classBlackList != null)
          classBlackList.remove(name);
       if (resourceBlackList != null)
          resourceBlackList.remove(name);
    }
    
    @Override
    public String toString()
    {
       return policy.toString();
    }
 }

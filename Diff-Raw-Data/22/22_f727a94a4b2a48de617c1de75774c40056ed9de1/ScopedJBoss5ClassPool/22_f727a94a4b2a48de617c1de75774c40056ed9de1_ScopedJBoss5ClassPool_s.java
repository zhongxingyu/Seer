 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.aop.asintegration.jboss5;
 
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.NotFoundException;
 import javassist.scopedpool.ScopedClassPoolRepository;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.classpool.AOPClassPool;
 import org.jboss.aop.classpool.AOPClassPoolRepository;
 import org.jboss.classloader.spi.ClassLoaderDomain;
 import org.jboss.classloader.spi.Loader;
 import org.jboss.classloader.spi.base.BaseClassLoader;
 import org.jboss.logging.Logger;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 public class ScopedJBoss5ClassPool extends JBoss5ClassPool
 {
    Logger log = Logger.getLogger(ScopedJBoss5ClassPool.class);
    
    ThreadLocal<ClassPool> lastPool = new ThreadLocal<ClassPool>();
    WeakReference<ClassLoaderDomain> domainRef;
 
    public ScopedJBoss5ClassPool(ClassLoader cl, ClassPool src, ScopedClassPoolRepository repository, 
          URL tmpURL, boolean parentFirst, ClassLoaderDomain domain)
    {
       super(cl, src, repository, tmpURL);
       super.childFirstLookup = !parentFirst;
       this.domainRef = new WeakReference<ClassLoaderDomain>(domain);
    }
 
    private URL getResourceUrlForClass(String resourcename)
    {
       ClassLoaderDomain domain = domainRef.get();
       return domain.getResource(resourcename);
    }
 
    private boolean isMine(URL myURL, String resourceName)
    {
       if (myURL == null)
       {
          return false;
       }
       
       ClassLoaderDomain domain = domainRef.get();
       Loader parent = domain.getParent();
       URL parentURL = parent.getResource(resourceName);
       
       if (parentURL == null)
       {
          return true;
       }
       
       if (!myURL.equals(parentURL))
       {
          return true;
       }
       
       return false;
    }
    
    public CtClass getCached(String classname)
    {
       if (classname == null)
       {
          return null;
       }
       if (isUnloadedClassLoader())
       {
          return null;
       }
 
       if (generatedClasses.get(classname) != null)
       {
          //It is a new class, and this callback is probably coming from the frozen check when creating a new nested class
          return super.getCached(classname);
       }
       
       //Is this from the scoped classloader itself of from the parent?
       String resourcename = getResourceName(classname);
       URL url = getResourceUrlForClass(resourcename);
       
       if (isMine(url, resourcename))
       {
          if (super.childFirstLookup)
          {
             //Parent delegation is false, attempt to get this class out of ourselves
             CtClass clazz = super.getCachedLocally(classname);
             if (clazz == null)
             {
                clazz = createCtClass(classname, false);
                if (clazz != null)
                {
                   lockInCache(clazz);
                }
             }
             if (clazz != null)
             {
                return clazz;
             }
          }
          return super.getCached(classname);
       }
       else if (url == null)
       {
          return super.getCached(classname);
       }
       
 
       try
       {
          ClassPool pool = getCorrectPoolForResource(classname, resourcename, url);
          if (pool != lastPool.get())
          {
             lastPool.set(pool);
             return pool.get(classname);
          }
       }
       catch (NotFoundException e)
       {
       }
       catch(StackOverflowError e)
       {
          throw e;
       }
       finally
       {
          lastPool.set(null);
       }
 
       return null;
    }
    
    protected boolean includeInGlobalSearch()
    {
       return false;
    } 
    
    private ClassPool getCorrectPoolForResource(String classname, String resourceName, URL url)
    {
       boolean trace = log.isTraceEnabled();
       synchronized(AspectManager.getRegisteredCLs())
       {
          //JBoss 5 has an extra NoAnnotationURLCLassLoader that is not on the default path, make sure that that is checked at the end
          //FIXME This needs revisiting/removing once the 
          ArrayList<ClassPool> noAnnotationURLClassLoaderPools = null;
          
 //         //EXTRA DEBUG STUFF
 //         if (classname.equals("org.jboss.test.aop.scopedextender.Base_A1"))
 //         {
 //            System.out.println("********** Looking for proper pool for Base_A1 - this pool " + this);
 //            boolean found = false;
 //            for(Iterator it = AspectManager.getRegisteredCLs().values().iterator() ; it.hasNext() ; )
 //            {
 //               AOPClassPool candidate = (AOPClassPool)it.next();
 //               if (candidate.isUnloadedClassLoader())
 //               {
 //                  System.out.println("Found something unloaded " + candidate);
 //                  continue;
 //               }
 //
 //               if (candidate.getClassLoader() instanceof BaseClassLoader)
 //               {
 //                  BaseClassLoader bcl = (BaseClassLoader)candidate.getClassLoader();
 //                  URL foundUrl = bcl.getResourceLocally(resourceName);
 //                  if (foundUrl != null)
 //                  {
 //                     System.out.println("=============> Found in " + bcl);
 //                     if (url.equals(foundUrl))
 //                     {
 //                        if (!found)
 //                        {
 //                           System.out.println("^^^ The one returned ^^^");
 //                           found = true;
 //                        }
 //                     }
 //                  }
 //               }
 //            }
 //         }         
          
          for(Iterator it = AspectManager.getRegisteredCLs().values().iterator() ; it.hasNext() ; )
          {
             AOPClassPool candidate = (AOPClassPool)it.next();
             if (candidate.isUnloadedClassLoader())
             {
                AspectManager.instance().unregisterClassLoader(candidate.getClassLoader());
                continue;
             }
             
             if (candidate.getClassLoader() instanceof BaseClassLoader)
             {
                //Sometimes the ClassLoader is a proxy for MBeanProxyExt?!
                BaseClassLoader bcl = (BaseClassLoader)candidate.getClassLoader();
                URL foundUrl = bcl.getResourceLocally(resourceName);
                if (trace)
                {
                   log.trace("Candidate classloader " + bcl + " has local resource " + foundUrl);
                }
                if (foundUrl != null)
                {
                   if (url.equals(foundUrl))
                   {
                      return candidate;
                   }
                }
             }
             //FIXME Remove once we have the JBoss 5 version of pool
             else if (isInstanceOfNoAnnotationURLClassLoader(candidate.getClassLoader()))
             {
                if (noAnnotationURLClassLoaderPools == null)
                {
                   noAnnotationURLClassLoaderPools = new ArrayList<ClassPool>(); 
                }
                noAnnotationURLClassLoaderPools.add(candidate);
             }
          }
          
          //FIXME Remove once we have the JBoss 5 version of pool
          if (noAnnotationURLClassLoaderPools != null)
          {
             for (ClassPool pool : noAnnotationURLClassLoaderPools)
             {
                try
                {
                   pool.get(classname);
                   return pool;
                }
                catch(NotFoundException ignoreTryNext)
                {
                }
             }
          }
       }
 
       return AOPClassPool.createAOPClassPool(ClassPool.getDefault(), AOPClassPoolRepository.getInstance());
    }
    
    /**
     * NoAnnotationURLCLassLoader lives in different packages in JBoss 4 and 5
     */
    private boolean isInstanceOfNoAnnotationURLClassLoader(ClassLoader loader)
    {
       Class parent = loader.getClass();
       while (parent != null)
       {
          if ("NoAnnotationURLClassLoader".equals(parent.getSimpleName()))
          {
             return true;
          }
          parent = parent.getSuperclass();
       }
       return false;
    }
    
 }

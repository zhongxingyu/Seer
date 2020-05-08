 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 12th November 2009
  *
  * Changelog:
  * - 12/11/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.primitive;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import au.edu.uts.eng.remotelabs.rigclient.main.RigClientDefines;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Cache for <code>IPrimitiveController</code> instances.
  */
 public class PrimitiveCache
 {
     /** The list of loaded primitive controller instances. */
     private final Map<String, IPrimitiveController> cache;
 
     /** Packages where the the primitive controller may reside. */
     private final List<String> packages;
 
     /** Logger. */
     private final ILogger logger;
 
     /**
      * Constructor.
      */
     public PrimitiveCache()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.logger.debug("Created a primitive control cache.");
         this.cache = new HashMap<String, IPrimitiveController>();
 
         this.packages = new ArrayList<String>();
         final String confPrefixes = ConfigFactory.getInstance().getProperty("Package_Prefixes");
         if (confPrefixes == null || confPrefixes.equals(""))
         {
             this.logger.info("No primitive control package prefixes have been loaded from configuration " +
             "(property Package_Prefixes).");
         }
         else
         {
             final String[] prefixes = confPrefixes.split(";");
             for (String prefix : prefixes)
             {
                 this.logger.info("Loaded primitive controller prefix: " + prefix + ".");
                 this.packages.add(prefix);
             }
         }
     }
 
     /**
      * Gets an instance a <code>IPrimitiveController</code> based on the 
      * provided <code>className</code> parameter. On first request, the 
      * class is resolved, an instance is created and the 
      * <code>IPrimitiveController.initController</code> method is called
      * to initialise the controller. For resolution to succeed:
      * <ol>
      *  <li>The provide <code>className</code> is a fully qualified 
      *  class name or be a class resident in one of the configured 
      *  packages.</li>
      *  <li>The class must implement the <code>IPrimitiveController</code>
      *  interface<li>
      *  <li>The <code>initController</code> method must succeed, that is 
      *  return <code>true</code>.
      * </ol>
      * 
      * @param className class to get an instance of
      * @return instance or null if the name cannot be resolved
      */
     public IPrimitiveController getInstance(final String className)
     {
         /* First try and find a previously loaded instance. */
         if (this.cache.containsKey(className))
         {
             this.logger.debug("Returning cached primitive controller " + className + ".");
             return this.cache.get(className);
         }
 
         /* Try all the configured packages. */
         String controllerName;
         for (String packagePrefix : this.packages)
         {
             controllerName = RigClientDefines.prependPackage(packagePrefix, className);
             if (this.cache.containsKey(RigClientDefines.prependPackage(packagePrefix, className)))
             {
                 this.logger.debug("Returning cached primitive controller " + packagePrefix + className + ".");
                 return this.cache.get(controllerName);
             }
         }
         
         synchronized (this)
         {
             /* Try again in case another thread has loaded the class. */
             if (this.cache.containsKey(className))
             {
                 this.logger.debug("Returning cached primitive controller " + className + ".");
                 return this.cache.get(className);
             }
             for (String packagePrefix : this.packages)
             {
                 controllerName = RigClientDefines.prependPackage(packagePrefix, className);
                 if (this.cache.containsKey(controllerName))
                 {
                     this.logger.debug("Returning cached primitive controller " + packagePrefix + className + ".");
                     return this.cache.get(controllerName);
                 }
             }
              
             /* Create the controller class, and check if it is the correct type. */
             IPrimitiveController controller = null;
             Class<?> controllerClass = null;
             controllerName = null;
 
             /* This is pretty ugly... */
             try
             {
                 controllerName = className;
                 controllerClass = Class.forName(controllerName);
             }
             catch (ClassNotFoundException e)
             {
                 this.logger.debug("Class " + className + " not found.");
                 for (String prefix : this.packages)
                 {
                     try
                     {
                         controllerName = RigClientDefines.prependPackage(prefix, className);
                         controllerClass = Class.forName(controllerName);
                         break;
                     }
                     catch (ClassNotFoundException ex)
                     {
                         this.logger.debug("Class " + controllerName + " not found.");
                     }
                    catch (Throwable t)
                    {
                        this.logger.error("Failed class initalisation, exception: " + t.getClass().getSimpleName() + 
                                ", reason: " + t.getMessage());
                    }
                 }
             }
            catch (Throwable t)
            {
                this.logger.error("Failed class initalisation, exception: " + t.getClass().getSimpleName() + 
                        ", reason: " + t.getMessage());
            }
 
             /* Check if a class was found. */
             if (controllerClass == null)
             {
                 this.logger.warn("Unable to load primitive controller " + className + ".");
                 return null;
             }
 
             try
             {
                 final Object obj = controllerClass.newInstance();
                 if (!(obj instanceof IPrimitiveController))
                 {
                     this.logger.warn("Instantiated class " + controllerClass.getName() + " not an instance of "
                             + " IPrimitiveController interface. Failing primitive controller resolution.");
                     return null;
                 }
 
                 controller = (IPrimitiveController)obj;
                 if (!controller.initController())
                 {
                     this.logger.warn("Primitive controller " + controllerName + " failed initialisation (initController"
                             + " failed).");
                     return null;
                 }
             }
             catch (InstantiationException e)
             {
                 this.logger.warn("Failed to create a primitive controller instance of " + controllerClass.getName() 
                         + " with error " + e.getMessage());
                 return null;
             }
             catch (IllegalAccessException e)
             {
                 this.logger.warn("Failed to create a primitive controller instance of " + controllerClass.getName() +
                         " due to illegal access exception with message " + e.getMessage() + ".");
                 return null;
             }
 
             this.logger.debug("Caching primitive controller " + controllerName + ".");
             this.cache.put(controllerName, controller);
             return controller;
         }
     }
     
     /**
      * Removes a cached instance from the cache.
      * 
      * @param className name of the class to remove
      */
     public void removeCachedInstance(String className)
     {
         this.logger.debug("Removing " + className + " instance from the primitive control cache.");
         String controllerName;
         
         synchronized (this)
         {
             /* Try again in case another thread has loaded the class. */
             if (this.cache.containsKey(className))
             {
                 this.cache.get(className).cleanup();
                 this.cache.remove(className);                
             }
             else
             {
                 for (String packagePrefix : this.packages)
                 {
                     controllerName = RigClientDefines.prependPackage(packagePrefix, className);
                     if (this.cache.containsKey(controllerName))
                     {
                         this.cache.get(controllerName).cleanup();
                         this.cache.remove(controllerName);
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * Expunges the primitive controller cache. 
      */
     public void expungeCache()
     {
         this.logger.debug("Expunging the primitive controller cache.");
         synchronized (this)
         {
             for (Entry<String, IPrimitiveController> e : this.cache.entrySet())
             {
                 this.logger.debug("Cleaning up controller " + e.getKey() + ".");
                 e.getValue().cleanup();
             }
             this.cache.clear();
         }
     }
 }

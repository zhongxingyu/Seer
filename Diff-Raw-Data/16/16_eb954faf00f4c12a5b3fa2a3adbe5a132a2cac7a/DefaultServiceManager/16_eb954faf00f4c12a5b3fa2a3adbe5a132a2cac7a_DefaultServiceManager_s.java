 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on May 14, 2008
  */
 package com.soartech.simjr.services;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.soartech.simjr.NullProgressMonitor;
 import com.soartech.simjr.SimulationException;
 import com.soartech.simjr.adaptables.Adaptables;
 
 /**
  * Default implementation of ServiceManager, mostly for testing purposes.
  * 
  * @author ray
  */
 public class DefaultServiceManager implements ServiceManager
 {
     private static final Logger logger = Logger.getLogger(DefaultServiceManager.class);
     
     private Object serviceLock = new String("Service Lock");
     private List<SimulationService> services = new ArrayList<SimulationService>();
     private Map<Class<?>, SimulationService> serviceCache = new HashMap<Class<?>, SimulationService>();
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.ServiceManager#addService(com.soartech.simjr.SimulationService)
      */
     public void addService(SimulationService service)
     {
         synchronized(serviceLock)
         {
             services.add(service);
         }
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.ServiceManager#findService(java.lang.Class)
      */
     @SuppressWarnings("unchecked")
     public <T extends SimulationService> T findService(Class<T> klass)
     {
         synchronized (serviceLock)
         {
             T r = (T) serviceCache.get(klass);
             if(r != null)
             {
                 return r;
             }
             
             r = Adaptables.findAdapter(services, klass);
             addServiceToCache(klass, r);
             
             if(r == null && isConstructOnDemandService(klass))
             {
                 r = constructDefaultService(klass);
                 addServiceToCache(klass, r);
                 startAndAddService(r);
             }
             return r;
         }
     }
 
     private <T extends SimulationService> boolean isConstructOnDemandService(Class<T> klass)
     {
         return klass.isAnnotationPresent(ConstructOnDemand.class);
     }
     
     
     private <T extends SimulationService> void addServiceToCache(Class<T> klass, T r)
     {
         if(r != null)
         {
             serviceCache.put(klass, r);
         }
     }
 
     private <T extends SimulationService> void startAndAddService(T r)
     {
         if(r != null)
         {
             try
             {
                 r.start(new NullProgressMonitor());
             }
             catch (SimulationException e)
             {
                 logger.error("Error while starting default constructed service: " + e.getMessage(), e);
                 throw new RuntimeException(e.getMessage(), e);
             }
             addService(r);
         }
     }
     
     public void shutdownServices() throws SimulationException
     {
         synchronized(serviceLock)
         {
             List<SimulationService> reversedServices = new ArrayList<SimulationService>(services);
             Collections.reverse(reversedServices);
             
             for(SimulationService service : reversedServices)
             {
                 service.shutdown();
             }
         }
     }
 
     private <T extends SimulationService> T constructDefaultService(Class<T> klass)
     {
         logger.info("Attempting to construct default instance of '" + klass.getCanonicalName() + "'");
         if(klass.isInterface() || klass.isArray() || klass.isPrimitive())
         {
             logger.info("'" + klass.getCanonicalName() + "' is not constructable. Ignoring.");
             return null;
         }
         
         try
         {
             try
             {
                 final Constructor<T> oneArgConstructor = klass.getConstructor(ServiceManager.class);
                 return oneArgConstructor.newInstance(this);
             }
             catch (NoSuchMethodException e)
             {
                 try
                 {
                     final Constructor<T> defaultConstructor = klass.getConstructor();
                     return defaultConstructor.newInstance();
                 }
                 catch (NoSuchMethodException e1)
                 {
                     logger.info("'" + klass.getCanonicalName() + "' does not have a default, or one-arg constructor. Ignoring.");
                     return null;
                 }
             }
         }
         catch (Exception e)
         {
             logger.error("Failed to construct default instance of '" + klass.getCanonicalName() + "': " + e.getMessage(), e);
             return null;
         }
     }
         
 }

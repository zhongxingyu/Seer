 package sorcer.service.spacer;
 /**
  *
  * Copyright 2013 Rafał Krupiński.
  * Copyright 2013 Sorcersoft.com S.A.
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
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.core.Provider;
 import sorcer.service.Accessor;
 import sorcer.service.Spacer;
 import sorcer.util.AccessorException;
 import sorcer.util.Providers;
 import sorcer.util.ServiceAccessor;
 
 /**
  * @author Rafał Krupiński
  */
 public class SpacerAccessor extends ServiceAccessor {
     private static final Logger log = LoggerFactory.getLogger(SpacerAccessor.class);
     private static SpacerAccessor instance = new SpacerAccessor();
 
     /**
      * Returns any SORCER Spacer service provider.
      *
      * @return a SORCER Spacer provider
      * @throws sorcer.util.AccessorException
      */
     public static Spacer getSpacer() throws AccessorException {
         return getSpacer(null);
     }
 
     /**
      * Returns a SORCER Spacer service provider using Jini lookup and discovery.
      *
      * @param name the name of a spacer service provider
      * @return a Spacer proxy
      */
     public static Spacer getSpacer(String name) {
         return instance.doGetSpacer(name);
     }
 
     public Spacer doGetSpacer(String name) {
        String spacerName = (name == null) ? providerNameUtil.getName(Spacer.class)
                 : name;
         Spacer spacer = (Spacer) cache.get(Spacer.class.getName());
         try {
             if (Providers.isAlive((Provider) spacer)) {
                 log.info(">>>returned cached Spacer ("
                         + ((Provider) spacer).getProviderID() + ") by "
                         + Accessor.getAccessorType());
             } else {
                 spacer = Accessor.getService(spacerName, Spacer.class);
                 cache.put(Spacer.class.getName(), spacer);
             }
             return spacer;
         } catch (Exception e) {
             log.error("getSpacer", e);
             return null;
         }
     }
 }

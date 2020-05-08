 /**
  *
  * Copyright 2010, 2011, 2012 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package ru.altruix.commons.impl.di;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.inject.AbstractModule;
 
 /**
  * @author DP118M
  * 
  */
 public abstract class AltruixInjectorModule extends AbstractModule {
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
    protected void configure() {
         final Map<Class, Object> interfacesByInstances =
                 new HashMap<Class, Object>();
 
         bindClassesToInstances(interfacesByInstances);
 
         for (final Class clazz : interfacesByInstances.keySet()) {
             final Object instance = interfacesByInstances.get(clazz);
 
             bind(clazz).toInstance(instance);
         }
 
     }
 
     @SuppressWarnings("rawtypes")
     protected abstract void
             bindClassesToInstances(
                     final Map<Class, Object> aInterfacesByInstances);
 
 }

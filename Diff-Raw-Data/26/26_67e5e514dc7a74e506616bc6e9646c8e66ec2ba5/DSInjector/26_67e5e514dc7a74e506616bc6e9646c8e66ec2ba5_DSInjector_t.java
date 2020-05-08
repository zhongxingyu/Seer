 /*
  *  Copyright 2009-2010 Mathieu ANCELIN
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.api;
 
 import cx.ath.mancel01.dependencyshot.api.event.Event;
 import cx.ath.mancel01.dependencyshot.api.event.EventListener;
 
 /**
  * Injector interface.
  * 
  * @author Mathieu ANCELIN
  */
 public interface DSInjector {
     /**
      * Get a instance of c.
      *
      * @param <T> type of the instance.
      * @param c the class to instanciate.
      * @return an instance of c.
      */
     <T> T getInstance(Class<T> c);
     /**
      * Inject the static members of c.
      * 
      * @param c the class to inject.
      */
     void injectStatics(Class<?> c);
 
     /**
      * Injection on an instance.
      * WARNING : Constructor injection doesn't work
      * with this kind of injection.
      *
      * @param <T> type
      * @param instance of T
      * @return injected instance
      */
     <T> T injectInstance(T instance);
 
     /**
      * @return the actual stage of the injector.
      */
     Stage getStage();
 
     /**
      * Enable or not the circular dependencies in the injector.
      *
      * @param allowCircularDependencies new value for circular dependencies.
      */
     void allowCircularDependencies(boolean allowCircularDependencies);
 
     /**
      * @return if circular dependencies are allowed by the injector
      */
     boolean areCircularDependenciesAllowed();
 
     /**
      * Register a shutdown hook to shutdown container when JVM stop.
      */
     void registerShutdownHook();
 
     void registerEventListener(EventListener<? extends Event> listener);
 
    void allowLazyStaticInjection(boolean allowLazyStaticInjection);

    boolean isLazyStaticInjectionAllowed();

 }

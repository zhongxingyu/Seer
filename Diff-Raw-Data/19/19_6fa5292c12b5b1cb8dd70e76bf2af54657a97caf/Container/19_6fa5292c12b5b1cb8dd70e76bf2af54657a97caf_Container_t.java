 /*
  *  Copyright 2011 mathieuancelin.
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
 
 package cx.ath.mancel01.dependencyshot;
 
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.deploy.BeanArchive;
 import cx.ath.mancel01.dependencyshot.deploy.CDIBinder;
 import cx.ath.mancel01.dependencyshot.deploy.ResourceLoader;
 import cx.ath.mancel01.dependencyshot.graph.Binding;
 import cx.ath.mancel01.dependencyshot.graph.BindingBuilder;
 import cx.ath.mancel01.dependencyshot.injection.InjectorBuilder;
 import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
 import cx.ath.mancel01.dependencyshot.spi.PluginsLoader;
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.decorator.Decorator;
 import javax.inject.Named;
 
 /**
  *
  * @author mathieuancelin
  */
 public class Container {
 
     private final BeanArchive archive;
 
     private final InjectorImpl injector;
 
     private Container(BeanArchive archive) {
         this.archive = archive;
         this.injector = createInjector();
     }
 
     public static Container init(BeanArchive archive) {
         return new Container(archive);
     }
 
     public void shutdown() {
         injector.triggerLifecycleDestroyCallbacks();
     }
 
     public <T> T getInstance(Class<T> clazz) {
         return injector.getInstance(clazz);
     }
 
    public Object getInstance(String elName) {
         // TODO ; getInstance on @Named
        return injector.getInstance(Object.class);
     }
 
     public <T> T injectInstance(T object) {
         return injector.injectInstance(object);
     }
     
     public void injectStatics(Class<?> clazz) {
         injector.injectStatics(clazz);
     }
     
     public <T> void fire(T event) {
         injector.fire(event);
     }
 
     private InjectorImpl createInjector() {
         PluginsLoader pluginsLoader = new PluginsLoader();
         InjectorImpl newInjector = InjectorBuilder.makeInjector(pluginsLoader, Stage.NONE);
         CDIBinder binder = new CDIBinder();
         ResourceLoader loader = archive.getResourceLoader();
         Collection<String> resources = archive.getResources();
         Collection<String> classesNames = archive.getBeanClasses();
         Collection<Class<?>> classes = new ArrayList<Class<?>>();
         for (String resource : resources) {
             if (resource.endsWith("beans.xml")) {
                 // TODO : parse it and extract informations about special classes
                // TODO : register interceptors
             }
         }
         for (String className : classesNames) {
             Class<?> clazz = loader.loadClass(className);
             classes.add(clazz);
         }
         for (Class<?> clazz : classes) {
             // TODO : register producers beans (methods and fields)
            
         }
         for (Class<?> clazz : classes) {
             registerComponent(clazz, newInjector, binder);
         }
         newInjector.allowCircularDependencies(true);
         newInjector.registerShutdownHook();
         return newInjector;
     }
 
     private void registerComponent(Class<?> clazz, InjectorImpl newInjector, CDIBinder binder) {
         if (!clazz.isInterface()) {
             List<Binding> bindings = new ArrayList<Binding>();
             Collection<Annotation> qualifiers = getQualifiers(clazz);
             if (qualifiers.isEmpty()) {
                 for (Annotation annotation : qualifiers) {
                     if (annotation.annotationType().equals(Named.class)) {
                         bindings.addAll(getNamedBindingsFor(clazz, annotation));
                     } else {
                         bindings.addAll(getQualifiedBindingsFor(clazz, annotation));
                     }
                 }
             } else {
                 bindings.addAll(getBindingsFor(clazz));
             }
             
             // register listener beans
             newInjector.registerEventListener(clazz);
             
             // TODO : register as intercepted
             
             // register decorators
             if (clazz.isAnnotationPresent(Decorator.class)) {
                 binder.registerDecorator(clazz);
             }
             // -----------------------------------------------
             // TODO : with SPI
             // TODO : register scopes -- with SPI
         }
     }
 
     private Collection<Binding> getBindingsFor(Class<?> clazz) {
         // TODO : register for interfaces and supertypes
         // TODO : check for producers
        return null;
     }
 
     private Collection<Binding> getQualifiedBindingsFor(Class<?> clazz, Annotation qualifier) {
         // TODO : register for interfaces and supertypes
         // TODO : check for producers
        return null;
     }
 
     private Collection<Binding> getNamedBindingsFor(Class<?> clazz, Annotation named) {
         // TODO : register for interfaces and supertypes
         // TODO : check for producers
        return null;
     }
 
     private Collection<Annotation> getQualifiers(Class<?> clazz) {
         // TODO : check for stereotypes
        return null;
     }
 }

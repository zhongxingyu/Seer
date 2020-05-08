 /*
  *  Copyright 2009-2010 Mathieu ANCELIN.
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
 package cx.ath.mancel01.dependencyshot.configurator;
 
 import cx.ath.mancel01.dependencyshot.api.Stage;
 import cx.ath.mancel01.dependencyshot.configurator.annotations.OnStage;
 import cx.ath.mancel01.dependencyshot.configurator.annotations.ProvidedBy;
 import cx.ath.mancel01.dependencyshot.graph.Binder;
 import cx.ath.mancel01.dependencyshot.graph.Binding;
 import cx.ath.mancel01.dependencyshot.injection.InjectorBuilder;
 import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
 import cx.ath.mancel01.dependencyshot.spi.ConfigurationHandler;
 import java.lang.annotation.Annotation;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.ManagedBean;
 import javax.inject.Named;
 import javax.inject.Provider;
 import javax.inject.Qualifier;
 import javax.inject.Singleton;
 import org.reflections.Reflections;
 import org.reflections.scanners.SubTypesScanner;
 import org.reflections.scanners.TypeAnnotationsScanner;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 
 /**
 * Configurator based on full annoations.
  *
  * @author Mathieu ANCELIN
  */
 public class AnnotationsConfigurator extends ConfigurationHandler {
 
     private String packagePrefix = "";
 
     @Override
     public InjectorImpl getInjector(Stage stage, Object... params) {
         Binder binder = new AnnotationBinder();
         Reflections reflections = new Reflections(new ConfigurationBuilder()
                 .setUrls(ClasspathHelper.getUrlsForPackagePrefix(packagePrefix))
                 .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
         Set<Class<?>> components = new HashSet<Class<?>>();
         components.addAll(reflections.getTypesAnnotatedWith(ManagedBean.class));
         components.addAll(reflections.getTypesAnnotatedWith(Singleton.class));
         components.addAll(reflections.getTypesAnnotatedWith(Named.class));
         for(Class<?> clazz : components) {
             String name = null;
             Named named = clazz.getAnnotation(Named.class);
             if (named != null)
                 name = named.value();
             ProvidedBy providedBy = clazz.getAnnotation(ProvidedBy.class);
             Provider provider = null;
             try {
                 if(providedBy != null)
                     provider = (Provider) providedBy.value().newInstance();
             } catch (Exception ex) {
                 Logger.getLogger(AnnotationsConfigurator.class.getName())
                         .log(Level.SEVERE, null, ex);
             }
             OnStage onStage = clazz.getAnnotation(OnStage.class);
             Stage stag = null;
             if (onStage != null)
                 stag = onStage.value();
             Annotation[] annotations = clazz.getAnnotations();
             Class<? extends Annotation> qualifier = null;
             for(Annotation anno : annotations) {
                 if((anno.annotationType().isAnnotationPresent(Qualifier.class))
                         && !(anno instanceof Named)) {
                     qualifier = anno.annotationType();
                 }
             }
             if (clazz.getInterfaces().length > 0) {
                 for(Class interf : clazz.getInterfaces()) {
                     Binding binding = new Binding(
                             qualifier, name, interf, clazz, provider, stag);
                     binder.getBindings().put(binding, binding);
                 }
             } else {
                 Binding binding = new Binding(
                         qualifier, name, clazz, clazz, provider, stag);
                 binder.getBindings().put(binding, binding);
             }
         }
         for(Binding b : binder.getBindings().values())
             System.out.println(b);
         return InjectorBuilder.makeInjector(binder, stage);
     }
 
     @Override
     public Object getDelegate() {
         return this;
     }
 
     @Override
     public boolean isAutoEnabled() {
         return true;
     }
 
     public void setPackagePrefix(String packagePrefix) {
         this.packagePrefix = packagePrefix;
     }
     
 }

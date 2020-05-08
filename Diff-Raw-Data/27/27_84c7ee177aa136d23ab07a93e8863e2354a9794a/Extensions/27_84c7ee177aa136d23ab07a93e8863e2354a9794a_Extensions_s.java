 package com.fathomdb.extensions;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fathomdb.Configuration;
 import com.fathomdb.discovery.DiscoveredSubTypes;
 import com.fathomdb.discovery.Discovery;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.util.Modules;
 
 public class Extensions {
     private static final Logger log = LoggerFactory.getLogger(Extensions.class);
 
     final List<ExtensionModule> extensions = Lists.newArrayList();
 
     @Inject
     public Extensions(Configuration configuration, Discovery discovery) {
         if (configuration != null) {
             loadExtensions(configuration);
         }
 
         if (discovery != null) {
             loadDiscoveredExtensions(discovery);
         }
     }
 
     // public static Extensions load(Configuration configuration,
     // ReflectionDiscovery reflection) {
     // return new Extensions(configuration, reflection);
     // }
     //
     // public void getInjector() {
     //
     // }
 
     private void loadExtensions(Configuration configuration) {
         String extensionList = configuration.find("extensions");
         if (!Strings.isNullOrEmpty(extensionList)) {
             for (String extension : Splitter.on(',').split(extensionList)) {
                 log.info("Using configured extension: " + extension);
 
                 Class<? extends ExtensionModule> extensionClass;
                 try {
                     if (!extension.contains(".")) {
                         extension = "org.platformlayer.extensions." + extension;
                     }
                     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                     extensionClass = (Class<? extends ExtensionModule>) classLoader.loadClass(extension);
                 } catch (ClassNotFoundException e) {
                     throw new IllegalStateException("Unable to load extension class: " + extension, e);
                 }
                 ExtensionModule extensionModule;
                 try {
                     extensionModule = extensionClass.newInstance();
                 } catch (InstantiationException e) {
                     throw new IllegalStateException("Unable to construct extension class: " + extension, e);
                 } catch (IllegalAccessException e) {
                     throw new IllegalStateException("Unable to construct extension class: " + extension, e);
                 }
 
                 extensions.add(extensionModule);
             }
         }
     }
 
     private void loadDiscoveredExtensions(Discovery discovery) {
         DiscoveredSubTypes<ExtensionModule> subTypes = discovery.getSubTypesOf(ExtensionModule.class);
 
         int count = 0;
 
         for (ExtensionModule extension : subTypes.getInstances()) {
             extensions.add(extension);
             log.info("Added extension: {}", extension.getClass().getSimpleName());
             count++;
         }
 
         if (count == 0) {
            log.info("No Annotated extensions found");
         }
     }
 
     public void addHttpExtensions(HttpConfiguration http) {
         for (ExtensionModule extension : extensions) {
             extension.addHttpExtensions(http);
         }
     }
 
     public void addEntities(List<Class<?>> entities) {
         for (ExtensionModule extension : extensions) {
             extension.addEntities(entities);
         }
     }
 
     public Injector createInjector(Configuration configuration, List<Module> modules) {
         for (ExtensionModule extension : extensions) {
             List<Module> extraModules = extension.getExtraModules(configuration);
             if (extraModules != null) {
                 modules.addAll(extraModules);
             }
         }
 
         List<Module> overrides = Lists.newArrayList();
 
         for (ExtensionModule extension : extensions) {
             Module module = extension.getOverrideModule();
             if (module != null) {
                 overrides.add(module);
             }
         }
 
         if (overrides.isEmpty()) {
             return Guice.createInjector(modules);
         } else {
             Module combined = Modules.override(modules).with(overrides);
             return Guice.createInjector(combined);
         }
     }
 
 }

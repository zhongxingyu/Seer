 /**
  * Copyright 2010 Marko Lavikainen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.contextfw.web.application.internal;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.regex.Pattern;
 
 import net.contextfw.web.application.PropertyProvider;
 import net.contextfw.web.application.ResourceCleaner;
 import net.contextfw.web.application.configuration.Configuration;
 import net.contextfw.web.application.internal.development.InternalDevelopmentTools;
 import net.contextfw.web.application.internal.development.ReloadingClassLoaderConf;
 import net.contextfw.web.application.internal.initializer.InitializerProvider;
 import net.contextfw.web.application.internal.page.PageScope;
 import net.contextfw.web.application.internal.service.DirectoryWatcher;
 import net.contextfw.web.application.internal.service.InitHandler;
 import net.contextfw.web.application.internal.service.UpdateHandler;
 import net.contextfw.web.application.internal.servlet.CSSServlet;
 import net.contextfw.web.application.internal.servlet.DevelopmentFilter;
 import net.contextfw.web.application.internal.servlet.InitServlet;
 import net.contextfw.web.application.internal.servlet.RegexUriMapping;
 import net.contextfw.web.application.internal.servlet.ScriptServlet;
 import net.contextfw.web.application.internal.servlet.UpdateServlet;
 import net.contextfw.web.application.internal.servlet.UriMapping;
 import net.contextfw.web.application.internal.servlet.UriMappingFactory;
 import net.contextfw.web.application.internal.util.ClassScanner;
 import net.contextfw.web.application.lifecycle.LifecycleListener;
 import net.contextfw.web.application.lifecycle.RequestInvocationFilter;
 import net.contextfw.web.application.scope.WebApplicationStorage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.servlet.ServletModule;
 
 public class WebApplicationServletModule extends ServletModule {
 
     private Logger logger = LoggerFactory.getLogger(WebApplicationServletModule.class);
     
     private final Pattern classPattern = Pattern.compile(".+\\.class");
 
     private final PropertyProvider properties;
 
     private final String resourcePrefix;
 
     private final Set<String> rootPackages;
 
     private final Configuration configuration;
 
     private final Map<String, InitServlet> servlets = new HashMap<String, InitServlet>();
     
     private InitializerProvider initializerProvider;
     
     private ReloadingClassLoaderConf reloadConf;
     
     private final RequestInvocationFilter filter;
     
     private InitHandler initHandler;
     
     private PageScope pageScope;
 
     private InternalDevelopmentTools internalDevelopmentTools;
     
     public WebApplicationServletModule(
             Configuration configuration,
             PropertyProvider propertyProvider,
             PageScope pageScope, 
             InternalDevelopmentTools internalDevelopmentTools) {
         
         resourcePrefix = configuration.get(Configuration.RESOURCES_PREFIX);
         this.configuration = configuration;
         this.properties = propertyProvider;
         this.pageScope = pageScope;
         this.internalDevelopmentTools = internalDevelopmentTools;
         rootPackages = configuration.get(Configuration.VIEW_COMPONENT_ROOT_PACKAGE);
         boolean reloadEnabled = configuration.get(Configuration.CLASS_RELOADING_ENABLED);
         
         if (reloadEnabled && configuration.get(Configuration.DEVELOPMENT_MODE)) {
             reloadConf = new ReloadingClassLoaderConf(configuration);
         }
         this.filter = configuration.get(Configuration.REQUEST_INVOCATION_FILTER);
         
     }
 
     @Override
     protected void configureServlets() {
         //bind(RequestInvocationFilter.class).toInstance(this.filter);
         requestInjection(this.filter);
         initHandler = new InitHandler(configuration, 
                                       pageScope, 
                                       internalDevelopmentTools);
         
         requestInjection(initHandler);
         initializerProvider = new InitializerProvider();
         
         serve(resourcePrefix + ".js").with(
                 ScriptServlet.class);
         serve(resourcePrefix + ".css").with(
                 CSSServlet.class);
         
         serveRegex(".*/contextfw-update/.*").with(UpdateServlet.class);
         serveRegex(".*/contextfw-refresh/.*").with(UpdateServlet.class);
         serveRegex(".*/contextfw-remove/.*").with(UpdateServlet.class);
         requestInjection(this);
         if (configuration.get(Configuration.DEVELOPMENT_MODE)) {
             serveDevelopmentMode();
         } else {
             serveProductionMode();
         }
     }
 
     private void serveDevelopmentMode() {
         logger.info("Serving view components in DEVELOPMENT mode");
         
         DirectoryWatcher classWatcher = reloadConf == null ?
             new DirectoryWatcher(configuration.get(Configuration.VIEW_COMPONENT_ROOT_PACKAGE),
                   classPattern) :  
             new DirectoryWatcher(reloadConf.getReloadablePackageNames(), 
                 classPattern);
         
         DevelopmentFilter developmentFilter = new DevelopmentFilter(
                 rootPackages,
                 initHandler,
                 initializerProvider,
                 internalDevelopmentTools,
                 classWatcher,
                 properties, 
                 filter);
         
         filter("/*").through(developmentFilter);
         requestInjection(developmentFilter);
     }
 
     private void serveProductionMode() {
         logger.info("Serving view components in PRODUCTION mode");
         
         List<Class<?>> classes = ClassScanner.getClasses(rootPackages);
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         UriMappingFactory fact = new UriMappingFactory();
         
         SortedSet<UriMapping> mappings = fact.createMappings(
                 classes, 
                 classLoader,
                 initializerProvider,
                 initHandler,
                 properties,
                 filter);
         
         serveMappings(mappings);
     }
 
     private void serveMappings(SortedSet<UriMapping> mappings) {
         for (UriMapping mapping : mappings) {
             servlets.put(mapping.getViewClass().getCanonicalName(), mapping.getInitServlet());
             if (mapping instanceof RegexUriMapping) {
                 logger.info("  Serving url: " + mapping.getViewClass().getName() + " => {} (regex)", mapping.getPath());
                 serveRegex(mapping.getPath()).with(mapping.getInitServlet());
             } else {
                 logger.info("  Serving url: " + mapping.getViewClass().getName() + " => {}", mapping.getPath());
                 serve(mapping.getPath()).with(mapping.getInitServlet());
             }   
         }
     }
     
     @Provides
     @Singleton
     public UpdateHandler provideUpdateHandler(
             LifecycleListener listeners,
             DirectoryWatcher watcher,
             ResourceCleaner cleaner,
            WebApplicationStorage storage) {
         
         return new UpdateHandler(listeners, 
                 watcher, 
                 cleaner,
                 storage,
                 configuration,
                pageScope);
     }
 }

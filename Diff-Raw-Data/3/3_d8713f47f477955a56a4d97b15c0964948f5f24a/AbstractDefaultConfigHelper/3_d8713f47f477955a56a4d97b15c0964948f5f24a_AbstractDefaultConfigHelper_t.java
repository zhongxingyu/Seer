 package org.grails.plugin.config;
 
 import grails.util.Environment;
 import groovy.lang.Closure;
 import groovy.lang.GroovyClassLoader;
 import groovy.util.ConfigObject;
 import groovy.util.ConfigSlurper;
 import groovy.util.Eval;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.groovy.grails.commons.GrailsApplication;
 import org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper;
 import org.codehaus.groovy.grails.plugins.GrailsPlugin;
 import org.codehaus.groovy.grails.plugins.GrailsPluginManager;
 import org.codehaus.groovy.grails.plugins.PluginManagerAware;
 import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 public abstract class AbstractDefaultConfigHelper implements
         PluginManagerAware, GrailsApplicationAware, InitializingBean {
 
     private final Log log = LogFactory.getLog(getClass());
     private GrailsApplication grailsApplication;
     private GrailsPluginManager pluginManager;
 
     @Override
     public void afterPropertiesSet() throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("afterPropertiesSet()");
         }
         enhanceGrailsApplication(this.pluginManager, this.grailsApplication);
     }
 
     @Override
     public void setGrailsApplication(GrailsApplication grailsApplication) {
         if (log.isDebugEnabled()) {
             log.debug("setGrailsApplication(): " + grailsApplication);
         }
         this.grailsApplication = grailsApplication;
     }
 
     @Override
     public void setPluginManager(GrailsPluginManager pluginManager) {
         if (log.isDebugEnabled()) {
             log.debug("setPluginManager(): " + pluginManager);
         }
         this.pluginManager = pluginManager;
     }
 
     public ConfigObject getMergedConfig(GrailsPluginManager pluginManager,
             GrailsApplication grailsApplication) {
         ConfigObject mergedConfig = (ConfigObject) Eval.x(grailsApplication,
                 "x.mergedConfig");
         if (mergedConfig == null) {
             enhanceGrailsApplication(pluginManager, grailsApplication);
         }
         mergedConfig = (ConfigObject) Eval.x(grailsApplication,
                 "x.mergedConfig");
         return mergedConfig;
     }
 
     public ConfigObject getMergedConfig() {
         return getMergedConfigImpl(this.pluginManager, this.grailsApplication);
     }
 
     protected abstract void enhanceGrailsApplication(
             GrailsPluginManager pluginManager,
             GrailsApplication grailsApplication);
 
     protected ConfigObject getMergedConfigImpl(
             GrailsPluginManager pluginManager,
             GrailsApplication grailsApplication) {
         if (log.isDebugEnabled()) {
             log.debug("getMergedConfigImpl()");
         }
         
         Assert.notNull(pluginManager);
         Assert.notNull(grailsApplication);
 
         List<Class<?>> defaultConfigClasses = new ArrayList<Class<?>>();
 
         /* Preserve plugin processing order. */
         for (GrailsPlugin plugin : pluginManager.getAllPlugins()) {
             String configName = plugin.getPluginClass().getSimpleName();
             if (configName.endsWith("GrailsPlugin")) {
                 configName = configName
                         .replace("GrailsPlugin", "DefaultConfig");
             } else {
                 configName = configName + "DefaultConfig";
             }
             if (log.isDebugEnabled()) {
                 log.debug("getMergedConfigImpl(): configName " + configName);
             }
 
             Class<?> defaultConfigClass = grailsApplication
                     .getClassForName(configName);
             if (defaultConfigClass != null) {
                 defaultConfigClasses.add(defaultConfigClass);
             }
         }
         
         if (log.isDebugEnabled()) {
             log.debug("getMergedConfigImpl(): defaultConfigClasses " + defaultConfigClasses);
         }
 
         GroovyClassLoader classLoader = null;
 
         {
             ClassLoader parentClassLoader = grailsApplication.getClassLoader();
             if (parentClassLoader == null) {
                 parentClassLoader = Thread.currentThread()
                         .getContextClassLoader();
                 if (parentClassLoader == null) {
                     parentClassLoader = this.getClass().getClassLoader();
                 }
             }
 
             if (parentClassLoader instanceof GroovyClassLoader) {
                 classLoader = (GroovyClassLoader) parentClassLoader;
             } else {
                 classLoader = new GroovyClassLoader(parentClassLoader);
             }
         }
 
         ConfigObject config = new ConfigObject();
         mergeInDefaultConfigs(config, defaultConfigClasses, classLoader);
 
         ConfigurationHelper.initConfig(config, null, classLoader);
         config.merge(grailsApplication.getConfig());
 
         return config;
     }
 
     @SuppressWarnings("serial")
     protected Closure buildGetMergedConfig(
             final GrailsPluginManager pluginManager,
             final GrailsApplication grailsApplication) {
         return new Closure(this) {
 
             @SuppressWarnings("unused")
             public ConfigObject doCall() {
                 return AbstractDefaultConfigHelper.this.getMergedConfigImpl(
                         pluginManager, grailsApplication);
             }
 
         };
 
     }
 
     protected void mergeInDefaultConfigs(ConfigObject config,
             List<Class<?>> defaultConfigClasses, GroovyClassLoader classLoader) {
         ConfigSlurper configSlurper = new ConfigSlurper(Environment
                 .getCurrent().getName());
         configSlurper.setClassLoader(classLoader);
         for (Class<?> defaultConfigClass : defaultConfigClasses) {
            configSlurper.setBinding(config);
             ConfigObject newConfig = configSlurper.parse(defaultConfigClass);
             config.merge(newConfig);
         }
     }
 
 }

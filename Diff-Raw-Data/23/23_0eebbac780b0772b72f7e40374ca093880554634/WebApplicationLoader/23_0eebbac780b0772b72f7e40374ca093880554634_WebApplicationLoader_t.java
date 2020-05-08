 /* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package org.snowfk.web;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.ServletContext;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.snowfk.SnowRuntimeException;
 import org.snowfk.util.ClassesInPackageScanner;
 import org.snowfk.web.db.hibernate.HibernateDaoHelper;
 import org.snowfk.web.db.hibernate.HibernateHandler;
 import org.snowfk.web.method.WebActionHandler;
 import org.snowfk.web.method.WebExceptionHandler;
 import org.snowfk.web.method.WebFileHandler;
 import org.snowfk.web.method.WebModelHandler;
 import org.snowfk.web.method.WebTemplateDirectiveHandler;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class WebApplicationLoader {
    static private Logger       logger           = LoggerFactory.getLogger(WebApplicationLoader.class);
     static private final String DEFAULT_APP_NAME = "default";

     public enum Alert {
         JNDI_CONFIG_PROPERTIES_FILE_NOT_FOUND,
         JNDI_CONFIG_PROPERTIES_JNDI_VALUE_NOT_FOUND,
         INVALID_CONFIG_CLASSPATH,
         FAILED_TO_LOAD_CLASSPATH_CONFIG_PROPERTIES,
         CONFIG_FILE_IN_CLASSPATH_NOT_FOUND;
     }
 
     private File          sfkFolder;
     Injector              appInjector;
     WebApplication        webApplication;
     ServletContext        servletContext;
     PropertyPostProcessor propertyPostProcessor;
 
     public WebApplicationLoader(File sfkFolder, ServletContext servletContext) {
         this.sfkFolder = sfkFolder;
         this.servletContext = servletContext;
 
     }
 
     public WebApplication getWebApplication() {
         return webApplication;
     }
 
     public WebController getWebController() {
         return appInjector.getInstance(WebController.class);
     }
 
     // so far only use for test
     public HibernateHandler getHibernateHandler() {
         return appInjector.getInstance(HibernateHandler.class);
     }
 
     // so far only use for test
     @Deprecated
     public HibernateDaoHelper getHibernateDaoHelper() {
         return appInjector.getInstance(HibernateDaoHelper.class);
     }
 
     public File getWebAppFolder() {
         File webInfFolder = new File(servletContext.getRealPath("WEB-INF/"));
         File webAppFolder = webInfFolder.getParentFile();
         String webAppFolderName = webAppFolder.getName();
         // Linux hack (somehow on Linux when contextPath empty, the
         // webAppFolder.getName() return ".", so, if this
         // is the case, need to go one parent up
         if (".".equals(webAppFolderName)) {
             webAppFolder = webAppFolder.getParentFile();
             webAppFolderName = webAppFolder.getName();
         }
 
         return webAppFolder;
     }
 
     @SuppressWarnings("unchecked")
     public WebApplicationLoader load() throws Exception {
 
         /*--------- Load the Properties ---------*/
         // First load the application.properties
         Properties appProperties = new Properties();
         File propertiesFile = new File(sfkFolder, "application.properties");
 
         if (propertiesFile.exists()) {
             appProperties.load(new FileReader(propertiesFile));
         } else {
             logger.info("Now application.properties found in WEB-INF/snow/. Starting blank application.");
         }
 
         // second load the config.properties. If we have a
         // config.properties.location load it from there.
         Properties configProperties = null;
 
         // --------- Loading the config.properties --------- //
         String configLocation = null;
 
         String jndiConfigLookup = appProperties.getProperty("snow.jndiConfigLocation", null);
 
         if (jndiConfigLookup != null) {
 
             // get the configLocation
             try {
                 Context initContext = new InitialContext();
                 configLocation = (String) initContext.lookup(jndiConfigLookup);
             } catch (Exception e) {
                 SnowRuntimeException se = new SnowRuntimeException(Alert.JNDI_CONFIG_PROPERTIES_JNDI_VALUE_NOT_FOUND, e, "snow.jndiConfigLocation", configLocation);
                 logger.error(se.getMessage());
                 // 2010-10-14-Jeremy: now, we just fail silently (this allow to
                 // have the jndi property optional)
                 // throw se;
             }
 
             // TODO: Should we close the InputStream or does the Properties.load
             // closes ti?
             if (configLocation != null) {
                 // if it is a classpath
                 if (configLocation.startsWith("classpath:")) {
                     if (configLocation.length() < "classpath:".length()) {
                         throw new SnowRuntimeException(Alert.INVALID_CONFIG_CLASSPATH, "configClassPath", configLocation);
                     }
                     String classpathConfigPath = configLocation.substring("classpath:".length());
                     try {
 
                         InputStream is = WebApplicationLoader.class.getResourceAsStream(classpathConfigPath);
                         if (is == null) {
                             throw new SnowRuntimeException(Alert.CONFIG_FILE_IN_CLASSPATH_NOT_FOUND, "configLocation", configLocation, "classpathConfigPath", classpathConfigPath);
                         }
                         configProperties = new Properties();
                         configProperties.load(is);
                     } catch (Throwable t) {
                         throw new SnowRuntimeException(Alert.FAILED_TO_LOAD_CLASSPATH_CONFIG_PROPERTIES, t, "configLocation", configLocation, "classpathConfigPath", classpathConfigPath);
                     }
                 }
                 // otherwise, assume a URI of a file
                 else {
                     File configFile = new File(new URI(configLocation));
                     if (!configFile.exists()) {
                         SnowRuntimeException se = new SnowRuntimeException(Alert.JNDI_CONFIG_PROPERTIES_FILE_NOT_FOUND, "snow.jndiConfigLocation", configLocation, "configFilePath", configFile.getAbsoluteFile());
                         logger.error(se.getMessage());
                         throw se;
                     }
                     configProperties = new Properties();
                     configProperties.load(new FileReader(configFile));
                 }
             }
         }
         // System.out.println("Loading config from JNDI location: " +
         // jndiConfigLocation);
 
         // otherwise, look if we have a config.properties
         if (configProperties == null) {
 
             File configFile = new File(sfkFolder, "config.properties");
 
             if (configFile.exists()) {
                 configProperties = new Properties();
                 configProperties.load(new FileReader(configFile));
             }
         }
 
         if (configProperties != null) {
             appProperties.putAll(configProperties);
         }
 
         // --------- /Loading the config.properties --------- //
         // if a ServletContext, then look if there is a WebApp instance
         // application.appname.properties
         if (servletContext != null) {
 
             File webAppFolder = getWebAppFolder();
             String webAppFolderName = webAppFolder.getName();
 
             File webAppPropertiesFile = new File(webAppFolder.getParentFile(), webAppFolderName + ".application.properties");
             if (webAppPropertiesFile.exists()) {
                 Properties webAppProperties = new Properties();
                 webAppProperties.load(new FileReader(webAppPropertiesFile));
                 // override the appProperties with the WebAppRoperties
                 appProperties.putAll(webAppProperties);
             }
 
         }
 
         PropertyPostProcessor propertyPostProcessor = getPropertyPostProcessor();
 
         // if we do not have it programmatically, then, look in the
         // snow.snow.propertyPostProcessorClass properties
         if (propertyPostProcessor == null) {
             String propertyPostProcessorClassName = appProperties.getProperty("snow.propertyPostProcessorClass");
             if (propertyPostProcessorClassName != null) {
                 try {
                     Class<PropertyPostProcessor> propertyPostProcessorClass = (Class<PropertyPostProcessor>) Class.forName(propertyPostProcessorClassName);
 
                     if (propertyPostProcessorClass != null) {
                         propertyPostProcessor = propertyPostProcessorClass.newInstance();
                     }
                 } catch (Exception e) {
                     logger.error("Cannot load or process the PropertyPostProcess class: " + propertyPostProcessorClassName
                                             + "\nException: "
                                             + e.getMessage());
                 }
             }
         }
         try {
             if (propertyPostProcessor != null) {
                 appProperties = propertyPostProcessor.processProperties(appProperties);
             }
         } catch (Exception e) {
             logger.error("Cannot process PropertyPostProcess class: " + propertyPostProcessor
                                     + "\nException: "
                                     + e.getMessage());
         }
 
         /*--------- /Load the Properties ---------*/
 
         /*--------- Load WebApplication ---------*/
         appInjector = Guice.createInjector(new WebApplicationConfig(appProperties, sfkFolder, servletContext));
         webApplication = appInjector.getInstance(WebApplication.class);
         /*--------- /Load WebApplication ---------*/
 
         // --------- Load ApplicationModule if defined --------- //
         String applicationConfigClassStr = appProperties.getProperty("snow.applicationWebModuleConfigClass");
         if (applicationConfigClassStr != null) {
             Class webModuleConfigClass = Class.forName(applicationConfigClassStr);
             WebModuleConfig webAppModuleConfig = (WebModuleConfig) webModuleConfigClass.newInstance();
 
             String applicationWebModuleName = webAppModuleConfig.getWebModuleName();
 
             if (applicationWebModuleName == null) {
                 applicationWebModuleName = DEFAULT_APP_NAME;
             }
             WebModule webModule = createAndRegisterWebModule(webAppModuleConfig, applicationWebModuleName);
 
             // set the root webapp folder as the view folder
            if (servletContext != null) {
                webModule.setViewFolder(getWebAppFolder());
            }
             // set the /WEB-INF/snow/conf as the conf folder
             webModule.setConfigFolder(new File(sfkFolder, "/config"));
 
             webApplication.setSnowDefaultModuleName(applicationWebModuleName);
         }
         // --------- Load ApplicationModule if defined --------- //
 
         // This way will be deprecated soon.
         /*--------- Load the WebModules from WEB-INF/snow/mods/ ---------*/
 
         List<File> moduleFolders = getModuleFolders(sfkFolder);
 
         for (File webModuleFolder : moduleFolders) {
             File modulePropertiesFile = new File(webModuleFolder, "module.properties");
 
             WebModuleConfig webModuleConfig;
            String webModuleName = null;

             if (modulePropertiesFile.exists()) {
                 Properties moduleProperties = new Properties();
                 moduleProperties.load(new FileReader(modulePropertiesFile));
                 // // get the webModule class
                 String webModuleConfigClassNanme = moduleProperties.getProperty("snow.webModuleConfigClass");
                 Class webModuleConfigClass = Class.forName(webModuleConfigClassNanme);
 
                 webModuleConfig = (WebModuleConfig) webModuleConfigClass.newInstance();
                 webModuleConfig.setProperties(moduleProperties);
             } else {
                 webModuleConfig = new WebModuleConfig();
             }

            if (webModuleName == null) {
                 webModuleName = webModuleFolder.getName();
             }
 
             WebModule webModule = createAndRegisterWebModule(webModuleConfig, webModuleName);
 
             // // set the webModule folder
             webModule.setFolder(webModuleFolder);
 
         }
         /*--------- /Load the WebModules from WEB-INF/snow/mods/ ---------*/
 
         // --------- Set Blank WebModule if no Default Module --------- //
         if (webApplication.getDefaultWebModule() == null) {
             WebModule webModule = createAndRegisterBlankDefaultWebModule();
             // set the root webapp folder as the view folder
             webModule.setViewFolder(getWebAppFolder());
             // set the /WEB-INF/snow/conf as the conf folder
             webModule.setConfigFolder(new File(sfkFolder, "/WEB-INF/snow/conf"));

             webApplication.setSnowDefaultModuleName(webModule.getName());
         }
         // --------- /Set Blank WebModule if no Default Module --------- //
         return this;
     }
 
     // --------- Public Methods --------- //
     public PropertyPostProcessor getPropertyPostProcessor() {
         return propertyPostProcessor;
     }
 
     /**
      * Allow to programatically set a propertyPostProcessor to the appLoader. Usually used for Unit Testing.
      * 
      * @param propertyPostProcessor
      */
     public void setPropertyPostProcessor(PropertyPostProcessor propertyPostProcessor) {
         this.propertyPostProcessor = propertyPostProcessor;
     }
 
     // --------- /Public Methods --------- //
 
     /*--------- Privates ---------*/
     private WebModule createAndRegisterBlankDefaultWebModule() {
         return createAndRegisterWebModule(null, DEFAULT_APP_NAME);
     }
 
     private WebModule createAndRegisterWebModule(WebModuleConfig webModuleConfig, String moduleName) {
         WebModule webModule = null;
         Injector moduleInjector = null;
 
         if (webModuleConfig != null) {
             // probably need to add some security, such as only authorized
             // module
             // get WebApplication
             webModuleConfig.setWebApplication(webApplication);
 
             // // get the injector and the webModule
             moduleInjector = appInjector.createChildInjector(webModuleConfig);
             webModule = moduleInjector.getInstance(WebModule.class);
 
         } else {
             webModule = new WebModule();
         }
 
         // // set the webModule name
         webModule.setName(moduleName);
 
         // / if the webHandlers are null, try to discover them. Start from the
         // package of the WebModule
         if (webModuleConfig != null && webModule.getWebHandlers() == null) {
             Class[] webHandlerClasses = scanForWebHandlerClasses(webModuleConfig);
             if (webHandlerClasses != null && webHandlerClasses.length > 0) {
                 List<Object> webHandlers = new ArrayList<Object>(webHandlerClasses.length);
                 for (Class webHandlerClass : webHandlerClasses) {
                     Object webHandler = moduleInjector.getInstance(webHandlerClass);
                     webHandlers.add(webHandler);
                 }
                 webModule.setWebHandlers(webHandlers);
             }
         }
 
         // // add the webModule to the application
         webApplication.addWebModule(webModule);
         return webModule;
     }
 
     private List<File> getModuleFolders(File sfkFolder) {
         List<File> moduleFolders = new ArrayList<File>();
         File modsFolder = new File(sfkFolder, "mods");
         if (modsFolder.exists()) {
             for (File childFile : modsFolder.listFiles()) {
                 if (childFile.isDirectory()) {
                     moduleFolders.add(childFile);
                 }
             }
         }
         return moduleFolders;
     }
 
     private Class[] scanForWebHandlerClasses(WebModuleConfig webModuleConfig) {
         Class[] webHandlerClasses = scanForClasses(webModuleConfig.getClass(), new ClassesInPackageScanner.AcceptanceTest() {
             @Override
             public boolean acceptClass(Class<?> cls) {
                 for (Method method : cls.getDeclaredMethods()) {
                     if (method.getAnnotation(WebActionHandler.class) != null || method.getAnnotation(WebFileHandler.class) != null
                                             || method.getAnnotation(WebModelHandler.class) != null
                                             || method.getAnnotation(WebTemplateDirectiveHandler.class) != null
                                             || method.getAnnotation(WebExceptionHandler.class) != null) {
                         return true;
                     }
                 }
                 return false;
             }
         });
 
         return webHandlerClasses;
     }
 
     private Class[] scanForClasses(Class baseClass,
                             org.snowfk.util.ClassesInPackageScanner.AcceptanceTest acceptanceTest) {
         Set<Class<?>> classes;
 
         try {
             // Note: the mobules have the same classloader as the application,
             // so, the getClass().getClassLoader() if fine.
             classes = new ClassesInPackageScanner(baseClass.getPackage().getName(), getClass().getClassLoader(), false, acceptanceTest).scan(true);
         } catch (IOException e) {
             throw new IllegalStateException("unable to scan package for classes", e);
         }
 
         return classes.toArray(new Class[classes.size()]);
 
     }
     /*--------- /Privates ---------*/
 }

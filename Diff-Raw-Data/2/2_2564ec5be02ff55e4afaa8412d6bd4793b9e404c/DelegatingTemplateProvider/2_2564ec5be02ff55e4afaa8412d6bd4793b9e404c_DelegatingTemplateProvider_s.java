 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 
 package org.webmacro.resource;
 
 import org.webmacro.*;
 import org.webmacro.util.Settings;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Arrays;
 
 /**
  * Alternative implementation of a TemplateProvider that uses TemplateLoaders to do the actual work.
  * This template provider controls a list of TemplateLoaders to do the actual work of loading
  * a template. It asks the template loaders one by one, until a template is found or the end of
  * the list is reached. It is configured by a list of "TemplateLoaderPath.n" settings in 
  * WebMacro.properties.<br>
  * <br>
  * Each template loader is described by an url like syntax with a TemplateLoaderPath.n setting,
  * where n should be a number starting from one.
  * <br>
  * Each template loader path is of the form "[protocol:][path]". If the protocol part in square brackets
  * is ommited, "default:" is assumed.
  * For each protocol, a "TemplateLoader.protocol" setting must give the fully qualified
  * classname of the template loader to be used for this protocol.
  * Example configuration:<br>
  * <pre>
  * TemplateLoaderPath.1=.
  * TemplateLoaderPath.2=classpath:
  * TemplateLoaderPath.3=webapp:/WEB-INF/templates/
  * TemplateLoader.default=org.webmacro.resource.FileTemplateLoader
  * TemplateLoader.classpath=org.webmacro.resource.ClassPathTemplateLoader
  * TemplateLoader.webapp=org.webmacro.resource.ServletContextTemplateLoader
  * </pre>
  * This configuration will search for templates at three locations in this order:
  * <ol>
  * <li>The current directory (".")
  * <li>The classpath (classpath:)
  * <li>The directory WEB-INF/templates/ in the web-app directory ("webapp:/WEB-INF/templates/")
  * </ol>
  * Note, that this setup only makes sense in a web-app environment, because the webapp template loader
  * won't work otherwise.
  * @author Sebastian Kanthak (sebastian.kanthak@muehlheim.de)
  */
 public class DelegatingTemplateProvider extends CachingProvider {
     private Broker broker;
     private Log log;
     private TemplateLoaderFactory factory;
     private TemplateLoader[] templateLoaders;
 
     public void init(Broker broker,Settings config) throws InitException {
         super.init(broker,config);
         this.broker = broker;
         log = broker.getLog("resource","DelegatingTemplateProvider");
 
         String factoryClass = config.getSetting("TemplateLoaderFactory","");
         log.info("DelegatingTemplateProvider: Using TemplateLoaderFactory "+factoryClass);
         factory = createFactory(factoryClass);
 
         List loaders = new ArrayList();
 
         // for compatability reasons, check old TemplatePath setting
         if (config.getBooleanSetting("DelegatingTemplateProvider.EmulateTemplatePath",false)) {
            if (config.getSetting("TemplatePath").length() > 0) {
                 TemplateLoader loader = new TemplatePathTemplateLoader();
                 loader.init(broker,config);
                 loader.setConfig("");
                 loaders.add(loader);
             }
         }
 
         int i = 0;
         String loader = config.getSetting("TemplateLoaderPath.".concat(String.valueOf(i+1)));
         while (loader != null) {
             loaders.add(factory.getTemplateLoader(broker,loader));
             i++;
             loader = config.getSetting("TemplateLoaderPath.".concat(String.valueOf(i+1)));
         }
         templateLoaders = new TemplateLoader[loaders.size()];
         loaders.toArray(templateLoaders);
     }
     
     public String getType() {
         return "template";
     }
 
     /**
      * Ask all template loaders to load a template from query.
      * Returns the template from the first provider, that returns a non-null value
      * or throws a NotFoundException, if all providers return null.
      */
     public Object load(String query,CacheElement ce) throws ResourceException {
         for (int i=0; i < templateLoaders.length; i++) {
             Template t = templateLoaders[i].load(query,ce);
             if (t != null) {
                 return t;
             }
         }
         throw new NotFoundException("Could not locate template "+query);
     }
 
     /**
      * Returns an unmodifieable list of this provider's template loaders.
      * The list is has the same order used for searching templates. You may
      * use this method to access template loaders and change their settings
      * at runtime if they have an appropriate method.
      * @return unmodifieable list of TemplateLoader objects
      */
     public List getTemplateLoaders() {
         return Collections.unmodifiableList(Arrays.asList(templateLoaders));
     }
 
     protected TemplateLoaderFactory createFactory(String classname) throws InitException {
         try {
             return (TemplateLoaderFactory)Class.forName(classname).newInstance();
         } catch (ClassNotFoundException e) {
             throw new InitException("Class "+classname+" for template loader factory not found",e);
         } catch (InstantiationException e) {
             throw new InitException("Could not instantiate class "+classname+" for template loader factory",e);
         } catch (IllegalAccessException e) {
             throw new InitException("Could not instantiate class "+classname+" for template loader facory",e);
         } catch (ClassCastException e) {
             throw new InitException("Class "+classname+" for template loader factory does not implement "+
                                     "interface org.webmacro.resource.TemplateLoaderFactory",e);
         }
     }
 }

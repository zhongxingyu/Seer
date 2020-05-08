 /*
  * Copyright (c) 1999-2004 Evavi, Inc. All Rights Reserved.
  *
  * This software is the proprietary information of Evavi, Inc.
  * Use is subject to license terms. License Agreement available at
  * <a href="http://www.evavi.com" target="_blank">www.evavi.com</a>
  */
 package com.cyclopsgroup.cyclib.log4j;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.avalon.framework.activity.Initializable;
 import org.apache.avalon.framework.configuration.Configurable;
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.context.Context;
 import org.apache.avalon.framework.context.ContextException;
 import org.apache.avalon.framework.context.Contextualizable;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.PropertyConfigurator;
 import org.codehaus.plexus.logging.AbstractLoggerManager;
 import org.codehaus.plexus.logging.Logger;
 
 /**
  * Log4j implemented Avalon Logger Manager
  *
  * @author <a href="mailto:jiaqi.guo@evavi.com">Jiaqi Guo</a>
  */
 public class Log4jLoggerManager extends AbstractLoggerManager implements
         Initializable, Configurable, Contextualizable
 {
     private ThreadLocal cache = new ThreadLocal();
 
     private Context context;
 
     private String log4jConfiguration;
 
     /**
      * Override method configure() in super class
      *
      * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
      */
     public void configure(Configuration configuration)
             throws ConfigurationException
     {
         log4jConfiguration = configuration.getChild("properties").getValue(
                 "com/cyclopsgroup/cyclib/log4j/log4j.properties");
     }
 
     /**
      * Override or implement method of parent class or interface
      *
      * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
      */
     public void contextualize(Context context) throws ContextException
     {
         this.context = context;
     }
 
     /**
      * Override method getActiveLoggerCount() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#getActiveLoggerCount()
      */
     public int getActiveLoggerCount()
     {
         return 0;
     }
 
     /**
      * Override method getLoggerForComponent() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#getLoggerForComponent(java.lang.String, java.lang.String)
      */
     public Logger getLoggerForComponent(String role, String hint)
     {
         AvalonLoggerAdapter logger = (AvalonLoggerAdapter) cache.get();
         Log log = LogFactory.getLog(role);
         if (logger == null)
         {
             logger = new AvalonLoggerAdapter(log);
             cache.set(logger);
         }
         else
         {
             logger.setLogger(log);
         }
         return logger;
     }
 
     /**
      * Override method getThreshold() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#getThreshold()
      */
     public int getThreshold()
     {
         return Logger.LEVEL_DEBUG;
     }
 
     /**
      * Override method getThreshold() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#getThreshold(java.lang.String, java.lang.String)
      */
     public int getThreshold(String role, String hint)
     {
         return Logger.LEVEL_DEBUG;
     }
 
     /**
      * Override method initialize() in super class
      *
      * @see org.apache.avalon.framework.activity.Initializable#initialize()
      */
     public void initialize() throws Exception
     {
         File file = new File(log4jConfiguration);
         Properties props = new Properties();
 
         URL resource = null;
         if (file.isFile())
         {
             resource = file.toURL();
         }
         else
         {
             resource = getClass().getClassLoader().getResource(
                     log4jConfiguration);
             if (resource == null)
             {
                 System.err.println("Can not find log4j properties file at ["
                         + log4jConfiguration + "], default will be used");
                 resource = getClass().getResource("log4j.properties");
             }
         }
         props.load(resource.openStream());
         String basedir = (String) context.get("basedir");
         if (StringUtils.isNotEmpty(basedir))
         {
             props.setProperty("basedir", basedir);
         }
        PropertyConfigurator.configure(props);
 
     }
 
     /**
      * Override method returnComponentLogger() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#returnComponentLogger(java.lang.String, java.lang.String)
      */
     public void returnComponentLogger(String role, String hint)
     {
         AvalonLoggerAdapter logger = (AvalonLoggerAdapter) cache.get();
         if (logger != null)
         {
             logger.setLogger(null);
         }
     }
 
     /**
      * Override method setThreshold() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#setThreshold(int)
      */
     public void setThreshold(int threshold)
     {
     }
 
     /**
      * Override method setThreshold() in super class
      *
      * @see org.codehaus.plexus.logging.LoggerManager#setThreshold(java.lang.String, java.lang.String, int)
      */
     public void setThreshold(String role, String hint, int threshold)
     {
     }
 }

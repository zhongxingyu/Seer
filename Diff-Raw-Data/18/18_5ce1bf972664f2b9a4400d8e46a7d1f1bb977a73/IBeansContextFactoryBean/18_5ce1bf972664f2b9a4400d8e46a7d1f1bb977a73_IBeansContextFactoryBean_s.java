 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.spring;
 
 import org.mule.api.MuleContext;
 import org.mule.api.config.ConfigurationBuilder;
 import org.mule.api.context.MuleContextFactory;
 import org.mule.config.DefaultMuleConfiguration;
 import org.mule.config.builders.DefaultsConfigurationBuilder;
 import org.mule.context.DefaultMuleContextBuilder;
 import org.mule.context.DefaultMuleContextFactory;
 import org.mule.ibeans.IBeansContext;
 import org.mule.ibeans.IBeansFactory;
 import org.mule.ibeans.config.IBeanHolderConfigurationBuilder;
 import org.mule.ibeans.internal.config.ShutdownSplash;
 import org.mule.ibeans.internal.config.StartupSplash;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanCreationException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.config.AbstractFactoryBean;
 import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.AbstractApplicationContext;
 
 /**
 * TODO
  */
 public class IBeansContextFactoryBean extends AbstractFactoryBean implements InitializingBean, ApplicationContextAware
 {
     private String serverId;
     private boolean disableInjectors = false;
     private ApplicationContext applicationContext;
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
     {
         this.applicationContext = applicationContext;
     }
 
     public Class getObjectType()
     {
         return IBeansContext.class;
     }
 
     protected Object createInstance() throws Exception
     {
         //Discover client iBeans
         List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
         builders.add(new DefaultsConfigurationBuilder());
         builders.add(new IBeanHolderConfigurationBuilder());
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
 
         DefaultMuleConfiguration muleConfiguration = new DefaultMuleConfiguration();
         if (getServerId() != null)
         {
             muleConfiguration.setId(getServerId());
         }
        DefaultMuleContextBuilder muleContextBuilder = new DefaultMuleContextBuilder();
         muleContextBuilder.setStartupScreen(new StartupSplash());
         muleContextBuilder.setShutdownScreen(new ShutdownSplash());
         muleContextBuilder.setMuleConfiguration(muleConfiguration);
 
         MuleContext mc = muleContextFactory.createMuleContext(builders, muleContextBuilder);
         new IBeansFactory().setMuleContext(mc);
 
         IBeansContext ibc = mc.getRegistry().lookupObject(IBeansContext.class);
 
         if (!disableInjectors)
         {
             if (applicationContext instanceof AbstractApplicationContext)
             {
                 ((AbstractAutowireCapableBeanFactory) applicationContext.getAutowireCapableBeanFactory())
                         .addBeanPostProcessor(new IBeanInjectorsBeanPostProcessor(mc));
             }
             else
             {
                 throw new BeanCreationException("IBean injectors are enabled but the applicationContext being used does not extend AbstractApplicationContext so cannot register post processor");
             }
         }
         return ibc;
     }
 
     public String getServerId()
     {
         return serverId;
     }
 
     public void setServerId(String serverId)
     {
         this.serverId = serverId;
     }
 
     public boolean isDisableInjectors()
     {
         return disableInjectors;
     }
 
     public void setDisableInjectors(boolean disableInjectors)
     {
         this.disableInjectors = disableInjectors;
     }
 }

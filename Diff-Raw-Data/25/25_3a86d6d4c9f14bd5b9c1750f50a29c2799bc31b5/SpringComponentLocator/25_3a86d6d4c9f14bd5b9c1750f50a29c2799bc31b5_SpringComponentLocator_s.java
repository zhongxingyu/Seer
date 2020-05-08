package com.atlassian.sal.api.component;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.AbstractApplicationContext;
 
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
 
 public class SpringComponentLocator extends ComponentLocator implements ApplicationContextAware
 {
     private ApplicationContext applicationContext;
     private static final Logger log = Logger.getLogger(SpringComponentLocator.class);
 
     public SpringComponentLocator()
     {
         ComponentLocator.setComponentLocator(this);
     }
 
     
     protected <T> T getComponentInternal(Class<T> iface)
     {
     	Map<String, T> beansOfType = getBeansOfType(iface);
 
     	if (beansOfType.isEmpty())
         {
             throw new RuntimeException("Could not retrieve " + iface.getName());
         } else if (beansOfType.size()>1)
         {
             // we have multiple implementations of this interface, choose one with name that looks like iface name
             String shortClassName = convertClassToName(iface);
             T implementation = (T) beansOfType.get(shortClassName);
             if (implementation == null)
             {
                 log.warn("More than one instance of " + iface.getName() + " found but none of them has key " + shortClassName);
             }
             return implementation;
         }
         return (T) beansOfType.values().iterator().next();
     }
 
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
     {
         this.applicationContext = applicationContext;
     }
     
 	@Override
 	protected <T> Collection<T> getComponentsInternal(Class<T> iface)
 	{
 		Map<String, T> beansOfType = getBeansOfType(iface);
 		return beansOfType.values();
 	}
 
 	private <T> Map<String, T> getBeansOfType(Class<T> iface)
 	{
         try
 		{
         	return applicationContext.getBeansOfType(iface);
 		} catch (BeansException e)
 		{
 			// This exception will start occuring after removing bean from the context (eg: disabling plugin)
 			// until next restart of Confluence. See https://studio.atlassian.com/browse/JST-509
 			log.debug(e,e);
 		}
 
 		Map<String, T> results = new HashMap<String, T>();
 		if (applicationContext instanceof AbstractApplicationContext)
 		{
 			AbstractApplicationContext abstractApplicationContext = (AbstractApplicationContext) applicationContext;
 			ConfigurableListableBeanFactory beanFactory = abstractApplicationContext.getBeanFactory();
 			if (beanFactory instanceof DefaultListableBeanFactory)
 			{
 				DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
 				String[] beanDefinitionNames = defaultListableBeanFactory.getBeanDefinitionNames();
 				for (int i = 0; i < beanDefinitionNames.length; i++)
 				{
 					String beanName = beanDefinitionNames[i];
 					try
 					{
 						Object bean = defaultListableBeanFactory.getBean(beanName);
 						if (bean != null && iface.isAssignableFrom(bean.getClass()))
 						{
 							results.put(beanName, (T) bean);
 						}
 					} catch (BeansException e)
 					{
 						// ignore
 					}
 				}
 			}
 		}
 		return results;
 	}
 }

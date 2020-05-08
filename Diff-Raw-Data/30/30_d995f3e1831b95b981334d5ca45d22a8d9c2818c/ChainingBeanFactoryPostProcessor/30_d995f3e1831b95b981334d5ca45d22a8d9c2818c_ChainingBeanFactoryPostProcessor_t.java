 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics.aop.config;
 
import java.util.HashSet;
import java.util.Set;

 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
 
 /** 
  * Re-runs all other post-processors on the given bean factory on 
  * another target bean factory, for chaining factory post-process
  * operations across multiple unrelated factories.
  * @see TemplateBeanDefinitionParser
  */
 public class ChainingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
 
	private static final HashSet<String> DEFAULT_EXCLUDES;
	static {
		DEFAULT_EXCLUDES = new HashSet<String>();
		DEFAULT_EXCLUDES.add("org.springframework.security.");
	}
	
 	private ConfigurableListableBeanFactory targetFactory;
	private Set<String> excludes = DEFAULT_EXCLUDES;
 	
 	public void setTargetFactory(ConfigurableListableBeanFactory targetFactory) {
 		this.targetFactory = targetFactory;
 	}
 
 	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
 		if (!targetFactory.equals(beanFactory)) {
 			String[] bfpps = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, false, false);
 			for (String name : bfpps) {
 				BeanFactoryPostProcessor delegate = (BeanFactoryPostProcessor)beanFactory.getBean(name);
				if (isAllowed(delegate))
 					delegate.postProcessBeanFactory(targetFactory);
 			}
 		}
 	}

	/**
	 * Filter out excluded post-processors from the chain.  Certain post-processors (like spring security)
	 * depend on finding beans in the target factory, so they should be excluded.
	 */
	protected boolean isAllowed(BeanFactoryPostProcessor proc) {
		if (proc == this)
			return false;
		for (String pkg : excludes)
			for (Class<?> type = proc.getClass(); type != Object.class; type = type.getSuperclass())
				if (type.getName().startsWith(pkg))
					return false;
		return true;
	}
 }

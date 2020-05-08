 /*
  * This is a utility project for wide range of applications
  * 
  * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.bean;
 
 import com.smartitengineering.util.bean.annotations.Aggregator;
 import com.smartitengineering.util.bean.annotations.InjectableField;
 import java.lang.reflect.Field;
 import java.util.Map;
 import java.util.WeakHashMap;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This registrar will basically be a registrar for all the bean factories which
  * are chosen to be register themselves. Plus it allows external beans to get
  * initialized by this through invocation of its static methods.
  * @author imyousuf
  */
 public class BeanFactoryRegistrar {
 
 		private static final Map<String, BeanFactory> beanFactories =
 																									new WeakHashMap<String, BeanFactory>();
 
 		public static void registerBeanFactory(final String beanFactoryContextName,
 																					 final BeanFactory beanFactory) {
 				if (beanFactory == null || StringUtils.isBlank(beanFactoryContextName)) {
 						throw new IllegalArgumentException();
 				}
 				synchronized (beanFactories) {
 						beanFactories.put(beanFactoryContextName, beanFactory);
 				}
 		}
 
 		public static void deregisterBeanFactory(final String beanFactoryContextName) {
 				if (StringUtils.isBlank(beanFactoryContextName)) {
 						throw new IllegalArgumentException();
 				}
 				synchronized (beanFactories) {
 						beanFactories.remove(beanFactoryContextName);
 				}
 		}
 
 		public static BeanFactory getBeanFactorForContext(
 						final String beanFactoryContextName) {
 				return beanFactories.get(beanFactoryContextName);
 		}
 
 		public static void aggregate(Object aggregator) {
 				if (aggregator == null || aggregator.getClass().equals(Object.class)) {
 						return;
 				}
 				Class<? extends Object> aggregatorClass = aggregator.getClass();
 				if (aggregate(aggregatorClass, aggregator)) {
 						return;
 				}
 		}
 
 		private static boolean aggregate(Class<? extends Object> aggregatorClass,
 																		 Object aggregator)
 						throws SecurityException {
 				if (aggregatorClass.equals(Object.class)) {
 						return true;
 				}
 				Class<? extends Object> superClass = aggregatorClass.getSuperclass();
 				if (superClass != null) {
 						aggregate(superClass, aggregator);
 				}
 				Aggregator aggregatorAnnotation =
 									 aggregatorClass.getAnnotation(Aggregator.class);
 				if (aggregatorAnnotation == null ||
 						StringUtils.isBlank(aggregatorAnnotation.contextName())) {
 						return true;
 				}
 				BeanFactory beanFactory =
 										getBeanFactorForContext(aggregatorAnnotation.contextName());
 				if (beanFactory == null) {
 						return true;
 				}
 				Field[] declaredFields = aggregatorClass.getDeclaredFields();
 				for (Field declaredField : declaredFields) {
 						InjectableField injectableField =
 														declaredField.getAnnotation(InjectableField.class);
 						if (injectableField == null) {
 								continue;
 						}
 						String beanName =
 									 StringUtils.isBlank(injectableField.beanName())
 									 ? declaredField.getName() : injectableField.beanName();
 						if (StringUtils.isNotEmpty(beanName)) {
 								try {
 										declaredField.setAccessible(true);
										if (beanFactory.containsBean(beanName)) {
												final Class<?> fieldType =
																			 declaredField.getType();
 												declaredField.set(aggregator, beanFactory.getBean(
 																beanName, fieldType));
 										}
 								}
 								catch (IllegalArgumentException ex) {
 										ex.printStackTrace();
 								}
 								catch (IllegalAccessException ex) {
 										ex.printStackTrace();
 								}
 						}
 				}
 				return false;
 		}
 }

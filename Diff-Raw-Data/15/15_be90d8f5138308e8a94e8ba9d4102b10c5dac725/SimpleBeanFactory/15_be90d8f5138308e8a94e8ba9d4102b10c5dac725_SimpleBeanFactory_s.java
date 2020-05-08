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
 
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This implements a bean factory based on a Map<String, Object>. User provides
  * the map and handles map manipulation while the bean factory simply uses it.
  * @author imyousuf
  * @since 0.1.1
  */
 public class SimpleBeanFactory
 				implements BeanFactory {
 
 		private Map<String, Object> beanContainer;
 
 		public SimpleBeanFactory(Map<String, Object> beanContainer) {
 				if (beanContainer == null) {
 						throw new IllegalArgumentException("Bean container can't be nulL!");
 				}
 				this.beanContainer = beanContainer;
 		}
 
		public boolean containsBean(String beanName)
 						throws IllegalArgumentException {
 				if (StringUtils.isBlank(beanName)) {
 						throw new IllegalArgumentException("Bean Name can not be blank!");
 				}
 				return contains(beanName);
 		}
 
 		public Object getBean(String beanName,
 													Class beanClass)
 						throws IllegalArgumentException {
 				if (StringUtils.isBlank(beanName)) {
 						throw new IllegalArgumentException("Bean Name can not be blank!");
 				}
				if (!containsBean(beanName)) {
 						throw new IllegalArgumentException("No such bean in factory!");
 				}
 				return get(beanName);
 		}
 
 		protected boolean contains(String beanName) {
 				return beanContainer.containsKey(beanName);
 		}
 
 		protected Object get(String beanName) {
 				return beanContainer.get(beanName);
 		}
 }

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
 package com.smartitengineering.util.bean.spring;
 
 import com.smartitengineering.util.bean.*;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A simply adapter for spring bean factory to be used for injection
  * @author imyousuf
  */
 public class SpringBeanFactory
     implements BeanFactory {
 
     private org.springframework.beans.factory.BeanFactory beanFactory;
 
     public SpringBeanFactory(
         org.springframework.beans.factory.BeanFactory beanFactory) {
         if (beanFactory == null) {
             throw new IllegalArgumentException(
                 "Spring bean factory can not be null!");
         }
         this.beanFactory = beanFactory;
     }
 
    public boolean containsBean(String beanName, Class beanClass)
         throws IllegalArgumentException {
         if (StringUtils.isBlank(beanName)) {
             throw new IllegalArgumentException("Bean Name can not be blank!");
         }
         return beanFactory.containsBean(beanName);
     }
 
     public Object getBean(String beanName, Class beanClass)
         throws IllegalArgumentException {
         if (StringUtils.isBlank(beanName)) {
             throw new IllegalArgumentException("Bean Name can not be blank!");
         }
        if (!containsBean(beanName, beanClass)) {
             throw new IllegalArgumentException("No such bean in factory!");
         }
 				if(beanClass == null) {
 						return beanFactory.getBean(beanName);
 				}
 				else {
 						return beanFactory.getBean(beanName, beanClass);
 				}
     }
 }

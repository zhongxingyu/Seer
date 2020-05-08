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
 
 /**
  * A Generic read-only bean factory, for registration and injection purpose.
  * @author imyousuf
  * @version 0.1.1
  */
 public interface BeanFactory {
 
 		/**
 		 * Checks whether the bean is available in the factory of not.
 		 * @param beanName Name of the bean to search with
 		 * @return True iff there is a bean in this name
 		 * @throws java.lang.IllegalArgumentException If beanName is blank
 		 */
		public boolean containsBean(String beanName)
 						throws IllegalArgumentException;
 
 		/**
 		 * Retrieves the bean iff it exists.
 		 * @param beanName Name of the bean to retrieve
 		 * @param beanClass Class expected from the bean factory
 		 * @return The bean with the specified name
 		 * @throws java.lang.IllegalArgumentException if beanName is blank or if
 		 *                                            there exists no such bean,
 		 *                                            i.e., containsBean is false
 		 */
 		public Object getBean(String beanName,
 													Class beanClass)
 						throws IllegalArgumentException;
 }

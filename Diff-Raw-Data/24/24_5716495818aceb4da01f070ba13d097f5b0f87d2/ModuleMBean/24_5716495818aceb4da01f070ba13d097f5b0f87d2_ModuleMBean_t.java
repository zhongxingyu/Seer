 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.jmx.mbean;
 
 import javax.management.MXBean;
 
 import org.openmrs.module.jmx.aop.WithSession;
 import org.openmrs.util.PrivilegeConstants;
 
 /**
  * Management bean interface for a module
  */
 @MXBean
 public interface ModuleMBean {
 	
 	/**
 	 * Gets the name of the module
 	 * @return the name
 	 */
 	public String getName();
 	
 	/**
 	 * Gets the version of the module
 	 * @return the version name
 	 */
 	public String getVersion();
 	
 	/**
 	 * Gets if the module is started
 	 * @return if module is started
 	 */
 	public boolean isStarted();
 	
 	/**
 	 * Starts the module
 	 */
	@WithSession({PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES, PrivilegeConstants.MANAGE_SCHEDULER})
 	public void start();
 	
 	/**
 	 * Stops the module
 	 */
	@WithSession({PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES, PrivilegeConstants.MANAGE_SCHEDULER})
 	public void stop();
 }

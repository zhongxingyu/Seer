 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.runtime.adaptor;
 
 import java.util.*;
 
 /**
  * Interface used as an entry to the IPluginConverter    
  */
 public interface IPluginInfo {
 	public Map getLibraries();
 
 	public String[] getLibrariesName();
 
 	public ArrayList getRequires();
 
 	public String getMasterId();
 
 	public String getMasterVersion();
 
 	public String getPluginClass();
 
 	public String getUniqueId();
 
 	public String getVersion();
 
 	public boolean isFragment();
 
 	public Set getPackageFilters();
 
 	public String getPluginName();
 
 	public String getProviderName();
 
 	public boolean isSingleton();
 	
 	String validateForm();
 }

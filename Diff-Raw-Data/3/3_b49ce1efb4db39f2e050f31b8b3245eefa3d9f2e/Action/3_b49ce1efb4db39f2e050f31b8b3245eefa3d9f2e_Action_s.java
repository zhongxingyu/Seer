 /**
  * Copyright (c) 2012 SQLI. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 
 package com.sqli.liferay.imex.core.role.model;
 
 import java.util.HashSet;
 
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 
 public class Action {
 	
 	@Element
 	private String actionId;
 	
 	@ElementList(name="sites", entry="site-name", required=false)
 	private HashSet<String> sitesNames = new HashSet<String>();
 	
 	public String getActionId() {
 		return actionId;
 	}
 	public void setActionId(String actionId) {
 		this.actionId = actionId;
 	}
 	public HashSet<String> getSitesNames() {
 		return sitesNames;
 	}
 	public void setSitesNames(HashSet<String> groupNames) {
 		this.sitesNames = groupNames;
 	}	
 }

 /*******************************************************************************
  * Copyright (c) Feb 28, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.core.connection.data.values;
 
 /**
  * Represents possible issue severities.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public enum IssueSeverity {
 
 	INFO("Info"),
 
 	WARNING("Warning"),
 
	CRITICAL("Critical");
 
 	private final String name;
 
 	private IssueSeverity(String name) {
 		this.name = name;
 	}
 
 	public static IssueSeverity byName(String name) {
 		if (name == null) {
			return null;
 		}
 
 		IssueSeverity[] values = values();
 		for (IssueSeverity issueStatus : values) {
 			if (name.equals(issueStatus.getName())) {
 				return issueStatus;
 			}
 		}
 
		return null;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 }

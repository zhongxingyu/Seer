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
 package org.openmrs.calculation;
 
 import org.openmrs.calculation.definition.ParameterDefinition;
 import org.openmrs.calculation.definition.ParameterDefinitionSet;
 
 /**
  * Simple abstract base class for Calculation to provide an empty ParameterDefinitionSet
  */
public abstract class BaseCalculation {
 	
 	private ParameterDefinitionSet parameterDefinitionSet;
 	
 	/**
 	 * Default Constructor
 	 */
 	public BaseCalculation() {}
 
 	/**
 	 * @return the parameterDefinitions
 	 */
 	public ParameterDefinitionSet getParameterDefinitionSet() {
 		if (parameterDefinitionSet == null) {
 			parameterDefinitionSet = new ParameterDefinitionSet();
 		}
 		return parameterDefinitionSet;
 	}
 
 	/**
 	 * @param parameterDefinitions the parameterDefinitions to set
 	 */
 	public void setParameterDefinitionSet(ParameterDefinitionSet parameterDefinitionSet) {
 		this.parameterDefinitionSet = parameterDefinitionSet;
 	}
 	
 	/**
 	 * @param parameterDefinition the parmaeterDefinition to add
 	 */
 	public void addParameterDefinition(ParameterDefinition parameterDefinitionSet) {
 		getParameterDefinitionSet().add(parameterDefinitionSet);
 	}
 }

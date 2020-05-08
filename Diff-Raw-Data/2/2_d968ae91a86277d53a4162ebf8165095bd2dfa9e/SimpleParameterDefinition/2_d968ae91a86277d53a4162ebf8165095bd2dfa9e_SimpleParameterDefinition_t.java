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
 package org.openmrs.calculation.parameter;
 
 import org.openmrs.util.OpenmrsUtil;
 
 /**
  * A simple implementation of the {@link ParameterDefinition} interface, you can create your own
  * implementations of the interface but this should satisfy most use cases
  */
 public class SimpleParameterDefinition implements ParameterDefinition {
 	
 	/**
 	 * Unique identifier for a calculation and and expected to be a valid java variable name.
 	 */
 	private String key;
 	
 	/**
 	 * The parameter's display label, can be any logical string e.g Start Date
 	 */
 	private String name;
 	
 	/**
 	 * The description for the parameter
 	 */
 	private String description;
 	
 	/**
 	 * The Java class of this parameter
 	 */
 	private String datatype;
 	
 	/**
 	 * Specifies whether the the parameter is nullable or not. If true, this parameter must have a
 	 * non-null value for evaluation to occur
 	 */
 	private boolean required;
 	
 	/**
 	 * Convenience constructor
 	 * 
 	 * @param key the key to set
 	 * @param datatype the datatype to set
 	 * @param name the name to set
 	 * @param required whether this property should be marked as required or not for the evaluation
 	 *            of the associated calculation to occur
 	 * @param description the description to set
 	 */
 	public SimpleParameterDefinition(String key, String datatype, String name, boolean required, String description) {
 		this.key = key;
 		this.datatype = datatype;
 		this.name = name;
 		this.required = required;
 		this.description = description;
 	}
 	
 	/**
 	 * Convenience constructor
 	 * 
 	 * @param key the key to set
 	 * @param datatype the datatype to set
 	 * @param name the name to set
 	 * @param required whether this property should be marked as required or not for the evaluation
 	 *            of the associated calculation to occur
 	 */
 	public SimpleParameterDefinition(String key, String datatype, String name, boolean required) {
 		this.key = key;
 		this.datatype = datatype;
 		this.name = name;
 		this.required = required;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#getKey()
 	 */
 	@Override
 	public String getKey() {
 		return key;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#setKey(java.lang.String)
 	 */
 	@Override
 	public void setKey(String key) {
 		this.key = key;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#getName()
 	 */
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#setName(java.lang.String)
 	 */
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return description;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#setDescription(java.lang.String)
 	 */
 	@Override
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#getDatatype()
 	 */
 	@Override
 	public String getDatatype() {
 		return datatype;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#setDatatype(java.lang.String)
 	 */
 	@Override
 	public void setDatatype(String datatype) {
 		this.datatype = datatype;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#isRequired()
 	 */
 	@Override
 	public boolean isRequired() {
 		return required;
 	}
 	
 	/**
 	 * @see org.openmrs.calculation.parameter.ParameterDefinition#setRequired(boolean)
 	 */
 	@Override
 	public void setRequired(boolean required) {
 		this.required = required;
 	}
 	
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!(obj instanceof ParameterDefinition))
 			return false;
 		ParameterDefinition other = (ParameterDefinition) obj;
 		return OpenmrsUtil.nullSafeEquals(this.getKey(), other.getKey());
 	}
 	
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		if (getKey() == null)
 			return super.hashCode();
 		return getKey().hashCode();
 	}
 	
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		if (getName() == null)
			return super.toString();
 		return getName();
 	}
 	
 }

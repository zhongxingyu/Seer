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
 
 package org.openmrs.module.odkconnector.reporting.metadata;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.BaseOpenmrsMetadata;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 
 public class ExtendedDefinition extends BaseOpenmrsMetadata {
 
 	private static final Log log = LogFactory.getLog(ExtendedDefinition.class);
 
 	public static final String DEFINITION_PROPERTY_FORM = "odkconnector.property.form";
 
 	private Integer id;
 
 	private CohortDefinition cohortDefinition;
 
 	private Set<DefinitionProperty> properties;
 
 	/**
 	 * @return id - The unique Identifier for the object
 	 */
 	@Override
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * @param id - The unique Identifier for the object
 	 */
 	@Override
 	public void setId(final Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Get the cohort cohortDefinition object
 	 *
 	 * @return the cohort cohortDefinition object
 	 */
 	public CohortDefinition getCohortDefinition() {
 		return cohortDefinition;
 	}
 
 	/**
 	 * Set the cohort cohortDefinition object
 	 *
 	 * @param cohortDefinition the cohort cohortDefinition object
 	 */
 	public void setCohortDefinition(final CohortDefinition cohortDefinition) {
 		this.cohortDefinition = cohortDefinition;
 	}
 
 	/**
 	 * Get the properties for the cohort cohortDefinition
 	 *
 	 * @return the properties
 	 */
 	public Set<DefinitionProperty> getProperties() {
 		if (properties == null)
 			return new LinkedHashSet<DefinitionProperty>();
 		return properties;
 	}
 
 	/**
 	 * Search for a certain property from all properties for the extended cohortDefinition
 	 *
 	 * @param property the property
 	 * @return the matching cohortDefinition property element
 	 */
	public Boolean containsProperty(String property) {
 		for (DefinitionProperty definitionProperty : properties) {
 			if (StringUtils.equalsIgnoreCase(definitionProperty.getProperty(), property))
 				return Boolean.TRUE;
 		}
 		return Boolean.FALSE;
 	}
 
 	/**
 	 * Set the properties for the cohort cohortDefinition
 	 *
 	 * @param properties the properties
 	 */
 	public void setProperties(final Set<DefinitionProperty> properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * Add a cohortDefinition property to the extended cohortDefinition
 	 *
 	 * @param definitionProperty the cohortDefinition property
 	 */
 	public void addDefinitionProperty(final DefinitionProperty definitionProperty) {
 		definitionProperty.setExtendedDefinition(this);
 		getProperties().add(definitionProperty);
 	}
 }

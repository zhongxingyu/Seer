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
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.api.APIException;
 import org.openmrs.api.context.Context;
 
 /**
  * Implementation of {@link CalculationProvider} which retrieves a {@link Calculation}
  * given a fully qualified class name of the {@link Calculation} to instantiate
  */
 public class ClasspathCalculationProvider implements CalculationProvider {
 	
 	/**
 	 * @see CalculationProvider#getCalculation(String, String)
 	 * 
 	 * @should retrieve a configured configurable calculation with a valid configuration string
 	 * @should retrieve a non configurable calculation with a null configuration string
 	 * @should throw an exception if a configurable calculation is passed an illegal configuration
 	 * @should throw an exception if a non configurable calculation is passed a configuration string
 	 */
 	public Calculation getCalculation(String calculationName, String configuration) throws InvalidCalculationException {
 		Calculation calculation = null;
 		try {
 			Class<?> c = Context.loadClass(calculationName);
 			calculation = (Calculation)c.newInstance();
 		}
 		catch (Exception e) {
 			throw new APIException("Unable to load Calculation class with name '" + calculationName + "'");
 		}
 		// If this is a ConfigurableCalculation, try to configure it
 		if (calculation instanceof ConfigurableCalculation) {
 			((ConfigurableCalculation)calculation).setConfiguration(configuration);
 		}
 		// If this is not a ConfigurableCalculation, but a configuration was passed in, throw an Exception
 		else {
 			if (StringUtils.isNotBlank(configuration)) {
 				throw new InvalidCalculationException(this, calculationName, configuration);
 			}
 		}
 		return calculation;
 	}
 }

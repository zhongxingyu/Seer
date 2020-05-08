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
 package org.openmrs.calculation.util;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.openmrs.api.context.Context;
 import org.openmrs.calculation.Calculation;
 import org.openmrs.calculation.InvalidCalculationException;
 import org.openmrs.calculation.TokenRegistration;
 import org.openmrs.calculation.provider.CalculationProvider;
 
 /**
  * Contains utility methods for the module
  */
 public class CalculationUtil {
 	
 	//private static final Log log = LogFactory.getLog(CalculationUtil.class);
 	
 	private static final Class<?>[] PRIMITIVE_TYPES = { Boolean.class, Character.class, Byte.class, Short.class,
 	        Integer.class, Float.class, Double.class, Long.class };
 	
 	/**
 	 * Checks if the specified type is a wrapper class for a primitive type
 	 * 
 	 * @param clazz the class to check
 	 * @return true if the class is a wrapper class for a primitive type otherwise false
 	 * @should return true for primitive type wrappers classes
 	 */
 	public static boolean isPrimitiveWrapperType(Class<?> clazz) {
 		return ArrayUtils.contains(PRIMITIVE_TYPES, clazz);
 	}
 	
 	/**
 	 * Checks if the specified class name is for a wrapper class for a primitive type
 	 * 
 	 * @param className the class name to check
 	 * @return true if the class name is for a wrapper class for a primitive type otherwise false
 	 * @should return true for primitive type wrapper class names
 	 */
 	public static boolean isPrimitiveWrapperClassName(String className) {
 		if (className != null) {
 			for (Class<?> type : PRIMITIVE_TYPES) {
 				if (className.equals(type.getName()))
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Utility method that constructs a Calculation instance from a TokenRegistration instance
 	 * @param tokenRegistration
 	 * @return the Calculation represented by the passed TokenRegistration
 	 * @throws InvalidCalculationException if the TokenRegistration is invalid
 	 */
	public static Calculation getCalcuationForTokenRegistration(TokenRegistration tokenRegistration) throws InvalidCalculationException {
 		Calculation c = null;
 		if (tokenRegistration != null) {
 			CalculationProvider calculationProvider = null;
 			try {
 				Class<?> providerClass = Context.loadClass(tokenRegistration.getProviderClassName());
 				calculationProvider = (CalculationProvider) providerClass.newInstance();
 			}
 			catch (Exception e) {
 				String msg = "Unable to instantiate CalculationProvider:" +  tokenRegistration.getProviderClassName();
 				throw new InvalidCalculationException(msg, e);
 			}
 			String calcName = tokenRegistration.getCalculationName();
 			String config = tokenRegistration.getConfiguration();
 			c = calculationProvider.getCalculation(calcName, config);
 		}
 		return c;
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
 /**
  * Meta-data processing type representing an length attribute value runtime type.
  * Does not support min and max values from MD.  Length must be positive.
  * that implements IValidValues, IDefaultValue, IValidELValues.
  */
 public class LengthType extends IntegerType {	
 	private static final String INVALID_LENGTH = Messages.LengthType_invalid_integer;
 	/**
 	 * Type coercion according to JSP 2.0 spec: JSP.1.14.2.1 Conversions from String values
 	 * Allows a percentage.
 	 * @see org.eclipse.jst.jsf.metadataprocessors.features.IValidValues#isValidValue(java.lang.String)
 	 **/
 	public boolean isValidValue(String value) {	
 		if (value == null) return true;
 		String aValue = stripPercentIfPresent(value);		
 		try {
			int anInt = Integer.valueOf(aValue).intValue();
 			if (anInt < 0)
 				addNewValidationMessage(INVALID_LENGTH);
 			
 			return getValidationMessages().isEmpty();
 		} catch (NumberFormatException e) {
 			addNewValidationMessage(INVALID_LENGTH);
 			return false;
 		}
 
 	}
 	//will strip '%' if at end of string.  If string is only '%', then will return empty which will be invalid.
 	private String stripPercentIfPresent(String value) {
 		//"%" is allowed at end
 		if (value.length() > 0
 			&& value.lastIndexOf('%') == value.length() - 1)
 				return value.replaceFirst("%","");
 		
 		return value;
 	}
 
 }

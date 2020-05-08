 /*******************************************************************************
 * Copyright (c) 2009 Intuit, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Intuit, Inc - initial API and implementation
 *******************************************************************************/
 package com.intuit.tools.imat.validators;
 
 import com.beust.jcommander.IParameterValidator;
 import com.beust.jcommander.ParameterException;
 
 /**
  * @author rpfeffer
  * @dateCreated Dec 2, 2011
  *
  * //TODO Explain why this file exists and how it is used.
  *
  */
 public class InstrumentsVersionValidator implements IParameterValidator {
 
 	private static final String MIN_INSTRUMENTS_VERSION = "4.2";
 	
 	/* (non-Javadoc)
 	 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
 	 */
 	public void validate(String name, String value) throws ParameterException {
		if (value == null || value.equals("")) {
			throw new ParameterException("IMAT could not find instruments on " +
					"your system. Please install the latest version of XCode.");
		}
 		if (value.compareTo(MIN_INSTRUMENTS_VERSION) < 0) {
 			throw new ParameterException("The minimum supported version of " +
 					"instruments is "+ MIN_INSTRUMENTS_VERSION + ". The installed " +
 					"version you are currently using is " + value);
 		}
 	}
 
 }

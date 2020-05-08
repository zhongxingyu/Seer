 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core.internal.utils;
 
 import org.eclipse.osgi.util.NLS;
 
 public class ValidationMessages extends NLS {
 	private static final String BUNDLE_NAME = ValidationMessages.class.getName(); //$NON-NLS-1$
 
 	public static String INVALID_CONTEXT_ANNOTATION;
 	
 	public static String INVALID_PATHPARAM_VALUE;
	
	public static String LINE_NUMBER;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, ValidationMessages.class);
 	}
 }

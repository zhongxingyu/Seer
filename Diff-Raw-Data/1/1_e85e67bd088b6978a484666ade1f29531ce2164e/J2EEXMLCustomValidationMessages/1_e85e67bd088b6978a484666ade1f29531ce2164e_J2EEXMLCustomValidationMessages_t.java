 /*
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *   IBM - Initial API and implementation
  * 
  */
 package org.eclipse.jst.j2ee.core.internal.validation.xmlerrorcustomization;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Strings used by XML Validation
  */
 public class J2EEXMLCustomValidationMessages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.jst.j2ee.core.internal.validation.xmlerrorcustomization.j2eexmlcustomvalidation";//$NON-NLS-1$
 
 	public static String J2EE_APPLICATION_ONE_OR_MORE_MODULES;
	public static String EJB_ONE_OR_MORE_BEANS;
 
 	static {
 		// load message values from bundle file
 		NLS.initializeMessages(BUNDLE_NAME, J2EEXMLCustomValidationMessages.class);
 	}
 
 	private J2EEXMLCustomValidationMessages() {
 		// cannot create new instance
 	}
 }

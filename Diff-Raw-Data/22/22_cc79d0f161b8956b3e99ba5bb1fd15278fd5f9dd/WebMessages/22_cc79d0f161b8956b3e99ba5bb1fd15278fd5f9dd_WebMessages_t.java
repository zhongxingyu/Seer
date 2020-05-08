 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Jan 13, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import org.eclipse.osgi.util.NLS;
 
 
 /**
  * @author jialin
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebMessages extends NLS {
 	private static final String BUNDLE_NAME = "web";//$NON-NLS-1$
 
 	private WebMessages() {
 		// Do not instantiate
 	}
 
 	public static String ERR_JAR_NAME_EMPTY;
 	public static String ERR_JAR_NAME_EXIST;
 	public static String ERR_JAVA_PROJECT_NAME_EMPTY;
 	public static String ERR_JAVA_PROJECT_NAME_EXIST;
 	public static String ERR_DUPLICATED_INIT_PARAMETER;
 	public static String ERR_DUPLICATED_URL_MAPPING;
 
 	public static String ERR_SERVLET_MAPPING_URL_PATTERN_EXIST;
 	public static String KEY_3;
 	public static String KEY_4;
 	public static String KEY_5;
 	public static String KEY_6;
 	public static String ERR_FILTER_PARAMETER_NAME_EXIST;
 	public static String ERR_FILTER_MAPPING_SERVLET_EXIST;
 	public static String ERR_FILTER_MAPPING_SERVLET_DISPATCHER_TYPES_EMPTY;
 	public static String ERR_SERVLET_MAPPING_URL_PATTERN_EMPTY;
 	public static String ERR_URL_MAPPING_LIST_EMPTY;
 	public static String ERR_URL_PATTERN_INVALID;
 	public static String ERR_FILTER_MAPPING_EMPTY;
 	public static String ERR_DISPLAY_NAME_EMPTY;
 	public static String ERR_SERVLET_CLASS_NAME_USED;
 	public static String ERR_SERVLET_JAVA_CLASS_NAME_INVALID;
 	public static String ERR_SERVLET_MAPPING_SERVLET_NOT_EXIST;
 	public static String ERR_SERVLET_NAME_EXIST;
 	public static String ERR_SERVLET_DISPLAY_NAME_EXIST;
 	public static String ERR_SERVLET_PARAMETER_NAME_EMPTY;
 	public static String ERR_SERVLET_PARAMETER_NAME_EXIST;
 	public static String ERR_SERVLET_PARAMETER_VALUE_EMPTY;
 	public static String ERR_SERVLET_PARAMETER_VALUE_EXIST;
 	public static String ERR_SERVLET_ROLE_REF_NAME_EMPTY;
 	public static String ERR_SERVLET_ROLE_REF_NAME_EXIST;
 	public static String ERR_SERVLET_ROLE_LINK_EMPTY;
 	public static String ERR_SERVLET_INTERFACE;
 
	public static String ERR_FILTER_NAME_EXIST;
 	public static String ERR_FILTER_CLASS_NAME_USED;
 	public static String ERR_FILTER_CLASS_NAME_INVALID;
 	public static String ERR_LISTENER_DISPLAY_NAME_EXIST;
 	public static String ERR_LISTENER_CLASS_NAME_USED;
 	public static String ERR_LISTENER_CLASS_NAME_INVALID;
 
 	public static String ERR_PAGE_ENCODING_EMPTY;
 	public static String ERR_PAGE_ENCODING_EXIST;
 	public static String ERR_URL_PATTERN_EXIST;
 	public static String ERR_INCLUDE_PRELUDES_EXIST;
 	public static String ERR_INCLUDE_CODAS_EXIST;
 	public static String ERR_SECURITY_CONSTRAINT_NAME_EMPTY;
 	public static String ERR_SECURITY_CONSTRAINT_NAME_EXIST;
 	public static String ERR_CONTEXT_PARAMETER_NAME_EMPTY;
 	public static String ERR_CONTEXT_PARAMETER_NAME_EXIST;
 	public static String ERR_RESOURCE_NAME_EMPTY;
 	public static String ERR_RESOURCE_NAME_EXIST;
 	public static String ERR_URL_PATTERNS_EMPTY;
 	public static String ERR_MIME_MAPPING_EXTENSION_EMPTY;
 	public static String ERR_MIME_MAPPING_EXTENSION_EXIST;
 	public static String ERR_WELCOME_PAGE_EMPTY;
 	public static String ERR_WELCOME_PAGE_EXIST;
 	public static String ERR_ENV_ENTRY_NAME_EMPTY;
 	public static String ERR_ENV_ENTRY_NAME_EXIST;
 	public static String ERR_ERROR_CODE_EMPTY;
 	public static String ERR_ERROR_CODE_MUST_BE_3_DIGITS;
 	public static String ERR_ERROR_PAGE_LOCATION_EMPTY;
 	public static String ERR_ERROR_PAGE_LOCATION_EXIST;
 	public static String ERR_ERROR_PAGE_LOCATION_MUST_START_WITH_SLASH;
 	public static String ERR_EXCEPTION_TYPE_EMPTY;
 	public static String ERR_EXCEPTION_SECURITY_ID_NO_BEANS;
 	public static String ERR_EXCEPTION_ROLE_NAME_EMPTY;
 	public static String ERR_LOCALE_NAME_EMPTY;
 	public static String ERR_LOCALE_NAME_EXIST;
 	public static String ERR_TAGLIBREF_URL_EMPTY;
 	public static String ERR_TAGLIBREF_URL_EXIST;
 	public static String ERR_MESSAGE_DESTINATION_NAME_EMPTY;
 	public static String ERR_MESSAGE_DESTINATION_NAME_EXIST;
 	public static String ERR_EXCEPTION_METHOD_ELEMENT_EMPTY;
 	public static String ERR_EXCEPTION_METHOD_TRANSACTION_ATTRIBUTES_EMPTY;
 	public static String ERR_SERVLET_LIST_EMPTY;
 	public static String ERR_FILTER_LIST_EMPTY;
 	public static String ERR_LISTENER_LIST_EMPTY;
 	public static String ERR_NO_LISTENER_SELECTED;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, WebMessages.class);
 	}
 
 	public static String getResourceString(String key, Object[] args) {
 		return NLS.bind(key, args);
 	}
 }

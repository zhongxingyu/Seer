 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Dec 3, 2003
  *
  * To change the template for this generated file go to
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.common;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.wst.common.frameworks.internal.Messages;
 
 
 /**
  * @author DABERG
  * 
  * To change the template for this generated type comment go to Window>Preferences>Java>Code
  * Generation>Code and Comments
  */
 public class J2EECommonMessages extends Messages {
 	public static final String ERR_SECURITY_ROLE_EMPTY = "ERR_SECURITY_ROLE_EMPTY"; //$NON-NLS-1$
 	public static final String ERR_SECURITY_ROLE_EXIST = "ERR_SECURITY_ROLE_EXIST"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NAME_EMPTY = "ERR_JAVA_CLASS_NAME_EMPTY"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NAME_EXIST = "ERR_JAVA_CLASS_NAME_EXIST"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FILE_NAME_EXIST = "ERR_JAVA_CLASS_FILE_NAME_EXIST"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FOLDER_NAME_EMPTY = "ERR_JAVA_CLASS_FOLDER_NAME_EMPTY"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FOLDER_PROJECT_NOT_OPEN = "ERR_JAVA_CLASS_FOLDER_PROJECT_NOT_OPEN"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FOLDER_NOT_EXIST = "ERR_JAVA_CLASS_FOLDER_NOT_EXIST"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NOT_JAVA_PROJECT = "ERR_JAVA_CLASS_NOT_JAVA_PROJECT"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FOLDER_STATIC_PROJECT = "ERR_JAVA_CLASS_FOLDER_STATIC_PROJECT"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_FOLDER_NOT_SOURCE = "ERR_JAVA_CLASS_FOLDER_NOT_SOURCE"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NAME_QUALIFIED = "ERR_JAVA_CLASS_NAME_QUALIFIED"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NAME_INVALID = "ERR_JAVA_CLASS_NAME_INVALID"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_NAME_WARNING = "ERR_JAVA_CLASS_NAME_WARNING"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_SUPERCLASS_NOT_EXIST = "ERR_JAVA_CLASS_SUPERCLASS_NOT_EXIST"; //$NON-NLS-1$
 	public static final String ERR_JAVA_CLASS_SUPERCLASS_FINAL = "ERR_JAVA_CLASS_SUPERCLASS_FINAL"; //$NON-NLS-1$
 	public static final String ERR_JAVA_PACAKGE_NAME_INVALID = "ERR_JAVA_PACAKGE_NAME_INVALID"; //$NON-NLS-1$
 	public static final String ERR_JAVA_PACKAGE_NAME_WARNING = "ERR_JAVA_PACKAGE_NAME_WARNING"; //$NON-NLS-1$
 	public static final String ERR_BOTH_FINAL_AND_ABSTRACT = "ERR_BOTH_FINAL_AND_ABSTRACT"; //$NON-NLS-1$
 	public static final String ERR_SECURITY_ROLE_LIST_EMPTY = "ERR_SECURITY_ROLE_LIST_EMPTY"; //$NON-NLS-1$
 	public static final String ERR_EMPTY_MODULE_NAME = "ERR_EMPTY_MODULE_NAME"; //$NON-NLS-1$
 	public static final String ERR_EMPTY_PROJECT_NAME = "ERR_EMPTY_PROJECT_NAME"; //$NON-NLS-1$
 
 	public static final String ERR_PROJECT_NAME_EXISTS = "ERR_PROJECT_NAME_EXISTS"; //$NON-NLS-1$
 	public static final String ERR_VERSION_NOT_SUPPORTED ="ERR_VERSION_NOT_SUPPORTED";//$NON-NLS-1$
 	public static final String ERR_NOT_SUPPORTED="ERR_NOT_SUPPORTED";//$NON-NLS-1$	
 		
 
 	private static final J2EECommonMessages INSTANCE = new J2EECommonMessages();
 
 	/**
 	 * Returns the string from the resource bundle, or 'key' if not found.
 	 */
 	public static String getResourceString(String key) {
 		return INSTANCE.doGetResourceString(key);
 	}
 
 	public static String getResourceString(String key, Object[] args) {
 		return INSTANCE.doGetResourceString(key, args);
 	}
 
 	protected J2EECommonMessages() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.Messages#initializeBundle()
 	 */
 	protected void initializeBundle() {
 		try {
 			resourceBundle = ResourceBundle.getBundle("j2ee_common"); //$NON-NLS-1$
 		} catch (MissingResourceException x) {
 		}
 	}
 }

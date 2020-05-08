 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 
 package org.jboss.tools.ws.ui.messages;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Grid Qian
  */
 public final class JbossWSUIMessages extends NLS {
 
 	private static final String BUNDLE_NAME = 
			"org.jboss.tools.ws.ui.JbossWSUI";		//$NON-NLS-1$
 
 	private JbossWSUIMessages() {
 		// Do not instantiate
 	}
 
 	public static String LABEL_BROUSE;
 	public static String LABEL_WEB_SERVICE_CODEGEN;
 	public static String LABEL_WEB_SERVICE_CLIENT_CODEGEN;
 	public static String LABEL_WEB_SERVICE_AAR;
 	public static String LABEL_AAR_EXTENTION;
 	public static String LABEL_DATABINDING;
 	public static String LABEL_JBOSSWS_RUNTIME_LOAD;
 	public static String LABEL_JBOSSWS_RUNTIME_LOAD_ERROR;
 	public static String LABEL_JBOSSWS_RUNTIME_NOT_EXIT;
 	public static String LABEL_GENERATE_TESTCASE_CAPTION;
 	public static String LABEL_GENERATE_SERVERSIDE_INTERFACE;
 	public static String LABEL_GENERATE_ALL;
 	public static String LABEL_CLIENT_SIDE;
 	public static String LABEL_SYNC_AND_ASYNC;
 	public static String LABEL_SYNC;
 	public static String LABEL_ASYNC;
 	
 	public static String JBOSSWS_LOCATION;
 	public static String JBOSSWS_RUNTIME;
 	public static String JBOSSWS_PREFERENCES;
 	public static String JBOSSWS_RUNTIME_TOOLTIP;
 	public static String JBOSSWS_PREFERENCES_TOOLTIP;
 	public static String JBOSSWS_RUNTIME_LOCATION;
 	public static String JBOSSWS_RUNTIME_PREFERENCES;
 
 	public static String ERROR_INVALID_JBOSSWS_SERVER_LOCATION;
 	public static String ERROR_INVALID_FILE_READ_WRITEL;
 	public static String ERROR_SERVER_IS_NOT_SET;
 
 	public static String PROGRESS_INSTALL_JBOSSWS_RUNTIME;
 	public static String PROGRESS_UNINSTALL_JBOSSWS_RUNTIME;
 
 	//generate for jbossws lib
 	public static String DIR_DOT_METADATA;
 	public static String DIR_DOT_PLUGINS;
 	public static String TEMP_JBOSSWS_FACET_DIR;
 	public static String WEBAPP_EXPLODED_SERVER_LOCATION_FILE;
 	public static String SERVER_STATUS_LOCATION_FILE;
 	public static String WAR_STATUS_LOCATION_FILE;
 	
 	public static String BIN;
 	public static String COMMOND;
 	
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, JbossWSUIMessages.class);
 	}
 }

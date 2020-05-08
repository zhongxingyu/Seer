 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.core.internal.rse;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.dltk.core.internal.rse.messages"; //$NON-NLS-1$
 	public static String RSEEnvironment_EmptyFileNameError;
 	public static String RSEEnvironment_EnvironmentNameSuffix;
 	public static String RSEEnvironmentProvider_providerName;
 	public static String RSEExecEnvironment_hostNotFound;
 	public static String RSEExecEnvironment_ErrorConnecting;
 	public static String RSEExecEnvironment_ErrorRunningCommand;
	public static String RSEExecEnvironment_fetchEnvVars;
 	public static String RSEExecEnvironment_ProcessCreateError;
 	public static String RSEExecEnvironment_LauncherUploadError;
 	public static String RSEExecEnvironment_NoFileServicerError;
 	public static String RSEExecEnvironment_NoShellService;
 	public static String RSEExecEnvironment_NotConnected;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }

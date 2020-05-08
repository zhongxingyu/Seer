 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.core.scripting.nls;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Target Explorer TCF processes extensions core plug-in externalized strings management.
  */
 public class Messages extends NLS {
 
 	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.scripting.core.nls.Messages"; //$NON-NLS-1$
 
 	/**
 	 * Static constructor.
 	 */
 	static {
 		// Load message values from bundle file
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	// **** Declare externalized string id's down here *****
 
 	public static String ScriptLauncher_error_channelConnectFailed;
 	public static String ScriptLauncher_error_channelNotConnected;
 	public static String ScriptLauncher_error_missingScript;
 	public static String ScriptLauncher_error_illegalNullArgument;
 	public static String ScriptLauncher_error_illegalIndex;
 	public static String ScriptLauncher_error_parsingScript;
 	public static String ScriptLauncher_error_executionFailed;
 }

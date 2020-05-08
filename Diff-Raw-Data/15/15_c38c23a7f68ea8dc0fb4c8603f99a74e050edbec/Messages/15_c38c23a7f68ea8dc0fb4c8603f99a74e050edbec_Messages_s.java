 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.operations;
 
 import org.eclipse.osgi.util.NLS;
 
 class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.epp.internal.mpc.ui.operations.messages"; //$NON-NLS-1$
 
 	public static String ProvisioningOperation_commaSeparator;
 
 	public static String ProvisioningOperation_configuringProvisioningOperation;
 
 	public static String ProvisioningOperation_nothingToUpdate;
 
 	public static String ProvisioningOperation_proceedQuestion;
 
	public static String ProvisioningOperation_unavailableConnectors;
 
	public static String ProvisioningOperation_unavailableConnectors_proceedQuestion;
 
 	public static String ProvisioningOperation_unavailableFeatures;
 
 	public static String ProvisioningOperation_unexpectedErrorUrl;
 
 	public static String ResolveFeatureNamesOperation_resolvingFeatures;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }

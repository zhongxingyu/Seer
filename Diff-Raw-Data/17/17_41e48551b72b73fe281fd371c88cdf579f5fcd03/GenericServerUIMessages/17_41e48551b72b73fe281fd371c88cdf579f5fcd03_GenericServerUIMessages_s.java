 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.ui.internal;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Helper class to get messages
  * 
  * @author Gorkem Ercan
  */
 public class GenericServerUIMessages extends NLS{
 
 	private static final String RESOURCE_BUNDLE= "org.eclipse.jst.server.generic.ui.internal.GenericServerUIMessages";//$NON-NLS-1$
 	public static String serverTypeGroup_label_browse;
 	public static String runtimeName;
 	public static String runtimeWizardDescription;
 	public static String runtimeWizardTitle;
 	public static String serverName;
 	public static String serverWizardDescription;
 	public static String serverWizardTitle;
 	public static String installed_jre_link;
 	public static String jre_select_label;
 	public static String defaultJRE;
 	public static String invalidPath;
 	
 	static{
 		  NLS.initializeMessages(RESOURCE_BUNDLE, GenericServerUIMessages.class);
 	}
 
 }

 /*******************************************************************************
  * Copyright (c) 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.deployment.debug.core;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
 
 	private static final String BUNDLE_NAME = "org.zend.php.zendserver.deployment.debug.core.messages"; //$NON-NLS-1$
 
 	public static String deploymentJob_Title;
 	public static String statusJob_Title;
 	public static String updateJob_Title;
 	public static String ExisitngAppIdJob_JobTitle;
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }

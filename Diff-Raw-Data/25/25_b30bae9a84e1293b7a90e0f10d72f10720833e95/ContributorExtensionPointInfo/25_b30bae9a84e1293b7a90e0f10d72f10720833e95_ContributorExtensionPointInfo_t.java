/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 package org.eclipse.jem.internal.proxy.core;
 
 import java.util.Map;
 
 
 /**
  * Result from {@link ProxyPlugin#processContributionExtensionPoint(String)}
  * 
  * @since 1.0.0
  */
 public class ContributorExtensionPointInfo {
 	
 	/**
 	 * Contributions that are based upon container paths that are built up from
 	 * the extension point.
 	 * 
 	 * @since 1.2.0
 	 */
 	public ContainerPathContributionMapping containerPathContributions;
 
 	ContributorExtensionPointInfo() {
 		// Not meant to be instantiated or subclassed outside of ProxyPlugin.
 	}
 	
 	/**
 	 * Map of plugin ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each plugin,
 	 * the contributions will be listed in plugin prereq order.
 	 */
 	public Map pluginToContributions;
 
 	
 }

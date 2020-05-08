 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors:
  * wesendon
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.provider;
 
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.ESServer;
import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 
 /**
  * This provider allows to set the default {@link ESServer} and initialize the {@link ESKeyStoreManager} with necessary
  * certificates.
  * 
  * @author wesendon
  */
 public interface ESClientConfigurationProvider {
 
 	/**
 	 * Returns a list of default {@link ESServer}s.
	 * This method is called during workspace initialization, i.e. you must <b>NOT</b>
	 * use {@link ESWorkspaceProvider#INSTANCE} in the scope of this method. This
	 * means you must not create a session while creating the default server information.
 	 * 
 	 * @return a list of default server entries
 	 */
 	List<ESServer> getDefaultServerInfos();
 
 	/**
 	 * Allows the {@link ESClientConfigurationProvider} to initialize the {@link ESKeyStoreManager}. Use
 	 * {@link ESKeyStoreManager#setDefaultCertificate(String)} to set the default alias and
 	 * {@link ESKeyStoreManager#addCertificate(String, java.io.InputStream)} to add you certificate.
 	 * 
 	 * @param keyStoreManager
 	 *            the {@link ESKeyStoreManager} to be initialized
 	 */
 	void initDefaultCertificates(ESKeyStoreManager keyStoreManager);
 
 }

 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.collab.cdo.repository;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
 import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
 import org.eclipse.emf.ecore.EPackage.Registry;
 import org.eclipse.mylyn.docs.intent.collab.cdo.adapters.CDOAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.net4j.Net4jUtil;
 import org.eclipse.net4j.channel.ChannelException;
 import org.eclipse.net4j.connector.IConnector;
 import org.eclipse.net4j.tcp.TCPUtil;
 import org.eclipse.net4j.util.concurrent.TimeoutRuntimeException;
 import org.eclipse.net4j.util.container.ContainerUtil;
 import org.eclipse.net4j.util.container.IManagedContainer;
 
 /**
  * Representation of a CDO Repository.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class CDORepository implements Repository {
 
 	/**
 	 * Connector to the repository.
 	 */
 	private static IConnector connector;
 
 	/**
 	 * Container containing the connection.
 	 */
 	private static IManagedContainer container;
 
 	/**
 	 * SessionConfiguration for the CDO repository (concrete notion).
 	 */
	private static CDONet4jSessionConfiguration cdoSessionConfiguration;
 
 	/**
 	 * Current session connected to the repository.
 	 */
	private static CDONet4jSession session;
 
 	/**
 	 * List of the active repositories (while not empty, we can't close the session).
 	 */
 	private static Set<CDORepository> activeRepositories = new LinkedHashSet<CDORepository>();
 
 	/**
 	 * Configuration of this CDORepository (abstract notion).
 	 */
 	private CDOConfig repositoryConfiguration;
 
 	/**
 	 * Registry containing all the clients currently subscribed to this repository.
 	 */
 	private Set<RepositoryClient> clientRegistry;
 
 	/**
 	 * CDORepository constructor.
 	 * 
 	 * @param configuration
 	 *            configuration of this CDORepository.
 	 */
 	public CDORepository(CDOConfig configuration) {
 		this.repositoryConfiguration = configuration;
 		this.clientRegistry = new LinkedHashSet<RepositoryClient>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#register(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient)
 	 */
 	public void register(RepositoryClient client) {
 		this.clientRegistry.add(client);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#unregister(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient)
 	 */
 	public void unregister(RepositoryClient client) {
 		this.clientRegistry.remove(client);
 		if (this.clientRegistry.isEmpty()) {
 			closeSession();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws RepositoryConnectionException
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#getOrCreateSession()
 	 */
 	public Object getOrCreateSession() throws RepositoryConnectionException {
 
 		// If no configuration has been created yet
 		if (cdoSessionConfiguration == null) {
 			// We create this configuration
 			container = ContainerUtil.createContainer();
 
 			Net4jUtil.prepareContainer(container); // Register Net4j factories
 			TCPUtil.prepareContainer(container); // Register TCP factories
 			CDONet4jUtil.prepareContainer(container); // Register CDO factories
 			container.activate();
 
 			// Create connector
 			connector = TCPUtil.getConnector(container, repositoryConfiguration.getServerAdress());
 
 			// Create configuration
			cdoSessionConfiguration = CDONet4jUtil.createNet4jSessionConfiguration();
 			cdoSessionConfiguration.setConnector(connector);
 			cdoSessionConfiguration.setRepositoryName(repositoryConfiguration.getRepositoryName());
 
 		}
 
 		// If no session is currently open
 		if ((session == null) || session.isClosed()) {
 			// Open session
 			try {
				session = cdoSessionConfiguration.openNet4jSession();
 			} catch (TimeoutRuntimeException tre) {
 				throw new RepositoryConnectionException(tre.getMessage());
 			} catch (ChannelException ce) {
 				throw new RepositoryConnectionException(ce.getMessage());
 			}
 		}
 
 		// We register the current instance as an active repository, as it has openned a section
 		if (!activeRepositories.contains(this)) {
 			activeRepositories.add(this);
 		}
 
 		return session;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#closeSession()
 	 */
 	public void closeSession() {
 
 		// We unregister the current instance of the active repositories list
 		activeRepositories.remove(this);
 
 		// If there is no active repository
 		if (activeRepositories.isEmpty()) {
 
 			// If the session needs to be closed
 			if (session != null) {
 				session.close();
 				session = null;
 				connector.close();
 				connector = null;
 				container.deactivate();
 				container = null;
 				cdoSessionConfiguration = null;
 			}
 		}
 	}
 
 	/**
 	 * Returns the CDOConfiguration associated to this repository.
 	 * 
 	 * @return the CDOConfiguration associated to this repository
 	 */
 	public CDOConfig getConfiguration() {
 		return this.repositoryConfiguration;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#getPackageRegistry()
 	 */
 	public Registry getPackageRegistry() throws RepositoryConnectionException {
		return ((CDONet4jSession)getOrCreateSession()).getPackageRegistry();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#createRepositoryAdapter()
 	 */
 	public RepositoryAdapter createRepositoryAdapter() {
 		try {
			return new CDOAdapter((CDONet4jSession)getOrCreateSession());
 		} catch (RepositoryConnectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.repository.Repository#setRepositoryStructurer(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer)
 	 */
 	public void setRepositoryStructurer(RepositoryStructurer structurer) {
 		// TODO Auto-generated method stub
 	}
 
 }

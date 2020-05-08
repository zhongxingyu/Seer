 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
 * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl.api;
 
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESWorkspace;
 import org.eclipse.emf.emfstore.client.exceptions.ESServerNotFoundException;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.Workspace;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommandWithResult;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.api.AbstractAPIImpl;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 
 /**
  * Mapping between {@link ESWorkspace} and {@link Workspace}.
  * 
  * @author emueller
  * 
  */
 public class ESWorkspaceImpl extends AbstractAPIImpl<ESWorkspaceImpl, Workspace> implements ESWorkspace {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param workspace
 	 *            the internal delegate
 	 */
 	public ESWorkspaceImpl(Workspace workspace) {
 		super(workspace);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#getLocalProjects()
 	 */
 	public List<ESLocalProject> getLocalProjects() {
 		return APIUtil.mapToAPI(ESLocalProject.class, toInternalAPI().getProjectSpaces());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#createLocalProject(java.lang.String)
 	 */
 	public ESLocalProjectImpl createLocalProject(final String projectName) {
 		return new EMFStoreCommandWithResult<ESLocalProjectImpl>() {
 			@Override
 			protected ESLocalProjectImpl doRun() {
 				ProjectSpace projectSpace = toInternalAPI().createLocalProject(projectName);
 				return projectSpace.toAPI();
 			}
 		}.run(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#getServers()
 	 */
 	public List<ESServer> getServers() {
 		return APIUtil.mapToAPI(ESServer.class, toInternalAPI().getServerInfos());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#addServer(org.eclipse.emf.emfstore.client.ESServer)
 	 */
 	public ESServerImpl addServer(ESServer server) {
 
 		final ESServerImpl serverImpl = (ESServerImpl) server;
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().addServerInfo(serverImpl.toInternalAPI());
 				return null;
 			}
 		});
 
 		toInternalAPI().save();
 
 		return serverImpl;
 	}
 
 	public boolean serverExists(ESServer server) {
 		ESServer existingServer = getExistingServer(server);
 		return existingServer != null;
 	}
 
 	/**
 	 * Returns the server with the same name, URL and port as the given one, if there's any.
 	 * 
 	 * @param server
 	 *            the server for which to retrieve an already existing server instance
 	 * @return the server with the same URL and port as the given one, or <code>null</code> if no such server exists.
 	 */
 	private ESServer getExistingServer(ESServer server) {
 
 		for (ESServer s : getServers()) {
 			if (s == server) {
 				return s;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#removeServer(org.eclipse.emf.emfstore.client.ESServer)
 	 */
 	public void removeServer(final ESServer server) throws ESServerNotFoundException {
 		final ESServerImpl serverImpl = (ESServerImpl) server;
 		final ESServerImpl existingServer = (ESServerImpl) getExistingServer(serverImpl);
 
 		if (existingServer == null) {
 			throw new ESServerNotFoundException(MessageFormat.format(
 				"The server {0} could not be found", server));
 		}
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				toInternalAPI().removeServerInfo(existingServer.toInternalAPI());
 				return null;
 			}
 		});
 		toInternalAPI().save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#getLocalProject(org.eclipse.emf.ecore.EObject)
 	 */
 	public ESLocalProject getLocalProject(final EObject modelElement) {
 		return RunESCommand.runWithResult(new Callable<ESLocalProject>() {
 			public ESLocalProject call() throws Exception {
 				Project project = ModelUtil.getProject(modelElement);
 				ProjectSpace projectSpace = toInternalAPI().getProjectSpace(project);
 				return projectSpace.toAPI();
 			}
 		});
 	}
 
 }

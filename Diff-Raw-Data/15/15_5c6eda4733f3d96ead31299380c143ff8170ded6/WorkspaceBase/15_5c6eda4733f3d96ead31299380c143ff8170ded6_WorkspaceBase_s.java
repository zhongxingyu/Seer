 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Maximilian Koegel, Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.emfstore.internal.client.importexport.impl.ExportProjectSpaceController;
 import org.eclipse.emf.emfstore.internal.client.importexport.impl.ExportWorkspaceController;
 import org.eclipse.emf.emfstore.internal.client.model.AdminBroker;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.Workspace;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ProjectUrlResolutionException;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ServerUrlResolutionException;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.UnkownProjectException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESWorkspaceImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.ResourceHelper;
 import org.eclipse.emf.emfstore.internal.client.observers.DeleteProjectSpaceObserver;
 import org.eclipse.emf.emfstore.internal.common.ESDisposable;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.model.url.ProjectUrlFragment;
 import org.eclipse.emf.emfstore.internal.server.model.url.ServerUrl;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 
 /**
  * Workspace space base class that contains custom user methods.
  * 
  * @author koegel
  * @author wesendon
  * @author emueller
  * 
  */
 public abstract class WorkspaceBase extends EObjectImpl implements Workspace, ESDisposable,
 	DeleteProjectSpaceObserver {
 
 	private ESWorkspaceImpl apiImplClass;
 
 	/**
 	 * A mapping between project and project spaces.
 	 * 
 	 * @generated NOT
 	 */
 	private Map<Project, ProjectSpace> projectToProjectSpaceMap;
 
 	/**
 	 * The resource set of the workspace.
 	 * 
 	 * @generated NOT
 	 */
 	private ResourceSet workspaceResourceSet;
 
 	// BEGIN OF CUSTOM CODE
 	/**
 	 * Adds a new ProjectSpace to the workspace.
 	 * 
 	 * @param projectSpace
 	 *            The project space to be added
 	 */
 	public void addProjectSpace(ProjectSpace projectSpace) {
 		getProjectSpaces().add(projectSpace);
 		projectToProjectSpaceMap.put(projectSpace.getProject(), projectSpace);
 	}
 
 	/**
 	 * Clone a project.
 	 * 
 	 * @param projectName The name of the new project.
 	 * @param originalProject The project to clone.
 	 * @return The new {@link ProjectSpace} of the cloned {@link Project}.
 	 */
 	public ProjectSpace cloneProject(String projectName, Project originalProject) {
 
 		final ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setProject(ModelUtil.clone(originalProject));
 		projectSpace.setProjectName(projectName);
 
 		projectSpace.initResources(getResourceSet());
 
 		addProjectSpace(projectSpace);
 		save();
 
 		return projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESWorkspace#createLocalProject(java.lang.String)
 	 */
 	public ProjectSpace createLocalProject(String projectName) {
 		return cloneProject(projectName,
 			org.eclipse.emf.emfstore.internal.common.model.ModelFactory.eINSTANCE.createProject());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#init()
 	 */
 	@SuppressWarnings("unchecked")
 	public void init() {
 		projectToProjectSpaceMap = new LinkedHashMap<Project, ProjectSpace>();
 		// initialize all projectSpaces
 		for (final ProjectSpace projectSpace : getProjectSpaces()) {
 			projectSpace.init();
 			projectToProjectSpaceMap.put(projectSpace.getProject(), projectSpace);
 		}
 
 		ESWorkspaceProviderImpl.getObserverBus().register(this, DeleteProjectSpaceObserver.class);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.ESDisposable#dispose()
 	 */
 	public void dispose() {
 		for (final ProjectSpace projectSpace : getProjectSpaces()) {
 			((ProjectSpaceBase) projectSpace).dispose();
 		}
 		getServerInfos().clear();
 		getUsersessions().clear();
 		save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#setResourceSet(org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void setResourceSet(ResourceSet resourceSet) {
 		workspaceResourceSet = resourceSet;
 		for (final ProjectSpace projectSpace : getProjectSpaces()) {
 			final ProjectSpaceBase base = (ProjectSpaceBase) projectSpace;
 			base.setResourceSet(workspaceResourceSet);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#updateACUser(org.eclipse.emf.emfstore.internal.client.model.ServerInfo)
 	 */
 	public void updateACUser(ServerInfo serverInfo) throws ESException {
 		new ServerCall<Void>(serverInfo) {
 			@Override
 			protected Void run() throws ESException {
 				getUsersession().setACUser(getConnectionManager().resolveUser(getSessionId(), null));
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#updateACUser(org.eclipse.emf.emfstore.internal.client.model.Usersession)
 	 */
 	public void updateACUser(Usersession usersession) throws ESException {
 		new ServerCall<Void>(usersession) {
 			@Override
 			protected Void run() throws ESException {
 				getUsersession().setACUser(getConnectionManager().resolveUser(getSessionId(), null));
 				return null;
 			}
 		}.execute();
 	}
 
 	// BEGIN OF CUSTOM CODE
 	/**
 	 * {@inheritDoc}
 	 */
 	public ResourceSet getResourceSet() {
 		return workspaceResourceSet;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ProjectSpace importProject(Project project, String name, String description) {
 		final ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setProject(project);
 		projectSpace.setProjectName(name);
 		projectSpace.setProjectDescription(description);
 		projectSpace.initResources(workspaceResourceSet);
 
 		addProjectSpace(projectSpace);
 		save();
 
 		return projectSpace;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#importProject(java.lang.String)
 	 */
 	public ProjectSpace importProject(String absoluteFileName) throws IOException {
 		final Project project = ResourceHelper.getElementFromResource(absoluteFileName, Project.class, 0);
 		return importProject(project, absoluteFileName.substring(absoluteFileName.lastIndexOf(File.separatorChar) + 1),
 			"Imported from " + absoluteFileName);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#importProjectSpace(java.lang.String)
 	 */
 	public ProjectSpace importProjectSpace(String absoluteFileName) throws IOException {
 
 		final ProjectSpace projectSpace = ResourceHelper
 			.getElementFromResource(absoluteFileName, ProjectSpace.class, 0);
 
 		projectSpace.initResources(workspaceResourceSet);
 
 		addProjectSpace(projectSpace);
 		save();
 		return projectSpace;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#resolve(org.eclipse.emf.emfstore.internal.server.model.url.ProjectUrlFragment)
 	 */
 	public Set<ProjectSpace> resolve(ProjectUrlFragment projectUrlFragment) throws ProjectUrlResolutionException {
 		final Set<ProjectSpace> result = new LinkedHashSet<ProjectSpace>();
 		for (final ProjectSpace projectSpace : getProjectSpaces()) {
 			if (projectSpace.getProjectId().equals(projectUrlFragment.getProjectId())) {
 				result.add(projectSpace);
 			}
 		}
 		if (result.size() == 0) {
 			throw new ProjectUrlResolutionException();
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#resolve(org.eclipse.emf.emfstore.internal.server.model.url.ServerUrl)
 	 */
 	public Set<ServerInfo> resolve(ServerUrl serverUrl) throws ServerUrlResolutionException {
 		final Set<ServerInfo> result = new LinkedHashSet<ServerInfo>();
 		for (final ServerInfo serverInfo : getServerInfos()) {
 			final boolean matchingHostname = serverInfo.getUrl().equals(serverUrl.getHostName());
 			final boolean matchingPort = serverInfo.getPort() == serverUrl.getPort();
 			if (matchingHostname && matchingPort) {
 				result.add(serverInfo);
 			}
 		}
 		if (result.size() == 0) {
 			throw new ServerUrlResolutionException();
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#save()
 	 */
 	public void save() {
 		try {
 			ModelUtil.saveResource(eResource(), ModelUtil.getResourceLogger());
 		} catch (final IOException e) {
 			// MK Auto-generated catch block
 			// FIXME OW MK: also insert code for dangling href handling here
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#exportProjectSpace(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace,
 	 *      java.io.File)
 	 */
 	public void exportProjectSpace(ProjectSpace projectSpace, File file) throws IOException {
 		new ExportProjectSpaceController(projectSpace).execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#exportProjectSpace(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace,
 	 *      java.io.File, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void exportProjectSpace(ProjectSpace projectSpace, File file, IProgressMonitor progressMonitor)
 		throws IOException {
 		new ExportProjectSpaceController(projectSpace).execute(file, progressMonitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#exportWorkSpace(java.io.File)
 	 */
 	public void exportWorkSpace(File file) throws IOException {
 		new ExportWorkspaceController().execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#exportWorkSpace(java.io.File,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void exportWorkSpace(File file, IProgressMonitor progressMonitor) throws IOException {
 		new ExportWorkspaceController().execute(file, progressMonitor);
 	}
 
 	/**
 	 * {@inheritDoc}<br/>
 	 * <br/>
 	 * This is to enable the workspace to be root of table views.
 	 * 
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 * @generated NOT
 	 */
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#getAdminBroker(org.eclipse.emf.emfstore.internal.client.model.ServerInfo)
 	 */
 	public AdminBroker getAdminBroker(final ServerInfo serverInfo) throws ESException, AccessControlException {
 		return new ServerCall<AdminBroker>(serverInfo) {
 			@Override
 			protected AdminBroker run() throws ESException {
 				return new AdminBrokerImpl(serverInfo, getSessionId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#getAdminBroker(org.eclipse.emf.emfstore.internal.client.model.Usersession)
 	 */
 	public AdminBroker getAdminBroker(final Usersession usersession) throws ESException, AccessControlException {
 		return new ServerCall<AdminBroker>(usersession) {
 			@Override
 			protected AdminBroker run() throws ESException {
 				return new AdminBrokerImpl(usersession.getServerInfo(), getSessionId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#getEditingDomain()
 	 */
 	public EditingDomain getEditingDomain() {
 		return ESWorkspaceProviderImpl.getInstance().getEditingDomain();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#getProjectSpace(org.eclipse.emf.emfstore.internal.common.model.Project)
 	 */
 	public ProjectSpace getProjectSpace(Project project) throws UnkownProjectException {
 		final ProjectSpace projectSpace = projectToProjectSpaceMap.get(project);
 		if (projectSpace == null) {
 			throw new UnkownProjectException();
 		}
 		return projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.observers.DeleteProjectSpaceObserver#projectSpaceDeleted(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace)
 	 */
 	public void projectSpaceDeleted(ProjectSpace projectSpace) {
 		assert projectSpace != null;
 
 		getProjectSpaces().remove(projectSpace);
 		save();
 		projectToProjectSpaceMap.remove(projectSpace.getProject());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#addServerInfo(org.eclipse.emf.emfstore.internal.client.model.ServerInfo)
 	 */
 	public void addServerInfo(ServerInfo serverInfo) {
 		getServerInfos().add(serverInfo);
 		save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.Workspace#removeServerInfo(org.eclipse.emf.emfstore.internal.client.model.ServerInfo)
 	 */
 	public void removeServerInfo(ServerInfo serverInfo) {
 		getServerInfos().remove(serverInfo);
 
 		final List<Usersession> deletables = new ArrayList<Usersession>();
 		for (final Usersession session : getUsersessions()) {
 			if (session.getServerInfo() == serverInfo) {
 				try {
 					session.logout();
 				} catch (final ESException e) {
 					// ignore, will be deleted anyways
 				}
 				deletables.add(session);
 			}
 		}
 
 		getUsersessions().removeAll(deletables);
 		save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.api.APIDelegate#toAPI()
 	 */
 	public ESWorkspaceImpl toAPI() {
 		if (apiImplClass == null) {
 			apiImplClass = createAPI();
 		}
 		return apiImplClass;
 	}
 
 	/**
 	 * Set the API implementation class.
 	 * 
 	 * @param apiImpl
 	 *            the internal workspace implementation class to be used
 	 */
 	public void setAPIImpl(ESWorkspaceImpl apiImpl) {
 		apiImplClass = apiImpl;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.api.APIDelegate#createAPI()
 	 */
 	public ESWorkspaceImpl createAPI() {
 		return new ESWorkspaceImpl(this);
 	}
 
 }

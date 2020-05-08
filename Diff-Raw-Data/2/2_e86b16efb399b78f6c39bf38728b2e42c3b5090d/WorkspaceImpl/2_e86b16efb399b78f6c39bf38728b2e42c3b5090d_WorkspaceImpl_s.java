 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.emfstore.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.client.model.AdminBroker;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.Workspace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.exceptions.ProjectUrlResolutionException;
 import org.eclipse.emf.emfstore.client.model.exceptions.ServerUrlResolutionException;
 import org.eclipse.emf.emfstore.client.model.exceptions.UnkownProjectException;
 import org.eclipse.emf.emfstore.client.model.importexport.impl.ExportProjectSpaceController;
 import org.eclipse.emf.emfstore.client.model.importexport.impl.ExportWorkspaceController;
 import org.eclipse.emf.emfstore.client.model.observers.CheckoutObserver;
 import org.eclipse.emf.emfstore.client.model.observers.DeleteProjectSpaceObserver;
 import org.eclipse.emf.emfstore.client.model.util.ResourceHelper;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.url.ProjectUrlFragment;
 import org.eclipse.emf.emfstore.server.model.url.ServerUrl;
 import org.eclipse.emf.emfstore.server.model.versioning.DateVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Workspace</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl#getProjectSpaces <em>Project Spaces</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl#getServerInfos <em>Server Infos</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl#getUsersessions <em>Usersessions</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class WorkspaceImpl extends EObjectImpl implements Workspace {
 
 	/**
 	 * The current connection manager used to connect to the server(s).
 	 * 
 	 * @generated NOT
 	 */
 	private ConnectionManager connectionManager;
 
 	/**
 	 * The cached value of the '{@link #getProjectSpaces() <em>Project Spaces</em>}' containment reference list.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @see #getProjectSpaces()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ProjectSpace> projectSpaces;
 
 	/**
 	 * A mapping between project and project spaces.
 	 * 
 	 * @generated NOT
 	 */
 	private Map<Project, ProjectSpace> projectToProjectSpaceMap;
 
 	/**
 	 * The cached value of the '{@link #getServerInfos() <em>Server Infos</em>}' containment reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getServerInfos()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ServerInfo> serverInfos;
 
 	/**
 	 * The cached value of the '{@link #getUsersessions() <em>Usersessions</em>} ' containment reference list. <!--
 	 * begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getUsersessions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Usersession> usersessions;
 
 	/**
 	 * The resource set of the workspace.
 	 * 
 	 * @generated NOT
 	 */
 	private ResourceSet workspaceResourceSet;
 
 	private ExecutorService executor;
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected WorkspaceImpl() {
 		super();
 	}
 
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
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#checkout(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectInfo)
 	 */
 	public ProjectSpace checkout(final Usersession usersession, final ProjectInfo projectInfo) throws EmfStoreException {
 		PrimaryVersionSpec targetSpec = this.connectionManager.resolveVersionSpec(usersession.getSessionId(),
 			projectInfo.getProjectId(), VersionSpec.HEAD_VERSION);
 		return checkout(usersession, projectInfo, targetSpec, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#checkout(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectInfo, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectSpace checkout(final Usersession usersession, final ProjectInfo projectInfo,
 		IProgressMonitor progressMonitor) throws EmfStoreException {
 		PrimaryVersionSpec targetSpec = this.connectionManager.resolveVersionSpec(usersession.getSessionId(),
 			projectInfo.getProjectId(), VersionSpec.HEAD_VERSION);
 		return checkout(usersession, projectInfo, targetSpec, progressMonitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#checkout(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectInfo,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectSpace checkout(final Usersession usersession, final ProjectInfo projectInfo,
 		PrimaryVersionSpec targetSpec, IProgressMonitor progressMonitor) throws EmfStoreException {
 
 		SubMonitor parent = SubMonitor.convert(progressMonitor, "Checkout", 100);
 
 		// FIXME: MK: hack: set head version manually because esbrowser does not update
 		// revisions properly
 		final ProjectInfo projectInfoCopy = ModelUtil.clone(projectInfo);
 		projectInfoCopy.setVersion(targetSpec);
 
 		// get project from server
 		Project project = null;
 
 		SubMonitor newChild = parent.newChild(40);
 		parent.subTask("Fetching project from server...");
 		project = new UnknownEMFStoreWorkloadCommand<Project>(newChild) {
 			@Override
 			public Project run(IProgressMonitor monitor) throws EmfStoreException {
 				return connectionManager.getProject(usersession.getSessionId(), projectInfo.getProjectId(),
 					projectInfoCopy.getVersion());
 			}
 		}.execute();
 
 		if (project == null) {
 			throw new EmfStoreException("Server returned a null project!");
 		}
 
 		final PrimaryVersionSpec primaryVersionSpec = projectInfoCopy.getVersion();
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 
 		// initialize project space
 		parent.subTask("Initializing Projectspace...");
 		projectSpace.setProjectId(projectInfo.getProjectId());
 		projectSpace.setProjectName(projectInfo.getName());
 		projectSpace.setProjectDescription(projectInfo.getDescription());
 		projectSpace.setBaseVersion(primaryVersionSpec);
 		projectSpace.setLastUpdated(new Date());
 		projectSpace.setUsersession(usersession);
 		WorkspaceManager.getObserverBus().register((ProjectSpaceBase) projectSpace);
 		projectSpace.setProject(project);
 		projectSpace.setResourceCount(0);
 		projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 		parent.worked(20);
 
 		// progressMonitor.subTask("Initializing resources...");
 		projectSpace.initResources(this.workspaceResourceSet);
 		parent.worked(10);
 
 		// retrieve recent changes
 		parent.subTask("Retrieving recent changes...");
 		try {
 			DateVersionSpec dateVersionSpec = VersioningFactory.eINSTANCE.createDateVersionSpec();
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DAY_OF_YEAR, -10);
 			dateVersionSpec.setDate(calendar.getTime());
 			PrimaryVersionSpec sourceSpec;
 			try {
 				sourceSpec = this.connectionManager.resolveVersionSpec(usersession.getSessionId(),
 					projectSpace.getProjectId(), dateVersionSpec);
 			} catch (InvalidVersionSpecException e) {
 				sourceSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 				sourceSpec.setIdentifier(0);
 			}
 			projectSpace.eResource().save(null);
 		} catch (EmfStoreException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			WorkspaceUtil.logException(e.getMessage(), e);
 		} catch (IOException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		}
 		parent.worked(10);
 
 		parent.subTask("Finishing checkout...");
 		addProjectSpace(projectSpace);
 		save();
 		WorkspaceManager.getObserverBus().notify(CheckoutObserver.class).checkoutDone(projectSpace);
 		parent.worked(10);
 		progressMonitor.done();
 
 		return projectSpace;
 	}
 
 	private ProjectInfo createEmptyRemoteProject(final Usersession usersession, final String projectName,
 		final String projectDescription, final IProgressMonitor progressMonitor) throws EmfStoreException {
 		final ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		final LogMessage log = VersioningFactory.eINSTANCE.createLogMessage();
 		log.setMessage("Creating project '" + projectName + "'");
 		log.setAuthor(usersession.getUsername());
 		log.setClientDate(new Date());
 		ProjectInfo emptyProject = null;
 
 		new UnknownEMFStoreWorkloadCommand<ProjectInfo>(progressMonitor) {
 			@Override
 			public ProjectInfo run(IProgressMonitor monitor) throws EmfStoreException {
 				return connectionManager.createEmptyProject(usersession.getSessionId(), projectName,
 					projectDescription, log);
 			}
 		}.execute();
 
 		progressMonitor.worked(10);
 		updateProjectInfos(usersession);
 		return emptyProject;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#createLocalProject(java.lang.String, java.lang.String)
 	 */
 	public ProjectSpace createLocalProject(String projectName, String projectDescription) {
 
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createProject());
 		projectSpace.setProjectName(projectName);
 		projectSpace.setProjectDescription(projectDescription);
 		projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 
 		projectSpace.initResources(getResourceSet());
 
 		this.addProjectSpace(projectSpace);
 		this.save();
 
 		return projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#createRemoteProject(org.eclipse.emf.emfstore.client.model.ServerInfo,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public ProjectInfo createRemoteProject(ServerInfo serverInfo, final String projectName,
 		final String projectDescription, final IProgressMonitor monitor) throws EmfStoreException {
 		return new ServerCall<ProjectInfo>(serverInfo) {
 			@Override
 			protected ProjectInfo run() throws EmfStoreException {
 				return createEmptyRemoteProject(getUsersession(), projectName, projectDescription, monitor);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#createRemoteProject(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      java.lang.String, java.lang.String)
 	 */
 	public ProjectInfo createRemoteProject(Usersession usersession, final String projectName,
 		final String projectDescription, final IProgressMonitor monitor) throws EmfStoreException {
 		return new ServerCall<ProjectInfo>(usersession) {
 			@Override
 			protected ProjectInfo run() throws EmfStoreException {
 				return createEmptyRemoteProject(getUsersession(), projectName, projectDescription, monitor);
 			}
 		}.execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#deleteProjectSpace(org.eclipse.emf.emfstore.client.model.ProjectSpace)
 	 */
 	public void deleteProjectSpace(ProjectSpace projectSpace) throws IOException {
 
 		assert (projectSpace != null);
 
 		// delete project to notify listeners
 		projectSpace.getProject().delete();
 
 		getProjectSpaces().remove(projectSpace);
 		save();
 		projectToProjectSpaceMap.remove(projectSpace.getProject());
 
 		projectSpace.delete();
 
 		WorkspaceManager.getObserverBus().notify(DeleteProjectSpaceObserver.class).projectSpaceDeleted(projectSpace);
 	}
 
 	public void deleteRemoteProject(ServerInfo serverInfo, final ProjectId projectId, final boolean deleteFiles)
 		throws EmfStoreException {
 		new ServerCall<Void>(serverInfo) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				new UnknownEMFStoreWorkloadCommand<Void>(getProgressMonitor()) {
 
 					@Override
 					public Void run(IProgressMonitor monitor) throws EmfStoreException {
 						getConnectionManager().deleteProject(getSessionId(), projectId, deleteFiles);
 						return null;
 					}
 				}.execute();
 
 				updateProjectInfos(getUsersession());
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#deleteRemoteProject(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectId, boolean)
 	 */
 	public void deleteRemoteProject(final Usersession usersession, final ProjectId projectId, final boolean deleteFiles)
 		throws EmfStoreException {
 		new ServerCall<Void>(usersession) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				getConnectionManager().deleteProject(getSessionId(), projectId, deleteFiles);
 				updateProjectInfos(getUsersession());
 				return null;
 			}
 		}.execute();
 	}
 
 	// END OF CUSTOM CODE
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			return getProjectSpaces();
 		case ModelPackage.WORKSPACE__SERVER_INFOS:
 			return getServerInfos();
 		case ModelPackage.WORKSPACE__USERSESSIONS:
 			return getUsersessions();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			return ((InternalEList<InternalEObject>) (InternalEList<?>) getProjectSpaces()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			return ((InternalEList<?>) getProjectSpaces()).basicRemove(otherEnd, msgs);
 		case ModelPackage.WORKSPACE__SERVER_INFOS:
 			return ((InternalEList<?>) getServerInfos()).basicRemove(otherEnd, msgs);
 		case ModelPackage.WORKSPACE__USERSESSIONS:
 			return ((InternalEList<?>) getUsersessions()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			return projectSpaces != null && !projectSpaces.isEmpty();
 		case ModelPackage.WORKSPACE__SERVER_INFOS:
 			return serverInfos != null && !serverInfos.isEmpty();
 		case ModelPackage.WORKSPACE__USERSESSIONS:
 			return usersessions != null && !usersessions.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			getProjectSpaces().clear();
 			getProjectSpaces().addAll((Collection<? extends ProjectSpace>) newValue);
 			return;
 		case ModelPackage.WORKSPACE__SERVER_INFOS:
 			getServerInfos().clear();
 			getServerInfos().addAll((Collection<? extends ServerInfo>) newValue);
 			return;
 		case ModelPackage.WORKSPACE__USERSESSIONS:
 			getUsersessions().clear();
 			getUsersessions().addAll((Collection<? extends Usersession>) newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.WORKSPACE;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 		case ModelPackage.WORKSPACE__PROJECT_SPACES:
 			getProjectSpaces().clear();
 			return;
 		case ModelPackage.WORKSPACE__SERVER_INFOS:
 			getServerInfos().clear();
 			return;
 		case ModelPackage.WORKSPACE__USERSESSIONS:
 			getUsersessions().clear();
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	// BEGIN OF CUSTOM CODE
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#exportProjectSpace(org.eclipse.emf.emfstore.client.model.ProjectSpace,
 	 *      java.io.File)
 	 */
 	public void exportProjectSpace(ProjectSpace projectSpace, File file) throws IOException {
 		new ExportProjectSpaceController(projectSpace).execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#exportProjectSpace(org.eclipse.emf.emfstore.client.model.ProjectSpace,
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
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#exportWorkSpace(java.io.File)
 	 */
 	public void exportWorkSpace(File file) throws IOException {
 		new ExportWorkspaceController().execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#exportWorkSpace(java.io.File,
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
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getAdminBroker(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public AdminBroker getAdminBroker(final ServerInfo serverInfo) throws EmfStoreException, AccessControlException {
 		return new ServerCall<AdminBroker>(serverInfo) {
 			@Override
 			protected AdminBroker run() throws EmfStoreException {
 				return new AdminBrokerImpl(serverInfo, getSessionId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getAdminBroker(org.eclipse.emf.emfstore.client.model.Usersession)
 	 */
 	public AdminBroker getAdminBroker(final Usersession usersession) throws EmfStoreException, AccessControlException {
 		return new ServerCall<AdminBroker>(usersession) {
 			@Override
 			protected AdminBroker run() throws EmfStoreException {
 				return new AdminBrokerImpl(usersession.getServerInfo(), getSessionId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getEditingDomain()
 	 */
 	public EditingDomain getEditingDomain() {
 		return Configuration.getEditingDomain();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getHistoryInfo(org.eclipse.emf.emfstore.client.model.ServerInfo,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectId,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery)
 	 */
 	public List<HistoryInfo> getHistoryInfo(ServerInfo serverInfo, final ProjectId projectId, final HistoryQuery query)
 		throws EmfStoreException {
 		return new ServerCall<List<HistoryInfo>>(serverInfo) {
 			@Override
 			protected List<HistoryInfo> run() throws EmfStoreException {
 				ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 				return connectionManager.getHistoryInfo(getUsersession().getSessionId(), projectId, query);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getHistoryInfo(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectId,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery)
 	 */
 	public List<HistoryInfo> getHistoryInfo(final Usersession usersession, final ProjectId projectId,
 		final HistoryQuery query) throws EmfStoreException {
 		return new ServerCall<List<HistoryInfo>>(usersession) {
 			@Override
 			protected List<HistoryInfo> run() throws EmfStoreException {
 				ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 				return connectionManager.getHistoryInfo(usersession.getSessionId(), projectId, query);
 			}
 		}.execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getProjectSpace(org.eclipse.emf.emfstore.common.model.Project)
 	 */
 	public ProjectSpace getProjectSpace(Project project) throws UnkownProjectException {
 		ProjectSpace projectSpace = projectToProjectSpaceMap.get(project);
 		if (projectSpace == null) {
 			throw new UnkownProjectException();
 		}
 		return projectSpace;
 	}
 
 	// END OF CUSTOM CODE
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<ProjectSpace> getProjectSpaces() {
 		if (projectSpaces == null) {
 			projectSpaces = new EObjectContainmentWithInverseEList.Resolving<ProjectSpace>(ProjectSpace.class, this,
 				ModelPackage.WORKSPACE__PROJECT_SPACES, ModelPackage.PROJECT_SPACE__WORKSPACE);
 		}
 		return projectSpaces;
 	}
 
 	// BEGIN OF CUSTOM CODE
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getRemoteProjectList(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public List<ProjectInfo> getRemoteProjectList(ServerInfo serverInfo) throws EmfStoreException {
 		return new ServerCall<List<ProjectInfo>>(serverInfo) {
 			@Override
 			protected List<ProjectInfo> run() throws EmfStoreException {
 				return getConnectionManager().getProjectList(getSessionId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#getRemoteProjectList(org.eclipse.emf.emfstore.client.model.Usersession)
 	 */
 	public List<ProjectInfo> getRemoteProjectList(Usersession usersession) throws EmfStoreException {
 		return new ServerCall<List<ProjectInfo>>(usersession) {
 			@Override
 			protected List<ProjectInfo> run() throws EmfStoreException {
 				return getConnectionManager().getProjectList(getSessionId());
 			}
 		}.execute();
 	}
 
 	// END OF CUSTOM CODE
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<ServerInfo> getServerInfos() {
 		if (serverInfos == null) {
 			serverInfos = new EObjectContainmentEList.Resolving<ServerInfo>(ServerInfo.class, this,
 				ModelPackage.WORKSPACE__SERVER_INFOS);
 		}
 		return serverInfos;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<Usersession> getUsersessions() {
 		if (usersessions == null) {
 			usersessions = new EObjectContainmentEList.Resolving<Usersession>(Usersession.class, this,
 				ModelPackage.WORKSPACE__USERSESSIONS);
 		}
 		return usersessions;
 	}
 
 	// BEGIN OF CUSTOM CODE
 	/**
 	 * {@inheritDoc}
 	 */
 	public ResourceSet getResourceSet() {
 		return this.workspaceResourceSet;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ProjectSpace importProject(Project project, String name, String description) {
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setProject(project);
 		projectSpace.setProjectName(name);
 		projectSpace.setProjectDescription(description);
 		projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 
 		projectSpace.initResources(this.workspaceResourceSet);
 
 		addProjectSpace(projectSpace);
 		this.save();
 
 		return projectSpace;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#importProject(java.lang.String)
 	 */
 	public ProjectSpace importProject(String absoluteFileName) throws IOException {
 		Project project = ResourceHelper.getElementFromResource(absoluteFileName, Project.class, 0);
 		return importProject(project, absoluteFileName.substring(absoluteFileName.lastIndexOf(File.separatorChar) + 1),
 			"Imported from " + absoluteFileName);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#importProjectSpace(java.lang.String)
 	 */
 	public ProjectSpace importProjectSpace(String absoluteFileName) throws IOException {
 
 		ProjectSpace projectSpace = ResourceHelper.getElementFromResource(absoluteFileName, ProjectSpace.class, 0);
 
 		projectSpace.initResources(this.workspaceResourceSet);
 
 		addProjectSpace(projectSpace);
 		this.save();
 		return projectSpace;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#init(org.eclipse.emf.transaction.TransactionalEditingDomain)
 	 * @generated NOT
 	 */
 	public void init() {
 		executor = Executors.newCachedThreadPool();
 		projectToProjectSpaceMap = new HashMap<Project, ProjectSpace>();
 		// initialize all projectSpaces
 		for (ProjectSpace projectSpace : getProjectSpaces()) {
 			projectSpace.init();
 			projectToProjectSpaceMap.put(projectSpace.getProject(), projectSpace);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#resolve(org.eclipse.emf.emfstore.server.model.url.ProjectUrlFragment)
 	 */
 	public Set<ProjectSpace> resolve(ProjectUrlFragment projectUrlFragment) throws ProjectUrlResolutionException {
 		Set<ProjectSpace> result = new HashSet<ProjectSpace>();
 		for (ProjectSpace projectSpace : getProjectSpaces()) {
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
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#resolve(org.eclipse.emf.emfstore.server.model.url.ServerUrl)
 	 */
 	public Set<ServerInfo> resolve(ServerUrl serverUrl) throws ServerUrlResolutionException {
 		Set<ServerInfo> result = new HashSet<ServerInfo>();
 		for (ServerInfo serverInfo : getServerInfos()) {
 			boolean matchingHostname = serverInfo.getUrl().equals(serverUrl.getHostName());
 			boolean matchingPort = serverInfo.getPort() == serverUrl.getPort();
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
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#resolveVersionSpec(org.eclipse.emf.emfstore.client.model.ServerInfo,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.VersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectId)
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(ServerInfo serverInfo, final VersionSpec versionSpec,
 		final ProjectId projectId) throws EmfStoreException {
 		return new ServerCall<PrimaryVersionSpec>(serverInfo) {
 			@Override
 			protected PrimaryVersionSpec run() throws EmfStoreException {
 				ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 				return connectionManager.resolveVersionSpec(getUsersession().getSessionId(), projectId, versionSpec);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#resolveVersionSpec(org.eclipse.emf.emfstore.client.model.Usersession,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.VersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.ProjectId)
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(final Usersession usersession, final VersionSpec versionSpec,
 		final ProjectId projectId) throws EmfStoreException {
		return new ServerCall<PrimaryVersionSpec>() {
 			@Override
 			protected PrimaryVersionSpec run() throws EmfStoreException {
 				ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 				return connectionManager.resolveVersionSpec(usersession.getSessionId(), projectId, versionSpec);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#save()
 	 */
 	public void save() {
 		try {
 			this.eResource().save(Configuration.getResourceSaveOptions());
 		} catch (IOException e) {
 			// MK Auto-generated catch block
 			// FIXME OW MK: also insert code for dangling href handling here
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#setConnectionManager(org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager)
 	 */
 	public void setConnectionManager(ConnectionManager connectionManager) {
 		this.connectionManager = connectionManager;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#setWorkspaceResourceSet(org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void setResourceSet(ResourceSet resourceSet) {
 		this.workspaceResourceSet = resourceSet;
 		for (ProjectSpace projectSpace : getProjectSpaces()) {
 			ProjectSpaceBase base = (ProjectSpaceBase) projectSpace;
 			base.setResourceSet(workspaceResourceSet);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#updateACUser(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public void updateACUser(ServerInfo serverInfo) throws EmfStoreException {
 		new ServerCall<Void>(serverInfo) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				getUsersession().setACUser(getConnectionManager().resolveUser(getSessionId(), null));
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#updateACUser(org.eclipse.emf.emfstore.client.model.Usersession)
 	 */
 	public void updateACUser(Usersession usersession) throws EmfStoreException {
 		new ServerCall<Void>(usersession) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				getUsersession().setACUser(getConnectionManager().resolveUser(getSessionId(), null));
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#updateProjectInfos(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public void updateProjectInfos(final ServerInfo serverInfo) throws EmfStoreException {
 		new ServerCall<Void>(serverInfo) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				return updateProjectInfos(serverInfo, getUsersession());
 			}
 		}.execute();
 	}
 
 	private Void updateProjectInfos(ServerInfo serverInfo, final Usersession usersession) {
 		// BEGIN SUPRESS CATCH EXCEPTION
 		try {
 			serverInfo.getProjectInfos().clear();
 			if (WorkspaceManager.getInstance().getConnectionManager().isLoggedIn(usersession.getSessionId())) {
 				serverInfo.getProjectInfos().addAll(getRemoteProjectList(usersession));
 			}
 			WorkspaceManager.getInstance().getCurrentWorkspace().save();
 		} catch (EmfStoreException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		} catch (RuntimeException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		}
 		// END SUPRESS CATCH EXCEPTION
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#updateProjectInfos(org.eclipse.emf.emfstore.client.model.Usersession)
 	 */
 	public void updateProjectInfos(final Usersession usersession) throws EmfStoreException {
 		new ServerCall<Void>(usersession) {
 			@Override
 			protected Void run() throws EmfStoreException {
 				return updateProjectInfos(usersession.getServerInfo(), usersession);
 			}
 		}.execute();
 	}
 
 	// END OF CUSTOM CODE
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#addServerInfo(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public void addServerInfo(ServerInfo serverInfo) {
 		getServerInfos().add(serverInfo);
 		save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.Workspace#removeServerInfo(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	public void removeServerInfo(ServerInfo serverInfo) {
 		getServerInfos().remove(serverInfo);
 		save();
 	}
 
 } // WorkspaceImpl

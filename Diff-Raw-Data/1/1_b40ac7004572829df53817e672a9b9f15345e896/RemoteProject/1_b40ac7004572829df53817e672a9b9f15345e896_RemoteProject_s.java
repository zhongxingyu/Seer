 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl;
 
 import static org.eclipse.emf.emfstore.internal.common.ListUtil.copy;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.model.observer.ESCheckoutObserver;
 import org.eclipse.emf.emfstore.internal.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.WorkspaceProvider;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectId;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.DateVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESBranchInfo;
 import org.eclipse.emf.emfstore.server.model.ESGlobalProjectId;
 import org.eclipse.emf.emfstore.server.model.ESHistoryInfo;
 import org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 
 /**
  * Represents a remote project that is located on a remote server.
  * 
  * @author wesendon
  * @author emueller
  */
 public class RemoteProject implements ESRemoteProject {
 
 	/**
 	 * The current connection manager used to connect to the server(s).
 	 */
 	private final ProjectInfo projectInfo;
 	private final ESServer server;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param server
 	 *            the server the remote project is located on
 	 * @param projectInfo
 	 *            information about which project to access
 	 */
 	public RemoteProject(ESServer server, ProjectInfo projectInfo) {
 		this.server = server;
 		this.projectInfo = projectInfo;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getGlobalProjectId()
 	 */
 	public ESGlobalProjectId getGlobalProjectId() {
 		return projectInfo.getProjectId();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getProjectName()
 	 */
 	public String getProjectName() {
 		return projectInfo.getName();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getBranches(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESBranchInfo> getBranches(IProgressMonitor monitor) throws ESException {
 		return new UnknownEMFStoreWorkloadCommand<List<ESBranchInfo>>(monitor) {
 			@Override
 			public List<ESBranchInfo> run(IProgressMonitor monitor) throws ESException {
 				return copy(new ServerCall<List<BranchInfo>>(server) {
 					@Override
 					protected List<BranchInfo> run() throws ESException {
 						final ConnectionManager connectionManager = WorkspaceProvider.getInstance()
 							.getConnectionManager();
 						return connectionManager.getBranches(getSessionId(), (ProjectId) getGlobalProjectId());
 					};
 				}.execute());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getBranches(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESBranchInfo> getBranches(ESUsersession usersession, IProgressMonitor monitor) throws ESException {
 		return copy(new ServerCall<List<BranchInfo>>(server) {
 			@Override
 			protected List<BranchInfo> run() throws ESException {
 				final ConnectionManager cm = WorkspaceProvider.getInstance().getConnectionManager();
 				return cm.getBranches(getSessionId(), (ProjectId) getGlobalProjectId());
 			};
 		}.execute());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#resolveVersionSpec(org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(final ESVersionSpec versionSpec, IProgressMonitor monitor)
 		throws ESException {
 		return new ServerCall<PrimaryVersionSpec>(server, monitor) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return getConnectionManager().resolveVersionSpec(getSessionId(), (ProjectId) getGlobalProjectId(),
 					(VersionSpec) versionSpec);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#resolveVersionSpec(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(ESUsersession session, final ESVersionSpec versionSpec,
 		IProgressMonitor monitor) throws ESException {
 		return new ServerCall<PrimaryVersionSpec>(session) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return getConnectionManager().resolveVersionSpec(getSessionId(), (ProjectId) getGlobalProjectId(),
 					(VersionSpec) versionSpec);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getHistoryInfos(org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESHistoryInfo> getHistoryInfos(final ESHistoryQuery query, IProgressMonitor monitor)
 		throws ESException {
 		return copy(new ServerCall<List<HistoryInfo>>(server, monitor) {
 			@Override
 			protected List<HistoryInfo> run() throws ESException {
 				return getConnectionManager().getHistoryInfo(getSessionId(), (ProjectId) getGlobalProjectId(),
 					(HistoryQuery) query);
 			}
 		}.execute());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getHistoryInfos(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESHistoryInfo> getHistoryInfos(ESUsersession usersession, final ESHistoryQuery query,
 		IProgressMonitor monitor) throws ESException {
 		return copy(new ServerCall<List<HistoryInfo>>(usersession, monitor) {
 			@Override
 			protected List<HistoryInfo> run() throws ESException {
 				return getConnectionManager().getHistoryInfo(getUsersession().getSessionId(),
 					(ProjectId) getGlobalProjectId(), (HistoryQuery) query);
 			}
 		}.execute());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#addTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void addTag(final ESPrimaryVersionSpec versionSpec, final ESTagVersionSpec tag, IProgressMonitor monitor)
 		throws ESException {
 		new ServerCall<Void>(server, monitor) {
 			@Override
 			protected Void run() throws ESException {
 				getConnectionManager().addTag(getUsersession().getSessionId(), (ProjectId) getGlobalProjectId(),
 					(PrimaryVersionSpec) versionSpec, (TagVersionSpec) tag);
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#removeTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void removeTag(final ESPrimaryVersionSpec versionSpec, final ESTagVersionSpec tag, IProgressMonitor monitor)
 		throws ESException {
 		new ServerCall<Void>(server, monitor) {
 			@Override
 			protected Void run() throws ESException {
 				getConnectionManager().removeTag(getUsersession().getSessionId(), (ProjectId) getGlobalProjectId(),
 					(PrimaryVersionSpec) versionSpec, (TagVersionSpec) tag);
 				return null;
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectSpace checkout(final IProgressMonitor monitor) throws ESException {
 		return new ServerCall<ProjectSpace>(server) {
 			@Override
 			protected ProjectSpace run() throws ESException {
 				return checkout(getUsersession(), monitor);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectSpace checkout(ESUsersession usersession, IProgressMonitor monitor) throws ESException {
 		PrimaryVersionSpec targetSpec = resolveVersionSpec(usersession, Versions.createHEAD(), monitor);
 		return checkout(usersession, targetSpec, monitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectSpace checkout(ESUsersession usersession, ESVersionSpec versionSpec, IProgressMonitor progressMonitor)
 		throws ESException {
 
 		SubMonitor parent = SubMonitor.convert(progressMonitor, "Checkout", 100);
 		final Usersession session = (Usersession) usersession;
 
 		// FIXME: MK: hack: set head version manually because esbrowser does not
 		// update
 		// revisions properly
 		final ProjectInfo projectInfoCopy = ModelUtil.clone(projectInfo);
 		projectInfoCopy.setVersion((PrimaryVersionSpec) versionSpec);
 
 		// get project from server
 		Project project = null;
 
 		SubMonitor newChild = parent.newChild(40);
 		parent.subTask("Fetching project from server...");
 		project = new UnknownEMFStoreWorkloadCommand<Project>(newChild) {
 			@Override
 			public Project run(IProgressMonitor monitor) throws ESException {
 				return new ServerCall<Project>(session) {
 					@Override
 					protected Project run() throws ESException {
 						return getConnectionManager().getProject(session.getSessionId(), projectInfo.getProjectId(),
 							projectInfoCopy.getVersion());
 					}
 				}.execute();
 			}
 		}.execute();
 
 		if (project == null) {
 			throw new ESException("Server returned a null project!");
 		}
 
 		final PrimaryVersionSpec primaryVersionSpec = projectInfoCopy.getVersion();
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 
 		// initialize project space
 		parent.subTask("Initializing Projectspace...");
 		projectSpace.setProjectId(projectInfoCopy.getProjectId());
 		projectSpace.setProjectName(projectInfoCopy.getName());
 		projectSpace.setProjectDescription(projectInfoCopy.getDescription());
 		projectSpace.setBaseVersion(primaryVersionSpec);
 		projectSpace.setLastUpdated(new Date());
 		projectSpace.setUsersession(session);
 		WorkspaceProvider.getObserverBus().register((ProjectSpaceBase) projectSpace);
 		projectSpace.setProject(project);
 		projectSpace.setResourceCount(0);
 		projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 		parent.worked(20);
 
 		// progressMonitor.subTask("Initializing resources...");
 		// TODO: OTS cast
 		projectSpace.initResources(((WorkspaceBase) WorkspaceProvider.INSTANCE.getWorkspace()).getResourceSet());
 		parent.worked(10);
 
 		// retrieve recent changes
 		// TODO why are we doing this?? And why HERE?
 		parent.subTask("Retrieving recent changes...");
 		try {
 			DateVersionSpec dateVersionSpec = VersioningFactory.eINSTANCE.createDateVersionSpec();
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DAY_OF_YEAR, -10);
 			dateVersionSpec.setDate(calendar.getTime());
 			PrimaryVersionSpec sourceSpec;
 			try {
 				sourceSpec = this.resolveVersionSpec(session, dateVersionSpec, progressMonitor);
 			} catch (InvalidVersionSpecException e) {
 				sourceSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 				sourceSpec.setIdentifier(0);
 			}
 			ModelUtil.saveResource(projectSpace.eResource(), WorkspaceUtil.getResourceLogger());
 
 		} catch (ESException e) {
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
 		// TODO: OTS cast
 		((WorkspaceBase) WorkspaceProvider.INSTANCE.getWorkspace()).addProjectSpace(projectSpace);
 		// TODO: OTS save
 		((WorkspaceBase) WorkspaceProvider.INSTANCE.getWorkspace()).save();
 		WorkspaceProvider.getObserverBus().notify(ESCheckoutObserver.class).checkoutDone(projectSpace);
 		parent.worked(10);
 		parent.done();
 
 		return projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#delete(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(IProgressMonitor monitor) throws ESException {
 		getDeleteProjectServerCall().setProgressMonitor(monitor).setServer(server).execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#delete(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(ESUsersession usersession, IProgressMonitor monitor) throws ESException {
 		getDeleteProjectServerCall().setProgressMonitor(monitor).setUsersession(usersession).execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getServer()
 	 */
 	public ESServer getServer() {
 		return server;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getHeadVersion(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec getHeadVersion(IProgressMonitor monitor) throws ESException {
 		return resolveVersionSpec(Versions.createHEAD(), monitor);
 	}
 
 	private ServerCall<Void> getDeleteProjectServerCall() {
 		return new ServerCall<Void>() {
 			@Override
 			protected Void run() throws ESException {
 				new UnknownEMFStoreWorkloadCommand<Void>(getProgressMonitor()) {
 					@Override
 					public Void run(IProgressMonitor monitor) throws ESException {
 						getConnectionManager().deleteProject(getSessionId(), projectInfo.getProjectId(), true);
 						return null;
 					}
 				}.execute();
 
				getServer().getRemoteProjects().remove(this);
 				return null;
 			}
 		};
 	}
 }

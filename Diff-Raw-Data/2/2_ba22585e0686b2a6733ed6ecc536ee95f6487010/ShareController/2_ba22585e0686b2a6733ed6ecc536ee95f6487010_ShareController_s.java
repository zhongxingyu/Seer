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
 package org.eclipse.emf.emfstore.client.model.controller;
 
 import java.util.Date;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.client.model.observers.LoginObserver;
 import org.eclipse.emf.emfstore.client.model.observers.ShareObserver;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 
 /**
  * Shares a project.
  * 
  * @author ovonwesen
  * @author emueller
  */
 public class ShareController extends ServerCall<Void> {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param projectSpace
 	 *            the project space to be shared
 	 * @param session
 	 *            the session to use during share
 	 * @param monitor
 	 *            a progress monitor that is used to indicate the progress of the share
 	 */
 	public ShareController(ProjectSpaceBase projectSpace, Usersession session, IProgressMonitor monitor) {
 		super(projectSpace);
 
 		// if session is null, session will be injected by sessionmanager
 		setUsersession(session);
 		setProgressMonitor(monitor);
 	}
 
 	@Override
 	protected Void run() throws EmfStoreException {
 		doRun();
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void doRun() throws EmfStoreException {
 
 		getProgressMonitor().beginTask("Sharing Project", 100);
 		getProgressMonitor().worked(1);
 		getProgressMonitor().subTask("Preparing project for sharing");
 
 		final LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		logMessage.setAuthor(getUsersession().getUsername());
 		logMessage.setClientDate(new Date());
 		logMessage.setMessage("Initial commit");
 		ProjectInfo createdProject = null;
 
 		getProjectSpace().stopChangeRecording();
 		Configuration.setAutoSave(false);
 
 		getProgressMonitor().worked(10);
 		if (getProgressMonitor().isCanceled()) {
 			Configuration.setAutoSave(true);
 			getProjectSpace().save();
 			getProjectSpace().startChangeRecording();
 			getProgressMonitor().done();
 		}
 		getProgressMonitor().subTask("Sharing project with server");
 
 		// make sure, current state of caches is written to resource
 		getProjectSpace().save();
 
 		createdProject = new UnknownEMFStoreWorkloadCommand<ProjectInfo>(getProgressMonitor()) {
 			@Override
 			public ProjectInfo run(IProgressMonitor monitor) throws EmfStoreException {
 				return WorkspaceManager
 					.getInstance()
 					.getConnectionManager()
 					.createProject(
 						getUsersession().getSessionId(),
 						getProjectSpace().getProjectName() == null ? "Project@" + new Date() : getProjectSpace()
 							.getProjectName(),
 						getProjectSpace().getProjectDescription() == null ? "" : getProjectSpace()
 							.getProjectDescription(), logMessage, getProjectSpace().getProject());
 			}
 		}.execute();
 
 		getProgressMonitor().worked(30);
 		getProgressMonitor().subTask("Finalizing share");
 
 		// set attributes after server call
 		getProgressMonitor().subTask("Setting attributes");
 		this.setUsersession(getUsersession());
 		WorkspaceManager.getObserverBus().register(getProjectSpace(), LoginObserver.class);
 
		Configuration.setAutoSave(true);
 		getProjectSpace().save();
 		getProjectSpace().startChangeRecording();
 		getProjectSpace().setBaseVersion(createdProject.getVersion());
 		getProjectSpace().setLastUpdated(new Date());
 		getProjectSpace().setProjectId(createdProject.getProjectId());
 		getProjectSpace().setUsersession(getUsersession());
 		getProjectSpace().saveProjectSpaceOnly();
 
 		// TODO ASYNC implement File Upload with observer
 		// If any files have already been added, upload them.
 		getProgressMonitor().worked(20);
 		getProgressMonitor().subTask("Uploading files");
 		getProjectSpace().getFileTransferManager().uploadQueuedFiles(getProgressMonitor());
 
 		getProgressMonitor().worked(20);
 		getProgressMonitor().subTask("Finalizing share.");
 		getProjectSpace().getOperations().clear();
 		getProjectSpace().updateDirtyState();
 
 		getProgressMonitor().done();
 		WorkspaceManager.getObserverBus().notify(ShareObserver.class).shareDone(getProjectSpace());
 	}
 }

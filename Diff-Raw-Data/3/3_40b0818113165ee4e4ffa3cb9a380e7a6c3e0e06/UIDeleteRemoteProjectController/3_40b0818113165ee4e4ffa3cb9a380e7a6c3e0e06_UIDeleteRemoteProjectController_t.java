 /*******************************************************************************
  * Copyright (c) 2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.controller;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.impl.RemoteProject;
 import org.eclipse.emf.emfstore.internal.client.ui.common.RunInUI;
 import org.eclipse.emf.emfstore.internal.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.internal.server.exceptions.EMFStoreException;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectId;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * UI controller for deleting a project on the server.
  * 
  * TODO REVIEW THIS PIECE OF SHIT
  * 
  * @author emueller
  * 
  */
 public class UIDeleteRemoteProjectController extends AbstractEMFStoreUIController<Void> {
 
 	private final ServerInfo serverInfo;
 	private final Usersession session;
 	private final ProjectInfo projectInfo;
 	private final ESRemoteProject remoteProject;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that should be used during the deletion of the project
 	 * @param serverInfo
 	 *            the server info containing the information about the server the project is hosted on
 	 * @param projectId
 	 *            the {@link ProjectId} of the project to be deleted
 	 * @param deleteFiles
 	 *            whether to delete the physical files on the server
 	 */
 	public UIDeleteRemoteProjectController(Shell shell, ServerInfo serverInfo, ProjectInfo projectInfo) {
 		super(shell);
 		this.serverInfo = serverInfo;
 		this.projectInfo = projectInfo;
 		session = null;
 		remoteProject = null;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that should be used during the deletion of the project
 	 * @param serverInfo
 	 *            the server info containing the information about the server the project is hosted on
 	 * @param projectId
 	 *            the {@link ProjectId} of the project to be deleted
 	 * @param deleteFiles
 	 *            whether to delete the physical files on the server
 	 */
 	public UIDeleteRemoteProjectController(Shell shell, Usersession session, ProjectInfo projectInfo) {
 		super(shell);
 		this.serverInfo = null;
 		this.projectInfo = projectInfo;
 		this.session = session;
 		remoteProject = null;
 	}
 
 	public UIDeleteRemoteProjectController(Shell shell, Usersession session, ESRemoteProject remoteProject) {
 		super(shell);
 		this.serverInfo = null;
 		this.projectInfo = null;
 		this.session = session;
 		this.remoteProject = remoteProject;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.common.MonitoredEMFStoreAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public Void doRun(IProgressMonitor progressMonitor) throws EMFStoreException {
 
 		try {
 
 			if (remoteProject != null) {
 				deleteRemoteProject(remoteProject, progressMonitor);
				return null;
 			} else {
 				deleteRemoteProject(progressMonitor);
 				return null;
 			}
 
 		} catch (EMFStoreException e) {
 			MessageDialog.openError(getShell(), "Delete project failed.",
 				"Deletion of project " + projectInfo.getName() + " failed: " + e.getMessage());
 		}
 
 		return null;
 	}
 
 	private void deleteRemoteProject(IProgressMonitor monitor) throws EMFStoreException {
 
 		Boolean shouldDelete = RunInUI.runWithResult(new Callable<Boolean>() {
 			public Boolean call() throws Exception {
 				return MessageDialog.openConfirm(getShell(),
 					"Delete " + projectInfo.getName(),
 					String.format("Are you sure you want to delete \'%s\'", projectInfo.getName()));
 			}
 		});
 
 		if (!shouldDelete) {
 			return;
 		}
 
 		if (session != null) {
 			new RemoteProject(serverInfo, projectInfo).delete(session, monitor);
 		} else {
 			new RemoteProject(serverInfo, projectInfo).delete(monitor);
 		}
 	}
 
 	private void deleteRemoteProject(ESRemoteProject remoteProject, IProgressMonitor monitor) throws EMFStoreException {
 
 		Boolean shouldDelete = RunInUI.runWithResult(new Callable<Boolean>() {
 			public Boolean call() throws Exception {
 				return MessageDialog.openConfirm(getShell(),
 					"Delete " + projectInfo.getName(),
 					String.format("Are you sure you want to delete \'%s\'", projectInfo.getName()));
 			}
 		});
 
 		if (!shouldDelete) {
 			return;
 		}
 
 		if (session != null) {
 			remoteProject.delete(session, monitor);
 		} else {
 			remoteProject.delete(monitor);
 		}
 	}
 
 	// private void deleteRemoteProject(Usersession session, ProjectId projectId, boolean deleteFiles)
 	// throws EMFStoreException {
 	// if (confirm("Confirmation", "Do you really want to delete the remote project?")) {
 	// // TODO: OTS casts + monitor
 	// new RemoteProject(serverInfo, projectInfo).delete(session, new NullProgressMonitor());
 	// }
 	// }
 	//
 	// private void deleteRemoteProject(final ServerInfo serverInfo, final ProjectId projectId, final boolean
 	// deleteFiles,
 	// IProgressMonitor monitor) throws EMFStoreException {
 	// if (confirm("Confirmation", "Do you really want to delete the remote project?")) {
 	// // TODO: OTS casts
 	// new RemoteProject(serverInfo, projectInfo).delete(monitor);
 	// }
 	// }
 }

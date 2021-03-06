 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.ccvs.ui.actions;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.internal.ccvs.core.*;
 import org.eclipse.team.internal.ccvs.core.client.*;
 import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
 import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
 import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
 import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
 import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
 import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;
 import org.eclipse.team.internal.ccvs.ui.Policy;
 import org.eclipse.team.internal.ccvs.ui.ResizableWizardDialog;
 import org.eclipse.team.internal.ccvs.ui.wizards.RestoreFromRepositoryWizard;
 
 /**
  * @author Administrator
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class RestoreFromRepositoryAction extends WorkspaceTraversalAction {
 
 	/*
 	 * This class handles the output from "cvs log -R ..." where -R
 	 * indicates that only the RCS file name is to be returned. Files
 	 * that have been deleted will be in the Attic. The Attic may also
 	 * contains files that exist on a branch but not in HEAD
 	 */
 	class AtticLogListener extends CommandOutputListener {
 		private static final String ATTIC = "Attic"; //$NON-NLS-1$
 		private static final String RCS_FILE_POSTFIX = ",v"; //$NON-NLS-1$
 		private static final String LOGGING_PREFIX = "Logging "; //$NON-NLS-1$
 		ICVSFolder currentFolder;
 		List atticFiles = new ArrayList();
 		
 		public IStatus messageLine(
 					String line,
 					ICVSRepositoryLocation location,
 					ICVSFolder commandRoot,
 					IProgressMonitor monitor) {
 			
			// Find all RCS file names that contain "Attic"
			int index = line.indexOf(ATTIC);
			if (index == -1) return OK;
 			// Extract the file name and path from the RCS path
 			// String filePath = line.substring(index);
			int start = line.indexOf(Session.SERVER_SEPARATOR, index);
			String fileName = line.substring(start + 1);
			if (fileName.endsWith(RCS_FILE_POSTFIX)) {
				fileName = fileName.substring(0, fileName.length() - RCS_FILE_POSTFIX.length());
			}
			try {
				atticFiles.add(currentFolder.getFile(fileName));
			} catch (CVSException e) {
				return e.getStatus();
			}
 			return OK;
 		}
 		
 		public IStatus errorLine(
 			String line,
 			ICVSRepositoryLocation location,
 			ICVSFolder commandRoot,
 			IProgressMonitor monitor) {
 			
 			CVSRepositoryLocation repo = (CVSRepositoryLocation)location;
 			String folderPath = repo.getServerMessageWithoutPrefix(line, SERVER_PREFIX);
 			if (folderPath != null) {
 				if (folderPath.startsWith(LOGGING_PREFIX)) {
 					folderPath = folderPath.substring(LOGGING_PREFIX.length());
 					try {
 						currentFolder = commandRoot.getFolder(folderPath);
 					} catch (CVSException e) {
 						return e.getStatus();
 					}
 					return OK;
 				}
 			}
 			return super.errorLine(line, location, commandRoot, monitor);
 		}
 
 		public ICVSFile[] getAtticFilePaths() {
 			return (ICVSFile[]) atticFiles.toArray(new ICVSFile[atticFiles.size()]);
 		}
 	}
 	
 	/**
 	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
 	 */
 	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
 		IContainer resource = (IContainer)getSelectedResources()[0];
 		ICVSFile[] files = fetchDeletedFiles(resource);
 		if (files == null) return;
 		if (files.length == 0) {
 			MessageDialog.openInformation(getShell(), Policy.bind("RestoreFromRepositoryAction.noFilesTitle"), Policy.bind("RestoreFromRepositoryAction.noFilesMessage", resource.getName())); //$NON-NLS-1$ //$NON-NLS-2$
 			return;
 		}
 		RestoreFromRepositoryWizard wizard = new RestoreFromRepositoryWizard(resource, files);
 		WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
 		dialog.setMinimumPageSize(350, 250);
 		dialog.open();
 	}
 
 	/**
 	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
 	 */
 	protected boolean isEnabled() throws TeamException {
 		IResource[] resources = getSelectedResources();
 		if (resources.length != 1) return false;
 		if (resources[0].getType() == IResource.FILE) return false;
 		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)resources[0]);
 		if (!folder.isCVSFolder()) return false;
 		return true;
 	}
 	
 	private ICVSFile[] fetchDeletedFiles(final IContainer parent) {
 		final ICVSFile[][] files = new ICVSFile[1][0];
 		files[0] = null;
 		try {
 			run(new IRunnableWithProgress() {
 				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					try {
 						ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(parent);
 						FolderSyncInfo info = folder.getFolderSyncInfo();
 						ICVSRepositoryLocation location = KnownRepositories.getInstance().getRepository(info.getRoot());
 						files[0] = fetchFilesInAttic(location, folder, monitor);
 					} catch (CVSException e) {
 						throw new InvocationTargetException(e);
 					}
 				}
 			}, true, PROGRESS_DIALOG);
 		} catch (InvocationTargetException e) {
 			handle(e);
 		} catch (InterruptedException e) {
 			return null;
 		}
 		return files[0];
 	}
 	
 	/*
 	 * Fetch the RCS paths (minus the Attic segment) of all files in the Attic.
 	 * This path includes the repository root path.
 	 */
 	private ICVSFile[] fetchFilesInAttic(ICVSRepositoryLocation location, ICVSFolder parent, IProgressMonitor monitor) throws CVSException {
 		monitor = Policy.monitorFor(monitor);
 		monitor.beginTask(null, 100);
 		AtticLogListener listener = new AtticLogListener();
 		Session session = new Session(location, parent, true /* output to console */);
 		session.open(Policy.subMonitorFor(monitor, 10), false /* read-only */);
 		try {
 			QuietOption quietness = CVSProviderPlugin.getPlugin().getQuietness();
 			try {
 				CVSProviderPlugin.getPlugin().setQuietness(Command.VERBOSE);
 				IStatus status = Command.LOG.execute(
 					session,
 					Command.NO_GLOBAL_OPTIONS, 
 					new LocalOption[] { Log.RCS_FILE_NAMES_ONLY },
 					new ICVSResource[] { parent }, listener,
 					Policy.subMonitorFor(monitor, 90));
 				if (status.getCode() == CVSStatus.SERVER_ERROR) {
 					throw new CVSServerException(status);
 				}
 			} finally {
 				CVSProviderPlugin.getPlugin().setQuietness(quietness);
 				monitor.done();
 			}
 		} finally {
 			session.close();
 		}
 		return listener.getAtticFilePaths();
 	}
 }

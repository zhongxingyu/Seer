 /*******************************************************************************
 * Cvopyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.commands;
 
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.client.model.exceptions.NoChangesOnServerException;
 import org.eclipse.emf.emfstore.client.model.observers.UpdateObserver;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.client.ui.dialogs.UpdateDialog;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This handlers handles UpdateWorkspace command. This command is shown in UC
  * View context menu only for Projects
  * 
  * @author Hodaie
  * @author Shterev
  */
 public class UpdateProjectHandler extends ServerRequestCommandHandler implements UpdateObserver {
 
 	private Usersession usersession;
 
 	/**
 	 * Default constructor.
 	 */
 	public UpdateProjectHandler() {
 		setTaskTitle("Update project...");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object run() throws EmfStoreException {
 		ProjectSpace projectSpace = getProjectSpace();
 		if (projectSpace == null) {
 			ProjectSpace activeProjectSpace = WorkspaceManager.getInstance().getCurrentWorkspace()
 				.getActiveProjectSpace();
 			if (activeProjectSpace == null) {
 				MessageDialog.openInformation(getShell(), "Information", "You must select the Project");
 				return null;
 			}
 			projectSpace = activeProjectSpace;
 		}
 
 		update(projectSpace);
 
 		return null;
 	}
 
 	/**
 	 * Updates the {@link ProjectSpace}.
 	 * 
 	 * @param projectSpace
 	 *            the target project space
 	 * @throws EmfStoreException
 	 *             if any.
 	 */
 	public void update(final ProjectSpace projectSpace) throws EmfStoreException {
 		usersession = projectSpace.getUsersession();
 		if (usersession == null) {
 			MessageDialog.openInformation(getShell(), null,
 				"This project is not yet shared with a server, you cannot update.");
 			return;
 		}
 
 		try {
 			projectSpace.getBaseVersion();
 			projectSpace.update(VersionSpec.HEAD_VERSION, UpdateProjectHandler.this);
 
 			// explicitly refresh the decorator since no simple attribute has
 			// been changed
 			// (as opposed to committing where the dirty property is being set)
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					PlatformUI.getWorkbench().getDecoratorManager()
 						.update("org.eclipse.emf.emfstore.client.ui.decorators.VersionDecorator");
 				}
 			});
 		} catch (ChangeConflictException e1) {
 			handleChangeConflictException(e1);
 		} catch (NoChangesOnServerException e) {
 			MessageDialog.openInformation(getShell(), "No need to update",
 				"Your project is up to date, you do not need to update.");
 		}
 	}
 
 	private void handleChangeConflictException(ChangeConflictException conflictException) {
 		ProjectSpace projectSpace = conflictException.getProjectSpace();
 		try {
 			PrimaryVersionSpec targetVersion = projectSpace.resolveVersionSpec(VersionSpec.HEAD_VERSION);
 			projectSpace.merge(targetVersion, new MergeProjectHandler(conflictException));
 		} catch (EmfStoreException e) {
 			WorkspaceUtil.logException("Exception when merging the project!", e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean inspectChanges(ProjectSpace projectSpace, List<ChangePackage> changePackages) {
 		UpdateDialog updateDialog = new UpdateDialog(getShell(), projectSpace, changePackages);
 		int returnCode = updateDialog.open();
 
 		if (returnCode == Window.OK) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.observers.UpdateObserver#updateCompleted()
 	 */
 	public void updateCompleted(ProjectSpace projectSpace) {
 
 	}
 
 }

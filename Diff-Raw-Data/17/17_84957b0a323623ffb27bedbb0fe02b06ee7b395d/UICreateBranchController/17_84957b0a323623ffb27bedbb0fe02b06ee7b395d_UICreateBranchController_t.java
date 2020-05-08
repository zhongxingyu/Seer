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
 package org.eclipse.emf.emfstore.internal.client.ui.controller;
 
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.common.model.ESModelElementIdToEObjectMapping;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.CancelOperationException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESLocalProjectImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.client.ui.common.RunInUI;
 import org.eclipse.emf.emfstore.internal.client.ui.dialogs.BranchSelectionDialog;
 import org.eclipse.emf.emfstore.internal.client.ui.dialogs.CommitDialog;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ESModelElementIdToEObjectMappingImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESChangePackageImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.exceptions.ESUpdateRequiredException;
 import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * UIController for branch creation. Slightly modified copy of the commit
  * controller
  * 
  * @author wesendon
  * 
  */
 public class UICreateBranchController extends
 	AbstractEMFStoreUIController<ESPrimaryVersionSpec> implements
 	ESCommitCallback {
 
 	private final ProjectSpace projectSpace;
 	private int dialogReturnValue;
 	private BranchVersionSpec branch;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that should be used during the
 	 *            creation of the branch
 	 * @param projectSpace
 	 *            the project space for which to create a branch for
 	 */
 	public UICreateBranchController(Shell shell, ESLocalProject projectSpace) {
 		this(shell, projectSpace, null);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that should be used during the
 	 *            creation of the branch
 	 * @param localProject
 	 *            the project space for which to create a branch for
 	 * @param branch
 	 *            the branch to be committed
 	 */
 	public UICreateBranchController(Shell shell, ESLocalProject localProject,
 		BranchVersionSpec branch) {
 		super(shell, true, true);
 		this.projectSpace = ((ESLocalProjectImpl) localProject)
 			.toInternalAPI();
 		this.branch = branch;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback#noLocalChanges(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace)
 	 */
 	public void noLocalChanges(ESLocalProject projectSpace) {
 		RunInUI.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				MessageDialog.openInformation(getShell(), null,
 					"No local changes in your project. No need to commit.");
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback#baseVersionOutOfDate(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace)
 	 */
 	public boolean baseVersionOutOfDate(final ESLocalProject projectSpace,
 		final IProgressMonitor progressMonitor) {
 
 		final String message = "Your project is outdated, you need to update before branching. Do you want to update now?";
 		return RunInUI.runWithResult(new Callable<Boolean>() {
 
 			public Boolean call() throws Exception {
 				boolean shouldUpdate = MessageDialog.openConfirm(getShell(),
 					"Confirmation", message);
 				if (shouldUpdate) {
 					ESPrimaryVersionSpec baseVersion = UICreateBranchController.this.projectSpace
 						.getBaseVersion().toAPI();
 					ESPrimaryVersionSpec version = new UIUpdateProjectController(
 						getShell(), projectSpace)
 						.executeSub(progressMonitor);
 					if (version.equals(baseVersion)) {
 						return false;
 					}
 
 				}
 				return shouldUpdate;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback#inspectChanges(org.eclipse.emf.emfstore.internal.client.model.ProjectSpace,
 	 *      org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage)
 	 */
 
 	public boolean inspectChanges(
 		ESLocalProject localProject,
 		final ESChangePackage changePackage,
 		ESModelElementIdToEObjectMapping idToEObjectMapping) {
 
 		ESChangePackageImpl internalChangePackage = (ESChangePackageImpl) changePackage;
 		ESLocalProjectImpl localProjectImpl = (ESLocalProjectImpl) localProject;
 
 		final CommitDialog commitDialog = new CommitDialog(getShell(),
 			internalChangePackage.toInternalAPI(),
 			localProjectImpl.toInternalAPI(),
 			((ESModelElementIdToEObjectMappingImpl) idToEObjectMapping).toInternalAPI());
 
 		dialogReturnValue = RunInUI.runWithResult(new Callable<Integer>() {
 			public Integer call() throws Exception {
 				return commitDialog.open();
 			}
 		});
 
 		if (dialogReturnValue == Dialog.OK) {
 			RunESCommand.run(new Callable<Void>() {
 				public Void call() throws Exception {
 					LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 					logMessage.setAuthor(projectSpace
 						.getUsersession().getUsername());
 					logMessage.setClientDate(new Date());
					logMessage.setMessage(commitDialog.getLogText());
 					changePackage.setLogMessage(logMessage.toAPI());
 					return null;
 				}
 			});
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.controller.AbstractEMFStoreUIController#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public ESPrimaryVersionSpec doRun(final IProgressMonitor progressMonitor)
 		throws ESException {
 		try {
 			if (branch == null) {
 				branch = branchSelection(projectSpace);
 			}
 			// TODO OTS
 			PrimaryVersionSpec commitToBranch = projectSpace.commitToBranch(
 				branch,
 				null,
 				UICreateBranchController.this,
 				progressMonitor);
 			return commitToBranch.toAPI();
 		} catch (ESUpdateRequiredException e) {
 			// project is out of date and user canceled update
 			// ignore
 		} catch (final ESException e) {
 			if (e instanceof CancelOperationException) {
 				return null;
 			}
 			WorkspaceUtil.logException(e.getMessage(), e);
 			RunInUI.run(new Callable<Void>() {
 				public Void call() throws Exception {
 					MessageDialog.openError(getShell(), "Create Branch failed",
 						"Create Branch failed: " + e.getMessage());
 					return null;
 				}
 			});
 		}
 
 		return null;
 	}
 
 	private BranchVersionSpec branchSelection(final ProjectSpace projectSpace)
 		throws ESException {
 		final List<BranchInfo> branches = projectSpace.getBranches();
 
 		@SuppressWarnings("static-access")
 		String branch = new RunInUI.WithException()
 			.runWithResult(new Callable<String>() {
 
 				public String call() throws Exception {
 					BranchSelectionDialog.Creation dialog = new BranchSelectionDialog.Creation(
 						getShell(), branches);
 					dialog.setBlockOnOpen(true);
 
 					if (dialog.open() != Dialog.OK
 						|| dialog.getNewBranch() == null
 						|| dialog.getNewBranch().equals("")) {
 						throw new CancelOperationException(
 							"No Branch specified");
 					}
 					return dialog.getNewBranch();
 				}
 			});
 
 		return Versions.createBRANCH(branch);
 	}
 }

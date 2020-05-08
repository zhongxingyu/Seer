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
 
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESProject;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.CancelOperationException;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.client.ui.common.RunInUI;
 import org.eclipse.emf.emfstore.internal.client.ui.dialogs.BranchSelectionDialog;
 import org.eclipse.emf.emfstore.internal.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESBranchInfo;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * UI controller for checking out a project.
  * 
  * @author ovonwesen
  * @author emueller
  */
 public class UICheckoutController extends AbstractEMFStoreUIController<ESProject> {
 
 	private ESUsersession session;
 	private ESPrimaryVersionSpec versionSpec;
 	private ESRemoteProject remoteProject;
 	private boolean askForBranch;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 */
 	public UICheckoutController(Shell shell, ESRemoteProject remoteProject) {
 		super(shell, true, true);
 		this.remoteProject = remoteProject;
 		this.askForBranch = false;
 		this.versionSpec = null;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 * @param askForBranch
 	 *            whether to ask for a branch from which the checkout should happen
 	 */
 	public UICheckoutController(Shell shell, ESRemoteProject remoteProject, boolean askForBranch) {
 		this(shell, remoteProject);
 		this.askForBranch = askForBranch;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param versionSpec
 	 *            the {@link ESPrimaryVersionSpec} that identifies a specific version to be checked out
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 */
 	public UICheckoutController(Shell shell, ESPrimaryVersionSpec versionSpec, ESRemoteProject remoteProject) {
 		this(shell, remoteProject);
 		this.versionSpec = versionSpec;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param versionSpec
 	 *            the {@link ESPrimaryVersionSpec} that identifies a specific version to be checked out
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 * @param askForBranch
 	 *            whether to ask for a branch from which the checkout should happen
 	 */
 	public UICheckoutController(Shell shell, ESPrimaryVersionSpec versionSpec, ESRemoteProject remoteProject,
 		boolean askForBranch) {
 		this(shell, versionSpec, remoteProject);
 		this.askForBranch = askForBranch;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param session
 	 *            the {@link ESUsersession} that will be used to checkout the project
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 */
 	public UICheckoutController(Shell shell, ESUsersession session, ESRemoteProject remoteProject) {
 		this(shell, remoteProject);
 		this.session = session;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param session
 	 *            the {@link ESUsersession} that will be used to checkout the project
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 * @param askForBranch
 	 *            whether to ask for a branch from which the checkout should happen
 	 */
 	public UICheckoutController(Shell shell, ESUsersession session, ESRemoteProject remoteProject, boolean askForBranch) {
 		this(shell, session, remoteProject);
 		this.askForBranch = askForBranch;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param versionSpec
 	 *            the {@link ESPrimaryVersionSpec} that identifies a specific version to be checked out
 	 * @param session
 	 *            the {@link ESUsersession} that will be used to checkout the project
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 */
 	public UICheckoutController(Shell shell, ESPrimaryVersionSpec versionSpec, ESUsersession session,
 		ESRemoteProject remoteProject) {
 		this(shell, versionSpec, remoteProject);
 		this.session = session;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that will be used during checkout
 	 * @param remoteProject
 	 *            the {@link ESRemoteProject} to be checked out
 	 * @param versionSpec
 	 *            the {@link ESPrimaryVersionSpec} that identifies a specific version to be checked out
 	 * @param session
 	 *            the {@link ESUsersession} that will be used to checkout the project
 	 * @param askForBranch
 	 *            whether to ask for a branch from which the checkout should happen
 	 */
 	public UICheckoutController(Shell shell, ESPrimaryVersionSpec versionSpec, ESUsersession session,
 		ESRemoteProject remoteProject, boolean askForBranch) {
 		this(shell, versionSpec, session, remoteProject);
 		this.askForBranch = askForBranch;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.common.MonitoredEMFStoreAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public ESLocalProject doRun(IProgressMonitor progressMonitor) throws ESException {
 		try {
 
 			if (askForBranch && versionSpec == null) {
 				versionSpec = branchSelection(remoteProject, progressMonitor);
 			}
 
 			if (session != null) {
 				if (versionSpec == null) {
					return remoteProject.checkout(session, ESVersionSpec.FACTORY.createHEAD(), progressMonitor);
 				} else {
					return remoteProject.checkout(session, versionSpec, progressMonitor);
 				}
 			} else {
 				if (versionSpec == null) {
					return remoteProject.checkout(progressMonitor);
 				} else {
					return remoteProject.checkout(remoteProject.getServer().getLastUsersession(), versionSpec,
						progressMonitor);
 				}
 			}
 
 		} catch (final ESException e) {
 			if (e instanceof CancelOperationException) {
 				return null;
 			}
 			RunInUI.run(new Callable<Void>() {
 				public Void call() throws Exception {
 					WorkspaceUtil.logException(e.getMessage(), e);
 					MessageDialog.openError(getShell(), "Checkout failed",
 						"Checkout of project " + remoteProject.getProjectName() + " failed: " + e.getMessage());
 					return null;
 				}
 			});
 		}
 
 		return null;
 	}
 
 	private ESPrimaryVersionSpec branchSelection(ESRemoteProject remoteProject, IProgressMonitor monitor)
 		throws ESException {
 
 		final List<ESBranchInfo> branches;
 
 		if (session != null) {
 			branches = remoteProject.getBranches(session, monitor);
 		} else {
 			branches = remoteProject.getBranches(monitor);
 		}
 
 		ESBranchInfo result = RunInUI.WithException.runWithResult(new Callable<ESBranchInfo>() {
 			public ESBranchInfo call() throws Exception {
 
 				BranchSelectionDialog.CheckoutSelection dialog = new BranchSelectionDialog.CheckoutSelection(
 					getShell(), branches);
 				dialog.setBlockOnOpen(true);
 
 				if (dialog.open() != Dialog.OK || dialog.getResult() == null) {
 					throw new CancelOperationException("No Branch specified");
 				}
 				return dialog.getResult();
 
 			}
 		});
 
 		return result.getHead();
 	}
 }

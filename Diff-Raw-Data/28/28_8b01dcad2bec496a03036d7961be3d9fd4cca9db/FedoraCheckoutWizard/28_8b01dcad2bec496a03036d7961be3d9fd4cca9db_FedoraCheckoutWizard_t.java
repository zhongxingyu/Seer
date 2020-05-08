 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.egit.core.RepositoryCache;
 import org.eclipse.egit.core.RepositoryUtil;
 import org.eclipse.egit.core.op.CloneOperation;
 import org.eclipse.egit.core.op.ConnectProviderOperation;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.RefUpdate;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.transport.URIish;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
 
 /**
  * Wizard to checkout package content from Fedora Git.
  *
  */
 public class FedoraCheckoutWizard extends Wizard implements IImportWizard {
 
 	private SelectModulePage page;
 	private Repository gitRepository;
 
 	/**
 	 * Creates the wizards and sets that it needs progress monitor.
 	 */
 	public FedoraCheckoutWizard() {
 		super();
 		// required to show progress info of clone job
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public void addPages() {
 		// get Fedora username from cert
 		page = new SelectModulePage();
 		addPage(page);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	private String getGitURL() {
 		String username = FedoraHandlerUtils.getUsernameFromCert();
 		String packageName = page.getPackageName();
 		if (username.equals("anonymous")) { //$NON-NLS-1$
 			return "git://pkgs.fedoraproject.org/" + packageName + ".git"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			return "ssh://" + username + "@pkgs.fedoraproject.org/" //$NON-NLS-1$ //$NON-NLS-2$
 					+ packageName + ".git"; //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// nothing
 	}
 
 	@Override
 	public boolean performFinish() {
 		try {
 			final URIish uri = new URIish(getGitURL());
 			final CloneOperation clone = new CloneOperation(uri, true,
 					new ArrayList<Ref>(), new File(ResourcesPlugin
 							.getWorkspace().getRoot().getLocation().toFile(),
 							page.getPackageName()), Constants.R_HEADS + Constants.MASTER,
 					"origin"); //$NON-NLS-1$
 
 			// Bail out if project already exists
 			IResource project = ResourcesPlugin.getWorkspace().getRoot()
 					.findMember(new Path(page.getPackageName()));
 			if (project != null && project.exists()) {
 				final String errorMessage = NLS.bind(
 						Messages.fedoraCheckoutWizard_projectExists, project.getName());
 				ErrorDialog
 						.openError(
 								getShell(),
 								getWindowTitle(),
 								Messages.fedoraCheckoutWizard_cloneFail,
 								new Status(
 										IStatus.ERROR,
 										org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
 										0, errorMessage, null));
 				// let's give user a chance to fix this minor problem
 				return false;
 			}
 
 			// Perform clone in ModalContext thread with progress
 			// reporting on the wizard.
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				@Override
 				public void run(IProgressMonitor monitor)
 						throws InvocationTargetException, InterruptedException {
 					clone.run(monitor);
 					if (monitor.isCanceled())
 						throw new InterruptedException();
 				}
 			});
 			// Add cloned repository to the list of Git repositories so that it
 			// shows up in the Git repositories view.
 			final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
 			config.addConfiguredRepository(clone.getGitDir());
 			IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(page.getPackageName());
 			newProject.create(null);
 			newProject.open(null);
 			ConnectProviderOperation connect = new ConnectProviderOperation(
 					newProject);
 			connect.execute(null);
 			
 			// Find repo we've just created and set gitRepo
 			RepositoryCache repoCache = org.eclipse.egit.core.Activator
 					.getDefault().getRepositoryCache();
 			try {
				this.gitRepository = repoCache.lookupRepository(clone.getGitDir());
 			} catch (IOException ex) {
				// Repo lookup failed, no way we can continue.
				ErrorDialog
				.openError(
						getShell(),
						getWindowTitle(),
						Messages.fedoraCheckoutWizard_cloneFail,
						new Status(
								IStatus.ERROR,
								org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
								0, ex.getMessage(), null));
				return false;
 			}
 			
 			// Create local branches
 			getContainer().run(true, true, new IRunnableWithProgress() {
 				@Override
 				public void run(IProgressMonitor monitor)
 						throws InvocationTargetException, InterruptedException {
 					try {
 						createLocalBranches(monitor);
 					} catch (CoreException e) {
						ErrorDialog
						.openError(
								getShell(),
								getWindowTitle(),
								Messages.fedoraCheckoutWizard_cloneFail,
								new Status(
										IStatus.ERROR,
										org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
										0, e.getMessage(), null));
						return;
 					}
 					if (monitor.isCanceled())
 						throw new InterruptedException();
 				}
 			});
 			
 			// Finally show the Git Repositories view for convenience
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().showView(
 							"org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
 			return true;
 		} catch (InterruptedException e) {
 			MessageDialog.openInformation(getShell(), Messages.fedoraCheckoutWizard_cloneFail, 
 					Messages.fedoraCheckoutWizard_cloneCancel);
 			return false;
 		} catch (Exception e) {
 			org.fedoraproject.eclipse.packager.git.Activator.handleError(
 					Messages.fedoraCheckoutWizard_cloneFail, e, true);
 			return false;
 		}
 	}
 	
 	/**
 	 * Create local branches based on remotes. Don't do checkouts.
 	 * 
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void createLocalBranches(IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask(Messages.fedoraCheckoutWizard_createLocalBranchesJob,
 				IProgressMonitor.UNKNOWN);
 
 		try {
 			Map<String, Ref> remotes = this.gitRepository.getRefDatabase()
 					.getRefs(Constants.R_REMOTES);
 			Set<String> keyset = remotes.keySet();
 			String branch;
 			for (String key : keyset) {
 				// use shortenRefName() to get rid of refs/*/ prefix
 				Ref origRef = remotes.get(key);
 				branch = this.gitRepository.shortenRefName(origRef
 						.getName());
 				// omit "origin
 				branch = branch.substring("origin".length()); //$NON-NLS-1$
 				// create local branches
 				String newRefName = Constants.R_HEADS + branch;
 
 				RefUpdate updateRef = this.gitRepository.updateRef(newRefName);
 				ObjectId startAt = new RevWalk(this.gitRepository).parseCommit(this.gitRepository
 							.resolve(origRef.getName()));
 				updateRef.setNewObjectId(startAt);
 				updateRef.setRefLogMessage(
 						"branch: Created from " + origRef.getName(), false); //$NON-NLS-1$
 				updateRef.update();
 			}
 		} catch (IOException ioException) {
 			ioException.printStackTrace();
 		}
 	}
 }

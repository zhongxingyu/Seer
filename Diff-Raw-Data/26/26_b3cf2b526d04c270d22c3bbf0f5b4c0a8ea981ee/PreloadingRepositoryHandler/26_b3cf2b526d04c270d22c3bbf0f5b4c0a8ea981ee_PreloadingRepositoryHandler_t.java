 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.equinox.internal.p2.ui.sdk;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.*;
 import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
 import org.eclipse.equinox.internal.provisional.p2.ui.ProvUI;
 import org.eclipse.equinox.internal.provisional.p2.ui.QueryableMetadataRepositoryManager;
 import org.eclipse.equinox.internal.provisional.p2.ui.policy.Policy;
 import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * PreloadingRepositoryHandler provides background loading of
  * repositories before executing the provisioning handler.
  * 
  * @since 3.5
  */
 abstract class PreloadingRepositoryHandler extends AbstractHandler {
 
 	Job loadJob = null;
 
 	/**
 	 * The constructor.
 	 */
 	public PreloadingRepositoryHandler() {
 		// constructor
 	}
 
 	/**
	 * Execute the command.
 	 */
 	public Object execute(ExecutionEvent event) {
 		try {
			final String profileId = ProvSDKUIActivator.getSelfProfileId();
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					doExecuteAndLoad(profileId, preloadRepositories());
				}
			});

 		} catch (ProvisionException e) {
 			MessageDialog.openInformation(null, ProvSDKMessages.Handler_SDKUpdateUIMessageTitle, ProvSDKMessages.Handler_CannotLaunchUI);
 		}
 		return null;
 	}
 
	void doExecuteAndLoad(final String profileId, boolean preloadRepositories) {
 		final QueryableMetadataRepositoryManager queryableManager = new QueryableMetadataRepositoryManager(Policy.getDefault(), false);
 		if (preloadRepositories) {
 			if (loadJob == null) {
 
 				loadJob = new Job(ProvSDKMessages.InstallNewSoftwareHandler_LoadRepositoryJobLabel) {
 
 					protected IStatus run(IProgressMonitor monitor) {
 						queryableManager.loadAll(monitor);
 						return Status.OK_STATUS;
 					}
 
 				};
 				loadJob.addJobChangeListener(new JobChangeAdapter() {
 					public void done(IJobChangeEvent event) {
 						loadJob = null;
 						if (PlatformUI.isWorkbenchRunning())
 							if (event.getResult().isOK()) {
 								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 									public void run() {
 										doExecute(profileId, queryableManager);
 									}
 								});
 							}
 					}
 				});
 				loadJob.setUser(true);
 				loadJob.schedule();
 			}
 			return;
 		}
 		doExecute(profileId, queryableManager);
 	}
 
 	protected abstract void doExecute(String profileId, QueryableMetadataRepositoryManager manager);
 
 	protected boolean preloadRepositories() {
 		return true;
 	}
 
 	/**
 	 * Return a shell appropriate for parenting dialogs of this handler.
 	 * @return a Shell
 	 */
 	protected Shell getShell() {
 		return ProvUI.getDefaultParentShell();
 	}
 }

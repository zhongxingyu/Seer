 /*******************************************************************************
  * Copyright (c) 2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.local.internal.handlers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 import org.fedoraproject.eclipse.packager.api.FedoraPackager;
 import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
 import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
 import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerText;
 import org.fedoraproject.eclipse.packager.rpm.RPMPlugin;
 import org.fedoraproject.eclipse.packager.rpm.RpmText;
 import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand;
 import org.fedoraproject.eclipse.packager.rpm.api.RpmBuildCommand.BuildType;
 import org.fedoraproject.eclipse.packager.rpm.api.errors.RpmBuildCommandException;
 import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
 import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
 import org.eclipse.swt.widgets.Shell;
 
 
 /**
  * Handler for preparing local sources for local build (prior building it). This is useful for testing
  * if patches apply properly.
  * This is the modified version of org.fedoraproject.eclipse.packager.rpm.internal.handlers.PrepHandler.java
  * to make it work with Local Fedora Packager Project since in the local version
  * downloading source from lookaside cache is not applicable
  */
 public class PrepHandler extends FedoraPackagerAbstractHandler {
 
 	@Override
 	public Object execute(final ExecutionEvent event) throws ExecutionException {
 
 		final Shell shell = getShell(event);
 		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		final IProjectRoot localFedoraProjectRoot;
 
 		IResource eventResource = FedoraHandlerUtils.getResource(event);
 		try {
 			localFedoraProjectRoot = FedoraPackagerUtils.getProjectRoot(eventResource);
 		} catch (InvalidProjectRootException e) {
 			logger.logError(LocalFedoraPackagerText.invalidLocalFedoraProjectRootError, e);
 			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
 					LocalFedoraPackagerText.invalidLocalFedoraProjectRootError);
 			return null;
 		}
 		FedoraPackager fp = new FedoraPackager(localFedoraProjectRoot);
 		final RpmBuildCommand prepCommand;
 		try {
 			// get RPM build command in order to produce an SRPM
 			prepCommand = (RpmBuildCommand) fp
 					.getCommandInstance(RpmBuildCommand.ID);
 		} catch (FedoraPackagerCommandNotFoundException e) {
 			logger.logError(e.getMessage(), e);
 			FedoraHandlerUtils.showErrorDialog(shell,
 					localFedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
 			return null;
 		} catch (FedoraPackagerCommandInitializationException e) {
 			logger.logError(e.getMessage(), e);
 			FedoraHandlerUtils.showErrorDialog(shell,
 					localFedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
 			return null;
 		}
 
 		// Need to nest jobs into this job for it to show up properly in the UI
 		// in terms of progress
 		Job job = new Job(localFedoraProjectRoot.getProductStrings().getProductName()) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				// Do the prep job
 				Job prepJob = new Job(localFedoraProjectRoot.getProductStrings().getProductName()) {
 					@Override
 					protected IStatus run(IProgressMonitor monitor) {
 						try {
 							monitor.beginTask(
 									RpmText.PrepHandler_prepareSourcesForBuildMsg,
 									IProgressMonitor.UNKNOWN);
 							List<String> nodeps = new ArrayList<String>(1);
 							nodeps.add(RpmBuildCommand.NO_DEPS);
 								prepCommand.buildType(BuildType.PREP)
 										.flags(nodeps).call(monitor);
 						} catch (CommandMisconfiguredException e) {
 							// This shouldn't happen, but report error
 							// anyway
 							logger.logError(e.getMessage(), e);
 							return FedoraHandlerUtils.errorStatus(
 									RPMPlugin.PLUGIN_ID, e.getMessage(), e);
 						} catch (CommandListenerException e) {
 							// There are no command listeners registered, so
 							// shouldn't
 							// happen. Do something reasonable anyway.
 							logger.logError(e.getMessage(), e);
 							return FedoraHandlerUtils.errorStatus(
 									RPMPlugin.PLUGIN_ID, e.getMessage(), e);
 						} catch (RpmBuildCommandException e) {
 							logger.logError(e.getMessage(), e.getCause());
 							return FedoraHandlerUtils.errorStatus(
 									RPMPlugin.PLUGIN_ID, e.getMessage(),
 									e.getCause());
 						} catch (IllegalArgumentException e) {
 							// nodeps flags can't be null
 						} finally {
 							monitor.done();
 						}
 						return Status.OK_STATUS;
 					}
 				};
 				prepJob.setUser(true);
 				prepJob.schedule();
 				try {
 					// wait for job to finish
 					prepJob.join();
 				} catch (InterruptedException e1) {
 					throw new OperationCanceledException();
 				}
 				return prepJob.getResult();
 			}
 
 		};
 		job.setSystem(true); // suppress UI. That's done in encapsulated jobs.
 		job.schedule();
 		return null;
 	}
 
 }

 package org.fedoraproject.eclipse.packager.git.internal.handlers;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.FedoraPackagerText;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.api.FedoraPackager;
 import org.fedoraproject.eclipse.packager.api.FedoraPackagerAbstractHandler;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
 import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
 import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;
 import org.fedoraproject.eclipse.packager.git.api.ConvertLocalResult;
 import org.fedoraproject.eclipse.packager.git.api.ConvertLocalToRemoteCommand;
 import org.fedoraproject.eclipse.packager.git.api.errors.LocalProjectConversionFailedException;
 import org.fedoraproject.eclipse.packager.git.api.errors.RemoteAlreadyExistsException;
 import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
 import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
 
 /**
  * Class responsible for converting local project to the main fedora packager
  *
  */
 public class ConvertLocalToRemoteHandler extends FedoraPackagerAbstractHandler {
 
 	/**
 	 * Converts the Local project to a remote fedora packager project Adds the
 	 * remote repository to the local git, creates the corresponding branches
 	 * locally, merges the remote master with the local one, sets the properties
 	 * of the local to main fedora packager
 	 *
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		final Shell shell = getShell(event);
 		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		final IProjectRoot localfedoraProjectRoot;
 		try {
 			IResource eventResource = FedoraHandlerUtils.getResource(event);
 			localfedoraProjectRoot = FedoraPackagerUtils
 					.getProjectRoot(eventResource);
 		} catch (InvalidProjectRootException e) {
 			logger.logError(FedoraPackagerText.invalidFedoraProjectRootError, e);
 			FedoraHandlerUtils.showErrorDialog(shell, "Error", //$NON-NLS-1$
 					FedoraPackagerText.invalidFedoraProjectRootError);
 			return null;
 		}
 
 		final FedoraPackager packager = new FedoraPackager(
 				localfedoraProjectRoot);
 
 		// Do the converting
 		Job job = new Job(
 				FedoraPackagerGitText.ConvertLocalToRemoteHandler_taskName) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				monitor.beginTask(
 						FedoraPackagerGitText.ConvertLocalToRemoteHandler_taskName,
 						IProgressMonitor.UNKNOWN);
 				final ConvertLocalToRemoteCommand convertCmd;
 				ConvertLocalResult result;
 
 				try {
 					// Get ConvertLocalToRemoteCommand from Fedora packager
 					// registry
 					convertCmd = (ConvertLocalToRemoteCommand) packager
 							.getCommandInstance(ConvertLocalToRemoteCommand.ID);
 				} catch (FedoraPackagerCommandNotFoundException e) {
 					logger.logError(e.getMessage(), e);
 					FedoraHandlerUtils.showErrorDialog(shell,
 							localfedoraProjectRoot.getProductStrings()
 									.getProductName(), e.getMessage());
 					return null;
 				} catch (FedoraPackagerCommandInitializationException e) {
 					logger.logError(e.getMessage(), e);
 					FedoraHandlerUtils.showErrorDialog(shell,
 							localfedoraProjectRoot.getProductStrings()
 									.getProductName(), e.getMessage());
 					return null;
 				}
 				try {
 					result = convertCmd.call(monitor);
 					String message = null;
 					message = NLS
 							.bind(FedoraPackagerGitText.ConvertLocalToRemoteHandler_information,
 									localfedoraProjectRoot.getPackageName());
 					String finalMessage = result.getHumanReadableMessage(message);
 					FedoraHandlerUtils
 							.showInformationDialog(
 									shell,
 									FedoraPackagerGitText.ConvertLocalToRemoteHandler_notificationTitle,
 									finalMessage);
 					return Status.OK_STATUS;
 
 				} catch (CommandMisconfiguredException e) {
 					logger.logError(e.getMessage(), e);
 					return FedoraHandlerUtils.errorStatus(
 							PackagerPlugin.PLUGIN_ID, e.getMessage(), e);
 				} catch (CommandListenerException e) {
 					logger.logError(e.getMessage(), e);
 					return FedoraHandlerUtils.errorStatus(
 							PackagerPlugin.PLUGIN_ID, e.getMessage(), e);

 				} catch (RemoteAlreadyExistsException e) {
					logger.logError(e.getCause().getMessage(), e);
 					return FedoraHandlerUtils
 							.errorStatus(
 									PackagerPlugin.PLUGIN_ID,
 									NLS.bind(
 											FedoraPackagerGitText.ConvertLocalToRemoteHandler_failToConvert,
 											localfedoraProjectRoot
 													.getPackageName(), e
													.getCause().getMessage()));
 				} catch (LocalProjectConversionFailedException e) {
 					logger.logError(e.getCause().getMessage(), e);
 					return FedoraHandlerUtils
 							.errorStatus(
 									PackagerPlugin.PLUGIN_ID,
 									NLS.bind(
 											FedoraPackagerGitText.ConvertLocalToRemoteHandler_failToConvert,
 											localfedoraProjectRoot
 													.getPackageName(), e
 													.getCause().getMessage()));
 				}
 
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 		return null;
 	}
 }

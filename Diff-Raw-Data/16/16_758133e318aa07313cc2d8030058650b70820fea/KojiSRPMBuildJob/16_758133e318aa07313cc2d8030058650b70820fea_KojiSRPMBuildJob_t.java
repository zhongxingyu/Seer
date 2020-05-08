 package org.fedoraproject.eclipse.packager.koji.api;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.IJobChangeListener;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.api.FedoraPackager;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
 import org.fedoraproject.eclipse.packager.koji.KojiMessageDialog;
 import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
 import org.fedoraproject.eclipse.packager.koji.KojiText;
 import org.fedoraproject.eclipse.packager.koji.KojiUrlUtils;
 import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.api.errors.TagSourcesException;
import org.fedoraproject.eclipse.packager.api.errors.UnpushedChangesException;
 import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
 import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
 import org.fedoraproject.eclipse.packager.utils.RPMUtils;
 
 /**
  * Job that uploads an SRPM to Koji and has Koji build an RPM from the SRPM.
  *
  */
 public class KojiSRPMBuildJob extends KojiBuildJob {
 
 	private Shell shell;
 	private IProjectRoot fedoraProjectRoot;
 	private BuildResult buildResult;
 	private URL kojiWebUrl;
 	private IPath srpmPath;
 
 	/**
 	 * @param name The name of the job.
 	 * @param shell The shell the job runs in.
 	 * @param fedoraProjectRoot The root of the project containing the SRPM being used.
 	 * @param srpmPath 
 	 */
 	public KojiSRPMBuildJob(String name, Shell shell, IProjectRoot fedoraProjectRoot, IPath srpmPath) {
 		super(name, shell, fedoraProjectRoot, true);
 		this.shell = shell;
 		this.fedoraProjectRoot = fedoraProjectRoot;
 		this.srpmPath = srpmPath;
 	}
 
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		monitor.beginTask(NLS.bind(
 				KojiText.KojiBuildHandler_pushBuildToKoji,
 				fedoraProjectRoot.getProductStrings().getBuildToolName()), 100);
 		monitor.worked(5);
 		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
 		final IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
 		final KojiBuildCommand kojiBuildCmd;
 		try {
 			// Get KojiBuildCommand from Fedora packager registry
 			kojiBuildCmd = (KojiBuildCommand) fp
 			.getCommandInstance(KojiBuildCommand.ID);
 		} catch (FedoraPackagerCommandNotFoundException e) {
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage(), e);
 		} catch (FedoraPackagerCommandInitializationException e) {
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage(), e);
 		}
 		
 		// build upload path
 		final String uploadPath = "cli-build/" + FedoraPackagerUtils.getUniqueIdentifier();  //$NON-NLS-1$
 		Job uploadJob = new KojiUploadSRPMJob(
 				fedoraProjectRoot.getProductStrings().getProductName(), shell, 
 				fedoraProjectRoot, srpmPath.toOSString(), uploadPath);
 		uploadJob.setUser(true);
 		uploadJob.schedule();
 		try {
 			// wait for SRPM upload to finish
 			uploadJob.join();
 		} catch (InterruptedException e1) {
 			throw new OperationCanceledException();
 		}
 		if (!uploadJob.getResult().isOK()) {
 			// bail if something failed
 			return uploadJob.getResult();
 		}
 		monitor.worked(5);
 		IKojiHubClient kojiClient;
 		try {
 			kojiClient = getHubClient();
 		} catch (MalformedURLException e) {
 			logger.logError(NLS.bind(
 					KojiText.KojiBuildHandler_invalidHubUrl,
 					fedoraProjectRoot.getProductStrings().getBuildToolName()), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
 					NLS.bind(KojiText.KojiBuildHandler_invalidHubUrl,
 							fedoraProjectRoot.getProductStrings().getBuildToolName()),
 							e);
 		}
 		kojiBuildCmd.setKojiClient(kojiClient);
 		kojiBuildCmd.sourceLocation(uploadPath + "/" + srpmPath.lastSegment()); //$NON-NLS-1$
 		String nvr;
 		try {
 			nvr = RPMUtils.getNVR(fedoraProjectRoot);
 		} catch (IOException e) {
 			logger.logError(KojiText.KojiBuildHandler_errorGettingNVR,
 					e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
 					KojiText.KojiBuildHandler_errorGettingNVR, e);
 		}
 		kojiBuildCmd.buildTarget(projectBits.getTarget()).nvr(nvr)
 		.isScratchBuild(true);
 		logger.logDebug(NLS.bind("", //$NON-NLS-1$
 				KojiBuildCommand.class.getName()));
 		// Call build command
 		try {
 			buildResult = kojiBuildCmd.call(monitor);
 		} catch (CommandMisconfiguredException e) {
 			// This shouldn't happen, but report error anyway
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (BuildAlreadyExistsException e) {
 			logger.logDebug(e.getMessage(), e);
 			FedoraHandlerUtils.showInformationDialog(shell,
 					fedoraProjectRoot.getProductStrings().getProductName(),
 					e.getMessage());
 			return Status.OK_STATUS;
 		} catch (UnpushedChangesException e) {
 			logger.logDebug(e.getMessage(), e);
 			FedoraHandlerUtils.showInformationDialog(shell,
 					fedoraProjectRoot.getProductStrings().getProductName(),
 					e.getMessage());
 			return Status.OK_STATUS;
 		} catch (TagSourcesException e) {
 			// something failed while tagging sources
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (CommandListenerException e) {
 			// This shouldn't happen, but report error anyway
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (KojiHubClientLoginException e) {
 			e.printStackTrace();
 			// Check if certs were missing
 			if (e.isCertificateMissing()) {
 				String msg = NLS
 				.bind(KojiText.KojiBuildHandler_missingCertificatesMsg,
 						fedoraProjectRoot.getProductStrings().getDistributionName());
 				logger.logError(msg, e);
 				return FedoraHandlerUtils.errorStatus(
 						KojiPlugin.PLUGIN_ID, msg, e);
 			}
 			if (e.isCertificateExpired()) {
 				String msg = NLS
 				.bind(KojiText.KojiBuildHandler_certificateExpriredMsg,
 						fedoraProjectRoot.getProductStrings().getDistributionName());
 				logger.logError(msg, e);
 				return FedoraHandlerUtils.errorStatus(
 						KojiPlugin.PLUGIN_ID, msg, e);
 			}
 			// return some generic error
 			logger.logError(e.getMessage(), e);
 			return FedoraHandlerUtils.errorStatus(
 					KojiPlugin.PLUGIN_ID, e.getMessage(), e);
 		} catch (KojiHubClientException e) {
 			// return some generic error
 			String msg = NLS.bind(KojiText.KojiBuildHandler_unknownBuildError, e.getMessage());
 			logger.logError(msg, e);
 			return FedoraHandlerUtils.errorStatus(
 					KojiPlugin.PLUGIN_ID, msg, e);
 		}
 		// success
 		return Status.OK_STATUS;
 	}			
 	
 	/**
 	 * Create a job listener for the event {@code done}.
 	 *  
 	 * @return The job change listener.
 	 */
 	protected IJobChangeListener getJobChangeListener() {
 		final FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		String webUrl = PackagerPlugin.getStringPreference(FedoraPackagerPreferencesConstants.PREF_KOJI_WEB_URL);
 		if (webUrl == null) {
 			// use default
 			webUrl = FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL; 
 		}
 		try {
 			kojiWebUrl = new URL(webUrl);
 		} catch (MalformedURLException e) {
 			// nothing critical, use default koji URL instead and log the bogus
 			// Web url set in preferences.
 			logger.logError(NLS.bind(
 					KojiText.KojiBuildHandler_invalidKojiWebUrl,
 					fedoraProjectRoot.getProductStrings().getBuildToolName(), webUrl), e);
 			try {
 				kojiWebUrl = new URL(FedoraPackagerPreferencesConstants.DEFAULT_KOJI_WEB_URL);
 			} catch (MalformedURLException ignored) {};
 		}
 		IJobChangeListener listener = new JobChangeAdapter() {
 			
 			// We are only interested in the done event
 			@Override
 			public void done(IJobChangeEvent event) {
 				final IStatus jobStatus = event.getResult();
 				PlatformUI.getWorkbench().getDisplay().asyncExec(
 						new Runnable() {
 							@Override
 							public void run() {
 								// Only show response message dialog on success
 								if (jobStatus.isOK() && buildResult != null && buildResult.wasSuccessful()) {
 									FedoraPackagerLogger logger = FedoraPackagerLogger
 											.getInstance();
 									logger.logDebug(NLS
 											.bind(KojiText.KojiMessageDialog_buildResponseMsg,
 													fedoraProjectRoot.getProductStrings().getBuildToolName())
 											+ " " //$NON-NLS-1$
 											+ KojiUrlUtils.constructTaskUrl(
 													buildResult.getTaskId(),
 													kojiWebUrl));
 									// open dialog
 									getKojiMsgDialog(buildResult.getTaskId(),
 											kojiWebUrl).open();
 								}
 							}
 						});
 			}
 		};
 		return listener;
 	}
 	
 	/**
 	 * Create KojiMessageDialog based on taskId and kojiWebUrl.
 	 * 
 	 * @param taskId
 	 *            The task ID to use for the message.
 	 * @param kojiWebUrl
 	 *            The url to Koji Web without any parameters.
 	 * @return A new KojiMessageDialog for the given Web Url and task Id.
 	 */
 	public KojiMessageDialog getKojiMsgDialog(int taskId, URL kojiWebUrl) {
 		ImageDescriptor descriptor = KojiPlugin
 				.getImageDescriptor("icons/Artwork_DesignService_koji-icon-16.png"); //$NON-NLS-1$
 		Image titleImage = descriptor.createImage();
 		Image msgContentImage = KojiPlugin.getImageDescriptor("icons/koji.png") //$NON-NLS-1$
 				.createImage();
 		KojiMessageDialog msgDialog = new KojiMessageDialog(shell, NLS.bind(
 				KojiText.KojiBuildHandler_kojiBuild,
 				fedoraProjectRoot.getProductStrings().getBuildToolName()), titleImage,
 				MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL },
 				0, kojiWebUrl, taskId, NLS.bind(
 						KojiText.KojiMessageDialog_buildResponseMsg,
 						fedoraProjectRoot.getProductStrings().getBuildToolName()),
 				msgContentImage,
 				fedoraProjectRoot.getProductStrings().getBuildToolName());
 		return msgDialog;
 	}
 }

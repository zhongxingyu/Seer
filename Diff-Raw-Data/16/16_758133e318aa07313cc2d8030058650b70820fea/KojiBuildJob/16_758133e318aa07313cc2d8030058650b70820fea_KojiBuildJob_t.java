 package org.fedoraproject.eclipse.packager.koji.api;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.FedoraPackagerPreferencesConstants;
 import org.fedoraproject.eclipse.packager.FedoraPackagerText;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.api.FedoraPackager;
import org.fedoraproject.eclipse.packager.api.TagSourcesListener;
import org.fedoraproject.eclipse.packager.api.UnpushedChangesListener;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandInitializationException;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerCommandNotFoundException;
 import org.fedoraproject.eclipse.packager.koji.KojiPlugin;
 import org.fedoraproject.eclipse.packager.koji.KojiText;
 import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.api.errors.TagSourcesException;
import org.fedoraproject.eclipse.packager.api.errors.UnpushedChangesException;
 import org.fedoraproject.eclipse.packager.utils.FedoraHandlerUtils;
 import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
 import org.fedoraproject.eclipse.packager.utils.RPMUtils;
 
 /**
  * Job to make a Koji Build.
  *
  */
 public class KojiBuildJob extends Job {
 
 	private IProjectRoot fedoraProjectRoot;
 	private boolean isScratch; 
 	private Shell shell;
 	/**
 	 * @param name The name of the job.
 	 * @param shell The shell the job is run in.
 	 * @param fpr The root of the project being built.
 	 * @param scratch True if scratch, false otherwise.
 	 */
 	public KojiBuildJob(String name, Shell shell, IProjectRoot fpr, boolean scratch) {
 		super(name);
 		fedoraProjectRoot = fpr;
 		isScratch = scratch;
 		this.shell = shell;
 	}
 
 	@Override
 	protected IStatus run(IProgressMonitor monitor) {
 		IFpProjectBits projectBits = FedoraPackagerUtils.getVcsHandler(fedoraProjectRoot);
 		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		FedoraPackager fp = new FedoraPackager(fedoraProjectRoot);
 		KojiBuildCommand kojiBuildCmd;
 		try {
 			kojiBuildCmd = (KojiBuildCommand) fp
 				.getCommandInstance(KojiBuildCommand.ID);
 		} catch (FedoraPackagerCommandNotFoundException e) {
 			logger.logError(e.getMessage(), e);
 			FedoraHandlerUtils.showErrorDialog(shell,
 					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage());
 		} catch (FedoraPackagerCommandInitializationException e) {
 			logger.logError(e.getMessage(), e);
 			FedoraHandlerUtils.showErrorDialog(shell,
 					fedoraProjectRoot.getProductStrings().getProductName(), e.getMessage());
 			return FedoraHandlerUtils.errorStatus(KojiPlugin.PLUGIN_ID, e.getMessage());
 		}
 		monitor.beginTask(NLS.bind(
 				KojiText.KojiBuildHandler_pushBuildToKoji,
 				fedoraProjectRoot.getProductStrings().getBuildToolName()), 100);
 		monitor.worked(5);
 		UnpushedChangesListener unpushedChangesListener = new UnpushedChangesListener(
 				fedoraProjectRoot, monitor);
 		// check for unpushed changes prior calling command
 		kojiBuildCmd.addCommandListener(unpushedChangesListener);
 		// tag sources if user wishes; TagSourcesListener takes care of this
 		TagSourcesListener tagSources = new TagSourcesListener(fedoraProjectRoot, monitor, shell);
 		kojiBuildCmd.addCommandListener(tagSources);
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
 		kojiBuildCmd.sourceLocation(projectBits
 				.getScmUrlForKoji(fedoraProjectRoot));
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
 		.isScratchBuild(isScratch);
 		logger.logDebug(NLS.bind(FedoraPackagerText.callingCommand,
 				KojiBuildCommand.class.getName()));
 		// Call build command
 		try {
 			kojiBuildCmd.call(monitor);
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
 	 * Create a hub client based on set preferences.
 	 * 
 	 * @throws MalformedURLException If the koji hub URL preference was invalid.
 	 * @return The koji client.
 	 */
 	protected IKojiHubClient getHubClient() throws MalformedURLException {
 		String kojiHubUrl = PackagerPlugin.getStringPreference(FedoraPackagerPreferencesConstants.PREF_KOJI_HUB_URL);
 		if (kojiHubUrl == null) {
 			// Set to default
 			kojiHubUrl = FedoraPackagerPreferencesConstants.DEFAULT_KOJI_HUB_URL;
 		}
 		return new KojiSSLHubClient(kojiHubUrl);
 	}
 }

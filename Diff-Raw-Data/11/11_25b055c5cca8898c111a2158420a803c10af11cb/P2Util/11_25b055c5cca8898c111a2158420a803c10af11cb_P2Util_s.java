 package com.example.p2.simpleupdate.utils;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;
 import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.operations.ProvisioningJob;
 import org.eclipse.equinox.p2.operations.ProvisioningSession;
 import org.eclipse.equinox.p2.operations.UpdateOperation;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.ui.PlatformUI;
 
 import com.example.p2.simpleupdate.utils.plugin.Activator;
 
 /**
  * See
  * http://wiki.eclipse.org/Equinox/p2/Adding_Self-Update_to_an_RCP_Application
  */
 public class P2Util {
 
 	public static void checkForUpdates() {
 		final IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper.getService(Activator.getDefault()
 				.getBundle().getBundleContext(), IProvisioningAgent.SERVICE_NAME);
 		if (agent == null) {
 			LogHelper.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					"No provisioning agent found.  This application is not set up for updates."));
 		}
 
 		// TODO: progress monitor dialog broke update (maybe because of the
 		// unsigned bundle confirmation dialog?), using NullProgressMonitor
 		// for now
 		IStatus updateStatus = P2Util.checkForUpdates(agent, new NullProgressMonitor());
 		if (updateStatus.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
 			return;
 		}
 		if (updateStatus.getSeverity() != IStatus.ERROR)
 			PlatformUI.getWorkbench().restart();
 		else
 			LogHelper.log(updateStatus);
 
 	}
 
 	// XXX Check for updates to this application and return a status.
 	static IStatus checkForUpdates(IProvisioningAgent agent, IProgressMonitor monitor)
 			throws OperationCanceledException {
 		ProvisioningSession session = new ProvisioningSession(agent);
 		// the default update operation looks for updates to the currently
 		// running profile, using the default profile root marker. To change
 		// which installable units are being updated, use the more detailed
 		// constructors.
 		UpdateOperation operation = new UpdateOperation(session);
 		SubMonitor sub = SubMonitor.convert(monitor, "Checking for application updates...", 200);
 		IStatus status = operation.resolveModal(sub.newChild(100));
 		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
 			return status;
 		}
 		if (status.getSeverity() == IStatus.CANCEL)
 			throw new OperationCanceledException();
 
 		if (status.getSeverity() != IStatus.ERROR) {
 			// More complex status handling might include showing the user what
 			// updates
 			// are available if there are multiples, differentiating patches vs.
 			// updates, etc.
 			// In this example, we simply update as suggested by the operation.
 			ProvisioningJob job = operation.getProvisioningJob(null);
			if (job == null) {
				// this happens if application not provisioned by p2
				// TODO: check if p2 is available beforehand (the way that the
				// UI uses is to see if you can get a provisioning agent,
				// profile registry, and then the SELF profile...look at the
				// code in org.eclipse.p2.ui.sdk.PreloadingRepositoryHandler)
				return new Status(IStatus.OK, Activator.PLUGIN_ID, UpdateOperation.STATUS_NOTHING_TO_UPDATE,
						"No ProvisioningJob could not be obtained", null);
			}
 			status = job.runModal(sub.newChild(100));
 			if (status.getSeverity() == IStatus.CANCEL)
 				throw new OperationCanceledException();
 		}
 		return status;
 	}
 }

 package com.example.rcpmain;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;
 import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.operations.UpdateOperation;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This class controls all aspects of the application's execution
  */
 public class Application implements IApplication {
 
 	private static final String JUSTUPDATED = "justUpdated";
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
 	 */
 	public Object start(IApplicationContext context) {
 		Display display = PlatformUI.createDisplay();
 		try {
 			if (tryToUpdateApplication(display)) {
 				return IApplication.EXIT_RESTART;
 			}
 			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
 			if (returnCode == PlatformUI.RETURN_RESTART) {
 				return IApplication.EXIT_RESTART;
 			}
 			return IApplication.EXIT_OK;
 		} finally {
 			display.dispose();
 		}
 	}
 
 	private boolean tryToUpdateApplication(Display display) {
 		final IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper
 				.getService(Activator.bundleContext,
 						IProvisioningAgent.SERVICE_NAME);
 		if (agent == null) {
 			log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					"No provisioning agent found.  This application is not set up for updates."));
 		}
 		// XXX if we're restarting after updating, don't check again.
 		final IPreferenceStore prefStore = Activator.getDefault()
 				.getPreferenceStore();
 		if (prefStore.getBoolean(JUSTUPDATED)) {
 			prefStore.setValue(JUSTUPDATED, false);
 			return false;
 		}
 
 		final boolean[] restart = new boolean[] { false };
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException, InterruptedException {
 				IStatus updateStatus = P2Util.checkForUpdates(agent, monitor);
 				if (updateStatus.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
 					PlatformUI.getWorkbench().getDisplay()
 							.asyncExec(new Runnable() {
 								public void run() {
 									MessageDialog.openInformation(null,
 											"Updates", "No updates were found");
 								}
 							});
 				} else if (updateStatus.getSeverity() != IStatus.ERROR) {
 					prefStore.setValue(JUSTUPDATED, true);
 					restart[0] = true;
 				} else {
 					log(updateStatus);
 				}
 			}
 		};
 		try {
 			new ProgressMonitorDialog(null).run(true, true, runnable);
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 		}
 
 		return restart[0];
 	}
 
 	private void log(IStatus status) {
 		Activator.getDefault().getLog().log(status);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.equinox.app.IApplication#stop()
 	 */
 	public void stop() {
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		if (workbench == null)
 			return;
 		final Display display = workbench.getDisplay();
 		display.syncExec(new Runnable() {
 			public void run() {
 				if (!display.isDisposed())
 					workbench.close();
 			}
 		});
 	}
 }

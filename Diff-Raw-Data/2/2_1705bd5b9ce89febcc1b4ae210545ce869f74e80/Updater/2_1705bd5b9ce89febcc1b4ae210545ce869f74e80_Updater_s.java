 /*
 blizzy's Backup - Easy to use personal file backup application
 Copyright (C) 2011 Maik Schreiber
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package de.blizzy.backup;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.operations.ProvisioningJob;
 import org.eclipse.equinox.p2.operations.ProvisioningSession;
 import org.eclipse.equinox.p2.operations.UpdateOperation;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.widgets.Shell;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 
 public class Updater {
 	private static final int VERSION_CHECK_INTERVAL = 7; // days
 	
 	private boolean showDialogIfNoUpdatesAvailable;
 	private boolean forceCheck;
 
 	public Updater(boolean showDialogIfNoUpdatesAvailable, boolean forceCheck) {
 		this.showDialogIfNoUpdatesAvailable = showDialogIfNoUpdatesAvailable;
 		this.forceCheck = forceCheck;
 	}
 	
 	public boolean update(Shell shell) throws Throwable {
 		final boolean[] restartNecessary = new boolean[1];
 		if (needsCheck()) {
 			final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
 			IRunnableWithProgress runnable = new IRunnableWithProgress() {
 				@SuppressWarnings("synthetic-access")
 				public void run(IProgressMonitor monitor) throws InvocationTargetException {
 					SubMonitor progress = SubMonitor.convert(monitor);
 					restartNecessary[0] = updateInJob(progress, pmd.getShell());
 					monitor.done();
 				}
 			};
 			try {
 				pmd.run(true, true, runnable);
 			} catch (InvocationTargetException e) {
 				throw e.getCause();
 			}
 		}
 		if (restartNecessary[0]) {
 			IDialogSettings settings = Utils.getSection("versionCheck"); //$NON-NLS-1$
 			settings.put("cleanupOldFeatures", true); //$NON-NLS-1$
 		}
 		return restartNecessary[0];
 	}
 
 	private boolean needsCheck() {
 		if (forceCheck) {
 			return true;
 		}
 		
 		IDialogSettings settings = Utils.getSection("versionCheck"); //$NON-NLS-1$
 		String lastCheckTimeStr = settings.get("lastCheckTime"); //$NON-NLS-1$
 		if (lastCheckTimeStr == null) {
 			return true;
 		}
 		
 		long lastCheckTime = Long.parseLong(lastCheckTimeStr);
 		Calendar next = Calendar.getInstance();
 		next.setTime(new Date(lastCheckTime));
 		next.add(Calendar.DAY_OF_YEAR, VERSION_CHECK_INTERVAL);
 		Calendar now = Calendar.getInstance();
 		return next.before(now) || next.equals(now);
 	}
 
 	private boolean updateInJob(SubMonitor progress, final Shell shell) {
 		progress.beginTask(Messages.CheckingForNewVersion, 2);
 		
 		BundleContext bundleContext = BackupPlugin.getDefault().getBundle().getBundleContext();
 		ServiceReference ref = bundleContext.getServiceReference(IProvisioningAgent.SERVICE_NAME);
 		IProvisioningAgent agent = (IProvisioningAgent) bundleContext.getService(ref);
 		boolean restartNecessary = false;
 		ProvisioningSession session = new ProvisioningSession(agent);
 		final UpdateOperation op = new UpdateOperation(session);
 		IStatus status = op.resolveModal(progress.newChild(1));
 		resetCheckTimeout();
 		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
 			if (showDialogIfNoUpdatesAvailable) {
 				shell.getDisplay().syncExec(new Runnable() {
 					public void run() {
 						MessageDialog.openInformation(shell, Messages.Title_NoNewVersionAvailable, Messages.NoNewVersionAvailable);
 					}
 				});
 			}
 		} else if (status.isOK()) {
 			final boolean[] installUpdates = new boolean[1];
 			shell.getDisplay().syncExec(new Runnable() {
 				public void run() {
 					installUpdates[0] = MessageDialog.openConfirm(shell,
 							Messages.Title_NewVersionAvailable, Messages.NewVersionAvailable);
 				}
 			});
 			if (installUpdates[0]) {
 				ProvisioningJob job = op.getProvisioningJob(null);
 				status = job.runModal(progress.newChild(1));
 				if (status.isOK()) {
 					restartNecessary = true;
 				} else {
 					BackupPlugin.getDefault().getLog().log(status);
 					final IStatus myStatus = status;
 					shell.getDisplay().syncExec(new Runnable() {
 						public void run() {
 							ErrorDialog.openError(shell, Messages.Title_Error, null, myStatus);
 						}
 					});
 				}
 			}
 		}
 
 		return restartNecessary;
 	}
 	
 	private void resetCheckTimeout() {
 		Utils.getSection("versionCheck").put("lastCheckTime", System.currentTimeMillis());  //$NON-NLS-1$//$NON-NLS-2$
 	}
 }

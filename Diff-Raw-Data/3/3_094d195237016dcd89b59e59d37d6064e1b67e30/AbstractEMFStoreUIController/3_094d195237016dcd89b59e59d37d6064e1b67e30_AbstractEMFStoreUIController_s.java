 package org.eclipse.emf.emfstore.client.ui.handlers;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.emfstore.client.ui.util.EMFStoreMessageDialog;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public abstract class AbstractEMFStoreUIController {
 
 	protected Shell shell;
 	private ProgressMonitorDialog progressDialog;
 
 	public AbstractEMFStoreUIController(Shell shell) {
 		setShell(shell);
 	}
 
 	public Shell getShell() {
 		return shell;
 	}
 
 	public void setShell(Shell shell) {
 		this.shell = shell;
 	}
 
 	protected ProgressMonitorDialog openProgress() {
 		progressDialog = new ProgressMonitorDialog(getShell());
		progressDialog.open();
 		progressDialog.setCancelable(true);
 		return progressDialog;
 	}
 
 	protected void closeProgress() {
 		if (progressDialog != null) {
 			progressDialog.close();
 		}
 	}
 
 	protected IProgressMonitor getProgressMonitor() {
 		if (progressDialog != null) {
 			return progressDialog.getProgressMonitor();
 		}
 		return new NullProgressMonitor();
 	}
 
 	protected boolean confirmationDialog(String message) {
 		MessageDialog dialog = new MessageDialog(null, "Confirmation", null, message, MessageDialog.QUESTION,
 			new String[] { "Yes", "No" }, 0);
 
 		return dialog.open() == Dialog.OK;
 	}
 
 	public void handleException(Exception exception) {
 		EMFStoreMessageDialog.showExceptionDialog(exception);
 		closeProgress();
 	}
 
 }

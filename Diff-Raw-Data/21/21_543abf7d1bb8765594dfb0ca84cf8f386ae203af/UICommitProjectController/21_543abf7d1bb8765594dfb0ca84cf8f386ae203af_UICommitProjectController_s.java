 package org.eclipse.emf.emfstore.client.ui.controller;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.controller.callbacks.CommitCallback;
 import org.eclipse.emf.emfstore.client.ui.dialogs.CommitDialog;
 import org.eclipse.emf.emfstore.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class UICommitProjectController extends AbstractEMFStoreUIController implements CommitCallback {
 
 	public UICommitProjectController(Shell shell) {
 		super(shell);
 	}
 
 	public PrimaryVersionSpec commit(ProjectSpace projectSpace) throws EmfStoreException {
 		return commit(projectSpace, null);
 	}
 
 	public PrimaryVersionSpec commit(ProjectSpace projectSpace, LogMessage logMessage) throws EmfStoreException {
 		openProgress();
 		PrimaryVersionSpec commit = projectSpace.commit(logMessage, this, getProgressMonitor());
 		closeProgress();
 		return commit;
 	}
 
 	public void noLocalChanges(ProjectSpace projectSpace) {
 		MessageDialog.openInformation(getShell(), null, "No local changes in your project. No need to commit.");
 		closeProgress();
 	}
 
 	public boolean baseVersionOutOfDate(ProjectSpace projectSpace) {
 		String message = "Your project is outdated, you need to update before commit. Do you want to update now?";
 		if (confirmationDialog(message)) {
 			// TODO results?
 			try {
 				new UIUpdateProjectController(getShell()).update(projectSpace);
 			} catch (EmfStoreException e) {
 				handleException(e);
 			}
 		}
 		closeProgress();
 		return true;
 	}
 
 	public boolean inspectChanges(ProjectSpace projectSpace, ChangePackage changePackage) {
 		if (changePackage.getOperations().isEmpty()) {
 			MessageDialog.openInformation(getShell(), "No local changes",
 				"Your local changes were mutually exclusive.\nThey are no changes pending for commit.");
 			return false;
 		}
 		CommitDialog commitDialog = new CommitDialog(getShell(), changePackage, projectSpace);
		// TODO add getLogMessage to callback
		// if (predefinedCommitMessage != null) {
		// if (changePackage.getLogMessage() == null) {
		// changePackage.setLogMessage(logMessage);
		// }
		// changePackage.getLogMessage().setMessage(predefinedCommitMessage);
		// }
 		if (commitDialog.open() == Dialog.OK) {
			// logMessage.setAuthor(usersession.getUsername());
			// logMessage.setClientDate(new Date());
			// logMessage.setMessage(commitDialog.getLogText());
 			return true;
 		}
 		return false;
 	}
 }

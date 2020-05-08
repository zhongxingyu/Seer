 package org.eclipse.emf.emfstore.client.ui.controller;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.impl.ProjectSpaceImpl;
 import org.eclipse.emf.emfstore.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.swt.widgets.Shell;
 
 public class UIShareProjectController extends AbstractEMFStoreUIController {
 
 	public UIShareProjectController(Shell shell) {
 		super(shell);
 	}
 
 	public void share(ProjectSpace projectSpace) throws EmfStoreException {
 		ProgressMonitorDialog progressDialog = openProgress();
 		try {
 			((ProjectSpaceImpl) projectSpace).shareProject(null, progressDialog.getProgressMonitor());
			MessageDialog.openInformation(getShell(), "Share completed", "The project was successfully shared.");
		} catch (EmfStoreException e) {
			throw e;
 		} finally {
 			closeProgress();
 		}
 	}
 }

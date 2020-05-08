 package org.eclipse.emf.emfstore.client.ui.controller;
 
 import org.eclipse.emf.emfstore.client.model.AdminBroker;
import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.dialogs.admin.ManageOrgUnitsDialog;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class UIManageOrgUnitsController extends AbstractEMFStoreUIController {
 
	public UIManageOrgUnitsController(Shell shell) {
 		super(shell);
 	}
 
	public void openManageOrgUnitsDialog(Usersession usersession) {
 		try {
			AdminBroker adminBroker = WorkspaceManager.getInstance().getCurrentWorkspace().getAdminBroker(usersession);
 			ManageOrgUnitsDialog dialog = new ManageOrgUnitsDialog(PlatformUI.getWorkbench().getDisplay()
 				.getActiveShell(), adminBroker);
 			dialog.create();
 			dialog.open();
 		} catch (AccessControlException e) {
 			MessageDialog.openError(getShell(), "Error", e.getMessage());
 		} catch (EmfStoreException e) {
 			MessageDialog.openError(getShell(), "Error", e.getMessage());
 		}
 
 	}
 
 }

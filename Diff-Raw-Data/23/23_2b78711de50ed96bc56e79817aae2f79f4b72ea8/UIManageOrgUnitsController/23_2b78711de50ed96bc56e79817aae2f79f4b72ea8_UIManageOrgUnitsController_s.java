 package org.eclipse.emf.emfstore.client.ui.controller;
 
 import org.eclipse.emf.emfstore.client.model.AdminBroker;
import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.ui.handlers.AbstractEMFStoreUIController;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.dialogs.admin.ManageOrgUnitsDialog;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class UIManageOrgUnitsController extends AbstractEMFStoreUIController {
 
	private final ServerInfo serverInfo;

	public UIManageOrgUnitsController(Shell shell, ServerInfo serverInfo) {
 		super(shell);
		this.serverInfo = serverInfo;
 	}
 
	public void openManageOrgUnitsDialog() {
 		try {
			AdminBroker adminBroker = WorkspaceManager.getInstance().getCurrentWorkspace()
				.getAdminBroker(serverInfo.getLastUsersession());
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

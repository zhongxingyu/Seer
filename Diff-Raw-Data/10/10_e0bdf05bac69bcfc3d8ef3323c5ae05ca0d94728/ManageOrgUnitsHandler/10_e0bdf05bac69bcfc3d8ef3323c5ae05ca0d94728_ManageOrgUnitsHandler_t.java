 package org.eclipse.emf.emfstore.client.ui.handlers;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.ui.controller.UIManageOrgUnitsController;
 
 public class ManageOrgUnitsHandler extends AbstractEMFStoreHandler {
 
 	@Override
 	public void handle() {
		ServerInfo serverInfo = requireSelection(ServerInfo.class);

		if (serverInfo.getLastUsersession() == null) {
			throw new RequiredSelectionException("Usersession not available in selected ServerInfo.");
		}
 
		new UIManageOrgUnitsController(getShell()).openManageOrgUnitsDialog(serverInfo.getLastUsersession());
	}
 }

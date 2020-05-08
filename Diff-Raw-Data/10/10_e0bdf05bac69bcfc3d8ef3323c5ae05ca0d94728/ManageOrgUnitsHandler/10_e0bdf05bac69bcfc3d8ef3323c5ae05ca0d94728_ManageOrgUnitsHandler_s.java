 package org.eclipse.emf.emfstore.client.ui.handlers;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.ui.controller.UIManageOrgUnitsController;
 
 public class ManageOrgUnitsHandler extends AbstractEMFStoreHandler {
 
 	@Override
 	public void handle() {
		new UIManageOrgUnitsController(getShell(), requireSelection(ServerInfo.class));
	}
 
 }

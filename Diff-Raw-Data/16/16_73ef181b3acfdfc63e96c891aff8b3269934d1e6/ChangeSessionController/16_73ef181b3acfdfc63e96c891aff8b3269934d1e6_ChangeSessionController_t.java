 package org.eclipse.emf.emfstore.client.model.controller;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 
 public class ChangeSessionController extends ServerCall<Void> {
 
 	public ChangeSessionController(ServerInfo serverInfo) {
		super(serverInfo);
 	}
 
 	@Override
 	protected Void run() throws EmfStoreException {
 		try {
			getUsersession().logout();
 		} catch (EmfStoreException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		}

 		// reset the password in the RAM cache
		if (!getUsersession().isSavePassword()) {
			getUsersession().setPassword(null);
 		}
 		WorkspaceManager.getInstance().getCurrentWorkspace().save();
 		return null;
 	}
 }

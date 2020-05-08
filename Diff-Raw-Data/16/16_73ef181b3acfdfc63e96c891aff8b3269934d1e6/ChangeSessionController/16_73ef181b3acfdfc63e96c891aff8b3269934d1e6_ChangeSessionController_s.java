 package org.eclipse.emf.emfstore.client.model.controller;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 
 public class ChangeSessionController extends ServerCall<Void> {
 
	private final ServerInfo serverInfo;

 	public ChangeSessionController(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
 	}
 
 	@Override
 	protected Void run() throws EmfStoreException {
 		try {
			serverInfo.getLastUsersession().logout();
 		} catch (EmfStoreException e) {
 			WorkspaceUtil.logException(e.getMessage(), e);
 		}
		serverInfo.getLastUsersession().setSessionId(null);
 		// reset the password in the RAM cache
		if (!serverInfo.getLastUsersession().isSavePassword()) {
			serverInfo.getLastUsersession().setPassword(null);
 		}
 		WorkspaceManager.getInstance().getCurrentWorkspace().save();
 		return null;
 	}
 }

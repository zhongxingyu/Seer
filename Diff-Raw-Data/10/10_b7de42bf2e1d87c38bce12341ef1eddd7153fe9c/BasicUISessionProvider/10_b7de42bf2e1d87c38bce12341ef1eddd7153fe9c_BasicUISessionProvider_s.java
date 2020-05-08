 package org.eclipse.emf.emfstore.client.ui.dialogs.login;
 
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.AbstractSessionProvider;
 import org.eclipse.emf.emfstore.client.model.exceptions.LoginCanceledException;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * 
  * @author wesendon
  * 
  */
 public class BasicUISessionProvider extends AbstractSessionProvider {
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.model.connectionmanager.SessionProvider#provideUsersession(org.eclipse.emf.emfstore.client.model.ServerInfo)
 	 */
 	@Override
 	public Usersession provideUsersession(ServerInfo serverInfo) throws EmfStoreException {
 		if (serverInfo == null) {
 			// try to retrieve a server info by showing a server info selection dialog
 			ServerInfoSelectionDialog dialog = new ServerInfoSelectionDialog(Display.getCurrent().getActiveShell(),
 				WorkspaceManager.getInstance().getCurrentWorkspace().getServerInfos());
 			if (dialog.open() == Dialog.OK) {
 				serverInfo = dialog.getResult();
 			} else if (dialog.open() == Dialog.CANCEL) {
 				throw new LoginCanceledException("Operation canceled by user.");
 			}
 		}
 		if (serverInfo == null) {
 			throw new AccessControlException("Couldn't determine which server to connect.");
 		}
 
 		// TODO Review this
 		// if (serverInfo.getLastUsersession() != null) {
 		// return serverInfo.getLastUsersession();
 		// }
 
 		return new LoginDialogController().login(serverInfo);
 	}
 
 	@Override
 	public void login(Usersession usersession) throws EmfStoreException {
 		if (usersession != null) {
 			new LoginDialogController().login(usersession);
 		}
 	}
 }

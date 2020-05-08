 package org.eclipse.emf.emfstore.client.model.connectionmanager;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.common.ExtensionPoint;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.SessionTimedOutException;
 import org.eclipse.emf.emfstore.server.exceptions.UnknownSessionException;
 
 /**
  * 
  * @author wesendon
  */
 public class SessionManager {
 
 	public void execute(ServerCall serverCall) {
 		Usersession usersession = prepareUsersession(serverCall);
 		loginUsersession(usersession, false);
 		executeCall(serverCall, usersession, true);
 	}
 
 	private void loginUsersession(Usersession usersession, boolean force) {
 		if (usersession == null) {
 			// TODO create exception
 			throw new RuntimeException("Ouch.");
 		}
 		if (!isLoggedIn(usersession) || force) {
			if (!usersession.getUsername().isEmpty() && usersession.getPassword() != null) {
 				try {
 					// if login fails, let the session provider handle the rest
 					usersession.logIn();
 					return;
 				} catch (AccessControlException e) {
 				} catch (EmfStoreException e) {
 				}
 			}
 			getSessionProvider().loginSession(usersession);
 		}
 	}
 
 	private boolean isLoggedIn(Usersession usersession) {
 		ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		return usersession.isLoggedIn() && connectionManager.isLoggedIn(usersession.getSessionId());
 	}
 
 	private void executeCall(ServerCall serverCall, Usersession usersession, boolean retry) {
 		try {
 			serverCall.run(usersession.getSessionId());
 		} catch (SessionTimedOutException e) {
 			if (retry) {
 				// login & retry
 				loginUsersession(usersession, true);
 				executeCall(serverCall, usersession, false);
 			} else {
 				serverCall.handleException(e);
 			}
 
 		} catch (UnknownSessionException e) {
 			if (retry) {
 				// login & retry
 				loginUsersession(usersession, true);
 				executeCall(serverCall, usersession, false);
 			} else {
 				serverCall.handleException(e);
 			}
 
 		} catch (Exception e) {
 			serverCall.handleException(e);
 		}
 	}
 
 	private Usersession prepareUsersession(ServerCall serverCall) {
 		Usersession usersession = serverCall.getUsersession();
 		if (usersession == null) {
 			usersession = getUsersessionFromProjectSpace(serverCall.getProjectSpace());
 		}
 
 		if (usersession == null) {
 			SessionProvider sessionProvider = getSessionProvider();
 			usersession = sessionProvider.provideUsersession();
 		}
 		serverCall.setUsersession(usersession);
 		return usersession;
 	}
 
 	private Usersession getUsersessionFromProjectSpace(ProjectSpace projectSpace) {
 		if (projectSpace != null && projectSpace.getUsersession() != null) {
 			return projectSpace.getUsersession();
 		}
 		return null;
 	}
 
 	private SessionProvider getSessionProvider() {
 		return new ExtensionPoint(SessionProvider.ID).getClass("class", SessionProvider.class);
 	}
 }

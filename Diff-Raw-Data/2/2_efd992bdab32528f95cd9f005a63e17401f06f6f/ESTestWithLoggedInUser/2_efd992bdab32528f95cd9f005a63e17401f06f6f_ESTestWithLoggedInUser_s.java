 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.common.cases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.exceptions.ESServerNotFoundException;
 import org.eclipse.emf.emfstore.client.test.common.util.ServerUtil;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESServerImpl;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESUsersessionImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommandWithException;
 import org.eclipse.emf.emfstore.internal.server.model.SessionId;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnitId;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESSessionIdImpl;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.junit.After;
 import org.junit.Before;
 
 /**
  * A test case that involves a started server and an already logged-in user.
  * The user to be logged can be specified via the {@link #getUser()} and {@link #getPassword()} methods.
  * If the user does not exists it will be created. <br/>
  * <br/>
  * <b>NOTE</b>: Remember to call {@code startEMFStore()} and {@code stopEMFStore()} respectively in
  * {@code beforeClass()} and {@code afterClass()}.
  * 
  * @author emueller
  * 
  */
public abstract class ESTestWithLoggedInUser extends ESTestWithServer {
 
 	private ESServer server;
 	private ESUsersession usersession;
 	private ESUsersession superSession;
 	private ACOrgUnitId userId;
 
 	public ESServer getServer() {
 		return server;
 	}
 
 	public ESUsersession getUsersession() {
 		return usersession;
 	}
 
 	public ESUsersession getSuperUsersession() {
 		return superSession;
 	}
 
 	/**
 	 * Returns the name of the user that is used by the test.
 	 * 
 	 * @return the user name
 	 */
 	public String getUser() {
 		return ServerUtil.superUser();
 	}
 
 	/**
 	 * Returns the password of the user that is used by the test to login the user.
 	 * 
 	 * @return the password to be used to perform the login.
 	 */
 	public String getPassword() {
 		return ServerUtil.superUserPassword();
 	}
 
 	@Override
 	@Before
 	public void before() {
 		super.before();
 		server = ESServer.FACTORY.createServer(
 			ServerUtil.localhost(),
 			ServerUtil.defaultPort(),
 			KeyStoreManager.DEFAULT_CERTIFICATE);
 
 		try {
 			superSession = server.login(
 				ServerUtil.superUser(), ServerUtil.superUserPassword());
 			final SessionId sessionId = ESSessionIdImpl.class.cast(
 				superSession.getSessionId()).toInternalAPI();
 
 			if (isSuperUser()) {
 				usersession = superSession;
 				return;
 			}
 
 			// if client requests other user, make sure that user exists
 			if (!userExists(sessionId, getUser())) {
 				userId = ServerUtil.createUser(sessionId, getUser());
 				ServerUtil.changeUser(sessionId, userId, getUser(), getPassword());
 			}
 
 			usersession = server.login(
 				getUser(),
 				getPassword());
 		} catch (final ESException e) {
 			fail(e.getMessage());
 		}
 		assertEquals(usersession, server.getLastUsersession());
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean isSuperUser() {
 		return getUser().equals(ServerUtil.superUser());
 	}
 
 	public boolean userExists(SessionId sessionId, String name) throws ESException {
 		final ACUser user = ServerUtil.getUser(sessionId, name);
 		return user != null;
 	}
 
 	@Override
 	@After
 	public void after() {
 
 		if (!isSuperUser()) {
 			try {
 				ESWorkspaceProviderImpl.getInstance().getAdminConnectionManager().deleteUser(
 					((ESUsersessionImpl) getSuperUsersession()).toInternalAPI().getSessionId(),
 					userId);
 			} catch (final ESException ex) {
 				fail(ex.getMessage());
 			}
 		}
 
 		final EMFStoreCommandWithException<ESException> cmd = new EMFStoreCommandWithException<ESException>() {
 			@Override
 			protected void doRun() {
 				((ESServerImpl) server).toInternalAPI().setLastUsersession(null);
 				((ESUsersessionImpl) usersession).setServer(null);
 				((ESUsersessionImpl) superSession).setServer(null);
 				// setUp might have failed
 				if (usersession != null && usersession.isLoggedIn()) {
 					try {
 						logoutSessions();
 
 						final Iterator<Usersession> iter = ESWorkspaceProviderImpl.getInstance().getWorkspace()
 							.toInternalAPI()
 							.getUsersessions().iterator();
 						while (iter.hasNext()) {
 							if (iter.next().getServerInfo() == ((ESServerImpl) server).toInternalAPI()) {
 								iter.remove();
 							}
 						}
 						ESWorkspaceProvider.INSTANCE.getWorkspace().removeServer(server);
 					} catch (final ESException e) {
 						setException(e);
 					} catch (final ESServerNotFoundException e) {
 						fail(e.getMessage());
 					}
 				}
 			}
 		};
 
 		cmd.run();
 
 		if (cmd.hasException()) {
 			fail(cmd.getException().getMessage());
 		}
 
 		super.after();
 	}
 
 	/**
 	 * @throws ESException
 	 */
 	private void logoutSessions() throws ESException {
 		superSession.logout();
 		if (!isSuperUser()) {
 			usersession.logout();
 		}
 	}
 }

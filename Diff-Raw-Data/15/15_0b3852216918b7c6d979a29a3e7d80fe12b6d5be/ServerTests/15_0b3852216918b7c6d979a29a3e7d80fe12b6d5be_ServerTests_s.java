/**
 * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
 */
 package org.eclipse.emf.emfstore.client.test.server;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.Workspace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.SetupHelper;
 import org.eclipse.emf.emfstore.client.test.WorkspaceTest;
 import org.eclipse.emf.emfstore.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidInputException;
 import org.eclipse.emf.emfstore.server.model.AuthenticationInformation;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnitId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.AccesscontrolFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 /**
  * Superclass for server tests.
  * 
  * @author wesendon
  */
 
 public abstract class ServerTests extends WorkspaceTest {
 
 	private static SessionId sessionId;
 	private static ConnectionManager connectionManager;
 	private int projectsOnServerBeforeTest;
 	private static HashMap<Class<?>, Object> arguments;
 	private static ServerInfo serverInfo;
 
 	public static void setServerInfo(ServerInfo newServerInfo) {
 		serverInfo = newServerInfo;
 	}
 
 	public static ServerInfo getServerInfo() {
 		return serverInfo;
 	}
 
 	/**
 	 * @return the sessionId
 	 */
 	public static SessionId getSessionId() {
 		return sessionId;
 	}
 
 	public static void setSessionId(SessionId newSessionId) {
 		sessionId = newSessionId;
 	}
 
 	/**
 	 * @return the connectionManager
 	 */
 	public static ConnectionManager getConnectionManager() {
 		return connectionManager;
 	}
 
 	public static void setConnectionManager(ConnectionManager newConnectionManager) {
 		connectionManager = newConnectionManager;
 	}
 
 	public ProjectInfo getProjectInfo() {
 		return getProjectSpace().getProjectInfo();
 	}
 
 	public ProjectId getProjectId() {
 		return getProjectSpace().getProjectId();
 	}
 
 	public PrimaryVersionSpec getProjectVersion() {
 		return getProjectInfo().getVersion();
 	}
 
 	/**
 	 * @return the projectsOnServerBeforeTest
 	 */
 	public int getProjectsOnServerBeforeTest() {
 		return projectsOnServerBeforeTest;
 	}
 
 	/**
 	 * Start server and gain sessionid.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 * @throws IOException
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws EmfStoreException, IOException {
 		ServerConfiguration.setTesting(true);
 		Configuration.setTesting(true);
 
 		// delete all data before test start
 		SetupHelper.removeServerTestProfile();
 
 		SetupHelper.addUserFileToServer(false);
 		SetupHelper.startSever();
 		setConnectionManager(WorkspaceManager.getInstance().getConnectionManager());
 		setServerInfo(SetupHelper.getServerInfo());
 		// login();
 		initArguments();
 	}
 
 	/**
 	 * @param serverInfo serverinfo
 	 * @throws EmfStoreException in case of failure
 	 */
 	protected static void login() throws EmfStoreException {
 		SessionId sessionId = login(getServerInfo(), "super", "super").getSessionId();
 		WorkspaceManager.getInstance().getAdminConnectionManager().initConnection(getServerInfo(), sessionId);
 		setSessionId(sessionId);
 	}
 
 	/**
 	 * @param serverInfo serverinfo
 	 * @param username username
 	 * @param password password
 	 * @return sessionId
 	 * @throws EmfStoreException in case of failure
 	 */
 	protected static AuthenticationInformation login(ServerInfo serverInfo, String username, String password)
 		throws EmfStoreException {
 		return getConnectionManager().logIn(username, KeyStoreManager.getInstance().encrypt(password, serverInfo),
 			serverInfo, Configuration.getClientVersion());
 	}
 
 	protected static void initArguments() {
 		arguments = new HashMap<Class<?>, Object>();
 		arguments.put(boolean.class, false);
 		arguments.put(String.class, new String());
 		arguments.put(SessionId.class, ModelUtil.clone(getSessionId()));
 		arguments.put(ProjectId.class, org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectId());
 		arguments.put(PrimaryVersionSpec.class, VersioningFactory.eINSTANCE.createPrimaryVersionSpec());
 		arguments.put(VersionSpec.class, VersioningFactory.eINSTANCE.createPrimaryVersionSpec());
 		arguments.put(TagVersionSpec.class, VersioningFactory.eINSTANCE.createTagVersionSpec());
 		arguments.put(LogMessage.class, VersioningFactory.eINSTANCE.createLogMessage());
 		arguments.put(Project.class, ModelFactory.eINSTANCE.createProject());
 		arguments.put(ChangePackage.class, VersioningFactory.eINSTANCE.createChangePackage());
 		arguments.put(HistoryQuery.class, VersioningFactory.eINSTANCE.createHistoryQuery());
 		arguments.put(ChangePackage.class, VersioningFactory.eINSTANCE.createChangePackage());
 		arguments.put(ACOrgUnitId.class, AccesscontrolFactory.eINSTANCE.createACOrgUnitId());
 	}
 
 	/**
 	 * Shuts down server after testing.
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() {
 		SetupHelper.stopServer();
 
 	}
 
 	/**
 	 * Adds a project to the server before test.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Before
 	public void beforeTest() throws EmfStoreException {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				try {
 					ServerInfo serverInfo = SetupHelper.getServerInfo();
 					Usersession session = org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE
 						.createUsersession();
 					session.setServerInfo(serverInfo);
 					session.setUsername("super");
 					session.setPassword("super");
 					session.setSavePassword(true);
 
 					Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
 					currentWorkspace.getServerInfos().add(serverInfo);
 					currentWorkspace.getUsersessions().add(session);
 					currentWorkspace.save();
 					getProjectSpace().shareProject(session, null);
 				} catch (EmfStoreException e) {
 					Assert.fail();
 				}
 			}
 		}.run(false);
 		// setProjectInfo(getConnectionManager().createProject(getSessionId(), "initialProject", "TestProject",
 		// SetupHelper.createLogMessage("super", "a logmessage"), getProject()));
 		this.projectsOnServerBeforeTest = 1;
 	}
 
 	/**
 	 * Removes all projects from server after test.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@After
 	public void afterTest() throws EmfStoreException {
 
 		for (ProjectInfo info : WorkspaceManager.getInstance().getCurrentWorkspace()
 			.getRemoteProjectList(getServerInfo())) {
 			WorkspaceManager.getInstance().getCurrentWorkspace()
 				.deleteRemoteProject(getServerInfo(), info.getProjectId(), true);
 		}
 
 		SetupHelper.cleanupServer();
 	}
 
 	/**
 	 * Sets up user on server.
 	 * 
 	 * @param name name of the user (must be specified in users.properties)
 	 * @param role of type RolesPackage.eINSTANCE.getWriterRole() or RolesPackage.eINSTANCE.getReaderRole() ....
 	 * @throws EmfStoreException in case of failure
 	 */
 
 	public ACOrgUnitId setupUsers(String name, EClass role) throws EmfStoreException {
 		try {
 			ACOrgUnitId orgUnitId = SetupHelper.createUserOnServer(name);
 			SetupHelper.setUsersRole(orgUnitId, role, getProjectId());
 			return orgUnitId;
 		} catch (InvalidInputException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Sets up usersession.
 	 * 
 	 * @param name of the user (must be specified in users.properties)
 	 * @param password of the user (must be specified in users.properties)
 	 * @return established usersession
 	 */
 	public Usersession setUpUsersession(String username, String password) {
 		Usersession usersession = org.eclipse.emf.emfstore.client.model.ModelFactory.eINSTANCE.createUsersession();
 		usersession.setServerInfo(getServerInfo());
 		usersession.setUsername(username);
 		usersession.setPassword(password);
 		return usersession;
 	}
 
 	/**
 	 * Compares two projects.
 	 * 
 	 * @param expected expected
 	 * @param compare to be compared
 	 */
 	public static void assertEqual(Project expected, Project compare) {
 		if (!ModelUtil.areEqual(expected, compare)) {
 			throw new AssertionError("Projects are not equal.");
 		}
 	}
 
 	/**
 	 * Creates a historyquery.
 	 * 
 	 * @param ver1 source
 	 * @param ver2 target
 	 * @return historyquery
 	 */
 	public static HistoryQuery createHistoryQuery(PrimaryVersionSpec ver1, PrimaryVersionSpec ver2) {
 		HistoryQuery historyQuery = VersioningFactory.eINSTANCE.createHistoryQuery();
 		historyQuery.setSource(ModelUtil.clone(ver1));
 		historyQuery.setTarget(ModelUtil.clone(ver2));
 		return historyQuery;
 	}
 
 	/**
 	 * Get a default Parameter.
 	 * 
 	 * @param clazz parameter type
 	 * @param b if false, null is returned
 	 * @return parameter
 	 */
 	protected static Object getParameter(Class<?> clazz, boolean b) {
 		if (clazz.equals(boolean.class)) {
 			return false;
 		}
 		return (b) ? arguments.get(clazz) : null;
 	}
 }

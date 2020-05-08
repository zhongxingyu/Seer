 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 
 package org.eclipse.emf.emfstore.client.test;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.security.AccessControlException;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.emfstore.client.model.Configuration;
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.Workspace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.AdminConnectionManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.test.integration.forward.IntegrationTestHelper;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.FileUtil;
 import org.eclipse.emf.emfstore.server.EmfStoreController;
 import org.eclipse.emf.emfstore.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnitId;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 
 /**
  * Helper class for setup/cleanup test fixtures.
  * 
  * @author hodaie
  */
 public class SetupHelper {
 
	private static final Logger LOGGER = Logger.getLogger("org.eclipse.emf.emfstore.client.test.SetupHelper");
 
 	private Workspace workSpace;
 	private ProjectSpace testProjectSpace;
 	private Project testProject;
 	private Usersession usersession;
 
 	private ProjectId projectId;
 	private Project compareProject;
 
 	private String projectPath;
 	private TestProjectEnum projectTemplate;
 
 	/**
 	 * @param projectTemplate test project to initialize SetupHelper
 	 */
 	public SetupHelper(TestProjectEnum projectTemplate) {
 
 		this.projectTemplate = projectTemplate;
 		LOGGER.log(Level.INFO, "SetupHelper instantiated with " + projectTemplate);
 	}
 
 	/**
 	 * @param absolutePath The absolute path of an exported project (.ucp file). This project will then be imported and
 	 *            used as test project.
 	 */
 	public SetupHelper(String absolutePath) {
 
 		projectPath = absolutePath;
 		LOGGER.log(Level.INFO, "SetupHelper instantiated with " + absolutePath);
 	}
 
 	/**
 	 * Starts the server.
 	 */
 	public static void startSever() {
 		try {
 			ServerConfiguration.setTesting(true);
 			// Properties properties = ServerConfiguration.getProperties();
 			// little workaround, there is a flaw in server configuration
 			// properties.setProperty(ServerConfiguration.RMI_ENCRYPTION, ServerConfiguration.FALSE);
 			EmfStoreController.runAsNewThread();
 			LOGGER.log(Level.INFO, "server started. ");
 		} catch (FatalEmfStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Stops the server.
 	 */
 	public static void stopServer() {
 		EmfStoreController server = EmfStoreController.getInstance();
 		if (server != null) {
 			server.stop();
 		}
 		try {
 			// give the server some time to unbind from it's ips. Not the nicest solution ...
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	/**
 	 * Copies user.properties in server directory.
 	 * 
 	 * @param override true if old file should be deleted first.
 	 */
 	public static void addUserFileToServer(boolean override) {
 		try {
 			File file = new File(ServerConfiguration.getProperties().getProperty(
 				ServerConfiguration.AUTHENTICATION_SPFV_FILEPATH, ServerConfiguration.getDefaultSPFVFilePath()));
 			if (override && file.exists()) {
 				file.delete();
 			}
 			FileUtil.copyFile(SetupHelper.class.getResourceAsStream("user.properties"), file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param sessionId sessionId
 	 * @param username username
 	 * @return acorgunitid
 	 * @throws EmfStoreException in case of failure
 	 */
 	public static ACOrgUnitId createUserOnServer(SessionId sessionId, String username) throws EmfStoreException {
 		AdminConnectionManager adminConnectionManager = WorkspaceManager.getInstance().getAdminConnectionManager();
 		return adminConnectionManager.createUser(sessionId, username);
 	}
 
 	/**
 	 * @param sessionId sessionid
 	 * @param orgUnitId orgunitid
 	 * @param role role
 	 * @param projectId projectid, can be null, if role is serveradmin
 	 * @throws EmfStoreException in case of failure
 	 */
 	public static void setUsersRole(SessionId sessionId, ACOrgUnitId orgUnitId, EClass role, ProjectId projectId)
 		throws EmfStoreException {
 		AdminConnectionManager adminConnectionManager = WorkspaceManager.getInstance().getAdminConnectionManager();
 		adminConnectionManager.changeRole(sessionId, projectId, orgUnitId, role);
 	}
 
 	/**
 	 * Setups server space.
 	 */
 	public static void setupServerSpace() {
 		// 1.
 		// create a new server space
 
 		// import project history from local folder (it is located in our test plug-in)
 
 		// add the history to server space
 
 		// ===============================
 		// 2.
 		// copy whole folders and storage from file system to .unicase.test/emfstore
 
 		ServerConfiguration.setTesting(true);
 		String serverPath = ServerConfiguration.getServerHome();
 		File targetLocation = new File(serverPath);
 		String path = "TestProjects/Projects";
 		String srcPath = Activator.getDefault().getBundle().getLocation() + path;
 		if (File.separator.equals("/")) {
 			srcPath = srcPath.replace("reference:file:", "");
 
 		} else {
 			srcPath = srcPath.replace("reference:file:/", "");
 		}
 		File sourceLocation = new File(srcPath);
 
 		try {
 			FileUtils.copyDirectory(sourceLocation, targetLocation);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// start server.
 
 		try {
 			Properties properties = ServerConfiguration.getProperties();
 			properties.setProperty(ServerConfiguration.RMI_ENCRYPTION, ServerConfiguration.FALSE);
 			EmfStoreController.runAsNewThread();
 		} catch (FatalEmfStoreException e) {
 			e.printStackTrace();
 		}
 		LOGGER.log(Level.INFO, "setup server space finished");
 
 	}
 
 	/**
 	 * log in the test server.
 	 */
 	public void loginServer() {
 		if (usersession == null) {
 			usersession = ModelFactory.eINSTANCE.createUsersession();
 
 			ServerInfo serverInfo = getServerInfo();
 			usersession.setServerInfo(serverInfo);
 			usersession.setUsername("super");
 			usersession.setPassword("super");
 		}
 
 		if (!usersession.isLoggedIn()) {
 			try {
 				usersession.logIn();
 			} catch (AccessControlException e) {
 				e.printStackTrace();
 			} catch (EmfStoreException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Returns server info.
 	 * 
 	 * @return server info
 	 */
 	public static ServerInfo getServerInfo() {
 		ServerInfo serverInfo = ModelFactory.eINSTANCE.createServerInfo();
 		serverInfo.setPort(8080);
 		// serverInfo.setUrl("127.0.0.1");
 		serverInfo.setUrl("localhost");
 		serverInfo.setCertificateAlias("emfstore test certificate (do not use in production!)");
 
 		return serverInfo;
 	}
 
 	/**
 	 * Setups workspace.
 	 */
 	public void setupWorkSpace() {
 		LOGGER.log(Level.INFO, "setting up workspace...");
 		Configuration.setTesting(true);
 		workSpace = WorkspaceManager.getInstance().getCurrentWorkspace();
 		LOGGER.log(Level.INFO, "workspace initialized");
 
 	}
 
 	/**
 	 * Creates an empty project space.
 	 */
 	public void createEmptyTestProjectSpace() {
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 				projectSpace.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createProject());
 				projectSpace.setProjectName("Testproject");
 				projectSpace.setProjectDescription("Test description");
 				projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 
 				projectSpace.initResources(workSpace.eResource().getResourceSet());
 
 				((WorkspaceImpl) workSpace).addProjectSpace(projectSpace);
 				workSpace.save();
 				testProjectSpace = projectSpace;
 
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Setups a new test project space by importing one of template test projects.
 	 */
 	public void setupTestProjectSpace() {
 		LOGGER.log(Level.INFO, "setting up projectspace...");
 		if (projectTemplate != null) {
 			// we are using a project template
 			setupTestProjectSpace(projectTemplate);
 		} else {
 			// we are using the absolute path of an exported unicase project (.ucp file)
 			setupTestProjectSpace(projectPath);
 		}
 		LOGGER.log(Level.INFO, "projectspace initialized");
 
 	}
 
 	private void setupTestProjectSpace(TestProjectEnum template) {
 		final String path;
 		path = template.getPath();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				String uriString = Activator.getDefault().getBundle().getLocation() + path;
 				if (File.separator.equals("/")) {
 					uriString = uriString.replace("reference:file:", "");
 
 				} else {
 					uriString = uriString.replace("reference:file:/", "");
 					uriString = uriString.replace("/", File.separator);
 				}
 				try {
 					testProjectSpace = importProject(uriString);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 		}.run(false);
 
 		testProject = testProjectSpace.getProject();
 	}
 
 	/**
 	 * Setups a new test project space by importing a project file located at absolutePath.
 	 * 
 	 * @param absolutePath absolutePath to a project to import.
 	 */
 	private void setupTestProjectSpace(final String absolutePath) {
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				try {
 					testProjectSpace = importProject(absolutePath);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 		}.run(false);
 
 		testProject = testProjectSpace.getProject();
 
 	}
 
 	/**
 	 * @throws EmfStoreException if any error occurs
 	 */
 	public void setupTestProjectOnServer() throws EmfStoreException {
 		System.out.println("**********************************************************");
 		System.out.println("*                                                        *");
 		System.out.println("*     Creating a random project with given parameters    *");
 		System.out.println("*                                                        *");
 		System.out.println("**********************************************************");
 
 		// running the server
 		startSever();
 		// logging in on server
 		loginServer();
 		// create a new project id
 		projectId = org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectId();
 		// visual check if null
 		System.out.println("-> Session id is: " + usersession.getSessionId().getId());
 		System.out.println("-> Project id is: " + projectId.getId());
 		// create a log message
 
 		ConnectionManager connectionManager = WorkspaceManager.getInstance().getConnectionManager();
 		ProjectInfo projectInfo = connectionManager.createEmptyProject(usersession.getSessionId(),
 			projectId.toString(), "test_project", createLogMessage("test", "log this!"));
 
 		WorkspaceManager.getInstance().getCurrentWorkspace().checkout(usersession, projectInfo);
 
 	}
 
 	/**
 	 * Cleans server up.
 	 */
 	public static void cleanupServer() {
 		String serverPath = ServerConfiguration.getServerHome();
 		File serverDirectory = new File(serverPath);
 		FileFilter serverFileFilter = new FileFilter() {
 
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith("project-");
 			}
 
 		};
 		File[] filesToDeleteOnServer = serverDirectory.listFiles(serverFileFilter);
 		for (int i = 0; i < filesToDeleteOnServer.length; i++) {
 			try {
 				FileUtil.deleteFolder(filesToDeleteOnServer[i]);
 			} catch (IOException e) {
 
 				e.printStackTrace();
 			}
 		}
 
 		new File(serverPath + "storage.uss").delete();
 		LOGGER.log(Level.INFO, "serverspce cleaned.");
 
 	}
 
 	/**
 	 * Cleans workspace up.
 	 */
 	public static void cleanupWorkspace() {
 
 		String workspacePath = Configuration.getWorkspaceDirectory();
 		File workspaceDirectory = new File(workspacePath);
 		FileFilter workspaceFileFilter = new FileFilter() {
 
 			public boolean accept(File pathname) {
 				return pathname.getName().startsWith("ps-");
 			}
 
 		};
 		File[] filesToDelete = workspaceDirectory.listFiles(workspaceFileFilter);
 		for (int i = 0; i < filesToDelete.length; i++) {
 			try {
 				FileUtil.deleteFolder(filesToDelete[i]);
 			} catch (IOException e) {
 
 				e.printStackTrace();
 			}
 		}
 
 		new File(workspacePath + "workspace.ucw").delete();
 		LOGGER.log(Level.INFO, "workspace cleaned.");
 	}
 
 	/**
 	 * Imports a project space from an exported project file.
 	 * 
 	 * @param absolutePath path to an exported project file
 	 * @return project space
 	 * @throws IOException IOException
 	 */
 	public ProjectSpace importProject(String absolutePath) throws IOException {
 		return workSpace.importProject(absolutePath);
 	}
 
 	/**
 	 * Imports a project space from an exported project file.
 	 * 
 	 * @param projectTemplate project template
 	 */
 	public void importProject(TestProjectEnum projectTemplate) {
 		final String path;
 		path = projectTemplate.getPath();
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				String uriString = Activator.getDefault().getBundle().getLocation() + path;
 				if (File.separator.equals("/")) {
 					uriString = uriString.replace("reference:file:", "");
 
 				} else {
 					uriString = uriString.replace("reference:file:/", "");
 				}
 				try {
 					testProjectSpace = workSpace.importProject(uriString);
 					testProject = testProjectSpace.getProject();
 					projectId = testProjectSpace.getProjectId();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}.run(false);
 	}
 
 	/**
 	 * This shares test project with server.
 	 */
 	public void shareProject() {
 		LOGGER.log(Level.INFO, "sharing project...");
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				if (usersession == null) {
 					usersession = ModelFactory.eINSTANCE.createUsersession();
 					ServerInfo serverInfo = getServerInfo();
 					usersession.setServerInfo(serverInfo);
 					usersession.setUsername("super");
 					usersession.setPassword("super");
 					WorkspaceManager.getInstance().getCurrentWorkspace().getUsersessions().add(usersession);
 				}
 				try {
 					if (!usersession.isLoggedIn()) {
 						usersession.logIn();
 					}
 
 					getTestProjectSpace().shareProject(usersession, new NullProgressMonitor());
 					LOGGER.log(Level.INFO, "project shared.");
 				} catch (EmfStoreException e) {
 					e.printStackTrace();
 				}
 				projectId = testProjectSpace.getProjectId();
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Commits the changes to server.
 	 */
 	public void commitChanges() {
 		final LogMessage logMessage = createLogMessage(usersession.getUsername(), "some message");
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				System.out.println(IntegrationTestHelper
 					.getChangePackage(getTestProjectSpace().getOperations(), true, false).getOperations().size()
 					+ " operations.");
 				try {
 					getTestProjectSpace().commit(logMessage, null, new NullProgressMonitor());
 					System.out.println("commit successful!");
 				} catch (EmfStoreException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}.run(false);
 	}
 
 	/**
 	 * Create LogMessage.
 	 * 
 	 * @param name name
 	 * @param message message
 	 * @return LogMessage
 	 */
 	public static LogMessage createLogMessage(String name, String message) {
 		final LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		logMessage.setAuthor(name);
 		logMessage.setDate(Calendar.getInstance().getTime());
 		logMessage.setClientDate(Calendar.getInstance().getTime());
 		logMessage.setMessage(message);
 		return logMessage;
 	}
 
 	/**
 	 * Returns project to be compared with test project. This is project that lies on server after committing the
 	 * changes. We check out and return it.
 	 * 
 	 * @return project lying on the server
 	 * @throws EmfStoreException EmfStoreException
 	 */
 	public Project getCompareProject() throws EmfStoreException {
 		LOGGER.log(Level.INFO, "retrieving compare project...");
 		final ProjectInfo projectInfo = org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE
 			.createProjectInfo();
 		projectInfo.setName("CompareProject");
 		projectInfo.setDescription("compare project description");
 		projectInfo.setProjectId(projectId);
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 
 				try {
 					compareProject = WorkspaceManager.getInstance().getCurrentWorkspace()
 						.checkout(usersession, projectInfo).getProject();
 					LOGGER.log(Level.INFO, "compare project checked out.");
 				} catch (EmfStoreException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}.run(false);
 		return compareProject;
 	}
 
 	/**
 	 * @return the testProject
 	 */
 	public Project getTestProject() {
 		return testProject;
 	}
 
 	/**
 	 * @return test project space
 	 */
 	public ProjectSpace getTestProjectSpace() {
 		return testProjectSpace;
 	}
 
 	/**
 	 * @return workspace
 	 */
 	public Workspace getWorkSpace() {
 		return workSpace;
 	}
 
 	/**
 	 * @return the usersession
 	 */
 	public Usersession getUsersession() {
 		return usersession;
 	}
 
 	/**
 	 * Creates a versionsepc.
 	 * 
 	 * @param i verion
 	 * @return versionspec
 	 */
 	public static PrimaryVersionSpec createPrimaryVersionSpec(int i) {
 		PrimaryVersionSpec versionSpec = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 		versionSpec.setIdentifier(i);
 		return versionSpec;
 	}
 
 	/**
 	 * Creates a new Usersession and adds it to the workspace.
 	 * 
 	 * @param user the session is initialized with this User's name
 	 * @return the session
 	 */
 	public Usersession createUsersession(String user) {
 		Usersession session = ModelFactory.eINSTANCE.createUsersession();
 		getWorkSpace().getUsersessions().add(session);
 		session.setServerInfo(getServerInfo());
 		session.setUsername(user);
 		session.setPassword("foo");
 		return session;
 	}
 
 	/**
 	 * Creates a new project id.
 	 */
 	public void createNewProjectId() {
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				testProjectSpace.setProjectId(org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE
 					.createProjectId());
 				projectId = testProjectSpace.getProjectId();
 			}
 		}.run(false);
 	}
 
 	public static void removeServerTestProfile() {
 		String serverPath = ServerConfiguration.getServerHome();
 		File serverDirectory = new File(serverPath);
 		try {
 			FileUtil.deleteFolder(serverDirectory);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }

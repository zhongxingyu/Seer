 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.server;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommandWithResult;
 import org.eclipse.emf.emfstore.client.test.SetupHelper;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.UnknownSessionException;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.OperationsFactory;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * This TestCase tests all methods in the main {@link org.unicase.emfstore.EmfStore} interface.
  * 
  * @author wesendon
  */
 public class ServerInterfaceTest extends ServerTests {
 
 	/**
 	 * Gets the list of all projects.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void getProjectListTest() throws EmfStoreException {
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest());
 	}
 
 	/**
 	 * Gets a project from the server.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void getProjectTest() throws EmfStoreException {
 		ProjectSpace projectSpace2 = new EMFStoreCommandWithResult<ProjectSpace>() {
 
 			@Override
 			protected ProjectSpace doRun() {
 				try {
 					return WorkspaceManager
 						.getInstance()
 						.getCurrentWorkspace()
 						.checkout(TestSessionProvider.getInstance().getDefaultUsersession(), getProjectInfo(),
 							new NullProgressMonitor());
 				} catch (EmfStoreException e) {
 					Assert.fail();
 					return null;
 				}
 			}
 		}.run(false);
 		assertEqual(getProject(), projectSpace2.getProject());
 	}
 
 	/**
 	 * Creates a project on the server and then deletes it.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws EmfStoreException in case of failure.
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void createEmptyProjectTest() throws EmfStoreException {
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest());
 
 		new EMFStoreCommand() {
 
 			@Override
 			protected void doRun() {
 				ProjectSpace projectSpace = WorkspaceManager.getInstance().getCurrentWorkspace()
 					.createLocalProject("createEmptyProjectAndDelete", "TestProject");
 				try {
 					projectSpace.shareProject();
 				} catch (EmfStoreException e) {
 					Assert.fail();
 				}
 			}
 		}.run(false);
 
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest() + 1);
 	}
 
 	/**
 	 * Creates a project, shares it with the server and then deletes it.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws EmfStoreException in case of failure.
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void shareProjectTest() throws EmfStoreException {
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest());
 
 		ProjectSpace projectSpace2 = new EMFStoreCommandWithResult<ProjectSpace>() {
 
 			@Override
 			protected ProjectSpace doRun() {
 				ProjectSpace projectSpace = WorkspaceManager.getInstance().getCurrentWorkspace()
 					.createLocalProject("createEmptyProjectAndDelete", "TestProject");
 				try {
 					projectSpace.shareProject();
 					return projectSpace;
 				} catch (EmfStoreException e) {
 					Assert.fail();
 					return null;
 				}
 			}
 		}.run(false);
 
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest() + 1);
 		assertNotNull(getProject());
 		assertEqual(getProject(), projectSpace2.getProject());
 	}
 
 	/**
 	 * Deletes a project on the server.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void deleteProjectTest() throws EmfStoreException {
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest());
 
 		WorkspaceManager.getInstance().getCurrentWorkspace().deleteRemoteProject(getServerInfo(), getProjectId(), true);
 
 		try {
 			List<ProjectInfo> remoteProjectList = WorkspaceManager.getInstance().getCurrentWorkspace()
 				.getRemoteProjectList(getServerInfo());
 			for (ProjectInfo projectInfo : remoteProjectList) {
 				if (projectInfo.getProjectId() == getProjectId()) {
 					assertTrue(false);
 				}
 			}
 			assertTrue(true);
 		} catch (EmfStoreException e) {
 			assertTrue(true);
 		}
 		assertTrue(WorkspaceManager.getInstance().getCurrentWorkspace().getRemoteProjectList(getServerInfo()).size() == getProjectsOnServerBeforeTest() - 1);
 	}
 
 	/**
 	 * Resolves version spec.
 	 * 
 	 * @throws EmfStoreException in case of failure.
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void resolveVersionSpecTest() throws EmfStoreException {
 		List<ProjectInfo> remoteProjectList = WorkspaceManager.getInstance().getCurrentWorkspace()
 			.getRemoteProjectList(getServerInfo());
 
 		boolean sameVersionSpec = false;
 		for (ProjectInfo projectInfo : remoteProjectList) {
 			if (projectInfo.getVersion().equals(getProjectVersion())) {
 				sameVersionSpec = true;
 			}
 		}
 
 		if (sameVersionSpec) {
 			assertTrue(true);
 		} else {
 			assertTrue(false);
 		}
 	}
 
 	/**
 	 * Creates a version.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void createVersionTest() throws EmfStoreException {
 
 		PrimaryVersionSpec createdVersion = new EMFStoreCommandWithResult<PrimaryVersionSpec>() {
 			@Override
 			protected PrimaryVersionSpec doRun() {
 				getProject().addModelElement(
 					org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectHistory());
 
 				try {
 
 					return getProjectSpace().commit(SetupHelper.createLogMessage("bla", "blablba"), null, null);
 
 				} catch (EmfStoreException e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 		}.run(false);
 
 		PrimaryVersionSpec baseVersion = getProjectSpace().getBaseVersion();
 
 		assertTrue(baseVersion.equals(createdVersion));
 	}
 
 	/**
 	 * Gets changes, which should be empty.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void getEmptyChangesTest() throws EmfStoreException {
 		List<ChangePackage> changes = getProjectSpace().getChanges(SetupHelper.createPrimaryVersionSpec(0),
 			getProjectVersion());
 		assertTrue(changes.size() == 0);
 	}
 
 	/**
 	 * Gets changes.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 * @throws SerializationException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 * @throws SerializationException
 	 */
 	@Test
 	public void getChangesTest() throws EmfStoreException, SerializationException {
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getProject().addModelElement(
 					org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectHistory());
 			}
 		}.run(false);
 
 		final AttributeOperation attributeOperation = OperationsFactory.eINSTANCE.createAttributeOperation();
 		attributeOperation.setModelElementId(getProject().getModelElementId(
 			(EObject) getProject().getAllModelElements().toArray()[0]));
 		attributeOperation.setFeatureName("name");
 		attributeOperation.setNewValue("nameeee");
 
 		PrimaryVersionSpec resolvedVersionSpec = projectSpace.getBaseVersion();
 
 		PrimaryVersionSpec versionSpec = new EMFStoreCommandWithResult<PrimaryVersionSpec>() {
 			@Override
 			protected PrimaryVersionSpec doRun() {
 				getProjectSpace().getOperations().add(attributeOperation);
 				try {
 					return getProjectSpace().commit(SetupHelper.createLogMessage("bla", "blablba"), null, null);
 
 				} catch (EmfStoreException e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 		}.run(false);
 
 		List<ChangePackage> changes = getProjectSpace().getChanges(resolvedVersionSpec, versionSpec);
 
 		assertTrue(changes.size() == 1);
 		for (ChangePackage cp : changes) {
 			assertTrue(cp.getOperations().size() == 2);
 			assertTrue(ModelUtil.areEqual(cp.getOperations().get(1), attributeOperation));
 		}
 
 	}
 
 	/**
 	 * Gets a historyInfo.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void getHistoryInfoTest() throws EmfStoreException {
 		final String logMessage = "historyInfo";
 		PrimaryVersionSpec createdVersion = new EMFStoreCommandWithResult<PrimaryVersionSpec>() {
 			@Override
 			protected PrimaryVersionSpec doRun() {
 				getProject().addModelElement(
 					org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectHistory());
 
 				try {
 					return getProjectSpace().commit(SetupHelper.createLogMessage("bla", logMessage), null, null);
 
 				} catch (EmfStoreException e) {
 					throw new RuntimeException(e);
 				}
 
 			}
 		}.run(false);
 
 		List<HistoryInfo> historyInfo = getProjectSpace().getHistoryInfo(
 			createHistoryQuery(createdVersion, createdVersion));
 
 		assertTrue(historyInfo.size() == 1);
 		// assertTrue(historyInfo.get(0).getLogMessage().getMessage().equals(logMessage));
 	}
 
 	/**
 	 * Sets a tag on the head revision.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void addTagTest() throws EmfStoreException {
 		String tagName = "testValue";
 
 		TagVersionSpec tag = VersioningFactory.eINSTANCE.createTagVersionSpec();
 		tag.setName(tagName);
 
 		getProjectSpace().addTag(getProjectVersion(), tag);
 
 		List<HistoryInfo> historyInfo = getProjectSpace().getHistoryInfo(
 			createHistoryQuery(getProjectVersion(), getProjectVersion()));
 
 		assertTrue(historyInfo.size() == 1);
		for (TagVersionSpec tagVersionSpec : historyInfo.get(0).getTagSpecs()) {
			if (!tagVersionSpec.getName().equals("HEAD")) {
				assertTrue(tagVersionSpec.getName().equals(tagName));
			}
		}
 	}
 
 	/**
 	 * Removes a tag.
 	 * 
 	 * @throws EmfStoreException in case of failure.
 	 */
 	/**
 	 * @throws EmfStoreException
 	 */
 	@Test
 	public void removeTagTest() throws EmfStoreException {
 		String tagName = "testValue";
 
 		TagVersionSpec tag = VersioningFactory.eINSTANCE.createTagVersionSpec();
 		tag.setName(tagName);
 
 		getProjectSpace().addTag(getProjectVersion(), tag);
 		getProjectSpace().removeTag(getProjectVersion(), tag);
 
 		List<HistoryInfo> historyInfo = getProjectSpace().getHistoryInfo(
 			createHistoryQuery(getProjectVersion(), getProjectVersion()));
 
 		assertTrue(historyInfo.size() == 1);
		for (TagVersionSpec tagVersionSpec : historyInfo.get(0).getTagSpecs()) {
			if (!tagVersionSpec.getName().equals("HEAD")) {
				assertTrue(tagVersionSpec.getName().equals(tagName));
			}
		}
 	}
 
 	/**
 	 * Deletes session on server and creates new one.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	// @Test
 	/**
 	 * @throws EmfStoreException
 	 */
 	public void logOutTest() throws EmfStoreException {
 
 		Assert.assertNotNull(getConnectionManager().resolveUser(getSessionId(), null));
 		getConnectionManager().logout(getSessionId());
 		try {
 			getConnectionManager().resolveUser(getSessionId(), null);
 			// if no exception is thrown at this point, the test fails.
 			Assert.assertTrue(false);
 		} catch (UnknownSessionException e) {
 			Assert.assertTrue(true);
 		}
 		// relogin for next test
 		login();
 		Assert.assertNotNull(getConnectionManager().resolveUser(getSessionId(), null));
 	}
 
 }

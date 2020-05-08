 /*******************************************************************************
  * Copyright (c) 2008-2012 EclipseSource Muenchen GmbH,
  * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
  * Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.modelmutator.test;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.test.SetupHelper;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.UnknownSessionException;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
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
 public class ModelMutatorServerInterfaceTest extends ModelMutatorServerSetup {
 
 	/**
 	 * If the user is logged in, the result of resolve user mustn't be null.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#resolveUser(org.eclipse.emf.emfstore.server.model.SessionId,
 	 *      org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnitId)
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void resolveUserTest() throws EmfStoreException {
 		assertTrue(getConnectionManager().resolveUser(getSessionId(), null) != null);
 
 		// TODO: test with orgunitid argument
 	}
 
 	/**
 	 * Gets the list of all projects.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void getProjectListTest() throws EmfStoreException {
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest());
 	}
 
 	/**
 	 * Gets a project from the server.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void getProjectTest() throws EmfStoreException {
 		Project project = getConnectionManager().getProject(getSessionId(), getGeneratedProjectId(),
 			VersionSpec.HEAD_VERSION);
 		assertEqual(getGeneratedProject(), project);
 	}
 
 	/**
 	 * Creates a project on the server and then deletes it.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws EmfStoreException in case of failure.
 	 */
 	@Test
 	public void createEmptyProjectTest() throws EmfStoreException {
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest());
 
 		getConnectionManager().createEmptyProject(getSessionId(), "createEmptyProjectAndDelete", "TestProject",
 			SetupHelper.createLogMessage("super", "a logmessage"));
 
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest() + 1);
 	}
 
 	/**
 	 * Creates a project, shares it with the server and then deletes it.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws EmfStoreException in case of failure.
 	 */
 	@Test
 	public void shareProjectTest() throws EmfStoreException {
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest());
 
 		ProjectInfo projectInfo = getConnectionManager().createProject(getSessionId(), "createProjectAndDelete",
 			"TestProject", SetupHelper.createLogMessage("super", "a logmessage"), getGeneratedProject());
 
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest() + 1);
 		assertNotNull(getGeneratedProject());
 		assertEqual(getGeneratedProject(),
 			getConnectionManager().getProject(getSessionId(), projectInfo.getProjectId(), VersionSpec.HEAD_VERSION));
 	}
 
 	/**
 	 * Deletes a project on the server.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void deleteProjectTest() throws EmfStoreException {
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest());
 
 		getConnectionManager().deleteProject(getSessionId(), getGeneratedProjectId(), false);
 		try {
 			getConnectionManager().getProject(getSessionId(), getGeneratedProjectId(), VersionSpec.HEAD_VERSION);
 			assertTrue(false);
 		} catch (EmfStoreException e) {
 			assertTrue(true);
 		}
 		assertTrue(getConnectionManager().getProjectList(getSessionId()).size() == getProjectsOnServerBeforeTest() - 1);
 	}
 
 	/**
 	 * Resolves version spec.
 	 * 
 	 * @throws EmfStoreException in case of failure.
 	 */
 	@Test
 	public void resolveVersionSpecTest() throws EmfStoreException {
 		PrimaryVersionSpec resolvedVersionSpec = getConnectionManager().resolveVersionSpec(getSessionId(),
 			getGeneratedProjectId(), VersionSpec.HEAD_VERSION);
 		assertTrue(resolvedVersionSpec.equals(getGeneratedProjectVersion()));
 	}
 
 	/**
 	 * Creates a version.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void createVersionTest() throws EmfStoreException {
 		PrimaryVersionSpec resolvedVersionSpec = getConnectionManager().resolveVersionSpec(getSessionId(),
 			getGeneratedProjectId(), VersionSpec.HEAD_VERSION);
 
		// TODO BRANCH
 		PrimaryVersionSpec createdVersion = getConnectionManager().createVersion(getSessionId(),
			getGeneratedProjectId(), resolvedVersionSpec, VersioningFactory.eINSTANCE.createChangePackage(), null,
			null, SetupHelper.createLogMessage("bla", "blablba"));
 
 		resolvedVersionSpec = getConnectionManager().resolveVersionSpec(getSessionId(), getGeneratedProjectId(),
 			VersionSpec.HEAD_VERSION);
 
 		assertTrue(resolvedVersionSpec.equals(createdVersion));
 	}
 
 	/**
 	 * Gets changes, which should be empty.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void getEmptyChangesTest() throws EmfStoreException {
 		List<ChangePackage> changes = getConnectionManager().getChanges(getSessionId(), getGeneratedProjectId(),
 			SetupHelper.createPrimaryVersionSpec(0), getGeneratedProjectVersion());
 		assertTrue(changes.size() == 0);
 	}
 
 	/**
 	 * Gets changes.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 * @throws SerializationException in case of failure
 	 */
 	@Test
 	public void getChangesTest() throws EmfStoreException, SerializationException {
 		ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 
 		AttributeOperation attributeOperation = OperationsFactory.eINSTANCE.createAttributeOperation();
 		attributeOperation.setModelElementId(getGeneratedProject().getModelElementId(
 			(EObject) getGeneratedProject().getAllModelElements().toArray()[0]));
 		attributeOperation.setFeatureName("name");
 		attributeOperation.setNewValue("nameeee");
 
 		changePackage.getOperations().add(attributeOperation);
 
 		PrimaryVersionSpec resolvedVersionSpec = getConnectionManager().resolveVersionSpec(getSessionId(),
 			getGeneratedProjectId(), VersionSpec.HEAD_VERSION);
		// TODO BRANCH
 		PrimaryVersionSpec versionSpec = getConnectionManager().createVersion(getSessionId(), getGeneratedProjectId(),
			resolvedVersionSpec, changePackage, null, null, SetupHelper.createLogMessage("", ""));
 
 		List<ChangePackage> changes = getConnectionManager().getChanges(getSessionId(), getGeneratedProjectId(),
 			resolvedVersionSpec, versionSpec);
 
 		assertTrue(changes.size() == 1);
 		for (ChangePackage cp : changes) {
 			assertTrue(cp.getOperations().size() == 1);
 			assertTrue(ModelUtil.eObjectToString(cp.getOperations().get(0)).equals(
 				ModelUtil.eObjectToString(attributeOperation)));
 		}
 
 	}
 
 	/**
 	 * Gets a historyInfo.
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 */
 	@Test
 	public void getHistoryInfoTest() throws EmfStoreException {
 		String logMessage = "historyInfo";
 
 		PrimaryVersionSpec resolvedVersionSpec = getConnectionManager().resolveVersionSpec(getSessionId(),
 			getGeneratedProjectId(), VersionSpec.HEAD_VERSION);

		// TODO BRANCH
 		PrimaryVersionSpec createdVersion = getConnectionManager().createVersion(getSessionId(),
			getGeneratedProjectId(), resolvedVersionSpec, VersioningFactory.eINSTANCE.createChangePackage(), null,
			null, SetupHelper.createLogMessage("bla", logMessage));
 
 		List<HistoryInfo> historyInfo = getConnectionManager().getHistoryInfo(getSessionId(), getGeneratedProjectId(),
 			createHistoryQuery(createdVersion, createdVersion));
 
 		assertTrue(historyInfo.size() == 1);
 		assertTrue(historyInfo.get(0).getLogMessage().getMessage().equals(logMessage));
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
 
 		getConnectionManager().addTag(getSessionId(), getGeneratedProjectId(), getGeneratedProjectVersion(), tag);
 
 		List<HistoryInfo> historyInfo = getConnectionManager().getHistoryInfo(getSessionId(), getGeneratedProjectId(),
 			createHistoryQuery(getGeneratedProjectVersion(), getGeneratedProjectVersion()));
 
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
 	@Test
 	public void removeTagTest() throws EmfStoreException {
 		String tagName = "testValue";
 
 		TagVersionSpec tag = VersioningFactory.eINSTANCE.createTagVersionSpec();
 		tag.setName(tagName);
 
 		getConnectionManager().addTag(getSessionId(), getGeneratedProjectId(), getGeneratedProjectVersion(), tag);
 
 		getConnectionManager().removeTag(getSessionId(), getGeneratedProjectId(), getGeneratedProjectVersion(), tag);
 
 		List<HistoryInfo> historyInfo = getConnectionManager().getHistoryInfo(getSessionId(), getGeneratedProjectId(),
 			createHistoryQuery(getGeneratedProjectVersion(), getGeneratedProjectVersion()));
 
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
 	@Test
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
 		login(SetupHelper.getServerInfo());
 		Assert.assertNotNull(getConnectionManager().resolveUser(getSessionId(), null));
 	}
 
 }

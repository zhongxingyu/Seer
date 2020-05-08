 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.changeTracking.topology;
 
 import java.util.Date;
 
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.junit.Before;
 
 /**
  * Abstract super class for operation tests, contains setup.
  * 
  * @author chodnick
  */
 public abstract class TopologyTest {
 
 	private Project project;
 	private ProjectSpace projectSpace;
 
 	/**
 	 * Setup a dummy project for testing.
 	 */
 	@Before
 	public void setupProjectSpace() {
 		ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setBaseVersion(VersioningFactory.eINSTANCE.createPrimaryVersionSpec());
 		projectSpace.setIdentifier("testProjectSpace");
 		projectSpace.setLastUpdated(new Date());
 		projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 		projectSpace.setProjectDescription("ps description");
 		projectSpace.setProjectId(org.eclipse.emf.emfstore.server.model.ModelFactory.eINSTANCE.createProjectId());
 		projectSpace.setProjectName("ps name");
 
 		setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createProject());
 
 		projectSpace.setProject(getProject());
 
 		projectSpace.makeTransient();
 		projectSpace.init();
 
 		setProjectSpace(projectSpace);
 
 	}
 
 	/**
 	 * @param project the project to set
 	 */
 	private void setProject(Project project) {
 		this.project = project;
 	}
 
 	/**
 	 * @return the project
 	 */
 	public Project getProject() {
 		return project;
 	}
 
 	/**
 	 * @param projectSpace the projectSpace to set
 	 */
 	private void setProjectSpace(ProjectSpace projectSpace) {
 		this.projectSpace = projectSpace;
 	}
 
 	/**
 	 * @return the projectSpace
 	 */
 	public ProjectSpace getProjectSpace() {
 		return projectSpace;
 	}
 
 	/**
 	 * Clear all operations from project space.
 	 */
 	protected void clearOperations() {
		clearOperations();
 	}
 
 }

 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.jaxrs.core;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.core.JavaProject;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.MethodRule;
 import org.junit.rules.TestWatchman;
 import org.junit.runners.model.FrameworkMethod;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Made abstract, so won't be automatically picked up as test (since intended to
  * be subclassed).
  * 
  * Based on
  * http://dev.eclipse.org/viewcvs/index.cgi/incubator/sourceediting/tests
  * /org.eclipse
  * .wst.xsl.ui.tests/src/org/eclipse/wst/xsl/ui/tests/AbstractXSLUITest
  * .java?revision=1.2&root=WebTools_Project&view=markup */
 @RunWithProject("org.jboss.tools.ws.jaxrs.tests.sampleproject")
 @SuppressWarnings("restriction")
 public abstract class AbstractCommonTestCase {
 
 	@SuppressWarnings("unused")
 	private static final String M2_REPO = "M2_REPO";
 
 	public static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommonTestCase.class);
 
 	protected String projectName = null;
 
 	protected IJavaProject javaProject;
 
 	protected IProject project;
 
 	protected static Bundle bundle = JBossJaxrsCoreTestsPlugin.getDefault().getBundle();
 
 	public final static String DEFAULT_SAMPLE_PROJECT_NAME = WorkbenchUtils
 			.retrieveSampleProjectName(AbstractCommonTestCase.class);
 
 	private ProjectSynchronizator synchronizor;
 
 	@Rule
 	public MethodRule watchman = new TestWatchman() {
 		@Override
 		public void starting(FrameworkMethod method) {
 			LOGGER.info("**********************************************************************************");
 			LOGGER.info("Starting test '{}'...", method.getName());
 			LOGGER.info("**********************************************************************************");
 		}
 
 		@Override
 		public void finished(FrameworkMethod method) {
 			LOGGER.info("**********************************************************************************");
 			LOGGER.info("Test '{}' finished.", method.getName());
 			LOGGER.info("**********************************************************************************");
 		}
 	};
 
 	@BeforeClass
 	public static void setupWorkspace() throws Exception {
		org.eclipse.jdt.core.JavaCore.getPlugin().start(bundle.getBundleContext());
 		long startTime = new Date().getTime();
 		try {
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			if (workspace.isAutoBuilding()) {
 				IWorkspaceDescription description = workspace.getDescription();
 				description.setAutoBuilding(false);
 				workspace.setDescription(description);
 			}
 
 			workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
 			LOGGER.info("Initial Synchronization (@BeforeClass)");
 			WorkbenchTasks.syncSampleProject(DEFAULT_SAMPLE_PROJECT_NAME);
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Initial Workspace setup in " + (endTime - startTime) + "ms.");
 		}
 	}
 
 	@BeforeClass
 	public static void unregistrerListeners() {
 		JBossJaxrsCorePlugin.getDefault().unregisterListeners();
 	}
 
 	@Before
 	public void bindSampleProject() throws Exception {
 		long startTime = new Date().getTime();
 		try {
 			JBossJaxrsCorePlugin.getDefault().unregisterListeners();
 
 			projectName = WorkbenchUtils.retrieveSampleProjectName(this.getClass());
 			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 			project.open(new NullProgressMonitor());
 			javaProject = JavaCore.create(project);
 			javaProject.open(new NullProgressMonitor());
 			Assert.assertNotNull("JavaProject not found", javaProject.exists());
 			Assert.assertNotNull("Project not found", javaProject.getProject().exists());
 			Assert.assertTrue("Project is not a JavaProject", JavaProject.hasJavaNature(javaProject.getProject()));
 			synchronizor = new ProjectSynchronizator();
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.addResourceChangeListener(synchronizor);
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Test Workspace setup in " + (endTime - startTime) + "ms.");
 		}
 	}
 
 	@After
 	public void removeResourceChangeListener() throws CoreException, InvocationTargetException, InterruptedException {
 		long startTime = new Date().getTime();
 		try {
 			LOGGER.info("Synchronizing the workspace back to its initial state...");
 			// remove listener before sync' to avoid desync...
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			workspace.removeResourceChangeListener(synchronizor);
 			synchronizor.resync();
 		} finally {
 			long endTime = new Date().getTime();
 			LOGGER.info("Test Workspace sync'd in " + (endTime - startTime) + "ms.");
 		}
 	}
 
 }

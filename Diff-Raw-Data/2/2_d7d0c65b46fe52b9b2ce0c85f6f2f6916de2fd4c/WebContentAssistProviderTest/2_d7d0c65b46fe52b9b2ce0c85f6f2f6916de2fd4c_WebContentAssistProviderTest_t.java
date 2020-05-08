 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.test;
 
 import java.util.List;
 import java.util.Properties;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.jboss.tools.common.model.XJob;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.Watcher;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.test.util.TestProjectProvider;
 import org.jboss.tools.jst.web.project.list.WebPromptingProvider;
 
 public class WebContentAssistProviderTest extends TestCase {
 
 	TestProjectProvider provider = null;
 	IProject project = null;
 	boolean makeCopy = true;
 	XModel projectModel;
 	WebPromptingProvider webPromptingProvider;
 
 	public static Test suite() {
 		return new TestSuite(WebContentAssistProviderTest.class);
 	}
 
 	public void testJsfBeanPropertyList() {
 		// seam beans list
 		List beanList = webPromptingProvider.getList(projectModel, WebPromptingProvider.JSF_BEAN_PROPERTIES, "numberGuess", new Properties());
 		assertTrue("Bean property list does not contain Seam bean property in XModel.", beanList.contains("remainingGuesses"));
 		// jsf beans list
 		beanList = webPromptingProvider.getList(projectModel, WebPromptingProvider.JSF_BEAN_PROPERTIES, "facesManagedBean", new Properties());
 		assertTrue("Bean property list does not contain Managed bean property in XModel.", beanList.contains("property1"));
 	}
 
 	public void testBundles() {
 		// bundle name list
 		List bundleList = webPromptingProvider.getList(projectModel, WebPromptingProvider.JSF_BUNDLES, "", null);
 		assertTrue("Bundle name list does not contain expected name in XModel.", bundleList.contains("org.jboss.seam.example.numberguess.test"));
 		// bundle property list
 		List bundlePropertyList = webPromptingProvider.getList(projectModel, WebPromptingProvider.JSF_BUNDLE_PROPERTIES, "org.jboss.seam.example.numberguess.test", null);
 		assertTrue("Bundle property list does not contain expected property in XModel.", bundlePropertyList.contains("bundleProperty1"));
 	}
 	
 	void waitForJobs() {
 		try {
 			XJob.waitForJob();
 		} catch (InterruptedException e) {
 			fail("Interrupted");
 		}
 	}
 
 	public void testTlds() {
 		waitForJobs();
 		List tldList = webPromptingProvider.getList(projectModel, WebPromptingProvider.JSF_GET_TAGLIBS, "", null);
 		assertTrue("TLD list does not contain expected TLD in XModel.", tldList.contains("http://jboss.com/products/seam/taglib"));
 	}
 
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.web.test", null, "TestsWebArtefacts", makeCopy); 
 		project = provider.getProject();
 		try {
 			project.refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (Exception e) {
			ModelPlugin.getPluginLog().logError(e);
 		}
 		XModelObject xmo = EclipseResourceUtil.getObjectByResource(project);
 		assertNotNull("Can't get XModel Object for test project.", xmo);
 		projectModel = xmo.getModel();
 		Watcher.getInstance(projectModel).forceUpdate();
 		projectModel.update();
 		assertNotNull("Can't get XModel for test project.", projectModel);
 		webPromptingProvider = WebPromptingProvider.getInstance();
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 }

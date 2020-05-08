 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.test;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.osgi.util.NLS;
 import org.jboss.tools.common.base.test.validation.TestUtil;
 import org.jboss.tools.common.validation.ValidatorManager;
 import org.jboss.tools.jst.web.validation.WebXMLCoreValidator;
 import org.jboss.tools.jst.web.validation.WebXMLValidatorMessages;
 import org.jboss.tools.jst.web.webapp.model.WebAppConstants;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ProjectImportTestSetup;
 import org.jboss.tools.test.util.ResourcesUtils;
 import org.jboss.tools.tests.AbstractResourceMarkerTest;
 
 /**
  * @author Alexey Kazakov
  */
 public class WebXMLValidationTest extends TestCase {
 
 	private IProject project;
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		project = ProjectImportTestSetup.loadProject(JstWebAllTests.PROJECT_NAME);
 		IResource target = project.getFolder("target");
 		target.setDerived(true, null);
 	}
 
 	/**
 	 * Test position of marker when comments contain textually matching fragments.
 	 */
 	public void testServletClassWithIdenticalComments() throws Exception {
 		String path0 = "WebContent/WEB-INF/web.xml";
 		IFile webxml = project.getFile(path0);
 		AbstractResourceMarkerTest.assertMarkerIsCreated(webxml, NLS.bind(WebXMLValidatorMessages.CLASS_NOT_EXISTS, "servlet-class", "javax.faces.webapp.FacesServlet111"), 11);
 	}
 
 	/**
 	 * See JBIDE-10161
 	 * @throws Exception
 	 */
 	public void testValidationInDerived() throws Exception {
 		//Check first that original web.xml contains errors. 
 		String path0 = "WebContent/WEB-INF/web.xml";
 		IFile webxml = project.getFile(path0);
 		assertTrue(webxml.exists());
 		IMarker[] markers = webxml.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length > 0);
 		
 		//Now we will copy that original web.xml to different folders and check incremental validation.
 
 		//Check that file added to a non-derived folder is validated incrementally.
 		String path1 = "aFolder/WEB-INF/web.xml";
 		IFile webxml1 = project.getFile(path1);
 		replaceFile(project, path0, path1);
 		markers = webxml1.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length > 0);
 		for (IMarker m: markers) {
 			System.out.println(m.getAttribute(IMarker.MESSAGE));
 		}
 
 		//Check that added to a derived folder, file will be skipped at incremental validation.
 		String path2 = "target/m2e-wtp/web-resources/WEB-INF/web.xml";
 		IFile webxml2 = project.getFile(path2);
 		replaceFile(project, path0, path2);
 		markers = webxml2.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length == 0);
 	}
 
 	/**
 	 * Tests incremental validation
 	 * @throws Exception
 	 */
 	public void testPathsInLoginConfig() throws Exception {
 		String path0 = "WebContent/WEB-INF/web.xml";
 		IFile webxml = project.getFile(path0);
 		assertTrue(webxml.exists());
 		
 		String path1 = "WebContent/WEB-INF/web.xml.loginconfigtest1";
 		replaceFile(project, path1, path0);
 		IMarker[] markers = webxml.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertFalse(hasMarkerOnLine(markers, 18));
 		assertTrue(hasMarkerOnLine(markers, 19));
 
 		String path2 = "WebContent/WEB-INF/web.xml.loginconfigtest2";
 		replaceFile(project, path2, path0);
 		markers = webxml.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(hasMarkerOnLine(markers, 18));
 		assertFalse(hasMarkerOnLine(markers, 19));
 	}
 
 	static boolean hasMarkerOnLine(IMarker[] ms, int line) {
 		return getMarkerOnLine(ms, line) != null;
 	}
 
 	static IMarker getMarkerOnLine(IMarker[] ms, int line) {
 		for (IMarker m: ms) {
 			int l = m.getAttribute(IMarker.LINE_NUMBER, -1);
 			if(line == l) {
 				return m;
 			}
 		}
 		return null;
 	}
 
 	public void testServletMapping() throws CoreException {
 		IFile webxml = project.getFile("WebContent/WEB-INF/webJAXFX.xml");
 		IMarker[] markers = webxml.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 
 		//1. If servlet-mapping/servlet-name=javax.ws.rs.core.Application, it is ok.
 		assertFalse(hasMarkerOnLine(markers, 6));
 
 		//2. If servlet-mapping/servlet-name is a class it should extend javax.ws.rs.core.Application.
 		assertTrue(hasMarkerOnLine(markers, 11));
 		IMarker m = getMarkerOnLine(markers, 11);
 		String expected = NLS.bind(WebXMLValidatorMessages.CLASS_NOT_EXTENDS, new Object[]{WebAppConstants.SERVLET_NAME, "test.MyApplication", "javax.ws.rs.core.Application"});
 		assertEquals(expected, m.getAttribute(IMarker.MESSAGE, ""));
 
 		//3. If servlet-mapping/servlet-name is not a class, it should reference servlet/servlet-name
 		assertTrue(hasMarkerOnLine(markers, 16));
 		m = getMarkerOnLine(markers, 16);
 		expected = NLS.bind(WebXMLValidatorMessages.SERVLET_NOT_EXISTS, new Object[]{WebAppConstants.SERVLET_NAME, "notaservlet"});
 		assertEquals(expected, m.getAttribute(IMarker.MESSAGE, ""));
 
 		assertFalse(hasMarkerOnLine(markers, 24));
 	}
 
 	public static void replaceFile(IProject project, String sourcePath, String targetPath) throws CoreException {
 		boolean saveAutoBuild = ResourcesUtils.setBuildAutomatically(false);
 		try {
 			IFile target = project.getFile(new Path(targetPath));
 			IFile source = project.getFile(new Path(sourcePath));
 			assertTrue(source.exists());
 			ValidatorManager.setStatus(ValidatorManager.RUNNING);
 			if(!target.exists()) {
 				target.create(source.getContents(), true, new NullProgressMonitor());
 			} else {
 				target.setContents(source.getContents(), true, false, new NullProgressMonitor());
 			}
 			JobUtils.waitForIdle();
 			TestUtil.validate(target);
 		} finally {
 			ResourcesUtils.setBuildAutomatically(saveAutoBuild);
 			JobUtils.waitForIdle();
 		}
 	}
 
 }

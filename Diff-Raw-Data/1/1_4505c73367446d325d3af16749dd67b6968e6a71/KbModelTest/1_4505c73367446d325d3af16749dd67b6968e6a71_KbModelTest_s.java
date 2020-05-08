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
 package org.jboss.tools.jst.web.kb.test;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.internal.scanner.LoadedDeclarations;
 import org.jboss.tools.jst.web.kb.internal.scanner.ScannerException;
 import org.jboss.tools.jst.web.kb.internal.scanner.XMLScanner;
 import org.jboss.tools.jst.web.kb.internal.taglib.ELFunction;
 import org.jboss.tools.jst.web.kb.taglib.IELFunction;
 import org.jboss.tools.jst.web.kb.taglib.IFunctionLibrary;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.test.util.JUnitUtils;
 
 public class KbModelTest extends TestCase {
 
 	IProject project = null;
 	boolean makeCopy = true;
 
 	public KbModelTest() {
 		super("Kb Model Test");
 	}
 
 	public void setUp() throws Exception {
 		project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestKbModel");
 		assertNotNull("Can't load TestKbModel", project); //$NON-NLS-1$
 	}
 
 	private IKbProject getKbProject() {
 		IKbProject kbProject = null;
 		try {
 			kbProject = (IKbProject)project.getNature(IKbProject.NATURE_ID);
 		} catch (Exception e) {
 			JUnitUtils.fail("Cannot get seam nature.",e);
 		}
 		return kbProject;
 	}
 
 	public void testTldXMLScanner() {
 		IKbProject kbProject = getKbProject();
 		
 		IFile f = project.getFile("WebContent/WEB-INF/faces-config.xml");
 		assertNotNull(f);
 		XMLScanner scanner = new XMLScanner();
 		List<ITagLibrary> ls = null;		
 		ls = null;
 		f = project.getFile("WebContent/WEB-INF/taglib2.tld");
 		assertNotNull(f);
 		try {
 			LoadedDeclarations ds = scanner.parse(f, kbProject);
 			ls = ds.getLibraries();
 		} catch (ScannerException e) {
 			JUnitUtils.fail("Error in xml scanner",e);
 		}
 		assertEquals(1, ls.size());
 		assertTrue(ls.get(0).getComponents().length > 0);
 		
 //		System.out.println("Libraries found=" + ls.size());
 //		for (ITagLibrary l: ls) {
 //			System.out.println(l + ":=>" + l.getComponents().length + " " + l.getURI());
 //		}
 	}
 
 	public void testKbProjectObjects() {
 		IKbProject kbProject = getKbProject();
 		ITagLibrary[] ls = kbProject.getTagLibraries("taglib2");
 		assertEquals(1, ls.length);
 		ITagLibrary l = ls[0];
 		assertTrue(l instanceof IFunctionLibrary);
 		IELFunction[] fs = ((IFunctionLibrary)l).getFunctions();
 		assertEquals(1, fs.length);
 		assertEquals("f1", fs[0].getName());
 		assertEquals("s1", fs[0].getFunctionSignature());
 		
 		//TODO continue
 		
 	}
 
 	public void toDoXMLSerialization() {
 		
 	}
 
 	public void toDoCleanBuild() {
 
 	}
 }

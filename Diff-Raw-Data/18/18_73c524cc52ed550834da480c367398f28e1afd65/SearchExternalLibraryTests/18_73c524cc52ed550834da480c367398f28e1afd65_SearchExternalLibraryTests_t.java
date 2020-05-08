 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.search;
 
 import static org.eclipse.dltk.javascript.core.tests.AllTests.PLUGIN_ID;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.tests.model.AbstractSingleProjectSearchTests;
 import org.eclipse.dltk.core.tests.model.TestSearchResults;
 import org.eclipse.dltk.core.tests.util.StringList;
 
 public class SearchExternalLibraryTests extends
 		AbstractSingleProjectSearchTests {
 
 	private static final String MY_EXPORTS_JS = "myExports.js";
 
 	public SearchExternalLibraryTests(String testName) {
 		super(PLUGIN_ID, testName, "searchExtLib");
 	}
 
 	public static Suite suite() {
 		return new Suite(SearchExternalLibraryTests.class);
 	}
 
 	private static final String LIB_NAME = "MyLibrary";
 
 	private File library;
 
 	@Override
 	public void setUpSuite() throws Exception {
 		library = File.createTempFile("dltk", "js");
 		if (library.exists()) {
 			library.delete();
 		}
 		assertTrue(library.mkdir());
 		final File f = new File(library, MY_EXPORTS_JS);
 		final StringList code = new StringList();
 		code.add("function myLibraryExports() {");
 		code.add("}");
 		final FileWriter writer = new FileWriter(f);
 		writer.write(code.toString());
 		writer.close();
 		DLTKCore.setBuildpathVariable(LIB_NAME, EnvironmentPathUtils
 				.getFullPath(EnvironmentManager.getLocalEnvironment(),
 						new Path(library.getAbsolutePath())), null);
 		super.setUpSuite();
 	}
 
	private void deleteDir(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					deleteDir(child);
				}
			}
		}
		file.delete();
	}

 	@Override
 	public void tearDownSuite() throws Exception {
		if (library != null) {
			deleteDir(library);
		}
 		super.tearDownSuite();
 		DLTKCore.removeBuildpathVariable(LIB_NAME, null);
 	}
 
 	public void testFindScriptFolderWithoutEnv() throws ModelException {
 		final IPath libraryPath = new Path(library.getAbsolutePath());
 		final IScriptFolder folder = getScriptProject().findScriptFolder(
 				libraryPath);
 		assertNotNull(folder);
 		assertTrue(EnvironmentPathUtils.isFull(folder.getPath()));
 		assertEquals(libraryPath,
 				EnvironmentPathUtils.getLocalPath(folder.getPath()));
 	}
 
 	public void testFindScriptFolderWithEnv() throws ModelException {
 		final IPath libraryPath = EnvironmentPathUtils.getFullPath(
 				EnvironmentManager.getLocalEnvironment(),
 				new Path(library.getAbsolutePath()));
 		final IScriptFolder folder = getScriptProject().findScriptFolder(
 				libraryPath);
 		assertNotNull(folder);
 		assertTrue(EnvironmentPathUtils.isFull(folder.getPath()));
 		assertEquals(libraryPath, folder.getPath());
 	}
 
 	public void testMyLibraryExports() throws CoreException {
 		final List<org.eclipse.dltk.core.ISourceModule> modules = new ArrayList<org.eclipse.dltk.core.ISourceModule>();
 		getScriptProject().accept(new IModelElementVisitor() {
 			public boolean visit(IModelElement element) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					if (MY_EXPORTS_JS.equals(element.getElementName())) {
 						modules.add((ISourceModule) element);
 					}
 					return false;
 				}
 				return true;
 			}
 		});
 		assertEquals(1, modules.size());
 		final TestSearchResults results = search("myLibraryExports", METHOD,
 				DECLARATIONS, EXACT_RULE,
 				SearchEngine.createSearchScope(modules.get(0)));
 		assertEquals(1, results.size());
 		final IModelElement method = results.locate(IMethod.class,
 				"myLibraryExports");
 		final IModelElement parent = method.getParent();
 		assertEquals(IModelElement.SOURCE_MODULE, parent.getElementType());
 		assertEquals(MY_EXPORTS_JS, parent.getElementName());
 	}
 }

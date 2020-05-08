 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core.tests.search.mixin;
 
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.tests.model.AbstractDLTKSearchTests;
 import org.eclipse.dltk.ruby.core.RubyLanguageToolkit;
 import org.eclipse.dltk.ruby.core.tests.Activator;
 
 /**
  * Tests for mixin model. Checks operations with a source modules and buildpaths.
  * For model building tests see AutoMixinTests.
  * 
  * Warning! Tests results depends of test execution order cause tests modifies a project.
  * 
  * @author fourdman
  * 
  */
 public class MixinModelManipulationTests extends AbstractDLTKSearchTests implements
 		IDLTKSearchConstants {
 	private static final String PROJECT_NAME = "mixins";
 
 	public MixinModelManipulationTests(String name) {
 		super(Activator.PLUGIN_ID, name);
 	}
 
 	public static Suite suite() {
 		return new Suite(MixinModelManipulationTests.class);
 	}
 
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 		up();
 		waitUntilIndexesReady();
 		buildAll();
 	}
 
 	public void tearDownSuite() throws Exception {
 		deleteProject(PROJECT_NAME);
 		super.tearDownSuite();
 	}
 
 	private void up() throws Exception {
 		if (SCRIPT_PROJECT != null) {
 			deleteProject (SCRIPT_PROJECT.getElementName());
 		}
 //		if (SCRIPT_PROJECT == null) {
 			SCRIPT_PROJECT = setUpScriptProject(PROJECT_NAME);
 //		}
 	}
 	
 	private void buildAll() throws CoreException {
 		ResourcesPlugin.getWorkspace()
 				.build(IncrementalProjectBuilder.FULL_BUILD,
 						new NullProgressMonitor());
 		waitForAutoBuild();
 	}
 
 	public void testTotalKeysCount() {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 		String[] keys = model.findKeys("*");
		assertEquals(26, keys.length);
 	}
 	
 	// If fails, call ghostbusters, please
 	public void testForGhosts() {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 		String[] keys = model.findKeys("*ghost*");
 		assertEquals(0, keys.length);
 	}
 
 	public void testModuleDeletion() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 
 		IMixinElement mixinElement = model.get("Foo");
 		assertNotNull(mixinElement);
 		Object[] objs = mixinElement.getAllObjects();
 		assertNotNull(objs);
 		assertEquals(2, objs.length);
 
 		ISourceModule sourceModule = getSourceModule(PROJECT_NAME, "src",
 				"src2.rb");
 		sourceModule.delete(true, null);
 
 		buildAll();
 
 		Object objs2[] = mixinElement.getAllObjects();
 		assertEquals(1, objs2.length);
 	}
 
 	public void testFatalModuleDeletion() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 
 		IMixinElement mixinElement = model.get("D");
 		assertNotNull(mixinElement);
 		Object[] objs = mixinElement.getAllObjects();
 		assertNotNull(objs);
 		assertEquals(1, objs.length);
 
 		ISourceModule sourceModule = getSourceModule(PROJECT_NAME, "src",
 				"src1.rb");
 		sourceModule.delete(true, null);
 
 		buildAll();
 
 		mixinElement = model.get("D");
 
 		assertNull(mixinElement);
 	}
 
 	public void testExistentClassExtension() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 
 		IMixinElement mixinElement = model.get("Foo");
 		assertNotNull(mixinElement);
 		Object[] objs = mixinElement.getAllObjects();
		assertEquals(2, objs.length);
 
 		IScriptFolder scriptFolder = getScriptFolder(PROJECT_NAME, "src",
 				new Path(""));
 		String contents = "class Foo\n def bar\n end\n end\n";
 		scriptFolder.createSourceModule("MoreFoo.rb", contents, true, null);
 
 		buildAll();
 
 		mixinElement = model.get("Foo");
 		Object objs2[] = mixinElement.getAllObjects();
 		assertEquals(2, objs2.length);
 	}
 
 	public void testNewClassAddition() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 
 		IMixinElement mixinElement = model.get("Buzzy");
 		assertNull(mixinElement);
 
 		IScriptFolder scriptFolder = getScriptFolder(PROJECT_NAME, "src",
 				new Path(""));
 		String contents = "class Buzzy\n" +
 						  "    def myFyb\n " +
 						  "    end\n " +
 						  "end\n";
 		
 		scriptFolder.createSourceModule("Buzzy.rb", contents, true, null);
 
 		buildAll();
 
 		mixinElement = model.get("Buzzy");
 		assertNotNull(mixinElement);
 		Object objs2[] = mixinElement.getAllObjects();
 		assertEquals(1, objs2.length);
 	}
 
 	public void testBuildpathAddition() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 		IMixinElement mixinElement = model.get("Cat");
 		assertNull(mixinElement);
 
 		IBuildpathEntry[] rawBuildpath = SCRIPT_PROJECT.getRawBuildpath();
 		IBuildpathEntry[] newRawBuildpath = new IBuildpathEntry[rawBuildpath.length + 1];
 		System.arraycopy(rawBuildpath, 0, newRawBuildpath, 0,
 				rawBuildpath.length);
 		newRawBuildpath[rawBuildpath.length] = DLTKCore
 				.newSourceEntry(new Path("/mixins/src3"));
 		SCRIPT_PROJECT.setRawBuildpath(newRawBuildpath, null);
 
 		buildAll();
 
 		mixinElement = model.get("Cat");
 		assertNotNull(mixinElement);
 		Object[] allObjects = mixinElement.getAllObjects();
 		assertTrue (allObjects.length > 0);
 	}
 
 	public void testBuildpathDeletion() throws Exception {
 		MixinModel model = new MixinModel(RubyLanguageToolkit.getDefault());
 		IMixinElement mixinElement = model.get("Dragon");
 		assertNotNull(mixinElement);
 		Object[] objs = mixinElement.getAllObjects();
 		assertNotNull(objs);
 
 		IBuildpathEntry[] rawBuildpath = SCRIPT_PROJECT.getRawBuildpath();
 		IBuildpathEntry[] newRawBuildpath = new IBuildpathEntry[rawBuildpath.length - 1];
 		boolean found = false;
 		for (int i = 0, j = 0; i < rawBuildpath.length; i++) {
 			if (!rawBuildpath[i].getPath().toString().endsWith("src2")) {
 				newRawBuildpath[j++] = rawBuildpath[i];
 			} else {
 				found = true;
 			}
 		}
 		assertTrue("Buildpath should contain source folder src2", found);
 
 		SCRIPT_PROJECT.setRawBuildpath(newRawBuildpath, null);
 
 		buildAll();
 
 		mixinElement = model.get("Dragon");
 		assertNull(mixinElement);
 	}
 
 }

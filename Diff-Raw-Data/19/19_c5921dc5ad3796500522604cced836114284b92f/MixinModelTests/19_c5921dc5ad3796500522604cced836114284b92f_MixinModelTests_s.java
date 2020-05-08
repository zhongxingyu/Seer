 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.mixin;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.mixin.MixinModel;
 import org.eclipse.dltk.core.search.index.MixinIndex;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.core.tests.model.TestLanguageToolkit;
 
 /**
  * Tests for the {@link MixinIndex} class.
  */
 @SuppressWarnings("deprecation")
 public class MixinModelTests extends AbstractModelTests {
 
 	private IProject project;
 
 	public MixinModelTests(String name) {
 		super("org.eclipse.dltk.core.tests", name);
 	}
 
	protected void setUp() throws Exception {
		super.setUp();
		if (project == null) {
			project = setUpProject("Mixin0");
		}
 		waitUntilIndexesReady();
 	}
 
	protected void tearDown() throws Exception {
		super.tearDown();
		if (project != null) {
			deleteProject(project.getName());
		}
 	}
 
 	public void testMixin001() throws Throwable {
 		MixinModel model = new MixinModel(TestLanguageToolkit.getDefault());
 		try {
 			IMixinElement[] results = model.find("{foo");
 			TestCase.assertEquals(1, results.length);
 		} finally {
 			model.stop();
 		}
 	}
 
 	public void testMixin002() throws Throwable {
 		MixinModel model = new MixinModel(TestLanguageToolkit.getDefault());
 		try {
 			IMixinElement[] results = model.find("{foo*");
 			TestCase.assertEquals(3, results.length);
 		} finally {
 			model.stop();
 		}
 	}
 
 	public void testMixin003() throws Throwable {
 		MixinModel model = new MixinModel(TestLanguageToolkit.getDefault());
 		try {
 			IMixinElement[] results = model.find("{foo*a");
 			TestCase.assertEquals(1, results.length);
 		} finally {
 			model.stop();
 		}
 	}
 }

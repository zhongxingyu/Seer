 /*******************************************************************************
  * Copyright (C) 2013, 2014 Obeo and others
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.tests.egit;
 
 import java.io.File;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.egit.core.Activator;
 import org.eclipse.egit.core.GitCorePreferences;
 import org.eclipse.emf.compare.ide.ui.tests.CompareTestCase;
 import org.eclipse.emf.compare.ide.ui.tests.egit.fixture.GitTestRepository;
 import org.eclipse.emf.compare.ide.ui.tests.egit.fixture.MockSystemReader;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.util.FileUtils;
 import org.eclipse.jgit.util.SystemReader;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 
 /**
  * The set up and tear down of this class were mostly copied from org.eclipse.egit.core.test.GitTestCase.
  */
 @SuppressWarnings("restriction")
 public class CompareGitTestCase extends CompareTestCase {
 	protected GitTestRepository repository;
 
 	// The ".git" folder of the test repository
 	private File gitDir;
 
 	@BeforeClass
 	public static void setUpClass() {
 		// suppress auto-ignoring and auto-sharing to avoid interference
 		IEclipsePreferences eGitPreferences = InstanceScope.INSTANCE.getNode(Activator.getPluginId());
 		eGitPreferences.putBoolean(GitCorePreferences.core_autoIgnoreDerivedResources, false);
 		eGitPreferences.putBoolean(GitCorePreferences.core_autoShareProjects, false);
 	}
 
 	@Override
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		// ensure there are no shared Repository instances left
 		// when starting a new test
 		Activator.getDefault().getRepositoryCache().clear();
 		final MockSystemReader mockSystemReader = new MockSystemReader();
 		SystemReader.setInstance(mockSystemReader);
 		mockSystemReader.setProperty(Constants.GIT_CEILING_DIRECTORIES_KEY, ResourcesPlugin.getWorkspace()
				.getRoot().getLocation().toFile().getAbsoluteFile().toString());
 		gitDir = new File(project.getProject().getWorkspace().getRoot().getRawLocation().toFile(),
 				Constants.DOT_GIT);
 		repository = new GitTestRepository(gitDir);
 		repository.connect(project.getProject());
 	}
 
 	@Override
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 		repository.dispose();
 		Activator.getDefault().getRepositoryCache().clear();
 		if (gitDir.exists()) {
 			FileUtils.delete(gitDir, FileUtils.RECURSIVE | FileUtils.RETRY);
 		}
 	}
 }

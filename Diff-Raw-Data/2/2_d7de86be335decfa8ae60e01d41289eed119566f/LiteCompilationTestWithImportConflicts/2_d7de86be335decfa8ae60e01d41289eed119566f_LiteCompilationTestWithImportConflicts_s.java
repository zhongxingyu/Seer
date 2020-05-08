 /**
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.tests.lite.gen;
 
 import java.text.MessageFormat;
 import java.util.Collections;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.codegen.ecore.Generator;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.jdt.core.IBuffer;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaCore;
 
 public class LiteCompilationTestWithImportConflicts extends LiteCompilationTest {
 	public LiteCompilationTestWithImportConflicts(String name) {
 		super(name);
 	}
 
 	public void testPreexistingImportConflicts() throws Exception {
		DiaGenSource gmfGenSource = loadSource();
 		gmfGenSource.getGenDiagram().getEditorGen().setSameFileForDiagramAndModel(false);
 		String pluginId = gmfGenSource.getGenDiagram().getEditorGen().getPlugin().getID();
 		IProject diagramProject = ResourcesPlugin.getWorkspace().getRoot().getProject(pluginId);
 		if (!diagramProject.isAccessible()) {
 			//Initialize the plugin the same way it would be initialized if present.
 			Generator.createEMFProject(diagramProject.getFolder("src").getFullPath(), null, Collections.EMPTY_LIST, new NullProgressMonitor(), Generator.EMF_PLUGIN_PROJECT_STYLE);	//$NON-NLS-1$
 		}
 		IJavaProject javaProject = JavaCore.create(diagramProject);
 		assertTrue(javaProject.exists());
 		IPackageFragment pf = javaProject.getPackageFragmentRoot(diagramProject.getFolder("src")).createPackageFragment(gmfGenSource.getGenDiagram().getNotationViewFactoriesPackageName(), false, new NullProgressMonitor());	//$NON-NLS-1$
 		ICompilationUnit cu = pf.getCompilationUnit(gmfGenSource.getGenDiagram().getNotationViewFactoryClassName() + ".java");	//$NON-NLS-1$
 		String contents = createContents(gmfGenSource.getGenDiagram().getNotationViewFactoriesPackageName(), gmfGenSource.getGenDiagram().getNotationViewFactoryClassName(), "javax.swing.text.View");	//$NON-NLS-1$
 		if (cu.exists()) {
 			IBuffer buffer = cu.getBuffer();
 			buffer.setContents(contents);
 			buffer.save(new NullProgressMonitor(), true);
 		} else {
 			pf.createCompilationUnit(cu.getElementName(), contents, false, new NullProgressMonitor());
 		}
 		generateAndCompile(gmfGenSource);
 	}
 
 	private String createContents(String packageName, String className, String conflictingImport) {
 		return MessageFormat.format("package {0};\nimport {2};\n /**\n * @generated\n */\npublic class {1} '{ }'", packageName, className, conflictingImport);	//$NON-NLS-1$
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2009 - 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.tests;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
 import org.eclipse.swtbot.swt.finder.results.Result;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 import org.eclipse.virgo.ide.tests.util.VirgoIdeTestUtil;
 import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
 import org.eclipse.virgo.ide.ui.editors.ParManifestEditor;
 import org.eclipse.virgo.ide.ui.tests.util.VirgoIdeTestBot;
 
 /**
  * @author Leo Dos Santos
  */
 public class AbstractManifestUiTestCase extends SWTBotTestCase {
 
 	public VirgoIdeTestBot bot;
 
 	protected IProject createPredefinedProject(String projectName) throws CoreException, IOException {
 		return VirgoIdeTestUtil.createPredefinedProject(projectName, getBundleName());
 	}
 
 	protected String getBundleName() {
 		return "org.eclipse.virgo.ide.ui.tests";
 	}
 
 	protected BundleManifestEditor openBundleManifestFile(String path) throws CoreException, IOException {
 		IProject project = createPredefinedProject("SimpleBundleProject");
 		BundleManifestEditor manifest = (BundleManifestEditor) openManifestEditor(project, path);
 		return manifest;
 	}
 
 	private ManifestEditor openManifestEditor(IProject project, String path) {
 		final IFile file = project.getFile(path);
 		assertTrue(file.exists());
 
 		return UIThreadRunnable.syncExec(new Result<ManifestEditor>() {
 			public ManifestEditor run() {
 				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				assertNotNull("Expected active workbench window", window);
 				IWorkbenchPage page = window.getActivePage();
 				assertNotNull("Expected active workbench page", page);
 
 				try {
 					IEditorPart editor = IDE.openEditor(page, file);
 					if (editor instanceof ManifestEditor) {
 						return ((ManifestEditor) editor);
 					}
 				}
 				catch (PartInitException e) {
 					fail("Could not open a manifest editor.");
 				}
 				return null;
 			}
 		});
 	}
 
 	protected ParManifestEditor openParManifestFile(String path) throws CoreException, IOException {
 		IProject project = createPredefinedProject("SimpleParProject");
 		ParManifestEditor manifest = (ParManifestEditor) openManifestEditor(project, path);
 		return manifest;
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		bot = new VirgoIdeTestBot();
 		try {
 			bot.viewByTitle("Welcome").close();
 		}
 		catch (WidgetNotFoundException e) {
 			// ignore
 		}
 
 		// trigger artefact repo initialization
 		ServerCorePlugin.getArtefactRepositoryManager().getArtefactRepository();
 		while (!ServerCorePlugin.getArtefactRepositoryManager().isArtefactRepositoryInitialized()) {
 			Thread.sleep(500);
 		}
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		List<? extends SWTBotEditor> editors = bot.editors();
 		for (SWTBotEditor editor : editors) {
 			editor.close();
 		}
 		VirgoIdeTestUtil.deleteAllProjects();
 	}
 }

 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2012, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 388211 - New plug-in with Graphiti editor wizard adds not needed dependency to org.eclipse.ui
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.bot.tests;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.pde.core.plugin.IPluginImport;
 import org.eclipse.pde.core.plugin.IPluginModelBase;
 import org.eclipse.pde.core.plugin.PluginRegistry;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PluginWizardTests extends AbstractGFTests {
 
 	private IProject newProject;
 	private SWTBotPerspective previousPerspective;
 
 	public PluginWizardTests() {
 		super();
 	}
 
 	@Override
 	@Before
 	protected void setUp() throws Exception {
 		super.setUp();
 		previousPerspective = bot.activePerspective();
 	}
 
 	@Override
 	@After
 	protected void tearDown() throws Exception {
 		page.closeAllEditors();
 		if (newProject != null) {
 			newProject.delete(true, true, new NullProgressMonitor());
 		}
 		super.tearDown();
 		bot.perspectiveByLabel(previousPerspective.getLabel()).activate();
 
 		Thread.sleep(5000);
 	}
 
 	@Test
 	public void testPluginWizardRun() throws Exception {
 		// Start wizard
 		bot.menu("File").menu("New").menu("Project...").click();
 		SWTBotShell shell = bot.shell("New Project");
 		shell.activate();
 		bot.tree().expandNode("Plug-in Development").select("Plug-in Project");
 		bot.button("Next >").click();
 
 		// Provide name for new plugin project
 		bot.textWithLabel("Project name:").setText("org.eclipse.graphiti.test1");
 		bot.button("Next >").click();
 
 		// Deselect activator generation, test for Bug 388211
 		bot.checkBoxInGroup("Options", 0).deselect();
 		bot.button("Next >").click();
 
 		// Select Graphiti extension
 		bot.table().select("Plug-in with a Graphiti Editor");
 		bot.button("Next >").click();
 
 		// Check fields are filled
 		String diagramTypeId = bot.textWithLabelInGroup("ID", "Diagram Type").getText();
 		assertEquals("org.eclipse.graphiti.test1.test1DiagramType", diagramTypeId);
 
 		// Finish the wizard
 		bot.button("Finish").click();
 
 		// Confirm perspective switch
 		shell = bot.shell("Open Associated Perspective?");
 		shell.activate();
 		bot.button("Yes").click();
 
		Thread.sleep(1000);

 		newProject = ResourcesPlugin.getWorkspace().getRoot().getProject("org.eclipse.graphiti.test1");
 		assertTrue(newProject.exists());
 
 		// Check problems view for any errors
 		SWTBotView view = bot.viewByTitle("Problems");
 	    view.show();
 		SWTBotTree tree = view.bot().tree();
 		SWTBotTreeItem[] allItems = tree.getAllItems();
 		assertTrue("Items found in problems view: " + allItems.toString(), allItems.length == 0);
 
 		// Check that org.eclipse.ui is not part of the dependencies (no
 		// activator is generated), test for Bug 388211
 		IPluginModelBase newPluginModel = PluginRegistry.findModel(newProject);
 		IPluginImport[] imports = newPluginModel.getPluginBase().getImports();
 		for (IPluginImport pluginImport : imports) {
 			if ("org.eclipse.ui".equals(pluginImport.getId())) {
 				fail("'org.eclipse.ui' should not be part of the dependencies");
 			}
 		}
 	}
 }

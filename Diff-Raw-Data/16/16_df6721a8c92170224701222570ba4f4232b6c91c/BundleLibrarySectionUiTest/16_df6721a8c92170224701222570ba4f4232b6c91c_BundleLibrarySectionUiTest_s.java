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
 package org.eclipse.virgo.ide.ui.editors;
 
 import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
 import org.eclipse.virgo.ide.ui.tests.AbstractManifestUiTestCase;
 
 /**
  * @author Leo Dos Santos
  */
 public class BundleLibrarySectionUiTest extends AbstractManifestUiTestCase {
 
 	private static String SECTION_LABEL = "Classpath";
 
 	public void testNewButton() throws Exception {
 		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
 		UIThreadRunnable.syncExec(new VoidResult() {
 			public void run() {
 				manifest.setActivePage(BundleRuntimePage.PAGE_ID);
 			}
 		});
		bot.buttonInSection("New...", SECTION_LABEL).click();
 
 		SWTBotShell newDialog = bot.shell("New Library");
 		assertTrue(newDialog.isOpen());
 		newDialog.close();
 	}
 
 	public void testAddButton() throws Exception {
 		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
 		UIThreadRunnable.syncExec(new VoidResult() {
 			public void run() {
 				manifest.setActivePage(BundleRuntimePage.PAGE_ID);
 			}
 		});
		bot.buttonInSection("Add...", SECTION_LABEL).click();
 
 		SWTBotShell addDialog = bot.shell("JAR Selection");
 		assertTrue(addDialog.isOpen());
 		addDialog.close();
 	}
 
 	public void testRemoveButton() throws Exception {
 		final BundleManifestEditor manifest = openBundleManifestFile("src/META-INF/MANIFEST.MF");
 		UIThreadRunnable.syncExec(new VoidResult() {
 			public void run() {
 				manifest.setActivePage(BundleRuntimePage.PAGE_ID);
 			}
 		});
 
		SWTBotButton button = bot.buttonInSection("Remove", SECTION_LABEL);
 		assertFalse(button.isEnabled());
 
 		SWTBotTable table = bot.tableInSection(SECTION_LABEL);
 		int count = table.rowCount();
 		assertTrue(count > 0);
 
 		table.select(0);
 		assertTrue(button.isEnabled());
 
 		button.click();
 		assertEquals(count - 1, table.rowCount());
 	}
 
 }

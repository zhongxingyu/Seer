  /*******************************************************************************
   * Copyright (c) 2007-2009 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributor:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.hibernate.ui.bot.testcase;
 
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
 import org.jboss.tools.hibernate.ui.bot.testsuite.HibernateTest;
 import org.jboss.tools.hibernate.ui.bot.testsuite.Project;
import org.jboss.tools.ui.bot.ext.SWTEclipseExt;
 import org.jboss.tools.ui.bot.ext.gen.ActionItem;
 import org.jboss.tools.ui.bot.ext.types.EntityType;
 import org.jboss.tools.ui.bot.ext.types.IDELabel;
 import org.jboss.tools.ui.bot.ext.types.ViewType;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class ConsoleTest extends HibernateTest {
 
 	@BeforeClass	
 	public static void setUpTest() {
 		HibernateTest.prepareProject();
 	}
 	
 	@AfterClass
 	public static void tearDownTest() { 
 		HibernateTest.clean();
 		bot.sleep(TIME_5S);
 	}	
 	
 	private SWTBotShell mainShell;
 	private static boolean consoleCreated = false;
 	
 	/**
 	 * Create console TestCases TC03 - TC16
 	 */
 	@Test
 	public void createConsole() {
 		if (consoleCreated) return;
 		
 		log.info("HB Console creation STARTED");
 		
 		eclipse.showView(ViewType.PACKAGE_EXPLORER);
 		packageExplorer.selectProject(Project.PROJECT_NAME);
 		eclipse.createNew(EntityType.HIBERNATE_CONSOLE);		
 				
 		createMainTab();
 		mainShell.activate();
 		createOptionTab();
 		createClasspathTab();
 		createMappingsTab();
 		createCommonTab();
 		
 		bot.button(IDELabel.Button.FINISH).click();
 		util.waitForNonIgnoredJobs();
 		log.info("HB Console creation FINISHED");
 		
 		consoleCreated = true;
 	}
 	
 	/**
 	 * TC 03
 	 */
 	private void createMainTab() {
 		bot.cTabItem(IDELabel.HBConsoleWizard.MAIN_TAB).activate();
 		bot.textWithLabelInGroup("",IDELabel.HBConsoleWizard.PROJECT_GROUP ).setText(Project.PROJECT_NAME);
 		mainShell =  bot.activeShell();
 			
 		// Create new configuration file
 		bot.buttonInGroup(IDELabel.HBConsoleWizard.SETUP_BUTTON,IDELabel.HBConsoleWizard.CONFIGURATION_FILE_GROUP).click();
 		bot.button(IDELabel.HBConsoleWizard.CREATE_NEW_BUTTON).click();
 		eclipse.selectTreeLocation(Project.PROJECT_NAME, "src");
 		bot.button(IDELabel.Button.NEXT).click();
 		bot.comboBoxWithLabel(IDELabel.HBConsoleWizard.DATABASE_DIALECT).setSelection(Project.DB_DIALECT);
 		bot.comboBoxWithLabel(IDELabel.HBConsoleWizard.DRIVER_CLASS).setSelection(Project.DRIVER_CLASS);
 		bot.comboBoxWithLabel(IDELabel.HBConsoleWizard.CONNECTION_URL).setText(Project.JDBC_STRING);
 		
 		SWTBotShell shell = bot.activeShell();
 		bot.button(IDELabel.Button.FINISH).click();
 		eclipse.waitForClosedShell(shell);
 			
 		log.info("HB Console Main tab DONE");		
 		bot.sleep(TIME_1S);
 	}
 
 	/**
 	 * TC 04
 	 */
 	private void createOptionTab() {
 		mainShell.activate();
 		bot.cTabItem(IDELabel.HBConsoleWizard.OPTIONS_TAB).activate();
 				
 
 		bot.comboBoxWithLabelInGroup("", IDELabel.HBConsoleWizard.DATABASE_DIALECT).setSelection(Project.DB_DIALECT);
 		log.info("HB Console Option tab DONE");
 		bot.sleep(TIME_1S);
 	}
 
 	/**
 	 * TC 05
 	 */
 	private void createClasspathTab() {
 		mainShell.activate();
 		bot.cTabItem(IDELabel.HBConsoleWizard.CLASSPATH_TAB).activate();
 		log.info("HB Console ClassPath tab DONE");
 		bot.sleep(TIME_1S);
 	}
 
 	/**
 	 * TC 06
 	 */
 	private void createMappingsTab() {
 		mainShell.activate();
 		bot.cTabItem(IDELabel.HBConsoleWizard.MAPPINGS_TAB).activate();
 		log.info("HB Console Mappings tab DONE");
 		bot.sleep(TIME_1S);
 	}
 
 	/**
 	 * TC 07
 	 */
 	private void createCommonTab() {
 		mainShell.activate();
 		bot.cTabItem(IDELabel.HBConsoleWizard.COMMON_TAB).activate();
 		log.info("HB Console Common tab DONE");
 		bot.sleep(TIME_1S);
 	}
 
 	/**
 	 * TC 16 - open console, change serveral value, apply changes and check changes if they were store correctly
 	 */
 	@Test	
 	public void editConsole() {
 		// prereq
 		createConsole();
 		
 		// open console
 		openConsoleConfiguration();		
 		editConsoleValues();
 		
 		// open console again
 		openConsoleConfiguration();
 		checkConsoleValues();		
 	}
 	
 	private void editConsoleValues() {
 		// perform change on
 		// - Main page
 		bot.cTabItem(IDELabel.HBConsoleWizard.MAIN_TAB).activate();
 		bot.radioInGroup("Type:",1).click();
 		bot.sleep(TIME_1S);
 		// - Option
 		bot.cTabItem(IDELabel.HBConsoleWizard.OPTIONS_TAB).activate();
 		bot.comboBoxInGroup("Database dialect:").setSelection("MySQL");
 		bot.sleep(TIME_1S);
 		// - Classpath
 		bot.cTabItem(IDELabel.HBConsoleWizard.CLASSPATH_TAB).activate();		
 		bot.sleep(TIME_1S);
 		// - Mapping
 		bot.cTabItem(IDELabel.HBConsoleWizard.MAPPINGS_TAB).activate();
 		bot.sleep(TIME_1S);
 		// - Common
 		bot.cTabItem(IDELabel.HBConsoleWizard.COMMON_TAB).activate();
 		bot.sleep(TIME_1S);		
 		// apply
 		bot.clickButton(IDELabel.Button.APPLY);
 	}
 	
 	private void checkConsoleValues() {
 		// perform change on
 		// - Main page
 		bot.cTabItem(IDELabel.HBConsoleWizard.MAIN_TAB).activate();
 		assertTrue(bot.radioInGroup("Type:",1).isSelected());	
 		// - Option
 		bot.cTabItem(IDELabel.HBConsoleWizard.OPTIONS_TAB).activate();
 		assertEquals("MySQL", bot.comboBoxInGroup("Database dialect:").getText());
 		bot.clickButton(IDELabel.Button.OK);
 		
 		log.info("Changed console value checked");
 	}
 	
 	private void openConsoleConfiguration() {
 		SWTBot viewBot = open.viewOpen(ActionItem.View.HibernateHibernateConfigurations.LABEL).bot();
		SWTBotTreeItem item = SWTEclipseExt.selectTreeLocation(viewBot, Project.PROJECT_NAME);
 		item.doubleClick();		
 	}
 }

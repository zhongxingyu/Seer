 package org.bonitasoft.studio.diagram.test;
 
 import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
 
 import org.bonitasoft.studio.common.jface.FileActionDialog;
 import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
 import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
 import org.hamcrest.Matcher;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class TestSave extends SWTBotGefTestCase {
 
 
     // Before and After
     private static boolean disablePopup;
 
     @BeforeClass
     public static void setUpBeforeClass() {
         disablePopup = FileActionDialog.getDisablePopup();
         FileActionDialog.setDisablePopup(true);
     }
 
 
     @AfterClass
     public static void tearDownAfterClass() {
 
         FileActionDialog.setDisablePopup(disablePopup);
     }
 
     @Override
     @After
     public void tearDown(){
         bot.saveAllEditors();
     }
 
     final static String SAVE_BUTTON_TEXT="Save";
     final static String WELCOME_EDITOR_TEXT="Welcome to Bonita Studio";
 
 
     @Test
     public void testSaveButtonAndMenuNotEnableAtInitiationAndEnableAfterChange(){
 
         boolean isSaveButtonEnable= true;
 
         // When opening Bonita soft
         Matcher<MenuItem> matcher = withMnemonic(SAVE_BUTTON_TEXT);
         bot.waitUntil(Conditions.waitForMenu(bot.activeShell(), matcher));
 
         // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
         Assert.assertFalse("Error: Save button must be disabled when opening Bonita Studio.", isSaveButtonEnable);
 
         // test menu	
        Assert.assertFalse("Error: Save menu must be disabled when opening Bonita Studio.",  isSaveMenuEnabled());
 
         // When Creating a new Diagram
         SWTBotTestUtil.createNewDiagram(bot);
 
         // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
        Assert.assertTrue("Error: Save button must be enabled when creating a new diagram.", isSaveButtonEnable);
 
         // test menu
        Assert.assertTrue("Error: Save menu must be enabled when creating a new diagram.",  isSaveMenuEnabled());
 
         bot.saveAllEditors();
 
         // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
         Assert.assertFalse("Error: Save button must be disabled when creating a new diagram.", isSaveButtonEnable);
 
         // test menu
        Assert.assertFalse("Error: Save menu must be disabled when creating a new diagram.",  isSaveMenuEnabled());
 
     }
 

	private boolean isSaveMenuEnabled() {
		return bot.menu("Diagram").click().menu("Save").isEnabled();
	}

     @Test
    public void testSaveButtonAndMenuEnableWhenNewDiagram(){
 
         boolean isSaveButtonEnable= false;
 
         SWTBotTestUtil.createNewDiagram(bot);
         SWTBotTestUtil.changeDiagramName(bot, "Step1", "testSave2");
 
         // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
         Assert.assertFalse("Error: Save button must not be enabled when changing diagram name.", isSaveButtonEnable);
 
         // test menu
         Assert.assertFalse("Error: Save menu must not be enabled when changing diagram name.",  bot.menu("Diagram").menu("Save").isEnabled());
 
         SWTBotTestUtil.createNewDiagram(bot);
 
         // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
        Assert.assertTrue("Error: Save button must enabled for the new editor of a new Dragram created.", isSaveButtonEnable);
 
         // test menu
        Assert.assertTrue("Error: Save menu must be enabled for the new editor of a new Dragram created.",  isSaveMenuEnabled());
 
         bot.saveAllEditors();
         
        // test button
         isSaveButtonEnable=  bot.toolbarButton(SAVE_BUTTON_TEXT).isEnabled();
         Assert.assertFalse("Error: Save button must be disabled when creating a new diagram.", isSaveButtonEnable);
 
         // test menu
        Assert.assertFalse("Error: Save menu must be disabled when creating a new diagram.",  isSaveMenuEnabled());
     }
 
 }

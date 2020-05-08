 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.actioneditor;
 
 import com.dmdirc.Main;
 import com.dmdirc.actions.Action;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.InvalidIdentityFileException;
 import com.dmdirc.harness.ui.ClassFinder;
 import com.dmdirc.harness.ui.JRadioButtonByTextMatcher;
 import com.dmdirc.addons.ui_swing.components.ImageButton;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 
 import java.awt.Component;
 import java.awt.Dialog;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JTextField;
 
 import org.fest.swing.core.matcher.JButtonMatcher;
 import org.fest.swing.core.matcher.JLabelMatcher;
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.DialogFixture;
 import org.fest.swing.fixture.JLabelFixture;
 import org.fest.swing.fixture.JPanelFixture;
 import org.fest.swing.junit.testcase.FestSwingJUnitTestCase;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ActionEditorDialogTest extends FestSwingJUnitTestCase {
 
     private DialogFixture window;
 
     @BeforeClass
     public static void setUpClass() throws InvalidIdentityFileException {
         IdentityManager.load();
         Main.setUI(new SwingController());
         ActionManager.init();
         ActionManager.loadActions();
     }
 
     @Before
     @Override
     public void onSetUp() {
         if (!ActionManager.getGroups().containsKey("amd-ui-test1")) {
             ActionManager.makeGroup("amd-ui-test1");
         }
     }
 
     @After
     @Override
     public void onTearDown() {
         if (window != null) {
             window.cleanUp();
         }
 
         if (ActionManager.getGroups().containsKey("amd-ui-test1")) {
             ActionManager.removeGroup("amd-ui-test1");
         }
     }
 
     @Test
     public void testName() {
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().requireEnabled().requireEditable().requireEmpty();
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
     }
 
     @Test
     public void testIssue1785() {
         // Invalidating+validating name allows enables OK button despite invalid conditions
         // 'Fix' was disabling the add trigger button when name was invalid
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().requireEnabled().requireEditable().requireEmpty();
         window.panel(new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null)).
                 button(JButtonMatcher.withText("Add")).requireDisabled();
     }
 
     @Test
     public void testTriggerWithNoArgs() {
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
 
         final JPanelFixture triggers = window.panel(
                 new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null));
 
         triggers.comboBox().selectItem("Client closed");
         robot().waitForIdle();
         
         triggers.button(JButtonMatcher.withText("Add")).requireEnabled().
                 click();
         robot().waitForIdle();
 
         window.panel(new ClassFinder<ActionConditionsPanel>(ActionConditionsPanel.class, null)).
                 button(JButtonMatcher.withText("Add")).requireDisabled();
     }
 
     @Test
     public void testBasicTriggers() {
         setupWindow(null);
 
         final JPanelFixture triggers = window.panel(
                 new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null));
         triggers.comboBox().requireDisabled();
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
 
         final int items = triggers.comboBox().target.getItemCount();
         triggers.comboBox().requireEnabled().selectItem("Channel message received");
         robot().waitForIdle();
 
         triggers.button(JButtonMatcher.withText("Add")).requireEnabled().click();
         robot().waitForIdle();
 
         final JLabelFixture label =
                 triggers.label(JLabelMatcher.withText("Channel message received"));
         label.requireVisible();
 
         assertTrue(items > triggers.comboBox().target.getItemCount());
         window.button(JButtonMatcher.withText("OK")).requireEnabled();
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().deleteText();
         robot().waitForIdle();
 
         triggers.button(new ClassFinder<ImageButton>(ImageButton.class, null)).
                 requireDisabled();
         triggers.comboBox().requireDisabled();
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
 
         triggers.button(new ClassFinder<ImageButton>(ImageButton.class, null)).
                 requireEnabled().click();
         robot().waitForIdle();
 
         for (Component comp : triggers.panel(new ClassFinder<ActionTriggersListPanel>(ActionTriggersListPanel.class,
                 null)).target.getComponents()) {
             assertNotSame(label.target, comp);
         }
 
         assertEquals(items, triggers.comboBox().target.getItemCount());
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
     }
 
     @Test
     public void testBasicConditionTrees() {
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
 
         final JPanelFixture triggers = window.panel(
                 new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null));
 
         triggers.comboBox().selectItem("Channel message received");
         robot().waitForIdle();
         triggers.button(JButtonMatcher.withText("Add")).requireEnabled().
                 click();
         robot().waitForIdle();
 
         window.radioButton(new JRadioButtonByTextMatcher("All of the conditions are true")).
                 requireEnabled().requireSelected();
         window.radioButton(new JRadioButtonByTextMatcher("At least one of the conditions is true")).
                 requireEnabled();
         window.radioButton(new JRadioButtonByTextMatcher("The conditions match a custom rule")).
                 requireEnabled();
         window.panel(new ClassFinder<ActionConditionsTreePanel>(ActionConditionsTreePanel.class,
                 null)).textBox(new ClassFinder<JTextField>(JTextField.class,
                 null)).requireDisabled();
 
         window.button(JButtonMatcher.withText("OK")).requireEnabled();
 
         window.radioButton(new JRadioButtonByTextMatcher("The conditions match a custom rule")).
                 click().requireSelected();
         robot().waitForIdle();
         window.panel(new ClassFinder<ActionConditionsTreePanel>(ActionConditionsTreePanel.class,
                 null)).textBox(new ClassFinder<JTextField>(JTextField.class,
                 null)).requireEnabled().enterText("invalid");
         robot().waitForIdle();
 
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
     }
 
     @Test
     public void testConditionText() {        
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
         final JPanelFixture triggers = window.panel(
                 new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null));
 
         triggers.comboBox().selectItem("Channel message received");
         robot().waitForIdle();
         triggers.button(JButtonMatcher.withText("Add")).requireEnabled().
                 click();
         robot().waitForIdle();
 
         window.panel(new ClassFinder<ActionConditionsPanel>(ActionConditionsPanel.class, null)).
                 button(JButtonMatcher.withText("Add")).requireEnabled().
                 click();
         robot().waitForIdle();
         
         Pattern pattern = Pattern.compile(".+<body>(.+)</body>.+", Pattern.DOTALL);
         
         Matcher matcher = pattern.matcher(window.panel(new ClassFinder<ActionConditionDisplayPanel>(ActionConditionDisplayPanel.class,
                 null)).textBox(new ClassFinder<TextLabel>(TextLabel.class,
                 null)).target.getText());
         matcher.find();
        assertEquals("The ...", matcher.group(1).trim());
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("argument").selectItem("message");
         robot().waitForIdle();
         
         matcher = pattern.matcher(window.panel(new ClassFinder<ActionConditionDisplayPanel>(ActionConditionDisplayPanel.class,
                 null)).textBox(new ClassFinder<TextLabel>(TextLabel.class,
                 null)).target.getText());
         matcher.find();
         assertEquals("The message's ...", matcher.group(1).trim());
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("component").selectItem("content");
         robot().waitForIdle();
         
         matcher = pattern.matcher(window.panel(new ClassFinder<ActionConditionDisplayPanel>(ActionConditionDisplayPanel.class,
                 null)).textBox(new ClassFinder<TextLabel>(TextLabel.class,
                 null)).target.getText());
         matcher.find();
         assertEquals("The message's content ...", matcher.group(1).trim());
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("comparison").selectItem("contains");
         robot().waitForIdle();
         
         matcher = pattern.matcher(window.panel(new ClassFinder<ActionConditionDisplayPanel>(ActionConditionDisplayPanel.class,
                 null)).textBox(new ClassFinder<TextLabel>(TextLabel.class,
                 null)).target.getText());
         matcher.find();
         assertEquals("The message's content contains ''", matcher.group(1).trim());
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).textBox().enterText("foo");
         robot().waitForIdle();
         
         matcher = pattern.matcher(window.panel(new ClassFinder<ActionConditionDisplayPanel>(ActionConditionDisplayPanel.class,
                 null)).textBox(new ClassFinder<TextLabel>(TextLabel.class,
                 null)).target.getText());
         matcher.find();
         assertEquals("The message's content contains 'foo'", matcher.group(1).trim());
     }
 
     @Test
     public void testIllegalCondition() {
         setupWindow(null);
 
         window.panel(new ClassFinder<ActionNamePanel>(ActionNamePanel.class, null)).
                 textBox().enterText("test1");
         robot().waitForIdle();
         final JPanelFixture triggers = window.panel(
                 new ClassFinder<ActionTriggersPanel>(ActionTriggersPanel.class, null));
 
         triggers.comboBox().selectItem("Channel message received");
         robot().waitForIdle();
         triggers.button(JButtonMatcher.withText("Add")).requireEnabled().click();
         robot().waitForIdle();
 
         window.button(JButtonMatcher.withText("OK")).requireEnabled();
 
         window.panel(new ClassFinder<ActionConditionsPanel>(ActionConditionsPanel.class, null)).
                 button(JButtonMatcher.withText("Add")).requireEnabled().click();
         robot().waitForIdle();
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("argument").requireEnabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("component").requireDisabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("comparison").requireDisabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).textBox().requireDisabled();
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("argument").selectItem("message");
         robot().waitForIdle();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("component").requireEnabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("comparison").requireDisabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).textBox().requireDisabled();
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("component").selectItem("content");
         robot().waitForIdle();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("comparison").requireEnabled();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).textBox().requireDisabled();
         window.button(JButtonMatcher.withText("OK")).requireDisabled();
 
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).comboBox("comparison").selectItem("contains");
         robot().waitForIdle();
         window.panel(new ClassFinder<ActionConditionEditorPanel>(ActionConditionEditorPanel.class,
                 null)).textBox().requireEnabled();
         window.button(JButtonMatcher.withText("OK")).requireEnabled();
     }
 
     protected void setupWindow(final Action action) {
         final Dialog d = GuiActionRunner.execute(new GuiQuery<Dialog>() {
             @Override
             protected Dialog executeInEDT() throws Throwable {
                 return ActionEditorDialog.getActionEditorDialog(null,
                         "amd-ui-test1", action);
             }
         });
         robot().waitForIdle();
 
         window = new DialogFixture(robot(), d);
         window.show();
     }
 
 }

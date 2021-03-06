 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.ui.swing.components;
 
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.commandparser.parsers.GlobalCommandParser;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.harness.TestConfigManagerMap;
 import com.dmdirc.harness.TestWritableFrameContainer;
 import com.dmdirc.harness.ui.ClassFinder;
 import com.dmdirc.harness.ui.UIClassTestRunner;
 import com.dmdirc.harness.ui.UITestIface;
 
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.ui.swing.CustomInputFrame;
 import com.dmdirc.ui.swing.SwingController;
 import com.dmdirc.ui.swing.UIUtilities;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.KeyEvent;
 import javax.swing.text.JTextComponent;
 import org.fest.swing.core.EventMode;
 import org.fest.swing.core.KeyPressInfo;
 import org.fest.swing.core.matcher.DialogByTitleMatcher;
 import org.fest.swing.core.matcher.JButtonByTextMatcher;
 import org.fest.swing.fixture.DialogFixture;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JInternalFrameFixture;
 import org.fest.swing.util.Platform;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.junit.Assert.*;
 
 @RunWith(UIClassTestRunner.class)
 public class InputTextFrameTest implements UITestIface {
 
     static FrameFixture mainframe;
     static JInternalFrameFixture window;
     static TestConfigManagerMap cmmap;
     static TestWritableFrameContainer owner;
 
     @Before
     public void setUp() {
         IdentityManager.load();
 
         cmmap = new TestConfigManagerMap();
         cmmap.settings.put("ui.pasteProtectionLimit", "1");
 
         if (window == null) {
             owner = new TestWritableFrameContainer(512, cmmap);
 
             setupWindow(cmmap);
         }
     }
 
     @After
     public void tearDown() {
         // ??
     }
 
     @Test
     public void testPasteDialogContents() throws InterruptedException {
         ((InputTextFrame) window.target).doPaste("line1\nline2");
 
         final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                 .withTitleAndShowing("Multi-line paste"));
 
         dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
         dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                 .requireText("line1\nline2");
         dlg.close();

        int i = 0;
        while (dlg.target.isVisible() && ++i < 100) {
            Thread.sleep(100);
        }
     }
 
     @Test
     public void testPasteDialogWithTextBefore() throws InterruptedException {
         window.textBox().enterText("testing:");
         ((InputTextFrame) window.target).doPaste("line1\nline2");
 
         final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                 .withTitleAndShowing("Multi-line paste"));
 
         dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
         dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                 .requireText("testing:line1\nline2");
         dlg.close();

        int i = 0;
        while (dlg.target.isVisible() && ++i < 100) {
            Thread.sleep(100);
        }
     }
 
     @Test
     public void testPasteDialogWithTextAfter() throws InterruptedException {
         window.textBox().enterText("<- testing").pressAndReleaseKey(
                 KeyPressInfo.keyCode(KeyEvent.VK_HOME));
         ((InputTextFrame) window.target).doPaste("line1\nline2");
 
         final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                 .withTitleAndShowing("Multi-line paste"));
 
         dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
         dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                 .requireText("line1\nline2<- testing");
         dlg.close();

        int i = 0;
        while (dlg.target.isVisible() && ++i < 100) {
            Thread.sleep(100);
        }
     }
 
     @Test
     public void testPasteDialogWithTextAround() throws InterruptedException {
         window.textBox().enterText("testing:<- testing").selectText(8, 8);
         ((InputTextFrame) window.target).doPaste("line1\nline2");
 
         final DialogFixture dlg = mainframe.dialog(DialogByTitleMatcher
                 .withTitleAndShowing("Multi-line paste"));
 
         dlg.requireVisible().button(JButtonByTextMatcher.withText("Edit")).click();
         dlg.textBox(new ClassFinder<TextAreaInputField>(TextAreaInputField.class, null))
                 .requireText("testing:line1\nline2<- testing");
         dlg.close();

        int i = 0;
        while (dlg.target.isVisible() && ++i < 100) {
            Thread.sleep(100);
        }
     }
 
     public static junit.framework.Test suite() {
         return new junit.framework.JUnit4TestAdapter(InputTextFrameTest.class);
     }
 
     protected void setupWindow(final ConfigManager configManager) {
         UIUtilities.initUISettings();
 
         mainframe = new FrameFixture(SwingController.getMainFrame());
         mainframe.robot.settings().eventMode(EventMode.AWT);
 
         final CustomInputFrame titf = new CustomInputFrame(owner,
                 GlobalCommandParser.getGlobalCommandParser());
 
         titf.setTitle("testing123");
 
         owner.window = titf;
 
         WindowManager.addWindow(titf);
 
         titf.open();
 
         window = new JInternalFrameFixture(mainframe.robot, titf);
     }
 
 }

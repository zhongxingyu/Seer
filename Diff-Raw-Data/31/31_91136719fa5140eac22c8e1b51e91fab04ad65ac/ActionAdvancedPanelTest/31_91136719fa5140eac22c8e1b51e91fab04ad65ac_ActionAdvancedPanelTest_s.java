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
 
 import com.dmdirc.harness.ui.DMDircUITestCase;
 import org.junit.Test;
 import org.uispec4j.Panel;
 
 public class ActionAdvancedPanelTest extends DMDircUITestCase {
 
     @Test
     public void testActionEnabled() {
         final ActionAdvancedPanel panel = new ActionAdvancedPanel();
         final Panel test = new Panel(panel);
 
        test.getCheckBox().isEnabled().check();
        assertTrue(test.getCheckBox().isSelected());
         assertTrue(panel.isActionEnabled());
        test.getCheckBox().click();
        assertFalse(test.getCheckBox().isSelected());
         assertFalse(panel.isActionEnabled());
 
         panel.setActionEnabled(false);
        assertFalse(test.getCheckBox().isSelected());
         panel.setActionEnabled(true);
        assertTrue(test.getCheckBox().isSelected());
     }
 
     @Test
     public void testConcurrencyGroup() {
         final ActionAdvancedPanel panel = new ActionAdvancedPanel();
         final Panel test = new Panel(panel);
 
         test.getInputTextBox().textIsEmpty().check();
         assertEquals("", panel.getConcurrencyGroup());
         test.getInputTextBox().setText("testing");
         assertEquals("testing", panel.getConcurrencyGroup());
 
         panel.setConcurrencyGroup("");
         assertEquals("", test.getInputTextBox().getText());
         panel.setConcurrencyGroup("testing");
         assertEquals("testing", test.getInputTextBox().getText());
     }
 }

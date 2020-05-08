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
 
 package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;
 
 import com.dmdirc.actions.ActionGroup;
 import org.junit.Test;
 import org.uispec4j.Panel;
 import org.uispec4j.UISpecTestCase;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 public class ActionGroupInformationPanelTest extends UISpecTestCase {
 
     @Test
     public void testShouldDisplayNull() {
         assertFalse(new ActionGroupInformationPanel(null).shouldDisplay());
     }
 
     @Test
     public void testShouldDisplayNoDescription() {
         assertFalse(new ActionGroupInformationPanel(mock(ActionGroup.class)).shouldDisplay());
     }
 
     @Test
     public void testShouldDisplay() {
         final ActionGroup group = mock(ActionGroup.class);
         when(group.getDescription()).thenReturn("description");
 
         assertTrue(new ActionGroupInformationPanel(group).shouldDisplay());
     }
 
     @Test
     public void testAllLabels() {
         final ActionGroup group = mock(ActionGroup.class);
         when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(17);
         when(group.getAuthor()).thenReturn("foo <bar@baz>");
         
         final Panel panel = new Panel(new ActionGroupInformationPanel(group));
 
         assertEquals("<panel>"
                 + "<textBox text=\"description\"/>"
                 + "<textBox text=\"Author: \"/>"
                 + "<textBox text=\"foo &lt;bar@baz&gt;\"/>"
                 + "<textBox text=\"Version: \"/>"
                 + "<textBox text=\"17\"/>"
                 + "</panel>", panel.getDescription().replace("\n", ""));
     }
 
     @Test
     public void testNoAuthor() {
         final ActionGroup group = mock(ActionGroup.class);
         when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(17);
 
         final Panel panel = new Panel(new ActionGroupInformationPanel(group));
 
         assertEquals("<panel>"
                 + "<textBox text=\"description\"/>"
                 + "<textBox text=\"Version: \"/>"
                 + "<textBox text=\"17\"/>"
                 + "</panel>", panel.getDescription().replace("\n", ""));
     }
 
     @Test
     public void testNoVersion() {
         final ActionGroup group = mock(ActionGroup.class);
         when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(-1);
         when(group.getAuthor()).thenReturn("foo <bar@baz>");
 
         final Panel panel = new Panel(new ActionGroupInformationPanel(group));
 
         assertEquals("<panel>"
                 + "<textBox text=\"description\"/>"
                 + "<textBox text=\"Author: \"/>"
                 + "<textBox text=\"foo &lt;bar@baz&gt;\"/>"
                 + "</panel>", panel.getDescription().replace("\n", ""));
     }
 
 }

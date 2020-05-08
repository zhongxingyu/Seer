 /*
  * ActionGroupTest.java
  * Copyright (C) 2005 by:
  *
  *----------------------------
  * cismet GmbH
  * Goebenstrasse 40
  * 66117 Saarbruecken
  * http://www.cismet.de
  *----------------------------
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *----------------------------
  * Author:
  * thorsten.hell@cismet.de
  *----------------------------
  *
  * Created on 22. Februar 2006, 14:16
  *
  */
 
 package de.cismet.tools.gui.actiongroup;
 
 
     import java.awt.event.ActionEvent;
     
     import javax.swing.*;
     
     public class ActionGroupTest {
         
         /**
          * @param args
          */
         public static void main(String[] args) {
            Action a = new TestActionA("Test A");
            Action b = new TestActionB("Test B");
            Action c = new TestActionC("Test C");
             ActionGroup group = new ActionGroup();
             group.add(a);
             group.add(b);
             group.add(c);
             
             JFrame frame = new JFrame();
             JMenuBar menubar  = new JMenuBar();
            JMenu menu = new JMenu("Test");
             menu.add(ComponentFactory.getRadioMenuItem(a));
             menu.add(ComponentFactory.getRadioMenuItem(b));
             menu.add(ComponentFactory.getRadioMenuItem(c));
             menubar.add(menu);
             frame.setJMenuBar(menubar);
             
             JToolBar toolbar = new JToolBar();
             toolbar.add(ComponentFactory.getToggleButton(a));
             toolbar.add(ComponentFactory.getToggleButton(b));
             toolbar.add(ComponentFactory.getToggleButton(c));
             frame.getContentPane().add(toolbar);
             
             frame.pack();
             frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
             frame.setVisible(true);
         }
         
         private static class TestActionA extends AbstractAction {
             public TestActionA(String name) {
                 super(name);
             }
             
             public void actionPerformed(ActionEvent arg0) {
                System.out.println("Test Action A was just turned on.");
             }
         }
         
         private static class TestActionB extends AbstractAction {
             public TestActionB(String name) {
                 super(name);
             }
             
             public void actionPerformed(ActionEvent arg0) {
                System.out.println("Test Action B was just turned on.");
             }
         }
         private static class TestActionC extends AbstractAction {
             public TestActionC(String name) {
                 super(name);
             }
             
             public void actionPerformed(ActionEvent arg0) {
                System.out.println("Test Action C was just turned on.");
             }
         }
         
        
     }

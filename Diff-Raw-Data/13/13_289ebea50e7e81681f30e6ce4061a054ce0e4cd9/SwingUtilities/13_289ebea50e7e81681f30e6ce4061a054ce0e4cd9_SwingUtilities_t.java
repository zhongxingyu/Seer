 /*
  * Copyright 2011 StackFrame, LLC
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.stackframe.swing;
 
 import java.awt.Component;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import javax.swing.JComponent;
 import javax.swing.JRootPane;
 import javax.swing.KeyStroke;
 import javax.swing.RootPaneContainer;
 
 /**
 * Utilities that make dealing with Swing easier.
  *
 * @author Gene McCulley
  */
 public class SwingUtilities {
 
     private SwingUtilities() {
         // Inhibit construction as this is a utility class.
     }
 
     /**
      * Install a key listener on a window to close it. (e.g., Control-W on Windows, Command-W on Mac).
      *
      * @param c
      */
     public static void installCloseKey(final RootPaneContainer c) {
         ActionListener closer = new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 ((Component) c).setVisible(false);
             }
         };
         JRootPane rootPane = c.getRootPane();
         Toolkit toolkit = ((Component) c).getToolkit();
         rootPane.registerKeyboardAction(closer, KeyStroke.getKeyStroke(KeyEvent.VK_W, toolkit.getMenuShortcutKeyMask()), JComponent.WHEN_IN_FOCUSED_WINDOW);
     }
 }

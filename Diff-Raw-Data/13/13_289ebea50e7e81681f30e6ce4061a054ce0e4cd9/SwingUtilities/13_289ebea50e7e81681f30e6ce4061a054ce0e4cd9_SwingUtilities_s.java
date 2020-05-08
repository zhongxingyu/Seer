 /*
  * Copyright 2011 StackFrame, LLC
 * All rights reserved.
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
  *
 * @author mcculley
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

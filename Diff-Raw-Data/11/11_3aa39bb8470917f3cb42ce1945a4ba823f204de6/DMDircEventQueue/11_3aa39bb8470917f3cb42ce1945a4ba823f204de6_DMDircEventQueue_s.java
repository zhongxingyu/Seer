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
 
 package com.dmdirc.addons.ui_swing;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.addons.ui_swing.actions.CopyAction;
 import com.dmdirc.addons.ui_swing.actions.CutAction;
 import com.dmdirc.addons.ui_swing.actions.PasteAction;
 
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;

 import java.awt.event.WindowEvent;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.MenuSelectionManager;
 import javax.swing.SwingUtilities;
 import javax.swing.text.JTextComponent;
 
 /**
  * Custom event queue to add common functionality to certain components.
  */
 public class DMDircEventQueue extends EventQueue {
 
     /** Swing Controller. */
     private final SwingController controller;
 
     /**
      * Instantiates the DMDircEventQueue.
      *
      * @param controller Swing controller
      */
     public DMDircEventQueue(final SwingController controller) {
         super();
 
         this.controller = controller;
     }
 
     /** {@inheritDoc} */
     @Override
     protected void dispatchEvent(final AWTEvent event) {
         if (event instanceof MouseEvent) {
             handleMouseEvent((MouseEvent) event);
         } else if (event instanceof KeyEvent) {
             handleKeyEvent((KeyEvent) event);
         } else if (event instanceof WindowEvent) {
             handleWindowEvent((WindowEvent) event);
         }
        preDispatchEvent(event);
        super.dispatchEvent(event);
        postDispatchEvent(event);
     }
 
     /**
      * Triggered before the event is dispatched to AWT.
      *
      * @param event Event about to be dispatched
      */
     protected void preDispatchEvent(final AWTEvent event) {
         //Do nothing
     }
 
     /**
      * Called just after an event is dispatched to AWT.
      *
      * @param event Event that has been dispatched
      */
     protected void postDispatchEvent(final AWTEvent event) {
         //Do nothing
     }
 
     /**
      * Handles key events.
      *
      * @param ke Key event
      */
     private void handleKeyEvent(final KeyEvent ke) {
         ActionManager.processEvent(CoreActionType.CLIENT_KEY_PRESSED, null,
                 KeyStroke.getKeyStroke(ke.getKeyChar(), ke.getModifiers()));
     }
 
     /**
      * Handles mouse events.
      *
      * @param me Mouse event
      */
     private void handleMouseEvent(final MouseEvent me) {
         if (!me.isPopupTrigger() || me.getComponent() == null) {
             return;
         }
 
         final Component comp = SwingUtilities.getDeepestComponentAt(
                 me.getComponent(), me.getX(), me.getY());
 
         if (!(comp instanceof JTextComponent) || MenuSelectionManager
                 .defaultManager().getSelectedPath().length > 0) {
             return;
         }
 
         final JTextComponent tc = (JTextComponent) comp;
         final JPopupMenu menu = new JPopupMenu();
         menu.add(new CutAction(tc));
         menu.add(new CopyAction(tc));
         menu.add(new PasteAction(tc));
 
         final Point pt = SwingUtilities.convertPoint(me.getComponent(),
                 me.getPoint(), tc);
         menu.show(tc, pt.x, pt.y);
     }
 
     /**
      * Handles window events.
      *
      * @param we Window event
      */
     private void handleWindowEvent(final WindowEvent we) {
         if ((we.getSource() instanceof Window) && controller.hasMainFrame()) {
             if (we.getID() == WindowEvent.WINDOW_OPENED) {
                 controller.addTopLevelWindow((Window) we.getSource());
             } else if (we.getID() == WindowEvent.WINDOW_CLOSED) {
                 controller.delTopLevelWindow((Window) we.getSource());
             }
         }
     }
 }

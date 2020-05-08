 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.tester;
 
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Frame;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.listener.EventLock;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.tester.AbstractApplicationTester;
 import org.eclipse.jubula.rc.common.util.KeyStrokeUtil;
 import org.eclipse.jubula.rc.common.util.MatchUtil;
 import org.eclipse.jubula.rc.common.util.Verifier;
 import org.eclipse.jubula.rc.swing.components.SwingComponent;
 import org.eclipse.jubula.rc.swing.listener.ComponentHandler;
 import org.eclipse.jubula.rc.swing.listener.FocusTracker;
 import org.eclipse.jubula.rc.swing.tester.util.EventListener;
 import org.eclipse.jubula.rc.swing.tester.util.WindowHelper;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 /**
  * 
  * @author BREDEX GmbH
  *
  */
 public class SwingApplicationTester extends AbstractApplicationTester {
 
     /**
      * This condition is true if the event is an 'window opened' event
      * and the event source is a frame/dialog with a certain title.
      * It is also true if the event is a 'component shown' event and the 
      * event source is a frame/dialog with a certain title.
      */
     private static class WindowOpenedCondition 
         implements EventListener.Condition {
         /**
          * the title
          */
         private final String m_title;
         
         /** the matches operation */
         private final String m_operator;
         
         /**
          * constructor
          * 
          * @param title the title
          * @param operator the matches operation
          */
         public WindowOpenedCondition(String title, String operator) {
             m_title = title;
             m_operator = operator;
         }
         /**
          * {@inheritDoc}
          */
         public boolean isTrue(AWTEvent event) {
             if (event.getID() != WindowEvent.WINDOW_OPENED
                 && event.getID() != ComponentEvent.COMPONENT_SHOWN) {
                 return false;
             }
             if (event.getSource() instanceof Frame) {
                 Frame frame = (Frame)event.getSource();
                 return MatchUtil.getInstance().match(
                     frame.getTitle(), m_title, m_operator);
             } else if (event.getSource() instanceof Dialog) {
                 Dialog dialog = (Dialog)event.getSource();
                 return MatchUtil.getInstance().match(
                     dialog.getTitle(), m_title, m_operator);
             } else {
                 return false;
             }
         }
     }
     
     /**
      * This condition is true if the event is an 'window activated' event
      * and the event source is a frame/dialog with a certain title.
      */
     private static class WindowActivatedCondition 
         implements EventListener.Condition {
         /**
          * the title
          */
         private final String m_title;
         
         /** the matches operation */
         private final String m_operator;
         
         /**
          * constructor
          * 
          * @param title the title
          * @param operator the matches operation
          */
         public WindowActivatedCondition(String title, String operator) {
             m_title = title;
             m_operator = operator;
         }
         /**
          * {@inheritDoc}
          */
         public boolean isTrue(AWTEvent event) {
             if (event.getID() != WindowEvent.WINDOW_ACTIVATED) {
                 return false;
             }
             if (event.getSource() instanceof Frame) {
                 Frame frame = (Frame)event.getSource();
                 return MatchUtil.getInstance().match(
                     frame.getTitle(), m_title, m_operator);
             } else if (event.getSource() instanceof Dialog) {
                 Dialog dialog = (Dialog)event.getSource();
                 return MatchUtil.getInstance().match(
                     dialog.getTitle(), m_title, m_operator);
             } else {
                 return false;
             }
         }
     }
 
     /**
      * This condition is true if the event is an 'window closed' event
      * and the event source is a frame/dialog with a certain title.
      * It is also true if the event is a 'component hidden' event and the 
      * event source is a frame/dialog with a certain title.
      */
     private static class WindowClosedCondition 
         implements EventListener.Condition {
         /**
          * the title
          */
         private final String m_title;
         
         /** the matches operation */
         private final String m_operator;
         
         /**
          * constructor
          * 
          * @param title the title
          * @param operator the matches operation
          */
         public WindowClosedCondition(String title, String operator) {
             m_title = title;
             m_operator = operator;
         }
         /**
          * {@inheritDoc}
          */
         public boolean isTrue(AWTEvent event) {
            if (event.getID() == WindowEvent.WINDOW_LOST_FOCUS) {
                String name = null;
                if (event.getSource() instanceof Frame) {
                    Frame frame = (Frame)event.getSource();
                    name = frame.getTitle();
                } else if (event.getSource() instanceof Dialog) {
                    Dialog dialog = (Dialog)event.getSource();
                    name = dialog.getTitle();
                } else {
                    // Window found, but we currently do not support it, because it has no title
                    return false;
                }
                return MatchUtil.getInstance().match(name, m_title, m_operator);
             }
            return false;
         }
     }
 
     /**
      * The logging.
      */
     private static AutServerLogger log = 
         new AutServerLogger(SwingApplicationTester.class);
     
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         return null;
     }
 
     /**
      * Waits <code>timeMillSec</code> if the application opens a window
      * with the given title.
      * 
      * @param title the title
      * @param operator the comparing operator
      * @param pTimeout the time in ms
      * @param delay delay after the window is shown
      */
     public void rcWaitForWindow(final String title, String operator, 
         int pTimeout, int delay) {
         
         EventListener.Condition cond = 
             new WindowOpenedCondition(title, operator);
         EventLock lock = new EventLock();
         AWTEventListener listener = new EventListener(lock, cond);
         Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                 AWTEvent.WINDOW_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
 
         if (isWindowOpen(title, operator)) {
             lock.release();
         }
         try {
             synchronized (lock) {
                 long timeout = pTimeout;
                 long done = System.currentTimeMillis() + timeout;
                 long now;
                 while (!lock.isReleased() && (timeout > 0)) {
                     try {
                         lock.wait(timeout);
                         now = System.currentTimeMillis();
                         timeout = done - now;
                     } catch (InterruptedException e) {
                         // ignore
                     }
                 }
             }
         } finally {
             Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
         }
         if (!lock.isReleased() && !isWindowOpen(title, operator)) {
             throw new StepExecutionException("window did not open", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.TIMEOUT_EXPIRED));
         }
         TimeUtil.delay(delay);
     }
     /**
      * Waits <code>timeMillSec</code> if the application activates a window
      * with the given title.
      * 
      * @param title the title
      * @param operator the comparing operator
      * @param pTimeout the time in ms
      * @param delay delay after the window is activated
      */
     public void rcWaitForWindowActivation(final String title, String operator, 
         int pTimeout, int delay) {
         
         EventListener.Condition cond = new WindowActivatedCondition(title, 
                 operator);
         EventLock lock = new EventLock();
         AWTEventListener listener = new EventListener(lock, cond);
         Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                 AWTEvent.WINDOW_EVENT_MASK);
         
         if (isWindowActive(title, operator)) {
             lock.release();
         }
         try {
             synchronized (lock) {
                 long timeout = pTimeout;
                 long done = System.currentTimeMillis() + timeout;
                 long now;
                 while (!lock.isReleased() && (timeout > 0)) {
                     try {
                         lock.wait(timeout);
                         now = System.currentTimeMillis();
                         timeout = done - now;
                     } catch (InterruptedException e) {
                         // ignore
                     }
 
                 }
             }
         } finally {
             Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
         }
         if (!lock.isReleased() && !isWindowActive(title, operator)) {
             throw new StepExecutionException("window was not activated", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.TIMEOUT_EXPIRED));
         }
         TimeUtil.delay(delay);
     }
     
     /**
      * Waits <code>timeMillSec</code> if the application closes (or hides) 
      * a window with the given title. If no window with the given title can
      * be found, then it is assumed that the window has already closed.
      * 
      * @param title the title
      * @param operator the comparing operator
      * @param pTimeout the time in ms
      * @param delay delay after the window is closed
      */
     public void rcWaitForWindowToClose(final String title, String operator, 
         int pTimeout, int delay) {
         
         EventListener.Condition cond = 
             new WindowClosedCondition(title, operator);
         EventLock lock = new EventLock();
         AWTEventListener listener = new EventListener(lock, cond);
 
         Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                 AWTEvent.WINDOW_EVENT_MASK);
         if (!isWindowOpen(title, operator)) {
             lock.release();
         }
         
         try {
             synchronized (lock) {
                 long timeout = pTimeout;
                 long done = System.currentTimeMillis() + timeout;
                 long now;
                 while (!lock.isReleased() && (timeout > 0)) {
                     try {
                         lock.wait(timeout);
                         now = System.currentTimeMillis();
                         timeout = done - now;
                     } catch (InterruptedException e) {
                         // ignore
                     }
                 }
             }
         } finally {
             Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
         }
         if (!lock.isReleased() && isWindowOpen(title, operator)) {
             throw new StepExecutionException("window did not close", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.TIMEOUT_EXPIRED));
         }
         
         TimeUtil.delay(delay);
     }
 
     /**
      * Checks for the existence of a window with the given title
      * 
      * @param title
      *            the title
      * @param operator
      *            the comparing operator
      * @param exists
      *            <code>True</code> if the window is expected to exist and be
      *            visible, otherwise <code>false</code>.
      */
     public void rcCheckExistenceOfWindow(final String title, String operator,
             boolean exists) {
         Verifier.equals(exists, isWindowOpen(title, operator));
     }
 
     /**
      * {@inheritDoc}
      */
     public Rectangle getActiveWindowBounds() {
         Window activeWindow = WindowHelper.getActiveWindow();
         if (activeWindow != null) {
             Rectangle activeWindowBounds = 
                 new Rectangle(activeWindow.getBounds()); 
             activeWindowBounds.setLocation(activeWindow.getLocationOnScreen());
             
             return activeWindowBounds;
         }
         return null;
     }
     /**
      * {@inheritDoc}
      */
     protected IRobot getRobot() {
         return AUTServer.getInstance().getRobot();
     }
 
     /**
      * perform a keystroke specified according <a
      * href=http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)>
      * string representation of a keystroke </a>,
      * 
      * @param modifierSpec the string representation of the modifiers
      * @param keySpec the string representation of the key
      */
     public void rcKeyStroke(String modifierSpec, String keySpec) {
         if (keySpec == null || keySpec.trim().length() == 0) {
             throw new StepExecutionException(
                 "The base key of the key stroke must not be null or empty", //$NON-NLS-1$
                 EventFactory.createActionError());
         }
         String key = keySpec.trim().toUpperCase();
         String mod = KeyStrokeUtil.getModifierString(modifierSpec);
         if (mod.length() > 0) {
             getRobot().keyStroke(mod.toString() + " " + key); //$NON-NLS-1$
         } else {
             int code = getKeyCode(key);
             if (code != -1) {
                 rcKeyType(code);
             } else {
                 getRobot().keyStroke(key);
             }
         }
     }
     /**
      * {@inheritDoc}
      */
     protected Object getFocusOwner() {
         return FocusTracker.getFocusOwner();
     }
 
     /**
      * {@inheritDoc}
      */
     protected int getEventCode(int key) {
         int event = 0;
         switch (key) {
             case 1 : 
                 event = KeyEvent.VK_NUM_LOCK;
                 break;
             case 2 : 
                 event = KeyEvent.VK_CAPS_LOCK;
                 break;
             case 3 : 
                 event = KeyEvent.VK_SCROLL_LOCK;
                 break;
             default : 
                 break;
         }
         return event;
     }
 
     /**
      * {@inheritDoc}
      */
     protected Object getActiveWindow() {
         return WindowHelper.getActiveWindow();
     }
 
     /**
      * Returns <code>true</code> if a window with the given title is open and 
      * visible.
      * 
      * @param title the title
      * @param operator the matches/equals operator
      * @return if the window is open and visible
      */
     private boolean isWindowOpen(String title, String operator) {
         boolean wasInterrupted;
         do {
             try {
                 wasInterrupted = false;
                 Collection components = ComponentHandler.getAutHierarchy()
                         .getHierarchyMap().keySet();
                 for (Iterator it = components.iterator(); it.hasNext();) {
                     Component c = ((SwingComponent)it.next())
                         .getRealComponent();
                     if (c.isShowing()) {
                         if (c instanceof Frame) {
                             Frame frame = (Frame)c;
                             if (MatchUtil.getInstance().match(frame.getTitle(), 
                                 title, operator)) {
 
                                 return true;
                             }
                         }
                         if (c instanceof Dialog) {
                             Dialog dialog = (Dialog)c;
                             if (MatchUtil.getInstance().match(dialog.getTitle(),
                                 title, operator)) {
 
                                 return true;
                             }
                         }
                     }
                 }
             } catch (ConcurrentModificationException e) {
                 log.debug("hierarchy modified while traversing", e); //$NON-NLS-1$
                 wasInterrupted = true;
             }
         } while (wasInterrupted);
         return false;
     }
     
     /**
      * Returns <code>true</code> if a window with the given title has focus
      * 
      * @param title the title
      * @param operator the matches/equals operator
      * @return if the window has focus
      */
     private boolean isWindowActive(String title, String operator) {
         
         Window activeWindow = WindowHelper.getActiveWindow();
         if (activeWindow != null) {
             String windowTitle = null;
             if (activeWindow instanceof Dialog) {
                 windowTitle = ((Dialog)activeWindow).getTitle();
             } else if (activeWindow instanceof Frame) {
                 windowTitle = ((Frame)activeWindow).getTitle();
             }
             
             if (MatchUtil.getInstance().match(windowTitle, title, operator)) {
                 return true;
             }
         }
 
         return false;
     }
     
     /**
      * @param keyCodeName
      *            The name of a key code, e.g. <code>TAB</code> for a
      *            tabulator key code
      * @return The key code or <code>-1</code>, if the key code name doesn't
      *         exist in the <code>KeyEvent</code> class
      * @throws StepExecutionException
      *             If the key code name cannot be converted to a key code due to
      *             the reflection call
      */
     public int getKeyCode(String keyCodeName) throws StepExecutionException {
         int code = -1;
         String codeName = "VK_" + keyCodeName; //$NON-NLS-1$
         try {
             code = KeyEvent.class.getField(codeName).getInt(KeyEvent.class);
         } catch (IllegalArgumentException e) {
             throw new StepExecutionException(e.getMessage(), EventFactory
                 .createActionError());
         } catch (SecurityException e) {
             throw new StepExecutionException(e.getMessage(), EventFactory
                 .createActionError());
         } catch (IllegalAccessException e) {
             throw new StepExecutionException(e.getMessage(), EventFactory
                 .createActionError());
         } catch (NoSuchFieldException e) {
             if (log.isInfoEnabled()) {
                 log.info("The key expression '" + keyCodeName //$NON-NLS-1$
                     + "' is not a key code, typed as key stroke instead"); //$NON-NLS-1$
             }
         }
         return code;
     }
 }

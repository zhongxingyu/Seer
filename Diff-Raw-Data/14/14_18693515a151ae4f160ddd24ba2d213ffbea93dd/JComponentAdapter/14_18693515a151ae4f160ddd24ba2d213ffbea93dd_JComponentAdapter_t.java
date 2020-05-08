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
 package org.eclipse.jubula.rc.swing.tester.adapter;
 
 
 import java.awt.AWTEvent;
 import java.awt.Toolkit;
 
 import javax.swing.JComponent;
 
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.listener.EventLock;
 import org.eclipse.jubula.rc.common.tester.AbstractMenuTester;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IWidgetComponent;
 import org.eclipse.jubula.rc.swing.driver.KeyCodeConverter;
 import org.eclipse.jubula.rc.swing.tester.JMenuBarTester;
 import org.eclipse.jubula.rc.swing.tester.util.EventListener;
 import org.eclipse.jubula.rc.swing.tester.util.TesterUtil;
 import org.eclipse.jubula.rc.swing.tester.util.TesterUtil.PopupShownCondition;
 import org.eclipse.jubula.tools.constants.TimeoutConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 /**
  * Implements the interface for widgets and supports basic methods
  * which are needed for nearly all Swing UI components.
  * This is a basic adaption for <code>JComponent</code>.
  * 
  * @author BREDEX GmbH 
  */
 public class JComponentAdapter extends AbstractComponentAdapter
     implements IWidgetComponent {
     /** the component */
     private JComponent m_component;
 
     /**
      * Used to store the component into the adapter.
      * 
      * @param objectToAdapt
      *            the object to adapt
      */
     public JComponentAdapter(Object objectToAdapt) {
         m_component = (JComponent) objectToAdapt;
     }
     
     /**
      * {@inheritDoc}
      */
     public Object getRealComponent() {
         return m_component;
     }
     
     /**
      * Gets the IEventThreadQueuer.
      *
      * @return The Robot
      * @throws RobotException
      *             If the Robot cannot be created.
      */
     protected IRobot getRobot() throws RobotException {
         return getRobotFactory().getRobot();
     }
     /**
      * @return The event thread queuer.
      */
     public IEventThreadQueuer getEventThreadQueuer() {
         return getRobotFactory().getEventThreadQueuer();
     }
     
     /**
      * {@inheritDoc}
      */
     public String getPropteryValue(final String propertyname) {
         Object prop = getEventThreadQueuer().invokeAndWait("getProperty",  //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         try {
                             return getRobot().getPropertyValue(
                                     getRealComponent(), propertyname);
                         } catch (RobotException e) {
                             throw new StepExecutionException(
                                 e.getMessage(), 
                                 EventFactory.createActionError(
                                     TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                         }
                     }
                 });
         return String.valueOf(prop);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isShowing() {
         Boolean returnvalue = (Boolean) getEventThreadQueuer().invokeAndWait(
                 "isShowing", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return m_component.isShowing()
                                 ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return (boolean) returnvalue.booleanValue();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean hasFocus() {
         Boolean returnvalue = (Boolean) getEventThreadQueuer().invokeAndWait(
                 "hasFocus", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return m_component.hasFocus()
                                 ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return (boolean) returnvalue.booleanValue();
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean isEnabled() {
         Boolean returnvalue = (Boolean) getEventThreadQueuer().invokeAndWait(
                 "isEnabled", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return m_component.isEnabled()
                                 ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return (boolean) returnvalue.booleanValue();
     }
     
     /**
      * {@inheritDoc}
      */
     public AbstractMenuTester showPopup(final int button) {
         final Object component = m_component;
         Runnable showPopup = new Runnable() {
             public void run() {
                 RobotTiming.sleepPreShowPopupDelay();
                 ClassLoader oldCl = Thread.currentThread()
                     .getContextClassLoader();
                 Thread.currentThread().setContextClassLoader(component
                         .getClass().getClassLoader());
                 if ((getRobot()).isMouseInComponent(component)) {
                     getRobot().clickAtCurrentPosition(
                             component, 1, button);
                 } else {
                     getRobot().click(component, null, 
                         ClickOptions.create()
                             .setClickCount(1)
                             .setMouseButton(button));
                 }
                 Thread.currentThread().setContextClassLoader(oldCl);
             }
         };
 
         return showPopup(showPopup);
     }
     
     /**
      * {@inheritDoc}
      */
     public AbstractMenuTester showPopup(
             final int xPos, final String xUnits, 
             final int yPos, final String yUnits, final int button)
         throws StepExecutionException {
         final Object component = m_component;
         Runnable showPopup = new Runnable() {
             public void run() {
                 RobotTiming.sleepPreShowPopupDelay();
                 boolean isAbsoluteCoordinatesX = 
                     xUnits.equalsIgnoreCase(
                         CompSystemConstants.POS_UNIT_PIXEL); 
                 boolean isAbsoluteCoordinatesY = 
                     yUnits.equalsIgnoreCase(
                         CompSystemConstants.POS_UNIT_PIXEL); 
                 getRobot().click(component, null, 
                     ClickOptions.create().setMouseButton(button),
                     xPos, isAbsoluteCoordinatesX, 
                     yPos, isAbsoluteCoordinatesY);
             }
         };
         return showPopup(showPopup);
     }
     
     /**
      * Shows a popup menu using the given runnable and waits for the popup
      * menu to appear.
      *
      * @param showPopupOperation The implementation to use for opening the
      *                           popup menu.
      * @return the popup menu.
      */
     public AbstractMenuTester showPopup(Runnable showPopupOperation) {
         PopupShownCondition cond = new PopupShownCondition();
         EventLock lock = new EventLock();
         EventListener listener = new EventListener(lock, cond);
         Toolkit.getDefaultToolkit().addAWTEventListener(listener,
                 AWTEvent.CONTAINER_EVENT_MASK);
 
         // showPopupOperation must run in the current thread in order to
         // avoid a race condition.
         showPopupOperation.run();
 
         synchronized (lock) {
             try {
                 long timeout = TimeoutConstants.SERVER_TIMEOUT_WAIT_FOR_POPUP;
                 long done = System.currentTimeMillis() + timeout;
                 long now;
                 while ((!lock.isReleased() || (cond.getPopup() == null)
                         || !cond.getPopup().isShowing())
                         && (timeout > 0)) {
                     lock.wait(timeout);
                     now = System.currentTimeMillis();
                     timeout = done - now;
                 }
             } catch (InterruptedException e) {
                 // ignore
             } finally {
                 Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
             }
         }
         if (!lock.isReleased() || (cond.getPopup() == null)
                 || !cond.getPopup().isShowing()) {
             throw new StepExecutionException("popup not shown", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.POPUP_NOT_FOUND));
         }
         AbstractMenuTester menuCAPs = new JMenuBarTester();
         menuCAPs.setComponent(cond.getPopup());
         return menuCAPs;
     }
     
     /**
      * {@inheritDoc}
      */
     public void showToolTip(final String text, final int textSize,
             final int timePerWord, final int windowWidth) {
        StepExecutionException.throwUnsupportedAction();
     }
     
     /**
      * {@inheritDoc}
      */
     public void rcDrag(int mouseButton, String modifier, int xPos,
             String xUnits, int yPos, String yUnits) {
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         final IRobot robot = getRobot();
         clickDirect(0, mouseButton, xPos, xUnits, yPos, yUnits);
         TesterUtil.pressOrReleaseModifiers(modifier, true);
         robot.mousePress(null, null, mouseButton);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void rcDrop(int xPos, String xUnits, int yPos, String yUnits,
             int delayBeforeDrop) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         final String modifier = dndHelper.getModifier();
         final int mouseButton = dndHelper.getMouseButton();
         try {
             clickDirect(0, mouseButton, xPos, xUnits, yPos, yUnits);
             TimeUtil.delay(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, mouseButton);
             TesterUtil.pressOrReleaseModifiers(modifier, false);
         }
     }
     
         /**
          * clicks into the component.
          *
          * @param count amount of clicks
          * @param button what mouse button should be used
          * @param xPos what x position
          * @param xUnits should x position be pixel or percent values
          * @param yPos what y position
          * @param yUnits should y position be pixel or percent values
          * @throws StepExecutionException error
          */
     private void clickDirect(int count, int button, int xPos, String xUnits,
             int yPos, String yUnits) throws StepExecutionException {
 
         getRobot().click(m_component, null,
                 ClickOptions.create()
                     .setClickCount(count)
                     .setMouseButton(button),
                 xPos, xUnits.equalsIgnoreCase(
                     CompSystemConstants.POS_UNIT_PIXEL),
                 yPos, yUnits.equalsIgnoreCase(
                     CompSystemConstants.POS_UNIT_PIXEL));
     }
     
     /**
      * {@inheritDoc}
      */
     public int getKeyCode(String mod) {
         return KeyCodeConverter.getKeyCode(mod);
     }
 }

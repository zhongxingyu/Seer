 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.swing.driver;
 
 import java.awt.AWTError;
 import java.awt.AWTEvent;
 import java.awt.AWTException;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Event;
 import java.awt.Frame;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.CellRendererPane;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.ClickOptions.ClickModifier;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IMouseMotionTracker;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRobotEventConfirmer;
 import org.eclipse.jubula.rc.common.driver.IRobotEventInterceptor;
 import org.eclipse.jubula.rc.common.driver.IRobotFactory;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.driver.InterceptorOptions;
 import org.eclipse.jubula.rc.common.driver.KeyTyper;
 import org.eclipse.jubula.rc.common.driver.MouseMovementStrategy;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.util.PointUtil;
 import org.eclipse.jubula.rc.swing.components.SwingHierarchyContainer;
 import org.eclipse.jubula.rc.swing.listener.ComponentHandler;
 import org.eclipse.jubula.rc.swing.utils.SwingUtils;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TimingConstantsServer;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 
 /**
  * <p>
  * AWT/Swing implementation of the <code>IRobot</code> interface. It
  * uses the {@link java.awt.Robot}to move the mouse and perform clicks. Any
  * mouse move or click is intercepted and confirmed using the appropriate
  * AWT/Swing implementations of
  * {@link org.eclipse.jubula.rc.swing.driver.IRobotEventInterceptor}and
  * {@link org.eclipse.jubula.rc.swing.driver.IRobotEventConfirmer}.
  * </p>
  * 
  * <p>
  * The <code>click()</code> and <code>move()</code> implementations expect
  * that the graphics component is of type {@link java.awt.Component}and the
  * constraints object is <code>null</code> or of type
  * {@link java.awt.Rectangle}.
  * </p>
  * 
  * @author BREDEX GmbH
  * @created 17.03.2005
  */
 public class RobotAwtImpl implements IRobot {
     /** the timeout to flush native events (only relevant for Java 7) */
     private static final int FLUSH_TIMEOUT = 10000;
     
     /** the logger */
     private static AutServerLogger log = 
         new AutServerLogger(RobotAwtImpl.class);
     /** max retries to get the location on screen */
     private static final int MAX_RETRIES = 1;
     /** ID of Metal Look and Feel */
     private static final String METAL_LAF_ID = "Metal"; //$NON-NLS-1$
     /** The AWT Robot instance. */
     private Robot m_robot;
     /** The toolkit utilities */
     private EventFlusher m_eventFlusher;
     /** The event interceptor. */
     private IRobotEventInterceptor m_interceptor; 
     /** The mouse motion tracker. */
     private IMouseMotionTracker m_mouseMotionTracker; 
     /** The event thread queuer. */
     private IEventThreadQueuer m_queuer;
 
     /**
      * Scrolls a component to visible. The Scroller assumes that the component is
      * embedded (directly or indirectly) into a <code>JScrollPane</code>(
      * <code>JViewPort</code> more precisely). Hierarchies of scrollpanes are
      * also supported. The default mechanism of
      * {@link JComponent#scrollRectToVisible(java.awt.Rectangle) is not used}
      * here because of limitations and bugs: <br>
      * <code>JViewPort</code> overrides <code>scrollRectToVisible()</code>,
      * but doesn't call <code>super.scrollRectToVisible()</code>, so that
      * hierarchies of scrollpanes don't work. <br>
      * <code>JTextField</code> interprets <code>scrollRectToVisible()</code>
      * in a different way by scrolling the containing text, not the component
      * itself. Again, <code>super.scrollRectToVisible()</code> is not called.
      */
     private class Scroller {
         /** The component to scroll to visible. */
         private Component m_component; 
 
         /**
          * @param component The component to scroll to visible.
          */
         public Scroller(Component component) {
             m_component = component;
         }
 
         /**
          * Scrolls the component to visible. 
          * @param component The component.
          * @param aRect The bounds.
          */
         private void scrollRectToVisible(Component component, Rectangle aRect) {
             Container parent;
             int dx = component.getX();
             int dy = component.getY(); 
             for (parent = component.getParent(); (parent != null)
                 && !(parent instanceof JComponent)
                 && !(parent instanceof CellRendererPane); parent = parent
                     .getParent()) {
 
                 Rectangle bounds = parent.getBounds();
                 dx += bounds.x;
                 dy += bounds.y;
             } 
             if ((parent != null) && !(parent instanceof CellRendererPane)) {
                 aRect.x += dx;
                 aRect.y += dy;
                 if (parent instanceof JComponent) {
                     ((JComponent)parent).scrollRectToVisible(aRect);
                     Point p1 = getLocation(m_component, null);
                     Point p2 = parent.getLocationOnScreen();
                     aRect.x = p1.x - p2.x;
                     aRect.y = p1.y - p2.y;
                 }
             }
             if (parent != null) {
                 scrollRectToVisible(parent, aRect);
             }
         }
 
         /**
          * Scrolls the component passed to the constructor to visible. 
          * @param aRect The bounds of the component.
          */
         public void scrollRectToVisible(Rectangle aRect) {
             scrollRectToVisible(m_component, aRect);
         }
     }
 
     /**
      * Creates a new instance. 
      * @param factory The Robot factory instance.
      * @throws RobotException If the AWT-Robot cannot be created.
      */
     public RobotAwtImpl(IRobotFactory factory) throws RobotException {
         // HERE init robot in the AWT Event Dispatch Thread on Linux ??? (see
         // http://www.netbeans.org/issues/show_bug.cgi?id=37476)
         try {
             m_robot = new Robot();
             m_robot.setAutoWaitForIdle(false);
             m_robot.setAutoDelay(0);
         } catch (AWTException awte) {
             log.error(awte);
             m_robot = null;
             throw new RobotException(awte);
         } catch (SecurityException se) {
             log.error(se);
             m_robot = null;
             throw new RobotException(se);
         }
         m_interceptor = factory.getRobotEventInterceptor();
         m_mouseMotionTracker = factory.getMouseMotionTracker();
         m_queuer = factory.getEventThreadQueuer();
         m_eventFlusher = new EventFlusher(m_robot, FLUSH_TIMEOUT);
     }
 
     /**
      * Gets a location inside the component. If <code>offset</code> is
      * <code>null</code>, it returns the middle of the component otherwise it
      * adds the offset to the upper left corner. 
      * @param component the component to get the location for
      * @param offset the offset
      * @throws IllegalArgumentException if <code>component</code> is null
      * @return the <b>global </b> coordinates of <code>component</code>
      */
     private Point getLocation(Component component, final Point offset)
         throws IllegalArgumentException {
 
         Validate.notNull(component, "component must not be null"); //$NON-NLS-1$ 
         final Component comp = component; 
         IRunnable runnable = new IRunnable() {
             public Object run() {
                 Point pos = comp.getLocationOnScreen();
                 if (offset == null) {
                     pos.x += comp.getBounds().width / 2;
                     pos.y += comp.getBounds().height / 2;
                 } else {
                     pos.x += offset.x;
                     pos.y += offset.y;
                 }
                 return pos;
             }
         };
         Point point = null;
         StepExecutionException exc = null;
         int retries = 0; 
         do {
             try {
                 point = (Point)m_queuer.invokeAndWait("getLocation", runnable); //$NON-NLS-1$
             } catch (StepExecutionException e) {
                 
                 List allElements = new ArrayList(ComponentHandler.
                         getAutHierarchy().getHierarchyMap().values());
                 List allCheckBoxes = new ArrayList();
                 for (int i = 0; i < allElements.size(); i++) {
                     if (((SwingHierarchyContainer)allElements.get(i))
                             .getComponentID().getRealComponent() 
                             instanceof JCheckBox) {
                         
                         allCheckBoxes.add(allElements.get(i));
                     }
                 } 
                 exc = e;
                 retries++;
                 log.error("getLocation failed - " + retries); //$NON-NLS-1$
                 try {
                     Thread.sleep(TimingConstantsServer
                             .GET_LOCATION_RETRY_DELAY);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 }
             } 
         } while (point == null && retries < MAX_RETRIES);
         if (point == null) {
             throw exc;
         }
         return point;
     }
     
     /**
      * Implementation of the mouse click. The mouse is moved into the graphics
      * component by calling <code>moveImpl()</code> before performing the click. 
      * @param graphicsComponent The graphics component to click on
      * @param constraints The constraints, must be a 
      *                    <code>java.awt.Rectangle</code> or <code>null</code>.
      *                    The constraints are <em>relative</em> to the 
      *                    location/origin of the <code>graphicsComponent</code>.
      * @param clickOptions The click options
      * @param xPos xPos in component           
      * @param yPos yPos in component
      * @param yAbsolute true if y-position should be absolute
      * @param xAbsolute true if x-position should be absolute
      * @throws RobotException If the click delay is interrupted or the event confirmation receives a timeout.
      */
     private void clickImpl(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions, int xPos,
             boolean xAbsolute, int yPos, boolean yAbsolute)
         throws RobotException {
         moveImpl(graphicsComponent, (Rectangle)constraints, xPos, xAbsolute,
                 yPos, yAbsolute, clickOptions);
         clickImpl(graphicsComponent, clickOptions);
     }
 
     /**
      * Clicks at the current mouse position.
      * 
      * @param graphicsComponent The component used for confirming the click.
      * @param clickOptions Configuration for the click.
      */
     private void clickImpl(Object graphicsComponent, 
             ClickOptions clickOptions) {
         
         int buttonMask = getButtonMask(clickOptions.getMouseButton());
         int clickCount = clickOptions.getClickCount();
         int[] modifierMask = getModifierMask(clickOptions.getClickModifier());
         if (clickCount > 0) {
             IRobotEventConfirmer confirmer = null;
             if (clickOptions.isConfirmClick()) {
                 InterceptorOptions options = new InterceptorOptions(
                     new long[] { AWTEvent.MOUSE_EVENT_MASK });
                 confirmer = m_interceptor.intercept(options);
             }
             try {
                 pressModifier(modifierMask);
                 RobotTiming.sleepPreClickDelay();
                 
                 for (int i = 0; i < clickCount; i++) {
                     m_robot.mousePress(buttonMask);
                     RobotTiming.sleepPostMouseDownDelay();
                     m_eventFlusher.flush();
                     
                     m_robot.mouseRelease(buttonMask);
                     RobotTiming.sleepPostMouseUpDelay();
                     m_eventFlusher.flush();
                 }
                 if (confirmer != null) {
                     confirmer.waitToConfirm(graphicsComponent,
                             new ClickAwtEventMatcher(clickOptions));
                 }
             } finally {
                 releaseModifier(modifierMask);
             }
         }
     }
 
     /**
      * @param modifierMask
      *            array of modifiers to press before click
      */
     private void pressModifier(int[] modifierMask) {
         for (int i = 0; i < modifierMask.length; i++) {
             keyPress(null, modifierMask[i]);
         }
     }
 
     /**
      * @param modifierMask
      *            array of modifiers release after click
      */
     private void releaseModifier(int[] modifierMask) {
         for (int i = 0; i < modifierMask.length; i++) {
             keyRelease(null, modifierMask[i]);
         }
     }
 
     /**
      * @param clickModifier the click modifier to use for this click
      * @return an array of modifiers to press before click and release after click
      */
     private int[] getModifierMask(ClickModifier clickModifier) {
         int[] modifier = new int[0];
         if (clickModifier.hasModifiers(ClickModifier.M1)) {
             modifier = ArrayUtils.add(modifier, 
                     SwingUtils.getSystemDefaultModifier());
         }
         if (clickModifier.hasModifiers(ClickModifier.M2)) {
             modifier = ArrayUtils.add(modifier, 
                     SwingUtils.getSystemModifier2());
         }
         if (clickModifier.hasModifiers(ClickModifier.M3)) {
             modifier = ArrayUtils.add(modifier, 
                     SwingUtils.getSystemModifier3());
         }
         if (clickModifier.hasModifiers(ClickModifier.M4)) {
             modifier = ArrayUtils.add(modifier, 
                     SwingUtils.getSystemModifier4());
         }
         return modifier;
     }
 
     /**
      * Checks if the mouse has to be moved on <code>p</code> or if the mouse
      * pointer already resides on this location. 
      * @param p The point to move to
      * @return <code>true</code> if the mouse pointer resides on a different point, otherwise <code>false</code>.
      */
     private boolean isMouseMoveRequired(Point p) {
         boolean result = true;
         Point point = m_mouseMotionTracker.getLastMousePointOnScreen();
         if (point != null) {
             result = !point.equals(p); 
             if (log.isDebugEnabled()) {
                 MouseEvent event = (MouseEvent)m_mouseMotionTracker
                     .getLastMouseMotionEvent();
                 if (event != null) {
                     log.debug("Last mouse motion event point: " //$NON-NLS-1$
                         + event.getPoint());
                 }
                 log.debug("Last converted screen point  : " + point); //$NON-NLS-1$
                 log.debug("Required screen point        : " + p); //$NON-NLS-1$
                 log.debug("Mouse move required?         : " + result); //$NON-NLS-1$
             }
         }
         return result;
     }
 
     /**
      * Implementation of the mouse move. The mouse is moved into the graphics component.
      * @param graphicsComponent The component to move to
      * @param constraints The more specific constraints. Use this, for example 
      *                    when you want the click point to be relative to a part 
      *                    of the component (e.g. tree node, table cell, etc)  
      *                    rather than the overall component itself. May be  
      *                    <code>null</code>.
      * @param xPos xPos in component           
      * @param yPos yPos in component
      * @param xAbsolute true if x-position should be absolute  
      * @param yAbsolute true if y-position should be absolute  
      * @param clickOptions The click options 
      * @throws StepExecutionException If the click delay is interrupted or the  
      *                                event confirmation receives a timeout. 
      */ 
     private void moveImpl(Object graphicsComponent,
             final Rectangle constraints, final int xPos,
             final boolean xAbsolute, final int yPos, final boolean yAbsolute,
             ClickOptions clickOptions)
         throws StepExecutionException {
         if (clickOptions.isScrollToVisible()) {
             ensureComponentVisible((Component)graphicsComponent, constraints);
             m_eventFlusher.flush();
         }
         
         Component component = (Component)graphicsComponent;
 
         Rectangle bounds = null;
         bounds = new Rectangle(getLocation(component, new Point(0, 0)));
         bounds.width = component.getWidth();
         bounds.height = component.getHeight();
 
         if (constraints != null) {
             bounds.x += constraints.x;
             bounds.y += constraints.y;
             bounds.height = constraints.height;
             bounds.width = constraints.width;
         }
         
         Point p = PointUtil.calculateAwtPointToGo(xPos, xAbsolute, yPos,
                 yAbsolute, bounds); 
 
         // Move if necessary         
         if (isMouseMoveRequired(p)) {
             if (log.isDebugEnabled()) {
                 log.debug("Moving mouse to: " + p); //$NON-NLS-1$
             }
             IRobotEventConfirmer confirmer = null;
             if (clickOptions.isConfirmClick()) {
                 InterceptorOptions options = new InterceptorOptions(new long[]{
                     AWTEvent.MOUSE_MOTION_EVENT_MASK});
                 confirmer = m_interceptor.intercept(options);
             }
             Point startpoint = m_mouseMotionTracker.getLastMousePointOnScreen();
             if (startpoint == null) {
                 // If there is no starting point the center of the root component is used
                 Component root = SwingUtilities.getRoot(component);
                 Component c = (root != null) ? root : component;
                 startpoint = getLocation(c, null);
             }
             Point[] mouseMove = MouseMovementStrategy.
                        getMovementPath(startpoint, p, true);
             
             for (int i = 0; i < mouseMove.length; i++) {
                 m_robot.mouseMove(mouseMove[i].x, mouseMove[i].y);
                 m_eventFlusher.flush();
             }
 
             if (confirmer != null) {
                 confirmer.waitToConfirm(component, 
                         new MouseMovedAwtEventMatcher());
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints)
         throws RobotException {
         click(graphicsComponent, constraints, ClickOptions.create());
     }
 
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints,
         ClickOptions clickOptions) throws RobotException {
         
         clickImpl(graphicsComponent, constraints, clickOptions,
             50, false, 50, false);
     }
 
     /**
      * Gets the InputEvent-ButtonMask of the given mouse button number
      * @param button the button number
      * @return the InputEvent button mask
      */
     private int getButtonMask(int button) {
         switch (button) {
             case InputConstants.MOUSE_BUTTON_LEFT:
                 return InputEvent.BUTTON1_MASK;
             case InputConstants.MOUSE_BUTTON_MIDDLE:
                 return InputEvent.BUTTON2_MASK;
             case InputConstants.MOUSE_BUTTON_RIGHT:
                 return InputEvent.BUTTON3_MASK;
             default:
                 throw new RobotException("unsupported mouse button", null); //$NON-NLS-1$
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void clickAtCurrentPosition(Object graphicsComponent, 
             int clickCount, int button) {
         ClickOptions clickOptions = new ClickOptions();
         clickOptions.setClickCount(clickCount);
         clickOptions.setMouseButton(button);
         clickImpl(graphicsComponent, clickOptions);
     }
 
     /**
      * {@inheritDoc} java.lang.Object)
      */
     public void doubleClick(Object graphicsComponent, Object constraints)
         throws RobotException {
         
         click(graphicsComponent, constraints, ClickOptions.create()
             .setClickCount(2));
     }
 
     /**
      * {@inheritDoc}
      */
     public void move(Object graphicsComponent, Object constraints)
         throws RobotException {
 
         moveImpl(graphicsComponent, (Rectangle)constraints, 50, false, 50,
                 false, ClickOptions.create());
     }
 
     /**
      * {@inheritDoc}
      */
     public void type(Object graphicsComponent, char character)
         throws RobotException {
         
         Validate.notNull(graphicsComponent, "The graphic component must not be null"); //$NON-NLS-1$
         try {
             int modifier = 0;
             Component component = (Component)graphicsComponent;
             KeyEvent ke = new KeyEvent(component, KeyEvent.KEY_PRESSED, 
                 System.currentTimeMillis(), modifier, KeyEvent.VK_UNDEFINED, 
                 character);
             Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
             ke = new KeyEvent(component, KeyEvent.KEY_TYPED, 
                 System.currentTimeMillis(), modifier, KeyEvent.VK_UNDEFINED, 
                 character);
             Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
             IRobotEventConfirmer confirmer = m_interceptor.intercept(
                 new InterceptorOptions(new long[]{AWTEvent.KEY_EVENT_MASK}));
             ke = new KeyEvent(component, KeyEvent.KEY_RELEASED, 
                 System.currentTimeMillis(), modifier, KeyEvent.VK_UNDEFINED, 
                 character);
             Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(ke);
             confirmer.waitToConfirm(component,
                 new DefaultAwtEventMatcher(KeyEvent.KEY_RELEASED));
         } catch (AWTError awte) {
             log.error(awte);
             throw new RobotException(awte);
         } catch (SecurityException se) {
             log.error(se);
             throw new RobotException(se);
         }
     }
 
     /**
      * {@inheritDoc}
      */ 
     public void type(Object graphicsComponent, String text)
         throws RobotException {
         if (text != null) {
             for (int i = 0; i < text.length(); i++) {
                 char ch = text.charAt(i);
                 type(graphicsComponent, ch);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyType(Object graphicsComponent, int keycode)
         throws RobotException {
         try {
             InterceptorOptions options = new InterceptorOptions(new long[]{
                 AWTEvent.KEY_EVENT_MASK});
             IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
             try {
                 m_robot.keyPress(keycode);
                 m_eventFlusher.flush();
             } finally {
                 m_robot.keyRelease(keycode);
                 m_eventFlusher.flush();
             }
             confirmer.waitToConfirm(graphicsComponent, new KeyAwtEventMatcher(
                 KeyEvent.KEY_RELEASED));
         } catch (IllegalArgumentException e) {
             throw new RobotException(e);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public String getSystemModifierSpec() {
         String keyStrokeSpec = CompSystemConstants.MODIFIER_CONTROL;
         if (!(UIManager.getLookAndFeel().getID().equals(METAL_LAF_ID))) {
             if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                     == Event.META_MASK) {
                 keyStrokeSpec = CompSystemConstants.MODIFIER_META;
             } else if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                     == Event.ALT_MASK) {
                 keyStrokeSpec = CompSystemConstants.MODIFIER_ALT;
             }
         }
         return keyStrokeSpec; 
     }
 
     /**
      * Implements the key press or release. 
      * @param graphicsComponent The component, may be <code>null</code>
      * @param keyCode  The key code
      * @param press If <code>true</code>, the key is pressed, otherwise released
      */
     private void keyPressReleaseImpl(Object graphicsComponent, int keyCode,
         boolean press) {
 
         InterceptorOptions options = new InterceptorOptions(new long[]{
             AWTEvent.KEY_EVENT_MASK});
         IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
         if (press) {
             m_robot.keyPress(keyCode);
             m_eventFlusher.flush();
         } else {
             m_robot.keyRelease(keyCode);
             m_eventFlusher.flush();
         }
         confirmer.waitToConfirm(graphicsComponent, new KeyAwtEventMatcher(
             press ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED));
     }
 
     /**
      * {@inheritDoc} 
      */
     public void keyPress(Object graphicsComponent, int keycode)
         throws RobotException {
         
         keyPressReleaseImpl(graphicsComponent, keycode, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyRelease(Object graphicsComponent, int keycode)
         throws RobotException {
         
         keyPressReleaseImpl(graphicsComponent, keycode, false);
     }
 
     /**
      * a method to turn the toggle keys caps-lock, num-lock and scroll-lock on and off.
      * @param obj Component
      * @param key to set key Event
      * @param activated  boolean
      */
     public void keyToggle(Object obj, int key, boolean activated) {
         Validate.notNull(obj, "The graphic component must not be null"); //$NON-NLS-1$
         if ((activated && !((Component)obj).getToolkit().getLockingKeyState(
                 key)) || (!activated && ((Component)obj).getToolkit()
                         .getLockingKeyState(key))) {
             
             keyPressReleaseImpl(null, key, true);
             keyPressReleaseImpl(null, key, false);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void keyStroke(String keyStrokeSpec) throws RobotException {
         try {
             KeyTyper.getInstance().type(keyStrokeSpec, m_interceptor, 
                 new DefaultAwtEventMatcher(KeyEvent.KEY_PRESSED), 
                 new KeyReleasedEventMatcher());
         } catch (AWTException e) {
             throw new RobotException(e);
         }
     }
 
     /**
      * Ensures that the passed component is visible. 
      * @param component The component.
      * @param bounds Optional bounds inside the component. If not <code>null</code>, the bounds are scrolled to visible.
      * @throws RobotException If the component's screen location cannot be calculated.
      */
     private void ensureComponentVisible(final Component component,
         final Rectangle bounds) throws RobotException {
         m_queuer.invokeAndWait("ensureVisible", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     Rectangle rectangle = bounds != null ? new Rectangle(bounds)
                         : SwingUtilities.getLocalBounds(component);
                     if (log.isDebugEnabled()) {
                         log.debug("Scrolling rectangle to visible: " + rectangle); //$NON-NLS-1$
                     }
                     Scroller scroller = new Scroller(component);
                     scroller.scrollRectToVisible(rectangle);
                     return null;
                 }
             });
     }
 
     /**
      * {@inheritDoc} 
      */
     public void scrollToVisible(Object graphicsComponent, Object constraints)
         throws RobotException {
         
         ensureComponentVisible((Component)graphicsComponent,
             (Rectangle)constraints);
     }
 
     /**
      * {@inheritDoc}
      */
     public void activateApplication(String method) throws RobotException {
         try {
             Window window = getActiveWindow();
             if (window == null) {
                 return;
             }
             WindowActivationMethod wam =
                 WindowActivationMethod.createWindowActivationMethod(
                     method, m_robot, m_queuer);
             wam.activate(window);
             
             // Verify that window was successfully activated
             Window activeWindow = (Window)m_queuer.invokeAndWait(
                 "getActiveWindow", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         if (Frame.getFrames().length == 0) {
                             return null;
                         }
                         for (int i = 0; i < Frame.getFrames().length; ++i) {
                             Window curWindow = Frame.getFrames()[i];
                             while (curWindow.getOwner() != null) {
                                 curWindow = curWindow.getOwner();
                             }
                             if (curWindow.isFocused()) {
                                 return curWindow;
                             }
                         }
                         return null;
                     }
                 });
             if (activeWindow != window) {
                 throw new StepExecutionException(
                     I18n.getString(
                         TestErrorEvent.WINDOW_ACTIVATION_FAILED, true),
                     EventFactory.createActionError(
                         TestErrorEvent.WINDOW_ACTIVATION_FAILED));
             }
             
             
         } catch (Exception exc) {
             throw new RobotException(exc);
         }
     }
     
     /**
      * @return The current mouse position as a Point
      * {@inheritDoc}
      */
     public Point getCurrentMousePosition() {
         return m_mouseMotionTracker.getLastMousePointOnScreen();
     }
     
     
     /**
      * Guesses the active window. Returns null if no active window is found.
      * @return the active window
      */
     private Window getActiveWindow() {
         return (Window)m_queuer.invokeAndWait(
             "getActiveWindow", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     if (Frame.getFrames().length == 0) {
                         return null;
                     }
                     for (int i = 0; i < Frame.getFrames().length; ++i) {
                         Window window = Frame.getFrames()[i];
                         while (window.getOwner() != null) {
                             window = window.getOwner();
                         }
                         if (window.isVisible()) {
                             return window;
                         }
                     }
                     return null;
                 }
             });
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public boolean isMouseInComponent(Object graphicsComponent) {
         
         Component comp = (Component)graphicsComponent;
         final Point currMousePos = getCurrentMousePosition();
         if (currMousePos == null) {
             return false;
         }
         final Point treeLocUpperLeft = comp.getLocationOnScreen();
         final Rectangle bounds = comp.getBounds();
         final Point treeLocLowerRight = new Point(
             bounds.width  + treeLocUpperLeft.x, 
             bounds.height + treeLocUpperLeft.y); 
         
         final boolean x1 = currMousePos.x >= treeLocUpperLeft.x;
         final boolean x2 = currMousePos.x < treeLocLowerRight.x;
         final boolean y1 = currMousePos.y >= treeLocUpperLeft.y;
         final boolean y2 = currMousePos.y < treeLocLowerRight.y;
         
         return x1 && x2 && y1 && y2;
     }
     
     /**
      * Presses the given mouse button on the given component in the given
      * constraints. <br>
      * <b>Note:</b> Use only for Drag and Drop!
      * To click with the mouse, use click-methods!
      * @param graphicsComponent the component where to press the mouse button.
      * If null, the mouse is pressed at the current location.
      * @param constraints A constraints object used by the Robot implementation, may be <code>null</code>.
      * @param button the mouse button which is to be pressed. 
      */
     public void mousePress(Object graphicsComponent, Object constraints, 
             int button) {
         DragAndDropHelper.getInstance().setDragMode(true);
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
         
         RobotTiming.sleepPreClickDelay();
         
         m_robot.mousePress(getButtonMask(button));
         m_eventFlusher.flush();
     }
 
     /**
      * Releases the given mouse button on the given component in the given
      * constraints. <br>
      * <b>Note:</b> Use only for Drag and Drop!
      * To click with the mouse, use click-methods!
      * @param graphicsComponent The graphics component.
      * If null, the mouse button is released at the current location.
      * @param constraints A constraints object used by the Robot implementation, may be <code>null</code>.
      * @param button the mouse button.
      */
     public void mouseRelease(Object graphicsComponent, Object constraints,
             int button) throws RobotException {
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
         
         RobotTiming.sleepPreClickDelay();
         
         m_robot.mouseRelease(getButtonMask(button));
         m_eventFlusher.flush();
         DragAndDropHelper.getInstance().setDragMode(false);
     }
 
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions, int xPos, boolean xAbsolute,
             int yPos, boolean yAbsolute) throws RobotException {
 
         clickImpl(graphicsComponent, constraints, clickOptions, 
                 xPos, xAbsolute, yPos, yAbsolute);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPropertyValue(Object graphicsComponent,
             String propertyName) throws RobotException {
         String propertyValue = StringConstants.EMPTY;
         Validate.notNull(graphicsComponent, "Tested component must not be null"); //$NON-NLS-1$
         try {
             final Object prop = PropertyUtils.getProperty(
                     graphicsComponent, propertyName);
             propertyValue = String.valueOf(prop);
         } catch (IllegalAccessException e) {
             throw new RobotException(e);
         } catch (InvocationTargetException e) {
             throw new RobotException(e);
         } catch (NoSuchMethodException e) {
             throw new RobotException(e);
         }
         
         return propertyValue;
     }
 }

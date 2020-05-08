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
 package org.eclipse.jubula.rc.swt.driver;
 
 import java.awt.event.InputEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.swing.KeyStroke;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.ClickOptions.ClickModifier;
 import org.eclipse.jubula.rc.common.driver.IEventMatcher;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRobotEventConfirmer;
 import org.eclipse.jubula.rc.common.driver.IRobotEventInterceptor;
 import org.eclipse.jubula.rc.common.driver.IRobotFactory;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.driver.InterceptorOptions;
 import org.eclipse.jubula.rc.common.driver.MouseMovementStrategy;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.OsNotSupportedException;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.util.WorkaroundUtil;
 import org.eclipse.jubula.rc.swt.SwtAUTServer;
 import org.eclipse.jubula.rc.swt.implclasses.SwtApplicationImplClass;
 import org.eclipse.jubula.rc.swt.utils.SwtKeyCodeConverter;
 import org.eclipse.jubula.rc.swt.utils.SwtPointUtil;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Widget;
 
 
 /**
  * <p>
  * AWT/Swing implementation of the <code>IRobot</code> interface. It
  * uses the {@link java.awt.Robot} to move the mouse and perform clicks. Any
  * mouse move or click is intercepted and confirmed using the appropriate
  * AWT/Swing implementations of
  * {@link org.eclipse.jubula.rc.swt.driver.IRobotEventInterceptor}and
  * {@link org.eclipse.jubula.rc.swt.driver.IRobotEventConfirmer}.
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
 public class RobotSwtImpl implements IRobot {
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         RobotSwtImpl.class);
 
     /**
      * Base class for key typers.
      *
      * @author BREDEX GmbH
      * @created Nov 16, 2009
      */
     private abstract static class AbstractKeyTyper {
 
         /**
          * 
          * @return an event that the key for this key typer has been pressed.
          */
         public Event createKeyDownEvent() {
             return createKeyEvent(SWT.KeyDown);
         }
 
         /**
          * 
          * @return an event that the key for this key typer has been released.
          */
         public Event createKeyUpEvent() {
             return createKeyEvent(SWT.KeyUp);
         }
 
         /**
          * 
          * @param eventType The type of event to create.
          * @return an event of the given type with the key-specific information
          *         for this key typer.
          */
         private Event createKeyEvent(int eventType) {
             final Event ke = new Event();
             ke.type = eventType;
             ke.time = (int)System.currentTimeMillis();
             assignKey(ke);
             return ke;
         }
         
         /**
          * Sets values of parameter(s) of the given event such that the event
          * corresponds to the key represented by this key typer.
          * 
          * @param keyEvent The event to modify.
          */
         protected abstract void assignKey(Event keyEvent);
     }
 
     /**
      * Provides a keycode-based key event.
      *
      * @author BREDEX GmbH
      * @created Nov 19, 2009
      */
     private static class KeyCodeTyper extends AbstractKeyTyper {
         /** the keycode represented by this key typer */
         private int m_keyCode;
         
         /**
          * Constructor
          * 
          * @param keyCode The keycode for events generated by this key typer.
          */
         public KeyCodeTyper(int keyCode) {
             m_keyCode = keyCode;
         }
 
         /**
          * {@inheritDoc}
          */
         public void assignKey(Event keyEvent) {
             keyEvent.keyCode = m_keyCode;
         }
     }
 
     /**
      * Provides a character-based key event.
      *
      * @author BREDEX GmbH
      * @created Nov 19, 2009
      */
     private static class KeyCharTyper extends AbstractKeyTyper {
         /** the character represented by this key typer */
         private char m_keyChar;
         
         /**
          * Constructor
          * 
          * @param keyChar The character for events generated by this key typer.
          */
         public KeyCharTyper(char keyChar) {
             m_keyChar = keyChar;
         }
 
         /**
          * {@inheritDoc}
          */
         public void assignKey(Event keyEvent) {
             keyEvent.character = m_keyChar;
         }
     }
     
     /**
      * <code>m_autServer</code>
      */
     private SwtAUTServer m_autServer = null;
 
     /**
      * The AWT Robot instance.
      */
     private SwtRobot m_robot;
     
     /**
      * The KeyboardHelper.
      */
     private KeyboardHelper m_keyboardHelper = null;
 
     /**
      * The event interceptor.
      */
     private IRobotEventInterceptor m_interceptor;
 
     /** The event thread queuer. */
     private IEventThreadQueuer m_queuer;
     
     /**
      * Scrolls a component to visible. The Scroller asumes that the component is
      * embedded (directly or indirectly) into a <code>JScrollPane</code>(
      * <code>JViewPort</code> more precisely). Hierarchies of scrollpanes are
      * also supported. The default mechanismn of
      * {@link JComponent#scrollRectToVisible(java.awt.Rectangle) is not used
      * here because of limitations and bugs: <br>
      * 
      * <code>JViewPort</code> overrides <code>scrollRectToVisible()</code>,
      * but doesn't call <code>super.scrollRectToVisible()</code>, so that
      * hierarchies of scrollpanes don't work. <br>
      * 
      * <code>JTextField</code> interpretes <code>scrollRectToVisible()</code>
      * in a different way by scrolling the containing text, not the component
      * ifself. Again, <code>super.scrollRectToVisible()</code> is not called.
      */
     private class Scroller {
         
         /** The component to scroll to visible. */
         private Control m_component;
 
         /**
          * @param component The component to scroll to visible.
          */
         public Scroller(Control component) {
             m_component = component;
         }
 
         /**
          * Scrolls the component passed to the constructor to visible. Also 
          * scrolls the given rectangle (component-relative) to visible.
          * <em>NOTE</em>: This changes the given rectangle in order to reflect
          * its relative position within the component's client area.
          * 
          * @param aRect
          *            The bounds of the component, or a constraint within 
          *            a component.
          */
         public void scrollRectToVisible(final Rectangle aRect) {
             m_queuer.invokeAndWait("scrollToComponent", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     Point currentPoint = new Point(aRect.x, aRect.y);
                     if (m_component instanceof Scrollable) {
                         final Scrollable scrollable = (Scrollable)m_component;
                         currentPoint.x = scroll(
                                 currentPoint.x, scrollable.getHorizontalBar());
                         currentPoint.y = scroll(
                                 currentPoint.y, scrollable.getVerticalBar());
                     }
 
                     for (Control current = m_component; 
                             current.getParent() != null; 
                             current = current.getParent()) {
 
                         final Composite currentParent = current.getParent();
                         currentPoint.x = scroll(currentPoint.x, 
                                 currentParent.getHorizontalBar());
                         currentPoint.y = scroll(currentPoint.y, 
                                 currentParent.getVerticalBar());
 
                         currentPoint = current.getDisplay().map(
                                 current, currentParent, currentPoint);
                     }
                     return null;
                 }
 
             });
 
         }
         
         /**
          * 
          * <b>Must be called from the GUI thread.</b>
          * 
          * @param scrollTarget The target value to make visible by scrolling.
          * @param scrollBar The scroll bar to use for performing the scrolling.
          *                  May be <code>null</code>. If this is 
          *                  <code>null</code>, or if the component cannot be 
          *                  scrolled, then no scrolling will occur.
          * @return the remaining distance to <code>scrollTarget</code>. This is
          *         equivalent to <code>scrollTarget - 
          *         &lt;amountScrolled&gt;</code>
          */
         private int scroll(int scrollTarget, ScrollBar scrollBar) {
             if (scrollBar != null && !scrollBar.isDisposed()) {
                 int oldScrollLoc = scrollBar.getSelection();
                 scrollBar.setSelection(scrollTarget);
                 Event selectionEvent = new Event();
                 selectionEvent.type = SWT.Selection;
                 selectionEvent.widget = scrollBar;
                 // SWT.None indicates the end of a mouse drag event
                 selectionEvent.detail = SWT.None;
                 selectionEvent.time = 
                     (int)System.currentTimeMillis();
                 selectionEvent.display = scrollBar.getDisplay();
                 scrollBar.notifyListeners(SWT.Selection, selectionEvent);
                 return scrollTarget - (scrollBar.getSelection() - oldScrollLoc);
             }
 
             return scrollTarget;
         }
 
     }
 
     /**
      * Creates a new instance.
      * @param factory The Robot factory instance.
      * @throws RobotException If the SWT-Robot cannot be created.
      */
     public RobotSwtImpl(IRobotFactory factory) throws RobotException {
         try {
             m_autServer = (SwtAUTServer)AUTServer.getInstance();
             m_robot = new SwtRobot((m_autServer).getAutDisplay());
             m_robot.setAutoWaitForIdle(false);
             m_robot.setAutoDelay(0);
         } catch (SWTException swte) {
             log.error(swte);
             m_robot = null;
             throw new RobotException(swte);
         } catch (SecurityException se) {
             log.error(se);
             m_robot = null;
             throw new RobotException(se);
         }
         m_interceptor = factory.getRobotEventInterceptor();
         m_queuer = factory.getEventThreadQueuer();
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void setKeyboardLayout(Properties layout) {
         m_keyboardHelper = new KeyboardHelper(layout);
     }
     
     /**
      * Implementation of the mouse click. The mouse is moved into the graphics
      * component by calling <code>moveImpl()</code> before performing the click.
      * @param graphicsComponent The graphics component to click on
      * @param constraints The constraints, may be <code>null</code>.
      * @param clickOptions The click options
      * @throws RobotException If the click delay is interupted or the event confirmation receives a timeout.
      */
     private void clickImpl(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions) throws RobotException {
 
         clickImpl(graphicsComponent, constraints, clickOptions, 50, false, 50,
                 false);
     }
     
         
     /**
      * Implementation of the mouse click. The mouse is moved into the graphics
      * component by calling <code>moveImpl()</code> before performing the click.
      * @param graphicsComponent The graphics component to click on
      * @param constraints The constraints, may be <code>null</code>.
      * @param clickOptions The click options
      * @param xPos xPos in component           
      * @param yPos yPos in component
      * @param yAbsolute true if y-position should be absolute
      * @param xAbsolute true if x-position should be absolute
      * @throws RobotException If the click delay is interupted or the event confirmation receives a timeout.
      */
     private void clickImpl(final Object graphicsComponent,
             final Object constraints, final ClickOptions clickOptions,
             final int xPos, final boolean xAbsolute, final int yPos,
             final boolean yAbsolute)
         throws RobotException {
 
         Widget component = (Widget)graphicsComponent;
         Rectangle constraintsRect = (Rectangle)constraints;
 
         moveImpl(component, constraintsRect, 
                 xPos, xAbsolute, yPos, yAbsolute, clickOptions);
         
         clickImpl(component, clickOptions);
     }
 
 
     /**
      * Logs information for a RobotException (usually a timeout).
      * 
      * @param graphicsComponent The component being tested.
      * @param re    The <code>RobotException</code> to log.
      * @param sb    <code>StringBuffer</code> containing any desired initial 
      *              content.
      */
     private void logRobotException(final Object graphicsComponent, 
         RobotException re, final StringBuffer sb) {
         
         // Get mouse coordinates
         Object mouseCoords = 
             m_queuer.invokeAndWait("get mouse coordinates",  //$NON-NLS-1$
                 new IRunnable() {
 
                     public Object run() throws StepExecutionException {
                         return m_autServer.getAutDisplay()
                             .getCursorLocation();
                     }
             
                 });
         
         // Get component bounds
         Rectangle compBounds = null;
         if (graphicsComponent instanceof Widget) {
             
             compBounds = (Rectangle)m_queuer.invokeAndWait(
                 "getBounds", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return SwtUtils.getWidgetBounds(
                             (Widget)graphicsComponent);
                     }
 
                 });
         }
         
         sb.append("Component: "); //$NON-NLS-1$
         
         // Component value must be appended in the GUI thread. Otherwise
         // we receive a "*Wrong Thread*" message rather than actual
         // component information.
         m_queuer.invokeAndWait(
             "getBounds", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     sb.append(graphicsComponent);
 
                     // Return value not used
                     return null;
                 }
             });
 
         sb.append("\n"); //$NON-NLS-1$
         sb.append("Bounds: "); //$NON-NLS-1$
         sb.append(compBounds);
         sb.append("\n"); ////$NON-NLS-1$
         sb.append("Mouse position: "); //$NON-NLS-1$
         sb.append(mouseCoords);
         
         log.error(sb.toString(), re);
     }
 
 
     /**
      * Performs a mouse move only if the mouse cursor is not currently within 
      * given constraints. In this case, the mouse is moved into the middle of 
      * the given constraint.
      * 
      * @param constraint
      *            The rectangle to move to the center of, if necessary.
      */
     public void preMove(final Rectangle constraint) 
         throws RobotException {
         
         boolean isAlreadyInConstraints = ((Boolean)m_queuer.invokeAndWait(
                 "isAlreadyInConstraints", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         return constraint.contains(
                             m_autServer.getAutDisplay().getCursorLocation()) 
                             ? Boolean.TRUE : Boolean.FALSE;
                     }
             
                 })).booleanValue();
         if (!isAlreadyInConstraints) {
             Point p = new Point(constraint.x + (constraint.width / 2), 
                 constraint.y + (constraint.height / 2));
             m_robot.mouseMove(p.x - 1, p.y - 1);
             m_robot.mouseMove(p.x, p.y);
         }
     }
 
     /**
      * Checks if the mouse has to be moved on <code>p</code> or if the mouse
      * pointer already resides on this location.
      * Runs in the GUI-Thread
      * @param p The point to move to
      * @return <code>true</code> if the mouse pointer resides on a different point, otherwise <code>false</code>.
      */
     private boolean isMouseMoveRequired(Point p) {
         boolean result = false;
         Point point = (Point)m_queuer.invokeAndWait("isMouseMoveRequired",  //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     Display d = (m_autServer).getAutDisplay();
                     return d.getCursorLocation();
                 }
             });
         if (point != null) {
             result = !point.equals(p);
             result = true;
         }
         return result;
     }
 
     /**
      * The mouse is moved into the center of the graphics component.
      * @param graphicsComponent graphicsComponent The graphics component to move to.
      */
     private void moveToCenter(final Object graphicsComponent) {
         moveImpl((Widget)graphicsComponent, null, 
                 50, false, 50, false, ClickOptions.create());
     }
     
     /**
      * Implementation of the mouse move. The mouse is moved into the graphics
      * component.
      * 
      * @param graphicsComponent The component into which the mouse will be 
      *                          moved.
      * @param constraints The rectangle to move to, relative to the location of
      *                    the given component. May be <code>null</code>.
      *                    If <code>null</code>, the mouse will be moved to the
      *                    center of the given component.
      * @param xPos xPos in component           
      * @param yPos yPos in component
      * @param xAbsolute true if x-position should be absolute  
      * @param yAbsolute true if y-position should be absolute  
      * @param clickOptions Contains mouse movement strategy information.
      * @throws StepExecutionException If the click delay is interupted or the event confirmation receives a timeout.
      */
     private void moveImpl(final Widget graphicsComponent, 
             final Rectangle constraints, final int xPos, 
             final boolean xAbsolute, final int yPos, final boolean yAbsolute, 
             ClickOptions clickOptions) throws StepExecutionException {
         
         if (clickOptions.isScrollToVisible() 
                 && graphicsComponent instanceof Control) {
             ensureComponentVisible((Control)graphicsComponent, constraints);
         }
         
         Rectangle bounds = getBounds(graphicsComponent);
         
         if (constraints != null) {
             if (graphicsComponent instanceof Control) {
                 Point convertedLocation = convertLocation(constraints,
                         (Control)graphicsComponent);
                 bounds.x = convertedLocation.x;
                 bounds.y = convertedLocation.y;
             } else {
                 bounds.x += constraints.x;
                 bounds.y += constraints.y;
             }
             
             bounds.height = constraints.height;
             bounds.width = constraints.width;
         }
 
         Point pointToGo = SwtPointUtil.calculatePointToGo(
                 xPos, xAbsolute, yPos, yAbsolute, bounds);
 
         if (isMouseMoveRequired(pointToGo)) {
             if (log.isDebugEnabled()) {
                 log.debug("Moving mouse to: " + pointToGo); //$NON-NLS-1$
             }
             Point initialPoint = (Point)m_queuer.invokeAndWait("moveImpl",  //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() throws StepExecutionException {
                             Display d = m_autServer.getAutDisplay();
                             return d.getCursorLocation();
                         }
                     });
 
             if (pointToGo != null && (pointToGo.x < 0 || pointToGo.y < 0)) {
                 throw new RobotException(
                     "Error occurred while attempting to move the mouse pointer.",  //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.CLICKPOINT_OFFSCREEN, 
                             new String[] {
                                     String.valueOf(pointToGo.x), 
                                     String.valueOf(pointToGo.y)}));
             }
             
             java.awt.Point [] path = 
                 MouseMovementStrategy.getMovementPath(
                         new java.awt.Point(initialPoint.x, initialPoint.y), 
                         new java.awt.Point(pointToGo.x, pointToGo.y),
                         clickOptions.getStepMovement());
             
             for (int i = 0; i < path.length; i++) {
                 m_robot.mouseMove(path[i].x, path[i].y);
             }
             
             logAndCorrectMousePosition(pointToGo);
         }
 
     }
 
     /**
      * Use SWT's mapping function, if possible, as it is more
      * multi-platform than simply adding the x and y values.
      * @param constraints the constraints
      * @param graphicsComponent the graphics component
      * @return the converted location
      */
     private Point convertLocation(final Rectangle constraints,
         final Control graphicsComponent) {
         Point convertedLocation = (Point)m_queuer.invokeAndWait(
                 "toDisplay", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return graphicsComponent.toDisplay(constraints.x,
                                 constraints.y);
                     }
 
                 });
         // adjust Y-location due to issue / bug 353907
         if (EnvironmentUtils.isMacOS() 
                 && graphicsComponent instanceof org.eclipse.swt.widgets.List) {
             final org.eclipse.swt.widgets.List list = 
                 (org.eclipse.swt.widgets.List)graphicsComponent;
             Integer correctedYPos = (Integer)m_queuer.invokeAndWait(
                     "correctedYPos", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             return new Integer(list.getClientArea().y);
                         }
                     });
             convertedLocation.y += correctedYPos.intValue();
         }
         return convertedLocation;
     }
 
     /**
      * Checks whether the move was really successful. If not, a few workarounds
      * are attempted in order to correct the situation. The workarounds are 
      * logged at the warn-level as they are used. If, after all workarounds
      * have been applied, the mouse pointer is still not at the correct
      * position, then an error is logged.
      * 
      * @param pointToGo The point where the mouse pointer should currently be.
      */
     private void logAndCorrectMousePosition(Point pointToGo) {
         String runnableName = "moveImpl.getCursorPoint"; //$NON-NLS-1$
         Point curPoint = (Point)m_queuer.invokeAndWait(runnableName, 
                 new IRunnable() {
                 public Object run() throws StepExecutionException {
                     Display d = m_autServer.getAutDisplay();
                     return d.getCursorLocation();
                 }
             });
         
         if (!curPoint.equals(pointToGo)) {
             log.warn("Current and end points not equal after mouse move. Waiting 1 second to see if a delay fixes the problem."); //$NON-NLS-1$
             SwtRobot.delay(1000);
             m_robot.waitForIdle();
             curPoint = (Point)m_queuer.invokeAndWait(runnableName,
                     new IRunnable() {
                     public Object run() throws StepExecutionException {
                         Display d = m_autServer.getAutDisplay();
                         return d.getCursorLocation();
                     }
                 });
             if (!curPoint.equals(pointToGo)) {
                 log.warn("Delay did not fix the problem. Trying to call mouse move one more time with an additional wait afterward."); //$NON-NLS-1$
                 m_robot.mouseMove(pointToGo.x, pointToGo.y);
                 SwtRobot.delay(1000);
                 m_robot.waitForIdle();
                 SwtRobot.delay(1000);
                 curPoint = (Point)m_queuer.invokeAndWait(runnableName,
                         new IRunnable() {
                         public Object run() throws StepExecutionException {
                             Display d = m_autServer.getAutDisplay();
                             return d.getCursorLocation();
                         }
                     });
                 if (!curPoint.equals(pointToGo)) {
                     log.error("Current and end points not equal after mouse move. The mouse pointer could not be correctly moved."); //$NON-NLS-1$
                 }
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
      *      java.lang.Object, org.eclipse.jubula.rc.swt.robot.ClickOptions)
      */
     public void click(Object graphicsComponent, Object constraints,
         ClickOptions clickOptions) throws RobotException {
         
         clickImpl(graphicsComponent, constraints, clickOptions);
     }
 
     /**
      * {@inheritDoc}
      */
     public void doubleClick(Object graphicsComponent, Object constraints)
         throws RobotException {
         
         click(graphicsComponent, constraints, ClickOptions.create()
             .setClickCount(2));
     }
 
     /**
      * {@inheritDoc}
      */
     public void move(final Object graphicsComponent, final Object constraints)
         throws RobotException {
 
         moveToCenter(graphicsComponent);
     }
 
     /**
      * {@inheritDoc}
      */
     public void type(final Object graphicsComponent, final char character)
         throws RobotException {
         Validate.notNull(graphicsComponent, "The graphic component must not be null");  //$NON-NLS-1$
 
         // Workaround for issue 342718
         if (EnvironmentUtils.isMacOS()
                 && Character.toLowerCase(character) == WorkaroundUtil.CHAR_B) {
             SwtApplicationImplClass impClass = new SwtApplicationImplClass();
             impClass.gdNativeInputText(String.valueOf(character));
             return;
         }
         if (m_keyboardHelper == null) {
             throw new StepExecutionException("No keyboard layout available.", //$NON-NLS-1$ 
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_KEYBOARD_LAYOUT));
         }
         final KeyboardHelper.KeyStroke keyStroke = 
             m_keyboardHelper.getKeyStroke(character);
         final Integer[] modifiers = keyStroke.getModifiers();
         final char key = keyStroke.getChar();
         final InterceptorOptions options = new InterceptorOptions(new long[]{
             SWT.KeyUp});
         // add confirmer
         final IEventMatcher matcher = new DefaultSwtEventMatcher(SWT.KeyUp);
         final IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
         final Boolean succeeded = (Boolean)m_queuer.invokeAndWait(
             this.getClass().getName() + ".type",  //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     Boolean success = Boolean.TRUE;
                     try {
                         // press the modifier keys
                         for (int i = 0; i < modifiers.length; i++) {
                             final int mod = modifiers[i].intValue();
                             if (!postKeyPress(graphicsComponent, mod, '\0')) {
                                 success = Boolean.FALSE;
                                 break;
                             }
                         }
                         if (success.booleanValue()) {
                             if (!postKeyPress(graphicsComponent, 0, key)) {
                                 success = Boolean.FALSE;
                             }
                         }
                     } finally {
                         if (!postKeyRelease(graphicsComponent, 0, key)) {
                             success = Boolean.FALSE;
                         }
                         // release the modifier keys
                         for (int i = 0; i < modifiers.length; i++) {
                             final int mod = modifiers[i].intValue();
                             if (!postKeyRelease(graphicsComponent, mod, '\0')) {
                                 success = Boolean.FALSE;
                             }
                         }            
                     }
                     return success;
                 }
             });
         if (!succeeded.booleanValue()) {
             final String msg = "Failed to type character '"  //$NON-NLS-1$
                 + String.valueOf(character) + "' into component '" //$NON-NLS-1$
                 + SwtUtils.toString((Widget)graphicsComponent) + "'"; //$NON-NLS-1$
             if (log.isWarnEnabled()) {
                 log.warn(msg);                
             }
             throw new RobotException(msg, EventFactory.createActionError(
                     TestErrorEvent.INPUT_FAILED));
         }
         // Workaround for bug 342718
         if (!(key == WorkaroundUtil.CHAR_9 && EnvironmentUtils.isMacOS())) {
             confirmer.waitToConfirm(graphicsComponent, matcher);
         } else {
             TimeUtil.delay(50);
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
                 SWT.KeyUp, SWT.KeyDown});
             IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
             try {
                 m_robot.keyPress(keycode);
             } finally {
                 m_robot.keyRelease(keycode);
             }
             confirmer.waitToConfirm(graphicsComponent, new KeySwtEventMatcher(
                 SWT.KeyUp));
         } catch (IllegalArgumentException e) {
             throw new RobotException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyPress(Object graphicsComponent, int keycode)
         throws RobotException {
 
         // A direct "post" is necessary here (rather than using the AWT Robot)
         // because we do not receive event confirmation for the KeyDown event
         // on MacOS X Cocoa.
         if (!post(new KeyCodeTyper(keycode).createKeyDownEvent())) {
             final String msg = "Failed to post key event for keycode '" //$NON-NLS-1$
                     + keycode + "'"; //$NON-NLS-1$
             log.warn(msg);
             throw new RobotException(
                     msg,
                     EventFactory.createActionError(
                             TestErrorEvent.INVALID_INPUT));
         }
 
     }
 
     /**
      * {@inheritDoc}
      *      int)
      */
     public void keyRelease(Object graphicsComponent, int keycode)
         throws RobotException {
 
         // A direct "post" is necessary here (rather than using the AWT Robot)
         // because we do not receive event confirmation for the KeyDown event
         // on MacOS X Cocoa.
         if (!post(new KeyCodeTyper(keycode).createKeyUpEvent())) {
             final String msg = "Failed to post key event for keycode '" //$NON-NLS-1$
                     + keycode + "'"; //$NON-NLS-1$
             log.warn(msg);
             throw new RobotException(
                     msg,
                     EventFactory.createActionError(
                             TestErrorEvent.INVALID_INPUT));
         }
     }
 
     /**
      * 
      * @param keyStroke The key stroke. 
      * @return a list of key typers capable of generating the necessary
      *         events to simulate the modifiers of the given key stroke.
      */
     private List modifierKeyTypers(KeyStroke keyStroke) {
         List l = new LinkedList();
         int modifiers = keyStroke.getModifiers();
         // this is jdk 1.3 - code.
         // use ALT_DOWN_MASK instead etc. with jdk 1.4 !
         if ((modifiers & InputEvent.ALT_MASK) != 0) {
             l.add(new KeyCodeTyper(SWT.ALT));
         }
         if ((modifiers & InputEvent.CTRL_MASK) != 0) {
             l.add(new KeyCodeTyper(SWT.CTRL));
         }
         if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
             l.add(new KeyCodeTyper(SWT.SHIFT));
         }
         if ((modifiers & InputEvent.META_MASK) != 0) {
             l.add(new KeyCodeTyper(SWT.COMMAND));
         }
         return l;
     }
 
     /**
      * 
      * @param keyStrokeSpec String representing the key for which a 
      *                      key typer should be returned. 
      * @return a key typer capable of generating the necessary
      *         event to simulate the base key of the given key stroke
      *         spec.
      */
     private static AbstractKeyTyper getBaseKeyTyper(String keyStrokeSpec) {
         String [] specElements = keyStrokeSpec.split(" "); //$NON-NLS-1$
         String baseKey = specElements[specElements.length - 1];
         int code = SwtKeyCodeConverter.getKeyCode(baseKey);
         
         if (code == -1) {
             return new KeyCharTyper(
                     getOSSspecificSpecBaseCharacter(SwtKeyCodeConverter
                             .getKeyChar(baseKey).charValue()));
         }
         
         return new KeyCodeTyper(code);
     }
     
     /**
      * {@inheritDoc}
      */
     public void keyStroke(final String keyStrokeSpec) throws RobotException {
         final KeyStroke keyStroke = getKeyStroke(keyStrokeSpec);
         final List keyTyperList = modifierKeyTypers(keyStroke);
         keyTyperList.add(getBaseKeyTyper(keyStrokeSpec));
 
         m_robot.setAutoWaitForIdle(true);
         // first press all keys, then release all keys, but
         // avoid to press and release any key twice (even if perhaps alt
         // and meta should have the same keycode(??)
         final Set alreadyDown = new HashSet();
         final ListIterator i = keyTyperList.listIterator();
         final InterceptorOptions options = new InterceptorOptions(
                 new long[] { SWT.KeyUp });
         try {
             m_queuer.invokeAndWait(this.getClass().getName() + ".type", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() { // SYNCH THREAD START
                             boolean success = true;
                             while (i.hasNext()) {
                                 AbstractKeyTyper keyTyper = (AbstractKeyTyper)i
                                         .next();
                                 if (log.isDebugEnabled()) {
                                     log.debug("trying to press: " + keyTyper); //$NON-NLS-1$
                                 }
                                 if (!alreadyDown.contains(keyTyper)) {
                                     if (log.isDebugEnabled()) {
                                         log.debug("pressing: " + keyTyper); //$NON-NLS-1$
                                     }
 
                                     // post the event!
                                     if (!post(keyTyper.createKeyDownEvent())) {
                                         success = false;
                                     }
                                     alreadyDown.add(keyTyper);
                                 }
                             }
                             if (!success) {
                                 final String msg = "Failed to post keystroke '" //$NON-NLS-1$
                                         + keyStrokeSpec + "'"; //$NON-NLS-1$
                                 if (log.isWarnEnabled()) {
                                     log.warn(msg);
                                 }
                                 throw new RobotException(
                                         msg,
                                         EventFactory.createActionError(
                                                 TestErrorEvent.INVALID_INPUT));
                             }
                             return Boolean.valueOf(success);
                         }
                     });
         } finally {
             releaseKeys(options, alreadyDown, i);
         }
     }
 
 
     /**
      * Creates a KeyStroke of the given keyStrokeSpec
      * 
      * @param keyStrokeSpec
      *            see {@link KeyStroke#getKeyStroke(String)} and
      *            {@link KeyStroke#getKeyStroke(Char)}
      * @return a KeyStroke.
      * @throws RobotException
      *             if no KeyStroke can be created.
      */
     private KeyStroke getKeyStroke(String keyStrokeSpec) throws RobotException {
         KeyStroke keyStroke;
         if (keyStrokeSpec.length() == 1) {
             char singeKeyStrokeSpecChar = keyStrokeSpec.charAt(0);
             singeKeyStrokeSpecChar = 
                 getOSSspecificSpecBaseCharacter(singeKeyStrokeSpecChar);
             keyStroke = KeyStroke.getKeyStroke(singeKeyStrokeSpecChar);
         } else {
             int keyStrokeSpecSize = keyStrokeSpec.length();
             char keySpec = keyStrokeSpec.charAt(keyStrokeSpecSize - 1);
             // ''.toUpperCase is "SS" we do not want that!
             if ('' != keySpec) {
                 keySpec = Character.toUpperCase(keySpec);
             }
             String modifiedKeyStrokeSpec = keyStrokeSpec.substring(0,
                     keyStrokeSpecSize - 1) + keySpec;
             keyStroke = KeyStroke.getKeyStroke(modifiedKeyStrokeSpec);
         }
         if (keyStroke == null) {
             final String msg = "Failed to post keystroke '" + keyStrokeSpec  //$NON-NLS-1$
                 + "'"; //$NON-NLS-1$
             if (log.isWarnEnabled()) {
                 log.warn(msg);                
             }
             throw new RobotException(msg, EventFactory.createActionError(
                 TestErrorEvent.INVALID_PARAM_VALUE));
         }
         return keyStroke;
     }
 
     /**
      * @param character
      *            the character
      * @return the "corrected" character, e.g. for Mac OS X it has to be in
      *         lower case
      */
     private static char getOSSspecificSpecBaseCharacter(char character) {
         if (EnvironmentUtils.isMacOS() && Character.isUpperCase(character)) {
             return Character.toLowerCase(character);
         }
         return character;
     }
 
     /**
      * Ensures that the passed component is visible
      * @param component The component.
      * @param bounds Optional bounds inside the component. If not <code>null</code>, the bounds are scrolled to visible.
      * @throws RobotException If the component's screen location cannot be calculated.
      */
     private void ensureComponentVisible(final Control component,
         final Rectangle bounds) throws RobotException {
         
         Rectangle rectangle = bounds; 
         if (rectangle == null) {
             rectangle = (Rectangle)m_queuer.invokeAndWait(
                     "getRelativeWidgetBounds", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             return SwtUtils.getRelativeWidgetBounds(
                                     component, component);
                         }
                 
                     });
         }
         if (log.isDebugEnabled()) {
             log.debug("Scrolling rectangle to visible: " + rectangle); //$NON-NLS-1$
         }
         Scroller scroller = new Scroller(component);
         scroller.scrollRectToVisible(rectangle);
     }
 
     /**
      * {@inheritDoc}
      */
     public void scrollToVisible(Object graphicsComponent, Object constraints)
         throws RobotException {
         
         if (graphicsComponent instanceof Control) {
             ensureComponentVisible((Control)graphicsComponent,
                     (Rectangle)constraints);
         }
     }
 
 
     
     /**
      * {@inheritDoc}
      */
     public void activateApplication(String method) throws RobotException {
         try {
             final Shell window = getActiveWindow();
             if (window == null) {
                 throw new RobotException("No AUT window is available.", //$NON-NLS-1$
                         EventFactory.createImplClassErrorEvent());
             }
             WindowActivationMethod wam =
                 WindowActivationMethod.createWindowActivationMethod(
                     method, m_robot, m_queuer);
             wam.activate(window);
             
             // Verify that window was successfully activated
             Shell activeWindow = (Shell)m_queuer.invokeAndWait(
                 "getActiveWindow", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         return window.getDisplay().getActiveShell();
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
      * Guesses the active window. If windows exist but are not active, one of 
      * the existing windows will be returned. 
      * Returns <code>null</code> if no windows exist for the AUT.
      * @return the active window
      */
     public Shell getActiveWindow() {
         return (Shell)m_queuer.invokeAndWait(
             "getActiveWindow", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     Display autDisplay = m_autServer.getAutDisplay();
                     Shell activeShell = autDisplay.getActiveShell();
                     if (activeShell == null) {
                         Shell [] existingShells = autDisplay.getShells();
                         for (int i = 0; i < existingShells.length; i++) {
                             Shell shell = existingShells[i];
                             if (shell.isVisible()) {
                                 activeShell = shell;
                                 break;
                             }
                         }
                         if (activeShell == null && existingShells.length > 0) {
                             activeShell = existingShells[0];
                         }
                     }
                     return activeShell;
                 }
             });
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyToggle(Object obj, int key, boolean activated) 
         throws OsNotSupportedException {
         
         if (activated && isActivated(key)
             || !activated && !isActivated(key)) {
             return;
         }
         List keyTyperList = new LinkedList();
         keyTyperList.add(new KeyCodeTyper(key));
         m_robot.setAutoWaitForIdle(true);
         final Set alreadyDown = new HashSet();
         final ListIterator i = keyTyperList.listIterator();
         final InterceptorOptions options = new InterceptorOptions(
             new long[]{SWT.KeyUp});
         try {
             while (i.hasNext()) {
                 m_queuer.invokeAndWait(this.getClass().getName() + ".type", //$NON-NLS-1$
                     new IRunnable() {
                             public Object run() { // SYNCH THREAD START
                                 AbstractKeyTyper keyTyper = 
                                     (AbstractKeyTyper)i.next();
                                 if (log.isDebugEnabled()) {
                                     log.debug("trying to press: " + keyTyper); //$NON-NLS-1$
                                 }
                                 if (!alreadyDown.contains(keyTyper)) {
                                     if (log.isDebugEnabled()) {
                                         log.debug("pressing: " + keyTyper); //$NON-NLS-1$
                                     }
                                     post(keyTyper.createKeyDownEvent());
                                     alreadyDown.add(keyTyper);
                                 }
                                 return null;
                             }
                         }
                 );
                 TimeUtil.delay(20);
             }
         } catch (IllegalArgumentException e) {
             throw new RobotException(e);
         } finally {
             releaseKeys(options, alreadyDown, i); 
         }
     }
     
     /**
      * @param key the key
      * @return true, if key is already activated
      */
     private boolean isActivated(int key) 
         throws OsNotSupportedException {
         
         if (SWT.getPlatform().equals("win32")) { //$NON-NLS-1$
             try {
                 return isActivatedWIN(key);
             } catch (Exception e) {
                 log.error(e);
             }
         } else if (SWT.getPlatform().equals("motif")) { //$NON-NLS-1$
             return isActivatedMOTIF(key);
         } else if (SWT.getPlatform().equals("gtk")) { //$NON-NLS-1$
             return isActivatedGTK(key);
         }
         throw new OsNotSupportedException("Current os \"" //$NON-NLS-1$
             + SWT.getPlatform() + "\" is not supported.", //$NON-NLS-1$
             MessageIDs.E_UNSUPPORTED_OS);
     }
 
     /**
      * @param key the key to toggle
      * @return true, if <b>windows</b> key is already locked
      */
     private boolean isActivatedWIN(int key) 
         throws ClassNotFoundException, IllegalArgumentException, 
         SecurityException, IllegalAccessException, NoSuchFieldException, 
         NoSuchMethodException, InvocationTargetException {
         
         // Use reflection so that this compiles on all platforms
         String className = "org.eclipse.swt.internal.win32.OS"; //$NON-NLS-1$
         Class clazz = Class.forName(className);
         int osSpecificKey = -1;
         switch (key) {
             case SWT.NUM_LOCK : 
                 osSpecificKey = 
                     clazz.getDeclaredField("VK_NUMLOCK").getInt(null); //$NON-NLS-1$
                 break;
             case SWT.CAPS_LOCK : 
                 osSpecificKey = 
                     clazz.getDeclaredField("VK_CAPITAL").getInt(null); //$NON-NLS-1$
                 break;
             case SWT.SCROLL_LOCK : 
                 osSpecificKey = 
                     clazz.getDeclaredField("VK_SCROLL").getInt(null); //$NON-NLS-1$
                 break;
             default : 
                 break;
         }
 
         // Call org.eclipse.swt.internal.win32.OS.GetKeyState
         String methodName = "GetKeyState"; //$NON-NLS-1$
         Class [] params = new Class [] {
             Integer.TYPE,
         };
         Object [] args = new Object [] {
             new Integer(osSpecificKey),
         };
         Method method = clazz.getMethod(methodName, params);
         short keyState = ((Short)method.invoke(clazz, args)).shortValue();
 
         if (keyState == 1) {
             return true;
         }
         return false;
     }
     
     /**
      * @param key the key to toggle
      * @return true, if <b>gtk</b> key is already locked
      */
     private boolean isActivatedGTK(int key) {
         switch (key) {
             default:
         }
         throw new UnsupportedOperationException("OS.GetKeyState"); //$NON-NLS-1$
     }
     
     /**
      * @param key the key to toggle
      * @return true, if <b>motif</b> key is already locked
      */
     private boolean isActivatedMOTIF(int key) {
         switch (key) {
             default:
         }
         throw new UnsupportedOperationException("OS.GetKeyState"); //$NON-NLS-1$
     }
 
     /**
      * Gets the InputEvent-ButtonMask of the given mouse button number
      * @param button the button number
      * @return the InputEvent button mask
      */
     private int getButtonMask(int button) {
         switch (button) {
             case InputConstants.MOUSE_BUTTON_LEFT:
                 return SWT.BUTTON1;
             case InputConstants.MOUSE_BUTTON_MIDDLE:
                 return SWT.BUTTON2;
             case InputConstants.MOUSE_BUTTON_RIGHT:
                 return SWT.BUTTON3;
             default:
                 throw new RobotException("unsupported mouse button", null); //$NON-NLS-1$
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void clickAtCurrentPosition(Object graphicsComponent, 
             int clickCount, int button) {
         
         clickImpl(graphicsComponent, 
             ClickOptions.create().setClickCount(clickCount)
                 .setMouseButton(button));
     }
 
     /**
      * {@inheritDoc}
      */
     public java.awt.Point getCurrentMousePosition() {
         java.awt.Point currentPos = (java.awt.Point)m_queuer.invokeAndWait(
             "getCursorPosition", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     Display d = (m_autServer).getAutDisplay();
                     final Point cursorLocation = d.getCursorLocation();
                     return new java.awt.Point(cursorLocation.x, 
                         cursorLocation.y);
                 }
             });
         
         return currentPos;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public boolean isMouseInComponent(Object graphicsComponent) {
         
         final Widget comp = (Widget)graphicsComponent;
         final java.awt.Point currentMousePosition = getCurrentMousePosition();
         final Point currMousePos = new Point(currentMousePosition.x,
                 currentMousePosition.y);
         final Rectangle bounds = (Rectangle)m_queuer.invokeAndWait(
             "getBounds", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return SwtUtils.getWidgetBounds(comp);
                 }
 
             });
         Point treeLocUpperLeft = new Point(bounds.x, bounds.y);
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
      * @param options options
      * @param alreadyDown set of pressed keys
      * @param i ListIterator
      */
     private void releaseKeys(final InterceptorOptions options,
         final Set alreadyDown, final ListIterator i) {
         
         // Release all keys in reverse order.
         final Set alreadyUp = new HashSet();
         try {
             while (i.hasPrevious()) {
                 IRobotEventConfirmer confirmer = m_interceptor
                     .intercept(options);
                 m_queuer.invokeAndWait(this.getClass().getName() + ".type", //$NON-NLS-1$
                     new IRunnable() {
                             public Object run() { // SYNCH THREAD START
                                 AbstractKeyTyper keyTyper = 
                                     (AbstractKeyTyper)i.previous();
                                 if (log.isDebugEnabled()) {
                                     log.debug("trying to release: " + keyTyper); //$NON-NLS-1$
                                 }
                                 if (alreadyDown.contains(keyTyper)
                                     && !alreadyUp.contains(keyTyper)) {
                                     
                                     if (log.isDebugEnabled()) {
                                         log.debug("releasing: " + keyTyper); //$NON-NLS-1$
                                     }
                                     
                                     post(keyTyper.createKeyUpEvent());
                                     alreadyUp.add(keyTyper);
                                 }
                                 return null;
                             }
                         }
                 );
                 confirmer.waitToConfirm(null, 
                     new KeySwtEventMatcher(SWT.KeyUp));
             }
         } catch (RobotException e) {
             log.error("error releasing keys", e); //$NON-NLS-1$
             if (!i.hasPrevious()) {
                 throw e;
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getSystemModifierSpec() {
         String keyStrokeSpec = CompSystemConstants.MODIFIER_CONTROL;
         if (SWT.MOD1 == SWT.COMMAND) {
             keyStrokeSpec = CompSystemConstants.MODIFIER_META;
         } else if (SWT.MOD1 == SWT.ALT) {
             keyStrokeSpec = CompSystemConstants.MODIFIER_ALT;
         }
         return keyStrokeSpec; 
     }
 
     /**
      * Gets the absolute bounds of the given Widget.
      * This method runs in the GUI-Thread.
      * @param component a Widget
      * @return the bounds
      * @see SwtUtils#getBounds(Control)  
      */
     private Rectangle getBounds(final Widget component) {
         return (Rectangle)m_queuer.invokeAndWait("getBounds", new IRunnable() { //$NON-NLS-1$
             public Object run() throws StepExecutionException {
                 return SwtUtils.getWidgetBounds(component);
             }
         });
     }
     
     /**
      * Posts a Key-Press-event with the given modifier and the given character.
      * @param graphicsComponent a graphicsComponent.
      * @param modifier The modifier (e.g. SWT.SHIFT, SWT.CTRL, etc.)
      * @param character a character to press.
      * @return true if the event could be posted false otherwise.
      */
     private boolean postKeyPress(Object graphicsComponent, int modifier, 
         char character) {
         
         final Event event = createUntypedEvent((Widget)graphicsComponent, 
             modifier, character);
         event.type = SWT.KeyDown;
         return post(event);
     }
     
     /**
      * Posts a Key-Release-event with the given modifier and the given character.
      * @param graphicsComponent a graphicsComponent.
      * @param modifier The modifier (e.g. SWT.SHIFT, SWT.CTRL, etc.)
      * @param character a character to release.
      * @return true if the event could be posted false otherwise.
      */
     private boolean postKeyRelease(Object graphicsComponent, int modifier, 
         char character) {
         
         final Event event = createUntypedEvent((Widget)graphicsComponent,
             modifier, character);
         event.type = SWT.KeyUp;
         return post(event);
     }
     
     
     /**
      * Creates an Event with the given parameters without an {@link Event#type}.
      * @param graphicsComponent a Widget
      * @param modifier a modifier, e.g. a SWT-constant (SWT.CTRL, etc.)
      * @param character a char value
      * @return an Event.
      */
     private Event createUntypedEvent(Widget graphicsComponent, int modifier, 
         char character) {
         final Event event = new Event();
         event.keyCode = modifier;
         event.character = character;
         event.widget = graphicsComponent;
         return event;
     }
     
     /**
      * Posts an {@link Event} on the UI input queue.
      * 
      * @param event the {@link Event} to post
      * @return true if the event could be posted false otherwise.
      * @see Display#post(Event)
      */
     private boolean post(Event event) {
         try {
             final Display display = m_autServer.getAutDisplay();
             if (!display.post(event)) {
                 final String msg = "posting event failed: " + event; //$NON-NLS-1$
                 log.error(msg); 
                 return false;
             }
         } catch (SWTError swte) {
             log.error(swte);
             throw new RobotException(swte);
         }
         return true;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void mousePress(final Object graphicsComponent, Object constraints,
             final int button) throws RobotException {
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
         
         RobotTiming.sleepPreClickDelay();
         
         m_robot.mousePress(getButtonMask(button));
     }
 
     
 
     /**
      * {@inheritDoc}
      */
     public void mouseRelease(final Object graphicsComponent, Object constraints,
             final int button) throws RobotException {
 
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
 
         RobotTiming.sleepPreClickDelay();
         
         m_robot.mouseRelease(getButtonMask(button));
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
             final InterceptorOptions options = new InterceptorOptions(
                     new long[]{SWT.MouseUp, SWT.MouseDown});
             final IEventMatcher matcher = 
                 new ClickSwtEventMatcher(clickOptions);
             final IRobotEventConfirmer confirmer = m_interceptor
                 .intercept(options);
             try {
                 pressModifier(modifierMask);
                RobotTiming.sleepPreClickDelay();
                 
                 for (int i = 0; i < clickCount; i++) {
                     m_robot.mousePress(buttonMask);
                     RobotTiming.sleepPostMouseDownDelay();
                     m_robot.mouseRelease(buttonMask);
                     RobotTiming.sleepPostMouseUpDelay();
                 }
                 if (clickOptions.isConfirmClick()) {
                     try {
                         confirmer.waitToConfirm(graphicsComponent, matcher);
                     } catch (RobotException re) {
                         StringBuffer sb = new StringBuffer(
                             "Robot exception occurred while clicking...\n"); //$NON-NLS-1$
                         logRobotException(graphicsComponent, re, sb);
                         throw re;
                     }
                 }
             } finally {
                 releaseModifier(modifierMask);
             }
         }
     }
 
     /**
      * @param clickModifier
      *            the click modifier to use for this click
      * @return an array of modifiers to press before click and release after
      *         click
      */
     private int[] getModifierMask(ClickModifier clickModifier) {
         int[] modifier = new int[0];
         if (clickModifier.hasModifiers(ClickModifier.M1)) {
             modifier = ArrayUtils.add(modifier, SwtUtils
                     .getSystemDefaultModifier());
         }
         if (clickModifier.hasModifiers(ClickModifier.M2)) {
             modifier = ArrayUtils.add(modifier, SwtUtils.getSystemModifier2());
         }
         if (clickModifier.hasModifiers(ClickModifier.M3)) {
             modifier = ArrayUtils.add(modifier, SwtUtils.getSystemModifier3());
         }
         if (clickModifier.hasModifiers(ClickModifier.M4)) {
             modifier = ArrayUtils.add(modifier, SwtUtils.getSystemModifier4());
         }
         return modifier;
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
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions, int xPos, boolean xAbsolute,
             int yPos, boolean yAbsolute) throws RobotException {
 
         clickImpl(graphicsComponent, constraints, clickOptions, 
                 xPos, xAbsolute, yPos, yAbsolute);
     }
 
 }

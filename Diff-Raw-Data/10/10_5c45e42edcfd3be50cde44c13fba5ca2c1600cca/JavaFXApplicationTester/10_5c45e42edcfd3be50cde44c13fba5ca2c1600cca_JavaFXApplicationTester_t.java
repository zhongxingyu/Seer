 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.javafx.tester;
 
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.EventHandler;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.tester.AbstractApplicationTester;
 import org.eclipse.jubula.rc.common.util.KeyStrokeUtil;
 import org.eclipse.jubula.rc.common.util.MatchUtil;
 import org.eclipse.jubula.rc.common.util.Verifier;
 import org.eclipse.jubula.rc.javafx.components.CurrentStages;
 import org.eclipse.jubula.rc.javafx.driver.EventThreadQueuerJavaFXImpl;
 import org.eclipse.jubula.rc.javafx.driver.RobotJavaFXImpl;
 import org.eclipse.jubula.rc.javafx.listener.ComponentHandler;
 import org.eclipse.jubula.rc.javafx.util.Rounding;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 
 /**
  * Tester-Class for the Application as a whole.
  *
  * @author BREDEX GmbH
  * @created 30.10.2013
  */
 public class JavaFXApplicationTester extends AbstractApplicationTester {
     /**
      * The logging.
      */
     private static AutServerLogger log = new AutServerLogger(
             JavaFXApplicationTester.class);
 
     /**
      * constructor to add the stage which has focus to our event confirming mechanism
      */
     public JavaFXApplicationTester() {
         RobotJavaFXImpl robot = ((RobotJavaFXImpl) getRobot());
         Stage focusStage = CurrentStages.getfocusStage();
         robot.getInterceptor().addSceneGraph(
                 focusStage.getScene().windowProperty());
     }
     
     @Override
     public String[] getTextArrayFromComponent() {
         return null;
     }
 
     @Override
     public Rectangle getActiveWindowBounds() {
         Stage window = CurrentStages.getfocusStage();
         Rectangle rec = new Rectangle(Rounding.round(window.getX()),
                 Rounding.round(window.getY()),
                 Rounding.round(window.getWidth()), Rounding.round(window
                         .getHeight()));
 
         return rec;
     }
 
     @Override
     protected IRobot getRobot() {
         return AUTServer.getInstance().getRobot();
     }
 
     /**
      * perform a keystroke specified according <a
      * href=http://java.sun.com/j2se/1.4
      * .2/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)>
      * string representation of a keystroke </a>,
      *
      * @param modifierSpec
      *            the string representation of the modifiers
      * @param keySpec
      *            the string representation of the key
      */
     @Override
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
 
     @Override
     protected Object getFocusOwner() {
         Object result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "getFocusOwner", new Callable<Object>() { //$NON-NLS-1$
 
                     @Override
                     public Object call() throws Exception {
                         Stage s = (Stage) getActiveWindow();
                         return s.getScene().getFocusOwner();
                     }
                 });
         return result;
     }
 
     @Override
     protected int getEventCode(int key) {
         int event = 0;
         switch (key) {
             case 1:
                 event = KeyEvent.VK_NUM_LOCK;
                 break;
             case 2:
                 event = KeyEvent.VK_CAPS_LOCK;
                 break;
             case 3:
                 event = KeyEvent.VK_SCROLL_LOCK;
                 break;
             default:
                 break;
         }
         return event;
     }
 
     @Override
     protected Object getActiveWindow() {
         return CurrentStages.getfocusStage();
     }
 
     /**
      * @param keyCodeName
      *            The name of a key code, e.g. <code>TAB</code> for a tabulator
      *            key code
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
             throw new StepExecutionException(e.getMessage(),
                     EventFactory.createActionError());
         } catch (SecurityException e) {
             throw new StepExecutionException(e.getMessage(),
                     EventFactory.createActionError());
         } catch (IllegalAccessException e) {
             throw new StepExecutionException(e.getMessage(),
                     EventFactory.createActionError());
         } catch (NoSuchFieldException e) {
             if (log.isInfoEnabled()) {
                 log.info("The key expression '" + keyCodeName //$NON-NLS-1$
                         + "' is not a key code, typed as key stroke instead"); //$NON-NLS-1$
             }
         }
         return code;
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
         Verifier.equals(exists, isStageInHierarchy(title, operator));
     }
 
     /**
      * Checks if the Window(Stage) is in in the Hierarchy and therefore open and
      * accessible.
      *
      * @param title
      *            the title of the Stage to look for
      * @param operator
      *            the operator
      * @return true if the Stage is open, otherwise false
      */
     private boolean isStageInHierarchy(final String title,
             final String operator) {
         // We are doing this on the JavaFX thread to avoid concurrent
         // modification in the hierarchy map.
         boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "isWindowInHierarchy", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         Stage stage = getStageByTitle(title, operator);
                         if (stage != null) {
                             return true;
                         }
                         return false;
                     }
                 });
         return result;
     }
 
     /**
      * Looks through the Hierarchy for a Stage with a given Title.
      *
      * @param title
      *            the Title of the Stage to look for
      * @param operator
      *            the operator
      * @return the Stage or null
      */
     private Stage getStageByTitle(final String title, final String operator) {
         Stage result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "getStageByTitle", new Callable<Stage>() { //$NON-NLS-1$
 
                     @Override
                     public Stage call() throws Exception {
                         List<? extends Stage> stages = ComponentHandler
                                 .getAssignableFrom(Stage.class);
                         for (final Stage stage : stages) {
                             if (MatchUtil.getInstance().match(stage.getTitle(),
                                     title, operator)) {
                                 return stage;
                             }
                         }
                         return null;
                     }
                 });
         return result;
     }
 
     /**
      * Waits <code>timeMillSec</code> if the application opens a window with the
      * given title.
      *
      * @param title
      *            the title
      * @param operator
      *            the comparing operator
      * @param pTimeout
      *            the time in ms
      * @param delay
      *            delay after the window is shown
      */
     public void rcWaitForWindow(final String title, String operator,
             int pTimeout, int delay) {
         final Stage s = getStageByTitle(title, operator);
 
         if (s == null) {
             log.error("no Window found! In rcWaitForWindowActivation. Title: " //$NON-NLS-1$
                     + title + "operator: " + operator); //$NON-NLS-1$
             throw new StepExecutionException("no Window found!", //$NON-NLS-1$
                     EventFactory
                             .createActionError(TestErrorEvent.COMP_NOT_FOUND));
         }
 
         final CountDownLatch signal = new CountDownLatch(1);
         final EventHandler<WindowEvent> showHandler =
                 new EventHandler<WindowEvent>() {
 
                 @Override
                 public void handle(WindowEvent event) {
                     signal.countDown();
                 }
             };
 
         boolean isShowing = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "rcWaitForWindow", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         if (!s.isShowing()) {
                             s.addEventFilter(WindowEvent.WINDOW_SHOWN,
                                     showHandler);
                             return false;
                         }
                         return true;
                     }
                 });
         if (!isShowing) {
             try {
                 signal.await(pTimeout, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 throw new StepExecutionException(
                         "Interrupted while waiting for window!", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(TestErrorEvent.
                                         EXECUTION_ERROR));
             } finally {
                 s.removeEventFilter(WindowEvent.WINDOW_SHOWN, showHandler);
             }
             boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                     "rcWaitForWindowConfirm", new Callable<Boolean>() { //$NON-NLS-1$
 
                         @Override
                         public Boolean call() throws Exception {
                             return s.isShowing();
                         }
                     });
             if (!result) {
                 throw new StepExecutionException("window did not open", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(
                                         TestErrorEvent.TIMEOUT_EXPIRED));
             }
         }
         TimeUtil.delay(delay);
     }
 
     /**
      * Waits <code>timeMillSec</code> if the application activates a window with
      * the given title.
      *
      * @param title
      *            the title
      * @param operator
      *            the comparing operator
      * @param pTimeout
      *            the time in ms
      * @param delay
      *            delay after the window is activated
      */
     public void rcWaitForWindowActivation(final String title, String operator,
             int pTimeout, int delay) {
         final Stage s = getStageByTitle(title, operator);
 
         if (s == null) {
             log.error("no Window found! In rcWaitForWindowActivation. Title: " //$NON-NLS-1$
                     + title + "operator: " + operator); //$NON-NLS-1$
             throw new StepExecutionException("no Window found!", //$NON-NLS-1$
                     EventFactory
                             .createActionError(TestErrorEvent.EXECUTION_ERROR));
         }
 
         final CountDownLatch signal = new CountDownLatch(1);
         final ChangeListener<Boolean> focusListener =
                 new ChangeListener<Boolean>() {
 
                 @Override
                 public void changed(ObservableValue<? extends Boolean>
                         observable, Boolean oldValue, Boolean newValue) {
                     if (newValue) {
                         signal.countDown();
                     }
                 }
             };
 
         boolean isFocused = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "rcWaitForWindowActivation", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         if (!s.isFocused()) {
                             s.focusedProperty().addListener(focusListener);
                             return false;
                         }
                         return true;
                     }
                 });
         if (isFocused) {
             try {
                 signal.await(pTimeout, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 throw new StepExecutionException(
                         "Interrupted while waiting for window activation!", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(TestErrorEvent.
                                         EXECUTION_ERROR));
             } finally {
                 s.focusedProperty().removeListener(focusListener);
             }
             boolean result = EventThreadQueuerJavaFXImpl
                     .invokeAndWait(
                             "rcWaitForWindowActivationConfirm", new Callable<Boolean>() { //$NON-NLS-1$
 
                                 @Override
                                 public Boolean call() throws Exception {
                                     return s.isFocused();
                                 }
                             });
             if (!result) {
                 throw new StepExecutionException("window was not activated", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(
                                         TestErrorEvent.TIMEOUT_EXPIRED));
             }
         }
         TimeUtil.delay(delay);
     }
 
     /**
      * Waits <code>timeMillSec</code> if the application closes (or hides) a
      * window with the given title. If no window with the given title can be
      * found, then it is assumed that the window has already closed.
      *
      * @param title
      *            the title
      * @param operator
      *            the comparing operator
      * @param pTimeout
      *            the time in ms
      * @param delay
      *            delay after the window is closed
      */
     public void rcWaitForWindowToClose(final String title,
             final String operator, int pTimeout, int delay) {
 
         final Stage s = getStageByTitle(title, operator);
 
         if (s == null) {
             return;
         }
 
         final CountDownLatch signal = new CountDownLatch(1);
         final EventHandler<WindowEvent> closeHandler =
                 new EventHandler<WindowEvent>() {
 
                 @Override
                 public void handle(WindowEvent event) {
                     signal.countDown();
                 }
             };
 
         boolean isClosing = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "rcWaitForWindowToClose", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         s.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST,
                                 closeHandler);
                         return false;
                     }
                 });
         if (isClosing) {
             try {
                 signal.await(pTimeout, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 throw new StepExecutionException(
                         "Interrupted while waiting for window closing!", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(
                                         TestErrorEvent.EXECUTION_ERROR));
             } finally {
                 s.removeEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST,
                         closeHandler);
             }
             boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                     "rcWaitForWindowToCloseConfirm", new Callable<Boolean>() { //$NON-NLS-1$
 
                         @Override
                         public Boolean call() throws Exception {
                             final Stage tmpS = getStageByTitle(title, operator);
 
                             if (tmpS == null) {
                                 return true;
                             }
                             return false;
                         }
                     });
             if (!result) {
                 throw new StepExecutionException("window was not closed", //$NON-NLS-1$
                         EventFactory
                                 .createActionError(
                                         TestErrorEvent.TIMEOUT_EXPIRED));
             }
         }
         TimeUtil.delay(delay);
     }
    
    @Override
    public void rcSyncShutdownAndRestart(int timeout) {
        StepExecutionException.throwUnsupportedAction();
    }
    
    @Override
    public void rcPrepareForShutdown() {
        StepExecutionException.throwUnsupportedAction();
    }
 }

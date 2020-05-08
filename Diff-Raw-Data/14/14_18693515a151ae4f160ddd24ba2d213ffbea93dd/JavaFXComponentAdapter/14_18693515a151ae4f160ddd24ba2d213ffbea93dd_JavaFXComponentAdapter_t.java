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
 package org.eclipse.jubula.rc.javafx.tester.adapter;
 
 import java.awt.event.KeyEvent;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.concurrent.Callable;
 
 import javafx.beans.property.ReadOnlyObjectProperty;
 import javafx.event.EventHandler;
 import javafx.scene.Node;
 import javafx.scene.control.ContextMenu;
 import javafx.scene.control.Control;
 import javafx.stage.Window;
 import javafx.stage.WindowEvent;
 
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.listener.EventLock;
 import org.eclipse.jubula.rc.common.tester.AbstractMenuTester;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IWidgetComponent;
 import org.eclipse.jubula.rc.common.util.KeyStrokeUtil;
 import org.eclipse.jubula.rc.javafx.driver.EventThreadQueuerJavaFXImpl;
 import org.eclipse.jubula.rc.javafx.tester.MenuTester;
 import org.eclipse.jubula.tools.constants.TimeoutConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 
 /**
  * Implements the interface for widgets and supports basic methods which are
  * needed for nearly all JavaFX UI components.
  * 
  * @param <T>
  *            type of the Component
  * 
  * @author BREDEX GmbH
  * @created 30.10.2013
  */
 public class JavaFXComponentAdapter<T extends Node> extends
         AbstractComponentAdapter<T> implements IWidgetComponent {
 
     /**
      * The Converter Map.
      */
     private static Map<String, Integer> converterTable = null;
 
     static {
         converterTable = new HashMap<String, Integer>();
         converterTable.put(CompSystemConstants.MODIFIER_NONE, new Integer(-1));
         converterTable.put(CompSystemConstants.MODIFIER_SHIFT, new Integer(
                 KeyEvent.VK_SHIFT));
         converterTable.put(CompSystemConstants.MODIFIER_CONTROL, new Integer(
                 KeyEvent.VK_CONTROL));
         converterTable.put(CompSystemConstants.MODIFIER_ALT, new Integer(
                 KeyEvent.VK_ALT));
         converterTable.put(CompSystemConstants.MODIFIER_META, new Integer(
                 KeyEvent.VK_META));
         converterTable.put(CompSystemConstants.MODIFIER_CMD, new Integer(
                 KeyEvent.VK_META));
         converterTable.put(CompSystemConstants.MODIFIER_MOD, new Integer(
                 KeyEvent.VK_CONTROL));
     }
 
     /**
      * Used to store the component into the adapter.
      * 
      * @param objectToAdapt
      *            the object to adapt
      */
     public JavaFXComponentAdapter(T objectToAdapt) {
         super(objectToAdapt);
     }
 
     @Override
     public boolean isShowing() {
         boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "isShowing", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         return getRealComponent().isVisible();
                     }
                 });
 
         return result;
     }
 
     @Override
     public boolean isEnabled() {
         boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "isEnabled", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         // because the logic in JavaFX
                         // is switched the return value is inverted
                         return !(getRealComponent().isDisabled());
                     }
                 });
         return result;
     }
 
     @Override
     public boolean hasFocus() {
         boolean result = EventThreadQueuerJavaFXImpl.invokeAndWait(
                 "hasFocus", new Callable<Boolean>() { //$NON-NLS-1$
 
                     @Override
                     public Boolean call() throws Exception {
                         return getRealComponent().isFocused();
                     }
                 });
         return result;
     }
 
     @Override
     public String getPropteryValue(final String propertyname) {
         Object prop = EventThreadQueuerJavaFXImpl.invokeAndWait("getProperty", //$NON-NLS-1$
                 new Callable<String>() {
 
                     @Override
                     public String call() throws Exception {
                         try {
                             return getRobot().getPropertyValue(
                                     getRealComponent(), propertyname);
                         } catch (RobotException e) {
                             throw new StepExecutionException(
                                     e.getMessage(),
                                     EventFactory
                                             .createActionError(TestErrorEvent.
                                                     PROPERTY_NOT_ACCESSABLE));
                         }
                     }
                 });
         return String.valueOf(prop);
     }
 
     @Override
     public AbstractMenuTester showPopup(int xPos, String xUnits, int yPos,
             String yUnits, int button) throws StepExecutionException {
         Node n = getRealComponent();
         if (n instanceof Control && ((Control) n).getContextMenu() != null) {
             Control comp = (Control) n;
             final EventLock event = new EventLock();
             ContextMenu cotxMenu = comp.getContextMenu();
             EventHandler<WindowEvent> filter = new EventHandler<WindowEvent>() {
 
                 @Override
                 public void handle(WindowEvent e) {
                     synchronized (event) {
                         event.notifyAll();
                     }
                 }
             };
             cotxMenu.addEventFilter(WindowEvent.WINDOW_SHOWN, filter);
             // RobotTiming.sleepPreShowPopupDelay();
             boolean isAbsoluteUnitsX = CompSystemConstants.POS_UNIT_PIXEL
                     .equalsIgnoreCase(xUnits);
             boolean isAbsoluteUnitsY = CompSystemConstants.POS_UNIT_PIXEL
                     .equalsIgnoreCase(yUnits);
             getRobot().click(
                     comp,
                     null,
                     ClickOptions.create().setClickCount(1)
                             .setMouseButton(button), xPos, isAbsoluteUnitsX,
                     yPos, isAbsoluteUnitsY);
 
             if (!comp.getContextMenu().isShowing()) {
                 try {
                     synchronized (event) {
                         event.wait(TimeoutConstants.
                                 SERVER_TIMEOUT_WAIT_FOR_POPUP);
                     }
                 } catch (InterruptedException e) {
                     // ignore
                 } finally {
                     cotxMenu.removeEventFilter(WindowEvent.
                             WINDOW_SHOWN, filter);
                 }
 
             }
             if (comp.getContextMenu().isShowing()) {
                 MenuTester tester = new MenuTester();
                 tester.setComponent(cotxMenu);
                 return tester;
             }
             throw new StepExecutionException("Popup could not be opened", //$NON-NLS-1$
                     EventFactory
                             .createActionError(TestErrorEvent.POPUP_NOT_FOUND));
 
         }
         throw new StepExecutionException("Component has no Popup", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.POPUP_NOT_FOUND));
     }
 
     @Override
     public AbstractMenuTester showPopup(int button) {
         return showPopup(50, CompSystemConstants.POS_UNIT_PERCENT, 50,
                 CompSystemConstants.POS_UNIT_PERCENT, button);
     }
 
     @Override
     public void showToolTip(String text, int textSize, int timePerWord,
             int windowWidth) {
        StepExecutionException.throwUnsupportedAction();
     }
 
     @Override
     public void rcDrag(int mouseButton, String modifier, int xPos,
             String xUnits, int yPos, String yUnits) {
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         final IRobot robot = getRobot();
         clickDirect(0, mouseButton, xPos, xUnits, yPos, yUnits);
         pressOrReleaseModifiers(modifier, true);
         robot.mousePress(null, null, mouseButton);
     }
 
     @Override
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
             pressOrReleaseModifiers(modifier, false);
         }
     }
 
     /**
      * Presses or releases the given modifier.
      * 
      * @param modifier
      *            the modifier.
      * @param press
      *            if true, the modifier will be pressed. if false, the modifier
      *            will be released.
      */
     private void pressOrReleaseModifiers(String modifier, boolean press) {
         final IRobot robot = getRobot();
         final StringTokenizer modTok = new StringTokenizer(
                 KeyStrokeUtil.getModifierString(modifier), " "); //$NON-NLS-1$
         while (modTok.hasMoreTokens()) {
             final String mod = modTok.nextToken();
             final int keyCode = getKeyCode(mod);
             if (press) {
                 robot.keyPress(null, keyCode);
             } else {
                 robot.keyRelease(null, keyCode);
             }
         }
     }
 
     /**
      * clicks into the component.
      * 
      * @param count
      *            amount of clicks
      * @param button
      *            what mouse button should be used
      * @param xPos
      *            what x position
      * @param xUnits
      *            should x position be pixel or percent values
      * @param yPos
      *            what y position
      * @param yUnits
      *            should y position be pixel or percent values
      * @throws StepExecutionException
      *             error
      */
     private void clickDirect(int count, int button, int xPos, String xUnits,
             int yPos, String yUnits) throws StepExecutionException {
 
         getRobot().click(
                 getRealComponent(),
                 null,
                 ClickOptions.create().setClickCount(count)
                         .setMouseButton(button), xPos,
                 xUnits.equalsIgnoreCase(CompSystemConstants.POS_UNIT_PIXEL),
                 yPos,
                 yUnits.equalsIgnoreCase(CompSystemConstants.POS_UNIT_PIXEL));
     }
 
     @Override
     public int getKeyCode(String key) {
         if (key == null) {
             throw new RobotException("Key is null!", //$NON-NLS-1$
                     EventFactory.createConfigErrorEvent());
         }
         final Integer keyCode = (Integer) converterTable.get(key.toLowerCase());
         if (keyCode == null) {
             throw new RobotException("No KeyCode found for key '" + key + "'", //$NON-NLS-1$//$NON-NLS-2$
                     EventFactory.createConfigErrorEvent());
         }
         return keyCode.intValue();
     }
 
     @Override
     public ReadOnlyObjectProperty<Window> getWindow() {
         return getRealComponent().getScene().windowProperty();
     }
 }

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
 package org.eclipse.jubula.rc.swt.implclasses;
 
 import java.awt.Point;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.ClickOptions.ClickModifier;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.exception.StepVerifyFailedException;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.swt.driver.DragAndDropHelperSwt;
 import org.eclipse.jubula.rc.swt.driver.SwtRobot;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * @author BREDEX GmbH
  * @created 19.04.2006
  */
 public abstract class AbstractControlImplClass extends AbstractSwtImplClass {
     
     /** number of pixels by which a "mouse shake" offsets the mouse cursor */
     private static final int MOUSE_SHAKE_OFFSET = 10;
     
     /**
      * Default constructor
      */
     public AbstractControlImplClass() {
         // nothing
     }
     
     /**
      * @return The component passed to the implementation class by calling
      *         {@link IImplementationClass#setComponent(Object)}
      */
     public abstract Control getComponent();
    
   
     
     /**
      * dummy method for "wait for component"
      * @param timeout the maximum amount of time to wait for the component
      * @param delay the time to wait after the component is found
      * {@inheritDoc}
      */
     public void gdWaitForComponent (int timeout, int delay) {
         // do NOT delete this method!
         // do nothing, implementation is in class CAPTestCommand.getImplClass
         // because this action needs a special implementation!
     }
     
     
     /**
      * Checks wether two boolean values are equal. The first boolean value is
      * passed to the method, the second one is expected to be returned by the
      * <code>runnable</code>. 
      * @param expected The expected value.
      * @param name The runnable name.
      * @param runnable The runnable.
      * @throws StepVerifyFailedException If the values are not equal.
      */
     protected void verify(boolean expected, String name, IRunnable runnable)
         throws StepVerifyFailedException {
         Boolean actual = (Boolean)getEventThreadQueuer().invokeAndWait(name,
             runnable);
         Verifier.equals(expected, actual.booleanValue());
     }
     
     /**
      * @param renderer The component which is used as the renderer
      * @return The string that the renderer displays.
      * @throws StepExecutionException
      *             If the renderer component is not of type <code>JLabel</code>,
      *             <code>JToggleButton</code>, <code>AbstractButton</code>
      *             or <code>JTextComponent</code>
      */
     String getRenderedTextImpl(Control renderer)
         throws StepExecutionException {
         
         if (renderer instanceof Label) {
             return ((Label)renderer).getText();
         } else if (renderer instanceof Button) {
             return ((Button)renderer).getText();
         } else if (renderer instanceof Text) {
             return ((Text)renderer).getText();
         } else {
             throw new StepExecutionException(
                 "Table cell renderer not supported: " + renderer.getClass(), //$NON-NLS-1$
                 EventFactory.createActionError(
                         TestErrorEvent.RENDERER_NOT_SUPPORTED));
         }
     }
     /**
      * Casts the passed renderer component to a known type and extracts the
      * rendered text.
      * @param renderer The renderer.
      * @param queueInEventThread If <code>true</code>, the text extraction is executed in
      *            the event queue thread.
      * @return The rendered text.
      * @throws StepExecutionException If the passed renderer is not supported. Supported types are
      *             <code>JLabel</code>, <code>JToggleButton</code>,
      *             <code>AbstractButton</code> and <code>JTextComponent</code>
      * 
      */
     protected String getRenderedText(final Control renderer,
         boolean queueInEventThread) throws StepExecutionException {
 
         if (queueInEventThread) {
             return (String)getEventThreadQueuer().invokeAndWait(
                 "getRenderedText", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return getRenderedTextImpl(renderer);
                     }
                 });
         }
         return getRenderedTextImpl(renderer);
     }
     
     /**
      * Verifies the <code>enabled</code> property.
      * @param enabled The <code>enabled</code> property value to verify
      */
     public void gdVerifyEnabled(boolean enabled) {
         verify(enabled, "isEnabled", new IRunnable() { //$NON-NLS-1$
             public Object run() {
                 return getComponent().isEnabled()
                     ? Boolean.TRUE : Boolean.FALSE; // see findBugs
             }
         });
     }
     
     /**
      * Verifies that the component exists and is visible.
      *
      * @param exists
      *            <code>True</code> if the component is expected to exist
      *            and be visible, otherwise <code>false</code>.
      */
     public void gdVerifyExists(boolean exists) {
         /*
          * The actual testing of the component's existence/non-existence is 
          * implemented in CAPTestCommand.getImplClass. This method only checks
          * for visibility.
          */
         verify(exists, "exists", new IRunnable() { //$NON-NLS-1$
             public Object run() {
                 return getComponent().isVisible() 
                     ? Boolean.TRUE : Boolean.FALSE; // see findBugs
             }
         });
     }
 
     /**
      * Verifies if the textfield has the focus.
      * @param hasFocus The hasFocus property to verify.
      */
     public void gdVerifyFocus(boolean hasFocus) {
         verify(hasFocus, "hasFocus", new IRunnable() { //$NON-NLS-1$
             public Object run() {
                 return getComponent().isFocusControl()
                     ? Boolean.TRUE : Boolean.FALSE; // see findBugs
             }
         });
     }
     
     /**
      * Verifies the value of the property with the name <code>name</code>.
      * The name of the property has be specified according to the JavaBean
      * specification. The value returned by the property is converted into a
      * string by calling <code>toString()</code> and is compared to the passed
      * <code>value</code>.
      * 
      * @param name The name of the property
      * @param value The value of the property as a string
      * @param operator The operator used to verify
      */
     public void gdVerifyProperty(final String name, String value, 
             String operator) {
         final Control bean = getComponent();
         Validate.notNull(bean, "Tested component must not be null"); //$NON-NLS-1$
         Object prop = getEventThreadQueuer().invokeAndWait("getProperty",  //$NON-NLS-1$
             new IRunnable() {
 
                 public Object run() throws StepExecutionException {
                     try {
                         return PropertyUtils.getProperty(bean, name);
                     } catch (IllegalAccessException e) {
                         throw new StepExecutionException(
                             e.getMessage(), 
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     } catch (InvocationTargetException e) {
                         throw new StepExecutionException(
                             e.getMessage(), 
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     } catch (NoSuchMethodException e) {
                         throw new StepExecutionException(
                             e.getMessage(), 
                             EventFactory.createActionError(
                                 TestErrorEvent.PROPERTY_NOT_ACCESSABLE));
                     }
                 }
                 
             });
         final String propToStr = String.valueOf(prop);
         Verifier.match(propToStr, value, operator);
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdClick(int count, int button) {
         getRobot().click(
                 getComponent(),
                 null,
                 ClickOptions.create().setClickCount(count).setMouseButton(
                         button));
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdClick(int count) {
         gdClick(count, 1);
     }
     
     /**
      * clicks into a component. 
      * @param count amount of clicks
      * @param button what button should be clicked
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException if step execution fails.
      */
     public void gdClickDirect(int count, int button, int xPos, String xUnits, 
         int yPos, String yUnits) throws StepExecutionException {
         
         clickDirect(count, button, xPos, xUnits, yPos, yUnits, null);
     }
     
     /**
      * clicks into a component. 
      * @param count amount of clicks
      * @param button what button should be clicked
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param constraints constraints
      * @throws StepExecutionException error
      */
     protected void clickDirect(int count, int button, int xPos, String xUnits,
             int yPos, String yUnits, Object constraints)
         throws StepExecutionException {
 
         getRobot().click(
                 getComponent(),
                 constraints,
                 ClickOptions.create().setClickCount(count).setMouseButton(
                         button), xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL),
                 yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
     }
     
     /**
      * @return <code>true</code> if the component is the focusowner
      */
     protected boolean hasFocus() {
         Boolean hasFocus =
             (Boolean)getEventThreadQueuer().invokeAndWait("hasFocus", //$NON-NLS-1$
                 new IRunnable() { 
                     public Object run() {
                         return getComponent().isFocusControl()
                             ? Boolean.TRUE : Boolean.FALSE; // see findBugs
                     }
                 });
         return hasFocus.booleanValue();
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and selects an item at the given position in the popup menu
      * 
      * @param xPos what x position
      * @param yPos what y position
      * @param units should x,y position be pixel or percent values
      * @param indexPath path of item indices
      * @throws StepExecutionException error
      * @deprecated Will be removed with gdPopupSelectByIndexPath with int parameter
      * for MouseButton
      */
     public void gdPopupSelectByIndexPath(int xPos, int yPos, String units,
             String indexPath) throws StepExecutionException {
         gdPopupSelectByIndexPath(xPos, units, yPos, units, indexPath, 3);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and selects an item at the given position in the popup menu
      * 
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param indexPath path of item indices
      * @param button MouseButton
      * @throws StepExecutionException error
      */
     public void gdPopupSelectByIndexPath(int xPos, String xUnits, int yPos, 
             String yUnits, String indexPath, 
             int button) throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         PopupMenuUtil.selectMenuItem(getRobot(), popup, 
             MenuUtil.splitIndexPath(indexPath));
     }
     
     /**
      * Select an item in the popup menu
      * @param indexPath path of item indices
      * @throws StepExecutionException error
      * @deprecated Will be removed with gdPopupSelectByIndexPath with int parameter
      * for MouseButton
      */
     public void gdPopupSelectByIndexPath(String indexPath) 
         throws StepExecutionException {
         gdPopupSelectByIndexPath(indexPath, 3);
     }
     
     /**
      * Select an item in the popup menu
      * @param indexPath path of item indices
      * @param button MouseButton
      * @throws StepExecutionException error
      */
     public void gdPopupSelectByIndexPath(String indexPath, int button) 
         throws StepExecutionException {
         
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         PopupMenuUtil.selectMenuItem(getRobot(), popup, 
             MenuUtil.splitIndexPath(indexPath));
     }
 
     /**
      * Selects an item in the popup menu
      * @param textPath path of item texts 
      * @param operator operator used for matching
      * @throws StepExecutionException error
      * @deprecated Will be removed with gdPopupSelectByTextPath with int parameter
      * for MouseButton
      */
     public void gdPopupSelectByTextPath(String textPath, String operator) 
         throws StepExecutionException {
         gdPopupSelectByTextPath(textPath, operator, 3);
     }
     
     /**
      * Selects an item in the popup menu
      * @param textPath path of item texts 
      * @param operator operator used for matching
      * @param button MouseButton
      * @throws StepExecutionException error
      */
     public void gdPopupSelectByTextPath(String textPath, String operator,
             int button) 
         throws StepExecutionException {
         
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         PopupMenuUtil.selectMenuItem(getRobot(), popup, 
             MenuUtil.splitPath(textPath), operator);
     }
     
     /**
      * Selects an item in the popup menu
      * 
      * @param xPos what x position
      * @param yPos what y position
      * @param units should x,y position be pixel or percent values
      * @param textPath path of item texts
      * @param operator operator used for matching
      * @throws StepExecutionException error
      * @deprecated Will be removed with gdPopupSelectByTextPath with int parameter
      * for MouseButton
      */
     public void gdPopupSelectByTextPath(final int xPos, final int yPos,
             final String units, String textPath, String operator)
         throws StepExecutionException {
         gdPopupSelectByTextPath(xPos, units, 
                 yPos, units, textPath, operator, 3);
     }
     
     /**
      * Selects an item in the popup menu
      * 
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param textPath path of item texts
      * @param operator operator used for matching
      * @param button MouseButton
      * @throws StepExecutionException error
      */
     public void gdPopupSelectByTextPath(final int xPos, final String xUnits, 
             final int yPos, final String yUnits, 
             String textPath, String operator, int button)
         throws StepExecutionException {
         
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(), 
             xPos, xUnits, yPos, yUnits, button);
         PopupMenuUtil.selectMenuItem(getRobot(), popup, 
             MenuUtil.splitPath(textPath), operator);
     }
     
     /**
      * Checks if the specified context menu entry is enabled.
      * @param indexPath the menu item to verify
      * @param enabled for checking enabled or disabled
      * @param button MouseButton
      */
     public void gdPopupVerifyEnabledByIndexPath(String indexPath,
             boolean enabled, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifyEnabledByIndexPath(indexPath, popup, enabled);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry is enabled.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param indexPath the menu item to verify
      * @param enabled for checking enabled or disabled
      * @param button MouseButton
      */
     public void gdPopupVerifyEnabledByIndexPath(int xPos, String xUnits, 
             int yPos, String yUnits, String indexPath, 
             boolean enabled, int button) throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifyEnabledByIndexPath(indexPath, popup, enabled);
     }
     
     /**
      * Checks if the specified context menu entry is enabled.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param enabled for checking enabled or disabled
      * @param button MouseButton
      */
     public void gdPopupVerifyEnabledByTextPath(String textPath,
             String operator, boolean enabled, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifyEnabledByTextPath(textPath, operator, popup, enabled);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry is enabled.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param enabled for checking enabled or disabled
      * @param button MouseButton
      */
     public void gdPopupVerifyEnabledByTextPath(final int xPos, 
             final String xUnits, final int yPos,
             final String yUnits, String textPath, String operator,
                 boolean enabled, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifyEnabledByTextPath(textPath, operator, popup, enabled);
     }
     
     /**
      * Checks if the specified context menu entry exists.
      * @param indexPath the menu item to verify
      * @param exists for checking if entry exists
      * @param button MouseButton
      */
     public void gdPopupVerifyExistsByIndexPath(String indexPath,
             boolean exists, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifyExistsByIndexPath(indexPath, popup, exists);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry exists.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param indexPath the menu item to verify
      * @param exists for checking if entry exists
      * @param button MouseButton
      */
     public void gdPopupVerifyExistsByIndexPath(int xPos, String xUnits, 
             int yPos, String yUnits, String indexPath, 
             boolean exists, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifyExistsByIndexPath(indexPath, popup, exists);
     }
     
     /**
      * Checks if the specified context menu entry exists.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param exists for checking if entry exists
      * @param button MouseButton
      */
     public void gdPopupVerifyExistsByTextPath(String textPath,
             String operator, boolean exists, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifyExistsByTextPath(textPath, operator, popup, exists);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry exists.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param exists for checking if entry exists
      * @param button MouseButton
      */
     public void gdPopupVerifyExistsByTextPath(final int xPos, 
             final String xUnits, final int yPos,
             final String yUnits, String textPath, String operator,
             boolean exists, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifyExistsByTextPath(textPath, operator, popup, exists);
     }
     
     /**
      * Checks if the specified context menu entry is selected.
      * @param indexPath the menu item to verify
      * @param selected for checking if entry is selected
      * @param button MouseButton
      */
     public void gdPopupVerifySelectedByIndexPath(String indexPath,
             boolean selected, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifySelectedByIndexPath(indexPath, popup, selected);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry is selected.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param indexPath the menu item to verify
      * @param selected for checking if entry is selected
      * @param button MouseButton
      */
     public void gdPopupVerifySelectedByIndexPath(int xPos, String xUnits, 
             int yPos, String yUnits, String indexPath, 
             boolean selected, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifySelectedByIndexPath(indexPath, popup, selected);
     }
     
     /**
      * Checks if the specified context menu entry is selected.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param selected for checking if entry is selected
      * @param button MouseButton
      */
     public void gdPopupVerifySelectedByTextPath(String textPath,
             String operator, boolean selected, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 button);
         popupVerifySelectedByTextPath(textPath, operator, popup, selected);
     }
     
     /**
      * Opens the popup menu at the given position relative the current component
      * and checks if the specified context menu entry is selected.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param selected for checking if entry is selected
      * @param button MouseButton
      */
     public void gdPopupVerifySelectedByTextPath(final int xPos, 
             final String xUnits, final int yPos,
             final String yUnits, String textPath, String operator,
                 boolean selected, int button)
         throws StepExecutionException {
         Menu popup = PopupMenuUtil.showPopup(getComponent(), getRobot(),
                 xPos, xUnits, yPos, yUnits, button);
         popupVerifySelectedByTextPath(textPath, operator, popup, selected);
     }
     
     /**
      * Checks if the specified context menu entry is enabled in the visible Popup
      * @param indexPath the menu item to verify
      * @param popup JPopupMenu
      * @param enabled for checking enabled or disabled
      */
     private void popupVerifyEnabledByIndexPath(String indexPath,
             Menu popup, boolean enabled) {
         final int[] indexItems = MenuUtil.splitIndexPath(indexPath);
         try {
             final MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), 
                 popup, indexItems);
             if (menuItem == null) {
                 throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
             final boolean isEnabled = MenuUtil.isMenuItemEnabled(menuItem);
             Verifier.equals(enabled, isEnabled);
         } finally {
             try {
                 int pathLength = indexItems != null ? indexItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
     }
     
     /**
      * Checks if the specified context menu entry is enabled.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param popup JPopupMenu
      * @param enabled for checking enabled or disabled
      */
     private void popupVerifyEnabledByTextPath(String textPath,
             String operator, Menu popup, boolean enabled)
         throws StepExecutionException {
         final String[] pathItems = MenuUtil.splitPath(textPath);
         try {
             final MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), 
                     popup, pathItems, operator);
             if (menuItem == null) {
                 throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
             final boolean isEnabled = MenuUtil.isMenuItemEnabled(menuItem);
             Verifier.equals(enabled, isEnabled);
         } finally {
             try {
                 int pathLength = pathItems != null ? pathItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
     }
     
     /**
      * Checks if the specified context menu entry exists in the visible Popup
      * @param indexPath the menu item to verify
      * @param popup JPopupMenu
      * @param exists for checking if item exists
      */
     private void popupVerifyExistsByIndexPath(String indexPath,
             Menu popup, boolean exists) {
         final int[] indexItems = MenuUtil.splitIndexPath(indexPath);
         boolean isExisting = false;
         final Menu menu = popup;
         try {
             MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), menu, 
                 indexItems);
             if (menuItem != null) {
                 isExisting = true;
             }
         } finally {
             try {
                 int pathLength = indexItems != null ? indexItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
         Verifier.equals(exists, isExisting);
     }
     
     /**
      * Checks if the specified context menu entry exists in the visible Popup.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param popup JPopupMenu
      * @param exists for checking if entry exists
      */
     private void popupVerifyExistsByTextPath(String textPath,
             String operator, Menu popup, boolean exists)
         throws StepExecutionException {
         final String[] pathItems = MenuUtil.splitPath(textPath);
         boolean isExisting = false;
         try {
             final MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), 
                 popup, pathItems, operator);
             if (menuItem != null) {
                 isExisting = true;
             }
         } finally {
             try {
                 int pathLength = pathItems != null ? pathItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
         Verifier.equals(exists, isExisting);
     }
     
     /**
      * Checks if the specified context menu entry is selected in the visible Popup.
      * @param indexPath the menu item to verify
      * @param popup JPopupMenu
      * @param selected for checking if entry is selected
      */
     private void popupVerifySelectedByIndexPath(String indexPath,
             Menu popup, boolean selected)
         throws StepExecutionException {
         final int[] indexItems = MenuUtil.splitIndexPath(indexPath);
         try {
             final MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), 
                 popup, indexItems);
             if (menuItem == null) {
                 throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
             final boolean isSelected = MenuUtil.isMenuItemSelected(menuItem);
             Verifier.equals(selected, isSelected);
         } finally {
             try {
                 int pathLength = indexItems != null ? indexItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
     }
     
     /**
      * Checks if the specified context menu entry is selected in the visible Popup.
      * @param textPath the menu item to verify
      * @param operator operator used for matching
      * @param popup JPopupMenu
      * @param selected for checking if entry is selected
      */
     private void popupVerifySelectedByTextPath(String textPath, String operator,
             Menu popup, boolean selected)
         throws StepExecutionException {
         final String[] pathItems = MenuUtil.splitPath(textPath);
         try {
             final MenuItem menuItem = MenuUtil.navigateToMenuItem(getRobot(), 
                 popup, pathItems, operator);
             if (menuItem == null) {
                 throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                         EventFactory.createActionError(
                                 TestErrorEvent.NOT_FOUND));
             }
             final boolean isSelected = MenuUtil.isMenuItemSelected(menuItem);
             Verifier.equals(selected, isSelected);
         } finally {
             try {
                 int pathLength = pathItems != null ? pathItems.length : 0;
                 PopupMenuUtil.closePopup(getRobot(), popup, pathLength);
             } catch (StepExecutionException e) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
             }
         }
     }
 
     /**
      * Simulates a tooltip for demonstration purposes.
      * 
      * @param text The text to show in the tooltip
      * @param textSize The size of the text in points
      * @param timePerWord The amount of time, in milliseconds, used to display a
      *                    single word. A word is defined as a string surrounded
      *                    by whitespace.
      * @param windowWidth The width of the tooltip window in pixels.
      */
     public void gdShowText(final String text, final int textSize, 
         final int timePerWord, final int windowWidth) {
 
         final Rectangle bounds = (Rectangle)getEventThreadQueuer()
             .invokeAndWait("gdShowText.getBounds", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() {
                     return SwtUtils.getWidgetBounds(getComponent());
                 }
             });
 
         SimulatedTooltip sp = (SimulatedTooltip)getEventThreadQueuer()
             .invokeAndWait("gdShowText.initToolTip", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     return new SimulatedTooltip(timePerWord, text,
                         windowWidth, textSize, bounds);
                 }
             
             });
         sp.start();
         try {
             sp.join();
         } catch (InterruptedException e) {
             throw new StepExecutionException(e);
         }
     }
     
     /**
      * Performs a Drag. Moves into the middle of the Component and presses and
      * holds the given modifier and the given mouse button.
      * @param mouseButton the mouse button.
      * @param modifier the modifier, e.g. shift, ctrl, etc.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      */
     public void gdDrag(int mouseButton, String modifier, int xPos, 
             String xUnits, int yPos, String yUnits) {
         // Only store the Drag-Information. Otherwise the GUI-Eventqueue
         // blocks after performed Drag!
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdClickDirect(0, mouseButton, xPos, xUnits, yPos, yUnits);
     }
     
     /**
      * Performs a Drop. Moves into the middle of the Component and releases
      * the modifier and mouse button pressed by gdDrag.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDrop(final int xPos, final String xUnits, final int yPos, 
             final String yUnits, int delayBeforeDrop) {
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         final String modifier = dndHelper.getModifier();
         final int mouseButton = dndHelper.getMouseButton();
         // Note: This method performs the drag AND drop action in one runnable
         // in the GUI-Eventqueue because after the mousePress, the eventqueue
         // blocks!
         try {
             pressOrReleaseModifiers(modifier, true);
 
             getEventThreadQueuer().invokeAndWait("gdStartDrag", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             mouseButton);
 
                     shakeMouse();
                     
                     // drop
                     gdClickDirect(0, mouseButton, xPos, xUnits, yPos, yUnits);
                     return null;
                 }            
             });
             
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, mouseButton);
             pressOrReleaseModifiers(modifier, false);
         }
     }
 
     /**
      * Move the mouse pointer from its current position to a few points in
      * its proximity. This is used to initiate a drag operation.
      */
     protected void shakeMouse() {
         Point origin = getRobot().getCurrentMousePosition();
         SwtRobot lowLevelRobot = new SwtRobot(Display.getDefault());
         lowLevelRobot.mouseMove(
                 origin.x + MOUSE_SHAKE_OFFSET, 
                 origin.y + MOUSE_SHAKE_OFFSET);
         lowLevelRobot.mouseMove(
                 origin.x - MOUSE_SHAKE_OFFSET, 
                 origin.y - MOUSE_SHAKE_OFFSET);
         lowLevelRobot.mouseMove(origin.x, origin.y);
         if (!EnvironmentUtils.isWindowsOS() 
                 && !EnvironmentUtils.isMacOS()) {
             boolean moreEvents = true;
             while (moreEvents) {
                 moreEvents = Display.getDefault().readAndDispatch();
             }
         }
     }
     
     /**
      * @param extendSelection
      *            the string to indicate that the selection should be extended
      * @return a ClickModifier for the given extend selection
      */
     protected ClickModifier getClickModifier(String extendSelection) {
         ClickModifier cm = ClickModifier.create();
         if (CompSystemConstants.EXTEND_SELECTION_YES
                 .equalsIgnoreCase(extendSelection)) {
             cm.add(ClickModifier.M1);
         }
         return cm;
     }   
 }

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
 package org.eclipse.jubula.rc.common.tester;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IMenuComponent;
 import org.eclipse.jubula.rc.common.tester.adapter.interfaces.IMenuItemComponent;
 import org.eclipse.jubula.rc.common.util.MatchUtil;
 import org.eclipse.jubula.rc.common.util.MenuUtilBase;
 import org.eclipse.jubula.rc.common.util.Verifier;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 
 /**
  * General implementation for Menus. Also used for context menus
  * if they behave the same.
  * 
  * @author BREDEX GmbH
  * 
  */
 public abstract class AbstractMenuTester extends AbstractUITester {
 
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
             AbstractMenuTester.class);
 
     /**
      * @return the log
      */
     public static AutServerLogger getLog() {
         return log;
     }
 
     /**
      * This method gets the object which should implemented the menu Interface.
      * It is saved as Component so it must be casted.
      * @return the MenuAdapter
      */
     public IMenuComponent getMenuAdapter() {
         return (IMenuComponent) getComponent(); 
     }
     /**
      * Checks if the specified menu item is enabled.
      * 
      * @param menuItem the menu item as a text path to verify against
      * @param operator operator used for matching
      * @param enabled is the specified menu item enabled?
      */
     public void verifyEnabled(String menuItem, String operator, boolean enabled)
     {
         verifyEnabled(MenuUtilBase.splitPath(menuItem), operator, enabled);
     }
 
     /**
      * Checks if the specified menu item is enabled.
      * 
      * @param menuItem the menu item to verify against
      * @param operator operator used for matching
      * @param enabled is the specified menu item enabled?
      */
     public void verifyEnabled(String[] menuItem, String operator,
             boolean enabled) {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                 getAndCheckMenu(), menuItem, operator);
         try {
            checkIsNull(item);
             Verifier.equals(enabled, item.isEnabled());
         } finally {
             closeMenu(getAndCheckMenu(), menuItem, operator);
         }
 
     }
     /**
      * Checks if the given MenuItemAdapter is null and throws an Exception
      * 
      * @param item the MenuItemAdapter which should be checked
      */
     private void checkIsNull(final IMenuItemComponent item) {
         if (item.getRealComponent() == null) {
             throwMenuItemNotFound();
         }
     }
 
     /**
      * Checks if the specified menu item is enabled.
      * 
      * @param menuItem the menu item as a text path to verify against
      * @param enabled is the specified menu item enabled?
      */
     public void verifyEnabledByIndexpath(String menuItem, boolean enabled) {
         verifyEnabledByIndexpath(MenuUtilBase.splitIndexPath(menuItem),
                 enabled);
     }
 
     /**
      * Checks if the specified menu item is enabled.
      * 
      * @param menuItem the menu item to verify against
      * @param enabled is the specified menu item enabled?
      */
     public void verifyEnabledByIndexpath(int[] menuItem, boolean enabled) {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                 getAndCheckMenu(), menuItem);
         try {
            checkIsNull(item);
             Verifier.equals(enabled, item.isEnabled());
         } finally {
             closeMenu(getAndCheckMenu(), menuItem);
         }
     }
 
 
 
     /**
      * Verifies if the specified menu item exists
      * 
      * @param menuItem the menu item to verify against
      * @param operator operator used for matching
      * @param exists  should the menu item exist?
      */
     public void verifyExists(String menuItem, String operator, boolean exists) {
         verifyExists(MenuUtilBase.splitPath(menuItem), operator, exists);
     }
 
     /**
      * Verifies if the specified menu item exists
      * 
      * @param menuItem the menu item to verify against
      * @param operator operator used for matching
      * @param exists should the menu item exist?
      */
     public void verifyExists(String[] menuItem, String operator, boolean exists)
     {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                 getAndCheckMenu(), menuItem, operator);
         try {
             Verifier.equals(exists, item.isExisting());
         } finally {
             closeMenu(getAndCheckMenu(), menuItem, operator);
         }
     }
 
     /**
      * Verifies if the specified menu item exists
      * @param menuItem the menu item to verify against
      * @param exists should the menu item exist?
      */
     public void verifyExistsByIndexpath(String menuItem, boolean exists) {
         verifyExistsByIndexpath(MenuUtilBase.splitIndexPath(menuItem), exists);
     }
 
     /**
      * Verifies if the specified menu item exists
      * 
      * @param menuItem the menu item to verify against
      * @param exists should the menu item exist?
      */
     public void verifyExistsByIndexpath(int[] menuItem, boolean exists) {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                         getAndCheckMenu(), menuItem);
         try {
             Verifier.equals(exists, item.isExisting());
         } finally {
             closeMenu(getAndCheckMenu(), menuItem);
         }
     }
 
     /**
      * Checks if the specified menu item is selected.
      * 
      * @param menuItem the menu item to verify against
      * @param operator operator used for matching
      * @param selected is the specified menu item selected?
      */
     public void verifySelected(String menuItem, String operator,
             boolean selected) {
         verifySelected(MenuUtilBase.splitPath(menuItem), operator, selected);
     }
 
     /**
      * Checks if the specified menu item is selected.
      * 
      * @param menuItem the menu item to verify against
      * @param operator operator used for matching
      * @param selected is the specified menu item selected?
      */
     public void verifySelected(String[] menuItem, String operator,
             boolean selected) {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                 getAndCheckMenu(), menuItem, operator);
         try {
            checkIsNull(item);
             Verifier.equals(selected, item.isSelected());
 
         } finally {
             closeMenu(getAndCheckMenu(), menuItem, operator);
         }
     }
 
     /**
      * Checks if the specified menu item is selected.
      * 
      * @param menuItem the menu item to verify against
      * @param selected is the specified menu item selected?
      */
     public void verifySelectedByIndexpath(String menuItem, boolean selected) {
         verifySelectedByIndexpath(MenuUtilBase.splitIndexPath(menuItem),
                 selected);
     }
 
     /**
      * Checks if the specified menu item is selected.
      * 
      * @param menuItem the menu item to verify against
      * @param selected is the specified menu item selected?
      */
     public void verifySelectedByIndexpath(int[] menuItem, boolean selected) {
         checkPathLength(menuItem.length);
         final IMenuItemComponent item = navigateToMenuItem(
                 getAndCheckMenu(), menuItem);
         try {
            checkIsNull(item);
             Verifier.equals(selected, item.isSelected());
 
         } finally {
             closeMenu(getAndCheckMenu(), menuItem);
         }
     }
     
     /**
      * Tries to select a menu item in a menu defined by an Index-Path
      * @param indexPath the menu item to select
      */
     public void selectMenuItemByIndexpath(String indexPath) {
         int[] indexItems = MenuUtilBase.splitIndexPath(indexPath);
         checkPathLength(indexItems.length);
         
         try {
             final IMenuItemComponent item = navigateToMenuItem(
                     getAndCheckMenu(), indexItems);
         
             checkIsNull(item);
             
             item.selectMenuItem();
         } catch (StepExecutionException e) {
             try {
                 closeMenu(getAndCheckMenu(), indexItems);
             } catch (StepExecutionException e1) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
                 if (getLog().isInfoEnabled()) {
                     getLog().info("Tried to close a disabled or already closed menu."); //$NON-NLS-1$
                 }
             }
             throwMenuItemNotFound();
         }
         
     }
     
     /**
      * Tries to select a menu item in a menu defined by a Text-Path
      * @param namePath the menu item to select
      * @param operator operator used for matching
      */
     public void selectMenuItem(String namePath, final String operator) {
         String[] menuItems = MenuUtilBase.splitPath(namePath);
         if (menuItems.length == 0) {
             throw new StepExecutionException("empty path to menuitem not allowed", //$NON-NLS-1$
                 EventFactory.createActionError());
         }
         IMenuItemComponent item = navigateToMenuItem(getAndCheckMenu(), 
                 menuItems, operator);
         if (item == null) {
             try {
                 closeMenu(getAndCheckMenu(), menuItems, operator);
             } catch (StepExecutionException see) {
                 // Menu item is disabled or menu is already closed
                 // Do nothing
                 getLog().info("Tried to close a disabled or already closed menu."); //$NON-NLS-1$
             }
             throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         item.selectMenuItem();
     }
     
 
     /**
      * 
      * @return the IMenuAdapter.
      * @throws StepExecutionException
      *             if the active window has no menu bar.
      */
     protected IMenuComponent getAndCheckMenu() throws StepExecutionException {
         Object menu = getMenuAdapter().getRealComponent();
         // Verify that the active window has a menu bar
         if (menu == null) {
             throw new StepExecutionException(
                     I18n.getString(TestErrorEvent.NO_MENU_BAR),
                     EventFactory.createActionError(TestErrorEvent.NO_MENU_BAR));
         }
         return getMenuAdapter();
     }
 
     /**
      * 
      */
     private void throwMenuItemNotFound() {
         throw new StepExecutionException("no such menu item found", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
     }
     
 
     /**
      * this methods closes the hole menu. It is clicking on the parent item in the menu bar.
      * 
      * If you need another implementation override this method.
      * @param menuBar the main menu
      * @param textPath the text path used for opening the menu
      * @param operator the operator which was used for opening the menu
      */
     protected void closeMenu(IMenuComponent menuBar, String[] textPath,
             String operator) {
         IMenuItemComponent menuitem = findMenu(menuBar,
                 getIndexForName(menuBar, textPath[0], operator));
         if (menuitem.getRealComponent() != null) {
             getRobot().click(
                     menuitem.getRealComponent(),
                     null,
                     ClickOptions.create().setClickType(
                             ClickOptions.ClickType.RELEASED));
         
         }
     
     }
     /**
      * this methods closes the hole menu. It is clicking on the parent item in the menu bar.
      * 
      * If you need another implementation override this method. 
      * @param menuBar the main menu
      * @param path the integer based path used for opening the menu
      */
     protected void closeMenu(IMenuComponent menuBar, int[] path) {
         IMenuItemComponent menuitem = findMenu(menuBar, path[0]);
         if (menuitem.getRealComponent() != null) {
             getRobot().click(
                 menuitem.getRealComponent(),
                 null,
                 ClickOptions.create().setClickType(
                         ClickOptions.ClickType.RELEASED));
             
         }
     }
     
     /**
      * Gets the index of the specific menu entry with the name
      * 
      * @param menu the menu in which all items are stored
      * @param name the name of the item we want the index from
      * @param operator the operator for the matching
      * @return the index for the specific menu entry
      */
     protected int getIndexForName(IMenuComponent menu, String name,
             String operator) {
         IMenuItemComponent [] subElements = menu.getItems();
         int downcount = 0;
         for (int j = 0; j < subElements.length; j++) {               
             IMenuItemComponent tempMenu = (IMenuItemComponent)subElements[j];
             if (tempMenu.isSeparator()) {
                 downcount++;
             }
             if (tempMenu.isShowing()
                     && MatchUtil.getInstance().match(
                             tempMenu.getText(), name, operator)) {
                 return j - downcount;
             }
         }
         return Integer.MAX_VALUE;
     }
     
     
     /**
      * implementation for "wait for component"
      * @param timeout the maximum amount of time to wait for the component
      * @param delay the time to wait after the component is found
      */
     public void waitForComponent(int timeout, int delay) {
         if (getComponent().getRealComponent() == null) {
             long start = System.currentTimeMillis();
             do {
                 RobotTiming.sleepWaitForComponentPollingDelay();
             } while (System.currentTimeMillis() - start < timeout
                     && getComponent().getRealComponent() == null);
             if (getComponent().getRealComponent() == null) {
                 throw new StepExecutionException("No Menubar found.", //$NON-NLS-1$
                         EventFactory.createComponentNotFoundErrorEvent());
             }
         }
         TimeUtil.delay(delay);
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * This method should be overridden if there is a need for a faster implementation.
      * 
      * @param menuBar the menubar
      * @param path the path where to navigate in the menu.
      * @param operator operator used for matching
      * @return the adapter at the end of the specified path or a adapter that contains no component.
      */
     protected IMenuItemComponent navigateToMenuItem(
             IMenuComponent menuBar, String[] path, String operator) {
         checkPathLength(path.length);
         IMenuComponent currentmenu = menuBar;
         IMenuItemComponent currentMenuItem = null;
         final int pathLength = path.length;
         final int beforeLast = pathLength - 1;
         
         for (int i = 0; i < path.length; i++) {
             int pathIndex = getIndexForName(currentmenu, path[i], operator);
             currentMenuItem = getNextMenuItem(currentmenu, pathIndex);
             
             if ((currentMenuItem.getRealComponent() == null) 
                     && (i < beforeLast)) {                
                 return currentMenuItem;
             }
             
             if (i < beforeLast) {            
                 if (!currentMenuItem.hasSubMenu()) {
                     // the given path is longer than the menu levels
                     return newMenuItemAdapter(null);
                 }
                 currentmenu = currentMenuItem.openSubMenu();
             }
         }
         return currentMenuItem;
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * This method should be overridden if there is a need for a faster implementation.
      * 
      * @param menubar the menubar
      * @param path the path where to navigate in the menu.
      * @return -the adapter at the end of the specified path or a adapter that contains no component.
      */
     protected IMenuItemComponent navigateToMenuItem(
             IMenuComponent menubar, int[] path) {
         checkPathLength(path.length);
         
         IMenuComponent currentmenu = menubar;
         IMenuItemComponent currentMenuItem = null;
         final int pathLength = path.length;
         final int beforeLast = pathLength - 1;
             
         for (int i = 0; i < path.length; i++) {
             final int pathIndex = path[i];
             currentMenuItem = getNextMenuItem(currentmenu, pathIndex);
             
             if ((currentMenuItem.getRealComponent() == null) 
                     && (i < beforeLast)) {                
                 return currentMenuItem;
             }
             
             if (i < beforeLast) {            
                 if (!currentMenuItem.hasSubMenu()) {
                     // the given path is longer than the menu levels
                     return newMenuItemAdapter(null);
                 }
                 currentmenu = currentMenuItem.openSubMenu();
             }
             
                 
                 
         }
      
         return currentMenuItem;
     }
     /**
      * gets the next menu item adapter from its specific index
      * @param currentmenu the current menu
      * @param pathIndex the index from the next menu item
      * @return the wanted menu item in a adapter
      */
     private IMenuItemComponent getNextMenuItem(IMenuComponent currentmenu,
             final int pathIndex) {
         IMenuItemComponent currentMenuItem;
         if (pathIndex < 0) {
             throwInvalidPathException();            
         }
         currentMenuItem = findMenu(currentmenu, pathIndex);
         return currentMenuItem;
     }
 
     /**
      * @param menu menu 
      * @param idx index of the current wanted item
      * @return the next IMenuItemAdapter from the next cascade
      */
     private IMenuItemComponent findMenu(IMenuComponent menu, int idx) {
         List visibleSubMenus = new ArrayList();
         IMenuItemComponent[] subElements = menu.getItems();
         
         for (int i = 0; i < subElements.length; ++i) {
             
             IMenuItemComponent menuitem = subElements[i];
             if (menuitem.getRealComponent() != null && !menuitem.isSeparator() 
                     && menuitem.isShowing()) {
                 visibleSubMenus.add(menuitem);
             }            
         }      
         
         if (idx >= visibleSubMenus.size() || idx < 0) {
             return newMenuItemAdapter(null);
         }        
         return (IMenuItemComponent) visibleSubMenus.get(idx);        
     }    
     
     /**
      * Checks the path for it length and throws and StepExecutionExecption if it is 0
      * @param length the path length to be checked
      */
     private void checkPathLength(int length) {
         if (length < 1) { 
             throw new StepExecutionException("empty path to menuitem is not allowed", EventFactory //$NON-NLS-1$
                     .createActionError(
                             TestErrorEvent.INVALID_PARAM_VALUE));            
         }
     }
     
     /**
      * 
      */
     private static void throwInvalidPathException() {
         throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
           .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
     }
     /**
      * This adapts or puts the new MenuItem in the context which is needed for
      * the algorithms.
      * @param component the new MenuItem which is used by the next step
      * @return the adapted or casted MenuItem
      */
     protected abstract IMenuItemComponent newMenuItemAdapter(Object component);
 
 }

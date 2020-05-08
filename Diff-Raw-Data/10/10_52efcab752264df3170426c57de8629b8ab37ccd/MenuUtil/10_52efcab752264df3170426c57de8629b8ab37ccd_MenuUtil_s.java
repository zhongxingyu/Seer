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
 package org.eclipse.jubula.rc.swing.swing.implclasses;
 
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.MenuElement;
 
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.MenuUtilBase;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 
 /**
  * Helper methods for menus
  *
  * @author BREDEX GmbH
  * @created 13.01.2006
  */
 public abstract class MenuUtil extends MenuUtilBase {
     
     /**
      * The standard constructor is declared private to prevent construction
      */
     private MenuUtil() {
         super();
         // nothing
     }
 
     /**
      * Tries to find a menu in the menubar.
      * 
      * @param mb the menu bar
      * @param name the name of the menu to find
      * @param operator operator used for matching
      * @return the menu or null if no menu with the given name was found.
      */
     public static JMenu findMenu(JMenuBar mb, String name, String operator) {
         MenuElement [] subElements = mb.getSubElements();
         for (int j = 0; j < subElements.length; j++) {
             if (subElements[j] instanceof JMenu) {
                 JMenu menu = (JMenu)subElements[j];
                 if (menu.isShowing()
                         && MatchUtil.getInstance().match(
                                 menu.getText(), name, operator)) {
                     return menu;
                 }
             }
         }
         return null;
     }
     /**
      * Tries to find a menu in the menubar.
      * 
      * @param mb the menu bar
      * @param idx the index of the menu to find
      * @return the menu, or <code>null</code> if not found
      */
     public static JMenu findMenu(JMenuBar mb, int idx) {
         List visibleSubMenus = new ArrayList();
         MenuElement [] subElements = mb.getSubElements();
         for (int i = 0; i < subElements.length; ++i) {
             if (subElements[i] instanceof JMenu) {
                 JMenu menu = (JMenu)subElements[i];
                 if (menu != null && menu.isShowing()) {
                     visibleSubMenus.add(menu);
                 }
             }
         }
 
         if (idx >= visibleSubMenus.size() || idx < 0) {
             return null;
         }
 
         return (JMenu)visibleSubMenus.get(idx);
     }
     
     /**
      * Tries to find a menu item in a menu.
      * 
      * @param menu the menu
      * @param name the name of the menu item
      * @param operator the operator to use when checking for equality
      * @return the menu item
      */
     private static JMenuItem findMenuItem(MenuElement menu, String name, 
             String operator) {
         
         MenuElement[] elems = menu.getSubElements();
         for (int i = 0; i < elems.length; ++i) {
             if (elems[i] instanceof JMenuItem) {
                 JMenuItem item = (JMenuItem)elems[i];
                 if (MatchUtil.getInstance()
                         .match(item.getText(), name, operator)) {
                     return item;
                 }
             }
         }
         return null;
     }
 
     /**
      * Tries to find a menu item in a menu.
      * 
      * @param mn the menu
      * @param name the name of the item in the menu
      * @param operator operator used for matching
      * @return the menu item or null if no item with the given name was found,
      */
     private static JMenuItem findMenuItem(JMenu mn, 
                                           String name, 
                                           String operator) {
         
         for (int j = 0; j < mn.getItemCount(); j++) {
             JMenuItem itn = mn.getItem(j);
             // don't know why it can become null, perhaps
             // seperator = null
             if (itn != null && itn.isShowing() && MatchUtil.getInstance()
                     .match(itn.getText(), name, operator)) {
                 
                 return itn;
             }
         }
         return null;
     }
     
     /**
      * Splits a path into integers
      * 
      * @param path the path
      * @return an array of int values
      */
     public static int[] splitIndexPath(String path) {
         return MenuUtilBase.splitIndexPath(path);
     }
     
     /**
      * Splits a path into its components. The separator is '/'.
      * 
      * @param path the path
      * @return the splitted path
      */
     public static String[] splitPath(String path) {
         return MenuUtilBase.splitPath(path);
     }
     
     
     /**
      * Tries to find a menu item in a menu.
      * 
      * @param menu the menu
      * @param idx the index of the item in the menu
      * @return the menu item
      */
     private static JMenuItem findMenuItem(JMenu menu, int idx) {
         List items = new ArrayList();
         for (int i = 0; i < menu.getItemCount(); ++i) {
             JMenuItem it = menu.getItem(i);
             if (it != null && it.isShowing()) {
                 items.add(it);
             }
         }
         if (idx >= items.size()) {
             return null;
         }
         return (JMenuItem)items.get(idx);
     }
     
     /**
      * Tries to find a menu item in a menu.
      * 
      * @param menu the menu
      * @param idx the index of the menu item
      * @return the menu item
      */
     public static JMenuItem findMenuItem(MenuElement menu, int idx) {
         MenuElement[] elems = menu.getSubElements();
         List items = new ArrayList();
         for (int i = 0; i < elems.length; ++i) {
             if (elems[i] instanceof JMenuItem 
                     && ((JMenuItem)elems[i]).isShowing()) {
                 items.add(elems[i]);
             }
         }
         if (idx >= items.size()) {
             throw new StepExecutionException("no such element: " + idx, //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         return (JMenuItem)items.get(idx);
     }
     
     /**
      * Closes the given item in the given menuBar (if open)
      * @param robot the robot
      * @param menuBar the menu bar
      * @param item the item to close
      */
     public static void closeMenu(IRobot robot, JMenuBar menuBar, int item) {
         if (menuBar.getSelectionModel().getSelectedIndex() == item) {
             JMenu menu = findMenu(menuBar, item);
             if (menu != null) {
                 clickMenuItem(robot, menu);
             }
         }
     }
     
     /**
      * Closes the given item in the given menuBar (if open)
      * @param robot the robot
      * @param menuBar the menu bar
      * @param item the item to close
      */
     public static void closeMenu(IRobot robot, JMenuBar menuBar, String item) {
         final int index = getItemNumber(menuBar, item);
         if (menuBar.getSelectionModel().getSelectedIndex() == index) {
             JMenu menu = findMenu(menuBar, index);
             if (menu != null) {
                 clickMenuItem(robot, menu);
             }
         }
     }
     
     /**
      * Closes the given PopupMenu (if open)
      * @param robot the robot
      * @param popup the PopupMenu
      */
     public static void closePopupMenu(IRobot robot, JPopupMenu popup) {
         //press escape
         robot.keyType(popup, KeyEvent.VK_ESCAPE);
     }
     
     /**
      * Gets the index of the given menu name
      * @param menuBar the menu bar to search in
      * @param item the item name
      * @return the index of the item or -1 if not found
      */
     private static int getItemNumber(JMenuBar menuBar, String item) {
         MenuElement [] subElements = menuBar.getSubElements();
         int invisibleElementCount = 0;
         for (int i = 0; i < subElements.length; i++) {
             if (subElements[i] instanceof JMenu) {
                 JMenu menu = (JMenu)subElements[i];
                 if (!menu.isShowing()) {
                     invisibleElementCount++;
                 } else if (menu.getText().equals(item)) {
                     return i - invisibleElementCount;
                 }
             }
         }
 
         return -1;
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      *
      * @param robot robot
      * @param menuBar the menubar
      * @param path the path where to navigate in the menu.
      * @param operator operator used for matching
      * @return the component at the end of the specified path or null.
      */
     public static JMenuItem navigateToMenuItem(IRobot robot, JMenuBar menuBar,
         String[] path, String operator) {
 
         if (path.length < 1) {
             throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
         }
         JMenu menu = findMenu(menuBar, path[0], operator);
         if (menu == null) {
             return null;
         }
         return navigateToMenuItem(robot, menu, path, operator, 1);
     }
     /**
      * Tries to navigate through the menu to the specified menu item.
      *
      * @param robot robot
      * @param menuBar the menubar
      * @param path the path where to navigate in the menu.
      * @return the component at the end of the specified path.
      */
     public static JMenuItem navigateToMenuItem(IRobot robot, JMenuBar menuBar,
             int[] path) {
         
         if (path.length < 1) {
             throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
         }
         JMenu menu = findMenu(menuBar, path[0]);
         return navigateToMenuItem(robot, menu, path, 1);
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * 
      * @param robot the robot
      * @param popup the popup menu
      * @param path the path where to navigate in the menu.
      * @param operator operator used for matching
      * @return the component at the end of the specified path.
      */
     public static JMenuItem navigateToMenuItem(IRobot robot, JPopupMenu popup,
             String[] path, String operator) {
 
         if (path.length < 1) {
             throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
         }
         JMenuItem item = findMenuItem(popup, path[0], operator);
         return navigateToMenuItem(robot, item, path, operator, 1);
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * 
      * @param robot the robot
      * @param popup the popup menu
      * @param path the path where to navigate in the menu.
      * @return the component at the end of the specified path.
      */
     public static JMenuItem navigateToMenuItem(IRobot robot, JPopupMenu popup,
             int[] path) {
 
         if (path.length < 1) {
             throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
         }
         JMenuItem item = findMenuItem(popup, path[0]);
         return navigateToMenuItem(robot, item, path, 1);
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * 
      * @param robot robot
      * @param item the menu item
      * @param path the path where to navigate in the menu.
      * @param operator operator used for matching
      * @param idx the current index in the path
      * @return the component at the end of the specified path.
      */
     private static JMenuItem navigateToMenuItem(IRobot robot, JMenuItem item,
             String[] path, String operator, int idx) {
         
         if (path.length == idx) {
             return item;
         }
 
         if (!(item instanceof JMenu)) {
             throw new StepExecutionException("unexpected item found", //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         JMenu menu = (JMenu) item;
         clickMenuItem(robot, menu);
         RobotTiming.sleepPostShowSubMenuItem(menu.getDelay());
         JMenuItem next = findMenuItem(menu, path[idx], operator);
         
         return navigateToMenuItem(robot, next, path, operator, idx + 1);
     }
     
     /**
      * Tries to navigate through the menu to the specified menu item.
      * 
      * @param robot robot
      * @param item the menu item
      * @param path the path where to navigate in the menu.
      * @param idx the current index in the path
      * @return the component at the end of the specified path.
      */
     private static JMenuItem navigateToMenuItem(IRobot robot, JMenuItem item,
             int[] path, int idx) {
         
         if (path.length == idx) {
             return item;
         }
 
         if (!(item instanceof JMenu)) {
             throw new StepExecutionException("unexpected item found", //$NON-NLS-1$
                     EventFactory.createActionError());
         }
         JMenu menu = (JMenu) item;
         clickMenuItem(robot, menu);
         RobotTiming.sleepPostShowSubMenuItem(menu.getDelay());
         JMenuItem next = findMenuItem(menu, path[idx]);
 
         return navigateToMenuItem(robot, next, path, idx + 1);
     }
     /**
      * Clicks on a menu item
      * 
      * @param robot
      *      the robot
      * @param item
      *      the menu item
      */
     public static void clickMenuItem(IRobot robot, JMenuItem item) {
         if (!item.isEnabled()) {
             throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.MENU_ITEM_NOT_ENABLED));
         }
         robot.click(item, null, ClickOptions.create().setClickType(
            ClickOptions.ClickType.RELEASED));
     }
 
 }

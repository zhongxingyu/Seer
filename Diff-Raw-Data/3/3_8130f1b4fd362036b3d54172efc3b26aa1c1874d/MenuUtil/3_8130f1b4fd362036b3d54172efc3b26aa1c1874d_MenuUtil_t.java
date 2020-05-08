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
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IEventMatcher;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRobotEventConfirmer;
 import org.eclipse.jubula.rc.common.driver.IRobotEventInterceptor;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.driver.InterceptorOptions;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.MenuUtilBase;
 import org.eclipse.jubula.rc.common.listener.EventLock;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swt.driver.EventThreadQueuerSwtImpl;
 import org.eclipse.jubula.rc.swt.driver.RobotFactorySwtImpl;
 import org.eclipse.jubula.rc.swt.driver.RobotSwtImpl;
 import org.eclipse.jubula.rc.swt.driver.SelectionSwtEventMatcher;
 import org.eclipse.jubula.rc.swt.driver.ShowSwtEventMatcher;
 import org.eclipse.jubula.rc.swt.implclasses.EventListener.Condition;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.TimeoutConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MenuEvent;
 import org.eclipse.swt.events.MenuListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 
 
 /**
  * @author BREDEX GmbH
  * @created 30.03.2007
  */
 public abstract class MenuUtil extends MenuUtilBase {
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         RobotSwtImpl.class);
     
     /**
      * Waits for a submenu to appear. Examples of submenus are cascading menus
      * and pulldown menus.
      *
      * @author BREDEX GmbH
      * @created Oct 30, 2008
      */
     public static class MenuShownCondition implements Condition {
         /** the menu that was shown */
         private Menu m_shownMenu = null;
 
         /** the parent item of the expected menu */
         private MenuItem m_parentItem;
 
         /**
          * Constructor
          *  
          * @param parentItem The parent item of the expected menu. This 
          *                   condition only matches if a menu with parent item
          *                   <code>parentItem</code> appears.
          */
         MenuShownCondition(MenuItem parentItem) {
             m_parentItem = parentItem;
         }
         
         /**
          * 
          * @return the menu that appeared
          */
         public Menu getMenu() {
             return m_shownMenu;
         }
         
         /**
          * 
          * {@inheritDoc}
          */
         public boolean isTrue(Event event) {
             if (event.type == SWT.Show && event.widget instanceof Menu
                     && ((Menu)(event.widget)).getParentItem() == m_parentItem) {
                 m_shownMenu = (Menu)event.widget;
                 return true;
             } 
             
             return false;
         }
     }
 
     /**
      * Listens for a menu to be hidden, the removes itself from the menu's
      * listener list.
      * 
      * @author BREDEX GmbH
      * @created Nov 01, 2011
      */
     private static final class MenuHiddenListener implements MenuListener {
 
         /** whether the expected event has occurred */
         private boolean m_eventOccurred = false;
         
         /**
          * 
          * {@inheritDoc}
          */
         public void menuHidden(MenuEvent e) {
             m_eventOccurred = true;
             ((Menu)e.widget).removeMenuListener(this);
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void menuShown(MenuEvent e) {
             // no-op
         }
 
         /**
          * 
          * @return <code>true</code> if the menu has been hidden since this 
          *         listener was registered. Otherwise, <code>false</code>.
          */
         public boolean isMenuHidden() {
             return m_eventOccurred;
         }
     }
     
     /** The Class name of this class */
     private static final String CLASSNAME = MenuUtil.class.getName();
     
     /** The IEventThreadQueuer to execute code in the GUI-Thread */
     private static final IEventThreadQueuer EVENT_THREAD_QUEUER = 
         new EventThreadQueuerSwtImpl();
     
     /**
      * The standard constructor is declared private to prevent construction
      */
     private MenuUtil() {
         // nothing
     }
 
     /**
      * Tries to navigate through the menu to the specified menu item.
      *
      * @param robot robot
      * @param menuBar the menubar
      * @param path the path where to navigate in the menu.
      * @param operator operator used for matching
      * @return the MenuItem at the end of the specified path or null if the
      * specified MenuItem was not found.
      */
     public static MenuItem navigateToMenuItem(IRobot robot, Menu menuBar,
             String[] path, String operator) {
 
         MenuItem menuItem = null;
         Menu currentMenu = menuBar;
         final int pathLength = path.length;
         final int beforeLast = pathLength - 1;
         for (int i = 0; i < pathLength; i++) {
             menuItem = findMenuItem(currentMenu, path[i], operator);
             if (menuItem == null) {
                 return null;
             }
             if (i < beforeLast) {
                 if (!hasSubMenu(menuItem)) {
                     // the given path is longer than the menu levels
                     return null;
                 }
                 currentMenu = openSubMenu(menuItem, robot);
             }
         }
         return menuItem;
     }
     
     /**
      * @param robot the Robot
      * @param menuBar the menubar
      * @param path path the path where to navigate in the menu.
      * @return the MenuItem at the end of the specified path or null if the
      * specified MenuItem was not found.
      */
     public static MenuItem navigateToMenuItem(final IRobot robot, Menu menuBar, 
         int[] path) {
         
         MenuItem menuItem = null;
         Menu currentMenu = menuBar;
         final int pathLength = path.length;
         final int beforeLast = pathLength - 1;
         for (int i = 0; i < pathLength; i++) {
             final int pathIndex = path[i];
             if (pathIndex < 0) {
                 throwInvalidPathException();            
             }
             menuItem = findMenuItem(currentMenu, pathIndex);
             if ((menuItem == null) && (i < beforeLast)) {
                 return null;
             }
             if (i < beforeLast) {
                 if (!hasSubMenu(menuItem)) {
                     // the given path is longer than the menu levels
                     return null;
                 }
                 currentMenu = openSubMenu(menuItem, robot);
             }
         }
         return menuItem;
     }
 
     /**
      * Opens and returns the submenu (cascade/dropdown/etc) of the given menu
      * item.
      * 
      * @param menuItem The menu item to click in order to open a submenu.
      * @param robot The robot to use in order to perform the click on 
      *              <code>menuItem</code>.
      * @return the opened submenu.
      * @throws StepExecutionException if the submenu does not appear in good
      *                                time.
      */
     private static Menu openSubMenu(final MenuItem menuItem, 
             final IRobot robot) throws StepExecutionException {
         MenuShownCondition cond = new MenuShownCondition(menuItem);
         EventLock lock = new EventLock();
         final EventListener listener = new EventListener(lock, cond);
         final Display d = menuItem.getDisplay();
         final IEventThreadQueuer queuer = new EventThreadQueuerSwtImpl();
         
         queuer.invokeAndWait("addMenuShownListeners", new IRunnable() { //$NON-NLS-1$
             public Object run() {
                 d.addFilter(SWT.Show, listener);
                 
                 return null;
             }
         });
         try {
             // Menu bar items require a click in order to open the submenu.
             // Cascading menus are opened with a mouse-over and 
             // may be closed by a click.
             int clickCount = isMenuBarItem(menuItem) ? 1 : 0;
             Menu menu = (Menu)EVENT_THREAD_QUEUER.invokeAndWait(
                     "openSubMenu", new IRunnable() { //$NON-NLS-1$
                         public Object run() {
                             return menuItem.getMenu();
                         }            
                     });
             Rectangle bounds = getMenuItemBounds(menuItem);
             Rectangle nullBounds = new Rectangle(0, 0, 0, 0);            
             if (bounds.equals(nullBounds)) {                               
                 openSubMenuProgramatically(menu);
             } else {
                 clickMenuItem(robot, menuItem, clickCount);
             }
             synchronized (lock) {
                 long timeout = TimeoutConstants.SERVER_TIMEOUT_WAIT_FOR_POPUP;
                 long done = System.currentTimeMillis() + timeout; 
                 long now;                
                 while (!lock.isReleased() && timeout > 0) {
                     lock.wait(timeout);
                     now = System.currentTimeMillis();
                     timeout = done - now;
                 }
             } 
         } catch (InterruptedException e) {
             // ignore
         } finally {
             queuer.invokeAndWait("removeMenuShownListeners", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     d.removeFilter(SWT.Show, listener);
                     
                     return null;
                 }
             });
         }
         if (!lock.isReleased()) {
             String itemText = (String)EVENT_THREAD_QUEUER.invokeAndWait(
                     "getItemText", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             if (menuItem != null && !menuItem.isDisposed()) {
                                 return menuItem.getText();
                             }
                             return "unknown menu item"; //$NON-NLS-1$
                         }
                 
                     });
             itemText = SwtUtils.removeMnemonics(itemText);
             throw new StepExecutionException(
                     I18n.getString("TestErrorEvent.MenuDidNotAppear",  //$NON-NLS-1$
                             new String [] {itemText}), 
                     EventFactory.createActionError(
                             "TestErrorEvent.MenuDidNotAppear", //$NON-NLS-1$ 
                             new String [] {itemText}));
         }        
         return cond.getMenu();
     }
 
     /**
      * @param menuItem the menu item to check
      * @return <code>true</code> of the given menu item is part of a menu
      *         bar. Otherwise, <code>false</code>.
      */
     private static boolean isMenuBarItem(final MenuItem menuItem) {
         return ((Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
                 "isMenuBarItem", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         if (menuItem != null && !menuItem.isDisposed()) {
                             Menu parent = menuItem.getParent();
                             if (parent != null && !parent.isDisposed()) {
                                 return (parent.getStyle() & SWT.BAR) != 0 
                                     ? Boolean.TRUE : Boolean.FALSE;
                             }
                         }
                         return Boolean.FALSE;
                     }
             
                 })).booleanValue();
     }
 
     /**
      * Checks if the given {@link MenuItem} has a submenu.
      * @param menuItem a {@link MenuItem}
      * @return true if the given {@link MenuItem} has a submenu, 
      * false if the {@link MenuItem} is a leaf.
      */
     private static boolean hasSubMenu(MenuItem menuItem) {
         return getMenu(menuItem) != null ? true : false;
     }
     
     /**
      * Gets the MenuItem of the given Menu at the given index.<br>
      * This method runs in the GUI-Thread!
      * @param menu the Menu
      * @param index the index of the wanted MenuItem
      * @return the MenuItem at the given index or null if no MenuItem was found.
      * @see Menu#getItemCount()
      */
     public static MenuItem findMenuItem(final Menu menu, final int index) {
         return (MenuItem)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".findMenuItem", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     if (index < 0 || index >= menu.getItemCount()) {
                         return null;
                     }
                     
                     // Check for separators, and increase the index by the number
                     // of separators encountered
                     int newIndex = index;
                     for (int i = 0; i <= newIndex; i++) {
                         if (isSeparator(menu.getItem(i))) {
                             newIndex++;
                             if (newIndex >= menu.getItemCount()) {
                                 return null;
                             }
                         }
                     }
 
                     return menu.getItem(newIndex);
                 }
             });
     }
     
     
     /**
      * Calls menuItem.getMenu in the GUI-Thread.
      * @param menuItem the MenuItem
      * @return a Menu or null.
      * @see MenuItem#getMenu()
      */
     public static Menu getMenu(final MenuItem menuItem) {
         return (Menu)EVENT_THREAD_QUEUER.invokeAndWait(CLASSNAME + ".getMenu",  //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     return menuItem.getMenu();
                 }
             });
     }
     
     
     /**
      * 
      */
     private static void throwInvalidPathException() {
         throw new StepExecutionException("invalid path", EventFactory //$NON-NLS-1$
           .createActionError(TestErrorEvent.INVALID_PARAM_VALUE));
     }
     
     /**
      * Tries to find a menu in the menubar.<br>
      * This method runs in GUI-Thread!
      * 
      * @param menu the menu bar
      * @param name the name of the menu to find
      * @param operator operator used for matching
      * @return the MenuItem or null if not found
      */
     public static MenuItem findMenuItem(final Menu menu, final String name, 
         final String operator) {
 
         return (MenuItem)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".findMenuItem", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     final MenuItem[] items = menu.getItems();
                     for (int i = 0; i < items.length; i++) {
                         MenuItem item = items[i];
                         String itemText = SwtUtils.removeMnemonics(
                                 item.getText());
                         if (MatchUtil.getInstance().match(itemText, name, 
                             operator) && !isSeparator(item)) {
     
                             return item;
                         }
                     }
                     return null;
                 }
             });
     }
     
     /**
      * Checks whether the given menu item is a separator. 
      * This method runs in the GUI thread.
      * @param menuItem the menu item to check
      * @return <code>true</code> if <code>menuItem</code> is a separator item.
      *         Otherwise <code>false</code>.
      */
     private static boolean isSeparator(final MenuItem menuItem) {
         final Boolean isSeparator = (Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".isSeparator", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return (menuItem.getStyle() & SWT.SEPARATOR) != 0 
                         ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return isSeparator.booleanValue();
     }
     
    
 
     /**
      * Clicks on a menu item
      * 
      * @param robot the robot
      * @param item the menu item
      * @param clickCount the number of times to click the menu item
      */
     public static void clickMenuItem(IRobot robot, final MenuItem item, 
             int clickCount) {
         
         if (!isMenuItemEnabled(item)) {
             throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.MENU_ITEM_NOT_ENABLED));
         }
 
         boolean isSecondInMenu = ((Boolean) EVENT_THREAD_QUEUER.invokeAndWait(
                 "isMenuBar", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         try {                            
                            if ((item.getParent().getParentMenu().getStyle() 
                                    & SWT.BAR) != 0) {
                                 return Boolean.TRUE;
                             }
                             Menu parent = item.getMenu().getParentMenu();
                             if (parent != null) {                            
                                 Menu preparent = parent.getParentMenu();
 
                                 if (preparent != null) {
                                     return (preparent.getStyle() & SWT.BAR) 
                                             != 0 
                                             ? Boolean.TRUE : Boolean.FALSE;
                                 }
                             }
                         } catch (NullPointerException ne) {
                             // Nothing here, there is no parent of parent.
                         }
                         return Boolean.FALSE;
                     }    
                 })).booleanValue();
         if (isSecondInMenu) {
             robot.click(item, null, 
                     ClickOptions.create()
                     .setClickType(ClickOptions.ClickType.RELEASED)
                     .setStepMovement(true).setClickCount(clickCount)
                     .setFirstHorizontal(false)); 
 
         } else {
             robot.click(item, null, 
                     ClickOptions.create()
                     .setClickType(ClickOptions.ClickType.RELEASED)
                     .setStepMovement(true).setClickCount(clickCount));
         }  
         
     }
     
     /**
      * select MenuItem programatically (for Mac OS)
      * @param menuItem the MenuItem
      */
     public static void selectProgramatically(final MenuItem menuItem) {
         if (!isMenuItemEnabled(menuItem)) {
             throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.MENU_ITEM_NOT_ENABLED));
         }
         
         final InterceptorOptions options = new InterceptorOptions(
                 new long[]{SWT.Selection});
         final IEventMatcher matcher = 
             new SelectionSwtEventMatcher();        
         RobotFactorySwtImpl robotSwt = new RobotFactorySwtImpl();
         IRobotEventInterceptor interceptor =
             robotSwt.getRobotEventInterceptor();        
         final IRobotEventConfirmer confirmer = interceptor
             .intercept(options);
         
         final Event event = new Event();
         event.time = (int) System.currentTimeMillis();
         event.widget = menuItem;
         event.display = menuItem.getDisplay();
         event.type = SWT.Selection;
         
         EVENT_THREAD_QUEUER.invokeLater(
                 "selectProgramatically", new Runnable() { //$NON-NLS-1$
                     public void run() {  
                         //if menuitem is checkbox or radiobutton set Selection
                         if ((menuItem.getStyle() & SWT.CHECK) == 0
                                 || (menuItem.getStyle() & SWT.RADIO) == 0) {
                             if (menuItem.getSelection()) {
                                 menuItem.setSelection(false);
                             } else {
                                 menuItem.setSelection(true);
                             }                            
                         }
 
                         menuItem.notifyListeners(SWT.Selection, event);
                     }            
                 });
 
         try {
             confirmer.waitToConfirm(menuItem, matcher);
         } catch (RobotException re) {
             final StringBuffer sb = new StringBuffer(
                 "Robot exception occurred while clicking...\n"); //$NON-NLS-1$
             //logRobotException(menuItem, re, sb);
             sb.append("Component: "); //$NON-NLS-1$
 
             EVENT_THREAD_QUEUER.invokeAndWait(
                 "getBounds", new IRunnable() { //$NON-NLS-1$
                     public Object run()
                         throws StepExecutionException {
                         sb.append(menuItem);
                         // Return value not used
                         return null;
                     }
                 });
             log.error(sb.toString(), re);
             throw re;
         }
     
     }
     
     /**
      * open SubMenu programatically (for Mac OS)
      * @param menu the Menu
      */
     public static void openSubMenuProgramatically(final Menu menu) {
         if (!isMenuEnabled(menu)) {
             throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.MENU_ITEM_NOT_ENABLED));
         }
         
         final InterceptorOptions options = new InterceptorOptions(
                 new long[]{SWT.Show});
         final IEventMatcher matcher = 
             new ShowSwtEventMatcher();  
         RobotFactorySwtImpl robotSwt = new RobotFactorySwtImpl();
         IRobotEventInterceptor interceptor =
             robotSwt.getRobotEventInterceptor();
         final IRobotEventConfirmer confirmer = interceptor
             .intercept(options);
         
         final Event event = new Event();
         event.time = (int) System.currentTimeMillis();
         event.widget = menu;
         event.display = menu.getDisplay();
         event.type = SWT.Show;
         
         EVENT_THREAD_QUEUER.invokeAndWait(
                 "openSubMenuProgramatically", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         
                         menu.notifyListeners(SWT.Show, event);
                         
                         return null;
                     }            
                 });
 
         try {
             confirmer.waitToConfirm(menu, matcher);
         } catch (RobotException re) {
             final StringBuffer sb = new StringBuffer(
                 "Robot exception occurred while clicking...\n"); //$NON-NLS-1$
             //logRobotException(menuItem, re, sb);
             sb.append("Component: "); //$NON-NLS-1$
 
             EVENT_THREAD_QUEUER.invokeAndWait(
                     "getBounds", new IRunnable() { //$NON-NLS-1$
                         public Object run()
                             throws StepExecutionException {
                             sb.append(menu);
                             // Return value not used
                             return null;
                         }
                     });
             log.error(sb.toString(), re);
             throw re;
         }
     }
 
     /**
      * Calls MenuItem.isEnabled() in the GUI-Thread
      * @param menuItem the MenuItem
      * @return true if enabled, false otherwise
      * @see MenuItem#isEnabled()
      */
     public static boolean isMenuItemEnabled(final MenuItem menuItem) {
         final Boolean isEnabled = (Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".isMenuItemEnabled", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return menuItem.isEnabled() ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return isEnabled.booleanValue();
     }
     
     /**
      * Calls MenuItem.isEnabled() in the GUI-Thread
      * @param menu the Menu
      * @return true if enabled, false otherwise
      * @see MenuItem#isEnabled()
      */
     public static boolean isMenuEnabled(final Menu menu) {
         final Boolean isEnabled = (Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".isMenuEnabled", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return menu.isEnabled() ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return isEnabled.booleanValue();
     }
     
     /**
      * Calls MenuItem.getSelection() in the GUI-Thread
      * @param menuItem the MenuItem
      * @return true if selected, false otherwise
      * @see MenuItem#getSelection()
      */
     public static boolean isMenuItemSelected(final MenuItem menuItem) {
         final Boolean isSelected = (Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".isMenuItemSelected", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return menuItem.getSelection() 
                         ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return isSelected.booleanValue();
     }
     
     
     /**
      * 
      * @param menu a Menu
      * @return true if the given Menu is visible, false otherwise.<br>
      * This method runs in the GUI-Thread.
      * @see Menu#isVisible()
      */
     public static boolean isMenuVisible(final Menu menu) {
         final Boolean isVisible = (Boolean)EVENT_THREAD_QUEUER.invokeAndWait(
             CLASSNAME + ".isMenuVisible", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     return menu.isVisible() ? Boolean.TRUE : Boolean.FALSE;
                 }
             });
         return isVisible.booleanValue();
     }
     
     /**
      * 
      * @param item MenuItem
      * @return bounds of MenuItem
      */
     public static Rectangle getMenuItemBounds(final MenuItem item) {
         Rectangle bounds = (Rectangle)EVENT_THREAD_QUEUER.invokeAndWait(
                 "getMenuItemBounds", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return SwtUtils.getBounds(item);
                     }            
                 });        
         return bounds;
     }
     
     /**
      * @param robot the IRobot
      * @param menu the Menu
      * @param name the name of the MenuItem
      * @param operator operator used for matching
      * @param maxCascadeLength the maximum number of submenus that may be open
      */
     public static void closeMenu(IRobot robot, Menu menu, String name, 
         String operator, int maxCascadeLength) {
         
         MenuItem item = findMenuItem(menu, name, operator);
         closeMenu(robot, item, maxCascadeLength);
     }
     
     /**
      * @param robot the IRobot
      * @param menu the Menu
      * @param index the index of the MenuItem
      * @param maxCascadeLength the maximum number of submenus that may be open
      */
     public static void closeMenu(IRobot robot, Menu menu, int index, 
         int maxCascadeLength) {
         
         MenuItem item = findMenuItem(menu, index);
         closeMenu(robot, item, maxCascadeLength);
     }
 
     /**
      * @param robot the IRobot
      * @param menu the Menu
      * @param maxCascadeLength the maximum number of submenus that may be open
      */
     public static void closeMenu(IRobot robot, final Menu menu, 
             int maxCascadeLength) {
 
         Validate.notNull(menu);
         final MenuHiddenListener menuListener = 
                 new MenuHiddenListener();
         menu.getDisplay().syncExec(new Runnable() {
             public void run() {
                 menu.addMenuListener(menuListener);
             }
         });
 
         // Press 'ESC' key until the first menu is gone or we reach
         // the maxCascadeLength. This prevents infinite loops if this
         // is used on a platform that does not use 'ESC' to close menus.
         for (int i = 0; 
             i < maxCascadeLength && !menuListener.isMenuHidden();
             i++) {
 
             robot.keyType(menu, SWT.ESC);
         }
     }
 
     /**
      * @param robot the IRobot
      * @param item the MenuItem which is to close
      * @param maxCascadeLength the maximum number of submenus that may be open
      */
     private static void closeMenu(IRobot robot, MenuItem item, int 
         maxCascadeLength) {
         
         if (item != null) {
             final Menu menuOfItem = getMenu(item);
             if (menuOfItem != null) {
                 closeMenu(robot, menuOfItem, maxCascadeLength);
             }
         }
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
      * Splits a path into integers
      * 
      * @param path the path
      * @return an array of int values
      */
     public static int[] splitIndexPath(String path) {
         return MenuUtilBase.splitIndexPath(path);
     }
 
     
 
    
     
 }

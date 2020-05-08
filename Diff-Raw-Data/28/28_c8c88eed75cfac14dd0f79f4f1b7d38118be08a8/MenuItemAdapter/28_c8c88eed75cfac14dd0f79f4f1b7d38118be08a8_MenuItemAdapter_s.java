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
 package org.eclipse.jubula.rc.swt.uiadapter;
 
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
 import org.eclipse.jubula.rc.common.listener.EventLock;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.IMenuAdapter;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.IMenuItemAdapter;
 import org.eclipse.jubula.rc.swt.driver.EventThreadQueuerSwtImpl;
 import org.eclipse.jubula.rc.swt.driver.RobotFactorySwtImpl;
 import org.eclipse.jubula.rc.swt.driver.SelectionSwtEventMatcher;
 import org.eclipse.jubula.rc.swt.driver.ShowSwtEventMatcher;
 import org.eclipse.jubula.rc.swt.implclasses.EventListener;
 import org.eclipse.jubula.rc.swt.implclasses.EventListener.Condition;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.TimeoutConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 
 /**
  * Implements the MenuItem interface for adapting a <code>SWT.MenuItem</code>
  * 
  *  @author BREDEX GmbH
  */
 public class MenuItemAdapter extends AbstractComponentAdapter
     implements IMenuItemAdapter {
 
     /** the MenuItem from the AUT*/
     private MenuItem m_menuItem;
     
 
     /**
      * 
      * @param component graphics component which will be adapted
      */
     public MenuItemAdapter(Object component) {
         super();
         m_menuItem = (MenuItem) component;
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
     public Object getRealComponent() {
        
         return m_menuItem;
     }
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object element) {
         m_menuItem = (MenuItem) element;
 
     }
     /**
      * {@inheritDoc}
      */
     public String getText() {
         return (String) getEventThreadQueuer().invokeAndWait(
                 "getText", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                        return m_menuItem.getText();
                     }
                 });
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isEnabled() {
         final Boolean isEnabled = (Boolean) getEventThreadQueuer()
                 .invokeAndWait("isEnabled", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             return m_menuItem.isEnabled() ? Boolean.TRUE
                                         : Boolean.FALSE;
                         }
                     });
         return isEnabled.booleanValue();
 
     }
     /**
      * {@inheritDoc}
      */
     public boolean isExisting() {
         if (m_menuItem != null) {
             return true;
         }
         return false;
     }
     /**
      * {@inheritDoc}
      */
     public boolean isSelected() {
         final Boolean isSelected = (Boolean)getEventThreadQueuer()
                 .invokeAndWait("isSelected", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return m_menuItem.getSelection() 
                             ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return isSelected.booleanValue();
     }
 
     /**
      * @return -
      */
     public boolean isShowing() {
 
         return true; //FIXME is here a Showing implementation?
     }
     /**
      * {@inheritDoc}
      */
     public IMenuAdapter getMenu() {
         
         Menu menu =
                 (Menu) getEventThreadQueuer().invokeAndWait(
                         "getItems", new IRunnable() { //$NON-NLS-1$
                             public Object run() {
                                 return m_menuItem.getMenu();
                             }
                         });
         
         
         return new MenuAdapter(menu);
     }
     /**
      * {@inheritDoc}
      */
     public boolean hasSubMenu() {
         
 
         
         if (getMenu() != null) {
             return true;
         }
         return false;
     }
 
 
     /**
      * Checks whether the given menu item is a separator. 
      * This method runs in the GUI thread.
      * @return <code>true</code> if <code>menuItem</code> is a separator item.
      *         Otherwise <code>false</code>.
      */
     public boolean isSeparator() {
         final Boolean isSeparator = (Boolean)getEventThreadQueuer()
                 .invokeAndWait(
                 ".isSeparator", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return (m_menuItem.getStyle() & SWT.SEPARATOR) != 0 
                                             ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return isSeparator.booleanValue();
     }
     
     /**
      * {@inheritDoc}
      */
     public void selectMenuItem() {
         Rectangle bounds = getMenuItemBounds();
         Rectangle nullBounds = new Rectangle(0, 0, 0, 0);
         
         if (bounds.equals(nullBounds)) {
             selectProgramatically();
         } else {
             clickMenuItem(getRobot(), m_menuItem, 1);
         }
         
     }
     
     /**
      * {@inheritDoc}
      */
     public IMenuAdapter openSubMenu() {
         final MenuItem menuItem = m_menuItem;
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
             Menu menu = (Menu)getEventThreadQueuer().invokeAndWait(
                     "openSubMenu", new IRunnable() { //$NON-NLS-1$
                         public Object run() {
                             return menuItem.getMenu();
                         }            
                     });
             Rectangle bounds = getMenuItemBounds();
             Rectangle nullBounds = new Rectangle(0, 0, 0, 0);            
             if (bounds.equals(nullBounds)) {                               
                 openSubMenuProgramatically(menu);
             } else {
                 clickMenuItem(getRobot(), menuItem, clickCount);
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
         } catch (InterruptedException e) { // ignore
         } finally {
             queuer.invokeAndWait("removeMenuShownListeners", new IRunnable() { //$NON-NLS-1$
                 public Object run() {
                     d.removeFilter(SWT.Show, listener);
                     
                     return null;
                 }
             });
         }
         if (!lock.isReleased()) {
             String itemText = (String)getEventThreadQueuer().invokeAndWait(
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
         return new MenuAdapter(cond.getMenu());
     }
     
     /**
      * @param menuItem the menu item to check
      * @return <code>true</code> of the given menu item is part of a menu
      *         bar. Otherwise, <code>false</code>.
      */
     private boolean isMenuBarItem(final MenuItem menuItem) {
         return ((Boolean)getEventThreadQueuer().invokeAndWait(
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
      * Clicks on a menu item
      * 
      * @param robot the robot
      * @param item the menu item
      * @param clickCount the number of times to click the menu item
      */
     public static void clickMenuItem(IRobot robot, final MenuItem item, 
             int clickCount) {
         // FIXME existiert so schon und kann man benutzen
 //        if (!isMenuItemEnabled(item)) {
 //            throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
 //                    EventFactory.createActionError(
 //                            TestErrorEvent.MENU_ITEM_NOT_ENABLED));
 //        }
 
         robot.click(item, null, 
             ClickOptions.create()
                 .setClickType(ClickOptions.ClickType.RELEASED)
                 .setStepMovement(false).setClickCount(clickCount));
         
     }
     /**
      * 
      * @return bounds of MenuItem
      */
     public Rectangle getMenuItemBounds() {
         Rectangle bounds = (Rectangle)getEventThreadQueuer().invokeAndWait(
                 "getMenuItemBounds", new IRunnable() { //$NON-NLS-1$
                     public Object run() {
                         return SwtUtils.getBounds(m_menuItem);
                     }            
                 });        
         return bounds;
     }
     
     /**
      * open SubMenu programatically (for Mac OS)
      * @param menu the Menu
      */
     public void openSubMenuProgramatically(final Menu menu) {
 //        if (!isMenuEnabled(menu)) {
 //            throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
 //                    EventFactory.createActionError(
 //                            TestErrorEvent.MENU_ITEM_NOT_ENABLED));
 //        }
         
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
         
         getEventThreadQueuer().invokeAndWait(
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
 //            logRobotException(menuItem, re, sb);
             sb.append("Component: "); //$NON-NLS-1$
 
             getEventThreadQueuer().invokeAndWait(
                     "getBounds", new IRunnable() { //$NON-NLS-1$
                         public Object run()
                             throws StepExecutionException {
                             sb.append(menu);
                             // Return value not used
                             return null;
                         }
                     });
 //            log.error(sb.toString(), re);
             throw re;
         }
     }
     
     /**
      * select MenuItem programatically (for Mac OS)
      */
     public void selectProgramatically() {
 //        FIXME must implement this check case
 //        if (!isMenuItemEnabled(menuItem)) {
 //            throw new StepExecutionException("menu item not enabled", //$NON-NLS-1$
 //                    EventFactory.createActionError(
 //                            TestErrorEvent.MENU_ITEM_NOT_ENABLED));
 //        }
         final MenuItem menuItem = m_menuItem;
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
         
         getEventThreadQueuer().invokeLater(
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
 
             getEventThreadQueuer().invokeAndWait(
                 "getBounds", new IRunnable() { //$NON-NLS-1$
                     public Object run()
                         throws StepExecutionException {
                         sb.append(menuItem);
                         // Return value not used
                         return null;
                     }
                 });
 //  FIXME LOG IS MISSING HERE
 //            log.error(sb.toString(), re);
             throw re;
         }
     
     }
     
     
 }

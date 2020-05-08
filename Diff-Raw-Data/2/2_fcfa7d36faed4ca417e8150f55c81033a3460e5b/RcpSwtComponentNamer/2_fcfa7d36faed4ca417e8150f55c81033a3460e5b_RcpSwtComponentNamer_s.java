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
 package org.eclipse.jubula.rc.rcp.swt.aut;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jubula.rc.swt.listener.ComponentHandler;
 import org.eclipse.jubula.tools.constants.AutEnvironmentConstants;
 import org.eclipse.jubula.tools.constants.SwtAUTHierarchyConstants;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * This listener assigns names to components as they become visible. The
  * assigned name is determined by using the component and its surroundings.
  *
  * @author BREDEX GmbH
  * @created Oct 19, 2007, 2013
  */
 public abstract class RcpSwtComponentNamer implements Listener {
 
     /** ID suffix for tool bars belonging to a part (view/editor) */
     private static final String TOOLBAR_ID_SUFFIX = ".toolbar"; //$NON-NLS-1$
 
     /** Key for RCP partId in component data */
     private static final String TEST_RCP_DATA_KEY =
             SwtAUTHierarchyConstants.RCP_NAME;
 
     /** map for naming dialog buttons */
     private static Map componentNAMES = new HashMap();
 
     /** is name generation enabled */
     private static boolean generateNames = false;
 
     static {
         generateNames = Boolean.valueOf(
                 EnvironmentUtils.getProcessEnvironment().getProperty(
                         AutEnvironmentConstants.GENERATE_COMPONENT_NAMES))
                 .booleanValue();
 
         addCompName(IDialogConstants.ABORT_ID, "abort"); //$NON-NLS-1$
         addCompName(IDialogConstants.BACK_ID, "back"); //$NON-NLS-1$
         addCompName(IDialogConstants.CANCEL_ID, "cancel"); //$NON-NLS-1$
         addCompName(IDialogConstants.CLIENT_ID, "client"); //$NON-NLS-1$
         addCompName(IDialogConstants.CLOSE_ID, "close"); //$NON-NLS-1$
         addCompName(IDialogConstants.DESELECT_ALL_ID, "deselectAll"); //$NON-NLS-1$
         addCompName(IDialogConstants.DETAILS_ID, "details"); //$NON-NLS-1$
         addCompName(IDialogConstants.FINISH_ID, "finish"); //$NON-NLS-1$
         addCompName(IDialogConstants.HELP_ID, "help"); //$NON-NLS-1$
         addCompName(IDialogConstants.IGNORE_ID, "ignore"); //$NON-NLS-1$
         addCompName(IDialogConstants.INTERNAL_ID, "internal"); //$NON-NLS-1$
         addCompName(IDialogConstants.NEXT_ID, "next"); //$NON-NLS-1$
         addCompName(IDialogConstants.NO_ID, "no"); //$NON-NLS-1$
         addCompName(IDialogConstants.NO_TO_ALL_ID, "noToAll"); //$NON-NLS-1$
         addCompName(IDialogConstants.OK_ID, "ok"); //$NON-NLS-1$
         addCompName(IDialogConstants.OPEN_ID, "open"); //$NON-NLS-1$
         addCompName(IDialogConstants.PROCEED_ID, "proceed"); //$NON-NLS-1$
         addCompName(IDialogConstants.RETRY_ID, "retry"); //$NON-NLS-1$
         addCompName(IDialogConstants.SELECT_ALL_ID, "selectAll"); //$NON-NLS-1$
         addCompName(IDialogConstants.SELECT_TYPES_ID, "selectTypes"); //$NON-NLS-1$
         addCompName(IDialogConstants.SKIP_ID, "skip"); //$NON-NLS-1$
         addCompName(IDialogConstants.STOP_ID, "stop"); //$NON-NLS-1$
         addCompName(IDialogConstants.YES_ID, "yes"); //$NON-NLS-1$
         addCompName(IDialogConstants.YES_TO_ALL_ID, "yesToAll"); //$NON-NLS-1$
     }
 
     /**
      * add component id <-> name mapping
      *
      * @param compID
      *            the component identifier
      * @param compName
      *            the component name
      */
     private static void addCompName(int compID, String compName) {
         String staticNamePreafix = "dialog.button."; //$NON-NLS-1$
         componentNAMES.put(new Integer(compID), staticNamePreafix + compName);
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleEvent(Event event) {
         addNameData(event.widget);
         Item[] items;
         if (event.widget instanceof ToolBar) {
             items = ((ToolBar) event.widget).getItems();
         } else if (event.widget instanceof CoolBar) {
             items = ((CoolBar) event.widget).getItems();
         } else {
             return;
         }
         for (int i = 0; i < items.length; i++) {
             addNameData(items[i]);
         }
     }
 
     /**
      * Adds name information to the given widget, if necessary.
      *
      * @param widget
      *            The widget to name.
      */
     private void addNameData(Widget widget) {
         // Assign name
         if (hasWidgetToBeNamed(widget)) {
             Object data = getWidgetData(widget);
 
             if (data instanceof IContributionItem) {
                 // Name buttons and toolitems according to the action that
                 // they represent, if possible.
                 String actionId = ((IContributionItem) data).getId();
                 if (actionId != null && actionId.trim().length() > 0) {
                     setComponentName(widget, actionId);
                     ComponentHandler.getAutHierarchy().refreshComponentName(
                             widget);
 
                 }
             } else if (data instanceof PreferenceDialog) {
                 PreferenceDialog prefDialog = (PreferenceDialog) data;
 
                 // Add a listener to add name data as pages are
                 // selected/created.
                 prefDialog.addPageChangedListener(new IPageChangedListener() {
 
                     public void pageChanged(PageChangedEvent event) {
                         addNameDataToPrefPage(event.getSelectedPage());
                     }
 
                 });
 
                 // The listener won't notice the initally selected page,
                 // so we have to add that name data here.
                 addNameDataToPrefPage(prefDialog.getSelectedPage());
             }
             if (generateNames && data instanceof Dialog) {
                 Dialog dialog = (Dialog) data;
                 setNameForDialogButtonBarButtons(dialog);
             }
         }
     }
 
     /**
      *
      * @param widget
      *            The widget for which to get the data.
      * @return the data object corresponding to the given widget.
      */
     private static Object getWidgetData(Widget widget) {
         Object data = widget.getData();
 
         // Handle the case of CoolBar containing CoolItem containing ToolBar.
         // The CoolItem is the widget that represents the toolbar
         // contribution, but it (the CoolItem) is not in our AUT
         // component hierarchy, due to the fact that the ToolBar's
         // getParent() returns the CoolBar rather than the CoolItem.
         // To resolve this discrepancy, we use the data from the
         // coresponding CoolItem to generate a name for the ToolBar.
         try {
             if (widget instanceof ToolBar) {
                 Composite toolbarParent = ((ToolBar) widget).getParent();
                 if (toolbarParent instanceof CoolBar) {
                     CoolItem[] coolItems = ((CoolBar) toolbarParent).getItems();
                     for (int i = 0; i < coolItems.length; i++) {
                         CoolItem item = coolItems[i];
                         if (item != null && item.getControl() == widget) {
                             data = item.getData();
                         }
                     }
                 }
             }
         } catch (NoClassDefFoundError e) {
             // we may be running in eRCP which doesn't know
             // about
             // toolbars, so we just ignore this
         }
 
         return data;
     }
 
     /**
      * @param dialog
      *            the dialog
      */
     private void setNameForDialogButtonBarButtons(Dialog dialog) {
         try {
             Method getButtonMethod = Dialog.class.getDeclaredMethod(
                     "getButton", new Class[] { int.class }); //$NON-NLS-1$
             getButtonMethod.setAccessible(true);
 
             Iterator components = componentNAMES.keySet().iterator();
             while (components.hasNext()) {
                 Integer componentID = (Integer) components.next();
                 invokeNameSetting(dialog, getButtonMethod, componentID,
                         componentNAMES.get(componentID));
             }
         } catch (SecurityException e) {
             // ignore exceptions
         } catch (NoSuchMethodException e) {
             // ignore exceptions
         }
     }
 
     /**
      * use this method to set a name on the given object
      *
      * @param useObject
      *            the object
      * @param methodToInvoke
      *            the method to invoke
      * @param buttonID
      *            the button id
      * @param buttonName
      *            the button name
      */
     private static void invokeNameSetting(
             Object useObject, Method methodToInvoke,
             Integer buttonID, Object buttonName) {
         Object ret = null;
         try {
             ret = methodToInvoke.invoke(useObject, new Object[] { buttonID });
         } catch (IllegalArgumentException e) {
             // ignore exceptions
         } catch (IllegalAccessException e) {
             // ignore exceptions
         } catch (InvocationTargetException e) {
             // ignore exceptions
         }
         if (ret instanceof Button) {
             Button button = (Button) ret;
             if (hasWidgetToBeNamed(button)) {
                 setComponentName(button, buttonName.toString());
             }
         }
     }
 
     /**
      * Attaches name data to the given page appropriate.
      *
      * @param selectedPage
      *            The page to which we will try to attach the name data.
      */
     private void addNameDataToPrefPage(Object selectedPage) {
         if (selectedPage == null) {
             return;
         }
         PreferenceManager prefMan = getPreferenceManager();
         if (prefMan == null) {
             return;
         }
 
         Iterator iter = prefMan.getElements(PreferenceManager.PRE_ORDER)
                 .iterator();
         while (iter.hasNext()) {
             IPreferenceNode prefNode = (IPreferenceNode) iter.next();
             if (selectedPage.equals(prefNode.getPage())) {
                 Control pageControl = prefNode.getPage().getControl();
                 String prefNodeId = prefNode.getId();
                 // Assign id to page composite only if the composite exists
                 // and if the id is usable
                 if (hasWidgetToBeNamed(pageControl)
                         && prefNodeId != null
                         && prefNodeId.trim().length() > 0) {
                     setComponentName(pageControl, prefNodeId);
                     Shell prefShell = pageControl.getDisplay().getActiveShell();
                     Event activateEvent = new Event();
                     activateEvent.time = (int) System.currentTimeMillis();
                     activateEvent.type = SWT.Activate;
                     activateEvent.widget = prefShell;
                     prefShell.notifyListeners(SWT.Activate, activateEvent);
                 }
                 // We found the page we were looking for, so we can stop
                 // searching.
                 break;
             }
         }
     }
 
     /**
      * @param widget
      *            The SWT widget to look at.
      * @return True, if the given SWT widget is not null, is not disposed and the component
      *         name has not been set, otherwise false.
      */
     public static boolean hasWidgetToBeNamed(Widget widget) {
         boolean hasToBeNamed =
                 // exists
                 widget != null
                 // is not destroyed
                 && !widget.isDisposed()
                 // the test component name has not been set
                 && widget.getData(TEST_RCP_DATA_KEY) == null;
         return hasToBeNamed;
     }
 
     /**
      * Sets the given component name id to the given widget.
      * @param widget
      *            The widget setting the id on.
      * @param id
      *            The id to set to the widget.
      */
     public static void setComponentName(Widget widget, String id) {
         widget.setData(TEST_RCP_DATA_KEY, id);
     }
 
     /**
      * Set the component name of a tool bar. Calls {@link #setComponentName(Widget, String)}
      * with appending the suffix tool bar to the finalPartId.
      * @param partToolbar The part of the tool bar.
      * @param finalPartId The ID for the part of the tool bar.
      */
     public static void setToolbarComponentName(Widget partToolbar,
             String finalPartId) {
        RcpSwtComponentNamer.setToolbarComponentName(
                 partToolbar,
                 finalPartId + TOOLBAR_ID_SUFFIX);
     }
 
     /**
      * @return An instance of the preference manager, or null if not available.
      *         This method must be implemented depending on Eclipse RCP e3 and e4.
      */
     protected abstract PreferenceManager getPreferenceManager();
 
 }

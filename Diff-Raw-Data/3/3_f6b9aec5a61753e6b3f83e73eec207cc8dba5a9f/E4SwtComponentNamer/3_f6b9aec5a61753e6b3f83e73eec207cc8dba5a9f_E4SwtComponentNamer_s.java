 /*******************************************************************************
  * Copyright (c) 2004, 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.rcp.e4.swt.namer;
 
 import org.eclipse.e4.ui.model.application.ui.MUIElement;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
 import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
 import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
 import org.eclipse.jubula.rc.rcp.e4.namer.E4ComponentNamer;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jubula.rc.rcp.swt.aut.RcpSwtComponentNamer;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Specific implementation of a component namer for RCP-E4-SWT applications.
  * used by {@link org.eclipse.jubula.rc.rcp.e4.swt.starter.SwtEventBrokerListener}.
  */
 public class E4SwtComponentNamer extends RcpSwtComponentNamer
     implements E4ComponentNamer {
 
     /**
      * Set the name of the control by using the element id of the given part stack.
      * Called, when the given part stack of the application model has been created.
      * @param mPartStack The opened part stack of the application model.
      */
     public void onModelPartStackCreated(MPartStack mPartStack) {
     }
 
     /**
      * Set the name of the tool bar by using the elementId.
      * @param mToolBar The created tool item of the application model.
      */
     public void onModelToolBarCreated(MToolBar mToolBar) {
         if (mToolBar.getWidget() instanceof ToolBar) {
             onModelElementCreated(mToolBar);
         }
     }
 
     /**
      * Set the name of the tool item by using the command name.
      * @param mToolItem The created tool item of the application model.
      */
     public void onModelToolItemCreated(MToolItem mToolItem) {
         if (mToolItem.getWidget() instanceof ToolItem) {
             onModelElementCreated(mToolItem);
         }
     }
 
     /**
      * Set the name of the control by using the element id of the given part.
      * Called, when the given part of the application model has been created.
      * @param mPart The opened part of the application model.
      */
     public void onModelPartCreated(MPart mPart) {
     }
 
     /**
      * Set the name of the control by using the element id of the given part stack.
      * Called, when the given part stack of the application model has been created.
      * @param mElement The opened part stack of the application model.
      */
     public void onModelElementCreated(MUIElement mElement) {
         Widget widget = (Widget) mElement.getWidget();
         if (hasWidgetToBeNamed(widget)) {
             // Name control based on element ID
             String componentName = mElement.getElementId();
             if (componentName != null) {
                 setComponentName(widget, componentName);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * Not implemented yet.
      */
     @Override
     protected PreferenceManager getPreferenceManager() {
         return null;
     }
 
 }

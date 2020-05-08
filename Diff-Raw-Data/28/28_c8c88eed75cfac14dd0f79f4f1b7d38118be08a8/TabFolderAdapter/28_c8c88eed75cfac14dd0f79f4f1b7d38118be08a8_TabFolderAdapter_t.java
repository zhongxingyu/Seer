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
 
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.ITabPaneAdapter;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.swt.widgets.TabFolder;
 /**
  * Implementation of the Interface <code>ITabPane</code> as a
  * adapter for the <code>TabFolder</code> component.
  * @author BREDEX GmbH
  *
  */
 public class TabFolderAdapter extends WidgetAdapter implements ITabPaneAdapter {
 
     /** the tabFolder from the AUT */
     private TabFolder m_tabFolder;
     
     /**
      * 
      * @param objectToAdapt the component from the AUT
      */
     public TabFolderAdapter(Object objectToAdapt) {
         super(objectToAdapt);
         m_tabFolder = (TabFolder) objectToAdapt;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getTabCount() {
         return ((Integer)getEventThreadQueuer().invokeAndWait(
                 "getSelectedIndex", //$NON-NLS-1$
                 new IRunnable() {
 
                 public Object run() throws StepExecutionException {
                     return new Integer(m_tabFolder.getItemCount());
                 }
             })).intValue();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getTitleofTab(final int index) {
         return (String)getEventThreadQueuer().invokeAndWait(
                 "verifyTextOfTabByIndex", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                        return SwtUtils.removeMnemonics(m_tabFolder
                                .getItem(index).getText());
                     }
                 });
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getBoundsAt(final int index) {
         return getEventThreadQueuer().invokeAndWait("getBoundsAt", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         return SwtUtils.getRelativeWidgetBounds(
                                 m_tabFolder.getItem(index), m_tabFolder);
                     }
                 });
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isEnabledAt(final int index) {
         return ((Boolean) getEventThreadQueuer().invokeAndWait("isEnabledAt", //$NON-NLS-1$
                 new IRunnable() {
                 public Object run() throws StepExecutionException {
                     return m_tabFolder.getItem(index).getControl()
                             .isEnabled() ? Boolean.TRUE : Boolean.FALSE;
                 }        
             })).booleanValue();
     }
 
     /**
      * {@inheritDoc}
      */
     public int getSelectedIndex() {
         return ((Integer)getEventThreadQueuer().invokeAndWait(
                 "getSelectedIndex", //$NON-NLS-1$
                 new IRunnable() {
 
                 public Object run() throws StepExecutionException {
                     return new Integer(m_tabFolder.getSelectionIndex());
                 }
             })).intValue();
     }
 
 }

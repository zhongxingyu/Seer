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
 package org.eclipse.jubula.client.ui.properties;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.dialogs.AUTPropertiesDialog;
 import org.eclipse.jubula.client.ui.dialogs.JBPropertyDialog;
 import org.eclipse.jubula.client.ui.editors.ObjectMappingMultiPageEditor;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.ui.IEditorReference;
 
 
 /**
  * This is the class for the AUT property page of a project.
  *
  * @author BREDEX GmbH
  * @created 08.02.2005
  */
 public class AUTPropertyPage extends AbstractProjectPropertyPage {
     
     /** number of columns = 1 */
     private static final int NUM_COLUMNS_1 = 1;   
     /** number of columns = 2 */
     private static final int NUM_COLUMNS_2 = 2; 
     /** the quantity of lines in a m_text field */
     private static final int LINES = 15;
     
     /** the add button */
     private Button m_addButton = null;
     
     /** the delete button */
     private Button m_removeButton = null;
     
     /** the edit button */
     private Button m_editButton = null;
     
     /** the list of AUTs of a project */
     private List m_autList = null;
         
     /** a new selection listener */
     private WidgetSelectionListener m_selectionListener = 
         new WidgetSelectionListener();
     
     /**
      * @param es the editSupport
      */
     public AUTPropertyPage(EditSupport es) {
         super(es);
     }
 
     /**
      * {@inheritDoc}
      */
     protected Control createContents(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         GridData compositeData = new GridData(SWT.FILL, SWT.FILL, true, true);
         createCompositeLayout(composite, compositeLayout, compositeData);
         composite.setLayoutData(compositeData);
         noDefaultAndApplyButton();
         createCompositeLayout(composite, compositeLayout, compositeData);
         createAUTList(composite);
         createButtons(composite);
         initFields();
         addListeners();
        Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.AUT_PROPERTY_PAGE);
         return composite;
     }
 
     /**
      * @param composite the composite
      * @param compositeLayout comp. layout
      * @param compositeData comp. data
      */
     private void createCompositeLayout(Composite composite, 
         GridLayout compositeLayout, GridData compositeData) {
         compositeData.grabExcessHorizontalSpace = false;
         compositeLayout.horizontalSpacing = Layout.SMALL_HORIZONTAL_SPACING;
         compositeLayout.verticalSpacing = Layout.SMALL_VERTICAL_SPACING;
         compositeLayout.numColumns = NUM_COLUMNS_2;
         compositeLayout.marginHeight = Layout.SMALL_MARGIN_HEIGHT;
         compositeLayout.marginWidth = Layout.SMALL_MARGIN_WIDTH;
         composite.setLayout(compositeLayout);
     }
     
     /**
      * Inits all swt field in this page.
      */
     private void initFields() {
         m_autList.removeAll();
         for (IAUTMainPO autW : getProject().getAutMainList()) {
             m_autList.add(autW.getName());
         }
         sortAUTList();
     }
         
     /**
      * Creates a m_text field with the AUTs of a project.
      * @param parent The parent composite.
      */
     private void createAUTList(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = NUM_COLUMNS_1;
         composite.setLayout(compositeLayout);
         GridData data = new GridData ();
         data.horizontalAlignment = SWT.FILL;
         data.grabExcessHorizontalSpace = true;
         composite.setLayoutData(data);
         newLabel(composite, Messages.AUTPropertyPageAUTList);
         m_autList = new List(composite, 
             Layout.MULTI_TEXT_STYLE | SWT.SINGLE);
         GridData textGridData = new GridData();
         textGridData.horizontalAlignment = GridData.FILL;
         textGridData.grabExcessHorizontalSpace = true;
         textGridData.heightHint = Dialog.convertHeightInCharsToPixels(
             Layout.getFontMetrics(m_autList), LINES);
         Layout.addToolTipAndMaxWidth(textGridData, m_autList);
         m_autList.setLayoutData(textGridData);       
     }
     
     /**
      * Creates three buttons.
      * @param parent The parent composite.
      */
     private void createButtons(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = NUM_COLUMNS_1;
         composite.setLayout(compositeLayout);
         GridData data = new GridData();
         data.verticalAlignment = GridData.BEGINNING;
         data.horizontalAlignment = GridData.END;
         composite.setLayoutData(data);
         new Label(composite, SWT.NONE);
         m_addButton = new Button(composite, SWT.PUSH);
         m_addButton.setText(Messages.AUTPropertyPageAdd);
         m_addButton.setLayoutData(buttonGrid());
         
         m_editButton = new Button(composite, SWT.PUSH);
         m_editButton.setText(Messages.AUTPropertyPageEdit);
         m_editButton.setLayoutData(buttonGrid());
         m_editButton.setEnabled(false);
         
         m_removeButton = new Button(composite, SWT.PUSH);
         m_removeButton.setText(Messages.AUTPropertyPageRemove);
         m_removeButton.setLayoutData(buttonGrid());
         m_removeButton.setEnabled(false);
     }
     
     /**
      * Creates new gridData for the buttons.
      * @return The new GridData.
      */
     private GridData buttonGrid() {
         GridData buttonData = new GridData();
         buttonData.horizontalAlignment = GridData.FILL;
         return buttonData;      
     }
     
     /**
      * Creates a label for this page.
      * @param text The label text to set.
      * @param parent The composite.
      * @return a new label
      */
     private Label newLabel(Composite parent, String text) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(text);
         GridData labelGrid = new GridData(GridData.BEGINNING, GridData.CENTER, 
             false , false, 1, 1);
         label.setLayoutData(labelGrid);
         return label;
     }
     
     /** Handels the add-button event. */
     void handleAddButtonEvent() {
         String[] selection = m_autList.getSelection();
         AUTPropertiesDialog dialog = new AUTPropertiesDialog(
             m_addButton.getShell(), false, null, getProject());
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         dialog.getShell().setText(Messages.AUTPropertyPageAUTConfig);
         dialog.open();
         if (dialog.getReturnCode() == Window.OK) {
             if (dialog.getAutMain().equals(
                 TestExecution.getInstance().getConnectedAut())) {
                 
                 ((JBPropertyDialog)getContainer()).setStartedAutChanged(true);
             }
 
             getProject().addAUTMain(dialog.getAutMain());
             m_autList.add(dialog.getAutMain().getName());
             sortAUTList();
             setFocus(new String[]{dialog.getAutMain().getName()});
             return;
         }
         setFocus(selection);
     }
 
     /**
      * sorts the AUTs according to the natural order of the elements
      */
     private void sortAUTList() {
         String[] sortedAUTList = m_autList.getItems();
         Arrays.sort(sortedAUTList);
         m_autList.removeAll();
         for (int i = 0; i < sortedAUTList.length; i++) {
             m_autList.add(sortedAUTList[i]);
         }
     }
     
     /** Handels the edit-button event. */
     void handleEditButtonEvent() {
         IAUTMainPO autMain = null;
         String[] selection = m_autList.getSelection();
         //can be 0
         if (selection.length == 0) {
             return;
         }
         String autName = selection[0];
         for (IAUTMainPO aut : getProject().getAutMainList()) {
             String autMainName = aut.getName();
             if (autName.equals(autMainName)) {
                 autMain = aut;
                 break;
             }
         }
         if (autMain != null) {
             AUTPropertiesDialog dialog = 
                 new AUTPropertiesDialog(m_editButton.getShell(),
                     true, autMain, getProject());
             dialog.create();
             DialogUtils.setWidgetNameForModalDialog(dialog);
             dialog.getShell().setText(
                     Messages.AUTPropertyPageAUTConfig);
             dialog.open();
             if (dialog.getReturnCode() == Window.OK) {
                 if (dialog.getAutMain().equals(
                     TestExecution.getInstance().getConnectedAut())) {
                     
                     ((JBPropertyDialog)getContainer()).setStartedAutChanged(
                         true);
                 }
                 initFields();
             }
             setFocus(new String[]{autMain.getName()});
         }
     }
     
     /** Handels the remove-button event. */
     void handleRemoveButtonEvent() {
         IAUTMainPO autMain = null;
         String[] selection = m_autList.getSelection();
         if (!StringConstants.EMPTY.equals(selection[0])) {
             for (IAUTMainPO aut : getProject().getAutMainList()) {
                 String autMainName = aut.getName();
                 if (selection[0].equals(autMainName)) {
                     autMain = aut;
                     break;
                 }
             }
             if (autMain != null) {
                 if (!checkTestSuiteAUT(autMain)) {
                     return;
                 }
                 closeEquivalentOMEditor(autMain);
                 getProject().removeAUTMain(autMain);
                 m_autList.remove(autMain.getName());
                 if (m_autList.getItemCount() > 0) {
                     m_autList.setSelection(0);
                 }
                 handleAutListEvent();
             }
         }
     }
     
     /**
      * Closes the equivalent OMEditor.
      * @param autMain The aut to delete.
      */
     private void closeEquivalentOMEditor(IAUTMainPO autMain) {
         IEditorReference[] editors = Plugin.getActivePage()
             .getEditorReferences();
         for (int i = 0; i < editors.length; i++) {
             if (editors[i].getPart(true) 
                     instanceof ObjectMappingMultiPageEditor) {
                 ObjectMappingMultiPageEditor omEditor = 
                     (ObjectMappingMultiPageEditor)editors[i].getPart(true);
                 if (omEditor.getAut().equals(autMain)) {
                     omEditor.getEditorSite().getPage().closeEditor(
                             omEditor, true);
                     return;
                 }
             }
         }
     }
 
     /**
      * @param autMain The selected AUT.
      * @return True, if the selected AUT is not used in any TestSuite.
      */
     private boolean checkTestSuiteAUT(IAUTMainPO autMain) {
         java.util.List<ITestSuitePO> tsList = getProject()
             .getTestSuiteCont().getTestSuiteList();
         java.util.List<String> tsNameList = new ArrayList<String>();
         for (ITestSuitePO ts : tsList) {    
             if (ts.getAut() != null && ts.getAut().equals(autMain)) {
                 tsNameList.add(ts.getName());
             }
         }
         if (tsNameList.size() > 0) {
             String error = tsNameList.toString();
             String output = error.substring(1, error.length() - 1);
             Utils.createMessageDialog(MessageIDs.E_CANNOT_DELETE_AUT, 
                     new Object[] {output}, null);
             return false;
         }
         return true;
     }
 
     /**
      * Sets the focus on the new/edited aut name.
      * 
      * @param autName
      *            The new/edited aut name.
      */
     private void setFocus(String[] autName) {
         m_autList.setSelection(autName);
         handleAutListEvent();
     }
     
     /** Handels the aut-list event. */
     void handleAutListEvent() {
         if (m_autList.getItemCount() == 0) {
             m_editButton.setEnabled(false);
             m_removeButton.setEnabled(false);
             return;
         }
         if (m_autList.getSelectionCount() > 0) {
             String[] selection = m_autList.getSelection();
             if (!StringConstants.EMPTY.equals(selection[0])) { 
                 m_editButton.setEnabled(true);
                 m_removeButton.setEnabled(true);
             }
         }
     }
     
     /** Adds all listeners. */
     private void addListeners() {
         m_addButton.addSelectionListener(m_selectionListener);
         m_editButton.addSelectionListener(m_selectionListener);
         m_removeButton.addSelectionListener(m_selectionListener);
         m_autList.addSelectionListener(m_selectionListener);
     }
  
     /**
      * This inner class creates a new SelectionListener.
      * @author BREDEX GmbH
      * @created 11.02.2005
      */
     private class WidgetSelectionListener 
         implements SelectionListener {
         /**
          * @param e The selection event.
          */
         public void widgetSelected(SelectionEvent e) {
             Object o = e.getSource();
             if (o == m_addButton) {
                 handleAddButtonEvent();
                 return;
             } else if (o == m_editButton) {
                 handleEditButtonEvent();
                 return;
             } else if (o == m_removeButton) {
                 handleRemoveButtonEvent();
                 return;
             } else if (o == m_autList) {
                 handleAutListEvent();
                 return;
             }
             Assert.notReached(Messages.EventWasCreatedByAnUnknownWidget 
                     + StringConstants.DOT);
         }
 
         /**
          * Reacts on double clicks. 
          * @param e The selection event. */
         public void widgetDefaultSelected(SelectionEvent e) { 
             Object o = e.getSource();
             if (o == m_autList) {
                 handleEditButtonEvent();
                 return;
             }
             Assert.notReached(Messages
                     .DoubleClickEventWasCreatedByAnUnknownWidget);
         }
     }
 }

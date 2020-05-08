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
 package org.eclipse.jubula.client.ui.rcp.views.dataset;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jubula.client.core.businessprocess.AbstractParamInterfaceBP;
 import org.eclipse.jubula.client.core.businessprocess.ParamNameBPDecorator;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IParamChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IDataSetPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataCategoryPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.utils.GuiParamValueConverter;
 import org.eclipse.jubula.client.core.utils.IParamValueValidator;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.TextControlBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.editors.AbstractJBEditor;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper;
 import org.eclipse.jubula.client.ui.rcp.factory.TestDataControlFactory;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.widgets.CheckedParamText;
 import org.eclipse.jubula.client.ui.rcp.widgets.CheckedParamTextContentAssisted;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.client.ui.widgets.DirectCombo;
 import org.eclipse.jubula.tools.constants.CharacterConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.SwtAUTHierarchyConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.ControlEditor;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.Page;
 
 
 /**
  * Abstract base class for data set pages
  *
  * @author BREDEX GmbH
  * @created Jul 13, 2010
  */
 @SuppressWarnings("synthetic-access") 
 public abstract class AbstractDataSetPage extends Page 
     implements ISelectionListener, IAdaptable, IParamChangedListener,
                IProjectLoadedListener, ILanguageChangedListener, 
                IDataChangedListener {
     /** Constant for the width of the DataSet column in the table */
     protected static final int DATASET_NUMBER_COLUMNWIDTH = 20;
     /** Constant for the default column witdh */ 
     protected static final int COLUMN_WIDTH = 70;
     /** The current selected Combo and its selection */
     
     private ComboSelection m_cActiveCombo = new ComboSelection(null, null);
     /** The current IParameterInterfacePO */
     private IParameterInterfacePO m_paramInterfaceObj;
 
     /** List of DirectCombos which depend on each other */
     private List<DirectCombo> m_propertyCombos;
     /** The Combo for selecting a language to display */
     private DirectCombo<Locale> m_languageCombo;
     /** The Combo for selecting a data set to display */
     private DirectCombo<Integer> m_dataSetCombo;
     /** The Combo for selecting a parameter to display */
     private DirectCombo<IParamDescriptionPO> m_paramCombo;
     
     /** the primary control for this page */
     private Control m_control;
     /** The TableViewer for this view */
     private TableViewer m_tableViewer;
     /** the tableCursor */
     private DSVTableCursor m_tableCursor;
     
     /** The Add-Button */
     private Button m_addButton;
     /** The Insert Button */
     private Button m_insertButton;
     /** The Delete Button */
     private Button m_deleteButton;
     
     /** En-/Disabler for swt.Controls */
     private ControlEnabler m_controlEnabler;
     /** bp class */
     private AbstractParamInterfaceBP m_paramBP;
     
     /** the corresponding part */
     private IWorkbenchPart m_currentPart;
     /** The current selection */
     private IStructuredSelection m_currentSelection;
     
     /** Constants for the button actions */
     private enum TestDataRowAction { ADDED, INSERTED, DELETED }
 
     /**
      *  The constructor
      *  @param bp the business process to use for this page
      */
     public AbstractDataSetPage(AbstractParamInterfaceBP bp) {
         setParamBP(bp);
     }
     
     /**
      * Class to hold the current selected DirectCombo and its selection.
      * 
      * @author BREDEX GmbH
      * @created 04.04.2006
      */
     private class ComboSelection {
         
         /** The current selected DirecCombo */
         private DirectCombo m_combo;
         
         /** The current selcted value of the current DirectCombo */
         private Object m_value;
         
         /**
          * Constructor.
          * @param combo the selcted DirectCombo
          * @param value the selcted value
          */
         public ComboSelection(DirectCombo combo, Object value) {
             m_combo = combo;
             m_value = value;
         }
 
         /**
          * @return the current DirectCombo. May be null.
          */
         public DirectCombo getCurrentCombo() {
             if (m_combo == null) {
                 return comboFallback();
             }
             return m_combo;
         }
 
         /**
          * @return the current selected value of the current DirectCombo.
          * @param isDefaultAllowed if true and there is no selection,
          *  a default value is returned. If false it returns null if there is 
          *  no selection.
          */
         public Object getCurrentSelectedValue(boolean isDefaultAllowed) {
             if (isDefaultAllowed) {
                 if (m_value == null) {
                     return valueFallback();
                 }
                 return m_value;
             } 
             return m_value;
         }
         
         /**
          * Sets the current Combo with its selection.
          * @param combo the current selected DirecCombo
          */
         public void setComboSelection(DirectCombo combo) {
             m_combo = combo;
             m_value = combo.getSelectedObject();
         }
         
         /**
          * Sets the default selection.
          */
         public void setDefaultComboSelection() {
             m_combo = getLanguageCombo();
             m_value = WorkingLanguageBP.getInstance()
                 .getWorkingLanguage();
         }
         
         /**
          * Fallback if selected value is null;
          * @return the default value
          */
         private Object valueFallback() {
             Locale defaultLocale = WorkingLanguageBP.getInstance()
                 .getWorkingLanguage();
             getLanguageCombo().setSelectedObject(defaultLocale);
             setComboSelection(getLanguageCombo());
             return m_value;
         }
         
         /**
          * Fallback if combo is null
          * @return the default combo
          */
         private DirectCombo comboFallback() {
             Locale defaultLocale =  null;
             if (GeneralStorage.getInstance().getProject() != null) {
                 defaultLocale = GeneralStorage.getInstance().getProject()
                     .getDefaultLanguage();
             }
             getLanguageCombo().setSelectedObject(defaultLocale);
             setComboSelection(getLanguageCombo());
             return getLanguageCombo();
         }
     }
 
     /**
      * Abstract class for ContentProviders
      * 
      * @author BREDEX GmbH
      * @created 04.04.2006
      */
     private abstract static class AbstractContentProvider implements
             IStructuredContentProvider {
         /** {@inheritDoc} */
         public Object[] getElements(Object inputElement) {
             return new Object[0];
         }
 
         /** {@inheritDoc} */
         public void dispose() {
         // nothing
         }
 
         /** {@inheritDoc} */
         public void inputChanged(Viewer viewer, Object oldInput, 
                 Object newInput) {
         // nothing
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void handleLanguageChanged(Locale locale) {
         if (getComboTracker().getCurrentCombo() == getLanguageCombo()) {
             getLanguageCombo().setSelectedObject(locale);
             getLanguageCombo().notifyListeners(SWT.Selection, new Event());
         }
     }
 
     /**
      * Base Class
      * 
      * @author BREDEX GmbH
      * @created Jul 13, 2010
      */
     private abstract class AbstractSelectionListener 
         implements SelectionListener {
         /** {@inheritDoc} */
         public void widgetDefaultSelected(SelectionEvent e) {
         // nothing
         }
     }
     
     /**
      * Abstract class for ITableLabelProvider
      * @author BREDEX GmbH
      * @created 04.04.2006
      */
     private abstract class AbstractLabelProvider 
         implements ITableLabelProvider, IColorProvider {
         /** {@inheritDoc} */
         public Image getColumnImage(Object element, int columnIndex) {
             return null;
         }
         
         /** {@inheritDoc} */
         public String getColumnText(Object element, int columnIndex) {
             return StringConstants.EMPTY;
         }
         
         /** {@inheritDoc} */
         public void addListener(ILabelProviderListener listener) {
             // nothing
         }
 
         /** {@inheritDoc} */
         public void dispose() {
             // nothing
         }
 
         /** {@inheritDoc} */
         public boolean isLabelProperty(Object element, String property) {
             return false;
         }
 
         /** {@inheritDoc} */
         public void removeListener(ILabelProviderListener listener) {
             // nothing
         }
         
         /**
          * {@inheritDoc}
          */
         public Color getBackground(Object element) {
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         public Color getForeground(Object element) {
             if (!getControlEnabler().areControlsEnabled()) {
                 return LayoutUtil.GRAY_COLOR;
             }
             return null;
         }
     }
     
     /**
      * @return the combo selection tracker
      */
     private ComboSelection getComboTracker() {
         return m_cActiveCombo;
     }
     
     /**
      * @return the zero relative index of the selected data set.
      */
     private int getSelectedDataSet() {
         if (getComboTracker().getCurrentCombo() != getDataSetCombo()) {
             return getTableViewer().getTable().getSelectionIndex();
         }
         return getDataSetCombo().getSelectionIndex() - 1;
     }
 
     /**
      * @param tableViewer the tableViewer to set
      */
     private void setTableViewer(TableViewer tableViewer) {
         m_tableViewer = tableViewer;
     }
 
     /**
      * @return the tableViewer
      */
     private TableViewer getTableViewer() {
         return m_tableViewer;
     }
     
     /**
      * @return the tableViewers table control
      */
     private Table getTable() {
         return getTableViewer().getTable();
     }
     
     /**
      * checks the combo selection. Call after any button action!
      * @param action the action of th ebutton
      * @param row the row on which the action was performed
      */    
     private void checkComboSelection(TestDataRowAction action, int row) {
         if (getComboTracker().getCurrentCombo() == getDataSetCombo()) {
             switch (action) {
                 case ADDED :
                     getDataSetCombo().setSelectedObject(
                         getDataSetCombo().getItemCount() - 1);
                     break;
                 case INSERTED :
                     getDataSetCombo().setSelectedObject(row + 1);
                     break;
                 case DELETED :
                     if ((row - 1) >= 0) {
                         getDataSetCombo().setSelectedObject(row);
                         getTableViewer().refresh();
                     }
                     break;
                 default :
                     break;
             }
         }
     }
 
     /**
      * @param dataSetCombo the dataSetCombo to set
      */
     private void setDataSetCombo(DirectCombo<Integer> dataSetCombo) {
         m_dataSetCombo = dataSetCombo;
     }
 
     /**
      * @return the dataSetCombo
      */
     private DirectCombo<Integer> getDataSetCombo() {
         return m_dataSetCombo;
     }
 
     /**
      * @param languageCombo the languageCombo to set
      */
     private void setLanguageCombo(DirectCombo<Locale> languageCombo) {
         m_languageCombo = languageCombo;
     }
 
     /**
      * @return the languageCombo
      */
     private DirectCombo<Locale> getLanguageCombo() {
         return m_languageCombo;
     }
     
     /**
      * {@inheritDoc}
      */
     public void createControl(Composite parent) {
         Composite topLevelComposite = new Composite(parent, SWT.NONE);
         topLevelComposite.setData(SwtAUTHierarchyConstants.WIDGET_NAME,
                 "DataSetViewPage"); //$NON-NLS-1$
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 2;
         layout.marginWidth = LayoutUtil.MARGIN_WIDTH;
         layout.marginHeight = LayoutUtil.MARGIN_HEIGHT;
         topLevelComposite.setLayout(layout);
         GridData layoutData = new GridData(GridData.FILL_BOTH);
         layoutData.grabExcessHorizontalSpace = true;
         topLevelComposite.setLayoutData(layoutData);
         m_control = topLevelComposite;
 
         Composite buttonComp = new Composite(topLevelComposite, SWT.BORDER);
 
         // Set numColumns to 3 for the buttons
         layout = new GridLayout(3, false);
         layout.marginWidth = 3;
         layout.marginHeight = 3;
         buttonComp.setLayout(layout);
 
         // Create a composite to hold the children
         GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
         buttonComp.setLayoutData(gridData);
         
         createComboLabels(buttonComp);
         createCombos(buttonComp);
         initTableViewer(buttonComp); 
         createButtons(buttonComp);
         Plugin.getHelpSystem().setHelp(getTable(),
                 ContextHelpIds.JB_DATASET_VIEW);
     }
 
     /**
      * Create the labels of the Combos
      * @param parent the parent of the labels
      */
     private void createComboLabels(Composite parent) {
         Label paramLabel = new Label(parent, SWT.NONE);
         paramLabel.setText(Messages.GDDataSetViewParameter);
         paramLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.ParamLabel"); //$NON-NLS-1$
         
         Label dataSetLabel = new Label(parent, SWT.NONE);
         dataSetLabel.setText(Messages.GDDataSetViewDataSet);
         dataSetLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.DataSetLabel"); //$NON-NLS-1$
         
         Label languageLabel = new Label(parent, SWT.NONE);
         languageLabel.setText(Messages.GDDataSetViewLanguage);
         languageLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.DataSetLabel"); //$NON-NLS-1$
     }
     
     /**
      * Add the "Add", "Delete" and "Insert" buttons
      * @param parent the parent composite
      */
     private void createButtons(Composite parent) {
         // Create and configure the "Add" button
         setAddButton(new Button(parent, SWT.PUSH | SWT.CENTER));
         getAddButton().setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.AddButton"); //$NON-NLS-1$
         getAddButton().setText(Messages.JubulaDataSetViewAppend);
         GridData gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
         gridData.widthHint = 80;
         getAddButton().setLayoutData(gridData);
         getAddButton().setEnabled(false);
         getControlEnabler().addControl(getAddButton());
         
         // Create and configure the "Insert" button
         setInsertButton(new Button(parent, SWT.PUSH | SWT.CENTER));
         getInsertButton().setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.InsertButton"); //$NON-NLS-1$
         getInsertButton().setText(Messages.GDDataSetViewInsert);
         gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
         gridData.widthHint = 80;
         getInsertButton().setLayoutData(gridData);
         getInsertButton().setEnabled(false);
         getControlEnabler().addControl(getInsertButton());
         
         //  Create and configure the "Delete" button
         setDeleteButton(new Button(parent, SWT.PUSH | SWT.CENTER));
         getDeleteButton().setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.DeleteButton"); //$NON-NLS-1$
         getDeleteButton().setText(Messages.JubulaDataSetViewDelete);
         gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
         gridData.widthHint = 80; 
         getDeleteButton().setLayoutData(gridData); 
         getDeleteButton().setEnabled(false);
         getControlEnabler().addControl(getDeleteButton());
         
         addListenerToButtons();
     }
     
     /**
      * inits the m_tableViewer
      * @param parent the parent of the m_tableViewer
      */
     private void initTableViewer(Composite parent) {
         setTableViewer(new TableViewer(parent, 
                 SWT.SINGLE | SWT.FULL_SELECTION));
         Table table = getTable();
         table.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.DataTable"); //$NON-NLS-1$
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
         GridData gridData = new GridData(GridData.FILL_BOTH);
         gridData.grabExcessVerticalSpace = true;
         gridData.horizontalSpan = 3;
         table.setLayoutData(gridData);
         getTableViewer().setUseHashlookup(true);
         getTableViewer().setContentProvider(new LanguageContentProvider());
         getTableViewer().setLabelProvider(new LanguageLabelProvider());
         setTableCursor(new DSVTableCursor(getTable(), SWT.NONE));
     }
     
     /**
      * add listener to buttons
      */
     private void addListenerToButtons() {
         getAddButton().addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 final int index = getSelectedDataSet();
                 addDataSet();
                 checkComboSelection(TestDataRowAction.ADDED, index);
                 getControlEnabler().selectionChanged(m_currentPart,
                         m_currentSelection);
             }
         });
         getInsertButton().addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 final int index = getSelectedDataSet();
                 insertDataSetAtCurrentSelection();
                 checkComboSelection(TestDataRowAction.INSERTED, index);
             }
         });
         getDeleteButton().addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 final int index = getSelectedDataSet();
                 removeDataSet();
                 checkComboSelection(TestDataRowAction.DELETED, index);
             }
         });
     }
     
     /**
      * Add a row as last element.
      */
     private void addDataSet() {
         final int rowCount = getParamInterfaceObj().getDataManager()
                 .getDataSetCount();
         insertDataSet(rowCount);
         fillDataSetCombo();
     }
     
     /**
      * Inserts a new data set at the current selection in the table
      */
     private void insertDataSetAtCurrentSelection() {
         final int row = getSelectedDataSet();
         insertDataSet(row);
     }
     
     /**
      * Inserts a new data set at the given row
      * 
      * @param row
      *            the row to insert the new data set
      */
     private void insertDataSet(int row) {
         final AbstractJBEditor editor = (AbstractJBEditor)m_currentPart;
         editor.getEditorHelper().requestEditableState();
         if (getParamInterfaceObj() instanceof IExecTestCasePO) {
             ITDManager man = ((IExecTestCasePO)getParamInterfaceObj())
                     .resolveTDReference();
             if (!man.equals(getTableViewer().getInput())) {
                 getTableViewer().setInput(man);
             }
         }
         if (row > -1) {
             getParamBP().addDataSet(getParamInterfaceObj(), row);
             fillDataSetCombo();
         } else {
             // if first data set is added
             addDataSet();
         }
         editor.getEditorHelper().setDirty(true);
         getTableViewer().refresh();
         List<Locale> projLangs = GeneralStorage.getInstance().getProject()
                 .getLangHelper().getLanguageList();
         for (Locale locale : projLangs) {
             setIsEntrySetComplete(getParamInterfaceObj(), locale);
         }
         int rowToSelect = row;
         if (getComboTracker().getCurrentCombo() == getDataSetCombo()) {
             rowToSelect = 0;
         }
         if (rowToSelect == -1) {
             rowToSelect = getTable().getItemCount();
         } else {
             getTableCursor().setSelection(rowToSelect, 1);
             setFocus();
         }
         getTable().setSelection(rowToSelect);
         DataEventDispatcher.getInstance().fireParamChangedListener();
     }
     
     /**
      * {@inheritDoc}
      */
     public Control getControl() {
         return m_control;
     }
     
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         return null;
     }
     
     /** 
      * {@inheritDoc}
      */
     public void setFocus() {
         getTable().setFocus();
     }
 
     /**
      * @return the controlEnabler
      */
     private ControlEnabler getControlEnabler() {
         if (m_controlEnabler == null) {
             m_controlEnabler = new ControlEnabler();
         }
         return m_controlEnabler;
     }
 
     /**
      * @param addButton the addButton to set
      */
     private void setAddButton(Button addButton) {
         m_addButton = addButton;
     }
 
     /**
      * @return the addButton
      */
     private Button getAddButton() {
         return m_addButton;
     }
 
     /**
      * @param insertButton the insertButton to set
      */
     private void setInsertButton(Button insertButton) {
         m_insertButton = insertButton;
     }
 
     /**
      * @return the insertButton
      */
     private Button getInsertButton() {
         return m_insertButton;
     }
 
     /**
      * @param deleteButton the deleteButton to set
      */
     private void setDeleteButton(Button deleteButton) {
         m_deleteButton = deleteButton;
     }
 
     /**
      * @return the deleteButton
      */
     private Button getDeleteButton() {
         return m_deleteButton;
     }
 
     /**
      * Class for En-/Disabling swt.Controls depending of active WorkbenchPart
      * and selection
      * @author BREDEX GmbH
      * @created 06.04.2006
      */
     private abstract class AbstractControlEnabler {
         /** List of Controls */
         private List<Control> m_controlList = new ArrayList<Control>();
         
         /** 
          * tracks whether managed controls were most recently 
          * enabled or disabled 
          */
         private boolean m_areControlsEnabled = true;
         
         /**
          * Adds the given Control to this Listener
          * @param control the Control
          */
         public void addControl(Control control) {
             if (!getControlList().contains(control)) {
                 getControlList().add(control);
             }
         }
         
         /**
          * @return the controlList
          */
         protected List<Control> getControlList() {
             return m_controlList;
         }
         
         /**
          * Enables or disables all controls managed by the receiver.
          * 
          * @param enabled <code>true</code> if all managed components should be
          *                enabled. <code>false</code> if all managed components
          *                should be disabled.
          */
         public void setControlsEnabled(boolean enabled) {
             m_areControlsEnabled = enabled;
             for (Control control : getControlList()) {
                 control.setEnabled(enabled);
             }
         }
 
         /**
          * 
          * @return <code>true</code> if all managed components are enabled. 
          *         <code>false</code> if all managed components are disabled.
          */
         public boolean areControlsEnabled() {
             return m_areControlsEnabled;
         }
     }
     
     /**
      * Clears the m_tableViewer
      */
     private void clearTableViewer() {
         getTable().removeAll();
         for (TableColumn column : getTable().getColumns()) {
             column.dispose();
         }
     }
     
     
     /**
      * Inits and creates the column for the data set numbers
      * @return the name of the column
      */
     private String initDataSetColumn() {
         clearTableViewer();
         final Table table = getTable();
         // create column for data set numer
         TableColumn dataSetNumberCol = new TableColumn(table, SWT.NONE);
         dataSetNumberCol.setText(Messages.GDDataSetViewControllerDataSetNumber);
         dataSetNumberCol.setWidth(DATASET_NUMBER_COLUMNWIDTH);
         return dataSetNumberCol.getText();
     }
     
     /**
      * Inits and creates the column for the paremeters
      * @return the name of the column
      */
     private String initParameterColumn() {
         clearTableViewer();
         final Table table = getTable();
         // create column for data set numer
         TableColumn paramCol = new TableColumn(table, SWT.NONE);
         paramCol.setText(Messages.GDDataSetViewParameter);
         paramCol.pack();
         return paramCol.getText();
     }
     
     /**
      * Packs the table.
      */
     private void packTable() {
         final Table table = getTable();
         final TableColumn[] columns = table.getColumns();
         final int columnCount = columns.length;
         for (int i = 1; i < columnCount; i++) {
             final TableColumn column = columns[i];
             column.pack();
             if (column.getWidth() < COLUMN_WIDTH) {
                 column.setWidth(COLUMN_WIDTH);
             }
         }
     }
     
     /**
      * SelectionListener that ensures only one Combo has a selection.
      * @author BREDEX GmbH
      * @created 30.03.2006
      */
     @SuppressWarnings("unchecked")  
     private class ComboSingleSelectionListener 
         extends AbstractSelectionListener {
         /** {@inheritDoc} */
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             if (((DirectCombo)source).getSelectionIndex() == 0) {
                 return;
             }
             for (DirectCombo combo : m_propertyCombos) {
                 if (combo != source) {
                     combo.setSelectedObject(null);
                 }
             }
             getComboTracker().setComboSelection((DirectCombo)source);
         }
     }
     
     /**
      * Sets the prior Combo selection. If the prior selection does not fit,
      * the default selection will be set.
      */
     @SuppressWarnings("unchecked") 
     private void setPriorComboSelection() {
         final DirectCombo priorCombo = getComboTracker().getCurrentCombo();
         for (DirectCombo combo : m_propertyCombos) {
             if (combo == priorCombo) {
                 combo.setSelectedObject(getComboTracker()
                     .getCurrentSelectedValue(false));
                 if (combo.getSelectedObject() == null) {
                     getComboTracker().setDefaultComboSelection();
                     getComboTracker().getCurrentCombo().setSelectedObject(
                             getComboTracker().getCurrentSelectedValue(false));
                 }
                 break;
             }
         }
     }
     
     /**
      * creates the Combos
      * @param parent the parent of the combos
      */
     private void createCombos(Composite parent) {
         m_propertyCombos = new ArrayList<DirectCombo>();
         
         List<IParamDescriptionPO> a1 = new ArrayList<IParamDescriptionPO>(0);
         List<String> a2 = new ArrayList<String>(0);
         m_paramCombo = new DirectCombo<IParamDescriptionPO>(parent, SWT.NONE, 
             a1, a2, true, false);
         m_paramCombo.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.ParamCombo"); //$NON-NLS-1$
         m_paramCombo.addSelectionListener(new ParameterComboListener());
         GridData paramComboLayoutData = new GridData ();
         paramComboLayoutData.horizontalAlignment = GridData.FILL;
         paramComboLayoutData.grabExcessHorizontalSpace = true;
         m_paramCombo.setLayoutData (paramComboLayoutData);
         m_propertyCombos.add(m_paramCombo);
         
         List<Integer> b1 = new ArrayList<Integer>(0);
         List<String> b2 = new ArrayList<String>(0);
         setDataSetCombo(new DirectCombo<Integer>(parent, SWT.NONE, b1, 
             b2, true, false));
         getDataSetCombo().setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.DataSetCombo"); //$NON-NLS-1$
         getDataSetCombo().addSelectionListener(new DataSetComboListener());
         getDataSetCombo().setSize(100, getDataSetCombo().getItemHeight());
         GridData dataSetComboLayoutData = new GridData ();
         dataSetComboLayoutData.horizontalAlignment = GridData.FILL;
         dataSetComboLayoutData.grabExcessHorizontalSpace = true;
         getDataSetCombo().setLayoutData (dataSetComboLayoutData);
         m_propertyCombos.add(getDataSetCombo());
         
         List<Locale> c1 = new ArrayList<Locale>(0);
         List<String> c2 = new ArrayList<String>(0);
         setLanguageCombo(new DirectCombo<Locale>(parent, SWT.NONE, c1, 
             c2, true, false));
         getLanguageCombo().setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DataSetView.LanguageCombo"); //$NON-NLS-1$
         getLanguageCombo().addSelectionListener(new LanguageComboListener());
         getLanguageCombo().setSize(100, getLanguageCombo().getItemHeight());
         GridData languageComboLayoutData = new GridData ();
         languageComboLayoutData.horizontalAlignment = GridData.FILL;
         languageComboLayoutData.grabExcessHorizontalSpace = true;
         getLanguageCombo().setLayoutData (languageComboLayoutData);
         m_propertyCombos.add(getLanguageCombo());
         
         SelectionListener comboSelListener = new ComboSingleSelectionListener();
         m_paramCombo.addSelectionListener(comboSelListener);
         getDataSetCombo().addSelectionListener(comboSelListener);
         getLanguageCombo().addSelectionListener(comboSelListener);
     }
     
     /**
      * Listener of DataSet-Combo
      * @author BREDEX GmbH
      * @created 03.04.2006
      */
     private class DataSetComboListener 
         extends AbstractSelectionListener {
         /** {@inheritDoc} */
         @SuppressWarnings("unchecked") 
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             if (((DirectCombo)source).getSelectionIndex() == 0) {
                 getComboTracker().getCurrentCombo().
                     setSelectedObject(getComboTracker().
                         getCurrentSelectedValue(false));
                 return;
             }
 
             initTableViewerLanguageColumns(false);
             getTableViewer().setContentProvider(new DataSetContentProvider());
             getTableViewer().setLabelProvider(new DataSetLabelProvider());
             getComboTracker().setComboSelection(getDataSetCombo());
             getTableViewer().refresh();
         }
     }
     
     /**
      * Listener of Language-Combo
      * @author BREDEX GmbH
      * @created 03.04.2006
      */
     private class LanguageComboListener 
         extends AbstractSelectionListener {
         /** {@inheritDoc} */
         @SuppressWarnings("unchecked") 
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             if (((DirectCombo)source).getSelectionIndex() == 0) {
                 getComboTracker().getCurrentCombo().
                     setSelectedObject(getComboTracker().
                         getCurrentSelectedValue(false));
                 return;
             }
             getTableViewer().setContentProvider(new LanguageContentProvider());
             getTableViewer().setLabelProvider(new LanguageLabelProvider());
             getComboTracker().setComboSelection(getLanguageCombo());
             initTableViewerParameterColumns();
             getTableViewer().refresh();
         }
     }
     
     /**
      * Listener of Parameter-Combo
      * @author BREDEX GmbH
      * @created 03.04.2006
      */
     private class ParameterComboListener 
         extends AbstractSelectionListener {
         /** {@inheritDoc} */
         @SuppressWarnings("unchecked")
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             if (((DirectCombo)source).getSelectionIndex() == 0) {
                 getComboTracker().getCurrentCombo().
                     setSelectedObject(getComboTracker().
                         getCurrentSelectedValue(false));
                 return;
             }
 
             initTableViewerLanguageColumns(true);
             getTableViewer().setContentProvider(new ParameterContentProvider());
             getTableViewer().setLabelProvider(new ParameterLabelProvider());
             getComboTracker().setComboSelection(m_paramCombo);
             getTableViewer().refresh();
         }
     }
     
     
     /**
      * creates the TableColumns with Parameter
      */
     private void initTableViewerParameterColumns() {
         if (getParamInterfaceObj() == null) {
             return;
         }
         final Table table = getTable();
         String[] columnProperties = new String[getParamInterfaceObj()
                 .getParameterList().size() + 1];
         columnProperties[0] = initDataSetColumn();
         // create columns for parameter
         int i = 1;
         for (IParamDescriptionPO descr : getParamInterfaceObj()
                 .getParameterList()) {
             TableColumn column = new TableColumn(table, SWT.NONE);
             String columnName = descr.getName();
             column.setText(columnName);
             columnProperties[i++] = columnName;
             column.setWidth(COLUMN_WIDTH);
         }
         getTableViewer().setColumnProperties(columnProperties);
     }
     
     /**
      * creates the TableColumns with languages
      * @param dataSetNumbers if true, the data set column is created, otherwise
      * the parameter column.
      */
     private void initTableViewerLanguageColumns(boolean dataSetNumbers) {
         if (getParamInterfaceObj() == null) {
             return;
         }
         List<Locale> locales = WorkingLanguageBP.getInstance()
                 .getDisplayableLanguages();
         String[] columnProperties = new String[locales.size() + 1];
         final Table table = getTable();
         if (dataSetNumbers) {
             columnProperties[0] = initDataSetColumn();
         } else {
             columnProperties[0] = initParameterColumn();
         }
         int i = 1;
         List<String> dispList = new ArrayList<String>();
         for (Locale locale : locales) {
             dispList.add(locale.getDisplayName());
         }
         String[] dispNames = dispList.toArray(new String[dispList.size()]);
 
         Arrays.sort(dispNames);
         for (String lang : dispNames) {
             TableColumn column = new TableColumn(table, SWT.NONE);
             column.setText(lang);
             column.setWidth(COLUMN_WIDTH);
             column.setData(Languages.getInstance().getLocale(lang));
             columnProperties[i++] = column.getText();
         }
         getTableViewer().setColumnProperties(columnProperties);
     }
     
     /**
      * Updates this view. Causes the view to get and display its data.
      */
     private void updateView() {
         clearTableViewer();
         fillParamCombo();
         fillDataSetCombo();
         fillLanguageCombo();
         setPriorComboSelection();
         IParameterInterfacePO paramObj = getParamInterfaceObj();
         if (paramObj != null && isNodeValid(paramObj)) {
             getTableViewer().setInput(getInputForTable(paramObj));
             createTable();
         } else {
             getTableViewer().setInput(null);
         }
     }
     
     /**
      * @param cParamInterfaceObj the param interface object to test
      * @return wether the object is valid
      */
     protected abstract boolean isNodeValid(
             IParameterInterfacePO cParamInterfaceObj);
 
     /**
      * Creates the table
      */
     private void createTable() {
         DirectCombo combo = getComboTracker().getCurrentCombo();
         Object formerValue = getComboTracker().getCurrentSelectedValue(true);
         if (combo != null && formerValue != null
                 && formerValue.equals(combo.getSelectedObject())) {
             combo.notifyListeners(SWT.Selection, new Event());
         } else {
             initTableViewerParameterColumns();
         }
         packTable();
     }
     
     /**
      * Fills the Parameter-Combo with its values
      */
     private void fillParamCombo() {
         List<IParamDescriptionPO> paramList = 
             new ArrayList<IParamDescriptionPO>(0);
         if (getParamInterfaceObj() != null) {
             paramList = getParamInterfaceObj().getParameterList();
         }
         final int listSize = paramList.size(); 
         List<String> keys = new ArrayList<String>(listSize);
         List<IParamDescriptionPO> values = 
             new ArrayList<IParamDescriptionPO>(listSize);
         for (IParamDescriptionPO descr : paramList) {          
             keys.add(descr.getName());
             values.add(descr);
         }
         m_paramCombo.setItems(values, keys);
         m_paramCombo.setSelectedObject(null);
     }
     
     /**
      * Fills the Language-Combo with its values displayed.
      */
     private void fillLanguageCombo() {
         List<Locale> langList = WorkingLanguageBP.getInstance()
                 .getDisplayableLanguages();
         List<String> dispList = new ArrayList<String>();
         for (Locale locale : langList) {
             dispList.add(locale.getDisplayName());
         }
         String[] dispNames = dispList.toArray(new String[dispList.size()]);
 
         Arrays.sort(dispNames);
         final int listSize = langList.size();
         List<String> keys = new ArrayList<String>(listSize);
         List<Locale> values = new ArrayList<Locale>(listSize);
         for (String lang : dispNames) {
             values.add(Languages.getInstance().getLocale(lang));
             keys.add(lang);
         }
         getLanguageCombo().setItems(values, keys);
     }
     
     /**
      * Fills the DataSet-Combo with its values
      */
     private void fillDataSetCombo() {
         int dataSets = 0;
         IParameterInterfacePO testDataProvider = getParamInterfaceObj();
         if (testDataProvider != null) {
             while (testDataProvider.getReferencedDataCube() != null) {
                 testDataProvider = testDataProvider.getReferencedDataCube();
             }
             dataSets = testDataProvider.getDataManager().getDataSetCount();
         }
         List<String> keys = new ArrayList<String>(dataSets);
         List<Integer> values = new ArrayList<Integer>(dataSets);
         for (int i = 0; i < dataSets; i++) {
             int value = i + 1;
             values.add(value);
             keys.add(StringConstants.EMPTY + value);
         }
         getDataSetCombo().setItems(values, keys);
     }
     
     /**
      * The AbstractContentProvider of the Language-Table.
      * @author BREDEX GmbH
      * @created 03.04.2006
      */
     private static class LanguageContentProvider 
         extends AbstractContentProvider {
         /** {@inheritDoc} */
         public Object[] getElements(Object inputElement) {
             ITDManager tdMan = (ITDManager)inputElement;
             List <IDataSetPO> rows = tdMan.getDataSets();
             return rows.toArray();
         }
     }
     
     /**
      * AbstractContentProvider for the Parameter-Table
      * @author BREDEX GmbH
      * @created 04.04.2006
      * @see LanguageContentProvider
      */
     private static class ParameterContentProvider 
         extends LanguageContentProvider {
         // currently empty
     }
     
     /**
      * AbstractContentProvider for the DataSet-Table
      * @author BREDEX GmbH
      * @created 05.04.2006
      */
     private class DataSetContentProvider extends AbstractContentProvider {
         /** {@inheritDoc} */
         public Object[] getElements(Object inputElement) {
             return getParamInterfaceObj().getParameterList().toArray();
         }
     }
     
     /**
      * The AbstractLabelProvider to display a data set's data
      * 
      * @author BREDEX GmbH
      * @created 05.04.2006
      */
     private class DataSetLabelProvider extends AbstractLabelProvider {
         /** {@inheritDoc} */
         public String getColumnText(Object element, int columnIndex) {
             if (!(element instanceof IParamDescriptionPO)) {
                 // this happens when Content-/LabelProvider changes!
                 // see ...ComboListener
                 return StringConstants.EMPTY;
             }
             IParamDescriptionPO desc = (IParamDescriptionPO)element;
             if (columnIndex == 0) {
                 int rowCount = getParamInterfaceObj().getParameterList()
                         .indexOf(desc);
                 getTable().getItem(rowCount).setBackground(columnIndex,
                         getTable().getDisplay().getSystemColor(
                                 SWT.COLOR_WIDGET_BACKGROUND));
                 return desc.getName();
             }
             Locale locale = (Locale)getTable().getColumn(columnIndex).getData();
             String value = StringConstants.EMPTY;
             if (getDataSetCombo().getSelectedObject() != null) {
                 int rowCount = getDataSetCombo().getSelectedObject() - 1;
                 IParameterInterfacePO paramInterface = getParamInterfaceObj();
                 value = getGuiStringForParamValue(paramInterface, desc,
                         rowCount, locale);
             }
             return value;
         }
     }
     
     /**
      * The AbstractLabelProvider to display a language's data
      * @author BREDEX GmbH
      * @created 03.04.2006
      */
     private class LanguageLabelProvider extends AbstractLabelProvider {
         /** {@inheritDoc} */
         public String getColumnText(Object element, int columnIndex) {
             if (!(element instanceof IDataSetPO)) {
                 // this happens when Content-/LabelProvider changes!
                 // see ...ComboListener
                 return StringConstants.EMPTY; 
             }
             ITDManager tdMan = (ITDManager)getTableViewer().getInput();
             IDataSetPO row = (IDataSetPO)element;
             int rowCount = tdMan.getDataSets().indexOf(row);
             if (columnIndex == 0) {
                 getTable().getItem(rowCount).setBackground(
                     columnIndex, getTable().getDisplay().getSystemColor(
                             SWT.COLOR_WIDGET_BACKGROUND));
                 return StringConstants.EMPTY + (rowCount + 1); 
             }
             List <IParamDescriptionPO>paramList = 
                 getParamInterfaceObj().getParameterList();
             String value = StringConstants.EMPTY;
             if ((columnIndex - 1) < paramList.size()) {
                 IParamDescriptionPO desc = paramList.get(columnIndex - 1);
                 IParameterInterfacePO paramInterface = getParamInterfaceObj();
                 value = getGuiStringForParamValue(paramInterface, desc,
                         rowCount, getLanguageCombo().getSelectedObject());
             }
             return value;
         }
     }
     
     /**
      * @param paramObj
      *            the param interface object
      * @param desc
      *            the ParamDescriptionP
      * @param rowCount
      *            the row count
      * @param locale
      *            the selected locale
      * @return a valid string for gui presentation of the given param value
      */
     private String getGuiStringForParamValue(IParameterInterfacePO paramObj,
             IParamDescriptionPO desc, int rowCount, Locale locale) {
         return AbstractParamInterfaceBP.getGuiStringForParamValue(paramObj,
                 desc, rowCount, locale);
     }
     
     /**
      * The AbstractLabelProvider to display a parameter's data
      * 
      * @author BREDEX GmbH
      * @created 04.04.2006
      */
     private class ParameterLabelProvider extends AbstractLabelProvider {
         /** {@inheritDoc} */
         public String getColumnText(Object element, int columnIndex) {
             if (!(element instanceof IDataSetPO)) {
                 // this happens when Content-/LabelProvider changes!
                 // see ...ComboListener
                 return StringConstants.EMPTY; 
             }
             ITDManager tdMan = (ITDManager)getTableViewer().getInput();
             IDataSetPO row = (IDataSetPO)element;
             int rowCount = tdMan.getDataSets().indexOf(row);
             if (columnIndex == 0) {                
                 getTable().getItem(rowCount).setBackground(
                     columnIndex, getTable().getDisplay().getSystemColor(
                             SWT.COLOR_WIDGET_BACKGROUND));
                 return StringConstants.EMPTY + (rowCount + 1); 
             }
             String value = StringConstants.EMPTY;
             if ((columnIndex - 1) < getTable().getColumnCount()) {
                 IParamDescriptionPO desc = m_paramCombo.getSelectedObject();
                 if (desc != null) {
                     Locale locale = (Locale)getTable()
                         .getColumn(columnIndex).getData();
                     IParameterInterfacePO paramInterface = 
                         getParamInterfaceObj();
                     value = getGuiStringForParamValue(paramInterface,
                             desc, rowCount, locale);
                 }
             }
             return value;
         }
     }
     
     /** {@inheritDoc} */
     public void handleParamChanged() {
         initTableViewerParameterColumns();
         updateView();            
     }
     
     /** {@inheritDoc} */
     public void handleProjectLoaded() {
         setParamInterfaceObj(null);
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 getTableViewer().setInput(null);
                 getComboTracker().setDefaultComboSelection();
             }
         });
     }
 
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         for (DataChangedEvent e : events) {
             handleDataChanged(e.getPo(), e.getDataState());
         }
     }
     
     /** {@inheritDoc} */
     public void handleDataChanged(IPersistentObject po, DataState dataState) {
         if (dataState == DataState.Deleted 
                 && po.equals(getParamInterfaceObj())) {
             setParamInterfaceObj(null);
             updateView();
         }
 
         if (dataState == DataState.StructureModified
                 && po instanceof ITestDataCategoryPO) {
             updateView();
         }
         
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 getControlEnabler().selectionChanged(m_currentPart,
                         m_currentSelection);
             }
         });
     }
     
     /**
      * The TableCursor for keyboard support
      * @author BREDEX GmbH
      * @created 11.04.2006
      */
     public class DSVTableCursor extends TableCursor {
         /** The ControlEditor */
         private ControlEditor m_editor;
         /** the current testcase editor */
         private AbstractJBEditor m_tcEditor;
         /** The KeyListener of the editor */
         private KeyAdapter m_keyListener = new EditorKeyListener();
         /** The MouseListener of this Cursor */
         private MouseAdapter m_mouseListener = new EditorMouseListener();
         /** The SelectionListener of this Cursor */
         private CursorListener m_cursorListener = new CursorListener();
         /** The FocusListener of this Cursor */
         private EditorFocusListener m_focusListener = new EditorFocusListener();
         /** true, if editor was activated with enter key */
         private boolean m_wasActivatedWithEnterKey = false;
         /** value to reset, when pressing "ESC" */
         private String m_oldValue;
         /** The untyped Listener of this Cursor */ 
         private Listener m_listener = new Listener() {
             public void handleEvent(Event event) {
                 if (event.type == SWT.Selection
                         && event.widget instanceof CCombo) {
                     writeData();
                 }
             }
         };
         
         /**
          * @param parent parent
          * @param style style
          */
         public DSVTableCursor(Table parent, int style) {
             super(parent, style);
             addSelectionListener(m_cursorListener);
             addMouseListener(m_mouseListener);
             addKeyListener(m_keyListener);
             m_editor = new ControlEditor(this);
             m_editor.grabHorizontal = true;
             m_editor.grabVertical = true;
         }
         
         /**
          * Gets the zero based column index of the given column property
          * @param columnProperty the property to get the index of
          * @return the zero based column index of the given column proerty 
          * or -1 if no column with the given property was found
          */
         private int getColumnIndexOfProperty(String columnProperty) {
             Object[] props = getTableViewer().getColumnProperties();
             for (int i = 0; i < props.length; i++) {
                 if (columnProperty.equals(props[i])) {
                     return i;
                 }
             }
             return -1;
         }
         
         /**
          * assumes the typed data
          */
         private void writeData() {
             if (m_currentPart instanceof AbstractJBEditor) {
                 m_tcEditor = (AbstractJBEditor)m_currentPart;
             }
             if (m_tcEditor == null) { // e.g. activeEditor = OMEditor
                 return;
             }
             int column = getColumn();
             final Control editor = m_editor.getEditor();
             if (!TextControlBP.isTextValid(editor)) {
                 TextControlBP.setText(m_oldValue, editor);
             }
             final Combo activeCombo = getComboTracker().getCurrentCombo();
             final String property = getTableViewer().getColumnProperties()
                 [column].toString();
             String value = TextControlBP.getText(editor);
             if (m_oldValue != null && m_oldValue.equals(value)) {
                 return;
             }
             if (value != null && value.equals(StringConstants.EMPTY)) {
                 value = null;
             }
             if (activeCombo == getLanguageCombo()) {
                 writeLanguageData(property, value, m_tcEditor);
             } else if (activeCombo == m_paramCombo) {
                 writeParamData(property, value, m_tcEditor);
             } else if (activeCombo == getDataSetCombo()) {
                 writeDataSetData(property, value, m_tcEditor);
             }
         }
         
         /**
          * Writes the data to the selected data set
          * @param property the column property
          * @param value the value to write
          * @param edit the editor
          */
         private void writeDataSetData(String property, Object value, 
                 AbstractJBEditor edit) {
             final int langIndex = getColumnIndexOfProperty(property);
             final Locale locale = (Locale)getTable()
                 .getColumn(langIndex).getData();
             final int dsNumber = getDataSetCombo().getSelectedObject();
             final int paramIndex = getTable()
                 .getSelectionIndex();
             setValueToModel(value, edit, paramIndex, dsNumber - 1, locale);
             getTable().getItem(paramIndex).setText(langIndex, 
                 value == null ? StringConstants.EMPTY : (String) value);
         }
         
         /**
          * Writes the data to the selected language
          * @param property the column property
          * @param value the value to write
          * @param edit the editor
          */
         private void writeLanguageData(String property, Object value, 
                 AbstractJBEditor edit) {
             final int paramIndex = getColumnIndexOfProperty(property);
             final int dsNumber = getTable().indexOf(getRow());
             Locale locale = getLanguageCombo().getSelectedObject();
             setValueToModel(value, edit, paramIndex - 1, dsNumber, locale);
             getTable().getItem(dsNumber).setText(paramIndex, 
                     value == null ? StringConstants.EMPTY : (String) value);
         }
 
         /**
          * @param value
          *            the value to set
          * @param editor
          *            the editor
          * @param paramIndex
          *            the index of the parameter
          * @param dsNumber
          *            the number of data set.
          * @param locale
          *            the locale of the test data
          */
         private void setValueToModel(Object value, AbstractJBEditor editor,
                 int paramIndex, int dsNumber, Locale locale) {
             if (editor.getEditorHelper().requestEditableState() 
                     == JBEditorHelper.EditableState.OK) {
                 ParamNameBPDecorator mapper = editor.getEditorHelper()
                         .getEditSupport().getParamMapper();
                 GuiParamValueConverter conv = getGuiParamValueConverter(
                         (String)value, getParamInterfaceObj(), locale,
                         getCurrentParamDescription(),
                         ((CheckedParamText)m_editor.getEditor())
                                 .getDataValidator());
                 if (conv.getErrors().isEmpty()) {
                     getParamBP().startParameterUpdate(conv, locale, dsNumber,
                             mapper);
                     setIsEntrySetComplete(getParamInterfaceObj(), locale);
                     editor.getEditorHelper().setDirty(true);
                     new Thread() {
                         public void run() {
                             Plugin.getDisplay().syncExec(new Runnable() {
                                 public void run() {
                                     DataEventDispatcher.getInstance()
                                             .firePropertyChanged(false);
                                     DataEventDispatcher.getInstance()
                                             .fireParamChangedListener();
                                 }
                             });
                         }
                     } .start();
                 }
             }
         }
         
         /**
          * Writes the data to the selected parameter
          * @param property the column property
          * @param value the value to write
          * @param edit the editor
          */
         private void writeParamData(String property, Object value, 
                 AbstractJBEditor edit) {
             final int langIndex = getColumnIndexOfProperty(property);
             final Locale locale = (Locale)getTable()
                 .getColumn(langIndex).getData();
             final IParamDescriptionPO desc = m_paramCombo
             .getSelectedObject();
             final int paramIndex = getParamInterfaceObj().getDataManager()
                 .findColumnForParam(desc.getUniqueId());
             final int dsNumber = getTable()
                 .getSelectionIndex();
             setValueToModel(value, edit, paramIndex, dsNumber, locale);
             getTable().getItem(dsNumber).setText(langIndex, 
                 value == null ? StringConstants.EMPTY : (String) value);
         }
         
         /** {@inheritDoc} */
         public void dispose() {
             removeSelectionListener(m_cursorListener);
             removeMouseListener(m_mouseListener);
             m_editor.getEditor().removeFocusListener(m_focusListener);
             super.dispose();
         }
         
         /**
          * @return if the value can be modified
          */
         private boolean canModify() {
             if (!(m_currentPart instanceof AbstractJBEditor)) {
                 return false;
             }
             final AbstractJBEditor edit = (AbstractJBEditor)m_currentPart;
             // First column is not editable!
             boolean isFirstColumn = getColumn() == 0;
             boolean isEditor = (edit != null);
 
             return !isFirstColumn && isEditor 
                 && getControlEnabler().areControlsEnabled();
         }
         
         /** {@inheritDoc} */
         protected void checkSubclass () {
             // only to subclass
         }
         
         /**
          * @return the editor to enter values
          */
         private Control createEditor() {
             Control control = TestDataControlFactory.createControl(
                     getParamInterfaceObj(), getParamName(), this, SWT.NONE);
             control.addKeyListener(m_keyListener);
             control.setFocus();
             // FIXME: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=390800
 //            control.addFocusListener(m_focusListener);
             // end https://bugs.eclipse.org/bugs/show_bug.cgi?id=390800
             control.addListener(SWT.Selection, m_listener);
             m_oldValue = getRow().getText(getColumn());
             TextControlBP.setText(m_oldValue, control);
             TextControlBP.selectAll(control);
             return control;
         }
         
         /**
          * @return the current param name
          */
         private String getParamName() {
             final Combo activeCombo = getComboTracker().getCurrentCombo();
             String paramName = StringConstants.EMPTY;
             if (activeCombo == getLanguageCombo()) {
                 paramName = getTable().getColumn(getColumn())
                     .getText();
             } else if (activeCombo == m_paramCombo) {
                 paramName = m_paramCombo.getSelectedObject().getName();
             } else if (activeCombo == getDataSetCombo()) {
                 paramName = getRow().getText(0);
             }
             return paramName;
         }
         
         /**
          * @return paramDescription for currently edited value
          */
         private IParamDescriptionPO getCurrentParamDescription() {
             String paramName = getParamName();
             return getParamInterfaceObj().getParameterForName(paramName);
         }
 
         /**
          * activate the editor
          */
         private void activateEditor() {
             if (canModify()) {
                 m_editor.setEditor(createEditor());
                 // FIXME: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=390800
                 Control editorCtrl = m_editor.getEditor();
                 if ((editorCtrl != null) && !editorCtrl.isDisposed()) {
                    editorCtrl.addFocusListener(m_focusListener);
                 }
                 // end https://bugs.eclipse.org/bugs/show_bug.cgi?id=390800
                 TextControlBP.selectAll(m_editor.getEditor());
             }
         }
 
         /**
          * KeyListener for the editor
          */
         private class EditorKeyListener extends KeyAdapter {
             /** {@inheritDoc} */
             public void keyReleased(KeyEvent e) {
                 if (e.keyCode == SWT.ARROW_DOWN 
                         || e.keyCode == SWT.ARROW_UP
                         || e.keyCode == SWT.ARROW_LEFT
                         || e.keyCode == SWT.ARROW_RIGHT) {
                     
                     return;
                 }
                 if (!(e.character == CharacterConstants.BACKSPACE
                     || e.character == SWT.DEL // the "DEL"-Key
                     || e.character == SWT.ESC // the "ESC"-Key
                     || e.character == SWT.CR // the "ENTER"-Key
                     || e.character == SWT.KEYPAD_CR // the "ENTER"-Key
                     || (!Character.isISOControl(e.character)))) {
                     return;
                 }
                 if (e.getSource().equals(m_editor.getEditor())) {
                     // close the text editor when the user hits "ESC"
                     if (e.character == SWT.ESC) {
                         TextControlBP.setText(m_oldValue, m_editor.getEditor());
                         writeData();
                         TableItem rowItem = getRow();
                         final int col = getColumn();
                         rowItem.setText(col, m_oldValue);
                         m_editor.getEditor().dispose();
                         return;
                     }
                     if (e.character == SWT.CR || e.character == SWT.KEYPAD_CR) {
                         if (m_wasActivatedWithEnterKey) {
                             m_wasActivatedWithEnterKey = false;
                             return;
                         }
                         handleCR();
                     }
                 }
                 if (e.getSource() instanceof DSVTableCursor) {
                     if (e.character == SWT.ESC) {
                         return;
                     }
                     activateEditor();
                     if (m_editor.getEditor() != null 
                             && !m_editor.getEditor().isDisposed()
                             && e.character != SWT.CR
                             && e.character != SWT.KEYPAD_CR
                             && !(m_editor.getEditor() instanceof CCombo)) {
                         String sign = new Character(e.character).toString();
                         if (e.character == SWT.DEL // the "DEL"-Key
                             || e.character == CharacterConstants.BACKSPACE) {
                             sign = StringConstants.EMPTY;
                         }
                         TextControlBP.setText(sign, m_editor.getEditor());
                         TextControlBP.setSelection(m_editor.getEditor(), 1);
                     }
                 }
             }
 
             /**
              * Handles the CR keys
              */
             private void handleCR() {
                 final Control editorControl = m_editor.getEditor();
                 if (!editorControl.isDisposed()) {
                     writeData();
                 }
                 // writeData() may actually dispose the control during error
                 // handling, a new check is needed!
                 if (!editorControl.isDisposed()) {
                     TableItem rowItem = getRow();
                     final int col = getColumn();
                     rowItem.setText(col, TextControlBP.getText(editorControl));
                     editorControl.dispose();
                     final int row = getTable().indexOf(getRow());
                     if (getTable().getColumnCount() > (col + 1)) {
                         setSelection(row, col + 1);
                         getTable().setSelection(row);
                         setFocus();
                     } else if (getTable().getItemCount() > (row + 1)) {
                         setSelection(row + 1, 1);
                         getTable().setSelection(row + 1);
                     } else {
                         getAddButton().setFocus();
                     }
                 }
             }
         }
          
         /**
          * The SelectionListener
          */
         private class CursorListener extends SelectionAdapter {
 
             /** {@inheritDoc} */
             public void widgetDefaultSelected(SelectionEvent e) {
                 activateEditor();
                 m_wasActivatedWithEnterKey = true;
             }
 
             /** {@inheritDoc} */
             public void widgetSelected(SelectionEvent e) {
                 getTable().setSelection(
                     new TableItem[] {getRow()});
             }
             
         }
 
         /**
          * MouseListener for the editor
          */
         private class EditorMouseListener extends MouseAdapter {
             /** {@inheritDoc} */
             public void mouseUp(MouseEvent e) {
                 activateEditor();
                 m_wasActivatedWithEnterKey = false;
             }
         }
         
         /**
          * @author BREDEX GmbH
          * @created 19.06.2006
          */
         private class EditorFocusListener extends FocusAdapter {
             /** {@inheritDoc} */
             public void focusLost(FocusEvent e) {
                 if (m_editor.getEditor() 
                         instanceof CheckedParamTextContentAssisted) {
                     CheckedParamTextContentAssisted ed = 
                         (CheckedParamTextContentAssisted)m_editor.getEditor();
                     if (ed.isPopupOpen() && ed.isFocusControl()) {
                         super.focusLost(e);
                         return;
                     }
                 }
                 writeData();
                 m_editor.getEditor().dispose();
                 super.focusLost(e);
             }  
         }
     }
     
     /**
      * Removes a selected data set.
      */
     private void removeDataSet() {
         final AbstractJBEditor editor = (AbstractJBEditor)m_currentPart;
         if (editor == null) {
             return;
         }
         if (editor.getEditorHelper().requestEditableState() 
                 == JBEditorHelper.EditableState.OK) {
             if (getParamInterfaceObj() instanceof IExecTestCasePO) {
                 ITDManager man = ((IExecTestCasePO)getParamInterfaceObj())
                         .resolveTDReference();
                 if (!man.equals(getTableViewer().getInput())) {
                     getTableViewer().setInput(man);
                 }
             }
 
             int row = getSelectedDataSet();
             try {
                 if (row == -1 && getTableCursor().getRow() != null) {
                     row = getTable().indexOf(getTableCursor()
                         .getRow());
                 }
                 if (row > -1) {
                     editor.getEditorHelper().getEditSupport()
                         .lockWorkVersion();
                     getParamBP().removeDataSet(getParamInterfaceObj(),
                             row, editor.getEditorHelper().getEditSupport()
                                     .getParamMapper(),
                             WorkingLanguageBP.getInstance()
                                     .getWorkingLanguage());
                     editor.getEditorHelper().setDirty(true);
                     getTableViewer().refresh();                    
                     fillDataSetCombo();
                     List<Locale> projLangs = GeneralStorage.getInstance()
                         .getProject().getLangHelper().getLanguageList();
                     for (Locale locale : projLangs) {
                         setIsEntrySetComplete(getParamInterfaceObj(), locale);
                     }
                     if (getComboTracker().getCurrentCombo() 
                             == getDataSetCombo()) {
                         row = 0;
                     }
                     if (getTable().getItemCount() != 0) {
                         if (getTable().getItemCount() <= row
                                 && getTable().getItemCount() > 0) {
                             --row;
                             getTable().setSelection(row);
                         } else {
                             getTable().setSelection(row);
                         }
                         getTableCursor().setSelection(row, 1);
                     } else {
                         getDeleteButton().setEnabled(false);
                         getInsertButton().setEnabled(false);
                     }
                     setFocus();
                     DataEventDispatcher.getInstance()
                             .fireParamChangedListener();
                 }
             } catch (PMException pme) {
                 PMExceptionHandler.handlePMExceptionForEditor(pme, editor);
             }
         }
     }
 
     /**
      * Reacts on the changes from the SelectionService of Eclipse.
      * @param part The Workbenchpart.
      * @param selection The selection.
      */
     private void reactOnChange(IWorkbenchPart part,
             IStructuredSelection selection) {
         m_currentPart = part;
         m_currentSelection = selection;
         getControlEnabler().selectionChanged(part, selection);
         
         IParameterInterfacePO paramInterfacePO = 
             getSelectedParamInterfaceObj(selection);
         if (getParamInterfaceObj() != null
                 && getParamInterfaceObj() == paramInterfacePO) {
             // identity check is ok here because node of SpecBrowser and
             // SpecEditor are equal but can have different data if the
             // Editor node has been edited!
             return;
         }
         
         setParamInterfaceObj(paramInterfacePO);
         updateView();
     }
     
     /**
      * @param selection
      *            the current selection
      * @return the valid param interface po or <code>null</code> if current
      *         selection does not contain a IParameterInterfacePO
      */
     private IParameterInterfacePO getSelectedParamInterfaceObj(
             IStructuredSelection selection) {
         IParameterInterfacePO paramInterfacePO = null;
         Object firstSel = selection.getFirstElement();
         if (firstSel instanceof IParameterInterfacePO) {
             paramInterfacePO = (IParameterInterfacePO)firstSel;
         }
         return paramInterfacePO;
     }
 
     /**
      * checks the given IParameterInterfacePO if all entrySets are complete for
      * the given Locale and sets the flag.
      * 
      * @param paramNode
      *            teh ParamNodePO to check.
      * @param locale
      *            the Locale to check
      */
     protected abstract void setIsEntrySetComplete(
             IParameterInterfacePO paramNode, Locale locale);
     
     /**
      * Class for En-/Disabling swt.Controls depending of active WorkbenchPart
      * and selection
      * @author BREDEX GmbH
      * @created 06.04.2006
      */
     protected class ControlEnabler extends AbstractControlEnabler 
             implements ISelectionListener {
         
         /** {@inheritDoc} */
         public void selectionChanged(IWorkbenchPart part, 
             ISelection selection) {
             if (!(selection instanceof IStructuredSelection)) { 
                 // e.g. in Jubula plugin-version you can open an java editor, 
                 // that reacts on org.eclipse.jface.text.TextSelection, which
                 // is not a StructuredSelection
                 return;
             }
             IStructuredSelection strucSelection = 
                     (IStructuredSelection)selection;
             IParameterInterfacePO paramNode = getSelectedParamInterfaceObj(
                     strucSelection);
 
             boolean correctPart = false;
             if (part != null) {
                 correctPart = (part == AbstractDataSetPage.this || part
                         .getAdapter(AbstractJBEditor.class) != null);
             }
             if (!correctPart) {
                 getTable().setForeground(LayoutUtil.GRAY_COLOR);
             } else {
                 getTable().setForeground(LayoutUtil.DEFAULT_OS_COLOR);
             }
             boolean hasInput = !strucSelection.isEmpty();
             boolean isEditorOpen = isEditorOpen(paramNode);
             boolean hasParameter = false; 
             boolean hasExcelFile = false;
             boolean hasReferencedDataCube = false;
             if (paramNode != null) {
                 hasParameter = !paramNode.getParameterList().isEmpty();
                 final String dataFile = paramNode.getDataFile();
                 hasExcelFile = !(dataFile == null || dataFile.length() == 0);
                 hasReferencedDataCube = 
                     paramNode.getReferencedDataCube() != null;
             }
             // En-/disable controls
             boolean isCAP = paramNode instanceof ICapPO;
             final boolean enable = correctPart && hasInput && isEditorOpen
                 && !isCAP && !hasExcelFile && !hasReferencedDataCube 
                 && hasParameter;
             setControlsEnabled(enable);
         }
     }
     /**
      * Checks if the given IParameterInterfacePO is in an open editor.
      * @param paramObj the object to check
      * @return true if the given node is in an open editor, false otherwise.
      */
     protected abstract boolean isEditorOpen(IParameterInterfacePO paramObj);
     
     /** {@inheritDoc} */
     public void selectionChanged(IWorkbenchPart part,
             ISelection selection) {
             
         if (!(selection instanceof IStructuredSelection)) { 
             // e.g. in Jubula plugin-version you can open an java editor, 
             // that reacts on org.eclipse.jface.text.TextSelection, which
             // is not a StructuredSelection
             return;
         }
                 
         reactOnChange(part, (IStructuredSelection)selection);
     }
 
     
     
     /**
      * @param paramBP
      *            the paramBP to set
      */
     private void setParamBP(AbstractParamInterfaceBP paramBP) {
         m_paramBP = paramBP;
     }
 
     /**
      * @return the paramBP
      */
     private AbstractParamInterfaceBP getParamBP() {
         return m_paramBP;
     }
 
     /**
      * @param paramInterfaceObj
      *            the paramInterfaceObj to set
      */
     private void setParamInterfaceObj(IParameterInterfacePO paramInterfaceObj) {
         m_paramInterfaceObj = paramInterfaceObj;
     }
 
     /**
      * @return the paramInterfaceObj
      */
     private IParameterInterfacePO getParamInterfaceObj() {
         return m_paramInterfaceObj;
     }
     
     /**
      * hint: the string could be null.
      * 
      * @param value
      *            to convert
      * @param paramInterfaceObj
      *            obj with parameter for this parameterValue
      * @param locale
      *            current used language
      * @param currentParamDescription
      *            param description associated with current string (parameter
      *            value)
      * @param dataValidator
      *            to use for special validations
      * @return a valid GuiParamValueConverter
      */
     private GuiParamValueConverter getGuiParamValueConverter(String value,
             IParameterInterfacePO paramInterfaceObj, Locale locale,
             IParamDescriptionPO currentParamDescription,
             IParamValueValidator dataValidator) {
         return new GuiParamValueConverter(value, paramInterfaceObj, locale,
                 currentParamDescription, dataValidator);
     }
     
     /**
      * 
      * @param paramInterface The object on which the input is based.
      * @return an object suitable for use as input in a DSV table.
      */
     protected ITDManager getInputForTable(
             IParameterInterfacePO paramInterface) {
         return paramInterface.getDataManager();
     }
 
     /**
      * @param tableCursor the tableCursor to set
      */
     private void setTableCursor(DSVTableCursor tableCursor) {
         m_tableCursor = tableCursor;
     }
 
     /**
      * @return the tableCursor
      */
     public DSVTableCursor getTableCursor() {
         return m_tableCursor;
     }
 }

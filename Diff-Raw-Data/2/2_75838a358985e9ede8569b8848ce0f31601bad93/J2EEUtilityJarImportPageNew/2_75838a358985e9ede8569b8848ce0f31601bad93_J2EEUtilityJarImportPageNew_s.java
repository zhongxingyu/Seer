 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  * David Schneider, david.schneider@unisys.com - [142500] WTP properties pages fonts don't follow Eclipse preferences
  *******************************************************************************/
 /*
  * Created on Dec 8, 2003
  * 
  * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEUtilityJarListImportDataModelProperties;
 import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPluginIcons;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 
 /**
  * @author mdelder
  */
 public class J2EEUtilityJarImportPageNew extends J2EEImportPage {
 
 	private static final String STORE_LABEL = "J2EE_UTILITY_JAR_LIST_IMPORT_"; //$NON-NLS-1$
 	// private static final int SIZING_TEXT_FIELD_WIDTH = 305;
 
 	private Button deselectAllButton;
 
 	private Button selectAllButton;
 
 	protected Button browseButton;
 
 	protected Button useAlternateRootBtn;
 
 	private Button overwriteProjectCheckbox;
 
 	protected CheckboxTableViewer availableJARsViewer;
 
 	protected boolean utilJarSelectionChanged = false;
 
 	private Combo availableJarsCombo;
 
 	protected Button linkedPathCheckbox;  
 
 
 	/**
 	 * @param model
 	 * @param pageName
 	 */
 	public J2EEUtilityJarImportPageNew(IDataModel model, String pageName) {
 		super(model, pageName);
 		setTitle(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_0")); //$NON-NLS-1$
 		setDescription(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_1")); //$NON-NLS-1$
 		setImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(J2EEUIPluginIcons.EAR_IMPORT_WIZARD_BANNER));
 		setInfopopID(IJ2EEUIContextIds.IMPORT_UTILITY_JAR_WIZARD_P1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#createTopLevelComposite(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Composite createTopLevelComposite(Composite parent) {
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		createUtilityJarFileNameComposite(composite);
 		createLinkedPathVariable(composite);
 		createJARsComposite(composite);
 		createOverwriteCheckbox(composite); 
 
 		restoreWidgetValues();
 	    Dialog.applyDialogFont(parent);
 		return composite;
 	}
 
 	/**
 	 * @param composite
 	 */
 	protected void createUtilityJarFileNameComposite(Composite parent) {
 		Group fileNameGroup = new Group(parent, SWT.NULL);
 		fileNameGroup.setText(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_2")); //$NON-NLS-1$
 		GridLayout layout = new GridLayout(3, false);
 		fileNameGroup.setLayout(layout);
 		fileNameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label fileLabel = new Label(fileNameGroup, SWT.NONE);
 		fileLabel.setText(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_3")); //$NON-NLS-1$
 		fileLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 
 		// setup combo
 		availableJarsCombo = new Combo(fileNameGroup, SWT.SINGLE | SWT.BORDER);
 		availableJarsCombo.setLayoutData((new GridData(GridData.FILL_HORIZONTAL)));
 
 		// setup browse button
 		browseButton = new Button(fileNameGroup, SWT.PUSH);
 		browseButton.setText(defBrowseButtonLabel);
 		browseButton.setLayoutData((new GridData(GridData.HORIZONTAL_ALIGN_END)));
 		browseButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleBrowseButtonPressed();
 			}
 		});
 		browseButton.setEnabled(true);
 
 		synchHelper.synchCombo(availableJarsCombo, IJ2EEUtilityJarListImportDataModelProperties.AVAILABLE_JARS_DIRECTORY, new Control[]{fileLabel, browseButton});
 	}
 
 	protected void createLinkedPathVariable(Composite parent) {
 
 
 		Group linkedPathGroup = new Group(parent, SWT.NULL);
 		linkedPathGroup.setText(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_4")); //$NON-NLS-1$
 
 		GridLayout layout = new GridLayout(1, true);
 		linkedPathGroup.setLayout(layout);
 		linkedPathGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Composite checkboxGroup = new Composite(linkedPathGroup, SWT.NULL);
 		GridLayout checkboxLayout = new GridLayout(2, false);
 		checkboxGroup.setLayout(checkboxLayout);
 		checkboxGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		linkedPathCheckbox = new Button(checkboxGroup, SWT.CHECK);
 		linkedPathCheckbox.setText(" "); //$NON-NLS-1$
 		final Text linkedPathText = new Text(checkboxGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
 		linkedPathText.setText(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_5")); //$NON-NLS-1$
		linkedPathText.setEnabled(getDataModel().isPropertyEnabled(IJ2EEUtilityJarListImportDataModelProperties.CREATE_LINKED_PATH_VARIABLE));
 		
 		getDataModel().addListener(new IDataModelListener() {
 			public void propertyChanged(DataModelEvent event) {
 				if(IJ2EEUtilityJarListImportDataModelProperties.CREATE_LINKED_PATH_VARIABLE.equals(event.getPropertyName())) {
 					linkedPathText.setEnabled(getDataModel().isPropertyEnabled(IJ2EEUtilityJarListImportDataModelProperties.CREATE_LINKED_PATH_VARIABLE));
 				}
 				
 			}
 		});
 		
 		GridData textGridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
 		textGridData.heightHint = 50;
 		textGridData.widthHint = 350;
 		linkedPathText.setLayoutData(textGridData);
 		linkedPathText.setBackground(checkboxGroup.getBackground());
 
 		// setup combo
 		Combo availableLinkedPathsCombo = new Combo(linkedPathGroup, SWT.SINGLE | SWT.BORDER);
 		availableLinkedPathsCombo.setLayoutData((new GridData(GridData.FILL_HORIZONTAL)));
 
 		synchHelper.synchCombo(availableLinkedPathsCombo, IJ2EEUtilityJarListImportDataModelProperties.LINKED_PATH_VARIABLE, new Control[]{availableLinkedPathsCombo});
 
 		synchHelper.synchCheckbox(linkedPathCheckbox, IJ2EEUtilityJarListImportDataModelProperties.CREATE_LINKED_PATH_VARIABLE, new Control[]{availableLinkedPathsCombo});
 
 	}
 
 	/**
 	 * Open an appropriate directory browser
 	 */
 	protected void handleBrowseButtonPressed() {
 		DirectoryDialog dialog = new DirectoryDialog(browseButton.getShell());
 		dialog.setMessage(J2EEUIMessages.getResourceString(J2EEUIMessages.SELECT_DIRECTORY_DLG));
 
 		String dirName = getBrowseStartLocation();
 
 		if (!isNullOrEmpty(dirName)) {
 			File path = new File(dirName);
 			if (path.exists())
 				dialog.setFilterPath(dirName);
 		}
 
 		String selectedDirectory = dialog.open();
 		if (selectedDirectory != null)
 			availableJarsCombo.setText(selectedDirectory);
 	}
 
 	/**
 	 * @return
 	 */
 	protected String getBrowseStartLocation() {
 		if (availableJarsCombo.getText() != null && availableJarsCombo.getText().length() > 0)
 			return availableJarsCombo.getText();
 		return null;
 	}
 
 	protected boolean isNullOrEmpty(String aString) {
 		return aString == null || aString.length() == 0;
 	}
 
 	/*
 	 * Updates the enable state of the all buttons
 	 */
 	protected void updateButtonEnablements() {
 
 		utilJarSelectionChanged = true;
 	}
 
 	protected void createAvailableJarsList(Composite listGroup) {
 		availableJARsViewer = CheckboxTableViewer.newCheckList(listGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 
 		GridData gData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
 		gData.widthHint = 200;
 		gData.heightHint = 80;
 		availableJARsViewer.getControl().setLayoutData(gData);
 		AvailableUtilityJarsProvider availableUtilJARsProvider = new AvailableUtilityJarsProvider();
 		availableJARsViewer.setContentProvider(availableUtilJARsProvider);
 		availableJARsViewer.setLabelProvider(availableUtilJARsProvider);
 
 		availableJARsViewer.getTable().setHeaderVisible(false);
 		availableJARsViewer.getTable().setLinesVisible(false);
 
 		availableJARsViewer.setInput(model);
 
 		/* getModel().addListener(getOperationDataModelListener()); */
 		synchHelper.synchCheckBoxTableViewer(availableJARsViewer, IJ2EEUtilityJarListImportDataModelProperties.UTILITY_JAR_LIST, null);
 
 
 		model.addListener(new IDataModelListener() {
 
 			public void propertyChanged(DataModelEvent event) {
 				if (IJ2EEUtilityJarListImportDataModelProperties.AVAILABLE_JARS_DIRECTORY.equals(event.getPropertyName()))
 					availableJARsViewer.setInput(model);
 			}
 		});
 	}
 
 	private void handleDeselectAllButtonPressed() {
 		model.setProperty(IJ2EEUtilityJarListImportDataModelProperties.UTILITY_JAR_LIST, new Object[0]);
 	}
 
 	private void handleSelectAllButtonPressed() {
 		Object[] selection = new Object[availableJARsViewer.getTable().getItemCount()];
 		for (int i = 0; i < selection.length; i++) {
 			selection[i] = availableJARsViewer.getElementAt(i);
 		}
 		model.setProperty(IJ2EEUtilityJarListImportDataModelProperties.UTILITY_JAR_LIST, selection);
 	}
 
 	protected void createButtonsGroup(org.eclipse.swt.widgets.Composite parent) {
 		Composite buttonGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 4;
 		buttonGroup.setLayout(layout);
 		buttonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		selectAllButton = new Button(buttonGroup, SWT.PUSH);
 		selectAllButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_SELECT_ALL_UTIL_BUTTON));
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = 1;
 		gd.heightHint = 22;
 		gd.widthHint = 120;
 		selectAllButton.setLayoutData(gd);
 		selectAllButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleSelectAllButtonPressed();
 			}
 		});
 
 		deselectAllButton = new Button(buttonGroup, SWT.PUSH);
 		deselectAllButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_DESELECT_ALL_UTIL_BUTTON));
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = 2;
 		gd.heightHint = 22;
 		gd.widthHint = 120;
 		deselectAllButton.setLayoutData(gd);
 		deselectAllButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleDeselectAllButtonPressed();
 			}
 		});
 	}
 
 	protected void createJARsComposite(Composite parent) {
 		Group group = new Group(parent, SWT.NULL);
 		group.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.EAR_IMPORT_JARS_GROUP));
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		group.setLayout(layout);
 		group.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label description = new Label(group, SWT.NULL);
 		description.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.J2EE_UTILITY_JAR_LISTEAR_IMPORT_SELECT_UTIL_JARS_TO_BE_PROJECTS));
 		GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd2.horizontalSpan = 2;
 		description.setLayoutData(gd2);
 
 		// create jars check box viewer
 		createAvailableJarsList(group);
 		createButtonsGroup(group);
 	}
 
 	/**
 	 * @param projectOptionsGroup
 	 */
 	protected void createOverwriteCheckbox(Composite parent) {
 
 		overwriteProjectCheckbox = new Button(parent, SWT.CHECK);
 		overwriteProjectCheckbox.setText(J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_6")); //$NON-NLS-1$
 		synchHelper.synchCheckbox(overwriteProjectCheckbox, IJ2EEUtilityJarListImportDataModelProperties.OVERWRITE_IF_NECESSARY, null);
 	}
 
 	protected void setJARsCompositeEnabled(boolean enabled) {
 		availableJARsViewer.getTable().setEnabled(enabled);
 		availableJARsViewer.setAllChecked(false);
 		availableJARsViewer.setAllGrayed(!enabled);
 		selectAllButton.setEnabled(enabled);
 		deselectAllButton.setEnabled(enabled);
 	}
 
 	@Override
 	protected String[] getValidationPropertyNames() {
 		return new String[]{IJ2EEUtilityJarListImportDataModelProperties.UTILITY_JAR_LIST, IJ2EEUtilityJarListImportDataModelProperties.OVERWRITE_IF_NECESSARY, IJ2EEUtilityJarListImportDataModelProperties.LINKED_PATH_VARIABLE};
 	}
 
 	@Override
 	protected void restoreWidgetValues() {
 
 		IDialogSettings settings = getDialogSettings();
 		if (settings != null) {
 			String[] sourceNames = settings.getArray(STORE_LABEL + getFileNamesStoreID());
 			if (sourceNames == null)
 				return; // ie.- no settings stored
 			for (int i = 0; i < sourceNames.length; i++) {
 				if (sourceNames[i] == null)
 					sourceNames[i] = ""; //$NON-NLS-1$
 			}
 			availableJarsCombo.setItems(sourceNames);
 		}
 	}
 
 	@Override
 	public void storeDefaultSettings() {
 		IDialogSettings settings = getDialogSettings();
 		if (settings != null) {
 			// update source names history
 			String[] sourceNames = settings.getArray(STORE_LABEL + getFileNamesStoreID());
 			if (sourceNames == null) {
 				sourceNames = new String[0];
 			}
 
 			String newName = availableJarsCombo.getText();
 
 			// rip out any empty filenames and trim length to 5
 			ArrayList newNames = new ArrayList();
 			for (int i = 0; i < sourceNames.length && i < 5; i++) {
 				if (sourceNames[i].trim().length() > 0 && !newName.equals(sourceNames[i])) {
 					newNames.add(sourceNames[i]);
 				}
 			}
 			newNames.add(0, availableJarsCombo.getText());
 			sourceNames = new String[newNames.size()];
 			newNames.toArray(sourceNames);
 
 			settings.put(STORE_LABEL + getFileNamesStoreID(), sourceNames);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEImportPage#getFileNamesStoreID()
 	 */
 	@Override
 	protected String getFileNamesStoreID() {
 		return "UTIL";//$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.internal.ui.wizard.J2EEImportPage#getFileImportLabel()
 	 */
 	@Override
 	protected String getFileImportLabel() {
 		return J2EEUIMessages.getResourceString("J2EEUtilityJarImportPage_UI_7"); //$NON-NLS-1$
 	}
 	
 	
 	@Override
 	public void restoreDefaultSettings() {
 	}
  
 }

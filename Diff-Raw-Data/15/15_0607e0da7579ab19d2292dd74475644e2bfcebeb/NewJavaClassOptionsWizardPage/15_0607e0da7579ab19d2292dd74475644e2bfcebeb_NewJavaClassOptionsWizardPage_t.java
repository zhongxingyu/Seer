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
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jst.j2ee.internal.actions.IJ2EEUIContextIds;
 import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
 import org.eclipse.jst.j2ee.internal.dialogs.TypeSearchEngine;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.Workbench;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
 
 /**
  * @author jialin
  * 
  * To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class NewJavaClassOptionsWizardPage extends DataModelWizardPage {
 
 	protected Button publicButton;
 	protected Button abstractButton;
 	protected Button finalButton;
 	protected ListViewer interfaceViewer;
 	protected Button addButton;
 	protected Button removeButton;
 	protected Button inheritButton;
 	protected Button constructorButton;
 	protected Button mainMethodButton;
 
 	/**
 	 * @param model
 	 * @param pageName
 	 */
 	public NewJavaClassOptionsWizardPage(IDataModel model, String pageName, String pageDesc, String pageTitle) {
 		super(model, pageName);
 		setDescription(pageDesc);
 		this.setTitle(pageTitle);
 		setInfopopID(IJ2EEUIContextIds.NEW_JAVA_CLASS_OPTION_WIZARD_P1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jem.util.ui.wizard.WTPWizardPage#getValidationPropertyNames()
 	 */
 	protected String[] getValidationPropertyNames() {
 		return new String[]{INewJavaClassDataModelProperties.MODIFIER_ABSTRACT, INewJavaClassDataModelProperties.MODIFIER_FINAL};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jem.util.ui.wizard.WTPWizardPage#createTopLevelComposite(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		composite.setLayout(layout);
 		GridData data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = 300;
 		composite.setLayoutData(data);
 
 		createModifierControls(composite);
 		createInterfaceControls(composite);
 
 		// Separator label
 		Label seperator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
 		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		data.horizontalSpan = 2;
 		seperator.setLayoutData(data);
 
 		createStubsComposite(composite);
 
 		publicButton.setFocus();
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, getInfopopID());
 	    Dialog.applyDialogFont(parent);
 		return composite;
 	}
 
 	/**
 	 * Create controls for the modifiers
 	 */
 	protected void createModifierControls(Composite parent) {
 		Label modifiersLabel = new Label(parent, SWT.NONE);
 		modifiersLabel.setText(J2EEUIMessages.JAVA_CLASS_MODIFIERS_LABEL);
 		modifiersLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 		Composite composite = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		layout.makeColumnsEqualWidth = true;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		publicButton = new Button(composite, SWT.CHECK);
 		publicButton.setText(J2EEUIMessages.JAVA_CLASS_PUBLIC_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(publicButton, INewJavaClassDataModelProperties.MODIFIER_PUBLIC, null);
 
 		abstractButton = new Button(composite, SWT.CHECK);
 		abstractButton.setText(J2EEUIMessages.JAVA_CLASS_ABSTRACT_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(abstractButton, INewJavaClassDataModelProperties.MODIFIER_ABSTRACT, null);
 
 		finalButton = new Button(composite, SWT.CHECK);
 		finalButton.setText(J2EEUIMessages.JAVA_CLASS_FINAL_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(finalButton, INewJavaClassDataModelProperties.MODIFIER_FINAL, null);
 	}
 
 	protected void createInterfaceControls(Composite parent) {
 		Label interfaceLabel = new Label(parent, SWT.NONE);
 		interfaceLabel.setText(J2EEUIMessages.JAVA_CLASS_INTERFACES_LABEL);
 		interfaceLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
 		Composite composite = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		interfaceViewer = new ListViewer(composite);
 		interfaceViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
 		interfaceViewer.setContentProvider(getInterfaceContentProvider());
 		interfaceViewer.setLabelProvider(getInterfaceLabelProvider());
 		interfaceViewer.setInput(model.getProperty(INewJavaClassDataModelProperties.INTERFACES));
 
 		Composite buttonCompo = new Composite(composite, SWT.NULL);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		buttonCompo.setLayout(layout);
 		buttonCompo.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_BEGINNING));
 
 		addButton = new Button(buttonCompo, SWT.PUSH);
 		addButton.setText(J2EEUIMessages.ADD_BUTTON_LABEL);
 		addButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL));
 		addButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent event) {
 				handleInterfaceAddButtonSelected();
 			}
 			public void widgetDefaultSelected(SelectionEvent event) {
 				//Do nothing
 			}
 		});
 
 		removeButton = new Button(buttonCompo, SWT.PUSH);
 		removeButton.setText(J2EEUIMessages.REMOVE_BUTTON);
 		removeButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL));
 		removeButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent event) {
 				handleInterfaceRemoveButtonSelected();
 			}
 			public void widgetDefaultSelected(SelectionEvent event) {
 				//Do nothing
 			}
 		});
 		removeButton.setEnabled(false);
 
 		interfaceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection selection = event.getSelection();
 				removeButton.setEnabled(!selection.isEmpty());
 			}
 		});
 
 	}
 
 	/**
 	 * Create the composite with all the stubs
 	 */
 	protected void createStubsComposite(Composite parent) {
 		Label stubLabel = new Label(parent, SWT.NONE);
 		stubLabel.setText(J2EEUIMessages.JAVA_CLASS_METHOD_STUBS_LABEL);
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		data.horizontalSpan = 2;
 		stubLabel.setLayoutData(data);
 
 		Composite buttonCompo = new Composite(parent, SWT.NULL);
 		buttonCompo.setLayout(new GridLayout());
 		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		data.horizontalSpan = 2;
 		data.horizontalIndent = 15;
 		buttonCompo.setLayoutData(data);
 
 		mainMethodButton = new Button(buttonCompo, SWT.CHECK);
 		mainMethodButton.setText(J2EEUIMessages.JAVA_CLASS_MAIN_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(mainMethodButton, INewJavaClassDataModelProperties.MAIN_METHOD, null);
 
 		inheritButton = new Button(buttonCompo, SWT.CHECK);
 		inheritButton.setText(J2EEUIMessages.JAVA_CLASS_INHERIT_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(inheritButton, INewJavaClassDataModelProperties.ABSTRACT_METHODS, null);
 
 		constructorButton = new Button(buttonCompo, SWT.CHECK);
 		constructorButton.setText(J2EEUIMessages.JAVA_CLASS_CONSTRUCTOR_CHECKBOX_LABEL);
 		synchHelper.synchCheckbox(constructorButton, INewJavaClassDataModelProperties.CONSTRUCTOR, null);
 	}
 
 	/**
 	 * @see IStatefulWizardPage#saveWidgetValues()
 	 */
 	// public void saveWidgetValues() {
 	// // TODO: do we want to do this here or in the concrete subclasses???
 	// IDialogSettings store = getDialogSettings();
 	// if (store != null)
 	// store.put(getUniqueKey(""), getUniqueKey("")); //$NON-NLS-1$
 	// //$NON-NLS-2$
 	// DialogSettingsHelper.saveButton(inheritButton,
 	// getUniqueKey(idInheritedAbstractButton), store);
 	// DialogSettingsHelper.saveButton(constructorButton,
 	// getUniqueKey(idSuperClassConstructorsButton), store);
 	// DialogSettingsHelper.saveButton(publicButton,
 	// getUniqueKey(idPublicButton), store);
 	// DialogSettingsHelper.saveButton(abstractButton,
 	// getUniqueKey(idAbstractButton), store);
 	// DialogSettingsHelper.saveButton(finalButton, getUniqueKey(idFinalButton),
 	// store);
 	//
 	// }
 	/**
 	 * @see IStatefulWizardPage#restoreWidgetValues()
 	 */
 	// public void restoreWidgetValues() {
 	// IDialogSettings store = super.getDialogSettings();
 	// if (store != null && store.get(getUniqueKey("")) != null) { //$NON-NLS-1$
 	// DialogSettingsHelper.restoreButton(inheritButton,
 	// getUniqueKey(idInheritedAbstractButton), store);
 	// DialogSettingsHelper.restoreButton(constructorButton,
 	// getUniqueKey(idSuperClassConstructorsButton), store);
 	// DialogSettingsHelper.restoreButton(publicButton,
 	// getUniqueKey(idPublicButton), store);
 	// DialogSettingsHelper.restoreButton(abstractButton,
 	// getUniqueKey(idAbstractButton), store);
 	// DialogSettingsHelper.restoreButton(finalButton,
 	// getUniqueKey(idFinalButton), store);
 	// }
 	// }
 	/**
 	 * Returns the Super Interface Content Provider
 	 */
 	protected IStructuredContentProvider getInterfaceContentProvider() {
 		return new IStructuredContentProvider() {
 			public Object[] getElements(Object inputElement) {
 				Object[] ret = new Object[0];
 				if (inputElement instanceof ArrayList) {
 					ret = ((ArrayList) inputElement).toArray();
 				}
 				return ret;
 			}
 			public void dispose() {
 				//Do nothing
 			}
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 				//Default is nothing
 			}
 		};
 	}
 
 	/**
 	 * Returns the Super Interface Label Provider
 	 */
 	protected ILabelProvider getInterfaceLabelProvider() {
 		return new ILabelProvider() {
 			public Image getImage(Object element) {
 				return J2EEUIPlugin.getDefault().getImage("full/obj16/interface_obj"); //$NON-NLS-1$
 			}
 
 			public String getText(Object element) {
 				String ret = J2EEUIMessages.EMPTY_STRING;
 				if (element instanceof String)
 					ret = (String) element;
 				return ret;
 			}
 
 			public void addListener(ILabelProviderListener listener) {
 				//Do nothing
 			}
 			public void dispose() {
 				//Do nothing
 			}
 			public boolean isLabelProperty(Object element, String property) {
 				return true;
 			}
 			public void removeListener(ILabelProviderListener listener) {
 				//Do nothing
 			}
 		};
 	}
 
 	/**
 	 * Browse for a new Super Interface Class
 	 */
 	protected void handleInterfaceAddButtonSelected() {
 		IProject project = (IProject) model.getProperty(INewJavaClassDataModelProperties.PROJECT);
 		IRunnableContext context = Workbench.getInstance().getActiveWorkbenchWindow();
 		IJavaProject javaProject = JemProjectUtilities.getJavaProject(project);
 		// this eliminates the non-exported classpath entries
 		final IJavaSearchScope scope = TypeSearchEngine.createJavaSearchScopeForAProject(javaProject, true, true);
		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(),false, context, scope,IJavaSearchConstants.INTERFACE);
 		dialog.setTitle(J2EEUIMessages.INTERFACE_SELECTION_DIALOG_TITLE);
 		if (dialog.open() == Window.OK) {
 			IType type = (IType) dialog.getFirstResult();
 			String superclassFullPath = ""; //$NON-NLS-1$
 			if (type != null)
 				superclassFullPath = type.getFullyQualifiedName();
 			interfaceViewer.add(superclassFullPath);
 		}
 		List valueList = Arrays.asList(interfaceViewer.getList().getItems());
 		model.setProperty(INewJavaClassDataModelProperties.INTERFACES, valueList);
 	}
 
 	/**
 	 * Remove an interface from the SuperInterface List
 	 */
 	protected void handleInterfaceRemoveButtonSelected() {
 		IStructuredSelection selection = (IStructuredSelection) interfaceViewer.getSelection();
 		List items = selection.toList();
 		if (!items.isEmpty()) {
 			Object array[] = interfaceViewer.getList().getItems();
 			List valueList = new ArrayList();
 			
 			for (int i = 0; i < array.length; i++) {
 				valueList.add(array[i]);
 			}
 			for (int i=0; i<items.size(); i++) {
 				valueList.remove(items.get(i));
 			}
 			interfaceViewer.setInput(valueList);
 			model.setProperty(INewJavaClassDataModelProperties.INTERFACES, valueList);
 		}
 	}
 }

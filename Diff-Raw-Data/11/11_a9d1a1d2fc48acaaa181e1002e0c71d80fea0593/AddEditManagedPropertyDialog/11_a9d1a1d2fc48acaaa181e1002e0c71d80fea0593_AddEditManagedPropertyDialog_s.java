 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.ClassButtonDialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.ComboDialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IDialogFieldApplyListener;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.IStringButtonAdapter;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.LayoutUtil;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.StringButtonDialogField;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.ListEntriesType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedBeanType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedPropertyType;
 import org.eclipse.jst.jsf.facesconfig.emf.MapEntriesType;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorMessages;
 import org.eclipse.jst.jsf.facesconfig.ui.EditorPlugin;
 import org.eclipse.jst.jsf.facesconfig.ui.IFacesConfigConstants;
 import org.eclipse.jst.jsf.facesconfig.ui.section.AbstractFacesConfigSection;
 import org.eclipse.jst.jsf.facesconfig.ui.util.JavaBeanProperty;
 import org.eclipse.jst.jsf.facesconfig.ui.util.JavaBeanUtils;
 import org.eclipse.jst.jsf.facesconfig.ui.util.JavaClassUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * The dialog is for adding and editing managed property.
  * 
  * @author sfshi
  * 
  */
 public class AddEditManagedPropertyDialog extends Dialog {
 
 	private static final int MIN_DIALOG_WIDTH = 300;
 
 	private static final int DEFAULT_CONTROL_WIDTH = 200;
 
 	private ComboDialogField nameField;
 
 	private ClassButtonDialogField classField;
 
 	private ComboDialogField valueTypeField;
 
 	private StringButtonDialogField valueField;
 
 	private IProject project;
 
 	private String propertyName;
 
 	private String propertyClass;
 
 	private String valueType;
 
 	/**
 	 * the value could be a string, or instance of <code>NullValueType</code>,
 	 * <code>MapEntriesType</code> and <code>ListEntriesType</code>.
 	 */
 	private Object valueObject;
 
 	private ManagedBeanType managedBean;
 
 	/**
 	 * the list of this bean properties.
 	 */
 	private List beanPropertyList;
 
 	/**
 	 * the managed-property object that is editing, but it will be read-only.
 	 */
 	private ManagedPropertyType managedProperty;
 
 	// private FormToolkit toolkit;
 
 	private Composite parent;
 
 	private static final String[] valueTypes = { IFacesConfigConstants.VALUE,
 			IFacesConfigConstants.NULL_VALUE,
 			IFacesConfigConstants.MAP_ENTRIES,
 			IFacesConfigConstants.LIST_ENTRIES };
 
 	private boolean isNew;
 
 	private AbstractFacesConfigSection section;
 
 	/**
 	 * constructor for adding a managed property.
 	 * 
 	 * @param parentShell
 	 * @param managedBean
 	 * @param section 
 	 */
 	public AddEditManagedPropertyDialog(Shell parentShell,
 			ManagedBeanType managedBean, AbstractFacesConfigSection section) {
 		super(parentShell);
 		this.isNew = true;
 		this.managedBean = managedBean;
 		this.section = section;
 	}
 
 	/**
 	 * 
 	 * @param parentShell
 	 * @param managedProperty
 	 * @param section
 	 */
 	public AddEditManagedPropertyDialog(Shell parentShell,
 			ManagedPropertyType managedProperty, AbstractFacesConfigSection section) {
 		super(parentShell);
 		this.isNew = false;
 		this.managedProperty = managedProperty;
 		this.managedBean = (ManagedBeanType) managedProperty.eContainer();
 		this.section = section;
 	}
 
 	/*
 	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
 	 */
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		if (isNew)
 			newShell.setText(EditorMessages.AddEditManagedPropertyDialog_Add);
 		else
 			newShell.setText(EditorMessages.AddEditManagedPropertyDialog_Edit);
 	}
 
 	/*
 	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
 	 */
 	protected Control createDialogArea(Composite parent_) {
 		Composite container = new Composite(parent_, SWT.FILL);
 		this.parent = container;
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginWidth = layout.marginHeight = 5;
 		container.setLayout(layout);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		container.setLayoutData(gd);
 
 		nameField = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY
 				| SWT.BORDER);
 		nameField.setLabelText(EditorMessages.ManagedBeanPropertyEditDialog_PropertyName);
 
 		int propertyCount = 0;
 		beanPropertyList = getBeanPropertyList(managedBean);
 		if (beanPropertyList != null)
 			propertyCount = beanPropertyList.size();
 
 		if (propertyCount > 0) {
 			String[] propertyNames = new String[propertyCount];
 
 			for (int i = 0; i < propertyCount; i++) {
 				JavaBeanProperty property = (JavaBeanProperty) beanPropertyList
 						.get(i);
 				propertyNames[i] = property.getName();
 			}
 			nameField.setItems(propertyNames);
 		}
 
 		nameField.setDialogFieldApplyListener(new IDialogFieldApplyListener() {
 			public void dialogFieldApplied(DialogField field) {
 				handleNameSelection(((ComboDialogField) field).getText());
 			}
 		});
 
 		classField = new ClassButtonDialogField(null);
 		classField.setProject(getProject());
 		classField.setLabelText(EditorMessages.ManagedBeanPropertyEditDialog_PropertyClass);
 
 		valueTypeField = new ComboDialogField(SWT.DROP_DOWN | SWT.READ_ONLY
 				| SWT.BORDER);
 		valueTypeField.setLabelText(EditorMessages.ManagedBeanPropertyEditDialog_ValueType);
 
 		valueTypeField.setItems(valueTypes);
 		valueTypeField
 				.setDialogFieldApplyListener(new IDialogFieldApplyListener() {
 					public void dialogFieldApplied(DialogField field) {
 						updateValueStatus();
 					}
 				});
 
 		valueField = new StringButtonDialogField(new IStringButtonAdapter() {
 			public void changeControlPressed(DialogField field) {
 				handleChangeValue(valueTypeField.getText(),
 						((StringButtonDialogField) field).getText());
 			}
 
 		});
 
 		valueField.setLabelText(EditorMessages.ManagedBeanPropertyEditDialog_Value);
 		valueField.setButtonLabel(EditorMessages.UI_Button_Edit_more);
 
 		valueField.setDialogFieldApplyListener(new IDialogFieldApplyListener() {
 
 			public void dialogFieldApplied(DialogField field) {
 				valueObject = ((StringButtonDialogField) field).getText();
 
 			}
 		});
 
 		GridData data = new GridData(GridData.FILL_BOTH);
 		container.setLayoutData(data);
 
 		int numberOfColumns = 5;
 		GridLayout gl = new GridLayout(numberOfColumns, false);
 		container.setLayout(gl);
 
 		nameField.doFillIntoGrid(null, container, numberOfColumns);
 		LayoutUtil.setWidthHint(nameField.getComboControl(null, container),
 				DEFAULT_CONTROL_WIDTH);
 
 		classField.doFillIntoGrid(null, container, numberOfColumns - 1);
 		data = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		data.horizontalSpan = 2;
 		classField.getChangeControl(null, container).setLayoutData(data);
 		LayoutUtil.setWidthHint(classField.getTextControl(null, container),
 				DEFAULT_CONTROL_WIDTH);
 
 		valueTypeField.doFillIntoGrid(null, container, numberOfColumns);
 
 		valueField.doFillIntoGrid(null, container, numberOfColumns);
 
 		LayoutUtil.setHorizontalGrabbing(classField.getTextControl(null,
 				container));
 
 		valueTypeField.selectItem(0);
 
 		if (!isNew && managedProperty != null) {
 			initFields();
 		}
 
 		return container;
 	}
 
 	private void initFields() {
 
 		if (managedProperty.getPropertyName() != null)
 			nameField.setText(managedProperty.getPropertyName()
 					.getTextContent());
 
 		if (managedProperty.getPropertyClass() != null)
 			classField.setText(managedProperty.getPropertyClass()
 					.getTextContent());
 
 		if (managedProperty.getValue() != null) {
 			valueTypeField.setText(valueTypes[0]);
 			valueField.setText(managedProperty.getValue().getTextContent());
 		} else if (managedProperty.getNullValue() != null)
 			valueTypeField.setText(valueTypes[1]);
 		else if (managedProperty.getMapEntries() != null)
 			valueTypeField.setText(valueTypes[2]);
 		else if (managedProperty.getListEntries() != null)
 			valueTypeField.setText(valueTypes[3]);
 		else
 			valueTypeField.setText(valueTypes[0]);
 		updateValueStatus();
 	}
 
 	/**
 	 * @param text
 	 */
 	protected void handleNameSelection(String text) {
 		int propertyCount = 0;
 		if (beanPropertyList != null)
 			propertyCount = beanPropertyList.size();
 
 		if (propertyCount > 0) {
 			for (int i = 0; i < propertyCount; i++) {
 				JavaBeanProperty property = (JavaBeanProperty) beanPropertyList
 						.get(i);
 
 				if (property.getName().equals(text.trim())) {
 					classField.setText(property.getQualifiedType());
 					break;
 				}
 			}
 		}
 
 	}
 
 	private void updateValueStatus() {
 		if (valueField == null) {
 			return;
 		}
 		valueObject = null;
 		if (valueTypeField.getText().equalsIgnoreCase(
 				IFacesConfigConstants.NULL_VALUE)) {
 			valueField.setEnabled(false);
 			valueObject = FacesConfigFactory.eINSTANCE.createNullValueType();
 		} else if (valueTypeField.getText().equalsIgnoreCase(
 				IFacesConfigConstants.MAP_ENTRIES)
 				|| valueTypeField.getText().equalsIgnoreCase(
 						IFacesConfigConstants.LIST_ENTRIES)) {
 			valueField.getTextControl(null, parent).setEditable(false);
 			valueField.setEnabled(true);
 		} else {
 			
 			valueObject = valueField.getText(); //Bug 173831
 			valueField.getTextControl(null, parent).setEditable(true);
 			valueField.setEnabled(true);
 		}
 	}
 
 	/**
 	 * @param valueType_ 
 	 * @param value 
 	 */
 	protected void handleChangeValue(String valueType_, String value) {
 		if (valueType_.equalsIgnoreCase(IFacesConfigConstants.VALUE)) {
 			handleChangeLongStringValue(value);
 		} else if (valueType_
 				.equalsIgnoreCase(IFacesConfigConstants.MAP_ENTRIES)) {
 			handleChangeMapEntries();
 		} else if (valueType_
 				.equalsIgnoreCase(IFacesConfigConstants.LIST_ENTRIES)) {
 			handleChangeListEntries();
 		}
 	}
 
 	/**
 	 * @param textControl
 	 */
 	private void handleChangeLongStringValue(String value) {
 		EditValueDialog valueDialog = new EditValueDialog(EditorPlugin
 				.getActiveShell(), value);
 		if (valueDialog.open() == Window.OK) {
 			valueField.setText((String) valueDialog.getResultData());
 			valueObject = valueDialog.getResultData();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void handleChangeMapEntries() {
 
 		MapEntriesType mapEntries;
 
 		if (valueObject instanceof MapEntriesType)
 			mapEntries = (MapEntriesType) valueObject;
 		else if (isNew || managedProperty.getMapEntries() == null) {
 			mapEntries = FacesConfigFactory.eINSTANCE.createMapEntriesType();
 		} else {
			mapEntries = (MapEntriesType) EcoreUtil.copy(managedProperty
 					.getMapEntries());
 		}
 
 		EditMapEntriesDialog dialog = new EditMapEntriesDialog(EditorPlugin
 				.getActiveShell(), mapEntries, section);
 		if (dialog.open() == Dialog.OK) {
 			valueObject = dialog.getMapEntries();
 		} else {
 			//user cancel the dialog, then restore.
 			if (!isNew && managedProperty.getMapEntries() != null)
 				valueObject = EcoreUtil.copy(managedProperty
 						.getMapEntries());
 			// else
 			//				valueObject = null;
 		}
 	}
 
 	private void handleChangeListEntries() {
 		ListEntriesType listEntries;
 
 		if (valueObject instanceof ListEntriesType)
 			listEntries = (ListEntriesType) valueObject;
 		else if (isNew || managedProperty.getListEntries() == null) {
 			listEntries = FacesConfigFactory.eINSTANCE.createListEntriesType();
 		} else {
			listEntries = (ListEntriesType) EcoreUtil.copy(managedProperty
 					.getListEntries());
 		}
 
 		EditListEntriesDialog dialog = new EditListEntriesDialog(EditorPlugin
 				.getActiveShell(), listEntries, section);
 		if (dialog.open() == Dialog.OK) {
 			valueObject = dialog.getListEntries();
 		} else {
 			//user cancel the dialog, then restore.
 			if (!isNew && managedProperty.getListEntries() != null)
 				valueObject = EcoreUtil.copy(managedProperty
 						.getListEntries());
 			// else
 			// valueObject = null;
 		}
 	}
 
 	/**
 	 * Get a list of JavaBeanProperty that from the managed bean class.
 	 * 
 	 * @return
 	 */
 	private List getBeanPropertyList(ManagedBeanType managedBean_) {
 		if (managedBean_.getManagedBeanClass() == null)
 			return null;
 		String beanClassName = managedBean_.getManagedBeanClass()
 				.getTextContent();
 		IType classType = JavaClassUtils.getType(getProject(), beanClassName);
 		if (classType == null) {
 			return null;
 		}
 		List list = new ArrayList();
 		JavaBeanProperty[] properties = JavaBeanUtils
 				.getBeanProperties(classType);
 		if (properties != null) {
 			for (int i = 0; i < properties.length; i++) {
 				list.add(properties[i]);
 			}
 		}
 		return list;
 	}
 
 	/*
 	 * @see org.eclipse.jface.window.Window#getInitialSize()
 	 */
 	protected Point getInitialSize() {
 		Point shellSize = super.getInitialSize();
 		return new Point(Math.max(
 				convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH), shellSize.x),
 				shellSize.y);
 	}
 
 	/**
 	 * @return the project
 	 */
 	public IProject getProject() {
 		if (project == null) {
 			project = (IProject) section.getPage().getEditor().getAdapter(
 					IProject.class);
 		}
 		return project;
 	}
 
 	/**
 	 * 
 	 */
 	protected void okPressed() {
 		propertyName = nameField.getText();
 		propertyClass = classField.getText();
 		valueType = valueTypeField.getText();
 
 		super.okPressed();
 	}
 
 	/**
 	 * @return the property class
 	 */
 	public String getPropertyClass() {
 		return propertyClass;
 	}
 
 	/**
 	 * @param attributeClass
 	 */
 	public void setAttributeClass(String attributeClass) {
 		this.propertyClass = attributeClass;
 	}
 
 	/**
 	 * @return the property name
 	 */
 	public String getPropertyName() {
 		return propertyName;
 	}
 
 	/**
 	 * @param attributeName
 	 */
 	public void setAttributeName(String attributeName) {
 		this.propertyName = attributeName;
 	}
 
 	/**
 	 * @return the value type
 	 */
 	public String getValueType() {
 		return valueType;
 	}
 
 	/**
 	 * @param defaultValue
 	 */
 	public void setValueType(String defaultValue) {
 		this.valueType = defaultValue;
 	}
 
 	/**
 	 * @param project
 	 */
 	public void setProject(IProject project) {
 		this.project = project;
 	}
 
 	/**
 	 * @return the value object
 	 */
 	public Object getValueObject() {
 		return valueObject;
 	}
 
 	/**
 	 * @param valueObject
 	 */
 	public void setValueObject(Object valueObject) {
 		this.valueObject = valueObject;
 	}
 
 }
